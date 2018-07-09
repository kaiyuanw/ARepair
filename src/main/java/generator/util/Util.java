package generator.util;

import static generator.etc.Contants.AMP;
import static generator.etc.Contants.ARROW;
import static generator.etc.Contants.CARET;
import static generator.etc.Contants.DOT;
import static generator.etc.Contants.HASH;
import static generator.etc.Contants.IDEN_EXPR;
import static generator.etc.Contants.INT_TYPE;
import static generator.etc.Contants.MINUS;
import static generator.etc.Contants.NONE_STRING;
import static generator.etc.Contants.ONE_STRING;
import static generator.etc.Contants.PLUS;
import static generator.etc.Contants.SPECIAL_TYPE_TO_EXPRESSIONS;
import static generator.etc.Contants.STAR;
import static generator.etc.Contants.TILDE;
import static generator.etc.Contants.UNIV_STRING;
import static generator.etc.Contants.ZERO_STRING;
import static parser.etc.Names.DOLLAR;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import generator.etc.Card;
import generator.fragment.Expression;
import generator.fragment.Fragment;
import generator.fragment.Type;
import generator.opt.GeneratorOpt;
import generator.util.rules.BinaryInfo;
import generator.util.rules.PruningRule;
import generator.util.rules.UnaryInfo;
import generator.util.rules.closure.VRule1;
import generator.util.rules.closure.VRule2;
import generator.util.rules.closure.VRule3;
import generator.util.rules.crossproduct.CRule1;
import generator.util.rules.difference.DRule1;
import generator.util.rules.difference.DRule10;
import generator.util.rules.difference.DRule11;
import generator.util.rules.difference.DRule2;
import generator.util.rules.difference.DRule3;
import generator.util.rules.difference.DRule4;
import generator.util.rules.difference.DRule5;
import generator.util.rules.difference.DRule6;
import generator.util.rules.difference.DRule7;
import generator.util.rules.difference.DRule8;
import generator.util.rules.difference.DRule9;
import generator.util.rules.intersect.IRule1;
import generator.util.rules.intersect.IRule2;
import generator.util.rules.intersect.IRule3;
import generator.util.rules.intersect.IRule4;
import generator.util.rules.intersect.IRule5;
import generator.util.rules.intersect.IRule6;
import generator.util.rules.intersect.IRule7;
import generator.util.rules.intersect.IRule8;
import generator.util.rules.join.JRule1;
import generator.util.rules.join.JRule10;
import generator.util.rules.join.JRule11;
import generator.util.rules.join.JRule12;
import generator.util.rules.join.JRule13;
import generator.util.rules.join.JRule14;
import generator.util.rules.join.JRule15;
import generator.util.rules.join.JRule16;
import generator.util.rules.join.JRule2;
import generator.util.rules.join.JRule3;
import generator.util.rules.join.JRule4;
import generator.util.rules.join.JRule5;
import generator.util.rules.join.JRule6;
import generator.util.rules.join.JRule7;
import generator.util.rules.join.JRule8;
import generator.util.rules.join.JRule9;
import generator.util.rules.rclosure.RRule1;
import generator.util.rules.rclosure.RRule2;
import generator.util.rules.transpose.TRule1;
import generator.util.rules.transpose.TRule2;
import generator.util.rules.transpose.TRule3;
import generator.util.rules.transpose.TRule4;
import generator.util.rules.transpose.TRule5;
import generator.util.rules.union.URule1;
import generator.util.rules.union.URule2;
import generator.util.rules.union.URule3;
import generator.util.rules.union.URule4;
import generator.util.rules.union.URule5;
import generator.util.rules.union.URule6;
import generator.util.rules.union.URule7;
import generator.util.rules.union.URule8;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.ConstExpr;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.Node;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.VarExpr;

public class Util {

  public static Type createType(String type) {
    return createType(type, type);
  }

  public static Type createType(String genType, String pruningType) {
    return new Type(genType, pruningType);
  }

