package fl;

import static alloyfl.mutation.util.ScoreInfo.sortScoreInfos;
import static parser.etc.Names.DOLLAR;
import static parser.etc.Names.IGNORE_NAMES;

import alloyfl.mutation.opt.FaultLocatorOpt;
import alloyfl.mutation.util.ScoreInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mualloy.rule.BOE;
import mualloy.rule.BOR;
import mualloy.rule.IEOE;
import mualloy.rule.LOD;
import mualloy.rule.LOR;
import mualloy.rule.MOR;
import mualloy.rule.PBD;
import mualloy.rule.QOR;
import mualloy.rule.UOD;
import mualloy.rule.UOI;
import mualloy.rule.UOR;
import mualloy.util.MInfo;
import mualloy.util.MInfo.MType;
import parser.ast.nodes.Assertion;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.Body;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
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
import parser.ast.nodes.Node;
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.QtExpr;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import parser.ast.visitor.VoidVisitorAdapter;
import parser.etc.MutationData;
import patcher.opt.PatcherOpt;
import testRunner.TestRunner;

public class FaultLocator extends VoidVisitorAdapter<Object> {

  private PatcherOpt opt;
  private ModelUnit mu;
  private MInfo mi;
  private List<Boolean> testResultsForBuggyModel;
  private Map<Node, Double> node2score;
  /**
   * If domain is null then we mutate every possible node, otherwise we mutate nodes in the domain.
   */
  private Set<Node> domain;

  private Map<Node, MutationImpact> node2impact;

  public FaultLocator(PatcherOpt opt, List<Boolean> testResultsForBuggyModel, Set<Node> domain) {
    this.opt = opt;
    this.mi = new MInfo();
    this.testResultsForBuggyModel = testResultsForBuggyModel;
    this.node2score = new HashMap<>();
    this.domain = domain;
    this.node2impact = new LinkedHashMap<>();
  }

  public MutationImpact getMutationImpact(Node node) {
    return node2impact.get(node);
  }

  @Override
  public void visit(ModelUnit n, Object arg) {
    this.mu = n;
    super.visit(n, arg);
  }

