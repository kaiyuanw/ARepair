package generator.util.rules.union;

import static generator.etc.Contants.CARET;
import static generator.etc.Contants.STAR;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class URule5 extends BinaryRule {

  private URule5(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static URule5 given(BinaryInfo binaryInfo) {
    return new URule5(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), STAR, CARET) && opIsOr(rightRel.getOp(), STAR, CARET)
        && sameRelations(getChild(leftRel, 0), getChild(rightRel, 0));
  }
}
