package generator.opt;

import edu.mit.csail.sdg.translator.A4Options;
import generator.etc.BoundType;
import parser.ast.visitor.PrettyStringVisitor;

public class GeneratorOpt {

  private BoundType boundType;
  private int maxDepthOrCost;
  private int maxArity;
  private int maxOpNum;
  private int scope;

  /**
   * Pretty printer
   */
  private PrettyStringVisitor psv;

  /**
   * A4Options for solving.
   */
  private A4Options options;

  /**
   * Use modulo pruning.
   */
  private boolean moduloPruning;

  private boolean enableCaching;

  public GeneratorOpt(BoundType boundType, int maxDepthOrCost, int maxArity, int maxOpNum,
      int scope, boolean enableCaching) {
    this.boundType = boundType;
    this.maxDepthOrCost = maxDepthOrCost;
    this.maxArity = maxArity;
    this.maxOpNum = maxOpNum;
    this.scope = scope;
    this.psv = new PrettyStringVisitor();
    this.options = new A4Options();
    options.noOverflow = true;
    this.moduloPruning = true;
    this.enableCaching = enableCaching;
  }

  public boolean boundOnDepth() {
    return boundType == BoundType.DEPTH;
  }

  public boolean boundOnCost() {
    return boundType == BoundType.COST;
  }

  public BoundType getBoundType() {
    return boundType;
  }

  public int getMaxDepthOrCost() {
    return maxDepthOrCost;
  }

  public int getMaxArity() {
    return maxArity;
  }

  public int getMaxOpNum() {
    return maxOpNum;
  }

  public int getScope() {
    return scope;
  }

  public PrettyStringVisitor getPSV() {
    return psv;
  }

  public A4Options getOptions() {
    return options;
  }

  public boolean isModuloPruning() {
    return moduloPruning;
  }

  public void setModuloPruning(boolean moduloPruning) {
    this.moduloPruning = moduloPruning;
  }

  public void setMaxDepthOrCost(int depthOrCost) {
    this.maxDepthOrCost = depthOrCost;
  }

  public boolean enableCaching() {
    return enableCaching;
  }
}
