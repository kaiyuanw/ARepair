package synthesizer.hole;

import static generator.etc.Contants.LONE;
import static generator.etc.Contants.NO;
import static generator.etc.Contants.ONE;
import static generator.etc.Contants.SOME;

import com.google.common.collect.ImmutableList;

/**
 * This class represents holes in the unary formula cardinality.
 */
public class UnaryFormulaCardHole extends Hole {

  public UnaryFormulaCardHole() {
    this.fragments = ImmutableList.of(SOME, NO, ONE, LONE);
    this.value = SOME;
  }

  @Override
  public String toString() {
    updated = false;
    return value + " ";
  }
}
