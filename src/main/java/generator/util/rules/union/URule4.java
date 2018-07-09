package generator.util.rules.union;

import static generator.etc.Contants.AMP;
import static generator.etc.Contants.ARROW;
import static generator.etc.Contants.DOT;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class URule4 extends BinaryRule {

  private URule4(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static URule4 given(BinaryInfo binaryInfo) {
    return new URule4(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return (opIsOr(leftRel.getOp(), DOT) && opIsOr(rightRel.getOp(), DOT)
        || opIsOr(leftRel.getOp(), ARROW) && opIsOr(rightRel.getOp(), ARROW)
        || opIsOr(leftRel.getOp(), AMP) && opIsOr(rightRel.getOp(), AMP))
        && (sameRelations(getChild(leftRel, 0), getChild(rightRel, 0))
        || sameRelations(getChild(leftRel, 1), getChild(rightRel, 1)));
  }
}