  public static List<Type> createTypes(Type type, int repeat) {
    List<Type> types = new ArrayList<>();
    for (int i = 0; i < repeat; i++) {
      types.add(type);
    }
    return types;
  }

  public static List<Type> createStarTypes(List<Type> types) {
    List<Type> newTypes = new ArrayList<>();
    for (Type type : types) {
      newTypes.add(createType(type.getGenType(), UNIV_STRING));
    }
    return newTypes;
  }

  public static boolean opIsOr(Fragment op, Fragment... cands) {
    for (Fragment cand : cands) {
      if (op.getValue().equals(cand.getValue())) {
        return true;
      }
    }
    return false;
  }

  public static boolean isCommutative(Fragment op) {
    return opIsOr(op, PLUS, AMP);
  }

  public static Card findMult(UnaryExpr expr) {
    switch (expr.getOp()) {
      case LONE:
        return Card.LONE;
      case ONE:
        return Card.ONE;
      case SOME:
        return Card.SOME;
      case SET:
        return Card.SET;
      default:
        return null;
    }
  }

  public static Fragment findOp(UnaryExpr expr) {
    switch (expr.getOp()) {
      case TRANSPOSE:
        return TILDE;
      case RCLOSURE:
        return STAR;
      case CLOSURE:
        return CARET;
      case CARDINALITY:
        return HASH;
      default:
        throw new RuntimeException("Unsupported unary operator " + expr.getOp());
    }
  }

  public static Fragment findOp(BinaryExpr expr) {
    switch (expr.getOp()) {
      case PLUS:
        return PLUS;
      case INTERSECT:
        return AMP;
      case MINUS:
        return MINUS;
      case ARROW:
      case ANY_ARROW_SOME:
      case ANY_ARROW_ONE:
      case ANY_ARROW_LONE:
      case SOME_ARROW_ANY:
      case SOME_ARROW_SOME:
      case SOME_ARROW_ONE:
      case SOME_ARROW_LONE:
      case ONE_ARROW_ANY:
      case ONE_ARROW_SOME:
      case ONE_ARROW_ONE:
      case ONE_ARROW_LONE:
      case LONE_ARROW_ANY:
      case LONE_ARROW_SOME:
      case LONE_ARROW_ONE:
      case LONE_ARROW_LONE:
      case ISSEQ_ARROW_LONE:
        return ARROW;
      case JOIN:
        return DOT;
      default:
        throw new RuntimeException("Unsupported binary operator " + expr.getOp());
    }
  }

  public static Card findCardinality(SigDecl sigDecl) {
    switch (sigDecl.getMult()) {
      case LONE:
        return Card.LONE;
      case ONE:
        return Card.ONE;
      case SOME:
        return Card.SOME;
      case SET:
        return Card.SET;
      default:
        throw new RuntimeException("Unsupported sig multiplicity " + sigDecl.getMult());
    }
  }

  private static final List<Card> CARD_LIST = ImmutableList
      .of(Card.EMPTY, Card.LONE, Card.ONE, Card.SOME, Card.SET);

  public static int compareCard(Card left, Card right) {
    return CARD_LIST.indexOf(left) - CARD_LIST.indexOf(right);
  }

  public static Card minCard(Card... cards) {
    Card minCard = Card.SET;
    for (Card card : cards) {
      if (compareCard(minCard, card) > 0) {
        minCard = card;
      }
    }
    return minCard;
  }

  public static Card maxCard(Card... cards) {
    Card maxCard = Card.EMPTY;
    for (Card card : cards) {
      if (compareCard(maxCard, card) < 0) {
        maxCard = card;
      }
    }
    return maxCard;
  }

  public static List<Card> createCards(Card card, int repeat) {
    List<Card> cardinalities = new ArrayList<>();
    for (int i = 0; i < repeat; i++) {
      cardinalities.add(card);
    }
    return cardinalities;
  }

