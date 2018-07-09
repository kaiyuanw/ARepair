package synthesizer.visitor;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.QtExpr;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.Run;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import parser.ast.visitor.GenericVisitor;
import synthesizer.hole.BinaryFormulaCmpHole;
import synthesizer.hole.BinaryFormulaLogicHole;
import synthesizer.hole.ExprHole;
import synthesizer.hole.Hole;
import synthesizer.hole.ListFormulaHole;
import synthesizer.hole.QuantifierHole;
import synthesizer.hole.SigHole;
import synthesizer.hole.UnaryExprCardHole;
import synthesizer.hole.UnaryFormulaBoolHole;
import synthesizer.hole.UnaryFormulaCardHole;

/**
 * This class returns the AST nodes that represent holes.  Note that if an AST node represents a
 * hole, then its subnodes will not be printed out, but instead the candidate value of the hole will
 * be printed out.
 */
public class HoleTemplateCollector implements GenericVisitor<Map<Node, Hole>, Object> {

  private boolean inFieldDecl;

  public HoleTemplateCollector() {
    this.inFieldDecl = false;
  }

  @Override
  public Map<Node, Hole> visit(ModelUnit n, Object arg) {
    // We return an empty map if the node should never be returned by the FL technique.
    return ImmutableMap.of();
  }

  @Override
  public Map<Node, Hole> visit(ModuleDecl n, Object arg) {
    return ImmutableMap.of();
  }

  @Override
  public Map<Node, Hole> visit(OpenDecl n, Object arg) {
    return ImmutableMap.of();
  }

  @Override
  public Map<Node, Hole> visit(SigDecl n, Object arg) {
    // We do not collect subnodes because if the FL technique returns a signature, it really means
    // the signature multiplicity instead of field declarations, etc.
    return ImmutableMap.of(n, new SigHole());
  }

  @Override
  public Map<Node, Hole> visit(FieldDecl n, Object arg) {
    inFieldDecl = true;
    Map<Node, Hole> map = new LinkedHashMap<>();
    // We do not dig holes in field names.
    n.getExpr().accept(this, arg).forEach(map::putIfAbsent);
    inFieldDecl = false;
    return map;
  }

