package generator.util.rules.join;

import static generator.etc.Contants.STAR;
import static generator.util.Util.buildExpression;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule6 extends BinaryRule {

  private JRule6(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule6 given(BinaryInfo binaryInfo) {
    return new JRule6(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return consecutiveNodesUnderOps(buildExpression(-1, op, leftRel, rightRel, inheritanceMap), op,
        (prev, cur) -> (opIsOr(prev.getOp(), STAR) && sameRelations(getChild(prev, 0), cur))
            || (opIsOr(cur.getOp(), STAR) && sameRelations(prev, getChild(cur, 0))));
  }
}