  /**
   * Find the lowest common ancestor type for both leftType and rightType.  If no LCA is found,
   * return null.
   */
  public static String getLCAType(String leftType, String rightType,
      Map<String, String> inheritanceMap) {
    if (leftType != null && leftType.equals(rightType)) {
      // We need this to handle the case where the signature is not in the inheritance map.
      // E.g. Int, String, etc.
      return leftType;
    }
    Set<String> leftChain = new HashSet<>();
    while (inheritanceMap.containsKey(leftType)) {
      leftChain.add(leftType);
      leftType = inheritanceMap.get(leftType);
    }
    while (inheritanceMap.containsKey(rightType)) {
      if (leftChain.contains(rightType)) {
        return rightType;
      }
      rightType = inheritanceMap.get(rightType);
    }
    return null;
  }

  public static String getLCATypeWithUniv(String leftType, String rightType,
      Map<String, String> inheritanceMap) {
    String lcaType = getLCAType(leftType, rightType, inheritanceMap);
    if (lcaType != null) {
      return lcaType;
    }
    return UNIV_STRING;
  }

  /**
   * Find the greatest common descendant type for both leftType and rightType.  This returns either
   * leftType or rightType.  If leftType and rightType are not in the same inheritance hierarchy,
   * return null.
   */
  public static String getMinimumType(String leftType, String rightType,
      Map<String, String> inheritanceMap) {
    String lcaType = getLCATypeWithUniv(leftType, rightType, inheritanceMap);
    if (leftType.equals(lcaType)) {
      return rightType;
    }
    if (rightType.equals(lcaType)) {
      return leftType;
    }
    return lcaType;
  }

  /**
   * Create a new expression fragment with the binary op, left operand and right operand.
   */
  public static Expression buildExpression(int cost, Fragment op, Expression leftExpr,
      Expression rightExpr, Map<String, String> inheritanceMap) {
    String newValue = "(" + leftExpr.getValue() + op + rightExpr.getValue() + ")";
    int newArity = 0;
    int leftExprArity = leftExpr.getArity();
    int rightExprArity = rightExpr.getArity();
    List<Type> newTypes = new ArrayList<>();
    List<Type> leftTypes = leftExpr.getTypes();
    List<Type> rightTypes = rightExpr.getTypes();
    List<Card> newCards = new ArrayList<>();
    List<Card> leftCards = leftExpr.getCards();
    List<Card> rightCards = rightExpr.getCards();
    boolean hasIden = false;
    int newOpNum = leftExpr.getOpNum() + 1 + rightExpr.getOpNum();
    String newRepr = "(" + leftExpr.getRepr() + op + rightExpr.getRepr() + ")";
    Set<VarExpr> usedVariables = orderedSet(leftExpr.getUsedVariables(),
        rightExpr.getUsedVariables());
    // +, - and & does not alternate the arity.
    if (op == PLUS) {
      newArity = leftExprArity;
      for (int i = 0; i < leftExprArity; i++) {
        // We do not allow S + T in the isValidExpression check, but if the expression is valid,
        // then we still correctly compute the generation type.  Note that the lca type of univ and
        // some other type is always null.
        String lcaGenType = getLCATypeWithUniv(leftTypes.get(i).getGenType(),
            rightTypes.get(i).getGenType(), inheritanceMap);
        String lcaPruningType = getLCATypeWithUniv(leftTypes.get(i).getPruneType(),
            rightTypes.get(i).getPruneType(), inheritanceMap);
        newTypes.add(createType(lcaGenType, lcaPruningType));
      }
      newCards.addAll(createCards(Card.SET, leftExprArity));
      if (leftExpr.hasIden() || rightExpr.hasIden()) {
        hasIden = true;
      }
    }
    if (op == AMP) {
      newArity = leftExprArity;
      for (int i = 0; i < leftExprArity; i++) {
        String minimumGenType = getMinimumType(leftTypes.get(i).getGenType(),
            rightTypes.get(i).getGenType(), inheritanceMap);
        String minimumPruneType = getMinimumType(leftTypes.get(i).getPruneType(),
            rightTypes.get(i).getPruneType(), inheritanceMap);
        newTypes.add(createType(minimumGenType, minimumPruneType));
      }
      newCards.addAll(leftCards);
      if (leftExpr.hasIden() && rightExpr.hasIden()) {
        hasIden = true;
      }
    }
    if (op == MINUS) {
      newArity = leftExprArity;
      for (int i = 0; i < leftExprArity; i++) {
        newTypes.add(createType(leftTypes.get(i).getGenType(), leftTypes.get(i).getPruneType()));
      }
      newCards.addAll(leftCards);
      if (leftExpr.hasIden()) {
        hasIden = true;
      }
    }
    // -> increases the arity to leftRelArity + rightRelArity.
    if (op == ARROW) {
      newArity = leftExprArity + rightExprArity;
      newTypes.addAll(leftTypes);
      newTypes.addAll(rightTypes);
      newCards.addAll(leftCards);
      newCards.addAll(rightCards);
    }
    // . decreases the arity by 2.
    if (op == DOT) {
      newArity = leftExprArity + rightExprArity - 2;
      // If leftRel has identity, it must be binary relation.
      for (int i = 0; i < leftTypes.size() - 1; i++) {
        newTypes.add(leftExpr.hasIden() ? rightTypes.get(0) : leftTypes.get(i));
        newCards.add(leftCards.get(i));
      }
      // If rightRel has identity, it must be binary relation.
      for (int i = 1; i < rightTypes.size(); i++) {
        newTypes.add(rightExpr.hasIden() ? leftTypes.get(leftTypes.size() - 1) : rightTypes.get(i));
        newCards.add(rightCards.get(i));
      }
    }
    return new Expression(newValue, cost, newArity, newTypes, hasIden, newCards, op,
        ImmutableList.of(leftExpr, rightExpr), newOpNum, newRepr, usedVariables);
  }

