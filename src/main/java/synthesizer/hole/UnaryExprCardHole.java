package synthesizer.hole;

import static generator.etc.Contants.LONE;
import static generator.etc.Contants.ONE;
import static generator.etc.Contants.SET;
import static generator.etc.Contants.SOME;

import com.google.common.collect.ImmutableList;

/**
 * This class represents holes in the unary expression cardinality.
 */
public class UnaryExprCardHole extends Hole {

  public UnaryExprCardHole() {
    this.fragments = ImmutableList.of(ONE, SET, LONE, SOME);
    this.value = ONE;
  }

  @Override
  public String toString() {
    updated = false;
    return value + " ";
  }
}
