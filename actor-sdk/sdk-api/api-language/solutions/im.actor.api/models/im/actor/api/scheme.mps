<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:02443686-c535-4c25-85f8-130218c98e66(im.actor.api.scheme)">
  <persistence version="9" />
  <languages>
    <use id="77fdf769-432b-4ede-8171-050f8dee73fc" name="im.actor.language" version="-1" />
  </languages>
  <imports />
  <registry>
    <language id="77fdf769-432b-4ede-8171-050f8dee73fc" name="im.actor.language">
      <concept id="2348480312265565650" name="im.actor.language.structure.IStruct" flags="ng" index="2m0hYO">
        <child id="2348480312265565703" name="attributes" index="2m0hLx" />
      </concept>
      <concept id="2348480312265228628" name="im.actor.language.structure.IHeaderStruct" flags="ng" index="2m1zGM">
        <child id="4689615199750927382" name="header" index="NuuwV" />
      </concept>
      <concept id="2348480312265149402" name="im.actor.language.structure.ResponseRefAnonymous" flags="ng" index="2m1R6W">
        <child id="5253915025833407436" name="docs" index="1y2DgH" />
      </concept>
      <concept id="2348480312265149479" name="im.actor.language.structure.ResponseRefValue" flags="ng" index="2m1Rp1">
        <reference id="2348480312265340979" name="response" index="2m1o9l" />
      </concept>
      <concept id="2348480312264620144" name="im.actor.language.structure.Enum" flags="ng" index="2m488m">
        <child id="2348480312264712169" name="attributes" index="2m7ymf" />
      </concept>
      <concept id="2348480312264233334" name="im.actor.language.structure.StructType" flags="ng" index="2m5mGg">
        <reference id="2348480312264233348" name="struct" index="2m5mJy" />
      </concept>
      <concept id="2348480312264233362" name="im.actor.language.structure.ApiSection" flags="ng" index="2m5mJO">
        <property id="3857470926884615265" name="package" index="3XOG$Z" />
        <child id="2348480312264233405" name="definitions" index="2m5mJr" />
        <child id="2861239048481128232" name="docs" index="1Dx9rD" />
      </concept>
      <concept id="2348480312264231121" name="im.actor.language.structure.Struct" flags="ng" index="2m5naR">
        <property id="2838010799854839786" name="isExpandable" index="tsOgz" />
        <property id="5312209286555312009" name="hasInterface" index="w4tQU" />
        <reference id="5312209286555405644" name="interface" index="w4$XZ" />
        <child id="773119248390105235" name="docs" index="NXodf" />
        <child id="5857873509723526645" name="header" index="3BtCOu" />
      </concept>
      <concept id="2348480312264231180" name="im.actor.language.structure.Int32" flags="ng" index="2m5ndE" />
      <concept id="2348480312264231189" name="im.actor.language.structure.Boolean" flags="ng" index="2m5ndN" />
      <concept id="2348480312264231184" name="im.actor.language.structure.Int64" flags="ng" index="2m5ndQ" />
      <concept id="2348480312264231195" name="im.actor.language.structure.String" flags="ng" index="2m5ndX" />
      <concept id="2348480312264232779" name="im.actor.language.structure.ApiDescription" flags="ng" index="2m5nkH">
        <property id="2838010799853060276" name="version" index="u_6dX" />
        <property id="291384077092427558" name="scalaPackage" index="WhUdw" />
        <property id="5857873509721316771" name="javaPackage" index="3BlOl8" />
        <child id="2348480312264237371" name="sections" index="2m5lHt" />
        <child id="5857873509721229475" name="aliases" index="3Bkp18" />
      </concept>
      <concept id="2348480312264232754" name="im.actor.language.structure.List" flags="ng" index="2m5nlk">
        <child id="803735062395365470" name="type" index="3GJlyp" />
      </concept>
      <concept id="2348480312264232735" name="im.actor.language.structure.Optional" flags="ng" index="2m5nlT">
        <child id="803735062394906775" name="type" index="3GH5xg" />
      </concept>
      <concept id="2348480312265108784" name="im.actor.language.structure.Bytes" flags="ng" index="2m61tm" />
      <concept id="2348480312265103643" name="im.actor.language.structure.Response" flags="ng" index="2m62dX">
        <child id="773119248390109922" name="docs" index="NXp4Y" />
      </concept>
      <concept id="2348480312265114812" name="im.actor.language.structure.Rpc" flags="ng" index="2m6fVq">
        <child id="2348480312265120188" name="response" index="2m6efq" />
        <child id="4092665470043063721" name="throws" index="2uC9gA" />
        <child id="3073351033372262909" name="docs" index="1GBnQ6" />
      </concept>
      <concept id="2348480312264710733" name="im.actor.language.structure.EnumAttribute" flags="ng" index="2m7y0F">
        <property id="2348480312264710768" name="id" index="2m7y0m" />
      </concept>
      <concept id="2348480312264653219" name="im.actor.language.structure.StructAttribute" flags="ng" index="2m7Kf5">
        <property id="2348480312264746197" name="id" index="2m7DUN" />
        <property id="2838010799853749721" name="isDeprecated" index="toYog" />
        <child id="2348480312264746167" name="type" index="2m7DVh" />
      </concept>
      <concept id="4092665470043052969" name="im.actor.language.structure.MethodThrows" flags="ng" index="2uC4CA">
        <property id="4092665470043053055" name="errorCode" index="2uC4DK" />
        <property id="4092665470043053057" name="errorTag" index="2uC4Qe" />
        <property id="4092665470043111358" name="description" index="2uCiSL" />
      </concept>
      <concept id="5312209286554516176" name="im.actor.language.structure.Trait" flags="ng" index="w93zz">
        <property id="6700515326227281642" name="isContainer" index="1FaRnq" />
      </concept>
      <concept id="5312209286553980838" name="im.actor.language.structure.AliasType" flags="ng" index="wb0Ql">
        <reference id="5312209286553980954" name="alias" index="wb18D" />
      </concept>
      <concept id="5312209286553449813" name="im.actor.language.structure.ApiAlias" flags="ng" index="wd7tA">
        <child id="5312209286553509039" name="sourceType" index="wdlUs" />
      </concept>
      <concept id="4689615199751283321" name="im.actor.language.structure.Update" flags="ng" index="NpBTk">
        <child id="773119248390107806" name="docs" index="NXp_2" />
      </concept>
      <concept id="4689615199750888590" name="im.actor.language.structure.HeaderKey" flags="ng" index="Nu42z">
        <property id="4689615199750888593" name="hexValue" index="Nu42W" />
      </concept>
      <concept id="4689615199750780323" name="im.actor.language.structure.ApiEmptyDef" flags="ng" index="NvyAe" />
      <concept id="4689615199750788559" name="im.actor.language.structure.ApiComment" flags="ng" index="NvWBy">
        <property id="4689615199750789856" name="text" index="NvWrd" />
      </concept>
      <concept id="773119248390078458" name="im.actor.language.structure.StructDocParameter" flags="ng" index="NX1gA">
        <property id="773119248390080030" name="description" index="NX6R2" />
        <property id="3073351033373018049" name="category" index="1GSvIU" />
        <reference id="773119248390080451" name="paramter" index="NX6Kv" />
      </concept>
      <concept id="773119248390047284" name="im.actor.language.structure.StructDocComment" flags="ng" index="NXeRC">
        <property id="773119248390047379" name="content" index="NXePf" />
      </concept>
      <concept id="5857873509721568548" name="im.actor.language.structure.TraitType" flags="ng" index="3BlaRf">
        <reference id="5857873509721852744" name="trait" index="3BrLez" />
      </concept>
      <concept id="2861239048481125696" name="im.actor.language.structure.SectionDoc" flags="ng" index="1Dx9M1">
        <property id="2861239048481125830" name="text" index="1Dx9K7" />
      </concept>
      <concept id="803735062395648228" name="im.actor.language.structure.UpdateBox" flags="ng" index="3GIgwz">
        <child id="773119248390108862" name="docs" index="NXpPy" />
      </concept>
      <concept id="803735062395533120" name="im.actor.language.structure.Double" flags="ng" index="3GIWu7" />
      <concept id="803735062395368411" name="im.actor.language.structure.EnumType" flags="ng" index="3GJkcs">
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
    <property role="3BlOl8" value="im.actor.core.api" />
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
    <node concept="wd7tA" id="6u8Nlnzqdrd" role="3Bkp18">
      <property role="TrG5h" value="msec" />
      <node concept="2m5ndQ" id="6u8Nlnzqdrn" role="wdlUs" />
    </node>
    <node concept="wd7tA" id="6u8Nlnzqdrq" role="3Bkp18">
      <property role="TrG5h" value="sec" />
      <node concept="2m5ndE" id="6u8NlnzqdrA" role="wdlUs" />
    </node>
    <node concept="wd7tA" id="5qm50Y0e3uz" role="3Bkp18">
      <property role="TrG5h" value="userId" />
      <node concept="2m5ndE" id="5qm50Y0e3uL" role="wdlUs" />
    </node>
    <node concept="wd7tA" id="5qm50Y0e3uO" role="3Bkp18">
      <property role="TrG5h" value="groupId" />
      <node concept="2m5ndE" id="5qm50Y0e3v4" role="wdlUs" />
    </node>
    <node concept="wd7tA" id="5_CDdZ2q761" role="3Bkp18">
      <property role="TrG5h" value="keyId" />
      <node concept="2m5ndE" id="5_CDdZ2q76j" role="wdlUs" />
    </node>
    <node concept="wd7tA" id="5_CDdZ2q76m" role="3Bkp18">
      <property role="TrG5h" value="keyGroupId" />
      <node concept="2m5ndE" id="5_CDdZ2q76E" role="wdlUs" />
    </node>
    <node concept="2m5mJO" id="GBscvBB6uy" role="2m5lHt">
      <property role="TrG5h" value="Authentication" />
      <property role="3XOG$Z" value="auth" />
      <node concept="2m488m" id="6Fl2chwBTwz" role="2m5mJr">
        <property role="TrG5h" value="PhoneActivationType" />
        <node concept="2m7y0F" id="6Fl2chwBTw_" role="2m7ymf">
          <property role="TrG5h" value="CODE" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="6Fl2chwBT_Y" role="2m7ymf">
          <property role="TrG5h" value="PASSWORD" />
          <property role="2m7y0m" value="2" />
        </node>
      </node>
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
        <node concept="2m7y0F" id="6Fl2chwBTr8" role="2m7ymf">
          <property role="TrG5h" value="PASSWORD" />
          <property role="2m7y0m" value="3" />
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
        <node concept="NX1gA" id="1GlYFhnboWX" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="TimeZone of device" />
          <ref role="NX6Kv" node="1GlYFhnbnF_" resolve="timeZone" />
        </node>
        <node concept="NX1gA" id="1GlYFhnbp$X" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Preferred languages of device" />
          <ref role="NX6Kv" node="1GlYFhnbokI" resolve="preferredLanguages" />
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
        <node concept="2m7Kf5" id="1GlYFhnbnF_" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="1GlYFhnbok_" role="2m7DVh">
            <node concept="2m5ndX" id="1GlYFhnbokF" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1GlYFhnbokI" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="1GlYFhnbokZ" role="2m7DVh">
            <node concept="2m5ndX" id="1GlYFhnbol5" role="3GJlyp" />
          </node>
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
          <node concept="2m7Kf5" id="6Fl2chwBUnH" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="activationType" />
            <node concept="2m5nlT" id="6Fl2chwBUnP" role="2m7DVh">
              <node concept="3GJkcs" id="6Fl2chwBUnV" role="3GH5xg">
                <ref role="3GJkik" node="6Fl2chwBTwz" resolve="PhoneActivationType" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="3zgy61Em3uV" role="NuuwV">
            <property role="Nu42W" value="C1" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="7fL1OLLLUpo" role="2m5mJr">
        <property role="TrG5h" value="SendCodeByPhoneCall" />
        <node concept="2m7Kf5" id="7fL1OLLLXdT" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="transactionHash" />
          <node concept="2m5ndX" id="7fL1OLLLXdX" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="7fL1OLLLUpp" role="NuuwV">
          <property role="Nu42W" value="C5" />
        </node>
        <node concept="2m1Rp1" id="7fL1OLLLXe0" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="7fL1OLLLXe3" role="1GBnQ6">
          <property role="NXePf" value="Dial phone and dictate auth code" />
        </node>
        <node concept="NX1gA" id="7fL1OLLLXe8" role="1GBnQ6">
          <property role="NX6R2" value="Transaction hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="7fL1OLLLXdT" resolve="transactionHash" />
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
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61ElWcS" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="3zgy61Em7qK" role="1GBnQ6">
          <property role="NX6R2" value="Device Title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3zgy61ElWd5" resolve="deviceTitle" />
        </node>
        <node concept="NX1gA" id="1GlYFhnbqP$" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="TimeZone of device" />
          <ref role="NX6Kv" node="1GlYFhnbqcQ" resolve="timeZone" />
        </node>
        <node concept="NX1gA" id="1GlYFhnbrtG" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Preferred languages" />
          <ref role="NX6Kv" node="1GlYFhnbqdd" resolve="preferredLanguages" />
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
        <node concept="2m7Kf5" id="1GlYFhnbqcQ" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="1GlYFhnbqd4" role="2m7DVh">
            <node concept="2m5ndX" id="1GlYFhnbqda" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1GlYFhnbqdd" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="1GlYFhnbqdu" role="2m7DVh">
            <node concept="2m5ndX" id="1GlYFhnbqd$" role="3GJlyp" />
          </node>
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
      <node concept="2m6fVq" id="64HNz1Ipfm5" role="2m5mJr">
        <property role="TrG5h" value="StartAnonymousAuth" />
        <node concept="2m7Kf5" id="64HNz1IpiSH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="64HNz1IpiSL" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBWSi" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="6Fl2chwBWSo" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBWSr" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="apiKey" />
          <node concept="2m5ndX" id="6Fl2chwBWSz" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBWTg" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="deviceHash" />
          <node concept="2m61tm" id="6Fl2chwBWTq" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBWTt" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="6Fl2chwBWTD" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBWTG" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="6Fl2chwBWTU" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwBWU0" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBWU3" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="6Fl2chwBWUk" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwBWUq" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="64HNz1Ipfm6" role="NuuwV">
          <property role="Nu42W" value="C6" />
        </node>
        <node concept="2m1Rp1" id="64HNz1IphvX" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="NXeRC" id="64HNz1Ipich" role="1GBnQ6">
          <property role="NXePf" value="Starting Anonymous login" />
        </node>
        <node concept="NX1gA" id="64HNz1IpkhH" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Name of new user" />
          <ref role="NX6Kv" node="64HNz1IpiSH" resolve="name" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBYyg" role="1GBnQ6">
          <property role="NX6R2" value="Application Id" />
          <property role="1GSvIU" value="hidden" />
          <ref role="NX6Kv" node="6Fl2chwBWSi" resolve="appId" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBYyq" role="1GBnQ6">
          <property role="1GSvIU" value="hidden" />
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="6Fl2chwBWSr" resolve="apiKey" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBYyA" role="1GBnQ6">
          <property role="NX6R2" value="Hash of device unique id and app bundle id. Used for autologout users when app is reinstalled" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBWTg" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBYyO" role="1GBnQ6">
          <property role="NX6R2" value="Device Title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBWTt" resolve="deviceTitle" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBYz4" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="TimeZone of device" />
          <ref role="NX6Kv" node="6Fl2chwBWTG" resolve="timeZone" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBYzm" role="1GBnQ6">
          <property role="NX6R2" value="Preferred languages" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBWU3" resolve="preferredLanguages" />
        </node>
      </node>
      <node concept="2m6fVq" id="64HNz1Ipfc1" role="2m5mJr">
        <property role="TrG5h" value="StartTokenAuth" />
        <node concept="2uC4CA" id="6Fl2chwBSDu" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="INCOERRECT_TOKEN" />
          <property role="2uCiSL" value="Token for authentication is incorrect" />
        </node>
        <node concept="2m7Kf5" id="64HNz1IplEr" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="token" />
          <node concept="2m5ndX" id="64HNz1IplEv" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBXGv" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="6Fl2chwBXG_" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBXGC" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="apiKey" />
          <node concept="2m5ndX" id="6Fl2chwBXGK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBXGN" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="deviceHash" />
          <node concept="2m61tm" id="6Fl2chwBXGX" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBXH0" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="6Fl2chwBXHc" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBXHf" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="6Fl2chwBXHt" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwBXHz" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBXHA" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="6Fl2chwBXHR" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwBXHX" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="64HNz1Ipfc2" role="NuuwV">
          <property role="Nu42W" value="CB" />
        </node>
        <node concept="2m1Rp1" id="64HNz1Ipfh3" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="NXeRC" id="64HNz1IplEy" role="1GBnQ6">
          <property role="NXePf" value="Starting token-based login" />
        </node>
        <node concept="NX1gA" id="64HNz1IplEB" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Token for authentication" />
          <ref role="NX6Kv" node="64HNz1IplEr" resolve="token" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC08Q" role="1GBnQ6">
          <property role="1GSvIU" value="hidden" />
          <property role="NX6R2" value="Application Id" />
          <ref role="NX6Kv" node="6Fl2chwBXGv" resolve="appId" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC090" role="1GBnQ6">
          <property role="1GSvIU" value="hidden" />
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="6Fl2chwBXGC" resolve="apiKey" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC09c" role="1GBnQ6">
          <property role="NX6R2" value="Hash of device unique id and app bundle id. Used for autologout users when app is reinstalled" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBXGN" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC09q" role="1GBnQ6">
          <property role="NX6R2" value="Device Title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBXH0" resolve="deviceTitle" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC09E" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="TimeZone of device" />
          <ref role="NX6Kv" node="6Fl2chwBXHf" resolve="timeZone" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC09W" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Preferred languages" />
          <ref role="NX6Kv" node="6Fl2chwBXHA" resolve="preferredLanguages" />
        </node>
      </node>
      <node concept="2m6fVq" id="6Fl2chwBWMC" role="2m5mJr">
        <property role="TrG5h" value="StartUsernameAuth" />
        <node concept="2m7Kf5" id="6Fl2chwBWSb" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="username" />
          <node concept="2m5ndX" id="6Fl2chwBWSf" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBYwg" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="appId" />
          <node concept="2m5ndE" id="6Fl2chwBYwm" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBYwp" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="apiKey" />
          <node concept="2m5ndX" id="6Fl2chwBYwx" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBYw$" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="deviceHash" />
          <node concept="2m61tm" id="6Fl2chwBYwI" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBYwL" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="deviceTitle" />
          <node concept="2m5ndX" id="6Fl2chwBYwX" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBYx0" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="6Fl2chwBYxe" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwBYxk" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBYxn" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="6Fl2chwBYxC" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwBYxI" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="6Fl2chwBWMD" role="NuuwV">
          <property role="Nu42W" value="A0B" />
        </node>
        <node concept="2m1R6W" id="6Fl2chwBYxL" role="2m6efq">
          <node concept="2m7Kf5" id="6Fl2chwBYxQ" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="transactionHash" />
            <node concept="2m5ndX" id="6Fl2chwBYxU" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="6Fl2chwBYxX" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="isRegistered" />
            <node concept="2m5ndN" id="6Fl2chwBYy3" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="6Fl2chwBYxM" role="NuuwV">
            <property role="Nu42W" value="A0C" />
          </node>
          <node concept="NXeRC" id="6Fl2chwC4uf" role="1y2DgH">
            <property role="NXePf" value="Result of login auth start. If is not registered move to signup." />
          </node>
          <node concept="NX1gA" id="6Fl2chwC4uk" role="1y2DgH">
            <property role="1GSvIU" value="danger" />
            <property role="NX6R2" value="Authentication transaction hash" />
            <ref role="NX6Kv" node="6Fl2chwBYxQ" resolve="transactionHash" />
          </node>
          <node concept="NX1gA" id="6Fl2chwC4us" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="If user is registered with this username" />
            <ref role="NX6Kv" node="6Fl2chwBYxX" resolve="isRegistered" />
          </node>
        </node>
        <node concept="NXeRC" id="6Fl2chwC1K9" role="1GBnQ6">
          <property role="NXePf" value="Starting Login Authentication" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC1Ke" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Username for signing in" />
          <ref role="NX6Kv" node="6Fl2chwBWSb" resolve="username" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC1Km" role="1GBnQ6">
          <property role="1GSvIU" value="hidden" />
          <property role="NX6R2" value="Application id" />
          <ref role="NX6Kv" node="6Fl2chwBYwg" resolve="appId" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC1Kw" role="1GBnQ6">
          <property role="1GSvIU" value="hidden" />
          <property role="NX6R2" value="Application API key" />
          <ref role="NX6Kv" node="6Fl2chwBYwp" resolve="apiKey" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC1KG" role="1GBnQ6">
          <property role="NX6R2" value="Hash of device unique id and app bundle id. Used for autologout users when app is reinstalled" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBYw$" resolve="deviceHash" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC1KU" role="1GBnQ6">
          <property role="NX6R2" value="Device Title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBYwL" resolve="deviceTitle" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC1La" role="1GBnQ6">
          <property role="NX6R2" value="Time Zone of device" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6Fl2chwBYx0" resolve="timeZone" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC3Fa" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Preferred languages of device" />
          <ref role="NX6Kv" node="6Fl2chwBYxn" resolve="preferredLanguages" />
        </node>
      </node>
      <node concept="2m6fVq" id="3zgy61ElQmM" role="2m5mJr">
        <property role="TrG5h" value="ValidateCode" />
        <node concept="2uC4CA" id="3zgy61ElSkB" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="UNOCCUPIED" />
          <property role="2uCiSL" value="Signup required" />
        </node>
        <node concept="2uC4CA" id="64HNz1Ipq9p" role="2uC9gA">
          <property role="2uC4DK" value="401" />
          <property role="2uC4Qe" value="PASSWORD_REQUIRED" />
          <property role="2uCiSL" value="Password is required" />
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
      <node concept="2m6fVq" id="64HNz1IpqV4" role="2m5mJr">
        <property role="TrG5h" value="ValidatePassword" />
        <node concept="2m7Kf5" id="64HNz1Ipr0o" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="transactionHash" />
          <node concept="2m5ndX" id="64HNz1Ipr0s" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64HNz1Ipr0v" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="password" />
          <node concept="2m5ndX" id="64HNz1Ipr0_" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="64HNz1IpqV5" role="NuuwV">
          <property role="Nu42W" value="CF" />
        </node>
        <node concept="2m1Rp1" id="64HNz1Ipr0l" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6Vl" resolve="Auth" />
        </node>
        <node concept="NXeRC" id="64HNz1Ipr0C" role="1GBnQ6">
          <property role="NXePf" value="Validation of account password" />
        </node>
        <node concept="NX1gA" id="64HNz1Ipr0H" role="1GBnQ6">
          <property role="NX6R2" value="Hash of transaction" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Ipr0o" resolve="transactionHash" />
        </node>
        <node concept="NX1gA" id="64HNz1Ipr0P" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Password for account" />
          <ref role="NX6Kv" node="64HNz1Ipr0v" resolve="password" />
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
        <node concept="NX1gA" id="6Fl2chwC0Xf" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Password for password-based accounts" />
          <ref role="NX6Kv" node="6Fl2chwC0WP" resolve="password" />
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
        <node concept="2m7Kf5" id="6Fl2chwC0WP" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="password" />
          <node concept="2m5nlT" id="6Fl2chwC0X0" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwC0X6" role="3GH5xg" />
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
        <node concept="2m7y0F" id="6u8Nlnzn64f" role="2m7ymf">
          <property role="TrG5h" value="Web" />
          <property role="2m7y0m" value="3" />
        </node>
        <node concept="2m7y0F" id="6u8Nlnzn6Cf" role="2m7ymf">
          <property role="TrG5h" value="Social" />
          <property role="2m7y0m" value="4" />
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
        <node concept="NX1gA" id="6u8Nlnzn8kT" role="NXodf">
          <property role="NX6R2" value="Value for specification type of contact, for example 'mobile/standalone/office' for phones or 'vk/fb/telegram' for extenrnal networks." />
          <ref role="NX6Kv" node="6u8Nlnzn7Ke" resolve="typeSpec" />
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
        <node concept="2m7Kf5" id="6u8Nlnzn7Ke" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="typeSpec" />
          <node concept="2m5nlT" id="6u8Nlnzn7KC" role="2m7DVh">
            <node concept="2m5ndX" id="6u8Nlnzn7KI" role="3GH5xg" />
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
        <node concept="NX1gA" id="6WYZhOUY$Rv" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's nickname" />
          <ref role="NX6Kv" node="6WYZhOUYzPj" resolve="nick" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYKFu" role="NXodf">
          <property role="NX6R2" value="User's about information" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUY$mg" resolve="about" />
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
        <node concept="NX1gA" id="6u8Nlnzn5w5" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="If not null, is user come from outer network" />
          <ref role="NX6Kv" node="6u8Nlnzn3Mp" resolve="external" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1rz_" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Preferred user languages" />
          <ref role="NX6Kv" node="4NJj1GT1qmy" resolve="preferredLanguages" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1_aU" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Time Zone of user in TZ format" />
          <ref role="NX6Kv" node="4NJj1GT1_9F" resolve="timeZone" />
        </node>
        <node concept="2m7Kf5" id="GBscvBAzcC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="wb0Ql" id="5qm50Y0eKWa" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
        <node concept="2m7Kf5" id="6WYZhOUYzPj" role="2m0hLx">
          <property role="2m7DUN" value="13" />
          <property role="TrG5h" value="nick" />
          <node concept="2m5nlT" id="6WYZhOUYzPJ" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUYzPP" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUY$mg" role="2m0hLx">
          <property role="2m7DUN" value="14" />
          <property role="TrG5h" value="about" />
          <node concept="2m5nlT" id="6WYZhOUY$mJ" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUY$mP" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6u8Nlnzn3Mp" role="2m0hLx">
          <property role="2m7DUN" value="15" />
          <property role="TrG5h" value="external" />
          <node concept="2m5nlT" id="6u8Nlnzn4nX" role="2m7DVh">
            <node concept="2m5ndX" id="6u8Nlnzn4o3" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1qmy" role="2m0hLx">
          <property role="2m7DUN" value="16" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="4NJj1GT1qot" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT1qoz" role="3GJlyp" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1_9F" role="2m0hLx">
          <property role="2m7DUN" value="17" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="4NJj1GT1_aj" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT1_ap" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBfq6" role="2m5mJr">
        <property role="TrG5h" value="EditUserLocalName" />
        <node concept="2m7Kf5" id="GBscvBBfqR" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0e47m" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0e47p" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0e4JF" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0e4JI" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
        <node concept="NX1gA" id="6WYZhOUYLGR" role="NXp_2">
          <property role="NX6R2" value="user's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="1ydqyopRK6g" resolve="uid" />
        </node>
        <node concept="NX1gA" id="1ydqyopRKZk" role="NXp_2">
          <property role="NX6R2" value="new phones list" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="1ydqyopRK6n" resolve="contactRecords" />
        </node>
        <node concept="2m7Kf5" id="1ydqyopRK6g" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0e4JL" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
      <node concept="NpBTk" id="6WYZhOUYziL" role="2m5mJr">
        <property role="TrG5h" value="UserNickChanged" />
        <node concept="NXeRC" id="6WYZhOUYLcb" role="NXp_2">
          <property role="NXePf" value="Update about nick changed" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYLGJ" role="NXp_2">
          <property role="NX6R2" value="user's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYzkC" resolve="uid" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYMdw" role="NXp_2">
          <property role="NX6R2" value="user's new nickname" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYzkJ" resolve="nickname" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYzkC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0e4JO" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYzkJ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="nickname" />
          <node concept="2m5nlT" id="6WYZhOUYzkP" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUYzkV" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUYziM" role="NuuwV">
          <property role="Nu42W" value="D1" />
        </node>
      </node>
      <node concept="NpBTk" id="6WYZhOUYNgI" role="2m5mJr">
        <property role="TrG5h" value="UserAboutChanged" />
        <node concept="NXeRC" id="6WYZhOUYNNS" role="NXp_2">
          <property role="NXePf" value="Update about user's about changed" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYNO2" role="NXp_2">
          <property role="NX6R2" value="User's uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYNiS" resolve="uid" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYOkO" role="NXp_2">
          <property role="NX6R2" value="User's about" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYNiZ" resolve="about" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYNiS" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0e4JR" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYNiZ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="about" />
          <node concept="2m5nlT" id="6WYZhOUYNj5" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUYNjb" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUYNgJ" role="NuuwV">
          <property role="Nu42W" value="D2" />
        </node>
      </node>
      <node concept="NpBTk" id="4NJj1GT1wpN" role="2m5mJr">
        <property role="TrG5h" value="UserPreferredLanguagesChanged" />
        <node concept="2m7Kf5" id="4NJj1GT1x1K" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0e4JU" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1x1R" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="4NJj1GT1x1X" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT1x23" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1wpO" role="NuuwV">
          <property role="Nu42W" value="D4" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1x26" role="NXp_2">
          <property role="NXePf" value="Update about user's preferred languages" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1xBG" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's uid" />
          <ref role="NX6Kv" node="4NJj1GT1x1K" resolve="uid" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1ydm" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's preferred languages. Can be empty." />
          <ref role="NX6Kv" node="4NJj1GT1x1R" resolve="preferredLanguages" />
        </node>
      </node>
      <node concept="NpBTk" id="4NJj1GT1_Nv" role="2m5mJr">
        <property role="TrG5h" value="UserTimeZoneChanged" />
        <node concept="NXeRC" id="4NJj1GT1_QB" role="NXp_2">
          <property role="NXePf" value="User TimeZone changed" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1_QH" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's uid" />
          <ref role="NX6Kv" node="4NJj1GT1_Qf" resolve="uid" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1_QP" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's new time zone in TZ format" />
          <ref role="NX6Kv" node="4NJj1GT1_Qm" resolve="timeZone" />
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1_Qf" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0e4JX" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1_Qm" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="4NJj1GT1_Qs" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT1_Qy" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1_Nw" role="NuuwV">
          <property role="Nu42W" value="D8" />
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
      <node concept="2m6fVq" id="6WYZhOUYr8S" role="2m5mJr">
        <property role="TrG5h" value="EditNickName" />
        <node concept="NXeRC" id="6WYZhOUYxKd" role="1GBnQ6">
          <property role="NXePf" value="Changing account's nickname" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYQnO" role="1GBnQ6">
          <property role="NX6R2" value="New Nickname" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYr9t" resolve="nickname" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYr9t" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="nickname" />
          <node concept="2m5nlT" id="6WYZhOUYPQW" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUYPR2" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUYr8T" role="NuuwV">
          <property role="Nu42W" value="CD" />
        </node>
        <node concept="2m1Rp1" id="6WYZhOUYr9$" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUYyKA" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="INVALID_FORMAT" />
          <property role="2uCiSL" value="Nick have invalid format" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUYyKC" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="TOO_SHORT" />
          <property role="2uCiSL" value="Nick is too short" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUYyKF" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="NICK_USED" />
          <property role="2uCiSL" value="Nick is used" />
        </node>
      </node>
      <node concept="2m6fVq" id="6WYZhOUYs9Y" role="2m5mJr">
        <property role="TrG5h" value="CheckNickName" />
        <node concept="NXeRC" id="6WYZhOUYxfY" role="1GBnQ6">
          <property role="NXePf" value="Checking availability of nickname" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYQSC" role="1GBnQ6">
          <property role="NX6R2" value="Nickname for checking" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYsaC" resolve="nickname" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUYvJr" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="INVALID_FORMAT" />
          <property role="2uCiSL" value="Nick have invalid format" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUYwf$" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="TOO_SHORT" />
          <property role="2uCiSL" value="Nick is too short" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUYwJJ" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="NICK_USED" />
          <property role="2uCiSL" value="Nick is used" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYsaC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="nickname" />
          <node concept="2m5ndX" id="6WYZhOUYsaG" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6WYZhOUYs9Z" role="NuuwV">
          <property role="Nu42W" value="CE" />
        </node>
        <node concept="2m1Rp1" id="6WYZhOUYRpy" role="2m6efq">
          <ref role="2m1o9l" node="6WYZhOUYtcE" resolve="Bool" />
        </node>
      </node>
      <node concept="2m6fVq" id="6WYZhOUYSrP" role="2m5mJr">
        <property role="TrG5h" value="EditAbout" />
        <node concept="NXeRC" id="6WYZhOUYTuA" role="1GBnQ6">
          <property role="NXePf" value="Changing about information" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYTZx" role="1GBnQ6">
          <property role="NX6R2" value="new about information" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYSXz" resolve="about" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYSXz" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="about" />
          <node concept="2m5nlT" id="6WYZhOUYSXB" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUYSXH" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUYSrQ" role="NuuwV">
          <property role="Nu42W" value="D4" />
        </node>
        <node concept="2m1Rp1" id="6WYZhOUYSsJ" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
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
      <node concept="2m6fVq" id="4NJj1GT1B3o" role="2m5mJr">
        <property role="TrG5h" value="EditMyTimeZone" />
        <node concept="2m7Kf5" id="4NJj1GT1B4q" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="tz" />
          <node concept="2m5ndX" id="4NJj1GT1B4u" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="4NJj1GT1B3p" role="NuuwV">
          <property role="Nu42W" value="90" />
        </node>
        <node concept="2m1Rp1" id="4NJj1GT1B4x" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1B4$" role="1GBnQ6">
          <property role="NXePf" value="Updating user's time zone" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1BEw" role="1GBnQ6">
          <property role="NX6R2" value="New Time Zone" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1B4q" resolve="tz" />
        </node>
      </node>
      <node concept="2m6fVq" id="4NJj1GT1Chv" role="2m5mJr">
        <property role="TrG5h" value="EditMyPreferredLanguages" />
        <node concept="2m7Kf5" id="4NJj1GT1CiF" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="4NJj1GT1CiJ" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT1CiP" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1Chw" role="NuuwV">
          <property role="Nu42W" value="91" />
        </node>
        <node concept="2m1Rp1" id="4NJj1GT1CiC" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1CiS" role="1GBnQ6">
          <property role="NXePf" value="Changing preffered languages" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1CSW" role="1GBnQ6">
          <property role="NX6R2" value="Preffered Languages" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1CiF" resolve="preferredLanguages" />
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
          <node concept="wb0Ql" id="5qm50Y0e5of" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0e5oi" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0e5ol" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
            <node concept="wb0Ql" id="5qm50Y0e5oo" role="3GJlyp">
              <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
            </node>
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
            <node concept="wb0Ql" id="5qm50Y0e5or" role="3GJlyp">
              <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
            </node>
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
            <node concept="wb0Ql" id="5qm50Y0eMQH" role="3GJlyp">
              <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
            </node>
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
      <node concept="2m5naR" id="64HNz1IoxVm" role="2m5mJr">
        <property role="TrG5h" value="TextModernMessage" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="5NX0N0RThX2" resolve="TextMessageEx" />
        <node concept="2m7Kf5" id="64HNz1Ioy3S" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="text" />
          <node concept="2m5nlT" id="64HNz1IoYce" role="2m7DVh">
            <node concept="2m5ndX" id="64HNz1IoYck" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Io_EV" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="senderNameOverride" />
          <node concept="2m5nlT" id="64HNz1Io_F1" role="2m7DVh">
            <node concept="2m5ndX" id="64HNz1Io_F7" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoGNl" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="senderPhotoOverride" />
          <node concept="2m5nlT" id="64HNz1IoGNu" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1IoGN$" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoJLo" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="style" />
          <node concept="2m5nlT" id="64HNz1IoJLW" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1IoJM2" role="3GH5xg">
              <ref role="2m5mJy" node="64HNz1IoIik" resolve="ParagraphStyle" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRCrYH" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="attaches" />
          <node concept="2m5nlk" id="64LjbWRCrYX" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWRCrZ3" role="3GJlyp">
              <ref role="2m5mJy" node="64HNz1IoM55" resolve="TextModernAttach" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="64HNz1Ioy3Q" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
        <node concept="NXeRC" id="64HNz1IoyIL" role="NXodf">
          <property role="NXePf" value="Modern text message" />
        </node>
        <node concept="NX1gA" id="64HNz1Iozpx" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="optional text of message" />
          <ref role="NX6Kv" node="64HNz1Ioy3S" resolve="text" />
        </node>
        <node concept="NX1gA" id="64HNz1Io_Fe" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="optional overriding of sender" />
          <ref role="NX6Kv" node="64HNz1Io_EV" resolve="senderNameOverride" />
        </node>
        <node concept="NX1gA" id="64LjbWRCsKq" role="NXodf">
          <property role="NX6R2" value="optional overriding sender's photo" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoGNl" resolve="senderPhotoOverride" />
        </node>
        <node concept="NX1gA" id="64LjbWRCtxQ" role="NXodf">
          <property role="NX6R2" value="optional paragraph style" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoJLo" resolve="style" />
        </node>
        <node concept="NX1gA" id="64LjbWRCujl" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="optional Attaches of message" />
          <ref role="NX6Kv" node="64LjbWRCrYH" resolve="attaches" />
        </node>
      </node>
      <node concept="2m5naR" id="64HNz1IoIik" role="2m5mJr">
        <property role="TrG5h" value="ParagraphStyle" />
        <node concept="2m7Kf5" id="64HNz1IoIr2" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="showParagraph" />
          <node concept="2m5nlT" id="64HNz1IoIr6" role="2m7DVh">
            <node concept="2m5ndN" id="64HNz1IoIrc" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoIrf" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="paragraphColor" />
          <node concept="2m5nlT" id="64HNz1IoIrm" role="2m7DVh">
            <node concept="3BlaRf" id="64HNz1Ip5mE" role="3GH5xg">
              <ref role="3BrLez" node="64HNz1IoYTS" resolve="Color" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoW8Q" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="bgColor" />
          <node concept="2m5nlT" id="64HNz1IoW90" role="2m7DVh">
            <node concept="3BlaRf" id="64HNz1Ip5mB" role="3GH5xg">
              <ref role="3BrLez" node="64HNz1IoYTS" resolve="Color" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="64HNz1IoJ6q" role="NXodf">
          <property role="NXePf" value="Paragraph style" />
        </node>
        <node concept="NX1gA" id="64HNz1IoKt7" role="NXodf">
          <property role="NX6R2" value="Show quote-like paragraph?" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoIr2" resolve="showParagraph" />
        </node>
        <node concept="NX1gA" id="64HNz1IoL8f" role="NXodf">
          <property role="NX6R2" value="Override paragraph color" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoIrf" resolve="paragraphColor" />
        </node>
        <node concept="NX1gA" id="64HNz1IoWOQ" role="NXodf">
          <property role="NX6R2" value="Override background color" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoW8Q" resolve="bgColor" />
        </node>
      </node>
      <node concept="2m5naR" id="64HNz1IoM55" role="2m5mJr">
        <property role="TrG5h" value="TextModernAttach" />
        <property role="tsOgz" value="true" />
        <node concept="2m7Kf5" id="64HNz1IoMdZ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="title" />
          <node concept="2m5nlT" id="64HNz1IoMe3" role="2m7DVh">
            <node concept="2m5ndX" id="64HNz1IoMe9" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoOgu" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="titleUrl" />
          <node concept="2m5nlT" id="64HNz1IoOgC" role="2m7DVh">
            <node concept="2m5ndX" id="64HNz1IoOgI" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoRgi" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="titleIcon" />
          <node concept="2m5nlT" id="64HNz1IoRgv" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1IoU5s" role="3GH5xg">
              <ref role="2m5mJy" node="64HNz1IoSEt" resolve="ImageLocation" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoMec" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="text" />
          <node concept="2m5nlT" id="64HNz1IoMej" role="2m7DVh">
            <node concept="2m5ndX" id="64HNz1IoMep" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoVt1" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="style" />
          <node concept="2m5nlT" id="64HNz1Ip5mt" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1Ip62B" role="3GH5xg">
              <ref role="2m5mJy" node="64HNz1IoIik" resolve="ParagraphStyle" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Ip8Nm" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="fields" />
          <node concept="2m5nlk" id="64HNz1Ip8ND" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1Ip8NJ" role="3GJlyp">
              <ref role="2m5mJy" node="64HNz1IoP4Y" resolve="TextModernField" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="64HNz1Ip6I$" role="NXodf">
          <property role="NXePf" value="Attaches to message" />
        </node>
        <node concept="NX1gA" id="64HNz1Ip7q$" role="NXodf">
          <property role="NX6R2" value="Attach of message" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoMdZ" resolve="title" />
        </node>
        <node concept="NX1gA" id="64HNz1Ip86C" role="NXodf">
          <property role="NX6R2" value="Attach title url" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoOgu" resolve="titleUrl" />
        </node>
        <node concept="NX1gA" id="64HNz1Ip86M" role="NXodf">
          <property role="NX6R2" value="Attach title icon" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoRgi" resolve="titleIcon" />
        </node>
        <node concept="NX1gA" id="64HNz1Ip86Y" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Attach text" />
          <ref role="NX6Kv" node="64HNz1IoMec" resolve="text" />
        </node>
        <node concept="NX1gA" id="64HNz1Ip87c" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Attach style" />
          <ref role="NX6Kv" node="64HNz1IoVt1" resolve="style" />
        </node>
      </node>
      <node concept="2m5naR" id="64HNz1IoP4Y" role="2m5mJr">
        <property role="TrG5h" value="TextModernField" />
        <property role="tsOgz" value="true" />
        <node concept="2m7Kf5" id="64HNz1IoPTd" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="64HNz1IoPTh" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64HNz1IoPTk" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="value" />
          <node concept="2m5ndX" id="64HNz1IoPTq" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64HNz1IoPTt" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="isShort" />
          <node concept="2m5nlT" id="64HNz1IoPT_" role="2m7DVh">
            <node concept="2m5ndN" id="64HNz1IoPTF" role="3GH5xg" />
          </node>
        </node>
        <node concept="NXeRC" id="64HNz1Ip9vP" role="NXodf">
          <property role="NXePf" value="Modern message fields" />
        </node>
        <node concept="NX1gA" id="64HNz1IpabY" role="NXodf">
          <property role="NX6R2" value="Field title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoPTd" resolve="title" />
        </node>
        <node concept="NX1gA" id="64HNz1IpaSt" role="NXodf">
          <property role="NX6R2" value="Field value" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoPTk" resolve="value" />
        </node>
        <node concept="NX1gA" id="64HNz1IpaSB" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Is field can be shown in compact way (default is TRUE)" />
          <ref role="NX6Kv" node="64HNz1IoPTt" resolve="isShort" />
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
          <node concept="wb0Ql" id="5qm50Y0eL_3" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eKWd" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
      <node concept="2m5naR" id="6nbRE0KfgG8" role="2m5mJr">
        <property role="TrG5h" value="ServiceExChangedTopic" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="NXeRC" id="6nbRE0KftNw" role="NXodf">
          <property role="NXePf" value="Service message on group topic change" />
        </node>
        <node concept="NX1gA" id="6nbRE0Kfuwn" role="NXodf">
          <property role="NX6R2" value="New group topic" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6nbRE0KfgPG" resolve="topic" />
        </node>
        <node concept="2m7Kf5" id="6nbRE0KfgPG" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="topic" />
          <node concept="2m5nlT" id="6cUOFUo0qtc" role="2m7DVh">
            <node concept="2m5ndX" id="6cUOFUo0ra2" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6nbRE0Kfi5A" role="3BtCOu">
          <property role="Nu42W" value="12" />
        </node>
      </node>
      <node concept="2m5naR" id="6nbRE0KfuE4" role="2m5mJr">
        <property role="TrG5h" value="ServiceExChangedAbout" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="55bmeIQ9med" resolve="ServiceEx" />
        <node concept="2m7Kf5" id="6nbRE0Kfvwp" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="about" />
          <node concept="2m5nlT" id="6cUOFUo0ra5" role="2m7DVh">
            <node concept="2m5ndX" id="6cUOFUo0rab" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6nbRE0Kfvww" role="3BtCOu">
          <property role="Nu42W" value="13" />
        </node>
        <node concept="NXeRC" id="6nbRE0Kfvwy" role="NXodf">
          <property role="NXePf" value="Service message on group about change" />
        </node>
        <node concept="NX1gA" id="6nbRE0KfvwB" role="NXodf">
          <property role="NX6R2" value="New group about" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6nbRE0Kfvwp" resolve="about" />
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
          <node concept="wb0Ql" id="5qm50Y0eMdS" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
        <node concept="NX1gA" id="6tgpW9bxrsC" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Duration of a phone call" />
          <ref role="NX6Kv" node="2tyCW$TWsq1" resolve="duration" />
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
      <node concept="2m5naR" id="4HpmlHNv7Js" role="2m5mJr">
        <property role="TrG5h" value="UnsupportedMessage" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="Nu42z" id="4HpmlHNv7PL" role="3BtCOu">
          <property role="Nu42W" value="05" />
        </node>
        <node concept="NXeRC" id="4HpmlHNv9$k" role="NXodf">
          <property role="NXePf" value="Explicit type for unsupported message" />
        </node>
      </node>
      <node concept="2m5naR" id="2EyE8f8Bt00" role="2m5mJr">
        <property role="TrG5h" value="StickerMessage" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="NXeRC" id="2EyE8f8Bvk1" role="NXodf">
          <property role="NXePf" value="Sticker message" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bvk7" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional Unique ID of sticker" />
          <ref role="NX6Kv" node="2EyE8f8BtRD" resolve="stickerId" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BvkN" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional Fast preview of sticker in webp format" />
          <ref role="NX6Kv" node="2EyE8f8BtRK" resolve="fastPreview" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BvkX" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional 512x512 sticker image in webp format" />
          <ref role="NX6Kv" node="2EyE8f8BtRT" resolve="image512" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bw32" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional 256x256 sticker image in webp format" />
          <ref role="NX6Kv" node="2EyE8f8Bu_U" resolve="image256" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bw3g" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional Collection ID" />
          <ref role="NX6Kv" node="2EyE8f8Bvkb" resolve="stickerCollectionId" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bw3w" role="NXodf">
          <property role="NX6R2" value="Optional Collection Access Hash" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2EyE8f8Bvks" resolve="stickerCollectionAccessHash" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BtRD" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="stickerId" />
          <node concept="2m5nlT" id="2EyE8f8BSSW" role="2m7DVh">
            <node concept="2m5ndE" id="2EyE8f8BST2" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BtRK" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="fastPreview" />
          <node concept="2m5nlT" id="2EyE8f8BST5" role="2m7DVh">
            <node concept="2m61tm" id="2EyE8f8BSTb" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BtRT" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="image512" />
          <node concept="2m5nlT" id="2EyE8f8Bu_L" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8Bu_R" role="3GH5xg">
              <ref role="2m5mJy" node="64HNz1IoSEt" resolve="ImageLocation" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8Bu_U" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="image256" />
          <node concept="2m5nlT" id="2EyE8f8BuA5" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8BuAb" role="3GH5xg">
              <ref role="2m5mJy" node="64HNz1IoSEt" resolve="ImageLocation" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8Bvkb" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="stickerCollectionId" />
          <node concept="2m5nlT" id="2EyE8f8BS9_" role="2m7DVh">
            <node concept="2m5ndE" id="2EyE8f8BS9F" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8Bvks" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="stickerCollectionAccessHash" />
          <node concept="2m5nlT" id="2EyE8f8BS9I" role="2m7DVh">
            <node concept="2m5ndQ" id="2EyE8f8BS9O" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="2EyE8f8Bta0" role="3BtCOu">
          <property role="Nu42W" value="06" />
        </node>
      </node>
      <node concept="2m5naR" id="5_CDdZ2rszW" role="2m5mJr">
        <property role="TrG5h" value="BinaryMessage" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="2m7Kf5" id="5_CDdZ2rsIw" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="contentTag" />
          <node concept="2m5ndX" id="5_CDdZ2rsI$" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2rsIM" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="msg" />
          <node concept="2m61tm" id="5_CDdZ2rsIS" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="5_CDdZ2rsIu" role="3BtCOu">
          <property role="Nu42W" value="07" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2rsIV" role="NXodf">
          <property role="NXePf" value="Binary Message. Useful for implementing your own content types" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2rsJ0" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Content Tag" />
          <ref role="NX6Kv" node="5_CDdZ2rsIw" resolve="contentTag" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2rsJ8" role="NXodf">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Message contents" />
          <ref role="NX6Kv" node="5_CDdZ2rsIM" resolve="msg" />
        </node>
      </node>
      <node concept="2m5naR" id="5_CDdZ2rtNF" role="2m5mJr">
        <property role="TrG5h" value="EncryptedMessage" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="55bmeIQey3W" resolve="Message" />
        <node concept="2m7Kf5" id="5_CDdZ2rA8j" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="box" />
          <node concept="2m5mGg" id="5_CDdZ2rA8n" role="2m7DVh">
            <ref role="2m5mJy" node="5_CDdZ2r0S4" resolve="EncryptedBox" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2rtYm" role="3BtCOu">
          <property role="Nu42W" value="08" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2rtYq" role="NXodf">
          <property role="NXePf" value="Encrypted Message" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2rB2r" role="NXodf">
          <property role="NX6R2" value="Encrypted box" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2rA8j" resolve="box" />
        </node>
      </node>
      <node concept="2m5naR" id="5TxE3W6ZYRc" role="2m5mJr">
        <property role="TrG5h" value="DialogShort" />
        <node concept="2m7Kf5" id="5TxE3W6ZYX$" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="5TxE3W6ZYXC" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5TxE3W6ZYXF" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="counter" />
          <node concept="2m5ndE" id="5TxE3W6ZYXL" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5TxE3W6ZYXO" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="5TxE3W6ZYY1" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="NXeRC" id="5TxE3W6ZZyt" role="NXodf">
          <property role="NXePf" value="Short Dialog from grouped conversation list" />
        </node>
        <node concept="NX1gA" id="5TxE3W7006W" role="NXodf">
          <property role="NX6R2" value="Peer of conversation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5TxE3W6ZYX$" resolve="peer" />
        </node>
        <node concept="NX1gA" id="5TxE3W700Fv" role="NXodf">
          <property role="NX6R2" value="Conversation unread count" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5TxE3W6ZYXF" resolve="counter" />
        </node>
        <node concept="NX1gA" id="5TxE3W702pm" role="NXodf">
          <property role="NX6R2" value="Conversation top message date" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5TxE3W6ZYXO" resolve="date" />
        </node>
      </node>
      <node concept="2m5naR" id="5TxE3W704q_" role="2m5mJr">
        <property role="TrG5h" value="DialogGroup" />
        <node concept="2m7Kf5" id="5TxE3W704xb" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="5TxE3W704xf" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5TxE3W704xi" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="key" />
          <node concept="2m5ndX" id="5TxE3W704xo" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5TxE3W704xr" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="dialogs" />
          <node concept="2m5nlk" id="5TxE3W704xz" role="2m7DVh">
            <node concept="2m5mGg" id="5TxE3W704xD" role="3GJlyp">
              <ref role="2m5mJy" node="5TxE3W6ZYRc" resolve="DialogShort" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="5TxE3W704xI" role="NXodf">
          <property role="NXePf" value="Grouped dialog list" />
        </node>
        <node concept="NX1gA" id="5TxE3W7056s" role="NXodf">
          <property role="NX6R2" value="Title of group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5TxE3W704xb" resolve="title" />
        </node>
        <node concept="NX1gA" id="5TxE3W7056$" role="NXodf">
          <property role="NX6R2" value="Key of group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5TxE3W704xi" resolve="key" />
        </node>
        <node concept="NX1gA" id="5TxE3W7056I" role="NXodf">
          <property role="NX6R2" value="Conversations in group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5TxE3W704xr" resolve="dialogs" />
        </node>
      </node>
      <node concept="2m5naR" id="64HNz1Io2Dg" role="2m5mJr">
        <property role="TrG5h" value="MessageReaction" />
        <node concept="2m7Kf5" id="64HNz1Io2KM" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="64HNz1Io2KY" role="2m7DVh">
            <node concept="wb0Ql" id="64HNz1Io2L4" role="3GJlyp">
              <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Io3qN" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="code" />
          <node concept="2m5ndX" id="64HNz1Io3qU" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="64HNz1Io3qX" role="NXodf">
          <property role="NXePf" value="Reaction to message" />
        </node>
        <node concept="NX1gA" id="64HNz1Io44L" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's reaction" />
          <ref role="NX6Kv" node="64HNz1Io2KM" resolve="users" />
        </node>
        <node concept="NX1gA" id="64HNz1Io4ID" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Reaction EMOJI code" />
          <ref role="NX6Kv" node="64HNz1Io3qN" resolve="code" />
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
      <node concept="2m6fVq" id="64HNz1Io6C_" role="2m5mJr">
        <property role="TrG5h" value="MessageSetReaction" />
        <node concept="2m7Kf5" id="64HNz1Io6KQ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="64HNz1Io6KW" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Io6KZ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="64HNz1Io6L7" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Io6Ky" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="code" />
          <node concept="2m5ndX" id="64HNz1Io6KA" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="64HNz1Io6CA" role="NuuwV">
          <property role="Nu42W" value="D2" />
        </node>
        <node concept="2m1Rp1" id="64HNz1Io8lk" role="2m6efq">
          <ref role="2m1o9l" node="64HNz1Io8db" resolve="ReactionsResponse" />
        </node>
        <node concept="NXeRC" id="64HNz1Io9TD" role="1GBnQ6">
          <property role="NXePf" value="Setting Message reaction" />
        </node>
        <node concept="NX1gA" id="64HNz1Io9TI" role="1GBnQ6">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Io6KQ" resolve="peer" />
        </node>
        <node concept="NX1gA" id="64HNz1Iobe$" role="1GBnQ6">
          <property role="NX6R2" value="Message random id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Io6KZ" resolve="rid" />
        </node>
        <node concept="NX1gA" id="64HNz1IonCQ" role="1GBnQ6">
          <property role="NX6R2" value="Reaction code" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Io6Ky" resolve="code" />
        </node>
      </node>
      <node concept="2m6fVq" id="64HNz1Io9Ld" role="2m5mJr">
        <property role="TrG5h" value="MessageRemoveReaction" />
        <node concept="2m7Kf5" id="64HNz1Io9Tb" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="64HNz1Io9Tf" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Io9Ti" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="64HNz1Io9To" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Io9Tr" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="code" />
          <node concept="2m5ndX" id="64HNz1Io9Tz" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="64HNz1Io9Le" role="NuuwV">
          <property role="Nu42W" value="DC" />
        </node>
        <node concept="2m1Rp1" id="64HNz1Io9TA" role="2m6efq">
          <ref role="2m1o9l" node="64HNz1Io8db" resolve="ReactionsResponse" />
        </node>
        <node concept="NXeRC" id="64HNz1Ioojl" role="1GBnQ6">
          <property role="NXePf" value="Removing Message reaction" />
        </node>
        <node concept="NX1gA" id="64HNz1IooXO" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Destination peer" />
          <ref role="NX6Kv" node="64HNz1Io9Tb" resolve="peer" />
        </node>
        <node concept="NX1gA" id="64HNz1IooXW" role="1GBnQ6">
          <property role="NX6R2" value="Message random id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Io9Ti" resolve="rid" />
        </node>
        <node concept="NX1gA" id="64HNz1IooY6" role="1GBnQ6">
          <property role="NX6R2" value="Reaction code" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Io9Tr" resolve="code" />
        </node>
      </node>
      <node concept="2m62dX" id="64HNz1Io8db" role="2m5mJr">
        <property role="TrG5h" value="ReactionsResponse" />
        <node concept="2m7Kf5" id="64HNz1IoazW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="64HNz1Ioa$3" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64HNz1Ioa$6" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="64HNz1Ioa$f" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Io8l5" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="reactions" />
          <node concept="2m5nlk" id="64HNz1Io8l9" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1Io8lf" role="3GJlyp">
              <ref role="2m5mJy" node="64HNz1Io2Dg" resolve="MessageReaction" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="64HNz1Io8dc" role="NuuwV">
          <property role="Nu42W" value="DB" />
        </node>
        <node concept="NXeRC" id="64HNz1IopCD" role="NXp4Y">
          <property role="NXePf" value="Response for reactions change" />
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
          <node concept="wb0Ql" id="5qm50Y0e60H" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
      <node concept="NpBTk" id="5TxE3W70bTg" role="2m5mJr">
        <property role="TrG5h" value="ChatGroupsChanged" />
        <node concept="2m7Kf5" id="5TxE3W70qfx" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="dialogs" />
          <node concept="2m5nlk" id="5TxE3W70qfy" role="2m7DVh">
            <node concept="2m5mGg" id="5TxE3W70qfz" role="3GJlyp">
              <ref role="2m5mJy" node="5TxE3W704q_" resolve="DialogGroup" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5TxE3W70bTh" role="NuuwV">
          <property role="Nu42W" value="01" />
        </node>
        <node concept="NXeRC" id="5TxE3W70c0v" role="NXp_2">
          <property role="NXePf" value="Update about chat groups changed. Called only when adding, removing and reordering of grouped dialog." />
        </node>
        <node concept="NX1gA" id="5TxE3W70c_I" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="New dialgos list" />
          <ref role="NX6Kv" node="5TxE3W70qfx" resolve="dialogs" />
        </node>
      </node>
      <node concept="NpBTk" id="64HNz1IobmL" role="2m5mJr">
        <property role="TrG5h" value="ReactionsUpdate" />
        <node concept="2m7Kf5" id="64HNz1Iobv8" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="64HNz1Iobvc" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Iobvf" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="64HNz1Iobvl" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1Ioc9H" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="reactions" />
          <node concept="2m5nlk" id="64HNz1Ioc9P" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1Ioc9V" role="3GJlyp">
              <ref role="2m5mJy" node="64HNz1Io2Dg" resolve="MessageReaction" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="64HNz1IobmM" role="NuuwV">
          <property role="Nu42W" value="DE" />
        </node>
        <node concept="NXeRC" id="64HNz1Ioqj9" role="NXp_2">
          <property role="NXePf" value="Update about reactions change" />
        </node>
        <node concept="NX1gA" id="64HNz1Ioqje" role="NXp_2">
          <property role="NX6R2" value="Destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Iobv8" resolve="peer" />
        </node>
        <node concept="NX1gA" id="64HNz1Ioqjm" role="NXp_2">
          <property role="NX6R2" value="Message random id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Iobvf" resolve="rid" />
        </node>
        <node concept="NX1gA" id="64HNz1Ioqjw" role="NXp_2">
          <property role="NX6R2" value="New Reactions" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1Ioc9H" resolve="reactions" />
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
        <property role="TrG5h" value="MessageContainer" />
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
        <node concept="NX1gA" id="64HNz1Iovfm" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Message reactions" />
          <ref role="NX6Kv" node="64HNz1IoveR" resolve="reactions" />
        </node>
        <node concept="2m7Kf5" id="gbd4oSj4vC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="senderUid" />
          <node concept="wb0Ql" id="5qm50Y0e60K" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
        <node concept="2m7Kf5" id="64HNz1IoveR" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="reactions" />
          <node concept="2m5nlk" id="64HNz1Iovf6" role="2m7DVh">
            <node concept="2m5mGg" id="64HNz1Iovfc" role="3GJlyp">
              <ref role="2m5mJy" node="64HNz1Io2Dg" resolve="MessageReaction" />
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
                <ref role="2m5mJy" node="gbd4oSj4vy" resolve="MessageContainer" />
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
          <node concept="wb0Ql" id="5qm50Y0e6D2" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
      <node concept="2m6fVq" id="5TxE3W703CS" role="2m5mJr">
        <property role="TrG5h" value="LoadGroupedDialogs" />
        <node concept="NXeRC" id="5TxE3W706uG" role="1GBnQ6">
          <property role="NXePf" value="Load all dialogs from grouped list" />
        </node>
        <node concept="Nu42z" id="5TxE3W703CT" role="NuuwV">
          <property role="Nu42W" value="E1" />
        </node>
        <node concept="2m1R6W" id="5TxE3W7073w" role="2m6efq">
          <node concept="2m7Kf5" id="5TxE3W708d6" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="dialogs" />
            <node concept="2m5nlk" id="5TxE3W708da" role="2m7DVh">
              <node concept="2m5mGg" id="5TxE3W70p4x" role="3GJlyp">
                <ref role="2m5mJy" node="5TxE3W704q_" resolve="DialogGroup" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="5TxE3W708M5" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="5TxE3W708Mc" role="2m7DVh">
              <node concept="2m5mGg" id="5TxE3W708Mi" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="5TxE3W708Ml" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="groups" />
            <node concept="2m5nlk" id="5TxE3W708Mv" role="2m7DVh">
              <node concept="2m5mGg" id="5TxE3W708M_" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="5TxE3W7073x" role="NuuwV">
            <property role="Nu42W" value="E2" />
          </node>
          <node concept="NXeRC" id="5TxE3W707Cl" role="1y2DgH">
            <property role="NXePf" value="Loaded grouped dialogs" />
          </node>
          <node concept="NX1gA" id="5TxE3W709nz" role="1y2DgH">
            <property role="1GSvIU" value="compact" />
            <property role="NX6R2" value="Loaded groups of dialogs" />
            <ref role="NX6Kv" node="5TxE3W708d6" resolve="dialogs" />
          </node>
          <node concept="NX1gA" id="5TxE3W709nF" role="1y2DgH">
            <property role="1GSvIU" value="compact" />
            <property role="NX6R2" value="Loaded users" />
            <ref role="NX6Kv" node="5TxE3W708M5" resolve="users" />
          </node>
          <node concept="NX1gA" id="5TxE3W709nP" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Loaded groups" />
            <ref role="NX6Kv" node="5TxE3W708Ml" resolve="groups" />
          </node>
        </node>
      </node>
      <node concept="2m62dX" id="5qm50Y0eGZ2" role="2m5mJr">
        <property role="TrG5h" value="DialogsOrder" />
        <node concept="2m7Kf5" id="5qm50Y0eH6p" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="5qm50Y0eH6t" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5qm50Y0eH6w" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="5qm50Y0eH6A" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5qm50Y0eH6D" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="groups" />
          <node concept="2m5nlk" id="5qm50Y0eH6L" role="2m7DVh">
            <node concept="2m5mGg" id="5qm50Y0eH6U" role="3GJlyp">
              <ref role="2m5mJy" node="5TxE3W704q_" resolve="DialogGroup" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5qm50Y0eGZ3" role="NuuwV">
          <property role="Nu42W" value="EB" />
        </node>
        <node concept="NXeRC" id="5qm50Y0eHJF" role="NXp4Y">
          <property role="NXePf" value="Dialogs order response" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eJ1e" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="update seq" />
          <ref role="NX6Kv" node="5qm50Y0eH6p" resolve="seq" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eJ1m" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="update state" />
          <ref role="NX6Kv" node="5qm50Y0eH6w" resolve="state" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eJ1w" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Update groups" />
          <ref role="NX6Kv" node="5qm50Y0eH6D" resolve="groups" />
        </node>
      </node>
      <node concept="2m6fVq" id="4NJj1GT1Ecq" role="2m5mJr">
        <property role="TrG5h" value="HideDialog" />
        <node concept="2m7Kf5" id="4NJj1GT1EjU" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="4NJj1GT1EjY" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1Ecr" role="NuuwV">
          <property role="Nu42W" value="E7" />
        </node>
        <node concept="2m1Rp1" id="5qm50Y0eJEv" role="2m6efq">
          <ref role="2m1o9l" node="5qm50Y0eGZ2" resolve="DialogsOrder" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1Ek4" role="1GBnQ6">
          <property role="NXePf" value="Hide Dialog from grouped list" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1EUf" role="1GBnQ6">
          <property role="NX6R2" value="Dialog peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1EjU" resolve="peer" />
        </node>
      </node>
      <node concept="2m6fVq" id="4NJj1GT1F1U" role="2m5mJr">
        <property role="TrG5h" value="ShowDialog" />
        <node concept="2m7Kf5" id="4NJj1GT1F9x" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="4NJj1GT1F9_" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1F1V" role="NuuwV">
          <property role="Nu42W" value="E8" />
        </node>
        <node concept="2m1Rp1" id="5qm50Y0eKjl" role="2m6efq">
          <ref role="2m1o9l" node="5qm50Y0eGZ2" resolve="DialogsOrder" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1F9F" role="1GBnQ6">
          <property role="NXePf" value="Show Dialog in grouped list" />
        </node>
      </node>
      <node concept="2m6fVq" id="4GqCdBPOcJQ" role="2m5mJr">
        <property role="TrG5h" value="FavouriteDialog" />
        <node concept="2m7Kf5" id="4GqCdBPOcTv" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="4GqCdBPOcTz" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="4GqCdBPOcJR" role="NuuwV">
          <property role="Nu42W" value="E0" />
        </node>
        <node concept="2m1Rp1" id="4GqCdBPOcTA" role="2m6efq">
          <ref role="2m1o9l" node="5qm50Y0eGZ2" resolve="DialogsOrder" />
        </node>
        <node concept="NXeRC" id="4GqCdBPOcTD" role="1GBnQ6">
          <property role="NXePf" value="Marking dialog as favourite" />
        </node>
        <node concept="NX1gA" id="4GqCdBPOcTI" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Peer for favouriting" />
          <ref role="NX6Kv" node="4GqCdBPOcTv" resolve="peer" />
        </node>
      </node>
      <node concept="2m6fVq" id="4GqCdBPOesM" role="2m5mJr">
        <property role="TrG5h" value="UnfavouriteDialog" />
        <node concept="2m7Kf5" id="4GqCdBPOeAI" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="4GqCdBPOeAM" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="4GqCdBPOesN" role="NuuwV">
          <property role="Nu42W" value="E3" />
        </node>
        <node concept="2m1Rp1" id="4GqCdBPOeAy" role="2m6efq">
          <ref role="2m1o9l" node="5qm50Y0eGZ2" resolve="DialogsOrder" />
        </node>
        <node concept="NXeRC" id="4GqCdBPOeA_" role="1GBnQ6">
          <property role="NXePf" value="Making dialog as unfavourite" />
        </node>
        <node concept="NX1gA" id="4GqCdBPOfjF" role="1GBnQ6">
          <property role="NX6R2" value="Peer for favouriting" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4GqCdBPOeAI" resolve="peer" />
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
        <node concept="NX1gA" id="6WYZhOUZ3$s" role="NXodf">
          <property role="NX6R2" value="Theme of group" />
          <ref role="NX6Kv" node="6WYZhOUZ2wi" resolve="theme" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZ4Cl" role="NXodf">
          <property role="NX6R2" value="About of group" />
          <ref role="NX6Kv" node="6WYZhOUZ45X" resolve="about" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8jI" role="NXodf">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Avatar of group" />
          <ref role="NX6Kv" node="GBscvBB6rV" resolve="avatar" />
        </node>
        <node concept="NX1gA" id="4zDDY4ER8jW" role="NXodf">
          <property role="NX6R2" value="is member of group (deprecated)" />
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
        <node concept="NX1gA" id="5qm50Y0edzr" role="NXodf">
          <property role="NX6R2" value="Group extension Data" />
          <ref role="NX6Kv" node="5qm50Y0edyd" resolve="ext" />
        </node>
        <node concept="NX1gA" id="5qm50Y0elK4" role="NXodf">
          <property role="NX6R2" value="Is current user admin (deprecated)" />
          <ref role="NX6Kv" node="3zYHvadTSnl" resolve="isAdmin" />
        </node>
        <node concept="NX1gA" id="5qm50Y0emrg" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Is group hidden" />
          <ref role="NX6Kv" node="3pJJa69XRFT" resolve="isHidden" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eCqk" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Group type" />
          <ref role="NX6Kv" node="5qm50Y0eCpm" resolve="groupType" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6pT" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="wb0Ql" id="5qm50Y0e7hk" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <property role="toYog" value="true" />
          <node concept="2m5ndN" id="GBscvBB6sr" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="GBscvBB6su" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="creatorUid" />
          <node concept="wb0Ql" id="5qm50Y0efts" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
        <node concept="2m7Kf5" id="3zYHvadTSnl" role="2m0hLx">
          <property role="2m7DUN" value="16" />
          <property role="TrG5h" value="isAdmin" />
          <property role="toYog" value="true" />
          <node concept="2m5nlT" id="3zYHvadTSnL" role="2m7DVh">
            <node concept="2m5ndN" id="3zYHvadTSnR" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZ2wi" role="2m0hLx">
          <property role="2m7DUN" value="17" />
          <property role="TrG5h" value="theme" />
          <node concept="2m5nlT" id="6WYZhOUZ2wU" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUZ2x0" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZ45X" role="2m0hLx">
          <property role="2m7DUN" value="18" />
          <property role="TrG5h" value="about" />
          <node concept="2m5nlT" id="6WYZhOUZ46C" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUZ46I" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="3pJJa69XRFT" role="2m0hLx">
          <property role="2m7DUN" value="20" />
          <property role="TrG5h" value="isHidden" />
          <node concept="2m5nlT" id="3pJJa69XSfL" role="2m7DVh">
            <node concept="2m5ndN" id="3pJJa69XSfR" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5qm50Y0eCpm" role="2m0hLx">
          <property role="2m7DUN" value="23" />
          <property role="TrG5h" value="groupType" />
          <node concept="2m5nlT" id="5qm50Y0eCpV" role="2m7DVh">
            <node concept="2m5ndX" id="5qm50Y0eCq1" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5qm50Y0edyd" role="2m0hLx">
          <property role="2m7DUN" value="22" />
          <property role="TrG5h" value="ext" />
          <node concept="2m5nlT" id="5qm50Y0edz1" role="2m7DVh">
            <node concept="2m5mGg" id="5qm50Y0edz7" role="3GH5xg">
              <ref role="2m5mJy" node="2WAO9Y$lCqr" resolve="MapValue" />
            </node>
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
        <node concept="NX1gA" id="6WYZhOUYXAa" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="is member admin of group" />
          <ref role="NX6Kv" node="6WYZhOUYX4O" resolve="isAdmin" />
        </node>
        <node concept="2m7Kf5" id="7d$A0Kt1Y6_" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0e7TA" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="7d$A0Kt1Y6G" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="inviterUid" />
          <node concept="wb0Ql" id="5qm50Y0e7TD" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="7d$A0Kt1Y6P" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="2m5ndQ" id="7d$A0Kt1Y6X" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYX4O" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="isAdmin" />
          <node concept="2m5nlT" id="6WYZhOUYX4Y" role="2m7DVh">
            <node concept="2m5ndN" id="6WYZhOUYX57" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBsbt" role="2m5mJr">
        <property role="TrG5h" value="CreateGroupObsolete" />
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
          <property role="NXePf" value="Creating group chat (obsolete)" />
        </node>
        <node concept="NX1gA" id="2EAJ7H6foAL" role="1GBnQ6">
          <property role="NX6R2" value="Random Id for avoiding double create" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBsbN" resolve="rid" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERalL" role="1GBnQ6">
          <property role="NX6R2" value="Title of new group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBsbU" resolve="title" />
        </node>
        <node concept="NX1gA" id="4zDDY4ERalZ" role="1GBnQ6">
          <property role="NX6R2" value="Members of new group" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="GBscvBBsc3" resolve="users" />
        </node>
      </node>
      <node concept="2m6fVq" id="5qm50Y0e2IH" role="2m5mJr">
        <property role="TrG5h" value="CreateGroup" />
        <node concept="2m7Kf5" id="5qm50Y0e3um" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="5qm50Y0e3uq" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5qm50Y0e3ut" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="5qm50Y0e8xV" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5qm50Y0e8xY" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="5qm50Y0e8y6" role="2m7DVh">
            <node concept="2m5mGg" id="5qm50Y0e8yc" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="5qm50Y0eAvD" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="groupType" />
          <node concept="2m5nlT" id="5qm50Y0eAvR" role="2m7DVh">
            <node concept="2m5ndX" id="5qm50Y0eAw0" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5qm50Y0e9aN" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="userData" />
          <node concept="2m5nlT" id="5qm50Y0edy4" role="2m7DVh">
            <node concept="2m5mGg" id="5qm50Y0edya" role="3GH5xg">
              <ref role="2m5mJy" node="2WAO9Y$lCqr" resolve="MapValue" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5qm50Y0e2II" role="NuuwV">
          <property role="Nu42W" value="E6" />
        </node>
        <node concept="2m1R6W" id="5qm50Y0e2Q2" role="2m6efq">
          <node concept="2m7Kf5" id="5qm50Y0e9Nl" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="5qm50Y0e9Np" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="5qm50Y0e9Ns" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="5qm50Y0e9Ny" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="2m7Kf5" id="5qm50Y0e9N_" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="group" />
            <node concept="2m5mGg" id="5qm50Y0easE" role="2m7DVh">
              <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
            </node>
          </node>
          <node concept="2m7Kf5" id="5qm50Y0e9NQ" role="2m0hLx">
            <property role="2m7DUN" value="4" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="5qm50Y0e9O1" role="2m7DVh">
              <node concept="2m5mGg" id="5qm50Y0e9O7" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="5qm50Y0e2Q3" role="NuuwV">
            <property role="Nu42W" value="D8" />
          </node>
          <node concept="NXeRC" id="5qm50Y0eD2Y" role="1y2DgH">
            <property role="NXePf" value="Created group" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eD33" role="1y2DgH">
            <property role="NX6R2" value="Update Seq" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="5qm50Y0e9Nl" resolve="seq" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eD3b" role="1y2DgH">
            <property role="1GSvIU" value="compact" />
            <property role="NX6R2" value="Update state" />
            <ref role="NX6Kv" node="5qm50Y0e9Ns" resolve="state" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eD3l" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Created group" />
            <ref role="NX6Kv" node="5qm50Y0e9N_" resolve="group" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eD3x" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Referenced users" />
            <ref role="NX6Kv" node="5qm50Y0e9NQ" resolve="users" />
          </node>
        </node>
        <node concept="NXeRC" id="5qm50Y0e2Q7" role="1GBnQ6">
          <property role="NXePf" value="Creating group chat" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eb5k" role="1GBnQ6">
          <property role="NX6R2" value="Random Id for avoiding double create" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5qm50Y0e3um" resolve="rid" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eb5s" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Group Title" />
          <ref role="NX6Kv" node="5qm50Y0e3ut" resolve="title" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eb5A" role="1GBnQ6">
          <property role="NX6R2" value="Members of group" />
          <ref role="NX6Kv" node="5qm50Y0e8xY" resolve="users" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eB8$" role="1GBnQ6">
          <property role="NX6R2" value="Group Type. 'public' for public groups" />
          <ref role="NX6Kv" node="5qm50Y0eAvD" resolve="groupType" />
        </node>
        <node concept="NX1gA" id="5qm50Y0edxX" role="1GBnQ6">
          <property role="NX6R2" value="Group external data" />
          <ref role="NX6Kv" node="5qm50Y0e9aN" resolve="userData" />
        </node>
      </node>
      <node concept="2m6fVq" id="3aztRmLKfon" role="2m5mJr">
        <property role="TrG5h" value="EnterGroup" />
        <node concept="NXeRC" id="3aztRmLKhr3" role="1GBnQ6">
          <property role="NXePf" value="Join random group by peer id" />
        </node>
        <node concept="NX1gA" id="3aztRmLKhUa" role="1GBnQ6">
          <property role="NX6R2" value="Public group peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3aztRmLKgUx" resolve="peer" />
        </node>
        <node concept="2m7Kf5" id="3aztRmLKgUx" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="3aztRmLKgU_" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="3aztRmLKfoo" role="NuuwV">
          <property role="Nu42W" value="C7" />
        </node>
        <node concept="2m1R6W" id="3aztRmLKgUC" role="2m6efq">
          <node concept="2m7Kf5" id="3aztRmLKgUH" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="group" />
            <node concept="2m5mGg" id="3aztRmLKgUL" role="2m7DVh">
              <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
            </node>
          </node>
          <node concept="2m7Kf5" id="3aztRmLKgUO" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="3aztRmLKgUU" role="2m7DVh">
              <node concept="2m5mGg" id="3aztRmLKgV0" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="3aztRmLKgV3" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="rid" />
            <node concept="wb0Ql" id="3aztRmLKgVc" role="2m7DVh">
              <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
            </node>
          </node>
          <node concept="2m7Kf5" id="3aztRmLKgVf" role="2m0hLx">
            <property role="2m7DUN" value="4" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="3aztRmLKgVq" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="3aztRmLKgVt" role="2m0hLx">
            <property role="2m7DUN" value="5" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="3aztRmLKgVE" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="2m7Kf5" id="3aztRmLKgVH" role="2m0hLx">
            <property role="2m7DUN" value="6" />
            <property role="TrG5h" value="date" />
            <node concept="wb0Ql" id="3aztRmLKgVW" role="2m7DVh">
              <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
            </node>
          </node>
          <node concept="Nu42z" id="3aztRmLKgUD" role="NuuwV">
            <property role="Nu42W" value="C8" />
          </node>
          <node concept="NXeRC" id="5qm50Y0eF_w" role="1y2DgH">
            <property role="NXePf" value="Joined group" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eGe4" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Joined group" />
            <ref role="NX6Kv" node="3aztRmLKgUH" resolve="group" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eGec" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Referenced users" />
            <ref role="NX6Kv" node="3aztRmLKgUO" resolve="users" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eGem" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Rid of join message" />
            <ref role="NX6Kv" node="3aztRmLKgV3" resolve="rid" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eGey" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Seq of Update" />
            <ref role="NX6Kv" node="3aztRmLKgVf" resolve="seq" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eGeK" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="State of Update" />
            <ref role="NX6Kv" node="3aztRmLKgVt" resolve="state" />
          </node>
          <node concept="NX1gA" id="5qm50Y0eGf0" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Date of update" />
            <ref role="NX6Kv" node="3aztRmLKgVH" resolve="date" />
          </node>
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
      <node concept="2m6fVq" id="6WYZhOUZfvG" role="2m5mJr">
        <property role="TrG5h" value="EditGroupTopic" />
        <node concept="NXeRC" id="6WYZhOUZhVs" role="1GBnQ6">
          <property role="NXePf" value="Edit group topic" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZit_" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZhbS" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZitH" role="1GBnQ6">
          <property role="NX6R2" value="Random id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZhbZ" resolve="rid" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZiZW" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="New Topic" />
          <ref role="NX6Kv" node="6WYZhOUZhc8" resolve="topic" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZhbS" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="6WYZhOUZhbW" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZhbZ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="6WYZhOUZhc5" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZhc8" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="topic" />
          <node concept="2m5nlT" id="6WYZhOUZhcg" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUZhcm" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUZfvH" role="NuuwV">
          <property role="Nu42W" value="D3" />
        </node>
        <node concept="2m1Rp1" id="6WYZhOUZhpl" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUZjy8" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="ACCESS_DENIED" />
          <property role="2uCiSL" value="User can't change group's topic" />
        </node>
      </node>
      <node concept="2m6fVq" id="6WYZhOUZkaT" role="2m5mJr">
        <property role="TrG5h" value="EditGroupAbout" />
        <node concept="NXeRC" id="6WYZhOUZlmF" role="1GBnQ6">
          <property role="NXePf" value="Edit Group About" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZlT3" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZkhz" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZlTb" role="1GBnQ6">
          <property role="NX6R2" value="Random id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZkhE" resolve="rid" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZlTl" role="1GBnQ6">
          <property role="NX6R2" value="New About" />
          <ref role="NX6Kv" node="6WYZhOUZkhN" resolve="about" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZkhz" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="6WYZhOUZkhB" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZkhE" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="6WYZhOUZkhK" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZkhN" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="about" />
          <node concept="2m5nlT" id="6WYZhOUZkhV" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUZki1" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUZkaU" role="NuuwV">
          <property role="Nu42W" value="D5" />
        </node>
        <node concept="2m1Rp1" id="6WYZhOUZki4" role="2m6efq">
          <ref role="2m1o9l" node="2vxDjotnSoJ" resolve="SeqDate" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUZmrK" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="ACCESS_DENIED" />
          <property role="2uCiSL" value="User can't change group's about info" />
        </node>
      </node>
      <node concept="2m6fVq" id="6WYZhOUZn4Z" role="2m5mJr">
        <property role="TrG5h" value="MakeUserAdmin" />
        <node concept="2uC4CA" id="6WYZhOUZp_J" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="ALREADY_ADMIN" />
          <property role="2uCiSL" value="Member is already admin" />
        </node>
        <node concept="NXeRC" id="6WYZhOUZovf" role="1GBnQ6">
          <property role="NXePf" value="Make user admin" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZovl" role="1GBnQ6">
          <property role="NX6R2" value="Group's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZnpy" resolve="groupPeer" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZovC" role="1GBnQ6">
          <property role="NX6R2" value="User's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZnq0" resolve="userPeer" />
        </node>
        <node concept="2uC4CA" id="6WYZhOUZnWH" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="ACCESS_DENIED" />
          <property role="2uCiSL" value="User can't change group's admins" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZnpy" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupPeer" />
          <node concept="2m5mGg" id="6WYZhOUZnpA" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZnq0" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="userPeer" />
          <node concept="2m5mGg" id="6WYZhOUZnq6" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUZn50" role="NuuwV">
          <property role="Nu42W" value="D6" />
        </node>
        <node concept="2m1R6W" id="6WYZhOUZovJ" role="2m6efq">
          <node concept="NX1gA" id="6WYZhOUZp2G" role="1y2DgH">
            <property role="NX6R2" value="new members" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="6WYZhOUZovO" resolve="members" />
          </node>
          <node concept="2m7Kf5" id="6WYZhOUZovO" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="members" />
            <node concept="2m5nlk" id="6WYZhOUZovS" role="2m7DVh">
              <node concept="2m5mGg" id="6WYZhOUZovY" role="3GJlyp">
                <ref role="2m5mJy" node="7d$A0Kt1Y2M" resolve="Member" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="6WYZhOUZp2J" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="seq" />
            <node concept="2m5ndE" id="6WYZhOUZp2Q" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="6WYZhOUZp2T" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="state" />
            <node concept="wb0Ql" id="6WYZhOUZp32" role="2m7DVh">
              <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
            </node>
          </node>
          <node concept="Nu42z" id="6WYZhOUZovK" role="NuuwV">
            <property role="Nu42W" value="D7" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eNvy" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eNv_" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eNvC" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eNvF" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBz2e" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="inviterUid" />
          <node concept="wb0Ql" id="5qm50Y0eNvI" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eOLo" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eO8z" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0ePqd" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eQ32" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBzqX" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="kickerUid" />
          <node concept="wb0Ql" id="5qm50Y0eQ35" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eQ38" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eQFX" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eQG0" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
      <node concept="NpBTk" id="6WYZhOUZ0P2" role="2m5mJr">
        <property role="TrG5h" value="GroupTopicChanged" />
        <node concept="NXeRC" id="6WYZhOUZ1t0" role="NXp_2">
          <property role="NXePf" value="Update about group topic change" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZ1t6" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZ0UP" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZ1Yo" role="NXp_2">
          <property role="NX6R2" value="Changer UID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZ0V5" resolve="uid" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZ1Yy" role="NXp_2">
          <property role="NX6R2" value="New topic of group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZ0Vg" resolve="topic" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZ1YI" role="NXp_2">
          <property role="NX6R2" value="Date of theme change" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZ0VA" resolve="date" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZ1YW" role="NXp_2">
          <property role="NX6R2" value="Randomd Id of operation" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZ0UW" resolve="rid" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZ0UP" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="wb0Ql" id="5qm50Y0eRkP" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZ0UW" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="6WYZhOUZ0V2" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZ0V5" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0eRkS" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZ0Vg" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="topic" />
          <node concept="2m5nlT" id="6WYZhOUZ0Vt" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUZ0Vz" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZ0VA" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="6WYZhOUZ0VN" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUZ0P3" role="NuuwV">
          <property role="Nu42W" value="D5" />
        </node>
      </node>
      <node concept="NpBTk" id="6WYZhOUZcCD" role="2m5mJr">
        <property role="TrG5h" value="GroupAboutChanged" />
        <node concept="NXeRC" id="6WYZhOUZdNu" role="NXp_2">
          <property role="NXePf" value="Update about group about change" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZelq" role="NXp_2">
          <property role="NX6R2" value="Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZcIR" resolve="groupId" />
        </node>
        <node concept="NX1gA" id="6WYZhOUZely" role="NXp_2">
          <property role="NX6R2" value="Group about" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUZdho" resolve="about" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZcIR" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="groupId" />
          <node concept="wb0Ql" id="5qm50Y0eRkV" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6WYZhOUZdho" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="about" />
          <node concept="2m5nlT" id="6WYZhOUZdhu" role="2m7DVh">
            <node concept="2m5ndX" id="6WYZhOUZdh$" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6WYZhOUZcCE" role="NuuwV">
          <property role="Nu42W" value="D6" />
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
          <node concept="wb0Ql" id="5qm50Y0eRXK" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eRXN" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
        <node concept="NX1gA" id="6WYZhOUYV1k" role="1GBnQ6">
          <property role="NX6R2" value="Url or Token for joining to group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="1$yIuJFB1yH" resolve="token" />
        </node>
        <node concept="2m7Kf5" id="1$yIuJFB1yH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="token" />
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
    <node concept="2m5mJO" id="2EyE8f8Bcr7" role="2m5lHt">
      <property role="TrG5h" value="Stickers" />
      <property role="3XOG$Z" value="stickers" />
      <node concept="2m5naR" id="2EyE8f8BdOZ" role="2m5mJr">
        <property role="TrG5h" value="StickerDescriptor" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="2EyE8f8Bfg8" role="NXodf">
          <property role="NXePf" value="Descriptor of a Sticker" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bfge" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Sticker unique id" />
          <ref role="NX6Kv" node="2EyE8f8BeyG" resolve="id" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bfgm" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Emoji code for sticker" />
          <ref role="NX6Kv" node="2EyE8f8Beyv" resolve="emoji" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bfgw" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Image of sticker 128x128 in WebP format" />
          <ref role="NX6Kv" node="2EyE8f8BdPb" resolve="image128" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BfgG" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Image of sticker 512x512 in WebP format" />
          <ref role="NX6Kv" node="2EyE8f8BdP2" resolve="image512" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BgFH" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Image of sticker 256x256 in WebP format" />
          <ref role="NX6Kv" node="2EyE8f8BgFn" resolve="image256" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BeyG" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="2EyE8f8BeyQ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8Beyv" role="2m0hLx">
          <property role="TrG5h" value="emoji" />
          <property role="2m7DUN" value="2" />
          <node concept="2m5nlT" id="2EyE8f8BgG9" role="2m7DVh">
            <node concept="2m5ndX" id="2EyE8f8BgGf" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BdPb" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="image128" />
          <node concept="2m5mGg" id="2EyE8f8BIqr" role="2m7DVh">
            <ref role="2m5mJy" node="64HNz1IoSEt" resolve="ImageLocation" />
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BdP2" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="image512" />
          <node concept="2m5nlT" id="2EyE8f8BgFR" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8BgFX" role="3GH5xg">
              <ref role="2m5mJy" node="64HNz1IoSEt" resolve="ImageLocation" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BgFn" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="image256" />
          <node concept="2m5nlT" id="2EyE8f8BgGi" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8BgGo" role="3GH5xg">
              <ref role="2m5mJy" node="64HNz1IoSEt" resolve="ImageLocation" />
            </node>
          </node>
        </node>
      </node>
      <node concept="2m5naR" id="2EyE8f8BjxK" role="2m5mJr">
        <property role="TrG5h" value="StickerCollection" />
        <property role="tsOgz" value="true" />
        <node concept="2m7Kf5" id="2EyE8f8Bjy8" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="2EyE8f8Bjyk" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BpY3" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="2EyE8f8BpYh" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8Bjyn" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="stickers" />
          <node concept="2m5nlk" id="2EyE8f8Bjyt" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8Bjyz" role="3GJlyp">
              <ref role="2m5mJy" node="2EyE8f8BdOZ" resolve="StickerDescriptor" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="2EyE8f8BjyA" role="NXodf">
          <property role="NXePf" value="Sticker collection" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BlFv" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Unique id of a collection" />
          <ref role="NX6Kv" node="2EyE8f8Bjy8" resolve="id" />
        </node>
        <node concept="NX1gA" id="2EyE8f8Bmpa" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Access Hash of a collection" />
          <ref role="NX6Kv" node="2EyE8f8BpY3" resolve="accessHash" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BpYw" role="NXodf">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Sticker list" />
          <ref role="NX6Kv" node="2EyE8f8Bjyn" resolve="stickers" />
        </node>
      </node>
      <node concept="2m62dX" id="2EyE8f8BK_Y" role="2m5mJr">
        <property role="TrG5h" value="StickersReponse" />
        <node concept="2m7Kf5" id="2EyE8f8BKAM" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="collections" />
          <node concept="2m5nlk" id="2EyE8f8BKAQ" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8BKAW" role="3GJlyp">
              <ref role="2m5mJy" node="2EyE8f8BjxK" resolve="StickerCollection" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BKAZ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="seq" />
          <node concept="2m5ndE" id="2EyE8f8BKB6" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BKB9" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="2EyE8f8BKBi" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="Nu42z" id="2EyE8f8BK_Z" role="NuuwV">
          <property role="Nu42W" value="F0" />
        </node>
        <node concept="NXeRC" id="2EyE8f8BKBl" role="NXp4Y">
          <property role="NXePf" value="Stickers response" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BKBq" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Sticker collections" />
          <ref role="NX6Kv" node="2EyE8f8BKAM" resolve="collections" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BKBy" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Seq of update" />
          <ref role="NX6Kv" node="2EyE8f8BKAZ" resolve="seq" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BKBG" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="State of update" />
          <ref role="NX6Kv" node="2EyE8f8BKB9" resolve="state" />
        </node>
      </node>
      <node concept="2m6fVq" id="2EyE8f8BwNe" role="2m5mJr">
        <property role="TrG5h" value="LoadOwnStickers" />
        <node concept="Nu42z" id="2EyE8f8BwNf" role="NuuwV">
          <property role="Nu42W" value="EE" />
        </node>
        <node concept="2m1R6W" id="2EyE8f8BwNR" role="2m6efq">
          <node concept="2m7Kf5" id="2EyE8f8BwNW" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="ownStickers" />
            <node concept="2m5nlk" id="2EyE8f8BwO0" role="2m7DVh">
              <node concept="2m5mGg" id="2EyE8f8BwO6" role="3GJlyp">
                <ref role="2m5mJy" node="2EyE8f8BjxK" resolve="StickerCollection" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="2EyE8f8BwNS" role="NuuwV">
            <property role="Nu42W" value="EF" />
          </node>
          <node concept="NXeRC" id="2EyE8f8BwOb" role="1y2DgH">
            <property role="NXePf" value="Own Stickers collections" />
          </node>
          <node concept="NX1gA" id="2EyE8f8BQFk" role="1y2DgH">
            <property role="NX6R2" value="Own sticker collections" />
            <ref role="NX6Kv" node="2EyE8f8BwNW" resolve="ownStickers" />
          </node>
        </node>
        <node concept="NXeRC" id="2EyE8f8BwO9" role="1GBnQ6">
          <property role="NXePf" value="Loading own stickers" />
        </node>
      </node>
      <node concept="NpBTk" id="2EyE8f8Byh3" role="2m5mJr">
        <property role="TrG5h" value="OwnStickersChanged" />
        <node concept="NXeRC" id="2EyE8f8BJQW" role="NXp_2">
          <property role="NXePf" value="Own Stickers changed" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BQFd" role="NXp_2">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="New own sticker collections" />
          <ref role="NX6Kv" node="2EyE8f8BJ8$" resolve="collections" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BJ8$" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="collections" />
          <node concept="2m5nlk" id="2EyE8f8BJ8C" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8BJ8I" role="3GJlyp">
              <ref role="2m5mJy" node="2EyE8f8BjxK" resolve="StickerCollection" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="2EyE8f8Byh4" role="NuuwV">
          <property role="Nu42W" value="A1" />
        </node>
      </node>
      <node concept="NpBTk" id="2EyE8f8BQDh" role="2m5mJr">
        <property role="TrG5h" value="StickerCollectionsChanged" />
        <node concept="2m7Kf5" id="2EyE8f8BQEP" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="collections" />
          <node concept="2m5nlk" id="2EyE8f8BQF1" role="2m7DVh">
            <node concept="2m5mGg" id="2EyE8f8BQF7" role="3GJlyp">
              <ref role="2m5mJy" node="2EyE8f8BjxK" resolve="StickerCollection" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="2EyE8f8BQDi" role="NuuwV">
          <property role="Nu42W" value="A4" />
        </node>
        <node concept="NXeRC" id="2EyE8f8BRqp" role="NXp_2">
          <property role="NXePf" value="Sticker collection changed" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BRqu" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Updated sticker collections" />
          <ref role="NX6Kv" node="2EyE8f8BQEP" resolve="collections" />
        </node>
      </node>
      <node concept="2m6fVq" id="2EyE8f8BMNf" role="2m5mJr">
        <property role="TrG5h" value="AddStickerCollection" />
        <node concept="2m7Kf5" id="2EyE8f8BNyQ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="2EyE8f8BNyU" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BNyX" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="2EyE8f8BNz3" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="2EyE8f8BMOo" role="1GBnQ6">
          <property role="NXePf" value="Adding sticker collection" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BNz9" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Collection id" />
          <ref role="NX6Kv" node="2EyE8f8BNyQ" resolve="id" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BNzh" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Collection access hash" />
          <ref role="NX6Kv" node="2EyE8f8BNyX" resolve="accessHash" />
        </node>
        <node concept="Nu42z" id="2EyE8f8BMNg" role="NuuwV">
          <property role="Nu42W" value="F4" />
        </node>
        <node concept="2m1Rp1" id="2EyE8f8BMOj" role="2m6efq">
          <ref role="2m1o9l" node="2EyE8f8BK_Y" resolve="StickersReponse" />
        </node>
      </node>
      <node concept="2m6fVq" id="2EyE8f8BOiZ" role="2m5mJr">
        <property role="TrG5h" value="RemoveStickerCollection" />
        <node concept="NXeRC" id="2EyE8f8BOkz" role="1GBnQ6">
          <property role="NXePf" value="Removing sticker collection" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BOkD" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Collection id" />
          <ref role="NX6Kv" node="2EyE8f8BOkd" resolve="id" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BOkL" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Collection access hash" />
          <ref role="NX6Kv" node="2EyE8f8BOkk" resolve="accessHash" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BOkd" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="2EyE8f8BOkh" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BOkk" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="2EyE8f8BOkq" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2EyE8f8BOj0" role="NuuwV">
          <property role="Nu42W" value="F5" />
        </node>
        <node concept="2m1Rp1" id="2EyE8f8BOku" role="2m6efq">
          <ref role="2m1o9l" node="2EyE8f8BK_Y" resolve="StickersReponse" />
        </node>
      </node>
      <node concept="2m6fVq" id="2EyE8f8BP4N" role="2m5mJr">
        <property role="TrG5h" value="LoadStickerCollection" />
        <node concept="NXeRC" id="2EyE8f8BPSq" role="1GBnQ6">
          <property role="NXePf" value="Loading stickers" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BPSw" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Collection id" />
          <ref role="NX6Kv" node="2EyE8f8BP6b" resolve="id" />
        </node>
        <node concept="NX1gA" id="2EyE8f8BPSC" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Collection access hash" />
          <ref role="NX6Kv" node="2EyE8f8BPPb" resolve="accessHash" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BP6b" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="2EyE8f8BPP8" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2EyE8f8BPPb" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="2EyE8f8BPPh" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2EyE8f8BP4O" role="NuuwV">
          <property role="Nu42W" value="F6" />
        </node>
        <node concept="2m1R6W" id="2EyE8f8BPS4" role="2m6efq">
          <node concept="2m7Kf5" id="2EyE8f8BPS9" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="collection" />
            <node concept="2m5mGg" id="2EyE8f8BPSl" role="2m7DVh">
              <ref role="2m5mJy" node="2EyE8f8BjxK" resolve="StickerCollection" />
            </node>
          </node>
          <node concept="Nu42z" id="2EyE8f8BPS5" role="NuuwV">
            <property role="Nu42W" value="F7" />
          </node>
          <node concept="NXeRC" id="2EyE8f8BPSH" role="1y2DgH">
            <property role="NXePf" value="Loaded collection" />
          </node>
          <node concept="NX1gA" id="2EyE8f8BPSM" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Collection of stickers" />
            <ref role="NX6Kv" node="2EyE8f8BPS9" resolve="collection" />
          </node>
        </node>
      </node>
      <node concept="1Dx9M1" id="2EyE8f8Bd7R" role="1Dx9rD">
        <property role="1Dx9K7" value="Stickers support in Actor" />
      </node>
    </node>
    <node concept="2m5mJO" id="4NJj1GT1QnC" role="2m5lHt">
      <property role="TrG5h" value="Search" />
      <property role="3XOG$Z" value="search" />
      <node concept="2m488m" id="4NJj1GT1U0C" role="2m5mJr">
        <property role="TrG5h" value="SearchPeerType" />
        <node concept="2m7y0F" id="4NJj1GT1U0E" role="2m7ymf">
          <property role="TrG5h" value="Groups" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="4NJj1GT1U0M" role="2m7ymf">
          <property role="TrG5h" value="Contacts" />
          <property role="2m7y0m" value="2" />
        </node>
        <node concept="2m7y0F" id="4NJj1GT224s" role="2m7ymf">
          <property role="TrG5h" value="Public" />
          <property role="2m7y0m" value="3" />
        </node>
      </node>
      <node concept="2m488m" id="5qm50Y0f7oy" role="2m5mJr">
        <property role="TrG5h" value="SearchContentType" />
        <node concept="2m7y0F" id="5qm50Y0f7o$" role="2m7ymf">
          <property role="TrG5h" value="Any" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="5qm50Y0f7q2" role="2m7ymf">
          <property role="TrG5h" value="Text" />
          <property role="2m7y0m" value="2" />
        </node>
        <node concept="2m7y0F" id="5qm50Y0f7q5" role="2m7ymf">
          <property role="TrG5h" value="Links" />
          <property role="2m7y0m" value="3" />
        </node>
        <node concept="2m7y0F" id="5qm50Y0f7q9" role="2m7ymf">
          <property role="TrG5h" value="Documents" />
          <property role="2m7y0m" value="4" />
        </node>
        <node concept="2m7y0F" id="5qm50Y0f7qe" role="2m7ymf">
          <property role="TrG5h" value="Photos" />
          <property role="2m7y0m" value="5" />
        </node>
      </node>
      <node concept="w93zz" id="4NJj1GT1Sc1" role="2m5mJr">
        <property role="TrG5h" value="SearchCondition" />
        <property role="1FaRnq" value="true" />
      </node>
      <node concept="2m5naR" id="4NJj1GT1TpF" role="2m5mJr">
        <property role="TrG5h" value="SearchPeerTypeCondition" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="4NJj1GT1Sc1" resolve="SearchCondition" />
        <node concept="2m7Kf5" id="4NJj1GT1UBJ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peerType" />
          <node concept="3GJkcs" id="4NJj1GT1UBN" role="2m7DVh">
            <ref role="3GJkik" node="4NJj1GT1U0C" resolve="SearchPeerType" />
          </node>
        </node>
        <node concept="NXeRC" id="4NJj1GT1VeG" role="NXodf">
          <property role="NXePf" value="Search peer type condition" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1VQY" role="NXodf">
          <property role="NX6R2" value="Peer type for searching" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1UBJ" resolve="peerType" />
        </node>
        <node concept="Nu42z" id="4NJj1GT1VR2" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
      </node>
      <node concept="2m5naR" id="4NJj1GT1Wun" role="2m5mJr">
        <property role="TrG5h" value="SearchPieceText" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="4NJj1GT1Sc1" resolve="SearchCondition" />
        <node concept="2m7Kf5" id="4NJj1GT1WuH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="query" />
          <node concept="2m5ndX" id="4NJj1GT1WuL" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="4NJj1GT1WuF" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1YjU" role="NXodf">
          <property role="NXePf" value="Search peer name condition" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1YV2" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Search query" />
          <ref role="NX6Kv" node="4NJj1GT1WuH" resolve="query" />
        </node>
      </node>
      <node concept="2m5naR" id="5qm50Y0eYkm" role="2m5mJr">
        <property role="TrG5h" value="SearchAndCondition" />
        <property role="tsOgz" value="false" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="4NJj1GT1Sc1" resolve="SearchCondition" />
        <node concept="2m7Kf5" id="5qm50Y0eYXU" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="andQuery" />
          <node concept="2m5nlk" id="5qm50Y0eYXY" role="2m7DVh">
            <node concept="3BlaRf" id="5qm50Y0eYY4" role="3GJlyp">
              <ref role="3BrLez" node="4NJj1GT1Sc1" resolve="SearchCondition" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="5qm50Y0eYY7" role="NXodf">
          <property role="NXePf" value="Search AND condion" />
        </node>
        <node concept="NX1gA" id="5qm50Y0eZAF" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="And Query" />
          <ref role="NX6Kv" node="5qm50Y0eYXU" resolve="andQuery" />
        </node>
        <node concept="Nu42z" id="5qm50Y0f0ff" role="3BtCOu">
          <property role="Nu42W" value="03" />
        </node>
      </node>
      <node concept="2m5naR" id="5qm50Y0f0T1" role="2m5mJr">
        <property role="TrG5h" value="SearchOrCondition" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="4NJj1GT1Sc1" resolve="SearchCondition" />
        <node concept="2m7Kf5" id="5qm50Y0f0Uj" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="orQuery" />
          <node concept="2m5nlk" id="5qm50Y0f0Un" role="2m7DVh">
            <node concept="3BlaRf" id="6nbRE0KfzHc" role="3GJlyp">
              <ref role="3BrLez" node="4NJj1GT1Sc1" resolve="SearchCondition" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5qm50Y0f0Uh" role="3BtCOu">
          <property role="Nu42W" value="04" />
        </node>
        <node concept="NXeRC" id="5qm50Y0f2bG" role="NXodf">
          <property role="NXePf" value="Search OR condition" />
        </node>
        <node concept="NX1gA" id="5qm50Y0f2Oo" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Or Query" />
          <ref role="NX6Kv" node="5qm50Y0f0Uj" resolve="orQuery" />
        </node>
      </node>
      <node concept="2m5naR" id="5qm50Y0f3uq" role="2m5mJr">
        <property role="TrG5h" value="SearchPeerCondition" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="4NJj1GT1Sc1" resolve="SearchCondition" />
        <node concept="2m7Kf5" id="5qm50Y0f4Lb" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="5qm50Y0f4Lf" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="5qm50Y0f3vL" role="3BtCOu">
          <property role="Nu42W" value="05" />
        </node>
        <node concept="NXeRC" id="5qm50Y0f3vN" role="NXodf">
          <property role="NXePf" value="Serch Peer condition" />
        </node>
        <node concept="NX1gA" id="5qm50Y0f62K" role="NXodf">
          <property role="NX6R2" value="Peer condition" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5qm50Y0f4Lb" resolve="peer" />
        </node>
      </node>
      <node concept="2m5naR" id="5qm50Y0f851" role="2m5mJr">
        <property role="TrG5h" value="SearchPeerContentType" />
        <property role="tsOgz" value="false" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="4NJj1GT1Sc1" resolve="SearchCondition" />
        <node concept="2m7Kf5" id="5qm50Y0f86A" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="contentType" />
          <node concept="3GJkcs" id="5qm50Y0f86E" role="2m7DVh">
            <ref role="3GJkik" node="5qm50Y0f7oy" resolve="SearchContentType" />
          </node>
        </node>
        <node concept="Nu42z" id="5qm50Y0f86$" role="3BtCOu">
          <property role="Nu42W" value="06" />
        </node>
        <node concept="NXeRC" id="5qm50Y0f86H" role="NXodf">
          <property role="NXePf" value="Search content type condition" />
        </node>
        <node concept="NX1gA" id="5qm50Y0f8JF" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Content Type" />
          <ref role="NX6Kv" node="5qm50Y0f86A" resolve="contentType" />
        </node>
      </node>
      <node concept="2m5naR" id="MlriG8H5kU" role="2m5mJr">
        <property role="TrG5h" value="SearchSenderIdConfition" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="4NJj1GT1Sc1" resolve="SearchCondition" />
        <node concept="2m7Kf5" id="MlriG8H5mE" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="senderId" />
          <node concept="wb0Ql" id="MlriG8H5mI" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="Nu42z" id="MlriG8H5mC" role="3BtCOu">
          <property role="Nu42W" value="07" />
        </node>
        <node concept="NXeRC" id="MlriG8H5mL" role="NXodf">
          <property role="NXePf" value="Searching sender uid condition" />
        </node>
        <node concept="NX1gA" id="MlriG8H5mQ" role="NXodf">
          <property role="NX6R2" value="sender UID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="MlriG8H5mE" resolve="senderId" />
        </node>
      </node>
      <node concept="2m5naR" id="4NJj1GT1Zyz" role="2m5mJr">
        <property role="TrG5h" value="PeerSearchResult" />
        <node concept="2m7Kf5" id="4NJj1GT1ZyX" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="4NJj1GT1Zz1" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1Zz4" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="4NJj1GT1Zza" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="4NJj1GT20aB" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="description" />
          <node concept="2m5nlT" id="4NJj1GT20aJ" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT20aP" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT20aS" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="membersCount" />
          <node concept="2m5nlT" id="4NJj1GT20b3" role="2m7DVh">
            <node concept="2m5ndE" id="4NJj1GT20b9" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT20bc" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="dateCreated" />
          <node concept="2m5nlT" id="4NJj1GT20bq" role="2m7DVh">
            <node concept="wb0Ql" id="4NJj1GT20bz" role="3GH5xg">
              <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT20bA" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="creator" />
          <node concept="2m5nlT" id="4NJj1GT20c4" role="2m7DVh">
            <node concept="2m5ndE" id="4NJj1GT20ca" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT20NO" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="isPublic" />
          <node concept="2m5nlT" id="4NJj1GT20O8" role="2m7DVh">
            <node concept="2m5ndN" id="4NJj1GT20Oe" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT2965" role="2m0hLx">
          <property role="2m7DUN" value="8" />
          <property role="TrG5h" value="isJoined" />
          <node concept="2m5nlT" id="4NJj1GT29I0" role="2m7DVh">
            <node concept="2m5ndN" id="4NJj1GT29I6" role="3GH5xg" />
          </node>
        </node>
        <node concept="NXeRC" id="4NJj1GT20Ny" role="NXodf">
          <property role="NXePf" value="Peer search result" />
        </node>
        <node concept="NX1gA" id="4NJj1GT20NB" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Peer information" />
          <ref role="NX6Kv" node="4NJj1GT1ZyX" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4NJj1GT20NJ" role="NXodf">
          <property role="NX6R2" value="Peer title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1Zz4" resolve="title" />
        </node>
        <node concept="NX1gA" id="4NJj1GT20Om" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Description" />
          <ref role="NX6Kv" node="4NJj1GT20aB" resolve="description" />
        </node>
        <node concept="NX1gA" id="4NJj1GT20Oy" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Members count" />
          <ref role="NX6Kv" node="4NJj1GT20aS" resolve="membersCount" />
        </node>
        <node concept="NX1gA" id="4NJj1GT20OK" role="NXodf">
          <property role="NX6R2" value="Group Creation Date" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT20bc" resolve="dateCreated" />
        </node>
        <node concept="NX1gA" id="4NJj1GT20P0" role="NXodf">
          <property role="NX6R2" value="Group Creator uid" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT20bA" resolve="creator" />
        </node>
        <node concept="NX1gA" id="4NJj1GT20Pi" role="NXodf">
          <property role="NX6R2" value="Is group public" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT20NO" resolve="isPublic" />
        </node>
      </node>
      <node concept="2m6fVq" id="4NJj1GT1Vft" role="2m5mJr">
        <property role="TrG5h" value="PeerSearch" />
        <node concept="2m7Kf5" id="4NJj1GT1VfH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="query" />
          <node concept="2m5nlk" id="4NJj1GT1VfL" role="2m7DVh">
            <node concept="3BlaRf" id="4NJj1GT1VfR" role="3GJlyp">
              <ref role="3BrLez" node="4NJj1GT1Sc1" resolve="SearchCondition" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1Vfu" role="NuuwV">
          <property role="Nu42W" value="E9" />
        </node>
        <node concept="2m1R6W" id="4NJj1GT1VfU" role="2m6efq">
          <node concept="2m7Kf5" id="4NJj1GT22G1" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="searchResults" />
            <node concept="2m5nlk" id="4NJj1GT22G5" role="2m7DVh">
              <node concept="2m5mGg" id="4NJj1GT22Gb" role="3GJlyp">
                <ref role="2m5mJy" node="4NJj1GT1Zyz" resolve="PeerSearchResult" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="4NJj1GT22Ge" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="4NJj1GT22Gl" role="2m7DVh">
              <node concept="2m5mGg" id="4NJj1GT22Gr" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="4NJj1GT22Gu" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="groups" />
            <node concept="2m5nlk" id="4NJj1GT22GC" role="2m7DVh">
              <node concept="2m5mGg" id="4NJj1GT22GI" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="4NJj1GT1VfV" role="NuuwV">
            <property role="Nu42W" value="EA" />
          </node>
          <node concept="NXeRC" id="4NJj1GT23Wg" role="1y2DgH">
            <property role="NXePf" value="Found peers" />
          </node>
          <node concept="NX1gA" id="4NJj1GT23Wp" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Search Results" />
            <ref role="NX6Kv" node="4NJj1GT22G1" resolve="searchResults" />
          </node>
          <node concept="NX1gA" id="4NJj1GT23Wx" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Related users" />
            <ref role="NX6Kv" node="4NJj1GT22Ge" resolve="users" />
          </node>
          <node concept="NX1gA" id="4NJj1GT23WF" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Related groups" />
            <ref role="NX6Kv" node="4NJj1GT22Gu" resolve="groups" />
          </node>
        </node>
        <node concept="NXeRC" id="4NJj1GT23kr" role="1GBnQ6">
          <property role="NXePf" value="Performing peer search" />
        </node>
        <node concept="NX1gA" id="4NJj1GT23kw" role="1GBnQ6">
          <property role="NX6R2" value="Search query. Warring not all combinations can be processed by server. (acts as OR)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1VfH" resolve="query" />
        </node>
      </node>
      <node concept="2m5naR" id="5qm50Y0fa3d" role="2m5mJr">
        <property role="TrG5h" value="MessageSearchResult" />
        <node concept="2m7Kf5" id="5qm50Y0faHL" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="5qm50Y0faHP" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5qm50Y0faHS" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="rid" />
          <node concept="wb0Ql" id="MlriG8H6D4" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnUB8" resolve="randomId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="MlriG8H6D7" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="MlriG8H6Df" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="MlriG8Hi8J" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="senderId" />
          <node concept="wb0Ql" id="MlriG8Hi8V" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="MlriG8H6Di" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="content" />
          <node concept="3BlaRf" id="MlriG8H6Ds" role="2m7DVh">
            <ref role="3BrLez" node="55bmeIQey3W" resolve="Message" />
          </node>
        </node>
        <node concept="NXeRC" id="MlriG8HiM9" role="NXodf">
          <property role="NXePf" value="Message container" />
        </node>
        <node concept="NX1gA" id="MlriG8HkHM" role="NXodf">
          <property role="NX6R2" value="Message Peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5qm50Y0faHL" resolve="peer" />
        </node>
        <node concept="NX1gA" id="MlriG8HkI0" role="NXodf">
          <property role="NX6R2" value="Message Random Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5qm50Y0faHS" resolve="rid" />
        </node>
        <node concept="NX1gA" id="MlriG8HkIa" role="NXodf">
          <property role="NX6R2" value="Message Date" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="MlriG8H6D7" resolve="date" />
        </node>
        <node concept="NX1gA" id="MlriG8HkIm" role="NXodf">
          <property role="NX6R2" value="Message sender UID" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="MlriG8Hi8J" resolve="senderId" />
        </node>
        <node concept="NX1gA" id="MlriG8HkI$" role="NXodf">
          <property role="NX6R2" value="Message content" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="MlriG8H6Di" resolve="content" />
        </node>
      </node>
      <node concept="2m5naR" id="MlriG8HkKF" role="2m5mJr">
        <property role="TrG5h" value="MessageSearchItem" />
        <node concept="2m7Kf5" id="MlriG8HkMF" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="result" />
          <node concept="2m5mGg" id="MlriG8Hls3" role="2m7DVh">
            <ref role="2m5mJy" node="5qm50Y0fa3d" resolve="MessageSearchResult" />
          </node>
        </node>
        <node concept="NXeRC" id="MlriG8Hm5q" role="NXodf">
          <property role="NXePf" value="Message Search result container" />
        </node>
      </node>
      <node concept="2m62dX" id="64LjbWR$H_z" role="2m5mJr">
        <property role="TrG5h" value="MessageSearchResponse" />
        <node concept="NXeRC" id="64LjbWR$IoF" role="NXp4Y">
          <property role="NXePf" value="Search Result" />
        </node>
        <node concept="NX1gA" id="64LjbWR$IoL" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Search results" />
          <ref role="NX6Kv" node="64LjbWR$HC6" resolve="searchResults" />
        </node>
        <node concept="NX1gA" id="64LjbWR$IoT" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Search users" />
          <ref role="NX6Kv" node="64LjbWR$HCj" resolve="users" />
        </node>
        <node concept="NX1gA" id="64LjbWR$Ip3" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Search groups" />
          <ref role="NX6Kv" node="64LjbWR$HCz" resolve="groups" />
        </node>
        <node concept="NX1gA" id="64LjbWR$Ipf" role="NXp4Y">
          <property role="NX6R2" value="State for loading more results" />
          <ref role="NX6Kv" node="64LjbWR$HCQ" resolve="loadMoreState" />
        </node>
        <node concept="2m7Kf5" id="64LjbWR$HC6" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="searchResults" />
          <node concept="2m5nlk" id="64LjbWR$HCa" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWR$HCg" role="3GJlyp">
              <ref role="2m5mJy" node="MlriG8HkKF" resolve="MessageSearchItem" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWR$HCj" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="64LjbWR$HCq" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWR$HCw" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWR$HCz" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="groups" />
          <node concept="2m5nlk" id="64LjbWR$HCH" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWR$HCN" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWR$HCQ" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="loadMoreState" />
          <node concept="2m5nlT" id="64LjbWR$HD3" role="2m7DVh">
            <node concept="2m61tm" id="64LjbWR$HD9" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="64LjbWR$H_$" role="NuuwV">
          <property role="Nu42W" value="DA" />
        </node>
      </node>
      <node concept="2m6fVq" id="MlriG8Ho3u" role="2m5mJr">
        <property role="TrG5h" value="MessageSearch" />
        <node concept="2m7Kf5" id="MlriG8HpsG" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="query" />
          <node concept="3BlaRf" id="MlriG8HpsS" role="2m7DVh">
            <ref role="3BrLez" node="4NJj1GT1Sc1" resolve="SearchCondition" />
          </node>
        </node>
        <node concept="Nu42z" id="MlriG8Ho3v" role="NuuwV">
          <property role="Nu42W" value="D9" />
        </node>
        <node concept="NXeRC" id="MlriG8Ho9Q" role="1GBnQ6">
          <property role="NXePf" value="Performing message search" />
        </node>
        <node concept="NX1gA" id="MlriG8Hq6y" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Search query" />
          <ref role="NX6Kv" node="MlriG8HpsG" resolve="query" />
        </node>
        <node concept="2m1Rp1" id="64LjbWR$Ipo" role="2m6efq">
          <ref role="2m1o9l" node="64LjbWR$H_z" resolve="MessageSearchResponse" />
        </node>
      </node>
      <node concept="2m6fVq" id="64LjbWR$Hwr" role="2m5mJr">
        <property role="TrG5h" value="MessageSearchMore" />
        <node concept="2m7Kf5" id="64LjbWR$HyW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="loadMoreState" />
          <node concept="2m61tm" id="64LjbWR$Hz0" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="64LjbWR$Hws" role="NuuwV">
          <property role="Nu42W" value="DE" />
        </node>
        <node concept="2m1Rp1" id="64LjbWR$Wgh" role="2m6efq">
          <ref role="2m1o9l" node="64LjbWR$H_z" resolve="MessageSearchResponse" />
        </node>
        <node concept="NXeRC" id="64LjbWR$VwP" role="1GBnQ6">
          <property role="NXePf" value="Performing message search paging" />
        </node>
        <node concept="NX1gA" id="64LjbWR$VwU" role="1GBnQ6">
          <property role="NX6R2" value="State for loading more results" />
          <ref role="NX6Kv" node="64LjbWR$HyW" resolve="loadMoreState" />
        </node>
      </node>
      <node concept="1Dx9M1" id="4NJj1GT1R$P" role="1Dx9rD">
        <property role="1Dx9K7" value="Searching API" />
      </node>
    </node>
    <node concept="2m5mJO" id="3aztRmLKeSG" role="2m5lHt">
      <property role="TrG5h" value="Public Groups" />
      <property role="3XOG$Z" value="pubgroups" />
      <node concept="1Dx9M1" id="3aztRmLKypC" role="1Dx9rD">
        <property role="1Dx9K7" value="Public group is easy way to find communities" />
      </node>
      <node concept="2m5naR" id="3aztRmLKfoA" role="2m5mJr">
        <property role="TrG5h" value="PublicGroup" />
        <node concept="2m7Kf5" id="3aztRmLKfoV" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="3aztRmLKfty" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3aztRmLKft_" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="3aztRmLKftF" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3aztRmLKftI" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="3aztRmLKftQ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3aztRmLKxpu" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="avatar" />
          <node concept="2m5nlT" id="3aztRmLKxpI" role="2m7DVh">
            <node concept="2m5mGg" id="3aztRmLKxpO" role="3GH5xg">
              <ref role="2m5mJy" node="GBscvB$$LB" resolve="Avatar" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="3aztRmLKfW_" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="membersCount" />
          <node concept="2m5ndE" id="3aztRmLKfWJ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3aztRmLKurZ" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="friendsCount" />
          <node concept="2m5ndE" id="3aztRmLKusd" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3aztRmLKfWM" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="description" />
          <node concept="2m5ndX" id="3aztRmLKfWY" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="3aztRmLKxoO" role="NXodf">
          <property role="NXePf" value="Public Group description" />
        </node>
        <node concept="NX1gA" id="3aztRmLKxoT" role="NXodf">
          <property role="NX6R2" value="Group id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3aztRmLKfoV" resolve="id" />
        </node>
        <node concept="NX1gA" id="3aztRmLKxp1" role="NXodf">
          <property role="NX6R2" value="Group Access hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="3aztRmLKft_" resolve="accessHash" />
        </node>
        <node concept="NX1gA" id="3aztRmLKxpb" role="NXodf">
          <property role="NX6R2" value="Group title" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3aztRmLKftI" resolve="title" />
        </node>
        <node concept="NX1gA" id="3aztRmLKxpn" role="NXodf">
          <property role="NX6R2" value="Group avatar" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3aztRmLKxpu" resolve="avatar" />
        </node>
        <node concept="NX1gA" id="3aztRmLKxqa" role="NXodf">
          <property role="NX6R2" value="Members count in group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3aztRmLKfW_" resolve="membersCount" />
        </node>
        <node concept="NX1gA" id="3aztRmLKxqq" role="NXodf">
          <property role="NX6R2" value="Friends count int group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3aztRmLKurZ" resolve="friendsCount" />
        </node>
        <node concept="NX1gA" id="3aztRmLKxqG" role="NXodf">
          <property role="NX6R2" value="Description of group" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="3aztRmLKfWM" resolve="description" />
        </node>
      </node>
      <node concept="2m6fVq" id="3aztRmLKvqB" role="2m5mJr">
        <property role="TrG5h" value="GetPublicGroups" />
        <node concept="NXeRC" id="3aztRmLKvUX" role="1GBnQ6">
          <property role="NXePf" value="Getting public groups" />
        </node>
        <node concept="Nu42z" id="3aztRmLKvqC" role="NuuwV">
          <property role="Nu42W" value="C9" />
        </node>
        <node concept="2m1R6W" id="3aztRmLKvru" role="2m6efq">
          <node concept="NXeRC" id="3aztRmLKwTs" role="1y2DgH">
            <property role="NXePf" value="Loaded public groups" />
          </node>
          <node concept="NX1gA" id="3aztRmLKwTy" role="1y2DgH">
            <property role="NX6R2" value="All available groups" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="3aztRmLKvrz" resolve="groups" />
          </node>
          <node concept="2m7Kf5" id="3aztRmLKvrz" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="groups" />
            <node concept="2m5nlk" id="3aztRmLKvrB" role="2m7DVh">
              <node concept="2m5mGg" id="3aztRmLKvrH" role="3GJlyp">
                <ref role="2m5mJy" node="3aztRmLKfoA" resolve="PublicGroup" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="3aztRmLKvrv" role="NuuwV">
            <property role="Nu42W" value="CA" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="64LjbWRBVJO" role="2m5lHt">
      <property role="TrG5h" value="Invites" />
      <property role="3XOG$Z" value="invites" />
      <node concept="2m5naR" id="64LjbWRBWwa" role="2m5mJr">
        <property role="TrG5h" value="InviteState" />
        <node concept="2m7Kf5" id="64LjbWRBWwk" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="email" />
          <node concept="2m5ndX" id="64LjbWRBWwo" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64LjbWRC1OH" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="name" />
          <node concept="2m5nlT" id="64LjbWRC1OQ" role="2m7DVh">
            <node concept="2m5ndX" id="64LjbWRC1OW" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRBXgx" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="uid" />
          <node concept="2m5nlT" id="64LjbWRBXgB" role="2m7DVh">
            <node concept="2m5ndE" id="64LjbWRBXgH" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRCeMh" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="tid" />
          <node concept="2m5nlT" id="64LjbWRCeMt" role="2m7DVh">
            <node concept="2m5ndE" id="64LjbWRCeMz" role="3GH5xg" />
          </node>
        </node>
        <node concept="NXeRC" id="64LjbWRBY0L" role="NXodf">
          <property role="NXePf" value="Invite state" />
        </node>
        <node concept="NX1gA" id="64LjbWRBY0Q" role="NXodf">
          <property role="NX6R2" value="Email of invite" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRBWwk" resolve="email" />
        </node>
        <node concept="NX1gA" id="64LjbWRCdfP" role="NXodf">
          <property role="NX6R2" value="Name of invited user" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRC1OH" resolve="name" />
        </node>
        <node concept="NX1gA" id="64LjbWRBY0Y" role="NXodf">
          <property role="NX6R2" value="Uid of registered user" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRBXgx" resolve="uid" />
        </node>
        <node concept="NX1gA" id="64LjbWRCeMG" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Team id of invite" />
          <ref role="NX6Kv" node="64LjbWRCeMh" resolve="tid" />
        </node>
      </node>
      <node concept="2m62dX" id="64LjbWRC0iH" role="2m5mJr">
        <property role="TrG5h" value="InviteList" />
        <node concept="2m7Kf5" id="64LjbWRC0ja" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="invites" />
          <node concept="2m5nlk" id="64LjbWRC0je" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWRC0jk" role="3GJlyp">
              <ref role="2m5mJy" node="64LjbWRBWwa" resolve="InviteState" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRCbH1" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="relatedUsers" />
          <node concept="2m5nlk" id="64LjbWRCbH8" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWRCbHe" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRCbHh" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="relatedGroups" />
          <node concept="2m5nlk" id="64LjbWRCbHr" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWRCbHx" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRCbH$" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="relatedTeams" />
          <node concept="2m5nlk" id="64LjbWRCbHL" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWRCbHR" role="3GJlyp">
              <ref role="2m5mJy" node="64LjbWR_0wc" resolve="Team" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="64LjbWRC0iI" role="NuuwV">
          <property role="Nu42W" value="A04" />
        </node>
        <node concept="NXeRC" id="64LjbWRC8Cp" role="NXp4Y">
          <property role="NXePf" value="Intites list" />
        </node>
        <node concept="NX1gA" id="64LjbWRC8Cu" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Intvites" />
          <ref role="NX6Kv" node="64LjbWRC0ja" resolve="invites" />
        </node>
        <node concept="NX1gA" id="64LjbWRCcuB" role="NXp4Y">
          <property role="NX6R2" value="Related users in invites" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRCbH1" resolve="relatedUsers" />
        </node>
        <node concept="NX1gA" id="64LjbWRCcuL" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Related groups in invites" />
          <ref role="NX6Kv" node="64LjbWRCbHh" resolve="relatedGroups" />
        </node>
        <node concept="NX1gA" id="64LjbWRCcuX" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Related teams in invites" />
          <ref role="NX6Kv" node="64LjbWRCbH$" resolve="relatedTeams" />
        </node>
      </node>
      <node concept="2m6fVq" id="64LjbWRBWvo" role="2m5mJr">
        <property role="TrG5h" value="LoadOwnSentInvites" />
        <node concept="Nu42z" id="64LjbWRBWvp" role="NuuwV">
          <property role="Nu42W" value="A03" />
        </node>
        <node concept="NXeRC" id="64LjbWRBYL7" role="1GBnQ6">
          <property role="NXePf" value="Loading current invite states" />
        </node>
        <node concept="2m1Rp1" id="64LjbWRC0jp" role="2m6efq">
          <ref role="2m1o9l" node="64LjbWRC0iH" resolve="InviteList" />
        </node>
      </node>
      <node concept="2m6fVq" id="64LjbWRC0hA" role="2m5mJr">
        <property role="TrG5h" value="SendInvite" />
        <node concept="2m7Kf5" id="64LjbWRC0hZ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="email" />
          <node concept="2m5ndX" id="64LjbWRC0i3" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64LjbWRC2_g" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="name" />
          <node concept="2m5nlT" id="64LjbWRC2_o" role="2m7DVh">
            <node concept="2m5ndX" id="64LjbWRC2_u" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRCpED" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="destTeam" />
          <node concept="2m5nlT" id="64LjbWRCpEP" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWRCpEV" role="3GH5xg">
              <ref role="2m5mJy" node="64LjbWRCab2" resolve="OutTeam" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="64LjbWRC0hB" role="NuuwV">
          <property role="Nu42W" value="A05" />
        </node>
        <node concept="2m1Rp1" id="64LjbWRC4Qs" role="2m6efq">
          <ref role="2m1o9l" node="64LjbWRC0iH" resolve="InviteList" />
        </node>
        <node concept="NXeRC" id="64LjbWRC1OF" role="1GBnQ6">
          <property role="NXePf" value="Sending an email invite" />
        </node>
        <node concept="NX1gA" id="64LjbWRC5AO" role="1GBnQ6">
          <property role="NX6R2" value="Email for invite" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRC0hZ" resolve="email" />
        </node>
        <node concept="NX1gA" id="64LjbWRC6nf" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional name for invite" />
          <ref role="NX6Kv" node="64LjbWRC2_g" resolve="name" />
        </node>
        <node concept="NX1gA" id="64LjbWRCpF3" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional team for invite" />
          <ref role="NX6Kv" node="64LjbWRCpED" resolve="destTeam" />
        </node>
        <node concept="2uC4CA" id="64LjbWRC77C" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="ALREADY_REGISTERED" />
          <property role="2uCiSL" value="If user already registered" />
        </node>
        <node concept="2uC4CA" id="64LjbWRC77E" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="ALREADY_SENT" />
          <property role="2uCiSL" value="If user already invited by CURRENT user" />
        </node>
      </node>
      <node concept="1Dx9M1" id="64LjbWRBWvk" role="1Dx9rD">
        <property role="1Dx9K7" value="Invite mechanizm" />
      </node>
    </node>
    <node concept="2m5mJO" id="64LjbWR$Z1O" role="2m5lHt">
      <property role="TrG5h" value="Teams" />
      <property role="3XOG$Z" value="teams" />
      <node concept="1Dx9M1" id="64LjbWR$ZKP" role="1Dx9rD">
        <property role="1Dx9K7" value="Teams support for Actor" />
      </node>
      <node concept="2m5naR" id="64LjbWR_0wc" role="2m5mJr">
        <property role="TrG5h" value="Team" />
        <node concept="2m7Kf5" id="64LjbWR_0we" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="64LjbWR_0wi" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64LjbWR_0wl" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="64LjbWR_0wr" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64LjbWR_1fU" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="name" />
          <node concept="2m5ndX" id="64LjbWR_1g2" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="64LjbWR_1fS" role="NXodf">
          <property role="NXePf" value="Team entity" />
        </node>
        <node concept="NX1gA" id="64LjbWR_1Z_" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Team Id" />
          <ref role="NX6Kv" node="64LjbWR_0we" resolve="id" />
        </node>
        <node concept="NX1gA" id="64LjbWR_1ZH" role="NXodf">
          <property role="NX6R2" value="Team Access Hash" />
          <property role="1GSvIU" value="danger" />
          <ref role="NX6Kv" node="64LjbWR_0wl" resolve="accessHash" />
        </node>
        <node concept="NX1gA" id="64LjbWR_1ZR" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Team name" />
          <ref role="NX6Kv" node="64LjbWR_1fU" resolve="name" />
        </node>
      </node>
      <node concept="2m5naR" id="64LjbWRCab2" role="2m5mJr">
        <property role="TrG5h" value="OutTeam" />
        <node concept="2m7Kf5" id="64LjbWRCaby" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="64LjbWRCabA" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64LjbWRCabD" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="accessHash" />
          <node concept="2m5ndQ" id="64LjbWRCabJ" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="64LjbWRCaWf" role="NXodf">
          <property role="NXePf" value="Reference to a team" />
        </node>
        <node concept="NX1gA" id="64LjbWRCaWk" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Team Id" />
          <ref role="NX6Kv" node="64LjbWRCaby" resolve="id" />
        </node>
        <node concept="NX1gA" id="64LjbWRCaWs" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Team access hash" />
          <ref role="NX6Kv" node="64LjbWRCabD" resolve="accessHash" />
        </node>
      </node>
      <node concept="2m62dX" id="64LjbWRBSHE" role="2m5mJr">
        <property role="TrG5h" value="TeamsList" />
        <node concept="2m7Kf5" id="64LjbWRBSIb" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="teams" />
          <node concept="2m5nlk" id="64LjbWRBSIf" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWRBSIl" role="3GJlyp">
              <ref role="2m5mJy" node="64LjbWR_0wc" resolve="Team" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="64LjbWRBSHF" role="NuuwV">
          <property role="Nu42W" value="A02" />
        </node>
        <node concept="NXeRC" id="64LjbWRBSIo" role="NXp4Y">
          <property role="NXePf" value="Teams response" />
        </node>
        <node concept="NX1gA" id="64LjbWRBUea" role="NXp4Y">
          <property role="NX6R2" value="Teams list" />
          <ref role="NX6Kv" node="64LjbWRBSIb" resolve="teams" />
        </node>
      </node>
      <node concept="2m6fVq" id="64LjbWR_3w1" role="2m5mJr">
        <property role="TrG5h" value="LoadOwnTeams" />
        <node concept="Nu42z" id="64LjbWR_3w2" role="NuuwV">
          <property role="Nu42W" value="A01" />
        </node>
        <node concept="NXeRC" id="64LjbWR_8Kd" role="1GBnQ6">
          <property role="NXePf" value="Loading own teams" />
        </node>
        <node concept="2m1Rp1" id="64LjbWRBTuj" role="2m6efq">
          <ref role="2m1o9l" node="64LjbWRBSHE" resolve="TeamsList" />
        </node>
      </node>
      <node concept="NpBTk" id="64LjbWR_4gt" role="2m5mJr">
        <property role="TrG5h" value="OwnTeamsChanged" />
        <node concept="2m7Kf5" id="64LjbWR_5Kk" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="ownTeams" />
          <node concept="2m5nlk" id="64LjbWR_5Ko" role="2m7DVh">
            <node concept="2m5mGg" id="64LjbWR_5Ku" role="3GJlyp">
              <ref role="2m5mJy" node="64LjbWR_0wc" resolve="Team" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="64LjbWR_50w" role="NXp_2">
          <property role="NXePf" value="Update about own teams changed" />
        </node>
        <node concept="NX1gA" id="64LjbWR_5Kg" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Updated own teams list" />
          <ref role="NX6Kv" node="64LjbWR_5Kk" resolve="ownTeams" />
        </node>
        <node concept="Nu42z" id="64LjbWR_4gu" role="NuuwV">
          <property role="Nu42W" value="A5" />
        </node>
      </node>
      <node concept="2m6fVq" id="64LjbWRCgm8" role="2m5mJr">
        <property role="TrG5h" value="CreateTeam" />
        <node concept="2m7Kf5" id="64LjbWRCgmP" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="title" />
          <node concept="2m5ndX" id="64LjbWRCgmT" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="64LjbWRCgm9" role="NuuwV">
          <property role="Nu42W" value="A06" />
        </node>
        <node concept="2m1R6W" id="64LjbWRCgmW" role="2m6efq">
          <node concept="2m7Kf5" id="64LjbWRCgn1" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="createdTeam" />
            <node concept="2m5mGg" id="64LjbWRCgn5" role="2m7DVh">
              <ref role="2m5mJy" node="64LjbWR_0wc" resolve="Team" />
            </node>
          </node>
          <node concept="Nu42z" id="64LjbWRCgmX" role="NuuwV">
            <property role="Nu42W" value="A07" />
          </node>
          <node concept="NXeRC" id="64LjbWRCh8a" role="1y2DgH">
            <property role="NXePf" value="Created team response" />
          </node>
          <node concept="NX1gA" id="64LjbWRCh8f" role="1y2DgH">
            <property role="NX6R2" value="Created Team" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="64LjbWRCgn1" resolve="createdTeam" />
          </node>
        </node>
        <node concept="NXeRC" id="64LjbWRCh81" role="1GBnQ6">
          <property role="NXePf" value="Creation of a Team" />
        </node>
        <node concept="NX1gA" id="64LjbWRCh86" role="1GBnQ6">
          <property role="NX6R2" value="Title of a team" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRCgmP" resolve="title" />
        </node>
      </node>
      <node concept="2m6fVq" id="64LjbWRCkgH" role="2m5mJr">
        <property role="TrG5h" value="InviteToTeam" />
        <node concept="2m7Kf5" id="64LjbWRCkhA" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="user" />
          <node concept="2m5mGg" id="64LjbWRCnn7" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64LjbWRCkhQ" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="destTeam" />
          <node concept="2m5mGg" id="64LjbWRCkhY" role="2m7DVh">
            <ref role="2m5mJy" node="64LjbWRCab2" resolve="OutTeam" />
          </node>
        </node>
        <node concept="Nu42z" id="64LjbWRCkgI" role="NuuwV">
          <property role="Nu42W" value="A08" />
        </node>
        <node concept="2m1Rp1" id="64LjbWRCki1" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="64LjbWRCl3a" role="1GBnQ6">
          <property role="NXePf" value="Inviting people to team" />
        </node>
        <node concept="NX1gA" id="64LjbWRCnmG" role="1GBnQ6">
          <property role="NX6R2" value="Dest user" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRCkhA" resolve="user" />
        </node>
        <node concept="NX1gA" id="64LjbWRCnmY" role="1GBnQ6">
          <property role="NX6R2" value="Dest team" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64LjbWRCkhQ" resolve="destTeam" />
        </node>
        <node concept="2uC4CA" id="64LjbWRClOj" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="ALREADY_MEMBER" />
          <property role="2uCiSL" value="User is already member of a group" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="1$yIuJFB7ca" role="2m5lHt">
      <property role="TrG5h" value="Integrations" />
      <property role="3XOG$Z" value="integrations" />
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
      <node concept="2m6fVq" id="4NJj1GT1MF1" role="2m5mJr">
        <property role="TrG5h" value="StopTyping" />
        <node concept="2m7Kf5" id="4NJj1GT1MGd" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="4NJj1GT1MGh" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1MGk" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="typingType" />
          <node concept="3GJkcs" id="4NJj1GT1MGq" role="2m7DVh">
            <ref role="3GJkik" node="4zDDY4ERgsM" resolve="TypingType" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1MF2" role="NuuwV">
          <property role="Nu42W" value="1E" />
        </node>
        <node concept="2m1Rp1" id="4NJj1GT1MGa" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1NiT" role="1GBnQ6">
          <property role="NXePf" value="Stop typing" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1NTr" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Destination peer" />
          <ref role="NX6Kv" node="4NJj1GT1MGd" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1NTz" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="typing type" />
          <ref role="NX6Kv" node="4NJj1GT1MGk" resolve="typingType" />
        </node>
      </node>
      <node concept="2m488m" id="1iu4pgORHO5" role="2m5mJr">
        <property role="TrG5h" value="DeviceType" />
        <node concept="2m7y0F" id="1iu4pgORHO7" role="2m7ymf">
          <property role="TrG5h" value="GENERIC" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="1iu4pgORHPF" role="2m7ymf">
          <property role="TrG5h" value="PC" />
          <property role="2m7y0m" value="2" />
        </node>
        <node concept="2m7y0F" id="1iu4pgORHPI" role="2m7ymf">
          <property role="TrG5h" value="MOBILE" />
          <property role="2m7y0m" value="3" />
        </node>
        <node concept="2m7y0F" id="1iu4pgORHPM" role="2m7ymf">
          <property role="TrG5h" value="TABLET" />
          <property role="2m7y0m" value="4" />
        </node>
        <node concept="2m7y0F" id="1iu4pgORILi" role="2m7ymf">
          <property role="TrG5h" value="WATCH" />
          <property role="2m7y0m" value="5" />
        </node>
        <node concept="2m7y0F" id="1iu4pgORQrg" role="2m7ymf">
          <property role="TrG5h" value="MIRROR" />
          <property role="2m7y0m" value="6" />
        </node>
        <node concept="2m7y0F" id="1iu4pgORQrn" role="2m7ymf">
          <property role="TrG5h" value="CAR" />
          <property role="2m7y0m" value="7" />
        </node>
        <node concept="2m7y0F" id="1iu4pgORQrv" role="2m7ymf">
          <property role="TrG5h" value="TABLE" />
          <property role="2m7y0m" value="8" />
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
          <node concept="wb0Ql" id="6u8NlnzqdYy" role="2m7DVh">
            <ref role="wb18D" node="6u8Nlnzqdrd" resolve="msec" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORHKg" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="deviceType" />
          <node concept="2m5nlT" id="1iu4pgORHKo" role="2m7DVh">
            <node concept="3GJkcs" id="1iu4pgORILo" role="3GH5xg">
              <ref role="3GJkik" node="1iu4pgORHO5" resolve="DeviceType" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORJGR" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="deviceCategory" />
          <node concept="2m5nlT" id="1iu4pgORJH2" role="2m7DVh">
            <node concept="2m5ndX" id="1iu4pgORJH8" role="3GH5xg" />
          </node>
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
          <property role="NX6R2" value="timeout of online state in milliseconds" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="GBscvBBuyA" resolve="timeout" />
        </node>
        <node concept="NX1gA" id="1iu4pgORJHg" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional device type" />
          <ref role="NX6Kv" node="1iu4pgORHKg" resolve="deviceType" />
        </node>
        <node concept="NX1gA" id="1iu4pgORJHs" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional device category, for example android or ios" />
          <ref role="NX6Kv" node="1iu4pgORJGR" resolve="deviceCategory" />
        </node>
      </node>
      <node concept="NpBTk" id="1iu4pgORGHm" role="2m5mJr">
        <property role="TrG5h" value="PauseNotifications" />
        <node concept="2m7Kf5" id="1iu4pgORMAM" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="timeout" />
          <node concept="wb0Ql" id="1iu4pgORMAQ" role="2m7DVh">
            <ref role="wb18D" node="6u8Nlnzqdrq" resolve="sec" />
          </node>
        </node>
        <node concept="Nu42z" id="1iu4pgORGHn" role="NuuwV">
          <property role="Nu42W" value="A6" />
        </node>
        <node concept="NXeRC" id="1iu4pgORGLF" role="NXp_2">
          <property role="NXePf" value="Update about pausing notifications" />
        </node>
        <node concept="NX1gA" id="1iu4pgORNBf" role="NXp_2">
          <property role="NX6R2" value="Timeout for notifications resume" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="1iu4pgORMAM" resolve="timeout" />
        </node>
      </node>
      <node concept="NpBTk" id="1iu4pgORGKd" role="2m5mJr">
        <property role="TrG5h" value="RestoreNotifications" />
        <node concept="Nu42z" id="1iu4pgORGKe" role="NuuwV">
          <property role="Nu42W" value="A7" />
        </node>
        <node concept="NXeRC" id="1iu4pgORGLH" role="NXp_2">
          <property role="NXePf" value="Update about restoring notifications" />
        </node>
      </node>
      <node concept="2m6fVq" id="1iu4pgORM$z" role="2m5mJr">
        <property role="TrG5h" value="PauseNotifications" />
        <node concept="2m7Kf5" id="1iu4pgORMAC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="timeout" />
          <node concept="wb0Ql" id="1iu4pgORMAG" role="2m7DVh">
            <ref role="wb18D" node="6u8Nlnzqdrq" resolve="sec" />
          </node>
        </node>
        <node concept="Nu42z" id="1iu4pgORM$$" role="NuuwV">
          <property role="Nu42W" value="A51" />
        </node>
        <node concept="2m1Rp1" id="1iu4pgORMAJ" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="1iu4pgORPvf" role="1GBnQ6">
          <property role="NXePf" value="Pause notifications" />
        </node>
        <node concept="NX1gA" id="1iu4pgORRnJ" role="1GBnQ6">
          <property role="NX6R2" value="Timeout of pause" />
          <ref role="NX6Kv" node="1iu4pgORMAC" resolve="timeout" />
        </node>
      </node>
      <node concept="2m6fVq" id="1iu4pgORN$T" role="2m5mJr">
        <property role="TrG5h" value="RestoreNotifications" />
        <node concept="Nu42z" id="1iu4pgORN$U" role="NuuwV">
          <property role="Nu42W" value="A52" />
        </node>
        <node concept="2m1Rp1" id="1iu4pgORNB9" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="1iu4pgORRnN" role="1GBnQ6">
          <property role="NXePf" value="Restoring notifications" />
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
          <node concept="wb0Ql" id="5qm50Y0eSAC" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
      <node concept="NpBTk" id="4NJj1GT1Oxk" role="2m5mJr">
        <property role="TrG5h" value="TypingStop" />
        <node concept="2m7Kf5" id="4NJj1GT1Oy$" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="4NJj1GT1OyC" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1P9y" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5qm50Y0eSAG" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1P9F" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="typingType" />
          <node concept="3GJkcs" id="4NJj1GT1P9N" role="2m7DVh">
            <ref role="3GJkik" node="4zDDY4ERgsM" resolve="TypingType" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1Oxl" role="NuuwV">
          <property role="Nu42W" value="51" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1OyF" role="NXp_2">
          <property role="NXePf" value="Update about user's typing stop" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1OyK" role="NXp_2">
          <property role="NX6R2" value="Conversation peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1Oy$" resolve="peer" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1P9t" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's id" />
          <ref role="NX6Kv" node="4NJj1GT1P9y" resolve="uid" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1P9V" role="NXp_2">
          <property role="NX6R2" value="Type of typing" />
          <ref role="NX6Kv" node="4NJj1GT1P9F" resolve="typingType" />
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
          <node concept="wb0Ql" id="5qm50Y0eSAJ" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORJHz" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="deviceType" />
          <node concept="2m5nlT" id="1iu4pgORJHD" role="2m7DVh">
            <node concept="3GJkcs" id="1iu4pgORJHJ" role="3GH5xg">
              <ref role="3GJkik" node="1iu4pgORHO5" resolve="DeviceType" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORL_T" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="deviceCategory" />
          <node concept="2m5nlT" id="1iu4pgORLA2" role="2m7DVh">
            <node concept="2m5ndX" id="1iu4pgORLA8" role="3GH5xg" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eSAM" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORJHM" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="deviceType" />
          <node concept="2m5nlT" id="1iu4pgORJHS" role="2m7DVh">
            <node concept="3GJkcs" id="1iu4pgORJHY" role="3GH5xg">
              <ref role="3GJkik" node="1iu4pgORHO5" resolve="DeviceType" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORLAb" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="deviceCategory" />
          <node concept="2m5nlT" id="1iu4pgORLAk" role="2m7DVh">
            <node concept="2m5ndX" id="1iu4pgORLAq" role="3GH5xg" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eSAP" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="GBscvBBy0M" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="2vxDjotoh82" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORKDW" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="deviceType" />
          <node concept="2m5nlT" id="1iu4pgORKDX" role="2m7DVh">
            <node concept="3GJkcs" id="1iu4pgORKDY" role="3GH5xg">
              <ref role="3GJkik" node="1iu4pgORHO5" resolve="DeviceType" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="1iu4pgORLAt" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="deviceCategory" />
          <node concept="2m5nlT" id="1iu4pgORLAC" role="2m7DVh">
            <node concept="2m5ndX" id="1iu4pgORLAI" role="3GH5xg" />
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
          <node concept="wb0Ql" id="5qm50Y0eSAS" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
      <node concept="2m5naR" id="64HNz1IoSEt" role="2m5mJr">
        <property role="TrG5h" value="ImageLocation" />
        <property role="tsOgz" value="true" />
        <node concept="2m7Kf5" id="64HNz1IoSEu" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="fileLocation" />
          <node concept="2m5mGg" id="64HNz1IoSEv" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
          </node>
        </node>
        <node concept="2m7Kf5" id="64HNz1IoSEw" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="width" />
          <node concept="2m5ndE" id="64HNz1IoSEx" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64HNz1IoSEy" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="height" />
          <node concept="2m5ndE" id="64HNz1IoSEz" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="64HNz1IoSE$" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="fileSize" />
          <node concept="2m5ndE" id="64HNz1IoSE_" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="64HNz1IoSEA" role="NXodf">
          <property role="NXePf" value="Image location" />
        </node>
        <node concept="NX1gA" id="64HNz1IoSEB" role="NXodf">
          <property role="NX6R2" value="Location of file" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoSEu" resolve="fileLocation" />
        </node>
        <node concept="NX1gA" id="64HNz1IoSEC" role="NXodf">
          <property role="NX6R2" value="Width of avatar image" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoSEw" resolve="width" />
        </node>
        <node concept="NX1gA" id="64HNz1IoSED" role="NXodf">
          <property role="NX6R2" value="Height of avatar image" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoSEy" resolve="height" />
        </node>
        <node concept="NX1gA" id="64HNz1IoSEE" role="NXodf">
          <property role="NX6R2" value="Size of file" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="64HNz1IoSE$" resolve="fileSize" />
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
      <node concept="2m488m" id="64HNz1Ip1S_" role="2m5mJr">
        <property role="TrG5h" value="Colors" />
        <node concept="2m7y0F" id="64HNz1Ip1SB" role="2m7ymf">
          <property role="TrG5h" value="red" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="64HNz1Ip3Yu" role="2m7ymf">
          <property role="TrG5h" value="yellow" />
          <property role="2m7y0m" value="2" />
        </node>
        <node concept="2m7y0F" id="64HNz1Ip3Yx" role="2m7ymf">
          <property role="TrG5h" value="green" />
          <property role="2m7y0m" value="3" />
        </node>
      </node>
      <node concept="w93zz" id="64HNz1IoYTS" role="2m5mJr">
        <property role="1FaRnq" value="true" />
        <property role="TrG5h" value="Color" />
      </node>
      <node concept="2m5naR" id="64HNz1IoZDn" role="2m5mJr">
        <property role="TrG5h" value="RgbColor" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="64HNz1IoYTS" resolve="Color" />
        <node concept="NXeRC" id="64HNz1Ip0n7" role="NXodf">
          <property role="NXePf" value="RGB Color" />
        </node>
        <node concept="NX1gA" id="64HNz1Ip2AI" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="RGB color value" />
          <ref role="NX6Kv" node="64HNz1IoZFg" resolve="rgb" />
        </node>
        <node concept="2m7Kf5" id="64HNz1IoZFg" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="rgb" />
          <node concept="2m5ndE" id="64HNz1IoZFk" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2EyE8f8B8Jz" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
      </node>
      <node concept="2m5naR" id="64HNz1Ip14P" role="2m5mJr">
        <property role="TrG5h" value="PredefinedColor" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="64HNz1IoYTS" resolve="Color" />
        <node concept="2m7Kf5" id="64HNz1Ip1UB" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="color" />
          <node concept="3GJkcs" id="64HNz1Ip1UF" role="2m7DVh">
            <ref role="3GJkik" node="64HNz1Ip1S_" resolve="Colors" />
          </node>
        </node>
        <node concept="NXeRC" id="64HNz1Ip16M" role="NXodf">
          <property role="NXePf" value="Predefined color" />
        </node>
        <node concept="NX1gA" id="64HNz1Ip4Ew" role="NXodf">
          <property role="NX6R2" value="Predefined color value" />
          <ref role="NX6Kv" node="64HNz1Ip1UB" resolve="color" />
        </node>
        <node concept="Nu42z" id="2EyE8f8B8NV" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
      </node>
      <node concept="NvyAe" id="GBscvBBiZs" role="2m5mJr" />
      <node concept="2m5naR" id="1B$5xp_zl9$" role="2m5mJr">
        <property role="TrG5h" value="HTTPHeader" />
        <node concept="2m7Kf5" id="1B$5xp_zlcl" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="key" />
          <node concept="2m5ndX" id="1B$5xp_zlcp" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="1B$5xp_zlcs" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="value" />
          <node concept="2m5ndX" id="1B$5xp_zlcy" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="1B$5xp_zox_" role="NXodf">
          <property role="NXePf" value="HTTP Header record" />
        </node>
        <node concept="NX1gA" id="1B$5xp_zoxE" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="HTTP Header name" />
          <ref role="NX6Kv" node="1B$5xp_zlcl" resolve="key" />
        </node>
        <node concept="NX1gA" id="1B$5xp_zoxM" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="HTTP Header value" />
          <ref role="NX6Kv" node="1B$5xp_zlcs" resolve="value" />
        </node>
      </node>
      <node concept="2m5naR" id="6Fl2chwC7Lu" role="2m5mJr">
        <property role="TrG5h" value="FileUrlDescription" />
        <node concept="2m7Kf5" id="6Fl2chwC8Bv" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="fileId" />
          <node concept="2m5ndQ" id="4PMjvAhaUPo" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwC7NJ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="url" />
          <node concept="2m5ndX" id="6Fl2chwC7NN" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwC7NQ" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="timeout" />
          <node concept="wb0Ql" id="6Fl2chwC7NW" role="2m7DVh">
            <ref role="wb18D" node="6u8Nlnzqdrq" resolve="sec" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1B$5xp_zi_E" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="unsignedUrl" />
          <node concept="2m5nlT" id="1B$5xp_zjr_" role="2m7DVh">
            <node concept="2m5ndX" id="1B$5xp_zjrF" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1B$5xp_zm1P" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="unsignedUrlHeaders" />
          <node concept="2m5nlk" id="1B$5xp_zm22" role="2m7DVh">
            <node concept="2m5mGg" id="1B$5xp_zm28" role="3GJlyp">
              <ref role="2m5mJy" node="1B$5xp_zl9$" resolve="HTTPHeader" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="6Fl2chwC7NZ" role="NXodf">
          <property role="NXePf" value="File url description" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC9qZ" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="File id of url" />
          <ref role="NX6Kv" node="6Fl2chwC8Bv" resolve="fileId" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC7O4" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Url for downloading" />
          <ref role="NX6Kv" node="6Fl2chwC7NJ" resolve="url" />
        </node>
        <node concept="NX1gA" id="6Fl2chwC7Oc" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Timeout of url" />
          <ref role="NX6Kv" node="6Fl2chwC7NQ" resolve="timeout" />
        </node>
        <node concept="NX1gA" id="1B$5xp_zjsa" role="NXodf">
          <property role="NX6R2" value="Unsigned URL (used to honor web caches)" />
          <ref role="NX6Kv" node="1B$5xp_zi_E" resolve="unsignedUrl" />
        </node>
        <node concept="NX1gA" id="1B$5xp_zmRa" role="NXodf">
          <property role="NX6R2" value="Headers that is required to download files with unsigned url" />
          <ref role="NX6Kv" node="1B$5xp_zm1P" resolve="unsignedUrlHeaders" />
        </node>
      </node>
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
          <node concept="NX1gA" id="1B$5xp_znGq" role="1y2DgH">
            <property role="NX6R2" value="Unsigned URL (used to honor web caches)" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="1B$5xp_zjsh" resolve="unsignedUrl" />
          </node>
          <node concept="NX1gA" id="1B$5xp_znG$" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Headers that is required to download files with unsigned url" />
            <ref role="NX6Kv" node="1B$5xp_zkhh" resolve="unsignedUrlHeaders" />
          </node>
          <node concept="2m7Kf5" id="3MpuFr6x68s" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="url" />
            <node concept="2m5ndX" id="3MpuFr6x68w" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="3MpuFr6x68z" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="timeout" />
            <node concept="wb0Ql" id="6u8Nlnzqexu" role="2m7DVh">
              <ref role="wb18D" node="6u8Nlnzqdrq" resolve="sec" />
            </node>
          </node>
          <node concept="2m7Kf5" id="1B$5xp_zjsh" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="unsignedUrl" />
            <node concept="2m5nlT" id="1B$5xp_zkh8" role="2m7DVh">
              <node concept="2m5ndX" id="1B$5xp_zkhe" role="3GH5xg" />
            </node>
          </node>
          <node concept="2m7Kf5" id="1B$5xp_zkhh" role="2m0hLx">
            <property role="2m7DUN" value="4" />
            <property role="TrG5h" value="unsignedUrlHeaders" />
            <node concept="2m5nlk" id="1B$5xp_znGh" role="2m7DVh">
              <node concept="2m5mGg" id="1B$5xp_znGn" role="3GJlyp">
                <ref role="2m5mJy" node="1B$5xp_zl9$" resolve="HTTPHeader" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="3MpuFr6x68o" role="NuuwV">
            <property role="Nu42W" value="4E" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="6Fl2chwC6Tz" role="2m5mJr">
        <property role="TrG5h" value="GetFileUrls" />
        <node concept="2m7Kf5" id="6Fl2chwC6VL" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="files" />
          <node concept="2m5nlk" id="6Fl2chwC6VP" role="2m7DVh">
            <node concept="2m5mGg" id="6Fl2chwC6VV" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="6Fl2chwC6T$" role="NuuwV">
          <property role="Nu42W" value="A0D" />
        </node>
        <node concept="2m1R6W" id="6Fl2chwC7J5" role="2m6efq">
          <node concept="NX1gA" id="6Fl2chwCds0" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="File urls" />
            <ref role="NX6Kv" node="6Fl2chwCb1L" resolve="fileUrls" />
          </node>
          <node concept="2m7Kf5" id="6Fl2chwCb1L" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="fileUrls" />
            <node concept="2m5nlk" id="6Fl2chwCefr" role="2m7DVh">
              <node concept="2m5mGg" id="6Fl2chwCefx" role="3GJlyp">
                <ref role="2m5mJy" node="6Fl2chwC7Lu" resolve="FileUrlDescription" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="6Fl2chwC7J6" role="NuuwV">
            <property role="Nu42W" value="A0E" />
          </node>
        </node>
        <node concept="NXeRC" id="6Fl2chwC6VY" role="1GBnQ6">
          <property role="NXePf" value="Requesting multiple fle URL for downloading" />
        </node>
        <node concept="NX1gA" id="6Fl2chwCaep" role="1GBnQ6">
          <property role="NX6R2" value="File locations to load urls" />
          <ref role="NX6Kv" node="6Fl2chwC6VL" resolve="files" />
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
    <node concept="2m5mJO" id="6tgpW9bx_JP" role="2m5lHt">
      <property role="TrG5h" value="Features" />
      <property role="3XOG$Z" value="features" />
      <node concept="2m6fVq" id="6tgpW9bxBsP" role="2m5mJr">
        <property role="TrG5h" value="EnableFeature" />
        <node concept="2m7Kf5" id="6tgpW9bxBsX" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="featureName" />
          <node concept="2m5ndX" id="6tgpW9bxBt1" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6tgpW9bxBty" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="args" />
          <node concept="2m5nlT" id="6tgpW9bxBtF" role="2m7DVh">
            <node concept="2m61tm" id="6tgpW9bxBtL" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="6tgpW9bxBsQ" role="NuuwV">
          <property role="Nu42W" value="A1C" />
        </node>
        <node concept="2m1Rp1" id="6tgpW9bxBt4" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxCkG" role="1GBnQ6">
          <property role="NXePf" value="Enabling feature on the device" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxCkL" role="1GBnQ6">
          <property role="NX6R2" value="Feature name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6tgpW9bxBsX" resolve="featureName" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxCkT" role="1GBnQ6">
          <property role="1GSvIU" value="danger" />
          <property role="NX6R2" value="Optional arguments to a feature" />
          <ref role="NX6Kv" node="6tgpW9bxBty" resolve="args" />
        </node>
      </node>
      <node concept="2m6fVq" id="6tgpW9bxBte" role="2m5mJr">
        <property role="TrG5h" value="DisableFeature" />
        <node concept="2m7Kf5" id="6tgpW9bxBtr" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="featureName" />
          <node concept="2m5ndX" id="6tgpW9bxBtv" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6tgpW9bxBtf" role="NuuwV">
          <property role="Nu42W" value="A1D" />
        </node>
        <node concept="2m1Rp1" id="6tgpW9bxBtO" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxDbQ" role="1GBnQ6">
          <property role="NXePf" value="Disabling feature on the device" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxDbV" role="1GBnQ6">
          <property role="NX6R2" value="Feature name" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6tgpW9bxBtr" resolve="featureName" />
        </node>
      </node>
      <node concept="2m6fVq" id="6tgpW9bxE3o" role="2m5mJr">
        <property role="TrG5h" value="CheckFeatureEnabled" />
        <node concept="2m7Kf5" id="6tgpW9bxE3M" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="userOutPeer" />
          <node concept="2m5mGg" id="6tgpW9bxE3T" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6tgpW9bxE3W" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="featureName" />
          <node concept="2m5ndX" id="6tgpW9bxE42" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6tgpW9bxE3p" role="NuuwV">
          <property role="Nu42W" value="A1E" />
        </node>
        <node concept="2m1Rp1" id="6tgpW9bxE3Q" role="2m6efq">
          <ref role="2m1o9l" node="6WYZhOUYtcE" resolve="Bool" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxE45" role="1GBnQ6">
          <property role="NXePf" value="Method for checking if feature is available to user" />
        </node>
      </node>
      <node concept="1Dx9M1" id="6tgpW9bxAA9" role="1Dx9rD">
        <property role="1Dx9K7" value="Enable feature discovery. Currently available features:" />
      </node>
      <node concept="1Dx9M1" id="6tgpW9by68p" role="1Dx9rD">
        <property role="1Dx9K7" value="* &quot;call&quot; - private audio calls" />
      </node>
    </node>
    <node concept="2m5mJO" id="6tgpW9bxo4d" role="2m5lHt">
      <property role="TrG5h" value="WebRTC" />
      <property role="3XOG$Z" value="webrtc" />
      <node concept="2m6fVq" id="6tgpW9bxsiR" role="2m5mJr">
        <property role="TrG5h" value="SubscribeToCalls" />
        <node concept="Nu42z" id="6tgpW9bxsiS" role="NuuwV">
          <property role="Nu42W" value="A20" />
        </node>
        <node concept="2m1Rp1" id="6tgpW9bxsiZ" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxz9M" role="1GBnQ6">
          <property role="NXePf" value="Enabling subscription to calls (on NewSession need to re-subscribing)" />
        </node>
      </node>
      <node concept="2m6fVq" id="6tgpW9bxsj7" role="2m5mJr">
        <property role="TrG5h" value="UnsubscribeToCalls" />
        <node concept="Nu42z" id="6tgpW9bxsj8" role="NuuwV">
          <property role="Nu42W" value="A21" />
        </node>
        <node concept="2m1Rp1" id="6tgpW9bxsji" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="6tgpW9bx$0p" role="1GBnQ6">
          <property role="NXePf" value="Disabling subscription to calls" />
        </node>
      </node>
      <node concept="NpBTk" id="6tgpW9bxsjt" role="2m5mJr">
        <property role="TrG5h" value="IncomingCall" />
        <node concept="2m7Kf5" id="6tgpW9bxt9W" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndQ" id="6tgpW9bxta0" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6tgpW9bxta3" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="6tgpW9by2EK" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="Nu42z" id="6tgpW9bxsju" role="NuuwV">
          <property role="Nu42W" value="34" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxsjC" role="NXp_2">
          <property role="NXePf" value="Update about incoming call" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxu0_" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Call Id" />
          <ref role="NX6Kv" node="6tgpW9bxt9W" resolve="callId" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxu0H" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's id" />
          <ref role="NX6Kv" node="6tgpW9bxta3" resolve="uid" />
        </node>
      </node>
      <node concept="NpBTk" id="6tgpW9bxuRr" role="2m5mJr">
        <property role="TrG5h" value="CallEnded" />
        <node concept="2m7Kf5" id="6tgpW9bxuRJ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndQ" id="6tgpW9bxuRN" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6tgpW9bxuRs" role="NuuwV">
          <property role="Nu42W" value="35" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxuRQ" role="NXp_2">
          <property role="NXePf" value="Update about call end" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxz9I" role="NXp_2">
          <property role="NX6R2" value="Call Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6tgpW9bxuRJ" resolve="callId" />
        </node>
      </node>
      <node concept="NpBTk" id="6tgpW9bxK5O" role="2m5mJr">
        <property role="TrG5h" value="CallSignal" />
        <node concept="2m7Kf5" id="6tgpW9bxKXq" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndQ" id="6tgpW9bxKXu" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6tgpW9bxKXx" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="content" />
          <node concept="2m61tm" id="6tgpW9bxKXB" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6tgpW9bxK5P" role="NuuwV">
          <property role="Nu42W" value="38" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxOAe" role="NXp_2">
          <property role="NXePf" value="WebRTC signaling package" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxOAj" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Call id" />
          <ref role="NX6Kv" node="6tgpW9bxKXq" resolve="callId" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxOAr" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Signaling raw package" />
          <ref role="NX6Kv" node="6tgpW9bxKXx" resolve="content" />
        </node>
      </node>
      <node concept="NpBTk" id="6tgpW9bxNHu" role="2m5mJr">
        <property role="TrG5h" value="CallInProgress" />
        <node concept="2m7Kf5" id="6tgpW9bxNIn" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndQ" id="6tgpW9bxNIr" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6tgpW9bxNIA" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="timeout" />
          <node concept="wb0Ql" id="6tgpW9bxNIG" role="2m7DVh">
            <ref role="wb18D" node="6u8Nlnzqdrq" resolve="sec" />
          </node>
        </node>
        <node concept="Nu42z" id="6tgpW9bxNHv" role="NuuwV">
          <property role="Nu42W" value="39" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxOAw" role="NXp_2">
          <property role="NXePf" value="Update about call in progress. If doesn't get next update in specified timeout - end call." />
        </node>
        <node concept="NX1gA" id="6tgpW9bxOA_" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Call Id" />
          <ref role="NX6Kv" node="6tgpW9bxNIn" resolve="callId" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxOAH" role="NXp_2">
          <property role="NX6R2" value="Update timeout" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6tgpW9bxNIA" resolve="timeout" />
        </node>
      </node>
      <node concept="2m6fVq" id="201xLeQvfQO" role="2m5mJr">
        <property role="TrG5h" value="DoCall" />
        <node concept="2m7Kf5" id="201xLeQvfRW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="peer" />
          <node concept="2m5mGg" id="201xLeQvfS5" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6hj" resolve="OutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="201xLeQvh_H" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="timeout" />
          <node concept="wb0Ql" id="201xLeQvh_N" role="2m7DVh">
            <ref role="wb18D" node="6u8Nlnzqdrq" resolve="sec" />
          </node>
        </node>
        <node concept="Nu42z" id="201xLeQvfQP" role="NuuwV">
          <property role="Nu42W" value="A25" />
        </node>
        <node concept="2m1R6W" id="201xLeQvfS8" role="2m6efq">
          <node concept="2m7Kf5" id="201xLeQvfSd" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="callId" />
            <node concept="2m5ndQ" id="201xLeQvfSh" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="201xLeQvfS9" role="NuuwV">
            <property role="Nu42W" value="A26" />
          </node>
          <node concept="NX1gA" id="201xLeQvgJ5" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Call Id" />
            <ref role="NX6Kv" node="201xLeQvfSd" resolve="callId" />
          </node>
        </node>
        <node concept="NXeRC" id="201xLeQvfSk" role="1GBnQ6">
          <property role="NXePf" value="Do Call. Right after a call client need to start sending CallInProgress" />
        </node>
        <node concept="NX1gA" id="201xLeQvfSp" role="1GBnQ6">
          <property role="NX6R2" value="destination peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="201xLeQvfRW" resolve="peer" />
        </node>
        <node concept="NX1gA" id="201xLeQvisy" role="1GBnQ6">
          <property role="NX6R2" value="Call timeout" />
          <ref role="NX6Kv" node="201xLeQvh_H" resolve="timeout" />
        </node>
        <node concept="2uC4CA" id="201xLeQvjjg" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="CALL_ENDED" />
          <property role="2uCiSL" value="Throws when call is already ended" />
        </node>
      </node>
      <node concept="2m6fVq" id="6tgpW9bxLVc" role="2m5mJr">
        <property role="TrG5h" value="EndCall" />
        <node concept="2m7Kf5" id="6tgpW9bxLVW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndQ" id="6tgpW9bxLW0" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6tgpW9bxLVd" role="NuuwV">
          <property role="Nu42W" value="A23" />
        </node>
        <node concept="2m1Rp1" id="6tgpW9bxLW3" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxOAV" role="1GBnQ6">
          <property role="NXePf" value="Ending Call" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxOB0" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Call Id" />
          <ref role="NX6Kv" node="6tgpW9bxLVW" resolve="callId" />
        </node>
        <node concept="2uC4CA" id="6tgpW9bxPuJ" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="CALL_NOT_STARTED" />
          <property role="2uCiSL" value="Throws if call not started" />
        </node>
        <node concept="2uC4CA" id="6tgpW9bxPuL" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="CALL_ENDED" />
          <property role="2uCiSL" value="Throsw if call already ended" />
        </node>
      </node>
      <node concept="2m6fVq" id="6tgpW9bxMO7" role="2m5mJr">
        <property role="TrG5h" value="CallInProgress" />
        <node concept="2uC4CA" id="6tgpW9by3yv" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="CALL_ENDED" />
          <property role="2uCiSL" value="Throws when call is already ended" />
        </node>
        <node concept="2m7Kf5" id="6tgpW9bxMOZ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndQ" id="6tgpW9bxMP3" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6tgpW9bxMP6" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="timeout" />
          <node concept="wb0Ql" id="6tgpW9bxMPc" role="2m7DVh">
            <ref role="wb18D" node="6u8Nlnzqdrq" resolve="sec" />
          </node>
        </node>
        <node concept="Nu42z" id="6tgpW9bxMO8" role="NuuwV">
          <property role="Nu42W" value="A24" />
        </node>
        <node concept="2m1Rp1" id="6tgpW9bxMOW" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="6tgpW9bxQmw" role="1GBnQ6">
          <property role="NXePf" value="Notifying about call. Need to be continiously sent to server with timeout/2. Sent as initial call answering." />
        </node>
        <node concept="NX1gA" id="6tgpW9bxQmD" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Call Id" />
          <ref role="NX6Kv" node="6tgpW9bxMOZ" resolve="callId" />
        </node>
        <node concept="NX1gA" id="6tgpW9bxQmL" role="1GBnQ6">
          <property role="NX6R2" value="Timeout of sec" />
          <ref role="NX6Kv" node="6tgpW9bxMP6" resolve="timeout" />
        </node>
      </node>
      <node concept="2m6fVq" id="201xLeQvo8l" role="2m5mJr">
        <property role="TrG5h" value="SendCallSignal" />
        <node concept="NXeRC" id="201xLeQvo9Y" role="1GBnQ6">
          <property role="NXePf" value="Sending call signal message" />
        </node>
        <node concept="NX1gA" id="201xLeQvoa4" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Call Id" />
          <ref role="NX6Kv" node="201xLeQvo9G" resolve="callId" />
        </node>
        <node concept="NX1gA" id="201xLeQvoac" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Signaling raw package" />
          <ref role="NX6Kv" node="201xLeQvo9N" resolve="content" />
        </node>
        <node concept="2m7Kf5" id="201xLeQvo9G" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="callId" />
          <node concept="2m5ndQ" id="201xLeQvo9K" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="201xLeQvo9N" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="content" />
          <node concept="2m61tm" id="201xLeQvo9T" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="201xLeQvo8m" role="NuuwV">
          <property role="Nu42W" value="A27" />
        </node>
        <node concept="2m1Rp1" id="201xLeQvo9D" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="2uC4CA" id="201xLeQvp5r" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="CALL_ENDED" />
          <property role="2uCiSL" value="Throws when call is already ended" />
        </node>
        <node concept="2uC4CA" id="201xLeQvp5t" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="CALL_NOT_STARTED" />
          <property role="2uCiSL" value="Throws when call is not started" />
        </node>
      </node>
      <node concept="1Dx9M1" id="6tgpW9bxpK8" role="1Dx9rD">
        <property role="1Dx9K7" value="WebRTC package that enables support to audio and video calls" />
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
          <node concept="2m5nlT" id="6Fl2chwCgDC" role="2m7DVh">
            <node concept="2m5ndX" id="6Fl2chwCgDI" role="3GH5xg" />
          </node>
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
    <node concept="2m5mJO" id="6u8NlnzqpWE" role="2m5lHt">
      <property role="TrG5h" value="Stats" />
      <property role="3XOG$Z" value="stats" />
      <node concept="2m5naR" id="6u8NlnzqUWi" role="2m5mJr">
        <property role="TrG5h" value="EventRecord" />
        <node concept="2m7Kf5" id="6u8NlnzqUWV" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="date" />
          <node concept="wb0Ql" id="6u8NlnzqUWZ" role="2m7DVh">
            <ref role="wb18D" node="2vxDjotnO8T" resolve="date" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6u8NlnzqUX2" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="event" />
          <node concept="3BlaRf" id="6u8NlnzqVxj" role="2m7DVh">
            <ref role="3BrLez" node="6u8NlnzqrBg" resolve="Event" />
          </node>
        </node>
        <node concept="NXeRC" id="6u8NlnzqW5x" role="NXodf">
          <property role="NXePf" value="Record for stored event" />
        </node>
      </node>
      <node concept="1Dx9M1" id="6u8NlnzqqvZ" role="1Dx9rD">
        <property role="1Dx9K7" value="Saving statistics information" />
      </node>
      <node concept="w93zz" id="6u8NlnzqrBg" role="2m5mJr">
        <property role="TrG5h" value="Event" />
        <property role="1FaRnq" value="true" />
      </node>
      <node concept="2m5naR" id="6u8NlnzqxKR" role="2m5mJr">
        <property role="TrG5h" value="UntypedEvent" />
        <property role="w4tQU" value="true" />
        <property role="tsOgz" value="true" />
        <ref role="w4$XZ" node="6u8NlnzqrBg" resolve="Event" />
        <node concept="2m7Kf5" id="6u8NlnzqxL7" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="eventType" />
          <node concept="2m5ndX" id="6u8NlnzqxLb" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8NlnzqxLe" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="params" />
          <node concept="2m5nlT" id="6u8NlnzqxLk" role="2m7DVh">
            <node concept="3BlaRf" id="6u8NlnzqxLq" role="3GH5xg">
              <ref role="3BrLez" node="2WAO9Y$lyRS" resolve="RawValue" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="6u8NlnzqxL5" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
        <node concept="NXeRC" id="6u8NlnzqxLt" role="NXodf">
          <property role="NXePf" value="Untyped event" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzqyll" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Event type" />
          <ref role="NX6Kv" node="6u8NlnzqxL7" resolve="eventType" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzqylt" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="optional params for event" />
          <ref role="NX6Kv" node="6u8NlnzqxLe" resolve="params" />
        </node>
      </node>
      <node concept="2m5naR" id="6u8NlnzqsaQ" role="2m5mJr">
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <property role="TrG5h" value="ContentViewChanged" />
        <ref role="w4$XZ" node="6u8NlnzqrBg" resolve="Event" />
        <node concept="2m7Kf5" id="6u8NlnzqtPF" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="contentType" />
          <node concept="2m5ndX" id="6u8NlnzqtPJ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8NlnzqYmz" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="contentId" />
          <node concept="2m5ndX" id="6u8NlnzqYmK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8NlnzqSCs" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="visible" />
          <node concept="2m5ndN" id="6u8NlnzqSC_" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8NlnzquX9" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="params" />
          <node concept="2m5nlT" id="6u8NlnzquXm" role="2m7DVh">
            <node concept="3BlaRf" id="6u8NlnzquXs" role="3GH5xg">
              <ref role="3BrLez" node="2WAO9Y$lyRS" resolve="RawValue" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="6u8Nlnzqti4" role="NXodf">
          <property role="NXePf" value="Content view event" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzqvxd" role="NXodf">
          <property role="NX6R2" value="unique content id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6u8NlnzqtPF" resolve="contentType" />
        </node>
        <node concept="NX1gA" id="6u8NlnzqTdh" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Is content visible" />
          <ref role="NX6Kv" node="6u8NlnzqSCs" resolve="visible" />
        </node>
        <node concept="NX1gA" id="6u8NlnzqxcU" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="optional params for content view" />
          <ref role="NX6Kv" node="6u8NlnzquX9" resolve="params" />
        </node>
        <node concept="Nu42z" id="6u8NlnzqsaU" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
      </node>
      <node concept="2m5naR" id="6u8NlnzqRu4" role="2m5mJr">
        <property role="TrG5h" value="AppVisibleChanged" />
        <property role="tsOgz" value="true" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="6u8NlnzqrBg" resolve="Event" />
        <node concept="2m7Kf5" id="6u8NlnzqTLq" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="visible" />
          <node concept="2m5ndN" id="6u8NlnzqTLu" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6u8NlnzqRuI" role="3BtCOu">
          <property role="Nu42W" value="04" />
        </node>
        <node concept="NXeRC" id="6u8NlnzqRuK" role="NXodf">
          <property role="NXePf" value="On App Visible event" />
        </node>
        <node concept="NX1gA" id="6u8NlnzqUlD" role="NXodf">
          <property role="NX6R2" value="Is app visible" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6u8NlnzqTLq" resolve="visible" />
        </node>
      </node>
      <node concept="2m6fVq" id="6u8NlnzqyTI" role="2m5mJr">
        <property role="TrG5h" value="StoreEvents" />
        <node concept="2m7Kf5" id="6u8NlnzqyUb" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="events" />
          <node concept="2m5nlk" id="6u8NlnzqyUf" role="2m7DVh">
            <node concept="3BlaRf" id="6u8NlnzqyUl" role="3GJlyp">
              <ref role="3BrLez" node="6u8NlnzqrBg" resolve="Event" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="6u8NlnzqyTJ" role="NuuwV">
          <property role="Nu42W" value="F3" />
        </node>
        <node concept="2m1Rp1" id="6u8NlnzqyUo" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="6u8Nlnzqzum" role="1GBnQ6">
          <property role="NXePf" value="Storing events on server" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq$Aj" role="1GBnQ6">
          <property role="NX6R2" value="Events for storing" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6u8NlnzqyUb" resolve="events" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="6Fl2chwBL71" role="2m5lHt">
      <property role="TrG5h" value="Raw API" />
      <property role="3XOG$Z" value="raw" />
      <node concept="2m6fVq" id="6Fl2chwBME$" role="2m5mJr">
        <property role="TrG5h" value="RawRequest" />
        <node concept="2m7Kf5" id="6Fl2chwBMEG" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="service" />
          <node concept="2m5ndX" id="6Fl2chwBMEK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBMEN" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="method" />
          <node concept="2m5ndX" id="6Fl2chwBMET" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6Fl2chwBMEW" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="params" />
          <node concept="2m5nlT" id="6Fl2chwBMF4" role="2m7DVh">
            <node concept="3BlaRf" id="6Fl2chwBMFa" role="3GH5xg">
              <ref role="3BrLez" node="2WAO9Y$lyRS" resolve="RawValue" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="6Fl2chwBME_" role="NuuwV">
          <property role="Nu42W" value="A09" />
        </node>
        <node concept="2m1R6W" id="6Fl2chwBMFd" role="2m6efq">
          <node concept="2m7Kf5" id="6Fl2chwBMFi" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="result" />
            <node concept="3BlaRf" id="6Fl2chwBMFm" role="2m7DVh">
              <ref role="3BrLez" node="2WAO9Y$lyRS" resolve="RawValue" />
            </node>
          </node>
          <node concept="Nu42z" id="6Fl2chwBMFe" role="NuuwV">
            <property role="Nu42W" value="A0A" />
          </node>
          <node concept="NXeRC" id="6Fl2chwBPM1" role="1y2DgH">
            <property role="NXePf" value="Response of a raw request" />
          </node>
          <node concept="NX1gA" id="6Fl2chwBPM6" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Result of request" />
            <ref role="NX6Kv" node="6Fl2chwBMFi" resolve="result" />
          </node>
        </node>
        <node concept="NXeRC" id="6Fl2chwBMFp" role="1GBnQ6">
          <property role="NXePf" value="Making raw request to external service via shema-less RPC request" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBOey" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Service name for RPC request" />
          <ref role="NX6Kv" node="6Fl2chwBMEG" resolve="service" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBP0d" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Method for execution" />
          <ref role="NX6Kv" node="6Fl2chwBMEN" resolve="method" />
        </node>
        <node concept="NX1gA" id="6Fl2chwBPLV" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Optional params for the method" />
          <ref role="NX6Kv" node="6Fl2chwBMEW" resolve="params" />
        </node>
      </node>
      <node concept="1Dx9M1" id="6Fl2chwBLTc" role="1Dx9rD">
        <property role="1Dx9K7" value="Schema-less API that is useful for external integrations" />
      </node>
    </node>
    <node concept="2m5mJO" id="6u8NlnzpZhz" role="2m5lHt">
      <property role="TrG5h" value="Wallpappers" />
      <property role="3XOG$Z" value="wallpappers" />
      <node concept="2m5naR" id="6u8Nlnzq1qw" role="2m5mJr">
        <property role="TrG5h" value="Wallpapper" />
        <node concept="2m7Kf5" id="6u8Nlnzq4H3" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="6u8Nlnzq4Hh" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8Nlnzq1qz" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="w" />
          <node concept="2m5ndE" id="6u8Nlnzq1qB" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8Nlnzq1qE" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="h" />
          <node concept="2m5ndE" id="6u8Nlnzq1qK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8Nlnzq1qY" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="fileSize" />
          <node concept="2m5ndE" id="6u8Nlnzq1r8" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8Nlnzq1qN" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="file" />
          <node concept="2m5mGg" id="6u8Nlnzq1qV" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvB$$Hy" resolve="FileLocation" />
          </node>
        </node>
        <node concept="2m7Kf5" id="6u8Nlnzq2wU" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="thumb" />
          <node concept="2m5mGg" id="6u8Nlnzq2x6" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB67s" resolve="FastThumb" />
          </node>
        </node>
        <node concept="NXeRC" id="6u8Nlnzq1X_" role="NXodf">
          <property role="NXePf" value="Wallpapper structure" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq5g7" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Wallpapper id" />
          <ref role="NX6Kv" node="6u8Nlnzq4H3" resolve="id" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq1XE" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Image width" />
          <ref role="NX6Kv" node="6u8Nlnzq1qz" resolve="w" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq1XM" role="NXodf">
          <property role="NX6R2" value="Image height" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6u8Nlnzq1qE" resolve="h" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq1XW" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Image file size" />
          <ref role="NX6Kv" node="6u8Nlnzq1qY" resolve="fileSize" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq1Y8" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Image file reference" />
          <ref role="NX6Kv" node="6u8Nlnzq1qN" resolve="file" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq2xg" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Image thumb" />
          <ref role="NX6Kv" node="6u8Nlnzq2wU" resolve="thumb" />
        </node>
      </node>
      <node concept="2m6fVq" id="6u8Nlnzq495" role="2m5mJr">
        <property role="TrG5h" value="LoadWallpappers" />
        <node concept="2m7Kf5" id="6u8Nlnzq5MW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="maxWidth" />
          <node concept="2m5ndE" id="6u8Nlnzq5N0" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="6u8Nlnzq5N3" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="maxHeight" />
          <node concept="2m5ndE" id="6u8Nlnzq5N9" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6u8Nlnzq496" role="NuuwV">
          <property role="Nu42W" value="F1" />
        </node>
        <node concept="2m1R6W" id="6u8Nlnzq4a8" role="2m6efq">
          <node concept="2m7Kf5" id="6u8Nlnzq4ad" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="wallpappers" />
            <node concept="2m5nlk" id="6u8Nlnzq4ah" role="2m7DVh">
              <node concept="2m5mGg" id="6u8Nlnzq4an" role="3GJlyp">
                <ref role="2m5mJy" node="6u8Nlnzq1qw" resolve="Wallpapper" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="6u8Nlnzq4a9" role="NuuwV">
            <property role="Nu42W" value="F2" />
          </node>
          <node concept="NXeRC" id="6u8Nlnzq8xB" role="1y2DgH">
            <property role="NXePf" value="Loaded Wallpappers" />
          </node>
          <node concept="NX1gA" id="6u8Nlnzq8xG" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Available wallpapper list" />
            <ref role="NX6Kv" node="6u8Nlnzq4ad" resolve="wallpappers" />
          </node>
        </node>
        <node concept="NXeRC" id="6u8Nlnzq6lW" role="1GBnQ6">
          <property role="NXePf" value="Load available wallpappers" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq6SM" role="1GBnQ6">
          <property role="NX6R2" value="Maximum width of wallpapper" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6u8Nlnzq5MW" resolve="maxWidth" />
        </node>
        <node concept="NX1gA" id="6u8Nlnzq6SU" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Maximum height of wallpapper" />
          <ref role="NX6Kv" node="6u8Nlnzq5N3" resolve="maxHeight" />
        </node>
      </node>
      <node concept="1Dx9M1" id="6u8Nlnzq0lW" role="1Dx9rD">
        <property role="1Dx9K7" value="Wallpappers support" />
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
      <node concept="2m6fVq" id="7ZzLuuoIUGN" role="2m5mJr">
        <property role="TrG5h" value="RegisterActorPush" />
        <node concept="NXeRC" id="7ZzLuuoIUHC" role="1GBnQ6">
          <property role="NXePf" value="Register Actor Push token" />
        </node>
        <node concept="NX1gA" id="7ZzLuuoIVxe" role="1GBnQ6">
          <property role="NX6R2" value="Endpoint for push sending" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="7ZzLuuoIUHm" resolve="endpoint" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qKUp" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Push Encryption keys" />
          <ref role="NX6Kv" node="5_CDdZ2qKU6" resolve="encryptionKeys" />
        </node>
        <node concept="2m7Kf5" id="7ZzLuuoIUHm" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="endpoint" />
          <node concept="2m5ndX" id="7ZzLuuoIUHq" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qKU6" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="encryptionKeys" />
          <node concept="2m5nlk" id="5_CDdZ2qKUc" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qKUi" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="7ZzLuuoIUGO" role="NuuwV">
          <property role="Nu42W" value="A0F" />
        </node>
        <node concept="2m1Rp1" id="7ZzLuuoIUHj" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
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
    <node concept="2m5mJO" id="7ZzLuuoIXbw" role="2m5lHt">
      <property role="TrG5h" value="Encryption" />
      <property role="3XOG$Z" value="encryption" />
      <node concept="NvWBy" id="5_CDdZ2jK4s" role="2m5mJr">
        <property role="NvWrd" value="Public Keys" />
      </node>
      <node concept="2m5naR" id="5_CDdZ2jBjR" role="2m5mJr">
        <property role="TrG5h" value="EncryptionKeyGroup" />
        <property role="tsOgz" value="true" />
        <node concept="2m7Kf5" id="5_CDdZ2jBld" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2q7YP" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2rb1o" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="supportedEncryption" />
          <node concept="2m5nlk" id="5_CDdZ2rzq8" role="2m7DVh">
            <node concept="2m5ndX" id="5_CDdZ2rzqe" role="3GJlyp" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qxR4" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="identityKey" />
          <node concept="2m5mGg" id="5_CDdZ2qxRd" role="2m7DVh">
            <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2jJbB" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="keys" />
          <node concept="2m5nlk" id="5_CDdZ2jJbK" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2jJbQ" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qzGT" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="signatures" />
          <node concept="2m5nlk" id="5_CDdZ2qzH4" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qzHd" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2qxTd" resolve="EncryptionKeySignature" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="5_CDdZ2jCcH" role="NXodf">
          <property role="NXePf" value="Encryption Key Group" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jCcM" role="NXodf">
          <property role="NX6R2" value="Key Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2jBld" resolve="keyGroupId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qxRl" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key that is used as identity and for validation of Key Group changes" />
          <ref role="NX6Kv" node="5_CDdZ2qxR4" resolve="identityKey" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2r$kj" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Supported encryption methods by this key group" />
          <ref role="NX6Kv" node="5_CDdZ2rb1o" resolve="supportedEncryption" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qgM2" role="NXodf">
          <property role="NX6R2" value="keys of Key Group" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="5_CDdZ2jJbB" resolve="keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qzHm" role="NXodf">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Signatures of keys" />
          <ref role="NX6Kv" node="5_CDdZ2qzGT" resolve="signatures" />
        </node>
      </node>
      <node concept="2m5naR" id="5_CDdZ2qxTd" role="2m5mJr">
        <property role="TrG5h" value="EncryptionKeySignature" />
        <node concept="2m7Kf5" id="5_CDdZ2qxWl" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keyId" />
          <node concept="2m5ndQ" id="5_CDdZ2qyOk" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qxVk" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="signatureAlg" />
          <node concept="2m5ndX" id="5_CDdZ2qxVs" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qxV7" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="signature" />
          <node concept="2m61tm" id="5_CDdZ2qxVd" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qxVv" role="NXodf">
          <property role="NXePf" value="Signed Key. Usually used for public keys." />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qyOr" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key Id used for signature" />
          <ref role="NX6Kv" node="5_CDdZ2qxWl" resolve="keyId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qxV$" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Signature algorithm" />
          <ref role="NX6Kv" node="5_CDdZ2qxVk" resolve="signatureAlg" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qyO_" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Signature value" />
          <ref role="NX6Kv" node="5_CDdZ2qxV7" resolve="signature" />
        </node>
      </node>
      <node concept="2m5naR" id="5_CDdZ2jFGC" role="2m5mJr">
        <property role="TrG5h" value="EncryptionKey" />
        <property role="tsOgz" value="true" />
        <node concept="2m7Kf5" id="5_CDdZ2jFHK" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keyId" />
          <node concept="2m5ndQ" id="5_CDdZ2jFHO" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2jG_5" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyAlg" />
          <node concept="2m5ndX" id="5_CDdZ2jG_b" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2jG_e" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="keyMaterial" />
          <node concept="2m5nlT" id="5_CDdZ2jTaq" role="2m7DVh">
            <node concept="2m61tm" id="5_CDdZ2jTaw" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qhEj" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="keyHash" />
          <node concept="2m5nlT" id="5_CDdZ2qhEu" role="2m7DVh">
            <node concept="2m61tm" id="5_CDdZ2qhE$" role="3GH5xg" />
          </node>
        </node>
        <node concept="NXeRC" id="5_CDdZ2jG$Z" role="NXodf">
          <property role="NXePf" value="Encryption Key" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jG_s" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key Id" />
          <ref role="NX6Kv" node="5_CDdZ2jFHK" resolve="keyId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jIkc" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key Algorithm" />
          <ref role="NX6Kv" node="5_CDdZ2jG_5" resolve="keyAlg" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jIkm" role="NXodf">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Public key material. Can be null, but always not null for LoadPublicKey" />
          <ref role="NX6Kv" node="5_CDdZ2jG_e" resolve="keyMaterial" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2q_vh" role="NXodf">
          <property role="NX6R2" value="If keyMaterial is null, pass keyHash - SHA256 of key" />
          <ref role="NX6Kv" node="5_CDdZ2qhEj" resolve="keyHash" />
        </node>
      </node>
      <node concept="2m62dX" id="5_CDdZ2jMRg" role="2m5mJr">
        <property role="TrG5h" value="PublicKeys" />
        <node concept="2m7Kf5" id="5_CDdZ2jMSW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="publicKey" />
          <node concept="2m5nlk" id="5_CDdZ2jMT0" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2jMT6" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qGm5" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="signatures" />
          <node concept="2m5nlk" id="5_CDdZ2qGmc" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qGmi" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2qxTd" resolve="EncryptionKeySignature" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2jMRh" role="NuuwV">
          <property role="Nu42W" value="A2A" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2jNOi" role="NXp4Y">
          <property role="NXePf" value="Public Keys response" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jNOn" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Public keys" />
          <ref role="NX6Kv" node="5_CDdZ2jMSW" resolve="publicKey" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qGmp" role="NXp4Y">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Signatures of public keys" />
          <ref role="NX6Kv" node="5_CDdZ2qGm5" resolve="signatures" />
        </node>
      </node>
      <node concept="2m62dX" id="5_CDdZ2jSgI" role="2m5mJr">
        <property role="TrG5h" value="PublicKeyGroups" />
        <node concept="2m7Kf5" id="5_CDdZ2jTag" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="publicKeyGroups" />
          <node concept="2m5nlk" id="5_CDdZ2jTak" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2jTaz" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2jBjR" resolve="EncryptionKeyGroup" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2jSgJ" role="NuuwV">
          <property role="Nu42W" value="A2C" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2jTae" role="NXp4Y">
          <property role="NXePf" value="Public key groups response" />
        </node>
      </node>
      <node concept="2m6fVq" id="5_CDdZ2jK78" role="2m5mJr">
        <property role="TrG5h" value="LoadPublicKeyGroups" />
        <node concept="2m7Kf5" id="5_CDdZ2jKbh" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="userPeer" />
          <node concept="2m5mGg" id="5_CDdZ2jKbl" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2jK79" role="NuuwV">
          <property role="Nu42W" value="A29" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2jKbf" role="1GBnQ6">
          <property role="NXePf" value="Loading Public key groups" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jL36" role="1GBnQ6">
          <property role="NX6R2" value="User's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2jKbh" resolve="userPeer" />
        </node>
        <node concept="2m1Rp1" id="5_CDdZ2jSix" role="2m6efq">
          <ref role="2m1o9l" node="5_CDdZ2jSgI" resolve="PublicKeyGroups" />
        </node>
      </node>
      <node concept="2m6fVq" id="5_CDdZ2jU4b" role="2m5mJr">
        <property role="TrG5h" value="LoadPublicKey" />
        <node concept="2m7Kf5" id="5_CDdZ2jU6a" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="userPeer" />
          <node concept="2m5mGg" id="5_CDdZ2jU6e" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2jU6h" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="2m5ndE" id="59$mN386Y3V" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2jUYg" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="keyIds" />
          <node concept="2m5nlk" id="5_CDdZ2jUYo" role="2m7DVh">
            <node concept="2m5ndQ" id="5_CDdZ2jUYu" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2jU4c" role="NuuwV">
          <property role="Nu42W" value="A2D" />
        </node>
        <node concept="2m1Rp1" id="5_CDdZ2jU67" role="2m6efq">
          <ref role="2m1o9l" node="5_CDdZ2jMRg" resolve="PublicKeys" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2jVQq" role="1GBnQ6">
          <property role="NXePf" value="Loading public key explictly" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jVQv" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's peer" />
          <ref role="NX6Kv" node="5_CDdZ2jU6a" resolve="userPeer" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jVQB" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key group's id" />
          <ref role="NX6Kv" node="5_CDdZ2jU6h" resolve="keyGroupId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jWIH" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key ids for loading" />
          <ref role="NX6Kv" node="5_CDdZ2jUYg" resolve="keyIds" />
        </node>
      </node>
      <node concept="2m6fVq" id="5_CDdZ2jMNJ" role="2m5mJr">
        <property role="TrG5h" value="LoadEphermalPublicKeys" />
        <node concept="2uC4CA" id="5_CDdZ2jQvA" role="2uC9gA">
          <property role="2uC4DK" value="400" />
          <property role="2uC4Qe" value="NO_GROUP_FOUND" />
          <property role="2uCiSL" value="Group not found or was deleted" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2jMPn" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="userPeer" />
          <node concept="2m5mGg" id="5_CDdZ2jMPr" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2jMPu" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="2m5ndE" id="I55RQoUDk1" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="5_CDdZ2jMNK" role="NuuwV">
          <property role="Nu42W" value="A2B" />
        </node>
        <node concept="2m1Rp1" id="5_CDdZ2jNOf" role="2m6efq">
          <ref role="2m1o9l" node="5_CDdZ2jMRg" resolve="PublicKeys" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2jMTe" role="1GBnQ6">
          <property role="NXePf" value="Loading SOME of ephermal public keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jMTj" role="1GBnQ6">
          <property role="NX6R2" value="User's peer" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2jMPn" resolve="userPeer" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2jMTr" role="1GBnQ6">
          <property role="NX6R2" value="User's key group id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2jMPu" resolve="keyGroupId" />
        </node>
      </node>
      <node concept="NpBTk" id="5_CDdZ2q73G" role="2m5mJr">
        <property role="TrG5h" value="PublicKeyGroupChanged" />
        <node concept="2m7Kf5" id="5_CDdZ2q75O" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5_CDdZ2q75S" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qizl" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroup" />
          <node concept="2m5mGg" id="5_CDdZ2qizr" role="2m7DVh">
            <ref role="2m5mJy" node="5_CDdZ2jBjR" resolve="EncryptionKeyGroup" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2q73H" role="NuuwV">
          <property role="Nu42W" value="67" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qe8Y" role="NXp_2">
          <property role="NXePf" value="Update about public key group changed" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qf1c" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's id" />
          <ref role="NX6Kv" node="5_CDdZ2q75O" resolve="uid" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qf1k" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Updated Key Group" />
          <ref role="NX6Kv" node="5_CDdZ2qizl" resolve="keyGroup" />
        </node>
      </node>
      <node concept="NpBTk" id="5_CDdZ2qAsI" role="2m5mJr">
        <property role="TrG5h" value="KeysAdded" />
        <node concept="2m7Kf5" id="5_CDdZ2qAuC" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5_CDdZ2qAuG" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qAuJ" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2qAuS" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qAuV" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="keys" />
          <node concept="2m5nlk" id="5_CDdZ2qAv3" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qAv9" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qAvc" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="signatures" />
          <node concept="2m5nlk" id="5_CDdZ2qAvn" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qAvt" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2qxTd" resolve="EncryptionKeySignature" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qAsJ" role="NuuwV">
          <property role="Nu42W" value="70" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qAvw" role="NXp_2">
          <property role="NXePf" value="Update about keys added to Key Group" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qAzK" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's id" />
          <ref role="NX6Kv" node="5_CDdZ2qAuC" resolve="uid" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qAzS" role="NXp_2">
          <property role="NX6R2" value="Key Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2qAuJ" resolve="keyGroupId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qA$2" role="NXp_2">
          <property role="NX6R2" value="Added keys" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="5_CDdZ2qAuV" resolve="keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qA$e" role="NXp_2">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Signatures of a public key" />
          <ref role="NX6Kv" node="5_CDdZ2qAvc" resolve="signatures" />
        </node>
      </node>
      <node concept="NpBTk" id="5_CDdZ2qAxA" role="2m5mJr">
        <property role="TrG5h" value="KeysRemoved" />
        <node concept="2m7Kf5" id="5_CDdZ2qBst" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5_CDdZ2qBsx" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qBs$" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2qBsE" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qBt2" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="keyIds" />
          <node concept="2m5nlk" id="5_CDdZ2qBta" role="2m7DVh">
            <node concept="2m5ndQ" id="5_CDdZ2qBtg" role="3GJlyp" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qAxB" role="NuuwV">
          <property role="Nu42W" value="71" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qBsr" role="NXp_2">
          <property role="NXePf" value="Update about keys removed from Key Group" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qBtm" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's id" />
          <ref role="NX6Kv" node="5_CDdZ2qBst" resolve="uid" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qClH" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key Group Id" />
          <ref role="NX6Kv" node="5_CDdZ2qBs$" resolve="keyGroupId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qClR" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Removed keys" />
          <ref role="NX6Kv" node="5_CDdZ2qBt2" resolve="keyIds" />
        </node>
      </node>
      <node concept="NpBTk" id="5_CDdZ2qkt8" role="2m5mJr">
        <property role="TrG5h" value="PublicKeyGroupAdded" />
        <node concept="2m7Kf5" id="5_CDdZ2qkvp" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5_CDdZ2qkvt" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qkvw" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroup" />
          <node concept="2m5mGg" id="5_CDdZ2qkvB" role="2m7DVh">
            <ref role="2m5mJy" node="5_CDdZ2jBjR" resolve="EncryptionKeyGroup" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qkt9" role="NuuwV">
          <property role="Nu42W" value="68" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qlnV" role="NXp_2">
          <property role="NXePf" value="Update about public key group added" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qlo0" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's id" />
          <ref role="NX6Kv" node="5_CDdZ2qkvp" resolve="uid" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qlo8" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Added Key Group" />
          <ref role="NX6Kv" node="5_CDdZ2qkvw" resolve="keyGroup" />
        </node>
      </node>
      <node concept="NpBTk" id="5_CDdZ2qlq$" role="2m5mJr">
        <property role="TrG5h" value="PublicKeyGroupRemoved" />
        <node concept="2m7Kf5" id="5_CDdZ2qlsY" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="uid" />
          <node concept="wb0Ql" id="5_CDdZ2qlt2" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qlt5" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2qltb" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qlq_" role="NuuwV">
          <property role="Nu42W" value="69" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qlte" role="NXp_2">
          <property role="NXePf" value="Update about public key group removed" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qltj" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="User's id" />
          <ref role="NX6Kv" node="5_CDdZ2qlsY" resolve="uid" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qltr" role="NXp_2">
          <property role="NX6R2" value="Removed Key Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2qlt5" resolve="keyGroupId" />
        </node>
      </node>
      <node concept="NvWBy" id="5_CDdZ2jYwL" role="2m5mJr">
        <property role="NvWrd" value="Device Side" />
      </node>
      <node concept="2m6fVq" id="5_CDdZ2qF1R" role="2m5mJr">
        <property role="TrG5h" value="CreateNewKeyGroup" />
        <node concept="2m7Kf5" id="5_CDdZ2qF4J" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="identityKey" />
          <node concept="2m5mGg" id="5_CDdZ2qF4T" role="2m7DVh">
            <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2rbVo" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="supportedEncryptions" />
          <node concept="2m5nlk" id="51JxSFfSq5F" role="2m7DVh">
            <node concept="2m5ndX" id="51JxSFfSq5L" role="3GJlyp" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qF4i" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="keys" />
          <node concept="2m5nlk" id="5_CDdZ2qF4m" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qF4s" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qF4v" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="signatures" />
          <node concept="2m5nlk" id="5_CDdZ2qF4A" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qF4G" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2qxTd" resolve="EncryptionKeySignature" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qF1S" role="NuuwV">
          <property role="Nu42W" value="A31" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qF4W" role="1GBnQ6">
          <property role="NXePf" value="Creation of a new Key Group" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qF51" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Identity Key of a new group" />
          <ref role="NX6Kv" node="5_CDdZ2qF4J" resolve="identityKey" />
        </node>
        <node concept="NX1gA" id="51JxSFfSq5U" role="1GBnQ6">
          <property role="NX6R2" value="Supported encryption methods" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2rbVo" resolve="supportedEncryptions" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qF59" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="keys of current group" />
          <ref role="NX6Kv" node="5_CDdZ2qF4i" resolve="keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qF5j" role="1GBnQ6">
          <property role="NX6R2" value="signatures of keys" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2qF4v" resolve="signatures" />
        </node>
        <node concept="2m1R6W" id="5_CDdZ2qN$H" role="2m6efq">
          <node concept="2m7Kf5" id="5_CDdZ2qN$M" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="keyGroupId" />
            <node concept="wb0Ql" id="5_CDdZ2qN$Q" role="2m7DVh">
              <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
            </node>
          </node>
          <node concept="Nu42z" id="5_CDdZ2qN$I" role="NuuwV">
            <property role="Nu42W" value="A32" />
          </node>
          <node concept="NX1gA" id="5_CDdZ2qN$V" role="1y2DgH">
            <property role="NX6R2" value="Created Key Group id" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="5_CDdZ2qN$M" resolve="keyGroupId" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="5_CDdZ2qFld" role="2m5mJr">
        <property role="TrG5h" value="DeleteKeyGroup" />
        <node concept="2m7Kf5" id="5_CDdZ2qFnR" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2qOzA" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qFle" role="NuuwV">
          <property role="Nu42W" value="A33" />
        </node>
        <node concept="2m1Rp1" id="5_CDdZ2qFnY" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qFo1" role="1GBnQ6">
          <property role="NXePf" value="Deletion of a Key Group" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qFo6" role="1GBnQ6">
          <property role="NX6R2" value="Key Group Id for deletion" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2qFnR" resolve="keyGroupId" />
        </node>
      </node>
      <node concept="2m6fVq" id="5_CDdZ2qRi_" role="2m5mJr">
        <property role="TrG5h" value="DisconnectKeyGroup" />
        <node concept="NXeRC" id="5_CDdZ2qRlT" role="1GBnQ6">
          <property role="NXePf" value="Disconnect Key Group from device" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qT8j" role="1GBnQ6">
          <property role="NX6R2" value="Key Group Id for disconnection" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2qRlH" resolve="keyGroupId" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qRlH" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2qRlL" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qRiA" role="NuuwV">
          <property role="Nu42W" value="A35" />
        </node>
        <node concept="2m1Rp1" id="5_CDdZ2qRlO" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
      </node>
      <node concept="2m6fVq" id="5_CDdZ2qU4D" role="2m5mJr">
        <property role="TrG5h" value="ConnectKeyGroup" />
        <node concept="2m7Kf5" id="5_CDdZ2qU7S" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2qU7W" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qU4E" role="NuuwV">
          <property role="Nu42W" value="A36" />
        </node>
        <node concept="2m1Rp1" id="5_CDdZ2qU7Z" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qU82" role="1GBnQ6">
          <property role="NXePf" value="Connectiong Key Group to device" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qU87" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key Group Id for connection" />
          <ref role="NX6Kv" node="5_CDdZ2qU7S" resolve="keyGroupId" />
        </node>
      </node>
      <node concept="2m6fVq" id="5_CDdZ2qOwz" role="2m5mJr">
        <property role="TrG5h" value="UploadEphermalKey" />
        <node concept="2m7Kf5" id="5_CDdZ2qOzv" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2qOzD" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qPsA" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keys" />
          <node concept="2m5nlk" id="5_CDdZ2qPsG" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qPsM" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2jFGC" resolve="EncryptionKey" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2qPsP" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="signatures" />
          <node concept="2m5nlk" id="5_CDdZ2qPsY" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2qPt4" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2qxTd" resolve="EncryptionKeySignature" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="5_CDdZ2qOw$" role="NuuwV">
          <property role="Nu42W" value="A34" />
        </node>
        <node concept="2m1Rp1" id="5_CDdZ2qOzs" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2qPs$" role="1GBnQ6">
          <property role="NXePf" value="Uploading Ephermal Keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qQm9" role="1GBnQ6">
          <property role="NX6R2" value="Key Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2qOzv" resolve="keyGroupId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qQmh" role="1GBnQ6">
          <property role="NX6R2" value="Encryprion keys" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="5_CDdZ2qPsA" resolve="keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2qQmr" role="1GBnQ6">
          <property role="NX6R2" value="Key signatures" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="5_CDdZ2qPsP" resolve="signatures" />
        </node>
      </node>
      <node concept="NvWBy" id="5_CDdZ2r0Lg" role="2m5mJr">
        <property role="NvWrd" value="Encrypted Packages" />
      </node>
      <node concept="2m5naR" id="5_CDdZ2r0S4" role="2m5mJr">
        <property role="TrG5h" value="EncryptedBox" />
        <node concept="2m7Kf5" id="5_CDdZ2r6rJ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="keys" />
          <node concept="2m5nlk" id="5_CDdZ2r6rQ" role="2m7DVh">
            <node concept="2m5mGg" id="5_CDdZ2r6rW" role="3GJlyp">
              <ref role="2m5mJy" node="5_CDdZ2r0YU" resolve="EncyptedBoxKey" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2rwG4" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="algType" />
          <node concept="2m5ndX" id="5_CDdZ2rwGd" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2r6rZ" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="encPackage" />
          <node concept="2m61tm" id="5_CDdZ2r6s6" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2r6s9" role="NXodf">
          <property role="NXePf" value="Encrypted package that is encrypted for multiple keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2r6se" role="NXodf">
          <property role="NX6R2" value="Encrypted encryption keys" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2r6rJ" resolve="keys" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2rxAe" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Package encryption type" />
          <ref role="NX6Kv" node="5_CDdZ2rwG4" resolve="algType" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2r6sm" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Encrypted package" />
          <ref role="NX6Kv" node="5_CDdZ2r6rZ" resolve="encPackage" />
        </node>
      </node>
      <node concept="2m5naR" id="5_CDdZ2r0YU" role="2m5mJr">
        <property role="TrG5h" value="EncyptedBoxKey" />
        <node concept="2m7Kf5" id="5_CDdZ2rhAZ" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="usersId" />
          <node concept="wb0Ql" id="5_CDdZ2rhB9" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2r1VM" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="keyGroupId" />
          <node concept="wb0Ql" id="5_CDdZ2r2Pk" role="2m7DVh">
            <ref role="wb18D" node="5_CDdZ2q76m" resolve="keyGroupId" />
          </node>
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2r4Cw" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="algType" />
          <node concept="2m5ndX" id="5_CDdZ2ruSj" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5_CDdZ2r2Pn" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="encryptedKey" />
          <node concept="2m61tm" id="5_CDdZ2r3IX" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="5_CDdZ2r7m5" role="NXodf">
          <property role="NXePf" value="Encrypted package encryption key" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2r8fQ" role="NXodf">
          <property role="NX6R2" value="Key Group Id" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5_CDdZ2r1VM" resolve="keyGroupId" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2r8fY" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Key encryption algorithm" />
          <ref role="NX6Kv" node="5_CDdZ2r4Cw" resolve="algType" />
        </node>
        <node concept="NX1gA" id="5_CDdZ2r8g8" role="NXodf">
          <property role="NX6R2" value="Encrypted encryption key" />
          <ref role="NX6Kv" node="5_CDdZ2r2Pn" resolve="encryptedKey" />
        </node>
      </node>
      <node concept="1Dx9M1" id="7ZzLuuoIXYH" role="1Dx9rD">
        <property role="1Dx9K7" value="Package that include encryption support" />
      </node>
      <node concept="1Dx9M1" id="7ZzLuuoJ1cV" role="1Dx9rD">
        <property role="1Dx9K7" value="Key alg types:" />
      </node>
      <node concept="1Dx9M1" id="7ZzLuuoJ1cY" role="1Dx9rD">
        <property role="1Dx9K7" value="* curve25519 - https://en.wikipedia.org/wiki/Curve25519" />
      </node>
      <node concept="1Dx9M1" id="7ZzLuuoJaIM" role="1Dx9rD">
        <property role="1Dx9K7" value="* rsa-2048" />
      </node>
      <node concept="1Dx9M1" id="7ZzLuuoJLv4" role="1Dx9rD">
        <property role="1Dx9K7" value="* rsa-4096" />
      </node>
      <node concept="1Dx9M1" id="5_CDdZ2jD3Y" role="1Dx9rD">
        <property role="1Dx9K7" value="* aes-128" />
      </node>
      <node concept="1Dx9M1" id="5_CDdZ2jG_I" role="1Dx9rD">
        <property role="1Dx9K7" value="* kuznechik-128" />
      </node>
      <node concept="1Dx9M1" id="5_CDdZ2qxVL" role="1Dx9rD">
        <property role="1Dx9K7" value=" " />
      </node>
      <node concept="1Dx9M1" id="5_CDdZ2qxVC" role="1Dx9rD">
        <property role="1Dx9K7" value="Signature alg types:" />
      </node>
      <node concept="1Dx9M1" id="5_CDdZ2qxVV" role="1Dx9rD">
        <property role="1Dx9K7" value="* Ed25519 - used curve25519 conveted to Ed255519 for signing and validataion" />
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
          <node concept="wb0Ql" id="5qm50Y0eTfH" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uz" resolve="userId" />
          </node>
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
          <node concept="wb0Ql" id="5qm50Y0eTfK" role="2m7DVh">
            <ref role="wb18D" node="5qm50Y0e3uO" resolve="groupId" />
          </node>
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
      <node concept="2m5naR" id="4fp6Gpc5n7o" role="2m5mJr">
        <property role="TrG5h" value="UpdateContainer" />
        <node concept="2m7Kf5" id="4fp6Gpc5n9Y" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="updateHeader" />
          <node concept="2m5ndE" id="4fp6Gpc5na2" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5na5" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="update" />
          <node concept="2m61tm" id="4fp6Gpc5nab" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="4fp6Gpc5nae" role="NXodf">
          <property role="NXePf" value="Update container" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5o51" role="NXodf">
          <property role="NX6R2" value="Header of update" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4fp6Gpc5n9Y" resolve="updateHeader" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5oZS" role="NXodf">
          <property role="NX6R2" value="The updatre" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="4fp6Gpc5na5" resolve="update" />
        </node>
      </node>
      <node concept="3GIgwz" id="4fp6Gpc58D4" role="2m5mJr">
        <property role="TrG5h" value="CombinedUpdate" />
        <node concept="2m7Kf5" id="4fp6Gpc5kaI" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="seqStart" />
          <node concept="2m5ndE" id="4fp6Gpc5kaM" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5kaP" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="seqEnd" />
          <node concept="2m5ndE" id="4fp6Gpc5kaV" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5kaY" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="state" />
          <node concept="wb0Ql" id="4fp6Gpc5kb6" role="2m7DVh">
            <ref role="wb18D" node="55bmeIQ7$gx" resolve="seq_state" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5l5L" role="2m0hLx">
          <property role="2m7DUN" value="4" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="4fp6Gpc5l5V" role="2m7DVh">
            <node concept="2m5mGg" id="4fp6Gpc5l61" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5l64" role="2m0hLx">
          <property role="2m7DUN" value="5" />
          <property role="TrG5h" value="groups" />
          <node concept="2m5nlk" id="4fp6Gpc5l6h" role="2m7DVh">
            <node concept="2m5mGg" id="4fp6Gpc5l6n" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5mWP" role="2m0hLx">
          <property role="2m7DUN" value="6" />
          <property role="TrG5h" value="updates" />
          <node concept="2m5nlk" id="4fp6Gpc5pXb" role="2m7DVh">
            <node concept="2m5mGg" id="4fp6Gpc5pXh" role="3GJlyp">
              <ref role="2m5mJy" node="4fp6Gpc5n7o" resolve="UpdateContainer" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5FTn" role="2m0hLx">
          <property role="2m7DUN" value="7" />
          <property role="TrG5h" value="messages" />
          <node concept="2m5nlk" id="4fp6Gpc5FTo" role="2m7DVh">
            <node concept="2m5mGg" id="4fp6Gpc5FTp" role="3GJlyp">
              <ref role="2m5mJy" node="gbd4oSj4vy" resolve="MessageContainer" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="4fp6Gpc58D5" role="NuuwV">
          <property role="Nu42W" value="A41" />
        </node>
        <node concept="NXeRC" id="4fp6Gpc5qRX" role="NXpPy">
          <property role="NXePf" value="Combined update" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5rMG" role="NXpPy">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Sequence number start" />
          <ref role="NX6Kv" node="4fp6Gpc5kaI" resolve="seqStart" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5rMO" role="NXpPy">
          <property role="NX6R2" value="Sequence number end" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4fp6Gpc5kaP" resolve="seqEnd" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5rMY" role="NXpPy">
          <property role="NX6R2" value="Sequence state" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4fp6Gpc5kaY" resolve="state" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5rNa" role="NXpPy">
          <property role="NX6R2" value="Update's users" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="4fp6Gpc5l5L" resolve="users" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5rNo" role="NXpPy">
          <property role="NX6R2" value="Update's groups" />
          <property role="1GSvIU" value="compact" />
          <ref role="NX6Kv" node="4fp6Gpc5l64" resolve="groups" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5rNC" role="NXpPy">
          <property role="NX6R2" value="Updates (can be empty)" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4fp6Gpc5mWP" resolve="updates" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5FU9" role="NXpPy">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="New messages" />
          <ref role="NX6Kv" node="4fp6Gpc5FTn" resolve="messages" />
        </node>
      </node>
      <node concept="2m488m" id="4fp6Gpc4YV7" role="2m5mJr">
        <property role="TrG5h" value="UpdateOptimization" />
        <node concept="2m7y0F" id="4fp6Gpc4YV9" role="2m7ymf">
          <property role="TrG5h" value="NONE" />
          <property role="2m7y0m" value="1" />
        </node>
        <node concept="2m7y0F" id="4fp6Gpc4YXp" role="2m7ymf">
          <property role="TrG5h" value="STRIP_ENTITIES" />
          <property role="2m7y0m" value="2" />
        </node>
        <node concept="2m7y0F" id="4fp6Gpc5sIx" role="2m7ymf">
          <property role="TrG5h" value="ENABLE_COMBINED" />
          <property role="2m7y0m" value="3" />
        </node>
        <node concept="2m7y0F" id="4fp6Gpc5GPi" role="2m7ymf">
          <property role="TrG5h" value="FASTER_MESSAGES" />
          <property role="2m7y0m" value="4" />
        </node>
      </node>
      <node concept="2m6fVq" id="GBscvBBAdf" role="2m5mJr">
        <property role="TrG5h" value="GetState" />
        <node concept="2m7Kf5" id="4fp6Gpc5tDm" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="optimizations" />
          <node concept="2m5nlk" id="4fp6Gpc5tDq" role="2m7DVh">
            <node concept="3GJkcs" id="4fp6Gpc5tDw" role="3GJlyp">
              <ref role="3GJkik" node="4fp6Gpc4YV7" resolve="UpdateOptimization" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="GBscvBBAdg" role="NuuwV">
          <property role="Nu42W" value="09" />
        </node>
        <node concept="2m1Rp1" id="GBscvBBAdN" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_W" resolve="Seq" />
        </node>
        <node concept="NXeRC" id="2EAJ7H6foAE" role="1GBnQ6">
          <property role="NXePf" value="Get main sequence state" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5tDA" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Enabled optimizations" />
          <ref role="NX6Kv" node="4fp6Gpc5tDm" resolve="optimizations" />
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
        <node concept="NX1gA" id="4fp6Gpc56Lx" role="1GBnQ6">
          <property role="NX6R2" value="Enabled optimizations" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4fp6Gpc540T" resolve="optimizations" />
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
        <node concept="2m7Kf5" id="4fp6Gpc540T" role="2m0hLx">
          <property role="2m7DUN" value="3" />
          <property role="TrG5h" value="optimizations" />
          <node concept="2m5nlk" id="4fp6Gpc56Lj" role="2m7DVh">
            <node concept="3GJkcs" id="4fp6Gpc56Lp" role="3GJlyp">
              <ref role="3GJkik" node="4fp6Gpc4YV7" resolve="UpdateOptimization" />
            </node>
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
          <node concept="NX1gA" id="4fp6Gpc5U0W" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Direct references of users" />
            <ref role="NX6Kv" node="4fp6Gpc5TZN" resolve="usersRefs" />
          </node>
          <node concept="NX1gA" id="4fp6Gpc5U1e" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Direct References of groups" />
            <ref role="NX6Kv" node="4fp6Gpc5U0i" resolve="groupsRefs" />
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
              <node concept="2m5mGg" id="4fp6Gpc5oZX" role="3GJlyp">
                <ref role="2m5mJy" node="4fp6Gpc5n7o" resolve="UpdateContainer" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="4fp6Gpc5EXx" role="2m0hLx">
            <property role="2m7DUN" value="7" />
            <property role="TrG5h" value="messages" />
            <node concept="2m5nlk" id="4fp6Gpc5EXO" role="2m7DVh">
              <node concept="2m5mGg" id="4fp6Gpc5EXU" role="3GJlyp">
                <ref role="2m5mJy" node="gbd4oSj4vy" resolve="MessageContainer" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="GBscvBB_Xm" role="2m0hLx">
            <property role="2m7DUN" value="5" />
            <property role="TrG5h" value="needMore" />
            <node concept="2m5ndN" id="GBscvBB_XB" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="4fp6Gpc5TZN" role="2m0hLx">
            <property role="2m7DUN" value="8" />
            <property role="TrG5h" value="usersRefs" />
            <node concept="2m5nlk" id="4fp6Gpc5U09" role="2m7DVh">
              <node concept="2m5mGg" id="4fp6Gpc5U0f" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="4fp6Gpc5U0i" role="2m0hLx">
            <property role="2m7DUN" value="9" />
            <property role="TrG5h" value="groupsRefs" />
            <node concept="2m5nlk" id="4fp6Gpc5U0F" role="2m7DVh">
              <node concept="2m5mGg" id="4fp6Gpc5U0L" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="GBscvBB_W6" role="NuuwV">
            <property role="Nu42W" value="0C" />
          </node>
        </node>
      </node>
      <node concept="2m6fVq" id="4fp6Gpc5Ra_" role="2m5mJr">
        <property role="TrG5h" value="GetReferencedEntitites" />
        <node concept="2m7Kf5" id="4fp6Gpc5S8r" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="users" />
          <node concept="2m5nlk" id="4fp6Gpc5S8v" role="2m7DVh">
            <node concept="2m5mGg" id="4fp6Gpc5S8_" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6ia" resolve="UserOutPeer" />
            </node>
          </node>
        </node>
        <node concept="2m7Kf5" id="4fp6Gpc5S8C" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="groups" />
          <node concept="2m5nlk" id="4fp6Gpc5S8J" role="2m7DVh">
            <node concept="2m5mGg" id="4fp6Gpc5S8P" role="3GJlyp">
              <ref role="2m5mJy" node="GBscvBB6j2" resolve="GroupOutPeer" />
            </node>
          </node>
        </node>
        <node concept="Nu42z" id="4fp6Gpc5RaA" role="NuuwV">
          <property role="Nu42W" value="A44" />
        </node>
        <node concept="2m1R6W" id="4fp6Gpc5S8S" role="2m6efq">
          <node concept="2m7Kf5" id="4fp6Gpc5S8X" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="users" />
            <node concept="2m5nlk" id="4fp6Gpc5S91" role="2m7DVh">
              <node concept="2m5mGg" id="4fp6Gpc5S97" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBAzbH" resolve="User" />
              </node>
            </node>
          </node>
          <node concept="2m7Kf5" id="4fp6Gpc5S9a" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="groups" />
            <node concept="2m5nlk" id="4fp6Gpc5S9h" role="2m7DVh">
              <node concept="2m5mGg" id="4fp6Gpc5S9n" role="3GJlyp">
                <ref role="2m5mJy" node="GBscvBB6pR" resolve="Group" />
              </node>
            </node>
          </node>
          <node concept="Nu42z" id="4fp6Gpc5S8T" role="NuuwV">
            <property role="Nu42W" value="A45" />
          </node>
          <node concept="NX1gA" id="4fp6Gpc5UWz" role="1y2DgH">
            <property role="NX6R2" value="Loaded users" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="4fp6Gpc5S8X" resolve="users" />
          </node>
          <node concept="NX1gA" id="4fp6Gpc5UWC" role="1y2DgH">
            <property role="NX6R2" value="Loaded groups" />
            <property role="1GSvIU" value="compact" />
            <ref role="NX6Kv" node="4fp6Gpc5S9a" resolve="groups" />
          </node>
        </node>
        <node concept="NXeRC" id="4fp6Gpc5S9q" role="1GBnQ6">
          <property role="NXePf" value="Loading referenced entities" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5S9v" role="1GBnQ6">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Users to load" />
          <ref role="NX6Kv" node="4fp6Gpc5S8r" resolve="users" />
        </node>
        <node concept="NX1gA" id="4fp6Gpc5T4D" role="1GBnQ6">
          <property role="1GSvIU" value="compact" />
          <property role="NX6R2" value="Groups to load. Also returns all members of a group." />
          <ref role="NX6Kv" node="4fp6Gpc5S8C" resolve="groups" />
        </node>
      </node>
      <node concept="NpBTk" id="1GlYFhnbv9G" role="2m5mJr">
        <property role="TrG5h" value="RawUpdate" />
        <node concept="2m7Kf5" id="1GlYFhnbws5" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="type" />
          <node concept="2m5nlT" id="1GlYFhnbwsq" role="2m7DVh">
            <node concept="2m5ndX" id="1GlYFhnbwsw" role="3GH5xg" />
          </node>
        </node>
        <node concept="2m7Kf5" id="1GlYFhnbvbN" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="bytes" />
          <node concept="2m61tm" id="1GlYFhnbvNZ" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="1GlYFhnbv9H" role="NuuwV">
          <property role="Nu42W" value="50" />
        </node>
        <node concept="NXeRC" id="1GlYFhnbvbU" role="NXp_2">
          <property role="NXePf" value="Custom Raw Update" />
        </node>
        <node concept="NX1gA" id="1GlYFhnbwsB" role="NXp_2">
          <property role="NX6R2" value="Type of content" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="1GlYFhnbws5" resolve="type" />
        </node>
        <node concept="NX1gA" id="1GlYFhnbvNV" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Raw data" />
          <ref role="NX6Kv" node="1GlYFhnbvbN" resolve="bytes" />
        </node>
      </node>
      <node concept="NpBTk" id="4fp6Gpc5wsV" role="2m5mJr">
        <property role="TrG5h" value="EmptyUpdate" />
        <node concept="Nu42z" id="4fp6Gpc5wsW" role="NuuwV">
          <property role="Nu42W" value="55" />
        </node>
        <node concept="NXeRC" id="4fp6Gpc5wvK" role="NXp_2">
          <property role="NXePf" value="Empty update" />
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
    <node concept="2m5mJO" id="2s6T_DpzBkW" role="2m5lHt">
      <property role="TrG5h" value="Counters" />
      <property role="3XOG$Z" value="counters" />
      <node concept="1Dx9M1" id="2s6T_DpzFJ5" role="1Dx9rD">
        <property role="1Dx9K7" value="Application Counters, used to display various counters in application" />
      </node>
      <node concept="2m5naR" id="2s6T_DpzDyV" role="2m5mJr">
        <property role="TrG5h" value="AppCounters" />
        <property role="tsOgz" value="true" />
        <node concept="NXeRC" id="2s6T_DpzE61" role="NXodf">
          <property role="NXePf" value="Application counters" />
        </node>
        <node concept="NX1gA" id="10Y$hTkXaXB" role="NXodf">
          <property role="NX6R2" value="Global unread counter" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="2s6T_DpzDyY" resolve="globalCounter" />
        </node>
        <node concept="2m7Kf5" id="2s6T_DpzDyY" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="globalCounter" />
          <node concept="2m5nlT" id="2s6T_DpzDz6" role="2m7DVh">
            <node concept="2m5ndE" id="2s6T_DpzDzc" role="3GH5xg" />
          </node>
        </node>
      </node>
      <node concept="NpBTk" id="2s6T_DpzECW" role="2m5mJr">
        <property role="TrG5h" value="CountersChanged" />
        <node concept="NXeRC" id="2s6T_DpzFc4" role="NXp_2">
          <property role="NXePf" value="Update about counters changed" />
        </node>
        <node concept="NX1gA" id="2s6T_DpzFca" role="NXp_2">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Current Application counters" />
          <ref role="NX6Kv" node="2s6T_DpzED6" resolve="counters" />
        </node>
        <node concept="2m7Kf5" id="2s6T_DpzED6" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="counters" />
          <node concept="2m5mGg" id="2s6T_DpzEDa" role="2m7DVh">
            <ref role="2m5mJy" node="2s6T_DpzDyV" resolve="AppCounters" />
          </node>
        </node>
        <node concept="Nu42z" id="2s6T_DpzECX" role="NuuwV">
          <property role="Nu42W" value="D7" />
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
      <node concept="2m62dX" id="6WYZhOUYtcE" role="2m5mJr">
        <property role="TrG5h" value="Bool" />
        <node concept="NXeRC" id="6WYZhOUYtHy" role="NXp4Y">
          <property role="NXePf" value="Boolean response" />
        </node>
        <node concept="NX1gA" id="6WYZhOUYudH" role="NXp4Y">
          <property role="NX6R2" value="Response value" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="6WYZhOUYtdi" resolve="value" />
        </node>
        <node concept="2m7Kf5" id="6WYZhOUYtdi" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="value" />
          <node concept="2m5ndN" id="6WYZhOUYtdp" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="6WYZhOUYtcF" role="NuuwV">
          <property role="Nu42W" value="D1" />
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
      <node concept="2m5naR" id="3pJJa69XFS8" role="2m5mJr">
        <property role="TrG5h" value="Extension" />
        <node concept="2m7Kf5" id="3pJJa69XFSO" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="id" />
          <node concept="2m5ndE" id="3pJJa69XFSS" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="3pJJa69XFSV" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="data" />
          <node concept="2m61tm" id="3pJJa69XFT1" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="3pJJa69XFT4" role="NXodf">
          <property role="NXePf" value="Extention" />
        </node>
        <node concept="NX1gA" id="3pJJa69XFTh" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Extension id" />
          <ref role="NX6Kv" node="3pJJa69XFSO" resolve="id" />
        </node>
        <node concept="NX1gA" id="3pJJa69XFTp" role="NXodf">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Extension data" />
          <ref role="NX6Kv" node="3pJJa69XFSV" resolve="data" />
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
    <node concept="2m5mJO" id="4NJj1GT1JDA" role="2m5lHt">
      <property role="TrG5h" value="Device Info" />
      <property role="3XOG$Z" value="device" />
      <node concept="1Dx9M1" id="4NJj1GT1Kfy" role="1Dx9rD">
        <property role="1Dx9K7" value="Submiting various information about device for providing better experience. " />
      </node>
      <node concept="1Dx9M1" id="4NJj1GT1Kf$" role="1Dx9rD">
        <property role="1Dx9K7" value="For example, getting timezone or preffered languages" />
      </node>
      <node concept="2m6fVq" id="4NJj1GT1KfB" role="2m5mJr">
        <property role="TrG5h" value="NotifyAboutDeviceInfo" />
        <node concept="2m7Kf5" id="4NJj1GT1KfO" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="preferredLanguages" />
          <node concept="2m5nlk" id="4NJj1GT1KfS" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT1KfY" role="3GJlyp" />
          </node>
        </node>
        <node concept="2m7Kf5" id="4NJj1GT1Kg1" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="timeZone" />
          <node concept="2m5nlT" id="4NJj1GT1Kgk" role="2m7DVh">
            <node concept="2m5ndX" id="4NJj1GT1Kgq" role="3GH5xg" />
          </node>
        </node>
        <node concept="Nu42z" id="4NJj1GT1KfC" role="NuuwV">
          <property role="Nu42W" value="e5" />
        </node>
        <node concept="2m1Rp1" id="4NJj1GT1KfL" role="2m6efq">
          <ref role="2m1o9l" node="GBscvBB6_K" resolve="Void" />
        </node>
        <node concept="NXeRC" id="4NJj1GT1KQJ" role="1GBnQ6">
          <property role="NXePf" value="Notifying about device information" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1Lt7" role="1GBnQ6">
          <property role="NX6R2" value="Preferred languages" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1KfO" resolve="preferredLanguages" />
        </node>
        <node concept="NX1gA" id="4NJj1GT1Ltf" role="1GBnQ6">
          <property role="NX6R2" value="Device Time Zone" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="4NJj1GT1Kg1" resolve="timeZone" />
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="5D8Crj7jK_S" role="2m5lHt">
      <property role="TrG5h" value="Webactions" />
      <property role="3XOG$Z" value="webactions" />
      <node concept="1Dx9M1" id="5D8Crj7jLFz" role="1Dx9rD">
        <property role="1Dx9K7" value="Web actions allow clients to visit some predefined web pages," />
      </node>
      <node concept="1Dx9M1" id="5D8Crj7jWP7" role="1Dx9rD">
        <property role="1Dx9K7" value="perform actions, and pass result on action completion" />
      </node>
      <node concept="2m6fVq" id="5D8Crj7jLFF" role="2m5mJr">
        <property role="TrG5h" value="InitWebaction" />
        <node concept="NXeRC" id="5D8Crj7jPzN" role="1GBnQ6">
          <property role="NXePf" value="Initialize start of web action" />
        </node>
        <node concept="2m7Kf5" id="5D8Crj7jMLG" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="actionName" />
          <node concept="2m5ndX" id="5D8Crj7jNkK" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5D8Crj7jNkU" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="params" />
          <node concept="2m5mGg" id="5D8Crj7jNSg" role="2m7DVh">
            <ref role="2m5mJy" node="2WAO9Y$lCqr" resolve="MapValue" />
          </node>
        </node>
        <node concept="Nu42z" id="5D8Crj7jLFG" role="NuuwV">
          <property role="Nu42W" value="74" />
        </node>
        <node concept="2m1R6W" id="5D8Crj7jNkN" role="2m6efq">
          <node concept="2m7Kf5" id="5D8Crj7jNSt" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="uri" />
            <node concept="2m5ndX" id="5D8Crj7jNSx" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="5D8Crj7jNS$" role="2m0hLx">
            <property role="2m7DUN" value="2" />
            <property role="TrG5h" value="regexp" />
            <node concept="2m5ndX" id="5D8Crj7jPye" role="2m7DVh" />
          </node>
          <node concept="2m7Kf5" id="5D8Crj7jRKl" role="2m0hLx">
            <property role="2m7DUN" value="3" />
            <property role="TrG5h" value="actionHash" />
            <node concept="2m5ndX" id="5D8Crj7jRKt" role="2m7DVh" />
          </node>
          <node concept="Nu42z" id="5D8Crj7jNkO" role="NuuwV">
            <property role="Nu42W" value="75" />
          </node>
          <node concept="NX1gA" id="5D8Crj7jVH0" role="1y2DgH">
            <property role="NX6R2" value="Web action uri" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="5D8Crj7jNSt" resolve="uri" />
          </node>
          <node concept="NX1gA" id="5D8Crj7jVHb" role="1y2DgH">
            <property role="1GSvIU" value="full" />
            <property role="NX6R2" value="Regular expression. Required to match completion of web action" />
            <ref role="NX6Kv" node="5D8Crj7jNS$" resolve="regexp" />
          </node>
          <node concept="NX1gA" id="5D8Crj7jVHj" role="1y2DgH">
            <property role="1GSvIU" value="danger" />
            <property role="NX6R2" value="Identifier of current web action. Required to complete it" />
            <ref role="NX6Kv" node="5D8Crj7jRKl" resolve="actionHash" />
          </node>
        </node>
        <node concept="NX1gA" id="5D8Crj7jNkS" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Name of web action" />
          <ref role="NX6Kv" node="5D8Crj7jMLG" resolve="actionName" />
        </node>
        <node concept="NX1gA" id="5D8Crj7jQDG" role="1GBnQ6">
          <property role="1GSvIU" value="full" />
          <property role="NX6R2" value="Additional params required to make action's uri" />
          <ref role="NX6Kv" node="5D8Crj7jNkU" resolve="params" />
        </node>
      </node>
      <node concept="2m6fVq" id="5D8Crj7jPyI" role="2m5mJr">
        <property role="TrG5h" value="CompleteWebaction" />
        <node concept="2m7Kf5" id="5D8Crj7jSRD" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="actionHash" />
          <node concept="2m5ndX" id="5D8Crj7jTqZ" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="5D8Crj7jWNu" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="completeUri" />
          <node concept="2m5ndX" id="5D8Crj7jWN$" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="5D8Crj7jQDM" role="1GBnQ6">
          <property role="NXePf" value="Complete started web action" />
        </node>
        <node concept="NX1gA" id="5D8Crj7jVGE" role="1GBnQ6">
          <property role="1GSvIU" value="danger" />
          <property role="NX6R2" value="Identifier of given web action" />
          <ref role="NX6Kv" node="5D8Crj7jSRD" resolve="actionHash" />
        </node>
        <node concept="NX1gA" id="5D8Crj7jWNS" role="1GBnQ6">
          <property role="NX6R2" value="Final uri of web action" />
          <property role="1GSvIU" value="full" />
          <ref role="NX6Kv" node="5D8Crj7jWNu" resolve="completeUri" />
        </node>
        <node concept="Nu42z" id="5D8Crj7jPyJ" role="NuuwV">
          <property role="Nu42W" value="7B" />
        </node>
        <node concept="2m1R6W" id="5D8Crj7jWON" role="2m6efq">
          <node concept="2m7Kf5" id="5D8Crj7jWOS" role="2m0hLx">
            <property role="2m7DUN" value="1" />
            <property role="TrG5h" value="result" />
            <node concept="2m5mGg" id="5D8Crj7jWOW" role="2m7DVh">
              <ref role="2m5mJy" node="2WAO9Y$lCqr" resolve="MapValue" />
            </node>
          </node>
          <node concept="Nu42z" id="5D8Crj7jWOO" role="NuuwV">
            <property role="Nu42W" value="7C" />
          </node>
          <node concept="NX1gA" id="5D8Crj7jZ2a" role="1y2DgH">
            <property role="NX6R2" value="Reslut of web action completion" />
            <property role="1GSvIU" value="full" />
            <ref role="NX6Kv" node="5D8Crj7jWOS" resolve="result" />
          </node>
        </node>
      </node>
    </node>
    <node concept="2m5mJO" id="2WAO9Y$lqDN" role="2m5lHt">
      <property role="TrG5h" value="Collections" />
      <property role="3XOG$Z" value="collections" />
      <node concept="w93zz" id="2WAO9Y$lyRS" role="2m5mJr">
        <property role="1FaRnq" value="true" />
        <property role="TrG5h" value="RawValue" />
      </node>
      <node concept="2m5naR" id="2WAO9Y$l$xO" role="2m5mJr">
        <property role="TrG5h" value="StringValue" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="2WAO9Y$lyRS" resolve="RawValue" />
        <node concept="2m7Kf5" id="2WAO9Y$l$xU" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="text" />
          <node concept="2m5ndX" id="2WAO9Y$l$xY" role="2m7DVh" />
        </node>
        <node concept="Nu42z" id="2WAO9Y$l$xS" role="3BtCOu">
          <property role="Nu42W" value="01" />
        </node>
        <node concept="NXeRC" id="2WAO9Y$l_CH" role="NXodf">
          <property role="NXePf" value="Text value" />
        </node>
      </node>
      <node concept="2m5naR" id="2WAO9Y$lFea" role="2m5mJr">
        <property role="TrG5h" value="Int32Value" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="2WAO9Y$lyRS" resolve="RawValue" />
        <node concept="2m7Kf5" id="2WAO9Y$lFeu" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="value" />
          <node concept="2m5ndE" id="2WAO9Y$lFey" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="2WAO9Y$lFe_" role="NXodf">
          <property role="NXePf" value="Int32 value" />
        </node>
        <node concept="Nu42z" id="2WAO9Y$lHxb" role="3BtCOu">
          <property role="Nu42W" value="02" />
        </node>
      </node>
      <node concept="2m5naR" id="2WAO9Y$lFM$" role="2m5mJr">
        <property role="TrG5h" value="Int64Value" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="2WAO9Y$lyRS" resolve="RawValue" />
        <node concept="2m7Kf5" id="2WAO9Y$lFMW" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="value" />
          <node concept="2m5ndQ" id="2WAO9Y$lFN0" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="2WAO9Y$lFN3" role="NXodf">
          <property role="NXePf" value="Int64 value" />
        </node>
        <node concept="Nu42z" id="2WAO9Y$lI4Z" role="3BtCOu">
          <property role="Nu42W" value="03" />
        </node>
      </node>
      <node concept="2m5naR" id="2WAO9Y$lGna" role="2m5mJr">
        <property role="TrG5h" value="DoubleValue" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="2WAO9Y$lyRS" resolve="RawValue" />
        <node concept="2m7Kf5" id="2WAO9Y$lGnA" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="value" />
          <node concept="3GIWu7" id="2WAO9Y$lGnE" role="2m7DVh" />
        </node>
        <node concept="NXeRC" id="2WAO9Y$lGVs" role="NXodf">
          <property role="NXePf" value="Double value" />
        </node>
        <node concept="Nu42z" id="2WAO9Y$lICO" role="3BtCOu">
          <property role="Nu42W" value="04" />
        </node>
      </node>
      <node concept="2m5naR" id="2WAO9Y$lHwB" role="2m5mJr">
        <property role="TrG5h" value="ArrayValue" />
        <property role="tsOgz" value="false" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="2WAO9Y$lyRS" resolve="RawValue" />
        <node concept="2m7Kf5" id="2WAO9Y$lHx7" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="array" />
          <node concept="2m5nlk" id="2WAO9Y$lJKz" role="2m7DVh">
            <node concept="3BlaRf" id="2WAO9Y$lJKD" role="3GJlyp">
              <ref role="3BrLez" node="2WAO9Y$lyRS" resolve="RawValue" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="2WAO9Y$lJcE" role="NXodf">
          <property role="NXePf" value="Array value" />
        </node>
        <node concept="Nu42z" id="2WAO9Y$lJKx" role="3BtCOu">
          <property role="Nu42W" value="05" />
        </node>
      </node>
      <node concept="2m5naR" id="2WAO9Y$lCqX" role="2m5mJr">
        <property role="TrG5h" value="MapValueItem" />
        <node concept="NXeRC" id="2WAO9Y$lDyM" role="NXodf">
          <property role="NXePf" value="Item of Map" />
        </node>
        <node concept="2m7Kf5" id="2WAO9Y$lCrc" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="key" />
          <node concept="2m5ndX" id="2WAO9Y$lCrg" role="2m7DVh" />
        </node>
        <node concept="2m7Kf5" id="2WAO9Y$lCrj" role="2m0hLx">
          <property role="2m7DUN" value="2" />
          <property role="TrG5h" value="value" />
          <node concept="3BlaRf" id="2WAO9Y$lCrp" role="2m7DVh">
            <ref role="3BrLez" node="2WAO9Y$lyRS" resolve="RawValue" />
          </node>
        </node>
      </node>
      <node concept="2m5naR" id="2WAO9Y$lCqr" role="2m5mJr">
        <property role="TrG5h" value="MapValue" />
        <property role="tsOgz" value="false" />
        <property role="w4tQU" value="true" />
        <ref role="w4$XZ" node="2WAO9Y$lyRS" resolve="RawValue" />
        <node concept="2m7Kf5" id="2WAO9Y$lCYY" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <property role="TrG5h" value="items" />
          <node concept="2m5nlk" id="2WAO9Y$lCZ2" role="2m7DVh">
            <node concept="2m5mGg" id="2WAO9Y$lCZ8" role="3GJlyp">
              <ref role="2m5mJy" node="2WAO9Y$lCqX" resolve="MapValueItem" />
            </node>
          </node>
        </node>
        <node concept="NXeRC" id="2WAO9Y$lE6r" role="NXodf">
          <property role="NXePf" value="Map Value" />
        </node>
        <node concept="Nu42z" id="6vhvPrvFThv" role="3BtCOu">
          <property role="Nu42W" value="06" />
        </node>
      </node>
      <node concept="1Dx9M1" id="2WAO9Y$lrKc" role="1Dx9rD">
        <property role="1Dx9K7" value="Flexible raw collections without structure" />
      </node>
    </node>
  </node>
  <node concept="2m5nkH" id="5HzswaJYSei">
    <property role="TrG5h" value="ActorApiExtension" />
    <property role="u_6dX" value="0.1" />
    <property role="3BlOl8" value="im.actor.extension.api" />
    <property role="WhUdw" value="im.actor.extension.api" />
    <node concept="2m5mJO" id="64HNz1Ipac2" role="2m5lHt">
      <node concept="2m5naR" id="64HNz1Ipac6" role="2m5mJr">
        <property role="TrG5h" value="test" />
        <node concept="2m7Kf5" id="64HNz1Ipac9" role="2m0hLx">
          <property role="2m7DUN" value="1" />
          <node concept="2m5mGg" id="64HNz1Ipacd" role="2m7DVh">
            <ref role="2m5mJy" node="GBscvBB6fx" resolve="Peer" />
          </node>
        </node>
      </node>
    </node>
  </node>
</model>

