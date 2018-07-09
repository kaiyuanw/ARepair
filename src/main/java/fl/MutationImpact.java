package fl;

public class MutationImpact {
  private String mutant;
  private int fail2pass;
  private int pass2fail;
  private double score;

  public MutationImpact(String mutant, int fail2pass, int pass2fail, double score) {
    this.mutant = mutant;
    this.fail2pass = fail2pass;
    this.pass2fail = pass2fail;
    this.score = score;
  }

  public String getMutant() {
    return mutant;
  }

  public int getFailToPass() {
    return fail2pass;
  }

  public int getPassToFail() {
    return pass2fail;
  }

  public double getScore() {
    return score;
  }
}
