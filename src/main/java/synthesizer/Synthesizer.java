package synthesizer;

import static parser.etc.Context.logger;
import static parser.etc.Context.timer;
import static parser.etc.Names.DOLLAR;
import static parser.etc.Names.TEST_PREFIX;
import static parser.etc.Names.UNDERSCORE;
import static parser.util.AlloyUtil.getFirstNonNOOPChild;
import static patcher.etc.Constants.FIX_FILE_PATH;
import static patcher.etc.Constants.MAX_DEPTH_OR_COST;
import static patcher.etc.Constants.getCostByDepth;
import static patcher.etc.Constants.getCostByHeight;
import static synthesizer.util.Cache.PARTITION_CACHE;
import static synthesizer.util.Util.collectInverseCallGraph;
import static synthesizer.util.Util.computeExplorationOrders;
import static synthesizer.util.Util.createHoles;
import static synthesizer.util.Util.getNodeId;
import static synthesizer.util.Util.getParagraphFromName;
import static synthesizer.util.Util.isSimilarType;
import static synthesizer.util.Util.product;
import static synthesizer.util.Util.toFormulaString;
import static synthesizer.visitor.FactCollector.FIELD_MULT;
import static synthesizer.visitor.FactCollector.SIG_FACT;
import static synthesizer.visitor.FactCollector.SIG_MULT;

import com.google.common.collect.ImmutableList;
import generator.Generator;
import generator.fragment.Expression;
import generator.fragment.Fragment;
import generator.fragment.Type;
import generator.modulo.TestCase;
import generator.opt.GeneratorOpt;
import generator.util.TypeAnalyzer;
import generator.util.TypeInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import parser.ast.nodes.Body;
import parser.ast.nodes.Cmd;
import parser.ast.nodes.FieldDecl;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.Paragraph;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.VarExpr;
import parser.etc.Pair;
import parser.util.FileUtil;
import patcher.opt.PatcherOpt;
import synthesizer.hole.ExprHole;
import synthesizer.hole.Hole;
import synthesizer.util.DepthInfo;
import synthesizer.util.TestInfo;
import synthesizer.visitor.CacheMapCreator;
import synthesizer.visitor.EvaluationCache;
import synthesizer.visitor.FactCollector;
import synthesizer.visitor.Inliner;
import synthesizer.visitor.PrettyStringWithHoleHandler;

public class Synthesizer {

  private ModelUnit modelToFix;
  private GeneratorOpt opt;
  private Generator generator;
  private Map<String, String> inheritanceHierarchy;
  private Map<Node, List<TypeInfo>> nodeToExprTypes;
  private Map<Node, TypeInfo> nodeToType;
  /**
   * Maps callee's name to callers' names.  The callers could be facts or test predicates.
   */
  private Map<String, List<String>> calleeToCallers;
  /**
   * Fact name to the fact formula, including new fact for signature/field multiplicity.
   */
  private Map<String, Node> name2fact;
  /**
   * Test name to the test predicate body.
   */
  private Map<String, Body> name2test;
  private Inliner inliner;
  /**
   * Maps test name to its information.
   */
  private Map<String, TestInfo> testInfos;

  /**
   * Number of combinations tried for each depth.
   */
  private int tried;

  /**
   * For caching purpose.
   */
  private boolean enableCaching;
  private Map<Node, String> uid;

