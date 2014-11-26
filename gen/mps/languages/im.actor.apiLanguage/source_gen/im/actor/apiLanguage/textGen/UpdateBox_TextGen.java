package im.actor.apiLanguage.textGen;

/*Generated by MPS */

import jetbrains.mps.textGen.SNodeTextGen;
import org.jetbrains.mps.openapi.model.SNode;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SPropertyOperations;
import im.actor.apiLanguage.behavior.HeaderKey_Behavior;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SLinkOperations;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SNodeOperations;

public class UpdateBox_TextGen extends SNodeTextGen {
  public void doGenerateText(SNode node) {
    this.append("{\"type\":\"update_box\",\"content\":{");
    this.appendNewLine();
    this.append("\"name\":\"");
    this.append(SPropertyOperations.getString(node, "name"));
    this.append("\",");
    this.appendNewLine();
    this.append("\"header\":");
    this.append(HeaderKey_Behavior.call_intValue_4689615199750893375(SLinkOperations.getTarget(node, "header", true)) + "");
    this.append(",");
    this.appendNewLine();
    if (SLinkOperations.getTargets(node, "docs", true) != null && SLinkOperations.getTargets(node, "docs", true).size() > 0) {
      this.append("\"doc\":[");
      this.appendNewLine();
      Boolean isFirstAttribute = true;
      for (SNode doc : ListSequence.fromList(SLinkOperations.getTargets(node, "docs", true))) {
        if (!(isFirstAttribute)) {
          this.append(",");
        } else {
          isFirstAttribute = false;
        }
        if (SNodeOperations.isInstanceOf(doc, "im.actor.apiLanguage.structure.StructDocComment")) {
          this.append("\"");
          this.append(SPropertyOperations.getString(SNodeOperations.cast(doc, "im.actor.apiLanguage.structure.StructDocComment"), "content"));
          this.append("\"");
        } else {
          SNode docParameter = SNodeOperations.cast(doc, "im.actor.apiLanguage.structure.StructDocParameter");
          this.append("{\"type\":\"reference\",\"argument\":\"");
          this.append(SPropertyOperations.getString(SLinkOperations.getTarget(docParameter, "paramter", false), "name"));
          this.append("\",");
          this.append("\"description\":\"");
          this.appendWithIndent(SPropertyOperations.getString(docParameter, "description"));
          this.append("\"}");
        }
      }
      this.append("],");
    }

    this.append("\"attributes\":[");
    this.appendNewLine();
    Boolean isFirstAttribute = true;
    for (SNode attr : ListSequence.fromList(SLinkOperations.getTargets(node, "attributes", true))) {
      if (!(isFirstAttribute)) {
        this.append(",");
      } else {
        isFirstAttribute = false;
      }
      this.append("{\"type\":");
      appendNode(SLinkOperations.getTarget(attr, "type", true));
      this.append(",\"id\":");
      this.append(SPropertyOperations.getInteger(attr, "id") + "");
      this.append(",\"name\":\"");
      this.append(SPropertyOperations.getString(attr, "name"));
      this.append("\"}");
      this.appendNewLine();
    }
    this.append("]}}");
  }
}
