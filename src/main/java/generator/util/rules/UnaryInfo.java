package generator.util.rules;

import generator.fragment.Expression;
import generator.fragment.Fragment;

public class UnaryInfo {

  private Fragment op;
  private Expression rel;

  public UnaryInfo(Fragment op, Expression rel) {
    this.op = op;
    this.rel = rel;
  }

  public Fragment getOp() {
    return op;
  }

  public Expression getRel() {
    return rel;
  }
}
