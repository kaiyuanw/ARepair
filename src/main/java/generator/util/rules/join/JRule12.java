package generator.util.rules.join;

import static generator.etc.Contants.TILDE;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule12 extends BinaryRule {

  private JRule12(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule12 given(BinaryInfo binaryInfo) {
    return new JRule12(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(leftRel, TILDE) && rightRel.getArity() == 1;
  }
}
