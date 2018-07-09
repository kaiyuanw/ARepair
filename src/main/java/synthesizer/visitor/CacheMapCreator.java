package synthesizer.visitor;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import parser.ast.nodes.ITEExprOrFormula;
import parser.ast.nodes.ITEFormula;
import parser.ast.nodes.LetExpr;
import parser.ast.nodes.ListExpr;
import parser.ast.nodes.ListFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.ModuleDecl;
import parser.ast.nodes.Node;
import parser.ast.nodes.OpenDecl;
import parser.ast.nodes.Paragraph;
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
 * This class generates a caching map that tells us which node we should use to generate the key and
 * which node we should map our key to.  We only need to consider nodes that could possibly
 * represent holes, so this class should be in consistent with {@link HoleTemplateCollector}.
 */
public class CacheMapCreator implements GenericVisitor<Node, Object> {

  /**
   * Current nodes that represent holes.
   */
  private Set<Node> nodeAsHoles;
  /**
   * A mapping from lower nodes to higher nodes that could potentially be used to compute keys for
   * caching.
   */
  private Map<Node, Node> l2h;
  /**
   * A mapping from higher nodes to lower nodes where the key of higher nodes depends on the value
   * of lower nodes.
   */
  private Map<Node, List<Node>> h2l;

  public CacheMapCreator(ModelUnit model, Set<Node> nodeAsHoles) {
    this.nodeAsHoles = nodeAsHoles;
    this.l2h = new LinkedHashMap<>();
    this.h2l = new LinkedHashMap<>();
    model.accept(this, null);
  }

  public Map<Node, Node> getL2H() {
    return l2h;
  }

  public Map<Node, List<Node>> getH2L() {
    return h2l;
  }

  private void addIfNotNull(List<Node> nodes, Node node) {
    if (node == null) {
      return;
    }
    nodes.add(node);
  }

  private Node getNode(Node n, List<Node> lowerNodes) {
    if (n instanceof Paragraph && !lowerNodes.isEmpty()) {
      // If the current node is a predicate/function/fact/assertion, then we create the mapping if
      // the lowerNodes is not empty.  We actually only need the body of the paragraph, but it is
      // easier to terminate the mapping chain once we detect the node is a paragraph, so we decide
      // to map lower nodes to the paragraph nodes.
      // Note that the size of the lowerNodes == 1.
      assert lowerNodes.size() == 1;
      lowerNodes.forEach(lowerNode -> l2h.put(lowerNode, n));
      h2l.put(n, lowerNodes);
      return n;
    }
    if (lowerNodes.size() >= 2) {
      // If the current node contains multiple nodes to cache, then we establish the mapping.
      // The goal is to reduce the number of nodes to compute the key for caching as we go from
      // bottom to top of the AST.
      lowerNodes.forEach(lowerNode -> l2h.put(lowerNode, n));
      h2l.put(n, lowerNodes);
      return n;
    } else if (lowerNodes.size() == 1) {
      // If the current node contains exactly one node to cache, then we just return that node
      // because caching the current node does not help increasing the hit rate of the cache.
      // However, if the current node represents a hole, then we still create the mapping.
      Node lowerNode = lowerNodes.get(0);
      if (nodeAsHoles.contains(n)) {
        l2h.put(lowerNode, n);
        h2l.put(n, lowerNodes);
        return n;
      }
      return lowerNode;
    } else {
      // If the current node does not contain any node to cache, then we can safely ignore it.
      // When computing the key of a parent node, the current node never needs to be evaluated,
      // instead we can use the string representation of the current node as part of the key.
      // However, if the current node represents a hole, then we return the node.
      if (nodeAsHoles.contains(n)) {
        return n;
      }
      return null;
    }
  }

  private Node getNode(Node n) {
    return getNode(n, ImmutableList.of());
  }

  @Override
  public Node visit(ModelUnit n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getModuleDecl().accept(this, arg));
    n.getOpenDeclList().forEach(open -> addIfNotNull(lowerNodes, open.accept(this, arg)));
    n.getSigDeclList().forEach(signature -> addIfNotNull(lowerNodes, signature.accept(this, arg)));
    n.getPredDeclList().forEach(predicate -> addIfNotNull(lowerNodes, predicate.accept(this, arg)));
    n.getFunDeclList().forEach(function -> addIfNotNull(lowerNodes, function.accept(this, arg)));
    n.getFactDeclList().forEach(fact -> addIfNotNull(lowerNodes, fact.accept(this, arg)));
    n.getAssertDeclList()
        .forEach(assertion -> addIfNotNull(lowerNodes, assertion.accept(this, arg)));
    n.getRunCmdList().forEach(run -> addIfNotNull(lowerNodes, run.accept(this, arg)));
    n.getCheckCmdList().forEach(check -> addIfNotNull(lowerNodes, check.accept(this, arg)));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(ModuleDecl n, Object arg) {
    return getNode(n);
  }

