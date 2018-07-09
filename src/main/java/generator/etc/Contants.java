package generator.etc;

import static generator.util.TypeInfo.of;
import static generator.util.Util.createType;
import static generator.util.Util.initExprFromType;
import static parser.etc.Names.DOLLAR;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import generator.fragment.Expression;
import generator.fragment.Fragment;
import generator.fragment.Type;
import generator.fragment.op.All;
import generator.fragment.op.And;
import generator.fragment.op.Cardinality;
import generator.fragment.op.CrossProduct;
import generator.fragment.op.Difference;
import generator.fragment.op.Empty;
import generator.fragment.op.Equal;
import generator.fragment.op.Iff;
import generator.fragment.op.Implies;
import generator.fragment.op.In;
import generator.fragment.op.Intersect;
import generator.fragment.op.Join;
import generator.fragment.op.Lone;
import generator.fragment.op.No;
import generator.fragment.op.Not;
import generator.fragment.op.NotEqual;
import generator.fragment.op.NotIn;
import generator.fragment.op.One;
import generator.fragment.op.Or;
import generator.fragment.op.ReflexiveTransitiveClosure;
import generator.fragment.op.Set;
import generator.fragment.op.Some;
import generator.fragment.op.TransitiveClosure;
import generator.fragment.op.Transpose;
import generator.fragment.op.Union;
import generator.util.TypeInfo;
import java.util.Map;
import parser.ast.nodes.ConstExpr;

public class Contants {

  // Signature multiplicity, quantifiers, unary operators.
  public static final Fragment EMPTY = new Empty();
  public static final Fragment NO = new No();
  public static final Fragment LONE = new Lone();
  public static final Fragment ONE = new One();
  public static final Fragment SOME = new Some();
  public static final Fragment ALL = new All();
  public static final Fragment SET = new Set();

  // Logical operators.
  public static final Fragment OR = new Or();
  public static final Fragment AND = new And();
  public static final Fragment IFF = new Iff();
  public static final Fragment IMPLIES = new Implies();

  // Compare operators.
  public static final Fragment EQ = new Equal();
  public static final Fragment NEQ = new NotEqual();
  public static final Fragment IN = new In();
  public static final Fragment NIN = new NotIn();

  // Unary operators for formulas.
  public static final Fragment NOT = new Not();
  // EMPTY is declared above.

  // Unary operators for expressions.
  public static final Fragment TILDE = new Transpose();
  public static final Fragment STAR = new ReflexiveTransitiveClosure();
  public static final Fragment CARET = new TransitiveClosure();
  public static final Fragment HASH = new Cardinality();

  // Binary operators.
  public static final Fragment AMP = new Intersect();
  public static final Fragment PLUS = new Union();
  public static final Fragment MINUS = new Difference();
  public static final Fragment DOT = new Join();
  public static final Fragment ARROW = new CrossProduct();

  // Alloy keywords.
  public static final String NONE_STRING = "none";
  public static final String UNIV_STRING = "univ";
  public static final String IDEN_STRING = "iden";
  public static final String INT_STRING = "Int";
  public static final String ZERO_STRING = "0";
  public static final String ONE_STRING = "1";
  public static final String STR_STRING = "String";
  public static final String BOOL_STRING = "Boolean";
  public static final String THIS_STRING = "this";

  // Ordering utility.
  public static final String ORDERING_FILE = "util/ordering";
  public static final String ORDERING_FIRST = "first";
  public static final String ORDERING_LAST = "last";
  public static final String ORDERING_PREV = "prev";
  public static final String ORDERING_NEXT = "next";

  // Special types.
  public static final Type NONE_TYPE = createType(NONE_STRING);
  public static final Type UNIV_TYPE = createType(UNIV_STRING);
  public static final Type INT_TYPE = createType(INT_STRING);
  public static final Type STR_TYPE = createType(STR_STRING);
  public static final Type BOOL_TYPE = createType(BOOL_STRING);

