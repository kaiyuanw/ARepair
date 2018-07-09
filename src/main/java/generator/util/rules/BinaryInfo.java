package generator.util.rules;

import generator.fragment.Expression;
import generator.fragment.Fragment;
import java.util.Map;

public class BinaryInfo {

  private Fragment op;
  private Expression leftRel;
  private int leftDepth;
  private Expression rightRel;
  private int rightDepth;
  private Map<String, String> inheritanceMap;

  public BinaryInfo(Fragment op, Expression leftRel,
      int leftDepth, Expression rightRel, int rightDepth, Map<String, String> inheritanceMap) {
    this.op = op;
    this.leftRel = leftRel;
    this.leftDepth = leftDepth;
    this.rightRel = rightRel;
    this.rightDepth = rightDepth;
    this.inheritanceMap = inheritanceMap;
  }

  public Fragment getOp() {
    return op;
  }

  public Expression getLeftRel() {
    return leftRel;
  }

  public int getLeftDepth() {
    return leftDepth;
  }

  public Expression getRightRel() {
    return rightRel;
  }

  public int getRightDepth() {
    return rightDepth;
  }

  public Map<String, String> getInheritanceMap() {
    return inheritanceMap;
  }
}
