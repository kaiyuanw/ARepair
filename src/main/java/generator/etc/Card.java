package generator.etc;

public enum Card {
  LONE("lone"),
  ONE("one"),
  SOME("some"),
  SET("set"),
  EMPTY("empty");

  private final String label;

  Card(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
