package synthesizer.hole;

import static generator.etc.Contants.EMPTY;
import static generator.etc.Contants.NOT;

import com.google.common.collect.ImmutableList;

/**
 * This class represents holes in the negation of a formula.
 */
public class UnaryFormulaBoolHole extends Hole {

  public UnaryFormulaBoolHole() {
    this.fragments = ImmutableList.of(NOT, EMPTY);
    this.value = EMPTY;
  }

  @Override
  public String toString() {
    updated = false;
    return value.getValue();
  }
}
