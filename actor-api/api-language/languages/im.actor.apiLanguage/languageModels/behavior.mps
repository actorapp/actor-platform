<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:bdd30f2e-5459-4fbf-a624-993b87581eaf(im.actor.language.behavior)">
  <persistence version="9" />
  <languages>
    <use id="af65afd8-f0dd-4942-87d9-63a55f2a9db1" name="jetbrains.mps.lang.behavior" version="-1" />
    <devkit ref="fbc25dd2-5da4-483a-8b19-70928e1b62d7(jetbrains.mps.devkit.general-purpose)" />
  </languages>
  <imports>
    <import index="tsp6" ref="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.language.structure)" />
    <import index="tpcu" ref="r:00000000-0000-4000-0000-011c89590282(jetbrains.mps.lang.core.behavior)" />
    <import index="tpck" ref="r:00000000-0000-4000-0000-011c89590288(jetbrains.mps.lang.core.structure)" />
    <import index="o8zo" ref="r:314576fc-3aee-4386-a0a5-a38348ac317d(jetbrains.mps.scope)" />
    <import index="e2lb" ref="f:java_stub#6354ebe7-c22a-4a0f-ac54-50b52ab9b065#java.lang(JDK/java.lang@java_stub)" implicit="true" />
  </imports>
  <registry>
    <language id="af65afd8-f0dd-4942-87d9-63a55f2a9db1" name="jetbrains.mps.lang.behavior">
      <concept id="1225194240794" name="jetbrains.mps.lang.behavior.structure.ConceptBehavior" flags="ng" index="13h7C7">
        <reference id="1225194240799" name="concept" index="13h7C2" />
        <child id="1225194240805" name="method" index="13h7CS" />
        <child id="1225194240801" name="constructor" index="13h7CW" />
      </concept>
      <concept id="1225194413805" name="jetbrains.mps.lang.behavior.structure.ConceptConstructorDeclaration" flags="in" index="13hLZK" />
      <concept id="1225194472830" name="jetbrains.mps.lang.behavior.structure.ConceptMethodDeclaration" flags="ng" index="13i0hz">
        <property id="1225194472832" name="isVirtual" index="13i0it" />
        <property id="1225194472834" name="isAbstract" index="13i0iv" />
        <reference id="1225194472831" name="overriddenMethod" index="13i0hy" />
      </concept>
      <concept id="1225194691553" name="jetbrains.mps.lang.behavior.structure.ThisNodeExpression" flags="nn" index="13iPFW" />
    </language>
    <language id="f3061a53-9226-4cc5-a443-f952ceaf5816" name="jetbrains.mps.baseLanguage">
      <concept id="1215693861676" name="jetbrains.mps.baseLanguage.structure.BaseAssignmentExpression" flags="nn" index="d038R">
        <child id="1068498886297" name="rValue" index="37vLTx" />
        <child id="1068498886295" name="lValue" index="37vLTJ" />
      </concept>
      <concept id="1197027756228" name="jetbrains.mps.baseLanguage.structure.DotExpression" flags="nn" index="2OqwBi">
        <child id="1197027771414" name="operand" index="2Oq$k0" />
        <child id="1197027833540" name="operation" index="2OqNvi" />
      </concept>
      <concept id="1164879751025" name="jetbrains.mps.baseLanguage.structure.TryCatchStatement" flags="nn" index="SfApY">
        <child id="1164879758292" name="body" index="SfCbr" />
        <child id="1164903496223" name="catchClause" index="TEbGg" />
      </concept>
      <concept id="1145552977093" name="jetbrains.mps.baseLanguage.structure.GenericNewExpression" flags="nn" index="2ShNRf">
        <child id="1145553007750" name="creator" index="2ShVmc" />
      </concept>
      <concept id="1164903280175" name="jetbrains.mps.baseLanguage.structure.CatchClause" flags="nn" index="TDmWw">
        <child id="1164903359218" name="catchBody" index="TDEfX" />
        <child id="1164903359217" name="throwable" index="TDEfY" />
      </concept>
      <concept id="1137021947720" name="jetbrains.mps.baseLanguage.structure.ConceptFunction" flags="in" index="2VMwT0">
        <child id="1137022507850" name="body" index="2VODD2" />
      </concept>
      <concept id="1070475926800" name="jetbrains.mps.baseLanguage.structure.StringLiteral" flags="nn" index="Xl_RD">
        <property id="1070475926801" name="value" index="Xl_RC" />
      </concept>
      <concept id="1081236700937" name="jetbrains.mps.baseLanguage.structure.StaticMethodCall" flags="nn" index="2YIFZM">
        <reference id="1144433194310" name="classConcept" index="1Pybhc" />
      </concept>
      <concept id="1070534058343" name="jetbrains.mps.baseLanguage.structure.NullLiteral" flags="nn" index="10Nm6u" />
      <concept id="1070534370425" name="jetbrains.mps.baseLanguage.structure.IntegerType" flags="in" index="10Oyi0" />
      <concept id="1068498886296" name="jetbrains.mps.baseLanguage.structure.VariableReference" flags="nn" index="37vLTw">
        <reference id="1068581517664" name="variableDeclaration" index="3cqZAo" />
      </concept>
      <concept id="1068498886292" name="jetbrains.mps.baseLanguage.structure.ParameterDeclaration" flags="ir" index="37vLTG" />
      <concept id="1068498886294" name="jetbrains.mps.baseLanguage.structure.AssignmentExpression" flags="nn" index="37vLTI" />
      <concept id="4972933694980447171" name="jetbrains.mps.baseLanguage.structure.BaseVariableDeclaration" flags="ng" index="19Szcq">
        <child id="5680397130376446158" name="type" index="1tU5fm" />
      </concept>
      <concept id="1068580123132" name="jetbrains.mps.baseLanguage.structure.BaseMethodDeclaration" flags="ng" index="3clF44">
        <child id="1068580123133" name="returnType" index="3clF45" />
        <child id="1068580123134" name="parameter" index="3clF46" />
        <child id="1068580123135" name="body" index="3clF47" />
      </concept>
      <concept id="1068580123155" name="jetbrains.mps.baseLanguage.structure.ExpressionStatement" flags="nn" index="3clFbF">
        <child id="1068580123156" name="expression" index="3clFbG" />
      </concept>
      <concept id="1068580123159" name="jetbrains.mps.baseLanguage.structure.IfStatement" flags="nn" index="3clFbJ">
        <child id="1068580123160" name="condition" index="3clFbw" />
        <child id="1068580123161" name="ifTrue" index="3clFbx" />
      </concept>
      <concept id="1068580123136" name="jetbrains.mps.baseLanguage.structure.StatementList" flags="sn" stub="5293379017992965193" index="3clFbS">
        <child id="1068581517665" name="statement" index="3cqZAp" />
      </concept>
      <concept id="1068580320020" name="jetbrains.mps.baseLanguage.structure.IntegerConstant" flags="nn" index="3cmrfG">
        <property id="1068580320021" name="value" index="3cmrfH" />
      </concept>
      <concept id="1068581242878" name="jetbrains.mps.baseLanguage.structure.ReturnStatement" flags="nn" index="3cpWs6">
        <child id="1068581517676" name="expression" index="3cqZAk" />
      </concept>
      <concept id="1068581242863" name="jetbrains.mps.baseLanguage.structure.LocalVariableDeclaration" flags="nr" index="3cpWsn" />
      <concept id="1204053956946" name="jetbrains.mps.baseLanguage.structure.IMethodCall" flags="ng" index="1ndlxa">
        <reference id="1068499141037" name="baseMethodDeclaration" index="37wK5l" />
        <child id="1068499141038" name="actualArgument" index="37wK5m" />
      </concept>
      <concept id="1107535904670" name="jetbrains.mps.baseLanguage.structure.ClassifierType" flags="in" index="3uibUv">
        <reference id="1107535924139" name="classifier" index="3uigEE" />
      </concept>
      <concept id="1178549954367" name="jetbrains.mps.baseLanguage.structure.IVisible" flags="ng" index="1B3ioH">
        <child id="1178549979242" name="visibility" index="1B3o_S" />
      </concept>
      <concept id="1146644602865" name="jetbrains.mps.baseLanguage.structure.PublicVisibility" flags="nn" index="3Tm1VV" />
    </language>
    <language id="7866978e-a0f0-4cc7-81bc-4d213d9375e1" name="jetbrains.mps.lang.smodel">
      <concept id="1226359078165" name="jetbrains.mps.lang.smodel.structure.LinkRefExpression" flags="nn" index="28GBK8">
        <reference id="1226359078166" name="conceptDeclaration" index="28GBKb" />
        <reference id="1226359192215" name="linkDeclaration" index="28H3Ia" />
      </concept>
      <concept id="1177026924588" name="jetbrains.mps.lang.smodel.structure.RefConcept_Reference" flags="nn" index="chp4Y">
        <reference id="1177026940964" name="conceptDeclaration" index="cht4Q" />
      </concept>
      <concept id="1180031783296" name="jetbrains.mps.lang.smodel.structure.Concept_IsSubConceptOfOperation" flags="nn" index="2Zo12i">
        <child id="1180031783297" name="conceptArgument" index="2Zo12j" />
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
      <concept id="1172420572800" name="jetbrains.mps.lang.smodel.structure.SConceptType" flags="in" index="3THzug" />
    </language>
    <language id="ceab5195-25ea-4f22-9b92-103b95ca8c0c" name="jetbrains.mps.lang.core">
      <concept id="1169194658468" name="jetbrains.mps.lang.core.structure.INamedConcept" flags="ng" index="TrEIO">
        <property id="1169194664001" name="name" index="TrG5h" />
      </concept>
    </language>
  </registry>
  <node concept="13h7C7" id="44kR2PMraTM">
    <ref role="13h7C2" to="tsp6:44kR2PMr9Me" resolve="HeaderKey" />
    <node concept="13i0hz" id="44kR2PMraWZ" role="13h7CS">
      <property role="TrG5h" value="intValue" />
      <node concept="3Tm1VV" id="44kR2PMraX0" role="1B3o_S" />
      <node concept="10Oyi0" id="44kR2PMrb3a" role="3clF45" />
      <node concept="3clFbS" id="44kR2PMraX2" role="3clF47">
        <node concept="SfApY" id="44kR2PMsb1s" role="3cqZAp">
          <node concept="3clFbS" id="44kR2PMsb1u" role="SfCbr">
            <node concept="3cpWs6" id="44kR2PMsbLd" role="3cqZAp">
              <node concept="2YIFZM" id="44kR2PMrc9o" role="3cqZAk">
                <ref role="37wK5l" to="e2lb:~Integer.parseInt(java.lang.String,int):int" resolve="parseInt" />
                <ref role="1Pybhc" to="e2lb:~Integer" resolve="Integer" />
                <node concept="2OqwBi" id="44kR2PMrhCT" role="37wK5m">
                  <node concept="13iPFW" id="44kR2PMrhAF" role="2Oq$k0" />
                  <node concept="3TrcHB" id="44kR2PMriBA" role="2OqNvi">
                    <ref role="3TsBF5" to="tsp6:44kR2PMr9Mh" resolve="hexValue" />
                  </node>
                </node>
                <node concept="3cmrfG" id="44kR2PMriFd" role="37wK5m">
                  <property role="3cmrfH" value="16" />
                </node>
              </node>
            </node>
          </node>
          <node concept="TDmWw" id="44kR2PMsb1v" role="TEbGg">
            <node concept="3cpWsn" id="44kR2PMsb1x" role="TDEfY">
              <property role="TrG5h" value="e" />
              <node concept="3uibUv" id="44kR2PMsbdv" role="1tU5fm">
                <ref role="3uigEE" to="e2lb:~Exception" resolve="Exception" />
              </node>
            </node>
            <node concept="3clFbS" id="44kR2PMsb1_" role="TDEfX">
              <node concept="3cpWs6" id="44kR2PMsbKQ" role="3cqZAp">
                <node concept="3cmrfG" id="44kR2PMsbs9" role="3cqZAk">
                  <property role="3cmrfH" value="0" />
                </node>
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
    <node concept="13hLZK" id="44kR2PMraWa" role="13h7CW">
      <node concept="3clFbS" id="44kR2PMraWb" role="2VODD2">
        <node concept="3clFbF" id="44kR2PMs0tV" role="3cqZAp">
          <node concept="37vLTI" id="44kR2PMs0Rz" role="3clFbG">
            <node concept="Xl_RD" id="44kR2PMs0RN" role="37vLTx">
              <property role="Xl_RC" value="01" />
            </node>
            <node concept="2OqwBi" id="44kR2PMs0v8" role="37vLTJ">
              <node concept="13iPFW" id="44kR2PMs0tU" role="2Oq$k0" />
              <node concept="3TrcHB" id="44kR2PMs0AF" role="2OqNvi">
                <ref role="3TsBF5" to="tsp6:44kR2PMr9Mh" resolve="hexValue" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="13h7C7" id="44kR2PMs5Y8">
    <ref role="13h7C2" to="tsp6:22nuAqQ$k5k" resolve="IHeaderStruct" />
    <node concept="13hLZK" id="44kR2PMs5Y9" role="13h7CW">
      <node concept="3clFbS" id="44kR2PMs5Ya" role="2VODD2">
        <node concept="3clFbF" id="44kR2PMs5Yc" role="3cqZAp">
          <node concept="37vLTI" id="44kR2PMs78g" role="3clFbG">
            <node concept="2ShNRf" id="44kR2PMs9$8" role="37vLTx">
              <node concept="3zrR0B" id="44kR2PMs8mP" role="2ShVmc">
                <node concept="3Tqbb2" id="44kR2PMs8mQ" role="3zrR0E">
                  <ref role="ehGHo" to="tsp6:44kR2PMr9Me" resolve="HeaderKey" />
                </node>
              </node>
            </node>
            <node concept="2OqwBi" id="44kR2PMs5Zh" role="37vLTJ">
              <node concept="13iPFW" id="44kR2PMs5Yb" role="2Oq$k0" />
              <node concept="3TrEf2" id="44kR2PMs6VB" role="2OqNvi">
                <ref role="3Tt5mk" to="tsp6:44kR2PMrjgm" />
              </node>
            </node>
          </node>
        </node>
      </node>
    </node>
  </node>
  <node concept="13h7C7" id="3zc4oYAt5p6">
    <ref role="13h7C2" to="tsp6:22nuAqQ_Ani" resolve="IStruct" />
    <node concept="13hLZK" id="3zc4oYAt5p7" role="13h7CW">
      <node concept="3clFbS" id="3zc4oYAt5p8" role="2VODD2" />
    </node>
    <node concept="13i0hz" id="3zc4oYAt5p9" role="13h7CS">
      <property role="13i0iv" value="false" />
      <property role="TrG5h" value="getScope" />
      <property role="13i0it" value="false" />
      <ref role="13i0hy" to="tpcu:3fifI_xCJOQ" resolve="getScope" />
      <node concept="3Tm1VV" id="3zc4oYAt5pa" role="1B3o_S" />
      <node concept="3clFbS" id="3zc4oYAt5pj" role="3clF47">
        <node concept="3clFbJ" id="3zc4oYAtcWF" role="3cqZAp">
          <node concept="3clFbS" id="3zc4oYAtcWG" role="3clFbx">
            <node concept="3cpWs6" id="3zc4oYAtku_" role="3cqZAp">
              <node concept="2YIFZM" id="3zc4oYAtkyL" role="3cqZAk">
                <ref role="37wK5l" to="o8zo:379IfaV6Tee" resolve="forNamedElements" />
                <ref role="1Pybhc" to="o8zo:7ipADkTevLm" resolve="SimpleRoleScope" />
                <node concept="13iPFW" id="3zc4oYAtkA5" role="37wK5m" />
                <node concept="28GBK8" id="3zc4oYAtnqi" role="37wK5m">
                  <ref role="28GBKb" to="tsp6:22nuAqQ_Ani" resolve="IStruct" />
                  <ref role="28H3Ia" to="tsp6:22nuAqQ_Ao7" />
                </node>
              </node>
            </node>
          </node>
          <node concept="2OqwBi" id="3zc4oYAtd18" role="3clFbw">
            <node concept="37vLTw" id="3zc4oYAtcXO" role="2Oq$k0">
              <ref role="3cqZAo" node="3zc4oYAt5pk" resolve="kind" />
            </node>
            <node concept="2Zo12i" id="3zc4oYAtdWQ" role="2OqNvi">
              <node concept="chp4Y" id="3zc4oYAtksU" role="2Zo12j">
                <ref role="cht4Q" to="tsp6:22nuAqQy7Az" resolve="StructAttribute" />
              </node>
            </node>
          </node>
        </node>
        <node concept="3cpWs6" id="3zc4oYAtnLD" role="3cqZAp">
          <node concept="10Nm6u" id="3zc4oYAtnR6" role="3cqZAk" />
        </node>
      </node>
      <node concept="37vLTG" id="3zc4oYAt5pk" role="3clF46">
        <property role="TrG5h" value="kind" />
        <node concept="3THzug" id="3zc4oYAt5pl" role="1tU5fm" />
      </node>
      <node concept="37vLTG" id="3zc4oYAt5pm" role="3clF46">
        <property role="TrG5h" value="child" />
        <node concept="3Tqbb2" id="3zc4oYAt5pn" role="1tU5fm" />
      </node>
      <node concept="3uibUv" id="3zc4oYAt5po" role="3clF45">
        <ref role="3uigEE" to="o8zo:3fifI_xCtN$" resolve="Scope" />
      </node>
    </node>
  </node>
  <node concept="13h7C7" id="EUEXKTjGxC">
    <ref role="13h7C2" to="tsp6:EUEXKTjFZU" resolve="StructDocParameter" />
    <node concept="13hLZK" id="EUEXKTjG$0" role="13h7CW">
      <node concept="3clFbS" id="EUEXKTjG$1" role="2VODD2" />
    </node>
  </node>
</model>

