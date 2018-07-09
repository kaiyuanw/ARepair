package generator.util.rules.difference;

import static generator.etc.Card.ONE;
import static generator.etc.Contants.ARROW;
import static generator.etc.Contants.STAR;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class DRule10 extends BinaryRule {

  private DRule10(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static DRule10 given(BinaryInfo binaryInfo) {
    return new DRule10(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel.getOp(), ARROW) && opIsOr(rightRel.getOp(), STAR)
        && sameRelations(getChild(leftRel, 0), getChild(leftRel, 1))
        && getChild(leftRel, 0).getArity() == 1
        && getChild(leftRel, 0).getCards().get(0).equals(ONE);
  }
}
