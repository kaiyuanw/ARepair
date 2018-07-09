package generator.fragment;

import static generator.etc.Contants.EMPTY;

import generator.etc.Card;
import java.util.List;
import java.util.Set;
import parser.ast.nodes.VarExpr;

/**
 * This class represents Alloy relations with any arity.
 */
public class Expression extends Fragment {

  private static final long serialVersionUID = -3573381903065302937L;

  /**
   * Arity of the relation.
   */
  private int arity;
  /**
   * Types of the relation, this should be a list of sigs.
   */
  private List<Type> types;
  /**
   * Contains identity relation;
   */
  private boolean hasIden;
  /**
   * Card of each type in the relation
   */
  private List<Card> cards;
  /**
   * Root operator.
   */
  private Fragment op;
  /**
   * Child fragments.
   */
  private List<Expression> subFragments;

  /**
   * Number of operators.
   */
  private int opNum;

  /**
   * Unique representation of the expression.  This is similar to the value field, but it stores the
   * identify of each leaf expression.  We use this field to cache the evaluator result.
   */
  private String repr;

  /**
   * The variables used in this expression.  The set should be ordered. We would use this field to
   * generate "let v1: D1 | let v2: D2 | ..." prefix to evaluate the expression.
   */
  private Set<VarExpr> usedVariables;

  public Expression(String value, int arity, List<Type> types, List<Card> cards, int opNum,
      String repr, Set<VarExpr> usedVariables) {
    this(value, 0, arity, types, false, cards, opNum, repr, usedVariables);
  }

  public Expression(String value, int cost, int arity, List<Type> types, boolean hasIden,
      List<Card> cards, int opNum, String repr, Set<VarExpr> usedVariables) {
    this(value, cost, arity, types, hasIden, cards, EMPTY, null, opNum, repr, usedVariables);
  }

  public Expression(String value, int cost, int arity, List<Type> types, boolean hasIden,
      List<Card> cards, Fragment op, List<Expression> subFragments, int opNum, String repr,
      Set<VarExpr> usedVariables) {
    super(value, cost);
    this.arity = arity;
    this.types = types;
    this.hasIden = hasIden;
    this.cards = cards;
    this.op = op;
    this.subFragments = subFragments;
    this.opNum = opNum;
    this.repr = repr;
    this.usedVariables = usedVariables;
  }

  public int getArity() {
    return arity;
  }

  public List<Type> getTypes() {
    return types;
  }

  public boolean hasIden() {
    return hasIden;
  }

  public List<Card> getCards() {
    return cards;
  }

  public Fragment getOp() {
    return op;
  }

  public List<Expression> getSubFragments() {
    return subFragments;
  }

  public int getOpNum() {
    return opNum;
  }

  public String getRepr() {
    return repr;
  }

  public Set<VarExpr> getUsedVariables() {
    return usedVariables;
  }

  @Override
  public String prettyString() {
    return "<value: " + getValue() + ", arity: " + arity
        + ", types: " + types + ", cards: " + cards + ", repr: " + repr + ">";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Expression) {
      Expression that = (Expression) obj;
      return getRepr().equals(that.getRepr());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getRepr().hashCode();
  }
}
