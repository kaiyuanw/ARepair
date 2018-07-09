package clean;

import static parser.etc.Context.logger;
import static parser.etc.Names.TMPT_FILE_PATH;

import clean.visitor.SimpleModelVisitor;
import edu.mit.csail.sdg.parser.CompModule;
import parser.ast.nodes.ModelUnit;
import parser.util.AlloyUtil;
import parser.util.FileUtil;

public class ModelSimplifier {

  public static String simplify(ModelUnit modelUnit) {
    SimpleModelVisitor smv = new SimpleModelVisitor();
    String modelAsString = modelUnit.accept(smv, null);
    FileUtil.writeText(modelAsString, TMPT_FILE_PATH, false);
    while (true) {
      CompModule module = AlloyUtil.compileAlloyModule(TMPT_FILE_PATH);
      modelUnit = new ModelUnit(null, module);
      String newModelAsString = modelUnit.accept(smv, null);
      if (modelAsString.equals(newModelAsString)) {
        break;
      }
      FileUtil.writeText(newModelAsString, TMPT_FILE_PATH, false);
      modelAsString = newModelAsString;
    }
    return modelAsString;
  }
}
