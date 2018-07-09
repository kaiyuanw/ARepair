package generator.util.rules.transpose;

import static generator.etc.Contants.IDEN_EXPR;

import generator.util.rules.UnaryInfo;
import generator.util.rules.UnaryRule;

public class TRule5 extends UnaryRule {

  private TRule5(UnaryInfo unaryInfo) {
    super(unaryInfo);
  }

  public static TRule5 given(UnaryInfo unaryInfo) {
    return new TRule5(unaryInfo);
  }

  @Override
  public boolean isPruned() {
    return rel == IDEN_EXPR;
  }
}