  // Special nodes.
  public static final ConstExpr CONST_NONE = new ConstExpr(NONE_STRING);
  public static final ConstExpr CONST_UNIV = new ConstExpr(UNIV_STRING);
  public static final ConstExpr CONST_IDEN = new ConstExpr(IDEN_STRING);
  public static final ConstExpr CONST_INT = new ConstExpr(INT_STRING);
  public static final ConstExpr CONST_ZERO = new ConstExpr(ZERO_STRING);
  public static final ConstExpr CONST_ONE = new ConstExpr(ONE_STRING);
  public static final ConstExpr CONST_STR = new ConstExpr(STR_STRING);
  public static final ConstExpr CONST_BOOL = new ConstExpr(BOOL_STRING);

  // Special type information.
  public static final TypeInfo NONE_TYPE_INFO = of(CONST_NONE, 1, false,
      ImmutableList.of(NONE_TYPE), ImmutableList.of(Card.EMPTY));
  public static final TypeInfo UNIV_TYPE_INFO = of(CONST_UNIV, 1, false,
      ImmutableList.of(UNIV_TYPE), ImmutableList.of(Card.SET));
  public static final TypeInfo IDEN_TYPE_INFO = of(CONST_IDEN, 2, true,
      ImmutableList.of(UNIV_TYPE, UNIV_TYPE), ImmutableList.of(Card.SET, Card.SET));
  // We set the cardinality of Int to one because we do not want enumerate all subset of integers.
  public static final TypeInfo INT_TYPE_INFO = of(CONST_INT, 1, false,
      ImmutableList.of(INT_TYPE), ImmutableList.of(Card.ONE));
  public static final TypeInfo ZERO_TYPE_INFO = of(CONST_ZERO, 1, false,
      ImmutableList.of(INT_TYPE), ImmutableList.of(Card.ONE));
  public static final TypeInfo ONE_TYPE_INFO = of(CONST_ONE, 1, false,
      ImmutableList.of(INT_TYPE), ImmutableList.of(Card.ONE));
  public static final TypeInfo STRING_TYPE_INFO = of(CONST_STR, 1, false,
      ImmutableList.of(STR_TYPE), ImmutableList.of(Card.SET));
  public static final TypeInfo BOOLEAN_TYPE_INFO = of(CONST_BOOL, 0, false,
      ImmutableList.of(BOOL_TYPE), ImmutableList.of(Card.ONE));

  // Special expression fragments.
  public static final Expression NONE_EXPR = initExprFromType(NONE_TYPE_INFO, NONE_STRING + DOLLAR);
  public static final Expression UNIV_EXPR = initExprFromType(UNIV_TYPE_INFO, UNIV_STRING + DOLLAR);
  public static final Expression IDEN_EXPR = initExprFromType(IDEN_TYPE_INFO, IDEN_STRING + DOLLAR);
  public static final Expression INT_EXPR = initExprFromType(INT_TYPE_INFO, INT_STRING + DOLLAR);
  public static final Expression ZERO_EXPR = initExprFromType(ZERO_TYPE_INFO, ZERO_STRING + DOLLAR);
  public static final Expression ONE_EXPR = initExprFromType(ONE_TYPE_INFO, ONE_STRING + DOLLAR);
  public static final Expression STRING_EXPR = initExprFromType(STRING_TYPE_INFO,
      STR_STRING + DOLLAR);

  // Special mapping.
  public static final Map<TypeInfo, Expression> SPECIAL_TYPE_TO_EXPRESSIONS
      = ImmutableMap.<TypeInfo, Expression>builder()
      .put(NONE_TYPE_INFO, NONE_EXPR)
      .put(UNIV_TYPE_INFO, UNIV_EXPR)
      .put(IDEN_TYPE_INFO, IDEN_EXPR)
      .put(INT_TYPE_INFO, INT_EXPR)
      .put(ZERO_TYPE_INFO, ZERO_EXPR)
      .put(ONE_TYPE_INFO, ONE_EXPR)
      .put(STRING_TYPE_INFO, STRING_EXPR)
      .build();

  // The node comparator to make sure that the variables are ordered so that later when we generate
  // combinations of let v1 = D1, v2 = D2, ... | e(v1, v2, ...), we can make sure that the
  // generated key "uid(v1) + uid(v2) + ... + value(e)" of the expression is correct.
  // We need to include the identity of variables to make sure the program works fine in case of
  // variable masking.
//  public static final Comparator<Node> EXPRESSION_COMPARATOR = Comparator.comparing(Util::uid);
}
