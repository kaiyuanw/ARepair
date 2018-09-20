package patcher;

import static clean.ModelSimplifier.simplify;
import static parser.etc.Context.logger;
import static parser.etc.Context.timer;
import static parser.util.AlloyUtil.countDescendantNum;
import static parser.util.AlloyUtil.mergeModelAndTests;
import static patcher.etc.Constants.BOUND_TYPE;
import static patcher.etc.Constants.CLI_USAGE_DESCRIPTION_WIDTH;
import static patcher.etc.Constants.ENABLE_CACHE;
import static patcher.etc.Constants.FIX_FILE_PATH;
import static patcher.etc.Constants.MAX_ARITY;
import static patcher.etc.Constants.MAX_DEPTH_OR_COST;
import static patcher.etc.Constants.MAX_OP_NUM;
import static patcher.etc.Constants.MAX_TRY_NUM_PER_DEPTH;
import static patcher.etc.Constants.MAX_TRY_PER_HOLE;
import static patcher.etc.Constants.MINIMUM_COST;
import static patcher.etc.Constants.MODEL_PATH;
import static patcher.etc.Constants.PARTITION_NUM;
import static patcher.etc.Constants.SCOPE;
import static patcher.etc.Constants.SEARCH_STRATEGY;
import static patcher.etc.Constants.SUSPICIOUSNESS_THRESHOLD;
import static patcher.etc.Constants.TEST_PATH;
import static patcher.etc.SearchStrategy.ALL_COMBINATIONS;

import alloyfl.coverage.util.TestResult;
import alloyfl.coverage.util.TestRunner;
import alloyfl.hybrid.visitor.DescendantCollector;
import alloyfl.mutation.util.ScoreInfo;
import edu.mit.csail.sdg.parser.CompModule;
import fl.FaultLocator;
import fl.MutationImpact;
import generator.Generator;
import generator.opt.GeneratorOpt;
import generator.util.TypeAnalyzer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.util.AlloyUtil;
import parser.util.FileUtil;
import patcher.etc.RelevantTestResults;
import patcher.etc.SearchStrategy;
import patcher.opt.PatcherOpt;
import synthesizer.Synthesizer;
import synthesizer.util.DepthInfo;

public class Patcher {

  public static void patch(PatcherOpt opt) {
    CompModule modelModule = AlloyUtil.compileAlloyModule(opt.getModelPath());
    assert modelModule != null;
    ModelUnit modelUnit = new ModelUnit(null, modelModule);
    FileUtil.writeText(modelUnit.accept(opt.getPSV(), null), FIX_FILE_PATH, false);
    logger.info("Original model:");
    System.out.println(FileUtil.readText(FIX_FILE_PATH));
    logger.info("==========");
    // Keep track of the depths visited by the synthesizer before.  We do not want to
    // synthesize expressions from the lowest depth every time we fixed some tests.
    DepthInfo depthInfo = new DepthInfo();
    int iteration = 1; // Debugging
    // Assume the model can be fixed.
    boolean canFix = true;
    while (canFix) {
      logger.info("Iteration " + iteration++ + ":");
      CompModule moduleToFix = AlloyUtil.compileAlloyModule(FIX_FILE_PATH);
      ModelUnit modelToFix = new ModelUnit(null, moduleToFix);
      // The test runner saves the model with tests into TMPT_FILE_PATH.
      List<TestResult> testResults = TestRunner
          .runTests(modelToFix, opt.getTestSuitePath(), opt.getPSV(), opt.getOptions());
      List<Boolean> testBooleanResults = testResults
          .stream()
          // True means the test passes.
          .map(testResult -> !testResult.isFailed())
          .collect(Collectors.toList());
      if (testBooleanResults.stream().allMatch(result -> result)) {
        break;
      }
      canFix = false;
      Set<Node> nodesCoveredByFailingTests = getNodesCoveredByFailingTests(modelToFix, testResults);
      FaultLocator faultLocator = new FaultLocator(opt, testBooleanResults,
          nodesCoveredByFailingTests);
      timer.record();
      modelToFix.accept(faultLocator, null);
      Map<Node, Integer> descNum = countDescendantNum(modelToFix);
      List<Node> rankedNodes = faultLocator.rankNode(descNum).stream()
          .map(ScoreInfo::getNode)
          .collect(Collectors.toList());
      timer.show("Fault localization time");
      if (rankedNodes.isEmpty()) {
        // Cannot find any location to fix.
        break;
      }
      Node mostSuspiciousNode = rankedNodes.get(0);
      MutationImpact mostSuspiciousImpact = faultLocator.getMutationImpact(mostSuspiciousNode);
      // We prioritize mutations that make some failing test pass and no passing test fails.
      // If such mutation does not exist, then we use the most suspicious mutation.
      if (mostSuspiciousImpact.getFailToPass() > 0 && mostSuspiciousImpact.getPassToFail() == 0
          || mostSuspiciousImpact.getScore() > SUSPICIOUSNESS_THRESHOLD) {
        logger.debug("Most suspicious node: " + mostSuspiciousNode.accept(opt.getPSV(), null));
        logger.debug(
            "Score: " + mostSuspiciousImpact.getScore() + ", FailToPass: " + mostSuspiciousImpact
                .getFailToPass() + ", PassToFail: " + mostSuspiciousImpact.getPassToFail());
        // Mutation can partially fix the bug.
        FileUtil.writeText(mostSuspiciousImpact.getMutant(), FIX_FILE_PATH, false);
        logger.info("Fixed by mutation:");
        System.out.println(FileUtil.readText(FIX_FILE_PATH));
        logger.info("==========");
        canFix = true;
      } else { // Mutation cannot fix the bug and we need to synthesize the components.
        GeneratorOpt generatorOpt = new GeneratorOpt(BOUND_TYPE, MAX_DEPTH_OR_COST, MAX_ARITY,
            MAX_OP_NUM, opt.getScope(), opt.enableCaching());
        TypeAnalyzer analyzer = new TypeAnalyzer(modelToFix);
        // ModuloInputChecker needs model with tests.
        CompModule moduleWithTests = mergeModelAndTests(modelUnit, opt.getTestSuitePath(),
            opt.getPSV());
        ModelUnit modelUnitWithTests = new ModelUnit(null, moduleWithTests);
        Generator generator = new Generator(analyzer, generatorOpt, modelUnitWithTests);
        logger.debug("Test num: " + generator.getTests().size());
        Synthesizer synthesizer = new Synthesizer(modelToFix, modelUnitWithTests, generatorOpt,
            generator, analyzer);
        for (int i = 0; i < rankedNodes.size(); i++) {
          Node rankedNode = rankedNodes.get(i);
          logger.debug("Rank Node " + i + ":");
          logger.debug(rankedNode.accept(opt.getPSV(), null));
          if (synthesizer.synthesize(rankedNode, depthInfo, opt)) {
            logger.info("Fixed by synthesizer:");
            System.out.println(FileUtil.readText(FIX_FILE_PATH));
            logger.info("==========");
            canFix = true;
            break;
          }
        }
      }
    }
    if (canFix) {
      logger.info("All tests pass.");
    } else {
      logger.info("Cannot fix the model.");
    }
    logger.info("Raw patched model:");
    System.out.println(FileUtil.readText(FIX_FILE_PATH));
    logger.info("After simplification:");
    String simplifiedModel = simplify(
        new ModelUnit(null, AlloyUtil.compileAlloyModule(FIX_FILE_PATH)));
    FileUtil.writeText(simplifiedModel, FIX_FILE_PATH, false);
    System.out.println(simplifiedModel);
  }