  public static Expression buildExpression(int cost, Fragment op, Expression expr) {
    String newValue = "(" + op + expr.getValue() + ")";
    List<Type> newTypes = new ArrayList<>();
    List<Type> exprTypes = expr.getTypes();
    List<Card> newCards = new ArrayList<>();
    List<Card> exprCards = expr.getCards();
    boolean hasIden = expr.hasIden();
    int newOpNum = 1 + expr.getOpNum();
    String newRepr = "(" + op + expr.getRepr() + ")";
    Set<VarExpr> usedVariables = orderedSet(expr.getUsedVariables());
    // If op is ~, then types and cardinality should be reversed.
    if (op == TILDE) {
      for (int i = exprTypes.size() - 1; i >= 0; i--) {
        newTypes.add(exprTypes.get(i));
        newCards.add(exprCards.get(i));
      }
    }
    // If op is *, then the pruning type should be univ.
    if (op == STAR) {
      for (int i = 0; i < exprTypes.size(); i++) {
        newTypes.add(createType(exprTypes.get(i).getGenType(), UNIV_STRING));
        newCards.add(exprCards.get(i));
      }
      hasIden = true;
    }
    // If op is ^, then the type should be same as the operand.
    if (op == CARET) {
      for (int i = 0; i < exprTypes.size(); i++) {
        newTypes.add(exprTypes.get(i));
        newCards.add(exprCards.get(i));
      }
    }
    // If op is #, then the type should be int.
    if (op == HASH) {
      newTypes.add(INT_TYPE);
      newCards.add(Card.ONE);
    }
    return new Expression(newValue, cost, expr.getArity(), newTypes, hasIden, newCards, op,
        ImmutableList.of(expr), newOpNum, newRepr, usedVariables);
  }

