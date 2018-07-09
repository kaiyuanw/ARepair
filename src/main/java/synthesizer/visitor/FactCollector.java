package synthesizer.visitor;

import static parser.etc.Names.DOLLAR;
import static parser.etc.Names.UNDERSCORE;
import static parser.util.AlloyUtil.getFirstNonNOOPChild;

import java.util.LinkedHashMap;
import java.util.Map;
import parser.ast.nodes.Fact;
import parser.ast.nodes.FieldDecl;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigDecl.MULT;
import parser.ast.nodes.UnaryExpr;
import parser.ast.visitor.VoidVisitorAdapter;

/**
 * This class is able to collect signature/field multiplicity constraints, sig facts and facts that
 * a test may depend on.
 */
public class FactCollector extends VoidVisitorAdapter<Object> {

  public static final String SIG_MULT = "_sig_mult";
  public static final String SIG_FACT = "_sig_fact";
  public static final String FIELD_MULT = "_field_mult";
  public static final String X = "x";

  private Map<String, Node> facts;

  public FactCollector() {
    this.facts = new LinkedHashMap<>();
  }

  public Map<String, Node> getFacts() {
    return facts;
  }

  @Override
  public void visit(ModelUnit n, Object arg) {
    n.getSigDeclList().forEach(signature -> signature.accept(this, arg));
    n.getFactDeclList().forEach(fact -> fact.accept(this, arg));
  }

  @Override
  public void visit(SigDecl n, Object arg) {
    if (n.getMult() != MULT.SET) {
      // E.g. "one sig S {}" => <"S_sig_mult", "one S">.
      facts.putIfAbsent(n.getName() + SIG_MULT, n);
    }
    n.getFieldList().forEach(field -> field.accept(this, arg));
    if (n.hasSigFact()) {
      facts.putIfAbsent(n.getName() + SIG_FACT, n.getSigFact());
    }
  }

  @Override
  public void visit(FieldDecl n, Object arg) {
    Node expr = getFirstNonNOOPChild(n.getExpr());
    if (expr instanceof UnaryExpr) {
      UnaryExpr unaryExpr = (UnaryExpr) expr;
      switch (unaryExpr.getOp()) {
        case LONE:
        case ONE:
        case SOME:
          // Alloy parses a single field declaration with multiple field names as separate field
          // declarations, so in theory we will not create duplicate constraints.
          n.getNames().forEach(name -> facts.putIfAbsent(name + FIELD_MULT, n));
          break;
        default:
          // No fact needed.
      }
    }
  }

  @Override
  public void visit(Fact n, Object arg) {
    facts.putIfAbsent(n.getName().replaceAll("\\" + DOLLAR, UNDERSCORE), n.getBody());
  }
}
