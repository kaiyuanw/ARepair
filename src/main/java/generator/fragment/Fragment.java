package generator.fragment;

import java.io.Serializable;

/**
 * This class represents the concrete values of each hole.
 */
public class Fragment implements Serializable {

  private static final long serialVersionUID = -8113153588441155660L;
  protected String value;
  protected int cost;

  public Fragment(String value) {
    this(value, 1);
  }

  public Fragment(String value, int cost) {
    this.value = value;
    this.cost = cost;
  }

  public String getValue() {
    return value;
  }

  public int getCost() {
    return cost;
  }

  /**
   * This is used mainly for debugging purpose.
   */
  public String prettyString() {
    return getValue();
  }

  @Override
  public String toString() {
    return getValue();
  }
}
