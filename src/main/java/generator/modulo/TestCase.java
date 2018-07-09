package generator.modulo;

import static generator.util.Util.findRealDomainExpr;
import static parser.etc.Names.COMMA;
import static parser.etc.Names.DOLLAR;
import static parser.etc.Names.VERTICAL_BAR;
import static parser.util.AlloyUtil.findNodeWithType;
import static parser.util.AlloyUtil.getFirstNonNOOPChild;

import edu.mit.csail.sdg.ast.ExprVar;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Solution;
import generator.fragment.Expression;
import generator.opt.GeneratorOpt;
import generator.util.TypeInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.LetExpr;
import parser.ast.nodes.Node;
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.RelDecl;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;

public class TestCase {

  private String name;
  private GeneratorOpt opt;
  /**
   * This field stores the keywords/signature/field/variable to domain mapping.
   */
  private Map<Node, Domain> nodeToDomain;
  /**
   * Used for sorting the used variables.
   */
  private Map<Node, String> uid;
  private CompModule module;
  private A4Solution sol;

  /**
   * This cache helps reuse expression values evaluated before.  E.g., before we evaluate n1 when
   * the variables in scope is n1.  Now we do not reevaluate n1 when the variables in scope is n1
   * and n2.
   */
  private Map<String, String> exprCache;

  public TestCase(String name, GeneratorOpt opt, Map<Node, TypeInfo> nodeToType,
      Map<Node, String> uid, CompModule module, A4Solution sol) {
    this.name = name;
    this.opt = opt;
    this.uid = uid;
    this.module = module;
    for (ExprVar atom : sol.getAllAtoms()) {
      this.module.addGlobal(atom.label, atom);
    }
    this.sol = sol;
    this.nodeToDomain = initializeNodeToDomain(nodeToType);
    this.exprCache = new LinkedHashMap<>();
  }

  private Map<Node, Domain> initializeNodeToDomain(Map<Node, TypeInfo> nodeToType) {
    Map<Node, Domain> n2d = new LinkedHashMap<>();

    nodeToType.forEach((node, type) -> {
      // Add quantifiers/parameters to domain mapping.
      if (node instanceof ParamDecl || node instanceof VarDecl) {
        RelDecl relDecl = (RelDecl) node;
        // If the domain does not have any variable, then parse it.
        boolean hasVar = !findNodeWithType(relDecl.getExpr(), VarExpr.class).isEmpty();
        for (ExprOrFormula variable : relDecl.getVariables()) {
          Node var = getFirstNonNOOPChild(variable);
          if (!(var instanceof VarExpr)) {
            continue;
          }
          TypeInfo varType = nodeToType.get(var);
          if (hasVar) {
            n2d.put(var, new Domain(varType, eval(varType.getTypeAsString())));
          } else {
            Node realDomain = findRealDomainExpr(relDecl.getExpr());
            n2d.put(var, new Domain(varType, eval(realDomain.accept(opt.getPSV(), null))));
          }
        }
      } else if (node instanceof LetExpr) { // Add let variable to domain mapping.
        LetExpr letExpr = (LetExpr) node;
        boolean hasVar = !findNodeWithType(letExpr.getBound(), VarExpr.class).isEmpty();
        VarExpr variable = letExpr.getRealVar();
        TypeInfo varType = nodeToType.get(variable);
        if (hasVar) {
          n2d.put(variable, new Domain(varType, eval(varType.getTypeAsString())));
        } else {
          Node realDomain = findRealDomainExpr(letExpr.getBound());
          n2d.put(variable, new Domain(varType, eval(realDomain.accept(opt.getPSV(), null))));
        }
      }
    });
    return n2d;
  }

  /**
   * Returns null if the expression is not parsable.  This typically happens for expressions with
   * variables.  Otherwise, return the A4TupleSet, Integer or Boolean.
   */
  public Object eval(String toEval) {
    return sol.eval(CompUtil.parseOneExpression_fromString(module, toEval));
  }

  /**
   * Compute unique valuations.  This method returns a string with unique variable ids as prefix,
   * followed by valuations of the expression with different combinations of variable values.
   * Returns null if the evaluation of {@code toEval} results in an error.
   */
  public String computeUniqueValuations(List<VarExpr> variables, String toEval) {
    String varIds = uniqueVariableIds(variables);
    String exprKey = varIds + toEval;
    if (exprCache.containsKey(exprKey)) {
//      System.out.println("ExprCache Hit: " + exprKey);
      return exprCache.get(exprKey);
    }
    List<String> values = new ArrayList<>();
    evaluateCombinations(values, new Stack<>(), toEval, variables, 0);
    exprCache.put(exprKey, varIds + String.join(COMMA, values));
    return exprCache.get(exprKey);
  }

  public void evaluateCombinations(List<String> res, Stack<String> lets, String toEval,
      List<VarExpr> variables, int start) {
    if (start == variables.size()) {
      String variableAssignments = String.join(COMMA, lets);
      String letPrefixes = lets.isEmpty() ? "" : ("let " + variableAssignments + VERTICAL_BAR);
      Object obj = eval(letPrefixes + toEval);
      res.add(String.valueOf(obj));
    } else {
      VarExpr variable = variables.get(start);
      List<String> domains = nodeToDomain.get(variable).getCombinations();
      for (String domain : domains) {
        lets.push(variable.getName() + " = " + domain);
        evaluateCombinations(res, lets, toEval, variables, start + 1);
        lets.pop();
      }
    }
  }

  /**
   * Compute a key for a given expression based on different valuations of the variables used.
   */
  public String computeExprKey(Expression expression) {
    List<VarExpr> orderedVariables = sortVariables(expression.getUsedVariables());
    try {
      return computeUniqueValuations(orderedVariables, expression.getValue());
    } catch (Exception e) {
      // Evaluating the generated expressions should not result in errors.
      e.printStackTrace();
      throw new RuntimeException("Evaluating " + expression.getValue() + " results in an error.");
    }
  }

  public String uniqueVariableIds(Collection<VarExpr> varExprs) {
    return String.join(DOLLAR, varExprs.stream().map(uid::get).collect(Collectors.toList()));
  }

  private List<VarExpr> sortVariables(Collection<VarExpr> variables) {
    return variables.stream()
        .sorted(Comparator.comparing(v -> uid.get(v)))
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("===== Test ").append(name).append("=====\n");
    nodeToDomain.forEach((node, domain) -> {
      sb.append(uid.get(node)).append(": {").append(String.join(COMMA, domain.getAtoms()))
          .append("}\n");
    });
    return sb.toString();
  }

  public String getName() {
    return name;
  }

  public CompModule getModule() {
    return module;
  }

  public A4Solution getSol() {
    return sol;
  }
}
