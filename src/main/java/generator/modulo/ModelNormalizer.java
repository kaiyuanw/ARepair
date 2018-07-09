package generator.modulo;

import static parser.etc.Names.COMMA;
import static parser.etc.Names.DOLLAR;
import static parser.etc.Names.NEW_LINE;
import static parser.etc.Names.TEST_PREFIX;
import static parser.etc.Names.UNDERSCORE;
import static parser.util.AlloyUtil.getFirstNonNOOPChild;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
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
import parser.ast.nodes.SigDecl.MULT;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryExpr.UnaryOp;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import parser.ast.visitor.GenericVisitor;

public class ModelNormalizer implements GenericVisitor<String, Object> {

  private boolean inSigDecl;
  private boolean inTestPred;
  /**
   * Stores the predicate/function names declared in the current model.
   */
  private Set<String> paragraphNames;

  public ModelNormalizer() {
    this.inSigDecl = false;
    this.inTestPred = false;
    this.paragraphNames = new LinkedHashSet<>();
  }

  @Override
  public String visit(ModelUnit n, Object arg) {
    // Collect paragraph names first.
    n.getPredDeclList().forEach(predicate -> paragraphNames.add(predicate.getName()));
    n.getFunDeclList().forEach(function -> paragraphNames.add(function.getName()));
    String moduleDecl = n.getModuleDecl().accept(this, arg);
    String openDecls = String.join(NEW_LINE,
        n.getOpenDeclList().stream().map(openDecl -> openDecl.accept(this, arg))
            .collect(Collectors.toList()));
    String sigDecls = String.join(NEW_LINE,
        n.getSigDeclList().stream().map(signature -> signature.accept(this, arg))
            .collect(Collectors.toList()));
    // We need to print test predicates.
    String predDecls = String.join(NEW_LINE,
        n.getPredDeclList().stream()
            .filter(predicate -> predicate.getName().startsWith(TEST_PREFIX))
            .map(predicate -> predicate.accept(this, arg))
            .collect(Collectors.toList()));
    return String
        .join(NEW_LINE, Arrays.<CharSequence>asList(moduleDecl, openDecls, sigDecls, predDecls));
  }

  @Override
  public String visit(ModuleDecl n, Object arg) {
    return String.join(" ", Arrays.<CharSequence>asList("module", n.getModelName()));
  }

  @Override
  public String visit(OpenDecl n, Object arg) {
    return String.join(" ",
        Arrays.asList("open", n.getFileName(), n.getArguments().toString(), "as", n.getAlias()));
  }

  @Override
  public String visit(SigDecl n, Object arg) {
    inSigDecl = true;
    // We do not need fact.
    String sigDeclAsString =
        (n.isAbstract() ? "abstract " : "") + MULT.SET + "sig " + n.getName() + " " + (
            n.isTopLevel() ? "" : (n.isSubsig() ? "extends" : "in") + " " + n.getParentName() + " ")
            + "{" +
            (n.getFieldList().size() > 0 ? NEW_LINE + String.join(COMMA + NEW_LINE,
                n.getFieldList().stream().map(field -> field.accept(this, arg))
                    .collect(Collectors.toList())) + NEW_LINE : "") +
            "}";
    inSigDecl = false;
    return sigDeclAsString;
  }

  @Override
  public String visit(FieldDecl n, Object arg) {
    return visitRelDecl(n, arg);
  }

  @Override
  public String visit(ParamDecl n, Object arg) {
    return visitRelDecl(n, arg);
  }

  @Override
  public String visit(VarDecl n, Object arg) {
    return visitRelDecl(n, arg);
  }

  private String visitRelDecl(RelDecl n, Object arg) {
    return (n.isDisjoint() ? "disj " : "") + String.join(COMMA,
        n.getVariables().stream()
            .map(variable -> variable.accept(this, arg))
            .collect(Collectors.toList())) + ": " + n.getExpr().accept(this, arg);
  }

  @Override
  public String visit(ExprOrFormula n, Object arg) {
    return n.accept(this, arg);
  }

  @Override
  public String visit(SigExpr n, Object arg) {
    return n.getName();
  }

  @Override
  public String visit(FieldExpr n, Object arg) {
    return n.getName();
  }

  @Override
  public String visit(VarExpr n, Object arg) {
    return n.getName();
  }

  @Override
  public String visit(UnaryExpr n, Object arg) {
    String subAsString = n.getSub().accept(this, arg);
    if (n.getOp() == UnaryExpr.UnaryOp.NOOP) {
      return subAsString;
    }
    UnaryExpr.UnaryOp op = n.getOp();
    // Do not change variable declaration in tests.
    if (!inTestPred) {
      switch (op) {
        case SET:
        case LONE:
        case ONE:
        case SOME:
          op = UnaryOp.SET;
          break;
      }
    }
    return "(" + op + subAsString + ")";
  }

  @Override
  public String visit(UnaryFormula n, Object arg) {
    return "(" + n.getOp() + n.getSub().accept(this, arg) + ")";
  }

  @Override
  public String visit(BinaryExpr n, Object arg) {
    if (inSigDecl) {
      if (n.getLeft() instanceof VarExpr) {
        String value = ((VarExpr) n.getLeft()).getName();
        if (value.equals("this")) {
          // E.g.
          // sig Book {
          //	entry: set Name,
          //	listed: entry ->set Listing
          // }
          // entry -> set Listing is actually this.entry -> set Listing.
          return n.getRight().accept(this, arg);
        }
      }
    }
    return "(" + n.getLeft().accept(this, arg) + n.getOp() + n.getRight().accept(this, arg) + ")";
  }