  @Override
  public Node visit(OpenDecl n, Object arg) {
    return getNode(n);
  }

  @Override
  public Node visit(SigDecl n, Object arg) {
    // If the hole is in SigDecl, caching does not help much.
    List<Node> lowerNodes = new ArrayList<>();
    n.getFieldList().forEach(fieldDecl -> addIfNotNull(lowerNodes, fieldDecl.accept(this, arg)));
    if (n.hasSigFact()) {
      addIfNotNull(lowerNodes, n.getSigFact().accept(this, arg));
    }
    return getNode(n, lowerNodes);
  }

  private Node visitRelDecl(RelDecl n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getVariables().forEach(variable -> addIfNotNull(lowerNodes, variable.accept(this, arg)));
    addIfNotNull(lowerNodes, n.getExpr().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(FieldDecl n, Object arg) {
    return visitRelDecl(n, arg);
  }

  @Override
  public Node visit(ParamDecl n, Object arg) {
    return visitRelDecl(n, arg);
  }

  @Override
  public Node visit(VarDecl n, Object arg) {
    return visitRelDecl(n, arg);
  }

  @Override
  public Node visit(ExprOrFormula n, Object arg) {
    return n.accept(this, arg);
  }

  @Override
  public Node visit(SigExpr n, Object arg) {
    return getNode(n);
  }

  @Override
  public Node visit(FieldExpr n, Object arg) {
    return getNode(n);
  }

  @Override
  public Node visit(VarExpr n, Object arg) {
    return getNode(n);
  }

  @Override
  public Node visit(UnaryExpr n, Object arg) {
    if (n.getOp() == UnaryOp.NOOP) {
      return n.getSub().accept(this, arg);
    }
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getSub().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(UnaryFormula n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getSub().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(BinaryExpr n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getLeft().accept(this, arg));
    addIfNotNull(lowerNodes, n.getRight().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(BinaryFormula n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getLeft().accept(this, arg));
    addIfNotNull(lowerNodes, n.getRight().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(ListExpr n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getArguments().forEach(argument -> addIfNotNull(lowerNodes, argument.accept(this, arg)));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(ListFormula n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getArguments().forEach(argument -> addIfNotNull(lowerNodes, argument.accept(this, arg)));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(CallExpr n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getArguments().forEach(argument -> addIfNotNull(lowerNodes, argument.accept(this, arg)));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(CallFormula n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getArguments().forEach(argument -> addIfNotNull(lowerNodes, argument.accept(this, arg)));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(QtExpr n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getVarDecls().forEach(varDecl -> addIfNotNull(lowerNodes, varDecl.accept(this, arg)));
    addIfNotNull(lowerNodes, n.getBody().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(QtFormula n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getVarDecls().forEach(varDecl -> addIfNotNull(lowerNodes, varDecl.accept(this, arg)));
    addIfNotNull(lowerNodes, n.getBody().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  private Node visitITE(ITEExprOrFormula n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getCondition().accept(this, arg));
    addIfNotNull(lowerNodes, n.getThenClause().accept(this, arg));
    addIfNotNull(lowerNodes, n.getElseClause().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(ITEExpr n, Object arg) {
    return visitITE(n, arg);
  }

  @Override
  public Node visit(ITEFormula n, Object arg) {
    return visitITE(n, arg);
  }

  @Override
  public Node visit(LetExpr n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getVar().accept(this, arg));
    addIfNotNull(lowerNodes, n.getBound().accept(this, arg));
    addIfNotNull(lowerNodes, n.getBody().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(ConstExpr n, Object arg) {
    return getNode(n);
  }

  @Override
  public Node visit(Body n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getBodyExpr().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(Predicate n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getParamList().forEach(paramDecl -> addIfNotNull(lowerNodes, paramDecl.accept(this, arg)));
    addIfNotNull(lowerNodes, n.getBody().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(Function n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    n.getParamList().forEach(paramDecl -> addIfNotNull(lowerNodes, paramDecl.accept(this, arg)));
    addIfNotNull(lowerNodes, n.getReturnType().accept(this, arg));
    addIfNotNull(lowerNodes, n.getBody().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(Fact n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getBody().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(Assertion n, Object arg) {
    List<Node> lowerNodes = new ArrayList<>();
    addIfNotNull(lowerNodes, n.getBody().accept(this, arg));
    return getNode(n, lowerNodes);
  }

  @Override
  public Node visit(Run n, Object arg) {
    return getNode(n);
  }

  @Override
  public Node visit(Check n, Object arg) {
    return getNode(n);
  }
}
