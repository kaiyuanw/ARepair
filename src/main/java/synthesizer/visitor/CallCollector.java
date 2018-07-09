package synthesizer.visitor;

import java.util.LinkedHashSet;
import java.util.Set;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
import parser.ast.visitor.VoidVisitorAdapter;

public class CallCollector extends VoidVisitorAdapter<Object> {

  /**
   * Stores the names of invoked functions/predicates.
   */
  private Set<String> callNames;

  public CallCollector() {
    this.callNames = new LinkedHashSet<>();
  }

  public Set<String> getCallNames() {
    return callNames;
  }

  @Override
  public void visit(CallExpr n, Object arg) {
    callNames.add(n.getName());
    super.visit(n, arg);
  }

  @Override
  public void visit(CallFormula n, Object arg) {
    callNames.add(n.getName());
    super.visit(n, arg);
  }
}
