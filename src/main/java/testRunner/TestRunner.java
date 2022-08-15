package testRunner;

import alloyfl.coverage.util.StaticAnalyzer;
import alloyfl.coverage.util.SubtreeMatcher;
import alloyfl.coverage.util.TestResult;
import alloyfl.coverage.util.UnsatAnalyzer;
import alloyfl.mutation.opt.FaultLocatorOpt;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.Predicate;
import parser.ast.visitor.GenericVisitor;
import parser.ast.visitor.PrettyStringVisitor;
import parser.ast.visitor.VoidVisitor;
import parser.etc.Names;
import parser.etc.NodeMap;
import parser.util.AlloyUtil;
import parser.util.FileUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestRunner {

    public static List<Boolean> runTests(String modelToTestAsString, FaultLocatorOpt opt) {
        List<Boolean> result = new ArrayList<>();
        String modelAsString = String.join("\n", Arrays.asList(modelToTestAsString, FileUtil.readTextWithFilter(opt.getTestSuitePath(), (line, pattern) -> !line.startsWith(pattern), "open")));
        FileUtil.writeText(modelAsString, Names.TMPT_FILE_PATH, false);
        CompModule model = AlloyUtil.compileAlloyModule(Names.TMPT_FILE_PATH);

        assert model != null;

        for (Command cmd : model.getAllCommands()) {
            if (!cmd.check && cmd.label.startsWith("test")) {
                try {
                    A4Solution ans = TranslateAlloyToKodkod.execute_command(A4Reporter.NOP, model.getAllReachableSigs(), cmd, opt.getOptions());
                    int actual = ans.satisfiable() ? 1 : 0;
                    result.add(actual == cmd.expects);
                } catch (Err e) {
                    /*
                        Assuming that the original was grammatically correct,
                        and assuming that semantically the original model did not
                        lead to any exception, means that an exception at this point
                        should be considered as a failing test.
                     */
                    result.add(false);
                }
            }
        }
        return result;
    }

    public static List<TestResult> runTests(ModelUnit modelUnit, String testSuitePath, PrettyStringVisitor psv, A4Options options)  {
        List<TestResult> result = new ArrayList<>();
        CompModule moduleWithTests = AlloyUtil.mergeModelAndTests(modelUnit, testSuitePath, psv);
        assert moduleWithTests != null;
        Node dummyNode = new Node(null, new NodeMap()) {
            @Override
            public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
                return null;
            }

            @Override
            public <A> void accept(VoidVisitor<A> v, A arg) {
            }
        };
        ModelUnit modelUnitWithTests = new ModelUnit(dummyNode, moduleWithTests);
        SubtreeMatcher matcher = new SubtreeMatcher();
        matcher.matches(modelUnitWithTests, modelUnit);
        Map<Node, Node> nodesWithTestsToNodes = matcher.getS2D();
        Map<String, Predicate> testNameToTestPred = modelUnitWithTests.getPredDeclList()
                .stream()
                .filter(predicate -> predicate.getName().startsWith(Names.TEST_PREFIX))
                .collect(Collectors.toMap(Predicate::getName, Function.identity()));

        for (Command cmd : moduleWithTests.getAllCommands()) {
            Predicate testPredicate = testNameToTestPred.get(cmd.label);
            if (!cmd.check && cmd.label.startsWith(Names.TEST_PREFIX)) {
                try {
                    A4Solution sol = TranslateAlloyToKodkod
                            .execute_command(A4Reporter.NOP, moduleWithTests.getAllReachableSigs(), cmd, options);
                    // Get nodes invoked by the test predicate.
                    Collection<Node> relevantNodes = getRelevantNodes(nodesWithTestsToNodes, modelUnitWithTests, testPredicate);
                    TestResult testResult = new TestResult(cmd.expects == 1, sol.satisfiable(), relevantNodes);
                    if (!sol.satisfiable() && cmd.expects == 1) {
                        // Get nodes highlighted in unsat core in terms of the original model (without tests).
                        Collection<Node> unsatNodes = UnsatAnalyzer.findHighlightedNodes(dummyNode.getNodeMap(), sol)
                                .stream()
                                // We only want to get node in the model, but not in the test.
                                .filter(nodesWithTestsToNodes::containsKey)
                                .map(nodesWithTestsToNodes::get)
                                .collect(Collectors.toSet());
                        testResult.setUnsatNodes(unsatNodes);
                    }
                    result.add(testResult);
                } catch (Err err) {
                    /*
                        Assuming that the original was grammatically correct,
                        and assuming that semantically the original model did not
                        lead to any exception, means that an exception at this point
                        should be considered as a failing test.
                     */
                    Collection<Node> relevantNodes = getRelevantNodes(nodesWithTestsToNodes, modelUnitWithTests, testPredicate);
                    TestResult testResult = new TestResult(cmd.expects == 1, false, relevantNodes);
                    result.add(testResult);
                }
            }
        }
        return result;
    }

    private static Collection<Node> getRelevantNodes(Map<Node, Node> nodesWithTestsToNodes, ModelUnit modelUnitWithTests, Predicate testPredicate) {
        return StaticAnalyzer.findAllDependentNodes(modelUnitWithTests, testPredicate)
                .stream()
                // We only want to get node in the model, but not in the test.
                .filter(nodesWithTestsToNodes::containsKey)
                .map(nodesWithTestsToNodes::get)
                .collect(Collectors.toSet());
    }

}
