package generator.util.rules.intersect;

import static generator.etc.Contants.CARET;
import static generator.etc.Contants.STAR;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class IRule5 extends BinaryRule {

  private IRule5(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static IRule5 given(BinaryInfo binaryInfo) {
    return new IRule5(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), STAR, CARET) && opIsOr(rightRel.getOp(), STAR, CARET)
        && sameRelations(getChild(leftRel, 0), getChild(rightRel, 0));
  }
}
