package generator.util.rules;

import generator.fragment.Expression;
import generator.fragment.Fragment;

public abstract class UnaryRule extends PruningRule {

  protected Fragment op;
  protected Expression rel;

  public UnaryRule(UnaryInfo unaryInfo) {
    this.op = unaryInfo.getOp();
    this.rel = unaryInfo.getRel();
  }
}
