package synthesizer.util;

import parser.ast.nodes.Node;

public class DepthInfo {

  private int lastVisitedDepth;
  private int lastFaultyNodeId;
  private Class<? extends Node> lastFaultyNodeType;

  public DepthInfo() {
    this.lastVisitedDepth = Integer.MAX_VALUE;
    this.lastFaultyNodeId = -1;
    this.lastFaultyNodeType = null;
  }

  public void setLastVisitedDepth(Integer lastVisitedDepth) {
    this.lastVisitedDepth = lastVisitedDepth;
  }

  public Integer getLastVisitedDepth() {
    return lastVisitedDepth;
  }

  public void setLastFaultyNodeId(int lastFaultyNodeId) {
    this.lastFaultyNodeId = lastFaultyNodeId;
  }

  public int getLastFaultyNodeId() {
    return lastFaultyNodeId;
  }

  public Class<? extends Node> getLastFaultyNodeType() {
    return lastFaultyNodeType;
  }

  public void setLastFaultyNodeType(Class<? extends Node> lastFaultyNodeType) {
    this.lastFaultyNodeType = lastFaultyNodeType;
  }
}