  @Override
  public void visit(SigDecl n, Object arg) {
    MType originalType = mi.getType();
    Node originalNode = mi.getNode();
    mi.setType(MType.SIG);
    mi.setNode(n);
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = MOR.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, MOR.class));
    }
    super.visit(n, arg);
    mi.setType(originalType);
    mi.setNode(originalNode);
  }

  @Override
  public void visit(FieldDecl n, Object arg) {
    MInfo.MType originalType = mi.getType();
    Node originalExtraNode = mi.getExtraNode();
    mi.setType(MType.FIELD);
    // We don't need to check if n is in domain because we do not mutate anything here.
    mi.setExtraNode(n);
    n.getExpr().accept(this, arg);
    mi.setType(originalType);
    mi.setExtraNode(originalExtraNode);
  }

  @Override
  public void visit(ParamDecl n, Object arg) {
    MInfo.MType originalType = mi.getType();
    Node originalNode = mi.getNode();
    mi.setType(MType.IGNORE);
    mi.setNode(n);
    n.getExpr().accept(this, arg);
    mi.setType(originalType);
    mi.setNode(originalNode);
  }

  @Override
  public void visit(VarDecl n, Object arg) {
    n.getExpr().accept(this, arg);
  }

  @Override
  public void visit(ExprOrFormula n, Object arg) {
    n.accept(this, arg);
  }

  @Override
  public void visit(FieldExpr n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = UOI.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, UOI.class));
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(VarExpr n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = UOI.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, UOI.class));
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(UnaryExpr n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = UOR.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, UOR.class));
      mutationDataList = UOI.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, UOI.class));
      MutationData mutationData = UOD.mutate(n, mu, opt, mi);
      if (mutationData != null) {
        process(n, mutationData, UOD.class);
      }
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(UnaryFormula n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = UOR.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, UOR.class));
      MutationData mutationData = UOD.mutate(n, mu, opt, mi);
      if (mutationData != null) {
        process(n, mutationData, UOD.class);
      }
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(BinaryExpr n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = BOR.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, BOR.class));
      mutationDataList = UOI.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, UOI.class));
      MutationData mutationData = BOE.mutate(n, mu, opt, mi);
      if (mutationData != null) {
        process(n, mutationData, BOE.class);
      }
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(BinaryFormula n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = BOR.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, BOR.class));
      MutationData mutationData = BOE.mutate(n, mu, opt, mi);
      if (mutationData != null) {
        process(n, mutationData, BOE.class);
      }
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(ListExpr n, Object arg) {
    super.visit(n, arg);
  }

  @Override
  public void visit(ListFormula n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = LOR.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, LOR.class));
      mutationDataList = LOD.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, LOD.class));
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(CallExpr n, Object arg) {
    super.visit(n, arg);
  }

  @Override
  public void visit(CallFormula n, Object arg) {
    super.visit(n, arg);
  }

  @Override
  public void visit(QtExpr n, Object arg) {
    super.visit(n, arg);
  }

  @Override
  public void visit(QtFormula n, Object arg) {
    if (domain == null || domain.contains(n)) {
      List<MutationData> mutationDataList = QOR.mutate(n, mu, opt, mi);
      mutationDataList.forEach(mutationData -> process(n, mutationData, QOR.class));
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(ITEExpr n, Object arg) {
    super.visit(n, arg);
  }

  @Override
  public void visit(ITEFormula n, Object arg) {
    if (domain == null || domain.contains(n)) {
      MutationData mutationData = IEOE.mutate(n, mu, opt, mi);
      if (mutationData != null) {
        process(n, mutationData, IEOE.class);
      }
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(LetExpr n, Object arg) {
    super.visit(n, arg);
  }

  @Override
  public void visit(Body n, Object arg) {
    if (domain == null || domain.contains(n)) {
      MutationData mutationData = PBD.mutate(n, mu, opt, mi);
      if (mutationData != null) {
        process(n, mutationData, PBD.class);
      }
    }
    super.visit(n, arg);
  }

  @Override
  public void visit(Predicate n, Object arg) {
    MType originalType = mi.getType();
    Node originalNode = mi.getNode();
    if (n.getName().contains(DOLLAR) || IGNORE_NAMES.stream()
        .anyMatch(ignoredName -> n.getName().startsWith(ignoredName))) {
      mi.setType(MType.IGNORE);
    } else {
      mi.setType(MType.PRED);
    }
    mi.setNode(n);
    super.visit(n, arg);
    mi.setType(originalType);
    mi.setNode(originalNode);
  }

  @Override
  public void visit(Function n, Object arg) {
    MType originalType = mi.getType();
    Node originalNode = mi.getNode();
    if (n.getName().contains(DOLLAR) || IGNORE_NAMES.stream()
        .anyMatch(ignoredName -> n.getName().startsWith(ignoredName))) {
      mi.setType(MType.IGNORE);
    } else {
      mi.setType(MType.FUN);
    }
    mi.setNode(n);
    super.visit(n, arg);
    mi.setType(originalType);
    mi.setNode(originalNode);
  }

  @Override
  public void visit(Fact n, Object arg) {
    MType originalType = mi.getType();
    Node originalNode = mi.getNode();
    if (IGNORE_NAMES.stream()
        .anyMatch(ignoredName -> n.getName().startsWith(ignoredName))) {
      mi.setType(MType.IGNORE);
    } else {
      mi.setType(MType.FACT);
    }
    mi.setNode(n);
    super.visit(n, arg);
    mi.setType(originalType);
    mi.setNode(originalNode);
  }

  @Override
  public void visit(Assertion n, Object arg) {
    MType originalType = mi.getType();
    Node originalNode = mi.getNode();
    mi.setType(MType.IGNORE);
    mi.setNode(n);
    super.visit(n, arg);
    mi.setType(originalType);
    mi.setNode(originalNode);
  }

  private void process(Node node, MutationData mutationData, Class<?> clazz) {
    if (mutationData.isEquivalent()) {
      return;
    }
    FaultLocatorOpt flo = new FaultLocatorOpt(opt.getModelPath(), null, opt.getTestSuitePath(),
        opt.getScope(), "ochiai", null);
    if (flo.isModelToAvoid(mutationData.getMutantAsString())) {
      return;
    }
    List<Boolean> testResultsForMutant = TestRunner.runTests(mutationData.getMutantAsString(), flo);
    int failToPass = 0;
    int passToFail = 0;
    for (int i = 0; i < testResultsForBuggyModel.size(); i++) {
      if (!testResultsForBuggyModel.get(i) && testResultsForMutant.get(i)) {
        failToPass += 1;
      }
      if (testResultsForBuggyModel.get(i) && !testResultsForMutant.get(i)) {
        passToFail += 1;
        // Early stop is fine as long as passToFail > 0.
        break;
      }
    }
    double oldScore = node2score.getOrDefault(node, 0.0);
    double newScore = flo.getFormula().compute(testResultsForBuggyModel, testResultsForMutant);
    if (oldScore < newScore) {
      node2score.put(node, newScore);
      node2impact
          .put(node, new MutationImpact(mutationData.getMutantAsString(), failToPass, passToFail,
              newScore));
    }
  }

  public List<ScoreInfo> constructScoreInfos(Map<Node, Integer> descCount) {
    List<ScoreInfo> scoreInfos = new ArrayList<>();
    node2score.forEach(
        (node, score) -> {
          // Filter out nodes with suspiciousness score <= 0.
          if (score > 0) {
            scoreInfos.add(
                new ScoreInfo(node, score, descCount.get(node), opt.getPSV()));
          }
        });
    return scoreInfos;
  }

  /**
   * Rank first based on score, then based on the number of descendants.
   */
  public List<ScoreInfo> rankNode(Map<Node, Integer> descCount) {
    List<ScoreInfo> scoreInfos = constructScoreInfos(descCount);
    sortScoreInfos(scoreInfos);
    return scoreInfos;
  }
}