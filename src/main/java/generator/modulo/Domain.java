package generator.modulo;

import static generator.etc.Contants.PLUS;

import edu.mit.csail.sdg.translator.A4Tuple;
import edu.mit.csail.sdg.translator.A4TupleSet;
import generator.etc.Card;
import generator.util.TypeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Domain {

  private List<String> atoms;
  /**
   * Cardinality determines the combinations.  If the cardinality is set, then we need to do 2^n
   * combinations.  Otherwise, we only need to consider n combinations.
   */
  private TypeInfo typeInfo;
  private boolean isTupleSet;
  private boolean isInteger;
  private List<String> combinations;

  public Domain(TypeInfo typeInfo, Object object) {
    this.typeInfo = typeInfo;
    this.atoms = new ArrayList<>();
    if (object instanceof A4TupleSet) {
      for (A4Tuple tuple : (A4TupleSet) object) {
        atoms.add(tuple.toString());
      }
      isTupleSet = true;
    } else {
      if (object instanceof Integer) {
        isInteger = true;
      }
      atoms.add(String.valueOf(object));
    }
    this.combinations = initializeCombinations();
  }

  private List<String> initializeCombinations() {
    List<String> combinations = new ArrayList<>();
    if (isTupleSet) {
      assert typeInfo.getArity() >= 1;
      if (typeInfo.getArity() > 1 || typeInfo.getCards().get(0) == Card.SET) {
        backtrack(combinations, new Stack<>(), 0);
      } else {
        combinations.addAll(atoms);
      }
      // We do not consider the cardinality in case if we want to synthesize variable domains.
      // TODO(kaiyuanw): Since we do not modify unary expressions that specify cardinalities.
      // We can use the optimization for now.
    } else if (isInteger) {
      combinations.addAll(atoms);
    }
    return combinations;
  }

  private void backtrack(List<String> res, Stack<String> path, int start) {
    if (path.isEmpty()) {
    } else {
      res.add(String.join(PLUS.getValue(), path));
    }
    for (int i = start; i < atoms.size(); i++) {
      path.push(atoms.get(i));
      backtrack(res, path, i + 1);
      path.pop();
    }
  }

  public List<String> getAtoms() {
    return atoms;
  }

  public List<String> getCombinations() {
    return combinations;
  }
}
