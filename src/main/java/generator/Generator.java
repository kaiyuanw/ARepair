package generator;

import static generator.etc.Contants.AMP;
import static generator.etc.Contants.CARET;
import static generator.etc.Contants.DOT;
import static generator.etc.Contants.MINUS;
import static generator.etc.Contants.NONE_EXPR;
import static generator.etc.Contants.PLUS;
import static generator.etc.Contants.STAR;
import static generator.etc.Contants.TILDE;
import static generator.util.Util.buildExpression;
import static generator.util.Util.createExprFromType;
import static generator.util.Util.isCommutative;
import static generator.util.Util.isStaticPruned;
import static generator.util.Util.isValidExpression;
import static parser.etc.Context.logger;
import static parser.etc.Names.HIDDEN_DIR_PATH;
import static parser.etc.Names.TEST_PREFIX;
import static parser.etc.Names.TMPT_FILE_PATH;
import static parser.util.AlloyUtil.mergeModelAndTests;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;
import generator.etc.BoundType;
import generator.fragment.Expression;
import generator.fragment.Fragment;
import generator.modulo.ModelNormalizer;
import generator.modulo.ModuloInputChecker;
import generator.modulo.TestCase;
import generator.opt.GeneratorOpt;
import generator.util.TypeAnalyzer;
import generator.util.TypeInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.Predicate;
import parser.ast.visitor.PrettyStringVisitor;
import parser.etc.Context;
import parser.util.AlloyUtil;
import parser.util.FileUtil;
import parser.util.Logger;

/**
 * This class implement static pruning + modulo input pruning.
 */
public class Generator {

  private Map<String, String> inheritanceHierarchy;
  private Map<Node, String> uid;
  private GeneratorOpt opt;
  private Map<Node, TypeInfo> nodeToType;
  private List<TestCase> tests;
  /**
   * Each scope should have its own checker.
   */
  private Map<List<TypeInfo>, ModuloInputChecker> cachedCheckers;
  private Map<List<TypeInfo>, Map<Integer, Map<Integer, List<Expression>>>> cachedExpressions;

  public Generator(TypeAnalyzer analyzer, GeneratorOpt opt, ModelUnit modelUnitWithTests) {
    this.inheritanceHierarchy = analyzer.getInheritanceHierarchy();
    this.uid = analyzer.getUid();
    this.opt = opt;
    this.nodeToType = analyzer.getNodeToType();
    FileUtil
        .writeText(modelUnitWithTests.accept(new ModelNormalizer(), null), TMPT_FILE_PATH, false);
    this.tests = initializeTests(new ModelUnit(null, AlloyUtil.compileAlloyModule(TMPT_FILE_PATH)));
    this.cachedCheckers = new LinkedHashMap<>();
    this.cachedExpressions = new LinkedHashMap<>();
  }

  private List<TestCase> initializeTests(ModelUnit modelUnitWithTests) {
    List<TestCase> testCases = new ArrayList<>();
    for (Predicate predicate : modelUnitWithTests.getPredDeclList()) {
      if (!predicate.getName().startsWith(TEST_PREFIX)) {
        continue;
      }
      // We create separate module for each test to avoid setting atoms each time we invoke evaluator.
      CompModule module = AlloyUtil.compileAlloyModule(TMPT_FILE_PATH);
      Expr invokeTest = CompUtil.parseOneExpression_fromString(module, predicate.getName() + "[]");
      Command runTestCommand = new Command(false, opt.getScope(), -1, -1, invokeTest);
      A4Solution sol = TranslateAlloyToKodkod
          .execute_commandFromBook(A4Reporter.NOP, module.getAllReachableSigs(), runTestCommand,
              opt.getOptions());
      if (!sol.satisfiable()) {
        // The test is unsatisfiable.  This should not happen for normalized model.
        throw new RuntimeException("Test " + predicate.getName() + " fails for normalized model.");
      }
      testCases.add(new TestCase(predicate.getName(), opt, nodeToType, uid, module, sol));
    }
    return testCases;
  }

