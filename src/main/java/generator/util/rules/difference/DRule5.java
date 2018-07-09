package generator.util.rules.difference;

import static generator.etc.Contants.AMP;
import static generator.etc.Contants.MINUS;
import static generator.etc.Contants.PLUS;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class DRule5 extends BinaryRule {

  private DRule5(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static DRule5 given(BinaryInfo binaryInfo) {
    return new DRule5(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), PLUS, AMP, MINUS)
        && (sameRelations(rightRel, getChild(leftRel, 0))
        || sameRelations(rightRel, getChild(leftRel, 1)));
  }
}
