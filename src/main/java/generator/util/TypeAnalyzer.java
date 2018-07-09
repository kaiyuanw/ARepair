package generator.util;

import static generator.etc.Contants.BOOLEAN_TYPE_INFO;
import static generator.etc.Contants.CONST_IDEN;
import static generator.etc.Contants.CONST_INT;
import static generator.etc.Contants.CONST_NONE;
import static generator.etc.Contants.CONST_ONE;
import static generator.etc.Contants.CONST_STR;
import static generator.etc.Contants.CONST_UNIV;
import static generator.etc.Contants.CONST_ZERO;
import static generator.etc.Contants.IDEN_TYPE_INFO;
import static generator.etc.Contants.INT_TYPE;
import static generator.etc.Contants.INT_TYPE_INFO;
import static generator.etc.Contants.NONE_TYPE_INFO;
import static generator.etc.Contants.ONE_TYPE_INFO;
import static generator.etc.Contants.ORDERING_FILE;
import static generator.etc.Contants.ORDERING_FIRST;
import static generator.etc.Contants.ORDERING_LAST;
import static generator.etc.Contants.ORDERING_NEXT;
import static generator.etc.Contants.ORDERING_PREV;
import static generator.etc.Contants.STRING_TYPE_INFO;
import static generator.etc.Contants.THIS_STRING;
import static generator.etc.Contants.UNIV_STRING;
import static generator.etc.Contants.UNIV_TYPE_INFO;
import static generator.etc.Contants.ZERO_TYPE_INFO;
import static generator.util.TypeInfo.of;
import static generator.util.Util.createCards;
import static generator.util.Util.createType;
import static generator.util.Util.findCardinality;
import static generator.util.Util.getLCAType;
import static generator.util.Util.getLCATypeWithUniv;
import static generator.util.Util.getMinimumType;
import static generator.util.Util.maxCard;
import static generator.util.Util.minCard;
import static parser.etc.Names.DOLLAR;
import static parser.etc.Names.IGNORE_NAMES;
import static parser.etc.Names.SLASH;
import static parser.etc.Names.TEST_PREFIX;
import static parser.util.AlloyUtil.getFirstNonNOOPChild;

import com.google.common.collect.ImmutableList;
import generator.etc.Card;
import generator.fragment.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import parser.ast.nodes.Assertion;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.Body;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
import parser.ast.nodes.Check;
import parser.ast.nodes.ConstExpr;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.Fact;
import parser.ast.nodes.FieldDecl;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.Function;
import parser.ast.nodes.ITEExpr;
import parser.ast.nodes.ITEFormula;
import parser.ast.nodes.LetExpr;
import parser.ast.nodes.ListExpr;
import parser.ast.nodes.ListFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.ModuleDecl;
import parser.ast.nodes.Node;
import parser.ast.nodes.OpenDecl;
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.QtExpr;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.RelDecl;
import parser.ast.nodes.Run;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryExpr.UnaryOp;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import parser.ast.visitor.GenericVisitor;

/**
 * This analyzer class implements the following features:
 *
 * (1) Collect the inheritance hierarchical information of the signatures.  E.g. save "S extends P
 * {}" or "S in P {}" as a mapping from S->P.
 *
 * (2) Find each AST node that could be dug as a hole, find all basic relations that can be used to
 * synthesize new expressions at that location.  Note that AST nodes with the same variables in
 * scope share the same basic relations to generate new expressions.
 */
public class TypeAnalyzer implements GenericVisitor<TypeInfo, Object> {

  /**
   * The inheritance hierarchy.  Note that we do not consider univ in the hierarchy and the parent
   * sig of a top level sig is null.
   */
  private Map<String, String> inheritanceHierarchy;
  private Map<String, Stack<TypeInfo>> variableScope;
  /**
   * Store the node to the types of basic expressions that can be used to generate new expressions
   * in the scope of the node
   */
  private Map<Node, List<TypeInfo>> nodeToExprTypes;
  private Map<Node, TypeInfo> nodeToType;
  private Map<FieldDecl, SigDecl> fieldToSig;
  private Map<String, TypeInfo> functionToType;
  /**
   * Store the order in which the variable are declared.  The map maps a basic variable to an unique
   * id which is based on the order it is declared.
   */
  private Map<Node, String> uid;

