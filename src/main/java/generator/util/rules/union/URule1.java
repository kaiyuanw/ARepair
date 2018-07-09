package generator.util.rules.union;

import static generator.etc.Contants.PLUS;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class URule1 extends BinaryRule {

  private URule1(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static URule1 given(BinaryInfo binaryInfo) {
    return new URule1(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return leftRel.getOp().equals(PLUS);
  }
}
