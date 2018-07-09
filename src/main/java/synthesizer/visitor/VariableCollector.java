package synthesizer.visitor;

import generator.fragment.Expression;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.Node;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.VarExpr;
import parser.ast.visitor.VoidVisitorAdapter;
import synthesizer.hole.Hole;

public class VariableCollector extends VoidVisitorAdapter<Object> {

  private Map<Node, Hole> node2hole;
  private Set<String> usedVariableNames;

  public VariableCollector(Map<Node, Hole> node2hole) {
    this.node2hole = node2hole;
    this.usedVariableNames = new HashSet<>();
  }

  public Set<String> getUsedVariableNames() {
    return usedVariableNames;
  }

  /**
   * Returns true if we collect variable names from the fragment of expression holes, false
   * otherwise.
   */
  private boolean collectVariables(Node n) {
    if (node2hole.containsKey(n)) {
      Hole hole = node2hole.get(n);
      if (hole.isActive() && hole.getValue() instanceof Expression) {
        Expression expr = (Expression) hole.getValue();
        expr.getUsedVariables().forEach(varExpr -> usedVariableNames.add(varExpr.getName()));
        return true;
      }
    }
    return false;
  }

  @Override
  public void visit(SigExpr n, Object arg) {
    collectVariables(n);
  }

  @Override
  public void visit(FieldExpr n, Object arg) {
    collectVariables(n);
  }

  @Override
  public void visit(VarExpr n, Object arg) {
    if (collectVariables(n)) {
      return;
    }
    usedVariableNames.add(n.getName());
  }

  @Override
  public void visit(UnaryExpr n, Object arg) {
    if (n.getOp() == UnaryExpr.UnaryOp.NOOP) {
      n.getSub().accept(this, arg);
    }
    if (collectVariables(n)) {
      return;
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(BinaryExpr n, Object arg) {
    if (collectVariables(n)) {
      return;
    }
    super.visit(n, arg);
  }
}