  private Map<Integer, List<Expression>> initializeExpressionsByArity(int maxArity) {
    Map<Integer, List<Expression>> expressionsByArity = new LinkedHashMap<>();
    for (int arity = 1; arity <= maxArity; arity++) {
      expressionsByArity.putIfAbsent(arity, new ArrayList<>());
    }
    return expressionsByArity;
  }

  /**
   * Generate a map of type cost/depth -> arity -> expression.
   */
  public Map<Integer, Map<Integer, List<Expression>>> generateExpressions(GeneratorOpt opt,
      List<TypeInfo> basicTypes) {
    ModuloInputChecker checker;
    if (cachedCheckers.containsKey(basicTypes)) {
      checker = cachedCheckers.get(basicTypes);
    } else {
      checker = new ModuloInputChecker(tests);
      cachedCheckers.put(basicTypes, checker);
    }
    // cands: depth/cost -> arity -> candidate expressions.
    Map<Integer, Map<Integer, List<Expression>>> cands = new LinkedHashMap<>();
    // We do not cache based on basic expressions because we create expressions for each run.
    if (cachedExpressions.containsKey(basicTypes)) {
      cands = cachedExpressions.get(basicTypes);
    }
    int startDepthOrCost = 0;
    if (opt.boundOnCost()) {
      startDepthOrCost = 1;
    }
    int lastDepthOrCost = startDepthOrCost;
    if (cands.isEmpty()) {
      // Initialize cands with depth 0 or cost 1.
      cands.putIfAbsent(startDepthOrCost, initializeExpressionsByArity(opt.getMaxArity()));
      List<Expression> basicCands = basicTypes.stream()
          .map(typeInfo -> createExprFromType(typeInfo, uid))
          .collect(Collectors.toList());
      // If the arity of some basic relation is greater than the maximum arity,
      // then that relation is not used to generate expressions.
      for (Expression basicCand : basicCands) {
        int arity = basicCand.getArity();
        // Get relations with depth 0 or cost 1.
        Map<Integer, List<Expression>> expressionsByArity = cands.get(startDepthOrCost);
        // Ignore relations with arity greater than maximum arity.
        if (!expressionsByArity.containsKey(arity)) {
          continue;
        }
        expressionsByArity.get(arity).add(basicCand);
        if (opt.isModuloPruning()) {
          // Give Modulo checker the basic relations.
          checker.isEquivalent(basicCand);
        }
      }
      // Special relations like none, univ, iden, Int and String are added as part of the basic
      // expressions, so we do not add them again.  But we should add special relations with
      // arity > 1.
      for (int arity = 2; arity <= opt.getMaxArity(); arity++) {
        Expression basicCand = buildExpression(NONE_EXPR, arity, opt);
        cands.get(startDepthOrCost).get(arity).add(basicCand);
        // Probably univ->...->univ will never be used.
        // cands.get(startDepthOrCost).get(arity).add(buildExpression(UNIV_EXPR, arity, opt));
        if (opt.isModuloPruning()) {
          // Give Modulo checker the basic relations.
          checker.isEquivalent(basicCand);
        }
      }
    } else {
      lastDepthOrCost = Collections.max(cands.keySet());
    }
    // Iteratively generate expressions from cost/depth not explored to the maximum depth.
    // For a given depth, generate expressions from arity 1 to maximum arity.
    for (int depthOrCost = lastDepthOrCost + 1; depthOrCost <= opt.getMaxDepthOrCost();
        depthOrCost++) {
      // If expressions of the depth or cost were generated before, we do not regenerate them.
      if (cands.containsKey(depthOrCost)) {
        continue;
      }
      cands.putIfAbsent(depthOrCost, initializeExpressionsByArity(opt.getMaxArity()));
      Map<Integer, List<Expression>> newExpressionsByArity = cands.get(depthOrCost);
      // For depth k, we choose left cands and right cands from depth 0..k-1,
      // without duplicates to avoid redundant work.
      // For cost k, we choose left cands and right cands where
      // cost(left) + cost(op) + cost(right) = k.
      for (int leftDepthOrCost = startDepthOrCost; leftDepthOrCost < depthOrCost;
          leftDepthOrCost++) {
        for (int rightDepthOrCost = startDepthOrCost; rightDepthOrCost < depthOrCost;
            rightDepthOrCost++) {
          // If the combination is tried before, skip it.
          if ((opt.boundOnDepth() && leftDepthOrCost != depthOrCost - 1
              && rightDepthOrCost != depthOrCost - 1)
              || (opt.boundOnCost() && leftDepthOrCost + rightDepthOrCost >= depthOrCost)) {
            continue;
          }
          for (int arity = 1; arity <= opt.getMaxArity(); arity++) {
            List<Expression> newExpressions = new ArrayList<>();
            // Generate sig operations with same arity.
            // Prune based on commutativity at the depth level.
            if (leftDepthOrCost <= rightDepthOrCost) {
              addExprs(newExpressions, PLUS, depthOrCost, leftDepthOrCost,
                  cands.get(leftDepthOrCost).get(arity),
                  rightDepthOrCost, cands.get(rightDepthOrCost).get(arity), opt,
                  inheritanceHierarchy);
              addExprs(newExpressions, AMP, depthOrCost, leftDepthOrCost,
                  cands.get(leftDepthOrCost).get(arity),
                  rightDepthOrCost, cands.get(rightDepthOrCost).get(arity), opt,
                  inheritanceHierarchy);
            }
            addExprs(newExpressions, MINUS, depthOrCost, leftDepthOrCost,
                cands.get(leftDepthOrCost).get(arity),
                rightDepthOrCost, cands.get(rightDepthOrCost).get(arity), opt,
                inheritanceHierarchy);

            // Cross product of all appropriate arity
            // We ignore generating cross product for repair.

            // Relational composition of all arities.
            for (int leftArity = Math.max(1, arity + 2 - opt.getMaxArity());
                leftArity <= Math.min(arity + 1, opt.getMaxArity()); leftArity++) {
              int rightArity = arity + 2 - leftArity;
              addExprs(newExpressions, DOT, depthOrCost, leftDepthOrCost,
                  cands.get(leftDepthOrCost).get(leftArity), rightDepthOrCost,
                  cands.get(rightDepthOrCost).get(rightArity), opt, inheritanceHierarchy);
            }
            newExpressionsByArity.get(arity).addAll(newExpressions);
          }
        }
      }
      if (opt.getMaxArity() >= 2) {
        for (int subDepthOrCost = startDepthOrCost; subDepthOrCost < depthOrCost;
            subDepthOrCost++) {
          if (opt.boundOnDepth() && subDepthOrCost + 1 != depthOrCost) {
            continue;
          }
          // Special expressions for binary relations.
          addExprs(newExpressionsByArity.get(2), TILDE, depthOrCost, subDepthOrCost,
              cands.get(subDepthOrCost).get(2), opt);
          addExprs(newExpressionsByArity.get(2), STAR, depthOrCost, subDepthOrCost,
              cands.get(subDepthOrCost).get(2), opt);
          addExprs(newExpressionsByArity.get(2), CARET, depthOrCost, subDepthOrCost,
              cands.get(subDepthOrCost).get(2), opt);
        }
      }
      if (opt.isModuloPruning()) {
        // uniqueRelations contains semantically unique relations.
        Map<Integer, List<Expression>> uniqueExpressionsByArity = new LinkedHashMap<>();
        for (int arity = 1; arity <= opt.getMaxArity(); arity++) {
          List<Expression> uniqueExpressions = new ArrayList<>();
          // Only check a given arity if checkArity > 0.
          for (Expression expression : newExpressionsByArity.get(arity)) {
            // If the relation is semantically unique w.r.t. other relations,
            // then keep it.
            if (!checker.isEquivalent(expression)) {
              uniqueExpressions.add(expression);
            }
          }
          uniqueExpressionsByArity.put(arity, uniqueExpressions);
        }
        newExpressionsByArity = uniqueExpressionsByArity;
      }
      cands.put(depthOrCost, newExpressionsByArity);
    }
    cachedExpressions.put(basicTypes, cands);
    return cands;
  }

