package generator.util.rules.join;

import static generator.etc.Contants.TILDE;
import static generator.util.Util.buildExpression;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule9 extends BinaryRule {

  private JRule9(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule9 given(BinaryInfo binaryInfo) {
    return new JRule9(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return consecutiveNodesUnderOps(buildExpression(-1, op, leftRel, rightRel, inheritanceMap), op,
        (prev, cur) -> opIsOr(prev.getOp(), TILDE) && opIsOr(cur.getOp(), TILDE));
  }
}