  private static Set<Node> getNodesCoveredByFailingTests(ModelUnit modelUnit,
      List<TestResult> testResults) {
    Set<Node> paragraphsCoveredByFailingTests = testResults.stream()
        .filter(TestResult::isFailed)
        .map(TestResult::getRelevantNodes)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    Set<Node> visitedNodes = new HashSet<>();
    Set<Node> descendants = new HashSet<>();
    for (Node coveredParagraph : paragraphsCoveredByFailingTests) {
      DescendantCollector descendantCollector = new DescendantCollector(coveredParagraph,
          visitedNodes);
      modelUnit.accept(descendantCollector, null);
      descendants.addAll(descendantCollector.getDescendants());
      visitedNodes.addAll(descendantCollector.getDescendants());
    }
    return descendants;
  }

  private static Map<Node, RelevantTestResults> constructNodeToTestMapping(ModelUnit modelUnit,
      List<TestResult> testResults) {
    // Find all nodes that are covered by failing tests and establish the mapping
    // between those nodes and the test results.
    Map<Node, RelevantTestResults> node2test = new HashMap<>();
    // First collect nodes covered by failing tests.
    Set<Node> nodesCoveredByFailingTests = testResults.stream()
        .filter(TestResult::isFailed)
        .map(TestResult::getRelevantNodes)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    // Then collect tests that execute nodes covered by failing tests.
    testResults.forEach(testResult -> {
      testResult.getRelevantNodes().stream()
          .filter(nodesCoveredByFailingTests::contains)
          .forEach(nodeCoveredByFailingTests -> {
            if (!node2test.containsKey(nodeCoveredByFailingTests)) {
              node2test.put(nodeCoveredByFailingTests, new RelevantTestResults());
            }
            node2test.get(nodeCoveredByFailingTests).addTestResult(testResult);
          });
    });
    // Finally map all descendants to the same test results as the paragraph node.
    // Note that all descendants keep the same test results copy as the paragraph node.
    node2test.forEach((paraNode, results) -> {
      DescendantCollector descendantCollector = new DescendantCollector(paraNode, new HashSet<>());
      modelUnit.accept(descendantCollector, null);
      descendantCollector.getDescendants().forEach(descendant -> {
        node2test.put(descendant, results);
      });
    });
    return node2test;
  }

  private static SearchStrategy findSearchStrategy(String strategy) {
    switch (strategy) {
      case "base-choice":
        return SearchStrategy.BASE_CHOICE;
      case "all-combinations":
        return ALL_COMBINATIONS;
      default:
        return null;
    }
  }