  public Synthesizer(ModelUnit modelToFix, ModelUnit modelWithTests, GeneratorOpt opt,
      Generator generator, TypeAnalyzer analyzer) {
    this.modelToFix = modelToFix;
    this.opt = opt;
    this.generator = generator;
    this.inheritanceHierarchy = analyzer.getInheritanceHierarchy();
    this.nodeToExprTypes = analyzer.getNodeToExprTypes();
    this.nodeToType = analyzer.getNodeToType();
    // Establish mappings between callees and callers, including the test predicates.
    this.calleeToCallers = collectInverseCallGraph(modelWithTests);
    // Collect implicit/explicit facts.
    FactCollector factCollector = new FactCollector();
    modelToFix.accept(factCollector, null);
    this.name2fact = factCollector.getFacts();
    this.name2test = modelWithTests.getPredDeclList().stream()
        .filter(predicate -> predicate.getName().startsWith(TEST_PREFIX))
        .collect(Collectors.toMap(Predicate::getName, Paragraph::getBody));
    this.inliner = new Inliner(modelToFix, new HashMap<>());
    Map<String, Cmd> name2cmd = modelWithTests.getRunCmdList().stream()
        .filter(cmd -> cmd.getName().startsWith(TEST_PREFIX))
        .collect(Collectors.toMap(Cmd::getName, cmd -> cmd));
    this.testInfos = collectTestInfo(name2cmd, generator.getTests(), name2fact);
    this.tried = 0;
    this.uid = analyzer.getUid();
    this.enableCaching = opt.enableCaching();
  }

  private Map<String, TestInfo> collectTestInfo(Map<String, Cmd> name2cmd, List<TestCase> tests,
      Map<String, Node> name2fact) {
    Map<String, TestInfo> testInfos = new LinkedHashMap<>();
    tests.forEach(test -> {
      String testName = test.getName();
      // Collect initial results for test dependent formulas, including facts and the test predicate.
      Map<String, String> name2formula = new LinkedHashMap<>();
      name2fact.forEach((name, fact) -> name2formula.put(name, toFormulaString(fact, inliner)));
      name2formula.put(testName, toFormulaString(name2test.get(testName), inliner));
      testInfos.put(testName,
          new TestInfo(test, name2formula, name2cmd.get(test.getName()).getExpect() == 1));
    });
    return testInfos;
  }

  /**
   * Returns the fact/test names that are affected by the predicate/function/fact/sig/field that we
   * synthesized.  Note the name could refer to predicate/function/fact/sig/field.
   *
   * @return the fact names and test names pair.
   */
  private Pair<List<String>, List<String>> findAllAffectedFactsAndTests(List<String> names) {
    List<String> facts = new ArrayList<>();
    List<String> tests = new ArrayList<>();
    Queue<String> toVisit = new LinkedList<>();
    names.forEach(toVisit::offer);
    // The call graph is acyclic in Alloy, so this should terminate.
    while (!toVisit.isEmpty()) {
      String visitingName = toVisit.poll();
      if (name2fact.containsKey(visitingName)) {
        facts.add(visitingName);
      }
      if (visitingName.startsWith(TEST_PREFIX)) {
        tests.add(visitingName);
      }
      if (calleeToCallers.containsKey(visitingName)) {
        calleeToCallers.get(visitingName).forEach(toVisit::offer);
      }
    }
    return Pair.of(facts, tests);
  }

  /**
   * Find the names of the affected predicate/function/fact/sig/field.  Since a field declaration
   * may have multiple field names (but it seems Alloy only has one field name per field
   * declaration), we decide to return a list.  Otherwise, this method should return a single name.
   */
  public static List<String> findAffectedParagraphNames(Node faultyNode) {
    Node visitingNode = faultyNode;
    while (visitingNode != null) {
      // Because we never mutate sig facts or assertions, so the faulty node should never be inside
      // a sig fact or an assertion.
      if (visitingNode.getParent() instanceof SigDecl) {
        SigDecl sigDecl = (SigDecl) visitingNode.getParent();
        if (sigDecl.hasSigFact() && sigDecl.getSigFact() == visitingNode) {
          return ImmutableList.of(sigDecl.getName() + SIG_FACT);
        }
      } else if (visitingNode instanceof SigDecl) {
        return ImmutableList.of(((SigDecl) visitingNode).getName() + SIG_MULT);
      } else if (visitingNode instanceof FieldDecl) {
        return ((FieldDecl) visitingNode).getNames().stream()
            .map(name -> name + FIELD_MULT)
            .collect(Collectors.toList());
      } else if (visitingNode instanceof Paragraph) {
        return ImmutableList
            .of((((Paragraph) visitingNode).getName()).replaceAll("\\" + DOLLAR, UNDERSCORE));
      }
      visitingNode = visitingNode.getParent();
    }
    return null;
  }

