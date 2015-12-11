<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.language.structure)">
  <persistence version="9" />
  <languages>
    <use id="c72da2b9-7cce-4447-8389-f407dc1158b7" name="jetbrains.mps.lang.structure" version="-1" />
    <devkit ref="fbc25dd2-5da4-483a-8b19-70928e1b62d7(jetbrains.mps.devkit.general-purpose)" />
  </languages>
  <imports>
    <import index="tpck" ref="r:00000000-0000-4000-0000-011c89590288(jetbrains.mps.lang.core.structure)" implicit="true" />
  </imports>
  <registry>
    <language id="c72da2b9-7cce-4447-8389-f407dc1158b7" name="jetbrains.mps.lang.structure">
      <concept id="1082978164219" name="jetbrains.mps.lang.structure.structure.EnumerationDataTypeDeclaration" flags="ng" index="AxPO7">
        <reference id="1083171729157" name="memberDataType" index="M4eZT" />
        <child id="1083172003582" name="member" index="M5hS2" />
      </concept>
      <concept id="1082978499127" name="jetbrains.mps.lang.structure.structure.ConstrainedDataTypeDeclaration" flags="ng" index="Az7Fb">
        <property id="1083066089218" name="constraint" index="FLfZY" />
      </concept>
      <concept id="1083171877298" name="jetbrains.mps.lang.structure.structure.EnumerationMemberDeclaration" flags="ig" index="M4N5e">
        <property id="1083923523172" name="externalValue" index="1uS6qo" />
        <property id="1083923523171" name="internalValue" index="1uS6qv" />
      </concept>
      <concept id="1169125787135" name="jetbrains.mps.lang.structure.structure.AbstractConceptDeclaration" flags="ig" index="PkWjJ">
        <property id="4628067390765956807" name="final" index="R5$K2" />
        <property id="4628067390765956802" name="abstract" index="R5$K7" />
        <property id="5092175715804935370" name="conceptAlias" index="34LRSv" />
        <child id="1071489727083" name="linkDeclaration" index="1TKVEi" />
        <child id="1071489727084" name="propertyDeclaration" index="1TKVEl" />
      </concept>
      <concept id="1169125989551" name="jetbrains.mps.lang.structure.structure.InterfaceConceptDeclaration" flags="ig" index="PlHQZ">
        <child id="1169127546356" name="extends" index="PrDN$" />
      </concept>
      <concept id="1169127622168" name="jetbrains.mps.lang.structure.structure.InterfaceConceptReference" flags="ig" index="PrWs8">
        <reference id="1169127628841" name="intfc" index="PrY4T" />
      </concept>
      <concept id="1071489090640" name="jetbrains.mps.lang.structure.structure.ConceptDeclaration" flags="ig" index="1TIwiD">
        <property id="1096454100552" name="rootable" index="19KtqR" />
        <reference id="1071489389519" name="extends" index="1TJDcQ" />
        <child id="1169129564478" name="implements" index="PzmwI" />
      </concept>
      <concept id="1071489288299" name="jetbrains.mps.lang.structure.structure.PropertyDeclaration" flags="ig" index="1TJgyi">
        <reference id="1082985295845" name="dataType" index="AX2Wp" />
      </concept>
      <concept id="1071489288298" name="jetbrains.mps.lang.structure.structure.LinkDeclaration" flags="ig" index="1TJgyj">
        <property id="1071599776563" name="role" index="20kJfa" />
        <property id="1071599893252" name="sourceCardinality" index="20lbJX" />
        <property id="1071599937831" name="metaClass" index="20lmBu" />
        <reference id="1071599976176" name="target" index="20lvS9" />
      </concept>
    </language>
    <language id="ceab5195-25ea-4f22-9b92-103b95ca8c0c" name="jetbrains.mps.lang.core">
      <concept id="1169194658468" name="jetbrains.mps.lang.core.structure.INamedConcept" flags="ng" index="TrEIO">
        <property id="1169194664001" name="name" index="TrG5h" />
      </concept>
    </language>
  </registry>
  <node concept="1TIwiD" id="22nuAqQwwzh">
    <property role="TrG5h" value="Struct" />
    <property role="34LRSv" value="struct" />
    <ref role="1TJDcQ" node="22nuAqQy50h" resolve="ApiNamedDef" />
    <node concept="1TJgyj" id="4ASKzdDECPc" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="interface" />
      <ref role="20lvS9" node="4ASKzdDBfFg" resolve="Trait" />
    </node>
    <node concept="1TJgyi" id="4ASKzdDEhY9" role="1TKVEl">
      <property role="TrG5h" value="hasInterface" />
      <ref role="AX2Wp" to="tpck:fKAQMTB" resolve="boolean" />
    </node>
    <node concept="1TJgyi" id="2tyCW$U4uvE" role="1TKVEl">
      <property role="TrG5h" value="isExpandable" />
      <ref role="AX2Wp" to="tpck:fKAQMTB" resolve="boolean" />
    </node>
    <node concept="PrWs8" id="22nuAqQ_AH3" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ_Ani" resolve="IStruct" />
    </node>
    <node concept="PrWs8" id="44kR2PMtgpM" role="PzmwI">
      <ref role="PrY4T" node="44kR2PMtavz" resolve="IEntity" />
    </node>
    <node concept="1TJgyj" id="EUEXKTjMyj" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="docs" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="EUEXKTjp2A" resolve="StructDoc" />
    </node>
    <node concept="1TJgyj" id="55bmeIQftnP" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="header" />
      <property role="20lbJX" value="0..1" />
      <ref role="20lvS9" node="44kR2PMr9Me" resolve="HeaderKey" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQww$c">
    <property role="TrG5h" value="Int32" />
    <property role="34LRSv" value="int32" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
  </node>
  <node concept="1TIwiD" id="22nuAqQww$g">
    <property role="TrG5h" value="Int64" />
    <property role="34LRSv" value="int64" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
  </node>
  <node concept="1TIwiD" id="22nuAqQww$l">
    <property role="TrG5h" value="Boolean" />
    <property role="34LRSv" value="bool" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
  </node>
  <node concept="1TIwiD" id="22nuAqQww$r">
    <property role="TrG5h" value="String" />
    <property role="34LRSv" value="string" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
  </node>
  <node concept="1TIwiD" id="22nuAqQww$5">
    <property role="TrG5h" value="SerializableType" />
    <property role="R5$K7" value="true" />
    <property role="R5$K2" value="false" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
  </node>
  <node concept="1TIwiD" id="22nuAqQwwWv">
    <property role="TrG5h" value="Optional" />
    <property role="34LRSv" value="opt" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
    <node concept="1TJgyj" id="GBscvB$Myn" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="type" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQww$5" resolve="SerializableType" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQwwWM">
    <property role="TrG5h" value="List" />
    <property role="34LRSv" value="list" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
    <node concept="1TJgyj" id="GBscvBAyxu" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="type" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQww$5" resolve="SerializableType" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQwwXb">
    <property role="19KtqR" value="true" />
    <property role="TrG5h" value="ApiDescription" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyi" id="55bmeIQ71Qz" role="1TKVEl">
      <property role="TrG5h" value="javaPackage" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
    <node concept="1TJgyi" id="gbd4oSj9sA" role="1TKVEl">
      <property role="TrG5h" value="scalaPackage" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
    <node concept="1TJgyi" id="2tyCW$TXG2O" role="1TKVEl">
      <property role="TrG5h" value="version" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
    <node concept="1TJgyj" id="55bmeIQ6Gyz" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="aliases" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="4ASKzdDzbll" resolve="ApiAlias" />
    </node>
    <node concept="PrWs8" id="22nuAqQwxDI" role="PzmwI">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
    <node concept="1TJgyj" id="22nuAqQwy4V" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="sections" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="22nuAqQwx6i" resolve="ApiSection" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQwx5Q">
    <property role="TrG5h" value="StructType" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
    <node concept="1TJgyj" id="22nuAqQwx64" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="struct" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQwwzh" resolve="Struct" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQwx6i">
    <property role="TrG5h" value="ApiSection" />
    <property role="34LRSv" value="section" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyi" id="3m8vlV8mFhx" role="1TKVEl">
      <property role="TrG5h" value="package" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
    <node concept="1TJgyj" id="22nuAqQwx6X" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="definitions" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="22nuAqQwx6w" resolve="ApiDef" />
    </node>
    <node concept="1TJgyj" id="2uPas5ecrWC" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="docs" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="2uPas5ecrl0" resolve="SectionDoc" />
    </node>
    <node concept="PrWs8" id="22nuAqQwx7n" role="PzmwI">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQwx6w">
    <property role="TrG5h" value="ApiDef" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
  </node>
  <node concept="1TIwiD" id="22nuAqQxZxK">
    <property role="TrG5h" value="Enum" />
    <property role="34LRSv" value="enum" />
    <ref role="1TJDcQ" node="22nuAqQy50h" resolve="ApiNamedDef" />
    <node concept="1TJgyj" id="22nuAqQylZD" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="attributes" />
      <property role="20lbJX" value="1..n" />
      <ref role="20lvS9" node="22nuAqQylDd" resolve="EnumAttribute" />
    </node>
    <node concept="PrWs8" id="44kR2PMtgqY" role="PzmwI">
      <ref role="PrY4T" node="44kR2PMtavz" resolve="IEntity" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQy50h">
    <property role="TrG5h" value="ApiNamedDef" />
    <property role="R5$K7" value="true" />
    <property role="R5$K2" value="false" />
    <ref role="1TJDcQ" node="22nuAqQwx6w" resolve="ApiDef" />
    <node concept="PrWs8" id="22nuAqQy50q" role="PzmwI">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQy7Az">
    <property role="TrG5h" value="StructAttribute" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyi" id="22nuAqQyujl" role="1TKVEl">
      <property role="TrG5h" value="id" />
      <ref role="AX2Wp" to="tpck:fKAQMTA" resolve="integer" />
    </node>
    <node concept="1TJgyi" id="2tyCW$U0knp" role="1TKVEl">
      <property role="TrG5h" value="isDeprecated" />
      <ref role="AX2Wp" to="tpck:fKAQMTB" resolve="boolean" />
    </node>
    <node concept="PrWs8" id="22nuAqQyuiq" role="PzmwI">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
    <node concept="1TJgyj" id="22nuAqQyuiR" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="type" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQww$5" resolve="SerializableType" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQylDd">
    <property role="TrG5h" value="EnumAttribute" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="PrWs8" id="22nuAqQylDm" role="PzmwI">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
    <node concept="1TJgyi" id="22nuAqQylDK" role="1TKVEl">
      <property role="TrG5h" value="id" />
      <ref role="AX2Wp" to="tpck:fKAQMTA" resolve="integer" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQzP$r">
    <property role="TrG5h" value="Response" />
    <property role="34LRSv" value="response" />
    <ref role="1TJDcQ" node="22nuAqQy50h" resolve="ApiNamedDef" />
    <node concept="PrWs8" id="44kR2PMtuLM" role="PzmwI">
      <ref role="PrY4T" node="44kR2PMtuJZ" resolve="IRpcNamed" />
    </node>
    <node concept="PrWs8" id="44kR2PMtaH3" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ_Ani" resolve="IStruct" />
    </node>
    <node concept="1TJgyj" id="EUEXKTjNFy" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="docs" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="EUEXKTjp2A" resolve="StructDoc" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQzQOK">
    <property role="TrG5h" value="Bytes" />
    <property role="34LRSv" value="bytes" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
  </node>
  <node concept="1TIwiD" id="22nuAqQzSiW">
    <property role="TrG5h" value="Rpc" />
    <property role="34LRSv" value="rpc" />
    <ref role="1TJDcQ" node="22nuAqQy50h" resolve="ApiNamedDef" />
    <node concept="1TJgyj" id="2EAJ7H6eW7X" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="docs" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="EUEXKTjp2A" resolve="StructDoc" />
    </node>
    <node concept="1TJgyj" id="22nuAqQzTAW" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="response" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQ$0I1" resolve="ResponseRef" />
    </node>
    <node concept="1TJgyj" id="3zc4oYAr8mD" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="throws" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="3zc4oYAr5ID" resolve="MethodThrows" />
    </node>
    <node concept="PrWs8" id="44kR2PMtuMT" role="PzmwI">
      <ref role="PrY4T" node="44kR2PMtuJZ" resolve="IRpcNamed" />
    </node>
    <node concept="PrWs8" id="44kR2PMtaxc" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ_Ani" resolve="IStruct" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQ$0I1">
    <property role="TrG5h" value="ResponseRef" />
    <property role="R5$K7" value="true" />
    <property role="R5$K2" value="false" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
  </node>
  <node concept="1TIwiD" id="22nuAqQ$0Jq">
    <property role="TrG5h" value="ResponseRefAnonymous" />
    <property role="34LRSv" value="tuple" />
    <ref role="1TJDcQ" node="22nuAqQ$0I1" resolve="ResponseRef" />
    <node concept="1TJgyj" id="4zDDY4ESNfc" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="docs" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="EUEXKTjp2A" resolve="StructDoc" />
    </node>
    <node concept="PrWs8" id="22nuAqQ$kkD" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ$k66" resolve="IRpcObject" />
    </node>
    <node concept="PrWs8" id="22nuAqQ_ATy" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ_Ani" resolve="IStruct" />
    </node>
  </node>
  <node concept="1TIwiD" id="22nuAqQ$0KB">
    <property role="TrG5h" value="ResponseRefValue" />
    <ref role="1TJDcQ" node="22nuAqQ$0I1" resolve="ResponseRef" />
    <node concept="1TJgyj" id="22nuAqQ$JwN" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="response" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQzP$r" resolve="Response" />
    </node>
  </node>
  <node concept="PlHQZ" id="22nuAqQ$k5k">
    <property role="TrG5h" value="IHeaderStruct" />
    <node concept="1TJgyj" id="44kR2PMrjgm" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="header" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="44kR2PMr9Me" resolve="HeaderKey" />
    </node>
  </node>
  <node concept="PlHQZ" id="22nuAqQ$k66">
    <property role="TrG5h" value="IRpcObject" />
    <node concept="PrWs8" id="22nuAqQ$k6f" role="PrDN$">
      <ref role="PrY4T" node="22nuAqQ$k5k" resolve="IHeaderStruct" />
    </node>
  </node>
  <node concept="PlHQZ" id="22nuAqQ$k6Y">
    <property role="TrG5h" value="IUpdateObject" />
    <node concept="PrWs8" id="22nuAqQ$k6Z" role="PrDN$">
      <ref role="PrY4T" node="22nuAqQ$k5k" resolve="IHeaderStruct" />
    </node>
    <node concept="PrWs8" id="44kR2PMtAj$" role="PrDN$">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
  </node>
  <node concept="PlHQZ" id="22nuAqQ_Ani">
    <property role="TrG5h" value="IStruct" />
    <node concept="1TJgyj" id="22nuAqQ_Ao7" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="attributes" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="22nuAqQy7Az" resolve="StructAttribute" />
    </node>
    <node concept="PrWs8" id="3zc4oYAt5nA" role="PrDN$">
      <ref role="PrY4T" to="tpck:3fifI_xCcJN" resolve="ScopeProvider" />
    </node>
  </node>
  <node concept="1TIwiD" id="44kR2PMqJmz">
    <property role="TrG5h" value="ApiEmptyDef" />
    <ref role="1TJDcQ" node="22nuAqQwx6w" resolve="ApiDef" />
  </node>
  <node concept="1TIwiD" id="44kR2PMqLnf">
    <property role="TrG5h" value="ApiComment" />
    <property role="34LRSv" value="//" />
    <ref role="1TJDcQ" node="22nuAqQwx6w" resolve="ApiDef" />
    <node concept="1TJgyi" id="44kR2PMqLFw" role="1TKVEl">
      <property role="TrG5h" value="text" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
  </node>
  <node concept="Az7Fb" id="44kR2PMr9Lk">
    <property role="TrG5h" value="hexByte" />
    <property role="FLfZY" value="([0-9a-fA-F])?([0-9a-fA-F])?[0-9a-fA-F][0-9a-fA-F]" />
  </node>
  <node concept="1TIwiD" id="44kR2PMr9Me">
    <property role="TrG5h" value="HeaderKey" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyi" id="44kR2PMr9Mh" role="1TKVEl">
      <property role="TrG5h" value="hexValue" />
      <ref role="AX2Wp" node="44kR2PMr9Lk" resolve="hexByte" />
    </node>
  </node>
  <node concept="1TIwiD" id="44kR2PMsE9T">
    <property role="TrG5h" value="Update" />
    <property role="34LRSv" value="update" />
    <ref role="1TJDcQ" node="22nuAqQy50h" resolve="ApiNamedDef" />
    <node concept="PrWs8" id="44kR2PMsEaY" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ$k6Y" resolve="IUpdateObject" />
    </node>
    <node concept="PrWs8" id="44kR2PMtaY4" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ_Ani" resolve="IStruct" />
    </node>
    <node concept="1TJgyj" id="EUEXKTjNau" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="docs" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="EUEXKTjp2A" resolve="StructDoc" />
    </node>
  </node>
  <node concept="PlHQZ" id="44kR2PMtavz">
    <property role="TrG5h" value="IEntity" />
    <node concept="PrWs8" id="44kR2PMtavG" role="PrDN$">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
  </node>
  <node concept="PlHQZ" id="44kR2PMtuJZ">
    <property role="TrG5h" value="IRpcNamed" />
    <node concept="PrWs8" id="44kR2PMtuK8" role="PrDN$">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
    <node concept="PrWs8" id="44kR2PMtuKF" role="PrDN$">
      <ref role="PrY4T" node="22nuAqQ$k66" resolve="IRpcObject" />
    </node>
  </node>
  <node concept="1TIwiD" id="GBscvBAzfr">
    <property role="TrG5h" value="EnumType" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
    <node concept="1TJgyj" id="GBscvBAzhj" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="struct" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQxZxK" resolve="Enum" />
    </node>
  </node>
  <node concept="1TIwiD" id="GBscvBBbt0">
    <property role="TrG5h" value="Double" />
    <property role="34LRSv" value="double" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
  </node>
  <node concept="1TIwiD" id="GBscvBBBz$">
    <property role="TrG5h" value="UpdateBox" />
    <property role="34LRSv" value="update box" />
    <ref role="1TJDcQ" node="22nuAqQy50h" resolve="ApiNamedDef" />
    <node concept="PrWs8" id="GBscvBBC5v" role="PzmwI">
      <ref role="PrY4T" node="44kR2PMtuJZ" resolve="IRpcNamed" />
    </node>
    <node concept="PrWs8" id="GBscvBBBzA" role="PzmwI">
      <ref role="PrY4T" node="22nuAqQ_Ani" resolve="IStruct" />
    </node>
    <node concept="1TJgyj" id="EUEXKTjNqY" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="docs" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="EUEXKTjp2A" resolve="StructDoc" />
    </node>
  </node>
  <node concept="1TIwiD" id="2uPas5ecrl0">
    <property role="TrG5h" value="SectionDoc" />
    <property role="34LRSv" value="#" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyi" id="2uPas5ecrn6" role="1TKVEl">
      <property role="TrG5h" value="text" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
  </node>
  <node concept="1TIwiD" id="3zc4oYAr5ID">
    <property role="TrG5h" value="MethodThrows" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyi" id="3zc4oYAr5JZ" role="1TKVEl">
      <property role="TrG5h" value="errorCode" />
      <ref role="AX2Wp" to="tpck:fKAQMTA" resolve="integer" />
    </node>
    <node concept="1TJgyi" id="3zc4oYAr5K1" role="1TKVEl">
      <property role="TrG5h" value="errorTag" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
    <node concept="1TJgyi" id="3zc4oYArjYY" role="1TKVEl">
      <property role="TrG5h" value="description" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
  </node>
  <node concept="1TIwiD" id="EUEXKTjp2A">
    <property role="TrG5h" value="StructDoc" />
    <property role="R5$K7" value="true" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
  </node>
  <node concept="1TIwiD" id="EUEXKTj$oO">
    <property role="TrG5h" value="StructDocComment" />
    <property role="34LRSv" value="#" />
    <ref role="1TJDcQ" node="EUEXKTjp2A" resolve="StructDoc" />
    <node concept="1TJgyi" id="EUEXKTj$qj" role="1TKVEl">
      <property role="TrG5h" value="content" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
  </node>
  <node concept="1TIwiD" id="EUEXKTjFZU">
    <property role="TrG5h" value="StructDocParameter" />
    <property role="34LRSv" value="param" />
    <ref role="1TJDcQ" node="EUEXKTjp2A" resolve="StructDoc" />
    <node concept="1TJgyj" id="EUEXKTjGv3" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="paramter" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQy7Az" resolve="StructAttribute" />
    </node>
    <node concept="1TJgyi" id="EUEXKTjGou" role="1TKVEl">
      <property role="TrG5h" value="description" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
    <node concept="1TJgyi" id="2EAJ7H6hOv1" role="1TKVEl">
      <property role="TrG5h" value="category" />
      <ref role="AX2Wp" node="2EAJ7H6hOl6" resolve="ParameterCategory" />
    </node>
  </node>
  <node concept="1TIwiD" id="4ASKzdDzbll">
    <property role="TrG5h" value="ApiAlias" />
    <property role="34LRSv" value="alias" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyj" id="4ASKzdDzpMJ" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="sourceType" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQww$5" resolve="SerializableType" />
    </node>
    <node concept="PrWs8" id="4ASKzdDAi9F" role="PzmwI">
      <ref role="PrY4T" node="44kR2PMtavz" resolve="IEntity" />
    </node>
    <node concept="PrWs8" id="55bmeIQ6Gwv" role="PzmwI">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
  </node>
  <node concept="1TIwiD" id="4ASKzdD_cYA">
    <property role="TrG5h" value="AliasType" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
    <node concept="1TJgyj" id="4ASKzdD_d0q" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="alias" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="4ASKzdDzbll" resolve="ApiAlias" />
    </node>
  </node>
  <node concept="1TIwiD" id="4ASKzdDBfFg">
    <property role="TrG5h" value="Trait" />
    <property role="34LRSv" value="trait" />
    <ref role="1TJDcQ" node="22nuAqQy50h" resolve="ApiNamedDef" />
    <node concept="1TJgyi" id="5NX0N0RPBrE" role="1TKVEl">
      <property role="TrG5h" value="isContainer" />
      <ref role="AX2Wp" to="tpck:fKAQMTB" resolve="boolean" />
    </node>
    <node concept="1TJgyj" id="4ASKzdDBz_8" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="interfaces" />
      <property role="20lbJX" value="0..n" />
      <ref role="20lvS9" node="4ASKzdDByZj" resolve="TraitAttribute" />
    </node>
  </node>
  <node concept="1TIwiD" id="4ASKzdDByZj">
    <property role="TrG5h" value="TraitAttribute" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyj" id="4ASKzdDBz16" role="1TKVEi">
      <property role="20lmBu" value="aggregation" />
      <property role="20kJfa" value="type" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="22nuAqQww$5" resolve="SerializableType" />
    </node>
    <node concept="PrWs8" id="4ASKzdDBz30" role="PzmwI">
      <ref role="PrY4T" to="tpck:h0TrEE$" resolve="INamedConcept" />
    </node>
  </node>
  <node concept="1TIwiD" id="4ASKzdDDeXa">
    <property role="TrG5h" value="InterfaceRef" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyj" id="4ASKzdDDf5b" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="interface" />
      <ref role="20lvS9" node="4ASKzdDBfFg" resolve="Trait" />
    </node>
  </node>
  <node concept="1TIwiD" id="55bmeIQ7Zk$">
    <property role="TrG5h" value="TraitType" />
    <ref role="1TJDcQ" node="22nuAqQww$5" resolve="SerializableType" />
    <node concept="1TJgyj" id="55bmeIQ94H8" role="1TKVEi">
      <property role="20lmBu" value="reference" />
      <property role="20kJfa" value="trait" />
      <property role="20lbJX" value="1" />
      <ref role="20lvS9" node="4ASKzdDBfFg" resolve="Trait" />
    </node>
  </node>
  <node concept="AxPO7" id="2EAJ7H6hOl6">
    <property role="TrG5h" value="ParameterCategory" />
    <ref role="M4eZT" to="tpck:fKAOsGN" resolve="string" />
    <node concept="M4N5e" id="2EAJ7H6hOl7" role="M5hS2">
      <property role="1uS6qv" value="hidden" />
      <property role="1uS6qo" value="hidden" />
    </node>
    <node concept="M4N5e" id="2EAJ7H6hOqO" role="M5hS2">
      <property role="1uS6qv" value="full" />
      <property role="1uS6qo" value="full" />
    </node>
    <node concept="M4N5e" id="2EAJ7H6hOqR" role="M5hS2">
      <property role="1uS6qv" value="compact" />
      <property role="1uS6qo" value="compact" />
    </node>
    <node concept="M4N5e" id="2EAJ7H6hOqV" role="M5hS2">
      <property role="1uS6qv" value="danger" />
      <property role="1uS6qo" value="danger" />
    </node>
  </node>
  <node concept="1TIwiD" id="2tyCW$U15Qi">
    <property role="TrG5h" value="IntroducedIn" />
    <ref role="1TJDcQ" to="tpck:gw2VY9q" resolve="BaseConcept" />
    <node concept="1TJgyi" id="2tyCW$U15Sk" role="1TKVEl">
      <property role="TrG5h" value="varsion" />
      <ref role="AX2Wp" to="tpck:fKAOsGN" resolve="string" />
    </node>
  </node>
</model>

