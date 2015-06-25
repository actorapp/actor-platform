<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:52c9340c-ed61-4b09-bbfb-18570a524404(im.actor.language.intentions)">
  <persistence version="9" />
  <languages>
    <use id="d7a92d38-f7db-40d0-8431-763b0c3c9f20" name="jetbrains.mps.lang.intentions" version="-1" />
    <devkit ref="fbc25dd2-5da4-483a-8b19-70928e1b62d7(jetbrains.mps.devkit.general-purpose)" />
  </languages>
  <imports>
    <import index="tsp6" ref="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.language.structure)" />
  </imports>
  <registry>
    <language id="f3061a53-9226-4cc5-a443-f952ceaf5816" name="jetbrains.mps.baseLanguage">
      <concept id="1082485599095" name="jetbrains.mps.baseLanguage.structure.BlockStatement" flags="nn" index="9aQIb">
        <child id="1082485599096" name="statements" index="9aQI4" />
      </concept>
      <concept id="1215693861676" name="jetbrains.mps.baseLanguage.structure.BaseAssignmentExpression" flags="nn" index="d038R">
        <child id="1068498886297" name="rValue" index="37vLTx" />
        <child id="1068498886295" name="lValue" index="37vLTJ" />
      </concept>
      <concept id="1197027756228" name="jetbrains.mps.baseLanguage.structure.DotExpression" flags="nn" index="2OqwBi">
        <child id="1197027771414" name="operand" index="2Oq$k0" />
        <child id="1197027833540" name="operation" index="2OqNvi" />
      </concept>
      <concept id="1145552977093" name="jetbrains.mps.baseLanguage.structure.GenericNewExpression" flags="nn" index="2ShNRf">
        <child id="1145553007750" name="creator" index="2ShVmc" />
      </concept>
      <concept id="1137021947720" name="jetbrains.mps.baseLanguage.structure.ConceptFunction" flags="in" index="2VMwT0">
        <child id="1137022507850" name="body" index="2VODD2" />
      </concept>
      <concept id="1070475926800" name="jetbrains.mps.baseLanguage.structure.StringLiteral" flags="nn" index="Xl_RD">
        <property id="1070475926801" name="value" index="Xl_RC" />
      </concept>
      <concept id="1070534058343" name="jetbrains.mps.baseLanguage.structure.NullLiteral" flags="nn" index="10Nm6u" />
      <concept id="1068498886294" name="jetbrains.mps.baseLanguage.structure.AssignmentExpression" flags="nn" index="37vLTI" />
      <concept id="1068580123155" name="jetbrains.mps.baseLanguage.structure.ExpressionStatement" flags="nn" index="3clFbF">
        <child id="1068580123156" name="expression" index="3clFbG" />
      </concept>
      <concept id="1068580123159" name="jetbrains.mps.baseLanguage.structure.IfStatement" flags="nn" index="3clFbJ">
        <child id="1082485599094" name="ifFalseStatement" index="9aQIa" />
        <child id="1068580123160" name="condition" index="3clFbw" />
        <child id="1068580123161" name="ifTrue" index="3clFbx" />
      </concept>
      <concept id="1068580123136" name="jetbrains.mps.baseLanguage.structure.StatementList" flags="sn" stub="5293379017992965193" index="3clFbS">
        <child id="1068581517665" name="statement" index="3cqZAp" />
      </concept>
      <concept id="1068580123137" name="jetbrains.mps.baseLanguage.structure.BooleanConstant" flags="nn" index="3clFbT">
        <property id="1068580123138" name="value" index="3clFbU" />
      </concept>
      <concept id="1068581242878" name="jetbrains.mps.baseLanguage.structure.ReturnStatement" flags="nn" index="3cpWs6">
        <child id="1068581517676" name="expression" index="3cqZAk" />
      </concept>
      <concept id="1079359253375" name="jetbrains.mps.baseLanguage.structure.ParenthesizedExpression" flags="nn" index="1eOMI4">
        <child id="1079359253376" name="expression" index="1eOMHV" />
      </concept>
      <concept id="1081516740877" name="jetbrains.mps.baseLanguage.structure.NotExpression" flags="nn" index="3fqX7Q">
        <child id="1081516765348" name="expression" index="3fr31v" />
      </concept>
    </language>
    <language id="d7a92d38-f7db-40d0-8431-763b0c3c9f20" name="jetbrains.mps.lang.intentions">
      <concept id="1192794744107" name="jetbrains.mps.lang.intentions.structure.IntentionDeclaration" flags="ig" index="2S6QgY" />
      <concept id="1192794782375" name="jetbrains.mps.lang.intentions.structure.DescriptionBlock" flags="in" index="2S6ZIM" />
      <concept id="1192795771125" name="jetbrains.mps.lang.intentions.structure.IsApplicableBlock" flags="in" index="2SaL7w" />
      <concept id="1192795911897" name="jetbrains.mps.lang.intentions.structure.ExecuteBlock" flags="in" index="2Sbjvc" />
      <concept id="1192796902958" name="jetbrains.mps.lang.intentions.structure.ConceptFunctionParameter_node" flags="nn" index="2Sf5sV" />
      <concept id="2522969319638091381" name="jetbrains.mps.lang.intentions.structure.BaseIntentionDeclaration" flags="ig" index="2ZfUlf">
        <reference id="2522969319638198290" name="forConcept" index="2ZfgGC" />
        <child id="2522969319638198291" name="executeFunction" index="2ZfgGD" />
        <child id="2522969319638093995" name="isApplicableFunction" index="2ZfVeh" />
        <child id="2522969319638093993" name="descriptionFunction" index="2ZfVej" />
      </concept>
    </language>
    <language id="7866978e-a0f0-4cc7-81bc-4d213d9375e1" name="jetbrains.mps.lang.smodel">
      <concept id="1138676077309" name="jetbrains.mps.lang.smodel.structure.EnumMemberReference" flags="nn" index="uoxfO">
        <reference id="1138676095763" name="enumMember" index="uo_Cq" />
      </concept>
      <concept id="6973815483243445083" name="jetbrains.mps.lang.smodel.structure.EnumMemberValueRefExpression" flags="nn" index="3f7Wdw">
        <reference id="6973815483243565416" name="member" index="3f7u_j" />
        <reference id="6973815483243564601" name="enum" index="3f7vo2" />
      </concept>
      <concept id="1146171026731" name="jetbrains.mps.lang.smodel.structure.Property_HasValue_Enum" flags="nn" index="3t7uKx">
        <child id="1146171026732" name="value" index="3t7uKA" />
      </concept>
      <concept id="1180636770613" name="jetbrains.mps.lang.smodel.structure.SNodeCreator" flags="nn" index="3zrR0B">
        <child id="1180636770616" name="createdType" index="3zrR0E" />
      </concept>
      <concept id="1138055754698" name="jetbrains.mps.lang.smodel.structure.SNodeType" flags="in" index="3Tqbb2">
        <reference id="1138405853777" name="concept" index="ehGHo" />
      </concept>
      <concept id="1138056022639" name="jetbrains.mps.lang.smodel.structure.SPropertyAccess" flags="nn" index="3TrcHB">
        <reference id="1138056395725" name="property" index="3TsBF5" />
      </concept>
      <concept id="1138056143562" name="jetbrains.mps.lang.smodel.structure.SLinkAccess" flags="nn" index="3TrEf2">
        <reference id="1138056516764" name="link" index="3Tt5mk" />
      </concept>
    </language>
    <language id="ceab5195-25ea-4f22-9b92-103b95ca8c0c" name="jetbrains.mps.lang.core">
      <concept id="1169194658468" name="jetbrains.mps.lang.core.structure.INamedConcept" flags="ng" index="TrEIO">
        <property id="1169194664001" name="name" index="TrG5h" />
      </concept>
    </language>
  </registry>
  <node concept="2S6QgY" id="4ASKzdDCJmI">
    <property role="TrG5h" value="ToggleImplements" />
    <ref role="2ZfgGC" to="tsp6:22nuAqQwwzh" resolve="Struct" />
    <node concept="2S6ZIM" id="4ASKzdDCJmJ" role="2ZfVej">
      <node concept="3clFbS" id="4ASKzdDCJmK" role="2VODD2">
        <node concept="3clFbF" id="4ASKzdDCZzf" role="3cqZAp">
          <node concept="Xl_RD" id="4ASKzdDDa$P" role="3clFbG">
            <property role="Xl_RC" value="Toggle Implements" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2Sbjvc" id="4ASKzdDCJmL" role="2ZfgGD">
      <node concept="3clFbS" id="4ASKzdDCJmM" role="2VODD2">
        <node concept="3clFbJ" id="55bmeIQfLP4" role="3cqZAp">
          <node concept="3clFbS" id="55bmeIQfLP7" role="3clFbx">
            <node concept="3clFbF" id="55bmeIQfMgz" role="3cqZAp">
              <node concept="37vLTI" id="55bmeIQfMYu" role="3clFbG">
                <node concept="3clFbT" id="55bmeIQfNLu" role="37vLTx">
                  <property role="3clFbU" value="false" />
                </node>
                <node concept="2OqwBi" id="55bmeIQfMjS" role="37vLTJ">
                  <node concept="2Sf5sV" id="55bmeIQfMgy" role="2Oq$k0" />
                  <node concept="3TrcHB" id="55bmeIQfM_K" role="2OqNvi">
                    <ref role="3TsBF5" to="tsp6:4ASKzdDEhY9" resolve="hasInterface" />
                  </node>
                </node>
              </node>
            </node>
            <node concept="3clFbF" id="55bmeIQfNkZ" role="3cqZAp">
              <node concept="37vLTI" id="55bmeIQfPfK" role="3clFbG">
                <node concept="10Nm6u" id="55bmeIQfPgw" role="37vLTx" />
                <node concept="2OqwBi" id="55bmeIQfNow" role="37vLTJ">
                  <node concept="2Sf5sV" id="55bmeIQfNkX" role="2Oq$k0" />
                  <node concept="3TrEf2" id="55bmeIQfOTM" role="2OqNvi">
                    <ref role="3Tt5mk" to="tsp6:4ASKzdDECPc" />
                  </node>
                </node>
              </node>
            </node>
            <node concept="3clFbF" id="55bmeIQfPkR" role="3cqZAp">
              <node concept="37vLTI" id="55bmeIQfRse" role="3clFbG">
                <node concept="10Nm6u" id="55bmeIQfRsW" role="37vLTx" />
                <node concept="2OqwBi" id="55bmeIQfPor" role="37vLTJ">
                  <node concept="2Sf5sV" id="55bmeIQfPkP" role="2Oq$k0" />
                  <node concept="3TrEf2" id="55bmeIQfSXi" role="2OqNvi">
                    <ref role="3Tt5mk" to="tsp6:55bmeIQftnP" />
                  </node>
                </node>
              </node>
            </node>
          </node>
          <node concept="2OqwBi" id="55bmeIQfLW5" role="3clFbw">
            <node concept="2Sf5sV" id="55bmeIQfLRV" role="2Oq$k0" />
            <node concept="3TrcHB" id="55bmeIQfMe1" role="2OqNvi">
              <ref role="3TsBF5" to="tsp6:4ASKzdDEhY9" resolve="hasInterface" />
            </node>
          </node>
          <node concept="9aQIb" id="55bmeIQfT1M" role="9aQIa">
            <node concept="3clFbS" id="55bmeIQfT1N" role="9aQI4">
              <node concept="3clFbF" id="55bmeIQfT4H" role="3cqZAp">
                <node concept="37vLTI" id="55bmeIQfTMC" role="3clFbG">
                  <node concept="3clFbT" id="55bmeIQfTNe" role="37vLTx">
                    <property role="3clFbU" value="true" />
                  </node>
                  <node concept="2OqwBi" id="55bmeIQfT82" role="37vLTJ">
                    <node concept="2Sf5sV" id="55bmeIQfT4G" role="2Oq$k0" />
                    <node concept="3TrcHB" id="55bmeIQfTpU" role="2OqNvi">
                      <ref role="3TsBF5" to="tsp6:4ASKzdDEhY9" resolve="hasInterface" />
                    </node>
                  </node>
                </node>
              </node>
              <node concept="3clFbF" id="55bmeIQglLK" role="3cqZAp">
                <node concept="37vLTI" id="55bmeIQgmvF" role="3clFbG">
                  <node concept="10Nm6u" id="55bmeIQgmwj" role="37vLTx" />
                  <node concept="2OqwBi" id="55bmeIQglPb" role="37vLTJ">
                    <node concept="2Sf5sV" id="55bmeIQglLI" role="2Oq$k0" />
                    <node concept="3TrEf2" id="55bmeIQgm9D" role="2OqNvi">
                      <ref role="3Tt5mk" to="tsp6:4ASKzdDECPc" />
                    </node>
                  </node>
                </node>
              </node>
              <node concept="3clFbF" id="55bmeIQfTX0" role="3cqZAp">
                <node concept="37vLTI" id="55bmeIQfUBg" role="3clFbG">
                  <node concept="2OqwBi" id="55bmeIQfU0o" role="37vLTJ">
                    <node concept="2Sf5sV" id="55bmeIQfTWY" role="2Oq$k0" />
                    <node concept="3TrEf2" id="55bmeIQfUid" role="2OqNvi">
                      <ref role="3Tt5mk" to="tsp6:55bmeIQftnP" />
                    </node>
                  </node>
                  <node concept="2ShNRf" id="55bmeIQgdY0" role="37vLTx">
                    <node concept="3zrR0B" id="55bmeIQgcxb" role="2ShVmc">
                      <node concept="3Tqbb2" id="55bmeIQgcxc" role="3zrR0E">
                        <ref role="ehGHo" to="tsp6:44kR2PMr9Me" resolve="HeaderKey" />
                      </node>
                    </node>
                  </node>
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="2S6QgY" id="2EAJ7H6hPtg">
    <property role="TrG5h" value="HideParameter" />
    <ref role="2ZfgGC" to="tsp6:EUEXKTjFZU" resolve="StructDocParameter" />
    <node concept="2Sbjvc" id="2EAJ7H6hPth" role="2ZfgGD">
      <node concept="3clFbS" id="2EAJ7H6hPti" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6hZHj" role="3cqZAp">
          <node concept="37vLTI" id="2EAJ7H6i46n" role="3clFbG">
            <node concept="3f7Wdw" id="2EAJ7H6i4g4" role="37vLTx">
              <ref role="3f7vo2" to="tsp6:2EAJ7H6hOl6" resolve="ParameterCategory" />
              <ref role="3f7u_j" to="tsp6:2EAJ7H6hOl7" />
            </node>
            <node concept="2OqwBi" id="2EAJ7H6hZIQ" role="37vLTJ">
              <node concept="2Sf5sV" id="2EAJ7H6hZHi" role="2Oq$k0" />
              <node concept="3TrcHB" id="2EAJ7H6hZSi" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="2S6ZIM" id="2EAJ7H6hPtj" role="2ZfVej">
      <node concept="3clFbS" id="2EAJ7H6hPtk" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6hQ7F" role="3cqZAp">
          <node concept="Xl_RD" id="2EAJ7H6hQ7E" role="3clFbG">
            <property role="Xl_RC" value="Hide Parameter" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2SaL7w" id="2EAJ7H6hQTC" role="2ZfVeh">
      <node concept="3clFbS" id="2EAJ7H6hQTD" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6hYSP" role="3cqZAp">
          <node concept="3fqX7Q" id="2EAJ7H6hYSN" role="3clFbG">
            <node concept="1eOMI4" id="2EAJ7H6hZ3J" role="3fr31v">
              <node concept="2OqwBi" id="2EAJ7H6hUVg" role="1eOMHV">
                <node concept="2OqwBi" id="2EAJ7H6hRdV" role="2Oq$k0">
                  <node concept="2Sf5sV" id="2EAJ7H6hR8W" role="2Oq$k0" />
                  <node concept="3TrcHB" id="2EAJ7H6hUx_" role="2OqNvi">
                    <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
                  </node>
                </node>
                <node concept="3t7uKx" id="2EAJ7H6hYxf" role="2OqNvi">
                  <node concept="uoxfO" id="2EAJ7H6hYxh" role="3t7uKA">
                    <ref role="uo_Cq" to="tsp6:2EAJ7H6hOl7" />
                  </node>
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="2S6QgY" id="2EAJ7H6i4QV">
    <property role="TrG5h" value="ShowParameter" />
    <ref role="2ZfgGC" to="tsp6:EUEXKTjFZU" resolve="StructDocParameter" />
    <node concept="2Sbjvc" id="2EAJ7H6i4QW" role="2ZfgGD">
      <node concept="3clFbS" id="2EAJ7H6i4QX" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6i8DO" role="3cqZAp">
          <node concept="37vLTI" id="2EAJ7H6i8DP" role="3clFbG">
            <node concept="3f7Wdw" id="2EAJ7H6i8DQ" role="37vLTx">
              <ref role="3f7vo2" to="tsp6:2EAJ7H6hOl6" resolve="ParameterCategory" />
              <ref role="3f7u_j" to="tsp6:2EAJ7H6hOqO" />
            </node>
            <node concept="2OqwBi" id="2EAJ7H6i8DR" role="37vLTJ">
              <node concept="2Sf5sV" id="2EAJ7H6i8DS" role="2Oq$k0" />
              <node concept="3TrcHB" id="2EAJ7H6i8DT" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="2S6ZIM" id="2EAJ7H6i4QY" role="2ZfVej">
      <node concept="3clFbS" id="2EAJ7H6i4QZ" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6i5Rz" role="3cqZAp">
          <node concept="Xl_RD" id="2EAJ7H6i5Ry" role="3clFbG">
            <property role="Xl_RC" value="Show Parameter" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2SaL7w" id="2EAJ7H6i76f" role="2ZfVeh">
      <node concept="3clFbS" id="2EAJ7H6i76g" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6i7LV" role="3cqZAp">
          <node concept="3fqX7Q" id="2EAJ7H6i7LW" role="3clFbG">
            <node concept="1eOMI4" id="2EAJ7H6i7LX" role="3fr31v">
              <node concept="2OqwBi" id="2EAJ7H6i7LY" role="1eOMHV">
                <node concept="2OqwBi" id="2EAJ7H6i7LZ" role="2Oq$k0">
                  <node concept="2Sf5sV" id="2EAJ7H6i7M0" role="2Oq$k0" />
                  <node concept="3TrcHB" id="2EAJ7H6i7M1" role="2OqNvi">
                    <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
                  </node>
                </node>
                <node concept="3t7uKx" id="2EAJ7H6i7M2" role="2OqNvi">
                  <node concept="uoxfO" id="2EAJ7H6i7M3" role="3t7uKA">
                    <ref role="uo_Cq" to="tsp6:2EAJ7H6hOqO" />
                  </node>
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="2S6QgY" id="2EAJ7H6i8Wq">
    <property role="TrG5h" value="ShrinkParameter" />
    <ref role="2ZfgGC" to="tsp6:EUEXKTjFZU" resolve="StructDocParameter" />
    <node concept="2Sbjvc" id="2EAJ7H6i8Wr" role="2ZfgGD">
      <node concept="3clFbS" id="2EAJ7H6i8Ws" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6ibRj" role="3cqZAp">
          <node concept="37vLTI" id="2EAJ7H6ibRk" role="3clFbG">
            <node concept="3f7Wdw" id="2EAJ7H6ibRl" role="37vLTx">
              <ref role="3f7vo2" to="tsp6:2EAJ7H6hOl6" resolve="ParameterCategory" />
              <ref role="3f7u_j" to="tsp6:2EAJ7H6hOqR" />
            </node>
            <node concept="2OqwBi" id="2EAJ7H6ibRm" role="37vLTJ">
              <node concept="2Sf5sV" id="2EAJ7H6ibRn" role="2Oq$k0" />
              <node concept="3TrcHB" id="2EAJ7H6ibRo" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="2S6ZIM" id="2EAJ7H6i8Wt" role="2ZfVej">
      <node concept="3clFbS" id="2EAJ7H6i8Wu" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6ialQ" role="3cqZAp">
          <node concept="Xl_RD" id="2EAJ7H6ialP" role="3clFbG">
            <property role="Xl_RC" value="Shrink Parameter" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2SaL7w" id="2EAJ7H6ia3D" role="2ZfVeh">
      <node concept="3clFbS" id="2EAJ7H6ia3E" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6ia9i" role="3cqZAp">
          <node concept="3fqX7Q" id="2EAJ7H6ia9j" role="3clFbG">
            <node concept="1eOMI4" id="2EAJ7H6ia9k" role="3fr31v">
              <node concept="2OqwBi" id="2EAJ7H6ia9l" role="1eOMHV">
                <node concept="2OqwBi" id="2EAJ7H6ia9m" role="2Oq$k0">
                  <node concept="2Sf5sV" id="2EAJ7H6ia9n" role="2Oq$k0" />
                  <node concept="3TrcHB" id="2EAJ7H6ia9o" role="2OqNvi">
                    <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
                  </node>
                </node>
                <node concept="3t7uKx" id="2EAJ7H6ia9p" role="2OqNvi">
                  <node concept="uoxfO" id="2EAJ7H6ia9q" role="3t7uKA">
                    <ref role="uo_Cq" to="tsp6:2EAJ7H6hOqR" />
                  </node>
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="2S6QgY" id="2EAJ7H6icfm">
    <property role="TrG5h" value="DangerParameter" />
    <ref role="2ZfgGC" to="tsp6:EUEXKTjFZU" resolve="StructDocParameter" />
    <node concept="2Sbjvc" id="2EAJ7H6icfn" role="2ZfgGD">
      <node concept="3clFbS" id="2EAJ7H6icfo" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6ijfv" role="3cqZAp">
          <node concept="37vLTI" id="2EAJ7H6ijfw" role="3clFbG">
            <node concept="3f7Wdw" id="2EAJ7H6ijfx" role="37vLTx">
              <ref role="3f7vo2" to="tsp6:2EAJ7H6hOl6" resolve="ParameterCategory" />
              <ref role="3f7u_j" to="tsp6:2EAJ7H6hOqV" />
            </node>
            <node concept="2OqwBi" id="2EAJ7H6ijfy" role="37vLTJ">
              <node concept="2Sf5sV" id="2EAJ7H6ijfz" role="2Oq$k0" />
              <node concept="3TrcHB" id="2EAJ7H6ijf$" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="2S6ZIM" id="2EAJ7H6icfp" role="2ZfVej">
      <node concept="3clFbS" id="2EAJ7H6icfq" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6idhn" role="3cqZAp">
          <node concept="Xl_RD" id="2EAJ7H6idhm" role="3clFbG">
            <property role="Xl_RC" value="Mark as Danger" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2SaL7w" id="2EAJ7H6ieTH" role="2ZfVeh">
      <node concept="3clFbS" id="2EAJ7H6ieTI" role="2VODD2">
        <node concept="3clFbF" id="2EAJ7H6ifBF" role="3cqZAp">
          <node concept="3fqX7Q" id="2EAJ7H6ifBG" role="3clFbG">
            <node concept="1eOMI4" id="2EAJ7H6ifBH" role="3fr31v">
              <node concept="2OqwBi" id="2EAJ7H6ifBI" role="1eOMHV">
                <node concept="2OqwBi" id="2EAJ7H6ifBJ" role="2Oq$k0">
                  <node concept="2Sf5sV" id="2EAJ7H6ifBK" role="2Oq$k0" />
                  <node concept="3TrcHB" id="2EAJ7H6ifBL" role="2OqNvi">
                    <ref role="3TsBF5" to="tsp6:2EAJ7H6hOv1" resolve="category" />
                  </node>
                </node>
                <node concept="3t7uKx" id="2EAJ7H6ifBM" role="2OqNvi">
                  <node concept="uoxfO" id="2EAJ7H6ifBN" role="3t7uKA">
                    <ref role="uo_Cq" to="tsp6:2EAJ7H6hOqV" />
                  </node>
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="2S6QgY" id="5NX0N0RPZlW">
    <property role="TrG5h" value="SwitchTrait" />
    <ref role="2ZfgGC" to="tsp6:4ASKzdDBfFg" resolve="Trait" />
    <node concept="2Sbjvc" id="5NX0N0RPZlX" role="2ZfgGD">
      <node concept="3clFbS" id="5NX0N0RPZlY" role="2VODD2">
        <node concept="3clFbF" id="5NX0N0RQ2PZ" role="3cqZAp">
          <node concept="37vLTI" id="5NX0N0RQ3tP" role="3clFbG">
            <node concept="3fqX7Q" id="5NX0N0RQ3u9" role="37vLTx">
              <node concept="2OqwBi" id="5NX0N0RQ3Bs" role="3fr31v">
                <node concept="2Sf5sV" id="5NX0N0RQ3$n" role="2Oq$k0" />
                <node concept="3TrcHB" id="5NX0N0RQ3Vq" role="2OqNvi">
                  <ref role="3TsBF5" to="tsp6:5NX0N0RPBrE" resolve="isContainer" />
                </node>
              </node>
            </node>
            <node concept="2OqwBi" id="5NX0N0RQ2Se" role="37vLTJ">
              <node concept="2Sf5sV" id="5NX0N0RQ2PX" role="2Oq$k0" />
              <node concept="3TrcHB" id="5NX0N0RQ35k" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:5NX0N0RPBrE" resolve="isContainer" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="2S6ZIM" id="5NX0N0RPZlZ" role="2ZfVej">
      <node concept="3clFbS" id="5NX0N0RPZm0" role="2VODD2">
        <node concept="3clFbJ" id="5NX0N0RQ0b$" role="3cqZAp">
          <node concept="3clFbS" id="5NX0N0RQ0bA" role="3clFbx">
            <node concept="3cpWs6" id="5NX0N0RQ1uL" role="3cqZAp">
              <node concept="Xl_RD" id="5NX0N0RQ0Ub" role="3cqZAk">
                <property role="Xl_RC" value="Change to Trait" />
              </node>
            </node>
          </node>
          <node concept="2OqwBi" id="5NX0N0RQ0m$" role="3clFbw">
            <node concept="2Sf5sV" id="5NX0N0RQ0h9" role="2Oq$k0" />
            <node concept="3TrcHB" id="5NX0N0RQ0HU" role="2OqNvi">
              <ref role="3TsBF5" to="tsp6:5NX0N0RPBrE" resolve="isContainer" />
            </node>
          </node>
          <node concept="9aQIb" id="5NX0N0RQ16o" role="9aQIa">
            <node concept="3clFbS" id="5NX0N0RQ16p" role="9aQI4">
              <node concept="3cpWs6" id="5NX0N0RQ1L1" role="3cqZAp">
                <node concept="Xl_RD" id="5NX0N0RQ1iu" role="3cqZAk">
                  <property role="Xl_RC" value="Change to Container" />
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="2S6QgY" id="2tyCW$U0lRF">
    <property role="TrG5h" value="ToggleDeprecated" />
    <ref role="2ZfgGC" to="tsp6:22nuAqQy7Az" resolve="StructAttribute" />
    <node concept="2Sbjvc" id="2tyCW$U0lRG" role="2ZfgGD">
      <node concept="3clFbS" id="2tyCW$U0lRH" role="2VODD2">
        <node concept="3clFbF" id="2tyCW$U0nwP" role="3cqZAp">
          <node concept="37vLTI" id="2tyCW$U0nYg" role="3clFbG">
            <node concept="3fqX7Q" id="2tyCW$U0nY$" role="37vLTx">
              <node concept="2OqwBi" id="2tyCW$U0oaD" role="3fr31v">
                <node concept="2Sf5sV" id="2tyCW$U0o8q" role="2Oq$k0" />
                <node concept="3TrcHB" id="2tyCW$U0okF" role="2OqNvi">
                  <ref role="3TsBF5" to="tsp6:2tyCW$U0knp" resolve="isDeprecated" />
                </node>
              </node>
            </node>
            <node concept="2OqwBi" id="2tyCW$U0nyo" role="37vLTJ">
              <node concept="2Sf5sV" id="2tyCW$U0nwO" role="2Oq$k0" />
              <node concept="3TrcHB" id="2tyCW$U0nG0" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:2tyCW$U0knp" resolve="isDeprecated" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="2S6ZIM" id="2tyCW$U0lRI" role="2ZfVej">
      <node concept="3clFbS" id="2tyCW$U0lRJ" role="2VODD2">
        <node concept="3clFbF" id="2tyCW$U0mpT" role="3cqZAp">
          <node concept="Xl_RD" id="2tyCW$U0mpS" role="3clFbG">
            <property role="Xl_RC" value="Toogle Deperecate" />
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="2S6QgY" id="2tyCW$U4wc5">
    <property role="TrG5h" value="ToggleExpandable" />
    <ref role="2ZfgGC" to="tsp6:22nuAqQwwzh" resolve="Struct" />
    <node concept="2Sbjvc" id="2tyCW$U4wc6" role="2ZfgGD">
      <node concept="3clFbS" id="2tyCW$U4wc7" role="2VODD2">
        <node concept="3clFbF" id="2tyCW$U4_k2" role="3cqZAp">
          <node concept="37vLTI" id="2tyCW$U4A6q" role="3clFbG">
            <node concept="3fqX7Q" id="2tyCW$U4A6I" role="37vLTx">
              <node concept="2OqwBi" id="2tyCW$U4Ama" role="3fr31v">
                <node concept="2Sf5sV" id="2tyCW$U4AhI" role="2Oq$k0" />
                <node concept="3TrcHB" id="2tyCW$U4ADi" role="2OqNvi">
                  <ref role="3TsBF5" to="tsp6:2tyCW$U4uvE" resolve="isExpandable" />
                </node>
              </node>
            </node>
            <node concept="2OqwBi" id="2tyCW$U4_no" role="37vLTJ">
              <node concept="2Sf5sV" id="2tyCW$U4_k1" role="2Oq$k0" />
              <node concept="3TrcHB" id="2tyCW$U4_DU" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:2tyCW$U4uvE" resolve="isExpandable" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="2S6ZIM" id="2tyCW$U4wc8" role="2ZfVej">
      <node concept="3clFbS" id="2tyCW$U4wc9" role="2VODD2">
        <node concept="3clFbJ" id="2tyCW$U4x70" role="3cqZAp">
          <node concept="3clFbS" id="2tyCW$U4x71" role="3clFbx">
            <node concept="3cpWs6" id="2tyCW$U4ysK" role="3cqZAp">
              <node concept="Xl_RD" id="2tyCW$U4yQM" role="3cqZAk">
                <property role="Xl_RC" value="Disable Expandable" />
              </node>
            </node>
          </node>
          <node concept="2OqwBi" id="2tyCW$U4xou" role="3clFbw">
            <node concept="2Sf5sV" id="2tyCW$U4xhG" role="2Oq$k0" />
            <node concept="3TrcHB" id="2tyCW$U4xPs" role="2OqNvi">
              <ref role="3TsBF5" to="tsp6:2tyCW$U4uvE" resolve="isExpandable" />
            </node>
          </node>
          <node concept="9aQIb" id="2tyCW$U4z4a" role="9aQIa">
            <node concept="3clFbS" id="2tyCW$U4z4b" role="9aQI4">
              <node concept="3cpWs6" id="2tyCW$U4za6" role="3cqZAp">
                <node concept="Xl_RD" id="2tyCW$U4zt$" role="3cqZAk">
                  <property role="Xl_RC" value="Enable Expandable" />
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
</model>