  private static void printAlloyPatcherUsage() {
    logger.info(
        "Patcher requires: model path, test path, scope, lowest cost, search strategy"
            + " and whether to enable sub-formula caching.  If the search strategy is base-choice,"
            + " then --" + MAX_TRY_PER_HOLE
            + " must be set.  If the search strategy is all-combinations,"
            + " then --" + PARTITION_NUM + " and --" + MAX_TRY_NUM_PER_DEPTH + " must be set."
    );
  }

  public static void printARepairUsage(HelpFormatter formatter, Options options) {
    formatter.setOptionComparator(null);
    formatter.printHelp(
        CLI_USAGE_DESCRIPTION_WIDTH,
        Patcher.class.getSimpleName(),
        "Repair faulty Alloy models.",
        options,
        null,
        true);
  }

  private static PatcherOpt parseCommandLineArgs(String[] args) {
    Options options = new Options();
    options.addRequiredOption("m", MODEL_PATH, true, "Path of the faulty model to repair.");
    options.addRequiredOption("t", TEST_PATH, true, "Path of the AUnit test suite.");
    options.addRequiredOption("s", SCOPE, true, "Scope to run all AUnit tests properly.");
    options.addRequiredOption("c", MINIMUM_COST, true,
        "Minimum cost/size of the generate expressions.");
    options.addOption("e", ENABLE_CACHE, false, "Enable hierarchical caching.");
    options.addRequiredOption("g", SEARCH_STRATEGY, true, "Search strategy of the synthesizer.");
    // Required for base choice search strategy.
    options.addOption("h", MAX_TRY_PER_HOLE, true, "Max number of tries per hole.");
    // Required for all combination search strategy.
    options.addOption("p", PARTITION_NUM, true, "Number of partitions.");
    options.addOption("d", MAX_TRY_NUM_PER_DEPTH, true, "Max number of tries per depth.");

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();

    try {
      CommandLine commandLine = parser.parse(options, args);
      String modelPath = Paths.get(commandLine.getOptionValue(MODEL_PATH)).toAbsolutePath()
          .toString();
      if (!FileUtil.fileExists(modelPath)) {
        logger.error("Cannot find model at " + modelPath);
        printARepairUsage(formatter, options);
        return null;
      }
      String testPath = Paths.get(commandLine.getOptionValue(TEST_PATH)).toAbsolutePath()
          .toString();
      if (!FileUtil.fileExists(testPath)) {
        logger.error("Cannot find tests at " + testPath);
        printARepairUsage(formatter, options);
        return null;
      }
      int scope = Integer.parseInt(commandLine.getOptionValue(SCOPE));
      if (scope <= 0) {
        logger.error("Scope must be greater than 0, but found " + scope);
        printAlloyPatcherUsage();
        return null;
      }
      int minimumCost = Integer.parseInt(commandLine.getOptionValue(MINIMUM_COST));
      if (minimumCost <= 0) {
        logger.error("Minimum cost/size must be greater than 0, but found " + minimumCost);
        printAlloyPatcherUsage();
        return null;
      }
      String searchStrategyOption = commandLine.getOptionValue(SEARCH_STRATEGY);
      SearchStrategy searchStrategy = findSearchStrategy(searchStrategyOption);
      if (searchStrategy == null) {
        logger.error(
            "Search strategy must be in " + Arrays.asList(SearchStrategy.values()) + ", but found "
                + searchStrategyOption);
        printAlloyPatcherUsage();
        return null;
      }
      int maxTryPerHole = -1;
      int partitionNum = -1;
      int maxTryPerDepth = -1;
      switch (searchStrategy) {
        case BASE_CHOICE:
          if (!commandLine.hasOption(MAX_TRY_PER_HOLE)) {
            logger.error(searchStrategyOption + " requires option --" + MAX_TRY_PER_HOLE);
            printAlloyPatcherUsage();
            return null;
          }
          maxTryPerHole = Integer.parseInt(commandLine.getOptionValue(MAX_TRY_PER_HOLE));
        case ALL_COMBINATIONS:
          if (!commandLine.hasOption(PARTITION_NUM) || !commandLine
              .hasOption(MAX_TRY_NUM_PER_DEPTH)) {
            logger.error(
                searchStrategyOption + " requires options --" + PARTITION_NUM + " and --"
                    + MAX_TRY_NUM_PER_DEPTH);
            printAlloyPatcherUsage();
            return null;
          }
          partitionNum = Integer.parseInt(commandLine.getOptionValue(PARTITION_NUM));
          maxTryPerDepth = Integer.parseInt(commandLine.getOptionValue(MAX_TRY_NUM_PER_DEPTH));
      }
      return new PatcherOpt(modelPath, testPath, scope, searchStrategy,
          commandLine.hasOption(ENABLE_CACHE), minimumCost, maxTryPerHole, partitionNum,
          maxTryPerDepth);
    } catch (ParseException e) {
      logger.error(e.getMessage());
      printARepairUsage(formatter, options);
      return null;
    }
  }

  public static void main(String... args) {
    PatcherOpt patcherOpt = parseCommandLineArgs(args);
    if (patcherOpt == null) {
      return;
    }
    patch(patcherOpt);
  }
}
