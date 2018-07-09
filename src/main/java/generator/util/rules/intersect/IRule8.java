package generator.util.rules.intersect;

import static generator.etc.Contants.PLUS;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class IRule8 extends BinaryRule {

  private IRule8(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static IRule8 given(BinaryInfo binaryInfo) {
    return new IRule8(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return (opIsOr(leftRel.getOp(), PLUS) && (sameRelations(getChild(leftRel, 0), rightRel)
        || sameRelations(getChild(leftRel, 1), rightRel)))
        || (opIsOr(rightRel.getOp(), PLUS) && (sameRelations(getChild(rightRel, 0), leftRel)
        || sameRelations(getChild(rightRel, 1), leftRel)));
  }
}
