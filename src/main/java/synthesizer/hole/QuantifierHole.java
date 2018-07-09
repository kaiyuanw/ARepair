package synthesizer.hole;

import static generator.etc.Contants.ALL;
import static generator.etc.Contants.LONE;
import static generator.etc.Contants.NO;
import static generator.etc.Contants.ONE;
import static generator.etc.Contants.SOME;

import com.google.common.collect.ImmutableList;

public class QuantifierHole extends Hole {

  public QuantifierHole() {
    // Maybe we do not need lone and one for quantifiers.
    this.fragments = ImmutableList.of(ALL, SOME, NO, ONE, LONE);
    this.value = ALL;
  }

  @Override
  public String toString() {
    updated = false;
    return value + " ";
  }
}
