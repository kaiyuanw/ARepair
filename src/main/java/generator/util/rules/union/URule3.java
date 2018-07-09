package generator.util.rules.union;

import static generator.etc.Contants.AMP;
import static generator.etc.Contants.MINUS;
import static generator.etc.Contants.PLUS;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class URule3 extends BinaryRule {

  private URule3(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static URule3 given(BinaryInfo binaryInfo) {
    return new URule3(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return (opIsOr(rightRel.getOp(), PLUS, AMP, MINUS)
        && (sameRelations(leftRel, getChild(rightRel, 0)) || sameRelations(leftRel,
        getChild(rightRel, 1))))
        || (opIsOr(leftRel.getOp(), PLUS, AMP, MINUS)
        && (sameRelations(rightRel, getChild(leftRel, 0)) || sameRelations(rightRel,
        getChild(leftRel, 1))));
  }
}
