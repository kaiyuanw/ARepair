package generator.util.rules.closure;

import static generator.etc.Contants.CARET;
import static generator.etc.Contants.STAR;

import generator.util.rules.UnaryInfo;
import generator.util.rules.UnaryRule;

public class VRule1 extends UnaryRule {

  private VRule1(UnaryInfo unaryInfo) {
    super(unaryInfo);
  }

  public static VRule1 given(UnaryInfo unaryInfo) {
    return new VRule1(unaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(rel.getOp(), STAR, CARET);
  }
}
