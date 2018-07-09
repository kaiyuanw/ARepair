package generator.util.rules.transpose;

import static generator.etc.Contants.AMP;
import static generator.etc.Contants.DOT;
import static generator.etc.Contants.MINUS;
import static generator.etc.Contants.PLUS;
import static generator.etc.Contants.TILDE;

import generator.util.rules.UnaryInfo;
import generator.util.rules.UnaryRule;

public class TRule4 extends UnaryRule {

  private TRule4(UnaryInfo unaryInfo) {
    super(unaryInfo);
  }

  public static TRule4 given(UnaryInfo unaryInfo) {
    return new TRule4(unaryInfo);
  }

  @Override
  public boolean isPruned() {
    return opIsOr(rel.getOp(), PLUS, AMP, MINUS, DOT)
        && (opIsOr(getChild(rel, 0).getOp(), TILDE) || opIsOr(getChild(rel, 1).getOp(), TILDE));
  }
}