  public List<Expression> getExpressionsFromType(
      Map<Integer, Map<Integer, List<Expression>>> expressions, List<Type> types) {
    List<Expression> expressionsGivenType = new ArrayList<>();
    expressions.forEach((cost, expressionsByArity) -> {
      expressionsByArity.get(types.size()).forEach(expression -> {
        if (isSimilarType(types, expression.getTypes(), inheritanceHierarchy)) {
          expressionsGivenType.add(expression);
        }
      });
    });
    return expressionsGivenType;
  }

  /**
   * Returns true if some failing tests pass and no passing test fails.  Not necessary that all
   * failing tests pass.
   */
  public boolean synthesize(Node faultyNode, DepthInfo depthInfo, PatcherOpt patcherOpt) {
    // Find affected paragraph names.
    List<String> paraNames = findAffectedParagraphNames(faultyNode);
    assert paraNames != null;
    logger.debug("Affected paragraph names: " + paraNames);
    // Find affected facts and test predicates.
    Pair<List<String>, List<String>> pair = findAllAffectedFactsAndTests(paraNames);
    List<String> affectedFacts = pair.a;
    logger.debug("Affected facts: " + affectedFacts);
    Set<String> affectedTests = new LinkedHashSet<>(pair.b);
    logger.debug("Affected tests (Size " + affectedTests.size() + "): " + affectedTests);
    Map<String, TestInfo> affectedTestInfos;
    if (!affectedFacts.isEmpty()) {
      // If any fact is affected, then all test infos must be checked.
      affectedTestInfos = new LinkedHashMap<>(testInfos);
    } else {
      affectedTestInfos = new LinkedHashMap<>();
      affectedTests.forEach(name -> affectedTestInfos.put(name, testInfos.get(name)));
    }
    // First group nodes by depth.  Ignore NOOP.
    List<List<Node>> nodesByDepth = groupByDepth(faultyNode);
    int faultyNodeId = getNodeId(modelToFix, faultyNode);
    if (faultyNodeId != depthInfo.getLastFaultyNodeId() ||
        faultyNode.getClass() != depthInfo.getLastFaultyNodeType()) {
      depthInfo.setLastVisitedDepth(Integer.MAX_VALUE);
      depthInfo.setLastFaultyNodeId(faultyNodeId);
      depthInfo.setLastFaultyNodeType(faultyNode.getClass());
    }
    // Get the declaring paragraph of the faulty node.  This is used for caching.
    // Enable caching.
    Paragraph declaringPara = null;
    Map<Node, List<VarExpr>> nodeToVariables = null;
    if (enableCaching) {
      declaringPara = getParagraphFromName(modelToFix, paraNames.get(0));
      nodeToVariables = new LinkedHashMap<>();
      for (Map.Entry<Node, List<TypeInfo>> entry : nodeToExprTypes.entrySet()) {
        Node node = entry.getKey();
        List<TypeInfo> types = entry.getValue();
        List<VarExpr> variables = new ArrayList<>();
        types.forEach(typeInfo -> {
          if (typeInfo.getNode() instanceof VarExpr) {
            variables.add((VarExpr) typeInfo.getNode());
          }
        });
        variables.sort(Comparator.comparing(v -> uid.get(v)));
        nodeToVariables.put(node, variables);
      }
    }
    // We iteratively create holes from bottom to top, from the deepest nodes to the root node.
    for (int i = nodesByDepth.size() - 1; i >= 0; i--) {
      // We do not try holes deeper than what we previously explored.
      if (i >= depthInfo.getLastVisitedDepth()) {
        continue;
      }
      depthInfo.setLastVisitedDepth(i);
      int currentCost = getCostByDepth(i, nodesByDepth.size() - 1, patcherOpt.getMinimumCost());
      // Create holes and enumerate for solutions.
      List<Node> nodesToDigHoles = nodesByDepth.get(i);
      Map<Node, Hole> node2hole = new LinkedHashMap<>();
      nodesToDigHoles.forEach(node -> node2hole.putAll(createHoles(node)));
      List<Pair<Hole, Integer>> holeInfos = new ArrayList<>();
      timer.record();
      node2hole.forEach((node, hole) -> {
        if (hole instanceof ExprHole) {
//          opt.setMaxDepthOrCost(costMap.get(node));
//          opt.setMaxDepthOrCost(0);
          opt.setMaxDepthOrCost(currentCost);
          Map<Integer, Map<Integer, List<Expression>>> cands = generator
              .generateExpressions(opt, nodeToExprTypes.get(node));
          ((ExprHole) hole)
              .setFragments(getExpressionsFromType(cands, nodeToType.get(node).getTypes()));
          // Reset back to MAX_DEPTH_OR_COST for now.
          opt.setMaxDepthOrCost(MAX_DEPTH_OR_COST);
        }
        holeInfos.add(Pair.of(hole, hole.getFragments().size()));
        logger.debug("Hole " + node.accept(opt.getPSV(), null));
        logger.debug("Size: " + hole.getFragments().size());
        logger.debug("Fragments: " + hole.getFragments());
      });
      timer.show("Expression generation time");
      // Report search space for all holes.
      long searchSpace = product(holeInfos.stream()
          .map(holeInfo -> holeInfo.b)
          .collect(Collectors.toList()));
      logger.info("Search space: " + searchSpace);
      // Create new inliner which handles holes.
      inliner = new Inliner(modelToFix, node2hole);
      // Enable caching.
      if (enableCaching) {
        CacheMapCreator cacheMapCreator = new CacheMapCreator(modelToFix, node2hole.keySet());
        logger.debug("Number of hierarchical cache points: " + cacheMapCreator.getH2L().size());
        // Each time we create new holes, we should update the evaluation cache because the location
        // of the holes may be changed.
        for (TestInfo testInfo : affectedTestInfos.values()) {
          testInfo.setParagraphToSynthesize(declaringPara);
          testInfo.setEvaluationCache(
              new EvaluationCache(node2hole, cacheMapCreator, testInfo.getTest(), nodeToVariables,
                  inliner));
        }
      }
      switch (patcherOpt.getSearchStrategy()) {
        case ALL_COMBINATIONS:
          // Reset the number of combinations tried per depth.
          tried = 0;
          // Compute the percentage of candidate fragments for each hole to try.  We do not
          // partition operator holes so we should first ignore them when computing the percentage
          // of expression hole search space to try.
          long operatorHoleSpace = 1;
          List<Integer> expressionHoleSizes = new ArrayList<>();
          for (Pair<Hole, Integer> holeInfo : holeInfos) {
            if (holeInfo.a instanceof ExprHole) {
              expressionHoleSizes.add(holeInfo.b);
            } else {
              operatorHoleSpace *= holeInfo.b;
            }
          }
          double kPercent;
          if (expressionHoleSizes.size() == 0) {
            kPercent = 0.0;
          } else {
            // s1*k * s2*k * ... sn*k = MAX_TRY_NUM_PER_DEPTH.  k <= 1.0.
            kPercent = Math.min(1.0,
                Math.pow(patcherOpt.getMaxTryPerDepth() * 1.0 / operatorHoleSpace,
                    1.0 / expressionHoleSizes.size()));
          }
          logger.debug(
              "Explore first " + kPercent + " of total search space for each expression hole.");
          // Compute the maximum number of expression candidate fragments to explore for each
          // expression hole.
          List<Integer> expressionHoleSizesToExplore = expressionHoleSizes.stream()
              .map(size -> (int) Math.round(size * kPercent))
              .collect(Collectors.toList());
          // Try to explore simple expression combinations first, then explore more complex
          // expression combinations.
          List<Integer> partitions = expressionHoleSizesToExplore.stream()
              .map(size -> Math.min(size, patcherOpt.getMaxPartitionNum()))
              .collect(Collectors.toList());
          List<List<Integer>> explorationOrders;
          if (PARTITION_CACHE.containsKey(partitions)) {
            explorationOrders = PARTITION_CACHE.get(partitions);
          } else {
            explorationOrders = computeExplorationOrders(partitions,
                patcherOpt.getMaxPartitionNum());
            PARTITION_CACHE.put(partitions, explorationOrders);
          }
          logger.debug("Enumerating:");
          for (List<Integer> explorationOrder : explorationOrders) {
            logger.debug("Exploration Order: " + explorationOrder);
            if (findSolutionWithAllCombinations(holeInfos, explorationOrder, partitions,
                expressionHoleSizesToExplore, 0, 0, affectedFacts, affectedTests,
                affectedTestInfos)) {
              FileUtil.writeText(
                  modelToFix.accept(new PrettyStringWithHoleHandler(node2hole), null),
                  FIX_FILE_PATH, false);
              logger.info("Fix succeeded, number of combinations tried: " + tried);
              return true;
            }
          }
          logger.info("Fix failed, number of combinations tried: " + tried);
          break;
        case BASE_CHOICE:
          // Reset the number of combinations tried per depth.
          tried = 0;
          logger.debug("Enumerating:");
          if (findSolutionWithBasicChoice(node2hole, affectedFacts, affectedTests,
              affectedTestInfos, patcherOpt)) {
            FileUtil.writeText(
                modelToFix.accept(new PrettyStringWithHoleHandler(node2hole), null),
                FIX_FILE_PATH, false);
            logger.info("Fix succeeded, number of combinations tried: " + tried);
            return true;
          }
          logger.info("Fix failed, number of combinations tried: " + tried);
          break;
        default:
          throw new RuntimeException(
              "Unsupported search strategy: " + patcherOpt.getSearchStrategy());
      }
    }
    return false;
  }

