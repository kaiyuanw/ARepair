package generator.util.rules.union;

import static generator.util.Util.isSuperType;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class URule2 extends BinaryRule {

  private URule2(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static URule2 given(BinaryInfo binaryInfo) {
    return new URule2(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return isSuperType(leftRel, inheritanceMap)
        || isSuperType(rightRel, inheritanceMap);
  }
}
