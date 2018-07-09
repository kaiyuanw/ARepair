package generator.util.rules.join;

import static generator.etc.Contants.ARROW;
import static generator.util.Util.isSuperType;

import generator.fragment.Expression;
import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule13 extends BinaryRule {

  private JRule13(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule13 given(BinaryInfo binaryInfo) {
    return new JRule13(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    if (opIsOr(leftRel.getOp(), ARROW)) {
      Expression leftSubRel = getChild(leftRel, 1);
      if (sameRelations(leftSubRel, rightRel)
          && rightRel.getArity() == 1
          && isSuperType(rightRel, inheritanceMap)
          && !getChild(leftRel, 0).hasIden()
          && getChild(leftRel, 0).getTypes().contains(rightRel.getTypes().get(0))) {
        return true;
      }
    }
    if (opIsOr(rightRel.getOp(), ARROW)) {
      Expression rightSubRel = getChild(rightRel, 0);
      if (sameRelations(leftRel, rightSubRel)
          && leftRel.getArity() == 1
          && isSuperType(leftRel, inheritanceMap)
          && !getChild(rightRel, 1).hasIden()
          && getChild(rightRel, 1).getTypes().contains(leftRel.getTypes().get(0))) {
        return true;
      }
    }
    return false;
  }
}
