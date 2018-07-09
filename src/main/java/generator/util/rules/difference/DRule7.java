package generator.util.rules.difference;

import static generator.etc.Contants.CARET;
import static generator.etc.Contants.DOT;
import static generator.etc.Contants.STAR;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class DRule7 extends BinaryRule {

  private DRule7(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static DRule7 given(BinaryInfo binaryInfo) {
    return new DRule7(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(rightRel.getOp(), STAR, CARET) && getChild(rightRel, 0)
        .equals(duplicateNodesUnderOps(leftRel, DOT));
  }
}