  public TypeAnalyzer(ModelUnit modelUnit) {
    this.inheritanceHierarchy = new LinkedHashMap<>();
    this.variableScope = new LinkedHashMap<>();
    this.nodeToExprTypes = new LinkedHashMap<>();
    this.nodeToType = new LinkedHashMap<>();
    this.fieldToSig = new LinkedHashMap<>();
    this.functionToType = new LinkedHashMap<>();
    this.uid = new LinkedHashMap<>();
    initializeVariableScope(modelUnit);
    modelUnit.accept(this, null);
  }

  public Map<String, String> getInheritanceHierarchy() {
    return inheritanceHierarchy;
  }

  public Map<Node, List<TypeInfo>> getNodeToExprTypes() {
    return nodeToExprTypes;
  }

  public Map<Node, TypeInfo> getNodeToType() {
    return nodeToType;
  }

  public Map<Node, String> getUid() {
    return uid;
  }

  private void putVariableScope(String name, TypeInfo typeInfo) {
    if (!variableScope.containsKey(name)) {
      variableScope.put(name, new Stack<>());
    }
    variableScope.get(name).push(typeInfo);
    // We also assign each variable a unique id.
    if (!uid.containsKey(typeInfo.node)) {
      uid.put(typeInfo.node, name + DOLLAR + (uid.size() + 1));
    }
  }

  private TypeInfo getVariableScope(String name) {
    if (!variableScope.containsKey(name)) {
      return null;
    }
    return variableScope.get(name).peek();
  }

  private void initializeVariableScope(ModelUnit modelUnit) {
    // We first collect mappings for keywords.
    putVariableScope(CONST_NONE.getValue(), NONE_TYPE_INFO);
    putVariableScope(CONST_UNIV.getValue(), UNIV_TYPE_INFO);
    putVariableScope(CONST_IDEN.getValue(), IDEN_TYPE_INFO);
    putVariableScope(CONST_INT.getValue(), INT_TYPE_INFO);
    putVariableScope(CONST_ZERO.getValue(), ZERO_TYPE_INFO);
    putVariableScope(CONST_ONE.getValue(), ONE_TYPE_INFO);
    putVariableScope(CONST_STR.getValue(), STRING_TYPE_INFO);

    // We need to first collect variable scope from signature declarations.
    // Because field declaration may use signatures.
    for (SigDecl sigDecl : modelUnit.getSigDeclList()) {
      // Collect inheritance hierarchy because we will use it to build types.
      inheritanceHierarchy.put(sigDecl.getName(), sigDecl.getParentName());
      // Collect field to signature mapping.
      for (FieldDecl fieldDecl : sigDecl.getFieldList()) {
        fieldToSig.put(fieldDecl, sigDecl);
      }
      // Add signatures as basic types.
      String sigName = sigDecl.getName();
      TypeInfo sigTypeInfo = of(sigDecl, 1, false, ImmutableList.of(createType(sigName)),
          ImmutableList.of(findCardinality(sigDecl)));
      putVariableScope(sigName, sigTypeInfo);
      nodeToType.put(sigDecl, sigTypeInfo);
    }

    // Collect function to type mapping.
    for (Function function : modelUnit.getFunDeclList()) {
      TypeInfo typeInfo = function.getReturnType().accept(this, null);
      functionToType.put(function.getName(), typeInfo);
    }
  }

  private List<TypeInfo> getFragmentsInScope() {
    return variableScope.values().stream()
        .map(Stack::peek)
        .collect(Collectors.toList());
  }