  /**
   * This method enumerate values of holes using the basic choice search strategy.  The method
   * activate one hole at a time and it enumerate candidate values only for the active hole.  When
   * the candidate value of the active hole changes, the method runs the affected tests and record
   * the number of tests from failing to passing and from passing to failing.  If the candidate
   * value makes some tests from failing to passing but does not make any test from passing to
   * failing, then we treat that candidate value as a potential fix.  For active hole, we find the
   * candidate value that makes the most tests from failing to passing and does not make any test
   * from passing to failing.  That candidate value is set as the final value of the hole.  Then,
   * the method explores candidate values of the next hole.  After the method, we will set a
   * candidate value for each hole and return true.  If no potential fix exists, then we just return
   * false.
   *
   * @param node2hole Maps each node with the corresponding holes.  If a node is not related to a
   * hole, then it is not in the map.
   * @param affectedFacts The names of the implicit/explicit facts whose satisfiability may change
   * because of the synthesized code.
   * @param affectedTests The test predicates whose result may vary because of the synthesized code.
   * Note that this parameter only contains test predicate names that transitively invokes
   * predicates/functions we are synthesizing.
   * @param affectedTestInfos Maps the test name to the test information which can be used to invoke
   * evaluators.  Note that this parameter contains the actually tests to be invoked.
   */
  private boolean findSolutionWithBasicChoice(Map<Node, Hole> node2hole, List<String> affectedFacts,
      Set<String> affectedTests, Map<String, TestInfo> affectedTestInfos, PatcherOpt patcherOpt) {
    // Deactivate all holes.
    node2hole.forEach((node, hole) -> hole.setActive(false));
    for (Map.Entry<Node, Hole> entry : node2hole.entrySet()) {
      Hole hole = entry.getValue();
      // Activate the current hole.
      hole.setActive(true);
      int maxFailToPass = 0;
      Fragment bestFragment = null;
      for (int i = 0; i < Math.min(hole.getFragments().size(), patcherOpt.getMaxTryPerHole());
          i++) {
        // Increase the number of combinations tried.
        tried += 1;
        Fragment fragment = hole.getFragments().get(i);
        hole.setValue(fragment);
        logger.debug("Hole values: " + node2hole.entrySet().stream().map(e -> {
          if (e.getValue().isActive()) {
            return e.getValue().toString();
          }
          return e.getKey().accept(opt.getPSV(), null);
        }).collect(Collectors.toList()));
        int failToPass = 0;
        boolean passToFail = false;
        boolean hasEvalError = false;
        // Create name to fact string mapping.
        Map<String, String> nameToFactString = new LinkedHashMap<>();
        affectedFacts.forEach(
            name -> nameToFactString.put(name, toFormulaString(name2fact.get(name), inliner)));
        for (Map.Entry<String, TestInfo> testInfoEntry : affectedTestInfos.entrySet()) {
          String testName = testInfoEntry.getKey();
          TestInfo testInfo = testInfoEntry.getValue();
          boolean originalActualSat = testInfo.isActualSat();
          boolean beforeFailed = testInfo.isFailed();
          Map<String, String> name2formula = new LinkedHashMap<>(nameToFactString);
          if (affectedTests.contains(testName)) {
            name2formula.put(testName, toFormulaString(name2test.get(testName), inliner));
          }
          try {
            testInfo.evalFormulasFast(name2formula);
          } catch (Exception e) {
            hasEvalError = true;
            break;
          }
          boolean afterFailed = testInfo.isFailed();
          // Revert to the original satisfiability.
          testInfo.setActualSat(originalActualSat);
          if (beforeFailed && !afterFailed) {
            failToPass += 1;
            logger.debug("Test from fail to pass: " + testName);
          }
          if (!beforeFailed && afterFailed) {
            logger.debug("Test from pass to fail: " + testName);
            passToFail = true;
            break;
          }
        }
        if (hasEvalError) {
          logger.debug("Evaluation error when hole is " + fragment);
          continue;
        }
        if (!passToFail) {
          logger.debug("Hole " + i + ", Fragment: " + fragment + ", FailToPass: " + failToPass);
          if (maxFailToPass < failToPass) {
            maxFailToPass = failToPass;
            bestFragment = fragment;
          }
        }
      }
      // If we find some fragment that makes tests from fail to pass but not from pass to fail.
      if (maxFailToPass > 0) {
        hole.setValue(bestFragment);
        logger.debug("Set best fragment for hole: " + bestFragment.getValue());
        affectedTestInfos.forEach((testName, testInfo) -> {
          Map<String, String> name2formula = new LinkedHashMap<>();
          affectedFacts.forEach(
              name -> name2formula.put(name, toFormulaString(name2fact.get(name), inliner)));
          if (affectedTests.contains(testName)) {
            name2formula.put(testName, toFormulaString(name2test.get(testName), inliner));
          }
          // Save the test result for the best fragment we find for this hole.
          testInfo.evalFormulas(name2formula);
        });
        // We do not need to deactivate the hole if we find some good fragment.
      } else {
        // Deactivate the current hole if no fragment makes tests from fail to pass but not from
        // pass to fail.  This means we use the original value of the node.
        hole.setActive(false);
      }
    }
    // If any of the hole is active, then that means we find a partial fix.
    return node2hole.entrySet().stream().map(Map.Entry::getValue).anyMatch(Hole::isActive);
  }

