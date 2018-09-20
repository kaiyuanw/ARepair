package patcher.opt;

import parser.opt.Opt;
import patcher.etc.SearchStrategy;

public class PatcherOpt extends Opt {

  protected final String testSuitePath;
  protected final SearchStrategy searchStrategy;
  /**
   * Whether we want to enable caching of sub formulas and expressions.
   */
  protected final boolean enableCaching;
  protected final int minimumCost;
  protected final int maxTryPerHole;
  protected final int partitionNum;
  protected final int maxTryPerDepth;

  public PatcherOpt(String modelPath, String testPath, int scope, SearchStrategy searchStrategy,
      boolean enableCaching, int minimumCost, int maxTryPerHole, int partitionNum,
      int maxTryPerDepth) {
    super(modelPath, null, scope);
    this.testSuitePath = testPath;
    this.searchStrategy = searchStrategy;
    this.enableCaching = enableCaching;
    this.minimumCost = minimumCost;
    this.maxTryPerHole = maxTryPerHole;
    this.partitionNum = partitionNum;
    this.maxTryPerDepth = maxTryPerDepth;
  }

  public String getTestSuitePath() {
    return testSuitePath;
  }

  public SearchStrategy getSearchStrategy() {
    return searchStrategy;
  }

  public boolean enableCaching() {
    return enableCaching;
  }

  public int getMinimumCost() {
    return minimumCost;
  }

  public int getMaxTryPerHole() {
    return maxTryPerHole;
  }

  public int getPartitionNum() {
    return partitionNum;
  }

  public int getMaxTryPerDepth() {
    return maxTryPerDepth;
  }
}
