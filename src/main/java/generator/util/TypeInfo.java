package generator.util;

import static parser.etc.Names.COMMA;

import generator.etc.Card;
import generator.fragment.Type;
import java.util.List;
import java.util.stream.Collectors;
import parser.ast.nodes.Node;

public class TypeInfo {

  Node node;
  int arity;
  boolean hasIden;
  List<Type> types;
  List<Card> cards;

  private TypeInfo(Node node, int arity, boolean hasIden, List<Type> types, List<Card> cards) {
    this.node = node;
    this.arity = arity;
    this.hasIden = hasIden;
    this.types = types;
    this.cards = cards;
  }

  public static TypeInfo of(Node node, TypeInfo typeInfo) {
    return new TypeInfo(node, typeInfo.arity, typeInfo.hasIden, typeInfo.types, typeInfo.cards);
  }

  public static TypeInfo of(int arity, boolean hasIden, List<Type> types, List<Card> cards) {
    return of(null, arity, hasIden, types, cards);
  }

  public static TypeInfo of(Node node, int arity, boolean hasIden, List<Type> types,
      List<Card> cards) {
    return new TypeInfo(node, arity, hasIden, types, cards);
  }

  public Node getNode() {
    return node;
  }

  public int getArity() {
    return arity;
  }

  public boolean isHasIden() {
    return hasIden;
  }

  public List<Type> getTypes() {
    return types;
  }

  public List<Card> getCards() {
    return cards;
  }

  public String getTypeAsString() {
    return String.join(COMMA, types.stream().map(Type::getPruneType).collect(Collectors.toList()));
  }

  @Override
  public String toString() {
    return "<node: " + node + ", arity: " + arity + ", hasIden: " + hasIden
        + ", types: " + types + ", cards: " + cards + ">";
  }
}
