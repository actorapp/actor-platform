<?xml version="1.0" encoding="UTF-8"?>
<model modelUID="r:10dad060-2c0e-4a4f-88f7-05a2c7d5e0f5(im.actor.apiLanguage.sandbox)">
  <persistence version="8" />
  <language namespace="77fdf769-432b-4ede-8171-050f8dee73fc(im.actor.apiLanguage)" />
  <language namespace="ceab5195-25ea-4f22-9b92-103b95ca8c0c(jetbrains.mps.lang.core)" />
  <import index="tpck" modelUID="r:00000000-0000-4000-0000-011c89590288(jetbrains.mps.lang.core.structure)" version="0" implicit="yes" />
  <import index="tsp6" modelUID="r:871d4088-0da0-4f3f-8413-5b3c60c61001(im.actor.apiLanguage.structure)" version="0" implicit="yes" />
  <root type="tsp6.ApiDescription" typeId="tsp6.2348480312264232779" id="2348480312264243645" nodeInfo="ng">
    <property name="name" nameId="tpck.1169194664001" value="ActorApi" />
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395512738" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Authentication" />
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481191537" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Actor now support only one way for authentication - by SMS or phone call." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481230379" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Authorization steps:" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481235572" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="1) Request SMS Code by calling RequestAuthCode" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481235576" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="2) If SMS arrives than send Authorization code in RequestAuthCodeSignIn/SignUp" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481236621" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="3) If sms doesn't arrive for a long time - request phone activation by " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481237668" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="   calling AuthCodeCall" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481240812" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481237675" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* If RequestAuthCode return isRegistered = false than use SignUp method else SignIn." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481241870" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* If on any step API return PHONE_CODE_EXPIRED than application MUST start " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481242924" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="authorization process from begining." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481242935" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* Each client MUST send valid RSA 2048 bit PublicKey encoded in x.509 format." />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395513514" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RequestAuthCode" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513520" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="phoneNumber" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395513524" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513527" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="appId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395513533" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513536" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="apiKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395513544" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395513515" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="01" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395513547" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513552" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="smsHash" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395513556" nodeInfo="ng" />
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513559" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="2" />
            <property name="name" nameId="tpck.1169194664001" value="isRegistered" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Boolean" typeId="tsp6.2348480312264231189" id="803735062395513565" nodeInfo="ng" />
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395513548" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="02" />
          </node>
        </node>
        <node role="doc" roleId="tsp6.2861239048480461214" type="tsp6.MethodDoc" typeId="tsp6.2861239048480449505" id="2861239048480641741" nodeInfo="ng">
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480641743" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_NUMBER_INVALID" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395517114" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395514047" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RequestAuthCodeCall" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514070" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="phoneNumber" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395514074" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514077" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="smsHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395514083" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514086" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="appId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395514094" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514097" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="apiKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395514107" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395514048" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="5a" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395514110" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
        <node role="doc" roleId="tsp6.2861239048480461214" type="tsp6.MethodDoc" typeId="tsp6.2861239048480449505" id="2861239048480690166" nodeInfo="ng">
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690167" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_CODE_EXPIRED" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690170" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_NUMBER_INVALID" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395517990" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Response" typeId="tsp6.2348480312265103643" id="803735062395514581" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Auth" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514631" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="publicKeyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395514635" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514638" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="user" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395514644" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514647" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="config" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395544209" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395513369" resolveInfo="Config" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395514582" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="05" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395521132" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395514294" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SignIn" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514328" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="phoneNumber" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395514332" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514335" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="smsHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395514341" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514344" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="smsCode" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395514352" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514355" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="publicKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395514365" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514368" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="deviceHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395514380" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514383" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="deviceTitle" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395514397" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514400" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="7" />
          <property name="name" nameId="tpck.1169194664001" value="appId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395514416" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395514419" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="appKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395514437" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395514295" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="03" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395515138" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395514581" resolveInfo="Auth" />
        </node>
        <node role="doc" roleId="tsp6.2861239048480461214" type="tsp6.MethodDoc" typeId="tsp6.2861239048480449505" id="2861239048480690456" nodeInfo="ng">
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690457" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: INVALID_KEY" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690770" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_NUMBER_UNOCCUPIED" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690773" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_CODE_INVALID " />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690777" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_CODE_EXPIRED" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690782" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_CODE_EMPTY" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480690788" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_NUMBER_INVALID" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395522048" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395515376" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SignUp" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515437" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="phoneNumber" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395515441" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515444" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="smsHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395515450" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515453" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="smsCode" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395515461" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515464" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395515474" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515477" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="publicKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395515489" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515492" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="7" />
          <property name="name" nameId="tpck.1169194664001" value="deviceHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395515506" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515509" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="deviceTitle" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395515525" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515528" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="9" />
          <property name="name" nameId="tpck.1169194664001" value="appId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395515546" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515549" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="10" />
          <property name="name" nameId="tpck.1169194664001" value="appKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395515569" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395515572" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="11" />
          <property name="name" nameId="tpck.1169194664001" value="isSilent" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Boolean" typeId="tsp6.2348480312264231189" id="803735062395515594" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395515377" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="04" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395515597" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395514581" resolveInfo="Auth" />
        </node>
        <node role="doc" roleId="tsp6.2861239048480461214" type="tsp6.MethodDoc" typeId="tsp6.2861239048480449505" id="2861239048480691969" nodeInfo="ng">
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480691970" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: INVALID_KEY" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480691976" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_CODE_EXPIRED" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480692263" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_CODE_EMPTY" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480692268" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: PHONE_NUMBER_INVALID" />
          </node>
          <node role="docs" roleId="tsp6.2861239048480449664" type="tsp6.MethodDocItem" typeId="tsp6.2861239048480449583" id="2861239048480692274" nodeInfo="ng">
            <property name="content" nameId="tsp6.2861239048480459664" value="#400: NAME_INVALID" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395527970" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395542238" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Auth controll" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395543136" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395528291" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="AuthItem" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395528612" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="id" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395528841" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395529069" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="authHolder" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395529302" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395529986" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="appId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395530223" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395530455" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="appTitle" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395530696" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395530930" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="deviceTitle" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395531175" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395531411" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="authTime" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395531660" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395532133" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="7" />
          <property name="name" nameId="tpck.1169194664001" value="authLocation" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395532386" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395532863" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="latitude" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395533320" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.Double" typeId="tsp6.803735062395533120" id="803735062395533326" nodeInfo="ng" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395533569" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="9" />
          <property name="name" nameId="tpck.1169194664001" value="longitude" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395533590" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.Double" typeId="tsp6.803735062395533120" id="803735062395533596" nodeInfo="ng" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395534933" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GetAuth" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395534934" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="50" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395535549" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395536048" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="userAuths" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395536052" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395536308" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395528291" resolveInfo="AuthItem" />
              </node>
            </node>
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395535550" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="51" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395537186" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RemoveAuth" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395538851" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="id" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395538855" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395537187" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="52" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395538076" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395538461" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RemoveAllOtherAuths" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395538462" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="53" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395539890" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395540284" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Logout" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395540285" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="54" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395540684" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062394849919" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Users" />
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481245039" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Users are objects that secured by accessHash. You can't load user profile by it's id." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481247135" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="You can't send message to user without finding it's object in Updates or by calling" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481248186" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="method for user search, contacts import or some other methods." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481251337" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481251342" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Applications need to keep all Users information forever." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481254501" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481254522" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Each User have optional localName - name of user that was set by current user and can be changed" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481255583" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="any time by calling EditUserLocalName method." />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395511462" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Model" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Enum" typeId="tsp6.2348480312264620144" id="803735062394850075" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Sex" />
        <node role="attributes" roleId="tsp6.2348480312264712169" type="tsp6.EnumAttribute" typeId="tsp6.2348480312264710733" id="803735062394850077" nodeInfo="ng">
          <property name="name" nameId="tpck.1169194664001" value="Unknown" />
          <property name="id" nameId="tsp6.2348480312264710768" value="1" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312264712169" type="tsp6.EnumAttribute" typeId="tsp6.2348480312264710733" id="803735062394850085" nodeInfo="ng">
          <property name="name" nameId="tpck.1169194664001" value="Male" />
          <property name="id" nameId="tsp6.2348480312264710768" value="2" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312264712169" type="tsp6.EnumAttribute" typeId="tsp6.2348480312264710733" id="803735062394850093" nodeInfo="ng">
          <property name="name" nameId="tpck.1169194664001" value="Female" />
          <property name="id" nameId="tsp6.2348480312264710768" value="3" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395368173" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="User" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395368232" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="id" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395368236" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395368239" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395368245" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395368248" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395368256" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395368259" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="localName" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395368269" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395368275" nodeInfo="ng" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395368278" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="sex" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395368291" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.EnumType" typeId="tsp6.803735062395368411" id="803735062395511055" nodeInfo="ng">
              <link role="struct" roleId="tsp6.803735062395368531" targetNodeId="803735062394850075" resolveInfo="Sex" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511102" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="keyHashes" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395511121" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395511127" nodeInfo="ng" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511130" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="7" />
          <property name="name" nameId="tpck.1169194664001" value="phone" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395511149" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511152" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="avatar" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395511173" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395525667" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850407" resolveInfo="Avatar" />
            </node>
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395548954" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395549318" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EditUserLocalName" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395549367" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395549371" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395549374" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395549380" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395549383" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395549391" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395549319" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="60" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395549394" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395620887" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395550597" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserAvatarChanged" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395550652" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395550656" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395550659" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="avatar" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395550665" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395550671" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850407" resolveInfo="Avatar" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395550598" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="10" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395550733" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserNameChanged" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395550795" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395550799" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395550802" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395550808" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395550734" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="20" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395550876" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserLocalNameChanged" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395550944" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395550948" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395550951" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="localName" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395550957" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395550963" nodeInfo="ng" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395550877" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="33" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395635838" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Profile" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395636765" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EditName" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395636773" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395636777" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395636766" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="35" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395636780" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395636790" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EditAvatar" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395636803" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="fileLocation" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395636807" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850146" resolveInfo="FileLocation" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395636791" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="1F" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395636810" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395604087" resolveInfo="AvatarChanged" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395637760" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RemoveAvatar" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395637761" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="5B" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395637778" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395551273" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Contacts" />
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481376883" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Before working with contact list is is useful to import contacts from phone first by calling" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481379243" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="method ImportContacts#0x07." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481380426" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481380430" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="All phone numbers MUST be preprocessed before import by some library (like libphonenumber)" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481381617" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="and build international phone number depending on current users phone and/or locale." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481382806" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481382813" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For loading contact list from server use GetContacts#0x57. " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481385191" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="If during this call there are some updates about contact list change" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481386386" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="it is recommended to call it again. Also applications need to sync contacts on application start." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481387595" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481386396" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For searching for users without adding to contacts list use method FindContacts#0x70." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481388810" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481387607" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For adding/deleting contacts AddContact#0x72 and DeleteContact#0x59." />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395552756" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Import" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395551889" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="PhoneToImport" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395551891" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="phoneNumber" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395551895" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395551898" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395551904" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395551910" nodeInfo="ng" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395551921" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EmailToImport" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395551930" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="email" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395551934" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395551937" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395551943" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395551949" nodeInfo="ng" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395552286" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ImportContacts" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395552319" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="phones" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395552323" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395552329" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395551889" resolveInfo="PhoneToImport" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395552332" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="emails" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395552339" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395552345" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395551921" resolveInfo="EmailToImport" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395552287" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="07" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395552348" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395552353" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="users" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395552357" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395552363" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395552366" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="2" />
            <property name="name" nameId="tpck.1169194664001" value="seq" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395552373" nodeInfo="ng" />
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395552376" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="3" />
            <property name="name" nameId="tpck.1169194664001" value="states" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395552385" nodeInfo="ng" />
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395552349" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="08" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395553660" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395552820" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Working with list" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395553728" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GetContacts" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395553768" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="contactsHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395553772" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395553729" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="57" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395553775" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395553780" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="users" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395553784" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395553790" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395553793" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="2" />
            <property name="name" nameId="tpck.1169194664001" value="isNotChanged" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Boolean" typeId="tsp6.2348480312264231189" id="803735062395553800" nodeInfo="ng" />
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395553776" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="58" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395553856" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RemoveContact" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395553907" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395553911" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395553914" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395553920" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395553857" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="59" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395553923" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395553978" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="AddContact" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395554036" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395554040" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395554043" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395554049" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395553979" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="72" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395554052" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395554479" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SearchContacts" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395554544" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="request" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395554548" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395554480" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="70" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395554551" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395554556" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="users" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395554560" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395554566" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
              </node>
            </node>
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395554552" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="71" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395555926" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395555996" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ContactRegistered" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395556445" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395556449" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395556452" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="isSilent" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Boolean" typeId="tsp6.2348480312264231189" id="803735062395556458" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395556461" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395556469" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395555997" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="05" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395556549" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ContactsAdded" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395556629" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uids" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395556633" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395556639" nodeInfo="ng" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395556550" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="28" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395556724" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ContactsRemoved" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395556809" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uids" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395556813" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395556819" nodeInfo="ng" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395556725" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="29" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395567464" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Messaging" />
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481256646" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Overview&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481257703" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Actor can work with encrypted and plain messages in one conversation. For both types of messages API" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481258768" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="contains a bit different methods. Also encrypted and plain messages have different schemes." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481258772" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Messages&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481259835" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Message entity contains:" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481260900" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* PeerType - group chat or private" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481261967" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* PeerId - group or user id of conversation" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481263036" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* RandomId - unique id of message that generated by sender. In Encrypted messages random id is encrypted." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481264107" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* Date - date of message (calculated on server)" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481265180" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="* Content" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481266255" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Message content&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481267332" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Message can be one of three basic types of messages: Text Message, File Message and Service message." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481268411" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="All messages can contain extensions. For example we can send text message and add markdown extension with " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481272693" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="formatted text in markdown and clients that support this extension will show markdown, and that clients that" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481273776" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="not supported extension then show simple text. File messages can have photo, video or voice extensions." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481274861" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Service message can have extensions extensions such as &quot;user added&quot;, &quot;group created&quot;, &quot;avatar changed&quot;, etc." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481277018" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Send messages&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481277036" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Sending messages looks same for encrypted and plain messages. Client MUST prepare all required data" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481279199" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="before sending message (for example FastThumb for photo/video/documents) and call required methods. " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481280292" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Encrypted messages differs here only by a little different scheme and encryption." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481281387" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;WRONG_KEYS and incorrect keys&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481282484" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For sending encrypted messages client MUST send messages encrypted for all own and receivers keys." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481283583" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="If client send encryption with missing, old or incorrect keys it will receive WRONG_KEYS." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481284684" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="In WRONG_KEYS you need to deserialize relatedData from RpcError to WrongKeysErrorData" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481285787" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="and get detailed information about keys. Sometimes there are some broken keys on server and client can't " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481287971" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="encrypt messages with it than client MUST send empty encrypted key in request elsewhere API return WRONG_KEYS." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481291238" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Encrypted messages and New Devices&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481292347" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="When you send message to someone and when he registered with new device there are no way to receive old encrypted" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481293458" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="messages on new device and because of this there are a problem about read/delivery statuses. " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481296737" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Alice send messages to Bob, but Bob lose his device and  buy new iPhone and installed Actor." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481298936" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Alice receive notification about new device and send another message. Bob open chat with Alice and" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481303308" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="send read status with maximum message read date. Alice will mark all sent messages as read and one that" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481306599" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="was not delivered. We can use status notifications per message, but in VERY heavy conversations it will be" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481309894" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="a lot of unnecessary traffic. For resolving this small issue we have different ways of message statuses" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481313193" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="for encrypted and plain messages. Also it is recomended to mark all undelivered messages on new device update as " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481321024" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="not devered with warring sign." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481313229" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Message Read and Delivery&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481315446" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="There are two different ways for read and delivery statuses for encrypted and plain messages." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481316575" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For encrypted messages used status change by RandomId and for plain messages used by maximum" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481318798" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="date of read/delivered message." />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395568757" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Model" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395570620" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageContent" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395570652" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="type" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395570656" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395570659" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="content" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395570665" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395570704" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="TextMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395571238" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="text" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395571242" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395571245" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="extType" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395571251" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395571254" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="ext" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395571262" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395571268" nodeInfo="ng" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395571315" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ServiceMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395571360" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="text" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395571364" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395571367" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="extType" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395571373" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395571376" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="ext" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395571384" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395571390" nodeInfo="ng" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395571957" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ServiceExUserAdded" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395572010" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="addedUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395572014" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395572072" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ServiceExUserKicked" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395572128" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="kickedUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395572132" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395572193" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ServiceExUserLeft" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395572311" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ServiceExGroupCreated" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395572951" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ServiceExChangedTItle" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573012" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="title" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395573016" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395573082" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ServiceExChangedAvatar" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573146" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="avatar" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395573150" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395573156" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850407" resolveInfo="Avatar" />
            </node>
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395573753" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="FileMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573821" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="fileId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395573825" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573828" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395573834" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573837" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="fileSize" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395573845" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573848" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="name" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395573858" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573861" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="mimeType" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395573873" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573876" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="thumb" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395573890" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395573896" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511260" resolveInfo="FastThumb" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573899" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="7" />
          <property name="name" nameId="tpck.1169194664001" value="extType" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395573916" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395573919" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="ext" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395573938" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395573944" nodeInfo="ng" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395574579" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="FileExPhoto" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395574666" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="w" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395574670" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395574673" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="h" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395574679" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395574773" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="FileExVideo" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395574865" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="w" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395574869" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395574872" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="h" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395574878" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395574881" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="duration" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395574889" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395574990" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="FileExVoice" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395575089" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="duration" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395575093" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395586701" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395587045" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="WrongKeysErrorData" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395587851" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="newKeys" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395587855" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395587861" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511433" resolveInfo="UserKey" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395587864" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="removedKeys" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395587871" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395587877" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511433" resolveInfo="UserKey" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395587880" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="invalidKeys" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395587890" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395587896" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511433" resolveInfo="UserKey" />
            </node>
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395590288" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Response" typeId="tsp6.2348480312265103643" id="803735062395591025" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageSent" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395591211" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="seq" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395591215" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395591218" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="state" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395591224" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395591227" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395591235" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395591026" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="73" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395592516" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EncryptedAesKey" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395592723" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="keyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395592727" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395592730" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="aesEncryptedKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395592736" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395592080" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SendEncryptedMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395592277" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395592281" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395592284" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395592290" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395592293" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="encryptedMessage" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395592301" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395592304" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="keys" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395593406" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395593412" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395592516" resolveInfo="EncryptedAesKey" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395593415" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="ownKeys" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395593428" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395593434" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395592516" resolveInfo="EncryptedAesKey" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395592081" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="0E" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395594108" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395591025" resolveInfo="MessageSent" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395594993" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SendMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395595210" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395595214" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395595217" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395595223" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395595226" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="message" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395595234" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395570620" resolveInfo="MessageContent" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395594994" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="5c" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395595237" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395591025" resolveInfo="MessageSent" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395596140" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EncryptedReceived" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395596366" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395596370" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395596373" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395596379" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395596141" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="74" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395596382" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395596612" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EncryptedRead" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395596845" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395596849" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395596852" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395596858" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395596613" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="75" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395596861" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395597098" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageReceived" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395597338" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395597342" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395597347" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395597353" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395597099" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="37" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395597356" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395597600" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageRead" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395597847" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395597851" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395597854" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395597860" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395597601" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="39" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395597863" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395598114" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="DeleteMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395598368" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395598372" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395598375" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395598381" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395598115" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="62" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395598384" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395599357" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="clearChat" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395599621" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395599625" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395599358" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="63" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395599618" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395600608" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="DeleteChat" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395600878" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395600882" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395600609" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="64" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395600885" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395570588" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395569448" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Logic" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395568089" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EncryptedMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568094" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395568098" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568101" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="senderUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395568107" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568110" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395568118" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568121" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="keyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395568131" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568134" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="aesEncryptedKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395568146" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568149" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="message" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395568163" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395568090" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="01" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395568658" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Message" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568677" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395568681" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568684" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="senderUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395568690" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568693" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395568701" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568704" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395568714" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395568717" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="message" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395576238" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395570620" resolveInfo="MessageContent" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395568659" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="37" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395576903" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageSent" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395577007" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395577019" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395577022" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395577028" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395577031" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395577039" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395576904" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="04" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395577720" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Encrypted" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395577940" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EncryptedReceived" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395578053" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395578057" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395578060" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395578066" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395578915" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="receivedDate" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395578923" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395577941" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="12" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395578185" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EncryptedRead" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395578304" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395578308" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395578311" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395578317" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395578320" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="readDate" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395578328" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395578186" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="34" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395579892" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EncryptedReadByMe" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395580022" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395580026" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395580029" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395580035" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395579893" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="35" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395580300" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Plain" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395580566" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageReceived" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395580702" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395580706" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395580709" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395580715" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395580718" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="receivedDate" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395580726" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395580567" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="36" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395580870" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageRead" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395581014" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395581018" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395581021" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395581027" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395581030" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="readDate" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395581038" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395580871" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="13" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395581799" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageReadByMe" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395581951" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395581955" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395581958" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395581964" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395581800" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="32" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395582737" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Message deletions" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395583049" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="MessageDelete" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395583208" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395583212" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395583215" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395583221" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395583227" nodeInfo="ng" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395583050" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="2E" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395583393" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ChatClear" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395583559" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395583563" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395583394" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="2F" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395583733" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="ChatDelete" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395583903" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395583907" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395583734" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="30" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395512239" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Groups" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395512439" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Group" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512441" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="id" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395512445" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512551" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395512557" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512560" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="title" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395512568" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512571" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="avatar" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395512581" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395525431" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850407" resolveInfo="Avatar" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512590" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="isMember" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Boolean" typeId="tsp6.2348480312264231189" id="803735062395512603" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512606" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="adminUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395512621" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395618165" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395601629" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="CreateGroup" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395601651" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395601655" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395601658" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="title" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395601664" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395601667" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="users" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395601675" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395601681" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511946" resolveInfo="UserOutPeer" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395601630" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="41" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395601684" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395601689" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395601693" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395601696" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="3" />
            <property name="name" nameId="tpck.1169194664001" value="seq" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395601702" nodeInfo="ng" />
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395601705" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="4" />
            <property name="name" nameId="tpck.1169194664001" value="state" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395601713" nodeInfo="ng" />
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395601716" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="5" />
            <property name="name" nameId="tpck.1169194664001" value="users" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395601726" nodeInfo="ng" />
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395601685" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="42" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395602508" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EditGroupTitle" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395602549" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395602553" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395602556" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="title" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395603313" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395602509" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="55" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395603316" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395605012" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="EditGroupAvatar" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395605060" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395605064" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395605067" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="fileLocation" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395605073" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850146" resolveInfo="FileLocation" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395605013" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="56" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395605076" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395604087" resolveInfo="AvatarChanged" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395605894" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RemoveGroupAvatar" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395605949" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395605953" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395605895" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="65" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395605956" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395609097" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="InviteUsers" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395609157" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395609161" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395609164" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="users" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395609170" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395609176" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511946" resolveInfo="UserOutPeer" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395609098" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="45" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395609179" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395609244" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="LeaveGroup" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395609312" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395609316" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395609245" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="46" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395609319" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395609389" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="DeleteGroup" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395609462" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395609466" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395609390" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="61" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395609469" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395609544" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RemoveUsers" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395609622" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupPeer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395609626" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395609629" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="users" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395609635" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395609641" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511946" resolveInfo="UserOutPeer" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395609545" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="47" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395609644" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395629235" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395629399" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupInvite" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395629484" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395629488" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395629491" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="inviteUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395629497" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395629500" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395629508" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395629400" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="24" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395629601" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupUserAdded" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395629694" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395629698" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395629701" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395629707" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395629710" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="inviterUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395629718" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395629721" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395629731" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395629602" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="15" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395630712" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupUserLeave" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395630815" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395630819" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395630822" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395630828" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395630831" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395630839" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395630713" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="17" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395631166" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupUserKick" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395631277" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395631281" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395631284" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395631290" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395631293" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="kickerUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395631301" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395631304" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395631314" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395631167" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="18" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395632331" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupMembersUpdate" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395632452" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395632456" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395632470" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="members" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395632476" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395632482" nodeInfo="ng" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395632332" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="2C" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395633513" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupTitleChanged" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395633641" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395633645" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395633648" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395633654" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395633657" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="title" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395633665" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395633668" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395633678" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395633514" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="26" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395634729" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupAvatarChanged" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395634867" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395634871" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395634874" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395634880" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395634883" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="avatar" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395634891" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395634897" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850407" resolveInfo="Avatar" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395634900" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395634911" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395634730" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="27" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="2861239048481322159" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Message and Dialogs lists" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="2861239048481363463" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="HistoryMessage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363466" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="senderUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="2861239048481363470" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363473" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="2861239048481363479" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363482" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="2861239048481363490" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363493" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="message" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481363503" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395570620" resolveInfo="MessageContent" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="2861239048481363517" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="LoadHistory" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363534" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481363538" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363541" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="startDate" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="2861239048481363547" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363550" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="limit" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="2861239048481363558" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="2861239048481363518" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="76" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="2861239048481363561" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481363566" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="history" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="2861239048481363570" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481363576" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="2861239048481363463" resolveInfo="HistoryMessage" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481365863" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="2" />
            <property name="name" nameId="tpck.1169194664001" value="users" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="2861239048481365870" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481365876" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
              </node>
            </node>
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="2861239048481363562" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="77" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="2861239048481375645" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="2861239048481369440" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Dialog" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481369475" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481369479" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481369482" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="unreadCount" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="2861239048481369488" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481369491" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="sortDate" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="2861239048481369499" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481369502" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="senderUid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="2861239048481369512" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481369515" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="rid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="2861239048481369527" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481370693" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="7" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="2861239048481370707" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481370710" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="8" />
          <property name="name" nameId="tpck.1169194664001" value="message" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481370726" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395570620" resolveInfo="MessageContent" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="2861239048481368197" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="LoadDialogs" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481369378" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="startDate" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="2861239048481369382" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481369385" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="limit" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="2861239048481369391" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="2861239048481368198" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="68" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="2861239048481371896" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481371901" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="groups" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="2861239048481371905" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481371911" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512439" resolveInfo="Group" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481371914" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="2" />
            <property name="name" nameId="tpck.1169194664001" value="users" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="2861239048481371921" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481371927" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="2861239048481371930" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="3" />
            <property name="name" nameId="tpck.1169194664001" value="dialogs" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="2861239048481371940" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="2861239048481371946" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="2861239048481369440" resolveInfo="Dialog" />
              </node>
            </node>
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="2861239048481371897" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="69" />
          </node>
        </node>
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481324349" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Overview&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481325447" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Actor can work with encrypted and plain messages and encrypted messages does not appear in history," />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481327644" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="but it affects conversation lists." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481328746" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Messages ordering&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481329850" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Server provide date in milliseconds for accurate ordering of incoming messages in applications. " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481330956" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="NOTHING can move conversation on conversation list down on list. For example if you clean chat or delete top message" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481333165" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="for the conversation conversation keep its position. Some events doesn't move conversation to top" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481336479" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="of conversations list. For example leaving chat or new device notification doesn't move it up." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481337591" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Deleting of messages&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481339809" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For deletion of messages either plain or encrypted there is method MessageDelete#0x62 deletion." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481342030" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Deletion of message is irreversible for now." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481343148" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Deleting and clearing of conversation&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481344268" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Clearing of conversation deletes all messages in conversation and clears top message in conversation list." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481346498" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Deletion of conversation deletes all messages and removes conversation from conversations list." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481347622" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For sync this operations there are updates ChatDelete#0x30 and ChatClear#0x2F." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481349858" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Deletion by ChatDelete of group causes automatic group leaving." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481349875" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="&lt;h1&gt;Loading history&lt;/h1&gt;" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481351005" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For loading conversation list use method LoadDialogs#0x68. " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481353250" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="When top message in conversation is encrypted than dialog item will contain empty text. Dialog item contains" />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481354384" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="two dates - visual and sort, visual used for displaying date and sort for sorting dialog in dialog list." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481360028" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481357750" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="For loading history of conversation use method LoadHistory#0x68." />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481358888" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value=" " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481360052" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="Loading initial list are called with zero startDate and after loading more messages " />
      </node>
      <node role="docs" roleId="tsp6.2861239048481128232" type="tsp6.SectionDoc" typeId="tsp6.2861239048481125696" id="2861239048481362315" nodeInfo="ng">
        <property name="text" nameId="tsp6.2861239048481125830" value="we will use maximum date from messages for startDate value." />
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395511373" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Encryption" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395511433" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserKey" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511490" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395511494" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511497" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="keyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395511503" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395511579" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="PublicKey" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511587" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395511591" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511594" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="keyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395511600" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511603" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="key" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395511611" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395558408" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="NewDevice" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395558425" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395558429" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395558432" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="keyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395558438" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395558441" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="key" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395558449" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395558455" nodeInfo="ng" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395558458" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395558469" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395558409" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="02" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395558901" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RemovedDevice" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395558929" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395558933" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395558936" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="keyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395558942" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395558902" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="25" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395560336" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395545933" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="PublicKeyRequest" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395545965" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395545969" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395545972" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395545978" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395545981" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="keyHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395545989" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395545868" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GetPublicKeys" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395545869" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="06" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395546265" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395546270" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="keys" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395546274" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395547982" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511579" resolveInfo="PublicKey" />
              </node>
            </node>
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395546266" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="18" />
          </node>
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395610444" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Typing and Online" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395611244" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Typing" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395611252" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395611256" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511891" resolveInfo="OutPeer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395611259" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="typingType" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395611265" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395611245" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="1B" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395611268" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395611280" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SetOnline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395611295" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="isOnline" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Boolean" typeId="tsp6.2348480312264231189" id="803735062395611299" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395611302" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="timeout" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395611308" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395611281" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="1D" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395611311" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395623567" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395623585" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Typing" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395623605" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="peer" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395623609" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511777" resolveInfo="Peer" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395623612" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395623618" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395623621" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="typingType" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395623629" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395623586" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="06" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395624496" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserOnline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395624524" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395624528" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395624497" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="07" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395624560" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserOffline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395624592" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395624596" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395624561" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="08" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395625479" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserLastSeen" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395625515" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395625519" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395625522" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="time" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395625528" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395625480" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="09" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395626423" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupOnline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395626465" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395626469" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395626472" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="count" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395626478" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395626424" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="21" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062394850116" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Media and Files" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062394850146" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="FileLocation" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062394850296" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="fileId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062394850300" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062394850303" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062394850309" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062394850132" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="AvatarImage" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062394850327" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="fileLocation" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062394850331" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850146" resolveInfo="FileLocation" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062394850334" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="width" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062394850340" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062394850343" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="height" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062394850351" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062394850354" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="fileSize" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062394850364" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062394850407" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Avatar" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395320485" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="smallImage" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395320639" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395367956" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850132" resolveInfo="AvatarImage" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395367959" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="largeImage" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395367980" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395367986" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850132" resolveInfo="AvatarImage" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395367989" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="fullImage" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Optional" typeId="tsp6.2348480312264232735" id="803735062395367999" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062394906775" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395368005" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850132" resolveInfo="AvatarImage" />
            </node>
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395511260" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="FastThumb" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511287" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="w" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395511291" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511294" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="h" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395511300" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511303" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="thumb" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395511311" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395562053" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395562857" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Download files" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395562121" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GetFile" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395562161" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="fileLocation" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395562165" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850146" resolveInfo="FileLocation" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395562168" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="offset" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395562174" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395562177" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="limit" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395562185" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395562122" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="10" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395562188" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395562193" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="payload" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395562197" nodeInfo="ng" />
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395562189" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="11" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395563996" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiComment" typeId="tsp6.4689615199750788559" id="803735062395562999" nodeInfo="ng">
        <property name="text" nameId="tsp6.4689615199750789856" value="Upload files" />
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395564624" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UploadConfig" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395564674" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="serverData" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395564678" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395565327" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="StartUpload" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395565328" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="12" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395565385" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395565390" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="config" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395565394" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395564624" resolveInfo="UploadConfig" />
            </node>
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395565386" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="13" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395565891" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UploadPart" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395565955" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="config" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395565959" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395564624" resolveInfo="UploadConfig" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395565962" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="blockIndex" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395565968" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395565971" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="payload" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395565979" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395565892" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="14" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395565982" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395566052" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="CompleteUpload" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395566125" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="config" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395566129" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395564624" resolveInfo="UploadConfig" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395566132" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="blocksCount" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395566138" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395566141" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="crc32" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395566149" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395566053" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="16" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395566152" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395566157" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="location" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395566161" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850146" resolveInfo="FileLocation" />
            </node>
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395566153" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="17" />
          </node>
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395612126" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Push" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395613754" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RegisterGooglePush" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395613762" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="projectId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395613766" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395613755" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="33" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395613769" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395613779" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="RegisterApplePush" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395613792" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="apnsKey" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395613796" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395613799" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="token" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.String" typeId="tsp6.2348480312264231195" id="803735062395613805" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395613780" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="4C" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395613808" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395614650" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UnregisterPush" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395614651" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="34" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395614674" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395511687" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Peers" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Enum" typeId="tsp6.2348480312264620144" id="803735062395511763" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="PeerType" />
        <node role="attributes" roleId="tsp6.2348480312264712169" type="tsp6.EnumAttribute" typeId="tsp6.2348480312264710733" id="803735062395511765" nodeInfo="ng">
          <property name="name" nameId="tpck.1169194664001" value="Private" />
          <property name="id" nameId="tsp6.2348480312264710768" value="1" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312264712169" type="tsp6.EnumAttribute" typeId="tsp6.2348480312264710733" id="803735062395511769" nodeInfo="ng">
          <property name="name" nameId="tpck.1169194664001" value="Group" />
          <property name="id" nameId="tsp6.2348480312264710768" value="2" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395511777" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Peer" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511783" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="type" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.EnumType" typeId="tsp6.803735062395368411" id="803735062395511787" nodeInfo="ng">
            <link role="struct" roleId="tsp6.803735062395368531" targetNodeId="803735062395511763" resolveInfo="PeerType" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511790" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="id" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395511796" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395511891" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="OutPeer" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511902" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="type" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.EnumType" typeId="tsp6.803735062395368411" id="803735062395511906" nodeInfo="ng">
            <link role="struct" roleId="tsp6.803735062395368531" targetNodeId="803735062395511763" resolveInfo="PeerType" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511909" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="id" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395511915" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511918" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395511926" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395511946" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="UserOutPeer" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511964" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="uid" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395511968" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395511971" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395511977" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395512002" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GroupOutPeer" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512025" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groupId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395512029" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395512032" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="accessHash" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395512137" nodeInfo="ng" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395638719" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Sequence and Updates" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.UpdateBox" typeId="tsp6.803735062395648228" id="803735062395694223" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SeqUpdate" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395695281" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="seq" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395695285" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395695288" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="state" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395695294" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395695297" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="updateHeader" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395695305" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395695308" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="update" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395695318" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395694224" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="0D" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.UpdateBox" typeId="tsp6.803735062395648228" id="803735062395698648" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="FatSeqUpdate" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395698719" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="seq" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395698723" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395698726" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="state" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395698732" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395698735" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="updateHeader" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395698743" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395698746" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="4" />
          <property name="name" nameId="tpck.1169194664001" value="update" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395698756" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395698759" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="5" />
          <property name="name" nameId="tpck.1169194664001" value="users" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395698771" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395699795" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
            </node>
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395699798" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="6" />
          <property name="name" nameId="tpck.1169194664001" value="groups" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395699813" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395699819" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512439" resolveInfo="Group" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395698649" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="49" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.UpdateBox" typeId="tsp6.803735062395648228" id="803735062395703075" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="WeakUpdate" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395703162" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="date" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int64" typeId="tsp6.2348480312264231184" id="803735062395703166" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395703169" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="updateId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395703175" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395703178" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="update" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395703186" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395703076" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="1A" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.UpdateBox" typeId="tsp6.803735062395648228" id="803735062395706474" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SeqUpdateTooLong" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395706475" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="19" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395708831" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395642703" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GetState" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395642704" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="09" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395642739" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513212" resolveInfo="Seq" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395640628" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="DifferenceUpdate" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395640638" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="updateId" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395640642" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395640645" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="update" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395640651" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395640597" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="GetDifference" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395640603" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="seq" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395640607" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395640610" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="state" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395640616" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395640598" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="0B" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefAnonymous" typeId="tsp6.2348480312265149402" id="803735062395641605" nodeInfo="ng">
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395641610" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="1" />
            <property name="name" nameId="tpck.1169194664001" value="seq" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395641614" nodeInfo="ng" />
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395641617" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="2" />
            <property name="name" nameId="tpck.1169194664001" value="state" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395641623" nodeInfo="ng" />
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395641626" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="3" />
            <property name="name" nameId="tpck.1169194664001" value="users" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395641634" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395641640" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395368173" resolveInfo="User" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395641643" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="6" />
            <property name="name" nameId="tpck.1169194664001" value="groups" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395641654" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395641660" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512439" resolveInfo="Group" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395641663" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="4" />
            <property name="name" nameId="tpck.1169194664001" value="updates" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395641677" nodeInfo="ng">
              <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395641683" nodeInfo="ng">
                <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395640628" resolveInfo="DifferenceUpdate" />
              </node>
            </node>
          </node>
          <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395641686" nodeInfo="ng">
            <property name="id" nameId="tsp6.2348480312264746197" value="5" />
            <property name="name" nameId="tpck.1169194664001" value="needMode" />
            <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Boolean" typeId="tsp6.2348480312264231189" id="803735062395641703" nodeInfo="ng" />
          </node>
          <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395641606" nodeInfo="ng">
            <property name="hexValue" nameId="tsp6.4689615199750888593" value="0C" />
          </node>
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.ApiEmptyDef" typeId="tsp6.4689615199750780323" id="803735062395645781" nodeInfo="ng" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395646820" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SubscribeToOnline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395646860" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="users" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395646864" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395646870" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511946" resolveInfo="UserOutPeer" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395646821" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="20" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395646873" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395646916" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SubscribeFromOnline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395646962" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="users" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395646966" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395646975" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395511946" resolveInfo="UserOutPeer" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395646917" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="21" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395646978" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395647027" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SubscribeToGrouOnline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395647079" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groups" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395647083" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395647089" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395647028" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="4A" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395647092" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Rpc" typeId="tsp6.2348480312265114812" id="803735062395647147" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="SubscribeFromGroupOnline" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395647205" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="groups" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.List" typeId="tsp6.2348480312264232754" id="803735062395647209" nodeInfo="ng">
            <node role="type" roleId="tsp6.803735062395365470" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395647215" nodeInfo="ng">
              <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395512002" resolveInfo="GroupOutPeer" />
            </node>
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395647148" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="4B" />
        </node>
        <node role="response" roleId="tsp6.2348480312265120188" type="tsp6.ResponseRefValue" typeId="tsp6.2348480312265149479" id="803735062395647218" nodeInfo="ng">
          <link role="response" roleId="tsp6.2348480312265340979" targetNodeId="803735062395513200" resolveInfo="Void" />
        </node>
      </node>
    </node>
    <node role="sections" roleId="tsp6.2348480312264237371" type="tsp6.ApiSection" typeId="tsp6.2348480312264233362" id="803735062395512968" nodeInfo="ng">
      <property name="name" nameId="tpck.1169194664001" value="Common" />
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Response" typeId="tsp6.2348480312265103643" id="803735062395513200" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Void" />
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395513201" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="32" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Response" typeId="tsp6.2348480312265103643" id="803735062395513212" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Seq" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513219" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="seq" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395513223" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513226" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="state" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395513232" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395513213" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="48" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Response" typeId="tsp6.2348480312265103643" id="803735062395604087" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="AvatarChanged" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395604107" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="avatar" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395604169" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062394850407" resolveInfo="Avatar" />
          </node>
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395604191" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="2" />
          <property name="name" nameId="tpck.1169194664001" value="seq" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395604197" nodeInfo="ng" />
        </node>
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395604200" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="3" />
          <property name="name" nameId="tpck.1169194664001" value="state" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Bytes" typeId="tsp6.2348480312265108784" id="803735062395604208" nodeInfo="ng" />
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395604088" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="44" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Struct" typeId="tsp6.2348480312264231121" id="803735062395513369" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Config" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395513380" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="maxGroupSize" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.Int32" typeId="tsp6.2348480312264231180" id="803735062395513384" nodeInfo="ng" />
        </node>
      </node>
      <node role="definitions" roleId="tsp6.2348480312264233405" type="tsp6.Update" typeId="tsp6.4689615199751283321" id="803735062395566634" nodeInfo="ng">
        <property name="name" nameId="tpck.1169194664001" value="Config" />
        <node role="attributes" roleId="tsp6.2348480312265565703" type="tsp6.StructAttribute" typeId="tsp6.2348480312264653219" id="803735062395566650" nodeInfo="ng">
          <property name="id" nameId="tsp6.2348480312264746197" value="1" />
          <property name="name" nameId="tpck.1169194664001" value="config" />
          <node role="type" roleId="tsp6.2348480312264746167" type="tsp6.StructType" typeId="tsp6.2348480312264233334" id="803735062395566654" nodeInfo="ng">
            <link role="struct" roleId="tsp6.2348480312264233348" targetNodeId="803735062395513369" resolveInfo="Config" />
          </node>
        </node>
        <node role="header" roleId="tsp6.4689615199750927382" type="tsp6.HeaderKey" typeId="tsp6.4689615199750888590" id="803735062395566635" nodeInfo="ng">
          <property name="hexValue" nameId="tsp6.4689615199750888593" value="2A" />
        </node>
      </node>
    </node>
  </root>
</model>