  @Override
  public String visit(BinaryFormula n, Object arg) {
    BinaryFormula.BinaryOp op = n.getOp();
    // We want to remove function calls, e.g. Depth[n] = 1.
    switch (op) {
      case EQUALS:
      case NOT_EQUALS:
        Node leftExpr = getFirstNonNOOPChild(n.getLeft());
        Node rightExpr = getFirstNonNOOPChild(n.getRight());
        // It is possible that the CallExpr is some method outside of the model.
        // E.g. ord/first = State0, in which case we do not want to exclude.
        // So we need to also check the name of the CallExpr
        if ((leftExpr instanceof CallExpr && paragraphNames
            .contains(((CallExpr) leftExpr).getName())) || (rightExpr instanceof CallExpr
            && paragraphNames.contains(((CallExpr) rightExpr).getName()))) {
          return "";
        }
    }
    return "(" + n.getLeft().accept(this, arg) + n.getOp() + n.getRight().accept(this, arg) + ")";
  }

  @Override
  public String visit(ListExpr n, Object arg) {
    return "(" + String.join(n.getOp().toString(),
        n.getArguments().stream()
            .map(expr -> expr.accept(this, arg))
            .collect(Collectors.toList()))
        + ")";
  }

  @Override
  public String visit(ListFormula n, Object arg) {
    boolean flattenList = false;
    if (n.getParent() instanceof ListFormula) {
      ListFormula parent = (ListFormula) n.getParent();
      if (parent.getOp() == n.getOp()) {
        flattenList = true;
      }
    }
    String innerString = String.join(n.getOp().toString(),
        n.getArguments().stream()
            .map(expr -> expr.accept(this, arg))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList()));
    if (flattenList) {
      return innerString;
    }
    return "(" + innerString + ")";
  }

  @Override
  public String visit(CallExpr n, Object arg) {
    // Ignore function calls.
    return "(" + n.getName() + "[" + String.join(COMMA,
        n.getArguments().stream().map(argument -> argument.accept(this, arg))
            .collect(Collectors.toList())) + "]" + ")";
  }

  @Override
  public String visit(CallFormula n, Object arg) {
    // Ignore predicate calls.
    return "";
  }

  @Override
  public String visit(QtExpr n, Object arg) {
    String qtExpr = n.getOp() + String.join(COMMA,
        n.getVarDecls().stream().map(varDecl -> varDecl.accept(this, arg))
            .collect(Collectors.toList())) + " " + n.getBody().accept(this, arg);
    // {v: D | F} is comprehension
    if (n.getOp() == QtExpr.Quantifier.COMPREHENSION) {
      qtExpr = "{ " + qtExpr + " }";
    }
    return "(" + qtExpr + ")";
  }

  @Override
  public String visit(QtFormula n, Object arg) {
    String qtExpr = n.getOp() + String.join(COMMA,
        n.getVarDecls().stream().map(varDecl -> varDecl.accept(this, arg))
            .collect(Collectors.toList())) + " " + n.getBody().accept(this, arg);
    return "(" + qtExpr + ")";
  }

  @Override
  public String visit(ITEExpr n, Object arg) {
    return "(" + n.getCondition().accept(this, arg) + " => " + n.getThenClause().accept(this, arg)
        + " else " + n.getElseClause().accept(this, arg) + ")";
  }

  @Override
  public String visit(ITEFormula n, Object arg) {
    return "(" + n.getCondition().accept(this, arg) + " => " + n.getThenClause().accept(this, arg)
        + " else " + n.getElseClause().accept(this, arg) + ")";
  }

  @Override
  public String visit(LetExpr n, Object arg) {
    return "(let " + n.getVar().accept(this, arg) + " = " + n.getBound().accept(this, arg) + " " + n
        .getBody().accept(this, arg) + ")";
  }

  @Override
  public String visit(ConstExpr n, Object arg) {
    return n.getValue();
  }

  @Override
  public String visit(Body n, Object arg) {
    return "{" + NEW_LINE + n.getBodyExpr().accept(this, arg) + NEW_LINE + "}";
  }

  @Override
  public String visit(Predicate n, Object arg) {
    inTestPred = true;
    String res =
        "pred " + n.getName().replaceAll("\\" + DOLLAR, UNDERSCORE) + "[" + String.join(COMMA,
            n.getParamList().stream().map(parameter -> parameter.accept(this, arg))
                .collect(Collectors.toList())) + "] " + n.getBody().accept(this, arg);
    inTestPred = false;
    return res;
  }

  @Override
  public String visit(Function n, Object arg) {
    // Can return "".
    return "fun " + n.getName().replaceAll("\\" + DOLLAR, UNDERSCORE) + "[" + String.join(COMMA,
        n.getParamList().stream().map(parameter -> parameter.accept(this, arg))
            .collect(Collectors.toList())) + "] : " + n.getReturnType().accept(this, arg) + " "
        + n.getBody().accept(this, arg);
  }

  @Override
  public String visit(Fact n, Object arg) {
    // Ignore facts.
    return "";
  }

  @Override
  public String visit(Assertion n, Object arg) {
    // Ignore assertions.
    return "";
  }

  @Override
  public String visit(Run n, Object arg) {
    // Ignore run commands.
    return "";
  }

  @Override
  public String visit(Check n, Object arg) {
    // Ignore check commands.
    return "";
  }
}