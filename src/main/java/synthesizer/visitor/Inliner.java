package synthesizer.visitor;

import static parser.etc.Names.COMMA;
import static parser.etc.Names.VERTICAL_BAR;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import parser.ast.nodes.Call;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.PredOrFun;
import synthesizer.hole.Hole;

/**
 * This class inline predicate/function calls.  E.g. "p[A, B]" => "let a: A, b: B | {...}". Note
 * that this class also considers holes and the class should be in sync with the {@link
 * HoleTemplateCollector}.
 */
public class Inliner extends PrettyStringWithHoleHandler {

  protected Map<String, PredOrFun> name2paragraph;

  public Inliner(Inliner someInliner) {
    super(someInliner);
    this.name2paragraph = someInliner.name2paragraph;
  }

  public Inliner(ModelUnit model, Map<Node, Hole> node2hole) {
    super(node2hole);
    this.name2paragraph = new LinkedHashMap<>();
    model.getPredDeclList()
        .forEach(predicate -> name2paragraph.put(predicate.getName(), predicate));
    model.getFunDeclList().forEach(function -> name2paragraph.put(function.getName(), function));
  }

  /**
   * Inline predicate/function calls.
   */
  private String visitCall(Call call, Object arg) {
    if (!name2paragraph.containsKey(call.getName())) { // The call is to the outside of the module.
      return "(" + call.getName() + "[" + String.join(COMMA,
          call.getArguments().stream().map(argument -> argument.accept(this, arg))
              .collect(Collectors.toList())) + "]" + ")";
    }
    PredOrFun predOrFun = name2paragraph.get(call.getName());
    List<String> paraNames = predOrFun.getParamList().stream()
        .map(ParamDecl::getNames)
        .flatMap(List::stream)
        .collect(Collectors.toList());
    List<String> paraDomain = call.getArguments().stream()
        .map(argument -> argument.accept(this, arg))
        .collect(Collectors.toList());
    List<String> lets = new ArrayList<>();
    for (int i = 0; i < paraNames.size(); i++) {
      lets.add(paraNames.get(i) + "=" + paraDomain.get(i));
    }
    String letPrefix = lets.isEmpty() ? "" : ("let " + String.join(COMMA, lets) + VERTICAL_BAR);
    String bodyString = predOrFun.getBody().accept(this, arg);
    return letPrefix + "(" + bodyString + ")";
  }

  @Override
  public String visit(CallExpr n, Object arg) {
    return putInMap(n, "(" + visitCall(n, arg) + ")");
  }

  @Override
  public String visit(CallFormula n, Object arg) {
    return putInMap(n, "(" + visitCall(n, arg) + ")");
  }
}
