<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:52c9340c-ed61-4b09-bbfb-18570a524404(im.actor.apiLanguage.intentions)">
  <persistence version="9" />
  <languages>
    <use id="d7a92d38-f7db-40d0-8431-763b0c3c9f20" name="jetbrains.mps.lang.intentions" version="-1" />
    <devkit ref="fbc25dd2-5da4-483a-8b19-70928e1b62d7(jetbrains.mps.devkit.general-purpose)" />
  </languages>
  <imports>
    <import index="tsp6" ref="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.apiLanguage.structure)" />
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
    </language>
    <language id="d7a92d38-f7db-40d0-8431-763b0c3c9f20" name="jetbrains.mps.lang.intentions">
      <concept id="1192794744107" name="jetbrains.mps.lang.intentions.structure.IntentionDeclaration" flags="ig" index="2S6QgY" />
      <concept id="1192794782375" name="jetbrains.mps.lang.intentions.structure.DescriptionBlock" flags="in" index="2S6ZIM" />
      <concept id="1192795911897" name="jetbrains.mps.lang.intentions.structure.ExecuteBlock" flags="in" index="2Sbjvc" />
      <concept id="1192796902958" name="jetbrains.mps.lang.intentions.structure.ConceptFunctionParameter_node" flags="nn" index="2Sf5sV" />
      <concept id="2522969319638091381" name="jetbrains.mps.lang.intentions.structure.BaseIntentionDeclaration" flags="ig" index="2ZfUlf">
        <reference id="2522969319638198290" name="forConcept" index="2ZfgGC" />
        <child id="2522969319638198291" name="executeFunction" index="2ZfgGD" />
        <child id="2522969319638093993" name="descriptionFunction" index="2ZfVej" />
      </concept>
    </language>
    <language id="7866978e-a0f0-4cc7-81bc-4d213d9375e1" name="jetbrains.mps.lang.smodel">
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
</model>