  /**
   * Search a solution by trying all combinations of hole fragments.  Suppose there are H holes and
   * each hole has N fragments, then we need to explore O(N^H) combinations to find a solution. To
   * speedup searching, we partition the fragments of each holes into k parts and prioritize
   * combinations with simpler fragments.  Note that the current implementation is a greedy
   * approach, which means if a potential fix is found, then the algorithm terminates.
   */
  private boolean findSolutionWithAllCombinations(List<Pair<Hole, Integer>> holeInfos,
      List<Integer> explorationOrder, List<Integer> partitions,
      List<Integer> expressionHoleSizesToExplore, int start, int partitionIdx,
      List<String> affectedFacts, Set<String> affectedTests,
      Map<String, TestInfo> affectedTestInfos) {
    if (start == holeInfos.size()) {
      // Increase the number of combinations tried.
      tried += 1;
      logger.debug("Hole values: " + holeInfos.stream()
          .map(holeInfo -> holeInfo.a)
          .collect(Collectors.toList()));
      int failToPass = 0;
      for (Map.Entry<String, TestInfo> entry : affectedTestInfos.entrySet()) {
        String testName = entry.getKey();
        TestInfo testInfo = entry.getValue();
        boolean originalActualSat = testInfo.isActualSat();
        boolean beforeFailed = testInfo.isFailed();
        Map<String, String> name2formula = new LinkedHashMap<>();
        affectedFacts
            .forEach(name -> name2formula.put(name, toFormulaString(name2fact.get(name), inliner)));
        if (affectedTests.contains(testName)) {
          name2formula.put(testName, toFormulaString(name2test.get(testName), inliner));
        }
        try {
          testInfo.evalFormulasFast(name2formula);
        } catch (Exception e) {
          // If evaluating formulas result in errors, return false.
          logger.debug("Evaluation error.");
          return false;
        }
        boolean afterFailed = testInfo.isFailed();
        // Revert to the original satisfiability.
        testInfo.setActualSat(originalActualSat);
        if (beforeFailed && !afterFailed) {
          failToPass += 1;
          logger.debug("Test from fail to pass: " + testName);
        }
        if (!beforeFailed && afterFailed) {
          logger.debug("Test from pass to fail: " + testName);
          return false;
        }
      }
      return failToPass > 0;
    }
    Hole hole = holeInfos.get(start).a;
    int from, to, nextPartitionIdx;
    if (hole instanceof ExprHole) {
      int size = expressionHoleSizesToExplore.get(partitionIdx);
      from = explorationOrder.get(partitionIdx) * size / partitions.get(partitionIdx);
      to = (explorationOrder.get(partitionIdx) + 1) * size / partitions.get(partitionIdx);
      nextPartitionIdx = partitionIdx + 1;
    } else {
      from = 0;
      to = hole.getFragments().size();
      nextPartitionIdx = partitionIdx;
    }
    for (int i = from; i < to; i++) {
      hole.setValue(hole.getFragments().get(i));
      if (findSolutionWithAllCombinations(holeInfos, explorationOrder, partitions,
          expressionHoleSizesToExplore, start + 1, nextPartitionIdx, affectedFacts, affectedTests,
          affectedTestInfos)) {
        return true;
      }
    }
    return false;
  }