  /**
   * Create special relations like none, none->none,... and univ, univ->univ, ... Note that the expr
   * argument must be of arity 1.
   *
   * @param repeats is the number of times to use ->, normally this is equal to the arity.  But for
   * iden, this should be 1.
   */
  public static Expression buildExpression(Expression expr, int repeats, GeneratorOpt opt) {
    StringBuilder newValue = new StringBuilder();
    int cost = opt.boundOnDepth() ? (repeats - 1) : (2 * repeats - 1);
    List<Type> newTypes = new ArrayList<>();
    List<Card> newCards = new ArrayList<>();
    for (int repeat = 0; repeat < repeats; repeat++) {
      newValue.append(expr.getValue());
      if (repeat != repeats - 1) {
        newValue.append("->");
      }
      newTypes.addAll(expr.getTypes());
      newCards.addAll(expr.getCards());
    }
    return new Expression("(" + newValue.toString() + ")", cost, repeats, newTypes, false, newCards,
        repeats - 1, "(" + newValue.toString() + ")" + DOLLAR + repeats, expr.getUsedVariables());
  }

  private static final Set<String> IGNORED_EXPRESSIONS = ImmutableSet
      .of(ZERO_STRING, ONE_STRING);

  public static boolean isValidExpression(Fragment op, Expression leftRel, Expression rightRel,
      Map<String, String> inheritanceMap) {
    // Do not generate expressions using 0 and 1 constants.
    if (IGNORED_EXPRESSIONS.contains(leftRel.getValue())
        || IGNORED_EXPRESSIONS.contains(rightRel.getValue())) {
      return false;
    }
    // Left and right operands must have the same type.
    if (opIsOr(op, PLUS, MINUS, AMP)) {
      if (leftRel.getArity() != rightRel.getArity()) {
        return false;
      }
      for (int i = 0; i < leftRel.getArity(); i++) {
        if (leftRel.getTypes().get(i).getGenType().equals(NONE_STRING)
            || rightRel.getTypes().get(i).getGenType().equals(NONE_STRING)) {
          return false;
        }
        if (getLCAType(leftRel.getTypes().get(i).getGenType(),
            rightRel.getTypes().get(i).getGenType(), inheritanceMap) == null
            && (leftRel != IDEN_EXPR && rightRel != IDEN_EXPR)) {
          return false;
        }
      }
      return true;
    }
    // The last type of the left operand must equal to
    // the first type of the right operand.
    if (opIsOr(op, DOT)) {
      List<Type> leftTypes = leftRel.getTypes();
      Type lastLeftType = leftTypes.get(leftTypes.size() - 1);
      List<Type> rightTypes = rightRel.getTypes();
      Type firstRightType = rightTypes.get(0);
      return !lastLeftType.getGenType().equals(NONE_STRING) &&
          !firstRightType.getGenType().equals(NONE_STRING) &&
          (getLCAType(lastLeftType.getGenType(), firstRightType.getGenType(), inheritanceMap)
              != null
              // TODO(kaiyuanw): Should remove this by implementing more advanced type inference.
              // The current implementation cannot remove n.header if n is List.header.*link with univ type.
              // Dynamic checking gives check { all n: univ | n.header = none } which yields counter-example.
              || lastLeftType.getGenType().equals(UNIV_STRING)
              || firstRightType.getGenType().equals(UNIV_STRING));
    }
    // Default to true.
    return true;
  }

  public static boolean isValidExpression(Fragment op, Expression rel) {
    // If any type is none, return false.
    for (int i = 0; i < rel.getArity(); i++) {
      if (rel.getTypes().get(i).getGenType().equals(NONE_STRING)) {
        return false;
      }
    }

    // Operand must be binary.
    if (opIsOr(op, TILDE)) {
      return rel.getTypes().size() == 2;
    }
    // Operand must be binary and homogeneous.
    if (opIsOr(op, STAR, CARET)) {
      return rel.getTypes().size() == 2 && rel.getTypes().get(0).equals(rel.getTypes().get(1));
    }
    // Default to true.
    return true;
  }