  private void putNodeToExprTypes(Node node) {
    // Make sure the order of the map follows the visitor.
    // We invoke the analyzer to find the return types of functions first, which breaks the order.
    // So here we pop out the key if it exists.
    if (nodeToExprTypes.containsKey(node)) {
      nodeToExprTypes.remove(node);
    }
    if (visitingSig != null) { // Visiting a signature declaration.
      // We can only use field declared inside the current signature when synthesizing.
      Set<String> fieldNamesToIgnore = fieldToSig.entrySet().stream()
          .filter(entry -> entry.getValue() != visitingSig)
          .map(Entry::getKey)
          .map(FieldDecl::getNames)
          .flatMap(List::stream)
          .collect(Collectors.toSet());
      List<TypeInfo> availableTypes = variableScope.entrySet().stream()
          .filter(entry -> !fieldNamesToIgnore.contains(entry.getKey()))
          .map(Entry::getValue)
          .map(Stack::peek)
          .collect(Collectors.toList());
      nodeToExprTypes.put(node, availableTypes);
      return;
    }
    nodeToExprTypes.put(node, getFragmentsInScope());
  }

  private void removeVariableInScope(String name) {
    variableScope.get(name).pop();
    if (variableScope.get(name).empty()) {
      variableScope.remove(name);
    }
  }

  private void removeVariableInScope(RelDecl n) {
    n.getNames().forEach(this::removeVariableInScope);
  }

  @Override
  public TypeInfo visit(ModelUnit n, Object arg) {
    n.getModuleDecl().accept(this, arg);
    n.getOpenDeclList().forEach(openDecl -> openDecl.accept(this, arg));
    n.getSigDeclList().forEach(signature -> signature.accept(this, arg));
    // Ignore variables declared in tests.
    n.getPredDeclList().stream()
        .filter(predicate -> !predicate.getName().startsWith(TEST_PREFIX))
        .forEach(predicate -> predicate.accept(this, arg));
    n.getFunDeclList().forEach(function -> function.accept(this, arg));
    n.getFactDeclList().forEach(fact -> fact.accept(this, arg));
    n.getAssertDeclList().forEach(assertion -> assertion.accept(this, arg));
    n.getRunCmdList().forEach(run -> run.accept(this, arg));
    n.getCheckCmdList().forEach(check -> check.accept(this, arg));
    return null;
  }

  @Override
  public TypeInfo visit(ModuleDecl n, Object arg) {
    return null;
  }

  @Override
  public TypeInfo visit(OpenDecl n, Object arg) {
    if (n.getFileName().equals(ORDERING_FILE)) {
      // E.g. "open util/ordering [State] as ord"
      String sigName = n.getArguments().get(0);
      TypeInfo sigTypeInfo = getVariableScope(sigName);
      assert sigTypeInfo != null;
      Type type = sigTypeInfo.types.get(0);
      String alias = n.getAlias();
      String functionName = alias + SLASH + ORDERING_FIRST;
      functionToType.put(functionName,
          of(1, false, ImmutableList.of(type), ImmutableList.of(Card.ONE)));
      functionName = alias + SLASH + ORDERING_LAST;
      functionToType.put(functionName,
          of(1, false, ImmutableList.of(type), ImmutableList.of(Card.ONE)));
      functionName = alias + SLASH + ORDERING_PREV;
      functionToType.put(functionName,
          of(2, false, ImmutableList.of(type, type), ImmutableList.of(Card.SET, Card.SET)));
      functionName = alias + SLASH + ORDERING_NEXT;
      functionToType.put(functionName,
          of(2, false, ImmutableList.of(type, type), ImmutableList.of(Card.SET, Card.SET)));
    }
    return null;
  }

  private SigDecl visitingSig;

  @Override
  public TypeInfo visit(SigDecl n, Object arg) {
    visitingSig = n;
    n.getFieldList().forEach(field -> field.accept(this, arg));
    if (n.hasSigFact()) {
      n.getSigFact().accept(this, arg);
    }
    visitingSig = null;
    // We updated nodeToType during initialization.
    return null;
  }