  @Override
  public Map<Node, Hole> visit(ParamDecl n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    // We do not dig holes in parameter names.
    n.getExpr().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(VarDecl n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    // We do not dig holes in variable names.
    n.getExpr().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(ExprOrFormula n, Object arg) {
    return n.accept(this, arg);
  }

  @Override
  public Map<Node, Hole> visit(SigExpr n, Object arg) {
    // Leaf node.
    return ImmutableMap.of(n, new ExprHole());
  }

  @Override
  public Map<Node, Hole> visit(FieldExpr n, Object arg) {
    // Leaf node.
    return ImmutableMap.of(n, new ExprHole());
  }

  @Override
  public Map<Node, Hole> visit(VarExpr n, Object arg) {
    // Leaf node.
    return ImmutableMap.of(n, new ExprHole());
  }

  @Override
  public Map<Node, Hole> visit(UnaryExpr n, Object arg) {
    // Return holes recursively.
    Map<Node, Hole> map = new LinkedHashMap<>();
    switch (n.getOp()) {
      case SET:
      case LONE:
      case ONE:
      case SOME:
        // We put operator holes the last.
        n.getSub().accept(this, arg).forEach(map::putIfAbsent);
        // TODO(kaiyuanw): Let's first do not create holes for unary expressions that specify
        // cardinalities.  The modulo input checker can be optimized using the cardinality
        // information.  Note that we do support unary expression holes for field declaration.
        if (inFieldDecl) {
          map.putIfAbsent(n, new UnaryExprCardHole());
        }
        return map;
      case EXACTLYOF:
      case CAST2INT:
      case CAST2SIGINT:
      case NOOP:
        n.getSub().accept(this, arg).forEach(map::putIfAbsent);
        return map;
      default:
        // If the operator is ~, ^, * or #, then we treat the entire expression as a hole.
        return ImmutableMap.of(n, new ExprHole());
    }
  }

  @Override
  public Map<Node, Hole> visit(UnaryFormula n, Object arg) {
    // Return nodes recursively.
    Map<Node, Hole> map = new LinkedHashMap<>();
    // We put operator holes last.
    n.getSub().accept(this, arg).forEach(map::putIfAbsent);
    switch (n.getOp()) {
      case LONE:
      case ONE:
      case SOME:
      case NO:
        map.putIfAbsent(n, new UnaryFormulaCardHole());
        break;
      default:
        // The operator is "!".
        map.putIfAbsent(n, new UnaryFormulaBoolHole());
        break;
    }
    return map;
  }

  @Override
  public Map<Node, Hole> visit(BinaryExpr n, Object arg) {
    // Return nodes recursively.
    if (n.getLeft() instanceof VarExpr) {
      String value = ((VarExpr) n.getLeft()).getName();
      // We do not dig hole if the left expression is "this".
      if (value.equals("this")) {
        Map<Node, Hole> map = new LinkedHashMap<>();
        n.getRight().accept(this, arg).forEach(map::putIfAbsent);
        return map;
      }
    }
    switch (n.getOp()) {
      case JOIN:
      case INTERSECT:
      case PLUS:
      case MINUS:
        // Since we can generate ".", "&", "+", "-", we don't need to preserve the operator.
        return ImmutableMap.of(n, new ExprHole());
      default:
        Map<Node, Hole> map = new LinkedHashMap<>();
        n.getLeft().accept(this, arg).forEach(map::putIfAbsent);
        n.getRight().accept(this, arg).forEach(map::putIfAbsent);
        return map;
    }
  }

  @Override
  public Map<Node, Hole> visit(BinaryFormula n, Object arg) {
    // Return nodes recursively.
    Map<Node, Hole> map = new LinkedHashMap<>();
    // We put operator holes last.
    n.getLeft().accept(this, arg).forEach(map::putIfAbsent);
    n.getRight().accept(this, arg).forEach(map::putIfAbsent);
    switch (n.getOp()) {
      case EQUALS:
      case NOT_EQUALS:
      case IN:
      case NOT_IN:
        map.putIfAbsent(n, new BinaryFormulaCmpHole());
        break;
      case IMPLIES:
      case IFF:
        map.putIfAbsent(n, new BinaryFormulaLogicHole());
        break;
      default:
    }
    return map;
  }

  @Override
  public Map<Node, Hole> visit(ListExpr n, Object arg) {
    // We do not create operator hole for ListExpr.
    // Return nodes recursively.
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getArguments()
        .forEach(exprOrFormula -> exprOrFormula.accept(this, arg).forEach(map::putIfAbsent));
    return map;
  }

  @Override
  public Map<Node, Hole> visit(ListFormula n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    map.putIfAbsent(n, new ListFormulaHole());
    n.getArguments()
        .forEach(exprOrFormula -> exprOrFormula.accept(this, arg).forEach(map::putIfAbsent));
    return map;
  }

  @Override
  public Map<Node, Hole> visit(CallExpr n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getArguments()
        .forEach(exprOrFormula -> exprOrFormula.accept(this, arg).forEach(map::putIfAbsent));
    return map;
  }

  @Override
  public Map<Node, Hole> visit(CallFormula n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getArguments()
        .forEach(exprOrFormula -> exprOrFormula.accept(this, arg).forEach(map::putIfAbsent));
    return map;
  }

  @Override
  public Map<Node, Hole> visit(QtExpr n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getVarDecls().forEach(varDecl -> varDecl.accept(this, arg).forEach(map::putIfAbsent));
    n.getBody().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(QtFormula n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getVarDecls().forEach(varDecl -> varDecl.accept(this, arg).forEach(map::putIfAbsent));
    n.getBody().accept(this, arg).forEach(map::putIfAbsent);
    // We put operator holes last.
    map.putIfAbsent(n, new QuantifierHole());
    return map;
  }

  @Override
  public Map<Node, Hole> visit(ITEExpr n, Object arg) {
    return visitITE(n, arg);
  }

  @Override
  public Map<Node, Hole> visit(ITEFormula n, Object arg) {
    return visitITE(n, arg);
  }

  private Map<Node, Hole> visitITE(ITEExprOrFormula n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getCondition().accept(this, arg).forEach(map::putIfAbsent);
    n.getThenClause().accept(this, arg).forEach(map::putIfAbsent);
    n.getElseClause().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(LetExpr n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    // We do not change the variable name.
    n.getBound().accept(this, arg).forEach(map::putIfAbsent);
    n.getBody().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(ConstExpr n, Object arg) {
    // We do not create holes for constants.
    // TODO(kaiyuanw): If we decide to add holes for constant expressions, change Inliner accordingly.
    return ImmutableMap.of();
  }

  @Override
  public Map<Node, Hole> visit(Body n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getBodyExpr().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(Predicate n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getParamList().forEach(paramDecl -> paramDecl.accept(this, arg).forEach(map::putIfAbsent));
    n.getBody().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(Function n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getParamList().forEach(paramDecl -> paramDecl.accept(this, arg).forEach(map::putIfAbsent));
    n.getReturnType().accept(this, arg).forEach(map::putIfAbsent);
    n.getBody().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(Fact n, Object arg) {
    Map<Node, Hole> map = new LinkedHashMap<>();
    n.getBody().accept(this, arg).forEach(map::putIfAbsent);
    return map;
  }

  @Override
  public Map<Node, Hole> visit(Assertion n, Object arg) {
    return ImmutableMap.of();
  }

  @Override
  public Map<Node, Hole> visit(Run n, Object arg) {
    return ImmutableMap.of();
  }

  @Override
  public Map<Node, Hole> visit(Check n, Object arg) {
    return ImmutableMap.of();
  }
}
