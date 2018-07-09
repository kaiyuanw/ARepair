package generator.util.rules.union;

import static generator.etc.Contants.TILDE;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class URule6 extends BinaryRule {

  private URule6(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static URule6 given(BinaryInfo binaryInfo) {
    return new URule6(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), TILDE) && opIsOr(rightRel.getOp(), TILDE);
  }
}
