package generator.util.rules.rclosure;

import static generator.etc.Contants.IDEN_EXPR;

import generator.util.rules.UnaryInfo;
import generator.util.rules.UnaryRule;

public class RRule2 extends UnaryRule {

  private RRule2(UnaryInfo unaryInfo) {
    super(unaryInfo);
  }

  public static RRule2 given(UnaryInfo unaryInfo) {
    return new RRule2(unaryInfo);
  }

  @Override
  public boolean isPruned() {
    return rel == IDEN_EXPR;
  }
}
