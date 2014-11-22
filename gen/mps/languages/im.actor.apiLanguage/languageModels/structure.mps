<?xml version="1.0" encoding="UTF-8"?>
<model modelUID="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.apiLanguage.structure)" version="10">
  <persistence version="8" />
  <language namespace="c72da2b9-7cce-4447-8389-f407dc1158b7(jetbrains.mps.lang.structure)" />
  <devkit namespace="fbc25dd2-5da4-483a-8b19-70928e1b62d7(jetbrains.mps.devkit.general-purpose)" />
  <import index="tpce" modelUID="r:00000000-0000-4000-0000-011c89590292(jetbrains.mps.lang.structure.structure)" version="0" implicit="yes" />
  <import index="tpck" modelUID="r:00000000-0000-4000-0000-011c89590288(jetbrains.mps.lang.core.structure)" version="0" implicit="yes" />
  <import index="tsp6" modelUID="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.apiLanguage.structure)" version="10" implicit="yes" />
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264231121" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Struct" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="struct" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264642577" resolveInfo="ApiNamedDef" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5312209286555405644" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="interface" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="5312209286554516176" resolveInfo="Trait" />
    </node>
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="5312209286555312009" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="hasInterface" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983657063" resolveInfo="boolean" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312265567043" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265565650" resolveInfo="IStruct" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751439986" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="4689615199751415779" resolveInfo="IEntity" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="773119248390105235" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="docs" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="773119248390000806" resolveInfo="StructDoc" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5857873509723526645" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="header" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="4689615199750888590" resolveInfo="HeaderKey" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264231180" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Int32" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="int32" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264231184" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Int64" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="int64" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264231189" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Boolean" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="bool" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264231195" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="String" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="string" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264231173" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="SerializableType" />
    <property name="abstract" nameId="tpce.4628067390765956802" value="true" />
    <property name="final" nameId="tpce.4628067390765956807" value="false" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264232735" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Optional" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="opt" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="803735062394906775" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="type" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264232754" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="List" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="list" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="803735062395365470" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="type" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264232779" nodeInfo="ig">
    <property name="rootable" nameId="tpce.1096454100552" value="true" />
    <property name="name" nameId="tpck.1169194664001" value="ApiDescription" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="5857873509721229654" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="objcPrefix" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="5857873509721316771" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="javaPackage" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5857873509721229475" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="aliases" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="5312209286553449813" resolveInfo="ApiAlias" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312264235630" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312264237371" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="sections" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264233362" resolveInfo="ApiSection" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264233334" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="StructType" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312264233348" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="struct" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264231121" resolveInfo="Struct" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264233362" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ApiSection" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="section" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="3857470926884615265" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="package" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312264233405" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="definitions" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264233376" resolveInfo="ApiDef" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2861239048481128232" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="docs" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2861239048481125696" resolveInfo="SectionDoc" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312264233431" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264233376" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ApiDef" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264620144" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Enum" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="enum" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264642577" resolveInfo="ApiNamedDef" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312264712169" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="attributes" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264710733" resolveInfo="EnumAttribute" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751440062" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="4689615199751415779" resolveInfo="IEntity" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264642577" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ApiNamedDef" />
    <property name="abstract" nameId="tpce.4628067390765956802" value="true" />
    <property name="final" nameId="tpce.4628067390765956807" value="false" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264233376" resolveInfo="ApiDef" />
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312264642586" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264653219" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="StructAttribute" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="2348480312264746197" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="id" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983657062" resolveInfo="integer" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312264746138" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312264746167" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="type" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312264710733" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="EnumAttribute" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312264710742" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="2348480312264710768" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="id" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983657062" resolveInfo="integer" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312265103643" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Response" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="response" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264642577" resolveInfo="ApiNamedDef" />
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751498866" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="4689615199751498751" resolveInfo="IRpcNamed" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751416643" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265565650" resolveInfo="IStruct" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="773119248390109922" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="docs" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="773119248390000806" resolveInfo="StructDoc" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312265108784" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Bytes" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="bytes" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312265114812" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Rpc" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="rpc" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264642577" resolveInfo="ApiNamedDef" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="4092665470044220438" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="docs" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="4092665470043358610" resolveInfo="RpcDoc" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312265120188" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="response" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312265149313" resolveInfo="ResponseRef" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="4092665470043063721" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="throws" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="4092665470043052969" resolveInfo="MethodThrows" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751498937" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="4689615199751498751" resolveInfo="IRpcNamed" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751415884" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265565650" resolveInfo="IStruct" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312265149313" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ResponseRef" />
    <property name="abstract" nameId="tpce.4628067390765956802" value="true" />
    <property name="final" nameId="tpce.4628067390765956807" value="false" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312265149402" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ResponseRefAnonymous" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="tuple" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312265149313" resolveInfo="ResponseRef" />
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312265229609" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265228678" resolveInfo="IRpcObject" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312265567842" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265565650" resolveInfo="IStruct" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2348480312265149479" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ResponseRefValue" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312265149313" resolveInfo="ResponseRef" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312265340979" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="response" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312265103643" resolveInfo="Response" />
    </node>
  </root>
  <root type="tpce.InterfaceConceptDeclaration" typeId="tpce.1169125989551" id="2348480312265228628" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="IHeaderStruct" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="4689615199750927382" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="header" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="4689615199750888590" resolveInfo="HeaderKey" />
    </node>
  </root>
  <root type="tpce.InterfaceConceptDeclaration" typeId="tpce.1169125989551" id="2348480312265228678" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="IRpcObject" />
    <node role="extends" roleId="tpce.1169127546356" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312265228687" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265228628" resolveInfo="IHeaderStruct" />
    </node>
  </root>
  <root type="tpce.InterfaceConceptDeclaration" typeId="tpce.1169125989551" id="2348480312265228734" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="IUpdateObject" />
    <node role="extends" roleId="tpce.1169127546356" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="2348480312265228735" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265228628" resolveInfo="IHeaderStruct" />
    </node>
    <node role="extends" roleId="tpce.1169127546356" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751529700" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
  </root>
  <root type="tpce.InterfaceConceptDeclaration" typeId="tpce.1169125989551" id="2348480312265565650" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="IStruct" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="2348480312265565703" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="attributes" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264653219" resolveInfo="StructAttribute" />
    </node>
    <node role="extends" roleId="tpce.1169127546356" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4092665470043575782" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.3734116213129792499" resolveInfo="ScopeProvider" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="4689615199750780323" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ApiEmptyDef" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264233376" resolveInfo="ApiDef" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="4689615199750788559" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ApiComment" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="//" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264233376" resolveInfo="ApiDef" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="4689615199750789856" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="text" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
  </root>
  <root type="tpce.ConstrainedDataTypeDeclaration" typeId="tpce.1082978499127" id="4689615199750888532" nodeInfo="ng">
    <property name="name" nameId="tpck.1169194664001" value="hexByte" />
    <property name="constraint" nameId="tpce.1083066089218" value="[0-9a-fA-F][0-9a-fA-F]" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="4689615199750888590" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="HeaderKey" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="4689615199750888593" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="hexValue" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="4689615199750888532" resolveInfo="hexByte" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="4689615199751283321" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Update" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="update" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264642577" resolveInfo="ApiNamedDef" />
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751283390" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265228734" resolveInfo="IUpdateObject" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751417732" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265565650" resolveInfo="IStruct" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="773119248390107806" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="docs" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="773119248390000806" resolveInfo="StructDoc" />
    </node>
  </root>
  <root type="tpce.InterfaceConceptDeclaration" typeId="tpce.1169125989551" id="4689615199751415779" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="IEntity" />
    <node role="extends" roleId="tpce.1169127546356" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751415788" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
  </root>
  <root type="tpce.InterfaceConceptDeclaration" typeId="tpce.1169125989551" id="4689615199751498751" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="IRpcNamed" />
    <node role="extends" roleId="tpce.1169127546356" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751498760" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
    <node role="extends" roleId="tpce.1169127546356" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="4689615199751498795" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265228678" resolveInfo="IRpcObject" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="803735062395368411" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="EnumType" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="803735062395368531" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="struct" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264620144" resolveInfo="Enum" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="803735062395533120" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Double" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="double" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="803735062395648228" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="UpdateBox" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="update box" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264642577" resolveInfo="ApiNamedDef" />
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="803735062395650399" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="4689615199751498751" resolveInfo="IRpcNamed" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="803735062395648230" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="2348480312265565650" resolveInfo="IStruct" />
    </node>
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="773119248390108862" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="docs" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="773119248390000806" resolveInfo="StructDoc" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2861239048480449583" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="RpcDocComment" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="#" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="4092665470043358610" resolveInfo="RpcDoc" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="2861239048480459664" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="content" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="2861239048481125696" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="SectionDoc" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="#" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="2861239048481125830" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="text" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="4092665470043052969" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="MethodThrows" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="4092665470043053055" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="errorCode" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983657062" resolveInfo="integer" />
    </node>
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="4092665470043053057" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="errorTag" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="4092665470043111358" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="description" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="4092665470043293715" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="RpcDocParameter" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="param" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="4092665470043358610" resolveInfo="RpcDoc" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="4092665470043358846" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="paramter" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264653219" resolveInfo="StructAttribute" />
    </node>
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="4092665470043359042" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="description" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="4092665470043358610" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="RpcDoc" />
    <property name="abstract" nameId="tpce.4628067390765956802" value="true" />
    <property name="final" nameId="tpce.4628067390765956807" value="false" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="773119248390000806" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="StructDoc" />
    <property name="abstract" nameId="tpce.4628067390765956802" value="true" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="773119248390047284" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="StructDocComment" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="#" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="773119248390000806" resolveInfo="StructDoc" />
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="773119248390047379" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="content" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="773119248390078458" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="StructDocParameter" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="param" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="773119248390000806" resolveInfo="StructDoc" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="773119248390080451" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="paramter" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264653219" resolveInfo="StructAttribute" />
    </node>
    <node role="propertyDeclaration" roleId="tpce.1071489727084" type="tpce.PropertyDeclaration" typeId="tpce.1071489288299" id="773119248390080030" nodeInfo="ig">
      <property name="name" nameId="tpck.1169194664001" value="description" />
      <link role="dataType" roleId="tpce.1082985295845" targetNodeId="tpck.1082983041843" resolveInfo="string" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="5312209286553449813" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="ApiAlias" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="alias" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5312209286553509039" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="sourceType" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="5312209286554264171" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="4689615199751415779" resolveInfo="IEntity" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="5857873509721229343" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="5312209286553980838" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="AliasType" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5312209286553980954" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="alias" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="5312209286553449813" resolveInfo="ApiAlias" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="5312209286554516176" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="Trait" />
    <property name="conceptAlias" nameId="tpce.5092175715804935370" value="trait" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264642577" resolveInfo="ApiNamedDef" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5312209286554597704" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="interfaces" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="0..n" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="5312209286554595283" resolveInfo="TraitAttribute" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="5312209286554595283" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="TraitAttribute" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5312209286554595398" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="aggregation" />
      <property name="role" nameId="tpce.1071599776563" value="type" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    </node>
    <node role="implements" roleId="tpce.1169129564478" type="tpce.InterfaceConceptReference" typeId="tpce.1169127622168" id="5312209286554595520" nodeInfo="ig">
      <link role="intfc" roleId="tpce.1169127628841" targetNodeId="tpck.1169194658468" resolveInfo="INamedConcept" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="5312209286555037514" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="InterfaceRef" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="tpck.1133920641626" resolveInfo="BaseConcept" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5312209286555038027" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="interface" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="5312209286554516176" resolveInfo="Trait" />
    </node>
  </root>
  <root type="tpce.ConceptDeclaration" typeId="tpce.1071489090640" id="5857873509721568548" nodeInfo="ig">
    <property name="name" nameId="tpck.1169194664001" value="TraitType" />
    <link role="extends" roleId="tpce.1071489389519" targetNodeId="2348480312264231173" resolveInfo="SerializableType" />
    <node role="linkDeclaration" roleId="tpce.1071489727083" type="tpce.LinkDeclaration" typeId="tpce.1071489288298" id="5857873509721852744" nodeInfo="ig">
      <property name="metaClass" nameId="tpce.1071599937831" value="reference" />
      <property name="role" nameId="tpce.1071599776563" value="trait" />
      <property name="sourceCardinality" nameId="tpce.1071599893252" value="1" />
      <link role="target" roleId="tpce.1071599976176" targetNodeId="5312209286554516176" resolveInfo="Trait" />
    </node>
  </root>
</model>

