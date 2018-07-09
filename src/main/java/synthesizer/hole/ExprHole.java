package synthesizer.hole;

import generator.fragment.Expression;
import java.util.List;

public class ExprHole extends Hole {

  public void setFragments(List<Expression> fragments) {
    this.fragments = fragments;
    this.value = null;
  }

  @Override
  public String toString() {
    updated = false;
    return value.getValue();
  }
}
