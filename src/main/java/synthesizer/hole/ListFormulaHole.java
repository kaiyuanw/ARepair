package synthesizer.hole;

import static generator.etc.Contants.AND;
import static generator.etc.Contants.OR;

import com.google.common.collect.ImmutableList;

public class ListFormulaHole extends Hole {

  public ListFormulaHole() {
    this.fragments = ImmutableList.of(AND, OR);
    this.value = AND;
  }

  @Override
  public String toString() {
    updated = false;
    return " " + value + " ";
  }
}
