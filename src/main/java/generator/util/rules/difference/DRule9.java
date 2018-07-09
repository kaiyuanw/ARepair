package generator.util.rules.difference;

import static generator.etc.Contants.TILDE;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class DRule9 extends BinaryRule {

  private DRule9(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static DRule9 given(BinaryInfo binaryInfo) {
    return new DRule9(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), TILDE) && opIsOr(rightRel.getOp(), TILDE);
  }
}
