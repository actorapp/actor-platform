<?xml version="1.0" encoding="UTF-8"?>
<model modelUID="r:c72a6870-63c0-4461-a878-b1c9ab9388c9(im.actor.apiLanguage.constraints)">
  <persistence version="8" />
  <language namespace="3f4bc5f5-c6c1-4a28-8b10-c83066ffa4a1(jetbrains.mps.lang.constraints)" />
  <devkit namespace="fbc25dd2-5da4-483a-8b19-70928e1b62d7(jetbrains.mps.devkit.general-purpose)" />
  <import index="tsp6" modelUID="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.apiLanguage.structure)" version="10" />
  <import index="tp1t" modelUID="r:00000000-0000-4000-0000-011c8959030d(jetbrains.mps.lang.constraints.structure)" version="9" implicit="yes" />
  <import index="tpck" modelUID="r:00000000-0000-4000-0000-011c89590288(jetbrains.mps.lang.core.structure)" version="0" implicit="yes" />
  <import index="tpee" modelUID="r:00000000-0000-4000-0000-011c895902ca(jetbrains.mps.baseLanguage.structure)" version="5" implicit="yes" />
  <import index="e2lb" modelUID="f:java_stub#6354ebe7-c22a-4a0f-ac54-50b52ab9b065#java.lang(JDK/java.lang@java_stub)" version="-1" implicit="yes" />
  <root type="tp1t.ConceptConstraints" typeId="tp1t.1213093968558" id="2348480312264250014" nodeInfo="ng">
    <link role="concept" roleId="tp1t.1213093996982" targetNodeId="tsp6.2348480312264233362" resolveInfo="ApiSection" />
  </root>
  <root type="tp1t.ConceptConstraints" typeId="tp1t.1213093968558" id="2348480312264429754" nodeInfo="ng">
    <link role="concept" roleId="tp1t.1213093996982" targetNodeId="tsp6.2348480312264233376" resolveInfo="ApiDef" />
  </root>
  <root type="tp1t.ConceptConstraints" typeId="tp1t.1213093968558" id="2348480312264440322" nodeInfo="ng">
    <link role="concept" roleId="tp1t.1213093996982" targetNodeId="tsp6.2348480312264231121" resolveInfo="Struct" />
    <node role="property" roleId="tp1t.1213098023997" type="tp1t.NodePropertyConstraint" typeId="tp1t.1147467115080" id="2348480312264440374" nodeInfo="ng">
      <link role="applicableProperty" roleId="tp1t.1147467295099" targetNodeId="tpck.1169194664001" resolveInfo="name" />
      <node role="propertyValidator" roleId="tp1t.1212097481299" type="tp1t.ConstraintFunction_PropertyValidator" typeId="tp1t.1212096972063" id="2348480312264440380" nodeInfo="nn">
        <node role="body" roleId="tpee.1137022507850" type="tpee.StatementList" typeId="tpee.1068580123136" id="2348480312264440381" nodeInfo="sn">
          <node role="statement" roleId="tpee.1068581517665" type="tpee.ExpressionStatement" typeId="tpee.1068580123155" id="2348480312264442107" nodeInfo="nn">
            <node role="expression" roleId="tpee.1068580123156" type="tpee.DotExpression" typeId="tpee.1197027756228" id="2348480312264442108" nodeInfo="nn">
              <node role="operand" roleId="tpee.1197027771414" type="tp1t.ConstraintsFunctionParameter_propertyValue" typeId="tp1t.1153138554286" id="2348480312264442109" nodeInfo="nn" />
              <node role="operation" roleId="tpee.1197027833540" type="tpee.InstanceMethodCallOperation" typeId="tpee.1202948039474" id="2348480312264442110" nodeInfo="nn">
                <link role="baseMethodDeclaration" roleId="tpee.1068499141037" targetNodeId="e2lb.~String%dmatches(java%dlang%dString)%cboolean" resolveInfo="matches" />
                <node role="actualArgument" roleId="tpee.1068499141038" type="tpee.StringLiteral" typeId="tpee.1070475926800" id="2348480312264442111" nodeInfo="nn">
                  <property name="value" nameId="tpee.1070475926801" value="[a-zA-Z_]([a-zA-Z0-9_]*)" />
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </root>
  <root type="tp1t.ConceptConstraints" typeId="tp1t.1213093968558" id="4092665470043482261" nodeInfo="ng">
    <link role="concept" roleId="tp1t.1213093996982" targetNodeId="tsp6.4092665470043293715" resolveInfo="RpcDocParameter" />
    <node role="referent" roleId="tp1t.1213100494875" type="tp1t.NodeReferentConstraint" typeId="tp1t.1148687176410" id="4092665470043652799" nodeInfo="ng">
      <link role="applicableLink" roleId="tp1t.1148687202698" targetNodeId="tsp6.4092665470043358846" />
      <node role="searchScopeFactory" roleId="tp1t.1148687345559" type="tp1t.InheritedNodeScopeFactory" typeId="tp1t.8401916545537438642" id="4092665470043652889" nodeInfo="ng">
        <link role="kind" roleId="tp1t.8401916545537438643" targetNodeId="tsp6.2348480312264653219" resolveInfo="StructAttribute" />
      </node>
    </node>
  </root>
  <root type="tp1t.ConceptConstraints" typeId="tp1t.1213093968558" id="773119248390085961" nodeInfo="ng">
    <link role="concept" roleId="tp1t.1213093996982" targetNodeId="tsp6.773119248390078458" resolveInfo="StructDocParameter" />
    <node role="referent" roleId="tp1t.1213100494875" type="tp1t.NodeReferentConstraint" typeId="tp1t.1148687176410" id="773119248390085962" nodeInfo="ng">
      <link role="applicableLink" roleId="tp1t.1148687202698" targetNodeId="tsp6.773119248390080451" />
      <node role="searchScopeFactory" roleId="tp1t.1148687345559" type="tp1t.InheritedNodeScopeFactory" typeId="tp1t.8401916545537438642" id="773119248390105075" nodeInfo="ng">
        <link role="kind" roleId="tp1t.8401916545537438643" targetNodeId="tsp6.2348480312264653219" resolveInfo="StructAttribute" />
      </node>
    </node>
  </root>
</model>