  /**
   * Generate expression for binary operators.  E.g. +,&,-,->,.. Note that leftOperand and
   * rightOperand may be at different depths.
   */
  private static void addExprs(List<Expression> relations, Fragment op, int depthOrCost,
      int leftDepthOrCost, List<Expression> leftOperands, int rightDepthOrCost,
      List<Expression> rightOperands, GeneratorOpt opt, Map<String, String> inheritanceMap) {
    int newExpressionCost = leftDepthOrCost + op.getCost() + rightDepthOrCost;
    if (opt.boundOnCost() && newExpressionCost != depthOrCost) {
      return;
    }
    // Prune based on commutativity at the expression level.
    int trick = isCommutative(op)
        && leftDepthOrCost == rightDepthOrCost ? 1 : 0;
    for (int i1 = 0; i1 < leftOperands.size() - trick; i1++) {
      Expression leftOperand = leftOperands.get(i1);
      int leftOpNum = leftOperand.getOpNum();
      // Stop generating expressions if number of operators in left operand
      // is greater than or equal to the maximum operator number.
      if (leftOpNum >= opt.getMaxOpNum()) {
        continue;
      }
      for (int i2 = i1 * trick + trick; i2 < rightOperands.size(); i2++) {
        Expression rightOperand = rightOperands.get(i2);
        int rightOpNum = rightOperand.getOpNum();
        // Stop generating expressions if number of operators in both left
        // operand and right operand is greater than or equal to the maximum
        // operator number.
        if (leftOpNum + rightOpNum >= opt.getMaxOpNum()) {
          continue;
        }
        if (isValidExpression(op, leftOperand, rightOperand, inheritanceMap)
            && !isStaticPruned(op, leftOperand, leftDepthOrCost, rightOperand, rightDepthOrCost,
            inheritanceMap)) {
          relations.add(
              buildExpression(newExpressionCost, op, leftOperand, rightOperand, inheritanceMap));
        }
      }
    }
  }

