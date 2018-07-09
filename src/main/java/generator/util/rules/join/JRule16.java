package generator.util.rules.join;

import static generator.etc.Contants.UNIV_EXPR;
import static generator.etc.Contants.UNIV_STRING;

import generator.util.rules.BinaryInfo;
import generator.util.rules.BinaryRule;

public class JRule16 extends BinaryRule {

  private JRule16(BinaryInfo binaryInfo) {
    super(binaryInfo);
  }

  public static JRule16 given(BinaryInfo binaryInfo) {
    return new JRule16(binaryInfo);
  }

  @Override
  public boolean isPruned() {
    return (leftRel == UNIV_EXPR && !rightRel.getTypes().get(0).getGenType().equals(UNIV_STRING))
        || (rightRel == UNIV_EXPR && !leftRel.getTypes().get(leftRel.getTypes().size() - 1)
        .getGenType().equals(UNIV_STRING));
  }
}
