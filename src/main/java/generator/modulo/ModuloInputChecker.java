package generator.modulo;

import static parser.etc.Context.logger;

import edu.mit.csail.sdg.parser.CompModule;
import generator.fragment.Expression;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import parser.ast.nodes.ModelUnit;
import parser.util.AlloyUtil;

/**
 * The modulo input checker that group equivalent expressions based on their valuations under all
 * satisfiable tests.  Note that if the model is modified, then we need to rerun modulo input
 * pruning.
 */
public class ModuloInputChecker {

  /**
   * A list of test cases that are satisfiable.
   */
  private List<TestCase> tests;

  /**
   * This field maps an arity to a map, where the map maps the output of the evaluator to a list of
   * expressions that evaluate to the same output.
   */
  private Map<Integer, Map<String, Set<Expression>>> equivalentClasses;

  public ModuloInputChecker(List<TestCase> tests) {
    this.tests = tests;
    this.equivalentClasses = new LinkedHashMap<>();
  }

  public boolean isEquivalent(Expression expression) {
    StringBuilder sb = new StringBuilder();
    for (TestCase test : tests) {
      sb.append(test.computeExprKey(expression));
    }
    String overallKey = sb.toString();
    if (!equivalentClasses.containsKey(expression.getArity())) {
      equivalentClasses.put(expression.getArity(), new LinkedHashMap<>());
    }
    Map<String, Set<Expression>> keyToExprs = equivalentClasses.get(expression.getArity());
    boolean isEquivalent = keyToExprs.containsKey(overallKey);
    if (!isEquivalent) {
      keyToExprs.put(overallKey, new LinkedHashSet<>());
    }
    keyToExprs.get(overallKey).add(expression);
    return isEquivalent;
  }

  public void printEquivalentClasses() {
    for (Map.Entry<Integer, Map<String, Set<Expression>>> entry1 : equivalentClasses.entrySet()) {
      logger.debug("Arity: " + entry1.getKey());
      for (Map.Entry<String, Set<Expression>> entry2 : entry1.getValue().entrySet()) {
        logger.debug("  Key: " + entry2.getKey());
        logger.debug("  Expressions (" + entry2.getValue().size() + "): " + entry2.getValue());
      }
    }
  }

  public static void main(String... args) {
    CompModule module = AlloyUtil.compileAlloyModule("tmp.als");
    ModelUnit modelUnit = new ModelUnit(null, module);
    System.out.println(modelUnit.accept(new ModelNormalizer(), null));
  }
}
