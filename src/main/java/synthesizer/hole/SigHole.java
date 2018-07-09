package synthesizer.hole;

import static generator.etc.Contants.LONE;
import static generator.etc.Contants.ONE;
import static generator.etc.Contants.SET;
import static generator.etc.Contants.SOME;

import com.google.common.collect.ImmutableList;
import generator.fragment.Fragment;

/**
 * This class represents holes in the signature multiplicity.
 */
public class SigHole extends Hole {

  public SigHole() {
    this.fragments = ImmutableList.of(ONE, SET, LONE, SOME);
    this.value = ONE;
  }


  @Override
  public String toString()  {
    updated = false;
    if (value == SET) {
      return "";
    }
    return value + " ";
  }
}
