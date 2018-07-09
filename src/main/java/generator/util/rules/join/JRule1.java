package generator.util.rules.join;

import static generator.etc.Contants.DOT;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule1 extends BinaryRule {

  private JRule1(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule1 given(BinaryInfo binaryInfo) {
    return new JRule1(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return leftRel.getOp().equals(DOT) && getChild(leftRel, 1).getArity() >= 2;
  }
}
