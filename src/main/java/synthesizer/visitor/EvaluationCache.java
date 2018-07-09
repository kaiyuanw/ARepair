package synthesizer.visitor;

import static parser.etc.Names.COMMA;

import generator.modulo.TestCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import parser.ast.nodes.Assertion;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.Body;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
import parser.ast.nodes.Check;
import parser.ast.nodes.ConstExpr;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.Fact;
import parser.ast.nodes.FieldDecl;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.Function;
import parser.ast.nodes.ITEExpr;
import parser.ast.nodes.ITEFormula;
import parser.ast.nodes.LetExpr;
import parser.ast.nodes.ListExpr;
import parser.ast.nodes.ListFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.ModuleDecl;
import parser.ast.nodes.Node;
import parser.ast.nodes.OpenDecl;
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.QtExpr;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.Run;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryExpr.UnaryOp;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import synthesizer.hole.Hole;

public class EvaluationCache extends PrettyStringWithHoleHandler {

  private Map<String, String> cache;
  private Set<Node> nodeToEval;
  private TestCase testCase;
  /**
   * Maps a node to a list of variables in scope.
   */
  private Map<Node, List<VarExpr>> nodeToVariables;
  private Inliner inliner;

  public EvaluationCache(Map<Node, Hole> node2hole, CacheMapCreator cacheMapCreator,
      TestCase testCase, Map<Node, List<VarExpr>> nodeToVariables, Inliner inliner) {
    super(node2hole);
    this.cache = new LinkedHashMap<>();
    this.nodeToEval = cacheMapCreator.getL2H().keySet();
    this.testCase = testCase;
    this.nodeToVariables = nodeToVariables;
    this.inliner = inliner;
  }

  private List<VarExpr> findMinimumUsedVariables(Node n) {
    VariableCollector variableCollector = new VariableCollector(node2hole);
    n.accept(variableCollector, null);
    return nodeToVariables.get(n).stream()
        .filter(varExpr -> variableCollector.getUsedVariableNames().contains(varExpr.getName()))
        .collect(Collectors.toList());
  }

  public void printCache() {
    cache.forEach((k, v) -> {
      System.out.println("Key: " + k);
      System.out.println("Value: " + v);
      System.out.println("=====");
    });
  }

  @Override
  public String visit(ModelUnit n, Object arg) {
    return null;
  }

  @Override
  public String visit(ModuleDecl n, Object arg) {
    return null;
  }

  @Override
  public String visit(OpenDecl n, Object arg) {
    return null;
  }

  @Override
  public String visit(SigDecl n, Object arg) {
    // We do not cache holes in signature declarations.  But we may consider cache holes in
    // signature facts in the future.
    return null;
  }

  @Override
  public String visit(FieldDecl n, Object arg) {
    // No need to cache holes in field declarations.
    return null;
  }

  @Override
  public String visit(ParamDecl n, Object arg) {
    // No need to cache holes in parameter declarations.
    return null;
  }

  @Override
  public String visit(VarDecl n, Object arg) {
    // No need to cache holes in variable declarations.
    return (n.isDisjoint() ? "disj " : "") + String.join(COMMA,
        n.getVariables().stream().map(variable -> variable.accept(this, arg)).collect(
            Collectors.toList())) + ": " + n.getExpr().accept(this, arg);
  }

  @Override
  public String visit(ExprOrFormula n, Object arg) {
    return n.accept(this, arg);
  }

  private String getValuation(String key, Node n) {
    if (cache.containsKey(key)) {
//      System.out.println("ExprOrFormulaCache Hit: " + key);
      return cache.get(key);
    }
    List<VarExpr> minimumUsedVariables = findMinimumUsedVariables(n);
    String valuation = testCase
        .computeUniqueValuations(minimumUsedVariables, n.accept(inliner, null));
    cache.put(key, valuation);
    return valuation;
  }

  @Override
  public String visit(SigExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(FieldExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(VarExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(UnaryExpr n, Object arg) {
    if (n.getOp() == UnaryOp.NOOP) {
      return n.getSub().accept(this, arg);
    }
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(UnaryFormula n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(BinaryExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(BinaryFormula n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(ListExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(ListFormula n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(CallExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(CallFormula n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(QtExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(QtFormula n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(ITEExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(ITEFormula n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(LetExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(ConstExpr n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(Body n, Object arg) {
    String key = super.visit(n, arg);
    if (nodeToEval.contains(n)) {
      return getValuation(key, n);
    }
    return key;
  }

  @Override
  public String visit(Predicate n, Object arg) {
    return n.getBody().accept(this, arg);
  }

  @Override
  public String visit(Function n, Object arg) {
    return n.getBody().accept(this, arg);
  }

  @Override
  public String visit(Fact n, Object arg) {
    return n.getBody().accept(this, arg);
  }

  @Override
  public String visit(Assertion n, Object arg) {
    return n.getBody().accept(this, arg);
  }

  @Override
  public String visit(Run n, Object arg) {
    return null;
  }

  @Override
  public String visit(Check n, Object arg) {
    return null;
  }
}
