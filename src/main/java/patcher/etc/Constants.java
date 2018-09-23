package patcher.etc;

import static parser.etc.Names.DOT_ALS;
import static parser.etc.Names.HIDDEN_DIR_PATH;

import generator.etc.BoundType;
import java.nio.file.Paths;

public class Constants {

  public static final String FIX_FILE_PATH = Paths.get(HIDDEN_DIR_PATH, "fix" + DOT_ALS).toString();

  public static final BoundType BOUND_TYPE = BoundType.COST;
  public static final int MAX_DEPTH_OR_COST = 3;
  public static final int MAX_ARITY = 3;
  public static final int MAX_OP_NUM = Integer.MAX_VALUE;
  public static final int COST_STRIDE = 3;
  // For fault localization.
  // The minimum suspiciousness score to consider the mutation as a fix.
  public static final double SUSPICIOUSNESS_THRESHOLD = 0.95;
  // For basic choice strategy.
  public static final int DEFAULT_NUM_TRY_PER_HOLE = 1000;
  // For all combination strategy.
  public static final int DEFAULT_PARTITION_NUM = 10;
  public static final int DEFAULT_NUM_TRY_NUM_PER_DEPTH = 10000;

  // Commandline arguments.
  public static final int CLI_USAGE_DESCRIPTION_WIDTH = 1000;
  public static final String MODEL_PATH = "model-path";
  public static final String TEST_PATH = "test-path";
  public static final String SCOPE = "scope";
  public static final String MINIMUM_COST = "minimum-cost";
  public static final String SEARCH_STRATEGY = "search-strategy";
  public static final String ENABLE_CACHE = "enable-cache";
  // For basic choice strategy.
  public static final String MAX_TRY_PER_HOLE = "max-try-per-hole";
  // For all combination strategy.
  public static final String PARTITION_NUM = "partition-num";
  public static final String MAX_TRY_NUM_PER_DEPTH = "max-try-per-depth";

  /**
   * Return the cost of the height.
   */
  public static int getCostByHeight(int height, int lowestCost) {
    if (height < 3) {
      return height + lowestCost;
    }
    return 6;
  }

  /**
   * If COST_STRIDE is 3 and minimumCost is 3, then the costs from deepest level to higher levels are
   * given by 3, 4, 5, 6, 6, 6...
   */
  public static int getCostByDepth(int depth, int maxDepth, int lowestCost) {
    if (maxDepth - depth < COST_STRIDE) {
      return maxDepth - depth + lowestCost;
    }
    return lowestCost + COST_STRIDE;
  }
}
