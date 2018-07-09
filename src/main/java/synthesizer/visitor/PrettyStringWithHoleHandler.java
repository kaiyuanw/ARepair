package synthesizer.visitor;

import static parser.etc.Names.COMMA;
import static parser.etc.Names.NEW_LINE;

import java.util.Map;
import java.util.stream.Collectors;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.ListFormula;
import parser.ast.nodes.Node;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarExpr;
import parser.ast.visitor.PrettyStringVisitor;
import synthesizer.hole.Hole;
import synthesizer.hole.UnaryExprCardHole;

/**
 * Note that this class also considers holes and the class should be in sync with the {@link
 * HoleTemplateCollector}.
 */
public class PrettyStringWithHoleHandler extends PrettyStringVisitor {

  protected Map<Node, Hole> node2hole;

  public PrettyStringWithHoleHandler(PrettyStringWithHoleHandler handler) {
    this.node2hole = handler.node2hole;
  }

  public PrettyStringWithHoleHandler(Map<Node, Hole> node2hole) {
    this.node2hole = node2hole;
  }

  @Override
  public String visit(SigDecl n, Object arg) {
    inSigDecl = true;
    String mult = n.getMult().toString();
    if (node2hole.containsKey(n)) { // SigHole.
      if (node2hole.get(n).isActive()) {
        mult = node2hole.get(n).toString();
      }
    }
    String sigDeclAsString =
        (n.isAbstract() ? "abstract " : "") + mult + "sig " + n.getName() + " " + (
            n.isTopLevel() ? "" : (n.isSubsig() ? "extends" : "in") + " " + n.getParentName() + " ")
            + "{" +
            (n.getFieldList().size() > 0 ? NEW_LINE + String.join(COMMA + NEW_LINE,
                n.getFieldList().stream().map(field -> field.accept(this, arg))
                    .collect(Collectors.toList())) + NEW_LINE : "") +
            "}" + (n.hasSigFact() ? "{" + NEW_LINE + n.getSigFact().accept(this, arg) + NEW_LINE
            + "}"
            : "");
    inSigDecl = false;
    return putInMap(n, sigDeclAsString);
  }

  @Override
  public String visit(SigExpr n, Object arg) {
    if (node2hole.containsKey(n)) { // ExprHole.
      if (node2hole.get(n).isActive()) {
        return putInMap(n, node2hole.get(n).toString());
      }
    }
    return super.visit(n, arg);
  }

  @Override
  public String visit(FieldExpr n, Object arg) {
    if (node2hole.containsKey(n)) { // ExprHole.
      if (node2hole.get(n).isActive()) {
        return putInMap(n, node2hole.get(n).toString());
      }
    }
    return super.visit(n, arg);
  }

  @Override
  public String visit(VarExpr n, Object arg) {
    if (node2hole.containsKey(n)) { // ExprHole.
      if (node2hole.get(n).isActive()) {
        return putInMap(n, node2hole.get(n).toString());
      }
    }
    return super.visit(n, arg);
  }

  @Override
  public String visit(UnaryExpr n, Object arg) {
    String subAsString = n.getSub().accept(this, arg);
    if (n.getOp() == UnaryExpr.UnaryOp.NOOP) {
      return putInMap(n, subAsString);
    }
    if (node2hole.containsKey(n)) {
      Hole hole = node2hole.get(n);
      if (hole.isActive()) {
        if (hole instanceof UnaryExprCardHole) { // UnaryExprCardHole.
          return putInMap(n, "(" + hole.toString() + subAsString + ")");
        } else { // ExprHole.
          return putInMap(n, hole.toString());
        }
      }
    }
    return putInMap(n, "(" + n.getOp() + subAsString + ")");
  }

  @Override
  public String visit(UnaryFormula n, Object arg) {
    String op = n.getOp().toString();
    if (node2hole.containsKey(n)) { // UnaryFormulaCardHole or UnaryFormulaBoolHole.
      if (node2hole.get(n).isActive()) {
        op = node2hole.get(n).toString();
      }
    }
    return putInMap(n, "(" + op + n.getSub().accept(this, arg) + ")");
  }

  @Override
  public String visit(BinaryExpr n, Object arg) {
    if (node2hole.containsKey(n)) { // ExprHole.
      if (node2hole.get(n).isActive()) {
        return putInMap(n, node2hole.get(n).toString());
      }
    }
    return super.visit(n, arg);
  }

  @Override
  public String visit(BinaryFormula n, Object arg) {
    String op = n.getOp().toString();
    if (node2hole.containsKey(n)) { // BinaryFormulaCmpHole or BinaryFormulaLogicHole.
      if (node2hole.get(n).isActive()) {
        op = node2hole.get(n).toString();
      }
    }
    return putInMap(n,
        "(" + n.getLeft().accept(this, arg) + op + n.getRight().accept(this, arg) + ")");
  }

  @Override
  public String visit(ListFormula n, Object arg) {
    String op = n.getOp().toString();
    if (node2hole.containsKey(n)) { // ListFormulaHole.
      if (node2hole.get(n).isActive()) {
        op = node2hole.get(n).toString();
      }
    }
    boolean flattenList = false;
    if (n.getParent() instanceof ListFormula) {
      ListFormula parent = (ListFormula) n.getParent();
      if (parent.getOp().toString().equals(op)) {
        flattenList = true;
      }
    }
    String innerString = String.join(op,
        n.getArguments().stream().map(expr -> expr.accept(this, arg)).collect(Collectors.toList()));
    if (flattenList) {
      return putInMap(n, innerString);
    }
    return putInMap(n, "(" + innerString + ")");
  }

  @Override
  public String visit(QtFormula n, Object arg) {
    String op = n.getOp().toString();
    if (node2hole.containsKey(n)) { // QuantifierHole.
      if (node2hole.get(n).isActive()) {
        op = node2hole.get(n).toString();
      }
    }
    String qtExpr = op + String.join(COMMA,
        n.getVarDecls().stream().map(varDecl -> varDecl.accept(this, arg))
            .collect(Collectors.toList())) + " " + n.getBody().accept(this, arg);
    return putInMap(n, "(" + qtExpr + ")");
  }
}
