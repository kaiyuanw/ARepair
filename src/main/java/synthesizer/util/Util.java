package synthesizer.util;

import static generator.etc.Contants.AND;
import static generator.etc.Contants.DOT;
import static generator.etc.Contants.NONE_STRING;
import static generator.etc.Contants.UNIV_STRING;
import static generator.util.Util.getLCAType;
import static parser.etc.Names.DOLLAR;
import static parser.etc.Names.UNDERSCORE;
import static parser.util.AlloyUtil.findFirstAncestorWithType;
import static parser.util.AlloyUtil.getFirstNonNOOPChild;
import static synthesizer.visitor.FactCollector.SIG_FACT;
import static synthesizer.visitor.FactCollector.X;

import generator.fragment.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.Body;
import parser.ast.nodes.Fact;
import parser.ast.nodes.FieldDecl;
import parser.ast.nodes.Function;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.Paragraph;
import parser.ast.nodes.PredOrFun;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigDecl.MULT;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.VarExpr;
import synthesizer.hole.Hole;
import synthesizer.visitor.CallCollector;
import synthesizer.visitor.HoleTemplateCollector;
import synthesizer.visitor.Inliner;
import synthesizer.visitor.NodeIdCollector;

public class Util {

  /**
   * Returns true if both types have the same arity and each column of types has the same
   * inheritance hierarchy.  We allow the type to compare to be the none type.
   */
  public static boolean isSimilarType(List<Type> typeBase, List<Type> typeToCompare,
      Map<String, String> inheritanceHierarchy) {
    if (typeBase.size() != typeToCompare.size()) {
      return false;
    }
    for (int i = 0; i < typeBase.size(); i++) {
      Type base = typeBase.get(i);
      Type toCompare = typeToCompare.get(i);
      // If the base type is none, then the compare type must be none.
      if (base.getGenType().equals(NONE_STRING) && !toCompare.getGenType().equals(NONE_STRING)) {
        return false;
      }
      // If base type contains univ, then we can use any expressions as long as the arity matches.
      // If the compare type is none, then we still want to use it.
      // Otherwise, if the base type and the compare type are not in the same inheritance chain,
      // we do not want to use such type.
      if (!base.getGenType().equals(UNIV_STRING) && !toCompare.getGenType().equals(NONE_STRING)
          && getLCAType(base.getGenType(), toCompare.getGenType(), inheritanceHierarchy) == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Maps the callee name to a list of caller names.
   */
  public static Map<String, List<String>> collectInverseCallGraph(ModelUnit modelUnit) {
    Queue<Node> queue = new LinkedList<>();
    modelUnit.getSigDeclList().forEach(sigDecl -> {
      if (sigDecl.hasSigFact()) {
        queue.offer(sigDecl.getSigFact());
      }
    });
    modelUnit.getFactDeclList().forEach(queue::offer);
    Map<String, PredOrFun> name2paragraph = new LinkedHashMap<>();
    modelUnit.getPredDeclList()
        .forEach(predicate -> {
          name2paragraph.put(predicate.getName(), predicate);
          queue.offer(predicate);
        });
    modelUnit.getFunDeclList()
        .forEach(function -> {
          name2paragraph.put(function.getName(), function);
          queue.offer(function);
        });
    // Maps the paragraph name A to a list of paragraph names L that invoke A.
    Map<String, List<String>> inverseCallGraph = new LinkedHashMap<>();
    while (!queue.isEmpty()) {
      Node caller = queue.poll();
      String callerName;
      if (caller instanceof Paragraph) {
        callerName = ((Paragraph) caller).getName();
      } else { // From sig fact.
        SigDecl sigDecl = (SigDecl) findFirstAncestorWithType(caller, SigDecl.class);
        callerName = sigDecl.getName() + SIG_FACT;
      }
      CallCollector callCollector = new CallCollector();
      caller.accept(callCollector, null);
      for (String calleeName : callCollector.getCallNames()) {
        if (!inverseCallGraph.containsKey(calleeName)) {
          inverseCallGraph.put(calleeName, new ArrayList<>());
        }
        inverseCallGraph.get(calleeName)
            .add(callerName.replaceAll("\\" + DOLLAR, UNDERSCORE));
        // If callee's name is in the current model, then we keep visiting it.  Note that it is
        // possible that the callee is outside of the model and we do not visit it.
        if (name2paragraph.containsKey(calleeName)) {
          queue.offer(name2paragraph.get(calleeName));
        }
      }
    }
    return inverseCallGraph;
  }

  public static Map<Node, Hole> createHoles(Node node) {
    HoleTemplateCollector collector = new HoleTemplateCollector();
    return node.accept(collector, null);
  }

  /**
   * Convert formulas (facts and test predicates) that a test may depend on to a string.  Note that
   * we inline predicate/function calls so it can be interpreted by the evaluator.  Returns null if
   * the formula does not add any constraint.
   */
  public static String toFormulaString(Node node, Inliner inliner) {
    if (node instanceof SigDecl) {
      SigDecl sigDecl = (SigDecl) node;
      if (sigDecl.getMult() == MULT.SET) {
        return null;
      }
      return sigDecl.getMult() + sigDecl.getName();
    } else if (node instanceof FieldDecl) {
      FieldDecl fieldDecl = (FieldDecl) node;
      SigDecl sigDecl = (SigDecl) findFirstAncestorWithType(fieldDecl, SigDecl.class);
      Node expr = getFirstNonNOOPChild(fieldDecl.getExpr());
      if (expr instanceof UnaryExpr) {
        UnaryExpr unaryExpr = (UnaryExpr) expr;
        switch (unaryExpr.getOp()) {
          case LONE:
          case ONE:
          case SOME:
            return "all " + X + ": " + sigDecl.getName() + " | " + String.join(AND.getValue(),
                fieldDecl.getNames().stream()
                    .map(name -> "(" + unaryExpr.getOp() + X + DOT.getValue() + name + ")")
                    .collect(Collectors.toList()));
          default:
        }
      }
      // We do not generate constraint for other field declarations.  E.g. field declared with "->".
      return null;
    } else if (node instanceof Body) { // Facts or test predicates.
      return node.accept(inliner, null);
    } else {
      SigDecl sigDecl = (SigDecl) findFirstAncestorWithType(node, SigDecl.class);
      if (sigDecl.hasSigFact() && sigDecl.getSigFact() == node) {
        // Here sig fact must be non-empty.  Alloy does not create sig fact if it is empty, so we
        // don't need to worry about it being empty for now.
        return "all " + X + ": " + sigDecl.getName() + " { " + node
            .accept(new SigFactPrettyStringVisitor(inliner), null) + " }";
      }
    }
    throw new RuntimeException("Unsupported node type " + node.getClass());
  }

  /**
   * Directly apply this visitor to signature facts.
   */
  private static class SigFactPrettyStringVisitor extends Inliner {

    public SigFactPrettyStringVisitor(Inliner someInliner) {
      super(someInliner);
    }

    @Override
    public String visit(BinaryExpr n, Object arg) {
      if (n.getLeft() instanceof VarExpr) {
        String value = ((VarExpr) n.getLeft()).getName();
        if (value.equals("this")) {
          // E.g.
          // sig Book {
          //	entry: set Name,
          //	listed: entry ->set Listing
          // }
          // entry -> set Listing is actually this.entry -> set Listing.
          putInMap(n.getLeft(), value);
          return putInMap(n, "(" + X + n.getOp() + n.getRight().accept(this, arg) + ")");
        }
      }
      return putInMap(n,
          "(" + n.getLeft().accept(this, arg) + n.getOp() + n.getRight().accept(this, arg) + ")");
    }
  }

  /**
   * Compute the order to explore fragment combinations, given a list of partitions over domains.
   * E.g. Given [3, 5, 10], we want to generate [1, 1, 1], [1, 1, 2], [1, 2, 1], [2, 1, 1] up to [3,
   * 5, 10].
   */
  public static List<List<Integer>> computeExplorationOrders(List<Integer> partitions,
      Integer maxPartitionNum) {
    List<List<Integer>> res = new ArrayList<>();
    backtrack(res, new Stack<>(), partitions, 0);
    res.sort((p1, p2) -> {
      int minA = maxPartitionNum;
      int maxA = -1;
      int sumA = 0;
      for (int e : p1) {
        minA = Math.min(minA, e);
        maxA = Math.max(maxA, e);
        sumA += e;
      }
      int minB = maxPartitionNum;
      int maxB = -1;
      int sumB = 0;
      for (int e : p2) {
        minB = Math.min(minB, e);
        maxB = Math.max(maxB, e);
        sumB += e;
      }
      if (sumA != sumB) {
        return sumA - sumB;
      }
      int diffA = maxA - minA;
      int diffB = maxB - minB;
      if (diffA != diffB) {
        return diffA - diffB;
      }
      for (int i = 0; i < p1.size(); i++) {
        if (p1.get(i).compareTo(p2.get(i)) != 0) {
          return p1.get(i) - p2.get(i);
        }
      }
      return 0;
    });
    return res;
  }

  private static void backtrack(List<List<Integer>> res, Stack<Integer> path,
      List<Integer> partitions, int start) {
    if (start == partitions.size()) {
      res.add(new ArrayList<>(path));
    } else {
      for (int i = 0; i < partitions.get(start); i++) {
        path.push(i);
        backtrack(res, path, partitions, start + 1);
        path.pop();
      }
    }
  }

  public static int getNodeId(ModelUnit model, Node node) {
    NodeIdCollector collector = new NodeIdCollector(model, node);
    return collector.getId();
  }

  public static Paragraph getParagraphFromName(ModelUnit model, String name) {
    for (Predicate predicate : model.getPredDeclList()) {
      if (predicate.getName().replaceAll("\\" + DOLLAR, UNDERSCORE).equals(name)) {
        return predicate;
      }
    }
    for (Function function : model.getFunDeclList()) {
      if (function.getName().replaceAll("\\" + DOLLAR, UNDERSCORE).equals(name)) {
        return function;
      }
    }
    for (Fact fact : model.getFactDeclList()) {
      if (fact.getName().replaceAll("\\" + DOLLAR, UNDERSCORE).equals(name)) {
        return fact;
      }
    }
    return null;
  }

  public static long product(List<Integer> integers) {
    long product = 1;
    for (int i : integers) {
      product *= i;
    }
    return product;
  }
}
