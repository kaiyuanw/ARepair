package generator.util.rules.closure;

import static generator.etc.Contants.IDEN_EXPR;

import generator.util.rules.UnaryInfo;
import generator.util.rules.UnaryRule;

public class VRule3 extends UnaryRule {

  private VRule3(UnaryInfo unaryInfo) {
    super(unaryInfo);
  }

  public static VRule3 given(UnaryInfo unaryInfo) {
    return new VRule3(unaryInfo);
  }

  @Override
  public boolean isPruned() {
    return rel == IDEN_EXPR;
  }
}
