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

  public PatcherOpt(String modelPath, String testPath, int scope, SearchStrategy searchStrategy,
      boolean enableCaching, int minimumCost) {
    super(modelPath, null, scope);
    this.testSuitePath = testPath;
    this.searchStrategy = searchStrategy;
    this.enableCaching = enableCaching;
    this.minimumCost = minimumCost;
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
}