  /**
   * Return true if all leaves are super types.  Super type means value == type and type is top
   * level.  For example, Farmer extends Object so Farmer is not super type.
   */
  public static boolean isSuperType(Expression root, Map<String, String> inheritanceMap) {
    if (root.getSubFragments() == null) {
      String value = root.getValue();
      return root.getArity() == 1
          && value.equals(root.getTypes().get(0).getPruneType())
          && inheritanceMap.get(value) == null;
    }
    // Super type must be a single type or connected with cross product S, S->T, ...
    if (!opIsOr(root.getOp(), ARROW)) {
      return false;
    }
    for (Expression subRelation : root.getSubFragments()) {
      if (!isSuperType(subRelation, inheritanceMap)) {
        return false;
      }
    }
    return true;
  }

  private static List<Integer> indexesOf(String str, String substring) {
    List<Integer> indexes = new ArrayList<>();
    int lastIndex = 0;
    while (lastIndex != -1) {
      lastIndex = str.indexOf(substring, lastIndex);
      if (lastIndex != -1) {
        indexes.add(lastIndex);
        lastIndex += substring.length();
      }
    }
    return indexes;
  }

  public static boolean isStaticPruned(Fragment op, Expression leftRel, int leftDepth,
      Expression rightRel, int rightDepth, Map<String, String> inheritanceMap) {
    // Prune if (1) combining leftRel and rightRel mix pre and post state relations.
    // TODO(kaiyuanw): Initialize all pruning rules to control whether enable them or not.
    BinaryInfo binaryInfo = new BinaryInfo(op, leftRel, leftDepth, rightRel, rightDepth,
        inheritanceMap);
    if (opIsOr(op, PLUS)) {
      // Prune if
      List<PruningRule> pruningRulesForSetUnion = Arrays.asList(
          // (1) a + b if a is c + d
          URule1.given(binaryInfo),
          // (2) one of the operands is a super set
          URule2.given(binaryInfo),
          // (3) a + b where a is (c +|&|- b) or (b +|&|- c) (not possible as left depth is bigger),
          // or b is (a +|&|- c) or (c +|&|- a)
          URule3.given(binaryInfo),
          // (4) a.|->|&b + a.|->|&c; or a.|->|&b + c.|->|&b
          URule4.given(binaryInfo),
          // (5) *|^a + *|^a
          URule5.given(binaryInfo),
          // (6) ~a + ~b
          URule6.given(binaryInfo),
          // (7) a + *|^a, a.a + *|^a, a.a.a + *|^a, ...
          URule7.given(binaryInfo),
          // (8) a + b + ... where some pair of elements are same
          URule8.given(binaryInfo)
      );
      return pruningRulesForSetUnion.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }
    if (opIsOr(op, AMP)) {
      // Prune if
      List<PruningRule> pruningRulesForSetIntersect = Arrays.asList(
          // (1) a & b if a is c & d
          IRule1.given(binaryInfo),
          // (2) a & b if a or b is a super set and type(a) == type(b)
          IRule2.given(binaryInfo),
          // (3) a & b & ... where some pair of elements are same
          IRule3.given(binaryInfo),
          // (4) a->b & a->c; or a->b & c->b.
          // Wrong as a.b & a.c != a.(b&c)
          IRule4.given(binaryInfo),
          // (5) *|^a & *|^a
          IRule5.given(binaryInfo),
          // (6) ~a & ~b
          IRule6.given(binaryInfo),
          // (7) a & *|^a, a.a & *|^a, a.a.a & *|^a, ...
          IRule7.given(binaryInfo),
          // (8) a & (a + b) or a & (b + a), or (a + b) & a or (b + a) & a
          IRule8.given(binaryInfo)
      );
      return pruningRulesForSetIntersect.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }
    if (opIsOr(op, MINUS)) {
      // Prune if
      List<PruningRule> pruningRulesForSetDifference = Arrays.asList(
          // (1) rightRel is super type
          // Wrong as *a - A->A is not empty
          DRule1.given(binaryInfo),
          // (2) both operands are same
          DRule2.given(binaryInfo),
          // (3) a-(b+c) = (a-b)-c
          DRule3.given(binaryInfo),
          // (4) a - (a +|&|- b) or a - (b +|&|- a)
          DRule4.given(binaryInfo),
          // (5) (a +|&|- b) - b or (b +|&|- a) - b
          DRule5.given(binaryInfo),
          // (6) a->|&b - a->|&c; or a->|&b - c->|&b
          // Wrong as a.b - a.c != a.(b-c)
          DRule6.given(binaryInfo),
          // (7) a - (*|^a), a.a - (*|^a), ...
          DRule7.given(binaryInfo),
          // (8) (*|^a) - (*|^a)
          // Wrong as *a - ^a in iden
          DRule8.given(binaryInfo),
          // (9) ~a - ~b
          DRule9.given(binaryInfo),
          // (10) (a->a) - *b if a has cardinality 1
          DRule10.given(binaryInfo),
          // (11) (a & b) - c = a & (b - c)
          DRule11.given(binaryInfo)
      );
      return pruningRulesForSetDifference.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }
    // Prune if (1) a -> b, where b is c -> d.
    if (opIsOr(op, ARROW)) {
      // Prune if
      List<PruningRule> pruningRulesForCrossProduct = Arrays.asList(
          // (1) a -> b if a is c -> d
          CRule1.given(binaryInfo)
          // (2) (a.b)->c = a.(b->c), a->(b.c) = (a->b).c
          // Should not use because arity(b->c) may be bigger than the max arity.
//          CRule2.given(binaryInfo)
      );
      return pruningRulesForCrossProduct.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }
    if (opIsOr(op, DOT)) {
      // Prune if
      List<PruningRule> pruningRulesForRelationJoin = Arrays.asList(
          // (1) (a.b).c where arity(b) >= 2
          JRule1.given(binaryInfo),
          // (2) a.(a->b) or (b->a).a if card(a) >= 1
          // Wrong without card as (Node.(Node->BinaryTree)) can be either BinaryTree or empty.
          JRule2.given(binaryInfo),
          // (3) a.(~b) = b.a if arity(a) = 1
          JRule3.given(binaryInfo),
          // (4) a.(*b) = a and (*b).a = a if a is super type
          JRule4.given(binaryInfo),
          // (5) a.(^b) = a.b and (^b).a = b.a if a is super type
          JRule5.given(binaryInfo),
          // (6) a1.a2.a3.a4 where a(i) is b and a(i+1) is *b, or a(i) is *b and a(i+1) is b
          // b.*b = *b.b = ^b
          JRule6.given(binaryInfo),
          // (7) a1.a2.a3.a4 where a(i) is ^b and a(i+1) is b (^b.b = b.^b)
          JRule7.given(binaryInfo),
          // (8) a1.a2.a3.a4 where a(i).a(i+1) is *a.*a = *a, *a.^a = ^a.*a = ^a, ^a.^a = a.^a
          JRule8.given(binaryInfo),
          // (9) a1.a2.a3.a4 where a(i).a(i+1) is (~a).(~b) = ~(a.b)
          JRule9.given(binaryInfo),
          // (10) (a-...-b-...).(b->c) = (a->b).(c-...-b-...) = none
          JRule10.given(binaryInfo),
          // (11) a.((b-...-a-...)->c) = (c->(b-...-a-...)).a = none
          JRule11.given(binaryInfo),
          // (12) ~b.a = a.b if arity(a) = 1
          JRule12.given(binaryInfo),
          // (13) A.(A->b) or (b->A).A if arity(A) = 1 and type(b) contains A
          // and b does not contain identity.
          JRule13.given(binaryInfo),
          // (14) a.(b->c) = (a.b)->c if arity(a) + arity(b) > 2, similarly (a->b).c = a->(b.c)
          JRule14.given(binaryInfo),
          // (15) iden.r = r.iden = r
          JRule15.given(binaryInfo),
          // (16) univ.r = leftType(r).r, r.univ = r.rightType(r), if leftType(r) and rightType(r)
          // is not univ.
          JRule16.given(binaryInfo)
      );
      return pruningRulesForRelationJoin.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }
    return false;
  }

