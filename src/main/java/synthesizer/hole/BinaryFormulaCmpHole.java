package synthesizer.hole;

import static generator.etc.Contants.EQ;
import static generator.etc.Contants.IN;
import static generator.etc.Contants.NEQ;
import static generator.etc.Contants.NIN;

import com.google.common.collect.ImmutableList;

public class BinaryFormulaCmpHole extends Hole {

  public BinaryFormulaCmpHole() {
    this.fragments = ImmutableList.of(EQ, NEQ, IN, NIN);
    this.value = EQ;
  }

  @Override
  public String toString() {
    updated = false;
    return " " + value + " ";
  }
}
