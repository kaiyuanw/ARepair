package generator.util.rules.join;

import static generator.etc.Contants.CARET;
import static generator.util.Util.isSuperType;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule5 extends BinaryRule {

  private JRule5(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule5 given(BinaryInfo binaryInfo) {
    return new JRule5(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return isSuperType(leftRel, inheritanceMap) && opIsOr(rightRel.getOp(), CARET)
        || isSuperType(rightRel, inheritanceMap) && opIsOr(leftRel.getOp(), CARET);
  }
}