  public static boolean isStaticPruned(Fragment op, Expression rel) {
    // TODO(kaiyuanw): Initialize all pruning rules to control whether enable them or not.
    UnaryInfo unaryInfo = new UnaryInfo(op, rel);
    if (opIsOr(op, STAR)) {
      // Prune if
      List<PruningRule> pruningRulesForReflexiveClosure = Arrays.asList(
          // (1) * and ^ appear consecutively
          RRule1.given(unaryInfo),
          // (2) *iden = iden
          RRule2.given(unaryInfo)
      );
      return pruningRulesForReflexiveClosure.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }

    if (opIsOr(op, CARET)) {
      // Prune if
      List<PruningRule> pruningRulesForClosure = Arrays.asList(
          // (1) ^ and * appear consecutively
          VRule1.given(unaryInfo),
          // (2) ^(a->b) = a->b
          VRule2.given(unaryInfo),
          // (3) ^iden = iden
          VRule3.given(unaryInfo)
      );
      return pruningRulesForClosure.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }

    if (opIsOr(op, TILDE)) {
      // Prune if
      List<PruningRule> pruningRulesForTranspose = Arrays.asList(
          // (1) ~ appear before *, ^ or ~
          TRule1.given(unaryInfo),
          // (2) ~a where a is b -> c
          TRule2.given(unaryInfo),
          // (3) ~(a.b) where a is c->d or b is c->d
          TRule3.given(unaryInfo),
          // (4) ~(a+|&|-|.b) if a or b is ~c
          TRule4.given(unaryInfo),
          // (5) ~iden = iden
          TRule5.given(unaryInfo)
      );
      return pruningRulesForTranspose.stream().anyMatch(PruningRule::isEnabledAndPruned);
    }
    return false;
  }

