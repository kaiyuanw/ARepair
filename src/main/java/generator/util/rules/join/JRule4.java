package generator.util.rules.join;

import static generator.etc.Contants.STAR;
import static generator.util.Util.isSuperType;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule4 extends BinaryRule {

  private JRule4(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule4 given(BinaryInfo binaryInfo) {
    return new JRule4(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return isSuperType(leftRel, inheritanceMap) && opIsOr(rightRel.getOp(), STAR)
        || isSuperType(rightRel, inheritanceMap) && opIsOr(leftRel.getOp(), STAR);
  }
}
