package im.actor.apiLanguage.behavior;

/*Generated by MPS */

import org.jetbrains.mps.openapi.model.SNode;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SPropertyOperations;
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory;

public class HeaderKey_Behavior {
  public static void init(SNode thisNode) {
    SPropertyOperations.set(thisNode, MetaAdapterFactory.getProperty(0x77fdf769432b4edeL, 0x8171050f8dee73fcL, 0x4114dc2d726c9c8eL, 0x4114dc2d726c9c91L, "hexValue"), "01");
  }
  public static int call_intValue_4689615199750893375(SNode thisNode) {
    try {
      return Integer.parseInt(SPropertyOperations.getString(thisNode, MetaAdapterFactory.getProperty(0x77fdf769432b4edeL, 0x8171050f8dee73fcL, 0x4114dc2d726c9c8eL, 0x4114dc2d726c9c91L, "hexValue")), 16);
    } catch (Exception e) {
      return 0;
    }
  }
}