  /**
   * Create basic expressions from type information.  Note that this method only create expressions
   * with cost 1 and op number 1.
   */
  public static Expression initExprFromType(TypeInfo typeInfo, String uid) {
    Node node = typeInfo.getNode();
    String name;
    Set<VarExpr> usedVariable = new HashSet<>();
    if (node instanceof SigDecl) {
      name = ((SigDecl) node).getName();
    } else if (node instanceof FieldExpr) {
      name = ((FieldExpr) node).getName();
    } else if (node instanceof VarExpr) {
      VarExpr var = (VarExpr) node;
      name = var.getName();
      usedVariable.add(var);
    } else if (node instanceof ConstExpr) {
      name = ((ConstExpr) node).getValue();
    } else {
      throw new RuntimeException("Unsupported type information " + typeInfo);
    }
    return new Expression(name, 1, typeInfo.arity, typeInfo.types, typeInfo.hasIden, typeInfo.cards,
        0, uid, usedVariable);
  }

  public static Expression createExprFromType(TypeInfo typeInfo, Map<Node, String> uid) {
    if (SPECIAL_TYPE_TO_EXPRESSIONS.containsKey(typeInfo)) {
      return SPECIAL_TYPE_TO_EXPRESSIONS.get(typeInfo);
    }
    return initExprFromType(typeInfo, uid.get(typeInfo.node));
  }

  public static Set<VarExpr> orderedSet(VarExpr... nodes) {
    return new HashSet<>(Arrays.asList(nodes));
  }

  @SafeVarargs
  public static Set<VarExpr> orderedSet(Collection<VarExpr>... nodesList) {
    Set<VarExpr> set = new HashSet<>();
    for (Collection<VarExpr> nodes : nodesList) {
      set.addAll(nodes);
    }
    return set;
  }

  public static Node findRealDomainExpr(ExprOrFormula exprOrFormula) {
    ExprOrFormula cur = exprOrFormula;
    while (cur instanceof UnaryExpr) {
      UnaryExpr unaryExpr = (UnaryExpr) cur;
      switch (unaryExpr.getOp()) {
        case SET:
        case LONE:
        case ONE:
        case SOME:
        case NOOP:
          cur = unaryExpr.getSub();
          break;
        default:
          return cur;
      }
    }
    return cur;
  }
}
