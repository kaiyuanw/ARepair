package generator.util.rules.crossproduct;

import static generator.etc.Contants.ARROW;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class CRule1 extends BinaryRule {

  private CRule1(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static CRule1 given(BinaryInfo binaryInfo) {
    return new CRule1(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), ARROW);
  }
}
