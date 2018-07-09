package generator.util.rules.intersect;

import static generator.etc.Contants.TILDE;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class IRule6 extends BinaryRule {

  private IRule6(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static IRule6 given(BinaryInfo binaryInfo) {
    return new IRule6(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), TILDE) && opIsOr(rightRel.getOp(), TILDE);
  }
}