  /**
   * Generate expression for unary operators.  E.g. ~,^,*.
   */
  private static void addExprs(List<Expression> relations, Fragment op, int depthOrCost,
      int subDepthOrCost, List<Expression> operands, GeneratorOpt opt) {
    int newExpressionCost = subDepthOrCost + op.getCost();
    if (opt.boundOnCost() && newExpressionCost != depthOrCost) {
      return;
    }
    for (Expression operand : operands) {
      int opNum = operand.getOpNum();
      if (opNum >= opt.getMaxOpNum()) {
        continue;
      }
      if (isValidExpression(op, operand) && !isStaticPruned(op, operand)) {
        relations.add(buildExpression(newExpressionCost, op, operand));
      }
    }
  }

  public List<TestCase> getTests() {
    return tests;
  }

  public static void printGeneratedExpressionsByDepth(GeneratorOpt opt,
      Map<Integer, Map<Integer, List<Expression>>> cands) {
    String depthOrCost = opt.getBoundType().name();
    for (Map.Entry<Integer, Map<Integer, List<Expression>>> entry1 : cands.entrySet()) {
      logger.info(depthOrCost + ": " + entry1.getKey());
      for (Map.Entry<Integer, List<Expression>> entry2 : entry1.getValue().entrySet()) {
        logger.info("  Arity: " + entry2.getKey());
        entry2.getValue().sort(Comparator.comparing(Fragment::getValue));
        logger.info("  Expressions (" + entry2.getValue().size() + "): " + entry2.getValue());
      }
    }
  }
}
