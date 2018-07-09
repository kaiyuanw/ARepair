package clean.visitor;

import static clean.etc.Constant.NONE;
import static clean.etc.Constant.ZERO;

import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.BinaryExpr.BinaryOp;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.ConstExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryExpr.UnaryOp;
import parser.ast.nodes.VarExpr;
import parser.ast.visitor.PrettyStringVisitor;

public class SimpleModelVisitor extends PrettyStringVisitor {

  @Override
  public String visit(UnaryExpr n, Object arg) {
    if (n.getOp() == UnaryOp.CARDINALITY) {
      if (n.getSub().accept(this, arg).equals(NONE)) {
        // Replace #none with 0.
        return putInMap(n, ZERO);
      }
    }
    return super.visit(n, arg);
  }

  @Override
  public String visit(BinaryExpr n, Object arg) {
    if (n.getOp() == BinaryOp.JOIN) {
      if (n.getLeft().accept(this, arg).equals(NONE)
          || n.getRight().accept(this, arg).equals(NONE)) {
        // Replace none.r/r.none with none.
        return putInMap(n, NONE);
      }
    }
    if (n.getOp() == BinaryOp.MINUS) {
      if (n.getRight().accept(this, arg).equals(NONE)) {
        // Replace s - none with s.
        return putInMap(n, n.getLeft().accept(this, arg));
      }
    }
    return super.visit(n, arg);
  }

  @Override
  public String visit(BinaryFormula n, Object arg) {
    if (n.getOp() == BinaryFormula.BinaryOp.NOT_EQUALS) {
      if (n.getLeft().accept(this, arg).equals(NONE) && n.getRight() instanceof VarExpr) {
        // Replace none != n with {}.
        return putInMap(n, "{}");
      }
    }
    if (n.getOp() == BinaryFormula.BinaryOp.IMPLIES) {
      if (n.getLeft() instanceof ConstExpr && ((ConstExpr) n.getLeft()).isBoolean()) {
        // Replace true => F with F.  Note that we never observe false const to appear in the model.
        // So if the const is boolean then we assume it is true.
        return putInMap(n, n.getRight().accept(this, arg));
      }
    }
    return super.visit(n, arg);
  }
}
