package synthesizer.util;

import generator.modulo.TestCase;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import parser.ast.nodes.Paragraph;
import synthesizer.visitor.EvaluationCache;

public class TestInfo {

  private TestCase test;
  /**
   * Maps each fact name and the test predicate name to their result.  To make the test pass, all
   * facts and the test predicate must be satisfiable.
   */
  private Map<String, Boolean> formulaResults;

  private final boolean expectSat;
  /**
   * If all of the facts and the test body is satisfiable, then this field is true.
   */
  private boolean actualSat;

  /**
   * For caching purpose.
   */
  private Paragraph toSynthesize;
  /**
   * This field caches Alloy expressions/formulas to their values in a bottom-up fashion.  The idea
   * is to only evaluate small expressions/formulas (from bottom of AST) if possible and if the
   * values are encountered before, then we do not need to evaluate the big expression (from top of
   * AST).
   */
  private EvaluationCache evaluationCache;

  /**
   * This field maps the valuation of the Alloy paragraph to synthesize to the result of facts and
   * test corresponding to this class.  Given the valuation of the paragraph, if it is encountered
   * before, then we do not need to evaluate the facts and test any more.
   */
  private Map<String, Map<String, Boolean>> topCache;

  public TestInfo(TestCase test, Map<String, String> name2formula, boolean expectSat) {
    this.test = test;
    this.formulaResults = new LinkedHashMap<>();
    this.expectSat = expectSat;
    // During initialization, the names contain all fact names and the test predicate name.
    // We should add the results of all formulas as we can during initialization.
    evalFormulas(name2formula);
    this.toSynthesize = null;
    this.evaluationCache = null;
    this.topCache = new LinkedHashMap<>();
  }

  /**
   * Evaluate the formulas and set {@code actualSat}.  This method throws exception if any of the
   * evaluation results in an error.  This method should never throw an exception.
   */
  public void evalFormulas(Map<String, String> name2formula) {
    name2formula.forEach((name, formula) -> {
      if (canCache()) {
        String paraKey = toSynthesize.accept(evaluationCache, null);
        if (!topCache.containsKey(paraKey)) {
          topCache.put(paraKey, new LinkedHashMap<>());
        }
        if (!topCache.get(paraKey).containsKey(name)) {
          // If the formula is null, it means it is trivially true.
          topCache.get(paraKey).put(name, formula == null || evalFormula(formula));
        }
        formulaResults.put(name, topCache.get(paraKey).get(name));
      } else {
        formulaResults.put(name, formula == null || evalFormula(formula));
      }
    });
    actualSat = formulaResults.values().stream().allMatch(result -> result);
  }

  /**
   * Check the satisfiability of the test.
   */
  public void evalFormulasFast(Map<String, String> name2formula) {
    Set<String> unaffectedFormulas = new LinkedHashSet<>(formulaResults.keySet());
    unaffectedFormulas.removeAll(name2formula.keySet());
    // We do not need to evaluate the formula if some unaffected formula is already unsatisfiable.
    if (unaffectedFormulas.stream().anyMatch(formulaName -> !formulaResults.get(formulaName))) {
      actualSat = false;
      return;
    }
    // Check the satisfiability of affected formulas.
    for (Map.Entry<String, String> entry : name2formula.entrySet()) {
      String name = entry.getKey();
      String formula = entry.getValue();
      boolean formulaSat;
      if (canCache()) {
        String paraKey = toSynthesize.accept(evaluationCache, null);
        if (!topCache.containsKey(paraKey)) {
          topCache.put(paraKey, new LinkedHashMap<>());
        }
        if (!topCache.get(paraKey).containsKey(name)) {
          // If the formula is null, it means it is trivially true.
          topCache.get(paraKey).put(name, formula == null || evalFormula(formula));
        }
        formulaSat = topCache.get(paraKey).get(name);
      } else {
        formulaSat = formula == null || evalFormula(formula);
      }
      if (!formulaSat) { // Fast fail.
        actualSat = false;
        return;
      }
    }
    // If formulaResults is empty, this still returns true (vacuously true).
    actualSat = true;
  }

  public boolean evalFormula(String formula) {
    String evalResult = String.valueOf(test.eval(formula));
    return Boolean.valueOf(evalResult);
  }

  public boolean isExpectSat() {
    return expectSat;
  }

  public boolean isActualSat() {
    return actualSat;
  }

  public void setActualSat(boolean actualSat) {
    this.actualSat = actualSat;
  }

  public boolean isFailed() {
    return actualSat != expectSat;
  }

  public TestCase getTest() {
    return test;
  }

  private boolean canCache() {
    return toSynthesize != null && evaluationCache != null;
  }

  public void setParagraphToSynthesize(Paragraph toSynthesize) {
    this.toSynthesize = toSynthesize;
  }

  public void setEvaluationCache(EvaluationCache evaluationCache) {
    this.evaluationCache = evaluationCache;
  }
}
