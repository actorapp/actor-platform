<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:10dad060-2c0e-4a4f-88f7-05a2c7d5e0f5(im.actor.apiLanguage.sandbox)">
  <persistence version="9" />
  <languages>
    <use id="77fdf769-432b-4ede-8171-050f8dee73fc" name="im.actor.api.language" version="-1" />
    <use id="ceab5195-25ea-4f22-9b92-103b95ca8c0c" name="jetbrains.mps.lang.core" version="1" />
  </languages>
  <imports />
  <registry>
    <language id="77fdf769-432b-4ede-8171-050f8dee73fc" name="im.actor.api.language">
      <concept id="2348480312265565650" name="im.actor.api.language.structure.IStruct" flags="ng" index="2m0hYO">
        <child id="2348480312265565703" name="attributes" index="2m0hLx" />
      </concept>
      <concept id="2348480312265228628" name="im.actor.api.language.structure.IHeaderStruct" flags="ng" index="2m1zGM">
        <child id="4689615199750927382" name="header" index="NuuwV" />
      </concept>
      <concept id="2348480312265149402" name="im.actor.api.language.structure.ResponseRefAnonymous" flags="ng" index="2m1R6W">
        <child id="5253915025833407436" name="docs" index="1y2DgH" />
      </concept>
      <concept id="2348480312265149479" name="im.actor.api.language.structure.ResponseRefValue" flags="ng" index="2m1Rp1">
        <reference id="2348480312265340979" name="response" index="2m1o9l" />
      </concept>
      <concept id="2348480312264620144" name="im.actor.api.language.structure.Enum" flags="ng" index="2m488m">
        <child id="2348480312264712169" name="attributes" index="2m7ymf" />
      </concept>
      <concept id="2348480312264233334" name="im.actor.api.language.structure.StructType" flags="ng" index="2m5mGg">
        <reference id="2348480312264233348" name="struct" index="2m5mJy" />
      </concept>
      <concept id="2348480312264233362" name="im.actor.api.language.structure.ApiSection" flags="ng" index="2m5mJO">
        <property id="3857470926884615265" name="package" index="3XOG$Z" />
        <child id="2348480312264233405" name="definitions" index="2m5mJr" />
        <child id="2861239048481128232" name="docs" index="1Dx9rD" />
      </concept>
      <concept id="2348480312264231121" name="im.actor.api.language.structure.Struct" flags="ng" index="2m5naR">
        <property id="2838010799854839786" name="isExpandable" index="tsOgz" />
        <property id="5312209286555312009" name="hasInterface" index="w4tQU" />
        <reference id="5312209286555405644" name="interface" index="w4$XZ" />
        <child id="773119248390105235" name="docs" index="NXodf" />
        <child id="5857873509723526645" name="header" index="3BtCOu" />
      </concept>
      <concept id="2348480312264231180" name="im.actor.api.language.structure.Int32" flags="ng" index="2m5ndE" />
      <concept id="2348480312264231189" name="im.actor.api.language.structure.Boolean" flags="ng" index="2m5ndN" />
      <concept id="2348480312264231184" name="im.actor.api.language.structure.Int64" flags="ng" index="2m5ndQ" />
      <concept id="2348480312264231195" name="im.actor.api.language.structure.String" flags="ng" index="2m5ndX" />
      <concept id="2348480312264232779" name="im.actor.api.language.structure.ApiDescription" flags="ng" index="2m5nkH">
        <property id="2838010799853060276" name="version" index="u_6dX" />
        <property id="291384077092427558" name="scalaPackage" index="WhUdw" />
        <property id="5857873509721316771" name="javaPackage" index="3BlOl8" />
        <child id="2348480312264237371" name="sections" index="2m5lHt" />
        <child id="5857873509721229475" name="aliases" index="3Bkp18" />
      </concept>
      <concept id="2348480312264232754" name="im.actor.api.language.structure.List" flags="ng" index="2m5nlk">
        <child id="803735062395365470" name="type" index="3GJlyp" />
      </concept>
      <concept id="2348480312264232735" name="im.actor.api.language.structure.Optional" flags="ng" index="2m5nlT">
        <child id="803735062394906775" name="type" index="3GH5xg" />
      </concept>
      <concept id="2348480312265108784" name="im.actor.api.language.structure.Bytes" flags="ng" index="2m61tm" />
      <concept id="2348480312265103643" name="im.actor.api.language.structure.Response" flags="ng" index="2m62dX">
        <child id="773119248390109922" name="docs" index="NXp4Y" />
      </concept>
      <concept id="2348480312265114812" name="im.actor.api.language.structure.Rpc" flags="ng" index="2m6fVq">
        <child id="2348480312265120188" name="response" index="2m6efq" />
        <child id="4092665470043063721" name="throws" index="2uC9gA" />
        <child id="3073351033372262909" name="docs" index="1GBnQ6" />
      </concept>
      <concept id="2348480312264710733" name="im.actor.api.language.structure.EnumAttribute" flags="ng" index="2m7y0F">
        <property id="2348480312264710768" name="id" index="2m7y0m" />
      </concept>
      <concept id="2348480312264653219" name="im.actor.api.language.structure.StructAttribute" flags="ng" index="2m7Kf5">
        <property id="2348480312264746197" name="id" index="2m7DUN" />
        <property id="2838010799853749721" name="isDeprecated" index="toYog" />
        <child id="2348480312264746167" name="type" index="2m7DVh" />
      </concept>
      <concept id="4092665470043052969" name="im.actor.api.language.structure.MethodThrows" flags="ng" index="2uC4CA">
        <property id="4092665470043053055" name="errorCode" index="2uC4DK" />
        <property id="4092665470043053057" name="errorTag" index="2uC4Qe" />
        <property id="4092665470043111358" name="description" index="2uCiSL" />
      </concept>
      <concept id="5312209286554516176" name="im.actor.api.language.structure.Trait" flags="ng" index="w93zz">
        <property id="6700515326227281642" name="isContainer" index="1FaRnq" />
      </concept>
      <concept id="5312209286553980838" name="im.actor.api.language.structure.AliasType" flags="ng" index="wb0Ql">
        <reference id="5312209286553980954" name="alias" index="wb18D" />
      </concept>
      <concept id="5312209286553449813" name="im.actor.api.language.structure.ApiAlias" flags="ng" index="wd7tA">
        <child id="5312209286553509039" name="sourceType" index="wdlUs" />
      </concept>
      <concept id="4689615199751283321" name="im.actor.api.language.structure.Update" flags="ng" index="NpBTk">
        <child id="773119248390107806" name="docs" index="NXp_2" />
      </concept>
      <concept id="4689615199750888590" name="im.actor.api.language.structure.HeaderKey" flags="ng" index="Nu42z">
        <property id="4689615199750888593" name="hexValue" index="Nu42W" />
      </concept>
      <concept id="4689615199750780323" name="im.actor.api.language.structure.ApiEmptyDef" flags="ng" index="NvyAe" />
      <concept id="4689615199750788559" name="im.actor.api.language.structure.ApiComment" flags="ng" index="NvWBy">
        <property id="4689615199750789856" name="text" index="NvWrd" />
      </concept>
      <concept id="773119248390078458" name="im.actor.api.language.structure.StructDocParameter" flags="ng" index="NX1gA">
        <property id="773119248390080030" name="description" index="NX6R2" />
        <property id="3073351033373018049" name="category" index="1GSvIU" />
        <reference id="773119248390080451" name="paramter" index="NX6Kv" />
      </concept>
      <concept id="773119248390047284" name="im.actor.api.language.structure.StructDocComment" flags="ng" index="NXeRC">
        <property id="773119248390047379" name="content" index="NXePf" />
      </concept>
      <concept id="5857873509721568548" name="im.actor.api.language.structure.TraitType" flags="ng" index="3BlaRf">
        <reference id="5857873509721852744" name="trait" index="3BrLez" />
      </concept>
      <concept id="2861239048481125696" name="im.actor.api.language.structure.SectionDoc" flags="ng" index="1Dx9M1">
        <property id="2861239048481125830" name="text" index="1Dx9K7" />
      </concept>
      <concept id="803735062395648228" name="im.actor.api.language.structure.UpdateBox" flags="ng" index="3GIgwz">
        <child id="773119248390108862" name="docs" index="NXpPy" />
      </concept>
      <concept id="803735062395533120" name="im.actor.api.language.structure.Double" flags="ng" index="3GIWu7" />
      <concept id="803735062395368411" name="im.actor.api.language.structure.EnumType" flags="ng" index="3GJkcs">
        <reference id="803735062395368531" name="struct" index="3GJkik" />
      </concept>
    </language>
    <language id="ceab5195-25ea-4f22-9b92-103b95ca8c0c" name="jetbrains.mps.lang.core">
      <concept id="1169194658468" name="jetbrains.mps.lang.core.structure.INamedConcept" flags="ng" index="TrEIO">
        <property id="1169194664001" name="name" index="TrG5h" />
      </concept>
    </language>
  </registry>
  <node concept="2m5nkH" id="22nuAqQwzAX">
    <property role="TrG5h" value="ActorApi" />
    <property role="3BlOl8" value="im.actor.model.api" />
    <property role="WhUdw" value="im.actor.api.rpc" />
    <property role="u_6dX" value="1.6" />
    <node concept="wd7tA" id="55bmeIQ7$gx" role="3Bkp18">
      <property role="TrG5h" value="seq_state" />
      <node concept="2m61tm" id="55bmeIQ7$g_" role="wdlUs" />
    </node>
    <node concept="wd7tA" id="2vxDjotnO8T" role="3Bkp18">
      <property role="TrG5h" value="date" />
      <node concept="2m5ndQ" id="2vxDjotnO92" role="wdlUs" />
    </node>
    <node concept="wd7tA" id="2vxDjotnUB8" role="3Bkp18">
      <property role="TrG5h" value="randomId" />
      <node concept="2m5ndQ" id="2vxDjotnUBg" role="wdlUs" />
    </node>
    <node concept="2m5mJO" id="GBscvBB6uy" role="2m5lHt">
      <property role="TrG5h" value="Authentication" />
      <property role="3XOG$Z" value="auth" />
      <node concept="1Dx9M1" id="2uPas5ecFpL" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;p&gt;Actor now support only one way for authentication - by SMS or phone call.&lt;/p&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecOSF" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;p&gt;Authorization steps:" />
      </node>
      <node concept="1Dx9M1" id="3zc4oYAom2n" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;ol&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecQ9O" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;Request SMS Code by calling RequestAuthCode&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecQ9S" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;If SMS arrives than send Authorization code in SignIn/SignUp&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecQqd" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;If sms doesn't arrive for a long time - request phone activation by " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecQE$" role="1Dx9rD">
        <property role="1Dx9K7" value="   calling AuthCodeCall&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecRrG" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;/ol&gt;" />
      </node>
      <node concept="1Dx9M1" id="3zc4oYAom2$" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;/p&gt;" />
      </node>
      <node concept="1Dx9M1" id="3zc4oYAom2M" role="1Dx9rD">
        <property role="1Dx9K7" value="Some rules&lt;br/&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecQEF" role="1Dx9rD">
        <property role="1Dx9K7" value="If RequestAuthCode return isRegistered = false than use SignUp method else SignIn.&lt;br/&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecRGe" role="1Dx9rD">
        <property role="1Dx9K7" value="If on any step API return PHONE_CODE_EXPIRED than application MUST start " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecRWG" role="1Dx9rD">
        <property role="1Dx9K7" value="authorization process from begining.&lt;br/&gt;" />
      </node>
      <node concept="2m488m" id="3zgy61ElGei" role="2m5mJr">
        <property role="TrG5h" value="EmailActivationType" />
        <node concept="2m7y0F" id="3zgy61ElGek" role="2m7ymf">
          <property role="TrG5h" value="CODE" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="3zgy61ElGhl" role="2m7ymf">
          <property role="TrG5h" value="OAUTH2" />
          <property role="2m7y0m" value="2" />
        </node>
      </node>
      <node concept="2m6fVq" id="3zgy61Em3ip" role="2m5mJr">
        <property role="TrG5h" value="StartPhoneAuth" />
        <node concept="NXeRC" id="3zgy61Em4S9" role="1GBnQ6">
          <property role="NXePf" value="Start Phone Activation" />
        </node>
        <node concept="NX1gA" id="3zgy61Em5lU" role="1GBnQ6">
          <property role="NX6R2" value="Phone number" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61Em3mo" resolve="phoneNumber" />
        </node>
        <node concept="NX1gA" id="3zgy61Em5m2" role="1GBnQ6">
          <property role="NX6R2" value="Appication Id" />
          <ref role="NX6Kv" node="3zgy61Em3me" resolve="appId" />
        </node>
        <node concept="NX1gA" id="3zgy61Em5mc" role="1GBnQ6">
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="3zgy61Em3mx" resolve="apiKey" />
        </node>
        <node concept="NX1gA" id="3zgy61Em5mo" role="1GBnQ6">
          <property role="NX6R2" value="Hash of device unique id and app bundle id. Used for autologout users when app is reinstalled" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61Em3mG" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="3zgy61Em5mA" role="1GBnQ6">
          <property role="NX6R2" value="Device Title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61Em3mT" resolve="deviceTitle" />
        </node>
        <node concept="2m7Kf5" id="3zgy61Em3mo" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="phoneNumber" />
          <node concept="2m5ndQ" id="3zgy61Em3mp" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61Em3me" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="3zgy61Em3mu" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61Em3mx" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="apiKey" />
          <node concept="2m5ndX" id="3zgy61Em3mD" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61Em3mG" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="deviceHash" />
          <node concept="2m61tm" id="3zgy61Em3mQ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61Em3mT" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="3zgy61Em3n5" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3zgy61Em3iq" role="NuuwV">
          <property role="Nu42W" value="BF" />
        </node>
        <node concept="2m1R6W" id="3zgy61Em3uU" role="2m6efq">
          <node concept="NXeRC" id="3zgy61Em6uz" role="1y2DgH">
            <property role="NXePf" value="Phone Activation response" />
          </node>
          <node concept="NX1gA" id="3zgy61Em6uD" role="1y2DgH">
            <property role="NX6R2" value="Hash of transaction" />
            <property role="1GSvIU" value="danger" />
            <ref role="NX6Kv" node="3zgy61Em3W_" resolve="transactionHash" />
          </node>
          <node concept="NX1gA" id="3zgy61Em6uL" role="1y2DgH">
            <property role="NX6R2" value="Is User registered" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="3zgy61Em4qk" resolve="isRegistered" />
          </node>
          <node concept="2m7Kf5" id="3zgy61Em3W_" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="transactionHash" />
            <node concept="2m5ndX" id="3zgy61Em3WD" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="3zgy61Em4qk" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="isRegistered" />
            <node concept="2m5ndN" id="3zgy61Em4qq" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="3zgy61Em3uV" role="NuuwV">
            <property role="Nu42W" value="C1" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="3zgy61ElELb" role="2m5mJr">
        <property role="TrG5h" value="StartEmailAuth" />
        <node concept="NXeRC" id="3zgy61ElFhG" role="1GBnQ6">
          <property role="NXePf" value="Start EMail Activation" />
        </node>
        <node concept="NX1gA" id="3zgy61ElFIx" role="1GBnQ6">
          <property role="NX6R2" value="Email" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61ElEO4" resolve="email" />
        </node>
        <node concept="NX1gA" id="3zgy61ElHb5" role="1GBnQ6">
          <property role="NX6R2" value="Application Id" />
          <ref role="NX6Kv" node="3zgy61ElEOb" resolve="appId" />
        </node>
        <node concept="NX1gA" id="3zgy61ElHbf" role="1GBnQ6">
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="3zgy61ElEOk" resolve="apiKey" />
        </node>
        <node concept="NX1gA" id="3zgy61Em7qy" role="1GBnQ6">
          <property role="NX6R2" value="Hash of device unique id and app bundle id. Used for autologout users when app is reinstalled" />
          <ref role="NX6Kv" node="3zgy61ElWcS" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="3zgy61Em7qK" role="1GBnQ6">
          <property role="NX6R2" value="Device Title" />
          <ref role="NX6Kv" node="3zgy61ElWd5" resolve="deviceTitle" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElEO4" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="email" />
          <node concept="2m5ndX" id="3zgy61ElEO8" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElEOb" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="3zgy61ElEOh" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElEOk" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="apiKey" />
          <node concept="2m5ndX" id="3zgy61ElEOs" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElWcS" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="deviceHash" />
          <node concept="2m61tm" id="3zgy61ElWd2" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElWd5" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="3zgy61ElWdh" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3zgy61ElELc" role="NuuwV">
          <property role="Nu42W" value="B9" />
        </node>
        <node concept="2m1R6W" id="3zgy61ElEOv" role="2m6efq">
          <node concept="NXeRC" id="3zgy61ElI4T" role="1y2DgH">
            <property role="NXePf" value="Email Activation response" />
          </node>
          <node concept="NX1gA" id="3zgy61ElIYz" role="1y2DgH">
            <property role="NX6R2" value="Hash of activation transaction" />
            <property role="1GSvIU" value="danger" />
            <ref role="NX6Kv" node="3zgy61ElEO$" resolve="transactionHash" />
          </node>
          <node concept="NX1gA" id="3zgy61ElIYF" role="1y2DgH">
            <property role="NX6R2" value="true if user is registered" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="3zgy61ElEOF" resolve="isRegistered" />
          </node>
          <node concept="NX1gA" id="3zgy61ElJrD" role="1y2DgH">
            <property role="NX6R2" value="Email Activation type" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="3zgy61ElGI5" resolve="activationType" />
          </node>
          <node concept="2m7Kf5" id="3zgy61ElEO$" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="transactionHash" />
            <node concept="2m5ndX" id="3zgy61ElEOC" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="3zgy61ElEOF" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="isRegistered" />
            <node concept="2m5ndN" id="3zgy61ElEOL" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="3zgy61ElGI5" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="activationType" />
            <node concept="3GJkcs" id="3zgy61ElGId" role="2m7DVh">
              <ref role="3GJkik" node="3zgy61ElGei" resolve="EmailActivationType" />
            </node>
          </node>
          <node concept="Nu42z" id="3zgy61ElEOw" role="NuuwV">
            <property role="Nu42W" value="BA" />
          </node>
        </node>
        <node concept="2uC4CA" id="3zgy61ElJS$" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="EMAIL_INVALID" />
          <property role="2uCiSL" value="Throws when email is invalid" />
        </node>
      </node>
      <node concept="2m6fVq" id="3zgy61ElQmM" role="2m5mJr">
        <property role="TrG5h" value="ValidateCode" />
        <node concept="2uC4CA" id="3zgy61ElSkB" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="UNOCCUPIED" />
          <property role="2uCiSL" value="Signup required" />
        </node>
        <node concept="2uC4CA" id="3zgy61ElSkC" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="INVALID" />
          <property role="2uCiSL" value="Activation code invalid" />
        </node>
        <node concept="2uC4CA" id="3zgy61ElSkD" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="EXPIRED" />
          <property role="2uCiSL" value="Transaction expired" />
        </node>
        <node concept="2uC4CA" id="3zgy61ElSkE" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="EMPTY" />
          <property role="2uCiSL" value="Activation code empty" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElRRg" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="transactionHash" />
          <node concept="2m5ndX" id="3zgy61ElRRk" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElTeQ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="code" />
          <node concept="2m5ndX" id="3zgy61ElTeW" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3zgy61ElQmN" role="NuuwV">
          <property role="Nu42W" value="BD" />
        </node>
        <node concept="2m1Rp1" id="3zgy61Em37K" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="NXeRC" id="3zgy61ElRqk" role="1GBnQ6">
          <property role="NXePf" value="Performing user sign in." />
        </node>
        <node concept="NX1gA" id="3zgy61ElU9y" role="1GBnQ6">
          <property role="NX6R2" value="Hash of transaction" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="3zgy61ElRRg" resolve="transactionHash" />
        </node>
        <node concept="NX1gA" id="3zgy61ElU9E" role="1GBnQ6">
          <property role="NX6R2" value="Activation code" />
          <ref role="NX6Kv" node="3zgy61ElTeQ" resolve="code" />
        </node>
      </node>
      <node concept="2m6fVq" id="3zgy61Em9Ob" role="2m5mJr">
        <property role="TrG5h" value="GetOAuth2Params" />
        <node concept="NXeRC" id="3zgy61Emcoc" role="1GBnQ6">
          <property role="NXePf" value="Loading OAuth2 Parameters" />
        </node>
        <node concept="NX1gA" id="3zgy61Emcoi" role="1GBnQ6">
          <property role="NX6R2" value="Hash of transaction" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="3zgy61EmbiL" resolve="transactionHash" />
        </node>
        <node concept="NX1gA" id="3zgy61Emcoq" role="1GBnQ6">
          <property role="NX6R2" value="Redirect URL for Application" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61EmamG" resolve="redirectUrl" />
        </node>
        <node concept="2m7Kf5" id="3zgy61EmbiL" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="transactionHash" />
          <node concept="2m5ndX" id="3zgy61EmbiR" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61EmamG" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="redirectUrl" />
          <node concept="2m5ndX" id="3zgy61EmamK" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3zgy61Em9Oc" role="NuuwV">
          <property role="Nu42W" value="C2" />
        </node>
        <node concept="2m1R6W" id="3zgy61Em9Sr" role="2m6efq">
          <node concept="NX1gA" id="3zgy61EmcQW" role="1y2DgH">
            <property role="NX6R2" value="Authentication url" />
            <ref role="NX6Kv" node="3zgy61Em9Sy" resolve="authUrl" />
          </node>
          <node concept="2m7Kf5" id="3zgy61Em9Sy" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="authUrl" />
            <node concept="2m5ndX" id="3zgy61Em9SA" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="3zgy61Em9Ss" role="NuuwV">
            <property role="Nu42W" value="C3" />
          </node>
        </node>
        <node concept="2uC4CA" id="3zgy61EmdNj" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="EXPIRED" />
          <property role="2uCiSL" value="Transaction expired" />
        </node>
      </node>
      <node concept="2m6fVq" id="3zgy61EmbPd" role="2m5mJr">
        <property role="TrG5h" value="CompleteOAuth2" />
        <node concept="NXeRC" id="3zgy61Emeh$" role="1GBnQ6">
          <property role="NXePf" value="Complete OAuth2 Authentication" />
        </node>
        <node concept="NX1gA" id="3zgy61EmehE" role="1GBnQ6">
          <property role="NX6R2" value="Hash of transaction" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="3zgy61EmbTB" resolve="transactionHash" />
        </node>
        <node concept="NX1gA" id="3zgy61EmehM" role="1GBnQ6">
          <property role="1GSvIU" value="danger" />
          <property role="NX6R2" value="Authentication Code" />
          <ref role="NX6Kv" node="3zgy61EmbTI" resolve="code" />
        </node>
        <node concept="2m7Kf5" id="3zgy61EmbTB" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="transactionHash" />
          <node concept="2m5ndX" id="3zgy61EmbTF" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61EmbTI" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="code" />
          <node concept="2m5ndX" id="3zgy61EmbTO" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3zgy61EmbPe" role="NuuwV">
          <property role="Nu42W" value="C4" />
        </node>
        <node concept="2m1Rp1" id="3zgy61EmbTX" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="2uC4CA" id="3zgy61EmeK5" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="EXPIRED" />
          <property role="2uCiSL" value="Transaction expired" />
        </node>
        <node concept="2uC4CA" id="3zgy61Emfem" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="UNOCCUPIED" />
          <property role="2uCiSL" value="Signup required" />
        </node>
      </node>
      <node concept="2m6fVq" id="3zgy61ElXbk" role="2m5mJr">
        <property role="TrG5h" value="SignUp" />
        <node concept="NXeRC" id="3zgy61ElZ4c" role="1GBnQ6">
          <property role="NXePf" value="Perform user SignUp" />
        </node>
        <node concept="NX1gA" id="3zgy61ElZxM" role="1GBnQ6">
          <property role="NX6R2" value="Hash of transaction" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="3zgy61ElZ4f" resolve="transactionHash" />
        </node>
        <node concept="NX1gA" id="3zgy61ElZxU" role="1GBnQ6">
          <property role="NX6R2" value="User name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61ElY9j" resolve="name" />
        </node>
        <node concept="NX1gA" id="3zgy61ElZy4" role="1GBnQ6">
          <property role="NX6R2" value="Optional user sex" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61ElYAE" resolve="sex" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElZ4f" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="transactionHash" />
          <node concept="2m5ndX" id="3zgy61ElZ4o" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElY9j" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="3zgy61ElY9n" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zgy61ElYAE" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="sex" />
          <node concept="2m5nlT" id="3zgy61ElYAK" role="2m7DVh">
            <node concept="3GJkcs" id="3zgy61ElYAQ" role="3GH5xg">
              <ref role="3GJkik" node="GBscvB$$Gr" resolve="Sex" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="3zgy61ElXbl" role="NuuwV">
          <property role="Nu42W" value="BE" />
        </node>
        <node concept="2m1Rp1" id="3zgy61Em37H" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="2uC4CA" id="3zgy61ElXG5" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="EXPIRED" />
          <property role="2uCiSL" value="Transaction is expired" />
        </node>
      </node>
      <node concept="2m62dX" id="GBscvBB6Vl" role="2m5mJr">
        <property role="TrG5h" value="Auth" />
        <node concept="NXeRC" id="3m8vlV8pAoI" role="NXp4Y">
          <property role="NXePf" value="Authentication result" />
        </node>
        <node concept="NX1gA" id="3m8vlV8pALZ" role="NXp4Y">
          <property role="NX6R2" value="The authenticated User" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBB6We" resolve="user" />
        </node>
        <node concept="NX1gA" id="3m8vlV8pAM9" role="NXp4Y">
          <property role="NX6R2" value="Current config of server" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6Wn" resolve="config" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6We" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="user" />
          <node concept="2m5mGg" id="GBscvBB6Wk" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBB6Wn" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="config" />
          <node concept="2m5mGg" id="GBscvBBeah" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6Cp" resolve="Config" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBB6Vm" role="NuuwV">
          <property role="Nu42W" value="05" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBahz" role="2m5mJr">
        <property role="TrG5h" value="AuthSession" />
        <node concept="NXeRC" id="EUEXKTlWk7" role="NXodf">
          <property role="NXePf" value="Authentication session" />
        </node>
        <node concept="NX1gA" id="EUEXKTlG40" role="NXodf">
          <property role="NX6R2" value="Unuque ID of session" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBam$" resolve="id" />
        </node>
        <node concept="NX1gA" id="EUEXKTlSPG" role="NXodf">
          <property role="NX6R2" value="holder of session. 0 - this device, 1 - other." />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBatH" resolve="authHolder" />
        </node>
        <node concept="NX1gA" id="EUEXKTlUSN" role="NXodf">
          <property role="NX6R2" value="Application Id (user in SignIn/SignUp)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBaG2" resolve="appId" />
        </node>
        <node concept="NX1gA" id="EUEXKTlVg3" role="NXodf">
          <property role="NX6R2" value="Title of application" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBaNn" resolve="appTitle" />
        </node>
        <node concept="NX1gA" id="EUEXKTlVgf" role="NXodf">
          <property role="NX6R2" value="Title of device" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBaUM" resolve="deviceTitle" />
        </node>
        <node concept="NX1gA" id="EUEXKTlVA4" role="NXodf">
          <property role="NX6R2" value="Time of session creating" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBb2j" resolve="authTime" />
        </node>
        <node concept="NX1gA" id="EUEXKTlVAk" role="NXodf">
          <property role="NX6R2" value="two-letter country code of session create" />
          <ref role="NX6Kv" node="GBscvBBbd_" resolve="authLocation" />
        </node>
        <node concept="NX1gA" id="EUEXKTlVAA" role="NXodf">
          <property role="NX6R2" value="optional latitude of auth if available" />
          <ref role="NX6Kv" node="GBscvBBboZ" resolve="latitude" />
        </node>
        <node concept="NX1gA" id="EUEXKTlVAU" role="NXodf">
          <property role="NX6R2" value="optional longitude of auth if available" />
          <ref role="NX6Kv" node="GBscvBBb$1" resolve="longitude" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBam$" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="GBscvBBaq9" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBatH" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="authHolder" />
          <node concept="3GJkcs" id="4zDDY4EReV_" role="2m7DVh">
            <ref role="3GJkik" node="4zDDY4EReoE" resolve="AuthHolder" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBaG2" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="GBscvBBaJJ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBaNn" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="appTitle" />
          <node concept="2m5ndX" id="GBscvBBaR8" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBaUM" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="GBscvBBaYB" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBb2j" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="authTime" />
          <node concept="2m5ndE" id="GBscvBBb6c" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBbd_" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="authLocation" />
          <node concept="2m5ndX" id="GBscvBBbhy" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBboZ" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="latitude" />
          <node concept="2m5nlT" id="GBscvBBbw8" role="2m7DVh">
            <node concept="3GIWu7" id="GBscvBBbwe" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBb$1" role="2m0hLx">
          <property role="2m7DUN" value="9" />
          <property role="TrG5h" value="longitude" />
          <node concept="2m5nlT" id="GBscvBBb$m" role="2m7DVh">
            <node concept="3GIWu7" id="GBscvBBb$s" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="2m488m" id="4zDDY4EReoE" role="2m5mJr">
        <property role="TrG5h" value="AuthHolder" />
        <node concept="2m7y0F" id="4zDDY4EReoG" role="2m7ymf">
          <property role="TrG5h" value="ThisDevice" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="4zDDY4ERerx" role="2m7ymf">
          <property role="TrG5h" value="OtherDevice" />
          <property role="2m7y0m" value="2" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBbTl" role="2m5mJr">
        <property role="TrG5h" value="GetAuthSessions" />
        <node concept="NXeRC" id="4zDDY4EPCGB" role="1GBnQ6">
          <property role="NXePf" value="Getting of all active user's authentication sessions" />
        </node>
        <node concept="Nu42z" id="GBscvBBbTm" role="NuuwV">
          <property role="Nu42W" value="50" />
        </node>
        <node concept="2m1R6W" id="GBscvBBc2X" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4EVlvZ" role="1y2DgH">
            <property role="NXePf" value="Current Auth sessions" />
          </node>
          <node concept="NX1gA" id="4zDDY4EVlw5" role="1y2DgH">
            <property role="NX6R2" value="User authentications" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBBcaK" resolve="userAuths" />
          </node>
          <node concept="2m7Kf5" id="GBscvBBcaK" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="userAuths" />
            <node concept="2m5nlk" id="GBscvBBcaO" role="2m7DVh">
              <node concept="2m5mGg" id="GBscvBBceO" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBBahz" resolve="AuthSession" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="GBscvBBc2Y" role="NuuwV">
            <property role="Nu42W" value="51" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBcsy" role="2m5mJr">
        <property role="TrG5h" value="TerminateSession" />
        <node concept="2m7Kf5" id="GBscvBBcQz" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="GBscvBBcQB" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBcsz" role="NuuwV">
          <property role="Nu42W" value="52" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBcEs" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAi" role="1GBnQ6">
          <property role="NXePf" value="SignOut on specified user's session" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAj" role="1GBnQ6">
          <property role="NX6R2" value="id from AuthItem" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBcQz" resolve="id" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBcKt" role="2m5mJr">
        <property role="TrG5h" value="TerminateAllSessions" />
        <node concept="Nu42z" id="GBscvBBcKu" role="NuuwV">
          <property role="Nu42W" value="53" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBd6M" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_A" role="1GBnQ6">
          <property role="NXePf" value="SignOut on all exept current sessions" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBdcW" role="2m5mJr">
        <property role="TrG5h" value="SignOut" />
        <node concept="Nu42z" id="GBscvBBdcX" role="NuuwV">
          <property role="Nu42W" value="54" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBdjc" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBy" role="1GBnQ6">
          <property role="NXePf" value="SignOut current session" />
        </node>
      </node>
      <node concept="NvWBy" id="3zgy61Em1Uf" role="2m5mJr">
        <property role="NvWrd" value="OBSOLETE METHODS" />
      </node>
      <node concept="2m6fVq" id="GBscvBB6QQ" role="2m5mJr">
        <property role="TrG5h" value="SignInObsolete" />
        <node concept="2uC4CA" id="3zc4oYArRGU" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_NUMBER_UNOCCUPIED" />
          <property role="2uCiSL" value="Signup required" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArRGX" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_CODE_INVALID" />
          <property role="2uCiSL" value="Activation code invalid" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArRH1" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_CODE_EXPIRED" />
          <property role="2uCiSL" value="Activation code expired" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArRH6" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_CODE_EMPTY" />
          <property role="2uCiSL" value="Activation code empty" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArRHc" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_NUMBER_INVALID" />
          <property role="2uCiSL" value="Phone number invalid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6Ro" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="phoneNumber" />
          <node concept="2m5ndQ" id="GBscvBB6Rs" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6Rv" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="smsHash" />
          <node concept="2m5ndX" id="GBscvBB6R_" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6RC" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="smsCode" />
          <node concept="2m5ndX" id="GBscvBB6RK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6S0" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="deviceHash" />
          <node concept="2m61tm" id="GBscvBB6Sc" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6Sf" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="GBscvBB6St" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6Sw" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="GBscvBB6SK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6SN" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="appKey" />
          <node concept="2m5ndX" id="GBscvBB6T5" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBB6QR" role="NuuwV">
          <property role="Nu42W" value="03" />
        </node>
        <node concept="2m1Rp1" id="GBscvBB742" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBA" role="1GBnQ6">
          <property role="NXePf" value="Performing user signin" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBB" role="1GBnQ6">
          <property role="NX6R2" value="Phone number in international format" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB6Ro" resolve="phoneNumber" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBC" role="1GBnQ6">
          <property role="NX6R2" value="Code request hash from RequestAuthCode" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB6Rv" resolve="smsHash" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBD" role="1GBnQ6">
          <property role="NX6R2" value="Confirmation code from SMS" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB6RC" resolve="smsCode" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBF" role="1GBnQ6">
          <property role="NX6R2" value="Hash of device unique id and app bundle id. Used for autologout users when app is reinstalled" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6S0" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBG" role="1GBnQ6">
          <property role="NX6R2" value="Device title like 'Steven's iPhone'" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6Sf" resolve="deviceTitle" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBH" role="1GBnQ6">
          <property role="NX6R2" value="Application ID" />
          <ref role="NX6Kv" node="GBscvBB6Sw" resolve="appId" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBI" role="1GBnQ6">
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="GBscvBB6SN" resolve="appKey" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBB77K" role="2m5mJr">
        <property role="TrG5h" value="SignUpObsolete" />
        <node concept="2uC4CA" id="3zc4oYArSmK" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_CODE_EXPIRED" />
          <property role="2uCiSL" value="Activation code expired" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArSmN" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_CODE_EMPTY" />
          <property role="2uCiSL" value="Activation code empty" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArSmR" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_NUMBER_INVALID" />
          <property role="2uCiSL" value="Phone number invalid" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArSmW" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="NAME_INVALID" />
          <property role="2uCiSL" value="Name is invalid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB78H" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="phoneNumber" />
          <property role="toYog" value="false" />
          <node concept="2m5ndQ" id="GBscvBB78L" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB78O" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="smsHash" />
          <node concept="2m5ndX" id="GBscvBB78U" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB78X" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="smsCode" />
          <node concept="2m5ndX" id="GBscvBB795" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB798" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="GBscvBB79i" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB79$" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="deviceHash" />
          <node concept="2m61tm" id="GBscvBB79M" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB79P" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="GBscvBB7a5" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB7a8" role="2m0hLx">
          <property role="2m7DUN" value="9" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="GBscvBB7aq" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB7at" role="2m0hLx">
          <property role="2m7DUN" value="10" />
          <property role="TrG5h" value="appKey" />
          <node concept="2m5ndX" id="GBscvBB7aL" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB7aO" role="2m0hLx">
          <property role="2m7DUN" value="11" />
          <property role="TrG5h" value="isSilent" />
          <node concept="2m5ndN" id="GBscvBB7ba" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBB77L" role="NuuwV">
          <property role="Nu42W" value="04" />
        </node>
        <node concept="2m1Rp1" id="GBscvBB7bd" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBk" role="1GBnQ6">
          <property role="NXePf" value="Performing user signup. If user perform signup on already registered user it just override previous" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBl" role="1GBnQ6">
          <property role="NXePf" value="profile information" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBm" role="1GBnQ6">
          <property role="NX6R2" value="Phone number in international format" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB78H" resolve="phoneNumber" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBn" role="1GBnQ6">
          <property role="NX6R2" value="Code request hash from RequestAuthCode" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB78O" resolve="smsHash" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBo" role="1GBnQ6">
          <property role="NX6R2" value="Confirmation code from SMS" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB78X" resolve="smsCode" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBp" role="1GBnQ6">
          <property role="NX6R2" value="User name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB798" resolve="name" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBr" role="1GBnQ6">
          <property role="NX6R2" value="Hash of device unique id and app bundle id. Used for autologout users when app is reinstalled" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB79$" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBs" role="1GBnQ6">
          <property role="NX6R2" value="Device title like 'Steven's iPhone'" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB79P" resolve="deviceTitle" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBt" role="1GBnQ6">
          <property role="NX6R2" value="Application ID" />
          <ref role="NX6Kv" node="GBscvBB7a8" resolve="appId" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBu" role="1GBnQ6">
          <property role="NX6R2" value="pplication API key" />
          <ref role="NX6Kv" node="GBscvBB7at" resolve="appKey" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBB6EE" role="2m5mJr">
        <property role="TrG5h" value="SendAuthCodeObsolete" />
        <node concept="2uC4CA" id="3zc4oYArGMn" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_NUMBER_INVALID" />
          <property role="2uCiSL" value="Throws when phone number is invalid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6EK" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="phoneNumber" />
          <node concept="2m5ndQ" id="GBscvBB6EO" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6ER" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="GBscvBB6EX" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6F0" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="apiKey" />
          <node concept="2m5ndX" id="GBscvBB6F8" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBB6EF" role="NuuwV">
          <property role="Nu42W" value="01" />
        </node>
        <node concept="2m1R6W" id="GBscvBB6Fb" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4ETN$x" role="1y2DgH">
            <property role="NXePf" value="Sms Request response" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUkal" role="1y2DgH">
            <property role="NX6R2" value="Hash of SMS request" />
            <property role="1GSvIU" value="danger" />
            <ref role="NX6Kv" node="GBscvBB6Fg" resolve="smsHash" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUkat" role="1y2DgH">
            <property role="NX6R2" value="true if user is registered" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="GBscvBB6Fn" resolve="isRegistered" />
          </node>
          <node concept="2m7Kf5" id="GBscvBB6Fg" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="smsHash" />
            <node concept="2m5ndX" id="GBscvBB6Fk" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="GBscvBB6Fn" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="isRegistered" />
            <node concept="2m5ndN" id="GBscvBB6Ft" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="GBscvBB6Fc" role="NuuwV">
            <property role="Nu42W" value="02" />
          </node>
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_S" role="1GBnQ6">
          <property role="NXePf" value="Sending SMS with activation code" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERiBH" role="1GBnQ6">
          <property role="NX6R2" value="Phone number in international format" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6EK" resolve="phoneNumber" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERiBI" role="1GBnQ6">
          <property role="NX6R2" value="Application ID" />
          <ref role="NX6Kv" node="GBscvBB6ER" resolve="appId" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERiBJ" role="1GBnQ6">
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="GBscvBB6F0" resolve="apiKey" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBB6MZ" role="2m5mJr">
        <property role="TrG5h" value="SendAuthCallObsolete" />
        <node concept="2m7Kf5" id="GBscvBB6Nm" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="phoneNumber" />
          <node concept="2m5ndQ" id="GBscvBB6Nq" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6Nt" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="smsHash" />
          <node concept="2m5ndX" id="GBscvBB6Nz" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6NA" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="GBscvBB6NI" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6NL" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="apiKey" />
          <node concept="2m5ndX" id="GBscvBB6NV" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBB6N0" role="NuuwV">
          <property role="Nu42W" value="5a" />
        </node>
        <node concept="2m1Rp1" id="GBscvBB6NY" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArH6Q" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_CODE_EXPIRED" />
          <property role="2uCiSL" value="Code expired" />
        </node>
        <node concept="2uC4CA" id="3zc4oYArR3Z" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="PHONE_NUMVER_INVALID" />
          <property role="2uCiSL" value="Phone number invalid" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_D" role="1GBnQ6">
          <property role="NXePf" value="Requesting Phone activation" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_E" role="1GBnQ6">
          <property role="NX6R2" value="Phone number in international format" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6Nm" resolve="phoneNumber" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_F" role="1GBnQ6">
          <property role="NX6R2" value="Code request hash from RequestAuthCode" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB6Nt" resolve="smsHash" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_G" role="1GBnQ6">
          <property role="NX6R2" value="Application ID" />
          <ref role="NX6Kv" node="GBscvBB6NA" resolve="appId" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_H" role="1GBnQ6">
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="GBscvBB6NL" resolve="apiKey" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvB$$DZ" role="2m5lHt">
      <property role="TrG5h" value="Users" />
      <property role="3XOG$Z" value="users" />
      <node concept="1Dx9M1" id="2uPas5ecStJ" role="1Dx9rD">
        <property role="1Dx9K7" value="Users are objects that secured by accessHash. You can't load user profile by it's id." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecSYv" role="1Dx9rD">
        <property role="1Dx9K7" value="You can't send message to user without finding it's object in Updates or by calling" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecTeU" role="1Dx9rD">
        <property role="1Dx9K7" value="method for user search, contacts import or some other methods." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecU09" role="1Dx9rD">
        <property role="1Dx9K7" value=" " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecU0e" role="1Dx9rD">
        <property role="1Dx9K7" value="Applications need to keep all Users information forever." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecUL_" role="1Dx9rD">
        <property role="1Dx9K7" value=" " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecULU" role="1Dx9rD">
        <property role="1Dx9K7" value="Each User have optional localName - name of user that was set by current user and can be changed" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecV2v" role="1Dx9rD">
        <property role="1Dx9K7" value="any time by calling EditUserLocalName method." />
      </node>
      <node concept="2m488m" id="GBscvB$$Gr" role="2m5mJr">
        <property role="TrG5h" value="Sex" />
        <node concept="2m7y0F" id="GBscvB$$Gt" role="2m7ymf">
          <property role="TrG5h" value="Unknown" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="GBscvB$$G_" role="2m7ymf">
          <property role="TrG5h" value="Male" />
          <property role="2m7y0m" value="2" />
        </node>
        <node concept="2m7y0F" id="GBscvB$$GH" role="2m7ymf">
          <property role="TrG5h" value="Female" />
          <property role="2m7y0m" value="3" />
        </node>
      </node>
      <node concept="2m488m" id="2tyCW$TVx9j" role="2m5mJr">
        <property role="TrG5h" value="ContactType" />
        <node concept="2m7y0F" id="2tyCW$TVx9l" role="2m7ymf">
          <property role="TrG5h" value="Phone" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="2tyCW$TVxc_" role="2m7ymf">
          <property role="TrG5h" value="Email" />
          <property role="2m7y0m" value="2" />
        </node>
      </node>
      <node concept="2m5naR" id="2tyCW$TVx2J" role="2m5mJr">
        <property role="TrG5h" value="ContactRecord" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="2tyCW$TVyXd" role="NXodf">
          <property role="NXePf" value="Contact information record" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVyXj" role="NXodf">
          <property role="NX6R2" value="Record type" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$TVx5V" resolve="type" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVyXr" role="NXodf">
          <property role="NX6R2" value="String value of record" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$TVxCx" resolve="stringValue" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVyX_" role="NXodf">
          <property role="NX6R2" value="Long value of record" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$TVxCK" resolve="longValue" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVyXL" role="NXodf">
          <property role="NX6R2" value="Title of record" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$TVy4N" resolve="title" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVzpV" role="NXodf">
          <property role="NX6R2" value="Subtitle of record" />
          <ref role="NX6Kv" node="2tyCW$TVywW" resolve="subtitle" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$TVx5V" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="type" />
          <node concept="3GJkcs" id="2tyCW$TVxCu" role="2m7DVh">
            <ref role="3GJkik" node="2tyCW$TVx9j" resolve="ContactType" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$TVxCx" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="stringValue" />
          <node concept="2m5nlT" id="2tyCW$TVxCB" role="2m7DVh">
            <node concept="2m5ndX" id="2tyCW$TVxCH" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$TVxCK" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="longValue" />
          <node concept="2m5nlT" id="2tyCW$TVxCT" role="2m7DVh">
            <node concept="2m5ndQ" id="2tyCW$TVxCZ" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$TVy4N" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="title" />
          <node concept="2m5nlT" id="2tyCW$TVy4Z" role="2m7DVh">
            <node concept="2m5ndX" id="2tyCW$TVy55" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$TVywW" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="subtitle" />
          <node concept="2m5nlT" id="2tyCW$TVyxb" role="2m7DVh">
            <node concept="2m5ndX" id="2tyCW$TVyxh" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBAzbH" role="2m5mJr">
        <property role="TrG5h" value="User" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="EUEXKTmbn1" role="NXodf">
          <property role="NXePf" value="Main user object" />
        </node>
        <node concept="NX1gA" id="EUEXKTmc3r" role="NXodf">
          <property role="NX6R2" value="uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBAzcC" resolve="id" />
        </node>
        <node concept="NX1gA" id="EUEXKTmcJH" role="NXodf">
          <property role="NX6R2" value="user's access hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBAzcJ" resolve="accessHash" />
        </node>
        <node concept="NX1gA" id="EUEXKTmds3" role="NXodf">
          <property role="NX6R2" value="user's name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBAzcS" resolve="name" />
        </node>
        <node concept="NX1gA" id="EUEXKTmfwT" role="NXodf">
          <property role="NX6R2" value="user's local name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBAzd3" resolve="localName" />
        </node>
        <node concept="NX1gA" id="EUEXKTmh_R" role="NXodf">
          <property role="NX6R2" value="optional sex of user" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBAzdm" resolve="sex" />
        </node>
        <node concept="NX1gA" id="EUEXKTmlM8" role="NXodf">
          <property role="NX6R2" value="avatar of user" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBB65K" resolve="avatar" />
        </node>
        <node concept="NX1gA" id="2tyCW$U0W_$" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Obsolete phone number. Need to be set for API &lt;= 1.5, not set for &gt;= 1.5" />
          <ref role="NX6Kv" node="2tyCW$U0UX5" resolve="phone" />
        </node>
        <node concept="NX1gA" id="1ydqyopRLrS" role="NXodf">
          <property role="NX6R2" value="phones of user" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="1ydqyopRJ_W" resolve="contactInfo" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVvhb" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Is user actually bot. By default is false." />
          <ref role="NX6Kv" node="2tyCW$TVuOS" resolve="isBot" />
        </node>
        <node concept="2m7Kf5" id="GBscvBAzcC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="GBscvBAzcG" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBAzcJ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBAzcP" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBAzcS" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="GBscvBAzd0" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBAzd3" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="localName" />
          <node concept="2m5nlT" id="GBscvBAzdd" role="2m7DVh">
            <node concept="2m5ndX" id="GBscvBAzdj" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBAzdm" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="sex" />
          <node concept="2m5nlT" id="GBscvBAzdz" role="2m7DVh">
            <node concept="3GJkcs" id="GBscvBB64f" role="3GH5xg">
              <ref role="3GJkik" node="GBscvB$$Gr" resolve="Sex" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBB65K" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="avatar" />
          <node concept="2m5nlT" id="GBscvBB665" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBB9Cz" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$U0UX5" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="phone" />
          <property role="toYog" value="true" />
          <node concept="2m5nlT" id="2tyCW$U0WZJ" role="2m7DVh">
            <node concept="2m5ndQ" id="2tyCW$U0WZP" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1ydqyopRJ_W" role="2m0hLx">
          <property role="2m7DUN" value="12" />
          <property role="TrG5h" value="contactInfo" />
          <node concept="2m5nlk" id="1ydqyopRJAr" role="2m7DVh">
            <node concept="2m5mGg" id="2tyCW$TV$nT" role="3GJlyp">
              <ref role="2m5mJy" node="2tyCW$TVx2J" resolve="ContactRecord" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$TVuOS" role="2m0hLx">
          <property role="2m7DUN" value="11" />
          <property role="TrG5h" value="isBot" />
          <node concept="2m5nlT" id="2tyCW$U0Uz0" role="2m7DVh">
            <node concept="2m5ndN" id="2tyCW$U0Uz6" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBfq6" role="2m5mJr">
        <property role="TrG5h" value="EditUserLocalName" />
        <node concept="2m7Kf5" id="GBscvBBfqR" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBfqV" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfqY" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBBfr4" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfr7" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="GBscvBBfrf" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBfq7" role="NuuwV">
          <property role="Nu42W" value="60" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBfri" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_q" role="1GBnQ6">
          <property role="NXePf" value="Renaming of user's visible name" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_r" role="1GBnQ6">
          <property role="NX6R2" value="target User's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBfqR" resolve="uid" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_s" role="1GBnQ6">
          <property role="NX6R2" value="User's accessHash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBBfqY" resolve="accessHash" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_t" role="1GBnQ6">
          <property role="NX6R2" value="New user name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBfr7" resolve="name" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBfI5" role="2m5mJr">
        <property role="TrG5h" value="UserAvatarChanged" />
        <node concept="NXeRC" id="EUEXKTlX2J" role="NXp_2">
          <property role="NXePf" value="Update about avatar changed" />
        </node>
        <node concept="NX1gA" id="EUEXKTlXq7" role="NXp_2">
          <property role="NX6R2" value="user's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBfIW" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTlY5F" role="NXp_2">
          <property role="NX6R2" value="user's new avatar" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBfJ3" resolve="avatar" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfIW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBfJ0" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfJ3" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="avatar" />
          <node concept="2m5nlT" id="GBscvBBfJ9" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBfJf" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBfI6" role="NuuwV">
          <property role="Nu42W" value="10" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBfKd" role="2m5mJr">
        <property role="TrG5h" value="UserNameChanged" />
        <node concept="NXeRC" id="EUEXKTlZvS" role="NXp_2">
          <property role="NXePf" value="Update about name changed" />
        </node>
        <node concept="NX1gA" id="EUEXKTlZvY" role="NXp_2">
          <property role="NX6R2" value="user's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBfLb" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTm0bC" role="NXp_2">
          <property role="NX6R2" value="user's name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBfLi" resolve="name" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfLb" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBfLf" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfLi" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="GBscvBBfLo" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBfKe" role="NuuwV">
          <property role="Nu42W" value="20" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBfMs" role="2m5mJr">
        <property role="TrG5h" value="UserLocalNameChanged" />
        <node concept="NXeRC" id="EUEXKTm1eA" role="NXp_2">
          <property role="NXePf" value="Update about local name changed" />
        </node>
        <node concept="NX1gA" id="EUEXKTm1A0" role="NXp_2">
          <property role="NX6R2" value="user's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBfNw" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTm2hK" role="NXp_2">
          <property role="NX6R2" value="new user's local name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBfNB" resolve="localName" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfNw" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBfN$" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBfNB" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="localName" />
          <node concept="2m5nlT" id="GBscvBBfNH" role="2m7DVh">
            <node concept="2m5ndX" id="GBscvBBfNN" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBfMt" role="NuuwV">
          <property role="Nu42W" value="33" />
        </node>
      </node>
      <node concept="NpBTk" id="1ydqyopRK4C" role="2m5mJr">
        <property role="TrG5h" value="UserContactsChanged" />
        <node concept="NXeRC" id="1ydqyopRK6U" role="NXp_2">
          <property role="NXePf" value="Update about contact information change" />
        </node>
        <node concept="NX1gA" id="1ydqyopRKZk" role="NXp_2">
          <property role="NX6R2" value="new phones list" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="1ydqyopRK6n" resolve="contactRecords" />
        </node>
        <node concept="2m7Kf5" id="1ydqyopRK6g" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="1ydqyopRK6k" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="1ydqyopRK6n" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="contactRecords" />
          <node concept="2m5nlk" id="1ydqyopRK6t" role="2m7DVh">
            <node concept="2m5mGg" id="2tyCW$TV_gg" role="3GJlyp">
              <ref role="2m5mJy" node="2tyCW$TVx2J" resolve="ContactRecord" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="1ydqyopRK4D" role="NuuwV">
          <property role="Nu42W" value="86" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBB$xY" role="2m5lHt">
      <property role="TrG5h" value="Profile" />
      <property role="3XOG$Z" value="profile" />
      <node concept="2m6fVq" id="GBscvBB$Kt" role="2m5mJr">
        <property role="TrG5h" value="EditName" />
        <node concept="2m7Kf5" id="GBscvBB$K_" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="GBscvBB$KD" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBB$Ku" role="NuuwV">
          <property role="Nu42W" value="35" />
        </node>
        <node concept="2m1Rp1" id="GBscvBB$KG" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foB0" role="1GBnQ6">
          <property role="NXePf" value="Changing account's name" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB1" role="1GBnQ6">
          <property role="NX6R2" value="New name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB$K_" resolve="name" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBB$KQ" role="2m5mJr">
        <property role="TrG5h" value="EditAvatar" />
        <node concept="2m7Kf5" id="GBscvBB$L3" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="fileLocation" />
          <node concept="2m5mGg" id="GBscvBB$L7" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBB$KR" role="NuuwV">
          <property role="Nu42W" value="1F" />
        </node>
        <node concept="2m1R6W" id="2vxDjoto8b2" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4EUkEK" role="1y2DgH">
            <property role="NXePf" value="Response for change account avatar" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUkEQ" role="1y2DgH">
            <property role="NX6R2" value="New avatar" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2vxDjoto8b7" resolve="avatar" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUkEY" role="1y2DgH">
            <property role="NX6R2" value="Sequence number" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2vxDjoto8be" resolve="seq" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUkF8" role="1y2DgH">
            <property role="NX6R2" value="Sequence state" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="2vxDjoto8bn" resolve="state" />
          </node>
          <node concept="2m7Kf5" id="2vxDjoto8b7" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="avatar" />
            <node concept="2m5mGg" id="2vxDjoto8bb" role="2m7DVh">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
          <node concept="2m7Kf5" id="2vxDjoto8be" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="2vxDjoto8bk" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="2vxDjoto8bn" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="2vxDjoto8bv" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="Nu42z" id="2vxDjoto8b3" role="NuuwV">
            <property role="Nu42W" value="67" />
          </node>
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_x" role="1GBnQ6">
          <property role="NXePf" value="Changing account's avatar" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_y" role="1GBnQ6">
          <property role="NX6R2" value="File Location of uploaded unencrypted avatar" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBB$L3" resolve="fileLocation" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBB_00" role="2m5mJr">
        <property role="TrG5h" value="RemoveAvatar" />
        <node concept="Nu42z" id="GBscvBB_01" role="NuuwV">
          <property role="Nu42W" value="5B" />
        </node>
        <node concept="2m1Rp1" id="GBscvBB_0i" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAp" role="1GBnQ6">
          <property role="NXePf" value="Removing account's avatar" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBBfSD" role="2m5lHt">
      <property role="TrG5h" value="Contacts" />
      <property role="3XOG$Z" value="contacts" />
      <node concept="1Dx9M1" id="2uPas5edoDN" role="1Dx9rD">
        <property role="1Dx9K7" value="Before working with contact list is is useful to import contacts from phone first by calling" />
      </node>
      <node concept="1Dx9M1" id="2uPas5edpeF" role="1Dx9rD">
        <property role="1Dx9K7" value="method ImportContacts#0x07." />
      </node>
      <node concept="1Dx9M1" id="2uPas5edpxa" role="1Dx9rD">
        <property role="1Dx9K7" value=" " />
      </node>
      <node concept="1Dx9M1" id="2uPas5edpxe" role="1Dx9rD">
        <property role="1Dx9K7" value="All phone numbers MUST be preprocessed before import by some library (like libphonenumber)" />
      </node>
      <node concept="1Dx9M1" id="2uPas5edpNL" role="1Dx9rD">
        <property role="1Dx9K7" value="and build international phone number depending on current users phone and/or locale." />
      </node>
      <node concept="1Dx9M1" id="2uPas5edq6m" role="1Dx9rD">
        <property role="1Dx9K7" value=" " />
      </node>
      <node concept="1Dx9M1" id="2uPas5edq6t" role="1Dx9rD">
        <property role="1Dx9K7" value="For loading contact list from server use GetContacts#0x57. " />
      </node>
      <node concept="1Dx9M1" id="2uPas5edqFB" role="1Dx9rD">
        <property role="1Dx9K7" value="If during this call there are some updates about contact list change" />
      </node>
      <node concept="1Dx9M1" id="2uPas5edqYi" role="1Dx9rD">
        <property role="1Dx9K7" value="it is recommended to call it again. Also applications need to sync contacts on application start." />
      </node>
      <node concept="1Dx9M1" id="2uPas5edrhb" role="1Dx9rD">
        <property role="1Dx9K7" value=" " />
      </node>
      <node concept="1Dx9M1" id="2uPas5edqYs" role="1Dx9rD">
        <property role="1Dx9K7" value="For searching for users without adding to contacts list use method FindContacts#0x70." />
      </node>
      <node concept="1Dx9M1" id="2uPas5edr$a" role="1Dx9rD">
        <property role="1Dx9K7" value=" " />
      </node>
      <node concept="1Dx9M1" id="2uPas5edrhn" role="1Dx9rD">
        <property role="1Dx9K7" value="For adding/deleting contacts AddContact#0x72 and DeleteContact#0x59." />
      </node>
      <node concept="2m5naR" id="GBscvBBg2h" role="2m5mJr">
        <property role="TrG5h" value="PhoneToImport" />
        <property role="w4tQU" value="false" />
        <node concept="NXeRC" id="EUEXKTm3kR" role="NXodf">
          <property role="NXePf" value="Phone for import" />
        </node>
        <node concept="NX1gA" id="EUEXKTm3kX" role="NXodf">
          <property role="NX6R2" value="phone number for import in international format" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBg2j" resolve="phoneNumber" />
        </node>
        <node concept="NX1gA" id="EUEXKTm40N" role="NXodf">
          <property role="NX6R2" value="optional name for contact" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBg2q" resolve="name" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBg2j" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="phoneNumber" />
          <node concept="2m5ndQ" id="GBscvBBg2n" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBg2q" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="name" />
          <node concept="2m5nlT" id="GBscvBBg2w" role="2m7DVh">
            <node concept="2m5ndX" id="GBscvBBg2A" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBg2L" role="2m5mJr">
        <property role="TrG5h" value="EmailToImport" />
        <property role="w4tQU" value="false" />
        <node concept="NXeRC" id="EUEXKTm5oq" role="NXodf">
          <property role="NXePf" value="Email for import" />
        </node>
        <node concept="NX1gA" id="EUEXKTm5o$" role="NXodf">
          <property role="NX6R2" value="email for importing" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBg2U" resolve="email" />
        </node>
        <node concept="NX1gA" id="EUEXKTm64w" role="NXodf">
          <property role="NX6R2" value="optional name for contact" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBg31" resolve="name" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBg2U" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="email" />
          <node concept="2m5ndX" id="GBscvBBg2Y" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBg31" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="name" />
          <node concept="2m5nlT" id="GBscvBBg37" role="2m7DVh">
            <node concept="2m5ndX" id="GBscvBBg3d" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBg8u" role="2m5mJr">
        <property role="TrG5h" value="ImportContacts" />
        <node concept="2m7Kf5" id="GBscvBBg8Z" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="phones" />
          <node concept="2m5nlk" id="GBscvBBg93" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBg99" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBBg2h" resolve="PhoneToImport" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBg9c" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="emails" />
          <node concept="2m5nlk" id="GBscvBBg9j" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBg9p" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBBg2L" resolve="EmailToImport" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBg8v" role="NuuwV">
          <property role="Nu42W" value="07" />
        </node>
        <node concept="2m1R6W" id="GBscvBBg9s" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4EUlbw" role="1y2DgH">
            <property role="NXePf" value="Imported contacts" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUlbO" role="1y2DgH">
            <property role="NX6R2" value="Imported users" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBBg9x" resolve="users" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUlbW" role="1y2DgH">
            <property role="NX6R2" value="Sequence number if users are imported" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="GBscvBBg9I" resolve="seq" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUlc6" role="1y2DgH">
            <property role="NX6R2" value="Sequence state if users are imported" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBBg9S" resolve="state" />
          </node>
          <node concept="2m7Kf5" id="GBscvBBg9x" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="GBscvBBg9_" role="2m7DVh">
              <node concept="2m5mGg" id="3m8vlV8pGbB" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBBg9I" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="GBscvBBg9P" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="GBscvBBg9S" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="55bmeIQ7_wG" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="Nu42z" id="GBscvBBg9t" role="NuuwV">
            <property role="Nu42W" value="08" />
          </node>
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBM" role="1GBnQ6">
          <property role="NXePf" value="Importing phones and emails for building contact list" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBN" role="1GBnQ6">
          <property role="NXePf" value="Maximum amount of items for import per method call equals to 100." />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBO" role="1GBnQ6">
          <property role="NX6R2" value="Phones for import" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBg8Z" resolve="phones" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBP" role="1GBnQ6">
          <property role="NX6R2" value="Emails for import" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBg9c" resolve="emails" />
        </node>
      </node>
      <node concept="NvyAe" id="GBscvBBgtW" role="2m5mJr" />
      <node concept="2m6fVq" id="GBscvBBgv0" role="2m5mJr">
        <property role="TrG5h" value="GetContacts" />
        <node concept="2m7Kf5" id="GBscvBBgvC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="contactsHash" />
          <node concept="2m5ndX" id="GBscvBBgvG" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBgv1" role="NuuwV">
          <property role="Nu42W" value="57" />
        </node>
        <node concept="2m1R6W" id="GBscvBBgvJ" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4EUmcQ" role="1y2DgH">
            <property role="NXePf" value="Current contact list" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUmcW" role="1y2DgH">
            <property role="NX6R2" value="User list if list is changed" />
            <ref role="NX6Kv" node="GBscvBBgvO" resolve="users" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUmd4" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="is list changed" />
            <ref role="NX6Kv" node="GBscvBBgw1" resolve="isNotChanged" />
          </node>
          <node concept="2m7Kf5" id="GBscvBBgvO" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="GBscvBBgvS" role="2m7DVh">
              <node concept="2m5mGg" id="3m8vlV8pGb$" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBBgw1" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="isNotChanged" />
            <node concept="2m5ndN" id="GBscvBBgw8" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="GBscvBBgvK" role="NuuwV">
            <property role="Nu42W" value="58" />
          </node>
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAF" role="1GBnQ6">
          <property role="NXePf" value="Getting current contact list" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAG" role="1GBnQ6">
          <property role="NXePf" value="SHA256 hash of list of a comma-separated list of contact UIDs in ascending " />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAH" role="1GBnQ6">
          <property role="NXePf" value="order may be passed in contactsHash parameter. " />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAI" role="1GBnQ6">
          <property role="NXePf" value="If the contact list was not changed, isNotChanged will be true." />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAJ" role="1GBnQ6">
          <property role="NX6R2" value="Hash of saved list in application" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBgvC" resolve="contactsHash" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBgx0" role="2m5mJr">
        <property role="TrG5h" value="RemoveContact" />
        <node concept="2m7Kf5" id="GBscvBBgxN" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBgxR" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBgxU" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBBgy0" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBgx1" role="NuuwV">
          <property role="Nu42W" value="59" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBgy3" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAU" role="1GBnQ6">
          <property role="NXePf" value="Removing contact from contact list" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAV" role="1GBnQ6">
          <property role="NX6R2" value="Contact's UID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBgxN" resolve="uid" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAW" role="1GBnQ6">
          <property role="NX6R2" value="Contact's AccessHash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBBgxU" resolve="accessHash" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBgyU" role="2m5mJr">
        <property role="TrG5h" value="AddContact" />
        <node concept="2m7Kf5" id="GBscvBBgzO" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBgzS" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBgzV" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBBg$1" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBgyV" role="NuuwV">
          <property role="Nu42W" value="72" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBg$4" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAa" role="1GBnQ6">
          <property role="NXePf" value="Adding contact to contact list" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAb" role="1GBnQ6">
          <property role="NX6R2" value="Contact's UID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBgzO" resolve="uid" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAc" role="1GBnQ6">
          <property role="NX6R2" value="Contact's AccessHash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBBgzV" resolve="accessHash" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBgEJ" role="2m5mJr">
        <property role="TrG5h" value="SearchContacts" />
        <node concept="NXeRC" id="2EAJ7H6fo_u" role="1GBnQ6">
          <property role="NXePf" value="Searching contacts by user's query" />
        </node>
        <node concept="NX1gA" id="4zDDY4EQWYi" role="1GBnQ6">
          <property role="NX6R2" value="Search query" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBgFK" resolve="request" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBgFK" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="request" />
          <node concept="2m5ndX" id="GBscvBBgFO" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBgEK" role="NuuwV">
          <property role="Nu42W" value="70" />
        </node>
        <node concept="2m1R6W" id="GBscvBBgFR" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4EUndT" role="1y2DgH">
            <property role="NXePf" value="Founded users" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUndZ" role="1y2DgH">
            <property role="NX6R2" value="Founded users" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBBgFW" resolve="users" />
          </node>
          <node concept="2m7Kf5" id="GBscvBBgFW" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="GBscvBBgG0" role="2m7DVh">
              <node concept="2m5mGg" id="3m8vlV8pGbx" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="GBscvBBgFS" role="NuuwV">
            <property role="Nu42W" value="71" />
          </node>
        </node>
      </node>
      <node concept="NvyAe" id="GBscvBBh1m" role="2m5mJr" />
      <node concept="NpBTk" id="GBscvBBh2s" role="2m5mJr">
        <property role="TrG5h" value="ContactRegistered" />
        <node concept="NXeRC" id="EUEXKTm77T" role="NXp_2">
          <property role="NXePf" value="Update about contact registration" />
        </node>
        <node concept="NX1gA" id="EUEXKTm8RA" role="NXp_2">
          <property role="NX6R2" value="contact's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBh9t" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTm77Z" role="NXp_2">
          <property role="NX6R2" value="is registration silent. If this value is true then don't show notification about registration" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBh9$" resolve="isSilent" />
        </node>
        <node concept="NX1gA" id="EUEXKTm7O1" role="NXp_2">
          <property role="NX6R2" value="date of registration" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBh9H" resolve="date" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBh9t" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBh9x" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBh9$" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="isSilent" />
          <node concept="2m5ndN" id="GBscvBBh9E" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBh9H" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotnO$1" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSmuIP" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="gbd4oSmuIZ" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBh2t" role="NuuwV">
          <property role="Nu42W" value="05" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBhb5" role="2m5mJr">
        <property role="TrG5h" value="ContactsAdded" />
        <node concept="NXeRC" id="EUEXKTm9Vc" role="NXp_2">
          <property role="NXePf" value="Update about contacts added" />
        </node>
        <node concept="NX1gA" id="EUEXKTm9Vi" role="NXp_2">
          <property role="NX6R2" value="added contacts" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBhcl" resolve="uids" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBhcl" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uids" />
          <node concept="2m5nlk" id="GBscvBBhcp" role="2m7DVh">
            <node concept="2m5ndE" id="GBscvBBhcv" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBhb6" role="NuuwV">
          <property role="Nu42W" value="28" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBhdO" role="2m5mJr">
        <property role="TrG5h" value="ContactsRemoved" />
        <node concept="2m7Kf5" id="GBscvBBhf9" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uids" />
          <node concept="2m5nlk" id="GBscvBBhfd" role="2m7DVh">
            <node concept="2m5ndE" id="GBscvBBhfj" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBhdP" role="NuuwV">
          <property role="Nu42W" value="29" />
        </node>
        <node concept="NXeRC" id="EUEXKTmaiY" role="NXp_2">
          <property role="NXePf" value="Update about contacts removed" />
        </node>
        <node concept="NX1gA" id="EUEXKTmajb" role="NXp_2">
          <property role="NX6R2" value="removed contacts" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBhf9" resolve="uids" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBBjPC" role="2m5lHt">
      <property role="TrG5h" value="Messaging" />
      <property role="3XOG$Z" value="messaging" />
      <node concept="1Dx9M1" id="2uPas5ecVzB" role="1Dx9rD">
        <property role="1Dx9K7" value="Actor can work with encrypted and plain messages in one conversation. For both types of messages API" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecVOg" role="1Dx9rD">
        <property role="1Dx9K7" value="contains a bit different methods. Also encrypted and plain messages have different schemes." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecVOk" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;h3&gt;Messages&lt;/h3&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecW4V" role="1Dx9rD">
        <property role="1Dx9K7" value="Message entity contains:" />
      </node>
      <node concept="1Dx9M1" id="3zc4oYAr4K2" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;ul&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecWl$" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;PeerType - group chat or private&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecWAf" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;PeerId - group or user id of conversation&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecWQW" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;RandomId - unique id of message that generated by sender. In Encrypted messages random id is encrypted.&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecX7F" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;Date - date of message (calculated on server)&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecXos" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;li&gt;Content&lt;/li&gt;" />
      </node>
      <node concept="1Dx9M1" id="3zc4oYAr4KF" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;/ul&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecXDf" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;h3&gt;Message content&lt;/h3&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecXU4" role="1Dx9rD">
        <property role="1Dx9K7" value="Message can be one of three basic types of messages: Text Message, File Message and Service message." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecYaV" role="1Dx9rD">
        <property role="1Dx9K7" value="All messages can contain extensions. For example we can send text message and add markdown extension with " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecZdP" role="1Dx9rD">
        <property role="1Dx9K7" value="formatted text in markdown and clients that support this extension will show markdown, and that clients that" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecZuK" role="1Dx9rD">
        <property role="1Dx9K7" value="not supported extension then show simple text. File messages can have photo, video or voice extensions." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ecZJH" role="1Dx9rD">
        <property role="1Dx9K7" value="Service message can have extensions extensions such as &quot;user added&quot;, &quot;group created&quot;, &quot;avatar changed&quot;, etc." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed0hq" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;h3&gt;Send messages&lt;/h3&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed0hG" role="1Dx9rD">
        <property role="1Dx9K7" value="Sending messages looks same for encrypted and plain messages. Client MUST prepare all required data" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed0Nv" role="1Dx9rD">
        <property role="1Dx9K7" value="before sending message (for example FastThumb for photo/video/documents) and call required methods. " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed14$" role="1Dx9rD">
        <property role="1Dx9K7" value="Encrypted messages differs here only by a little different scheme and encryption." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed1lF" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;h3&gt;WRONG_KEYS and incorrect keys&lt;/h3&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed1AO" role="1Dx9rD">
        <property role="1Dx9K7" value="For sending encrypted messages client MUST send messages encrypted for all own and receivers keys." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed1RZ" role="1Dx9rD">
        <property role="1Dx9K7" value="If client send encryption with missing, old or incorrect keys it will receive WRONG_KEYS." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed29c" role="1Dx9rD">
        <property role="1Dx9K7" value="In WRONG_KEYS you need to deserialize relatedData from RpcError to WrongKeysErrorData" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed2qr" role="1Dx9rD">
        <property role="1Dx9K7" value="and get detailed information about keys. Sometimes there are some broken keys on server and client can't " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed2Wz" role="1Dx9rD">
        <property role="1Dx9K7" value="encrypt messages with it than client MUST send empty encrypted key in request elsewhere API return WRONG_KEYS." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed3JA" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;h3&gt;Encrypted messages and New Devices&lt;/h3&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed40V" role="1Dx9rD">
        <property role="1Dx9K7" value="When you send message to someone and when he registered with new device there are no way to receive old encrypted" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed4ii" role="1Dx9rD">
        <property role="1Dx9K7" value="messages on new device and because of this there are a problem about read/delivery statuses. " />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed55x" role="1Dx9rD">
        <property role="1Dx9K7" value="Alice send messages to Bob, but Bob lose his device and  buy new iPhone and installed Actor." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed5BS" role="1Dx9rD">
        <property role="1Dx9K7" value="Alice receive notification about new device and send another message. Bob open chat with Alice and" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed6Gc" role="1Dx9rD">
        <property role="1Dx9K7" value="send read status with maximum message read date. Alice will mark all sent messages as read and one that" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed7vB" role="1Dx9rD">
        <property role="1Dx9K7" value="was not delivered. We can use status notifications per message, but in VERY heavy conversations it will be" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed8j6" role="1Dx9rD">
        <property role="1Dx9K7" value="a lot of unnecessary traffic. For resolving this small issue we have different ways of message statuses" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed96D" role="1Dx9rD">
        <property role="1Dx9K7" value="for encrypted and plain messages. Also it is recomended to mark all undelivered messages on new device update as " />
      </node>
      <node concept="1Dx9M1" id="2uPas5edb10" role="1Dx9rD">
        <property role="1Dx9K7" value="not devered with warring sign." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed97d" role="1Dx9rD">
        <property role="1Dx9K7" value="&lt;h3&gt;Message Read and Delivery&lt;/h3&gt;" />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed9DQ" role="1Dx9rD">
        <property role="1Dx9K7" value="There are two different ways for read and delivery statuses for encrypted and plain messages." />
      </node>
      <node concept="1Dx9M1" id="2uPas5ed9Vv" role="1Dx9rD">
        <property role="1Dx9K7" value="For encrypted messages used status change by RandomId and for plain messages used by maximum" />
      </node>
      <node concept="1Dx9M1" id="2uPas5edaue" role="1Dx9rD">
        <property role="1Dx9K7" value="date of read/delivered message." />
      </node>
      <node concept="w93zz" id="55bmeIQey3W" role="2m5mJr">
        <property role="TrG5h" value="Message" />
        <property role="1FaRnq" value="true" />
      </node>
      <node concept="2m5naR" id="GBscvBBkCg" role="2m5mJr">
        <property role="TrG5h" value="TextMessage" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="NXeRC" id="EUEXKTmoqy" role="NXodf">
          <property role="NXePf" value="Text message" />
        </node>
        <node concept="NX1gA" id="EUEXKTmoqC" role="NXodf">
          <property role="NX6R2" value="the text" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBBkKA" resolve="text" />
        </node>
        <node concept="NX1gA" id="2tyCW$U1eS_" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User mentions in message" />
          <ref role="NX6Kv" node="2tyCW$U1etp" resolve="mentions" />
        </node>
        <node concept="NX1gA" id="EUEXKTmpO4" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional bytes of extension" />
          <ref role="NX6Kv" node="GBscvBBkKQ" resolve="ext" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBkKA" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="text" />
          <node concept="2m5ndX" id="GBscvBBkKE" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U1etp" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="mentions" />
          <node concept="2m5nlk" id="2tyCW$U1ety" role="2m7DVh">
            <node concept="2m5ndE" id="2tyCW$U1etC" role="3GJlyp" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBkKQ" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="ext" />
          <node concept="2m5nlT" id="GBscvBBkKY" role="2m7DVh">
            <node concept="3BlaRf" id="5NX0N0RTi3j" role="3GH5xg">
              <ref role="3BrLez" node="5NX0N0RThX2" resolve="TextMessageEx" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="55bmeIQfKZq" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
      </node>
      <node concept="w93zz" id="5NX0N0RThX2" role="2m5mJr">
        <property role="TrG5h" value="TextMessageEx" />
        <property role="1FaRnq" value="true" />
      </node>
      <node concept="2m5naR" id="2tyCW$TVtRM" role="2m5mJr">
        <property role="TrG5h" value="TextExMarkdown" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="5NX0N0RThX2" resolve="TextMessageEx" />
        <node concept="NXeRC" id="2tyCW$TVtXD" role="NXodf">
          <property role="NXePf" value="Markdown extension" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVuph" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Markdown text" />
          <ref role="NX6Kv" node="2tyCW$TVtXw" resolve="markdown" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$TVtXw" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="markdown" />
          <node concept="2m5ndX" id="2tyCW$TVtX$" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2HoLzB7uiEM" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBkLN" role="2m5mJr">
        <property role="TrG5h" value="ServiceMessage" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="NXeRC" id="EUEXKTmqwO" role="NXodf">
          <property role="NXePf" value="Service message" />
        </node>
        <node concept="NX1gA" id="EUEXKTmqwU" role="NXodf">
          <property role="NX6R2" value="service message text" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBkMw" resolve="text" />
        </node>
        <node concept="NX1gA" id="4zDDY4EQZpR" role="NXodf">
          <property role="NX6R2" value="Extension" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBkMK" resolve="ext" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBkMw" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="text" />
          <node concept="2m5ndX" id="GBscvBBkM$" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBkMK" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="ext" />
          <node concept="2m5nlT" id="55bmeIQctJM" role="2m7DVh">
            <node concept="3BlaRf" id="2vxDjotnFmR" role="3GH5xg">
              <ref role="3BrLez" node="55bmeIQ9med" resolve="ServiceEx" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="55bmeIQgoCU" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
      </node>
      <node concept="w93zz" id="55bmeIQ9med" role="2m5mJr">
        <property role="TrG5h" value="ServiceEx" />
        <property role="1FaRnq" value="true" />
      </node>
      <node concept="2m5naR" id="GBscvBBkVP" role="2m5mJr">
        <property role="TrG5h" value="ServiceExUserInvited" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="EUEXKTmu$H" role="NXodf">
          <property role="NXePf" value="Service message about adding user to group" />
        </node>
        <node concept="NX1gA" id="EUEXKTmu$N" role="NXodf">
          <property role="NX6R2" value="added user id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBkWE" resolve="invitedUid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBkWE" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="invitedUid" />
          <node concept="2m5ndE" id="GBscvBBkWI" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="55bmeIQgi$r" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
      </node>
      <node concept="2m5naR" id="1$yIuJFAWzz" role="2m5mJr">
        <property role="TrG5h" value="ServiceExUserJoined" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="1$yIuJFAWDS" role="NXodf">
          <property role="NXePf" value="Service message about user join to group" />
        </node>
        <node concept="Nu42z" id="1$yIuJFAWDO" role="3BtCOu">
          <property role="Nu42W" value="11" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBkXC" role="2m5mJr">
        <property role="TrG5h" value="ServiceExUserKicked" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="EUEXKTmvhF" role="NXodf">
          <property role="NXePf" value="Service message about kicking user from group" />
        </node>
        <node concept="NX1gA" id="EUEXKTmvhL" role="NXodf">
          <property role="NX6R2" value="kicked user id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBkYw" resolve="kickedUid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBkYw" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="kickedUid" />
          <node concept="2m5ndE" id="GBscvBBkY$" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="55bmeIQgjmN" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBkZx" role="2m5mJr">
        <property role="TrG5h" value="ServiceExUserLeft" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="EUEXKTmvhR" role="NXodf">
          <property role="NXePf" value="Service message about user left group" />
        </node>
        <node concept="Nu42z" id="55bmeIQgk9d" role="3BtCOu">
          <property role="Nu42W" value="03" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBl1n" role="2m5mJr">
        <property role="TrG5h" value="ServiceExGroupCreated" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="EUEXKTmvhW" role="NXodf">
          <property role="NXePf" value="Service message about group creating" />
        </node>
        <node concept="Nu42z" id="55bmeIQgk9f" role="3BtCOu">
          <property role="Nu42W" value="04" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBlbn" role="2m5mJr">
        <property role="TrG5h" value="ServiceExChangedTitle" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="EUEXKTmvi1" role="NXodf">
          <property role="NXePf" value="Service message about group title change" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER0Rs" role="NXodf">
          <property role="NX6R2" value="New group title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlck" resolve="title" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlck" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="GBscvBBlco" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="55bmeIQgkVH" role="3BtCOu">
          <property role="Nu42W" value="05" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBldq" role="2m5mJr">
        <property role="TrG5h" value="ServiceExChangedAvatar" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="EUEXKTmvi6" role="NXodf">
          <property role="NXePf" value="Service message about avatar change" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER0Rz" role="NXodf">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Updated avatar" />
          <ref role="NX6Kv" node="GBscvBBleq" resolve="avatar" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBleq" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="avatar" />
          <node concept="2m5nlT" id="GBscvBBleu" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBle$" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="55bmeIQgnrB" role="3BtCOu">
          <property role="Nu42W" value="06" />
        </node>
      </node>
      <node concept="2m5naR" id="2tyCW$TVsJo" role="2m5mJr">
        <property role="TrG5h" value="ServiceExContactRegistered" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="2m7Kf5" id="2tyCW$TVsP8" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="2tyCW$TVsPc" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2tyCW$TVsP6" role="3BtCOu">
          <property role="Nu42W" value="08" />
        </node>
        <node concept="NXeRC" id="2tyCW$TVsPf" role="NXodf">
          <property role="NXePf" value="Service message about user registration" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVsPk" role="NXodf">
          <property role="NX6R2" value="User Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$TVsP8" resolve="uid" />
        </node>
      </node>
      <node concept="2m5naR" id="2tyCW$TWrIZ" role="2m5mJr">
        <property role="TrG5h" value="ServiceExPhoneMissed" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="2tyCW$TWrOH" role="NXodf">
          <property role="NXePf" value="Update about missing phone call" />
        </node>
        <node concept="Nu42z" id="2tyCW$TWrOD" role="3BtCOu">
          <property role="Nu42W" value="09" />
        </node>
      </node>
      <node concept="2m5naR" id="2tyCW$TWski" role="2m5mJr">
        <property role="TrG5h" value="ServiceExPhoneCall" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="2tyCW$TWsO4" role="NXodf">
          <property role="NXePf" value="Update about phone call" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$TWsq1" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="duration" />
          <node concept="2m5ndE" id="2tyCW$TWsq5" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2tyCW$TWspZ" role="3BtCOu">
          <property role="Nu42W" value="10" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBlnT" role="2m5mJr">
        <property role="TrG5h" value="DocumentMessage" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="NXeRC" id="EUEXKTmvEb" role="NXodf">
          <property role="NXePf" value="File message" />
        </node>
        <node concept="NX1gA" id="EUEXKTmvEh" role="NXodf">
          <property role="NX6R2" value="file id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBloX" resolve="fileId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmy80" role="NXodf">
          <property role="NX6R2" value="file access hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBBlp4" resolve="accessHash" />
        </node>
        <node concept="NX1gA" id="EUEXKTmzbK" role="NXodf">
          <property role="NX6R2" value="file size" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlpd" resolve="fileSize" />
        </node>
        <node concept="NX1gA" id="EUEXKTm$A8" role="NXodf">
          <property role="NX6R2" value="name of file" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlpo" resolve="name" />
        </node>
        <node concept="NX1gA" id="EUEXKTmA0A" role="NXodf">
          <property role="NX6R2" value="mimetype of file" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlp_" resolve="mimeType" />
        </node>
        <node concept="NX1gA" id="EUEXKTmAI0" role="NXodf">
          <property role="NX6R2" value="optional thumb of file. JPEG less that 90x90 with 60-70 quality." />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBlpO" resolve="thumb" />
        </node>
        <node concept="NX1gA" id="55bmeIQgt3X" role="NXodf">
          <property role="NX6R2" value="Extension" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBlqv" resolve="ext" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBloX" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="fileId" />
          <node concept="2m5ndQ" id="3zc4oYAoa39" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlp4" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBBlpa" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlpd" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="fileSize" />
          <node concept="2m5ndE" id="GBscvBBlpl" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlpo" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="GBscvBBlpy" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlp_" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="mimeType" />
          <node concept="2m5ndX" id="GBscvBBlpL" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlpO" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="thumb" />
          <node concept="2m5nlT" id="GBscvBBlq2" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBlq8" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvBB67s" resolve="FastThumb" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBlqv" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="ext" />
          <node concept="2m5nlT" id="GBscvBBlqM" role="2m7DVh">
            <node concept="3BlaRf" id="5NX0N0RThlN" role="3GH5xg">
              <ref role="3BrLez" node="55bmeIQ9FSf" resolve="DocumentEx" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="55bmeIQgpQg" role="3BtCOu">
          <property role="Nu42W" value="03" />
        </node>
      </node>
      <node concept="w93zz" id="55bmeIQ9FSf" role="2m5mJr">
        <property role="TrG5h" value="DocumentEx" />
        <property role="1FaRnq" value="true" />
      </node>
      <node concept="2m5naR" id="GBscvBBl$N" role="2m5mJr">
        <property role="TrG5h" value="DocumentExPhoto" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9FSf" resolve="DocumentEx" />
        <node concept="NXeRC" id="EUEXKTmCyv" role="NXodf">
          <property role="NXePf" value="File photo extension" />
        </node>
        <node concept="NX1gA" id="EUEXKTmCy_" role="NXodf">
          <property role="NX6R2" value="image width" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlAa" resolve="w" />
        </node>
        <node concept="NX1gA" id="EUEXKTmDfZ" role="NXodf">
          <property role="NX6R2" value="image height" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlAh" resolve="h" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlAa" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="w" />
          <node concept="2m5ndE" id="GBscvBBlAe" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlAh" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="h" />
          <node concept="2m5ndE" id="GBscvBBlAn" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="55bmeIQgr3D" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBlBP" role="2m5mJr">
        <property role="TrG5h" value="DocumentExVideo" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9FSf" resolve="DocumentEx" />
        <node concept="NXeRC" id="EUEXKTmDg6" role="NXodf">
          <property role="NXePf" value="File video extension" />
        </node>
        <node concept="NX1gA" id="EUEXKTmDgc" role="NXodf">
          <property role="NX6R2" value="video width" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlDh" resolve="w" />
        </node>
        <node concept="NX1gA" id="EUEXKTmDgk" role="NXodf">
          <property role="NX6R2" value="video height" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlDo" resolve="h" />
        </node>
        <node concept="NX1gA" id="EUEXKTmDgu" role="NXodf">
          <property role="NX6R2" value="video duration" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlDx" resolve="duration" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlDh" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="w" />
          <node concept="2m5ndE" id="GBscvBBlDl" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlDo" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="h" />
          <node concept="2m5ndE" id="GBscvBBlDu" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlDx" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="duration" />
          <node concept="2m5ndE" id="GBscvBBlDD" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="55bmeIQgrQh" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBBlFe" role="2m5mJr">
        <property role="TrG5h" value="DocumentExVoice" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9FSf" resolve="DocumentEx" />
        <node concept="NXeRC" id="EUEXKTmDBk" role="NXodf">
          <property role="NXePf" value="File voice extension" />
        </node>
        <node concept="NX1gA" id="EUEXKTmDBq" role="NXodf">
          <property role="NX6R2" value="voice duration" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBlGL" resolve="duration" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBlGL" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="duration" />
          <node concept="2m5ndE" id="GBscvBBlGP" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="55bmeIQgsCV" role="3BtCOu">
          <property role="Nu42W" value="03" />
        </node>
      </node>
      <node concept="2m5naR" id="2tyCW$U5XZ6" role="2m5mJr">
        <property role="TrG5h" value="JsonMessage" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="2m7Kf5" id="2tyCW$U5Y5z" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="rawJson" />
          <node concept="2m5ndX" id="2tyCW$U5Y5B" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="2tyCW$U5Y5n" role="NXodf">
          <property role="NXePf" value="Custom-data JsonMessage" />
        </node>
        <node concept="NX1gA" id="2tyCW$U5YxA" role="NXodf">
          <property role="1GSvIU" value="danger" />
          <property role="NX6R2" value="JSON object" />
          <ref role="NX6Kv" node="2tyCW$U5Y5z" resolve="rawJson" />
        </node>
        <node concept="Nu42z" id="2tyCW$U5Y5j" role="3BtCOu">
          <property role="Nu42W" value="04" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBqzL" role="2m5mJr">
        <property role="TrG5h" value="SendMessage" />
        <node concept="2m7Kf5" id="GBscvBBqBa" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBqBe" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBqBh" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2vxDjotnVtp" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBqBq" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="message" />
          <node concept="3BlaRf" id="5NX0N0RTefZ" role="2m7DVh">
            <ref role="3BrLez" node="55bmeIQey3W" resolve="Message" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBqzM" role="NuuwV">
          <property role="Nu42W" value="5c" />
        </node>
        <node concept="2m1Rp1" id="2vxDjotnUc7" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAO" role="1GBnQ6">
          <property role="NXePf" value="Sending plain message" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAP" role="1GBnQ6">
          <property role="NX6R2" value="Destination peer for message (now supported only user's peer)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBqBa" resolve="peer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAQ" role="1GBnQ6">
          <property role="NX6R2" value="Message random id (generated on clien side)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBqBh" resolve="rid" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAR" role="1GBnQ6">
          <property role="NX6R2" value="The message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBqBq" resolve="message" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBr4E" role="2m5mJr">
        <property role="TrG5h" value="MessageReceived" />
        <node concept="2m7Kf5" id="GBscvBBr8q" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBr8u" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBr8z" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotnPpZ" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBr4F" role="NuuwV">
          <property role="Nu42W" value="37" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBr8G" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAq" role="1GBnQ6">
          <property role="NXePf" value="Confirmation of plain message receive by device" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAr" role="1GBnQ6">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBr8q" resolve="peer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAs" role="1GBnQ6">
          <property role="NX6R2" value="Maximum date of received messages" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBr8z" resolve="date" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBrcw" role="2m5mJr">
        <property role="TrG5h" value="MessageRead" />
        <node concept="2m7Kf5" id="GBscvBBrgn" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBrgr" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBrgu" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotnPq2" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBrcx" role="NuuwV">
          <property role="Nu42W" value="39" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBrgB" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_Z" role="1GBnQ6">
          <property role="NXePf" value="Marking plain messages as read" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foA0" role="1GBnQ6">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBrgn" resolve="peer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foA1" role="1GBnQ6">
          <property role="NX6R2" value="Maximum date of read messages" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBrgu" resolve="date" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBrky" role="2m5mJr">
        <property role="TrG5h" value="DeleteMessage" />
        <node concept="2m7Kf5" id="GBscvBBrow" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBro$" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBroB" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="rids" />
          <node concept="2m5nlk" id="2vxDjotnzvI" role="2m7DVh">
            <node concept="wb0Ql" id="2vxDjotnVSv" role="3GJlyp">
              <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBrkz" role="NuuwV">
          <property role="Nu42W" value="62" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAk" role="1GBnQ6">
          <property role="NXePf" value="Deleting messages" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAl" role="1GBnQ6">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBrow" resolve="peer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAm" role="1GBnQ6">
          <property role="NX6R2" value="Message random id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBroB" resolve="rids" />
        </node>
        <node concept="2m1Rp1" id="3MpuFr6x89N" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBrBX" role="2m5mJr">
        <property role="TrG5h" value="ClearChat" />
        <node concept="2m7Kf5" id="GBscvBBrG5" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBrG9" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBrBY" role="NuuwV">
          <property role="Nu42W" value="63" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBrG2" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foA6" role="1GBnQ6">
          <property role="NXePf" value="Clearing of conversation (without removing dialog from dialogs list)" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foA7" role="1GBnQ6">
          <property role="NX6R2" value="Conversation peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBrG5" resolve="peer" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBrVw" role="2m5mJr">
        <property role="TrG5h" value="DeleteChat" />
        <node concept="2m7Kf5" id="GBscvBBrZI" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBrZM" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBrVx" role="NuuwV">
          <property role="Nu42W" value="64" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBrZP" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAg" role="1GBnQ6">
          <property role="NXePf" value="Deleting of conversation (also leave group for group conversations)" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAh" role="1GBnQ6">
          <property role="NX6R2" value="Conversation peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBrZI" resolve="peer" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBk8i" role="2m5mJr">
        <property role="TrG5h" value="Message" />
        <node concept="NXeRC" id="7UKSaUun8Rh" role="NXp_2">
          <property role="NXePf" value="Update about plain message" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER2Ol" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBk8_" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER2Ot" role="NXp_2">
          <property role="NX6R2" value="Sender of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBk8G" resolve="senderUid" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER2OB" role="NXp_2">
          <property role="NX6R2" value="date of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBk8P" resolve="date" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER2ON" role="NXp_2">
          <property role="NX6R2" value="Rid of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBk90" resolve="rid" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER2P1" role="NXp_2">
          <property role="NX6R2" value="message content" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBk9d" resolve="message" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBk8_" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBk8D" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBk8G" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="senderUid" />
          <node concept="2m5ndE" id="GBscvBBk8M" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBk8P" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotnQg0" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBk90" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2vxDjoto6uL" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBk9d" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="message" />
          <node concept="3BlaRf" id="5NX0N0RTeKY" role="2m7DVh">
            <ref role="3BrLez" node="55bmeIQey3W" resolve="Message" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBk8j" role="NuuwV">
          <property role="Nu42W" value="37" />
        </node>
      </node>
      <node concept="NpBTk" id="2tyCW$U5Q8T" role="2m5mJr">
        <property role="TrG5h" value="MessageContentChanged" />
        <node concept="NXeRC" id="2tyCW$U5R6n" role="NXp_2">
          <property role="NXePf" value="Update about message change" />
        </node>
        <node concept="NX1gA" id="2tyCW$U5Ry4" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U5QeK" resolve="peer" />
        </node>
        <node concept="NX1gA" id="2tyCW$U5Ryc" role="NXp_2">
          <property role="NX6R2" value="Rid of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U5QeR" resolve="rid" />
        </node>
        <node concept="NX1gA" id="2tyCW$U5RXZ" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Message content" />
          <ref role="NX6Kv" node="2tyCW$U5QE$" resolve="message" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U5QeK" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="2tyCW$U5QeO" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$U5QeR" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2tyCW$U5QeX" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$U5QE$" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="message" />
          <node concept="3BlaRf" id="2tyCW$U5QEG" role="2m7DVh">
            <ref role="3BrLez" node="55bmeIQey3W" resolve="Message" />
          </node>
        </node>
        <node concept="Nu42z" id="2tyCW$U5Q8U" role="NuuwV">
          <property role="Nu42W" value="A2" />
        </node>
      </node>
      <node concept="NpBTk" id="2tyCW$U5TMH" role="2m5mJr">
        <property role="TrG5h" value="MessageDateChanged" />
        <node concept="NXeRC" id="2tyCW$U5UkM" role="NXp_2">
          <property role="NXePf" value="Update about message date changed" />
        </node>
        <node concept="NX1gA" id="2tyCW$U5UKW" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Destination peer" />
          <ref role="NX6Kv" node="2tyCW$U5TSK" resolve="peer" />
        </node>
        <node concept="NX1gA" id="2tyCW$U5UL4" role="NXp_2">
          <property role="NX6R2" value="Rid of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U5TSR" resolve="rid" />
        </node>
        <node concept="NX1gA" id="2tyCW$U5ULe" role="NXp_2">
          <property role="NX6R2" value="Date of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U5UKD" resolve="date" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U5TSK" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="2tyCW$U5TSO" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$U5TSR" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2tyCW$U5TSX" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$U5UKD" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2tyCW$U5UKL" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="2tyCW$U5TMI" role="NuuwV">
          <property role="Nu42W" value="A3" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBm97" role="2m5mJr">
        <property role="TrG5h" value="MessageSent" />
        <node concept="NXeRC" id="4zDDY4ER3kt" role="NXp_2">
          <property role="NXePf" value="Update about message sent" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER3kz" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBmaJ" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER3kF" role="NXp_2">
          <property role="NX6R2" value="Rid of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBmaY" resolve="rid" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER3kP" role="NXp_2">
          <property role="NX6R2" value="Date of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBmb7" resolve="date" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBmaJ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBmaV" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBmaY" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2vxDjotnWjy" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBmb7" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotnR6a" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBm98" role="NuuwV">
          <property role="Nu42W" value="04" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBn2m" role="2m5mJr">
        <property role="TrG5h" value="MessageReceived" />
        <node concept="2m7Kf5" id="GBscvBBn4u" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBn4y" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBn4_" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="startDate" />
          <node concept="wb0Ql" id="2vxDjotnQF5" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBn4I" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="receivedDate" />
          <node concept="wb0Ql" id="2vxDjoto6uU" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBn2n" role="NuuwV">
          <property role="Nu42W" value="36" />
        </node>
        <node concept="NXeRC" id="4zDDY4ER6iu" role="NXp_2">
          <property role="NXePf" value="Update about message received" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER6iz" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBn4u" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER6iF" role="NXp_2">
          <property role="NX6R2" value="Start date of received message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBn4_" resolve="startDate" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER6iP" role="NXp_2">
          <property role="NX6R2" value="Date of receive" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBn4I" resolve="receivedDate" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBn76" role="2m5mJr">
        <property role="TrG5h" value="MessageRead" />
        <node concept="NXeRC" id="4zDDY4ER6Mu" role="NXp_2">
          <property role="NXePf" value="Update about message read" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER6M$" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBn9m" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER6MG" role="NXp_2">
          <property role="NX6R2" value="Start date of read message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBn9t" resolve="startDate" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER6MQ" role="NXp_2">
          <property role="NX6R2" value="Date of read" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBn9A" resolve="readDate" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBn9m" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBn9q" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBn9t" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="startDate" />
          <node concept="wb0Ql" id="2vxDjotnQF2" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBn9A" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="readDate" />
          <node concept="wb0Ql" id="2vxDjoto6uR" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBn77" role="NuuwV">
          <property role="Nu42W" value="13" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBnlB" role="2m5mJr">
        <property role="TrG5h" value="MessageReadByMe" />
        <node concept="NXeRC" id="4zDDY4ER7iz" role="NXp_2">
          <property role="NXePf" value="Update about message read by me" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER7iD" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBnnZ" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER7iL" role="NXp_2">
          <property role="NX6R2" value="Start date of read message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBno6" resolve="startDate" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBnnZ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBno3" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBno6" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="startDate" />
          <node concept="wb0Ql" id="2vxDjotnQEZ" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBnlC" role="NuuwV">
          <property role="Nu42W" value="32" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBnD9" role="2m5mJr">
        <property role="TrG5h" value="MessageDelete" />
        <node concept="NXeRC" id="4zDDY4ER7Mw" role="NXp_2">
          <property role="NXePf" value="Update about message delete" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER7MA" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBnFC" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER7MI" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Deleted messages" />
          <ref role="NX6Kv" node="GBscvBBnFJ" resolve="rids" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBnFC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBnFG" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBnFJ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rids" />
          <node concept="2m5nlk" id="GBscvBBnFP" role="2m7DVh">
            <node concept="wb0Ql" id="2vxDjoto6uO" role="3GJlyp">
              <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBnDa" role="NuuwV">
          <property role="Nu42W" value="2E" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBnIx" role="2m5mJr">
        <property role="TrG5h" value="ChatClear" />
        <node concept="NXeRC" id="4zDDY4ER7MP" role="NXp_2">
          <property role="NXePf" value="Update about chat clear" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER7MV" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBnL7" resolve="peer" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBnL7" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBnLb" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBnIy" role="NuuwV">
          <property role="Nu42W" value="2F" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBnNP" role="2m5mJr">
        <property role="TrG5h" value="ChatDelete" />
        <node concept="NXeRC" id="4zDDY4ER7N1" role="NXp_2">
          <property role="NXePf" value="Update about chat delete" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER7Nf" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBnQv" resolve="peer" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBnQv" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBnQz" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBnNQ" role="NuuwV">
          <property role="Nu42W" value="30" />
        </node>
      </node>
      <node concept="2m488m" id="gbd4oSj4vu" role="2m5mJr">
        <property role="TrG5h" value="MessageState" />
        <node concept="2m7y0F" id="gbd4oSj4vv" role="2m7ymf">
          <property role="TrG5h" value="Sent" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="gbd4oSj4vw" role="2m7ymf">
          <property role="TrG5h" value="Received" />
          <property role="2m7y0m" value="2" />
        </node>
        <node concept="2m7y0F" id="gbd4oSj4vx" role="2m7ymf">
          <property role="TrG5h" value="Read" />
          <property role="2m7y0m" value="3" />
        </node>
      </node>
      <node concept="2m5naR" id="gbd4oSj4vy" role="2m5mJr">
        <property role="TrG5h" value="HistoryMessage" />
        <node concept="NXeRC" id="gbd4oSj4vz" role="NXodf">
          <property role="NXePf" value="Message from history" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4v$" role="NXodf">
          <property role="NX6R2" value="Sender of mesasge" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4vC" resolve="senderUid" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4v_" role="NXodf">
          <property role="NX6R2" value="Random Id of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4vE" resolve="rid" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4vA" role="NXodf">
          <property role="NX6R2" value="Date of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4vG" resolve="date" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4vB" role="NXodf">
          <property role="NX6R2" value="Content of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4vI" resolve="message" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="senderUid" />
          <node concept="2m5ndE" id="gbd4oSj4vD" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vE" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="gbd4oSj4vF" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vG" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="gbd4oSj4vH" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vI" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="message" />
          <node concept="3BlaRf" id="gbd4oSj4vJ" role="2m7DVh">
            <ref role="3BrLez" node="55bmeIQey3W" resolve="Message" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vK" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="state" />
          <node concept="2m5nlT" id="gbd4oSj4vL" role="2m7DVh">
            <node concept="3GJkcs" id="gbd4oSj4vM" role="3GH5xg">
              <ref role="3GJkik" node="gbd4oSj4vu" resolve="MessageState" />
            </node>
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="gbd4oSj4vN" role="2m5mJr">
        <property role="TrG5h" value="LoadHistory" />
        <node concept="2m7Kf5" id="gbd4oSj4vO" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="gbd4oSj4vP" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vQ" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="minDate" />
          <node concept="wb0Ql" id="gbd4oSj4vR" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vS" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="limit" />
          <node concept="2m5ndE" id="gbd4oSj4vT" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="gbd4oSj4vU" role="NuuwV">
          <property role="Nu42W" value="76" />
        </node>
        <node concept="2m1R6W" id="gbd4oSj4vV" role="2m6efq">
          <node concept="NXeRC" id="gbd4oSj4vW" role="1y2DgH">
            <property role="NXePf" value="Loaded history" />
          </node>
          <node concept="NX1gA" id="gbd4oSj4vX" role="1y2DgH">
            <property role="NX6R2" value="Messages" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="gbd4oSj4vZ" resolve="history" />
          </node>
          <node concept="NX1gA" id="gbd4oSj4vY" role="1y2DgH">
            <property role="NX6R2" value="Loaded users" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="gbd4oSj4w2" resolve="users" />
          </node>
          <node concept="2m7Kf5" id="gbd4oSj4vZ" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="history" />
            <node concept="2m5nlk" id="gbd4oSj4w0" role="2m7DVh">
              <node concept="2m5mGg" id="gbd4oSj4w1" role="3GJlyp">
                <ref role="2m5mJy" node="gbd4oSj4vy" resolve="HistoryMessage" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="gbd4oSj4w2" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="gbd4oSj4w3" role="2m7DVh">
              <node concept="2m5mGg" id="gbd4oSj4w4" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="gbd4oSj4w5" role="NuuwV">
            <property role="Nu42W" value="77" />
          </node>
        </node>
        <node concept="NXeRC" id="gbd4oSj4w6" role="1GBnQ6">
          <property role="NXePf" value="Loading history of chat" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4w7" role="1GBnQ6">
          <property role="NX6R2" value="Peer of conversation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4vO" resolve="peer" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4w8" role="1GBnQ6">
          <property role="NX6R2" value="start date of messages for loading or 0 for loading from start" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4vQ" resolve="minDate" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4w9" role="1GBnQ6">
          <property role="NX6R2" value="maximum amount of messages (max is 100)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4vS" resolve="limit" />
        </node>
      </node>
      <node concept="2m5naR" id="gbd4oSj4wb" role="2m5mJr">
        <property role="TrG5h" value="Dialog" />
        <node concept="NXeRC" id="gbd4oSj4wc" role="NXodf">
          <property role="NXePf" value="Conversation from history" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wd" role="NXodf">
          <property role="NX6R2" value="Peer of conversation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4wk" resolve="peer" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4we" role="NXodf">
          <property role="NX6R2" value="plain messages unread messages count" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4wm" resolve="unreadCount" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wf" role="NXodf">
          <property role="NX6R2" value="date of conversation for sorting" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4wo" resolve="sortDate" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wg" role="NXodf">
          <property role="NX6R2" value="Sender of top message (may be zero)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4wq" resolve="senderUid" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wh" role="NXodf">
          <property role="NX6R2" value="Random ID of top message (may be zero)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4ws" resolve="rid" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wi" role="NXodf">
          <property role="NX6R2" value="Date of top message (can't be zero)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4wu" resolve="date" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wj" role="NXodf">
          <property role="NX6R2" value="Content of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4ww" resolve="message" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4wk" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="gbd4oSj4wl" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4wm" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="unreadCount" />
          <node concept="2m5ndE" id="gbd4oSj4wn" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4wo" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="sortDate" />
          <node concept="wb0Ql" id="gbd4oSj4wp" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4wq" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="senderUid" />
          <node concept="2m5ndE" id="gbd4oSj4wr" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4ws" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="gbd4oSj4wt" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4wu" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="gbd4oSj4wv" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4ww" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="message" />
          <node concept="3BlaRf" id="gbd4oSj4wx" role="2m7DVh">
            <ref role="3BrLez" node="55bmeIQey3W" resolve="Message" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4wy" role="2m0hLx">
          <property role="2m7DUN" value="9" />
          <property role="TrG5h" value="state" />
          <node concept="2m5nlT" id="gbd4oSj4wz" role="2m7DVh">
            <node concept="3GJkcs" id="gbd4oSj4w$" role="3GH5xg">
              <ref role="3GJkik" node="gbd4oSj4vu" resolve="MessageState" />
            </node>
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="gbd4oSj4w_" role="2m5mJr">
        <property role="TrG5h" value="LoadDialogs" />
        <node concept="2m7Kf5" id="gbd4oSj4wA" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="minDate" />
          <node concept="wb0Ql" id="gbd4oSj4wB" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4wC" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="limit" />
          <node concept="2m5ndE" id="gbd4oSj4wD" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="gbd4oSj4wE" role="NuuwV">
          <property role="Nu42W" value="68" />
        </node>
        <node concept="2m1R6W" id="gbd4oSj4wF" role="2m6efq">
          <node concept="NXeRC" id="gbd4oSj4wG" role="1y2DgH">
            <property role="NXePf" value="Loaded dialogs" />
          </node>
          <node concept="NX1gA" id="gbd4oSj4wH" role="1y2DgH">
            <property role="NX6R2" value="Loaded groups" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="gbd4oSj4wK" resolve="groups" />
          </node>
          <node concept="NX1gA" id="gbd4oSj4wI" role="1y2DgH">
            <property role="NX6R2" value="Loaded users" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="gbd4oSj4wN" resolve="users" />
          </node>
          <node concept="NX1gA" id="gbd4oSj4wJ" role="1y2DgH">
            <property role="NX6R2" value="Loaded dialogs" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="gbd4oSj4wQ" resolve="dialogs" />
          </node>
          <node concept="2m7Kf5" id="gbd4oSj4wK" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="groups" />
            <node concept="2m5nlk" id="gbd4oSj4wL" role="2m7DVh">
              <node concept="2m5mGg" id="gbd4oSj4wM" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="gbd4oSj4wN" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="gbd4oSj4wO" role="2m7DVh">
              <node concept="2m5mGg" id="gbd4oSj4wP" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="gbd4oSj4wQ" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="dialogs" />
            <node concept="2m5nlk" id="gbd4oSj4wR" role="2m7DVh">
              <node concept="2m5mGg" id="gbd4oSj4wS" role="3GJlyp">
                <ref role="2m5mJy" node="gbd4oSj4wb" resolve="Dialog" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="gbd4oSj4wT" role="NuuwV">
            <property role="Nu42W" value="69" />
          </node>
        </node>
        <node concept="NXeRC" id="gbd4oSj4wU" role="1GBnQ6">
          <property role="NXePf" value="Loading conversation history" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wV" role="1GBnQ6">
          <property role="NX6R2" value="start date of conversation loading. Use 0 to load latest messages" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4wA" resolve="minDate" />
        </node>
        <node concept="NX1gA" id="gbd4oSj4wW" role="1GBnQ6">
          <property role="NX6R2" value="limit maximum amount of messages (max is 100)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="gbd4oSj4wC" resolve="limit" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBB6mJ" role="2m5lHt">
      <property role="TrG5h" value="Groups" />
      <property role="3XOG$Z" value="groups" />
      <node concept="2m5naR" id="GBscvBB6pR" role="2m5mJr">
        <property role="TrG5h" value="Group" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="4zDDY4ER8j4" role="NXodf">
          <property role="NXePf" value="Group information" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8ja" role="NXodf">
          <property role="NX6R2" value="group id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6pT" resolve="id" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8jo" role="NXodf">
          <property role="1GSvIU" value="danger" />
          <property role="NX6R2" value="Access hash of group" />
          <ref role="NX6Kv" node="GBscvBB6rB" resolve="accessHash" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8jy" role="NXodf">
          <property role="NX6R2" value="Title of group" />
          <ref role="NX6Kv" node="GBscvBB6rK" resolve="title" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8jI" role="NXodf">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Avatar of group" />
          <ref role="NX6Kv" node="GBscvBB6rV" resolve="avatar" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8jW" role="NXodf">
          <property role="NX6R2" value="is member of group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6se" resolve="isMember" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8kG" role="NXodf">
          <property role="NX6R2" value="Group creator" />
          <ref role="NX6Kv" node="GBscvBB6su" resolve="creatorUid" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8kY" role="NXodf">
          <property role="NX6R2" value="Members of group" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="3zc4oYAo8yQ" resolve="members" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8P9" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Date of creation" />
          <ref role="NX6Kv" node="2vxDjotnRx9" resolve="createDate" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6pT" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="GBscvBB6pX" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6rB" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBB6rH" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6rK" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="GBscvBB6rS" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6rV" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="avatar" />
          <node concept="2m5nlT" id="GBscvBB6s5" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBB9$R" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBB6se" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="isMember" />
          <node concept="2m5ndN" id="GBscvBB6sr" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6su" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="creatorUid" />
          <node concept="2m5ndE" id="GBscvBB6sH" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3zc4oYAo8yQ" role="2m0hLx">
          <property role="2m7DUN" value="9" />
          <property role="TrG5h" value="members" />
          <node concept="2m5nlk" id="3zc4oYAo8_s" role="2m7DVh">
            <node concept="2m5mGg" id="7d$A0Kt1YyH" role="3GJlyp">
              <ref role="2m5mJy" node="7d$A0Kt1Y2M" resolve="Member" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="2vxDjotnRx9" role="2m0hLx">
          <property role="2m7DUN" value="10" />
          <property role="TrG5h" value="createDate" />
          <node concept="wb0Ql" id="2vxDjotnRxt" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
      </node>
      <node concept="2m5naR" id="7d$A0Kt1Y2M" role="2m5mJr">
        <property role="TrG5h" value="Member" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="4zDDY4ER9le" role="NXodf">
          <property role="NXePf" value="Member information" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER9lk" role="NXodf">
          <property role="NX6R2" value="User id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="7d$A0Kt1Y6_" resolve="uid" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER9ls" role="NXodf">
          <property role="NX6R2" value="User inviter id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="7d$A0Kt1Y6G" resolve="inviterUid" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER9lA" role="NXodf">
          <property role="NX6R2" value="Adding date" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="7d$A0Kt1Y6P" resolve="date" />
        </node>
        <node concept="2m7Kf5" id="7d$A0Kt1Y6_" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="7d$A0Kt1Y6D" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="7d$A0Kt1Y6G" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="inviterUid" />
          <node concept="2m5ndE" id="7d$A0Kt1Y6M" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="7d$A0Kt1Y6P" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="2m5ndQ" id="7d$A0Kt1Y6X" role="2m7DVh" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBsbt" role="2m5mJr">
        <property role="TrG5h" value="CreateGroup" />
        <node concept="2m7Kf5" id="GBscvBBsbN" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2vxDjotofry" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBsbU" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="GBscvBBsc0" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBsc3" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="GBscvBBscb" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBsch" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBsbu" role="NuuwV">
          <property role="Nu42W" value="41" />
        </node>
        <node concept="2m1R6W" id="GBscvBBsck" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4EUnIu" role="1y2DgH">
            <property role="NXePf" value="Created group" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUnI$" role="1y2DgH">
            <property role="NX6R2" value="Peer of created group" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="GBscvBBscp" resolve="groupPeer" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUnIG" role="1y2DgH">
            <property role="NX6R2" value="Sequence number" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="GBscvBBscw" resolve="seq" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUnIQ" role="1y2DgH">
            <property role="NX6R2" value="Sequence state" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBBscD" resolve="state" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUnJ2" role="1y2DgH">
            <property role="NX6R2" value="Members of created group" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBBscO" resolve="users" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUnJg" role="1y2DgH">
            <property role="NX6R2" value="Group creation date" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2vxDjotnNHJ" resolve="date" />
          </node>
          <node concept="2m7Kf5" id="GBscvBBscp" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="groupPeer" />
            <node concept="2m5mGg" id="GBscvBBsct" role="2m7DVh">
              <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBBscw" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="GBscvBBscA" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="GBscvBBscD" role="2m0hLx">
            <property role="2m7DUN" value="4" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="55bmeIQ7Ama" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBBscO" role="2m0hLx">
            <property role="2m7DUN" value="5" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="3zc4oYAob0h" role="2m7DVh">
              <node concept="2m5ndE" id="3zc4oYAob0n" role="3GJlyp" />
            </node>
          </node>
          <node concept="2m7Kf5" id="2vxDjotnNHJ" role="2m0hLx">
            <property role="2m7DUN" value="6" />
            <property role="TrG5h" value="date" />
            <node concept="wb0Ql" id="2vxDjotnRWu" role="2m7DVh">
              <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
            </node>
          </node>
          <node concept="Nu42z" id="GBscvBBscl" role="NuuwV">
            <property role="Nu42W" value="42" />
          </node>
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAK" role="1GBnQ6">
          <property role="NXePf" value="Creating group chat" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAL" role="1GBnQ6">
          <property role="NX6R2" value="Random Id for avoiding double create" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBsbN" resolve="rid" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAM" role="1GBnQ6">
          <property role="NX6R2" value="Group title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBsbU" resolve="title" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAN" role="1GBnQ6">
          <property role="NX6R2" value="Members of group" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBsc3" resolve="users" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERalL" role="1GBnQ6">
          <property role="NX6R2" value="Title of new group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBsbU" resolve="title" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERalZ" role="1GBnQ6">
          <property role="NX6R2" value="Members of new group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBsc3" resolve="users" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBspc" role="2m5mJr">
        <property role="TrG5h" value="EditGroupTitle" />
        <node concept="2m7Kf5" id="GBscvBBspP" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="GBscvBBspT" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2hmARQJSOZp" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSOZx" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBspW" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="GBscvBBs_L" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBspd" role="NuuwV">
          <property role="Nu42W" value="55" />
        </node>
        <node concept="2m1Rp1" id="2vxDjoto7JX" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foB8" role="1GBnQ6">
          <property role="NXePf" value="Changing group title" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB9" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBspP" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBa" role="1GBnQ6">
          <property role="NX6R2" value="new group title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBspW" resolve="title" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBb" role="1GBnQ6">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSOZp" resolve="rid" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBt0k" role="2m5mJr">
        <property role="TrG5h" value="EditGroupAvatar" />
        <node concept="2m7Kf5" id="GBscvBBt14" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="GBscvBBt18" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2hmARQJSPrC" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSPrD" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBt1b" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="fileLocation" />
          <node concept="2m5mGg" id="GBscvBBt1h" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBt0l" role="NuuwV">
          <property role="Nu42W" value="56" />
        </node>
        <node concept="2m1R6W" id="2vxDjoto8AF" role="2m6efq">
          <node concept="NXeRC" id="4zDDY4EUofT" role="1y2DgH">
            <property role="NXePf" value="Updated group avatar" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUofZ" role="1y2DgH">
            <property role="NX6R2" value="Changed avatar" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="2vxDjoto8AK" resolve="avatar" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUog7" role="1y2DgH">
            <property role="NX6R2" value="Sequence number" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2vxDjoto8AR" resolve="seq" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUogh" role="1y2DgH">
            <property role="NX6R2" value="Sequence state" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="2vxDjoto8B0" resolve="state" />
          </node>
          <node concept="NX1gA" id="4zDDY4EUogt" role="1y2DgH">
            <property role="NX6R2" value="Avatar change date" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2vxDjoto8Bb" resolve="date" />
          </node>
          <node concept="2m7Kf5" id="2vxDjoto8AK" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="avatar" />
            <node concept="2m5mGg" id="2vxDjoto8AO" role="2m7DVh">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
          <node concept="2m7Kf5" id="2vxDjoto8AR" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="2vxDjoto8AX" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="2vxDjoto8B0" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="2vxDjoto8B8" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="2m7Kf5" id="2vxDjoto8Bb" role="2m0hLx">
            <property role="2m7DUN" value="4" />
            <property role="TrG5h" value="date" />
            <node concept="wb0Ql" id="2vxDjoto8Bl" role="2m7DVh">
              <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
            </node>
          </node>
          <node concept="Nu42z" id="2vxDjoto8AG" role="NuuwV">
            <property role="Nu42W" value="73" />
          </node>
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBT" role="1GBnQ6">
          <property role="NXePf" value="Changing group avatar" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBU" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBt14" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBV" role="1GBnQ6">
          <property role="NX6R2" value="uploaded file for avatar" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBt1b" resolve="fileLocation" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBW" role="1GBnQ6">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSPrC" resolve="rid" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBte6" role="2m5mJr">
        <property role="TrG5h" value="RemoveGroupAvatar" />
        <node concept="2m7Kf5" id="GBscvBBteX" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="GBscvBBtf1" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2hmARQJSPRq" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSPRr" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBte7" role="NuuwV">
          <property role="Nu42W" value="65" />
        </node>
        <node concept="2m1Rp1" id="2vxDjoto92C" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foA$" role="1GBnQ6">
          <property role="NXePf" value="Removing group avatar" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foA_" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBteX" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAA" role="1GBnQ6">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSPRq" resolve="rid" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBu09" role="2m5mJr">
        <property role="TrG5h" value="InviteUser" />
        <node concept="2m7Kf5" id="GBscvBBu15" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="GBscvBBu19" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2hmARQJSQj8" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSQj9" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBu1c" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="user" />
          <node concept="2m5mGg" id="2vxDjotocSv" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBu0a" role="NuuwV">
          <property role="Nu42W" value="45" />
        </node>
        <node concept="2m1Rp1" id="2vxDjoto9tV" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foB2" role="1GBnQ6">
          <property role="NXePf" value="Inviting user to group" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB3" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBu15" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB4" role="1GBnQ6">
          <property role="NX6R2" value="Users for invitation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBu1c" resolve="user" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB5" role="1GBnQ6">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSQj8" resolve="rid" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBu2s" role="2m5mJr">
        <property role="TrG5h" value="LeaveGroup" />
        <node concept="2m7Kf5" id="GBscvBBu3w" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="GBscvBBu3$" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2hmARQJSQIU" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSQIV" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBu2t" role="NuuwV">
          <property role="Nu42W" value="46" />
        </node>
        <node concept="2m1Rp1" id="2vxDjoto9Te" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBv" role="1GBnQ6">
          <property role="NXePf" value="Leaving group" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBw" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBu3w" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBx" role="1GBnQ6">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSQIU" resolve="rid" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBu78" role="2m5mJr">
        <property role="TrG5h" value="KickUser" />
        <node concept="2m7Kf5" id="GBscvBBu8m" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="GBscvBBu8q" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2hmARQJSRAi" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSRAj" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBu8t" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="user" />
          <node concept="2m5mGg" id="2vxDjotoctb" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBu79" role="NuuwV">
          <property role="Nu42W" value="47" />
        </node>
        <node concept="2m1Rp1" id="2vxDjotoaJO" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_I" role="1GBnQ6">
          <property role="NXePf" value="Kicking user from group" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_J" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBu8m" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_K" role="1GBnQ6">
          <property role="NX6R2" value="users for removing" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBu8t" resolve="user" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_L" role="1GBnQ6">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSRAi" resolve="rid" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBByXn" role="2m5mJr">
        <property role="TrG5h" value="GroupInvite" />
        <node concept="NXeRC" id="EUEXKTmM9p" role="NXp_2">
          <property role="NXePf" value="Update about inviting current user to group" />
        </node>
        <node concept="NX1gA" id="EUEXKTmM9v" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBByYG" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmM9B" role="NXp_2">
          <property role="NX6R2" value="Inviter UID. If equals to current uid than group created by user." />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBByYN" resolve="inviteUid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmMyr" role="NXp_2">
          <property role="NX6R2" value="Date of creating" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBByYW" resolve="date" />
        </node>
        <node concept="NX1gA" id="2hmARQJSZ4E" role="NXp_2">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSzwa" resolve="rid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBByYG" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBByYK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2hmARQJSzwa" role="2m0hLx">
          <property role="2m7DUN" value="9" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSS1Y" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBByYN" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="inviteUid" />
          <node concept="2m5ndE" id="GBscvBByYT" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBByYW" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotobb7" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBByXo" role="NuuwV">
          <property role="Nu42W" value="24" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBz0x" role="2m5mJr">
        <property role="TrG5h" value="GroupUserInvited" />
        <node concept="NXeRC" id="EUEXKTmMyj" role="NXp_2">
          <property role="NXePf" value="Update about inviting user to group" />
        </node>
        <node concept="NX1gA" id="EUEXKTmMy$" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBz1Y" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmNC1" role="NXp_2">
          <property role="NX6R2" value="Added user ID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBz25" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmNCb" role="NXp_2">
          <property role="NX6R2" value="Inviter user ID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBz2e" resolve="inviterUid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmNZw" role="NXp_2">
          <property role="NX6R2" value="Date of adding user to group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBz2p" resolve="date" />
        </node>
        <node concept="NX1gA" id="2hmARQJSZ4S" role="NXp_2">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJSzVt" resolve="rid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBz1Y" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBBz22" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2hmARQJSzVt" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSS22" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBz25" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBz2b" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBz2e" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="inviterUid" />
          <node concept="2m5ndE" id="GBscvBBz2m" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBz2p" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotobba" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBz0y" role="NuuwV">
          <property role="Nu42W" value="15" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBzhS" role="2m5mJr">
        <property role="TrG5h" value="GroupUserLeave" />
        <node concept="NXeRC" id="EUEXKTmOok" role="NXp_2">
          <property role="NXePf" value="Update about leaving user" />
        </node>
        <node concept="NX1gA" id="EUEXKTmOoq" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzjv" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmPu6" role="NXp_2">
          <property role="NX6R2" value="User's ID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzjA" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmPug" role="NXp_2">
          <property role="NX6R2" value="Date of user leave" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzjJ" resolve="date" />
        </node>
        <node concept="NX1gA" id="2hmARQJSZwF" role="NXp_2">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJS$mO" resolve="rid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzjv" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBBzjz" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2hmARQJS$mO" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSS25" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBzjA" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBzjG" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzjJ" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotobAt" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBzhT" role="NuuwV">
          <property role="Nu42W" value="17" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBzoY" role="2m5mJr">
        <property role="TrG5h" value="GroupUserKick" />
        <node concept="NXeRC" id="EUEXKTmPuo" role="NXp_2">
          <property role="NXePf" value="Update about kicking user" />
        </node>
        <node concept="NX1gA" id="EUEXKTmPuu" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzqH" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmPuA" role="NXp_2">
          <property role="NX6R2" value="Kicked user's ID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzqO" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmPuK" role="NXp_2">
          <property role="NX6R2" value="Kicker user's ID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzqX" resolve="kickerUid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmPQe" role="NXp_2">
          <property role="NX6R2" value="Date of user kick" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzr8" resolve="date" />
        </node>
        <node concept="NX1gA" id="2hmARQJSZWv" role="NXp_2">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJS$Mb" resolve="rid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzqH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBBzqL" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2hmARQJS$Mb" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSSyA" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBzqO" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBzqU" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzqX" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="kickerUid" />
          <node concept="2m5ndE" id="GBscvBBzr5" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzr8" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotobAw" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBzoZ" role="NuuwV">
          <property role="Nu42W" value="18" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBzFb" role="2m5mJr">
        <property role="TrG5h" value="GroupMembersUpdate" />
        <node concept="NXeRC" id="EUEXKTmQfb" role="NXp_2">
          <property role="NXePf" value="Silent group members update" />
        </node>
        <node concept="NX1gA" id="EUEXKTmQfh" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzH4" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmQfp" role="NXp_2">
          <property role="NX6R2" value="New members list" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzHm" resolve="members" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzH4" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBBzH8" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzHm" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="members" />
          <node concept="2m5nlk" id="GBscvBBzHs" role="2m7DVh">
            <node concept="2m5mGg" id="7d$A0Kt1YYt" role="3GJlyp">
              <ref role="2m5mJy" node="7d$A0Kt1Y2M" resolve="Member" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBzFc" role="NuuwV">
          <property role="Nu42W" value="2C" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBzXD" role="2m5mJr">
        <property role="TrG5h" value="GroupTitleChanged" />
        <node concept="NXeRC" id="EUEXKTmRGS" role="NXp_2">
          <property role="NXePf" value="Update about group title change" />
        </node>
        <node concept="NX1gA" id="EUEXKTmRGY" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzZD" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmS60" role="NXp_2">
          <property role="NX6R2" value="Changer UID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzZK" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmS6a" role="NXp_2">
          <property role="NX6R2" value="New Title of group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBzZT" resolve="title" />
        </node>
        <node concept="NX1gA" id="EUEXKTmS6m" role="NXp_2">
          <property role="NX6R2" value="Date of title change" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB$04" resolve="date" />
        </node>
        <node concept="NX1gA" id="2hmARQJT0ol" role="NXp_2">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJS_dA" resolve="rid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzZD" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBBzZH" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2hmARQJS_dA" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSUHX" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBzZK" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBzZQ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBzZT" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="GBscvBB$01" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB$04" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotoc1N" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBzXE" role="NuuwV">
          <property role="Nu42W" value="26" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBB$gD" role="2m5mJr">
        <property role="TrG5h" value="GroupAvatarChanged" />
        <node concept="NXeRC" id="EUEXKTmSvr" role="NXp_2">
          <property role="NXePf" value="Update about group avatar change" />
        </node>
        <node concept="NX1gA" id="EUEXKTmSvx" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB$iN" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmSvD" role="NXp_2">
          <property role="NX6R2" value="Avatar changer uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB$iU" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmSSU" role="NXp_2">
          <property role="NX6R2" value="New Avatar. If null then avatar is removed" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBB$j3" resolve="avatar" />
        </node>
        <node concept="NX1gA" id="EUEXKTmUn2" role="NXp_2">
          <property role="NX6R2" value="Date of avatar change" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB$jk" resolve="date" />
        </node>
        <node concept="NX1gA" id="2hmARQJT0Oc" role="NXp_2">
          <property role="NX6R2" value="Random Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2hmARQJS_D3" resolve="rid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB$iN" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBB$iR" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2hmARQJS_D3" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="2hmARQJSV9Q" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBB$iU" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBB$j0" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB$j3" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="avatar" />
          <node concept="2m5nlT" id="GBscvBB$jb" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBB$jh" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBB$jk" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotoc1Q" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBB$gE" role="NuuwV">
          <property role="Nu42W" value="27" />
        </node>
      </node>
      <node concept="2m62dX" id="1$yIuJFAZ_X" role="2m5mJr">
        <property role="TrG5h" value="InviteUrl" />
        <node concept="NXeRC" id="1$yIuJFB06p" role="NXp4Y">
          <property role="NXePf" value="Response for invite url methods" />
        </node>
        <node concept="2m7Kf5" id="1$yIuJFAZEz" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="url" />
          <node concept="2m5ndX" id="1$yIuJFAZEB" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="1$yIuJFAZ_Y" role="NuuwV">
          <property role="Nu42W" value="B2" />
        </node>
      </node>
      <node concept="2m6fVq" id="1$yIuJFAX9y" role="2m5mJr">
        <property role="TrG5h" value="GetGroupInviteUrl" />
        <node concept="NXeRC" id="1$yIuJFAY5i" role="1GBnQ6">
          <property role="NXePf" value="Building invite url" />
        </node>
        <node concept="NX1gA" id="1$yIuJFAY5c" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Destination group peer" />
          <ref role="NX6Kv" node="1$yIuJFAXDx" resolve="groupPeer" />
        </node>
        <node concept="2m7Kf5" id="1$yIuJFAXDx" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="1$yIuJFAXD_" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="1$yIuJFAX9z" role="NuuwV">
          <property role="Nu42W" value="B1" />
        </node>
        <node concept="2m1Rp1" id="1$yIuJFAZEG" role="2m6efq">
          <ref role="2m1o9l" node="1$yIuJFAZ_X" resolve="InviteUrl" />
        </node>
      </node>
      <node concept="2m6fVq" id="1$yIuJFAZ0Z" role="2m5mJr">
        <property role="TrG5h" value="RevokeInviteUrl" />
        <node concept="2m7Kf5" id="1$yIuJFAZ5y" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="1$yIuJFAZ5A" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="1$yIuJFAZ10" role="NuuwV">
          <property role="Nu42W" value="B3" />
        </node>
        <node concept="2m1Rp1" id="1$yIuJFB0y2" role="2m6efq">
          <ref role="2m1o9l" node="1$yIuJFAZ_X" resolve="InviteUrl" />
        </node>
        <node concept="NXeRC" id="1$yIuJFB0y5" role="1GBnQ6">
          <property role="NXePf" value="Revoking invite urls" />
        </node>
        <node concept="NX1gA" id="1$yIuJFB0XL" role="1GBnQ6">
          <property role="NX6R2" value="Destination group peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="1$yIuJFAZ5y" resolve="groupPeer" />
        </node>
      </node>
      <node concept="2m6fVq" id="1$yIuJFB1u2" role="2m5mJr">
        <property role="TrG5h" value="JoinGroup" />
        <node concept="2uC4CA" id="1$yIuJFB3DJ" role="2uC9gA">
          <property role="2uC4DK" value="500" />
          <property role="2uC4Qe" value="ACCESS_REVOKED" />
          <property role="2uCiSL" value="When url is obsolete" />
        </node>
        <node concept="NXeRC" id="1$yIuJFB1Yz" role="1GBnQ6">
          <property role="NXePf" value="Join group method" />
        </node>
        <node concept="2m7Kf5" id="1$yIuJFB1yH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="url" />
          <node concept="2m5ndX" id="1$yIuJFB1yL" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="1$yIuJFB1u3" role="NuuwV">
          <property role="Nu42W" value="B4" />
        </node>
        <node concept="2m1R6W" id="1$yIuJFB27U" role="2m6efq">
          <node concept="NX1gA" id="1$yIuJFB3DS" role="1y2DgH">
            <property role="NX6R2" value="Joined group" />
            <ref role="NX6Kv" node="1$yIuJFB2cl" resolve="group" />
          </node>
          <node concept="NX1gA" id="2HoLzB7ue7s" role="1y2DgH">
            <property role="NX6R2" value="Users from members" />
            <property role="1GSvIU" value="hidden" />
            <ref role="NX6Kv" node="2HoLzB7udD8" resolve="users" />
          </node>
          <node concept="NX1gA" id="2HoLzB7ue7$" role="1y2DgH">
            <property role="NX6R2" value="Random id" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2HoLzB7udEC" resolve="rid" />
          </node>
          <node concept="NX1gA" id="2HoLzB7ue7I" role="1y2DgH">
            <property role="NX6R2" value="Sequence number" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="1$yIuJFB45J" resolve="seq" />
          </node>
          <node concept="NX1gA" id="2HoLzB7ue7U" role="1y2DgH">
            <property role="NX6R2" value="State" />
            <ref role="NX6Kv" node="1$yIuJFB45S" resolve="state" />
          </node>
          <node concept="NX1gA" id="2HoLzB7ue$$" role="1y2DgH">
            <property role="NX6R2" value="Date of join" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="1$yIuJFB4xV" resolve="date" />
          </node>
          <node concept="2m7Kf5" id="1$yIuJFB2cl" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="group" />
            <node concept="2m5mGg" id="1$yIuJFB2cp" role="2m7DVh">
              <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
            </node>
          </node>
          <node concept="2m7Kf5" id="2HoLzB7udD8" role="2m0hLx">
            <property role="2m7DUN" value="5" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="2HoLzB7udEv" role="2m7DVh">
              <node concept="2m5mGg" id="2HoLzB7udE_" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="2HoLzB7udEC" role="2m0hLx">
            <property role="2m7DUN" value="6" />
            <property role="TrG5h" value="rid" />
            <node concept="wb0Ql" id="2HoLzB7udEY" role="2m7DVh">
              <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
            </node>
          </node>
          <node concept="2m7Kf5" id="1$yIuJFB45J" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="1$yIuJFB45P" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="1$yIuJFB45S" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="1$yIuJFB460" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="2m7Kf5" id="1$yIuJFB4xV" role="2m0hLx">
            <property role="2m7DUN" value="4" />
            <property role="TrG5h" value="date" />
            <node concept="wb0Ql" id="1$yIuJFB4y5" role="2m7DVh">
              <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
            </node>
          </node>
          <node concept="Nu42z" id="1$yIuJFB27V" role="NuuwV">
            <property role="Nu42W" value="B5" />
          </node>
        </node>
        <node concept="2uC4CA" id="1$yIuJFB2LB" role="2uC9gA">
          <property role="2uC4DK" value="500" />
          <property role="2uC4Qe" value="ACCESS_DENIED" />
          <property role="2uCiSL" value="When it is unable to join group for this user" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="1$yIuJFB7ca" role="2m5lHt">
      <property role="TrG5h" value="Integrations" />
      <property role="3XOG$Z" value="integrtions" />
      <node concept="2m62dX" id="1$yIuJFB8wz" role="2m5mJr">
        <property role="TrG5h" value="IntegrationToken" />
        <node concept="NXeRC" id="1$yIuJFB9pb" role="NXp4Y">
          <property role="NXePf" value="Group token response" />
        </node>
        <node concept="NX1gA" id="1$yIuJFBahx" role="NXp4Y">
          <property role="NX6R2" value="current group token" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="1$yIuJFB8wH" resolve="token" />
        </node>
        <node concept="NX1gA" id="1$yIuJFBahD" role="NXp4Y">
          <property role="NX6R2" value="current group url" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="1$yIuJFB8WT" resolve="url" />
        </node>
        <node concept="2m7Kf5" id="1$yIuJFB8wH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="token" />
          <node concept="2m5ndX" id="1$yIuJFB8wL" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="1$yIuJFB8WT" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="url" />
          <node concept="2m5ndX" id="1$yIuJFB8WZ" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="1$yIuJFB8w$" role="NuuwV">
          <property role="Nu42W" value="B7" />
        </node>
      </node>
      <node concept="2m6fVq" id="1$yIuJFB83Z" role="2m5mJr">
        <property role="TrG5h" value="GetIntegrationToken" />
        <node concept="NXeRC" id="1$yIuJFBba7" role="1GBnQ6">
          <property role="NXePf" value="Getting current group token" />
        </node>
        <node concept="NX1gA" id="1$yIuJFBbBc" role="1GBnQ6">
          <property role="NX6R2" value="Peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="1$yIuJFB84f" resolve="groupPeer" />
        </node>
        <node concept="2m7Kf5" id="1$yIuJFB84f" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="1$yIuJFBe7K" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="1$yIuJFB840" role="NuuwV">
          <property role="Nu42W" value="B6" />
        </node>
        <node concept="2m1Rp1" id="1$yIuJFBaHS" role="2m6efq">
          <ref role="2m1o9l" node="1$yIuJFB8wz" resolve="IntegrationToken" />
        </node>
      </node>
      <node concept="2m6fVq" id="1$yIuJFBbAA" role="2m5mJr">
        <property role="TrG5h" value="RevokeIntegrationToken" />
        <node concept="2m7Kf5" id="1$yIuJFBbAX" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="1$yIuJFBe7N" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="1$yIuJFBbAB" role="NuuwV">
          <property role="Nu42W" value="B8" />
        </node>
        <node concept="2m1Rp1" id="1$yIuJFBbB4" role="2m6efq">
          <ref role="2m1o9l" node="1$yIuJFB8wz" resolve="IntegrationToken" />
        </node>
        <node concept="NXeRC" id="1$yIuJFBbB7" role="1GBnQ6">
          <property role="NXePf" value="Revoke group token" />
        </node>
        <node concept="NX1gA" id="1$yIuJFBbBj" role="1GBnQ6">
          <property role="NX6R2" value="Peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="1$yIuJFBbAX" resolve="groupPeer" />
        </node>
      </node>
      <node concept="1Dx9M1" id="1$yIuJFB7BZ" role="1Dx9rD">
        <property role="1Dx9K7" value="Package contains methods for providing integration" />
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBBulc" role="2m5lHt">
      <property role="TrG5h" value="Typing and Online" />
      <property role="3XOG$Z" value="weak" />
      <node concept="2m488m" id="4zDDY4ERgsM" role="2m5mJr">
        <property role="TrG5h" value="TypingType" />
        <node concept="2m7y0F" id="4zDDY4ERgsO" role="2m7ymf">
          <property role="TrG5h" value="Text" />
          <property role="2m7y0m" value="0" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBuxG" role="2m5mJr">
        <property role="TrG5h" value="Typing" />
        <node concept="2m7Kf5" id="GBscvBBuxO" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBuxS" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBuxV" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="typingType" />
          <node concept="3GJkcs" id="4zDDY4ERgtR" role="2m7DVh">
            <ref role="3GJkik" node="4zDDY4ERgsM" resolve="TypingType" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBuxH" role="NuuwV">
          <property role="Nu42W" value="1B" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBuy4" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBz" role="1GBnQ6">
          <property role="NXePf" value="Sending typing notification" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB$" role="1GBnQ6">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBuxO" resolve="peer" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB_" role="1GBnQ6">
          <property role="NX6R2" value="typing type." />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBuxV" resolve="typingType" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBuyg" role="2m5mJr">
        <property role="TrG5h" value="SetOnline" />
        <node concept="2m7Kf5" id="GBscvBBuyv" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="isOnline" />
          <node concept="2m5ndN" id="GBscvBBuyz" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBuyA" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="timeout" />
          <node concept="2m5ndQ" id="GBscvBBuyG" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBuyh" role="NuuwV">
          <property role="Nu42W" value="1D" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBuyJ" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAB" role="1GBnQ6">
          <property role="NXePf" value="Sending online state" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAC" role="1GBnQ6">
          <property role="NX6R2" value="is user online" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBuyv" resolve="isOnline" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAD" role="1GBnQ6">
          <property role="NX6R2" value="timeout of online state" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBuyA" resolve="timeout" />
        </node>
      </node>
      <node concept="NvyAe" id="GBscvBBxyf" role="2m5mJr" />
      <node concept="NpBTk" id="GBscvBBxyx" role="2m5mJr">
        <property role="TrG5h" value="Typing" />
        <node concept="NXeRC" id="EUEXKTmXzh" role="NXp_2">
          <property role="NXePf" value="Update about user's typing" />
        </node>
        <node concept="NX1gA" id="EUEXKTmXzn" role="NXp_2">
          <property role="NX6R2" value="Conversation peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBxyP" resolve="peer" />
        </node>
        <node concept="NX1gA" id="EUEXKTmXzv" role="NXp_2">
          <property role="NX6R2" value="User's id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBxyW" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmXzD" role="NXp_2">
          <property role="NX6R2" value="Type of typing" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBxz5" resolve="typingType" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBxyP" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="GBscvBBxyT" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBxyW" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBxz2" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBxz5" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="typingType" />
          <node concept="3GJkcs" id="4zDDY4ERgtU" role="2m7DVh">
            <ref role="3GJkik" node="4zDDY4ERgsM" resolve="TypingType" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBxyy" role="NuuwV">
          <property role="Nu42W" value="06" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBxKK" role="2m5mJr">
        <property role="TrG5h" value="UserOnline" />
        <node concept="NXeRC" id="EUEXKTmXzL" role="NXp_2">
          <property role="NXePf" value="Update about user became online" />
        </node>
        <node concept="NX1gA" id="EUEXKTmXzV" role="NXp_2">
          <property role="NX6R2" value="User's Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBxLc" resolve="uid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBxLc" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBxLg" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBxKL" role="NuuwV">
          <property role="Nu42W" value="07" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBxLK" role="2m5mJr">
        <property role="TrG5h" value="UserOffline" />
        <node concept="NXeRC" id="EUEXKTmX$1" role="NXp_2">
          <property role="NXePf" value="Update about user became offline" />
        </node>
        <node concept="NX1gA" id="EUEXKTmX$7" role="NXp_2">
          <property role="NX6R2" value="User's id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBxMg" resolve="uid" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBxMg" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBxMk" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBxLL" role="NuuwV">
          <property role="Nu42W" value="08" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBy07" role="2m5mJr">
        <property role="TrG5h" value="UserLastSeen" />
        <node concept="NXeRC" id="EUEXKTmX$d" role="NXp_2">
          <property role="NXePf" value="Update about user's last seen state" />
        </node>
        <node concept="NX1gA" id="EUEXKTmX$j" role="NXp_2">
          <property role="NX6R2" value="User's id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBy0F" resolve="uid" />
        </node>
        <node concept="NX1gA" id="EUEXKTmX$r" role="NXp_2">
          <property role="NX6R2" value="Last seen time" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBy0M" resolve="date" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBy0F" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBBy0J" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBy0M" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotoh82" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBy08" role="NuuwV">
          <property role="Nu42W" value="09" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBByeR" role="2m5mJr">
        <property role="TrG5h" value="GroupOnline" />
        <node concept="NXeRC" id="EUEXKTmX$y" role="NXp_2">
          <property role="NXePf" value="Update about group online change" />
        </node>
        <node concept="NX1gA" id="EUEXKTmX$K" role="NXp_2">
          <property role="NX6R2" value="Group id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBByfx" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmX$S" role="NXp_2">
          <property role="NX6R2" value="current online user's count" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBByfC" resolve="count" />
        </node>
        <node concept="2m7Kf5" id="GBscvBByfx" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBByf_" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBByfC" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="count" />
          <node concept="2m5ndE" id="GBscvBByfI" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBByeS" role="NuuwV">
          <property role="Nu42W" value="21" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvB$$H4" role="2m5lHt">
      <property role="TrG5h" value="Media and Files" />
      <property role="3XOG$Z" value="files" />
      <node concept="2m5naR" id="GBscvB$$Hy" role="2m5mJr">
        <property role="TrG5h" value="FileLocation" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="EUEXKTmYot" role="NXodf">
          <property role="NXePf" value="Location of file on server" />
        </node>
        <node concept="NX1gA" id="EUEXKTmYoz" role="NXodf">
          <property role="NX6R2" value="Unique Id of file" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvB$$JS" resolve="fileId" />
        </node>
        <node concept="NX1gA" id="EUEXKTmYoF" role="NXodf">
          <property role="NX6R2" value="Access hash of file" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvB$$JZ" resolve="accessHash" />
        </node>
        <node concept="2m7Kf5" id="GBscvB$$JS" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="fileId" />
          <node concept="2m5ndQ" id="GBscvB$$JW" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvB$$JZ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvB$$K5" role="2m7DVh" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvB$$Hk" role="2m5mJr">
        <property role="TrG5h" value="AvatarImage" />
        <property role="tsOgz" value="true" />
        <node concept="2m7Kf5" id="GBscvB$$Kn" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="fileLocation" />
          <node concept="2m5mGg" id="GBscvB$$Kr" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvB$$Ku" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="width" />
          <node concept="2m5ndE" id="GBscvB$$K$" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvB$$KB" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="height" />
          <node concept="2m5ndE" id="GBscvB$$KJ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvB$$KM" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="fileSize" />
          <node concept="2m5ndE" id="GBscvB$$KW" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="EUEXKTmYMy" role="NXodf">
          <property role="NXePf" value="Avatar Image" />
        </node>
        <node concept="NX1gA" id="EUEXKTmYMB" role="NXodf">
          <property role="NX6R2" value="Location of file" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvB$$Kn" resolve="fileLocation" />
        </node>
        <node concept="NX1gA" id="EUEXKTn00b" role="NXodf">
          <property role="NX6R2" value="Width of avatar image" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvB$$Ku" resolve="width" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm0s" role="NXodf">
          <property role="NX6R2" value="Height of avatar image" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvB$$KB" resolve="height" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm0C" role="NXodf">
          <property role="NX6R2" value="Size of file" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvB$$KM" resolve="fileSize" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvB$$LB" role="2m5mJr">
        <property role="TrG5h" value="Avatar" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="7UKSaUukm0L" role="NXodf">
          <property role="NXePf" value="Avatar of User or Group" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm0R" role="NXodf">
          <property role="NX6R2" value="Optional small image of avatar box in 100x100" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBAny_" resolve="smallImage" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm0Z" role="NXodf">
          <property role="NX6R2" value="Optional large image of avatar box in 200x200" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBAz8n" resolve="largeImage" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm19" role="NXodf">
          <property role="NX6R2" value="Optional full screen image of avatar" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBAz8P" resolve="fullImage" />
        </node>
        <node concept="2m7Kf5" id="GBscvBAny_" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="smallImage" />
          <node concept="2m5nlT" id="GBscvBAn$Z" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBAz8k" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$Hk" resolve="AvatarImage" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBAz8n" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="largeImage" />
          <node concept="2m5nlT" id="GBscvBAz8G" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBAz8M" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$Hk" resolve="AvatarImage" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBAz8P" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="fullImage" />
          <node concept="2m5nlT" id="GBscvBAz8Z" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBAz95" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$Hk" resolve="AvatarImage" />
            </node>
          </node>
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBB67s" role="2m5mJr">
        <property role="TrG5h" value="FastThumb" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="7UKSaUukm1h" role="NXodf">
          <property role="NXePf" value="Fast thumb of media messages. Less than 90x90 and compressed by JPEG with low quality" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm1v" role="NXodf">
          <property role="NX6R2" value="Width of thumb" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB67R" resolve="w" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm1B" role="NXodf">
          <property role="NX6R2" value="Height of thump" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB67Y" resolve="h" />
        </node>
        <node concept="NX1gA" id="7UKSaUukm1L" role="NXodf">
          <property role="NX6R2" value="compressed image data" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBB687" resolve="thumb" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB67R" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="w" />
          <node concept="2m5ndE" id="GBscvBB67V" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB67Y" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="h" />
          <node concept="2m5ndE" id="GBscvBB684" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB687" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="thumb" />
          <node concept="2m61tm" id="GBscvBB68f" role="2m7DVh" />
        </node>
      </node>
      <node concept="NvyAe" id="GBscvBBiZs" role="2m5mJr" />
      <node concept="2m6fVq" id="3MpuFr6x5xl" role="2m5mJr">
        <property role="TrG5h" value="GetFileUrl" />
        <node concept="NXeRC" id="3MpuFr6x791" role="1GBnQ6">
          <property role="NXePf" value="Requesting file URL for downloading" />
        </node>
        <node concept="NX1gA" id="3MpuFr6x797" role="1GBnQ6">
          <property role="NX6R2" value="file's location" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3MpuFr6x6CL" resolve="file" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6x6CL" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="file" />
          <node concept="2m5mGg" id="3MpuFr6x6CP" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
          </node>
        </node>
        <node concept="Nu42z" id="3MpuFr6x5xm" role="NuuwV">
          <property role="Nu42W" value="4D" />
        </node>
        <node concept="2m1R6W" id="3MpuFr6x68n" role="2m6efq">
          <node concept="NX1gA" id="3MpuFr6x79h" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Url for downloading" />
            <ref role="NX6Kv" node="3MpuFr6x68s" resolve="url" />
          </node>
          <node concept="NX1gA" id="3MpuFr6x79n" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Timeout of url" />
            <ref role="NX6Kv" node="3MpuFr6x68z" resolve="timeout" />
          </node>
          <node concept="2m7Kf5" id="3MpuFr6x68s" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="url" />
            <node concept="2m5ndX" id="3MpuFr6x68w" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="3MpuFr6x68z" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="timeout" />
            <node concept="2m5ndE" id="3MpuFr6x68D" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="3MpuFr6x68o" role="NuuwV">
            <property role="Nu42W" value="4E" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="3MpuFr6x8F5" role="2m5mJr">
        <property role="TrG5h" value="GetFileUploadUrl" />
        <node concept="NXeRC" id="3MpuFr6xfFT" role="1GBnQ6">
          <property role="NXePf" value="Requesting pload url" />
        </node>
        <node concept="NX1gA" id="gbd4oSiue5" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Expected size of uploading file. May be inaccurate. Used for size allocation optimizations." />
          <ref role="NX6Kv" node="3MpuFr6xckS" resolve="expectedSize" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6xckS" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="expectedSize" />
          <node concept="2m5ndE" id="3MpuFr6xckW" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3MpuFr6x8F6" role="NuuwV">
          <property role="Nu42W" value="61" />
        </node>
        <node concept="2m1R6W" id="3MpuFr6x8Ir" role="2m6efq">
          <node concept="2m7Kf5" id="3MpuFr6x8Iw" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="url" />
            <node concept="2m5ndX" id="3MpuFr6x8I$" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="3MpuFr6xcPv" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="uploadKey" />
            <node concept="2m61tm" id="3MpuFr6xcP_" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="3MpuFr6x8Is" role="NuuwV">
            <property role="Nu42W" value="79" />
          </node>
          <node concept="NX1gA" id="gbd4oSiylW" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Url for uploading" />
            <ref role="NX6Kv" node="3MpuFr6x8Iw" resolve="url" />
          </node>
          <node concept="NX1gA" id="gbd4oSiyR0" role="1y2DgH">
            <property role="1GSvIU" value="compact" />
            <property role="NX6R2" value="Upload key for upload" />
            <ref role="NX6Kv" node="3MpuFr6xcPv" resolve="uploadKey" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="3MpuFr6xcQQ" role="2m5mJr">
        <property role="TrG5h" value="CommitFileUpload" />
        <node concept="NXeRC" id="gbd4oSimij" role="1GBnQ6">
          <property role="NXePf" value="Comminting uploaded file to storage" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6xcSa" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uploadKey" />
          <node concept="2m61tm" id="3MpuFr6xcSe" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2fcK_nBaSeD" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="fileName" />
          <node concept="2m5ndX" id="2fcK_nBaSh$" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3MpuFr6xcQR" role="NuuwV">
          <property role="Nu42W" value="7A" />
        </node>
        <node concept="2m1R6W" id="3MpuFr6xcUP" role="2m6efq">
          <node concept="NX1gA" id="gbd4oSixOV" role="1y2DgH">
            <property role="NX6R2" value="Result file location" />
            <ref role="NX6Kv" node="3MpuFr6xcUU" resolve="uploadedFileLocation" />
          </node>
          <node concept="2m7Kf5" id="3MpuFr6xcUU" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="uploadedFileLocation" />
            <node concept="2m5mGg" id="3MpuFr6xcV6" role="2m7DVh">
              <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
            </node>
          </node>
          <node concept="Nu42z" id="3MpuFr6xcUQ" role="NuuwV">
            <property role="Nu42W" value="8A" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="gbd4oSisAS" role="2m5mJr">
        <property role="TrG5h" value="GetFileUploadPartUrl" />
        <node concept="2m7Kf5" id="gbd4oSizT4" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="partNumber" />
          <node concept="2m5ndE" id="gbd4oSizTa" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSi$qf" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="partSize" />
          <node concept="2m5ndE" id="gbd4oSi$qn" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSitH3" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="uploadKey" />
          <node concept="2m61tm" id="gbd4oSitH7" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="gbd4oSitc6" role="1GBnQ6">
          <property role="NXePf" value="Upload file part" />
        </node>
        <node concept="NX1gA" id="gbd4oSivKU" role="1GBnQ6">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Upload Key from requestFileUploadUrl" />
          <ref role="NX6Kv" node="gbd4oSitH3" resolve="uploadKey" />
        </node>
        <node concept="Nu42z" id="gbd4oSisAT" role="NuuwV">
          <property role="Nu42W" value="8E" />
        </node>
        <node concept="2m1R6W" id="gbd4oSisFa" role="2m6efq">
          <node concept="2m7Kf5" id="gbd4oSiwMM" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="url" />
            <node concept="2m5ndX" id="gbd4oSiwMQ" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="gbd4oSisFb" role="NuuwV">
            <property role="Nu42W" value="8D" />
          </node>
          <node concept="NX1gA" id="gbd4oSixjU" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Upload file part url" />
            <ref role="NX6Kv" node="gbd4oSiwMM" resolve="url" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="2tyCW$TWimx" role="2m5lHt">
      <property role="TrG5h" value="Calls" />
      <property role="3XOG$Z" value="calls" />
      <node concept="NpBTk" id="2tyCW$TWiJK" role="2m5mJr">
        <property role="TrG5h" value="CallRing" />
        <node concept="NXeRC" id="2tyCW$TWj9r" role="NXp_2">
          <property role="NXePf" value="Update about phone ring" />
        </node>
        <node concept="NX1gA" id="2tyCW$TWl8T" role="NXp_2">
          <property role="NX6R2" value="Calling User" />
          <ref role="NX6Kv" node="2tyCW$TWiJP" resolve="user" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$TWiJP" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="user" />
          <node concept="2m5mGg" id="2tyCW$TWiJT" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2tyCW$TWiJW" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndX" id="2tyCW$TWiK2" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2tyCW$TWiJL" role="NuuwV">
          <property role="Nu42W" value="31" />
        </node>
      </node>
      <node concept="NpBTk" id="2tyCW$TWjWh" role="2m5mJr">
        <property role="TrG5h" value="CallEnd" />
        <node concept="NXeRC" id="2tyCW$TWklZ" role="NXp_2">
          <property role="NXePf" value="Update about phone call end" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$TWjWt" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndX" id="2tyCW$TWjWx" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2tyCW$TWjWi" role="NuuwV">
          <property role="Nu42W" value="53" />
        </node>
      </node>
      <node concept="2m6fVq" id="2tyCW$TWmlt" role="2m5mJr">
        <property role="TrG5h" value="InitVoxSupport" />
        <node concept="2uC4CA" id="2tyCW$TWqqL" role="2uC9gA">
          <property role="2uC4DK" value="405" />
          <property role="2uC4Qe" value="VOX_NOT_AVAILABLE" />
          <property role="2uCiSL" value="VoxImplant not available" />
        </node>
        <node concept="NXeRC" id="2tyCW$TWn9L" role="1GBnQ6">
          <property role="NXePf" value="Initialization VoxImplant support on client" />
        </node>
        <node concept="Nu42z" id="2tyCW$TWmlu" role="NuuwV">
          <property role="Nu42W" value="82" />
        </node>
        <node concept="2m1R6W" id="2tyCW$TWmmk" role="2m6efq">
          <node concept="2m7Kf5" id="2tyCW$TWmmp" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="userName" />
            <node concept="2m5ndX" id="2tyCW$TWmmt" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="2tyCW$TWmmw" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="password" />
            <node concept="2m5ndX" id="2tyCW$TWmmA" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="2tyCW$TWmml" role="NuuwV">
            <property role="Nu42W" value="81" />
          </node>
          <node concept="NX1gA" id="2tyCW$TWnzx" role="1y2DgH">
            <property role="NX6R2" value="User Name for authentication" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2tyCW$TWmmp" resolve="userName" />
          </node>
          <node concept="NX1gA" id="2tyCW$TWnXb" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Password for authentication" />
            <ref role="NX6Kv" node="2tyCW$TWmmw" resolve="password" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="2tyCW$TWoMw" role="2m5mJr">
        <property role="TrG5h" value="GetVoxUser" />
        <node concept="NXeRC" id="2tyCW$TWpdg" role="1GBnQ6">
          <property role="NXePf" value="Get Vox user name for VoxImplant" />
        </node>
        <node concept="NX1gA" id="2tyCW$TWpB5" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Destination peer" />
          <ref role="NX6Kv" node="2tyCW$TWoN0" resolve="userPeer" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$TWoN0" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="userPeer" />
          <node concept="2m5mGg" id="2tyCW$TWoNh" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="2tyCW$TWoMx" role="NuuwV">
          <property role="Nu42W" value="83" />
        </node>
        <node concept="2m1R6W" id="2tyCW$TWoNk" role="2m6efq">
          <node concept="NX1gA" id="2tyCW$TWq0V" role="1y2DgH">
            <property role="NX6R2" value="Call number" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="2tyCW$TWoNp" resolve="callNumber" />
          </node>
          <node concept="2m7Kf5" id="2tyCW$TWoNp" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="callNumber" />
            <node concept="2m5ndX" id="2tyCW$TWoNt" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="2tyCW$TWoNl" role="NuuwV">
            <property role="Nu42W" value="84" />
          </node>
        </node>
        <node concept="2uC4CA" id="2tyCW$TWrfx" role="2uC9gA">
          <property role="2uC4DK" value="405" />
          <property role="2uC4Qe" value="VOX_NOT_AVAILABLE" />
          <property role="2uCiSL" value="VoxImplant not available" />
        </node>
      </node>
      <node concept="1Dx9M1" id="2tyCW$TWiJC" role="1Dx9rD">
        <property role="1Dx9K7" value="Performing voice calls with WebRTC and SIP" />
      </node>
    </node>
    <node concept="2m5mJO" id="2tyCW$U3Rwz" role="2m5lHt">
      <property role="TrG5h" value="llectro" />
      <property role="3XOG$Z" value="llectro" />
      <node concept="2m5naR" id="2tyCW$U3TcM" role="2m5mJr">
        <property role="TrG5h" value="Interest" />
        <node concept="NXeRC" id="2tyCW$U3U3I" role="NXodf">
          <property role="NXePf" value="User interests" />
        </node>
        <node concept="NX1gA" id="2tyCW$U3Uv0" role="NXodf">
          <property role="NX6R2" value="Interest Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U3TcS" resolve="id" />
        </node>
        <node concept="NX1gA" id="2tyCW$U3Uv8" role="NXodf">
          <property role="NX6R2" value="Interest title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U3TcZ" resolve="title" />
        </node>
        <node concept="NX1gA" id="2tyCW$U3Uvi" role="NXodf">
          <property role="NX6R2" value="Child Interests" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U3TCg" resolve="childInterests" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U3TcS" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="2tyCW$U3TcW" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U3TcZ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="2tyCW$U3Td5" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U44Po" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="isSelected" />
          <node concept="2m5ndN" id="2tyCW$U44Pz" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U3TCg" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="childInterests" />
          <node concept="2m5nlk" id="2tyCW$U3TCo" role="2m7DVh">
            <node concept="2m5mGg" id="2tyCW$U3TCu" role="3GJlyp">
              <ref role="2m5mJy" node="2tyCW$U3TcM" resolve="Interest" />
            </node>
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="57aXbevi0VB" role="2m5mJr">
        <property role="TrG5h" value="InitLlectro" />
        <node concept="2m7Kf5" id="57aXbevi0WY" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="screenWidth" />
          <node concept="2m5ndE" id="57aXbevi0X2" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="57aXbevi0X5" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="screenHeight" />
          <node concept="2m5ndE" id="57aXbevi0Xb" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="57aXbevi0VC" role="NuuwV">
          <property role="Nu42W" value="A3" />
        </node>
        <node concept="2m1Rp1" id="57aXbevi0Ww" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="57aXbevi0Wz" role="1GBnQ6">
          <property role="NXePf" value="Init Llectro support" />
        </node>
        <node concept="NX1gA" id="57aXbevi0Xh" role="1GBnQ6">
          <property role="NX6R2" value="Screen width" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="57aXbevi0WY" resolve="screenWidth" />
        </node>
        <node concept="NX1gA" id="57aXbevi0Xp" role="1GBnQ6">
          <property role="NX6R2" value="Screen height" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="57aXbevi0X5" resolve="screenHeight" />
        </node>
      </node>
      <node concept="2m6fVq" id="2tyCW$U3SLw" role="2m5mJr">
        <property role="TrG5h" value="GetAvailableInterests" />
        <node concept="NXeRC" id="2tyCW$U3VLY" role="1GBnQ6">
          <property role="NXePf" value="Getting all available interests" />
        </node>
        <node concept="Nu42z" id="2tyCW$U3SLx" role="NuuwV">
          <property role="Nu42W" value="98" />
        </node>
        <node concept="2m1R6W" id="2tyCW$U3UV7" role="2m6efq">
          <node concept="NX1gA" id="2tyCW$U3Wdr" role="1y2DgH">
            <property role="NX6R2" value="Root interests list" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="2tyCW$U3Vms" resolve="rootInterests" />
          </node>
          <node concept="2m7Kf5" id="2tyCW$U3Vms" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="rootInterests" />
            <node concept="2m5nlk" id="2tyCW$U3Vmw" role="2m7DVh">
              <node concept="2m5mGg" id="2tyCW$U3VmA" role="3GJlyp">
                <ref role="2m5mJy" node="2tyCW$U3TcM" resolve="Interest" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="2tyCW$U3UV8" role="NuuwV">
            <property role="Nu42W" value="99" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="2tyCW$U45jg" role="2m5mJr">
        <property role="TrG5h" value="EnableInterests" />
        <node concept="NXeRC" id="2tyCW$U48oT" role="1GBnQ6">
          <property role="NXePf" value="Enabling interest for current user" />
        </node>
        <node concept="NX1gA" id="2tyCW$U48Oz" role="1GBnQ6">
          <property role="NX6R2" value="Ids of enabled interests" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U47vx" resolve="interests" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U47vx" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="interests" />
          <node concept="2m5nlk" id="2tyCW$U47v_" role="2m7DVh">
            <node concept="2m5ndE" id="2tyCW$U47vF" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="2tyCW$U45jh" role="NuuwV">
          <property role="Nu42W" value="9D" />
        </node>
        <node concept="2m1Rp1" id="2tyCW$U473Y" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
      </node>
      <node concept="2m6fVq" id="2tyCW$U47Wv" role="2m5mJr">
        <property role="TrG5h" value="DisableInterests" />
        <node concept="NXeRC" id="2tyCW$U48OD" role="1GBnQ6">
          <property role="NXePf" value="Disabling interest for current user" />
        </node>
        <node concept="NX1gA" id="2tyCW$U48OJ" role="1GBnQ6">
          <property role="NX6R2" value="Ids of disabled interests" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U47X4" resolve="interests" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U47X4" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="interests" />
          <node concept="2m5nlk" id="2tyCW$U47X8" role="2m7DVh">
            <node concept="2m5ndE" id="2tyCW$U47Xe" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="2tyCW$U47Ww" role="NuuwV">
          <property role="Nu42W" value="9E" />
        </node>
        <node concept="2m1Rp1" id="2tyCW$U47Xh" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
      </node>
      <node concept="2m6fVq" id="2tyCW$U4km$" role="2m5mJr">
        <property role="TrG5h" value="NotifyAdView" />
        <node concept="NXeRC" id="2tyCW$U4lHh" role="1GBnQ6">
          <property role="NXePf" value="Notify about banner view" />
        </node>
        <node concept="NX1gA" id="2tyCW$U4lHn" role="1GBnQ6">
          <property role="NX6R2" value="Banner id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U4knT" resolve="bannerId" />
        </node>
        <node concept="NX1gA" id="2tyCW$U4m9P" role="1GBnQ6">
          <property role="NX6R2" value="viewDuration in miliseconds" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2tyCW$U4ko0" resolve="viewDuration" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U4knT" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="bannerId" />
          <node concept="2m5ndE" id="2tyCW$U4knX" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2tyCW$U4ko0" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="viewDuration" />
          <node concept="2m5ndE" id="2tyCW$U4ko6" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2tyCW$U4km_" role="NuuwV">
          <property role="Nu42W" value="A2" />
        </node>
        <node concept="2m1Rp1" id="2tyCW$U4lgS" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
      </node>
      <node concept="2m6fVq" id="Yg9rG6yUIi" role="2m5mJr">
        <property role="TrG5h" value="GetBalance" />
        <node concept="Nu42z" id="Yg9rG6yUIj" role="NuuwV">
          <property role="Nu42W" value="A4" />
        </node>
        <node concept="2m1R6W" id="Yg9rG6yULv" role="2m6efq">
          <node concept="2m7Kf5" id="Yg9rG6yULA" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="balance" />
            <node concept="2m5ndX" id="Yg9rG6yULX" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="Yg9rG6yULw" role="NuuwV">
            <property role="Nu42W" value="A5" />
          </node>
        </node>
        <node concept="NXeRC" id="Yg9rG6yUM0" role="1GBnQ6">
          <property role="NXePf" value="Get current balance" />
        </node>
      </node>
      <node concept="1Dx9M1" id="2tyCW$U3RVs" role="1Dx9rD">
        <property role="1Dx9K7" value="llectro Advertisment API" />
      </node>
    </node>
    <node concept="2m5mJO" id="3MpuFr6x9eS" role="2m5lHt">
      <property role="TrG5h" value="Config sync" />
      <property role="3XOG$Z" value="configs" />
      <node concept="1Dx9M1" id="gbd4oSi_sy" role="1Dx9rD">
        <property role="1Dx9K7" value="Parameter Syncronization across devices. Can be used for simple sync" />
      </node>
      <node concept="1Dx9M1" id="gbd4oSi_XD" role="1Dx9rD">
        <property role="1Dx9K7" value="across devices without rewriting server side code." />
      </node>
      <node concept="2m5naR" id="3MpuFr6xf4A" role="2m5mJr">
        <property role="TrG5h" value="Parameter" />
        <node concept="NXeRC" id="2tyCW$TVCsa" role="NXodf">
          <property role="NXePf" value="Syncing Parameter" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVCsg" role="NXodf">
          <property role="NX6R2" value="Key of parameter" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3MpuFr6xf5m" resolve="key" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVCso" role="NXodf">
          <property role="NX6R2" value="Value of parameter" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3MpuFr6xf79" resolve="value" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6xf5m" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="key" />
          <node concept="2m5ndX" id="3MpuFr6xf5q" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6xf79" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="value" />
          <node concept="2m5ndX" id="3MpuFr6xf7f" role="2m7DVh" />
        </node>
      </node>
      <node concept="2m6fVq" id="3MpuFr6xf33" role="2m5mJr">
        <property role="TrG5h" value="GetParameters" />
        <node concept="NXeRC" id="2tyCW$TVCPJ" role="1GBnQ6">
          <property role="NXePf" value="Getting Parameters" />
        </node>
        <node concept="Nu42z" id="3MpuFr6xf34" role="NuuwV">
          <property role="Nu42W" value="86" />
        </node>
        <node concept="2m1R6W" id="3MpuFr6xfaO" role="2m6efq">
          <node concept="NX1gA" id="2tyCW$TVDfj" role="1y2DgH">
            <property role="NX6R2" value="Current parameters" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="3MpuFr6xfaT" resolve="parameters" />
          </node>
          <node concept="2m7Kf5" id="3MpuFr6xfaT" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="parameters" />
            <node concept="2m5nlk" id="3MpuFr6xfaX" role="2m7DVh">
              <node concept="2m5mGg" id="3MpuFr6xfb3" role="3GJlyp">
                <ref role="2m5mJy" node="3MpuFr6xf4A" resolve="Parameter" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="3MpuFr6xfaP" role="NuuwV">
            <property role="Nu42W" value="87" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="3MpuFr6x9J6" role="2m5mJr">
        <property role="TrG5h" value="EditParameter" />
        <node concept="NXeRC" id="2tyCW$TVE1W" role="1GBnQ6">
          <property role="NXePf" value="Change parameter value" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVE22" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key of parameter" />
          <ref role="NX6Kv" node="3MpuFr6x9Jn" resolve="key" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVE2a" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Value of parameter" />
          <ref role="NX6Kv" node="3MpuFr6x9JM" resolve="value" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6x9Jn" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="key" />
          <node concept="2m5ndX" id="3MpuFr6x9Jr" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6x9JM" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="value" />
          <node concept="2m5ndX" id="3MpuFr6x9JS" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="3MpuFr6x9J7" role="NuuwV">
          <property role="Nu42W" value="80" />
        </node>
        <node concept="2m1Rp1" id="3MpuFr6x9Jk" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
      </node>
      <node concept="NpBTk" id="3MpuFr6xeti" role="2m5mJr">
        <property role="TrG5h" value="ParameterChanged" />
        <node concept="NXeRC" id="2tyCW$TVErA" role="NXp_2">
          <property role="NXePf" value="Update about parameter change" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVEP2" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key of parameter" />
          <ref role="NX6Kv" node="3MpuFr6xetG" resolve="key" />
        </node>
        <node concept="NX1gA" id="2tyCW$TVEPa" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Value of parameter" />
          <ref role="NX6Kv" node="3MpuFr6xetN" resolve="value" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6xetG" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="key" />
          <node concept="2m5ndX" id="3MpuFr6xetK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3MpuFr6xetN" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="value" />
          <node concept="2m5nlT" id="3MpuFr6xetX" role="2m7DVh">
            <node concept="2m5ndX" id="3MpuFr6xeu3" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="3MpuFr6xetj" role="NuuwV">
          <property role="Nu42W" value="83" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBBuJu" role="2m5lHt">
      <property role="TrG5h" value="Push" />
      <property role="3XOG$Z" value="push" />
      <node concept="1Dx9M1" id="EUEXKTjk6X" role="1Dx9rD">
        <property role="1Dx9K7" value="Vendor's pushes for receiving push notifications." />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjkOL" role="1Dx9rD">
        <property role="1Dx9K7" value="Push notification contains current sequence number of main sequence." />
      </node>
      <node concept="2m6fVq" id="GBscvBBv8U" role="2m5mJr">
        <property role="TrG5h" value="RegisterGooglePush" />
        <node concept="2m7Kf5" id="GBscvBBv92" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="projectId" />
          <node concept="2m5ndQ" id="GBscvBBv96" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="1qxBM7m3kL" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="token" />
          <node concept="2m5ndX" id="1qxBM7m3kR" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBv8V" role="NuuwV">
          <property role="Nu42W" value="33" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBv99" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAt" role="1GBnQ6">
          <property role="NXePf" value="Registering push token on server" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAu" role="1GBnQ6">
          <property role="NX6R2" value="Project Id of token" />
          <property role="1GSvIU" value="hidden" />
          <ref role="NX6Kv" node="GBscvBBv92" resolve="projectId" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAv" role="1GBnQ6">
          <property role="NX6R2" value="token value" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="1qxBM7m3kL" resolve="token" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBv9j" role="2m5mJr">
        <property role="TrG5h" value="RegisterApplePush" />
        <node concept="2m7Kf5" id="GBscvBBv9w" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="apnsKey" />
          <node concept="2m5ndE" id="GBscvBBv9$" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBv9B" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="token" />
          <node concept="2m5ndX" id="GBscvBBv9H" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBv9k" role="NuuwV">
          <property role="Nu42W" value="4C" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBv9K" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_z" role="1GBnQ6">
          <property role="NXePf" value="Registering apple push on server" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_$" role="1GBnQ6">
          <property role="NX6R2" value="apns key id" />
          <ref role="NX6Kv" node="GBscvBBv9w" resolve="apnsKey" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo__" role="1GBnQ6">
          <property role="NX6R2" value="token value" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBBv9B" resolve="token" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBvmU" role="2m5mJr">
        <property role="TrG5h" value="UnregisterPush" />
        <node concept="Nu42z" id="GBscvBBvmV" role="NuuwV">
          <property role="Nu42W" value="34" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBvni" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBJ" role="1GBnQ6">
          <property role="NXePf" value="Unregister push" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBB6e7" role="2m5lHt">
      <property role="TrG5h" value="Peers" />
      <property role="3XOG$Z" value="peers" />
      <node concept="1Dx9M1" id="EUEXKTjjpb" role="1Dx9rD">
        <property role="1Dx9K7" value="Peer is an identificator of specific conversation." />
      </node>
      <node concept="2m488m" id="GBscvBB6fj" role="2m5mJr">
        <property role="TrG5h" value="PeerType" />
        <node concept="2m7y0F" id="GBscvBB6fl" role="2m7ymf">
          <property role="TrG5h" value="Private" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="GBscvBB6fp" role="2m7ymf">
          <property role="TrG5h" value="Group" />
          <property role="2m7y0m" value="2" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBB6fx" role="2m5mJr">
        <property role="TrG5h" value="Peer" />
        <node concept="NXeRC" id="7UKSaUukma2" role="NXodf">
          <property role="NXePf" value="Peer" />
        </node>
        <node concept="NX1gA" id="7UKSaUukma8" role="NXodf">
          <property role="NX6R2" value="Peer Type" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6fB" resolve="type" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmag" role="NXodf">
          <property role="NX6R2" value="Peer Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6fI" resolve="id" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6fB" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="type" />
          <node concept="3GJkcs" id="GBscvBB6fF" role="2m7DVh">
            <ref role="3GJkik" node="GBscvBB6fj" resolve="PeerType" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBB6fI" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="GBscvBB6fO" role="2m7DVh" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBB6hj" role="2m5mJr">
        <property role="TrG5h" value="OutPeer" />
        <node concept="2m7Kf5" id="GBscvBB6hu" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="type" />
          <node concept="3GJkcs" id="GBscvBB6hy" role="2m7DVh">
            <ref role="3GJkik" node="GBscvBB6fj" resolve="PeerType" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBB6h_" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="GBscvBB6hF" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6hI" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBB6hQ" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="7UKSaUukmal" role="NXodf">
          <property role="NXePf" value="Out peer with access hash" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmaq" role="NXodf">
          <property role="NX6R2" value="Peer Type" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6hu" resolve="type" />
        </node>
        <node concept="NX1gA" id="7UKSaUukma_" role="NXodf">
          <property role="NX6R2" value="Peer Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6h_" resolve="id" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmaJ" role="NXodf">
          <property role="NX6R2" value="Peer access hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB6hI" resolve="accessHash" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBB6ia" role="2m5mJr">
        <property role="TrG5h" value="UserOutPeer" />
        <node concept="NXeRC" id="7UKSaUukmaR" role="NXodf">
          <property role="NXePf" value="User's out peer" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmaX" role="NXodf">
          <property role="NX6R2" value="User's id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6is" resolve="uid" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmb5" role="NXodf">
          <property role="NX6R2" value="User's access hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB6iz" resolve="accessHash" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6is" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5ndE" id="GBscvBB6iw" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6iz" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBB6iD" role="2m7DVh" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBB6j2" role="2m5mJr">
        <property role="TrG5h" value="GroupOutPeer" />
        <node concept="NXeRC" id="7UKSaUukmbc" role="NXodf">
          <property role="NXePf" value="Group's out peer" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmbi" role="NXodf">
          <property role="NX6R2" value="Group's Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6jp" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmbq" role="NXodf">
          <property role="NX6R2" value="Group's access hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="GBscvBB6jw" resolve="accessHash" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6jp" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="2m5ndE" id="GBscvBB6jt" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6jw" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="GBscvBB6l9" role="2m7DVh" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBB_eZ" role="2m5lHt">
      <property role="TrG5h" value="Sequence and Updates" />
      <property role="3XOG$Z" value="sequence" />
      <node concept="1Dx9M1" id="EUEXKTjlyC" role="1Dx9rD">
        <property role="1Dx9K7" value="Each device has it's own update sequence. At the begining application request initial sequence state by" />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjmgw" role="1Dx9rD">
        <property role="1Dx9K7" value="calling GetState. On each application restart or NewSessionCreated application calls GetDifference for receiving" />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjmY_" role="1Dx9rD">
        <property role="1Dx9K7" value="updates in update sequence." />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjmYx" role="1Dx9rD">
        <property role="1Dx9K7" value="GetState and GetDifference automatically subscribes session to receiving updates in session." />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjnlC" role="1Dx9rD">
        <property role="1Dx9K7" value="Each update has seq and state. Seq is sequental index of updated and used for detecting of holes in update sequence" />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjo3G" role="1Dx9rD">
        <property role="1Dx9K7" value="(because of server failure or session die) on client side." />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjo3N" role="1Dx9rD">
        <property role="1Dx9K7" value="All updates needed to be processed in partucular order according to seq values." />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjoqW" role="1Dx9rD">
        <property role="1Dx9K7" value="In some updates there can be references to users that are not available at client yer. In this case application need" />
      </node>
      <node concept="1Dx9M1" id="EUEXKTjor5" role="1Dx9rD">
        <property role="1Dx9K7" value="to ignore such update and init getting difference." />
      </node>
      <node concept="3GIgwz" id="GBscvBBMMf" role="2m5mJr">
        <property role="TrG5h" value="SeqUpdate" />
        <node concept="NXeRC" id="7UKSaUukmbx" role="NXpPy">
          <property role="NXePf" value="Sequence update" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmbB" role="NXpPy">
          <property role="NX6R2" value="Sequence number of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBN2L" resolve="seq" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmbJ" role="NXpPy">
          <property role="NX6R2" value="Sequece state of update" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBN2S" resolve="state" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmbT" role="NXpPy">
          <property role="NX6R2" value="header of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBN31" resolve="updateHeader" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmc5" role="NXpPy">
          <property role="NX6R2" value="The update" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBN3c" resolve="update" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBN2L" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="GBscvBBN2P" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBN2S" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="55bmeIQ7AKW" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBN31" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="updateHeader" />
          <node concept="2m5ndE" id="GBscvBBN39" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBN3c" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="update" />
          <node concept="2m61tm" id="GBscvBBN3m" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBMMg" role="NuuwV">
          <property role="Nu42W" value="0D" />
        </node>
      </node>
      <node concept="3GIgwz" id="GBscvBBNRo" role="2m5mJr">
        <property role="TrG5h" value="FatSeqUpdate" />
        <node concept="2m7Kf5" id="GBscvBBNSv" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="GBscvBBNSz" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBNSA" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="55bmeIQ7AKT" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBNSJ" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="updateHeader" />
          <node concept="2m5ndE" id="GBscvBBNSR" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBNSU" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="update" />
          <node concept="2m61tm" id="GBscvBBNT4" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBNT7" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="GBscvBBNTj" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBO9j" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBO9m" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="groups" />
          <node concept="2m5nlk" id="GBscvBBO9_" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBO9F" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBNRp" role="NuuwV">
          <property role="Nu42W" value="49" />
        </node>
        <node concept="NXeRC" id="7UKSaUukmcc" role="NXpPy">
          <property role="NXePf" value="Fat sequence update with additional data" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmch" role="NXpPy">
          <property role="NX6R2" value="Sequence number of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBNSv" resolve="seq" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmcp" role="NXpPy">
          <property role="NX6R2" value="Sequence state of update" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBNSA" resolve="state" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmcz" role="NXpPy">
          <property role="NX6R2" value="header of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBNSJ" resolve="updateHeader" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmcJ" role="NXpPy">
          <property role="NX6R2" value="The update" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBNSU" resolve="update" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmcX" role="NXpPy">
          <property role="NX6R2" value="Users that are referenced in update " />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBNT7" resolve="users" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmdd" role="NXpPy">
          <property role="NX6R2" value="Groups that are referenced in update" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBO9m" resolve="groups" />
        </node>
      </node>
      <node concept="3GIgwz" id="GBscvBBOWz" role="2m5mJr">
        <property role="TrG5h" value="WeakUpdate" />
        <node concept="NXeRC" id="7UKSaUukmdo" role="NXpPy">
          <property role="NXePf" value="Out of sequence update (for typing and online statuses)" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmdu" role="NXpPy">
          <property role="NX6R2" value="Date of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBOXU" resolve="date" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmdA" role="NXpPy">
          <property role="NX6R2" value="Header of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBOY1" resolve="updateHeader" />
        </node>
        <node concept="NX1gA" id="7UKSaUukme5" role="NXpPy">
          <property role="NX6R2" value="The update" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBOYa" resolve="update" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBOXU" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="date" />
          <node concept="2m5ndQ" id="GBscvBBOXY" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBOY1" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="updateHeader" />
          <node concept="2m5ndE" id="GBscvBBOY7" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBOYa" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="update" />
          <node concept="2m61tm" id="GBscvBBOYi" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="GBscvBBOW$" role="NuuwV">
          <property role="Nu42W" value="1A" />
        </node>
      </node>
      <node concept="3GIgwz" id="GBscvBBPLE" role="2m5mJr">
        <property role="TrG5h" value="SeqUpdateTooLong" />
        <node concept="NXeRC" id="7UKSaUukmed" role="NXpPy">
          <property role="NXePf" value="Notification about requiring performing manual GetDifference" />
        </node>
        <node concept="Nu42z" id="GBscvBBPLF" role="NuuwV">
          <property role="Nu42W" value="19" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBAdf" role="2m5mJr">
        <property role="TrG5h" value="GetState" />
        <node concept="Nu42z" id="GBscvBBAdg" role="NuuwV">
          <property role="Nu42W" value="09" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBAdN" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAE" role="1GBnQ6">
          <property role="NXePf" value="Get main sequence state" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBB_GO" role="2m5mJr">
        <property role="TrG5h" value="DifferenceUpdate" />
        <node concept="NXeRC" id="7UKSaUukmem" role="NXodf">
          <property role="NXePf" value="Update from GetDifference" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmes" role="NXodf">
          <property role="NX6R2" value="Header of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB_GY" resolve="updateHeader" />
        </node>
        <node concept="NX1gA" id="7UKSaUukme$" role="NXodf">
          <property role="NX6R2" value="The update" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBB_H5" resolve="update" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB_GY" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="updateHeader" />
          <node concept="2m5ndE" id="GBscvBB_H2" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB_H5" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="update" />
          <node concept="2m61tm" id="GBscvBB_Hb" role="2m7DVh" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBB_Gl" role="2m5mJr">
        <property role="TrG5h" value="GetDifference" />
        <node concept="NXeRC" id="2EAJ7H6foA2" role="1GBnQ6">
          <property role="NXePf" value="Getting difference of sequence" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERhuh" role="1GBnQ6">
          <property role="NX6R2" value="Sequence number" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB_Gr" resolve="seq" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERhup" role="1GBnQ6">
          <property role="NX6R2" value="Sequence state" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBB_Gy" resolve="state" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB_Gr" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="GBscvBB_Gv" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB_Gy" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="55bmeIQ7BbF" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBB_Gm" role="NuuwV">
          <property role="Nu42W" value="0B" />
        </node>
        <node concept="2m1R6W" id="GBscvBB_W5" role="2m6efq">
          <node concept="NX1gA" id="2tyCW$U1bP_" role="1y2DgH">
            <property role="NX6R2" value="Seq of LAST update in updates" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="GBscvBB_Wa" resolve="seq" />
          </node>
          <node concept="NX1gA" id="2tyCW$U1bPN" role="1y2DgH">
            <property role="NX6R2" value="State of LAST update in updates" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="GBscvBB_Wh" resolve="state" />
          </node>
          <node concept="NX1gA" id="2tyCW$U1bPF" role="1y2DgH">
            <property role="NX6R2" value="Users referenced in updates" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBB_Wq" resolve="users" />
          </node>
          <node concept="NX1gA" id="2tyCW$U1ch5" role="1y2DgH">
            <property role="NX6R2" value="Groups referenced in updates" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBB_WF" resolve="groups" />
          </node>
          <node concept="NX1gA" id="2tyCW$U1d6R" role="1y2DgH">
            <property role="NX6R2" value="Updates" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="GBscvBB_WZ" resolve="updates" />
          </node>
          <node concept="NX1gA" id="2tyCW$U1d79" role="1y2DgH">
            <property role="NX6R2" value="Need to perform other difference" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="GBscvBB_Xm" resolve="needMore" />
          </node>
          <node concept="2m7Kf5" id="GBscvBB_Wa" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="GBscvBB_We" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="GBscvBB_Wh" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="55bmeIQ7BbI" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBB_Wq" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="GBscvBB_Wy" role="2m7DVh">
              <node concept="2m5mGg" id="3m8vlV8pH0U" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBB_WF" role="2m0hLx">
            <property role="2m7DUN" value="6" />
            <property role="TrG5h" value="groups" />
            <node concept="2m5nlk" id="GBscvBB_WQ" role="2m7DVh">
              <node concept="2m5mGg" id="GBscvBB_WW" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBB_WZ" role="2m0hLx">
            <property role="2m7DUN" value="4" />
            <property role="TrG5h" value="updates" />
            <node concept="2m5nlk" id="GBscvBB_Xd" role="2m7DVh">
              <node concept="2m5mGg" id="GBscvBB_Xj" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB_GO" resolve="DifferenceUpdate" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBB_Xm" role="2m0hLx">
            <property role="2m7DUN" value="5" />
            <property role="TrG5h" value="needMore" />
            <node concept="2m5ndN" id="GBscvBB_XB" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="GBscvBB_W6" role="NuuwV">
            <property role="Nu42W" value="0C" />
          </node>
        </node>
      </node>
      <node concept="NvyAe" id="GBscvBBAXl" role="2m5mJr" />
      <node concept="2m6fVq" id="GBscvBBBd$" role="2m5mJr">
        <property role="TrG5h" value="SubscribeToOnline" />
        <node concept="2m7Kf5" id="GBscvBBBec" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="GBscvBBBeg" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBBem" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBBd_" role="NuuwV">
          <property role="Nu42W" value="20" />
        </node>
        <node concept="2m1Rp1" id="55bmeIQ92fp" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6fo_B" role="1GBnQ6">
          <property role="NXePf" value="Subscribing for users online" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6fo_C" role="1GBnQ6">
          <property role="NX6R2" value="Users for subscription" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBBec" resolve="users" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBBf4" role="2m5mJr">
        <property role="TrG5h" value="SubscribeFromOnline" />
        <node concept="2m7Kf5" id="GBscvBBBfM" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="GBscvBBBfQ" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBBfZ" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBBf5" role="NuuwV">
          <property role="Nu42W" value="21" />
        </node>
        <node concept="2m1Rp1" id="55bmeIQ92fm" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foBK" role="1GBnQ6">
          <property role="NXePf" value="Removing subscription for users online" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foBL" role="1GBnQ6">
          <property role="NX6R2" value="Users of subscriptions" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBBfM" resolve="users" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBBgN" role="2m5mJr">
        <property role="TrG5h" value="SubscribeToGroupOnline" />
        <node concept="2m7Kf5" id="GBscvBBBhB" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groups" />
          <node concept="2m5nlk" id="GBscvBBBhF" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBBhL" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBBgO" role="NuuwV">
          <property role="Nu42W" value="4A" />
        </node>
        <node concept="2m1Rp1" id="55bmeIQ92fj" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foB6" role="1GBnQ6">
          <property role="NXePf" value="Subscribing for groups online" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foB7" role="1GBnQ6">
          <property role="NX6R2" value="Groups for subscription" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBBhB" resolve="groups" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBBiF" role="2m5mJr">
        <property role="TrG5h" value="SubscribeFromGroupOnline" />
        <node concept="2m7Kf5" id="GBscvBBBj_" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groups" />
          <node concept="2m5nlk" id="GBscvBBBjD" role="2m7DVh">
            <node concept="2m5mGg" id="GBscvBBBjJ" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBBiG" role="NuuwV">
          <property role="Nu42W" value="4B" />
        </node>
        <node concept="2m1Rp1" id="55bmeIQ92fg" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foA8" role="1GBnQ6">
          <property role="NXePf" value="Removing subscription for groups online" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foA9" role="1GBnQ6">
          <property role="NX6R2" value="Groups of subscriptions" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBBj_" resolve="groups" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="GBscvBB6y8" role="2m5lHt">
      <property role="TrG5h" value="Miscellaneous" />
      <property role="3XOG$Z" value="misc" />
      <node concept="2m62dX" id="GBscvBB6_K" role="2m5mJr">
        <property role="TrG5h" value="Void" />
        <node concept="NXeRC" id="7UKSaUukmeF" role="NXp4Y">
          <property role="NXePf" value="Empty response" />
        </node>
        <node concept="Nu42z" id="GBscvBB6_L" role="NuuwV">
          <property role="Nu42W" value="32" />
        </node>
      </node>
      <node concept="2m62dX" id="GBscvBB6_W" role="2m5mJr">
        <property role="TrG5h" value="Seq" />
        <node concept="NXeRC" id="7UKSaUukmeK" role="NXp4Y">
          <property role="NXePf" value="Sequence response. Methods that return this value must process response in particular order" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmeQ" role="NXp4Y">
          <property role="NX6R2" value="Sequence number of response" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6A3" resolve="seq" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmeY" role="NXp4Y">
          <property role="NX6R2" value="Sequence state of response" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6Aa" resolve="state" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6A3" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="GBscvBB6A7" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6Aa" role="2m0hLx">
          <property role="TrG5h" value="state" />
          <property role="2m7DUN" value="2" />
          <node concept="wb0Ql" id="55bmeIQ7BAw" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBB6_X" role="NuuwV">
          <property role="Nu42W" value="48" />
        </node>
      </node>
      <node concept="2m62dX" id="2vxDjotnSoJ" role="2m5mJr">
        <property role="TrG5h" value="SeqDate" />
        <node concept="NXeRC" id="2vxDjotnTg0" role="NXp4Y">
          <property role="NXePf" value="Sequence response with date. Methods that return this value must process response in particular order" />
        </node>
        <node concept="NX1gA" id="2vxDjotnTg6" role="NXp4Y">
          <property role="NX6R2" value="Sequence number of response" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2vxDjotnSpn" resolve="seq" />
        </node>
        <node concept="NX1gA" id="2vxDjotnTge" role="NXp4Y">
          <property role="NX6R2" value="Sequence state of response" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2vxDjotnSpu" resolve="state" />
        </node>
        <node concept="NX1gA" id="2vxDjotnTgo" role="NXp4Y">
          <property role="NX6R2" value="Date of response" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2vxDjotnSpB" resolve="date" />
        </node>
        <node concept="2m7Kf5" id="2vxDjotnSpn" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="2vxDjotnSpr" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2vxDjotnSpu" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="2vxDjotnSp$" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2vxDjotnSpB" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotnSpJ" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="2vxDjotnSoK" role="NuuwV">
          <property role="Nu42W" value="66" />
        </node>
      </node>
      <node concept="2m5naR" id="GBscvBB6Cp" role="2m5mJr">
        <property role="TrG5h" value="Config" />
        <node concept="NXeRC" id="7UKSaUukmfK" role="NXodf">
          <property role="NXePf" value="Configuration of system" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmfQ" role="NXodf">
          <property role="NX6R2" value="Current maximum group size" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBB6C$" resolve="maxGroupSize" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6C$" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="maxGroupSize" />
          <node concept="2m5ndE" id="GBscvBB6CC" role="2m7DVh" />
        </node>
      </node>
      <node concept="NpBTk" id="GBscvBBjCE" role="2m5mJr">
        <property role="TrG5h" value="Config" />
        <node concept="NXeRC" id="7UKSaUukmD4" role="NXp_2">
          <property role="NXePf" value="Update about config change" />
        </node>
        <node concept="NX1gA" id="7UKSaUukmDa" role="NXp_2">
          <property role="NX6R2" value="new config" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBjCU" resolve="config" />
        </node>
        <node concept="2m7Kf5" id="GBscvBBjCU" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="config" />
          <node concept="2m5mGg" id="GBscvBBjCY" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6Cp" resolve="Config" />
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBjCF" role="NuuwV">
          <property role="Nu42W" value="2A" />
        </node>
      </node>
    </node>
  </node>
</model>

