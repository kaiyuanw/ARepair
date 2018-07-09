package generator.util.rules;

import static generator.etc.Contants.EMPTY;

import generator.fragment.Expression;
import generator.fragment.Fragment;
import generator.fragment.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class PruningRule {

  protected boolean enabled;

  public PruningRule() {
    this.enabled = true;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public abstract boolean isPruned();

  public boolean isEnabledAndPruned() {
    return isEnabled() && isPruned();
  }

  protected boolean sameRelations(Expression rel1, Expression rel2) {
    if (rel1 == null || rel2 == null) {
      return rel1 == rel2;
    }
    return rel1.getRepr().equals(rel2.getRepr());
  }

  protected Expression getChild(Expression parent, int childIndex) {
    return parent.getSubFragments().get(childIndex);
  }

  protected boolean opIsOr(Fragment op, Fragment... cands) {
    for (Fragment cand : cands) {
      if (op.equals(cand)) {
        return true;
      }
    }
    return false;
  }

  protected boolean pruningTypeMatches(List<Type> leftTypes, List<Type> rightTypes) {
    return leftTypes.size() == rightTypes.size() && IntStream.range(0, leftTypes.size())
        .allMatch(i -> leftTypes.get(i).getPruneType().equals(rightTypes.get(i).getPruneType()));
  }

  protected boolean opIsAll(Fragment op, Expression relation) {
    return relation.getOp().equals(EMPTY) || relation.getOp().equals(op) && relation
        .getSubFragments().stream().allMatch(subRelation -> opIsAll(op, subRelation));
  }

  protected boolean leafIsAll(Expression atomicRelation, Expression relation) {
    if (relation.getOp().equals(EMPTY)) {
      return relation.equals(atomicRelation);
    }
    return relation.getSubFragments().stream()
        .allMatch(subRelation -> leafIsAll(atomicRelation, subRelation));
  }

  /**
   * Find a barrier of operators in an AST and return true if all direct children are unique.
   */
  protected boolean uniqueNodesUnderOps(Expression root, Fragment op) {
    Set<Expression> visited = new HashSet<>();
    return uniqueNodesUnderOps(root, op, visited);
  }

  private boolean uniqueNodesUnderOps(Expression root, Fragment op, Set<Expression> visited) {
    if (!root.getOp().equals(op)) {
      return visited.add(root);
    }
    return root.getSubFragments().stream()
        .allMatch(subRelation -> uniqueNodesUnderOps(subRelation, op, visited));
  }

  /**
   * Find a barrier of operators in an AST and return true if all direct children are same.
   */
  protected Expression duplicateNodesUnderOps(Expression root, Fragment op) {
    Set<Expression> visited = new HashSet<>();
    if (duplicateNodesUnderOps(root, op, visited)) {
      return visited.iterator().next();
    }
    return null;
  }

  private boolean duplicateNodesUnderOps(Expression root, Fragment op, Set<Expression> visited) {
    if (!root.getOp().equals(op)) {
      if (visited.isEmpty()) {
        return visited.add(root);
      }
      return !visited.add(root);
    }
    return root.getSubFragments().stream()
        .allMatch(subRelation -> duplicateNodesUnderOps(subRelation, op, visited));
  }

  static class Wrapper {

    Expression prev;

    public void setPrev(Expression prev) {
      this.prev = prev;
    }

    public Expression getPrev() {
      return prev;
    }
  }

  /**
   * Find a barrier of operators in an AST and return true if any of the two consecutive children
   * meet certain constraints.
   */
  protected boolean consecutiveNodesUnderOps(Expression root, Fragment op,
      BiFunction<Expression, Expression, Boolean> matcher) {
    Wrapper wrapper = new Wrapper();
    return consecutiveNodesUnderOps(root, op, matcher, wrapper);
  }

  private boolean consecutiveNodesUnderOps(Expression root, Fragment op,
      BiFunction<Expression, Expression, Boolean> matcher, Wrapper wrapper) {
    if (!root.getOp().equals(op)) {
      if (wrapper.getPrev() != null && matcher.apply(wrapper.getPrev(), root)) {
        return true;
      }
      wrapper.setPrev(root);
      return false;
    }
    return root.getSubFragments().stream()
        .anyMatch(subRelation -> consecutiveNodesUnderOps(subRelation, op, matcher, wrapper));
  }

  /**
   * Go to left subtree from root as long as op is as specified.  If a constraint hold for any
   * iteration, return true.  Otherwise, return false. For example, process (((a-b)-c)-d) with the
   * following order: (1) (((a-b)-c)-d) (2) ((a-b)-c) (3) (a-b) (4) a
   */
  protected boolean rightChildUnderLeftImbalancedOps(Expression root, Fragment op,
      Function<Expression, Boolean> matcher) {
    return root.getOp().equals(op) && (matcher.apply(root) || rightChildUnderLeftImbalancedOps(
        getChild(root, 0), op, matcher));
  }
}
