package synthesizer.hole;

import static generator.etc.Contants.IFF;
import static generator.etc.Contants.IMPLIES;

import com.google.common.collect.ImmutableList;

public class BinaryFormulaLogicHole extends Hole {

  public BinaryFormulaLogicHole() {
    this.fragments = ImmutableList.of(IMPLIES, IFF);
    this.value = IMPLIES;
  }

  @Override
  public String toString() {
    updated = false;
    return " " + value + " ";
  }
}