  private TypeInfo updateTypeInfo(RelDecl relDecl, TypeInfo typeInfo) {
    relDecl.getVariables().forEach(variable -> {
      Node var = getFirstNonNOOPChild(variable);
      String name;
      // The variable is either a VarExpr or a FieldExpr according to ExprHasName in Alloy API.
      if (var instanceof VarExpr) {
        name = ((VarExpr) var).getName();
      } else {
        name = ((FieldExpr) var).getName();
      }
      TypeInfo variableType = of(var, typeInfo);
      putVariableScope(name, variableType);
      nodeToType.put(var, variableType);
    });
    nodeToType.put(relDecl, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(FieldDecl n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo exprType = n.getExpr().accept(this, arg);
    TypeInfo declaredSigType = getVariableScope(visitingSig.getName());
    assert declaredSigType != null;
    List<Type> newTypes = new ArrayList<>(declaredSigType.types);
    newTypes.addAll(exprType.types);
    List<Card> newCards = new ArrayList<>(declaredSigType.cards);
    newCards.addAll(exprType.cards);
    TypeInfo fieldType = of(exprType.arity + 1, exprType.hasIden, newTypes, newCards);
    return updateTypeInfo(n, fieldType);
  }

  @Override
  public TypeInfo visit(ParamDecl n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo exprType = n.getExpr().accept(this, arg);
    return updateTypeInfo(n, exprType);
  }

  @Override
  public TypeInfo visit(VarDecl n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo exprType = n.getExpr().accept(this, arg);
    return updateTypeInfo(n, exprType);
  }

  @Override
  public TypeInfo visit(ExprOrFormula n, Object arg) {
    return n.accept(this, arg);
  }

  @Override
  public TypeInfo visit(SigExpr n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo typeInfo = getVariableScope(n.getName());
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(FieldExpr n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo typeInfo = getVariableScope(n.getName());
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(VarExpr n, Object arg) {
    putNodeToExprTypes(n);
    // It's possible that VarExpr is "this", in which case we need to lookup the declared signature.
    String varName = n.getName();
    if (varName.equals(THIS_STRING)) {
      varName = visitingSig.getName();
    }
    TypeInfo typeInfo = getVariableScope(varName);
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  private void createCardsInPlace(List<Card> cards, Card card, int repeat) {
    for (int i = 0; i < repeat; i++) {
      cards.add(card);
    }
  }

  @Override
  public TypeInfo visit(UnaryExpr n, Object arg) {
    if (n.getOp() != UnaryOp.NOOP) {
      putNodeToExprTypes(n);
    }
    TypeInfo subTypeInfo = n.getSub().accept(this, arg);
    if (n.getOp() == UnaryOp.NOOP) {
      return subTypeInfo;
    }
    List<Type> newTypes = new ArrayList<>();
    List<Card> newCards = new ArrayList<>();
    boolean hasIden = subTypeInfo.hasIden;
    switch (n.getOp()) {
      case LONE:
        newTypes.addAll(subTypeInfo.types);
        createCardsInPlace(newCards, Card.LONE, subTypeInfo.cards.size());
        break;
      case ONE:
        newTypes.addAll(subTypeInfo.types);
        createCardsInPlace(newCards, Card.ONE, subTypeInfo.cards.size());
        break;
      case SOME:
        newTypes.addAll(subTypeInfo.types);
        createCardsInPlace(newCards, Card.SOME, subTypeInfo.cards.size());
        break;
      case SET:
        newTypes.addAll(subTypeInfo.types);
        createCardsInPlace(newCards, Card.SET, subTypeInfo.cards.size());
        break;
      case TRANSPOSE:
        // If op is ~, then types and cardinality should be reversed.
        for (int i = subTypeInfo.types.size() - 1; i >= 0; i--) {
          newTypes.add(subTypeInfo.types.get(i));
          newCards.add(subTypeInfo.cards.get(i));
        }
        break;
      case RCLOSURE:
        // If op is *, then the pruning type should be univ.
        for (int i = 0; i < subTypeInfo.types.size(); i++) {
          newTypes.add(createType(subTypeInfo.types.get(i).getGenType(), UNIV_STRING));
          newCards.add(subTypeInfo.cards.get(i));
        }
        hasIden = true;
        break;
      case CLOSURE:
        // If op is ^, then the type should be same as the operand.
        for (int i = 0; i < subTypeInfo.types.size(); i++) {
          newTypes.add(subTypeInfo.types.get(i));
          newCards.add(subTypeInfo.cards.get(i));
        }
        break;
      case CARDINALITY:
        // If op is #, then the type should be int.
        newTypes.add(INT_TYPE);
        newCards.add(Card.ONE);
        break;
      default:
        throw new RuntimeException("Unsupported unary operator " + n.getOp());
    }
    TypeInfo typeInfo = of(subTypeInfo.arity, hasIden, newTypes, newCards);
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(UnaryFormula n, Object arg) {
    putNodeToExprTypes(n);
    n.getSub().accept(this, arg);
    nodeToType.put(n, BOOLEAN_TYPE_INFO);
    return BOOLEAN_TYPE_INFO;
  }

  @Override
  public TypeInfo visit(BinaryExpr n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo leftTypeInfo = n.getLeft().accept(this, arg);
    TypeInfo rightTypeInfo = n.getRight().accept(this, arg);
    int newArity;
    List<Type> newTypes = new ArrayList<>();
    List<Card> newCards = new ArrayList<>();
    boolean hasIden = false;
    switch (n.getOp()) {
      case PLUS:
        // +, - and & does not alternate the arity.
        newArity = leftTypeInfo.arity;
        for (int i = 0; i < newArity; i++) {
          // Note that the generation type could be null for combined types, e.g. S + T,
          // and we make it consistent with the pruning type, namely univ.
          String lcaType = getLCAType(leftTypeInfo.types.get(i).getGenType(),
              rightTypeInfo.types.get(i).getGenType(), inheritanceHierarchy);
          String lcaTypeWithUniv = getLCATypeWithUniv(leftTypeInfo.types.get(i).getPruneType(),
              rightTypeInfo.types.get(i).getPruneType(), inheritanceHierarchy);
          newTypes.add(createType(lcaType, lcaTypeWithUniv));
        }
        newCards.addAll(createCards(Card.SET, newArity));
        if (leftTypeInfo.hasIden || rightTypeInfo.hasIden) {
          hasIden = true;
        }
        break;
      case INTERSECT:
        newArity = leftTypeInfo.arity;
        for (int i = 0; i < newArity; i++) {
          String lcaType = getLCAType(leftTypeInfo.types.get(i).getGenType(),
              rightTypeInfo.types.get(i).getGenType(), inheritanceHierarchy);
          String minimumType = getMinimumType(leftTypeInfo.types.get(i).getPruneType(),
              rightTypeInfo.types.get(i).getPruneType(), inheritanceHierarchy);
          newTypes.add(createType(lcaType, minimumType));
          newCards.add(minCard(leftTypeInfo.cards.get(i), rightTypeInfo.cards.get(i)));
        }
        if (leftTypeInfo.hasIden && rightTypeInfo.hasIden) {
          hasIden = true;
        }
        break;
      case MINUS:
        newArity = leftTypeInfo.arity;
        for (int i = 0; i < newArity; i++) {
          newTypes.add(createType(leftTypeInfo.types.get(i).getGenType(),
              leftTypeInfo.types.get(i).getPruneType()));
        }
        newCards.addAll(leftTypeInfo.cards);
        if (leftTypeInfo.hasIden) {
          hasIden = true;
        }
        break;
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
        // TODO(kaiyuanw): we should be more accurate about the cardinality for arrows.
        // E.g. "sig S { r: S lone->some T }" should have cardinality <set, lone, some>.
        // The problem is if we have "sig S { r: (S lone->some T) some-> (S one->one T) }".
        // In this case, what is the cardinality of "r"?

        // -> increases the arity to leftRelArity + rightRelArity.
        newArity = leftTypeInfo.arity + rightTypeInfo.arity;
        newTypes.addAll(leftTypeInfo.types);
        newTypes.addAll(rightTypeInfo.types);
        newCards.addAll(leftTypeInfo.cards);
        newCards.addAll(rightTypeInfo.cards);
        break;
      case JOIN:
        // . decreases the arity by 2.
        newArity = leftTypeInfo.arity + rightTypeInfo.arity - 2;
        // If leftRel has identity, it must be binary relation.
        for (int i = 0; i < leftTypeInfo.types.size() - 1; i++) {
          newTypes
              .add(leftTypeInfo.hasIden ? rightTypeInfo.types.get(0) : leftTypeInfo.types.get(i));
          newCards.add(leftTypeInfo.cards.get(i));
        }
        // If rightRel has identity, it must be binary relation.
        for (int i = 1; i < rightTypeInfo.types.size(); i++) {
          newTypes
              .add(rightTypeInfo.hasIden ? leftTypeInfo.types.get(leftTypeInfo.types.size() - 1)
                  : rightTypeInfo.types.get(i));
          newCards.add(rightTypeInfo.cards.get(i));
        }
        break;
      default:
        throw new RuntimeException("Unsupported binary operator " + n.getOp());
    }
    TypeInfo typeInfo = of(newArity, hasIden, newTypes, newCards);
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(BinaryFormula n, Object arg) {
    putNodeToExprTypes(n);
    n.getLeft().accept(this, arg);
    n.getRight().accept(this, arg);
    nodeToType.put(n, BOOLEAN_TYPE_INFO);
    return BOOLEAN_TYPE_INFO;
  }

  @Override
  public TypeInfo visit(ListExpr n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo typeInfo = null;
    for (ExprOrFormula argument : n.getArguments()) {
      typeInfo = argument.accept(this, arg);
    }
    assert typeInfo != null;
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(ListFormula n, Object arg) {
    putNodeToExprTypes(n);
    n.getArguments().forEach(argument -> argument.accept(this, arg));
    nodeToType.put(n, BOOLEAN_TYPE_INFO);
    return BOOLEAN_TYPE_INFO;
  }

  @Override
  public TypeInfo visit(CallExpr n, Object arg) {
    putNodeToExprTypes(n);
    n.getArguments().forEach(argument -> argument.accept(this, arg));
    TypeInfo typeInfo = functionToType.get(n.getName());
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(CallFormula n, Object arg) {
    putNodeToExprTypes(n);
    n.getArguments().forEach(argument -> argument.accept(this, arg));
    nodeToType.put(n, BOOLEAN_TYPE_INFO);
    return BOOLEAN_TYPE_INFO;
  }

  @Override
  public TypeInfo visit(QtExpr n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo varTypeInfo = null;
    for (VarDecl varDecl : n.getVarDecls()) {
      varTypeInfo = varDecl.accept(this, arg);
    }
    assert varTypeInfo != null;
    n.getBody().accept(this, arg);
    TypeInfo typeInfo;
    switch (n.getOp()) {
      case SUM:
        typeInfo = INT_TYPE_INFO;
        break;
      case COMPREHENSION:
        typeInfo = of(varTypeInfo.arity, false, varTypeInfo.types,
            IntStream.rangeClosed(1, varTypeInfo.arity).mapToObj(i -> Card.SET)
                .collect(Collectors.toList()));
        break;
      default:
        throw new RuntimeException("Unsupported quantifier operator " + n.getOp());
    }
    nodeToType.put(n, typeInfo);
    // Backtrack.
    n.getVarDecls().forEach(this::removeVariableInScope);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(QtFormula n, Object arg) {
    putNodeToExprTypes(n);
    n.getVarDecls().forEach(varDecl -> varDecl.accept(this, arg));
    n.getBody().accept(this, arg);
    nodeToType.put(n, BOOLEAN_TYPE_INFO);
    // Backtrack.
    n.getVarDecls().forEach(this::removeVariableInScope);
    return BOOLEAN_TYPE_INFO;
  }

  @Override
  public TypeInfo visit(ITEExpr n, Object arg) {
    putNodeToExprTypes(n);
    n.getCondition().accept(this, arg);
    TypeInfo thenTypeInfo = n.getThenClause().accept(this, arg);
    TypeInfo elseTypeInfo = n.getElseClause().accept(this, arg);
    int newArity = thenTypeInfo.arity;
    boolean hasIden = false;
    List<Type> newTypes = new ArrayList<>();
    List<Card> newCards = new ArrayList<>();
    for (int i = 0; i < newArity; i++) {
      String lcaType = getLCAType(thenTypeInfo.types.get(i).getGenType(),
          elseTypeInfo.types.get(i).getGenType(), inheritanceHierarchy);
      String lcaTypeWithUniv = getLCATypeWithUniv(thenTypeInfo.types.get(i).getPruneType(),
          elseTypeInfo.types.get(i).getPruneType(), inheritanceHierarchy);
      newTypes.add(createType(lcaType, lcaTypeWithUniv));
      newCards.add(maxCard(thenTypeInfo.cards.get(i), elseTypeInfo.cards.get(i)));
    }
    if (thenTypeInfo.hasIden || elseTypeInfo.hasIden) {
      hasIden = true;
    }
    TypeInfo typeInfo = of(newArity, hasIden, newTypes, newCards);
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(ITEFormula n, Object arg) {
    putNodeToExprTypes(n);
    n.getCondition().accept(this, arg);
    n.getThenClause().accept(this, arg);
    n.getElseClause().accept(this, arg);
    nodeToType.put(n, BOOLEAN_TYPE_INFO);
    return BOOLEAN_TYPE_INFO;
  }

  @Override
  public TypeInfo visit(LetExpr n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo boundType = n.getBound().accept(this, arg);
    // Let expression only contains a single variable name.
    // Note that let expressions are parsed nested so we don't need to worry about the case where
    // multiple variable names are in the same declaration.
    VarExpr varExpr = n.getRealVar();
    TypeInfo varType = of(varExpr, boundType);
    putVariableScope(varExpr.getName(), varType);
    nodeToType.put(varExpr, varType);
    n.getBody().accept(this, arg);
    nodeToType.put(n, BOOLEAN_TYPE_INFO);
    // Backtrack.
    removeVariableInScope(varExpr.getName());
    return BOOLEAN_TYPE_INFO;
  }

  @Override
  public TypeInfo visit(ConstExpr n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo typeInfo = getVariableScope(n.getValue());
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(Body n, Object arg) {
    putNodeToExprTypes(n);
    TypeInfo typeInfo = n.getBodyExpr().accept(this, arg);
    nodeToType.put(n, typeInfo);
    return typeInfo;
  }

  @Override
  public TypeInfo visit(Predicate n, Object arg) {
    if (n.getName().contains(DOLLAR) || IGNORE_NAMES.stream()
        .anyMatch(ignoredName -> n.getName().startsWith(ignoredName))) {
      return null;
    }
    // We should create a new variable and hide the variables with the same name.
    // In the meanwhile, we want to add new basic relations from parameter declarations.
    // Note that parameters declared later can use parameters declared before.
    // E.g. "pred p (s: S, t: s) { some t }" works fine.
    n.getParamList().forEach(paramDecl -> paramDecl.accept(this, arg));
    n.getBody().accept(this, arg);
    // Backtrack.
    n.getParamList().forEach(this::removeVariableInScope);
    return null;
  }

  @Override
  public TypeInfo visit(Function n, Object arg) {
    if (n.getName().contains(DOLLAR) || IGNORE_NAMES.stream()
        .anyMatch(ignoredName -> n.getName().startsWith(ignoredName))) {
      return null;
    }
    // Note that parameters declared later can use parameters declared before.
    // Return type can also use parameters.
    // E.g. "fun f (s: S, t: s): set s { s }" works fine.
    n.getParamList().forEach(parameter -> parameter.accept(this, arg));
    n.getReturnType().accept(this, arg);
    n.getBody().accept(this, arg);
    // Backtrack.
    n.getParamList().forEach(this::removeVariableInScope);
    return null;
  }

  @Override
  public TypeInfo visit(Fact n, Object arg) {
    if (IGNORE_NAMES.stream()
        .anyMatch(ignoredName -> n.getName().startsWith(ignoredName))) {
      return null;
    }
    n.getBody().accept(this, arg);
    return null;
  }

  @Override
  public TypeInfo visit(Assertion n, Object arg) {
    // We do not synthesize assertions so no need to collect node to fragments mapping
    // inside any assertion.
    return null;
  }

  @Override
  public TypeInfo visit(Run n, Object arg) {
    return null;
  }

  @Override
  public TypeInfo visit(Check n, Object arg) {
    return null;
  }
}