  private List<List<Node>> groupByDepth(Node node) {
    List<List<Node>> nodesByDepth = new ArrayList<>();
    Queue<Node> q = new LinkedList<>();
    q.offer(node);
    while (!q.isEmpty()) {
      int size = q.size();
      List<Node> level = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        Node n = getFirstNonNOOPChild(q.poll());
        level.add(n);
        for (Node child : n.getChildren()) {
          q.offer(child);
        }
      }
      nodesByDepth.add(level);
    }
    return nodesByDepth;
  }

  /**
   * Collect the node to cost mapping.  Nodes with lower heights are assigned with smaller cost
   * during synthesis.  Note that here we treat non-expression node the same as expression node.
   * TODO(kaiyuanw): We may not want to increase the height if the node is non-expression node.
   */
  private int collectNodeCost(Node node, Map<Node, Integer> costs, int lowestCost) {
    Node n = getFirstNonNOOPChild(node);
    if (n.getChildren().isEmpty()) {
      costs.putIfAbsent(n, getCostByHeight(0, lowestCost));
      return 0;
    }
    int height = -1;
    for (Node child : n.getChildren()) {
      int childHeight = collectNodeCost(child, costs, lowestCost);
      height = Math.max(height, childHeight + 1);
    }
    costs.putIfAbsent(n, getCostByHeight(height, lowestCost));
    return height;
  }
}
