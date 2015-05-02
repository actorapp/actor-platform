var $wnd = $wnd || window.parent;
var __gwtModuleFunction = $wnd.ActorMessenger;
var $sendStats = __gwtModuleFunction.__sendStats;
$sendStats('moduleStartup', 'moduleEvalStart');
var $gwt_version = "2.7.0";
var $strongName = 'A1014B69E9C0789C4F7EA37632293315';
var $gwt = {};
var $doc = $wnd.document;
var $moduleName, $moduleBase;
function __gwtStartLoadingFragment(frag) {
var fragFile = 'deferredjs/' + $strongName + '/' + frag + '.cache.js';
return __gwtModuleFunction.__startLoadingFragment(fragFile);
}
function __gwtInstallCode(code) {return __gwtModuleFunction.__installRunAsyncCode(code);}
function __gwt_isKnownPropertyValue(propName, propValue) {
return __gwtModuleFunction.__gwt_isKnownPropertyValue(propName, propValue);
}
function __gwt_getMetaProperty(name) {
return __gwtModuleFunction.__gwt_getMetaProperty(name);
}
var $stats = $wnd.__gwtStatsEvent ? function(a) {
return $wnd.__gwtStatsEvent && $wnd.__gwtStatsEvent(a);
} : null;
var $sessionId = $wnd.__gwtStatsSessionId ? $wnd.__gwtStatsSessionId : null;
var $intern_0 = 2147483647, $intern_1 = {3:1, 13:1}, $intern_2 = {3:1, 14:1, 13:1}, $intern_3 = {3:1, 6:1, 759:1}, $intern_4 = {3:1}, $intern_5 = 65535, $intern_6 = {3:1, 6:1}, $intern_7 = 4194303, $intern_8 = 1048575, $intern_9 = 524288, $intern_10 = 4194304, $intern_11 = 17592186044416, $intern_12 = -9223372036854775808, $intern_13 = {3:1, 6:1, 470:1}, $intern_14 = {4:1}, $intern_15 = {87:1, 4:1}, $intern_16 = {46:1, 4:1}, $intern_17 = {36:1, 3:1}, $intern_18 = 1048576, $intern_19 = {4:1, 149:1}, $intern_20 = {30:1}, $intern_21 = {3:1, 221:1, 6:1}, $intern_22 = {3:1, 91:1, 6:1}, $intern_23 = {336:1, 3:1, 6:1}, $intern_24 = {435:1, 3:1, 6:1}, $intern_25 = {32:1}, $intern_26 = 5.9604644775390625E-8, $intern_27 = {472:1, 3:1, 6:1}, $intern_28 = 65536, $intern_29 = -2147483648, $intern_30 = 1.52587890625E-5, $intern_31 = 9.5367431640625E-7, $intern_32 = 2.220446049250313E-16, $intern_33 = 1073741824, $intern_34 = {148:1}, $intern_35 = {92:1}, $intern_36 = {118:1, 20:1}, $intern_37 = {3:1, 43:1, 434:1}, $intern_38 = {3:1, 148:1}, $intern_39 = 15525485, $intern_40 = 16777216, $intern_41 = {3:1, 29:1, 18:1, 76:1};
var _, initFnList_0, prototypesByTypeId_0 = {}, permutationId = -1;
function typeMarkerFn(){
}

function portableObjCreate(obj){
  function F(){
  }

  ;
  F.prototype = obj || {};
  return new F;
}

function modernizeBrowser(){
  !Array.isArray && (Array.isArray = function(vArg){
    return Object.prototype.toString.call(vArg) === '[object Array]';
  }
  );
}

function maybeGetClassLiteralFromPlaceHolder_0(entry){
  return entry instanceof Array?entry[0]:null;
}

function emptyMethod(){
}

function defineClass(typeId, superTypeId, castableTypeMap){
  var prototypesByTypeId = prototypesByTypeId_0;
  var createSubclassPrototype = createSubclassPrototype_0;
  var maybeGetClassLiteralFromPlaceHolder = maybeGetClassLiteralFromPlaceHolder_0;
  var prototype_0 = prototypesByTypeId[typeId];
  var clazz = maybeGetClassLiteralFromPlaceHolder(prototype_0);
  if (prototype_0 && !clazz) {
    _ = prototype_0;
  }
   else {
    _ = prototypesByTypeId[typeId] = !superTypeId?{}:createSubclassPrototype(superTypeId);
    _.castableTypeMap$ = castableTypeMap;
    _.constructor = _;
    !superTypeId && (_.typeMarker$ = typeMarkerFn);
  }
  for (var i_0 = 3; i_0 < arguments.length; ++i_0) {
    arguments[i_0].prototype = _;
  }
  clazz && (_.___clazz$ = clazz);
}

function createSubclassPrototype_0(superTypeId){
  var prototypesByTypeId = prototypesByTypeId_0;
  return portableObjCreate(prototypesByTypeId[superTypeId]);
}

function setGwtProperty(propertyName, propertyValue){
  typeof window === 'object' && typeof window['$gwt'] === 'object' && (window['$gwt'][propertyName] = propertyValue);
}

function registerEntry(){
  return entry_0;
}

function gwtOnLoad_0(errFn, modName, modBase, softPermutationId){
  ensureModuleInit();
  var initFnList = initFnList_0;
  $moduleName = modName;
  $moduleBase = modBase;
  permutationId = softPermutationId;
  function initializeModules(){
    for (var i_0 = 0; i_0 < initFnList.length; i_0++) {
      initFnList[i_0]();
    }
  }

  if (errFn) {
    try {
      $entry(initializeModules)();
    }
     catch (e) {
      errFn(modName, e);
    }
  }
   else {
    $entry(initializeModules)();
  }
}

function ensureModuleInit(){
  initFnList_0 == null && (initFnList_0 = []);
}

function addInitFunctions(){
  ensureModuleInit();
  var initFnList = initFnList_0;
  for (var i_0 = 0; i_0 < arguments.length; i_0++) {
    initFnList.push(arguments[i_0]);
  }
}

function Object_0(){
}

function equals_Ljava_lang_Object__Z__devirtual$(this$static, other){
  return isJavaString(this$static)?$equals_3(this$static, other):hasJavaObjectVirtualDispatch(this$static)?this$static.equals$(other):isJavaArray(this$static)?this$static === other:this$static === other;
}

function getClass__Ljava_lang_Class___devirtual$(this$static){
  return isJavaString(this$static)?Ljava_lang_String_2_classLit:hasJavaObjectVirtualDispatch(this$static)?this$static.___clazz$:isJavaArray(this$static)?this$static.___clazz$:Lcom_google_gwt_core_client_JavaScriptObject_2_classLit;
}

function hashCode__I__devirtual$(this$static){
  return isJavaString(this$static)?getHashCode_0(this$static):hasJavaObjectVirtualDispatch(this$static)?this$static.hashCode$():isJavaArray(this$static)?getHashCode(this$static):getHashCode(this$static);
}

defineClass(1, null, {}, Object_0);
_.equals$ = function equals(other){
  return this === other;
}
;
_.getClass$ = function getClass_0(){
  return this.___clazz$;
}
;
_.hashCode$ = function hashCode_0(){
  return getHashCode(this);
}
;
_.toString$ = function toString_0(){
  return $getName_0(getClass__Ljava_lang_Class___devirtual$(this)) + '@' + toUnsignedRadixString(hashCode__I__devirtual$(this), 16);
}
;
_.toString = function(){
  return this.toString$();
}
;
stringCastMap = {3:1, 801:1, 29:1, 2:1};
modernizeBrowser();
function canCast(src_0, dstId){
  return isJavaString(src_0) && !!stringCastMap[dstId] || src_0.castableTypeMap$ && !!src_0.castableTypeMap$[dstId];
}

function charToString(x_0){
  return String.fromCharCode(x_0);
}

function dynamicCast(src_0, dstId){
  if (src_0 != null && !canCast(src_0, dstId)) {
    throw new ClassCastException;
  }
  return src_0;
}

function dynamicCastJso(src_0){
  if (src_0 != null && !(!isJavaString(src_0) && !hasTypeMarker(src_0))) {
    throw new ClassCastException;
  }
  return src_0;
}

function dynamicCastToString(src_0){
  if (src_0 != null && !isJavaString(src_0)) {
    throw new ClassCastException;
  }
  return src_0;
}

function hasJavaObjectVirtualDispatch(src_0){
  return !instanceofArray(src_0) && hasTypeMarker(src_0);
}

function instanceOf(src_0, dstId){
  return src_0 != null && canCast(src_0, dstId);
}

function instanceOfJso(src_0){
  return src_0 != null && !isJavaString(src_0) && !hasTypeMarker(src_0);
}

function instanceofArray(src_0){
  return Array.isArray(src_0);
}

function isJavaArray(src_0){
  return instanceofArray(src_0) && hasTypeMarker(src_0);
}

function isJavaString(src_0){
  return typeof src_0 === 'string';
}

function maskUndefined(src_0){
  return src_0 == null?null:src_0;
}

function narrow_byte(x_0){
  return x_0 << 24 >> 24;
}

function round_int(x_0){
  return ~~Math.max(Math.min(x_0, $intern_0), -2147483648);
}

function throwClassCastExceptionUnlessNull(o){
  if (o != null) {
    throw new ClassCastException;
  }
  return null;
}

var stringCastMap;
function $ensureNamesAreInitialized(this$static){
  if (this$static.typeName != null) {
    return;
  }
  initializeNames(this$static);
}

function $getName_0(this$static){
  $ensureNamesAreInitialized(this$static);
  return this$static.typeName;
}

function $getSimpleName(this$static){
  $ensureNamesAreInitialized(this$static);
  return this$static.simpleName;
}

function Class(){
  ++nextSequentialId;
  this.typeName = null;
  this.simpleName = null;
  this.packageName = null;
  this.compoundName = null;
  this.canonicalName = null;
  this.typeId = null;
  this.arrayLiterals = null;
}

function createClassObject(packageName, compoundClassName){
  var clazz;
  clazz = new Class;
  clazz.packageName = packageName;
  clazz.compoundName = compoundClassName;
  return clazz;
}

function createForClass(packageName, compoundClassName, typeId, superclass){
  var clazz;
  clazz = createClassObject(packageName, compoundClassName);
  maybeSetClassLiteral(typeId, clazz);
  clazz.superclass = superclass;
  return clazz;
}

function createForEnum(packageName, compoundClassName, typeId, superclass, enumConstantsFunc){
  var clazz;
  clazz = createClassObject(packageName, compoundClassName);
  maybeSetClassLiteral(typeId, clazz);
  clazz.modifiers = enumConstantsFunc?8:0;
  clazz.superclass = superclass;
  return clazz;
}

function createForInterface(packageName, compoundClassName){
  var clazz;
  clazz = createClassObject(packageName, compoundClassName);
  clazz.modifiers = 2;
  return clazz;
}

function createForPrimitive(className, primitiveTypeId){
  var clazz;
  clazz = createClassObject('', className);
  clazz.typeId = primitiveTypeId;
  clazz.modifiers = 1;
  return clazz;
}

function getClassLiteralForArray_0(leafClass, dimensions){
  var arrayLiterals = leafClass.arrayLiterals = leafClass.arrayLiterals || [];
  return arrayLiterals[dimensions] || (arrayLiterals[dimensions] = leafClass.createClassLiteralForArray(dimensions));
}

function getPrototypeForClass(clazz){
  if (clazz.isPrimitive()) {
    return null;
  }
  var typeId = clazz.typeId;
  var prototype_0 = prototypesByTypeId_0[typeId];
  return prototype_0;
}

function initializeNames(clazz){
  if (clazz.isArray_0()) {
    var componentType = clazz.componentType;
    componentType.isPrimitive()?(clazz.typeName = '[' + componentType.typeId):!componentType.isArray_0()?(clazz.typeName = '[L' + componentType.getName() + ';'):(clazz.typeName = '[' + componentType.getName());
    clazz.canonicalName = componentType.getCanonicalName() + '[]';
    clazz.simpleName = componentType.getSimpleName() + '[]';
    return;
  }
  var packageName = clazz.packageName;
  var compoundName = clazz.compoundName;
  compoundName = compoundName.split('/');
  clazz.typeName = join_0('.', [packageName, join_0('$', compoundName)]);
  clazz.canonicalName = join_0('.', [packageName, join_0('.', compoundName)]);
  clazz.simpleName = compoundName[compoundName.length - 1];
}

function join_0(separator, strings){
  var i_0 = 0;
  while (!strings[i_0] || strings[i_0] == '') {
    i_0++;
  }
  var result = strings[i_0++];
  for (; i_0 < strings.length; i_0++) {
    if (!strings[i_0] || strings[i_0] == '') {
      continue;
    }
    result += separator + strings[i_0];
  }
  return result;
}

function maybeSetClassLiteral(typeId, clazz){
  var proto;
  if (!typeId) {
    return;
  }
  clazz.typeId = typeId;
  var prototype_0 = getPrototypeForClass(clazz);
  if (!prototype_0) {
    prototypesByTypeId_0[typeId] = [clazz];
    return;
  }
  prototype_0.___clazz$ = clazz;
}

defineClass(153, 1, {153:1}, Class);
_.createClassLiteralForArray = function createClassLiteralForArray(dimensions){
  var clazz;
  clazz = new Class;
  clazz.modifiers = 4;
  clazz.superclass = Ljava_lang_Object_2_classLit;
  dimensions > 1?(clazz.componentType = getClassLiteralForArray_0(this, dimensions - 1)):(clazz.componentType = this);
  return clazz;
}
;
_.getCanonicalName = function getCanonicalName(){
  $ensureNamesAreInitialized(this);
  return this.canonicalName;
}
;
_.getName = function getName(){
  return $getName_0(this);
}
;
_.getSimpleName = function getSimpleName(){
  return $getSimpleName(this);
}
;
_.isArray_0 = function isArray(){
  return (this.modifiers & 4) != 0;
}
;
_.isPrimitive = function isPrimitive_0(){
  return (this.modifiers & 1) != 0;
}
;
_.toString$ = function toString_178(){
  return ((this.modifiers & 2) != 0?'interface ':(this.modifiers & 1) != 0?'':'class ') + ($ensureNamesAreInitialized(this) , this.typeName);
}
;
_.modifiers = 0;
var nextSequentialId = 1;
var Ljava_lang_Object_2_classLit = createForClass('java.lang', 'Object', 1, null), Lcom_google_gwt_core_client_JavaScriptObject_2_classLit = createForClass('com.google.gwt.core.client', 'JavaScriptObject$', 0, Ljava_lang_Object_2_classLit), Ljava_lang_Class_2_classLit = createForClass('java.lang', 'Class', 153, Ljava_lang_Object_2_classLit);
function Duration(){
  this.start_0 = now_1();
}

defineClass(516, 1, {}, Duration);
_.start_0 = 0;
var Lcom_google_gwt_core_client_Duration_2_classLit = createForClass('com.google.gwt.core.client', 'Duration', 516, Ljava_lang_Object_2_classLit);
function isScript(){
  return true;
}

function setUncaughtExceptionHandler(handler){
  uncaughtExceptionHandler = handler;
}

var uncaughtExceptionHandler = null;
function $fillInStackTrace(this$static){
  this$static.stackTrace = null;
  captureStackTrace(this$static, this$static.detailMessage);
  return this$static;
}

function $printStackTrace(this$static, out){
  var element, element$array, element$index, element$max, t, stackTrace;
  for (t = this$static; t; t = t.cause) {
    t != this$static && out.print_0('Caused by: ');
    out.println(t);
    for (element$array = (t.stackTrace == null && (t.stackTrace = ($clinit_StackTraceCreator() , stackTrace = collector.getStackTrace(t) , dropInternalFrames(stackTrace))) , t.stackTrace) , element$index = 0 , element$max = element$array.length; element$index < element$max; ++element$index) {
      element = element$array[element$index];
      out.println_0('\tat ' + element);
    }
  }
}

defineClass(13, 1, $intern_1);
_.getMessage = function getMessage(){
  return this.detailMessage;
}
;
_.toString$ = function toString_1(){
  var className, msg;
  className = $getName_0(this.___clazz$);
  msg = this.getMessage();
  return msg != null?className + ': ' + msg:className;
}
;
var Ljava_lang_Throwable_2_classLit = createForClass('java.lang', 'Throwable', 13, Ljava_lang_Object_2_classLit);
function Exception(){
  $fillInStackTrace(this);
}

function Exception_0(message){
  this.detailMessage = message;
  $fillInStackTrace(this);
}

defineClass(14, 13, $intern_2);
var Ljava_lang_Exception_2_classLit = createForClass('java.lang', 'Exception', 14, Ljava_lang_Throwable_2_classLit);
function RuntimeException(){
  Exception.call(this);
}

function RuntimeException_0(message){
  Exception_0.call(this, message);
}

defineClass(10, 14, $intern_2, RuntimeException, RuntimeException_0);
var Ljava_lang_RuntimeException_2_classLit = createForClass('java.lang', 'RuntimeException', 10, Ljava_lang_Exception_2_classLit);
defineClass(476, 10, $intern_2);
var Lcom_google_gwt_core_client_impl_JavaScriptExceptionBase_2_classLit = createForClass('com.google.gwt.core.client.impl', 'JavaScriptExceptionBase', 476, Ljava_lang_RuntimeException_2_classLit);
function $clinit_JavaScriptException(){
  $clinit_JavaScriptException = emptyMethod;
  NOT_SET = new Object_0;
}

function $ensureInit(this$static){
  var exception;
  if (this$static.message_0 == null) {
    exception = maskUndefined(this$static.e) === maskUndefined(NOT_SET)?null:this$static.e;
    this$static.name_0 = exception == null?'null':instanceOfJso(exception)?getExceptionName0(dynamicCastJso(exception)):isJavaString(exception)?'String':$getName_0(getClass__Ljava_lang_Class___devirtual$(exception));
    this$static.description = this$static.description + ': ' + (instanceOfJso(exception)?getExceptionDescription0(dynamicCastJso(exception)):exception + '');
    this$static.message_0 = '(' + this$static.name_0 + ') ' + this$static.description;
  }
}

function JavaScriptException(e){
  $clinit_JavaScriptException();
  this.cause = null;
  this.detailMessage = null;
  this.description = '';
  this.e = e;
  this.description = '';
}

function getExceptionDescription0(e){
  return e == null?null:e.message;
}

function getExceptionName0(e){
  return e == null?null:e.name;
}

defineClass(79, 476, {79:1, 3:1, 14:1, 13:1}, JavaScriptException);
_.getMessage = function getMessage_0(){
  $ensureInit(this);
  return this.message_0;
}
;
_.getThrown = function getThrown(){
  return maskUndefined(this.e) === maskUndefined(NOT_SET)?null:this.e;
}
;
var NOT_SET;
var Lcom_google_gwt_core_client_JavaScriptException_2_classLit = createForClass('com.google.gwt.core.client', 'JavaScriptException', 79, Lcom_google_gwt_core_client_impl_JavaScriptExceptionBase_2_classLit);
function $push(this$static, value_0){
  this$static[this$static.length] = value_0;
}

function create(milliseconds){
  return new Date(milliseconds);
}

function now_1(){
  if (Date.now) {
    return Date.now();
  }
  return (new Date).getTime();
}

defineClass(761, 1, {});
var Lcom_google_gwt_core_client_Scheduler_2_classLit = createForClass('com.google.gwt.core.client', 'Scheduler', 761, Ljava_lang_Object_2_classLit);
function apply_0(jsFunction, thisObj, args){
  return jsFunction.apply(thisObj, args);
  var __0;
}

function enter(){
  var now_0;
  if (entryDepth != 0) {
    now_0 = now_1();
    if (now_0 - watchdogEntryDepthLastScheduled > 2000) {
      watchdogEntryDepthLastScheduled = now_0;
      watchdogEntryDepthTimerId = $wnd.setTimeout(watchdogEntryDepthRun, 10);
    }
  }
  if (entryDepth++ == 0) {
    $flushEntryCommands(($clinit_SchedulerImpl() , INSTANCE));
    return true;
  }
  return false;
}

function entry_0(jsFunction){
  return function(){
    if (isScript()) {
      return entry0(jsFunction, this, arguments);
    }
     else {
      var __0 = entry0(jsFunction, this, arguments);
      __0 != null && (__0 = __0.val);
      return __0;
    }
  }
  ;
}

function entry0(jsFunction, thisObj, args){
  var initialEntry, t;
  initialEntry = enter();
  try {
    if (uncaughtExceptionHandler) {
      try {
        return apply_0(jsFunction, thisObj, args);
      }
       catch ($e0) {
        $e0 = wrap($e0);
        if (instanceOf($e0, 13)) {
          t = $e0;
          reportUncaughtException(t);
          return undefined;
        }
         else 
          throw unwrap($e0);
      }
    }
     else {
      return apply_0(jsFunction, thisObj, args);
    }
  }
   finally {
    exit(initialEntry);
  }
}

function exit(initialEntry){
  initialEntry && $flushFinallyCommands(($clinit_SchedulerImpl() , INSTANCE));
  --entryDepth;
  if (initialEntry) {
    if (watchdogEntryDepthTimerId != -1) {
      watchdogEntryDepthCancel(watchdogEntryDepthTimerId);
      watchdogEntryDepthTimerId = -1;
    }
  }
}

function getHashCode(o){
  return o.$H || (o.$H = ++sNextHashId);
}

function reportToBrowser(e){
  $wnd.setTimeout(function(){
    throw e;
  }
  , 0);
}

function reportUncaughtException(e){
  var handler;
  handler = uncaughtExceptionHandler;
  if (handler) {
    if (handler == uncaughtExceptionHandlerForTest) {
      return;
    }
    $log_1(handler.val$log2, ($clinit_Level() , e.getMessage()), e);
    return;
  }
  reportToBrowser(instanceOf(e, 79)?dynamicCast(e, 79).getThrown():e);
}

function watchdogEntryDepthCancel(timerId){
  $wnd.clearTimeout(timerId);
}

function watchdogEntryDepthRun(){
  entryDepth != 0 && (entryDepth = 0);
  watchdogEntryDepthTimerId = -1;
}

var entryDepth = 0, sNextHashId = 0, uncaughtExceptionHandlerForTest, watchdogEntryDepthLastScheduled = 0, watchdogEntryDepthTimerId = -1;
function $clinit_SchedulerImpl(){
  $clinit_SchedulerImpl = emptyMethod;
  INSTANCE = new SchedulerImpl;
}

function $flushEntryCommands(this$static){
  var oldQueue, rescheduled;
  if (this$static.entryCommands) {
    rescheduled = null;
    do {
      oldQueue = this$static.entryCommands;
      this$static.entryCommands = null;
      rescheduled = runScheduledTasks(oldQueue, rescheduled);
    }
     while (this$static.entryCommands);
    this$static.entryCommands = rescheduled;
  }
}

function $flushFinallyCommands(this$static){
  var oldQueue, rescheduled;
  if (this$static.finallyCommands) {
    rescheduled = null;
    do {
      oldQueue = this$static.finallyCommands;
      this$static.finallyCommands = null;
      rescheduled = runScheduledTasks(oldQueue, rescheduled);
    }
     while (this$static.finallyCommands);
    this$static.finallyCommands = rescheduled;
  }
}

function $flushPostEventPumpCommands(this$static){
  var oldDeferred;
  if (this$static.deferredCommands) {
    oldDeferred = this$static.deferredCommands;
    this$static.deferredCommands = null;
    !this$static.incrementalCommands && (this$static.incrementalCommands = []);
    runScheduledTasks(oldDeferred, this$static.incrementalCommands);
  }
  !!this$static.incrementalCommands && (this$static.incrementalCommands = $runRepeatingTasks(this$static.incrementalCommands));
}

function $isWorkQueued(this$static){
  return !!this$static.deferredCommands || !!this$static.incrementalCommands;
}

function $maybeSchedulePostEventPumpCommands(this$static){
  if (!this$static.shouldBeRunning) {
    this$static.shouldBeRunning = true;
    !this$static.flusher && (this$static.flusher = new SchedulerImpl$Flusher(this$static));
    scheduleFixedDelayImpl(this$static.flusher, 1);
    !this$static.rescue && (this$static.rescue = new SchedulerImpl$Rescuer(this$static));
    scheduleFixedDelayImpl(this$static.rescue, 50);
  }
}

function $runRepeatingTasks(tasks){
  var canceledSomeTasks, duration, executedSomeTask, i_0, length_0, newTasks, t;
  length_0 = tasks.length;
  if (length_0 == 0) {
    return null;
  }
  canceledSomeTasks = false;
  duration = new Duration;
  while (now_1() - duration.start_0 < 16) {
    executedSomeTask = false;
    for (i_0 = 0; i_0 < length_0; i_0++) {
      t = tasks[i_0];
      if (!t) {
        continue;
      }
      executedSomeTask = true;
      if (!t[0].execute()) {
        tasks[i_0] = null;
        canceledSomeTasks = true;
      }
    }
    if (!executedSomeTask) {
      break;
    }
  }
  if (canceledSomeTasks) {
    newTasks = [];
    for (i_0 = 0; i_0 < length_0; i_0++) {
      !!tasks[i_0] && $push(newTasks, tasks[i_0]);
    }
    return newTasks.length == 0?null:newTasks;
  }
   else {
    return tasks;
  }
}

function $scheduleDeferred(this$static, cmd){
  this$static.deferredCommands = push_1(this$static.deferredCommands, [cmd, false]);
  $maybeSchedulePostEventPumpCommands(this$static);
}

function $scheduleIncremental(this$static, cmd){
  this$static.deferredCommands = push_1(this$static.deferredCommands, [cmd, true]);
  $maybeSchedulePostEventPumpCommands(this$static);
}

function SchedulerImpl(){
}

function execute(cmd){
  return cmd.execute();
}

function push_1(queue, task){
  !queue && (queue = []);
  $push(queue, task);
  return queue;
}

function runScheduledTasks(tasks, rescheduled){
  var e, i_0, j, t;
  for (i_0 = 0 , j = tasks.length; i_0 < j; i_0++) {
    t = tasks[i_0];
    try {
      t[1]?t[0].execute() && (rescheduled = push_1(rescheduled, t)):t[0].execute_0();
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 13)) {
        e = $e0;
        reportUncaughtException(e);
      }
       else 
        throw unwrap($e0);
    }
  }
  return rescheduled;
}

function scheduleFixedDelayImpl(cmd, delayMs){
  $clinit_SchedulerImpl();
  function callback(){
    var ret = $entry(execute)(cmd);
    !isScript() && (ret = ret == true);
    ret && $wnd.setTimeout(callback, delayMs);
  }

  $wnd.setTimeout(callback, delayMs);
}

defineClass(504, 761, {}, SchedulerImpl);
_.flushRunning = false;
_.shouldBeRunning = false;
var INSTANCE;
var Lcom_google_gwt_core_client_impl_SchedulerImpl_2_classLit = createForClass('com.google.gwt.core.client.impl', 'SchedulerImpl', 504, Lcom_google_gwt_core_client_Scheduler_2_classLit);
function SchedulerImpl$Flusher(this$0){
  this.this$01 = this$0;
}

defineClass(505, 1, {}, SchedulerImpl$Flusher);
_.execute = function execute_0(){
  this.this$01.flushRunning = true;
  $flushPostEventPumpCommands(this.this$01);
  this.this$01.flushRunning = false;
  return this.this$01.shouldBeRunning = $isWorkQueued(this.this$01);
}
;
var Lcom_google_gwt_core_client_impl_SchedulerImpl$Flusher_2_classLit = createForClass('com.google.gwt.core.client.impl', 'SchedulerImpl/Flusher', 505, Ljava_lang_Object_2_classLit);
function SchedulerImpl$Rescuer(this$0){
  this.this$01 = this$0;
}

defineClass(506, 1, {}, SchedulerImpl$Rescuer);
_.execute = function execute_1(){
  this.this$01.flushRunning && scheduleFixedDelayImpl(this.this$01.flusher, 1);
  return this.this$01.shouldBeRunning;
}
;
var Lcom_google_gwt_core_client_impl_SchedulerImpl$Rescuer_2_classLit = createForClass('com.google.gwt.core.client.impl', 'SchedulerImpl/Rescuer', 506, Ljava_lang_Object_2_classLit);
function $clinit_StackTraceCreator(){
  $clinit_StackTraceCreator = emptyMethod;
  var c, enforceLegacy;
  enforceLegacy = !(!!Error.stackTraceLimit || 'stack' in new Error);
  c = new StackTraceCreator$CollectorModernNoSourceMap;
  collector = enforceLegacy?new StackTraceCreator$CollectorLegacy:c;
}

function captureStackTrace(throwable, reference){
  $clinit_StackTraceCreator();
  collector.collect(throwable, reference);
}

function dropInternalFrames(stackTrace){
  var dropFrameUntilFnName, i_0, numberOfFrameToSearch;
  dropFrameUntilFnName = 'captureStackTrace';
  numberOfFrameToSearch = min_0(stackTrace.length, 5);
  for (i_0 = 0; i_0 < numberOfFrameToSearch; i_0++) {
    if ($equals_3(stackTrace[i_0].methodName, dropFrameUntilFnName)) {
      return dynamicCast((stackTrace.length >= i_0 + 1 && stackTrace.splice(0, i_0 + 1) , stackTrace), 759);
    }
  }
  return stackTrace;
}

function extractFunctionName(fnName){
  var fnRE = /function(?:\s+([\w$]+))?\s*\(/;
  var match_0 = fnRE.exec(fnName);
  return match_0 && match_0[1] || 'anonymous';
}

function parseInt_0(number){
  $clinit_StackTraceCreator();
  return parseInt(number) || -1;
}

var collector;
defineClass(772, 1, {});
var Lcom_google_gwt_core_client_impl_StackTraceCreator$Collector_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/Collector', 772, Ljava_lang_Object_2_classLit);
function StackTraceCreator$CollectorLegacy(){
}

defineClass(477, 772, {}, StackTraceCreator$CollectorLegacy);
_.collect = function collect(t, thrownIgnored){
  var seen = {}, name_1;
  t.fnStack = [];
  var callee = arguments.callee.caller;
  while (callee) {
    var name_0 = ($clinit_StackTraceCreator() , callee.name || (callee.name = extractFunctionName(callee.toString())));
    t.fnStack.push(name_0);
    var keyName = ':' + name_0;
    var withThisName = seen[keyName];
    if (withThisName) {
      var i_0, j;
      for (i_0 = 0 , j = withThisName.length; i_0 < j; i_0++) {
        if (withThisName[i_0] === callee) {
          return;
        }
      }
    }
    (withThisName || (seen[keyName] = [])).push(callee);
    callee = callee.caller;
  }
}
;
_.getStackTrace = function getStackTrace(t){
  var i_0, length_0, stack_0, stackTrace;
  stack_0 = ($clinit_StackTraceCreator() , t && t.fnStack && t.fnStack instanceof Array?t.fnStack:[]);
  length_0 = stack_0.length;
  stackTrace = initDim(Ljava_lang_StackTraceElement_2_classLit, $intern_3, 80, length_0, 0, 1);
  for (i_0 = 0; i_0 < length_0; i_0++) {
    stackTrace[i_0] = new StackTraceElement(stack_0[i_0], null, -1);
  }
  return stackTrace;
}
;
var Lcom_google_gwt_core_client_impl_StackTraceCreator$CollectorLegacy_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/CollectorLegacy', 477, Lcom_google_gwt_core_client_impl_StackTraceCreator$Collector_2_classLit);
function $clinit_StackTraceCreator$CollectorModern(){
  $clinit_StackTraceCreator$CollectorModern = emptyMethod;
  Error.stackTraceLimit = 64;
}

function $parse(this$static, stString){
  var closeParen, col, endFileUrlIndex, fileName, index_0, lastColonIndex, line, location_0, toReturn;
  if (!stString.length) {
    return this$static.createSte('Unknown', 'anonymous', -1, -1);
  }
  toReturn = $trim(stString);
  $equals_3(toReturn.substr(0, 3), 'at ') && (toReturn = __substr(toReturn, 3, toReturn.length - 3));
  toReturn = toReturn.replace(/\[.*?\]/g, '');
  index_0 = toReturn.indexOf('(');
  if (index_0 == -1) {
    index_0 = toReturn.indexOf('@');
    if (index_0 == -1) {
      location_0 = toReturn;
      toReturn = '';
    }
     else {
      location_0 = $trim(__substr(toReturn, index_0 + 1, toReturn.length - (index_0 + 1)));
      toReturn = $trim(toReturn.substr(0, index_0));
    }
  }
   else {
    closeParen = toReturn.indexOf(')', index_0);
    location_0 = toReturn.substr(index_0 + 1, closeParen - (index_0 + 1));
    toReturn = $trim(toReturn.substr(0, index_0));
  }
  index_0 = $indexOf(toReturn, fromCodePoint(46));
  index_0 != -1 && (toReturn = __substr(toReturn, index_0 + 1, toReturn.length - (index_0 + 1)));
  (!toReturn.length || $equals_3(toReturn, 'Anonymous function')) && (toReturn = 'anonymous');
  lastColonIndex = $lastIndexOf(location_0, fromCodePoint(58));
  endFileUrlIndex = $lastIndexOf_0(location_0, fromCodePoint(58), lastColonIndex - 1);
  line = -1;
  col = -1;
  fileName = 'Unknown';
  if (lastColonIndex != -1 && endFileUrlIndex != -1) {
    fileName = location_0.substr(0, endFileUrlIndex);
    line = parseInt_0(location_0.substr(endFileUrlIndex + 1, lastColonIndex - (endFileUrlIndex + 1)));
    col = parseInt_0(__substr(location_0, lastColonIndex + 1, location_0.length - (lastColonIndex + 1)));
  }
  return this$static.createSte(fileName, toReturn, line, col);
}

defineClass(773, 772, {});
_.collect = function collect_0(t, jsThrown){
  function fixIE(e){
    if (!('stack' in e)) {
      try {
        throw e;
      }
       catch (ignored) {
      }
    }
    return e;
  }

  var backingJsError;
  typeof jsThrown == 'string'?(backingJsError = fixIE(new Error(jsThrown))):jsThrown instanceof Object && 'stack' in jsThrown?(backingJsError = jsThrown):(backingJsError = fixIE(new Error));
  t.__gwt$backingJsError = backingJsError;
}
;
_.createSte = function createSte(fileName, method, line, col){
  return new StackTraceElement(method, fileName + '@' + col, line < 0?-1:line);
}
;
_.getStackTrace = function getStackTrace_0(t){
  var addIndex, i_0, length_0, stack_0, stackTrace, ste, e;
  stack_0 = ($clinit_StackTraceCreator() , e = t.__gwt$backingJsError , e && e.stack?e.stack.split('\n'):[]);
  stackTrace = initDim(Ljava_lang_StackTraceElement_2_classLit, $intern_3, 80, 0, 0, 1);
  addIndex = 0;
  length_0 = stack_0.length;
  if (length_0 == 0) {
    return stackTrace;
  }
  ste = $parse(this, stack_0[0]);
  $equals_3(ste.methodName, 'anonymous') || (stackTrace[addIndex++] = ste);
  for (i_0 = 1; i_0 < length_0; i_0++) {
    stackTrace[addIndex++] = $parse(this, stack_0[i_0]);
  }
  return stackTrace;
}
;
var Lcom_google_gwt_core_client_impl_StackTraceCreator$CollectorModern_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/CollectorModern', 773, Lcom_google_gwt_core_client_impl_StackTraceCreator$Collector_2_classLit);
function StackTraceCreator$CollectorModernNoSourceMap(){
  $clinit_StackTraceCreator$CollectorModern();
}

defineClass(478, 773, {}, StackTraceCreator$CollectorModernNoSourceMap);
_.createSte = function createSte_0(fileName, method, line, col){
  return new StackTraceElement(method, fileName, -1);
}
;
var Lcom_google_gwt_core_client_impl_StackTraceCreator$CollectorModernNoSourceMap_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/CollectorModernNoSourceMap', 478, Lcom_google_gwt_core_client_impl_StackTraceCreator$CollectorModern_2_classLit);
function checkArrayType(expression, errorMessage){
  if (!expression) {
    throw new ArrayStoreException_0('' + errorMessage);
  }
}

function checkCriticalArgument(expression){
  if (!expression) {
    throw new IllegalArgumentException;
  }
}

function checkCriticalArgument_0(expression, errorMessage){
  if (!expression) {
    throw new IllegalArgumentException_0('' + errorMessage);
  }
}

function checkCriticalArgument_1(expression, errorMessageArgs){
  if (!expression) {
    throw new IllegalArgumentException_0(format('%s > %s', errorMessageArgs));
  }
}

function checkCriticalElement(expression){
  if (!expression) {
    throw new NoSuchElementException;
  }
}

function checkElementIndex(index_0, size_0){
  if (index_0 < 0 || index_0 >= size_0) {
    throw new IndexOutOfBoundsException_0('Index: ' + index_0 + ', Size: ' + size_0);
  }
}

function checkNotNull(reference){
  if (reference == null) {
    throw new NullPointerException;
  }
  return reference;
}

function checkNotNull_0(reference, errorMessage){
  if (reference == null) {
    throw new NullPointerException_0('' + errorMessage);
  }
}

function checkPositionIndex(index_0, size_0){
  if (index_0 < 0 || index_0 > size_0) {
    throw new IndexOutOfBoundsException_0('Index: ' + index_0 + ', Size: ' + size_0);
  }
}

function checkState(expression){
  if (!expression) {
    throw new IllegalStateException;
  }
}

function format(template, args){
  var builder, i_0, placeholderStart, templateStart;
  template = '' + template;
  builder = new StringBuilder_0(template.length + 16 * args.length);
  templateStart = 0;
  i_0 = 0;
  while (i_0 < args.length) {
    placeholderStart = template.indexOf('%s', templateStart);
    if (placeholderStart == -1) {
      break;
    }
    $append_1(builder, template.substr(templateStart, placeholderStart - templateStart));
    $append_0(builder, args[i_0++]);
    templateStart = placeholderStart + 2;
  }
  $append_1(builder, __substr(template, templateStart, template.length - templateStart));
  if (i_0 < args.length) {
    builder.string += ' [';
    $append_0(builder, args[i_0++]);
    while (i_0 < args.length) {
      builder.string += ', ';
      $append_0(builder, args[i_0++]);
    }
    builder.string += ']';
  }
  return builder.string;
}

function throwIfNull(value_0){
  if (null == value_0) {
    throw new NullPointerException_0('encodedURLComponent cannot be null');
  }
}

function $clinit_DateTimeFormat(){
  $clinit_DateTimeFormat = emptyMethod;
  new HashMap;
}

function $addPart(this$static, buf, count){
  var oldLength;
  if (buf.string.length > 0) {
    $add_0(this$static.patternParts, new DateTimeFormat$PatternPart(buf.string, count));
    oldLength = buf.string.length;
    0 < oldLength?(buf.string = $substring_0(buf.string, 0, 0)):0 > oldLength && (buf.string += valueOf_2(initDim(C_classLit, $intern_4, 0, -oldLength, 7, 1)));
  }
}

function $format(this$static, date, timeZone){
  var ch_0, diff, i_0, j, keepDate, keepTime, n, toAppendTo, trailQuote;
  !timeZone && (timeZone = createTimeZone(date.jsdate.getTimezoneOffset()));
  diff = (date.jsdate.getTimezoneOffset() - timeZone.standardOffset) * 60000;
  keepDate = new Date_1(add_0(fromDouble(date.jsdate.getTime()), fromInt(diff)));
  keepTime = keepDate;
  if (keepDate.jsdate.getTimezoneOffset() != date.jsdate.getTimezoneOffset()) {
    diff > 0?(diff -= 86400000):(diff += 86400000);
    keepTime = new Date_1(add_0(fromDouble(date.jsdate.getTime()), fromInt(diff)));
  }
  toAppendTo = new StringBuilder_0;
  n = this$static.pattern.length;
  for (i_0 = 0; i_0 < n;) {
    ch_0 = $charAt(this$static.pattern, i_0);
    if (ch_0 >= 97 && ch_0 <= 122 || ch_0 >= 65 && ch_0 <= 90) {
      for (j = i_0 + 1; j < n && $charAt(this$static.pattern, j) == ch_0; ++j)
      ;
      $subFormat(toAppendTo, ch_0, j - i_0, keepDate, keepTime, timeZone);
      i_0 = j;
    }
     else if (ch_0 == 39) {
      ++i_0;
      if (i_0 < n && $charAt(this$static.pattern, i_0) == 39) {
        toAppendTo.string += "'";
        ++i_0;
        continue;
      }
      trailQuote = false;
      while (!trailQuote) {
        j = i_0;
        while (j < n && $charAt(this$static.pattern, j) != 39) {
          ++j;
        }
        if (j >= n) {
          throw new IllegalArgumentException_0("Missing trailing '");
        }
        j + 1 < n && $charAt(this$static.pattern, j + 1) == 39?++j:(trailQuote = true);
        $append_1(toAppendTo, $substring_0(this$static.pattern, i_0, j));
        i_0 = j + 1;
      }
    }
     else {
      toAppendTo.string += charToString(ch_0);
      ++i_0;
    }
  }
  return toAppendTo.string;
}

function $formatFractionalSeconds(buf, count, date){
  var time, value_0;
  time = fromDouble(date.jsdate.getTime());
  if (lt(time, {l:0, m:0, h:0})) {
    value_0 = 1000 - toInt(mod(neg(time), {l:1000, m:0, h:0}));
    value_0 == 1000 && (value_0 = 0);
  }
   else {
    value_0 = toInt(mod(time, {l:1000, m:0, h:0}));
  }
  if (count == 1) {
    value_0 = ~~((value_0 + 50) / 100) < 9?~~((value_0 + 50) / 100):9;
    $append(buf, 48 + value_0 & $intern_5);
  }
   else if (count == 2) {
    value_0 = ~~((value_0 + 5) / 10) < 99?~~((value_0 + 5) / 10):99;
    $zeroPaddingNumber(buf, value_0, 2);
  }
   else {
    $zeroPaddingNumber(buf, value_0, 3);
    count > 3 && $zeroPaddingNumber(buf, 0, count - 3);
  }
}

function $formatMonth(buf, count, date){
  var value_0;
  value_0 = date.jsdate.getMonth();
  switch (count) {
    case 5:
      $append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['J', 'F', 'M', 'A', 'M', 'J', 'J', 'A', 'S', 'O', 'N', 'D'])[value_0]);
      break;
    case 4:
      $append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'])[value_0]);
      break;
    case 3:
      $append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'])[value_0]);
      break;
    default:$zeroPaddingNumber(buf, value_0 + 1, count);
  }
}

function $formatYear(buf, count, date){
  var value_0;
  value_0 = date.jsdate.getFullYear() - 1900 + 1900;
  value_0 < 0 && (value_0 = -value_0);
  switch (count) {
    case 1:
      buf.string += value_0;
      break;
    case 2:
      $zeroPaddingNumber(buf, value_0 % 100, 2);
      break;
    default:$zeroPaddingNumber(buf, value_0, count);
  }
}

function $getNextCharCountInPattern(start_0){
  var ch_0, next;
  ch_0 = 'HH:mm:ss.SSSS'.charCodeAt(start_0);
  next = start_0 + 1;
  while (next < 13 && 'HH:mm:ss.SSSS'.charCodeAt(next) == ch_0) {
    ++next;
  }
  return next - start_0;
}

function $identifyAbutStart(this$static){
  var abut, i_0, len;
  abut = false;
  len = this$static.patternParts.array.length;
  for (i_0 = 0; i_0 < len; i_0++) {
    if ($isNumeric(dynamicCast($get_3(this$static.patternParts, i_0), 175))) {
      if (!abut && i_0 + 1 < len && $isNumeric(dynamicCast($get_3(this$static.patternParts, i_0 + 1), 175))) {
        abut = true;
        dynamicCast($get_3(this$static.patternParts, i_0), 175).abutStart = true;
      }
    }
     else {
      abut = false;
    }
  }
}

function $isNumeric(part){
  var i_0;
  if (part.count <= 0) {
    return false;
  }
  i_0 = $indexOf('MLydhHmsSDkK', fromCodePoint(part.text_0.charCodeAt(0)));
  return i_0 > 1 || i_0 >= 0 && part.count < 3;
}

function $parsePattern(this$static){
  var buf, ch_0, count, i_0, inQuote;
  buf = new StringBuilder_0;
  inQuote = false;
  for (i_0 = 0; i_0 < 13; i_0++) {
    ch_0 = 'HH:mm:ss.SSSS'.charCodeAt(i_0);
    if (ch_0 == 32) {
      $addPart(this$static, buf, 0);
      buf.string += ' ';
      $addPart(this$static, buf, 0);
      while (i_0 + 1 < 13 && 'HH:mm:ss.SSSS'.charCodeAt(i_0 + 1) == 32) {
        ++i_0;
      }
      continue;
    }
    if (inQuote) {
      if (ch_0 == 39) {
        if (i_0 + 1 < 13 && 'HH:mm:ss.SSSS'.charCodeAt(i_0 + 1) == 39) {
          buf.string += "'";
          ++i_0;
        }
         else {
          inQuote = false;
        }
      }
       else {
        buf.string += charToString(ch_0);
      }
      continue;
    }
    if ($indexOf('GyMLdkHmsSEcDahKzZv', fromCodePoint(ch_0)) > 0) {
      $addPart(this$static, buf, 0);
      buf.string += charToString(ch_0);
      count = $getNextCharCountInPattern(i_0);
      $addPart(this$static, buf, count);
      i_0 += count - 1;
      continue;
    }
    if (ch_0 == 39) {
      if (i_0 + 1 < 13 && 'HH:mm:ss.SSSS'.charCodeAt(i_0 + 1) == 39) {
        buf.string += "'";
        ++i_0;
      }
       else {
        inQuote = true;
      }
    }
     else {
      buf.string += charToString(ch_0);
    }
  }
  $addPart(this$static, buf, 0);
  $identifyAbutStart(this$static);
}

function $subFormat(buf, ch_0, count, adjustedDate, adjustedTime, timezone){
  var value_0, value_1, value_2, value_3, value_4, value_5, value_6, value_7, value_8, value_9, value_10, value_11;
  switch (ch_0) {
    case 71:
      value_0 = adjustedDate.jsdate.getFullYear() - 1900 >= -1900?1:0;
      count >= 4?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Before Christ', 'Anno Domini'])[value_0]):$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['BC', 'AD'])[value_0]);
      break;
    case 121:
      $formatYear(buf, count, adjustedDate);
      break;
    case 77:
      $formatMonth(buf, count, adjustedDate);
      break;
    case 107:
      value_1 = adjustedTime.jsdate.getHours();
      value_1 == 0?$zeroPaddingNumber(buf, 24, count):$zeroPaddingNumber(buf, value_1, count);
      break;
    case 83:
      $formatFractionalSeconds(buf, count, adjustedTime);
      break;
    case 69:
      value_2 = adjustedDate.jsdate.getDay();
      count == 5?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['S', 'M', 'T', 'W', 'T', 'F', 'S'])[value_2]):count == 4?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'])[value_2]):$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'])[value_2]);
      break;
    case 97:
      adjustedTime.jsdate.getHours() >= 12 && adjustedTime.jsdate.getHours() < 24?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['AM', 'PM'])[1]):$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['AM', 'PM'])[0]);
      break;
    case 104:
      value_3 = adjustedTime.jsdate.getHours() % 12;
      value_3 == 0?$zeroPaddingNumber(buf, 12, count):$zeroPaddingNumber(buf, value_3, count);
      break;
    case 75:
      value_4 = adjustedTime.jsdate.getHours() % 12;
      $zeroPaddingNumber(buf, value_4, count);
      break;
    case 72:
      value_5 = adjustedTime.jsdate.getHours();
      $zeroPaddingNumber(buf, value_5, count);
      break;
    case 99:
      value_6 = adjustedDate.jsdate.getDay();
      count == 5?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['S', 'M', 'T', 'W', 'T', 'F', 'S'])[value_6]):count == 4?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'])[value_6]):count == 3?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'])[value_6]):$zeroPaddingNumber(buf, value_6, 1);
      break;
    case 76:
      value_7 = adjustedDate.jsdate.getMonth();
      count == 5?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['J', 'F', 'M', 'A', 'M', 'J', 'J', 'A', 'S', 'O', 'N', 'D'])[value_7]):count == 4?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'])[value_7]):count == 3?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'])[value_7]):$zeroPaddingNumber(buf, value_7 + 1, count);
      break;
    case 81:
      value_8 = ~~(adjustedDate.jsdate.getMonth() / 3);
      count < 4?$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Q1', 'Q2', 'Q3', 'Q4'])[value_8]):$append_1(buf, initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['1st quarter', '2nd quarter', '3rd quarter', '4th quarter'])[value_8]);
      break;
    case 100:
      value_9 = adjustedDate.jsdate.getDate();
      $zeroPaddingNumber(buf, value_9, count);
      break;
    case 109:
      value_10 = adjustedTime.jsdate.getMinutes();
      $zeroPaddingNumber(buf, value_10, count);
      break;
    case 115:
      value_11 = adjustedTime.jsdate.getSeconds();
      $zeroPaddingNumber(buf, value_11, count);
      break;
    case 122:
      count < 4?$append_1(buf, timezone.tzNames[0]):$append_1(buf, timezone.tzNames[1]);
      break;
    case 118:
      $append_1(buf, timezone.timezoneID);
      break;
    case 90:
      count < 3?$append_1(buf, $getRFCTimeZoneString(timezone)):count == 3?$append_1(buf, $getISOTimeZoneString(timezone)):$append_1(buf, composeGMTString(timezone.standardOffset));
      break;
    default:return false;
  }
  return true;
}

function $zeroPaddingNumber(buf, value_0, minWidth){
  var b, i_0;
  b = 10;
  for (i_0 = 0; i_0 < minWidth - 1; i_0++) {
    value_0 < b && (buf.string += '0' , buf);
    b *= 10;
  }
  buf.string += value_0;
}

defineClass(632, 1, {});
var Lcom_google_gwt_i18n_shared_DateTimeFormat_2_classLit = createForClass('com.google.gwt.i18n.shared', 'DateTimeFormat', 632, Ljava_lang_Object_2_classLit);
function $clinit_DateTimeFormat_0(){
  $clinit_DateTimeFormat_0 = emptyMethod;
  $clinit_DateTimeFormat();
  cache = new HashMap;
}

function DateTimeFormat(){
  $clinit_DateTimeFormat();
  this.patternParts = new ArrayList;
  this.pattern = 'HH:mm:ss.SSSS';
  $parsePattern(this);
}

function getFormat(dtfi){
  $clinit_DateTimeFormat_0();
  var defaultDtfi, dtf;
  defaultDtfi = $getDateTimeFormatInfo(($clinit_LocaleInfo() , $clinit_LocaleInfo() , instance_0));
  dtf = null;
  dtfi == defaultDtfi && (dtf = dynamicCast($getStringValue(cache, 'HH:mm:ss.SSSS'), 354));
  if (!dtf) {
    dtf = new DateTimeFormat;
    dtfi == defaultDtfi && $putStringValue(cache, 'HH:mm:ss.SSSS', dtf);
  }
  return dtf;
}

defineClass(354, 632, {354:1}, DateTimeFormat);
var cache;
var Lcom_google_gwt_i18n_client_DateTimeFormat_2_classLit = createForClass('com.google.gwt.i18n.client', 'DateTimeFormat', 354, Lcom_google_gwt_i18n_shared_DateTimeFormat_2_classLit);
defineClass(795, 1, {});
var Lcom_google_gwt_i18n_shared_DefaultDateTimeFormatInfo_2_classLit = createForClass('com.google.gwt.i18n.shared', 'DefaultDateTimeFormatInfo', 795, Ljava_lang_Object_2_classLit);
defineClass(796, 795, {});
var Lcom_google_gwt_i18n_client_DefaultDateTimeFormatInfo_2_classLit = createForClass('com.google.gwt.i18n.client', 'DefaultDateTimeFormatInfo', 796, Lcom_google_gwt_i18n_shared_DefaultDateTimeFormatInfo_2_classLit);
function $clinit_LocaleInfo(){
  $clinit_LocaleInfo = emptyMethod;
  instance_0 = new LocaleInfo;
}

function $getDateTimeFormatInfo(this$static){
  !this$static.dateTimeFormatInfo && (this$static.dateTimeFormatInfo = new DateTimeFormatInfoImpl);
  return this$static.dateTimeFormatInfo;
}

function LocaleInfo(){
}

defineClass(662, 1, {}, LocaleInfo);
var instance_0;
var Lcom_google_gwt_i18n_client_LocaleInfo_2_classLit = createForClass('com.google.gwt.i18n.client', 'LocaleInfo', 662, Ljava_lang_Object_2_classLit);
function $getISOTimeZoneString(this$static){
  var data_0, offset;
  offset = -this$static.standardOffset;
  data_0 = initValues(getClassLiteralForArray(C_classLit, 1), $intern_4, 0, 7, [43, 48, 48, 58, 48, 48]);
  if (offset < 0) {
    data_0[0] = 45;
    offset = -offset;
  }
  data_0[1] = data_0[1] + ~~(~~(offset / 60) / 10) & $intern_5;
  data_0[2] = data_0[2] + ~~(offset / 60) % 10 & $intern_5;
  data_0[4] = data_0[4] + ~~(offset % 60 / 10) & $intern_5;
  data_0[5] = data_0[5] + offset % 10 & $intern_5;
  return __valueOf(data_0, 0, data_0.length);
}

function $getRFCTimeZoneString(this$static){
  var data_0, offset;
  offset = -this$static.standardOffset;
  data_0 = initValues(getClassLiteralForArray(C_classLit, 1), $intern_4, 0, 7, [43, 48, 48, 48, 48]);
  if (offset < 0) {
    data_0[0] = 45;
    offset = -offset;
  }
  data_0[1] = data_0[1] + ~~(~~(offset / 60) / 10) & $intern_5;
  data_0[2] = data_0[2] + ~~(offset / 60) % 10 & $intern_5;
  data_0[3] = data_0[3] + ~~(offset % 60 / 10) & $intern_5;
  data_0[4] = data_0[4] + offset % 10 & $intern_5;
  return __valueOf(data_0, 0, data_0.length);
}

function TimeZone(){
}

function composeGMTString(offset){
  var data_0;
  data_0 = initValues(getClassLiteralForArray(C_classLit, 1), $intern_4, 0, 7, [71, 77, 84, 45, 48, 48, 58, 48, 48]);
  if (offset <= 0) {
    data_0[3] = 43;
    offset = -offset;
  }
  data_0[4] = data_0[4] + ~~(~~(offset / 60) / 10) & $intern_5;
  data_0[5] = data_0[5] + ~~(offset / 60) % 10 & $intern_5;
  data_0[7] = data_0[7] + ~~(offset % 60 / 10) & $intern_5;
  data_0[8] = data_0[8] + offset % 10 & $intern_5;
  return __valueOf(data_0, 0, data_0.length);
}

function composePOSIXTimeZoneID(offset){
  var str;
  if (offset == 0) {
    return 'Etc/GMT';
  }
  if (offset < 0) {
    offset = -offset;
    str = 'Etc/GMT-';
  }
   else {
    str = 'Etc/GMT+';
  }
  return str + offsetDisplay(offset);
}

function composeUTCString(offset){
  var str;
  if (offset == 0) {
    return 'UTC';
  }
  if (offset < 0) {
    offset = -offset;
    str = 'UTC+';
  }
   else {
    str = 'UTC-';
  }
  return str + offsetDisplay(offset);
}

function createTimeZone(timeZoneOffsetInMinutes){
  var tz;
  tz = new TimeZone;
  tz.standardOffset = timeZoneOffsetInMinutes;
  tz.timezoneID = composePOSIXTimeZoneID(timeZoneOffsetInMinutes);
  tz.tzNames = initDim(Ljava_lang_String_2_classLit, $intern_6, 2, 2, 4, 1);
  tz.tzNames[0] = composeUTCString(timeZoneOffsetInMinutes);
  tz.tzNames[1] = composeUTCString(timeZoneOffsetInMinutes);
  return tz;
}

function offsetDisplay(offset){
  var hour, mins;
  hour = ~~(offset / 60);
  mins = offset % 60;
  if (mins == 0) {
    return '' + hour;
  }
  return '' + hour + ':' + ('' + mins);
}

defineClass(705, 1, {}, TimeZone);
_.standardOffset = 0;
var Lcom_google_gwt_i18n_client_TimeZone_2_classLit = createForClass('com.google.gwt.i18n.client', 'TimeZone', 705, Ljava_lang_Object_2_classLit);
function DateTimeFormatInfoImpl(){
}

defineClass(717, 796, {}, DateTimeFormatInfoImpl);
var Lcom_google_gwt_i18n_client_impl_cldr_DateTimeFormatInfoImpl_2_classLit = createForClass('com.google.gwt.i18n.client.impl.cldr', 'DateTimeFormatInfoImpl', 717, Lcom_google_gwt_i18n_client_DefaultDateTimeFormatInfo_2_classLit);
function DateTimeFormat$PatternPart(txt, cnt){
  this.text_0 = txt;
  this.count = cnt;
  this.abutStart = false;
}

defineClass(175, 1, {175:1}, DateTimeFormat$PatternPart);
_.abutStart = false;
_.count = 0;
var Lcom_google_gwt_i18n_shared_DateTimeFormat$PatternPart_2_classLit = createForClass('com.google.gwt.i18n.shared', 'DateTimeFormat/PatternPart', 175, Ljava_lang_Object_2_classLit);
function cloneSubrange(array, toIndex){
  var result;
  result = array.slice(0, toIndex);
  initValues(getClass__Ljava_lang_Class___devirtual$(array), array.castableTypeMap$, array.__elementTypeId$, array.__elementTypeCategory$, result);
  return result;
}

function createFrom(array, length_0){
  var result;
  result = initializeArrayElementsWithDefaults(0, length_0);
  initValues(getClass__Ljava_lang_Class___devirtual$(array), array.castableTypeMap$, array.__elementTypeId$, array.__elementTypeCategory$, result);
  return result;
}

function getClassLiteralForArray(clazz, dimensions){
  return getClassLiteralForArray_0(clazz, dimensions);
}

function initDim(leafClassLiteral, castableTypeMap, elementTypeId, length_0, elementTypeCategory, dimensions){
  var result;
  result = initializeArrayElementsWithDefaults(elementTypeCategory, length_0);
  initValues(getClassLiteralForArray(leafClassLiteral, dimensions), castableTypeMap, elementTypeId, elementTypeCategory, result);
  return result;
}

function initValues(arrayClass, castableTypeMap, elementTypeId, elementTypeCategory, array){
  array.___clazz$ = arrayClass;
  array.castableTypeMap$ = castableTypeMap;
  array.typeMarker$ = typeMarkerFn;
  array.__elementTypeId$ = elementTypeId;
  array.__elementTypeCategory$ = elementTypeCategory;
  return array;
}

function initializeArrayElementsWithDefaults(elementTypeCategory, length_0){
  var array = new Array(length_0);
  var initValue;
  switch (elementTypeCategory) {
    case 6:
      initValue = {l:0, m:0, h:0};
      break;
    case 7:
      initValue = 0;
      break;
    case 8:
      initValue = false;
      break;
    default:return array;
  }
  for (var i_0 = 0; i_0 < length_0; ++i_0) {
    array[i_0] = initValue;
  }
  return array;
}

function nativeArraySplice(src_0, srcOfs, dest, destOfs, len, overwrite){
  if (src_0 === dest) {
    src_0 = src_0.slice(srcOfs, srcOfs + len);
    srcOfs = 0;
  }
  for (var batchStart = srcOfs, end = srcOfs + len; batchStart < end;) {
    var batchEnd = Math.min(batchStart + 10000, end);
    len = batchEnd - batchStart;
    Array.prototype.splice.apply(dest, [destOfs, overwrite?len:0].concat(src_0.slice(batchStart, batchEnd)));
    batchStart = batchEnd;
    destOfs += len;
  }
}

function setCheck(array, index_0, value_0){
  var elementTypeId;
  if (value_0 != null) {
    switch (array.__elementTypeCategory$) {
      case 4:
        if (!isJavaString(value_0)) {
          throw new ArrayStoreException;
        }

        break;
      case 0:
        {
          elementTypeId = array.__elementTypeId$;
          if (!canCast(value_0, elementTypeId)) {
            throw new ArrayStoreException;
          }
          break;
        }

      case 2:
        if (!(!isJavaString(value_0) && !hasTypeMarker(value_0))) {
          throw new ArrayStoreException;
        }

        break;
      case 1:
        {
          elementTypeId = array.__elementTypeId$;
          if (!(!isJavaString(value_0) && !hasTypeMarker(value_0)) && !canCast(value_0, elementTypeId)) {
            throw new ArrayStoreException;
          }
          break;
        }

    }
  }
  return array[index_0] = value_0;
}

function cacheJavaScriptException(e, jse){
  if (e && typeof e == 'object') {
    try {
      e.__gwt$exception = jse;
    }
     catch (ignored) {
    }
  }
}

function unwrap(e){
  var jse;
  if (instanceOf(e, 79)) {
    jse = dynamicCast(e, 79);
    if (maskUndefined(jse.e) !== maskUndefined(($clinit_JavaScriptException() , NOT_SET))) {
      return maskUndefined(jse.e) === maskUndefined(NOT_SET)?null:jse.e;
    }
  }
  return e;
}

function wrap(e){
  var jse;
  if (instanceOf(e, 13)) {
    return e;
  }
  jse = e && e.__gwt$exception;
  if (!jse) {
    jse = new JavaScriptException(e);
    captureStackTrace(jse, e);
    cacheJavaScriptException(e, jse);
  }
  return jse;
}

function create_0(value_0){
  var a0, a1, a2;
  a0 = value_0 & $intern_7;
  a1 = value_0 >> 22 & $intern_7;
  a2 = value_0 < 0?$intern_8:0;
  return create0(a0, a1, a2);
}

function create_1(a){
  return create0(a.l, a.m, a.h);
}

function create0(l, m, h){
  return {l:l, m:m, h:h};
}

function divMod(a, b, computeRemainder){
  var aIsCopy, aIsMinValue, aIsNegative, bpower, c, negative;
  if (b.l == 0 && b.m == 0 && b.h == 0) {
    throw new ArithmeticException;
  }
  if (a.l == 0 && a.m == 0 && a.h == 0) {
    computeRemainder && (remainder = create0(0, 0, 0));
    return create0(0, 0, 0);
  }
  if (b.h == $intern_9 && b.m == 0 && b.l == 0) {
    return divModByMinValue(a, computeRemainder);
  }
  negative = false;
  if (b.h >> 19 != 0) {
    b = neg(b);
    negative = true;
  }
  bpower = powerOfTwo(b);
  aIsNegative = false;
  aIsMinValue = false;
  aIsCopy = false;
  if (a.h == $intern_9 && a.m == 0 && a.l == 0) {
    aIsMinValue = true;
    aIsNegative = true;
    if (bpower == -1) {
      a = create_1(($clinit_LongLib$Const() , MAX_VALUE));
      aIsCopy = true;
      negative = !negative;
    }
     else {
      c = shr(a, bpower);
      negative && negate(c);
      computeRemainder && (remainder = create0(0, 0, 0));
      return c;
    }
  }
   else if (a.h >> 19 != 0) {
    aIsNegative = true;
    a = neg(a);
    aIsCopy = true;
    negative = !negative;
  }
  if (bpower != -1) {
    return divModByShift(a, bpower, negative, aIsNegative, computeRemainder);
  }
  if (!gte_0(a, b)) {
    computeRemainder && (aIsNegative?(remainder = neg(a)):(remainder = create0(a.l, a.m, a.h)));
    return create0(0, 0, 0);
  }
  return divModHelper(aIsCopy?a:create0(a.l, a.m, a.h), b, negative, aIsNegative, aIsMinValue, computeRemainder);
}

function divModByMinValue(a, computeRemainder){
  if (a.h == $intern_9 && a.m == 0 && a.l == 0) {
    computeRemainder && (remainder = create0(0, 0, 0));
    return create_1(($clinit_LongLib$Const() , ONE));
  }
  computeRemainder && (remainder = create0(a.l, a.m, a.h));
  return create0(0, 0, 0);
}

function divModByShift(a, bpower, negative, aIsNegative, computeRemainder){
  var c;
  c = shr(a, bpower);
  negative && negate(c);
  if (computeRemainder) {
    a = maskRight(a, bpower);
    aIsNegative?(remainder = neg(a)):(remainder = create0(a.l, a.m, a.h));
  }
  return c;
}

function divModHelper(a, b, negative, aIsNegative, aIsMinValue, computeRemainder){
  var bshift, gte, quotient, shift_0, a1, a2, a0;
  shift_0 = numberOfLeadingZeros(b) - numberOfLeadingZeros(a);
  bshift = shl(b, shift_0);
  quotient = create0(0, 0, 0);
  while (shift_0 >= 0) {
    gte = trialSubtract(a, bshift);
    if (gte) {
      shift_0 < 22?(quotient.l |= 1 << shift_0 , undefined):shift_0 < 44?(quotient.m |= 1 << shift_0 - 22 , undefined):(quotient.h |= 1 << shift_0 - 44 , undefined);
      if (a.l == 0 && a.m == 0 && a.h == 0) {
        break;
      }
    }
    a1 = bshift.m;
    a2 = bshift.h;
    a0 = bshift.l;
    setH(bshift, a2 >>> 1);
    bshift.m = a1 >>> 1 | (a2 & 1) << 21;
    bshift.l = a0 >>> 1 | (a1 & 1) << 21;
    --shift_0;
  }
  negative && negate(quotient);
  if (computeRemainder) {
    if (aIsNegative) {
      remainder = neg(a);
      aIsMinValue && (remainder = sub_0(remainder, ($clinit_LongLib$Const() , ONE)));
    }
     else {
      remainder = create0(a.l, a.m, a.h);
    }
  }
  return quotient;
}

function maskRight(a, bits){
  var b0, b1, b2;
  if (bits <= 22) {
    b0 = a.l & (1 << bits) - 1;
    b1 = b2 = 0;
  }
   else if (bits <= 44) {
    b0 = a.l;
    b1 = a.m & (1 << bits - 22) - 1;
    b2 = 0;
  }
   else {
    b0 = a.l;
    b1 = a.m;
    b2 = a.h & (1 << bits - 44) - 1;
  }
  return create0(b0, b1, b2);
}

function negate(a){
  var neg0, neg1, neg2;
  neg0 = ~a.l + 1 & $intern_7;
  neg1 = ~a.m + (neg0 == 0?1:0) & $intern_7;
  neg2 = ~a.h + (neg0 == 0 && neg1 == 0?1:0) & $intern_8;
  setL(a, neg0);
  setM(a, neg1);
  setH(a, neg2);
}

function numberOfLeadingZeros(a){
  var b1, b2;
  b2 = numberOfLeadingZeros_0(a.h);
  if (b2 == 32) {
    b1 = numberOfLeadingZeros_0(a.m);
    return b1 == 32?numberOfLeadingZeros_0(a.l) + 32:b1 + 20 - 10;
  }
   else {
    return b2 - 12;
  }
}

function powerOfTwo(a){
  var h, l, m;
  l = a.l;
  if ((l & l - 1) != 0) {
    return -1;
  }
  m = a.m;
  if ((m & m - 1) != 0) {
    return -1;
  }
  h = a.h;
  if ((h & h - 1) != 0) {
    return -1;
  }
  if (h == 0 && m == 0 && l == 0) {
    return -1;
  }
  if (h == 0 && m == 0 && l != 0) {
    return numberOfTrailingZeros(l);
  }
  if (h == 0 && m != 0 && l == 0) {
    return numberOfTrailingZeros(m) + 22;
  }
  if (h != 0 && m == 0 && l == 0) {
    return numberOfTrailingZeros(h) + 44;
  }
  return -1;
}

function setH(a, x_0){
  a.h = x_0;
}

function setL(a, x_0){
  a.l = x_0;
}

function setM(a, x_0){
  a.m = x_0;
}

function toDoubleHelper(a){
  return a.l + a.m * $intern_10 + a.h * $intern_11;
}

function trialSubtract(a, b){
  var sum0, sum1, sum2;
  sum2 = a.h - b.h;
  if (sum2 < 0) {
    return false;
  }
  sum0 = a.l - b.l;
  sum1 = a.m - b.m + (sum0 >> 22);
  sum2 += sum1 >> 22;
  if (sum2 < 0) {
    return false;
  }
  setL(a, sum0 & $intern_7);
  setM(a, sum1 & $intern_7);
  setH(a, sum2 & $intern_8);
  return true;
}

var remainder;
function add_0(a, b){
  var sum0, sum1, sum2;
  sum0 = a.l + b.l;
  sum1 = a.m + b.m + (sum0 >> 22);
  sum2 = a.h + b.h + (sum1 >> 22);
  return {l:sum0 & $intern_7, m:sum1 & $intern_7, h:sum2 & $intern_8};
}

function and(a, b){
  return {l:a.l & b.l, m:a.m & b.m, h:a.h & b.h};
}

function div(a, b){
  return divMod(a, b, false);
}

function eq(a, b){
  return a.l == b.l && a.m == b.m && a.h == b.h;
}

function fromDouble(value_0){
  var a0, a1, a2, negative, result;
  if (isNaN_0(value_0)) {
    return $clinit_LongLib$Const() , ZERO;
  }
  if (value_0 < $intern_12) {
    return $clinit_LongLib$Const() , MIN_VALUE;
  }
  if (value_0 >= 9223372036854775807) {
    return $clinit_LongLib$Const() , MAX_VALUE;
  }
  negative = false;
  if (value_0 < 0) {
    negative = true;
    value_0 = -value_0;
  }
  a2 = 0;
  if (value_0 >= $intern_11) {
    a2 = round_int(value_0 / $intern_11);
    value_0 -= a2 * $intern_11;
  }
  a1 = 0;
  if (value_0 >= $intern_10) {
    a1 = round_int(value_0 / $intern_10);
    value_0 -= a1 * $intern_10;
  }
  a0 = round_int(value_0);
  result = create0(a0, a1, a2);
  negative && negate(result);
  return result;
}

function fromInt(value_0){
  var rebase, result;
  if (value_0 > -129 && value_0 < 128) {
    rebase = value_0 + 128;
    boxedValues == null && (boxedValues = initDim(Lcom_google_gwt_lang_LongLibBase$LongEmul_2_classLit, $intern_6, 817, 256, 0, 1));
    result = boxedValues[rebase];
    !result && (result = boxedValues[rebase] = create_0(value_0));
    return result;
  }
  return create_0(value_0);
}

function gt(a, b){
  var signa, signb;
  signa = a.h >> 19;
  signb = b.h >> 19;
  return signa == 0?signb != 0 || a.h > b.h || a.h == b.h && a.m > b.m || a.h == b.h && a.m == b.m && a.l > b.l:!(signb == 0 || a.h < b.h || a.h == b.h && a.m < b.m || a.h == b.h && a.m == b.m && a.l <= b.l);
}

function gte_0(a, b){
  var signa, signb;
  signa = a.h >> 19;
  signb = b.h >> 19;
  return signa == 0?signb != 0 || a.h > b.h || a.h == b.h && a.m > b.m || a.h == b.h && a.m == b.m && a.l >= b.l:!(signb == 0 || a.h < b.h || a.h == b.h && a.m < b.m || a.h == b.h && a.m == b.m && a.l < b.l);
}

function lt(a, b){
  return !gte_0(a, b);
}

function lte(a, b){
  return !gt(a, b);
}

function mod(a, b){
  divMod(a, b, true);
  return remainder;
}

function mul(a, b){
  var a0, a1, a2, a3, a4, b0, b1, b2, b3, b4, c0, c00, c01, c1, c10, c11, c12, c13, c2, c22, c23, c24, p0, p1, p2, p3, p4;
  a0 = a.l & 8191;
  a1 = a.l >> 13 | (a.m & 15) << 9;
  a2 = a.m >> 4 & 8191;
  a3 = a.m >> 17 | (a.h & 255) << 5;
  a4 = (a.h & 1048320) >> 8;
  b0 = b.l & 8191;
  b1 = b.l >> 13 | (b.m & 15) << 9;
  b2 = b.m >> 4 & 8191;
  b3 = b.m >> 17 | (b.h & 255) << 5;
  b4 = (b.h & 1048320) >> 8;
  p0 = a0 * b0;
  p1 = a1 * b0;
  p2 = a2 * b0;
  p3 = a3 * b0;
  p4 = a4 * b0;
  if (b1 != 0) {
    p1 += a0 * b1;
    p2 += a1 * b1;
    p3 += a2 * b1;
    p4 += a3 * b1;
  }
  if (b2 != 0) {
    p2 += a0 * b2;
    p3 += a1 * b2;
    p4 += a2 * b2;
  }
  if (b3 != 0) {
    p3 += a0 * b3;
    p4 += a1 * b3;
  }
  b4 != 0 && (p4 += a0 * b4);
  c00 = p0 & $intern_7;
  c01 = (p1 & 511) << 13;
  c0 = c00 + c01;
  c10 = p0 >> 22;
  c11 = p1 >> 9;
  c12 = (p2 & 262143) << 4;
  c13 = (p3 & 31) << 17;
  c1 = c10 + c11 + c12 + c13;
  c22 = p2 >> 18;
  c23 = p3 >> 5;
  c24 = (p4 & 4095) << 8;
  c2 = c22 + c23 + c24;
  c1 += c0 >> 22;
  c0 &= $intern_7;
  c2 += c1 >> 22;
  c1 &= $intern_7;
  c2 &= $intern_8;
  return create0(c0, c1, c2);
}

function neg(a){
  var neg0, neg1, neg2;
  neg0 = ~a.l + 1 & $intern_7;
  neg1 = ~a.m + (neg0 == 0?1:0) & $intern_7;
  neg2 = ~a.h + (neg0 == 0 && neg1 == 0?1:0) & $intern_8;
  return create0(neg0, neg1, neg2);
}

function neq(a, b){
  return a.l != b.l || a.m != b.m || a.h != b.h;
}

function or(a, b){
  return {l:a.l | b.l, m:a.m | b.m, h:a.h | b.h};
}

function shl(a, n){
  var res0, res1, res2;
  n &= 63;
  if (n < 22) {
    res0 = a.l << n;
    res1 = a.m << n | a.l >> 22 - n;
    res2 = a.h << n | a.m >> 22 - n;
  }
   else if (n < 44) {
    res0 = 0;
    res1 = a.l << n - 22;
    res2 = a.m << n - 22 | a.l >> 44 - n;
  }
   else {
    res0 = 0;
    res1 = 0;
    res2 = a.l << n - 44;
  }
  return {l:res0 & $intern_7, m:res1 & $intern_7, h:res2 & $intern_8};
}

function shr(a, n){
  var a2, negative, res0, res1, res2;
  n &= 63;
  a2 = a.h;
  negative = (a2 & $intern_9) != 0;
  negative && (a2 |= -1048576);
  if (n < 22) {
    res2 = a2 >> n;
    res1 = a.m >> n | a2 << 22 - n;
    res0 = a.l >> n | a.m << 22 - n;
  }
   else if (n < 44) {
    res2 = negative?$intern_8:0;
    res1 = a2 >> n - 22;
    res0 = a.m >> n - 22 | a2 << 44 - n;
  }
   else {
    res2 = negative?$intern_8:0;
    res1 = negative?$intern_7:0;
    res0 = a2 >> n - 44;
  }
  return {l:res0 & $intern_7, m:res1 & $intern_7, h:res2 & $intern_8};
}

function shru(a, n){
  var a2, res0, res1, res2;
  n &= 63;
  a2 = a.h & $intern_8;
  if (n < 22) {
    res2 = a2 >>> n;
    res1 = a.m >> n | a2 << 22 - n;
    res0 = a.l >> n | a.m << 22 - n;
  }
   else if (n < 44) {
    res2 = 0;
    res1 = a2 >>> n - 22;
    res0 = a.m >> n - 22 | a.h << 44 - n;
  }
   else {
    res2 = 0;
    res1 = 0;
    res0 = a2 >>> n - 44;
  }
  return {l:res0 & $intern_7, m:res1 & $intern_7, h:res2 & $intern_8};
}

function sub_0(a, b){
  var sum0, sum1, sum2;
  sum0 = a.l - b.l;
  sum1 = a.m - b.m + (sum0 >> 22);
  sum2 = a.h - b.h + (sum1 >> 22);
  return {l:sum0 & $intern_7, m:sum1 & $intern_7, h:sum2 & $intern_8};
}

function toDouble(a){
  if (eq(a, ($clinit_LongLib$Const() , MIN_VALUE))) {
    return $intern_12;
  }
  if (!gte_0(a, ZERO)) {
    return -toDoubleHelper(neg(a));
  }
  return a.l + a.m * $intern_10 + a.h * $intern_11;
}

function toInt(a){
  return a.l | a.m << 22;
}

function toString_2(a){
  var digits, rem, res, tenPowerLong, zeroesNeeded;
  if (a.l == 0 && a.m == 0 && a.h == 0) {
    return '0';
  }
  if (a.h == $intern_9 && a.m == 0 && a.l == 0) {
    return '-9223372036854775808';
  }
  if (a.h >> 19 != 0) {
    return '-' + toString_2(neg(a));
  }
  rem = a;
  res = '';
  while (!(rem.l == 0 && rem.m == 0 && rem.h == 0)) {
    tenPowerLong = fromInt(1000000000);
    rem = divMod(rem, tenPowerLong, true);
    digits = '' + toInt(remainder);
    if (!(rem.l == 0 && rem.m == 0 && rem.h == 0)) {
      zeroesNeeded = 9 - digits.length;
      for (; zeroesNeeded > 0; zeroesNeeded--) {
        digits = '0' + digits;
      }
    }
    res = digits + res;
  }
  return res;
}

function xor(a, b){
  return {l:a.l ^ b.l, m:a.m ^ b.m, h:a.h ^ b.h};
}

var boxedValues;
function $clinit_LongLib$Const(){
  $clinit_LongLib$Const = emptyMethod;
  MAX_VALUE = create0($intern_7, $intern_7, 524287);
  MIN_VALUE = create0(0, 0, $intern_9);
  ONE = fromInt(1);
  fromInt(2);
  ZERO = fromInt(0);
}

var MAX_VALUE, MIN_VALUE, ONE, ZERO;
function hasTypeMarker(o){
  return o.typeMarker$ === typeMarkerFn;
}

function init(){
  $wnd.setTimeout($entry(assertCompileTimeUserAgent));
  $onModuleLoad_0();
  $onModuleLoad($clinit_LogConfiguration());
  $clinit_ExporterUtil();
  new ExportAllExporterImpl;
  $wnd.jsAppLoaded && $wnd.jsAppLoaded();
}

function $getLevel(this$static){
  if (this$static.level) {
    return this$static.level;
  }
  return $clinit_Level() , ALL;
}

function $setFormatter(this$static, newFormatter){
  this$static.formatter = newFormatter;
}

function $setLevel(this$static, newLevel){
  this$static.level = newLevel;
}

defineClass(154, 1, {154:1});
var Ljava_util_logging_Handler_2_classLit = createForClass('java.util.logging', 'Handler', 154, Ljava_lang_Object_2_classLit);
function ConsoleLogHandler(){
  $setFormatter(this, new TextLogFormatter(true));
  $setLevel(this, ($clinit_Level() , ALL));
}

defineClass(517, 154, {154:1}, ConsoleLogHandler);
_.publish = function publish(record){
  var msg;
  if (!window.console || ($getLevel(this) , false)) {
    return;
  }
  msg = $format_0(this.formatter, record);
  $clinit_Level();
  window.console.error(msg);
}
;
var Lcom_google_gwt_logging_client_ConsoleLogHandler_2_classLit = createForClass('com.google.gwt.logging.client', 'ConsoleLogHandler', 517, Ljava_util_logging_Handler_2_classLit);
function DevelopmentModeLogHandler(){
  $setFormatter(this, new TextLogFormatter(false));
  $setLevel(this, ($clinit_Level() , ALL));
}

defineClass(518, 154, {154:1}, DevelopmentModeLogHandler);
_.publish = function publish_0(record){
  return;
}
;
var Lcom_google_gwt_logging_client_DevelopmentModeLogHandler_2_classLit = createForClass('com.google.gwt.logging.client', 'DevelopmentModeLogHandler', 518, Ljava_util_logging_Handler_2_classLit);
function $clinit_LogConfiguration(){
  $clinit_LogConfiguration = emptyMethod;
  impl = new LogConfiguration$LogConfigurationImplRegular;
}

function $onModuleLoad(){
  var log_0;
  $configureClientSideLogging(impl);
  if (!uncaughtExceptionHandler) {
    log_0 = getLogger(($ensureNamesAreInitialized(Lcom_google_gwt_logging_client_LogConfiguration_2_classLit) , Lcom_google_gwt_logging_client_LogConfiguration_2_classLit.typeName));
    setUncaughtExceptionHandler(new LogConfiguration$1(log_0));
  }
}

var impl;
var Lcom_google_gwt_logging_client_LogConfiguration_2_classLit = createForClass('com.google.gwt.logging.client', 'LogConfiguration', null, Ljava_lang_Object_2_classLit);
function LogConfiguration$1(val$log){
  this.val$log2 = val$log;
}

defineClass(474, 1, {}, LogConfiguration$1);
var Lcom_google_gwt_logging_client_LogConfiguration$1_2_classLit = createForClass('com.google.gwt.logging.client', 'LogConfiguration/1', 474, Ljava_lang_Object_2_classLit);
function $configureClientSideLogging(this$static){
  this$static.root = (new LoggerImplRegular , $ensureLogger(getLogManager(), ''));
  this$static.root.impl.useParentHandlers = false;
  $setLevels(this$static.root);
  $setDefaultHandlers(this$static.root);
}

function $setDefaultHandlers(l){
  var console_0, dev;
  console_0 = new ConsoleLogHandler;
  $addHandler(l.impl, console_0);
  dev = new DevelopmentModeLogHandler;
  $addHandler(l.impl, dev);
}

function $setLevels(l){
  var level, levelParam, paramsForName;
  levelParam = (ensureListParameterMap() , paramsForName = dynamicCast(listParamMap.get_0('logLevel'), 43) , !paramsForName?null:dynamicCastToString(paramsForName.get_1(paramsForName.size_1() - 1)));
  level = levelParam == null?null:($clinit_Level() , $parse_0(levelParam));
  level?$setLevel_0(l.impl, level):$setLevel_1(l, ($clinit_Level() , INFO));
}

function LogConfiguration$LogConfigurationImplRegular(){
}

defineClass(473, 1, {}, LogConfiguration$LogConfigurationImplRegular);
var Lcom_google_gwt_logging_client_LogConfiguration$LogConfigurationImplRegular_2_classLit = createForClass('com.google.gwt.logging.client', 'LogConfiguration/LogConfigurationImplRegular', 473, Ljava_lang_Object_2_classLit);
defineClass(783, 1, {});
var Ljava_util_logging_Formatter_2_classLit = createForClass('java.util.logging', 'Formatter', 783, Ljava_lang_Object_2_classLit);
defineClass(784, 783, {});
var Lcom_google_gwt_logging_impl_FormatterImpl_2_classLit = createForClass('com.google.gwt.logging.impl', 'FormatterImpl', 784, Ljava_util_logging_Formatter_2_classLit);
function $format_0(this$static, event_0){
  var message, date, s;
  message = new StringBuilder;
  $append_1(message, (date = new Date_1(event_0.millis) , s = new StringBuilder , $append_1(s, $toString_1(date)) , s.string += ' ' , $append_1(s, event_0.loggerName) , s.string += '\n' , s.string += 'SEVERE' , s.string += ': ' , s.string));
  $append_1(message, event_0.msg);
  this$static.showStackTraces && !!event_0.thrown && $printStackTrace(event_0.thrown, new StackTracePrintStream(message));
  return message.string;
}

function TextLogFormatter(showStackTraces){
  this.showStackTraces = showStackTraces;
}

defineClass(443, 784, {}, TextLogFormatter);
_.showStackTraces = false;
var Lcom_google_gwt_logging_client_TextLogFormatter_2_classLit = createForClass('com.google.gwt.logging.client', 'TextLogFormatter', 443, Lcom_google_gwt_logging_impl_FormatterImpl_2_classLit);
function $parse_0(name_0){
  name_0 = name_0.toUpperCase();
  if ($equals_3(name_0, 'ALL')) {
    return $clinit_Level() , ALL;
  }
   else if ($equals_3(name_0, 'CONFIG')) {
    return $clinit_Level() , CONFIG;
  }
   else if ($equals_3(name_0, 'FINE')) {
    return $clinit_Level() , FINE;
  }
   else if ($equals_3(name_0, 'FINER')) {
    return $clinit_Level() , FINER;
  }
   else if ($equals_3(name_0, 'FINEST')) {
    return $clinit_Level() , FINEST;
  }
   else if ($equals_3(name_0, 'INFO')) {
    return $clinit_Level() , INFO;
  }
   else if ($equals_3(name_0, 'OFF')) {
    return $clinit_Level() , OFF;
  }
   else if ($equals_3(name_0, 'SEVERE')) {
    return $clinit_Level() , SEVERE;
  }
   else if ($equals_3(name_0, 'WARNING')) {
    return $clinit_Level() , WARNING;
  }
  throw new IllegalArgumentException_0('Invalid level "' + name_0 + '"');
}

function $addHandler(this$static, handler){
  $add_0(this$static.handlers, handler);
}

function $getEffectiveLevel(this$static){
  var effectiveLevel, logger;
  if (this$static.level) {
    return this$static.level;
  }
  logger = this$static.parent_0;
  while (logger) {
    effectiveLevel = logger.impl.level;
    if (effectiveLevel) {
      return effectiveLevel;
    }
    logger = logger.impl.parent_0;
  }
  return $clinit_Level() , INFO;
}

function $getHandlers(this$static){
  return dynamicCast($toArray_0(this$static.handlers, initDim(Ljava_util_logging_Handler_2_classLit, $intern_13, 154, this$static.handlers.array.length, 0, 1)), 470);
}

function $log(this$static, msg, thrown){
  var record;
  if ($getEffectiveLevel(this$static).intValue() <= 1000) {
    record = new LogRecord(msg);
    record.thrown = thrown;
    $setLoggerName(record, this$static.name_0);
    $log_0(this$static, record);
  }
}

function $log_0(this$static, record){
  var handler, handler$array, handler$array0, handler$index, handler$index0, handler$max, handler$max0, logger;
  if ($getEffectiveLevel(this$static).intValue() <= 1000) {
    for (handler$array0 = dynamicCast($toArray_0(this$static.handlers, initDim(Ljava_util_logging_Handler_2_classLit, $intern_13, 154, this$static.handlers.array.length, 0, 1)), 470) , handler$index0 = 0 , handler$max0 = handler$array0.length; handler$index0 < handler$max0; ++handler$index0) {
      handler = handler$array0[handler$index0];
      handler.publish(record);
    }
    logger = this$static.useParentHandlers?this$static.parent_0:null;
    while (logger) {
      for (handler$array = $getHandlers(logger.impl) , handler$index = 0 , handler$max = handler$array.length; handler$index < handler$max; ++handler$index) {
        handler = handler$array[handler$index];
        handler.publish(record);
      }
      logger = logger.impl.useParentHandlers?logger.impl.parent_0:null;
    }
  }
}

function $setLevel_0(this$static, newLevel){
  this$static.level = newLevel;
}

function $setName(this$static, newName){
  this$static.name_0 = newName;
}

function $setParent(this$static, newParent){
  !!newParent && (this$static.parent_0 = newParent);
}

function LoggerImplRegular(){
  this.useParentHandlers = true;
  this.handlers = new ArrayList;
}

defineClass(342, 1, {}, LoggerImplRegular);
_.level = null;
_.useParentHandlers = false;
var Lcom_google_gwt_logging_impl_LoggerImplRegular_2_classLit = createForClass('com.google.gwt.logging.impl', 'LoggerImplRegular', 342, Ljava_lang_Object_2_classLit);
defineClass(780, 1, {});
var Ljava_io_OutputStream_2_classLit = createForClass('java.io', 'OutputStream', 780, Ljava_lang_Object_2_classLit);
defineClass(781, 780, {});
var Ljava_io_FilterOutputStream_2_classLit = createForClass('java.io', 'FilterOutputStream', 781, Ljava_io_OutputStream_2_classLit);
function PrintStream(){
}

defineClass(503, 781, {}, PrintStream);
_.print_0 = function print_0(s){
}
;
_.println = function println(x_0){
}
;
_.println_0 = function println_0(s){
}
;
var Ljava_io_PrintStream_2_classLit = createForClass('java.io', 'PrintStream', 503, Ljava_io_FilterOutputStream_2_classLit);
function StackTracePrintStream(builder){
  this.builder = builder;
}

defineClass(522, 503, {}, StackTracePrintStream);
_.print_0 = function print_1(str){
  $append_1(this.builder, str);
}
;
_.println = function println_1(obj){
  $append_1(this.builder, '' + obj);
  $append_1(this.builder, '\n');
}
;
_.println_0 = function println_2(str){
  $append_1(this.builder, str);
  $append_1(this.builder, '\n');
}
;
var Lcom_google_gwt_logging_impl_StackTracePrintStream_2_classLit = createForClass('com.google.gwt.logging.impl', 'StackTracePrintStream', 522, Ljava_io_PrintStream_2_classLit);
function $getItem(this$static, key){
  return $getItem_0(this$static.storage, key);
}

function $removeItem(this$static, key){
  $removeItem_0(this$static.storage, key);
}

function $setItem(this$static, key, data_0){
  $setItem_0(this$static.storage, key, data_0);
}

function Storage_0(){
  this.storage = 'localStorage';
}

defineClass(446, 1, {}, Storage_0);
var localStorage_0;
var Lcom_google_gwt_storage_client_Storage_2_classLit = createForClass('com.google.gwt.storage.client', 'Storage', 446, Ljava_lang_Object_2_classLit);
function $clinit_Storage$StorageSupportDetector(){
  $clinit_Storage$StorageSupportDetector = emptyMethod;
  localStorageSupported = checkStorageSupport('localStorage');
  checkStorageSupport('sessionStorage');
}

function checkStorageSupport(storage){
  var c = '_gwt_dummy_';
  try {
    $wnd[storage].setItem(c, c);
    $wnd[storage].removeItem(c);
    return true;
  }
   catch (e) {
    return false;
  }
}

var localStorageSupported = false;
function $getItem_0(storage, key){
  return $wnd[storage].getItem(key);
}

function $removeItem_0(storage, key){
  $getItem_0(storage, key);
  $wnd[storage].removeItem(key);
}

function $setItem_0(storage, key, data_0){
  $getItem_0(storage, key);
  $wnd[storage].setItem(key, data_0);
}

function create_2(length_0){
  return new Uint8Array(length_0);
}

function $onModuleLoad_0(){
  var allowedModes, currentMode, i_0;
  currentMode = $doc.compatMode;
  allowedModes = initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['CSS1Compat']);
  for (i_0 = 0; i_0 < allowedModes.length; i_0++) {
    if ($equals_3(allowedModes[i_0], currentMode)) {
      return;
    }
  }
  allowedModes.length == 1 && $equals_3('CSS1Compat', allowedModes[0]) && $equals_3('BackCompat', currentMode)?"GWT no longer supports Quirks Mode (document.compatMode=' BackCompat').<br>Make sure your application's host HTML page has a Standards Mode (document.compatMode=' CSS1Compat') doctype,<br>e.g. by using &lt;!doctype html&gt; at the start of your application's HTML page.<br><br>To continue using this unsupported rendering mode and risk layout problems, suppress this message by adding<br>the following line to your*.gwt.xml module file:<br>&nbsp;&nbsp;&lt;extend-configuration-property name=\"document.compatMode\" value=\"" + currentMode + '"/&gt;':"Your *.gwt.xml module configuration prohibits the use of the current document rendering mode (document.compatMode=' " + currentMode + "').<br>Modify your application's host HTML page doctype, or update your custom " + "'document.compatMode' configuration property settings.";
}

function $cancel(this$static){
  if (!this$static.timerId) {
    return;
  }
  ++this$static.cancelCounter;
  this$static.isRepeating?clearInterval_0(this$static.timerId.value_0):clearTimeout_0(this$static.timerId.value_0);
  this$static.timerId = null;
}

function $schedule(this$static, delayMillis){
  if (delayMillis < 0) {
    throw new IllegalArgumentException_0('must be non-negative');
  }
  !!this$static.timerId && $cancel(this$static);
  this$static.isRepeating = false;
  this$static.timerId = valueOf(setTimeout_0(createCallback_0(this$static, this$static.cancelCounter), delayMillis));
}

function clearInterval_0(timerId){
  $wnd.clearInterval(timerId);
}

function clearTimeout_0(timerId){
  $wnd.clearTimeout(timerId);
}

function createCallback_0(timer, cancelCounter){
  return $entry(function(){
    timer.fire(cancelCounter);
  }
  );
}

function setTimeout_0(func, time){
  return $wnd.setTimeout(func, time);
}

defineClass(707, 1, {});
_.fire = function fire(scheduleCancelCounter){
  if (scheduleCancelCounter != this.cancelCounter) {
    return;
  }
  this.isRepeating || (this.timerId = null);
  $doIteration(this.this$01);
}
;
_.cancelCounter = 0;
_.isRepeating = false;
_.timerId = null;
var Lcom_google_gwt_user_client_Timer_2_classLit = createForClass('com.google.gwt.user.client', 'Timer', 707, Ljava_lang_Object_2_classLit);
function buildListParamMap(queryString){
  var entry, entry$iterator, key, kv, kvPair, kvPair$array, kvPair$index, kvPair$max, out, qs, val, values, regexp;
  out = new HashMap;
  if (queryString != null && queryString.length > 1) {
    qs = __substr(queryString, 1, queryString.length - 1);
    for (kvPair$array = $split(qs, '&', 0) , kvPair$index = 0 , kvPair$max = kvPair$array.length; kvPair$index < kvPair$max; ++kvPair$index) {
      kvPair = kvPair$array[kvPair$index];
      kv = $split(kvPair, '=', 2);
      key = kv[0];
      if (!key.length) {
        continue;
      }
      val = kv.length > 1?kv[1]:'';
      try {
        val = (throwIfNull(val) , regexp = /\+/g , decodeURIComponent(val.replace(regexp, '%20')));
      }
       catch ($e0) {
        $e0 = wrap($e0);
        if (!instanceOf($e0, 79))
          throw unwrap($e0);
      }
      values = dynamicCast(out.get_0(key), 43);
      if (!values) {
        values = new ArrayList;
        out.put(key, values);
      }
      values.add_1(val);
    }
  }
  for (entry$iterator = out.entrySet_0().iterator(); entry$iterator.hasNext();) {
    entry = dynamicCast(entry$iterator.next(), 20);
    entry.setValue(unmodifiableList(dynamicCast(entry.getValue(), 43)));
  }
  out = new Collections$UnmodifiableMap(out);
  return out;
}

function ensureListParameterMap(){
  var currentQueryString, href_0, hashLoc, questionLoc;
  currentQueryString = (href_0 = $wnd.location.href , hashLoc = href_0.indexOf('#') , hashLoc >= 0 && (href_0 = href_0.substring(0, hashLoc)) , questionLoc = href_0.indexOf('?') , questionLoc > 0?href_0.substring(questionLoc):'');
  if (!listParamMap || !$equals_3(cachedQueryString, currentQueryString)) {
    listParamMap = buildListParamMap(currentQueryString);
    cachedQueryString = currentQueryString;
  }
}

var cachedQueryString = '', listParamMap;
function assertCompileTimeUserAgent(){
  var runtimeValue;
  runtimeValue = $getRuntimeValue();
  if (!$equals_3('ie9', runtimeValue)) {
    throw new UserAgentAsserter$UserAgentAssertionError(runtimeValue);
  }
}

function Error_0(message, cause){
  this.cause = cause;
  this.detailMessage = message;
  $fillInStackTrace(this);
}

defineClass(337, 13, $intern_1);
var Ljava_lang_Error_2_classLit = createForClass('java.lang', 'Error', 337, Ljava_lang_Throwable_2_classLit);
defineClass(47, 337, $intern_1);
var Ljava_lang_AssertionError_2_classLit = createForClass('java.lang', 'AssertionError', 47, Ljava_lang_Error_2_classLit);
function UserAgentAsserter$UserAgentAssertionError(runtimeValue){
  Error_0.call(this, '' + ('Possible problem with your *.gwt.xml module file.\nThe compile time user.agent value (ie9) does not match the runtime user.agent value (' + runtimeValue + ').\n' + 'Expect more errors.'), instanceOf('Possible problem with your *.gwt.xml module file.\nThe compile time user.agent value (ie9) does not match the runtime user.agent value (' + runtimeValue + ').\n' + 'Expect more errors.', 13)?dynamicCast('Possible problem with your *.gwt.xml module file.\nThe compile time user.agent value (ie9) does not match the runtime user.agent value (' + runtimeValue + ').\n' + 'Expect more errors.', 13):null);
}

defineClass(475, 47, $intern_1, UserAgentAsserter$UserAgentAssertionError);
var Lcom_google_gwt_useragent_client_UserAgentAsserter$UserAgentAssertionError_2_classLit = createForClass('com.google.gwt.useragent.client', 'UserAgentAsserter/UserAgentAssertionError', 475, Ljava_lang_AssertionError_2_classLit);
function $getRuntimeValue(){
  var ua = navigator.userAgent.toLowerCase();
  var docMode = $doc.documentMode;
  if (function(){
    return ua.indexOf('webkit') != -1;
  }
  ())
    return 'safari';
  if (function(){
    return ua.indexOf('msie') != -1 && docMode >= 10 && docMode < 11;
  }
  ())
    return 'ie10';
  if (function(){
    return ua.indexOf('msie') != -1 && docMode >= 9 && docMode < 11;
  }
  ())
    return 'ie9';
  if (function(){
    return ua.indexOf('msie') != -1 && docMode >= 8 && docMode < 11;
  }
  ())
    return 'ie8';
  if (function(){
    return ua.indexOf('gecko') != -1 || docMode >= 11;
  }
  ())
    return 'gecko1_8';
  return 'unknown';
}

function ApiConfiguration(deviceString){
  this.appTitle = 'Actor Web App';
  this.appId = 1;
  this.appKey = '??';
  this.deviceString = deviceString;
}

defineClass(535, 1, {}, ApiConfiguration);
_.appId = 0;
var Lim_actor_model_ApiConfiguration_2_classLit = createForClass('im.actor.model', 'ApiConfiguration', 535, Ljava_lang_Object_2_classLit);
function $compareTo(this$static, other){
  return this$static.ordinal - other.ordinal;
}

function Enum(name_0, ordinal){
  this.name_0 = name_0;
  this.ordinal = ordinal;
}

defineClass(18, 1, {3:1, 29:1, 18:1});
_.compareTo = function compareTo(other){
  return $compareTo(this, dynamicCast(other, 18));
}
;
_.equals$ = function equals_0(other){
  return this === other;
}
;
_.hashCode$ = function hashCode_1(){
  return getHashCode(this);
}
;
_.toString$ = function toString_3(){
  return this.name_0 != null?this.name_0:'' + this.ordinal;
}
;
_.ordinal = 0;
var Ljava_lang_Enum_2_classLit = createForClass('java.lang', 'Enum', 18, Ljava_lang_Object_2_classLit);
function $clinit_AuthState(){
  $clinit_AuthState = emptyMethod;
  AUTH_START = new AuthState('AUTH_START', 0);
  CODE_VALIDATION = new AuthState('CODE_VALIDATION', 1);
  SIGN_UP = new AuthState('SIGN_UP', 2);
  LOGGED_IN = new AuthState('LOGGED_IN', 3);
}

function AuthState(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_0(){
  $clinit_AuthState();
  return initValues(getClassLiteralForArray(Lim_actor_model_AuthState_2_classLit, 1), $intern_6, 119, 0, [AUTH_START, CODE_VALIDATION, SIGN_UP, LOGGED_IN]);
}

defineClass(119, 18, {119:1, 3:1, 29:1, 18:1}, AuthState);
var AUTH_START, CODE_VALIDATION, LOGGED_IN, SIGN_UP;
var Lim_actor_model_AuthState_2_classLit = createForEnum('im.actor.model', 'AuthState', 119, Ljava_lang_Enum_2_classLit, values_0);
function Configuration(networkProvider, endpoints, threadingProvider, storageProvider, log_0, localeProvider, notificationProvider, dispatcherProvider, apiConfiguration){
  this.networkProvider = networkProvider;
  this.endpoints = endpoints;
  this.threadingProvider = threadingProvider;
  this.storageProvider = storageProvider;
  this.log_0 = log_0;
  this.localeProvider = localeProvider;
  this.enableContactsLogging = false;
  this.notificationProvider = notificationProvider;
  this.apiConfiguration = apiConfiguration;
  this.dispatcherProvider = dispatcherProvider;
}

defineClass(536, 1, {}, Configuration);
_.enableContactsLogging = false;
var Lim_actor_model_Configuration_2_classLit = createForClass('im.actor.model', 'Configuration', 536, Ljava_lang_Object_2_classLit);
function $addEndpoint(this$static){
  var host, parts, port;
  host = 'mtproto-api.actor.im:10443/';
  $equals_3(__substr(host, host.length - 1, 1), '/') && (host = $substring_0(host, 0, host.length - 1));
  port = -1;
  if (host.indexOf(':') != -1) {
    parts = $split(host, ':', 0);
    host = parts[0];
    port = __parseAndValidateInt(parts[1]);
  }
  port <= 0 && (port = 443);
  $add_0(this$static.endpoints, new ConnectionEndpoint(host, port, ($clinit_ConnectionEndpoint$Type() , WS_TLS)));
  return this$static;
}

function $build(this$static){
  if (!this$static.networkProvider) {
    throw new RuntimeException_0('Networking is not set');
  }
  if (!this$static.threadingProvider) {
    throw new RuntimeException_0('Threading is not set');
  }
  if (!this$static.mainThreadProvider) {
    throw new RuntimeException_0('Main Thread is not set');
  }
  if (!this$static.enginesFactory) {
    throw new RuntimeException_0('Storage not set');
  }
  if (this$static.endpoints.array.length == 0) {
    throw new RuntimeException_0('Endpoints not set');
  }
  if (!this$static.localeProvider) {
    throw new RuntimeException_0('Locale Provider not set');
  }
  if (!this$static.phoneBookProvider) {
    throw new RuntimeException_0('Phonebook Provider not set');
  }
  if (!this$static.cryptoProvider) {
    throw new RuntimeException_0('Crypto Provider not set');
  }
  if (!this$static.apiConfiguration) {
    throw new RuntimeException_0('Api Configuration not set');
  }
  if (!this$static.dispatcherProvider) {
    throw new RuntimeException_0('Dispatcher Provider not set');
  }
  return new Configuration(this$static.networkProvider, dynamicCast($toArray_0(this$static.endpoints, initDim(Lim_actor_model_network_ConnectionEndpoint_2_classLit, {804:1, 3:1, 6:1}, 343, this$static.endpoints.array.length, 0, 1)), 804), this$static.threadingProvider, this$static.enginesFactory, this$static.log_0, this$static.localeProvider, this$static.notificationProvider, this$static.dispatcherProvider, this$static.apiConfiguration);
}

function $setApiConfiguration(this$static, apiConfiguration){
  this$static.apiConfiguration = apiConfiguration;
  return this$static;
}

function $setCryptoProvider(this$static, cryptoProvider){
  this$static.cryptoProvider = cryptoProvider;
  return this$static;
}

function $setDispatcherProvider(this$static, dispatcherProvider){
  this$static.dispatcherProvider = dispatcherProvider;
  return this$static;
}

function $setLocale(this$static, localeProvider){
  this$static.localeProvider = localeProvider;
  return this$static;
}

function $setLog(this$static, log_0){
  this$static.log_0 = log_0;
  return this$static;
}

function $setMainThreadProvider(this$static, mainThreadProvider){
  this$static.mainThreadProvider = mainThreadProvider;
  return this$static;
}

function $setNetworkProvider(this$static, networkProvider){
  this$static.networkProvider = networkProvider;
  return this$static;
}

function $setNotificationProvider(this$static, notificationProvider){
  this$static.notificationProvider = notificationProvider;
  return this$static;
}

function $setPhoneBookProvider(this$static, phoneBookProvider){
  this$static.phoneBookProvider = phoneBookProvider;
  return this$static;
}

function $setStorage(this$static, storageProvider){
  this$static.enginesFactory = storageProvider;
  return this$static;
}

function $setThreadingProvider(this$static, threadingProvider){
  this$static.threadingProvider = threadingProvider;
  return this$static;
}

defineClass(533, 1, {});
var Lim_actor_model_ConfigurationBuilder_2_classLit = createForClass('im.actor.model', 'ConfigurationBuilder', 533, Ljava_lang_Object_2_classLit);
function $clearChat(this$static, peer){
  return new Messages$11(this$static.modules.messages, peer);
}

function $deleteChat(this$static, peer){
  return new Messages$10(this$static.modules.messages, peer);
}

function $getGroups(this$static){
  if (!this$static.modules.groups) {
    return null;
  }
  return this$static.modules.groups.collection;
}

function $getUsers(this$static){
  if (!this$static.modules.users) {
    return null;
  }
  return this$static.modules.users.collection;
}

function $loadDraft(this$static, peer){
  return $loadDraft_0(this$static.modules.messages, peer);
}

function $onConversationClosed(this$static, peer){
  !!this$static.modules.presence && $onConversationClose(this$static.modules.notifications, peer);
}

function $onConversationOpen(this$static, peer){
  if (this$static.modules.presence) {
    $subscribe_1(this$static.modules.presence, peer);
    $onConversationOpen_0(this$static.modules.notifications, peer);
    $assumeConvActor(this$static.modules.messages, peer);
  }
}

function $onDialogsClosed(this$static){
  !!this$static.modules.notifications && $send_1(this$static.modules.notifications.notificationsActor, new NotificationsActor$OnDialogsHidden);
}

function $onDialogsOpen(this$static){
  !!this$static.modules.notifications && $send_1(this$static.modules.notifications.notificationsActor, new NotificationsActor$OnDialogsVisible);
}

function $onProfileOpen(this$static, uid){
  !!this$static.modules.presence && $subscribe_1(this$static.modules.presence, new Peer_2(($clinit_PeerType_0() , PRIVATE_0), uid));
}

function $onTyping(this$static, peer){
  $onTyping_0(this$static.modules.typing, peer);
}

function $requestSms(this$static, phone){
  return new Auth$2(this$static.modules.auth, phone);
}

function $saveDraft(this$static, peer, draft){
  $saveDraft_0(this$static.modules.messages, peer, draft);
}

function $sendCode(this$static, code_0){
  return new Auth$3(this$static.modules.auth, code_0);
}

function $sendMessage(this$static, peer, text_0){
  $sendMessage_1(this$static.modules.messages, peer, text_0);
}

defineClass(531, 1, {});
var Lim_actor_model_Messenger_2_classLit = createForClass('im.actor.model', 'Messenger', 531, Ljava_lang_Object_2_classLit);
function $clinit_AuthHolder(){
  $clinit_AuthHolder = emptyMethod;
  THISDEVICE = new AuthHolder('THISDEVICE', 0, 1);
  OTHERDEVICE = new AuthHolder('OTHERDEVICE', 1, 2);
  UNSUPPORTED_VALUE = new AuthHolder('UNSUPPORTED_VALUE', 2, -1);
}

function AuthHolder(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function parse_0(value_0){
  $clinit_AuthHolder();
  switch (value_0) {
    case 1:
      return THISDEVICE;
    case 2:
      return OTHERDEVICE;
    default:return UNSUPPORTED_VALUE;
  }
}

function values_1(){
  $clinit_AuthHolder();
  return initValues(getClassLiteralForArray(Lim_actor_model_api_AuthHolder_2_classLit, 1), $intern_6, 218, 0, [THISDEVICE, OTHERDEVICE, UNSUPPORTED_VALUE]);
}

defineClass(218, 18, {218:1, 3:1, 29:1, 18:1}, AuthHolder);
_.value_0 = 0;
var OTHERDEVICE, THISDEVICE, UNSUPPORTED_VALUE;
var Lim_actor_model_api_AuthHolder_2_classLit = createForEnum('im.actor.model.api', 'AuthHolder', 218, Ljava_lang_Enum_2_classLit, values_1);
function $toByteArray(this$static){
  var outputStream, writer;
  outputStream = new DataOutput;
  writer = new BserWriter(outputStream);
  try {
    this$static.serialize(writer);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      throw new RuntimeException_0('Unexpected IO exception');
    }
     else 
      throw unwrap($e0);
  }
  return $toByteArray_0(outputStream);
}

defineClass(4, 1, $intern_14);
var Lim_actor_model_droidkit_bser_BserObject_2_classLit = createForClass('im.actor.model.droidkit.bser', 'BserObject', 4, Ljava_lang_Object_2_classLit);
function AuthSession(){
}

defineClass(752, 4, $intern_14, AuthSession);
_.parse_0 = function parse_1(values){
  this.id_0 = convertInt($getLong(values, 1));
  this.authHolder = parse_0(convertInt($getLong(values, 2)));
  this.appId = convertInt($getLong(values, 3));
  this.appTitle = convertString($getBytes(values, 4));
  this.deviceTitle = convertString($getBytes(values, 5));
  this.authTime = convertInt($getLong(values, 6));
  this.authLocation = convertString($getBytes(values, 7));
  this.latitude = ($clinit_Double() , new Double(longBitsToDouble($getLong_0(values, 8))));
  this.longitude = new Double(longBitsToDouble($getLong_0(values, 9)));
}
;
_.serialize = function serialize(writer){
  $writeInt(writer, 1, this.id_0);
  if (!this.authHolder) {
    throw new IOException;
  }
  $writeInt(writer, 2, this.authHolder.value_0);
  $writeInt(writer, 3, this.appId);
  if (this.appTitle == null) {
    throw new IOException;
  }
  $writeString(writer, 4, this.appTitle);
  if (this.deviceTitle == null) {
    throw new IOException;
  }
  $writeString(writer, 5, this.deviceTitle);
  $writeInt(writer, 6, this.authTime);
  if (this.authLocation == null) {
    throw new IOException;
  }
  $writeString(writer, 7, this.authLocation);
  !!this.latitude && $writeVar64Field(writer, 8, doubleToLongBits(this.latitude.value_0));
  !!this.longitude && $writeVar64Field(writer, 9, doubleToLongBits(this.longitude.value_0));
}
;
_.toString$ = function toString_4(){
  var res;
  res = 'struct AuthSession{';
  res += 'id=' + this.id_0;
  res += ', authHolder=' + this.authHolder;
  res += ', appId=' + this.appId;
  res += ', appTitle=' + this.appTitle;
  res += ', deviceTitle=' + this.deviceTitle;
  res += ', authTime=' + this.authTime;
  res += '}';
  return res;
}
;
_.appId = 0;
_.authTime = 0;
_.id_0 = 0;
var Lim_actor_model_api_AuthSession_2_classLit = createForClass('im.actor.model.api', 'AuthSession', 752, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Avatar(){
}

defineClass(50, 4, {50:1, 4:1}, Avatar);
_.parse_0 = function parse_2(values){
  this.smallImage = dynamicCast($optObj(values, 1, new AvatarImage), 115);
  this.largeImage = dynamicCast($optObj(values, 2, new AvatarImage), 115);
  this.fullImage = dynamicCast($optObj(values, 3, new AvatarImage), 115);
}
;
_.serialize = function serialize_0(writer){
  !!this.smallImage && $writeObject(writer, 1, this.smallImage);
  !!this.largeImage && $writeObject(writer, 2, this.largeImage);
  !!this.fullImage && $writeObject(writer, 3, this.fullImage);
}
;
_.toString$ = function toString_5(){
  var res;
  res = 'struct Avatar{';
  res += 'smallImage=' + (this.smallImage?'set':'empty');
  res += ', largeImage=' + (this.largeImage?'set':'empty');
  res += ', fullImage=' + (this.fullImage?'set':'empty');
  res += '}';
  return res;
}
;
var Lim_actor_model_api_Avatar_2_classLit = createForClass('im.actor.model.api', 'Avatar', 50, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function AvatarImage(){
}

defineClass(115, 4, {115:1, 4:1}, AvatarImage);
_.parse_0 = function parse_3(values){
  this.fileLocation = dynamicCast($getObj(values, 1, new FileLocation), 65);
  this.width_0 = convertInt($getLong(values, 2));
  this.height = convertInt($getLong(values, 3));
  this.fileSize = convertInt($getLong(values, 4));
}
;
_.serialize = function serialize_1(writer){
  if (!this.fileLocation) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.fileLocation);
  $writeInt(writer, 2, this.width_0);
  $writeInt(writer, 3, this.height);
  $writeInt(writer, 4, this.fileSize);
}
;
_.toString$ = function toString_6(){
  var res;
  res = 'struct AvatarImage{';
  res += 'fileLocation=' + this.fileLocation;
  res += ', width=' + this.width_0;
  res += ', height=' + this.height;
  res += ', fileSize=' + this.fileSize;
  res += '}';
  return res;
}
;
_.fileSize = 0;
_.height = 0;
_.width_0 = 0;
var Lim_actor_model_api_AvatarImage_2_classLit = createForClass('im.actor.model.api', 'AvatarImage', 115, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Config(){
}

defineClass(217, 4, {217:1, 4:1}, Config);
_.parse_0 = function parse_4(values){
  this.maxGroupSize = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_2(writer){
  $writeInt(writer, 1, this.maxGroupSize);
}
;
_.toString$ = function toString_7(){
  var res;
  res = 'struct Config{';
  res += 'maxGroupSize=' + this.maxGroupSize;
  res += '}';
  return res;
}
;
_.maxGroupSize = 0;
var Lim_actor_model_api_Config_2_classLit = createForClass('im.actor.model.api', 'Config', 217, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Dialog(){
}

defineClass(428, 4, {428:1, 4:1}, Dialog);
_.parse_0 = function parse_5(values){
  var val_state;
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.unreadCount = convertInt($getLong(values, 3));
  this.sortDate = $getLong(values, 4);
  this.senderUid = convertInt($getLong(values, 5));
  this.rid = $getLong(values, 6);
  this.date = $getLong(values, 7);
  this.message_0 = fromBytes_0($getBytes(values, 8));
  val_state = convertInt($getLong_0(values, 9));
  val_state != 0 && (this.state = parse_21(val_state));
}
;
_.serialize = function serialize_3(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeInt(writer, 3, this.unreadCount);
  $writeLong(writer, 4, this.sortDate);
  $writeInt(writer, 5, this.senderUid);
  $writeLong(writer, 6, this.rid);
  $writeLong(writer, 7, this.date);
  if (!this.message_0) {
    throw new IOException;
  }
  $writeBytes(writer, 8, $buildContainer_0(this.message_0));
  !!this.state && $writeInt(writer, 9, this.state.value_0);
}
;
_.toString$ = function toString_8(){
  var res;
  res = 'struct Dialog{';
  res += 'peer=' + this.peer;
  res += ', unreadCount=' + this.unreadCount;
  res += ', sortDate=' + toString_2(this.sortDate);
  res += ', senderUid=' + this.senderUid;
  res += ', rid=' + toString_2(this.rid);
  res += ', date=' + toString_2(this.date);
  res += ', message=' + this.message_0;
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
_.senderUid = 0;
_.sortDate = {l:0, m:0, h:0};
_.unreadCount = 0;
var Lim_actor_model_api_Dialog_2_classLit = createForClass('im.actor.model.api', 'Dialog', 428, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function DifferenceUpdate(){
}

defineClass(430, 4, {430:1, 4:1}, DifferenceUpdate);
_.parse_0 = function parse_6(values){
  this.updateHeader = convertInt($getLong(values, 1));
  this.update = $getBytes(values, 2);
}
;
_.serialize = function serialize_4(writer){
  $writeInt(writer, 1, this.updateHeader);
  if (this.update == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.update);
}
;
_.toString$ = function toString_9(){
  var res;
  res = 'struct DifferenceUpdate{';
  res += 'updateHeader=' + this.updateHeader;
  res += ', update=' + byteArrayToStringCompact(this.update);
  res += '}';
  return res;
}
;
_.updateHeader = 0;
var Lim_actor_model_api_DifferenceUpdate_2_classLit = createForClass('im.actor.model.api', 'DifferenceUpdate', 430, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $buildContainer(this$static){
  var res, writer;
  res = new DataOutput;
  writer = new BserWriter(res);
  $writeInt(writer, 1, this$static.getHeader());
  $writeBytes(writer, 2, $toByteArray(this$static));
  return $toByteArray_0(res);
}

function fromBytes(src_0){
  var content_0, key, values;
  values = new BserValues(deserialize(new DataInput_0(src_0, 0, src_0.length)));
  key = convertInt($getLong(values, 1));
  content_0 = $getBytes(values, 2);
  switch (key) {
    case 1:
      return dynamicCast(parse_159(new DocumentExPhoto, content_0), 87);
    case 2:
      return dynamicCast(parse_159(new DocumentExVideo, content_0), 87);
    case 3:
      return dynamicCast(parse_159(new DocumentExVoice, content_0), 87);
    default:return new DocumentExUnsupported(key, content_0);
  }
}

defineClass(87, 4, $intern_15);
var Lim_actor_model_api_DocumentEx_2_classLit = createForClass('im.actor.model.api', 'DocumentEx', 87, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function DocumentExPhoto(){
}

function DocumentExPhoto_0(w, h){
  this.w = w;
  this.h_0 = h;
}

defineClass(196, 87, {87:1, 196:1, 4:1}, DocumentExPhoto, DocumentExPhoto_0);
_.getHeader = function getHeader(){
  return 1;
}
;
_.parse_0 = function parse_7(values){
  this.w = convertInt($getLong(values, 1));
  this.h_0 = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_5(writer){
  $writeInt(writer, 1, this.w);
  $writeInt(writer, 2, this.h_0);
}
;
_.toString$ = function toString_10(){
  var res;
  res = 'struct DocumentExPhoto{';
  res += 'w=' + this.w;
  res += ', h=' + this.h_0;
  res += '}';
  return res;
}
;
_.h_0 = 0;
_.w = 0;
var Lim_actor_model_api_DocumentExPhoto_2_classLit = createForClass('im.actor.model.api', 'DocumentExPhoto', 196, Lim_actor_model_api_DocumentEx_2_classLit);
function DocumentExUnsupported(key, content_0){
  this.key = key;
  this.content_0 = content_0;
}

defineClass(748, 87, $intern_15, DocumentExUnsupported);
_.getHeader = function getHeader_0(){
  return this.key;
}
;
_.parse_0 = function parse_8(values){
  throw new IOException_0('Parsing is unsupported');
}
;
_.serialize = function serialize_6(writer){
  $writeInt(writer, 1, this.key);
  $writeBytes(writer, 2, this.content_0);
}
;
_.key = 0;
var Lim_actor_model_api_DocumentExUnsupported_2_classLit = createForClass('im.actor.model.api', 'DocumentExUnsupported', 748, Lim_actor_model_api_DocumentEx_2_classLit);
function DocumentExVideo(){
}

function DocumentExVideo_0(w, h, duration){
  this.w = w;
  this.h_0 = h;
  this.duration = duration;
}

defineClass(197, 87, {87:1, 197:1, 4:1}, DocumentExVideo, DocumentExVideo_0);
_.getHeader = function getHeader_1(){
  return 2;
}
;
_.parse_0 = function parse_9(values){
  this.w = convertInt($getLong(values, 1));
  this.h_0 = convertInt($getLong(values, 2));
  this.duration = convertInt($getLong(values, 3));
}
;
_.serialize = function serialize_7(writer){
  $writeInt(writer, 1, this.w);
  $writeInt(writer, 2, this.h_0);
  $writeInt(writer, 3, this.duration);
}
;
_.toString$ = function toString_11(){
  var res;
  res = 'struct DocumentExVideo{';
  res += 'w=' + this.w;
  res += ', h=' + this.h_0;
  res += ', duration=' + this.duration;
  res += '}';
  return res;
}
;
_.duration = 0;
_.h_0 = 0;
_.w = 0;
var Lim_actor_model_api_DocumentExVideo_2_classLit = createForClass('im.actor.model.api', 'DocumentExVideo', 197, Lim_actor_model_api_DocumentEx_2_classLit);
function DocumentExVoice(){
}

defineClass(747, 87, $intern_15, DocumentExVoice);
_.getHeader = function getHeader_2(){
  return 3;
}
;
_.parse_0 = function parse_10(values){
  this.duration = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_8(writer){
  $writeInt(writer, 1, this.duration);
}
;
_.toString$ = function toString_12(){
  var res;
  res = 'struct DocumentExVoice{';
  res += 'duration=' + this.duration;
  res += '}';
  return res;
}
;
_.duration = 0;
var Lim_actor_model_api_DocumentExVoice_2_classLit = createForClass('im.actor.model.api', 'DocumentExVoice', 747, Lim_actor_model_api_DocumentEx_2_classLit);
function $buildContainer_0(this$static){
  var res, writer;
  res = new DataOutput;
  writer = new BserWriter(res);
  $writeInt(writer, 1, this$static.getHeader());
  $writeBytes(writer, 2, $toByteArray(this$static));
  return $toByteArray_0(res);
}

function fromBytes_0(src_0){
  var content_0, key, values;
  values = new BserValues(deserialize(new DataInput_0(src_0, 0, src_0.length)));
  key = convertInt($getLong(values, 1));
  content_0 = $getBytes(values, 2);
  switch (key) {
    case 1:
      return dynamicCast(parse_159(new TextMessage, content_0), 86);
    case 2:
      return dynamicCast(parse_159(new ServiceMessage, content_0), 86);
    case 3:
      return dynamicCast(parse_159(new DocumentMessage, content_0), 86);
    default:return new MessageUnsupported(key, content_0);
  }
}

defineClass(86, 4, {86:1, 4:1});
var Lim_actor_model_api_Message_2_classLit = createForClass('im.actor.model.api', 'Message', 86, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function DocumentMessage(){
}

function DocumentMessage_0(fileId, accessHash, fileSize, name_0, mimeType, thumb, ext){
  this.fileId = fileId;
  this.accessHash = accessHash;
  this.fileSize = fileSize;
  this.encryptionType = null;
  this.encryptionKey = null;
  this.plainFileSize = null;
  this.name_0 = name_0;
  this.mimeType = mimeType;
  this.thumb = thumb;
  this.ext = ext;
}

defineClass(198, 86, {198:1, 86:1, 4:1}, DocumentMessage, DocumentMessage_0);
_.getHeader = function getHeader_3(){
  return 3;
}
;
_.parse_0 = function parse_11(values){
  var val_encryptionType;
  this.fileId = $getLong(values, 1);
  this.accessHash = $getLong(values, 2);
  this.fileSize = convertInt($getLong(values, 3));
  val_encryptionType = convertInt($getLong_0(values, 9));
  val_encryptionType != 0 && (this.encryptionType = parse_14(val_encryptionType));
  this.encryptionKey = $getBytes_0(values, 10);
  this.plainFileSize = valueOf(convertInt($getLong_0(values, 11)));
  this.name_0 = convertString($getBytes(values, 4));
  this.mimeType = convertString($getBytes(values, 5));
  this.thumb = dynamicCast($optObj(values, 6, new FastThumb), 304);
  $getBytes_0(values, 8) != null && (this.ext = fromBytes($getBytes(values, 8)));
}
;
_.serialize = function serialize_9(writer){
  $writeLong(writer, 1, this.fileId);
  $writeLong(writer, 2, this.accessHash);
  $writeInt(writer, 3, this.fileSize);
  !!this.encryptionType && $writeInt(writer, 9, this.encryptionType.value_0);
  this.encryptionKey != null && $writeBytes(writer, 10, this.encryptionKey);
  !!this.plainFileSize && $writeInt(writer, 11, this.plainFileSize.value_0);
  if (this.name_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 4, this.name_0);
  if (this.mimeType == null) {
    throw new IOException;
  }
  $writeString(writer, 5, this.mimeType);
  !!this.thumb && $writeObject(writer, 6, this.thumb);
  !!this.ext && $writeBytes(writer, 8, $buildContainer(this.ext));
}
;
_.toString$ = function toString_13(){
  var res;
  res = 'struct DocumentMessage{';
  res += 'fileId=' + toString_2(this.fileId);
  res += ', fileSize=' + this.fileSize;
  res += ', name=' + this.name_0;
  res += ', mimeType=' + this.mimeType;
  res += ', thumb=' + (this.thumb?'set':'empty');
  res += ', ext=' + (this.ext?'set':'empty');
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.fileId = {l:0, m:0, h:0};
_.fileSize = 0;
var Lim_actor_model_api_DocumentMessage_2_classLit = createForClass('im.actor.model.api', 'DocumentMessage', 198, Lim_actor_model_api_Message_2_classLit);
function Email(){
}

defineClass(468, 4, $intern_14, Email);
_.parse_0 = function parse_12(values){
  this.id_0 = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.email = convertString($getBytes(values, 3));
  this.emailTitle = convertString($getBytes(values, 4));
}
;
_.serialize = function serialize_10(writer){
  $writeInt(writer, 1, this.id_0);
  $writeLong(writer, 2, this.accessHash);
  if (this.email == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.email);
  if (this.emailTitle == null) {
    throw new IOException;
  }
  $writeString(writer, 4, this.emailTitle);
}
;
_.toString$ = function toString_14(){
  var res;
  res = 'struct Email{';
  res += 'id=' + this.id_0;
  res += ', email=' + this.email;
  res += ', emailTitle=' + this.emailTitle;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.id_0 = 0;
var Lim_actor_model_api_Email_2_classLit = createForClass('im.actor.model.api', 'Email', 468, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function EmailToImport(){
}

function EmailToImport_0(email, name_0){
  this.email = email;
  this.name_0 = name_0;
}

defineClass(184, 4, {184:1, 4:1}, EmailToImport, EmailToImport_0);
_.parse_0 = function parse_13(values){
  this.email = convertString($getBytes(values, 1));
  this.name_0 = convertString($getBytes_0(values, 2));
}
;
_.serialize = function serialize_11(writer){
  if (this.email == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.email);
  this.name_0 != null && $writeString(writer, 2, this.name_0);
}
;
_.toString$ = function toString_15(){
  var res;
  res = 'struct EmailToImport{';
  res += 'email=' + this.email;
  res += ', name=' + this.name_0;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_EmailToImport_2_classLit = createForClass('im.actor.model.api', 'EmailToImport', 184, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_EncryptionType(){
  $clinit_EncryptionType = emptyMethod;
  NONE = new EncryptionType('NONE', 0, 0);
  AES = new EncryptionType('AES', 1, 1);
  AES_THEN_MAC = new EncryptionType('AES_THEN_MAC', 2, 2);
  UNSUPPORTED_VALUE_0 = new EncryptionType('UNSUPPORTED_VALUE', 3, -1);
}

function EncryptionType(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function parse_14(value_0){
  $clinit_EncryptionType();
  switch (value_0) {
    case 0:
      return NONE;
    case 1:
      return AES;
    case 2:
      return AES_THEN_MAC;
    default:return UNSUPPORTED_VALUE_0;
  }
}

function values_2(){
  $clinit_EncryptionType();
  return initValues(getClassLiteralForArray(Lim_actor_model_api_EncryptionType_2_classLit, 1), $intern_6, 137, 0, [NONE, AES, AES_THEN_MAC, UNSUPPORTED_VALUE_0]);
}

defineClass(137, 18, {137:1, 3:1, 29:1, 18:1}, EncryptionType);
_.value_0 = 0;
var AES, AES_THEN_MAC, NONE, UNSUPPORTED_VALUE_0;
var Lim_actor_model_api_EncryptionType_2_classLit = createForEnum('im.actor.model.api', 'EncryptionType', 137, Ljava_lang_Enum_2_classLit, values_2);
function FastThumb(){
}

function FastThumb_0(w, h, thumb){
  this.w = w;
  this.h_0 = h;
  this.thumb = thumb;
}

defineClass(304, 4, {304:1, 4:1}, FastThumb, FastThumb_0);
_.parse_0 = function parse_15(values){
  this.w = convertInt($getLong(values, 1));
  this.h_0 = convertInt($getLong(values, 2));
  this.thumb = $getBytes(values, 3);
}
;
_.serialize = function serialize_12(writer){
  $writeInt(writer, 1, this.w);
  $writeInt(writer, 2, this.h_0);
  if (this.thumb == null) {
    throw new IOException;
  }
  $writeBytes(writer, 3, this.thumb);
}
;
_.toString$ = function toString_16(){
  var res;
  res = 'struct FastThumb{';
  res += 'w=' + this.w;
  res += ', h=' + this.h_0;
  res += ', thumb=' + byteArrayToStringCompact(this.thumb);
  res += '}';
  return res;
}
;
_.h_0 = 0;
_.w = 0;
var Lim_actor_model_api_FastThumb_2_classLit = createForClass('im.actor.model.api', 'FastThumb', 304, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function FileLocation(){
}

function FileLocation_0(fileId, accessHash){
  this.fileId = fileId;
  this.accessHash = accessHash;
}

defineClass(65, 4, {65:1, 4:1}, FileLocation, FileLocation_0);
_.parse_0 = function parse_16(values){
  this.fileId = $getLong(values, 1);
  this.accessHash = $getLong(values, 2);
}
;
_.serialize = function serialize_13(writer){
  $writeLong(writer, 1, this.fileId);
  $writeLong(writer, 2, this.accessHash);
}
;
_.toString$ = function toString_17(){
  var res;
  res = 'struct FileLocation{';
  res += 'fileId=' + toString_2(this.fileId);
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.fileId = {l:0, m:0, h:0};
var Lim_actor_model_api_FileLocation_2_classLit = createForClass('im.actor.model.api', 'FileLocation', 65, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Group(){
}

defineClass(215, 4, {215:1, 4:1}, Group);
_.parse_0 = function parse_17(values){
  var _members, i_0;
  this.id_0 = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.title_0 = convertString($getBytes(values, 3));
  this.avatar = dynamicCast($optObj(values, 4, new Avatar), 50);
  this.isMember = neq($getLong(values, 6), {l:0, m:0, h:0});
  this.creatorUid = convertInt($getLong(values, 8));
  _members = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 9); i_0++) {
    $add_0(_members, new Member);
  }
  this.members = $getRepeatedObj(values, 9, _members);
  this.createDate = $getLong(values, 10);
}
;
_.serialize = function serialize_14(writer){
  $writeInt(writer, 1, this.id_0);
  $writeLong(writer, 2, this.accessHash);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.title_0);
  !!this.avatar && $writeObject(writer, 4, this.avatar);
  $writeBool(writer, 6, this.isMember);
  $writeInt(writer, 8, this.creatorUid);
  $writeRepeatedObj(writer, 9, this.members);
  $writeLong(writer, 10, this.createDate);
}
;
_.toString$ = function toString_18(){
  var res;
  res = 'struct Group{';
  res += 'id=' + this.id_0;
  res += ', avatar=' + (this.avatar?'set':'empty');
  res += ', isMember=' + this.isMember;
  res += ', members=' + this.members.array.length;
  res += ', createDate=' + toString_2(this.createDate);
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.createDate = {l:0, m:0, h:0};
_.creatorUid = 0;
_.id_0 = 0;
_.isMember = false;
var Lim_actor_model_api_Group_2_classLit = createForClass('im.actor.model.api', 'Group', 215, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function GroupOutPeer(){
}

function GroupOutPeer_0(groupId, accessHash){
  this.groupId = groupId;
  this.accessHash = accessHash;
}

defineClass(38, 4, {38:1, 4:1}, GroupOutPeer, GroupOutPeer_0);
_.parse_0 = function parse_18(values){
  this.groupId = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
}
;
_.serialize = function serialize_15(writer){
  $writeInt(writer, 1, this.groupId);
  $writeLong(writer, 2, this.accessHash);
}
;
_.toString$ = function toString_19(){
  var res;
  res = 'struct GroupOutPeer{';
  res += 'groupId=' + this.groupId;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.groupId = 0;
var Lim_actor_model_api_GroupOutPeer_2_classLit = createForClass('im.actor.model.api', 'GroupOutPeer', 38, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function HistoryMessage(){
}

defineClass(429, 4, {429:1, 4:1}, HistoryMessage);
_.parse_0 = function parse_19(values){
  var val_state;
  this.senderUid = convertInt($getLong(values, 1));
  this.rid = $getLong(values, 2);
  this.date = $getLong(values, 3);
  this.message_0 = fromBytes_0($getBytes(values, 5));
  val_state = convertInt($getLong_0(values, 6));
  val_state != 0 && (this.state = parse_21(val_state));
}
;
_.serialize = function serialize_16(writer){
  $writeInt(writer, 1, this.senderUid);
  $writeLong(writer, 2, this.rid);
  $writeLong(writer, 3, this.date);
  if (!this.message_0) {
    throw new IOException;
  }
  $writeBytes(writer, 5, $buildContainer_0(this.message_0));
  !!this.state && $writeInt(writer, 6, this.state.value_0);
}
;
_.toString$ = function toString_20(){
  var res;
  res = 'struct HistoryMessage{';
  res += 'senderUid=' + this.senderUid;
  res += ', rid=' + toString_2(this.rid);
  res += ', date=' + toString_2(this.date);
  res += ', message=' + this.message_0;
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
_.senderUid = 0;
var Lim_actor_model_api_HistoryMessage_2_classLit = createForClass('im.actor.model.api', 'HistoryMessage', 429, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Member(){
}

defineClass(216, 4, {216:1, 4:1}, Member);
_.parse_0 = function parse_20(values){
  this.uid = convertInt($getLong(values, 1));
  this.inviterUid = convertInt($getLong(values, 2));
  this.date = $getLong(values, 3);
}
;
_.serialize = function serialize_17(writer){
  $writeInt(writer, 1, this.uid);
  $writeInt(writer, 2, this.inviterUid);
  $writeLong(writer, 3, this.date);
}
;
_.toString$ = function toString_21(){
  var res;
  res = 'struct Member{';
  res += 'uid=' + this.uid;
  res += ', inviterUid=' + this.inviterUid;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.inviterUid = 0;
_.uid = 0;
var Lim_actor_model_api_Member_2_classLit = createForClass('im.actor.model.api', 'Member', 216, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_MessageState(){
  $clinit_MessageState = emptyMethod;
  SENT = new MessageState('SENT', 0, 1);
  RECEIVED = new MessageState('RECEIVED', 1, 2);
  READ = new MessageState('READ', 2, 3);
  UNSUPPORTED_VALUE_1 = new MessageState('UNSUPPORTED_VALUE', 3, -1);
}

function MessageState(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function parse_21(value_0){
  $clinit_MessageState();
  switch (value_0) {
    case 1:
      return SENT;
    case 2:
      return RECEIVED;
    case 3:
      return READ;
    default:return UNSUPPORTED_VALUE_1;
  }
}

function values_3(){
  $clinit_MessageState();
  return initValues(getClassLiteralForArray(Lim_actor_model_api_MessageState_2_classLit, 1), $intern_6, 144, 0, [SENT, RECEIVED, READ, UNSUPPORTED_VALUE_1]);
}

defineClass(144, 18, {144:1, 3:1, 29:1, 18:1}, MessageState);
_.value_0 = 0;
var READ, RECEIVED, SENT, UNSUPPORTED_VALUE_1;
var Lim_actor_model_api_MessageState_2_classLit = createForEnum('im.actor.model.api', 'MessageState', 144, Ljava_lang_Enum_2_classLit, values_3);
function MessageUnsupported(key, content_0){
  this.key = key;
  this.content_0 = content_0;
}

defineClass(749, 86, {86:1, 4:1}, MessageUnsupported);
_.getHeader = function getHeader_4(){
  return this.key;
}
;
_.parse_0 = function parse_22(values){
  throw new IOException_0('Parsing is unsupported');
}
;
_.serialize = function serialize_18(writer){
  $writeInt(writer, 1, this.key);
  $writeBytes(writer, 2, this.content_0);
}
;
_.key = 0;
var Lim_actor_model_api_MessageUnsupported_2_classLit = createForClass('im.actor.model.api', 'MessageUnsupported', 749, Lim_actor_model_api_Message_2_classLit);
function OutPeer(){
}

function OutPeer_0(type_0, id_0, accessHash){
  this.type_0 = type_0;
  this.id_0 = id_0;
  this.accessHash = accessHash;
}

defineClass(31, 4, {31:1, 4:1}, OutPeer, OutPeer_0);
_.parse_0 = function parse_23(values){
  this.type_0 = parse_26(convertInt($getLong(values, 1)));
  this.id_0 = convertInt($getLong(values, 2));
  this.accessHash = $getLong(values, 3);
}
;
_.serialize = function serialize_19(writer){
  if (!this.type_0) {
    throw new IOException;
  }
  $writeVarIntField(writer, 1, fromInt(this.type_0.value_0));
  $writeVarIntField(writer, 2, fromInt(this.id_0));
  $writeVarIntField(writer, 3, this.accessHash);
}
;
_.toString$ = function toString_22(){
  var res;
  res = 'struct OutPeer{';
  res += 'type=' + this.type_0;
  res += ', id=' + this.id_0;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.id_0 = 0;
var Lim_actor_model_api_OutPeer_2_classLit = createForClass('im.actor.model.api', 'OutPeer', 31, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Parameter(){
}

defineClass(427, 4, {427:1, 4:1}, Parameter);
_.parse_0 = function parse_24(values){
  this.key = convertString($getBytes(values, 1));
  this.value_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_20(writer){
  if (this.key == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.key);
  if (this.value_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.value_0);
}
;
_.toString$ = function toString_23(){
  var res;
  res = 'struct Parameter{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_Parameter_2_classLit = createForClass('im.actor.model.api', 'Parameter', 427, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Peer(){
}

function Peer_0(type_0, id_0){
  this.type_0 = type_0;
  this.id_0 = id_0;
}

defineClass(24, 4, {24:1, 4:1}, Peer, Peer_0);
_.parse_0 = function parse_25(values){
  this.type_0 = parse_26(convertInt($getLong(values, 1)));
  this.id_0 = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_21(writer){
  if (!this.type_0) {
    throw new IOException;
  }
  $writeVarIntField(writer, 1, fromInt(this.type_0.value_0));
  $writeVarIntField(writer, 2, fromInt(this.id_0));
}
;
_.toString$ = function toString_24(){
  var res;
  res = 'struct Peer{';
  res += 'type=' + this.type_0;
  res += ', id=' + this.id_0;
  res += '}';
  return res;
}
;
_.id_0 = 0;
var Lim_actor_model_api_Peer_2_classLit = createForClass('im.actor.model.api', 'Peer', 24, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_PeerType(){
  $clinit_PeerType = emptyMethod;
  PRIVATE = new PeerType('PRIVATE', 0, 1);
  GROUP = new PeerType('GROUP', 1, 2);
  EMAIL = new PeerType('EMAIL', 2, 3);
  UNSUPPORTED_VALUE_2 = new PeerType('UNSUPPORTED_VALUE', 3, -1);
}

function PeerType(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function parse_26(value_0){
  $clinit_PeerType();
  switch (value_0) {
    case 1:
      return PRIVATE;
    case 2:
      return GROUP;
    case 3:
      return EMAIL;
    default:return UNSUPPORTED_VALUE_2;
  }
}

function values_4(){
  $clinit_PeerType();
  return initValues(getClassLiteralForArray(Lim_actor_model_api_PeerType_2_classLit, 1), $intern_6, 123, 0, [PRIVATE, GROUP, EMAIL, UNSUPPORTED_VALUE_2]);
}

defineClass(123, 18, {123:1, 3:1, 29:1, 18:1}, PeerType);
_.value_0 = 0;
var EMAIL, GROUP, PRIVATE, UNSUPPORTED_VALUE_2;
var Lim_actor_model_api_PeerType_2_classLit = createForEnum('im.actor.model.api', 'PeerType', 123, Ljava_lang_Enum_2_classLit, values_4);
function Phone(){
}

defineClass(467, 4, $intern_14, Phone);
_.parse_0 = function parse_27(values){
  this.id_0 = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.phone = $getLong(values, 3);
  this.phoneTitle = convertString($getBytes(values, 4));
}
;
_.serialize = function serialize_22(writer){
  $writeInt(writer, 1, this.id_0);
  $writeLong(writer, 2, this.accessHash);
  $writeLong(writer, 3, this.phone);
  if (this.phoneTitle == null) {
    throw new IOException;
  }
  $writeString(writer, 4, this.phoneTitle);
}
;
_.toString$ = function toString_25(){
  var res;
  res = 'struct Phone{';
  res += 'id=' + this.id_0;
  res += ', phone=' + toString_2(this.phone);
  res += ', phoneTitle=' + this.phoneTitle;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.id_0 = 0;
_.phone = {l:0, m:0, h:0};
var Lim_actor_model_api_Phone_2_classLit = createForClass('im.actor.model.api', 'Phone', 467, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function PhoneToImport(){
}

function PhoneToImport_0(phoneNumber, name_0){
  this.phoneNumber = phoneNumber;
  this.name_0 = name_0;
}

defineClass(183, 4, {183:1, 4:1}, PhoneToImport, PhoneToImport_0);
_.parse_0 = function parse_28(values){
  this.phoneNumber = $getLong(values, 1);
  this.name_0 = convertString($getBytes_0(values, 2));
}
;
_.serialize = function serialize_23(writer){
  $writeLong(writer, 1, this.phoneNumber);
  this.name_0 != null && $writeString(writer, 2, this.name_0);
}
;
_.toString$ = function toString_26(){
  var res;
  res = 'struct PhoneToImport{';
  res += 'phoneNumber=' + toString_2(this.phoneNumber);
  res += ', name=' + this.name_0;
  res += '}';
  return res;
}
;
_.phoneNumber = {l:0, m:0, h:0};
var Lim_actor_model_api_PhoneToImport_2_classLit = createForClass('im.actor.model.api', 'PhoneToImport', 183, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $buildContainer_1(this$static){
  var res, writer;
  res = new DataOutput;
  writer = new BserWriter(res);
  $writeInt(writer, 1, this$static.getHeader());
  $writeBytes(writer, 2, $toByteArray(this$static));
  return $toByteArray_0(res);
}

function fromBytes_1(src_0){
  var content_0, key, values;
  values = new BserValues(deserialize(new DataInput_0(src_0, 0, src_0.length)));
  key = convertInt($getLong(values, 1));
  content_0 = $getBytes(values, 2);
  switch (key) {
    case 1:
      return dynamicCast(parse_159(new ServiceExUserAdded, content_0), 46);
    case 2:
      return dynamicCast(parse_159(new ServiceExUserKicked, content_0), 46);
    case 3:
      return dynamicCast(parse_159(new ServiceExUserLeft, content_0), 46);
    case 4:
      return dynamicCast(parse_159(new ServiceExGroupCreated, content_0), 46);
    case 5:
      return dynamicCast(parse_159(new ServiceExChangedTitle, content_0), 46);
    case 6:
      return dynamicCast(parse_159(new ServiceExChangedAvatar, content_0), 46);
    case 7:
      return dynamicCast(parse_159(new ServiceExEmailContactRegistered, content_0), 46);
    default:return new ServiceExUnsupported(key, content_0);
  }
}

defineClass(46, 4, $intern_16);
var Lim_actor_model_api_ServiceEx_2_classLit = createForClass('im.actor.model.api', 'ServiceEx', 46, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function ServiceExChangedAvatar(){
}

defineClass(329, 46, {46:1, 329:1, 4:1}, ServiceExChangedAvatar);
_.getHeader = function getHeader_5(){
  return 6;
}
;
_.parse_0 = function parse_29(values){
  this.avatar = dynamicCast($optObj(values, 1, new Avatar), 50);
}
;
_.serialize = function serialize_24(writer){
  !!this.avatar && $writeObject(writer, 1, this.avatar);
}
;
_.toString$ = function toString_27(){
  var res;
  res = 'struct ServiceExChangedAvatar{';
  res += 'avatar=' + (this.avatar?'set':'empty');
  res += '}';
  return res;
}
;
var Lim_actor_model_api_ServiceExChangedAvatar_2_classLit = createForClass('im.actor.model.api', 'ServiceExChangedAvatar', 329, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceExChangedTitle(){
}

defineClass(330, 46, {46:1, 330:1, 4:1}, ServiceExChangedTitle);
_.getHeader = function getHeader_6(){
  return 5;
}
;
_.parse_0 = function parse_30(values){
  this.title_0 = convertString($getBytes(values, 1));
}
;
_.serialize = function serialize_25(writer){
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.title_0);
}
;
_.toString$ = function toString_28(){
  var res;
  res = 'struct ServiceExChangedTitle{';
  res += 'title=' + this.title_0;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_ServiceExChangedTitle_2_classLit = createForClass('im.actor.model.api', 'ServiceExChangedTitle', 330, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceExEmailContactRegistered(){
}

defineClass(754, 46, $intern_16, ServiceExEmailContactRegistered);
_.getHeader = function getHeader_7(){
  return 7;
}
;
_.parse_0 = function parse_31(values){
  this.uid = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_26(writer){
  $writeInt(writer, 1, this.uid);
}
;
_.toString$ = function toString_29(){
  var res;
  res = 'struct ServiceExEmailContactRegistered{';
  res += 'uid=' + this.uid;
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_ServiceExEmailContactRegistered_2_classLit = createForClass('im.actor.model.api', 'ServiceExEmailContactRegistered', 754, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceExGroupCreated(){
}

defineClass(432, 46, {46:1, 432:1, 4:1}, ServiceExGroupCreated);
_.getHeader = function getHeader_8(){
  return 4;
}
;
_.parse_0 = function parse_32(values){
}
;
_.serialize = function serialize_27(writer){
}
;
_.toString$ = function toString_30(){
  var res;
  res = 'struct ServiceExGroupCreated{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_ServiceExGroupCreated_2_classLit = createForClass('im.actor.model.api', 'ServiceExGroupCreated', 432, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceExUnsupported(key, content_0){
  this.key = key;
  this.content_0 = content_0;
}

defineClass(755, 46, $intern_16, ServiceExUnsupported);
_.getHeader = function getHeader_9(){
  return this.key;
}
;
_.parse_0 = function parse_33(values){
  throw new IOException_0('Parsing is unsupported');
}
;
_.serialize = function serialize_28(writer){
  $writeInt(writer, 1, this.key);
  $writeBytes(writer, 2, this.content_0);
}
;
_.key = 0;
var Lim_actor_model_api_ServiceExUnsupported_2_classLit = createForClass('im.actor.model.api', 'ServiceExUnsupported', 755, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceExUserAdded(){
}

defineClass(331, 46, {46:1, 331:1, 4:1}, ServiceExUserAdded);
_.getHeader = function getHeader_10(){
  return 1;
}
;
_.parse_0 = function parse_34(values){
  this.addedUid = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_29(writer){
  $writeInt(writer, 1, this.addedUid);
}
;
_.toString$ = function toString_31(){
  var res;
  res = 'struct ServiceExUserAdded{';
  res += 'addedUid=' + this.addedUid;
  res += '}';
  return res;
}
;
_.addedUid = 0;
var Lim_actor_model_api_ServiceExUserAdded_2_classLit = createForClass('im.actor.model.api', 'ServiceExUserAdded', 331, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceExUserKicked(){
}

defineClass(332, 46, {46:1, 332:1, 4:1}, ServiceExUserKicked);
_.getHeader = function getHeader_11(){
  return 2;
}
;
_.parse_0 = function parse_35(values){
  this.kickedUid = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_30(writer){
  $writeInt(writer, 1, this.kickedUid);
}
;
_.toString$ = function toString_32(){
  var res;
  res = 'struct ServiceExUserKicked{';
  res += 'kickedUid=' + this.kickedUid;
  res += '}';
  return res;
}
;
_.kickedUid = 0;
var Lim_actor_model_api_ServiceExUserKicked_2_classLit = createForClass('im.actor.model.api', 'ServiceExUserKicked', 332, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceExUserLeft(){
}

defineClass(431, 46, {46:1, 431:1, 4:1}, ServiceExUserLeft);
_.getHeader = function getHeader_12(){
  return 3;
}
;
_.parse_0 = function parse_36(values){
}
;
_.serialize = function serialize_31(writer){
}
;
_.toString$ = function toString_33(){
  var res;
  res = 'struct ServiceExUserLeft{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_ServiceExUserLeft_2_classLit = createForClass('im.actor.model.api', 'ServiceExUserLeft', 431, Lim_actor_model_api_ServiceEx_2_classLit);
function ServiceMessage(){
}

defineClass(328, 86, {86:1, 328:1, 4:1}, ServiceMessage);
_.getHeader = function getHeader_13(){
  return 2;
}
;
_.parse_0 = function parse_37(values){
  this.text_0 = convertString($getBytes(values, 1));
  $getBytes_0(values, 3) != null && (this.ext = fromBytes_1($getBytes(values, 3)));
}
;
_.serialize = function serialize_32(writer){
  if (this.text_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.text_0);
  !!this.ext && $writeBytes(writer, 3, $buildContainer_1(this.ext));
}
;
_.toString$ = function toString_34(){
  var res;
  res = 'struct ServiceMessage{';
  res += 'text=' + this.text_0;
  res += ', ext=' + (this.ext?'set':'empty');
  res += '}';
  return res;
}
;
var Lim_actor_model_api_ServiceMessage_2_classLit = createForClass('im.actor.model.api', 'ServiceMessage', 328, Lim_actor_model_api_Message_2_classLit);
function $clinit_Sex(){
  $clinit_Sex = emptyMethod;
  UNKNOWN = new Sex('UNKNOWN', 0, 1);
  MALE = new Sex('MALE', 1, 2);
  FEMALE = new Sex('FEMALE', 2, 3);
  UNSUPPORTED_VALUE_3 = new Sex('UNSUPPORTED_VALUE', 3, -1);
}

function Sex(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function parse_38(value_0){
  $clinit_Sex();
  switch (value_0) {
    case 1:
      return UNKNOWN;
    case 2:
      return MALE;
    case 3:
      return FEMALE;
    default:return UNSUPPORTED_VALUE_3;
  }
}

function values_5(){
  $clinit_Sex();
  return initValues(getClassLiteralForArray(Lim_actor_model_api_Sex_2_classLit, 1), $intern_6, 145, 0, [UNKNOWN, MALE, FEMALE, UNSUPPORTED_VALUE_3]);
}

defineClass(145, 18, {145:1, 3:1, 29:1, 18:1}, Sex);
_.value_0 = 0;
var FEMALE, MALE, UNKNOWN, UNSUPPORTED_VALUE_3;
var Lim_actor_model_api_Sex_2_classLit = createForEnum('im.actor.model.api', 'Sex', 145, Ljava_lang_Enum_2_classLit, values_5);
function TextMessage(){
}

function TextMessage_0(text_0){
  this.text_0 = text_0;
  this.ext = null;
}

defineClass(195, 86, {86:1, 195:1, 4:1}, TextMessage, TextMessage_0);
_.getHeader = function getHeader_14(){
  return 1;
}
;
_.parse_0 = function parse_39(values){
  this.text_0 = convertString($getBytes(values, 1));
  $getBytes_0(values, 3) != null && (this.ext = fromBytes_2($getBytes(values, 3)));
}
;
_.serialize = function serialize_33(writer){
  if (this.text_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.text_0);
  !!this.ext && $writeBytes(writer, 3, $buildContainer_2(this.ext));
}
;
_.toString$ = function toString_35(){
  var res;
  res = 'struct TextMessage{';
  res += 'text=' + this.text_0;
  res += ', ext=' + this.ext;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_TextMessage_2_classLit = createForClass('im.actor.model.api', 'TextMessage', 195, Lim_actor_model_api_Message_2_classLit);
function $buildContainer_2(this$static){
  var res, writer;
  res = new DataOutput;
  writer = new BserWriter(res);
  $writeInt(writer, 1, this$static.key);
  $writeBytes(writer, 2, $toByteArray(this$static));
  return $toByteArray_0(res);
}

function fromBytes_2(src_0){
  var content_0, key, values;
  values = new BserValues(deserialize(new DataInput_0(src_0, 0, src_0.length)));
  key = convertInt($getLong(values, 1));
  content_0 = $getBytes(values, 2);
  return new TextMessageExUnsupported(key, content_0);
}

defineClass(799, 4, $intern_14);
var Lim_actor_model_api_TextMessageEx_2_classLit = createForClass('im.actor.model.api', 'TextMessageEx', 799, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function TextMessageExUnsupported(key, content_0){
  this.key = key;
  this.content_0 = content_0;
}

defineClass(746, 799, $intern_14, TextMessageExUnsupported);
_.parse_0 = function parse_40(values){
  throw new IOException_0('Parsing is unsupported');
}
;
_.serialize = function serialize_34(writer){
  $writeInt(writer, 1, this.key);
  $writeBytes(writer, 2, this.content_0);
}
;
_.key = 0;
var Lim_actor_model_api_TextMessageExUnsupported_2_classLit = createForClass('im.actor.model.api', 'TextMessageExUnsupported', 746, Lim_actor_model_api_TextMessageEx_2_classLit);
function $clinit_TypingType(){
  $clinit_TypingType = emptyMethod;
  TEXT = new TypingType('TEXT', 0, 0);
  UNSUPPORTED_VALUE_4 = new TypingType('UNSUPPORTED_VALUE', 1, -1);
}

function TypingType(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function parse_41(value_0){
  $clinit_TypingType();
  switch (value_0) {
    case 0:
      return TEXT;
    default:return UNSUPPORTED_VALUE_4;
  }
}

function values_6(){
  $clinit_TypingType();
  return initValues(getClassLiteralForArray(Lim_actor_model_api_TypingType_2_classLit, 1), $intern_6, 289, 0, [TEXT, UNSUPPORTED_VALUE_4]);
}

defineClass(289, 18, {289:1, 3:1, 29:1, 18:1}, TypingType);
_.value_0 = 0;
var TEXT, UNSUPPORTED_VALUE_4;
var Lim_actor_model_api_TypingType_2_classLit = createForEnum('im.actor.model.api', 'TypingType', 289, Ljava_lang_Enum_2_classLit, values_6);
function User(){
}

defineClass(58, 4, {58:1, 4:1}, User);
_.parse_0 = function parse_42(values){
  var val_sex;
  this.id_0 = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.name_0 = convertString($getBytes(values, 3));
  this.localName = convertString($getBytes_0(values, 4));
  val_sex = convertInt($getLong_0(values, 5));
  val_sex != 0 && (this.sex = parse_38(val_sex));
  this.keyHashes = $getRepeatedLong(values, 6);
  this.phone = $getLong(values, 7);
  this.avatar = dynamicCast($optObj(values, 8, new Avatar), 50);
  this.phones = $getRepeatedInt(values, 9);
  this.emails = $getRepeatedInt(values, 10);
}
;
_.serialize = function serialize_35(writer){
  $writeInt(writer, 1, this.id_0);
  $writeLong(writer, 2, this.accessHash);
  if (this.name_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.name_0);
  this.localName != null && $writeString(writer, 4, this.localName);
  !!this.sex && $writeInt(writer, 5, this.sex.value_0);
  $writeRepeatedLong(writer, 6, this.keyHashes);
  $writeLong(writer, 7, this.phone);
  !!this.avatar && $writeObject(writer, 8, this.avatar);
  $writeRepeatedInt(writer, 9, this.phones);
  $writeRepeatedInt(writer, 10, this.emails);
}
;
_.toString$ = function toString_36(){
  var res;
  res = 'struct User{';
  res += 'id=' + this.id_0;
  res += ', name=' + this.name_0;
  res += ', localName=' + this.localName;
  res += ', sex=' + this.sex;
  res += ', keyHashes=' + this.keyHashes.array.length;
  res += ', avatar=' + (this.avatar?'set':'empty');
  res += ', phones=' + this.phones.array.length;
  res += ', emails=' + this.emails.array.length;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.id_0 = 0;
_.phone = {l:0, m:0, h:0};
var Lim_actor_model_api_User_2_classLit = createForClass('im.actor.model.api', 'User', 58, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function UserOutPeer(){
}

function UserOutPeer_0(uid, accessHash){
  this.uid = uid;
  this.accessHash = accessHash;
}

defineClass(69, 4, {69:1, 4:1}, UserOutPeer, UserOutPeer_0);
_.parse_0 = function parse_43(values){
  this.uid = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
}
;
_.serialize = function serialize_36(writer){
  $writeInt(writer, 1, this.uid);
  $writeLong(writer, 2, this.accessHash);
}
;
_.toString$ = function toString_37(){
  var res;
  res = 'struct UserOutPeer{';
  res += 'uid=' + this.uid;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_UserOutPeer_2_classLit = createForClass('im.actor.model.api', 'UserOutPeer', 69, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
defineClass(787, 4, $intern_14);
var Lim_actor_model_network_parser_HeaderBserObject_2_classLit = createForClass('im.actor.model.network.parser', 'HeaderBserObject', 787, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
defineClass(788, 787, $intern_14);
var Lim_actor_model_network_parser_RpcScope_2_classLit = createForClass('im.actor.model.network.parser', 'RpcScope', 788, Lim_actor_model_network_parser_HeaderBserObject_2_classLit);
function FatSeqUpdate(){
}

function FatSeqUpdate_0(seq, state, update, users, groups, phones, emails){
  this.seq = seq;
  this.state = state;
  this.updateHeader = 40;
  this.update = update;
  this.users = users;
  this.groups = groups;
  this.phones = phones;
  this.emails = emails;
}

defineClass(57, 788, {57:1, 4:1}, FatSeqUpdate, FatSeqUpdate_0);
_.getHeaderKey = function getHeaderKey(){
  return 73;
}
;
_.parse_0 = function parse_44(values){
  var _emails, _groups, _phones, _users, i_0, i0, i1, i2;
  this.seq = convertInt($getLong(values, 1));
  this.state = $getBytes(values, 2);
  this.updateHeader = convertInt($getLong(values, 3));
  this.update = $getBytes(values, 4);
  _users = new ArrayList;
  for (i0 = 0; i0 < $getRepeatedCount(values, 5); i0++) {
    $add_0(_users, new User);
  }
  this.users = $getRepeatedObj(values, 5, _users);
  _groups = new ArrayList;
  for (i1 = 0; i1 < $getRepeatedCount(values, 6); i1++) {
    $add_0(_groups, new Group);
  }
  this.groups = $getRepeatedObj(values, 6, _groups);
  _phones = new ArrayList;
  for (i2 = 0; i2 < $getRepeatedCount(values, 7); i2++) {
    $add_0(_phones, new Phone);
  }
  this.phones = $getRepeatedObj(values, 7, _phones);
  _emails = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 8); i_0++) {
    $add_0(_emails, new Email);
  }
  this.emails = $getRepeatedObj(values, 8, _emails);
}
;
_.serialize = function serialize_37(writer){
  $writeInt(writer, 1, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.state);
  $writeInt(writer, 3, this.updateHeader);
  if (this.update == null) {
    throw new IOException;
  }
  $writeBytes(writer, 4, this.update);
  $writeRepeatedObj(writer, 5, this.users);
  $writeRepeatedObj(writer, 6, this.groups);
  $writeRepeatedObj(writer, 7, this.phones);
  $writeRepeatedObj(writer, 8, this.emails);
}
;
_.toString$ = function toString_38(){
  var res;
  res = 'update box FatSeqUpdate{';
  res += 'seq=' + this.seq;
  res += ', state=' + byteArrayToStringCompact(this.state);
  res += ', updateHeader=' + this.updateHeader;
  res += ', update=' + byteArrayToStringCompact(this.update);
  res += ', users=' + this.users.array.length;
  res += ', groups=' + this.groups.array.length;
  res += '}';
  return res;
}
;
_.seq = 0;
_.updateHeader = 0;
var Lim_actor_model_api_base_FatSeqUpdate_2_classLit = createForClass('im.actor.model.api.base', 'FatSeqUpdate', 57, Lim_actor_model_network_parser_RpcScope_2_classLit);
function SeqUpdate(){
}

function SeqUpdate_0(seq, state, updateHeader, update){
  this.seq = seq;
  this.state = state;
  this.updateHeader = updateHeader;
  this.update = update;
}

defineClass(56, 788, {56:1, 4:1}, SeqUpdate, SeqUpdate_0);
_.getHeaderKey = function getHeaderKey_0(){
  return 13;
}
;
_.parse_0 = function parse_45(values){
  this.seq = convertInt($getLong(values, 1));
  this.state = $getBytes(values, 2);
  this.updateHeader = convertInt($getLong(values, 3));
  this.update = $getBytes(values, 4);
}
;
_.serialize = function serialize_38(writer){
  $writeInt(writer, 1, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.state);
  $writeInt(writer, 3, this.updateHeader);
  if (this.update == null) {
    throw new IOException;
  }
  $writeBytes(writer, 4, this.update);
}
;
_.toString$ = function toString_39(){
  var res;
  res = 'update box SeqUpdate{';
  res += 'seq=' + this.seq;
  res += ', state=' + byteArrayToStringCompact(this.state);
  res += ', updateHeader=' + this.updateHeader;
  res += ', update=' + byteArrayToStringCompact(this.update);
  res += '}';
  return res;
}
;
_.seq = 0;
_.updateHeader = 0;
var Lim_actor_model_api_base_SeqUpdate_2_classLit = createForClass('im.actor.model.api.base', 'SeqUpdate', 56, Lim_actor_model_network_parser_RpcScope_2_classLit);
function SeqUpdateTooLong(){
}

defineClass(288, 788, {288:1, 4:1}, SeqUpdateTooLong);
_.getHeaderKey = function getHeaderKey_1(){
  return 25;
}
;
_.parse_0 = function parse_46(values){
}
;
_.serialize = function serialize_39(writer){
}
;
_.toString$ = function toString_40(){
  var res;
  res = 'update box SeqUpdateTooLong{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_base_SeqUpdateTooLong_2_classLit = createForClass('im.actor.model.api.base', 'SeqUpdateTooLong', 288, Lim_actor_model_network_parser_RpcScope_2_classLit);
function WeakUpdate(){
}

defineClass(136, 788, {136:1, 4:1}, WeakUpdate);
_.getHeaderKey = function getHeaderKey_2(){
  return 26;
}
;
_.parse_0 = function parse_47(values){
  this.date = $getLong(values, 1);
  this.updateHeader = convertInt($getLong(values, 2));
  this.update = $getBytes(values, 3);
}
;
_.serialize = function serialize_40(writer){
  $writeLong(writer, 1, this.date);
  $writeInt(writer, 2, this.updateHeader);
  if (this.update == null) {
    throw new IOException;
  }
  $writeBytes(writer, 3, this.update);
}
;
_.toString$ = function toString_41(){
  var res;
  res = 'update box WeakUpdate{';
  res += 'date=' + toString_2(this.date);
  res += ', updateHeader=' + this.updateHeader;
  res += ', update=' + byteArrayToStringCompact(this.update);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.updateHeader = 0;
var Lim_actor_model_api_base_WeakUpdate_2_classLit = createForClass('im.actor.model.api.base', 'WeakUpdate', 136, Lim_actor_model_network_parser_RpcScope_2_classLit);
function $read(type_0, payload){
  switch (type_0) {
    case 1:
      return dynamicCast(parse_159(new RequestSendAuthCode, payload), 247);
    case 90:
      return dynamicCast(parse_159(new RequestSendAuthCall, payload), 382);
    case 3:
      return dynamicCast(parse_159(new RequestSignIn, payload), 248);
    case 4:
      return dynamicCast(parse_159(new RequestSignUp, payload), 383);
    case 80:
      return dynamicCast(parse_159(new RequestGetAuthSessions, payload), 384);
    case 82:
      return dynamicCast(parse_159(new RequestTerminateSession, payload), 385);
    case 83:
      return dynamicCast(parse_159(new RequestTerminateAllSessions, payload), 386);
    case 84:
      return dynamicCast(parse_159(new RequestSignOut, payload), 387);
    case 96:
      return dynamicCast(parse_159(new RequestEditUserLocalName, payload), 388);
    case 53:
      return dynamicCast(parse_159(new RequestEditName, payload), 389);
    case 31:
      return dynamicCast(parse_159(new RequestEditAvatar, payload), 370);
    case 91:
      return dynamicCast(parse_159(new RequestRemoveAvatar, payload), 372);
    case 120:
      return dynamicCast(parse_159(new RequestSendEmailCode, payload), 390);
    case 123:
      return dynamicCast(parse_159(new RequestDetachEmail, payload), 391);
    case 124:
      return dynamicCast(parse_159(new RequestChangePhoneTitle, payload), 392);
    case 125:
      return dynamicCast(parse_159(new RequestChangeEmailTitle, payload), 393);
    case 7:
      return dynamicCast(parse_159(new RequestImportContacts, payload), 314);
    case 87:
      return dynamicCast(parse_159(new RequestGetContacts, payload), 300);
    case 89:
      return dynamicCast(parse_159(new RequestRemoveContact, payload), 394);
    case 114:
      return dynamicCast(parse_159(new RequestAddContact, payload), 395);
    case 112:
      return dynamicCast(parse_159(new RequestSearchContacts, payload), 396);
    case 92:
      return dynamicCast(parse_159(new RequestSendMessage, payload), 305);
    case 55:
      return dynamicCast(parse_159(new RequestMessageReceived, payload), 303);
    case 57:
      return dynamicCast(parse_159(new RequestMessageRead, payload), 302);
    case 98:
      return dynamicCast(parse_159(new RequestDeleteMessage, payload), 306);
    case 99:
      return dynamicCast(parse_159(new RequestClearChat, payload), 245);
    case 100:
      return dynamicCast(parse_159(new RequestDeleteChat, payload), 246);
    case 118:
      return dynamicCast(parse_159(new RequestLoadHistory, payload), 290);
    case 104:
      return dynamicCast(parse_159(new RequestLoadDialogs, payload), 301);
    case 65:
      return dynamicCast(parse_159(new RequestCreateGroup, payload), 397);
    case 85:
      return dynamicCast(parse_159(new RequestEditGroupTitle, payload), 398);
    case 86:
      return dynamicCast(parse_159(new RequestEditGroupAvatar, payload), 371);
    case 101:
      return dynamicCast(parse_159(new RequestRemoveGroupAvatar, payload), 399);
    case 69:
      return dynamicCast(parse_159(new RequestInviteUser, payload), 400);
    case 70:
      return dynamicCast(parse_159(new RequestLeaveGroup, payload), 401);
    case 71:
      return dynamicCast(parse_159(new RequestKickUser, payload), 402);
    case 27:
      return dynamicCast(parse_159(new RequestTyping, payload), 298);
    case 29:
      return dynamicCast(parse_159(new RequestSetOnline, payload), 297);
    case 77:
      return dynamicCast(parse_159(new RequestGetFileUrl, payload), 277);
    case 97:
      return dynamicCast(parse_159(new RequestGetFileUploadUrl, payload), 403);
    case 122:
      return dynamicCast(parse_159(new RequestCommitFileUpload, payload), 404);
    case 142:
      return dynamicCast(parse_159(new RequestGetFileUploadPartUrl, payload), 405);
    case 134:
      return dynamicCast(parse_159(new RequestGetParameters, payload), 282);
    case 128:
      return dynamicCast(parse_159(new RequestEditParameter, payload), 299);
    case 51:
      return dynamicCast(parse_159(new RequestRegisterGooglePush, payload), 291);
    case 76:
      return dynamicCast(parse_159(new RequestRegisterApplePush, payload), 292);
    case 52:
      return dynamicCast(parse_159(new RequestUnregisterPush, payload), 406);
    case 9:
      return dynamicCast(parse_159(new RequestGetState, payload), 307);
    case 11:
      return dynamicCast(parse_159(new RequestGetDifference, payload), 308);
    case 32:
      return dynamicCast(parse_159(new RequestSubscribeToOnline, payload), 199);
    case 33:
      return dynamicCast(parse_159(new RequestSubscribeFromOnline, payload), 407);
    case 74:
      return dynamicCast(parse_159(new RequestSubscribeToGroupOnline, payload), 200);
    case 75:
      return dynamicCast(parse_159(new RequestSubscribeFromGroupOnline, payload), 408);
    case 2:
      return dynamicCast(parse_159(new ResponseSendAuthCode, payload), 315);
    case 5:
      return dynamicCast(parse_159(new ResponseAuth, payload), 316);
    case 81:
      return dynamicCast(parse_159(new ResponseGetAuthSessions, payload), 409);
    case 103:
      return dynamicCast(parse_159(new ResponseEditAvatar, payload), 410);
    case 8:
      return dynamicCast(parse_159(new ResponseImportContacts, payload), 317);
    case 88:
      return dynamicCast(parse_159(new ResponseGetContacts, payload), 284);
    case 113:
      return dynamicCast(parse_159(new ResponseSearchContacts, payload), 411);
    case 119:
      return dynamicCast(parse_159(new ResponseLoadHistory, payload), 318);
    case 105:
      return dynamicCast(parse_159(new ResponseLoadDialogs, payload), 319);
    case 66:
      return dynamicCast(parse_159(new ResponseCreateGroup, payload), 412);
    case 115:
      return dynamicCast(parse_159(new ResponseEditGroupAvatar, payload), 413);
    case 78:
      return dynamicCast(parse_159(new ResponseGetFileUrl, payload), 320);
    case 121:
      return dynamicCast(parse_159(new ResponseGetFileUploadUrl, payload), 414);
    case 138:
      return dynamicCast(parse_159(new ResponseCommitFileUpload, payload), 415);
    case 141:
      return dynamicCast(parse_159(new ResponseGetFileUploadPartUrl, payload), 416);
    case 135:
      return dynamicCast(parse_159(new ResponseGetParameters, payload), 321);
    case 12:
      return dynamicCast(parse_159(new ResponseGetDifference, payload), 322);
    case 50:
      return dynamicCast(parse_159(new ResponseVoid, payload), 88);
    case 72:
      return dynamicCast(parse_159(new ResponseSeq, payload), 89);
    case 102:
      return dynamicCast(parse_159(new ResponseSeqDate, payload), 323);
    case 13:
      return dynamicCast(parse_159(new SeqUpdate, payload), 56);
    case 73:
      return dynamicCast(parse_159(new FatSeqUpdate, payload), 57);
    case 26:
      return dynamicCast(parse_159(new WeakUpdate, payload), 136);
    case 25:
      return dynamicCast(parse_159(new SeqUpdateTooLong, payload), 288);
  }
  throw new IOException;
}

function $read_0(type_0, payload){
  switch (type_0) {
    case 16:
      return dynamicCast(parse_159(new UpdateUserAvatarChanged, payload), 202);
    case 32:
      return dynamicCast(parse_159(new UpdateUserNameChanged, payload), 203);
    case 51:
      return dynamicCast(parse_159(new UpdateUserLocalNameChanged, payload), 106);
    case 87:
      return dynamicCast(parse_159(new UpdateUserPhoneAdded, payload), 417);
    case 88:
      return dynamicCast(parse_159(new UpdateUserPhoneRemoved, payload), 418);
    case 89:
      return dynamicCast(parse_159(new UpdatePhoneTitleChanged, payload), 419);
    case 101:
      return dynamicCast(parse_159(new UpdatePhoneMoved, payload), 420);
    case 96:
      return dynamicCast(parse_159(new UpdateUserEmailAdded, payload), 421);
    case 97:
      return dynamicCast(parse_159(new UpdateUserEmailRemoved, payload), 422);
    case 98:
      return dynamicCast(parse_159(new UpdateEmailTitleChanged, payload), 423);
    case 102:
      return dynamicCast(parse_159(new UpdateEmailMoved, payload), 424);
    case 86:
      return dynamicCast(parse_159(new UpdateUserContactsChanged, payload), 425);
    case 5:
      return dynamicCast(parse_159(new UpdateContactRegistered, payload), 107);
    case 40:
      return dynamicCast(parse_159(new UpdateContactsAdded, payload), 90);
    case 41:
      return dynamicCast(parse_159(new UpdateContactsRemoved, payload), 108);
    case 55:
      return dynamicCast(parse_159(new UpdateMessage, payload), 109);
    case 4:
      return dynamicCast(parse_159(new UpdateMessageSent, payload), 138);
    case 54:
      return dynamicCast(parse_159(new UpdateMessageReceived, payload), 204);
    case 19:
      return dynamicCast(parse_159(new UpdateMessageRead, payload), 205);
    case 50:
      return dynamicCast(parse_159(new UpdateMessageReadByMe, payload), 206);
    case 46:
      return dynamicCast(parse_159(new UpdateMessageDelete, payload), 139);
    case 47:
      return dynamicCast(parse_159(new UpdateChatClear, payload), 140);
    case 48:
      return dynamicCast(parse_159(new UpdateChatDelete, payload), 141);
    case 36:
      return dynamicCast(parse_159(new UpdateGroupInvite, payload), 110);
    case 21:
      return dynamicCast(parse_159(new UpdateGroupUserAdded, payload), 111);
    case 23:
      return dynamicCast(parse_159(new UpdateGroupUserLeave, payload), 112);
    case 24:
      return dynamicCast(parse_159(new UpdateGroupUserKick, payload), 113);
    case 44:
      return dynamicCast(parse_159(new UpdateGroupMembersUpdate, payload), 142);
    case 38:
      return dynamicCast(parse_159(new UpdateGroupTitleChanged, payload), 207);
    case 39:
      return dynamicCast(parse_159(new UpdateGroupAvatarChanged, payload), 208);
    case 6:
      return dynamicCast(parse_159(new UpdateTyping, payload), 209);
    case 7:
      return dynamicCast(parse_159(new UpdateUserOnline, payload), 210);
    case 8:
      return dynamicCast(parse_159(new UpdateUserOffline, payload), 211);
    case 9:
      return dynamicCast(parse_159(new UpdateUserLastSeen, payload), 212);
    case 33:
      return dynamicCast(parse_159(new UpdateGroupOnline, payload), 213);
    case 131:
      return dynamicCast(parse_159(new UpdateParameterChanged, payload), 114);
    case 42:
      return dynamicCast(parse_159(new UpdateConfig, payload), 426);
  }
  throw new IOException;
}

defineClass(789, 788, $intern_14);
var Lim_actor_model_network_parser_Request_2_classLit = createForClass('im.actor.model.network.parser', 'Request', 789, Lim_actor_model_network_parser_RpcScope_2_classLit);
function RequestAddContact(){
}

defineClass(395, 789, {395:1, 4:1}, RequestAddContact);
_.getHeaderKey = function getHeaderKey_3(){
  return 114;
}
;
_.parse_0 = function parse_48(values){
  this.uid = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
}
;
_.serialize = function serialize_41(writer){
  $writeInt(writer, 1, this.uid);
  $writeLong(writer, 2, this.accessHash);
}
;
_.toString$ = function toString_42(){
  var res;
  res = 'rpc AddContact{';
  res += 'uid=' + this.uid;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_rpc_RequestAddContact_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestAddContact', 395, Lim_actor_model_network_parser_Request_2_classLit);
function RequestChangeEmailTitle(){
}

defineClass(393, 789, {393:1, 4:1}, RequestChangeEmailTitle);
_.getHeaderKey = function getHeaderKey_4(){
  return 125;
}
;
_.parse_0 = function parse_49(values){
  this.emailId = convertInt($getLong(values, 1));
  this.title_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_42(writer){
  $writeInt(writer, 1, this.emailId);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.title_0);
}
;
_.toString$ = function toString_43(){
  var res;
  res = 'rpc ChangeEmailTitle{';
  res += 'emailId=' + this.emailId;
  res += ', title=' + this.title_0;
  res += '}';
  return res;
}
;
_.emailId = 0;
var Lim_actor_model_api_rpc_RequestChangeEmailTitle_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestChangeEmailTitle', 393, Lim_actor_model_network_parser_Request_2_classLit);
function RequestChangePhoneTitle(){
}

defineClass(392, 789, {392:1, 4:1}, RequestChangePhoneTitle);
_.getHeaderKey = function getHeaderKey_5(){
  return 124;
}
;
_.parse_0 = function parse_50(values){
  this.phoneId = convertInt($getLong(values, 1));
  this.title_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_43(writer){
  $writeInt(writer, 1, this.phoneId);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.title_0);
}
;
_.toString$ = function toString_44(){
  var res;
  res = 'rpc ChangePhoneTitle{';
  res += 'phoneId=' + this.phoneId;
  res += ', title=' + this.title_0;
  res += '}';
  return res;
}
;
_.phoneId = 0;
var Lim_actor_model_api_rpc_RequestChangePhoneTitle_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestChangePhoneTitle', 392, Lim_actor_model_network_parser_Request_2_classLit);
function RequestClearChat(){
}

function RequestClearChat_0(peer){
  this.peer = peer;
}

defineClass(245, 789, {245:1, 4:1}, RequestClearChat, RequestClearChat_0);
_.getHeaderKey = function getHeaderKey_6(){
  return 99;
}
;
_.parse_0 = function parse_51(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
}
;
_.serialize = function serialize_44(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
}
;
_.toString$ = function toString_45(){
  var res;
  res = 'rpc ClearChat{';
  res += 'peer=' + this.peer;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestClearChat_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestClearChat', 245, Lim_actor_model_network_parser_Request_2_classLit);
function RequestCommitFileUpload(){
}

defineClass(404, 789, {404:1, 4:1}, RequestCommitFileUpload);
_.getHeaderKey = function getHeaderKey_7(){
  return 122;
}
;
_.parse_0 = function parse_52(values){
  this.uploadKey = $getBytes(values, 1);
}
;
_.serialize = function serialize_45(writer){
  if (this.uploadKey == null) {
    throw new IOException;
  }
  $writeBytes(writer, 1, this.uploadKey);
}
;
_.toString$ = function toString_46(){
  var res;
  res = 'rpc CommitFileUpload{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestCommitFileUpload_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestCommitFileUpload', 404, Lim_actor_model_network_parser_Request_2_classLit);
function RequestCreateGroup(){
}

defineClass(397, 789, {397:1, 4:1}, RequestCreateGroup);
_.getHeaderKey = function getHeaderKey_8(){
  return 65;
}
;
_.parse_0 = function parse_53(values){
  var _users, i_0;
  this.rid = $getLong(values, 1);
  this.title_0 = convertString($getBytes(values, 2));
  _users = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 3); i_0++) {
    $add_0(_users, new UserOutPeer);
  }
  this.users = $getRepeatedObj(values, 3, _users);
}
;
_.serialize = function serialize_46(writer){
  $writeLong(writer, 1, this.rid);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.title_0);
  $writeRepeatedObj(writer, 3, this.users);
}
;
_.toString$ = function toString_47(){
  var res;
  res = 'rpc CreateGroup{';
  res += 'rid=' + toString_2(this.rid);
  res += ', title=' + this.title_0;
  res += ', users=' + this.users.array.length;
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestCreateGroup_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestCreateGroup', 397, Lim_actor_model_network_parser_Request_2_classLit);
function RequestDeleteChat(){
}

function RequestDeleteChat_0(peer){
  this.peer = peer;
}

defineClass(246, 789, {246:1, 4:1}, RequestDeleteChat, RequestDeleteChat_0);
_.getHeaderKey = function getHeaderKey_9(){
  return 100;
}
;
_.parse_0 = function parse_54(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
}
;
_.serialize = function serialize_47(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
}
;
_.toString$ = function toString_48(){
  var res;
  res = 'rpc DeleteChat{';
  res += 'peer=' + this.peer;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestDeleteChat_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestDeleteChat', 246, Lim_actor_model_network_parser_Request_2_classLit);
function RequestDeleteMessage(){
}

function RequestDeleteMessage_0(peer, rids){
  this.peer = peer;
  this.rids = rids;
}

defineClass(306, 789, {306:1, 4:1}, RequestDeleteMessage, RequestDeleteMessage_0);
_.getHeaderKey = function getHeaderKey_10(){
  return 98;
}
;
_.parse_0 = function parse_55(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
  this.rids = $getRepeatedLong(values, 3);
}
;
_.serialize = function serialize_48(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeRepeatedLong(writer, 3, this.rids);
}
;
_.toString$ = function toString_49(){
  var res;
  res = 'rpc DeleteMessage{';
  res += 'peer=' + this.peer;
  res += ', rids=' + this.rids;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestDeleteMessage_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestDeleteMessage', 306, Lim_actor_model_network_parser_Request_2_classLit);
function RequestDetachEmail(){
}

defineClass(391, 789, {391:1, 4:1}, RequestDetachEmail);
_.getHeaderKey = function getHeaderKey_11(){
  return 123;
}
;
_.parse_0 = function parse_56(values){
  this.email = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
}
;
_.serialize = function serialize_49(writer){
  $writeInt(writer, 1, this.email);
  $writeLong(writer, 2, this.accessHash);
}
;
_.toString$ = function toString_50(){
  var res;
  res = 'rpc DetachEmail{';
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.email = 0;
var Lim_actor_model_api_rpc_RequestDetachEmail_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestDetachEmail', 391, Lim_actor_model_network_parser_Request_2_classLit);
function RequestEditAvatar(){
}

defineClass(370, 789, {370:1, 4:1}, RequestEditAvatar);
_.getHeaderKey = function getHeaderKey_12(){
  return 31;
}
;
_.parse_0 = function parse_57(values){
  this.fileLocation = dynamicCast($getObj(values, 1, new FileLocation), 65);
}
;
_.serialize = function serialize_50(writer){
  if (!this.fileLocation) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.fileLocation);
}
;
_.toString$ = function toString_51(){
  var res;
  res = 'rpc EditAvatar{';
  res += 'fileLocation=' + (this.fileLocation?'set':'empty');
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestEditAvatar_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestEditAvatar', 370, Lim_actor_model_network_parser_Request_2_classLit);
function RequestEditGroupAvatar(){
}

defineClass(371, 789, {371:1, 4:1}, RequestEditGroupAvatar);
_.getHeaderKey = function getHeaderKey_13(){
  return 86;
}
;
_.parse_0 = function parse_58(values){
  this.groupPeer = dynamicCast($getObj(values, 1, new GroupOutPeer), 38);
  this.rid = $getLong(values, 4);
  this.fileLocation = dynamicCast($getObj(values, 3, new FileLocation), 65);
}
;
_.serialize = function serialize_51(writer){
  if (!this.groupPeer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.groupPeer);
  $writeLong(writer, 4, this.rid);
  if (!this.fileLocation) {
    throw new IOException;
  }
  $writeObject(writer, 3, this.fileLocation);
}
;
_.toString$ = function toString_52(){
  var res;
  res = 'rpc EditGroupAvatar{';
  res += 'groupPeer=' + this.groupPeer;
  res += ', rid=' + toString_2(this.rid);
  res += ', fileLocation=' + (this.fileLocation?'set':'empty');
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestEditGroupAvatar_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestEditGroupAvatar', 371, Lim_actor_model_network_parser_Request_2_classLit);
function RequestEditGroupTitle(){
}

defineClass(398, 789, {398:1, 4:1}, RequestEditGroupTitle);
_.getHeaderKey = function getHeaderKey_14(){
  return 85;
}
;
_.parse_0 = function parse_59(values){
  this.groupPeer = dynamicCast($getObj(values, 1, new GroupOutPeer), 38);
  this.rid = $getLong(values, 4);
  this.title_0 = convertString($getBytes(values, 3));
}
;
_.serialize = function serialize_52(writer){
  if (!this.groupPeer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.groupPeer);
  $writeLong(writer, 4, this.rid);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.title_0);
}
;
_.toString$ = function toString_53(){
  var res;
  res = 'rpc EditGroupTitle{';
  res += 'groupPeer=' + this.groupPeer;
  res += ', rid=' + toString_2(this.rid);
  res += ', title=' + this.title_0;
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestEditGroupTitle_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestEditGroupTitle', 398, Lim_actor_model_network_parser_Request_2_classLit);
function RequestEditName(){
}

defineClass(389, 789, {389:1, 4:1}, RequestEditName);
_.getHeaderKey = function getHeaderKey_15(){
  return 53;
}
;
_.parse_0 = function parse_60(values){
  this.name_0 = convertString($getBytes(values, 1));
}
;
_.serialize = function serialize_53(writer){
  if (this.name_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.name_0);
}
;
_.toString$ = function toString_54(){
  var res;
  res = 'rpc EditName{';
  res += 'name=' + this.name_0;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestEditName_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestEditName', 389, Lim_actor_model_network_parser_Request_2_classLit);
function RequestEditParameter(){
}

function RequestEditParameter_0(key, value_0){
  this.key = key;
  this.value_0 = value_0;
}

defineClass(299, 789, {299:1, 4:1}, RequestEditParameter, RequestEditParameter_0);
_.getHeaderKey = function getHeaderKey_16(){
  return 128;
}
;
_.parse_0 = function parse_61(values){
  this.key = convertString($getBytes(values, 1));
  this.value_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_54(writer){
  if (this.key == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.key);
  if (this.value_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.value_0);
}
;
_.toString$ = function toString_55(){
  var res;
  res = 'rpc EditParameter{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestEditParameter_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestEditParameter', 299, Lim_actor_model_network_parser_Request_2_classLit);
function RequestEditUserLocalName(){
}

defineClass(388, 789, {388:1, 4:1}, RequestEditUserLocalName);
_.getHeaderKey = function getHeaderKey_17(){
  return 96;
}
;
_.parse_0 = function parse_62(values){
  this.uid = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.name_0 = convertString($getBytes(values, 3));
}
;
_.serialize = function serialize_55(writer){
  $writeInt(writer, 1, this.uid);
  $writeLong(writer, 2, this.accessHash);
  if (this.name_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.name_0);
}
;
_.toString$ = function toString_56(){
  var res;
  res = 'rpc EditUserLocalName{';
  res += 'uid=' + this.uid;
  res += ', name=' + this.name_0;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_rpc_RequestEditUserLocalName_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestEditUserLocalName', 388, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetAuthSessions(){
}

defineClass(384, 789, {384:1, 4:1}, RequestGetAuthSessions);
_.getHeaderKey = function getHeaderKey_18(){
  return 80;
}
;
_.parse_0 = function parse_63(values){
}
;
_.serialize = function serialize_56(writer){
}
;
_.toString$ = function toString_57(){
  var res;
  res = 'rpc GetAuthSessions{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestGetAuthSessions_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetAuthSessions', 384, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetContacts(){
}

function RequestGetContacts_0(contactsHash){
  this.contactsHash = contactsHash;
}

defineClass(300, 789, {300:1, 4:1}, RequestGetContacts, RequestGetContacts_0);
_.getHeaderKey = function getHeaderKey_19(){
  return 87;
}
;
_.parse_0 = function parse_64(values){
  this.contactsHash = convertString($getBytes(values, 1));
}
;
_.serialize = function serialize_57(writer){
  if (this.contactsHash == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.contactsHash);
}
;
_.toString$ = function toString_58(){
  var res;
  res = 'rpc GetContacts{';
  res += 'contactsHash=' + this.contactsHash;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestGetContacts_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetContacts', 300, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetDifference(){
}

function RequestGetDifference_0(seq, state){
  this.seq = seq;
  this.state = state;
}

defineClass(308, 789, {308:1, 4:1}, RequestGetDifference, RequestGetDifference_0);
_.getHeaderKey = function getHeaderKey_20(){
  return 11;
}
;
_.parse_0 = function parse_65(values){
  this.seq = convertInt($getLong(values, 1));
  this.state = $getBytes(values, 2);
}
;
_.serialize = function serialize_58(writer){
  $writeInt(writer, 1, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.state);
}
;
_.toString$ = function toString_59(){
  var res;
  res = 'rpc GetDifference{';
  res += 'seq=' + this.seq;
  res += ', state=' + byteArrayToStringCompact(this.state);
  res += '}';
  return res;
}
;
_.seq = 0;
var Lim_actor_model_api_rpc_RequestGetDifference_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetDifference', 308, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetFileUploadPartUrl(){
}

defineClass(405, 789, {405:1, 4:1}, RequestGetFileUploadPartUrl);
_.getHeaderKey = function getHeaderKey_21(){
  return 142;
}
;
_.parse_0 = function parse_66(values){
  this.partNumber = convertInt($getLong(values, 1));
  this.partSize = convertInt($getLong(values, 2));
  this.uploadKey = $getBytes(values, 3);
}
;
_.serialize = function serialize_59(writer){
  $writeInt(writer, 1, this.partNumber);
  $writeInt(writer, 2, this.partSize);
  if (this.uploadKey == null) {
    throw new IOException;
  }
  $writeBytes(writer, 3, this.uploadKey);
}
;
_.toString$ = function toString_60(){
  var res;
  res = 'rpc GetFileUploadPartUrl{';
  res += 'uploadKey=' + byteArrayToStringCompact(this.uploadKey);
  res += '}';
  return res;
}
;
_.partNumber = 0;
_.partSize = 0;
var Lim_actor_model_api_rpc_RequestGetFileUploadPartUrl_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetFileUploadPartUrl', 405, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetFileUploadUrl(){
}

defineClass(403, 789, {403:1, 4:1}, RequestGetFileUploadUrl);
_.getHeaderKey = function getHeaderKey_22(){
  return 97;
}
;
_.parse_0 = function parse_67(values){
  this.expectedSize = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_60(writer){
  $writeInt(writer, 1, this.expectedSize);
}
;
_.toString$ = function toString_61(){
  var res;
  res = 'rpc GetFileUploadUrl{';
  res += 'expectedSize=' + this.expectedSize;
  res += '}';
  return res;
}
;
_.expectedSize = 0;
var Lim_actor_model_api_rpc_RequestGetFileUploadUrl_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetFileUploadUrl', 403, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetFileUrl(){
}

function RequestGetFileUrl_0(file){
  this.file = file;
}

defineClass(277, 789, {277:1, 4:1}, RequestGetFileUrl, RequestGetFileUrl_0);
_.getHeaderKey = function getHeaderKey_23(){
  return 77;
}
;
_.parse_0 = function parse_68(values){
  this.file = dynamicCast($getObj(values, 1, new FileLocation), 65);
}
;
_.serialize = function serialize_61(writer){
  if (!this.file) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.file);
}
;
_.toString$ = function toString_62(){
  var res;
  res = 'rpc GetFileUrl{';
  res += 'file=' + this.file;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestGetFileUrl_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetFileUrl', 277, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetParameters(){
}

defineClass(282, 789, {282:1, 4:1}, RequestGetParameters);
_.getHeaderKey = function getHeaderKey_24(){
  return 134;
}
;
_.parse_0 = function parse_69(values){
}
;
_.serialize = function serialize_62(writer){
}
;
_.toString$ = function toString_63(){
  var res;
  res = 'rpc GetParameters{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestGetParameters_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetParameters', 282, Lim_actor_model_network_parser_Request_2_classLit);
function RequestGetState(){
}

defineClass(307, 789, {307:1, 4:1}, RequestGetState);
_.getHeaderKey = function getHeaderKey_25(){
  return 9;
}
;
_.parse_0 = function parse_70(values){
}
;
_.serialize = function serialize_63(writer){
}
;
_.toString$ = function toString_64(){
  var res;
  res = 'rpc GetState{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestGetState_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestGetState', 307, Lim_actor_model_network_parser_Request_2_classLit);
function RequestImportContacts(){
}

function RequestImportContacts_0(phones, emails){
  this.phones = phones;
  this.emails = emails;
}

defineClass(314, 789, {314:1, 4:1}, RequestImportContacts, RequestImportContacts_0);
_.getHeaderKey = function getHeaderKey_26(){
  return 7;
}
;
_.parse_0 = function parse_71(values){
  var _emails, _phones, i_0, i0;
  _phones = new ArrayList;
  for (i0 = 0; i0 < $getRepeatedCount(values, 1); i0++) {
    $add_0(_phones, new PhoneToImport);
  }
  this.phones = $getRepeatedObj(values, 1, _phones);
  _emails = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 2); i_0++) {
    $add_0(_emails, new EmailToImport);
  }
  this.emails = $getRepeatedObj(values, 2, _emails);
}
;
_.serialize = function serialize_64(writer){
  $writeRepeatedObj(writer, 1, this.phones);
  $writeRepeatedObj(writer, 2, this.emails);
}
;
_.toString$ = function toString_65(){
  var res;
  res = 'rpc ImportContacts{';
  res += 'phones=' + this.phones.array.length;
  res += ', emails=' + this.emails.array.length;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestImportContacts_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestImportContacts', 314, Lim_actor_model_network_parser_Request_2_classLit);
function RequestInviteUser(){
}

defineClass(400, 789, {400:1, 4:1}, RequestInviteUser);
_.getHeaderKey = function getHeaderKey_27(){
  return 69;
}
;
_.parse_0 = function parse_72(values){
  this.groupPeer = dynamicCast($getObj(values, 1, new GroupOutPeer), 38);
  this.rid = $getLong(values, 4);
  this.user = dynamicCast($getObj(values, 3, new UserOutPeer), 69);
}
;
_.serialize = function serialize_65(writer){
  if (!this.groupPeer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.groupPeer);
  $writeLong(writer, 4, this.rid);
  if (!this.user) {
    throw new IOException;
  }
  $writeObject(writer, 3, this.user);
}
;
_.toString$ = function toString_66(){
  var res;
  res = 'rpc InviteUser{';
  res += 'groupPeer=' + this.groupPeer;
  res += ', rid=' + toString_2(this.rid);
  res += ', user=' + this.user;
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestInviteUser_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestInviteUser', 400, Lim_actor_model_network_parser_Request_2_classLit);
function RequestKickUser(){
}

defineClass(402, 789, {402:1, 4:1}, RequestKickUser);
_.getHeaderKey = function getHeaderKey_28(){
  return 71;
}
;
_.parse_0 = function parse_73(values){
  this.groupPeer = dynamicCast($getObj(values, 1, new GroupOutPeer), 38);
  this.rid = $getLong(values, 4);
  this.user = dynamicCast($getObj(values, 3, new UserOutPeer), 69);
}
;
_.serialize = function serialize_66(writer){
  if (!this.groupPeer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.groupPeer);
  $writeLong(writer, 4, this.rid);
  if (!this.user) {
    throw new IOException;
  }
  $writeObject(writer, 3, this.user);
}
;
_.toString$ = function toString_67(){
  var res;
  res = 'rpc KickUser{';
  res += 'groupPeer=' + this.groupPeer;
  res += ', rid=' + toString_2(this.rid);
  res += ', user=' + this.user;
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestKickUser_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestKickUser', 402, Lim_actor_model_network_parser_Request_2_classLit);
function RequestLeaveGroup(){
}

defineClass(401, 789, {401:1, 4:1}, RequestLeaveGroup);
_.getHeaderKey = function getHeaderKey_29(){
  return 70;
}
;
_.parse_0 = function parse_74(values){
  this.groupPeer = dynamicCast($getObj(values, 1, new GroupOutPeer), 38);
  this.rid = $getLong(values, 2);
}
;
_.serialize = function serialize_67(writer){
  if (!this.groupPeer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.groupPeer);
  $writeLong(writer, 2, this.rid);
}
;
_.toString$ = function toString_68(){
  var res;
  res = 'rpc LeaveGroup{';
  res += 'groupPeer=' + this.groupPeer;
  res += ', rid=' + toString_2(this.rid);
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestLeaveGroup_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestLeaveGroup', 401, Lim_actor_model_network_parser_Request_2_classLit);
function RequestLoadDialogs(){
}

function RequestLoadDialogs_0(minDate){
  this.minDate = minDate;
  this.limit = 20;
}

defineClass(301, 789, {301:1, 4:1}, RequestLoadDialogs, RequestLoadDialogs_0);
_.getHeaderKey = function getHeaderKey_30(){
  return 104;
}
;
_.parse_0 = function parse_75(values){
  this.minDate = $getLong(values, 1);
  this.limit = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_68(writer){
  $writeLong(writer, 1, this.minDate);
  $writeInt(writer, 2, this.limit);
}
;
_.toString$ = function toString_69(){
  var res;
  res = 'rpc LoadDialogs{';
  res += 'minDate=' + toString_2(this.minDate);
  res += ', limit=' + this.limit;
  res += '}';
  return res;
}
;
_.limit = 0;
_.minDate = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestLoadDialogs_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestLoadDialogs', 301, Lim_actor_model_network_parser_Request_2_classLit);
function RequestLoadHistory(){
}

function RequestLoadHistory_0(peer, minDate){
  this.peer = peer;
  this.minDate = minDate;
  this.limit = 20;
}

defineClass(290, 789, {290:1, 4:1}, RequestLoadHistory, RequestLoadHistory_0);
_.getHeaderKey = function getHeaderKey_31(){
  return 118;
}
;
_.parse_0 = function parse_76(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
  this.minDate = $getLong(values, 3);
  this.limit = convertInt($getLong(values, 4));
}
;
_.serialize = function serialize_69(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 3, this.minDate);
  $writeInt(writer, 4, this.limit);
}
;
_.toString$ = function toString_70(){
  var res;
  res = 'rpc LoadHistory{';
  res += 'peer=' + this.peer;
  res += ', minDate=' + toString_2(this.minDate);
  res += ', limit=' + this.limit;
  res += '}';
  return res;
}
;
_.limit = 0;
_.minDate = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestLoadHistory_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestLoadHistory', 290, Lim_actor_model_network_parser_Request_2_classLit);
function RequestMessageRead(){
}

function RequestMessageRead_0(peer, date){
  this.peer = peer;
  this.date = date;
}

defineClass(302, 789, {302:1, 4:1}, RequestMessageRead, RequestMessageRead_0);
_.getHeaderKey = function getHeaderKey_32(){
  return 57;
}
;
_.parse_0 = function parse_77(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
  this.date = $getLong(values, 3);
}
;
_.serialize = function serialize_70(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 3, this.date);
}
;
_.toString$ = function toString_71(){
  var res;
  res = 'rpc MessageRead{';
  res += 'peer=' + this.peer;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestMessageRead_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestMessageRead', 302, Lim_actor_model_network_parser_Request_2_classLit);
function RequestMessageReceived(){
}

function RequestMessageReceived_0(peer, date){
  this.peer = peer;
  this.date = date;
}

defineClass(303, 789, {303:1, 4:1}, RequestMessageReceived, RequestMessageReceived_0);
_.getHeaderKey = function getHeaderKey_33(){
  return 55;
}
;
_.parse_0 = function parse_78(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
  this.date = $getLong(values, 3);
}
;
_.serialize = function serialize_71(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 3, this.date);
}
;
_.toString$ = function toString_72(){
  var res;
  res = 'rpc MessageReceived{';
  res += 'peer=' + this.peer;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestMessageReceived_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestMessageReceived', 303, Lim_actor_model_network_parser_Request_2_classLit);
function RequestRegisterApplePush(){
}

function RequestRegisterApplePush_0(apnsKey, token){
  this.apnsKey = apnsKey;
  this.token = token;
}

defineClass(292, 789, {292:1, 4:1}, RequestRegisterApplePush, RequestRegisterApplePush_0);
_.getHeaderKey = function getHeaderKey_34(){
  return 76;
}
;
_.parse_0 = function parse_79(values){
  this.apnsKey = convertInt($getLong(values, 1));
  this.token = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_72(writer){
  $writeInt(writer, 1, this.apnsKey);
  if (this.token == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.token);
}
;
_.toString$ = function toString_73(){
  var res;
  res = 'rpc RegisterApplePush{';
  res += '}';
  return res;
}
;
_.apnsKey = 0;
var Lim_actor_model_api_rpc_RequestRegisterApplePush_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestRegisterApplePush', 292, Lim_actor_model_network_parser_Request_2_classLit);
function RequestRegisterGooglePush(){
}

function RequestRegisterGooglePush_0(projectId, token){
  this.projectId = projectId;
  this.token = token;
}

defineClass(291, 789, {291:1, 4:1}, RequestRegisterGooglePush, RequestRegisterGooglePush_0);
_.getHeaderKey = function getHeaderKey_35(){
  return 51;
}
;
_.parse_0 = function parse_80(values){
  this.projectId = $getLong(values, 1);
  this.token = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_73(writer){
  $writeLong(writer, 1, this.projectId);
  if (this.token == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.token);
}
;
_.toString$ = function toString_74(){
  var res;
  res = 'rpc RegisterGooglePush{';
  res += '}';
  return res;
}
;
_.projectId = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestRegisterGooglePush_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestRegisterGooglePush', 291, Lim_actor_model_network_parser_Request_2_classLit);
function RequestRemoveAvatar(){
}

defineClass(372, 789, {372:1, 4:1}, RequestRemoveAvatar);
_.getHeaderKey = function getHeaderKey_36(){
  return 91;
}
;
_.parse_0 = function parse_81(values){
}
;
_.serialize = function serialize_74(writer){
}
;
_.toString$ = function toString_75(){
  var res;
  res = 'rpc RemoveAvatar{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestRemoveAvatar_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestRemoveAvatar', 372, Lim_actor_model_network_parser_Request_2_classLit);
function RequestRemoveContact(){
}

defineClass(394, 789, {394:1, 4:1}, RequestRemoveContact);
_.getHeaderKey = function getHeaderKey_37(){
  return 89;
}
;
_.parse_0 = function parse_82(values){
  this.uid = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
}
;
_.serialize = function serialize_75(writer){
  $writeInt(writer, 1, this.uid);
  $writeLong(writer, 2, this.accessHash);
}
;
_.toString$ = function toString_76(){
  var res;
  res = 'rpc RemoveContact{';
  res += 'uid=' + this.uid;
  res += '}';
  return res;
}
;
_.accessHash = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_rpc_RequestRemoveContact_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestRemoveContact', 394, Lim_actor_model_network_parser_Request_2_classLit);
function RequestRemoveGroupAvatar(){
}

defineClass(399, 789, {399:1, 4:1}, RequestRemoveGroupAvatar);
_.getHeaderKey = function getHeaderKey_38(){
  return 101;
}
;
_.parse_0 = function parse_83(values){
  this.groupPeer = dynamicCast($getObj(values, 1, new GroupOutPeer), 38);
  this.rid = $getLong(values, 4);
}
;
_.serialize = function serialize_76(writer){
  if (!this.groupPeer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.groupPeer);
  $writeLong(writer, 4, this.rid);
}
;
_.toString$ = function toString_77(){
  var res;
  res = 'rpc RemoveGroupAvatar{';
  res += 'groupPeer=' + this.groupPeer;
  res += ', rid=' + toString_2(this.rid);
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestRemoveGroupAvatar_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestRemoveGroupAvatar', 399, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSearchContacts(){
}

defineClass(396, 789, {396:1, 4:1}, RequestSearchContacts);
_.getHeaderKey = function getHeaderKey_39(){
  return 112;
}
;
_.parse_0 = function parse_84(values){
  this.request = convertString($getBytes(values, 1));
}
;
_.serialize = function serialize_77(writer){
  if (this.request == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.request);
}
;
_.toString$ = function toString_78(){
  var res;
  res = 'rpc SearchContacts{';
  res += 'request=' + this.request;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestSearchContacts_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSearchContacts', 396, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSendAuthCall(){
}

defineClass(382, 789, {382:1, 4:1}, RequestSendAuthCall);
_.getHeaderKey = function getHeaderKey_40(){
  return 90;
}
;
_.parse_0 = function parse_85(values){
  this.phoneNumber = $getLong(values, 1);
  this.smsHash = convertString($getBytes(values, 2));
  this.appId = convertInt($getLong(values, 3));
  this.apiKey = convertString($getBytes(values, 4));
}
;
_.serialize = function serialize_78(writer){
  $writeLong(writer, 1, this.phoneNumber);
  if (this.smsHash == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.smsHash);
  $writeInt(writer, 3, this.appId);
  if (this.apiKey == null) {
    throw new IOException;
  }
  $writeString(writer, 4, this.apiKey);
}
;
_.toString$ = function toString_79(){
  var res;
  res = 'rpc SendAuthCall{';
  res += 'phoneNumber=' + toString_2(this.phoneNumber);
  res += '}';
  return res;
}
;
_.appId = 0;
_.phoneNumber = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestSendAuthCall_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSendAuthCall', 382, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSendAuthCode(){
}

function RequestSendAuthCode_0(phoneNumber, appId, apiKey){
  this.phoneNumber = phoneNumber;
  this.appId = appId;
  this.apiKey = apiKey;
}

defineClass(247, 789, {247:1, 4:1}, RequestSendAuthCode, RequestSendAuthCode_0);
_.getHeaderKey = function getHeaderKey_41(){
  return 1;
}
;
_.parse_0 = function parse_86(values){
  this.phoneNumber = $getLong(values, 1);
  this.appId = convertInt($getLong(values, 2));
  this.apiKey = convertString($getBytes(values, 3));
}
;
_.serialize = function serialize_79(writer){
  $writeVarIntField(writer, 1, this.phoneNumber);
  $writeVarIntField(writer, 2, fromInt(this.appId));
  if (this.apiKey == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.apiKey);
}
;
_.toString$ = function toString_80(){
  var res;
  res = 'rpc SendAuthCode{';
  res += 'phoneNumber=' + toString_2(this.phoneNumber);
  res += '}';
  return res;
}
;
_.appId = 0;
_.phoneNumber = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestSendAuthCode_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSendAuthCode', 247, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSendEmailCode(){
}

defineClass(390, 789, {390:1, 4:1}, RequestSendEmailCode);
_.getHeaderKey = function getHeaderKey_42(){
  return 120;
}
;
_.parse_0 = function parse_87(values){
  this.email = convertString($getBytes(values, 1));
  this.description = convertString($getBytes_0(values, 2));
}
;
_.serialize = function serialize_80(writer){
  if (this.email == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.email);
  this.description != null && $writeString(writer, 2, this.description);
}
;
_.toString$ = function toString_81(){
  var res;
  res = 'rpc SendEmailCode{';
  res += 'email=' + this.email;
  res += ', description=' + this.description;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestSendEmailCode_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSendEmailCode', 390, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSendMessage(){
}

function RequestSendMessage_0(peer, rid, message){
  this.peer = peer;
  this.rid = rid;
  this.message_0 = message;
}

defineClass(305, 789, {305:1, 4:1}, RequestSendMessage, RequestSendMessage_0);
_.getHeaderKey = function getHeaderKey_43(){
  return 92;
}
;
_.parse_0 = function parse_88(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
  this.rid = $getLong(values, 3);
  this.message_0 = fromBytes_0($getBytes(values, 4));
}
;
_.serialize = function serialize_81(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 3, this.rid);
  if (!this.message_0) {
    throw new IOException;
  }
  $writeBytes(writer, 4, $buildContainer_0(this.message_0));
}
;
_.toString$ = function toString_82(){
  var res;
  res = 'rpc SendMessage{';
  res += 'peer=' + this.peer;
  res += ', rid=' + toString_2(this.rid);
  res += ', message=' + this.message_0;
  res += '}';
  return res;
}
;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestSendMessage_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSendMessage', 305, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSetOnline(){
}

function RequestSetOnline_0(isOnline){
  this.isOnline = isOnline;
  this.timeout = {l:90000, m:0, h:0};
}

defineClass(297, 789, {297:1, 4:1}, RequestSetOnline, RequestSetOnline_0);
_.getHeaderKey = function getHeaderKey_44(){
  return 29;
}
;
_.parse_0 = function parse_89(values){
  this.isOnline = neq($getLong(values, 1), {l:0, m:0, h:0});
  this.timeout = $getLong(values, 2);
}
;
_.serialize = function serialize_82(writer){
  $writeBool(writer, 1, this.isOnline);
  $writeLong(writer, 2, this.timeout);
}
;
_.toString$ = function toString_83(){
  var res;
  res = 'rpc SetOnline{';
  res += 'isOnline=' + this.isOnline;
  res += ', timeout=' + toString_2(this.timeout);
  res += '}';
  return res;
}
;
_.isOnline = false;
_.timeout = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestSetOnline_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSetOnline', 297, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSignIn(){
}

function RequestSignIn_0(phoneNumber, smsHash, smsCode, deviceHash, deviceTitle, appId, appKey){
  this.phoneNumber = phoneNumber;
  this.smsHash = smsHash;
  this.smsCode = smsCode;
  this.deviceHash = deviceHash;
  this.deviceTitle = deviceTitle;
  this.appId = appId;
  this.appKey = appKey;
}

defineClass(248, 789, {248:1, 4:1}, RequestSignIn, RequestSignIn_0);
_.getHeaderKey = function getHeaderKey_45(){
  return 3;
}
;
_.parse_0 = function parse_90(values){
  this.phoneNumber = $getLong(values, 1);
  this.smsHash = convertString($getBytes(values, 2));
  this.smsCode = convertString($getBytes(values, 3));
  this.deviceHash = $getBytes(values, 5);
  this.deviceTitle = convertString($getBytes(values, 6));
  this.appId = convertInt($getLong(values, 7));
  this.appKey = convertString($getBytes(values, 8));
}
;
_.serialize = function serialize_83(writer){
  $writeVarIntField(writer, 1, this.phoneNumber);
  if (this.smsHash == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.smsHash);
  if (this.smsCode == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.smsCode);
  if (this.deviceHash == null) {
    throw new IOException;
  }
  $writeBytes(writer, 5, this.deviceHash);
  if (this.deviceTitle == null) {
    throw new IOException;
  }
  $writeString(writer, 6, this.deviceTitle);
  $writeVarIntField(writer, 7, fromInt(this.appId));
  if (this.appKey == null) {
    throw new IOException;
  }
  $writeString(writer, 8, this.appKey);
}
;
_.toString$ = function toString_84(){
  var res;
  res = 'rpc SignIn{';
  res += 'deviceHash=' + byteArrayToString(this.deviceHash);
  res += ', deviceTitle=' + this.deviceTitle;
  res += '}';
  return res;
}
;
_.appId = 0;
_.phoneNumber = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestSignIn_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSignIn', 248, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSignOut(){
}

defineClass(387, 789, {387:1, 4:1}, RequestSignOut);
_.getHeaderKey = function getHeaderKey_46(){
  return 84;
}
;
_.parse_0 = function parse_91(values){
}
;
_.serialize = function serialize_84(writer){
}
;
_.toString$ = function toString_85(){
  var res;
  res = 'rpc SignOut{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestSignOut_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSignOut', 387, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSignUp(){
}

defineClass(383, 789, {383:1, 4:1}, RequestSignUp);
_.getHeaderKey = function getHeaderKey_47(){
  return 4;
}
;
_.parse_0 = function parse_92(values){
  this.phoneNumber = $getLong(values, 1);
  this.smsHash = convertString($getBytes(values, 2));
  this.smsCode = convertString($getBytes(values, 3));
  this.name_0 = convertString($getBytes(values, 4));
  this.deviceHash = $getBytes(values, 7);
  this.deviceTitle = convertString($getBytes(values, 8));
  this.appId = convertInt($getLong(values, 9));
  this.appKey = convertString($getBytes(values, 10));
  this.isSilent = neq($getLong(values, 11), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_85(writer){
  $writeLong(writer, 1, this.phoneNumber);
  if (this.smsHash == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.smsHash);
  if (this.smsCode == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.smsCode);
  if (this.name_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 4, this.name_0);
  if (this.deviceHash == null) {
    throw new IOException;
  }
  $writeBytes(writer, 7, this.deviceHash);
  if (this.deviceTitle == null) {
    throw new IOException;
  }
  $writeString(writer, 8, this.deviceTitle);
  $writeInt(writer, 9, this.appId);
  if (this.appKey == null) {
    throw new IOException;
  }
  $writeString(writer, 10, this.appKey);
  $writeBool(writer, 11, this.isSilent);
}
;
_.toString$ = function toString_86(){
  var res;
  res = 'rpc SignUp{';
  res += 'name=' + this.name_0;
  res += ', deviceHash=' + byteArrayToString(this.deviceHash);
  res += ', deviceTitle=' + this.deviceTitle;
  res += '}';
  return res;
}
;
_.appId = 0;
_.isSilent = false;
_.phoneNumber = {l:0, m:0, h:0};
var Lim_actor_model_api_rpc_RequestSignUp_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSignUp', 383, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSubscribeFromGroupOnline(){
}

defineClass(408, 789, {408:1, 4:1}, RequestSubscribeFromGroupOnline);
_.getHeaderKey = function getHeaderKey_48(){
  return 75;
}
;
_.parse_0 = function parse_93(values){
  var _groups, i_0;
  _groups = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_groups, new GroupOutPeer);
  }
  this.groups = $getRepeatedObj(values, 1, _groups);
}
;
_.serialize = function serialize_86(writer){
  $writeRepeatedObj(writer, 1, this.groups);
}
;
_.toString$ = function toString_87(){
  var res;
  res = 'rpc SubscribeFromGroupOnline{';
  res += 'groups=' + this.groups.array.length;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestSubscribeFromGroupOnline_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSubscribeFromGroupOnline', 408, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSubscribeFromOnline(){
}

defineClass(407, 789, {407:1, 4:1}, RequestSubscribeFromOnline);
_.getHeaderKey = function getHeaderKey_49(){
  return 33;
}
;
_.parse_0 = function parse_94(values){
  var _users, i_0;
  _users = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_users, new UserOutPeer);
  }
  this.users = $getRepeatedObj(values, 1, _users);
}
;
_.serialize = function serialize_87(writer){
  $writeRepeatedObj(writer, 1, this.users);
}
;
_.toString$ = function toString_88(){
  var res;
  res = 'rpc SubscribeFromOnline{';
  res += 'users=' + this.users.array.length;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestSubscribeFromOnline_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSubscribeFromOnline', 407, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSubscribeToGroupOnline(){
}

function RequestSubscribeToGroupOnline_0(groups){
  this.groups = groups;
}

defineClass(200, 789, {200:1, 4:1}, RequestSubscribeToGroupOnline, RequestSubscribeToGroupOnline_0);
_.getHeaderKey = function getHeaderKey_50(){
  return 74;
}
;
_.parse_0 = function parse_95(values){
  var _groups, i_0;
  _groups = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_groups, new GroupOutPeer);
  }
  this.groups = $getRepeatedObj(values, 1, _groups);
}
;
_.serialize = function serialize_88(writer){
  $writeRepeatedObj(writer, 1, this.groups);
}
;
_.toString$ = function toString_89(){
  var res;
  res = 'rpc SubscribeToGroupOnline{';
  res += 'groups=' + this.groups.array.length;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestSubscribeToGroupOnline_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSubscribeToGroupOnline', 200, Lim_actor_model_network_parser_Request_2_classLit);
function RequestSubscribeToOnline(){
}

function RequestSubscribeToOnline_0(users){
  this.users = users;
}

defineClass(199, 789, {199:1, 4:1}, RequestSubscribeToOnline, RequestSubscribeToOnline_0);
_.getHeaderKey = function getHeaderKey_51(){
  return 32;
}
;
_.parse_0 = function parse_96(values){
  var _users, i_0;
  _users = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_users, new UserOutPeer);
  }
  this.users = $getRepeatedObj(values, 1, _users);
}
;
_.serialize = function serialize_89(writer){
  $writeRepeatedObj(writer, 1, this.users);
}
;
_.toString$ = function toString_90(){
  var res;
  res = 'rpc SubscribeToOnline{';
  res += 'users=' + this.users.array.length;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestSubscribeToOnline_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestSubscribeToOnline', 199, Lim_actor_model_network_parser_Request_2_classLit);
function RequestTerminateAllSessions(){
}

defineClass(386, 789, {386:1, 4:1}, RequestTerminateAllSessions);
_.getHeaderKey = function getHeaderKey_52(){
  return 83;
}
;
_.parse_0 = function parse_97(values){
}
;
_.serialize = function serialize_90(writer){
}
;
_.toString$ = function toString_91(){
  var res;
  res = 'rpc TerminateAllSessions{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestTerminateAllSessions_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestTerminateAllSessions', 386, Lim_actor_model_network_parser_Request_2_classLit);
function RequestTerminateSession(){
}

defineClass(385, 789, {385:1, 4:1}, RequestTerminateSession);
_.getHeaderKey = function getHeaderKey_53(){
  return 82;
}
;
_.parse_0 = function parse_98(values){
  this.id_0 = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_91(writer){
  $writeInt(writer, 1, this.id_0);
}
;
_.toString$ = function toString_92(){
  var res;
  res = 'rpc TerminateSession{';
  res += 'id=' + this.id_0;
  res += '}';
  return res;
}
;
_.id_0 = 0;
var Lim_actor_model_api_rpc_RequestTerminateSession_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestTerminateSession', 385, Lim_actor_model_network_parser_Request_2_classLit);
function RequestTyping(){
}

function RequestTyping_0(peer, typingType){
  this.peer = peer;
  this.typingType = typingType;
}

defineClass(298, 789, {298:1, 4:1}, RequestTyping, RequestTyping_0);
_.getHeaderKey = function getHeaderKey_54(){
  return 27;
}
;
_.parse_0 = function parse_99(values){
  this.peer = dynamicCast($getObj(values, 1, new OutPeer), 31);
  this.typingType = parse_41(convertInt($getLong(values, 3)));
}
;
_.serialize = function serialize_92(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  if (!this.typingType) {
    throw new IOException;
  }
  $writeInt(writer, 3, this.typingType.value_0);
}
;
_.toString$ = function toString_93(){
  var res;
  res = 'rpc Typing{';
  res += 'peer=' + this.peer;
  res += ', typingType=' + this.typingType;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestTyping_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestTyping', 298, Lim_actor_model_network_parser_Request_2_classLit);
function RequestUnregisterPush(){
}

defineClass(406, 789, {406:1, 4:1}, RequestUnregisterPush);
_.getHeaderKey = function getHeaderKey_55(){
  return 52;
}
;
_.parse_0 = function parse_100(values){
}
;
_.serialize = function serialize_93(writer){
}
;
_.toString$ = function toString_94(){
  var res;
  res = 'rpc UnregisterPush{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_RequestUnregisterPush_2_classLit = createForClass('im.actor.model.api.rpc', 'RequestUnregisterPush', 406, Lim_actor_model_network_parser_Request_2_classLit);
defineClass(35, 788, {4:1, 35:1});
var Lim_actor_model_network_parser_Response_2_classLit = createForClass('im.actor.model.network.parser', 'Response', 35, Lim_actor_model_network_parser_RpcScope_2_classLit);
function ResponseAuth(){
}

defineClass(316, 35, {316:1, 4:1, 35:1}, ResponseAuth);
_.getHeaderKey = function getHeaderKey_56(){
  return 5;
}
;
_.parse_0 = function parse_101(values){
  this.user = dynamicCast($getObj(values, 2, new User), 58);
  this.config = dynamicCast($getObj(values, 3, new Config), 217);
}
;
_.serialize = function serialize_94(writer){
  if (!this.user) {
    throw new IOException;
  }
  $writeObject(writer, 2, this.user);
  if (!this.config) {
    throw new IOException;
  }
  $writeObject(writer, 3, this.config);
}
;
_.toString$ = function toString_95(){
  var res;
  res = 'response Auth{';
  res += 'user=' + (this.user?'set':'empty');
  res += ', config=' + this.config;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseAuth_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseAuth', 316, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseCommitFileUpload(){
}

defineClass(415, 35, {415:1, 4:1, 35:1}, ResponseCommitFileUpload);
_.getHeaderKey = function getHeaderKey_57(){
  return 138;
}
;
_.parse_0 = function parse_102(values){
  this.uploadedFileLocation = dynamicCast($getObj(values, 1, new FileLocation), 65);
}
;
_.serialize = function serialize_95(writer){
  if (!this.uploadedFileLocation) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.uploadedFileLocation);
}
;
_.toString$ = function toString_96(){
  var res;
  res = 'tuple CommitFileUpload{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseCommitFileUpload_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseCommitFileUpload', 415, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseCreateGroup(){
}

defineClass(412, 35, {412:1, 4:1, 35:1}, ResponseCreateGroup);
_.getHeaderKey = function getHeaderKey_58(){
  return 66;
}
;
_.parse_0 = function parse_103(values){
  this.groupPeer = dynamicCast($getObj(values, 1, new GroupOutPeer), 38);
  this.seq = convertInt($getLong(values, 3));
  this.state = $getBytes(values, 4);
  this.users = $getRepeatedInt(values, 5);
  this.date = $getLong(values, 6);
}
;
_.serialize = function serialize_96(writer){
  if (!this.groupPeer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.groupPeer);
  $writeInt(writer, 3, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 4, this.state);
  $writeRepeatedInt(writer, 5, this.users);
  $writeLong(writer, 6, this.date);
}
;
_.toString$ = function toString_97(){
  var res;
  res = 'tuple CreateGroup{';
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.seq = 0;
var Lim_actor_model_api_rpc_ResponseCreateGroup_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseCreateGroup', 412, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseEditAvatar(){
}

defineClass(410, 35, {410:1, 4:1, 35:1}, ResponseEditAvatar);
_.getHeaderKey = function getHeaderKey_59(){
  return 103;
}
;
_.parse_0 = function parse_104(values){
  this.avatar = dynamicCast($getObj(values, 1, new Avatar), 50);
  this.seq = convertInt($getLong(values, 2));
  this.state = $getBytes(values, 3);
}
;
_.serialize = function serialize_97(writer){
  if (!this.avatar) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.avatar);
  $writeInt(writer, 2, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 3, this.state);
}
;
_.toString$ = function toString_98(){
  var res;
  res = 'tuple EditAvatar{';
  res += '}';
  return res;
}
;
_.seq = 0;
var Lim_actor_model_api_rpc_ResponseEditAvatar_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseEditAvatar', 410, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseEditGroupAvatar(){
}

defineClass(413, 35, {413:1, 4:1, 35:1}, ResponseEditGroupAvatar);
_.getHeaderKey = function getHeaderKey_60(){
  return 115;
}
;
_.parse_0 = function parse_105(values){
  this.avatar = dynamicCast($getObj(values, 1, new Avatar), 50);
  this.seq = convertInt($getLong(values, 2));
  this.state = $getBytes(values, 3);
  this.date = $getLong(values, 4);
}
;
_.serialize = function serialize_98(writer){
  if (!this.avatar) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.avatar);
  $writeInt(writer, 2, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 3, this.state);
  $writeLong(writer, 4, this.date);
}
;
_.toString$ = function toString_99(){
  var res;
  res = 'tuple EditGroupAvatar{';
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.seq = 0;
var Lim_actor_model_api_rpc_ResponseEditGroupAvatar_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseEditGroupAvatar', 413, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseGetAuthSessions(){
}

defineClass(409, 35, {409:1, 4:1, 35:1}, ResponseGetAuthSessions);
_.getHeaderKey = function getHeaderKey_61(){
  return 81;
}
;
_.parse_0 = function parse_106(values){
  var _userAuths, i_0;
  _userAuths = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_userAuths, new AuthSession);
  }
  this.userAuths = $getRepeatedObj(values, 1, _userAuths);
}
;
_.serialize = function serialize_99(writer){
  $writeRepeatedObj(writer, 1, this.userAuths);
}
;
_.toString$ = function toString_100(){
  var res;
  res = 'tuple GetAuthSessions{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseGetAuthSessions_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseGetAuthSessions', 409, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseGetContacts(){
}

defineClass(284, 35, {284:1, 4:1, 35:1}, ResponseGetContacts);
_.getHeaderKey = function getHeaderKey_62(){
  return 88;
}
;
_.parse_0 = function parse_107(values){
  var _users, i_0;
  _users = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_users, new User);
  }
  this.users = $getRepeatedObj(values, 1, _users);
  this.isNotChanged = neq($getLong(values, 2), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_100(writer){
  $writeRepeatedObj(writer, 1, this.users);
  $writeBool(writer, 2, this.isNotChanged);
}
;
_.toString$ = function toString_101(){
  var res;
  res = 'tuple GetContacts{';
  res += '}';
  return res;
}
;
_.isNotChanged = false;
var Lim_actor_model_api_rpc_ResponseGetContacts_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseGetContacts', 284, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseGetDifference(){
}

defineClass(322, 35, {322:1, 4:1, 35:1}, ResponseGetDifference);
_.getHeaderKey = function getHeaderKey_63(){
  return 12;
}
;
_.parse_0 = function parse_108(values){
  var _emails, _groups, _phones, _updates, _users, i_0, i0, i1, i2, i3;
  this.seq = convertInt($getLong(values, 1));
  this.state = $getBytes(values, 2);
  _users = new ArrayList;
  for (i0 = 0; i0 < $getRepeatedCount(values, 3); i0++) {
    $add_0(_users, new User);
  }
  this.users = $getRepeatedObj(values, 3, _users);
  _groups = new ArrayList;
  for (i1 = 0; i1 < $getRepeatedCount(values, 6); i1++) {
    $add_0(_groups, new Group);
  }
  this.groups = $getRepeatedObj(values, 6, _groups);
  _phones = new ArrayList;
  for (i2 = 0; i2 < $getRepeatedCount(values, 7); i2++) {
    $add_0(_phones, new Phone);
  }
  this.phones = $getRepeatedObj(values, 7, _phones);
  _emails = new ArrayList;
  for (i3 = 0; i3 < $getRepeatedCount(values, 8); i3++) {
    $add_0(_emails, new Email);
  }
  this.emails = $getRepeatedObj(values, 8, _emails);
  _updates = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 4); i_0++) {
    $add_0(_updates, new DifferenceUpdate);
  }
  this.updates = $getRepeatedObj(values, 4, _updates);
  this.needMore = neq($getLong(values, 5), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_101(writer){
  $writeInt(writer, 1, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.state);
  $writeRepeatedObj(writer, 3, this.users);
  $writeRepeatedObj(writer, 6, this.groups);
  $writeRepeatedObj(writer, 7, this.phones);
  $writeRepeatedObj(writer, 8, this.emails);
  $writeRepeatedObj(writer, 4, this.updates);
  $writeBool(writer, 5, this.needMore);
}
;
_.toString$ = function toString_102(){
  var res;
  res = 'tuple GetDifference{';
  res += '}';
  return res;
}
;
_.needMore = false;
_.seq = 0;
var Lim_actor_model_api_rpc_ResponseGetDifference_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseGetDifference', 322, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseGetFileUploadPartUrl(){
}

defineClass(416, 35, {416:1, 4:1, 35:1}, ResponseGetFileUploadPartUrl);
_.getHeaderKey = function getHeaderKey_64(){
  return 141;
}
;
_.parse_0 = function parse_109(values){
  this.url_0 = convertString($getBytes(values, 1));
}
;
_.serialize = function serialize_102(writer){
  if (this.url_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.url_0);
}
;
_.toString$ = function toString_103(){
  var res;
  res = 'tuple GetFileUploadPartUrl{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseGetFileUploadPartUrl_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseGetFileUploadPartUrl', 416, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseGetFileUploadUrl(){
}

defineClass(414, 35, {414:1, 4:1, 35:1}, ResponseGetFileUploadUrl);
_.getHeaderKey = function getHeaderKey_65(){
  return 121;
}
;
_.parse_0 = function parse_110(values){
  this.url_0 = convertString($getBytes(values, 1));
  this.uploadKey = $getBytes(values, 2);
}
;
_.serialize = function serialize_103(writer){
  if (this.url_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.url_0);
  if (this.uploadKey == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.uploadKey);
}
;
_.toString$ = function toString_104(){
  var res;
  res = 'tuple GetFileUploadUrl{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseGetFileUploadUrl_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseGetFileUploadUrl', 414, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseGetFileUrl(){
}

defineClass(320, 35, {320:1, 4:1, 35:1}, ResponseGetFileUrl);
_.getHeaderKey = function getHeaderKey_66(){
  return 78;
}
;
_.parse_0 = function parse_111(values){
  this.url_0 = convertString($getBytes(values, 1));
  this.timeout = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_104(writer){
  if (this.url_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.url_0);
  $writeInt(writer, 2, this.timeout);
}
;
_.toString$ = function toString_105(){
  var res;
  res = 'tuple GetFileUrl{';
  res += '}';
  return res;
}
;
_.timeout = 0;
var Lim_actor_model_api_rpc_ResponseGetFileUrl_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseGetFileUrl', 320, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseGetParameters(){
}

defineClass(321, 35, {321:1, 4:1, 35:1}, ResponseGetParameters);
_.getHeaderKey = function getHeaderKey_67(){
  return 135;
}
;
_.parse_0 = function parse_112(values){
  var _parameters, i_0;
  _parameters = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_parameters, new Parameter);
  }
  this.parameters = $getRepeatedObj(values, 1, _parameters);
}
;
_.serialize = function serialize_105(writer){
  $writeRepeatedObj(writer, 1, this.parameters);
}
;
_.toString$ = function toString_106(){
  var res;
  res = 'tuple GetParameters{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseGetParameters_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseGetParameters', 321, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseImportContacts(){
}

defineClass(317, 35, {317:1, 4:1, 35:1}, ResponseImportContacts);
_.getHeaderKey = function getHeaderKey_68(){
  return 8;
}
;
_.parse_0 = function parse_113(values){
  var _users, i_0;
  _users = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_users, new User);
  }
  this.users = $getRepeatedObj(values, 1, _users);
  this.seq = convertInt($getLong(values, 2));
  this.state = $getBytes(values, 3);
}
;
_.serialize = function serialize_106(writer){
  $writeRepeatedObj(writer, 1, this.users);
  $writeInt(writer, 2, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 3, this.state);
}
;
_.toString$ = function toString_107(){
  var res;
  res = 'tuple ImportContacts{';
  res += '}';
  return res;
}
;
_.seq = 0;
var Lim_actor_model_api_rpc_ResponseImportContacts_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseImportContacts', 317, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseLoadDialogs(){
}

defineClass(319, 35, {319:1, 4:1, 35:1}, ResponseLoadDialogs);
_.getHeaderKey = function getHeaderKey_69(){
  return 105;
}
;
_.parse_0 = function parse_114(values){
  var _dialogs, _groups, _users, i_0, i0, i1;
  _groups = new ArrayList;
  for (i0 = 0; i0 < $getRepeatedCount(values, 1); i0++) {
    $add_0(_groups, new Group);
  }
  this.groups = $getRepeatedObj(values, 1, _groups);
  _users = new ArrayList;
  for (i1 = 0; i1 < $getRepeatedCount(values, 2); i1++) {
    $add_0(_users, new User);
  }
  this.users = $getRepeatedObj(values, 2, _users);
  _dialogs = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 3); i_0++) {
    $add_0(_dialogs, new Dialog);
  }
  this.dialogs = $getRepeatedObj(values, 3, _dialogs);
}
;
_.serialize = function serialize_107(writer){
  $writeRepeatedObj(writer, 1, this.groups);
  $writeRepeatedObj(writer, 2, this.users);
  $writeRepeatedObj(writer, 3, this.dialogs);
}
;
_.toString$ = function toString_108(){
  var res;
  res = 'tuple LoadDialogs{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseLoadDialogs_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseLoadDialogs', 319, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseLoadHistory(){
}

defineClass(318, 35, {318:1, 4:1, 35:1}, ResponseLoadHistory);
_.getHeaderKey = function getHeaderKey_70(){
  return 119;
}
;
_.parse_0 = function parse_115(values){
  var _history, _users, i_0, i0;
  _history = new ArrayList;
  for (i0 = 0; i0 < $getRepeatedCount(values, 1); i0++) {
    $add_0(_history, new HistoryMessage);
  }
  this.history_0 = $getRepeatedObj(values, 1, _history);
  _users = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 2); i_0++) {
    $add_0(_users, new User);
  }
  this.users = $getRepeatedObj(values, 2, _users);
}
;
_.serialize = function serialize_108(writer){
  $writeRepeatedObj(writer, 1, this.history_0);
  $writeRepeatedObj(writer, 2, this.users);
}
;
_.toString$ = function toString_109(){
  var res;
  res = 'tuple LoadHistory{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseLoadHistory_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseLoadHistory', 318, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseSearchContacts(){
}

defineClass(411, 35, {411:1, 4:1, 35:1}, ResponseSearchContacts);
_.getHeaderKey = function getHeaderKey_71(){
  return 113;
}
;
_.parse_0 = function parse_116(values){
  var _users, i_0;
  _users = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 1); i_0++) {
    $add_0(_users, new User);
  }
  this.users = $getRepeatedObj(values, 1, _users);
}
;
_.serialize = function serialize_109(writer){
  $writeRepeatedObj(writer, 1, this.users);
}
;
_.toString$ = function toString_110(){
  var res;
  res = 'tuple SearchContacts{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseSearchContacts_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseSearchContacts', 411, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseSendAuthCode(){
}

defineClass(315, 35, {315:1, 4:1, 35:1}, ResponseSendAuthCode);
_.getHeaderKey = function getHeaderKey_72(){
  return 2;
}
;
_.parse_0 = function parse_117(values){
  this.smsHash = convertString($getBytes(values, 1));
  this.isRegistered = neq($getLong(values, 2), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_110(writer){
  if (this.smsHash == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.smsHash);
  $writeBool(writer, 2, this.isRegistered);
}
;
_.toString$ = function toString_111(){
  var res;
  res = 'tuple SendAuthCode{';
  res += '}';
  return res;
}
;
_.isRegistered = false;
var Lim_actor_model_api_rpc_ResponseSendAuthCode_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseSendAuthCode', 315, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseSeq(){
}

defineClass(89, 35, {89:1, 4:1, 35:1}, ResponseSeq);
_.getHeaderKey = function getHeaderKey_73(){
  return 72;
}
;
_.parse_0 = function parse_118(values){
  this.seq = convertInt($getLong(values, 1));
  this.state = $getBytes(values, 2);
}
;
_.serialize = function serialize_111(writer){
  $writeInt(writer, 1, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.state);
}
;
_.toString$ = function toString_112(){
  var res;
  res = 'response Seq{';
  res += 'seq=' + this.seq;
  res += ', state=' + byteArrayToString(this.state);
  res += '}';
  return res;
}
;
_.seq = 0;
var Lim_actor_model_api_rpc_ResponseSeq_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseSeq', 89, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseSeqDate(){
}

defineClass(323, 35, {323:1, 4:1, 35:1}, ResponseSeqDate);
_.getHeaderKey = function getHeaderKey_74(){
  return 102;
}
;
_.parse_0 = function parse_119(values){
  this.seq = convertInt($getLong(values, 1));
  this.state = $getBytes(values, 2);
  this.date = $getLong(values, 3);
}
;
_.serialize = function serialize_112(writer){
  $writeInt(writer, 1, this.seq);
  if (this.state == null) {
    throw new IOException;
  }
  $writeBytes(writer, 2, this.state);
  $writeLong(writer, 3, this.date);
}
;
_.toString$ = function toString_113(){
  var res;
  res = 'response SeqDate{';
  res += 'seq=' + this.seq;
  res += ', state=' + byteArrayToString(this.state);
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.seq = 0;
var Lim_actor_model_api_rpc_ResponseSeqDate_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseSeqDate', 323, Lim_actor_model_network_parser_Response_2_classLit);
function ResponseVoid(){
}

defineClass(88, 35, {88:1, 4:1, 35:1}, ResponseVoid);
_.getHeaderKey = function getHeaderKey_75(){
  return 50;
}
;
_.parse_0 = function parse_120(values){
}
;
_.serialize = function serialize_113(writer){
}
;
_.toString$ = function toString_114(){
  var res;
  res = 'response Void{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_rpc_ResponseVoid_2_classLit = createForClass('im.actor.model.api.rpc', 'ResponseVoid', 88, Lim_actor_model_network_parser_Response_2_classLit);
defineClass(800, 787, $intern_14);
var Lim_actor_model_network_parser_Update_2_classLit = createForClass('im.actor.model.network.parser', 'Update', 800, Lim_actor_model_network_parser_HeaderBserObject_2_classLit);
function UpdateChatClear(){
}

function UpdateChatClear_0(peer){
  this.peer = peer;
}

defineClass(140, 800, {140:1, 4:1}, UpdateChatClear, UpdateChatClear_0);
_.getHeaderKey = function getHeaderKey_76(){
  return 47;
}
;
_.parse_0 = function parse_121(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
}
;
_.serialize = function serialize_114(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
}
;
_.toString$ = function toString_115(){
  var res;
  res = 'update ChatClear{';
  res += 'peer=' + this.peer;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_updates_UpdateChatClear_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateChatClear', 140, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateChatDelete(){
}

function UpdateChatDelete_0(peer){
  this.peer = peer;
}

defineClass(141, 800, {141:1, 4:1}, UpdateChatDelete, UpdateChatDelete_0);
_.getHeaderKey = function getHeaderKey_77(){
  return 48;
}
;
_.parse_0 = function parse_122(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
}
;
_.serialize = function serialize_115(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
}
;
_.toString$ = function toString_116(){
  var res;
  res = 'update ChatDelete{';
  res += 'peer=' + this.peer;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_updates_UpdateChatDelete_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateChatDelete', 141, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateConfig(){
}

defineClass(426, 800, {426:1, 4:1}, UpdateConfig);
_.getHeaderKey = function getHeaderKey_78(){
  return 42;
}
;
_.parse_0 = function parse_123(values){
  this.config = dynamicCast($getObj(values, 1, new Config), 217);
}
;
_.serialize = function serialize_116(writer){
  if (!this.config) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.config);
}
;
_.toString$ = function toString_117(){
  var res;
  res = 'update Config{';
  res += 'config=' + this.config;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_updates_UpdateConfig_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateConfig', 426, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateContactRegistered(){
}

defineClass(107, 800, {107:1, 4:1}, UpdateContactRegistered);
_.getHeaderKey = function getHeaderKey_79(){
  return 5;
}
;
_.parse_0 = function parse_124(values){
  this.uid = convertInt($getLong(values, 1));
  this.isSilent = neq($getLong(values, 2), {l:0, m:0, h:0});
  this.date = $getLong(values, 3);
}
;
_.serialize = function serialize_117(writer){
  $writeInt(writer, 1, this.uid);
  $writeBool(writer, 2, this.isSilent);
  $writeLong(writer, 3, this.date);
}
;
_.toString$ = function toString_118(){
  var res;
  res = 'update ContactRegistered{';
  res += 'uid=' + this.uid;
  res += ', isSilent=' + this.isSilent;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.isSilent = false;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateContactRegistered_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateContactRegistered', 107, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateContactsAdded(){
}

function UpdateContactsAdded_0(uids){
  this.uids = uids;
}

defineClass(90, 800, {90:1, 4:1}, UpdateContactsAdded, UpdateContactsAdded_0);
_.getHeaderKey = function getHeaderKey_80(){
  return 40;
}
;
_.parse_0 = function parse_125(values){
  this.uids = $getRepeatedInt(values, 1);
}
;
_.serialize = function serialize_118(writer){
  $writeRepeatedInt(writer, 1, this.uids);
}
;
_.toString$ = function toString_119(){
  var res;
  res = 'update ContactsAdded{';
  res += 'uids=' + this.uids.array.length;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_updates_UpdateContactsAdded_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateContactsAdded', 90, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateContactsRemoved(){
}

defineClass(108, 800, {108:1, 4:1}, UpdateContactsRemoved);
_.getHeaderKey = function getHeaderKey_81(){
  return 41;
}
;
_.parse_0 = function parse_126(values){
  this.uids = $getRepeatedInt(values, 1);
}
;
_.serialize = function serialize_119(writer){
  $writeRepeatedInt(writer, 1, this.uids);
}
;
_.toString$ = function toString_120(){
  var res;
  res = 'update ContactsRemoved{';
  res += 'uids=' + this.uids.array.length;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_updates_UpdateContactsRemoved_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateContactsRemoved', 108, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateEmailMoved(){
}

defineClass(424, 800, {424:1, 4:1}, UpdateEmailMoved);
_.getHeaderKey = function getHeaderKey_82(){
  return 102;
}
;
_.parse_0 = function parse_127(values){
  this.emailId = convertInt($getLong(values, 1));
  this.uid = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_120(writer){
  $writeInt(writer, 1, this.emailId);
  $writeInt(writer, 2, this.uid);
}
;
_.toString$ = function toString_121(){
  var res;
  res = 'update EmailMoved{';
  res += 'emailId=' + this.emailId;
  res += ', uid=' + this.uid;
  res += '}';
  return res;
}
;
_.emailId = 0;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateEmailMoved_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateEmailMoved', 424, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateEmailTitleChanged(){
}

defineClass(423, 800, {423:1, 4:1}, UpdateEmailTitleChanged);
_.getHeaderKey = function getHeaderKey_83(){
  return 98;
}
;
_.parse_0 = function parse_128(values){
  this.emailId = convertInt($getLong(values, 1));
  this.title_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_121(writer){
  $writeInt(writer, 1, this.emailId);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.title_0);
}
;
_.toString$ = function toString_122(){
  var res;
  res = 'update EmailTitleChanged{';
  res += 'emailId=' + this.emailId;
  res += ', title=' + this.title_0;
  res += '}';
  return res;
}
;
_.emailId = 0;
var Lim_actor_model_api_updates_UpdateEmailTitleChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateEmailTitleChanged', 423, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupAvatarChanged(){
}

defineClass(208, 800, {208:1, 4:1}, UpdateGroupAvatarChanged);
_.getHeaderKey = function getHeaderKey_84(){
  return 39;
}
;
_.parse_0 = function parse_129(values){
  this.groupId = convertInt($getLong(values, 1));
  this.rid = $getLong(values, 5);
  this.uid = convertInt($getLong(values, 2));
  this.avatar = dynamicCast($optObj(values, 3, new Avatar), 50);
  this.date = $getLong(values, 4);
}
;
_.serialize = function serialize_122(writer){
  $writeInt(writer, 1, this.groupId);
  $writeLong(writer, 5, this.rid);
  $writeInt(writer, 2, this.uid);
  !!this.avatar && $writeObject(writer, 3, this.avatar);
  $writeLong(writer, 4, this.date);
}
;
_.toString$ = function toString_123(){
  var res;
  res = 'update GroupAvatarChanged{';
  res += 'groupId=' + this.groupId;
  res += ', rid=' + toString_2(this.rid);
  res += ', uid=' + this.uid;
  res += ', avatar=' + (this.avatar?'set':'empty');
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.groupId = 0;
_.rid = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_updates_UpdateGroupAvatarChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupAvatarChanged', 208, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupInvite(){
}

defineClass(110, 800, {110:1, 4:1}, UpdateGroupInvite);
_.getHeaderKey = function getHeaderKey_85(){
  return 36;
}
;
_.parse_0 = function parse_130(values){
  this.groupId = convertInt($getLong(values, 1));
  this.rid = $getLong(values, 9);
  this.inviteUid = convertInt($getLong(values, 5));
  this.date = $getLong(values, 8);
}
;
_.serialize = function serialize_123(writer){
  $writeInt(writer, 1, this.groupId);
  $writeLong(writer, 9, this.rid);
  $writeInt(writer, 5, this.inviteUid);
  $writeLong(writer, 8, this.date);
}
;
_.toString$ = function toString_124(){
  var res;
  res = 'update GroupInvite{';
  res += 'groupId=' + this.groupId;
  res += ', rid=' + toString_2(this.rid);
  res += ', inviteUid=' + this.inviteUid;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.groupId = 0;
_.inviteUid = 0;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_updates_UpdateGroupInvite_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupInvite', 110, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupMembersUpdate(){
}

defineClass(142, 800, {142:1, 4:1}, UpdateGroupMembersUpdate);
_.getHeaderKey = function getHeaderKey_86(){
  return 44;
}
;
_.parse_0 = function parse_131(values){
  var _members, i_0;
  this.groupId = convertInt($getLong(values, 1));
  _members = new ArrayList;
  for (i_0 = 0; i_0 < $getRepeatedCount(values, 2); i_0++) {
    $add_0(_members, new Member);
  }
  this.members = $getRepeatedObj(values, 2, _members);
}
;
_.serialize = function serialize_124(writer){
  $writeInt(writer, 1, this.groupId);
  $writeRepeatedObj(writer, 2, this.members);
}
;
_.toString$ = function toString_125(){
  var res;
  res = 'update GroupMembersUpdate{';
  res += 'groupId=' + this.groupId;
  res += ', members=' + this.members;
  res += '}';
  return res;
}
;
_.groupId = 0;
var Lim_actor_model_api_updates_UpdateGroupMembersUpdate_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupMembersUpdate', 142, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupOnline(){
}

defineClass(213, 800, {213:1, 4:1}, UpdateGroupOnline);
_.getHeaderKey = function getHeaderKey_87(){
  return 33;
}
;
_.parse_0 = function parse_132(values){
  this.groupId = convertInt($getLong(values, 1));
  this.count = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_125(writer){
  $writeInt(writer, 1, this.groupId);
  $writeInt(writer, 2, this.count);
}
;
_.toString$ = function toString_126(){
  var res;
  res = 'update GroupOnline{';
  res += 'groupId=' + this.groupId;
  res += ', count=' + this.count;
  res += '}';
  return res;
}
;
_.count = 0;
_.groupId = 0;
var Lim_actor_model_api_updates_UpdateGroupOnline_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupOnline', 213, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupTitleChanged(){
}

defineClass(207, 800, {207:1, 4:1}, UpdateGroupTitleChanged);
_.getHeaderKey = function getHeaderKey_88(){
  return 38;
}
;
_.parse_0 = function parse_133(values){
  this.groupId = convertInt($getLong(values, 1));
  this.rid = $getLong(values, 5);
  this.uid = convertInt($getLong(values, 2));
  this.title_0 = convertString($getBytes(values, 3));
  this.date = $getLong(values, 4);
}
;
_.serialize = function serialize_126(writer){
  $writeInt(writer, 1, this.groupId);
  $writeLong(writer, 5, this.rid);
  $writeInt(writer, 2, this.uid);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.title_0);
  $writeLong(writer, 4, this.date);
}
;
_.toString$ = function toString_127(){
  var res;
  res = 'update GroupTitleChanged{';
  res += 'groupId=' + this.groupId;
  res += ', rid=' + toString_2(this.rid);
  res += ', uid=' + this.uid;
  res += ', title=' + this.title_0;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.groupId = 0;
_.rid = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_updates_UpdateGroupTitleChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupTitleChanged', 207, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupUserAdded(){
}

defineClass(111, 800, {111:1, 4:1}, UpdateGroupUserAdded);
_.getHeaderKey = function getHeaderKey_89(){
  return 21;
}
;
_.parse_0 = function parse_134(values){
  this.groupId = convertInt($getLong(values, 1));
  this.rid = $getLong(values, 5);
  this.uid = convertInt($getLong(values, 2));
  this.inviterUid = convertInt($getLong(values, 3));
  this.date = $getLong(values, 4);
}
;
_.serialize = function serialize_127(writer){
  $writeInt(writer, 1, this.groupId);
  $writeLong(writer, 5, this.rid);
  $writeInt(writer, 2, this.uid);
  $writeInt(writer, 3, this.inviterUid);
  $writeLong(writer, 4, this.date);
}
;
_.toString$ = function toString_128(){
  var res;
  res = 'update GroupUserAdded{';
  res += 'groupId=' + this.groupId;
  res += ', rid=' + toString_2(this.rid);
  res += ', uid=' + this.uid;
  res += ', inviterUid=' + this.inviterUid;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.groupId = 0;
_.inviterUid = 0;
_.rid = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_updates_UpdateGroupUserAdded_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupUserAdded', 111, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupUserKick(){
}

defineClass(113, 800, {113:1, 4:1}, UpdateGroupUserKick);
_.getHeaderKey = function getHeaderKey_90(){
  return 24;
}
;
_.parse_0 = function parse_135(values){
  this.groupId = convertInt($getLong(values, 1));
  this.rid = $getLong(values, 5);
  this.uid = convertInt($getLong(values, 2));
  this.kickerUid = convertInt($getLong(values, 3));
  this.date = $getLong(values, 4);
}
;
_.serialize = function serialize_128(writer){
  $writeInt(writer, 1, this.groupId);
  $writeLong(writer, 5, this.rid);
  $writeInt(writer, 2, this.uid);
  $writeInt(writer, 3, this.kickerUid);
  $writeLong(writer, 4, this.date);
}
;
_.toString$ = function toString_129(){
  var res;
  res = 'update GroupUserKick{';
  res += 'groupId=' + this.groupId;
  res += ', rid=' + toString_2(this.rid);
  res += ', uid=' + this.uid;
  res += ', kickerUid=' + this.kickerUid;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.groupId = 0;
_.kickerUid = 0;
_.rid = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_updates_UpdateGroupUserKick_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupUserKick', 113, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateGroupUserLeave(){
}

defineClass(112, 800, {112:1, 4:1}, UpdateGroupUserLeave);
_.getHeaderKey = function getHeaderKey_91(){
  return 23;
}
;
_.parse_0 = function parse_136(values){
  this.groupId = convertInt($getLong(values, 1));
  this.rid = $getLong(values, 4);
  this.uid = convertInt($getLong(values, 2));
  this.date = $getLong(values, 3);
}
;
_.serialize = function serialize_129(writer){
  $writeInt(writer, 1, this.groupId);
  $writeLong(writer, 4, this.rid);
  $writeInt(writer, 2, this.uid);
  $writeLong(writer, 3, this.date);
}
;
_.toString$ = function toString_130(){
  var res;
  res = 'update GroupUserLeave{';
  res += 'groupId=' + this.groupId;
  res += ', rid=' + toString_2(this.rid);
  res += ', uid=' + this.uid;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.groupId = 0;
_.rid = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_updates_UpdateGroupUserLeave_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateGroupUserLeave', 112, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateMessage(){
}

defineClass(109, 800, {109:1, 4:1}, UpdateMessage);
_.getHeaderKey = function getHeaderKey_92(){
  return 55;
}
;
_.parse_0 = function parse_137(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.senderUid = convertInt($getLong(values, 2));
  this.date = $getLong(values, 3);
  this.rid = $getLong(values, 4);
  this.message_0 = fromBytes_0($getBytes(values, 5));
}
;
_.serialize = function serialize_130(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeInt(writer, 2, this.senderUid);
  $writeLong(writer, 3, this.date);
  $writeLong(writer, 4, this.rid);
  if (!this.message_0) {
    throw new IOException;
  }
  $writeBytes(writer, 5, $buildContainer_0(this.message_0));
}
;
_.toString$ = function toString_131(){
  var res;
  res = 'update Message{';
  res += 'peer=' + this.peer;
  res += ', senderUid=' + this.senderUid;
  res += ', date=' + toString_2(this.date);
  res += ', rid=' + toString_2(this.rid);
  res += ', message=' + this.message_0;
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
_.senderUid = 0;
var Lim_actor_model_api_updates_UpdateMessage_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateMessage', 109, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateMessageDelete(){
}

function UpdateMessageDelete_0(peer, rids){
  this.peer = peer;
  this.rids = rids;
}

defineClass(139, 800, {139:1, 4:1}, UpdateMessageDelete, UpdateMessageDelete_0);
_.getHeaderKey = function getHeaderKey_93(){
  return 46;
}
;
_.parse_0 = function parse_138(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.rids = $getRepeatedLong(values, 2);
}
;
_.serialize = function serialize_131(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeRepeatedLong(writer, 2, this.rids);
}
;
_.toString$ = function toString_132(){
  var res;
  res = 'update MessageDelete{';
  res += 'peer=' + this.peer;
  res += ', rids=' + this.rids;
  res += '}';
  return res;
}
;
var Lim_actor_model_api_updates_UpdateMessageDelete_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateMessageDelete', 139, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateMessageRead(){
}

defineClass(205, 800, {205:1, 4:1}, UpdateMessageRead);
_.getHeaderKey = function getHeaderKey_94(){
  return 19;
}
;
_.parse_0 = function parse_139(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.startDate = $getLong(values, 2);
  this.readDate = $getLong(values, 3);
}
;
_.serialize = function serialize_132(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 2, this.startDate);
  $writeLong(writer, 3, this.readDate);
}
;
_.toString$ = function toString_133(){
  var res;
  res = 'update MessageRead{';
  res += 'peer=' + this.peer;
  res += ', startDate=' + toString_2(this.startDate);
  res += ', readDate=' + toString_2(this.readDate);
  res += '}';
  return res;
}
;
_.readDate = {l:0, m:0, h:0};
_.startDate = {l:0, m:0, h:0};
var Lim_actor_model_api_updates_UpdateMessageRead_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateMessageRead', 205, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateMessageReadByMe(){
}

defineClass(206, 800, {206:1, 4:1}, UpdateMessageReadByMe);
_.getHeaderKey = function getHeaderKey_95(){
  return 50;
}
;
_.parse_0 = function parse_140(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.startDate = $getLong(values, 2);
}
;
_.serialize = function serialize_133(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 2, this.startDate);
}
;
_.toString$ = function toString_134(){
  var res;
  res = 'update MessageReadByMe{';
  res += 'peer=' + this.peer;
  res += ', startDate=' + toString_2(this.startDate);
  res += '}';
  return res;
}
;
_.startDate = {l:0, m:0, h:0};
var Lim_actor_model_api_updates_UpdateMessageReadByMe_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateMessageReadByMe', 206, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateMessageReceived(){
}

defineClass(204, 800, {204:1, 4:1}, UpdateMessageReceived);
_.getHeaderKey = function getHeaderKey_96(){
  return 54;
}
;
_.parse_0 = function parse_141(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.startDate = $getLong(values, 2);
  this.receivedDate = $getLong(values, 3);
}
;
_.serialize = function serialize_134(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 2, this.startDate);
  $writeLong(writer, 3, this.receivedDate);
}
;
_.toString$ = function toString_135(){
  var res;
  res = 'update MessageReceived{';
  res += 'peer=' + this.peer;
  res += ', startDate=' + toString_2(this.startDate);
  res += ', receivedDate=' + toString_2(this.receivedDate);
  res += '}';
  return res;
}
;
_.receivedDate = {l:0, m:0, h:0};
_.startDate = {l:0, m:0, h:0};
var Lim_actor_model_api_updates_UpdateMessageReceived_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateMessageReceived', 204, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateMessageSent(){
}

function UpdateMessageSent_0(peer, rid, date){
  this.peer = peer;
  this.rid = rid;
  this.date = date;
}

defineClass(138, 800, {138:1, 4:1}, UpdateMessageSent, UpdateMessageSent_0);
_.getHeaderKey = function getHeaderKey_97(){
  return 4;
}
;
_.parse_0 = function parse_142(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.rid = $getLong(values, 2);
  this.date = $getLong(values, 3);
}
;
_.serialize = function serialize_135(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 2, this.rid);
  $writeLong(writer, 3, this.date);
}
;
_.toString$ = function toString_136(){
  var res;
  res = 'update MessageSent{';
  res += 'peer=' + this.peer;
  res += ', rid=' + toString_2(this.rid);
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_api_updates_UpdateMessageSent_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateMessageSent', 138, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateParameterChanged(){
}

function UpdateParameterChanged_0(key, value_0){
  this.key = key;
  this.value_0 = value_0;
}

defineClass(114, 800, {114:1, 4:1}, UpdateParameterChanged, UpdateParameterChanged_0);
_.getHeaderKey = function getHeaderKey_98(){
  return 131;
}
;
_.parse_0 = function parse_143(values){
  this.key = convertString($getBytes(values, 1));
  this.value_0 = convertString($getBytes_0(values, 2));
}
;
_.serialize = function serialize_136(writer){
  if (this.key == null) {
    throw new IOException;
  }
  $writeString(writer, 1, this.key);
  this.value_0 != null && $writeString(writer, 2, this.value_0);
}
;
_.toString$ = function toString_137(){
  var res;
  res = 'update ParameterChanged{';
  res += '}';
  return res;
}
;
var Lim_actor_model_api_updates_UpdateParameterChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateParameterChanged', 114, Lim_actor_model_network_parser_Update_2_classLit);
function UpdatePhoneMoved(){
}

defineClass(420, 800, {420:1, 4:1}, UpdatePhoneMoved);
_.getHeaderKey = function getHeaderKey_99(){
  return 101;
}
;
_.parse_0 = function parse_144(values){
  this.phoneId = convertInt($getLong(values, 1));
  this.uid = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_137(writer){
  $writeInt(writer, 1, this.phoneId);
  $writeInt(writer, 2, this.uid);
}
;
_.toString$ = function toString_138(){
  var res;
  res = 'update PhoneMoved{';
  res += 'phoneId=' + this.phoneId;
  res += ', uid=' + this.uid;
  res += '}';
  return res;
}
;
_.phoneId = 0;
_.uid = 0;
var Lim_actor_model_api_updates_UpdatePhoneMoved_2_classLit = createForClass('im.actor.model.api.updates', 'UpdatePhoneMoved', 420, Lim_actor_model_network_parser_Update_2_classLit);
function UpdatePhoneTitleChanged(){
}

defineClass(419, 800, {419:1, 4:1}, UpdatePhoneTitleChanged);
_.getHeaderKey = function getHeaderKey_100(){
  return 89;
}
;
_.parse_0 = function parse_145(values){
  this.phoneId = convertInt($getLong(values, 2));
  this.title_0 = convertString($getBytes(values, 3));
}
;
_.serialize = function serialize_138(writer){
  $writeInt(writer, 2, this.phoneId);
  if (this.title_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 3, this.title_0);
}
;
_.toString$ = function toString_139(){
  var res;
  res = 'update PhoneTitleChanged{';
  res += 'phoneId=' + this.phoneId;
  res += ', title=' + this.title_0;
  res += '}';
  return res;
}
;
_.phoneId = 0;
var Lim_actor_model_api_updates_UpdatePhoneTitleChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdatePhoneTitleChanged', 419, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateTyping(){
}

defineClass(209, 800, {209:1, 4:1}, UpdateTyping);
_.getHeaderKey = function getHeaderKey_101(){
  return 6;
}
;
_.parse_0 = function parse_146(values){
  this.peer = dynamicCast($getObj(values, 1, new Peer), 24);
  this.uid = convertInt($getLong(values, 2));
  this.typingType = parse_41(convertInt($getLong(values, 3)));
}
;
_.serialize = function serialize_139(writer){
  if (!this.peer) {
    throw new IOException;
  }
  $writeObject(writer, 1, this.peer);
  $writeInt(writer, 2, this.uid);
  if (!this.typingType) {
    throw new IOException;
  }
  $writeInt(writer, 3, this.typingType.value_0);
}
;
_.toString$ = function toString_140(){
  var res;
  res = 'update Typing{';
  res += 'peer=' + this.peer;
  res += ', uid=' + this.uid;
  res += ', typingType=' + this.typingType;
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateTyping_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateTyping', 209, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserAvatarChanged(){
}

defineClass(202, 800, {202:1, 4:1}, UpdateUserAvatarChanged);
_.getHeaderKey = function getHeaderKey_102(){
  return 16;
}
;
_.parse_0 = function parse_147(values){
  this.uid = convertInt($getLong(values, 1));
  this.avatar = dynamicCast($optObj(values, 2, new Avatar), 50);
}
;
_.serialize = function serialize_140(writer){
  $writeInt(writer, 1, this.uid);
  !!this.avatar && $writeObject(writer, 2, this.avatar);
}
;
_.toString$ = function toString_141(){
  var res;
  res = 'update UserAvatarChanged{';
  res += 'uid=' + this.uid;
  res += ', avatar=' + (this.avatar?'set':'empty');
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserAvatarChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserAvatarChanged', 202, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserContactsChanged(){
}

defineClass(425, 800, {425:1, 4:1}, UpdateUserContactsChanged);
_.getHeaderKey = function getHeaderKey_103(){
  return 86;
}
;
_.parse_0 = function parse_148(values){
  this.uid = convertInt($getLong(values, 1));
  this.phones = $getRepeatedInt(values, 2);
  this.emails = $getRepeatedInt(values, 3);
}
;
_.serialize = function serialize_141(writer){
  $writeInt(writer, 1, this.uid);
  $writeRepeatedInt(writer, 2, this.phones);
  $writeRepeatedInt(writer, 3, this.emails);
}
;
_.toString$ = function toString_142(){
  var res;
  res = 'update UserContactsChanged{';
  res += 'uid=' + this.uid;
  res += ', phones=' + this.phones.array.length;
  res += ', emails=' + this.emails.array.length;
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserContactsChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserContactsChanged', 425, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserEmailAdded(){
}

defineClass(421, 800, {421:1, 4:1}, UpdateUserEmailAdded);
_.getHeaderKey = function getHeaderKey_104(){
  return 96;
}
;
_.parse_0 = function parse_149(values){
  this.uid = convertInt($getLong(values, 1));
  this.emailId = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_142(writer){
  $writeInt(writer, 1, this.uid);
  $writeInt(writer, 2, this.emailId);
}
;
_.toString$ = function toString_143(){
  var res;
  res = 'update UserEmailAdded{';
  res += 'uid=' + this.uid;
  res += ', emailId=' + this.emailId;
  res += '}';
  return res;
}
;
_.emailId = 0;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserEmailAdded_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserEmailAdded', 421, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserEmailRemoved(){
}

defineClass(422, 800, {422:1, 4:1}, UpdateUserEmailRemoved);
_.getHeaderKey = function getHeaderKey_105(){
  return 97;
}
;
_.parse_0 = function parse_150(values){
  this.uid = convertInt($getLong(values, 1));
  this.emailId = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_143(writer){
  $writeInt(writer, 1, this.uid);
  $writeInt(writer, 2, this.emailId);
}
;
_.toString$ = function toString_144(){
  var res;
  res = 'update UserEmailRemoved{';
  res += 'uid=' + this.uid;
  res += ', emailId=' + this.emailId;
  res += '}';
  return res;
}
;
_.emailId = 0;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserEmailRemoved_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserEmailRemoved', 422, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserLastSeen(){
}

defineClass(212, 800, {212:1, 4:1}, UpdateUserLastSeen);
_.getHeaderKey = function getHeaderKey_106(){
  return 9;
}
;
_.parse_0 = function parse_151(values){
  this.uid = convertInt($getLong(values, 1));
  this.date = $getLong(values, 2);
}
;
_.serialize = function serialize_144(writer){
  $writeInt(writer, 1, this.uid);
  $writeLong(writer, 2, this.date);
}
;
_.toString$ = function toString_145(){
  var res;
  res = 'update UserLastSeen{';
  res += 'uid=' + this.uid;
  res += ', date=' + toString_2(this.date);
  res += '}';
  return res;
}
;
_.date = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserLastSeen_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserLastSeen', 212, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserLocalNameChanged(){
}

defineClass(106, 800, {106:1, 4:1}, UpdateUserLocalNameChanged);
_.getHeaderKey = function getHeaderKey_107(){
  return 51;
}
;
_.parse_0 = function parse_152(values){
  this.uid = convertInt($getLong(values, 1));
  this.localName = convertString($getBytes_0(values, 2));
}
;
_.serialize = function serialize_145(writer){
  $writeInt(writer, 1, this.uid);
  this.localName != null && $writeString(writer, 2, this.localName);
}
;
_.toString$ = function toString_146(){
  var res;
  res = 'update UserLocalNameChanged{';
  res += 'uid=' + this.uid;
  res += ', localName=' + this.localName;
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserLocalNameChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserLocalNameChanged', 106, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserNameChanged(){
}

defineClass(203, 800, {203:1, 4:1}, UpdateUserNameChanged);
_.getHeaderKey = function getHeaderKey_108(){
  return 32;
}
;
_.parse_0 = function parse_153(values){
  this.uid = convertInt($getLong(values, 1));
  this.name_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_146(writer){
  $writeInt(writer, 1, this.uid);
  if (this.name_0 == null) {
    throw new IOException;
  }
  $writeString(writer, 2, this.name_0);
}
;
_.toString$ = function toString_147(){
  var res;
  res = 'update UserNameChanged{';
  res += 'uid=' + this.uid;
  res += ', name=' + this.name_0;
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserNameChanged_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserNameChanged', 203, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserOffline(){
}

defineClass(211, 800, {211:1, 4:1}, UpdateUserOffline);
_.getHeaderKey = function getHeaderKey_109(){
  return 8;
}
;
_.parse_0 = function parse_154(values){
  this.uid = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_147(writer){
  $writeInt(writer, 1, this.uid);
}
;
_.toString$ = function toString_148(){
  var res;
  res = 'update UserOffline{';
  res += 'uid=' + this.uid;
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserOffline_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserOffline', 211, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserOnline(){
}

defineClass(210, 800, {210:1, 4:1}, UpdateUserOnline);
_.getHeaderKey = function getHeaderKey_110(){
  return 7;
}
;
_.parse_0 = function parse_155(values){
  this.uid = convertInt($getLong(values, 1));
}
;
_.serialize = function serialize_148(writer){
  $writeInt(writer, 1, this.uid);
}
;
_.toString$ = function toString_149(){
  var res;
  res = 'update UserOnline{';
  res += 'uid=' + this.uid;
  res += '}';
  return res;
}
;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserOnline_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserOnline', 210, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserPhoneAdded(){
}

defineClass(417, 800, {417:1, 4:1}, UpdateUserPhoneAdded);
_.getHeaderKey = function getHeaderKey_111(){
  return 87;
}
;
_.parse_0 = function parse_156(values){
  this.uid = convertInt($getLong(values, 1));
  this.phoneId = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_149(writer){
  $writeInt(writer, 1, this.uid);
  $writeInt(writer, 2, this.phoneId);
}
;
_.toString$ = function toString_150(){
  var res;
  res = 'update UserPhoneAdded{';
  res += 'uid=' + this.uid;
  res += ', phoneId=' + this.phoneId;
  res += '}';
  return res;
}
;
_.phoneId = 0;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserPhoneAdded_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserPhoneAdded', 417, Lim_actor_model_network_parser_Update_2_classLit);
function UpdateUserPhoneRemoved(){
}

defineClass(418, 800, {418:1, 4:1}, UpdateUserPhoneRemoved);
_.getHeaderKey = function getHeaderKey_112(){
  return 88;
}
;
_.parse_0 = function parse_157(values){
  this.uid = convertInt($getLong(values, 1));
  this.phoneId = convertInt($getLong(values, 2));
}
;
_.serialize = function serialize_150(writer){
  $writeInt(writer, 1, this.uid);
  $writeInt(writer, 2, this.phoneId);
}
;
_.toString$ = function toString_151(){
  var res;
  res = 'update UserPhoneRemoved{';
  res += 'uid=' + this.uid;
  res += ', phoneId=' + this.phoneId;
  res += '}';
  return res;
}
;
_.phoneId = 0;
_.uid = 0;
var Lim_actor_model_api_updates_UpdateUserPhoneRemoved_2_classLit = createForClass('im.actor.model.api.updates', 'UpdateUserPhoneRemoved', 418, Lim_actor_model_network_parser_Update_2_classLit);
function $initActor(this$static, context){
  this$static.context = context;
}

function $reply(this$static, message){
  !!this$static.context.actorScope.sender && $send_2(this$static.context.actorScope.sender, message, this$static.context.actorScope.actorRef);
}

defineClass(785, 1, {});
_.onReceive = function onReceive(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
_.preStart = function preStart(){
}
;
var Lim_actor_model_droidkit_actors_Actor_2_classLit = createForClass('im.actor.model.droidkit.actors', 'Actor', 785, Ljava_lang_Object_2_classLit);
function TimerActor(){
}

defineClass(744, 785, {}, TimerActor);
_.onReceive = function onReceive_0(message){
  instanceOf(message, 78)?$sendOnce_0(this.context.actorScope.actorRef, dynamicCast(message, 78).timerCompat, dynamicCast(message, 78).delay):instanceOf(message, 146)?$cancelMessage(this.context.actorScope.actorRef, dynamicCast(message, 146).timerCompat):instanceOf(message, 59)?dynamicCast(message, 59).runnable.run():(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
var Lim_actor_model_concurrency_TimerActor_2_classLit = createForClass('im.actor.model.concurrency', 'TimerActor', 744, Lim_actor_model_droidkit_actors_Actor_2_classLit);
function TimerActor$Cancel(timerCompat){
  this.timerCompat = timerCompat;
}

defineClass(146, 1, {146:1}, TimerActor$Cancel);
var Lim_actor_model_concurrency_TimerActor$Cancel_2_classLit = createForClass('im.actor.model.concurrency', 'TimerActor/Cancel', 146, Ljava_lang_Object_2_classLit);
function TimerActor$Schedule(timerCompat, delay){
  this.timerCompat = timerCompat;
  this.delay = delay;
}

defineClass(78, 1, {78:1}, TimerActor$Schedule);
_.delay = {l:0, m:0, h:0};
var Lim_actor_model_concurrency_TimerActor$Schedule_2_classLit = createForClass('im.actor.model.concurrency', 'TimerActor/Schedule', 78, Ljava_lang_Object_2_classLit);
function $clinit_TimerCompat(){
  $clinit_TimerCompat = emptyMethod;
  TIMER_ACTOR = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new TimerCompat$1, null), 'actor/global_timer');
}

function $cancel_0(this$static){
  $send_1(TIMER_ACTOR, new TimerActor$Cancel(this$static));
}

function $schedule_0(this$static, delay){
  $send_1(TIMER_ACTOR, new TimerActor$Schedule(this$static, delay));
}

function TimerCompat(runnable){
  $clinit_TimerCompat();
  this.runnable = runnable;
}

defineClass(59, 1, {59:1}, TimerCompat);
var TIMER_ACTOR;
var Lim_actor_model_concurrency_TimerCompat_2_classLit = createForClass('im.actor.model.concurrency', 'TimerCompat', 59, Ljava_lang_Object_2_classLit);
function TimerCompat$1(){
}

defineClass(745, 1, {}, TimerCompat$1);
_.create_0 = function create_3(){
  return new TimerActor;
}
;
var Lim_actor_model_concurrency_TimerCompat$1_2_classLit = createForClass('im.actor.model.concurrency', 'TimerCompat/1', 745, Ljava_lang_Object_2_classLit);
function $clinit_CryptoUtils(){
  var charArr;
  $clinit_CryptoUtils = emptyMethod;
  hexArray = (charArr = initDim(C_classLit, $intern_4, 0, 16, 7, 1) , $getChars('0123456789abcdef', 16, charArr, 0) , charArr);
}

function hex(bytes){
  $clinit_CryptoUtils();
  var hexChars, j, v;
  hexChars = initDim(C_classLit, $intern_4, 0, bytes.length * 2, 7, 1);
  for (j = 0; j < bytes.length; j++) {
    v = bytes[j] & 255;
    hexChars[j * 2] = hexArray[v >>> 4];
    hexChars[j * 2 + 1] = hexArray[v & 15];
  }
  return __valueOf(hexChars, 0, hexChars.length);
}

var hexArray;
function $clinit_BouncyCastleProvider(){
  $clinit_BouncyCastleProvider = emptyMethod;
  new BigInteger_2;
  valueOf_4({l:0, m:0, h:0});
}

function $SHA256(data_0){
  var digest, res;
  digest = new SHA256Digest;
  $update_3(digest, data_0, 0, data_0.length);
  res = initDim(B_classLit, $intern_17, 0, 32, 7, 1);
  $finish(digest);
  intToBigEndian(digest.H1, res, 0);
  intToBigEndian(digest.H2, res, 4);
  intToBigEndian(digest.H3, res, 8);
  intToBigEndian(digest.H4, res, 12);
  intToBigEndian(digest.H5, res, 16);
  intToBigEndian(digest.H6, res, 20);
  intToBigEndian(digest.H7, res, 24);
  intToBigEndian(digest.H8, res, 28);
  $reset_2(digest);
  return res;
}

defineClass(786, 1, {});
var Lim_actor_model_crypto_bouncycastle_BouncyCastleProvider_2_classLit = createForClass('im.actor.model.crypto.bouncycastle', 'BouncyCastleProvider', 786, Ljava_lang_Object_2_classLit);
function ActorContext(scope_0){
  this.actorScope = scope_0;
}

defineClass(722, 1, {}, ActorContext);
var Lim_actor_model_droidkit_actors_ActorContext_2_classLit = createForClass('im.actor.model.droidkit.actors', 'ActorContext', 722, Ljava_lang_Object_2_classLit);
function $cancelMessage(this$static, message){
  $cancelSend(this$static.endpoint, message);
}

function $send(this$static, message){
  $sendMessage_0(this$static.dispatcher, this$static.endpoint, message, getActorTime(), null);
}

function $send_0(this$static, message, delay){
  $sendMessage_0(this$static.dispatcher, this$static.endpoint, message, add_0(getActorTime(), delay), null);
}

function $send_1(this$static, message){
  $sendMessage_0(this$static.dispatcher, this$static.endpoint, message, getActorTime(), null);
}

function $send_2(this$static, message, sender){
  $sendMessage_0(this$static.dispatcher, this$static.endpoint, message, getActorTime(), sender);
}

function $sendOnce(this$static, message){
  $sendMessage_0(this$static.dispatcher, this$static.endpoint, message, getActorTime(), null);
}

function $sendOnce_0(this$static, message, delay){
  $sendMessageOnce(this$static.endpoint, message, add_0(getActorTime(), delay));
}

function ActorRef(endpoint, dispatcher){
  this.endpoint = endpoint;
  this.dispatcher = dispatcher;
}

defineClass(226, 1, {226:1}, ActorRef);
var Lim_actor_model_droidkit_actors_ActorRef_2_classLit = createForClass('im.actor.model.droidkit.actors', 'ActorRef', 226, Ljava_lang_Object_2_classLit);
function $setSender(this$static, sender){
  this$static.sender = sender;
}

function ActorScope(actorSystem, mailbox, dispatcher, path, props, endpoint){
  this.actorSystem = actorSystem;
  this.mailbox = mailbox;
  this.actorRef = new ActorRef(endpoint, dispatcher);
  this.path = path;
  this.props = props;
}

defineClass(357, 1, {357:1}, ActorScope);
var Lim_actor_model_droidkit_actors_ActorScope_2_classLit = createForClass('im.actor.model.droidkit.actors', 'ActorScope', 357, Ljava_lang_Object_2_classLit);
function ActorSelection(props, path){
  this.props = props;
  this.path = path;
}

defineClass(143, 1, {}, ActorSelection);
var Lim_actor_model_droidkit_actors_ActorSelection_2_classLit = createForClass('im.actor.model.droidkit.actors', 'ActorSelection', 143, Ljava_lang_Object_2_classLit);
function $clinit_ActorSystem(){
  $clinit_ActorSystem = emptyMethod;
  mainSystem = new ActorSystem;
}

function $actorOf(this$static, selection){
  return $actorOf_0(this$static, selection.props, selection.path);
}

function $actorOf_0(this$static, props, path){
  var mailboxesDispatcher;
  if (!$hasStringValue(this$static.dispatchers, 'default')) {
    throw new RuntimeException_0("Unknown dispatcherId 'default'");
  }
  mailboxesDispatcher = dynamicCast($getStringValue(this$static.dispatchers, 'default'), 244);
  return $referenceActor(mailboxesDispatcher, path, props);
}

function $addDispatcher(this$static){
  var dispatcher;
  if ($hasStringValue(this$static.dispatchers, 'default')) {
    return;
  }
  dispatcher = createDefaultDispatcher(this$static);
  $addDispatcher_1(this$static, dispatcher);
}

function $addDispatcher_0(this$static){
  var dispatcher;
  if ($hasStringValue(this$static.dispatchers, 'db')) {
    return;
  }
  dispatcher = createDispatcher(this$static);
  $putStringValue(this$static.dispatchers, 'db', dispatcher);
}

function $addDispatcher_1(this$static, dispatcher){
  if ($hasStringValue(this$static.dispatchers, 'default')) {
    return;
  }
  $putStringValue(this$static.dispatchers, 'default', dispatcher);
}

function $setTraceInterface(this$static, traceInterface){
  this$static.traceInterface = traceInterface;
}

function ActorSystem(){
  this.dispatchers = new HashMap;
  $addDispatcher(this);
}

defineClass(452, 1, {}, ActorSystem);
var mainSystem;
var Lim_actor_model_droidkit_actors_ActorSystem_2_classLit = createForClass('im.actor.model.droidkit.actors', 'ActorSystem', 452, Ljava_lang_Object_2_classLit);
function createAtomicInt(){
  if (!threadingProvider_0) {
    throw new RuntimeException_0('Environment is not inited!');
  }
  return new JsAtomicInteger;
}

function createAtomicLong(){
  if (!threadingProvider_0) {
    throw new RuntimeException_0('Environment is not inited!');
  }
  return new JsAtomicLong;
}

function createDefaultDispatcher(actorSystem){
  if (!threadingProvider_0) {
    throw new RuntimeException_0('Environment is not inited!');
  }
  return new JsDispatch(actorSystem);
}

function createDispatcher(actorSystem){
  if (!threadingProvider_0) {
    throw new RuntimeException_0('Environment is not inited!');
  }
  return new JsDispatch(actorSystem);
}

function createThreadLocal(){
  if (!threadingProvider_0) {
    throw new RuntimeException_0('Environment is not inited!');
  }
  return new JsThreadLocal;
}

function getActorTime(){
  if (!threadingProvider_0) {
    throw new RuntimeException_0('Environment is not inited!');
  }
  return fromDouble((new Date_0).jsdate.getTime());
}

function getCurrentTime(){
  if (!threadingProvider_0) {
    throw new RuntimeException_0('Environment is not inited!');
  }
  return fromDouble((new Date_0).jsdate.getTime());
}

function setDispatcherProvider(dispatcherProvider){
  dispatcherProvider_0 = dispatcherProvider;
}

function setThreadingProvider(threadingProvider){
  threadingProvider_0 = threadingProvider;
}

var dispatcherProvider_0, threadingProvider_0;
function $createMailbox(this$static, queue){
  return this$static.mailboxCreator?this$static.mailboxCreator.createMailbox(queue):new Mailbox(queue);
}

function Props(creator, mailboxCreator){
  this.creator = creator;
  this.mailboxCreator = mailboxCreator;
}

defineClass(16, 1, {}, Props);
var Lim_actor_model_droidkit_actors_Props_2_classLit = createForClass('im.actor.model.droidkit.actors', 'Props', 16, Ljava_lang_Object_2_classLit);
function $notifyQueueChanged(this$static){
  var lListener;
  lListener = this$static.listener;
  !!lListener && $notifyDispatcher(lListener.this$01);
}

function $setListener(this$static, listener){
  this$static.listener = listener;
}

defineClass(790, 1, {});
var Lim_actor_model_droidkit_actors_dispatch_AbstractDispatchQueue_2_classLit = createForClass('im.actor.model.droidkit.actors.dispatch', 'AbstractDispatchQueue', 790, Ljava_lang_Object_2_classLit);
function $dispatchMessage(this$static, message){
  !!this$static.dispatch && $dispatchMessage_0(this$static.dispatch, message);
}

defineClass(658, 1, {});
var Lim_actor_model_droidkit_actors_dispatch_AbstractDispatcher_2_classLit = createForClass('im.actor.model.droidkit.actors.dispatch', 'AbstractDispatcher', 658, Ljava_lang_Object_2_classLit);
function AbstractDispatcher$1(this$0){
  this.this$01 = this$0;
}

defineClass(659, 1, {}, AbstractDispatcher$1);
var Lim_actor_model_droidkit_actors_dispatch_AbstractDispatcher$1_2_classLit = createForClass('im.actor.model.droidkit.actors.dispatch', 'AbstractDispatcher/1', 659, Ljava_lang_Object_2_classLit);
function $clinit_DispatchResult(){
  $clinit_DispatchResult = emptyMethod;
  FREE_RESULTS = createThreadLocal();
}

function $update(this$static, isResult, res, delay){
  this$static.isResult = isResult;
  this$static.res = res;
  this$static.delay = delay;
}

function DispatchResult(isResult, res, delay){
  this.isResult = isResult;
  this.res = res;
  this.delay = delay;
}

function delay_0(delay){
  $clinit_DispatchResult();
  var result;
  result = dynamicCast(FREE_RESULTS.obj, 189);
  if (result) {
    FREE_RESULTS.obj = null;
    $update(result, false, null, delay);
  }
   else {
    result = new DispatchResult(false, null, delay);
  }
  return result;
}

function result_0(res){
  $clinit_DispatchResult();
  var result;
  result = dynamicCast(FREE_RESULTS.obj, 189);
  if (result) {
    FREE_RESULTS.obj = null;
    $update(result, true, res, {l:0, m:0, h:0});
  }
   else {
    result = new DispatchResult(true, res, {l:0, m:0, h:0});
  }
  return result;
}

defineClass(189, 1, {189:1}, DispatchResult);
_.delay = {l:0, m:0, h:0};
_.isResult = false;
var FREE_RESULTS;
var Lim_actor_model_droidkit_actors_dispatch_DispatchResult_2_classLit = createForClass('im.actor.model.droidkit.actors.dispatch', 'DispatchResult', 189, Ljava_lang_Object_2_classLit);
function $cancelSend(endpoint, message){
  endpoint.isDisconnected || $unschedule(endpoint.mailbox, new Envelope(message, endpoint.scope_0, endpoint.mailbox, null));
}

function $initDispatcher(this$static, dispatcher){
  if (this$static.dispatcher) {
    throw new RuntimeException_0('Double dispatcher init');
  }
  this$static.dispatcher = dispatcher;
}

function $processEnvelope(this$static, envelope){
  var actor, e, e$array, e$index, e$max, isDisconnected, scope_0, start_0;
  scope_0 = envelope.scope_0;
  start_0 = getActorTime();
  isDisconnected = false;
  if (!scope_0.actor_0) {
    if (maskUndefined(envelope.message_0) === maskUndefined(($clinit_PoisonPill() , INSTANCE_0))) {
      return;
    }
    try {
      actor = scope_0.props.creator.create_0();
      $initActor(actor, new ActorContext(scope_0));
      actor.preStart();
      scope_0.actor_0 = actor;
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 14)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
        !!envelope.sender && $send_1(envelope.sender, new DeadLetter('Unable to create actor'));
        return;
      }
       else 
        throw unwrap($e0);
    }
  }
  try {
    if (maskUndefined(envelope.message_0) === maskUndefined(($clinit_StartActor() , INSTANCE_1))) {
      return;
    }
     else if (maskUndefined(envelope.message_0) === maskUndefined(($clinit_PoisonPill() , INSTANCE_0))) {
      isDisconnected = true;
      scope_0.actor_0 = null;
      for (e$array = $allEnvelopes(scope_0.mailbox.envelopes) , e$index = 0 , e$max = e$array.length; e$index < e$max; ++e$index) {
        e = e$array[e$index];
        !!e.sender && $send_1(e.sender, new DeadLetter(e.message_0));
      }
      $clear(scope_0.mailbox.envelopes);
      $removeStringValue(this$static.scopes, scope_0.path);
      $removeStringValue(this$static.endpoints, scope_0.path);
      $disconnectMailbox(this$static.dispatcher.queue, scope_0.mailbox);
    }
     else {
      $setSender(scope_0, envelope.sender);
      if (instanceOf(envelope.message_0, 30)) {
        dynamicCast(envelope.message_0, 30).run();
        return;
      }
      scope_0.actor_0.onReceive(envelope.message_0);
    }
  }
   catch ($e1) {
    $e1 = wrap($e1);
    if (instanceOf($e1, 14)) {
      e = $e1;
      !!this$static.actorSystem.traceInterface && (!!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Die: ' + e)) , $printStackTrace(e, ($clinit_System() , err)));
      scope_0.actor_0 = null;
      isDisconnected = true;
      $removeStringValue(this$static.scopes, scope_0.path);
      $removeStringValue(this$static.endpoints, scope_0.path);
      $disconnectMailbox(this$static.dispatcher.queue, scope_0.mailbox);
    }
     else 
      throw unwrap($e1);
  }
   finally {
    !!this$static.actorSystem.traceInterface && $onEnvelopeProcessed(envelope, sub_0(getActorTime(), start_0));
    isDisconnected || $unlockMailbox(this$static.dispatcher.queue, envelope.mailbox);
  }
}

function $referenceActor(this$static, path, props){
  var endpoint, mailbox, scope_0;
  if ($hasStringValue(this$static.scopes, path)) {
    return dynamicCast($getStringValue(this$static.scopes, path), 357).actorRef;
  }
  mailbox = $createMailbox(props, this$static.dispatcher.queue);
  endpoint = dynamicCast($getStringValue(this$static.endpoints, path), 356);
  if (!endpoint) {
    endpoint = new ActorEndpoint;
    $putStringValue(this$static.endpoints, path, endpoint);
  }
  scope_0 = new ActorScope(this$static.actorSystem, mailbox, this$static, path, props, endpoint);
  endpoint.isDisconnected = false;
  endpoint.mailbox = mailbox;
  endpoint.scope_0 = scope_0;
  $putStringValue(this$static.scopes, scope_0.path, scope_0);
  $send_1(scope_0.actorRef, ($clinit_StartActor() , INSTANCE_1));
  return scope_0.actorRef;
}

function $sendMessage_0(this$static, endpoint, message, time, sender){
  if (endpoint.isDisconnected) {
    if (sender) {
      !!this$static.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Dead Letter: ' + message));
      $send_1(sender, new DeadLetter(message));
    }
  }
   else {
    $schedule_1(endpoint.mailbox, new Envelope(message, endpoint.scope_0, endpoint.mailbox, sender), time);
  }
}

function $sendMessageOnce(endpoint, message, time){
  if (endpoint.isDisconnected)
  ;
  else {
    $scheduleOnce(endpoint.mailbox, new Envelope(message, endpoint.scope_0, endpoint.mailbox, null), time);
  }
}

defineClass(244, 1, {244:1});
var Lim_actor_model_droidkit_actors_mailbox_ActorDispatcher_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox', 'ActorDispatcher', 244, Ljava_lang_Object_2_classLit);
function ActorEndpoint(){
  this.isDisconnected = false;
}

defineClass(356, 1, {356:1}, ActorEndpoint);
_.isDisconnected = false;
var Lim_actor_model_droidkit_actors_mailbox_ActorEndpoint_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox', 'ActorEndpoint', 356, Ljava_lang_Object_2_classLit);
function Envelope(message, scope_0, mailbox, sender){
  this.scope_0 = scope_0;
  this.message_0 = message;
  this.sender = sender;
  this.mailbox = mailbox;
  getActorTime();
}

defineClass(169, 1, {169:1}, Envelope);
_.toString$ = function toString_152(){
  return '{' + this.message_0 + ' -> ' + this.scope_0.path + '}';
}
;
var Lim_actor_model_droidkit_actors_mailbox_Envelope_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox', 'Envelope', 169, Ljava_lang_Object_2_classLit);
function $schedule_1(this$static, envelope, time){
  if (envelope.mailbox != this$static) {
    throw new RuntimeException_0('envelope.mailbox != this mailbox');
  }
  $putEnvelope(this$static.envelopes, envelope, time);
}

function $scheduleOnce(this$static, envelope, time){
  if (envelope.mailbox != this$static) {
    throw new RuntimeException_0('envelope.mailbox != this mailbox');
  }
  $putEnvelopeOnce(this$static.envelopes, envelope, time, this$static.comparator);
}

function $unschedule(this$static, envelope){
  $removeEnvelope(this$static.envelopes, envelope, this$static.comparator);
}

function Mailbox(queue){
  this.comparator = new Mailbox$1(this);
  this.envelopes = new EnvelopeCollection(queue.envelopeRoot);
}

defineClass(348, 1, {}, Mailbox);
_.isEqualEnvelope = function isEqualEnvelope(a, b){
  return getClass__Ljava_lang_Class___devirtual$(a.message_0) == getClass__Ljava_lang_Class___devirtual$(b.message_0);
}
;
var Lim_actor_model_droidkit_actors_mailbox_Mailbox_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox', 'Mailbox', 348, Ljava_lang_Object_2_classLit);
function $equals(this$static, a, b){
  return this$static.this$01.isEqualEnvelope(a, b);
}

function Mailbox$1(this$0){
  this.this$01 = this$0;
}

defineClass(625, 1, {}, Mailbox$1);
var Lim_actor_model_droidkit_actors_mailbox_Mailbox$1_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox', 'Mailbox/1', 625, Ljava_lang_Object_2_classLit);
function $disconnectMailbox(this$static, mailbox){
  $detachCollection(this$static.envelopeRoot, mailbox.envelopes);
}

function $dispatch(this$static, time){
  var res, result;
  res = $fetchCollection(this$static.envelopeRoot, time);
  if (!res) {
    return delay_0({l:300000, m:0, h:0});
  }
  if (res.envelope) {
    result = result_0(res.envelope);
    $set(($clinit_EnvelopeRoot$FetchResult() , RESULT_CACHE_0), res);
    return result;
  }
   else {
    result = delay_0(res.delay);
    $set(($clinit_EnvelopeRoot$FetchResult() , RESULT_CACHE_0), res);
    return result;
  }
}

function $unlockMailbox(this$static, mailbox){
  $attachCollection(this$static.envelopeRoot, mailbox.envelopes);
}

function MailboxesQueue(){
  this.envelopeRoot = new EnvelopeRoot(this);
}

defineClass(660, 790, {}, MailboxesQueue);
var Lim_actor_model_droidkit_actors_mailbox_MailboxesQueue_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox', 'MailboxesQueue', 660, Lim_actor_model_droidkit_actors_dispatch_AbstractDispatchQueue_2_classLit);
function $clinit_EnvelopeCollection(){
  $clinit_EnvelopeCollection = emptyMethod;
  NEXT_ID = createAtomicInt();
}

function $allEnvelopes(this$static){
  var i_0, res, scheduledEnvelopes;
  scheduledEnvelopes = dynamicCast($toArray(new AbstractMap$2(this$static.envelopes), initDim(Lim_actor_model_droidkit_actors_mailbox_collections_ScheduledEnvelope_2_classLit, {806:1, 3:1, 6:1}, 95, this$static.envelopes.size_0, 0, 1)), 806);
  res = initDim(Lim_actor_model_droidkit_actors_mailbox_Envelope_2_classLit, $intern_6, 169, scheduledEnvelopes.length, 0, 1);
  for (i_0 = 0; i_0 < res.length; i_0++) {
    res[i_0] = scheduledEnvelopes[i_0].envelope;
  }
  return res;
}

function $clear(this$static){
  $clear_3(this$static.envelopes);
  this$static.topKey = {l:0, m:0, h:0};
  $changedTopKey(this$static.root, this$static);
}

function $fetchEnvelope(this$static, time){
  var envelope, oldKey, result;
  oldKey = this$static.topKey;
  if (this$static.envelopes.size_0 == 0) {
    return null;
  }
  envelope = dynamicCast(copyOf($getFirstEntry(this$static.envelopes)).value_0, 95);
  if (lte(envelope.time, time)) {
    $remove_8(this$static.envelopes, valueOf_0(envelope.key));
    this$static.envelopes.size_0 == 0?(this$static.topKey = {l:0, m:0, h:0}):(this$static.topKey = dynamicCast(getKeyOrNSE($getFirstEntry(this$static.envelopes)), 23).value_0);
    result = envelope_0(envelope);
  }
   else {
    result = delay_1(sub_0(envelope.time, time));
  }
  neq(oldKey, this$static.topKey) && $changedTopKey(this$static.root, this$static);
  return result;
}

function $putEnvelope(this$static, envelope, time){
  var key, oldKey;
  key = $buildKey(this$static.root, time);
  oldKey = this$static.topKey;
  $put_4(this$static.envelopes, valueOf_0(key), new ScheduledEnvelope(key, time, envelope));
  (lt(key, this$static.topKey) || eq(this$static.topKey, {l:0, m:0, h:0})) && (this$static.topKey = key);
  neq(oldKey, this$static.topKey) && $changedTopKey(this$static.root, this$static);
  return key;
}

function $putEnvelopeOnce(this$static, envelope, time, comparator){
  var iterator, key, oldKey;
  key = $buildKey(this$static.root, time);
  oldKey = this$static.topKey;
  iterator = new TreeMap$EntryIterator((new TreeMap$EntrySet(this$static.envelopes)).this$01);
  while ($hasNext_0(iterator.iter)) {
    $equals(comparator, dynamicCast((iterator.last = dynamicCast($next_0(iterator.iter), 20)).getValue(), 95).envelope, envelope) && $remove_9(iterator);
  }
  $put_4(this$static.envelopes, valueOf_0(key), new ScheduledEnvelope(key, time, envelope));
  this$static.topKey = dynamicCast(getKeyOrNSE($getFirstEntry(this$static.envelopes)), 23).value_0;
  neq(oldKey, this$static.topKey) && $changedTopKey(this$static.root, this$static);
  return key;
}

function $removeEnvelope(this$static, envelope, comparator){
  var iterator, oldKey;
  oldKey = this$static.topKey;
  iterator = new TreeMap$EntryIterator((new TreeMap$EntrySet(this$static.envelopes)).this$01);
  while ($hasNext_0(iterator.iter)) {
    $equals(comparator, dynamicCast((iterator.last = dynamicCast($next_0(iterator.iter), 20)).getValue(), 95).envelope, envelope) && $remove_9(iterator);
  }
  this$static.envelopes.size_0 == 0?(this$static.topKey = {l:0, m:0, h:0}):(this$static.topKey = dynamicCast(getKeyOrNSE($getFirstEntry(this$static.envelopes)), 23).value_0);
  neq(oldKey, this$static.topKey) && $changedTopKey(this$static.root, this$static);
}

function EnvelopeCollection(root){
  $clinit_EnvelopeCollection();
  this.envelopes = new TreeMap;
  this.id_0 = NEXT_ID.value_0++;
  this.root = root;
  this.topKey = {l:0, m:0, h:0};
  $attachCollection(this.root, this);
}

defineClass(349, 1, {349:1}, EnvelopeCollection);
_.id_0 = 0;
_.topKey = {l:0, m:0, h:0};
var NEXT_ID;
var Lim_actor_model_droidkit_actors_mailbox_collections_EnvelopeCollection_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox.collections', 'EnvelopeCollection', 349, Ljava_lang_Object_2_classLit);
function $clinit_EnvelopeCollection$FetchResult(){
  $clinit_EnvelopeCollection$FetchResult = emptyMethod;
  RESULT_CACHE = createThreadLocal();
}

function EnvelopeCollection$FetchResult(delay){
  this.delay = delay;
}

function EnvelopeCollection$FetchResult_0(envelope){
  this.envelope = envelope;
}

function delay_1(delay){
  $clinit_EnvelopeCollection$FetchResult();
  var res;
  res = dynamicCast(RESULT_CACHE.obj, 170);
  if (res) {
    RESULT_CACHE.obj = null;
    res.envelope = null;
    res.delay = delay;
  }
   else {
    res = new EnvelopeCollection$FetchResult(delay);
  }
  return res;
}

function envelope_0(envelope){
  $clinit_EnvelopeCollection$FetchResult();
  var res;
  res = dynamicCast(RESULT_CACHE.obj, 170);
  if (res) {
    RESULT_CACHE.obj = null;
    res.envelope = envelope;
    res.delay = {l:0, m:0, h:0};
  }
   else {
    res = new EnvelopeCollection$FetchResult_0(envelope);
  }
  return res;
}

defineClass(170, 1, {170:1}, EnvelopeCollection$FetchResult, EnvelopeCollection$FetchResult_0);
_.delay = {l:0, m:0, h:0};
var RESULT_CACHE;
var Lim_actor_model_droidkit_actors_mailbox_collections_EnvelopeCollection$FetchResult_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox.collections', 'EnvelopeCollection/FetchResult', 170, Ljava_lang_Object_2_classLit);
function $attachCollection(this$static, collection){
  var key;
  key = collection.topKey;
  if (!$containsKey(this$static.collections, valueOf(collection.id_0))) {
    $put_1(this$static.collections, valueOf(collection.id_0), collection);
    $put_1(this$static.lastTopKey, valueOf(collection.id_0), valueOf_0(key));
    gt(key, {l:0, m:0, h:0}) && $put_4(this$static.sortedCollection, valueOf_0(key), collection);
  }
  $notifyQueueChanged(this$static.queue);
}

function $buildKey(this$static, time){
  var currentTime, iterator, shift_0, t;
  currentTime = getActorTime();
  lt(time, currentTime) && (time = currentTime);
  iterator = $iterator(new AbstractMap$1(this$static.usedSlot.map_0));
  while ($hasNext(iterator.val$outerIter2)) {
    t = dynamicCast($next_1(iterator), 23).value_0;
    lt(t, mul(currentTime, {l:10000, m:0, h:0})) && $remove_1(iterator.val$outerIter2);
  }
  shift_0 = {l:0, m:0, h:0};
  while ($contains_0(this$static.usedSlot, valueOf_0(add_0(mul(time, {l:10000, m:0, h:0}), shift_0)))) {
    shift_0 = add_0(shift_0, {l:1, m:0, h:0});
  }
  $add_1(this$static.usedSlot, valueOf_0(add_0(mul(time, {l:10000, m:0, h:0}), shift_0)));
  return add_0(mul(time, {l:10000, m:0, h:0}), shift_0);
}

function $changedTopKey(this$static, collection){
  var key, prevKey;
  if (!$containsKey(this$static.collections, valueOf(collection.id_0))) {
    return;
  }
  key = collection.topKey;
  prevKey = dynamicCast($get_2(this$static.lastTopKey, valueOf(collection.id_0)), 23);
  $remove_0(this$static.lastTopKey, valueOf(collection.id_0));
  gt(prevKey.value_0, {l:0, m:0, h:0}) && $remove_8(this$static.sortedCollection, prevKey);
  $put_1(this$static.lastTopKey, valueOf(collection.id_0), valueOf_0(key));
  gt(key, {l:0, m:0, h:0}) && $put_4(this$static.sortedCollection, valueOf_0(key), collection);
  $notifyQueueChanged(this$static.queue);
}

function $detachCollection(this$static, collection){
  var prevKey;
  if (!$containsKey(this$static.collections, valueOf(collection.id_0))) {
    return;
  }
  $remove_0(this$static.collections, valueOf(collection.id_0));
  prevKey = dynamicCast($get_2(this$static.lastTopKey, valueOf(collection.id_0)), 23);
  $remove_0(this$static.lastTopKey, valueOf(collection.id_0));
  gt(prevKey.value_0, {l:0, m:0, h:0}) && $remove_8(this$static.sortedCollection, prevKey);
}

function $fetchCollection(this$static, time){
  var collection, envelope, res, result;
  res = this$static.sortedCollection.size_0 == 0?null:copyOf($getFirstEntry(this$static.sortedCollection));
  if (res) {
    collection = dynamicCast(res.value_0, 349);
    envelope = $fetchEnvelope(collection, time);
    if (envelope) {
      if (envelope.envelope) {
        $detachCollection(this$static, collection);
        result = envelope_1(envelope.envelope.envelope);
        $set(($clinit_EnvelopeCollection$FetchResult() , RESULT_CACHE), envelope);
        return result;
      }
       else {
        result = delay_2(envelope.delay);
        $set(($clinit_EnvelopeCollection$FetchResult() , RESULT_CACHE), envelope);
        return result;
      }
    }
     else {
      return null;
    }
  }
   else {
    return null;
  }
}

function EnvelopeRoot(queue){
  this.usedSlot = new HashSet;
  this.collections = new HashMap;
  this.lastTopKey = new HashMap;
  this.sortedCollection = new TreeMap;
  this.queue = queue;
}

defineClass(704, 1, {}, EnvelopeRoot);
var Lim_actor_model_droidkit_actors_mailbox_collections_EnvelopeRoot_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox.collections', 'EnvelopeRoot', 704, Ljava_lang_Object_2_classLit);
function $clinit_EnvelopeRoot$FetchResult(){
  $clinit_EnvelopeRoot$FetchResult = emptyMethod;
  RESULT_CACHE_0 = createThreadLocal();
}

function EnvelopeRoot$FetchResult(delay){
  this.delay = delay;
}

function EnvelopeRoot$FetchResult_0(envelope){
  this.envelope = envelope;
}

function delay_2(delay){
  $clinit_EnvelopeRoot$FetchResult();
  var res;
  res = dynamicCast(RESULT_CACHE_0.obj, 187);
  if (res) {
    RESULT_CACHE_0.obj = null;
    res.envelope = null;
    res.delay = delay;
  }
   else {
    res = new EnvelopeRoot$FetchResult(delay);
  }
  return res;
}

function envelope_1(envelope){
  $clinit_EnvelopeRoot$FetchResult();
  var res;
  res = dynamicCast(RESULT_CACHE_0.obj, 187);
  if (res) {
    RESULT_CACHE_0.obj = null;
    res.envelope = envelope;
    res.delay = {l:0, m:0, h:0};
  }
   else {
    res = new EnvelopeRoot$FetchResult_0(envelope);
  }
  return res;
}

defineClass(187, 1, {187:1}, EnvelopeRoot$FetchResult, EnvelopeRoot$FetchResult_0);
_.delay = {l:0, m:0, h:0};
var RESULT_CACHE_0;
var Lim_actor_model_droidkit_actors_mailbox_collections_EnvelopeRoot$FetchResult_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox.collections', 'EnvelopeRoot/FetchResult', 187, Ljava_lang_Object_2_classLit);
function ScheduledEnvelope(key, time, envelope){
  this.key = key;
  this.time = time;
  this.envelope = envelope;
}

defineClass(95, 1, {95:1}, ScheduledEnvelope);
_.toString$ = function toString_153(){
  return '<' + this.envelope.message_0 + '>';
}
;
_.key = {l:0, m:0, h:0};
_.time = {l:0, m:0, h:0};
var Lim_actor_model_droidkit_actors_mailbox_collections_ScheduledEnvelope_2_classLit = createForClass('im.actor.model.droidkit.actors.mailbox.collections', 'ScheduledEnvelope', 95, Ljava_lang_Object_2_classLit);
function DeadLetter(message){
  this.message_0 = message;
}

defineClass(22, 1, {}, DeadLetter);
_.toString$ = function toString_154(){
  return 'DeadLetter(' + this.message_0 + ')';
}
;
var Lim_actor_model_droidkit_actors_messages_DeadLetter_2_classLit = createForClass('im.actor.model.droidkit.actors.messages', 'DeadLetter', 22, Ljava_lang_Object_2_classLit);
function $clinit_PoisonPill(){
  $clinit_PoisonPill = emptyMethod;
  INSTANCE_0 = new PoisonPill;
}

function PoisonPill(){
}

defineClass(721, 1, {}, PoisonPill);
_.toString$ = function toString_155(){
  return 'PoisonPill';
}
;
var INSTANCE_0;
var Lim_actor_model_droidkit_actors_messages_PoisonPill_2_classLit = createForClass('im.actor.model.droidkit.actors.messages', 'PoisonPill', 721, Ljava_lang_Object_2_classLit);
function $clinit_StartActor(){
  $clinit_StartActor = emptyMethod;
  INSTANCE_1 = new StartActor;
}

function StartActor(){
}

defineClass(661, 1, {}, StartActor);
_.toString$ = function toString_156(){
  return 'StartActor';
}
;
var INSTANCE_1;
var Lim_actor_model_droidkit_actors_messages_StartActor_2_classLit = createForClass('im.actor.model.droidkit.actors.messages', 'StartActor', 661, Ljava_lang_Object_2_classLit);
function parse_158(res, inputStream){
  var reader;
  reader = new BserValues(deserialize(inputStream));
  res.parse_0(reader);
  return res;
}

function parse_159(res, data_0){
  return parse_158(res, new DataInput_0(data_0, 0, data_0.length));
}

function deserialize(is){
  var currentTag, hashMap, id_0, size_0, type_0;
  hashMap = new SparseArray;
  while (is.maxOffset > is.offset) {
    currentTag = $readByte(is);
    id_0 = currentTag >> 3;
    type_0 = currentTag & 7;
    if (type_0 == 0) {
      put(id_0, valueOf_0($readVarInt(is)), hashMap);
    }
     else if (type_0 == 2) {
      size_0 = toInt($readVarInt(is));
      put(id_0, $readBytes(is, size_0), hashMap);
    }
     else if (type_0 == 1) {
      put(id_0, valueOf_0($readLong(is)), hashMap);
    }
     else if (type_0 == 5) {
      put(id_0, valueOf_0($readUInt(is)), hashMap);
    }
     else {
      throw new IOException_0('Unknown Wire Type #' + type_0);
    }
  }
  return hashMap;
}

function put(id_0, res, hashMap){
  var list;
  if ($get(hashMap, id_0) != null) {
    if (instanceOf($get(hashMap, id_0), 43)) {
      dynamicCast($get(hashMap, id_0), 43).add_1(res);
    }
     else {
      list = new ArrayList;
      $add_0(list, $get(hashMap, id_0));
      setCheck(list.array, list.array.length, res);
      $put(hashMap, id_0, list);
    }
  }
   else {
    $put(hashMap, id_0, res);
  }
}

function $getBytes(this$static, id_0){
  if ($indexOfKey(this$static.fields, id_0) < 0) {
    throw new UnknownFieldException('Unable to find field #' + id_0);
  }
  return $getBytes_0(this$static, id_0);
}

function $getBytes_0(this$static, id_0){
  var obj;
  if ($indexOfKey(this$static.fields, id_0) >= 0) {
    obj = $get(this$static.fields, id_0);
    if (instanceOf(obj, 36)) {
      return dynamicCast(obj, 36);
    }
    throw new IncorrectTypeException('Expected type: byte[], got ' + $getSimpleName(getClass__Ljava_lang_Class___devirtual$(obj)));
  }
  return null;
}

function $getLong(this$static, id_0){
  if ($indexOfKey(this$static.fields, id_0) < 0) {
    throw new UnknownFieldException('Unable to find field #' + id_0);
  }
  return $getLong_0(this$static, id_0);
}

function $getLong_0(this$static, id_0){
  var obj;
  if ($indexOfKey(this$static.fields, id_0) >= 0) {
    obj = $get(this$static.fields, id_0);
    if (instanceOf(obj, 23)) {
      return dynamicCast(obj, 23).value_0;
    }
    throw new IncorrectTypeException('Expected type: long, got ' + $getSimpleName(getClass__Ljava_lang_Class___devirtual$(obj)));
  }
  return {l:0, m:0, h:0};
}

function $getObj(this$static, id_0, obj){
  var data_0;
  data_0 = $getBytes_0(this$static, id_0);
  if (data_0 == null) {
    throw new UnknownFieldException('Unable to find field #' + id_0);
  }
  return parse_158(obj, new DataInput_0(data_0, 0, data_0.length));
}

function $getRepeatedBytes(this$static, id_0){
  var rep, res, val, val2, val2$iterator;
  res = new ArrayList;
  if ($indexOfKey(this$static.fields, id_0) >= 0) {
    val = $get(this$static.fields, id_0);
    if (instanceOf(val, 36)) {
      $add_0(res, dynamicCast(val, 36));
    }
     else if (instanceOf(val, 43)) {
      rep = dynamicCast(val, 43);
      for (val2$iterator = rep.iterator(); val2$iterator.hasNext();) {
        val2 = val2$iterator.next();
        if (instanceOf(val2, 36)) {
          $add_0(res, dynamicCast(val2, 36));
        }
         else {
          throw new IOException_0('Expected type: byte[], got ' + $getSimpleName(getClass__Ljava_lang_Class___devirtual$(val2)));
        }
      }
    }
     else {
      throw new IOException_0('Expected type: byte[], got ' + $getSimpleName(getClass__Ljava_lang_Class___devirtual$(val)));
    }
  }
  return res;
}

function $getRepeatedCount(this$static, id_0){
  var val;
  if ($indexOfKey(this$static.fields, id_0) >= 0) {
    val = $get(this$static.fields, id_0);
    return instanceOf(val, 43)?dynamicCast(val, 43).size_1():1;
  }
  return 0;
}

function $getRepeatedInt(this$static, id_0){
  var l, l$iterator, res, src_0;
  src_0 = $getRepeatedLong(this$static, id_0);
  res = new ArrayList;
  for (l$iterator = new AbstractList$IteratorImpl(src_0); l$iterator.i < l$iterator.this$01.size_1();) {
    l = (checkCriticalElement(l$iterator.i < l$iterator.this$01.size_1()) , dynamicCast(l$iterator.this$01.get_1(l$iterator.last = l$iterator.i++), 23));
    $add_0(res, valueOf(convertInt(l.value_0)));
  }
  return res;
}

function $getRepeatedLong(this$static, id_0){
  var rep, res, val, val2, val2$iterator;
  res = new ArrayList;
  if ($indexOfKey(this$static.fields, id_0) >= 0) {
    val = $get(this$static.fields, id_0);
    if (instanceOf(val, 23)) {
      $add_0(res, dynamicCast(val, 23));
    }
     else if (instanceOf(val, 43)) {
      rep = dynamicCast(val, 43);
      for (val2$iterator = rep.iterator(); val2$iterator.hasNext();) {
        val2 = val2$iterator.next();
        if (instanceOf(val2, 23)) {
          $add_0(res, dynamicCast(val2, 23));
        }
         else {
          throw new IOException_0('Expected type: long, got ' + $getSimpleName(getClass__Ljava_lang_Class___devirtual$(val2)));
        }
      }
    }
     else {
      throw new IOException_0('Expected type: long, got ' + $getSimpleName(getClass__Ljava_lang_Class___devirtual$(val)));
    }
  }
  return res;
}

function $getRepeatedObj(this$static, id_0, objs){
  var res, v, v$iterator;
  res = new ArrayList;
  for (v$iterator = new AbstractList$IteratorImpl($getRepeatedBytes(this$static, id_0)); v$iterator.i < v$iterator.this$01.size_1();) {
    v = (checkCriticalElement(v$iterator.i < v$iterator.this$01.size_1()) , dynamicCast(v$iterator.this$01.get_1(v$iterator.last = v$iterator.i++), 36));
    $add_0(res, parse_158(dynamicCast(objs.remove_1(0), 4), new DataInput_0(v, 0, v.length)));
  }
  return res;
}

function $optObj(this$static, id_0, obj){
  var data_0;
  data_0 = $getBytes_0(this$static, id_0);
  if (data_0 == null) {
    return null;
  }
  return parse_158(obj, new DataInput_0(data_0, 0, data_0.length));
}

function BserValues(fields){
  this.fields = fields;
}

defineClass(98, 1, {}, BserValues);
var Lim_actor_model_droidkit_bser_BserValues_2_classLit = createForClass('im.actor.model.droidkit.bser', 'BserValues', 98, Ljava_lang_Object_2_classLit);
function $writeBool(this$static, fieldNumber, value_0){
  $writeTag(this$static, fieldNumber, 0);
  $writeVarInt(this$static, fromInt(value_0?1:0));
}

function $writeBytes(this$static, fieldNumber, value_0){
  if (value_0 == null) {
    throw new IllegalArgumentException_0('Value can not be null');
  }
  if (value_0.length > $intern_18) {
    throw new IllegalArgumentException_0('Unable to write more than 1 MB');
  }
  $writeTag(this$static, fieldNumber, 2);
  $writeProtoBytes(this$static.stream, value_0, value_0.length);
}

function $writeBytes_0(this$static, data_0){
  $writeProtoBytes(this$static.stream, data_0, data_0.length);
}

function $writeBytesField(this$static, fieldNumber, value_0){
  $writeTag(this$static, fieldNumber, 2);
  $writeProtoBytes(this$static.stream, value_0, value_0.length);
}

function $writeInt(this$static, fieldNumber, value_0){
  $writeTag(this$static, fieldNumber, 0);
  $writeVarInt(this$static, fromInt(value_0));
}

function $writeLong(this$static, fieldNumber, value_0){
  $writeTag(this$static, fieldNumber, 0);
  $writeVarInt_0(this$static.stream, and(value_0, {l:$intern_7, m:$intern_7, h:$intern_8}));
}

function $writeObject(this$static, fieldNumber, value_0){
  var outputStream, writer;
  if (!value_0) {
    throw new IllegalArgumentException_0('Value can not be null');
  }
  $writeTag(this$static, fieldNumber, 2);
  outputStream = new DataOutput;
  writer = new BserWriter(outputStream);
  value_0.serialize(writer);
  $writeBytes_0(this$static, $toByteArray_0(outputStream));
}

function $writeRepeatedInt(this$static, fieldNumber, values){
  var l, l$iterator;
  if (!values) {
    throw new IllegalArgumentException_0('Values can not be null');
  }
  if (values.array.length > 1024) {
    throw new IllegalArgumentException_0('Too many values');
  }
  for (l$iterator = new AbstractList$IteratorImpl(values); l$iterator.i < l$iterator.this$01.size_1();) {
    l = (checkCriticalElement(l$iterator.i < l$iterator.this$01.size_1()) , dynamicCast(l$iterator.this$01.get_1(l$iterator.last = l$iterator.i++), 33));
    if (!l) {
      throw new IllegalArgumentException_0('Value can not be null');
    }
    $writeVar32Field(this$static, fieldNumber, fromInt(l.value_0));
  }
}

function $writeRepeatedLong(this$static, fieldNumber, values){
  var l, l$iterator;
  if (!values) {
    throw new IllegalArgumentException_0('Values can not be null');
  }
  if (values.array.length > 1024) {
    throw new IllegalArgumentException_0('Too many values');
  }
  for (l$iterator = new AbstractList$IteratorImpl(values); l$iterator.i < l$iterator.this$01.size_1();) {
    l = (checkCriticalElement(l$iterator.i < l$iterator.this$01.size_1()) , dynamicCast(l$iterator.this$01.get_1(l$iterator.last = l$iterator.i++), 23));
    if (!l) {
      throw new IllegalArgumentException_0('Value can not be null');
    }
    $writeVar64Field(this$static, fieldNumber, l.value_0);
  }
}

function $writeRepeatedObj(this$static, fieldNumber, values){
  var l, l$iterator;
  if (!values) {
    throw new IllegalArgumentException_0('Values can not be null');
  }
  if (values.size_1() > 1024) {
    throw new IllegalArgumentException_0('Too many values');
  }
  for (l$iterator = values.iterator(); l$iterator.hasNext();) {
    l = dynamicCast(l$iterator.next(), 4);
    if (!l) {
      throw new IllegalArgumentException_0('Value can not be null');
    }
    $writeObject(this$static, fieldNumber, l);
  }
}

function $writeString(this$static, fieldNumber, value_0){
  if (value_0 == null) {
    throw new IllegalArgumentException_0('Value can not be null');
  }
  $writeBytesField(this$static, fieldNumber, getBytesUtf8(value_0));
}

function $writeTag(this$static, fieldNumber, wireType){
  var tag;
  fieldNumber = fieldNumber & $intern_5;
  if (fieldNumber <= 0) {
    throw new IllegalArgumentException_0("fieldNumber can't be less or eq to zero");
  }
  tag = or(fromInt(fieldNumber << 3), fromInt(wireType));
  $writeVarInt_0(this$static.stream, tag);
}

function $writeVar32Field(this$static, fieldNumber, value_0){
  $writeTag(this$static, fieldNumber, 5);
  $writeInt_0(this$static.stream, toInt(and(value_0, {l:$intern_5, m:0, h:0})));
}

function $writeVar64Field(this$static, fieldNumber, value_0){
  $writeTag(this$static, fieldNumber, 1);
  $writeLong_0(this$static.stream, and(value_0, {l:$intern_7, m:$intern_7, h:$intern_8}));
}

function $writeVarInt(this$static, value_0){
  $writeVarInt_0(this$static.stream, and(value_0, {l:$intern_7, m:$intern_7, h:$intern_8}));
}

function $writeVarIntField(this$static, fieldNumber, value_0){
  $writeTag(this$static, fieldNumber, 0);
  $writeVarInt_0(this$static.stream, and(value_0, {l:$intern_7, m:$intern_7, h:$intern_8}));
}

function BserWriter(stream){
  this.stream = stream;
}

defineClass(132, 1, {}, BserWriter);
var Lim_actor_model_droidkit_bser_BserWriter_2_classLit = createForClass('im.actor.model.droidkit.bser', 'BserWriter', 132, Ljava_lang_Object_2_classLit);
function $readByte(this$static){
  if (this$static.offset == this$static.maxOffset) {
    throw new IOException;
  }
  return this$static.data_0[this$static.offset++] & 255;
}

function $readBytes(this$static, count){
  var i_0, res;
  if (count < 0) {
    throw new IOException_0("Count can't be negative");
  }
  if (count > $intern_18) {
    throw new IOException_0('Unable to read more than 1 MB');
  }
  res = initDim(B_classLit, $intern_17, 0, count, 7, 1);
  for (i_0 = 0; i_0 < count; i_0++) {
    res[i_0] = this$static.data_0[this$static.offset++];
  }
  return res;
}

function $readInt(this$static){
  var res;
  if (this$static.offset + 4 > this$static.maxOffset) {
    throw new IOException;
  }
  res = (this$static.data_0[this$static.offset + 3] & 255) + ((this$static.data_0[this$static.offset + 2] & 255) << 8) + ((this$static.data_0[this$static.offset + 1] & 255) << 16) + ((this$static.data_0[this$static.offset] & 255) << 24);
  this$static.offset += 4;
  return res;
}

function $readLong(this$static){
  var a1, a2, a3, a4, b1, b2, b3, b4, res1, res2;
  if (this$static.offset + 8 > this$static.maxOffset) {
    throw new IOException;
  }
  a1 = fromInt(this$static.data_0[this$static.offset + 3] & 255);
  a2 = fromInt(this$static.data_0[this$static.offset + 2] & 255);
  a3 = fromInt(this$static.data_0[this$static.offset + 1] & 255);
  a4 = fromInt(this$static.data_0[this$static.offset] & 255);
  res1 = add_0(add_0(add_0(a1, shl(a2, 8)), shl(a3, 16)), shl(a4, 24));
  this$static.offset += 4;
  b1 = fromInt(this$static.data_0[this$static.offset + 3] & 255);
  b2 = fromInt(this$static.data_0[this$static.offset + 2] & 255);
  b3 = fromInt(this$static.data_0[this$static.offset + 1] & 255);
  b4 = fromInt(this$static.data_0[this$static.offset] & 255);
  res2 = add_0(add_0(add_0(b1, shl(b2, 8)), shl(b3, 16)), shl(b4, 24));
  this$static.offset += 4;
  return add_0(res2, shl(res1, 32));
}

function $readProtoBytes(this$static){
  var len;
  len = $readVarInt(this$static);
  if (lt(len, {l:0, m:0, h:0})) {
    throw new IOException;
  }
  if (gt(len, {l:$intern_18, m:0, h:0})) {
    throw new IOException;
  }
  return $readBytes(this$static, toInt(len));
}

function $readProtoLongs(this$static){
  var i_0, len, res;
  len = $readVarInt(this$static);
  if (lt(len, {l:0, m:0, h:0})) {
    throw new IOException;
  }
  if (gt(len, {l:1024, m:0, h:0})) {
    throw new IOException;
  }
  res = initDim(J_classLit, $intern_4, 0, toInt(len), 6, 1);
  for (i_0 = 0; i_0 < res.length; i_0++) {
    res[i_0] = $readLong(this$static);
  }
  return res;
}

function $readProtoString(this$static){
  var data_0;
  data_0 = $readProtoBytes(this$static);
  return _String(data_0, data_0.length, 'UTF-8');
}

function $readUInt(this$static){
  var a1, a2, a3, a4;
  if (this$static.offset + 4 > this$static.maxOffset) {
    throw new IOException;
  }
  a1 = fromInt(this$static.data_0[this$static.offset + 3] & 255);
  a2 = fromInt(this$static.data_0[this$static.offset + 2] & 255);
  a3 = fromInt(this$static.data_0[this$static.offset + 1] & 255);
  a4 = fromInt(this$static.data_0[this$static.offset] & 255);
  this$static.offset += 4;
  return add_0(add_0(add_0(a1, shl(a2, 8)), shl(a3, 16)), shl(a4, 24));
}

function $readVarInt(this$static){
  var b, i_0, value_0;
  value_0 = {l:0, m:0, h:0};
  i_0 = {l:0, m:0, h:0};
  do {
    if (this$static.offset == this$static.maxOffset) {
      throw new IOException;
    }
    b = fromInt(this$static.data_0[this$static.offset++] & 255);
    if (neq(and(b, {l:128, m:0, h:0}), {l:0, m:0, h:0})) {
      value_0 = or(value_0, shl(and(b, {l:127, m:0, h:0}), toInt(i_0)));
      i_0 = add_0(i_0, {l:7, m:0, h:0});
      if (gt(i_0, {l:70, m:0, h:0})) {
        throw new IOException;
      }
    }
     else {
      break;
    }
  }
   while (true);
  return or(value_0, shl(b, toInt(i_0)));
}

function DataInput(data_0){
  this.data_0 = data_0;
  this.offset = 0;
  this.maxOffset = data_0.length;
}

function DataInput_0(data_0, offset, len){
  if (data_0 == null) {
    throw new IllegalArgumentException_0("data can't be null");
  }
  if (offset < 0) {
    throw new IllegalArgumentException_0("Offset can't be negative");
  }
  if (len < 0) {
    throw new IllegalArgumentException_0("Length can't be negative");
  }
  if (data_0.length < offset + len) {
    throw new IllegalArgumentException_0('Inconsistent lengths, total: ' + data_0.length + ', offset: ' + offset + ', len: ' + len);
  }
  this.data_0 = data_0;
  this.offset = offset;
  this.maxOffset = offset + len;
}

defineClass(28, 1, {}, DataInput, DataInput_0);
_.maxOffset = 0;
_.offset = 0;
var Lim_actor_model_droidkit_bser_DataInput_2_classLit = createForClass('im.actor.model.droidkit.bser', 'DataInput', 28, Ljava_lang_Object_2_classLit);
function $expand(this$static, size_0){
  var i_0, nData;
  nData = initDim(B_classLit, $intern_17, 0, size_0, 7, 1);
  for (i_0 = 0; i_0 < this$static.offset; i_0++) {
    nData[i_0] = this$static.data_0[i_0];
  }
  this$static.data_0 = nData;
}

function $toByteArray_0(this$static){
  var i_0, res;
  res = initDim(B_classLit, $intern_17, 0, this$static.offset, 7, 1);
  for (i_0 = 0; i_0 < this$static.offset; i_0++) {
    res[i_0] = this$static.data_0[i_0];
  }
  return res;
}

function $writeByte(this$static, v){
  if (v < 0) {
    throw new IllegalArgumentException_0("Value can't be negative");
  }
  if (v > 255) {
    throw new IllegalArgumentException_0("Value can't be more than 255");
  }
  this$static.data_0.length <= this$static.offset + 1 && $expand(this$static, this$static.offset + 1);
  this$static.data_0[this$static.offset++] = narrow_byte(v);
}

function $writeBytes_1(this$static, v, ofs, len){
  var i_0;
  if (len > $intern_18) {
    throw new IllegalArgumentException_0('Unable to write more than 1 MB');
  }
  if (len < 0) {
    throw new IllegalArgumentException_0("Length can't be negative");
  }
  if (ofs < 0) {
    throw new IllegalArgumentException_0("Offset can't be negative");
  }
  if (ofs + len > v.length) {
    throw new IllegalArgumentException_0('Inconsistent sizes');
  }
  this$static.data_0.length < this$static.offset + v.length && $expand(this$static, this$static.offset + v.length);
  for (i_0 = 0; i_0 < len; i_0++) {
    this$static.data_0[this$static.offset++] = v[i_0 + ofs];
  }
}

function $writeInt_0(this$static, v){
  this$static.data_0.length <= this$static.offset + 4 && $expand(this$static, this$static.offset + 4);
  this$static.data_0[this$static.offset++] = narrow_byte(v >> 24 & 255);
  this$static.data_0[this$static.offset++] = narrow_byte(v >> 16 & 255);
  this$static.data_0[this$static.offset++] = narrow_byte(v >> 8 & 255);
  this$static.data_0[this$static.offset++] = narrow_byte(v & 255);
}

function $writeLong_0(this$static, v){
  this$static.data_0.length <= this$static.offset + 8 && $expand(this$static, this$static.offset + 8);
  v = and(v, {l:$intern_7, m:$intern_7, h:$intern_8});
  this$static.data_0[this$static.offset++] = toInt(and(shr(v, 56), {l:255, m:0, h:0})) << 24 >> 24;
  this$static.data_0[this$static.offset++] = toInt(and(shr(v, 48), {l:255, m:0, h:0})) << 24 >> 24;
  this$static.data_0[this$static.offset++] = toInt(and(shr(v, 40), {l:255, m:0, h:0})) << 24 >> 24;
  this$static.data_0[this$static.offset++] = toInt(and(shr(v, 32), {l:255, m:0, h:0})) << 24 >> 24;
  this$static.data_0[this$static.offset++] = toInt(and(shr(v, 24), {l:255, m:0, h:0})) << 24 >> 24;
  this$static.data_0[this$static.offset++] = toInt(and(shr(v, 16), {l:255, m:0, h:0})) << 24 >> 24;
  this$static.data_0[this$static.offset++] = toInt(and(shr(v, 8), {l:255, m:0, h:0})) << 24 >> 24;
  this$static.data_0[this$static.offset++] = toInt(and(v, {l:255, m:0, h:0})) << 24 >> 24;
}

function $writeProtoBytes(this$static, v, len){
  $writeVarInt_0(this$static, fromInt(len));
  $writeBytes_1(this$static, v, 0, len);
}

function $writeProtoLongs(this$static, values){
  var l, l$index, l$max;
  if (values.length > 1024) {
    throw new IllegalArgumentException_0("Values can't be more than 1024");
  }
  $writeVarInt_0(this$static, fromInt(values.length));
  for (l$index = 0 , l$max = values.length; l$index < l$max; ++l$index) {
    l = values[l$index];
    $writeLong_0(this$static, l);
  }
}

function $writeProtoString(this$static, value_0){
  var data_0;
  data_0 = getBytesUtf8(value_0);
  $writeProtoBytes(this$static, data_0, data_0.length);
}

function $writeVarInt_0(this$static, v){
  while (neq(and(v, {l:4194176, m:$intern_7, h:$intern_8}), {l:0, m:0, h:0})) {
    $writeByte(this$static, toInt(or(and(v, {l:127, m:0, h:0}), {l:128, m:0, h:0})));
    v = shru(v, 7);
  }
  $writeByte(this$static, toInt(and(v, {l:127, m:0, h:0})));
}

function DataOutput(){
  this.data_0 = initDim(B_classLit, $intern_17, 0, 16, 7, 1);
}

defineClass(44, 1, {}, DataOutput);
_.offset = 0;
var Lim_actor_model_droidkit_bser_DataOutput_2_classLit = createForClass('im.actor.model.droidkit.bser', 'DataOutput', 44, Ljava_lang_Object_2_classLit);
function IncorrectTypeException(detailMessage){
  RuntimeException_0.call(this, detailMessage);
}

defineClass(462, 10, $intern_2, IncorrectTypeException);
var Lim_actor_model_droidkit_bser_IncorrectTypeException_2_classLit = createForClass('im.actor.model.droidkit.bser', 'IncorrectTypeException', 462, Ljava_lang_RuntimeException_2_classLit);
function UnknownFieldException(message){
  RuntimeException_0.call(this, message);
}

defineClass(364, 10, $intern_2, UnknownFieldException);
var Lim_actor_model_droidkit_bser_UnknownFieldException_2_classLit = createForClass('im.actor.model.droidkit.bser', 'UnknownFieldException', 364, Ljava_lang_RuntimeException_2_classLit);
function byteArrayToString(data_0){
  return data_0 == null?'null':hex(data_0);
}

function byteArrayToStringCompact(data_0){
  var digest, res;
  return data_0 == null?'null':hex(($clinit_CryptoUtils() , digest = new MD5Digest , $update_3(digest, data_0, 0, data_0.length) , res = initDim(B_classLit, $intern_17, 0, 16, 7, 1) , $finish(digest) , $unpackWord(digest.H1, res, 0) , $unpackWord(digest.H2, res, 4) , $unpackWord(digest.H3, res, 8) , $unpackWord(digest.H4, res, 12) , $reset_1(digest) , res));
}

function convertInt(val){
  if (lt(val, {l:0, m:4193792, h:$intern_8})) {
    throw new IOException_0('Too small value');
  }
   else if (gt(val, {l:$intern_7, m:511, h:0})) {
    throw new IOException_0('Too big value');
  }
  return toInt(val);
}

function convertString(data_0){
  return data_0 == null?null:_String(data_0, data_0.length, 'utf-8');
}

function idealByteArraySize(need){
  var i_0;
  for (i_0 = 4; i_0 < 32; i_0++)
    if (need <= (1 << i_0) - 12)
      return (1 << i_0) - 12;
  return need;
}

function $clinit_ContainerHelpers(){
  $clinit_ContainerHelpers = emptyMethod;
  EMPTY_INTS = initDim(I_classLit, $intern_4, 0, 0, 7, 1);
  EMPTY_OBJECTS = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
}

function binarySearch(array, size_0, value_0){
  $clinit_ContainerHelpers();
  var hi, lo, mid, midVal;
  lo = 0;
  hi = size_0 - 1;
  while (lo <= hi) {
    mid = lo + hi >>> 1;
    midVal = array[mid];
    if (midVal < value_0) {
      lo = mid + 1;
    }
     else if (midVal > value_0) {
      hi = mid - 1;
    }
     else {
      return mid;
    }
  }
  return ~lo;
}

var EMPTY_INTS, EMPTY_OBJECTS;
function $clinit_SparseArray(){
  $clinit_SparseArray = emptyMethod;
  DELETED = new Object_0;
}

function $gc(this$static){
  var i_0, keys_0, n, o, val, values;
  n = this$static.mSize;
  o = 0;
  keys_0 = this$static.mKeys;
  values = this$static.mValues;
  for (i_0 = 0; i_0 < n; i_0++) {
    val = values[i_0];
    if (maskUndefined(val) !== maskUndefined(DELETED)) {
      if (i_0 != o) {
        keys_0[o] = keys_0[i_0];
        setCheck(values, o, val);
        setCheck(values, i_0, null);
      }
      ++o;
    }
  }
  this$static.mGarbage = false;
  this$static.mSize = o;
}

function $get(this$static, key){
  var i_0;
  i_0 = binarySearch(this$static.mKeys, this$static.mSize, key);
  return i_0 < 0 || maskUndefined(this$static.mValues[i_0]) === maskUndefined(DELETED)?null:this$static.mValues[i_0];
}

function $indexOfKey(this$static, key){
  this$static.mGarbage && $gc(this$static);
  return binarySearch(this$static.mKeys, this$static.mSize, key);
}

function $put(this$static, key, value_0){
  var i_0, n, nkeys, nvalues;
  i_0 = binarySearch(this$static.mKeys, this$static.mSize, key);
  if (i_0 >= 0) {
    setCheck(this$static.mValues, i_0, value_0);
  }
   else {
    i_0 = ~i_0;
    if (i_0 < this$static.mSize && maskUndefined(this$static.mValues[i_0]) === maskUndefined(DELETED)) {
      this$static.mKeys[i_0] = key;
      setCheck(this$static.mValues, i_0, value_0);
      return;
    }
    if (this$static.mGarbage && this$static.mSize >= this$static.mKeys.length) {
      $gc(this$static);
      i_0 = ~binarySearch(this$static.mKeys, this$static.mSize, key);
    }
    if (this$static.mSize >= this$static.mKeys.length) {
      n = ~~(idealByteArraySize((this$static.mSize + 1) * 4) / 4);
      nkeys = initDim(I_classLit, $intern_4, 0, n, 7, 1);
      nvalues = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, n, 3, 1);
      arraycopy(this$static.mKeys, 0, nkeys, 0, this$static.mKeys.length);
      arraycopy(this$static.mValues, 0, nvalues, 0, this$static.mValues.length);
      this$static.mKeys = nkeys;
      this$static.mValues = nvalues;
    }
    if (this$static.mSize - i_0 != 0) {
      arraycopy(this$static.mKeys, i_0, this$static.mKeys, i_0 + 1, this$static.mSize - i_0);
      arraycopy(this$static.mValues, i_0, this$static.mValues, i_0 + 1, this$static.mSize - i_0);
    }
    this$static.mKeys[i_0] = key;
    setCheck(this$static.mValues, i_0, value_0);
    ++this$static.mSize;
  }
}

function SparseArray(){
  $clinit_SparseArray();
  SparseArray_0.call(this, 10);
}

function SparseArray_0(initialCapacity){
  if (initialCapacity == 0) {
    this.mKeys = ($clinit_ContainerHelpers() , EMPTY_INTS);
    this.mValues = EMPTY_OBJECTS;
  }
   else {
    initialCapacity = ~~(idealByteArraySize(initialCapacity * 4) / 4);
    this.mKeys = initDim(I_classLit, $intern_4, 0, initialCapacity, 7, 1);
    this.mValues = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, initialCapacity, 3, 1);
  }
  this.mSize = 0;
}

defineClass(453, 1, {}, SparseArray);
_.toString$ = function toString_157(){
  var buffer, i_0, key, value_0;
  this.mGarbage && $gc(this);
  if (this.mSize <= 0) {
    return '{}';
  }
  buffer = new StringBuilder_0;
  buffer.string += '{';
  for (i_0 = 0; i_0 < this.mSize; i_0++) {
    i_0 > 0 && (buffer.string += ', ' , buffer);
    key = (this.mGarbage && $gc(this) , this.mKeys[i_0]);
    buffer.string += key;
    buffer.string += '=';
    value_0 = (this.mGarbage && $gc(this) , this.mValues[i_0]);
    value_0 !== this?(buffer.string += value_0 , buffer):(buffer.string += '(this Map)' , buffer);
  }
  buffer.string += '}';
  return buffer.string;
}
;
_.mGarbage = false;
_.mSize = 0;
var DELETED;
var Lim_actor_model_droidkit_bser_util_SparseArray_2_classLit = createForClass('im.actor.model.droidkit.bser.util', 'SparseArray', 453, Ljava_lang_Object_2_classLit);
function KeyValueRecord(id_0, data_0){
  this.id_0 = id_0;
  this.data_0 = data_0;
}

defineClass(377, 1, {377:1}, KeyValueRecord);
_.id_0 = {l:0, m:0, h:0};
var Lim_actor_model_droidkit_engine_KeyValueRecord_2_classLit = createForClass('im.actor.model.droidkit.engine', 'KeyValueRecord', 377, Ljava_lang_Object_2_classLit);
function ListEngineRecord(key, order, data_0){
  this.key = key;
  this.order = order;
  this.data_0 = data_0;
}

defineClass(97, 1, {97:1}, ListEngineRecord);
_.key = {l:0, m:0, h:0};
_.order = {l:0, m:0, h:0};
var Lim_actor_model_droidkit_engine_ListEngineRecord_2_classLit = createForClass('im.actor.model.droidkit.engine', 'ListEngineRecord', 97, Ljava_lang_Object_2_classLit);
function $get_0(this$static, key){
  return $getValue_0(this$static.storage, key);
}

function $put_0(this$static, key, data_0){
  $addOrUpdateItem(this$static.storage, key, data_0);
}

function SyncKeyValue(storage){
  this.storage = storage;
}

defineClass(360, 1, {}, SyncKeyValue);
var Lim_actor_model_droidkit_engine_SyncKeyValue_2_classLit = createForClass('im.actor.model.droidkit.engine', 'SyncKeyValue', 360, Ljava_lang_Object_2_classLit);
function Avatar_0(){
}

function Avatar_1(smallImage, largeImage, fullImage){
  this.smallImage = smallImage;
  this.largeImage = largeImage;
  this.fullImage = fullImage;
}

function fromBytes_3(data_0){
  return dynamicCast(parse_159(new Avatar_0, data_0), 60);
}

defineClass(60, 4, {4:1, 60:1}, Avatar_0, Avatar_1);
_.equals$ = function equals_1(o){
  var avatar;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_entity_Avatar_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  avatar = dynamicCast(o, 60);
  if (this.fullImage?!$equals_0(this.fullImage, avatar.fullImage):!!avatar.fullImage)
    return false;
  if (this.largeImage?!$equals_0(this.largeImage, avatar.largeImage):!!avatar.largeImage)
    return false;
  if (this.smallImage?!$equals_0(this.smallImage, avatar.smallImage):!!avatar.smallImage)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_2(){
  var result;
  result = this.smallImage?$hashCode(this.smallImage):0;
  result = 31 * result + (this.largeImage?$hashCode(this.largeImage):0);
  result = 31 * result + (this.fullImage?$hashCode(this.fullImage):0);
  return result;
}
;
_.parse_0 = function parse_160(values){
  var full, large, small_0;
  small_0 = $getBytes_0(values, 1);
  small_0 != null && (this.smallImage = dynamicCast(parse_159(new AvatarImage_0, small_0), 75));
  large = $getBytes_0(values, 2);
  large != null && (this.largeImage = dynamicCast(parse_159(new AvatarImage_0, large), 75));
  full = $getBytes_0(values, 3);
  full != null && (this.fullImage = dynamicCast(parse_159(new AvatarImage_0, full), 75));
}
;
_.serialize = function serialize_151(writer){
  !!this.smallImage && $writeObject(writer, 1, this.smallImage);
  !!this.largeImage && $writeObject(writer, 2, this.smallImage);
  !!this.fullImage && $writeObject(writer, 3, this.fullImage);
}
;
var Lim_actor_model_entity_Avatar_2_classLit = createForClass('im.actor.model.entity', 'Avatar', 60, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $equals_0(this$static, o){
  var that;
  if (this$static === o)
    return true;
  if (o == null || Lim_actor_model_entity_AvatarImage_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 75);
  if (this$static.height != that.height)
    return false;
  if (this$static.width_0 != that.width_0)
    return false;
  if (!$equals_1(this$static.fileReference, that.fileReference))
    return false;
  return true;
}

function $hashCode(this$static){
  var result;
  result = this$static.width_0;
  result = 31 * result + this$static.height;
  result = 31 * result + $hashCode_0(this$static.fileReference);
  return result;
}

function AvatarImage_0(){
}

function AvatarImage_1(width_0, height, fileReference){
  this.width_0 = width_0;
  this.height = height;
  this.fileReference = fileReference;
}

defineClass(75, 4, {4:1, 75:1}, AvatarImage_0, AvatarImage_1);
_.equals$ = function equals_2(o){
  return $equals_0(this, o);
}
;
_.hashCode$ = function hashCode_3(){
  return $hashCode(this);
}
;
_.parse_0 = function parse_161(values){
  this.width_0 = convertInt($getLong(values, 1));
  this.height = convertInt($getLong(values, 2));
  this.fileReference = fromBytes_5($getBytes(values, 3));
}
;
_.serialize = function serialize_152(writer){
  $writeInt(writer, 1, this.width_0);
  $writeInt(writer, 2, this.height);
  $writeObject(writer, 3, this.fileReference);
}
;
_.height = 0;
_.width_0 = 0;
var Lim_actor_model_entity_AvatarImage_2_classLit = createForClass('im.actor.model.entity', 'AvatarImage', 75, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_Contact(){
  $clinit_Contact = emptyMethod;
  CREATOR = new Contact$1;
}

function Contact(){
  $clinit_Contact();
}

function Contact_0(uid, sortKey, avatar, name_0){
  $clinit_Contact();
  this.uid = uid;
  this.sortKey = sortKey;
  this.avatar = avatar;
  this.name_0 = name_0;
}

defineClass(460, 4, $intern_19, Contact, Contact_0);
_.getEngineId = function getEngineId(){
  return fromInt(this.uid);
}
;
_.getEngineSearch = function getEngineSearch(){
  return this.name_0;
}
;
_.getEngineSort = function getEngineSort(){
  return this.sortKey;
}
;
_.parse_0 = function parse_162(values){
  this.uid = convertInt($getLong(values, 1));
  this.sortKey = $getLong(values, 2);
  this.name_0 = convertString($getBytes(values, 3));
  $getBytes_0(values, 4) != null && (this.avatar = fromBytes_3($getBytes(values, 4)));
}
;
_.serialize = function serialize_153(writer){
  $writeInt(writer, 1, this.uid);
  $writeLong(writer, 2, this.sortKey);
  $writeString(writer, 3, this.name_0);
  !!this.avatar && $writeObject(writer, 4, this.avatar);
}
;
_.sortKey = {l:0, m:0, h:0};
_.uid = 0;
var CREATOR;
var Lim_actor_model_entity_Contact_2_classLit = createForClass('im.actor.model.entity', 'Contact', 460, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Contact$1(){
}

defineClass(713, 1, {}, Contact$1);
_.createInstance = function createInstance(){
  return new Contact;
}
;
var Lim_actor_model_entity_Contact$1_2_classLit = createForClass('im.actor.model.entity', 'Contact/1', 713, Ljava_lang_Object_2_classLit);
function ContactRecord(){
}

function ContactRecord_0(recordData){
  this.id_0 = 0;
  this.accessHash = {l:0, m:0, h:0};
  this.recordType = 0;
  this.recordData = recordData;
  this.recordTitle = 'Mobile';
}

defineClass(124, 4, {4:1, 116:1, 124:1}, ContactRecord, ContactRecord_0);
_.getEngineId = function getEngineId_0(){
  return fromInt(this.id_0);
}
;
_.parse_0 = function parse_163(values){
  this.id_0 = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.recordType = convertInt($getLong(values, 3));
  this.recordData = convertString($getBytes(values, 4));
  this.recordTitle = convertString($getBytes(values, 5));
}
;
_.serialize = function serialize_154(writer){
  $writeVarIntField(writer, 1, fromInt(this.id_0));
  $writeVarIntField(writer, 2, this.accessHash);
  $writeVarIntField(writer, 3, fromInt(this.recordType));
  $writeString(writer, 4, this.recordData);
  $writeString(writer, 5, this.recordTitle);
}
;
_.accessHash = {l:0, m:0, h:0};
_.id_0 = 0;
_.recordType = 0;
var Lim_actor_model_entity_ContactRecord_2_classLit = createForClass('im.actor.model.entity', 'ContactRecord', 124, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function ContentDescription(){
}

function ContentDescription_0(contentType){
  ContentDescription_2.call(this, contentType, '', 0, false);
}

function ContentDescription_1(contentType, text_0){
  ContentDescription_2.call(this, contentType, text_0, 0, false);
}

function ContentDescription_2(contentType, text_0, relatedUser, isSilent){
  this.contentType = contentType;
  this.text_0 = text_0;
  this.relatedUser = relatedUser;
  this.isSilent = isSilent;
  this.isEncrypted = false;
}

function fromBytes_4(data_0){
  return dynamicCast(parse_159(new ContentDescription, data_0), 39);
}

function fromContent(msg){
  return instanceOf(msg, 55)?new ContentDescription_1(($clinit_ContentType() , TEXT_0), dynamicCast(msg, 55).text_0):instanceOf(msg, 62)?new ContentDescription_0(($clinit_ContentType() , DOCUMENT_PHOTO)):instanceOf(msg, 63)?new ContentDescription_0(($clinit_ContentType() , DOCUMENT_VIDEO)):instanceOf(msg, 61)?new ContentDescription_0(($clinit_ContentType() , DOCUMENT)):instanceOf(msg, 133)?new ContentDescription_0(($clinit_ContentType() , SERVICE_REGISTERED)):instanceOf(msg, 74)?!dynamicCast(msg, 74).newAvatar?new ContentDescription_0(($clinit_ContentType() , SERVICE_AVATAR_REMOVED)):new ContentDescription_0(($clinit_ContentType() , SERVICE_AVATAR)):instanceOf(msg, 73)?new ContentDescription_1(($clinit_ContentType() , SERVICE_TITLE), dynamicCast(msg, 73).newTitle):instanceOf(msg, 84)?new ContentDescription_0(($clinit_ContentType() , SERVICE_CREATED)):instanceOf(msg, 68)?new ContentDescription_2(($clinit_ContentType() , SERVICE_ADD), '', dynamicCast(msg, 68).addedUid, false):instanceOf(msg, 72)?new ContentDescription_2(($clinit_ContentType() , SERVICE_KICK), '', dynamicCast(msg, 72).kickedUid, false):instanceOf(msg, 102)?new ContentDescription_2(($clinit_ContentType() , SERVICE_LEAVE), '', 0, true):new ContentDescription_0(($clinit_ContentType() , UNKNOWN_CONTENT));
}

defineClass(39, 4, {4:1, 39:1}, ContentDescription, ContentDescription_0, ContentDescription_1, ContentDescription_2);
_.parse_0 = function parse_164(values){
  this.contentType = fromValue(convertInt($getLong(values, 1)));
  this.text_0 = convertString($getBytes(values, 2));
  this.relatedUser = convertInt($getLong(values, 3));
  this.isSilent = neq($getLong(values, 4), {l:0, m:0, h:0});
  this.isEncrypted = neq($getLong(values, 5), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_155(writer){
  $writeInt(writer, 1, this.contentType.value_0);
  $writeString(writer, 2, this.text_0);
  $writeInt(writer, 3, this.relatedUser);
  $writeBool(writer, 4, this.isSilent);
  $writeBool(writer, 5, this.isEncrypted);
}
;
_.isEncrypted = false;
_.isSilent = false;
_.relatedUser = 0;
var Lim_actor_model_entity_ContentDescription_2_classLit = createForClass('im.actor.model.entity', 'ContentDescription', 39, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_ContentType(){
  $clinit_ContentType = emptyMethod;
  TEXT_0 = new ContentType('TEXT', 0, 2);
  EMPTY = new ContentType('EMPTY', 1, 1);
  DOCUMENT = new ContentType('DOCUMENT', 2, 3);
  DOCUMENT_PHOTO = new ContentType('DOCUMENT_PHOTO', 3, 4);
  DOCUMENT_VIDEO = new ContentType('DOCUMENT_VIDEO', 4, 5);
  SERVICE = new ContentType('SERVICE', 5, 6);
  SERVICE_ADD = new ContentType('SERVICE_ADD', 6, 7);
  SERVICE_KICK = new ContentType('SERVICE_KICK', 7, 8);
  SERVICE_LEAVE = new ContentType('SERVICE_LEAVE', 8, 9);
  SERVICE_REGISTERED = new ContentType('SERVICE_REGISTERED', 9, 10);
  SERVICE_CREATED = new ContentType('SERVICE_CREATED', 10, 11);
  SERVICE_TITLE = new ContentType('SERVICE_TITLE', 11, 12);
  SERVICE_AVATAR = new ContentType('SERVICE_AVATAR', 12, 13);
  SERVICE_AVATAR_REMOVED = new ContentType('SERVICE_AVATAR_REMOVED', 13, 14);
  UNKNOWN_CONTENT = new ContentType('UNKNOWN_CONTENT', 14, 15);
}

function ContentType(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function fromValue(value_0){
  $clinit_ContentType();
  switch (value_0) {
    default:case 1:
      return EMPTY;
    case 2:
      return TEXT_0;
    case 3:
      return DOCUMENT;
    case 4:
      return DOCUMENT_PHOTO;
    case 5:
      return DOCUMENT_VIDEO;
    case 6:
      return SERVICE;
    case 7:
      return SERVICE_ADD;
    case 8:
      return SERVICE_KICK;
    case 9:
      return SERVICE_LEAVE;
    case 10:
      return SERVICE_REGISTERED;
    case 11:
      return SERVICE_CREATED;
    case 12:
      return SERVICE_TITLE;
    case 13:
      return SERVICE_AVATAR;
    case 14:
      return SERVICE_AVATAR_REMOVED;
  }
}

function values_7(){
  $clinit_ContentType();
  return initValues(getClassLiteralForArray(Lim_actor_model_entity_ContentType_2_classLit, 1), $intern_6, 41, 0, [TEXT_0, EMPTY, DOCUMENT, DOCUMENT_PHOTO, DOCUMENT_VIDEO, SERVICE, SERVICE_ADD, SERVICE_KICK, SERVICE_LEAVE, SERVICE_REGISTERED, SERVICE_CREATED, SERVICE_TITLE, SERVICE_AVATAR, SERVICE_AVATAR_REMOVED, UNKNOWN_CONTENT]);
}

defineClass(41, 18, {41:1, 3:1, 29:1, 18:1}, ContentType);
_.value_0 = 0;
var DOCUMENT, DOCUMENT_PHOTO, DOCUMENT_VIDEO, EMPTY, SERVICE, SERVICE_ADD, SERVICE_AVATAR, SERVICE_AVATAR_REMOVED, SERVICE_CREATED, SERVICE_KICK, SERVICE_LEAVE, SERVICE_REGISTERED, SERVICE_TITLE, TEXT_0, UNKNOWN_CONTENT;
var Lim_actor_model_entity_ContentType_2_classLit = createForEnum('im.actor.model.entity', 'ContentType', 41, Ljava_lang_Enum_2_classLit, values_7);
function $clinit_Dialog(){
  $clinit_Dialog = emptyMethod;
  CREATOR_0 = new Dialog$1;
}

function $editPeerInfo(this$static, title_0, dialogAvatar){
  return new Dialog_1(this$static.peer, this$static.sortDate, title_0, dialogAvatar, this$static.unreadCount, this$static.rid, this$static.messageType, this$static.text_0, this$static.status_0, this$static.senderId, this$static.date, this$static.relatedUid);
}

function Dialog_0(){
  $clinit_Dialog();
}

function Dialog_1(peer, sortKey, dialogTitle, dialogAvatar, unreadCount, rid, messageType, text_0, status_0, senderId, date, relatedUid){
  $clinit_Dialog();
  this.peer = peer;
  this.dialogTitle = dialogTitle;
  this.dialogAvatar = dialogAvatar;
  this.unreadCount = unreadCount;
  this.rid = rid;
  this.sortDate = sortKey;
  this.senderId = senderId;
  this.date = date;
  this.messageType = messageType;
  this.text_0 = text_0;
  this.status_0 = status_0;
  this.relatedUid = relatedUid;
}

defineClass(49, 4, {4:1, 149:1, 49:1}, Dialog_0, Dialog_1);
_.getEngineId = function getEngineId_1(){
  return $getUnuqueId(this.peer);
}
;
_.getEngineSearch = function getEngineSearch_0(){
  return this.dialogTitle;
}
;
_.getEngineSort = function getEngineSort_0(){
  return this.sortDate;
}
;
_.parse_0 = function parse_165(values){
  var av;
  this.peer = fromBytes_6($getBytes(values, 1));
  this.dialogTitle = convertString($getBytes(values, 2));
  av = $getBytes_0(values, 3);
  av != null && (this.dialogAvatar = dynamicCast(parse_159(new Avatar_0, av), 60));
  this.unreadCount = convertInt($getLong(values, 4));
  this.sortDate = $getLong(values, 5);
  this.rid = $getLong(values, 6);
  this.senderId = convertInt($getLong(values, 7));
  this.date = $getLong(values, 8);
  this.messageType = fromValue(convertInt($getLong(values, 9)));
  this.text_0 = convertString($getBytes(values, 10));
  this.status_0 = fromValue_0(convertInt($getLong(values, 11)));
  this.relatedUid = convertInt($getLong(values, 12));
}
;
_.serialize = function serialize_156(writer){
  $writeObject(writer, 1, this.peer);
  $writeString(writer, 2, this.dialogTitle);
  !!this.dialogAvatar && $writeObject(writer, 3, this.dialogAvatar);
  $writeVarIntField(writer, 4, fromInt(this.unreadCount));
  $writeVarIntField(writer, 5, this.sortDate);
  $writeVarIntField(writer, 6, this.rid);
  $writeVarIntField(writer, 7, fromInt(this.senderId));
  $writeVarIntField(writer, 8, this.date);
  $writeVarIntField(writer, 9, fromInt(this.messageType.value_0));
  $writeString(writer, 10, this.text_0);
  $writeVarIntField(writer, 11, fromInt(this.status_0.value_0));
  $writeVarIntField(writer, 12, fromInt(this.relatedUid));
}
;
_.date = {l:0, m:0, h:0};
_.relatedUid = 0;
_.rid = {l:0, m:0, h:0};
_.senderId = 0;
_.sortDate = {l:0, m:0, h:0};
_.unreadCount = 0;
var CREATOR_0;
var Lim_actor_model_entity_Dialog_2_classLit = createForClass('im.actor.model.entity', 'Dialog', 49, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Dialog$1(){
}

defineClass(657, 1, {}, Dialog$1);
_.createInstance = function createInstance_0(){
  return new Dialog_0;
}
;
var Lim_actor_model_entity_Dialog$1_2_classLit = createForClass('im.actor.model.entity', 'Dialog/1', 657, Ljava_lang_Object_2_classLit);
function $$init(this$static){
}

function $createDialog(this$static){
  return new Dialog_1(this$static.peer, this$static.sortKey, this$static.dialogTitle, this$static.dialogAvatar, this$static.unreadCount, this$static.rid, this$static.messageType, this$static.text_0, this$static.status_0, this$static.senderId, this$static.time, this$static.relatedUid);
}

function $setDialogAvatar(this$static, avatar){
  this$static.dialogAvatar = avatar;
  return this$static;
}

function $setDialogTitle(this$static, dialogTitle){
  this$static.dialogTitle = dialogTitle;
  return this$static;
}

function $setMessageType(this$static, messageType){
  this$static.messageType = messageType;
  return this$static;
}

function $setPeer(this$static, peer){
  this$static.peer = peer;
  return this$static;
}

function $setRelatedUid(this$static, relatedUid){
  this$static.relatedUid = relatedUid;
  return this$static;
}

function $setRid(this$static, rid){
  this$static.rid = rid;
  return this$static;
}

function $setSenderId(this$static, senderId){
  this$static.senderId = senderId;
  return this$static;
}

function $setSortKey(this$static, sortKey){
  this$static.sortKey = sortKey;
  return this$static;
}

function $setStatus(this$static, status_0){
  this$static.status_0 = status_0;
  return this$static;
}

function $setText(this$static, text_0){
  this$static.text_0 = text_0;
  return this$static;
}

function $setTime(this$static, time){
  this$static.time = time;
  return this$static;
}

function $setUnreadCount(this$static, unreadCount){
  this$static.unreadCount = unreadCount;
  return this$static;
}

function DialogBuilder(){
  $$init(this);
}

function DialogBuilder_0(dialog){
  $$init(this);
  this.peer = dialog.peer;
  this.sortKey = dialog.sortDate;
  this.dialogTitle = dialog.dialogTitle;
  this.dialogAvatar = dialog.dialogAvatar;
  this.unreadCount = dialog.unreadCount;
  this.rid = dialog.rid;
  this.messageType = dialog.messageType;
  this.text_0 = dialog.text_0;
  this.status_0 = dialog.status_0;
  this.senderId = dialog.senderId;
  this.time = dialog.date;
  this.relatedUid = dialog.relatedUid;
}

defineClass(194, 1, {}, DialogBuilder, DialogBuilder_0);
_.relatedUid = 0;
_.rid = {l:0, m:0, h:0};
_.senderId = 0;
_.sortKey = {l:0, m:0, h:0};
_.time = {l:0, m:0, h:0};
_.unreadCount = 0;
var Lim_actor_model_entity_DialogBuilder_2_classLit = createForClass('im.actor.model.entity', 'DialogBuilder', 194, Ljava_lang_Object_2_classLit);
function $equals_1(this$static, o){
  var that;
  if (this$static === o)
    return true;
  if (o == null || Lim_actor_model_entity_FileReference_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 134);
  if (neq(this$static.fileId, that.fileId))
    return false;
  return true;
}

function $hashCode_0(this$static){
  return toInt(xor(this$static.fileId, shru(this$static.fileId, 32)));
}

function FileReference(){
}

function FileReference_0(fileId, accessHash, fileSize, fileName){
  this.fileId = fileId;
  this.accessHash = accessHash;
  this.fileSize = fileSize;
  this.fileName = fileName;
}

function fromBytes_5(data_0){
  return dynamicCast(parse_159(new FileReference, data_0), 134);
}

defineClass(134, 4, {4:1, 134:1}, FileReference, FileReference_0);
_.equals$ = function equals_3(o){
  return $equals_1(this, o);
}
;
_.hashCode$ = function hashCode_4(){
  return $hashCode_0(this);
}
;
_.parse_0 = function parse_166(values){
  this.fileId = $getLong(values, 1);
  this.accessHash = $getLong(values, 2);
  this.fileSize = convertInt($getLong(values, 3));
  this.fileName = convertString($getBytes(values, 4));
}
;
_.serialize = function serialize_157(writer){
  $writeLong(writer, 1, this.fileId);
  $writeLong(writer, 2, this.accessHash);
  $writeInt(writer, 3, this.fileSize);
  $writeString(writer, 4, this.fileName);
}
;
_.accessHash = {l:0, m:0, h:0};
_.fileId = {l:0, m:0, h:0};
_.fileSize = 0;
var Lim_actor_model_entity_FileReference_2_classLit = createForClass('im.actor.model.entity', 'FileReference', 134, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $addMember(this$static, uid, inviterUid, inviteDate, isAdmin){
  var member, member$iterator, nMembers;
  nMembers = new ArrayList;
  for (member$iterator = this$static.members.iterator(); member$iterator.hasNext();) {
    member = dynamicCast(member$iterator.next(), 103);
    member.uid != uid && (setCheck(nMembers.array, nMembers.array.length, member) , true);
  }
  $add_0(nMembers, new GroupMember_0(uid, inviterUid, inviteDate, isAdmin));
  return new Group_1(this$static.groupId, this$static.accessHash, this$static.title_0, this$static.avatar, nMembers, this$static.adminId, this$static.isMember);
}

function $changeMember(this$static){
  return new Group_1(this$static.groupId, this$static.accessHash, this$static.title_0, this$static.avatar, this$static.members, this$static.adminId, false);
}

function $removeMember(this$static, uid){
  var member, member$iterator, nMembers;
  nMembers = new ArrayList;
  for (member$iterator = this$static.members.iterator(); member$iterator.hasNext();) {
    member = dynamicCast(member$iterator.next(), 103);
    member.uid != uid && (setCheck(nMembers.array, nMembers.array.length, member) , true);
  }
  return new Group_1(this$static.groupId, this$static.accessHash, this$static.title_0, this$static.avatar, nMembers, this$static.adminId, this$static.isMember);
}

function Group_0(){
}

function Group_1(groupId, accessHash, title_0, avatar, members, adminId, isMember){
  this.groupId = groupId;
  this.accessHash = accessHash;
  this.title_0 = title_0;
  this.avatar = avatar;
  this.members = members;
  this.adminId = adminId;
  this.isMember = isMember;
}

defineClass(21, 4, {4:1, 116:1, 21:1}, Group_0, Group_1);
_.getEngineId = function getEngineId_2(){
  return fromInt(this.groupId);
}
;
_.parse_0 = function parse_167(values){
  var count, i_0, res;
  this.groupId = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.title_0 = convertString($getBytes(values, 3));
  $getBytes_0(values, 4) != null && (this.avatar = fromBytes_3($getBytes(values, 4)));
  this.adminId = convertInt($getLong(values, 5));
  count = $getRepeatedCount(values, 6);
  if (count > 0) {
    res = new ArrayList;
    for (i_0 = 0; i_0 < count; i_0++) {
      $add_0(res, new GroupMember);
    }
    this.members = $getRepeatedObj(values, 6, res);
  }
   else {
    this.members = new ArrayList;
  }
  this.isMember = neq($getLong(values, 7), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_158(writer){
  $writeVarIntField(writer, 1, fromInt(this.groupId));
  $writeVarIntField(writer, 2, this.accessHash);
  $writeString(writer, 3, this.title_0);
  !!this.avatar && $writeObject(writer, 4, this.avatar);
  $writeVarIntField(writer, 5, fromInt(this.adminId));
  $writeRepeatedObj(writer, 6, this.members);
  $writeVarIntField(writer, 7, fromInt(this.isMember?1:0));
}
;
_.accessHash = {l:0, m:0, h:0};
_.adminId = 0;
_.groupId = 0;
_.isMember = false;
var Lim_actor_model_entity_Group_2_classLit = createForClass('im.actor.model.entity', 'Group', 21, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function GroupMember(){
}

function GroupMember_0(uid, inviterUid, inviteDate, isAdministrator){
  this.uid = uid;
  this.inviterUid = inviterUid;
  this.inviteDate = inviteDate;
  this.isAdministrator = isAdministrator;
}

defineClass(103, 4, {4:1, 103:1}, GroupMember, GroupMember_0);
_.equals$ = function equals_4(o){
  var member;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_entity_GroupMember_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  member = dynamicCast(o, 103);
  if (this.uid != member.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_5(){
  return this.uid;
}
;
_.parse_0 = function parse_168(values){
  this.uid = convertInt($getLong(values, 1));
  this.inviterUid = convertInt($getLong(values, 2));
  this.inviteDate = $getLong(values, 3);
  this.isAdministrator = neq($getLong(values, 4), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_159(writer){
  $writeInt(writer, 1, this.uid);
  $writeInt(writer, 2, this.inviterUid);
  $writeLong(writer, 3, this.inviteDate);
  $writeBool(writer, 4, this.isAdministrator);
}
;
_.inviteDate = {l:0, m:0, h:0};
_.inviterUid = 0;
_.isAdministrator = false;
_.uid = 0;
var Lim_actor_model_entity_GroupMember_2_classLit = createForClass('im.actor.model.entity', 'GroupMember', 103, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_Message(){
  $clinit_Message = emptyMethod;
  CREATOR_1 = new Message$1;
}

function $changeState(this$static, messageState){
  return new Message_0(this$static.rid, this$static.sortDate, this$static.date, this$static.senderId, messageState, this$static.content_0);
}

function Message(){
  $clinit_Message();
}

function Message_0(rid, sortDate, date, senderId, messageState, content_0){
  $clinit_Message();
  this.rid = rid;
  this.sortDate = sortDate;
  this.date = date;
  this.senderId = senderId;
  this.messageState = messageState;
  this.content_0 = content_0;
}

defineClass(26, 4, {4:1, 149:1, 26:1}, Message, Message_0);
_.getEngineId = function getEngineId_3(){
  return this.rid;
}
;
_.getEngineSearch = function getEngineSearch_1(){
  return null;
}
;
_.getEngineSort = function getEngineSort_1(){
  return this.sortDate;
}
;
_.parse_0 = function parse_169(values){
  this.rid = $getLong(values, 1);
  this.sortDate = $getLong(values, 2);
  this.date = $getLong(values, 3);
  this.senderId = convertInt($getLong(values, 4));
  this.messageState = fromValue_0(convertInt($getLong(values, 5)));
  this.content_0 = contentFromBytes($getBytes(values, 6));
}
;
_.serialize = function serialize_160(writer){
  $writeVarIntField(writer, 1, this.rid);
  $writeVarIntField(writer, 2, this.sortDate);
  $writeVarIntField(writer, 3, this.date);
  $writeVarIntField(writer, 4, fromInt(this.senderId));
  $writeVarIntField(writer, 5, fromInt(this.messageState.value_0));
  $writeObject(writer, 6, this.content_0);
}
;
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
_.senderId = 0;
_.sortDate = {l:0, m:0, h:0};
var CREATOR_1;
var Lim_actor_model_entity_Message_2_classLit = createForClass('im.actor.model.entity', 'Message', 26, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function Message$1(){
}

defineClass(646, 1, {}, Message$1);
_.createInstance = function createInstance_1(){
  return new Message;
}
;
var Lim_actor_model_entity_Message$1_2_classLit = createForClass('im.actor.model.entity', 'Message/1', 646, Ljava_lang_Object_2_classLit);
function $clinit_MessageState_0(){
  $clinit_MessageState_0 = emptyMethod;
  PENDING = new MessageState_0('PENDING', 0, 1);
  SENT_0 = new MessageState_0('SENT', 1, 2);
  RECEIVED_0 = new MessageState_0('RECEIVED', 2, 3);
  READ_0 = new MessageState_0('READ', 3, 4);
  ERROR = new MessageState_0('ERROR', 4, 5);
  UNKNOWN_0 = new MessageState_0('UNKNOWN', 5, 6);
}

function MessageState_0(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function fromValue_0(value_0){
  $clinit_MessageState_0();
  switch (value_0) {
    case 1:
      return PENDING;
    case 2:
      return SENT_0;
    case 3:
      return RECEIVED_0;
    case 4:
      return READ_0;
    case 5:
      return ERROR;
    default:case 6:
      return UNKNOWN_0;
  }
}

function values_8(){
  $clinit_MessageState_0();
  return initValues(getClassLiteralForArray(Lim_actor_model_entity_MessageState_2_classLit, 1), $intern_6, 83, 0, [PENDING, SENT_0, RECEIVED_0, READ_0, ERROR, UNKNOWN_0]);
}

defineClass(83, 18, {83:1, 3:1, 29:1, 18:1}, MessageState_0);
_.value_0 = 0;
var ERROR, PENDING, READ_0, RECEIVED_0, SENT_0, UNKNOWN_0;
var Lim_actor_model_entity_MessageState_2_classLit = createForEnum('im.actor.model.entity', 'MessageState', 83, Ljava_lang_Enum_2_classLit, values_8);
function Notification_0(peer, sender, contentDescription){
  this.peer = peer;
  this.sender = sender;
  this.contentDescription = contentDescription;
}

defineClass(373, 1, {373:1}, Notification_0);
_.sender = 0;
var Lim_actor_model_entity_Notification_2_classLit = createForClass('im.actor.model.entity', 'Notification', 373, Ljava_lang_Object_2_classLit);
function $equals_2(this$static, o){
  var peer;
  if (this$static === o)
    return true;
  if (o == null || Lim_actor_model_entity_Peer_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  peer = dynamicCast(o, 19);
  if (this$static.peerId != peer.peerId)
    return false;
  if (this$static.peerType != peer.peerType)
    return false;
  return true;
}

function $getUnuqueId(this$static){
  var type_0;
  switch (this$static.peerType.ordinal) {
    default:case 0:
      type_0 = 0;
      break;
    case 1:
      type_0 = 1;
      break;
    case 2:
      type_0 = 2;
  }
  return add_0(and(fromInt(this$static.peerId), {l:$intern_7, m:1023, h:0}), shl(and(fromInt(type_0), {l:$intern_7, m:1023, h:0}), 32));
}

function $hashCode_1(this$static){
  var result;
  result = getHashCode(this$static.peerType);
  result = 31 * result + this$static.peerId;
  return result;
}

function Peer_1(){
}

function Peer_2(peerType, peerId){
  this.peerType = peerType;
  this.peerId = peerId;
}

function fromBytes_6(data_0){
  return dynamicCast(parse_159(new Peer_1, data_0), 19);
}

function fromUniqueId(uid){
  var id_0, type_0;
  id_0 = toInt(and(uid, {l:$intern_7, m:1023, h:0}));
  type_0 = toInt(and(shr(uid, 32), {l:$intern_7, m:1023, h:0}));
  switch (type_0) {
    default:case 0:
      return new Peer_2(($clinit_PeerType_0() , PRIVATE_0), id_0);
    case 1:
      return new Peer_2(($clinit_PeerType_0() , GROUP_0), id_0);
    case 2:
      return new Peer_2(($clinit_PeerType_0() , EMAIL_0), id_0);
  }
}

function group_0(gid){
  return new Peer_2(($clinit_PeerType_0() , GROUP_0), gid);
}

function user_0(uid){
  return new Peer_2(($clinit_PeerType_0() , PRIVATE_0), uid);
}

defineClass(19, 4, {4:1, 19:1}, Peer_1, Peer_2);
_.equals$ = function equals_5(o){
  return $equals_2(this, o);
}
;
_.hashCode$ = function hashCode_6(){
  return $hashCode_1(this);
}
;
_.parse_0 = function parse_170(values){
  this.peerId = convertInt($getLong(values, 1));
  switch (convertInt($getLong(values, 2))) {
    default:case 1:
      this.peerType = ($clinit_PeerType_0() , PRIVATE_0);
      break;
    case 2:
      this.peerType = ($clinit_PeerType_0() , EMAIL_0);
      break;
    case 3:
      this.peerType = ($clinit_PeerType_0() , GROUP_0);
  }
}
;
_.serialize = function serialize_161(writer){
  $writeVarIntField(writer, 1, fromInt(this.peerId));
  switch (this.peerType.ordinal) {
    default:case 0:
      $writeTag(writer, 2, 0);
      $writeVarInt_0(writer.stream, {l:1, m:0, h:0});
      break;
    case 2:
      $writeTag(writer, 2, 0);
      $writeVarInt_0(writer.stream, {l:2, m:0, h:0});
      break;
    case 1:
      $writeTag(writer, 2, 0);
      $writeVarInt_0(writer.stream, {l:3, m:0, h:0});
  }
}
;
_.toString$ = function toString_158(){
  return '{type:' + this.peerType + ', id:' + this.peerId + '}';
}
;
_.peerId = 0;
var Lim_actor_model_entity_Peer_2_classLit = createForClass('im.actor.model.entity', 'Peer', 19, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_PeerType_0(){
  $clinit_PeerType_0 = emptyMethod;
  PRIVATE_0 = new PeerType_0('PRIVATE', 0);
  GROUP_0 = new PeerType_0('GROUP', 1);
  EMAIL_0 = new PeerType_0('EMAIL', 2);
}

function PeerType_0(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_9(){
  $clinit_PeerType_0();
  return initValues(getClassLiteralForArray(Lim_actor_model_entity_PeerType_2_classLit, 1), $intern_6, 162, 0, [PRIVATE_0, GROUP_0, EMAIL_0]);
}

defineClass(162, 18, {162:1, 3:1, 29:1, 18:1}, PeerType_0);
var EMAIL_0, GROUP_0, PRIVATE_0;
var Lim_actor_model_entity_PeerType_2_classLit = createForEnum('im.actor.model.entity', 'PeerType', 162, Ljava_lang_Enum_2_classLit, values_9);
function $clinit_SearchEntity(){
  $clinit_SearchEntity = emptyMethod;
  CREATOR_2 = new SearchEntity$1;
}

function SearchEntity(){
  $clinit_SearchEntity();
}

function SearchEntity_0(peer, order, avatar, title_0){
  $clinit_SearchEntity();
  this.peer = peer;
  this.order = order;
  this.avatar = avatar;
  this.title_0 = title_0;
}

defineClass(363, 4, $intern_19, SearchEntity, SearchEntity_0);
_.getEngineId = function getEngineId_4(){
  return $getUnuqueId(this.peer);
}
;
_.getEngineSearch = function getEngineSearch_2(){
  return this.title_0;
}
;
_.getEngineSort = function getEngineSort_2(){
  return this.order;
}
;
_.parse_0 = function parse_171(values){
  this.peer = fromBytes_6($getBytes(values, 1));
  this.order = $getLong(values, 2);
  $getBytes_0(values, 3) != null?(this.avatar = fromBytes_3($getBytes(values, 3))):(this.avatar = null);
  this.title_0 = convertString($getBytes(values, 4));
}
;
_.serialize = function serialize_162(writer){
  $writeObject(writer, 1, this.peer);
  $writeLong(writer, 2, this.order);
  !!this.avatar && $writeObject(writer, 3, this.avatar);
  $writeString(writer, 4, this.title_0);
}
;
_.order = {l:0, m:0, h:0};
var CREATOR_2;
var Lim_actor_model_entity_SearchEntity_2_classLit = createForClass('im.actor.model.entity', 'SearchEntity', 363, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function SearchEntity$1(){
}

defineClass(712, 1, {}, SearchEntity$1);
_.createInstance = function createInstance_2(){
  return new SearchEntity;
}
;
var Lim_actor_model_entity_SearchEntity$1_2_classLit = createForClass('im.actor.model.entity', 'SearchEntity/1', 712, Ljava_lang_Object_2_classLit);
function $clinit_Sex_0(){
  $clinit_Sex_0 = emptyMethod;
  UNKNOWN_1 = new Sex_0('UNKNOWN', 0, 1);
  MALE_0 = new Sex_0('MALE', 1, 2);
  FEMALE_0 = new Sex_0('FEMALE', 2, 3);
}

function Sex_0(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function fromValue_1(value_0){
  $clinit_Sex_0();
  switch (value_0) {
    default:case 1:
      return UNKNOWN_1;
    case 2:
      return MALE_0;
    case 3:
      return FEMALE_0;
  }
}

function values_10(){
  $clinit_Sex_0();
  return initValues(getClassLiteralForArray(Lim_actor_model_entity_Sex_2_classLit, 1), $intern_6, 188, 0, [UNKNOWN_1, MALE_0, FEMALE_0]);
}

defineClass(188, 18, {188:1, 3:1, 29:1, 18:1}, Sex_0);
_.value_0 = 0;
var FEMALE_0, MALE_0, UNKNOWN_1;
var Lim_actor_model_entity_Sex_2_classLit = createForEnum('im.actor.model.entity', 'Sex', 188, Ljava_lang_Enum_2_classLit, values_10);
function $getName(this$static){
  return this$static.localName == null?this$static.name_0:this$static.localName;
}

function User_0(){
}

function User_1(uid, accessHash, name_0, localName, avatar, sex, records){
  this.uid = uid;
  this.accessHash = accessHash;
  this.name_0 = name_0;
  this.localName = localName;
  this.avatar = avatar;
  this.sex = sex;
  this.records = records;
}

defineClass(11, 4, {4:1, 116:1, 11:1}, User_0, User_1);
_.getEngineId = function getEngineId_5(){
  return fromInt(this.uid);
}
;
_.parse_0 = function parse_172(values){
  var a, count, i_0, rec;
  this.uid = convertInt($getLong(values, 1));
  this.accessHash = $getLong(values, 2);
  this.name_0 = convertString($getBytes(values, 3));
  this.localName = convertString($getBytes_0(values, 4));
  a = $getBytes_0(values, 5);
  a != null && (this.avatar = dynamicCast(parse_159(new Avatar_0, a), 60));
  this.sex = fromValue_1(convertInt($getLong(values, 6)));
  count = $getRepeatedCount(values, 7);
  if (count > 0) {
    rec = new ArrayList;
    for (i_0 = 0; i_0 < count; i_0++) {
      $add_0(rec, new ContactRecord);
    }
    this.records = $getRepeatedObj(values, 7, rec);
  }
}
;
_.serialize = function serialize_163(writer){
  $writeVarIntField(writer, 1, fromInt(this.uid));
  $writeVarIntField(writer, 2, this.accessHash);
  $writeString(writer, 3, this.name_0);
  this.localName != null && $writeString(writer, 4, this.localName);
  !!this.avatar && $writeObject(writer, 5, this.avatar);
  $writeVarIntField(writer, 6, fromInt(this.sex.value_0));
  $writeRepeatedObj(writer, 7, this.records);
}
;
_.accessHash = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_entity_User_2_classLit = createForClass('im.actor.model.entity', 'User', 11, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function contentFromBytes(data_0){
  var reader, type_0;
  reader = new BserValues(deserialize(new DataInput_0(data_0, 0, data_0.length)));
  type_0 = typeFromValue(convertInt($getLong(reader, 1)));
  switch (type_0.ordinal) {
    case 0:
      return dynamicCast(parse_159(new TextContent, data_0), 55);
    case 1:
      return dynamicCast(parse_159(new DocumentContent, data_0), 61);
    case 2:
      return dynamicCast(parse_159(new PhotoContent, data_0), 62);
    case 3:
      return dynamicCast(parse_159(new VideoContent, data_0), 63);
    case 4:
      return dynamicCast(parse_159(new ServiceContent, data_0), 37);
    case 11:
      return dynamicCast(parse_159(new ServiceUserRegistered, data_0), 133);
    case 5:
      return dynamicCast(parse_159(new ServiceGroupCreated, data_0), 84);
    case 7:
      return dynamicCast(parse_159(new ServiceGroupTitleChanged, data_0), 73);
    case 6:
      return dynamicCast(parse_159(new ServiceGroupAvatarChanged, data_0), 74);
    case 8:
      return dynamicCast(parse_159(new ServiceGroupUserAdded, data_0), 68);
    case 9:
      return dynamicCast(parse_159(new ServiceGroupUserKicked, data_0), 72);
    case 10:
      return dynamicCast(parse_159(new ServiceGroupUserLeave, data_0), 102);
    default:throw new IOException_0('Unknown type');
  }
}

function typeFromValue(val){
  switch (val) {
    default:case 1:
      return $clinit_AbsContent$ContentType() , TEXT_1;
    case 2:
      return $clinit_AbsContent$ContentType() , DOCUMENT_0;
    case 3:
      return $clinit_AbsContent$ContentType() , DOCUMENT_PHOTO_0;
    case 4:
      return $clinit_AbsContent$ContentType() , DOCUMENT_VIDEO_0;
    case 5:
      return $clinit_AbsContent$ContentType() , SERVICE_0;
    case 6:
      return $clinit_AbsContent$ContentType() , SERVICE_CREATED_0;
    case 7:
      return $clinit_AbsContent$ContentType() , SERVICE_AVATAR_0;
    case 8:
      return $clinit_AbsContent$ContentType() , SERVICE_TITLE_0;
    case 9:
      return $clinit_AbsContent$ContentType() , SERVICE_ADDED;
    case 10:
      return $clinit_AbsContent$ContentType() , SERVICE_KICKED;
    case 11:
      return $clinit_AbsContent$ContentType() , SERVICE_LEAVE_0;
    case 12:
      return $clinit_AbsContent$ContentType() , SERVICE_REGISTERED_0;
  }
}

defineClass(792, 4, $intern_14);
_.parse_0 = function parse_173(values){
}
;
_.serialize = function serialize_164(writer){
  $writeVarIntField(writer, 1, fromInt(this.getContentType().value_0));
}
;
var Lim_actor_model_entity_content_AbsContent_2_classLit = createForClass('im.actor.model.entity.content', 'AbsContent', 792, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $clinit_AbsContent$ContentType(){
  $clinit_AbsContent$ContentType = emptyMethod;
  TEXT_1 = new AbsContent$ContentType('TEXT', 0, 1);
  DOCUMENT_0 = new AbsContent$ContentType('DOCUMENT', 1, 2);
  DOCUMENT_PHOTO_0 = new AbsContent$ContentType('DOCUMENT_PHOTO', 2, 3);
  DOCUMENT_VIDEO_0 = new AbsContent$ContentType('DOCUMENT_VIDEO', 3, 4);
  SERVICE_0 = new AbsContent$ContentType('SERVICE', 4, 5);
  SERVICE_CREATED_0 = new AbsContent$ContentType('SERVICE_CREATED', 5, 6);
  SERVICE_AVATAR_0 = new AbsContent$ContentType('SERVICE_AVATAR', 6, 7);
  SERVICE_TITLE_0 = new AbsContent$ContentType('SERVICE_TITLE', 7, 8);
  SERVICE_ADDED = new AbsContent$ContentType('SERVICE_ADDED', 8, 9);
  SERVICE_KICKED = new AbsContent$ContentType('SERVICE_KICKED', 9, 10);
  SERVICE_LEAVE_0 = new AbsContent$ContentType('SERVICE_LEAVE', 10, 11);
  SERVICE_REGISTERED_0 = new AbsContent$ContentType('SERVICE_REGISTERED', 11, 12);
}

function AbsContent$ContentType(enum$name, enum$ordinal, value_0){
  Enum.call(this, enum$name, enum$ordinal);
  this.value_0 = value_0;
}

function values_11(){
  $clinit_AbsContent$ContentType();
  return initValues(getClassLiteralForArray(Lim_actor_model_entity_content_AbsContent$ContentType_2_classLit, 1), $intern_6, 54, 0, [TEXT_1, DOCUMENT_0, DOCUMENT_PHOTO_0, DOCUMENT_VIDEO_0, SERVICE_0, SERVICE_CREATED_0, SERVICE_AVATAR_0, SERVICE_TITLE_0, SERVICE_ADDED, SERVICE_KICKED, SERVICE_LEAVE_0, SERVICE_REGISTERED_0]);
}

defineClass(54, 18, {54:1, 3:1, 29:1, 18:1}, AbsContent$ContentType);
_.value_0 = 0;
var DOCUMENT_0, DOCUMENT_PHOTO_0, DOCUMENT_VIDEO_0, SERVICE_0, SERVICE_ADDED, SERVICE_AVATAR_0, SERVICE_CREATED_0, SERVICE_KICKED, SERVICE_LEAVE_0, SERVICE_REGISTERED_0, SERVICE_TITLE_0, TEXT_1;
var Lim_actor_model_entity_content_AbsContent$ContentType_2_classLit = createForEnum('im.actor.model.entity.content', 'AbsContent/ContentType', 54, Ljava_lang_Enum_2_classLit, values_11);
function $parse_1(this$static, values){
  var ft;
  this$static.source = fromBytes_7($getBytes(values, 2));
  this$static.mimetype = convertString($getBytes(values, 3));
  this$static.name_0 = convertString($getBytes(values, 4));
  ft = $getBytes_0(values, 5);
  ft != null && (this$static.fastThumb = dynamicCast(parse_159(new FastThumb_1, ft), 278));
}

function $serialize(this$static, writer){
  $writeVarIntField(writer, 1, fromInt(this$static.getContentType().value_0));
  $writeBytes(writer, 2, $toByteArray(this$static.source));
  $writeString(writer, 3, this$static.mimetype);
  $writeString(writer, 4, this$static.name_0);
  !!this$static.fastThumb && $writeObject(writer, 5, this$static.fastThumb);
}

function DocumentContent(){
}

function DocumentContent_0(source, mimetype, name_0, fastThumb){
  this.source = source;
  this.mimetype = mimetype;
  this.name_0 = name_0;
  this.fastThumb = fastThumb;
}

defineClass(61, 792, {4:1, 61:1}, DocumentContent, DocumentContent_0);
_.getContentType = function getContentType(){
  return $clinit_AbsContent$ContentType() , DOCUMENT_0;
}
;
_.parse_0 = function parse_174(values){
  $parse_1(this, values);
}
;
_.serialize = function serialize_165(writer){
  $serialize(this, writer);
}
;
var Lim_actor_model_entity_content_DocumentContent_2_classLit = createForClass('im.actor.model.entity.content', 'DocumentContent', 61, Lim_actor_model_entity_content_AbsContent_2_classLit);
function FastThumb_1(){
}

function FastThumb_2(w, h, image){
  this.w = w;
  this.h_0 = h;
  this.image = image;
}

defineClass(278, 4, {4:1, 278:1}, FastThumb_1, FastThumb_2);
_.parse_0 = function parse_175(values){
  this.w = convertInt($getLong(values, 1));
  this.h_0 = convertInt($getLong(values, 2));
  this.image = $getBytes(values, 3);
}
;
_.serialize = function serialize_166(writer){
  $writeInt(writer, 1, this.w);
  $writeInt(writer, 2, this.h_0);
  $writeBytes(writer, 3, this.image);
}
;
_.h_0 = 0;
_.w = 0;
var Lim_actor_model_entity_content_FastThumb_2_classLit = createForClass('im.actor.model.entity.content', 'FastThumb', 278, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $serialize_0(this$static, writer){
  if (instanceOf(this$static, 279)) {
    $writeTag(writer, 1, 0);
    $writeVarInt_0(writer.stream, {l:1, m:0, h:0});
  }
   else if (instanceOf(this$static, 190)) {
    $writeTag(writer, 1, 0);
    $writeVarInt_0(writer.stream, {l:2, m:0, h:0});
  }
   else {
    throw new IOException_0('Invalid source type');
  }
}

function fromBytes_7(data_0){
  var reader, type_0, fileLocalSource, fileLocalSource_0;
  reader = new BserValues(deserialize(new DataInput_0(data_0, 0, data_0.length)));
  type_0 = convertInt($getLong(reader, 1));
  switch (type_0) {
    case 1:
      return fileLocalSource = new FileLocalSource , $parse_2(fileLocalSource, reader) , fileLocalSource;
    case 2:
      return fileLocalSource_0 = new FileRemoteSource , fileLocalSource_0.fileReference = fromBytes_5($getBytes(reader, 2)) , fileLocalSource_0;
    default:throw new IOException_0('Invalid source type');
  }
}

defineClass(797, 4, $intern_14);
_.parse_0 = function parse_176(values){
}
;
_.serialize = function serialize_167(writer){
  $serialize_0(this, writer);
}
;
var Lim_actor_model_entity_content_FileSource_2_classLit = createForClass('im.actor.model.entity.content', 'FileSource', 797, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $parse_2(this$static, values){
  this$static.fileName = convertString($getBytes(values, 2));
  this$static.size_0 = convertInt($getLong(values, 3));
  this$static.fileDescriptor = convertString($getBytes(values, 4));
}

function FileLocalSource(){
}

defineClass(279, 797, {4:1, 279:1}, FileLocalSource);
_.parse_0 = function parse_177(values){
  $parse_2(this, values);
}
;
_.serialize = function serialize_168(writer){
  $serialize_0(this, writer);
  $writeString(writer, 2, this.fileName);
  $writeInt(writer, 3, this.size_0);
  $writeString(writer, 4, this.fileDescriptor);
}
;
_.size_0 = 0;
var Lim_actor_model_entity_content_FileLocalSource_2_classLit = createForClass('im.actor.model.entity.content', 'FileLocalSource', 279, Lim_actor_model_entity_content_FileSource_2_classLit);
function FileRemoteSource(){
}

function FileRemoteSource_0(fileReference){
  this.fileReference = fileReference;
}

defineClass(190, 797, {4:1, 190:1}, FileRemoteSource, FileRemoteSource_0);
_.parse_0 = function parse_178(values){
  this.fileReference = fromBytes_5($getBytes(values, 2));
}
;
_.serialize = function serialize_169(writer){
  $serialize_0(this, writer);
  $writeObject(writer, 2, this.fileReference);
}
;
var Lim_actor_model_entity_content_FileRemoteSource_2_classLit = createForClass('im.actor.model.entity.content', 'FileRemoteSource', 190, Lim_actor_model_entity_content_FileSource_2_classLit);
function PhotoContent(){
}

function PhotoContent_0(location_0, mimetype, name_0, fastThumb, w, h){
  DocumentContent_0.call(this, location_0, mimetype, name_0, fastThumb);
  this.w = w;
  this.h_0 = h;
}

defineClass(62, 61, {4:1, 61:1, 62:1}, PhotoContent, PhotoContent_0);
_.getContentType = function getContentType_0(){
  return $clinit_AbsContent$ContentType() , DOCUMENT_PHOTO_0;
}
;
_.parse_0 = function parse_179(values){
  $parse_1(this, values);
  this.w = convertInt($getLong(values, 10));
  this.h_0 = convertInt($getLong(values, 11));
}
;
_.serialize = function serialize_170(writer){
  $serialize(this, writer);
  $writeInt(writer, 10, this.w);
  $writeInt(writer, 11, this.h_0);
}
;
_.h_0 = 0;
_.w = 0;
var Lim_actor_model_entity_content_PhotoContent_2_classLit = createForClass('im.actor.model.entity.content', 'PhotoContent', 62, Lim_actor_model_entity_content_DocumentContent_2_classLit);
function ServiceContent(){
}

function ServiceContent_0(compatText){
  this.compatText = compatText;
}

defineClass(37, 792, {4:1, 37:1}, ServiceContent, ServiceContent_0);
_.getContentType = function getContentType_1(){
  return $clinit_AbsContent$ContentType() , SERVICE_0;
}
;
_.parse_0 = function parse_180(values){
  this.compatText = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_171(writer){
  $writeVarIntField(writer, 1, fromInt(this.getContentType().value_0));
  $writeString(writer, 2, this.compatText);
}
;
var Lim_actor_model_entity_content_ServiceContent_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceContent', 37, Lim_actor_model_entity_content_AbsContent_2_classLit);
function ServiceGroupAvatarChanged(){
}

function ServiceGroupAvatarChanged_0(newAvatar){
  ServiceContent_0.call(this, 'Group avatar changed');
  this.newAvatar = newAvatar;
}

defineClass(74, 37, {4:1, 37:1, 74:1}, ServiceGroupAvatarChanged, ServiceGroupAvatarChanged_0);
_.getContentType = function getContentType_2(){
  return $clinit_AbsContent$ContentType() , SERVICE_AVATAR_0;
}
;
_.parse_0 = function parse_181(values){
  var data_0;
  this.compatText = convertString($getBytes(values, 2));
  data_0 = $getBytes_0(values, 10);
  data_0 != null && (this.newAvatar = dynamicCast(parse_159(new Avatar_0, data_0), 60));
}
;
_.serialize = function serialize_172(writer){
  $writeVarIntField(writer, 1, fromInt(($clinit_AbsContent$ContentType() , SERVICE_AVATAR_0).value_0));
  $writeString(writer, 2, this.compatText);
  !!this.newAvatar && $writeObject(writer, 10, this.newAvatar);
}
;
var Lim_actor_model_entity_content_ServiceGroupAvatarChanged_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceGroupAvatarChanged', 74, Lim_actor_model_entity_content_ServiceContent_2_classLit);
function ServiceGroupCreated(){
}

function ServiceGroupCreated_0(groupTitle){
  ServiceContent_0.call(this, "Group '" + groupTitle + "' created");
  this.groupTitle = groupTitle;
}

defineClass(84, 37, {4:1, 37:1, 84:1}, ServiceGroupCreated, ServiceGroupCreated_0);
_.getContentType = function getContentType_3(){
  return $clinit_AbsContent$ContentType() , SERVICE_CREATED_0;
}
;
_.parse_0 = function parse_182(values){
  this.compatText = convertString($getBytes(values, 2));
  this.groupTitle = convertString($getBytes(values, 10));
}
;
_.serialize = function serialize_173(writer){
  $writeVarIntField(writer, 1, fromInt(($clinit_AbsContent$ContentType() , SERVICE_CREATED_0).value_0));
  $writeString(writer, 2, this.compatText);
  $writeString(writer, 10, this.groupTitle);
}
;
var Lim_actor_model_entity_content_ServiceGroupCreated_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceGroupCreated', 84, Lim_actor_model_entity_content_ServiceContent_2_classLit);
function ServiceGroupTitleChanged(){
}

function ServiceGroupTitleChanged_0(newTitle){
  ServiceContent_0.call(this, 'Group theme changed');
  this.newTitle = newTitle;
}

defineClass(73, 37, {4:1, 37:1, 73:1}, ServiceGroupTitleChanged, ServiceGroupTitleChanged_0);
_.getContentType = function getContentType_4(){
  return $clinit_AbsContent$ContentType() , SERVICE_TITLE_0;
}
;
_.parse_0 = function parse_183(values){
  this.compatText = convertString($getBytes(values, 2));
  this.newTitle = convertString($getBytes(values, 10));
}
;
_.serialize = function serialize_174(writer){
  $writeVarIntField(writer, 1, fromInt(($clinit_AbsContent$ContentType() , SERVICE_TITLE_0).value_0));
  $writeString(writer, 2, this.compatText);
  $writeString(writer, 10, this.newTitle);
}
;
var Lim_actor_model_entity_content_ServiceGroupTitleChanged_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceGroupTitleChanged', 73, Lim_actor_model_entity_content_ServiceContent_2_classLit);
function ServiceGroupUserAdded(){
}

function ServiceGroupUserAdded_0(addedUid){
  ServiceContent_0.call(this, 'Member added');
  this.addedUid = addedUid;
}

defineClass(68, 37, {4:1, 37:1, 68:1}, ServiceGroupUserAdded, ServiceGroupUserAdded_0);
_.getContentType = function getContentType_5(){
  return $clinit_AbsContent$ContentType() , SERVICE_ADDED;
}
;
_.parse_0 = function parse_184(values){
  this.compatText = convertString($getBytes(values, 2));
  this.addedUid = convertInt($getLong(values, 10));
}
;
_.serialize = function serialize_175(writer){
  $writeVarIntField(writer, 1, fromInt(($clinit_AbsContent$ContentType() , SERVICE_ADDED).value_0));
  $writeString(writer, 2, this.compatText);
  $writeInt(writer, 10, this.addedUid);
}
;
_.addedUid = 0;
var Lim_actor_model_entity_content_ServiceGroupUserAdded_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceGroupUserAdded', 68, Lim_actor_model_entity_content_ServiceContent_2_classLit);
function ServiceGroupUserKicked(){
}

function ServiceGroupUserKicked_0(kickedUid){
  ServiceContent_0.call(this, 'User kicked');
  this.kickedUid = kickedUid;
}

defineClass(72, 37, {4:1, 37:1, 72:1}, ServiceGroupUserKicked, ServiceGroupUserKicked_0);
_.getContentType = function getContentType_6(){
  return $clinit_AbsContent$ContentType() , SERVICE_KICKED;
}
;
_.parse_0 = function parse_185(values){
  this.compatText = convertString($getBytes(values, 2));
  this.kickedUid = convertInt($getLong(values, 10));
}
;
_.serialize = function serialize_176(writer){
  $writeVarIntField(writer, 1, fromInt(($clinit_AbsContent$ContentType() , SERVICE_KICKED).value_0));
  $writeString(writer, 2, this.compatText);
  $writeInt(writer, 10, this.kickedUid);
}
;
_.kickedUid = 0;
var Lim_actor_model_entity_content_ServiceGroupUserKicked_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceGroupUserKicked', 72, Lim_actor_model_entity_content_ServiceContent_2_classLit);
function ServiceGroupUserLeave(){
  ServiceContent_0.call(this, 'User leave');
}

defineClass(102, 37, {4:1, 37:1, 102:1}, ServiceGroupUserLeave);
_.getContentType = function getContentType_7(){
  return $clinit_AbsContent$ContentType() , SERVICE_LEAVE_0;
}
;
var Lim_actor_model_entity_content_ServiceGroupUserLeave_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceGroupUserLeave', 102, Lim_actor_model_entity_content_ServiceContent_2_classLit);
function ServiceUserRegistered(){
  ServiceContent_0.call(this, 'User registered');
}

defineClass(133, 37, {4:1, 37:1, 133:1}, ServiceUserRegistered);
var Lim_actor_model_entity_content_ServiceUserRegistered_2_classLit = createForClass('im.actor.model.entity.content', 'ServiceUserRegistered', 133, Lim_actor_model_entity_content_ServiceContent_2_classLit);
function TextContent(){
}

function TextContent_0(text_0){
  this.text_0 = text_0;
}

defineClass(55, 792, {4:1, 55:1}, TextContent, TextContent_0);
_.getContentType = function getContentType_8(){
  return $clinit_AbsContent$ContentType() , TEXT_1;
}
;
_.parse_0 = function parse_186(values){
  this.text_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_177(writer){
  $writeVarIntField(writer, 1, fromInt(($clinit_AbsContent$ContentType() , TEXT_1).value_0));
  $writeString(writer, 2, this.text_0);
}
;
var Lim_actor_model_entity_content_TextContent_2_classLit = createForClass('im.actor.model.entity.content', 'TextContent', 55, Lim_actor_model_entity_content_AbsContent_2_classLit);
function VideoContent(){
}

function VideoContent_0(location_0, mimetype, name_0, fastThumb, duration, w, h){
  DocumentContent_0.call(this, location_0, mimetype, name_0, fastThumb);
  this.duration = duration;
  this.w = w;
  this.h_0 = h;
}

defineClass(63, 61, {4:1, 61:1, 63:1}, VideoContent, VideoContent_0);
_.getContentType = function getContentType_9(){
  return $clinit_AbsContent$ContentType() , DOCUMENT_VIDEO_0;
}
;
_.parse_0 = function parse_187(values){
  $parse_1(this, values);
  this.duration = convertInt($getLong(values, 10));
  this.w = convertInt($getLong(values, 11));
  this.h_0 = convertInt($getLong(values, 12));
}
;
_.serialize = function serialize_178(writer){
  $serialize(this, writer);
  $writeInt(writer, 10, this.duration);
  $writeInt(writer, 11, this.w);
  $writeInt(writer, 12, this.h_0);
}
;
_.duration = 0;
_.h_0 = 0;
_.w = 0;
var Lim_actor_model_entity_content_VideoContent_2_classLit = createForClass('im.actor.model.entity.content', 'VideoContent', 63, Lim_actor_model_entity_content_DocumentContent_2_classLit);
function $formatContentDialogText(this$static, senderId, contentType, text_0, relatedUid){
  switch (contentType.ordinal) {
    case 5:
    case 0:
      return text_0;
    case 2:
      if (text_0 == null || text_0.length == 0) {
        return dynamicCastToString($getStringValue(this$static.locale, 'ContentDocument'));
      }

      return text_0;
    case 3:
      return dynamicCastToString($getStringValue(this$static.locale, 'ContentPhoto'));
    case 4:
      return dynamicCastToString($getStringValue(this$static.locale, 'ContentVideo'));
    case 9:
      return $replace($getTemplate(this$static, senderId, 'ServiceRegistered'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
    case 10:
      return $replace($getTemplate(this$static, senderId, 'ServiceGroupCreated'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
    case 6:
      return $replace($replace($getTemplate(this$static, senderId, 'ServiceGroupAdded'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11))), '{name_added}', relatedUid == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'Thee')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(relatedUid)), 11)));
    case 8:
      return $replace($getTemplate(this$static, senderId, 'ServiceGroupLeaved'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
    case 7:
      return $replace($replace($getTemplate(this$static, senderId, 'ServiceGroupKicked'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11))), '{name_kicked}', relatedUid == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'Thee')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(relatedUid)), 11)));
    case 12:
      return $replace($getTemplate(this$static, senderId, 'ServiceGroupAvatarChanged'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
    case 13:
      return $replace($getTemplate(this$static, senderId, 'ServiceGroupAvatarRemoved'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
    case 11:
      return $replace($getTemplate(this$static, senderId, 'ServiceGroupTitle'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
    case 1:
      return '';
    default:case 14:
      return dynamicCastToString($getStringValue(this$static.locale, 'ContentUnsupported'));
  }
}

function $formatFullServiceMessage(this$static, senderId, content_0){
  if (instanceOf(content_0, 133)) {
    return $getTemplate(this$static, senderId, 'ServiceRegisteredFull');
  }
   else if (instanceOf(content_0, 84)) {
    return $replace($replace($getTemplate(this$static, senderId, 'ServiceGroupCreatedFull'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11))), '{title}', dynamicCast(content_0, 84).groupTitle);
  }
   else if (instanceOf(content_0, 68)) {
    return $replace($replace($getTemplate(this$static, senderId, 'ServiceGroupAdded'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11))), '{name_added}', $getSubjectName(this$static, dynamicCast(content_0, 68).addedUid));
  }
   else if (instanceOf(content_0, 72)) {
    return $replace($replace($getTemplate(this$static, senderId, 'ServiceGroupKicked'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11))), '{name_kicked}', $getSubjectName(this$static, dynamicCast(content_0, 72).kickedUid));
  }
   else if (instanceOf(content_0, 102)) {
    return $replace($getTemplate(this$static, senderId, 'ServiceGroupLeaved'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
  }
   else if (instanceOf(content_0, 73)) {
    return $replace($replace($getTemplate(this$static, senderId, 'ServiceGroupTitleFull'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11))), '{title}', dynamicCast(content_0, 73).newTitle);
  }
   else if (instanceOf(content_0, 74)) {
    return dynamicCast(content_0, 74).newAvatar?$replace($getTemplate(this$static, senderId, 'ServiceGroupAvatarChanged'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11))):$replace($getTemplate(this$static, senderId, 'ServiceGroupAvatarRemoved'), '{name}', senderId == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'You')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11)));
  }
  !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'i18NEngine' + ':' + ('Unknown service content: ' + content_0));
  return content_0.compatText;
}

function $formatShortDate(this$static, date){
  var d, date1, delta, month;
  delta = sub_0(fromDouble((new Date_0).jsdate.getTime()), date);
  if (lt(delta, {l:60000, m:0, h:0})) {
    return dynamicCastToString($getStringValue(this$static.locale, 'TimeShortNow'));
  }
   else if (lt(delta, {l:3600000, m:0, h:0})) {
    return $replace(dynamicCastToString($getStringValue(this$static.locale, 'TimeShortMinutes')), '{minutes}', '' + toString_2(div(delta, {l:60000, m:0, h:0})));
  }
   else if (lt(delta, {l:2513920, m:20, h:0})) {
    return $replace(dynamicCastToString($getStringValue(this$static.locale, 'TimeShortHours')), '{hours}', '' + toString_2(div(delta, {l:3600000, m:0, h:0})));
  }
   else if (lt(delta, {l:833536, m:41, h:0})) {
    return $replace(dynamicCastToString($getStringValue(this$static.locale, 'TimeShortYesterday')), '{hours}', '' + toString_2(div(delta, {l:3600000, m:0, h:0})));
  }
   else {
    date1 = new Date_1(date);
    month = date1.jsdate.getMonth();
    d = date1.jsdate.getDate();
    return d + ' ' + this$static.MONTHS_SHORT[month].toUpperCase();
  }
}

function $formatTime(this$static, date){
  var dateVal, hours;
  dateVal = new Date_1(date);
  if (this$static.is24Hours) {
    return dateVal.jsdate.getHours() + ':' + $formatTwoDigit(dateVal.jsdate.getMinutes());
  }
   else {
    hours = dateVal.jsdate.getHours();
    return hours > 12?hours - 12 + ':' + $formatTwoDigit(dateVal.jsdate.getMinutes()) + ' PM':hours + ':' + $formatTwoDigit(dateVal.jsdate.getMinutes()) + ' AM';
  }
}

function $formatTwoDigit(v){
  var res;
  if (v < 0) {
    return '00';
  }
   else if (v < 10) {
    return '0' + v;
  }
   else if (v < 100) {
    return '' + v;
  }
   else {
    res = '' + v;
    return $substring(res, res.length - 2);
  }
}

function $getSubjectName(this$static, uid){
  return uid == this$static.modules.auth.myUid?dynamicCastToString($getStringValue(this$static.locale, 'Thee')):$getName(dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11));
}

function $getTemplate(this$static, senderId, baseString){
  var u;
  if (senderId == this$static.modules.auth.myUid) {
    if ($hasStringValue(this$static.locale, baseString + 'You')) {
      return dynamicCastToString($getStringValue(this$static.locale, baseString + 'You'));
    }
  }
  if ($hasStringValue(this$static.locale, baseString + 'Male') && $hasStringValue(this$static.locale, baseString + 'Female')) {
    u = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(senderId)), 11);
    if (u.sex == ($clinit_Sex_0() , MALE_0)) {
      return dynamicCastToString($getStringValue(this$static.locale, baseString + 'Male'));
    }
     else if (u.sex == FEMALE_0) {
      return dynamicCastToString($getStringValue(this$static.locale, baseString + 'Female'));
    }
  }
  return dynamicCastToString($getStringValue(this$static.locale, baseString));
}

function I18nEngine(provider, modules){
  this.modules = modules;
  this.locale = provider.locale;
  this.is24Hours = true;
  this.MONTHS_SHORT = initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, [dynamicCastToString($getStringValue(this.locale, 'JanShort')), dynamicCastToString($getStringValue(this.locale, 'FebShort')), dynamicCastToString($getStringValue(this.locale, 'MarShort')), dynamicCastToString($getStringValue(this.locale, 'AprShort')), dynamicCastToString($getStringValue(this.locale, 'MayShort')), dynamicCastToString($getStringValue(this.locale, 'JunShort')), dynamicCastToString($getStringValue(this.locale, 'JulShort')), dynamicCastToString($getStringValue(this.locale, 'AugShort')), dynamicCastToString($getStringValue(this.locale, 'SepShort')), dynamicCastToString($getStringValue(this.locale, 'OctShort')), dynamicCastToString($getStringValue(this.locale, 'NovShort')), dynamicCastToString($getStringValue(this.locale, 'DecShort'))]);
  initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, [dynamicCastToString($getStringValue(this.locale, 'JanFull')), dynamicCastToString($getStringValue(this.locale, 'FebFull')), dynamicCastToString($getStringValue(this.locale, 'MarFull')), dynamicCastToString($getStringValue(this.locale, 'AprFull')), dynamicCastToString($getStringValue(this.locale, 'MayFull')), dynamicCastToString($getStringValue(this.locale, 'JunFull')), dynamicCastToString($getStringValue(this.locale, 'JulFull')), dynamicCastToString($getStringValue(this.locale, 'AugFull')), dynamicCastToString($getStringValue(this.locale, 'SepFull')), dynamicCastToString($getStringValue(this.locale, 'OctFull')), dynamicCastToString($getStringValue(this.locale, 'NovFull')), dynamicCastToString($getStringValue(this.locale, 'DecFull'))]);
}

defineClass(633, 1, {}, I18nEngine);
_.is24Hours = false;
var Lim_actor_model_i18n_I18nEngine_2_classLit = createForClass('im.actor.model.i18n', 'I18nEngine', 633, Ljava_lang_Object_2_classLit);
function JsConfigurationBuilder(){
  this.endpoints = new ArrayList;
  $setThreadingProvider(this, new JsThreadingProvider);
  $setNetworkProvider(this, new JsNetworkingProvider);
  $setLog(this, new JsLogProvider);
  $setMainThreadProvider(this, new JsMainThreadProvider);
  $setLocale(this, new JsLocaleProvider);
  $setCryptoProvider(this, new JsCryptoProvider);
  $setDispatcherProvider(this, new JsDispatcherProvider);
  $setPhoneBookProvider(this, new JsPhoneBookProvider);
  $setStorage(this, new JsStorageProvider);
  $setNotificationProvider(this, new JsNotificationsProvider);
}

defineClass(534, 533, {}, JsConfigurationBuilder);
var Lim_actor_model_js_JsConfigurationBuilder_2_classLit = createForClass('im.actor.model.js', 'JsConfigurationBuilder', 534, Lim_actor_model_ConfigurationBuilder_2_classLit);
function JsFacade(){
  var configuration, uniqueId;
  navigator.userAgent;
  uniqueId = getUniqueId();
  configuration = new JsConfigurationBuilder;
  $setApiConfiguration(configuration, new ApiConfiguration(uniqueId));
  $addEndpoint(configuration);
  this.messenger = new JsMessenger($build(configuration));
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'JsMessenger' + ':' + 'JsMessenger created');
}

defineClass(520, 1, {222:1}, JsFacade);
_.bindChat_0 = function bindChat(peer, callback){
  d_0('JsFacade', 'Bind chat: ' + peer.type + ' : ' + peer.id);
  $subscribe($getConversationList(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id)), callback);
}
;
_.bindDialogs_0 = function bindDialogs(callback){
  $subscribe($getDialogsList(this.messenger.angularModule), callback);
}
;
_.bindGroup_0 = function bindGroup(gid, callback){
  $subscribe_0($getGroup(this.messenger, gid), callback);
}
;
_.bindUser_0 = function bindUser(uid, callback){
  $subscribe_0($getUser(this.messenger, uid), callback);
}
;
_.clearChat_0 = function clearChat(peer, success, error){
  $start_2($clearChat(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id)), new JsFacade$6(success, error));
}
;
_.deleteChat_0 = function deleteChat(peer, success, error){
  $start_1($deleteChat(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id)), new JsFacade$5(success, error));
}
;
_.getAuthPhone_0 = function getAuthPhone(){
  return '' + toString_2($getLong_1(this.messenger.modules.auth.modules.preferences, 'auth_phone', {l:0, m:0, h:0}));
}
;
_.getAuthState_0 = function getAuthState(){
  return convert(this.messenger.modules.auth.state);
}
;
_.getGroup_0 = function getGroup(gid){
  return $getGroup(this.messenger, gid).value_0;
}
;
_.getUid_0 = function getUid(){
  return this.messenger.modules.auth.myUid;
}
;
_.getUser_0 = function getUser(uid){
  return $getUser(this.messenger, uid).value_0;
}
;
_.isLoggedIn_0 = function isLoggedIn(){
  return this.messenger.modules.auth.state == ($clinit_AuthState() , LOGGED_IN);
}
;
_.loadDraft_0 = function loadDraft(peer){
  return $loadDraft(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id));
}
;
_.onAppHidden_0 = function onAppHidden(){
  $onAppHidden(this.messenger.modules);
}
;
_.onAppVisible_0 = function onAppVisible(){
  $onAppVisible(this.messenger.modules);
}
;
_.onConversationClosed_0 = function onConversationClosed(peer){
  d_0('JsFacade', 'On chat closed: ' + peer.type + ' : ' + peer.id);
  $onConversationClosed(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id));
}
;
_.onConversationOpen_0 = function onConversationOpen(peer){
  d_0('JsFacade', 'On chat open: ' + peer.type + ' : ' + peer.id);
  $onConversationOpen(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id));
}
;
_.onDialogsClosed_0 = function onDialogsClosed(){
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'JsFacade' + ':' + 'On dialogs closed');
  $onDialogsClosed(this.messenger);
}
;
_.onDialogsOpen_0 = function onDialogsOpen(){
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'JsFacade' + ':' + 'On dialogs open');
  $onDialogsOpen(this.messenger);
}
;
_.onProfileClosed_0 = function onProfileClosed(uid){
}
;
_.onProfileOpen_0 = function onProfileOpen(uid){
  $onProfileOpen(this.messenger, uid);
}
;
_.onTyping_0 = function onTyping(peer){
  $onTyping(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id));
}
;
_.requestSms_0 = function requestSms(phone, success, error){
  var e, res;
  try {
    res = __parseAndValidateLong(phone);
    $start($requestSms(this.messenger, res), new JsFacade$1(this, success, error));
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 14)) {
      e = $e0;
      !!log_2 && error_0($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[E] ' + 'JsMessenger' + ':' + e);
      $postToMainThread(new JsFacade$2(this, error));
    }
     else 
      throw unwrap($e0);
  }
}
;
_.saveDraft_0 = function saveDraft(peer, text_0){
  $saveDraft(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id), text_0);
}
;
_.sendCode_0 = function sendCode(code_0, success, error){
  var e, res;
  try {
    res = __parseAndValidateInt(code_0);
    $start_0($sendCode(this.messenger, res), new JsFacade$3(this, success, error));
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 14)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      $postToMainThread(new JsFacade$4(this, error));
    }
     else 
      throw unwrap($e0);
  }
}
;
_.sendMessage_0 = function sendMessage_0(peer, text_0){
  $sendMessage(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id), text_0);
}
;
_.unbindChat_0 = function unbindChat(peer, callback){
  d_0('JsFacade', 'UnBind chat: ' + peer.type + ' : ' + peer.id);
  $subscribe($getConversationList(this.messenger, $equals_3(peer.type, 'user')?user_0(peer.id):group_0(peer.id)), callback);
}
;
_.unbindDialogs_0 = function unbindDialogs(callback){
  $unsubscribe($getDialogsList(this.messenger.angularModule), callback);
}
;
_.unbindGroup_0 = function unbindGroup(gid, callback){
  $unsubscribe_0($getGroup(this.messenger, gid), callback);
}
;
_.unbindUser_0 = function unbindUser(uid, callback){
  $unsubscribe_0($getUser(this.messenger, uid), callback);
}
;
var Lim_actor_model_js_JsFacade_2_classLit = createForClass('im.actor.model.js', 'JsFacade', 520, Ljava_lang_Object_2_classLit);
function $onError(this$static){
  $onError_3(this$static.val$error3, 'INTERNAL_ERROR', 'Internal error', false, convert(this$static.this$01.messenger.modules.auth.state));
}

function $onResult(this$static, res){
  $onResult_4(this$static.val$success2, convert(res));
}

function JsFacade$1(this$0, val$success, val$error){
  this.this$01 = this$0;
  this.val$success2 = val$success;
  this.val$error3 = val$error;
}

defineClass(523, 1, {}, JsFacade$1);
var Lim_actor_model_js_JsFacade$1_2_classLit = createForClass('im.actor.model.js', 'JsFacade/1', 523, Ljava_lang_Object_2_classLit);
function JsFacade$2(this$0, val$error){
  this.this$01 = this$0;
  this.val$error2 = val$error;
}

defineClass(524, 1, $intern_20, JsFacade$2);
_.run = function run(){
  $onError_3(this.val$error2, 'PHONE_NUMBER_INVALID', 'Invalid phone number', false, convert(this.this$01.messenger.modules.auth.state));
}
;
var Lim_actor_model_js_JsFacade$2_2_classLit = createForClass('im.actor.model.js', 'JsFacade/2', 524, Ljava_lang_Object_2_classLit);
function $onError_0(this$static){
  $onError_3(this$static.val$error3, 'INTERNAL_ERROR', 'Internal error', false, convert(this$static.this$01.messenger.modules.auth.state));
}

function $onResult_0(this$static, res){
  $onResult_4(this$static.val$success2, convert(res));
}

function JsFacade$3(this$0, val$success, val$error){
  this.this$01 = this$0;
  this.val$success2 = val$success;
  this.val$error3 = val$error;
}

defineClass(525, 1, {}, JsFacade$3);
var Lim_actor_model_js_JsFacade$3_2_classLit = createForClass('im.actor.model.js', 'JsFacade/3', 525, Ljava_lang_Object_2_classLit);
function JsFacade$4(this$0, val$error){
  this.this$01 = this$0;
  this.val$error2 = val$error;
}

defineClass(526, 1, $intern_20, JsFacade$4);
_.run = function run_0(){
  $onError_3(this.val$error2, 'PHONE_CODE_INVALID', 'Invalid code number', false, convert(this.this$01.messenger.modules.auth.state));
}
;
var Lim_actor_model_js_JsFacade$4_2_classLit = createForClass('im.actor.model.js', 'JsFacade/4', 526, Ljava_lang_Object_2_classLit);
function $onError_1(this$static){
  $invoke_3(this$static.val$error3.jso);
}

function $onResult_1(this$static){
  $invoke_3(this$static.val$success2.jso);
}

function JsFacade$5(val$success, val$error){
  this.val$success2 = val$success;
  this.val$error3 = val$error;
}

defineClass(527, 1, {}, JsFacade$5);
var Lim_actor_model_js_JsFacade$5_2_classLit = createForClass('im.actor.model.js', 'JsFacade/5', 527, Ljava_lang_Object_2_classLit);
function $onError_2(this$static){
  $invoke_3(this$static.val$error3.jso);
}

function $onResult_2(this$static){
  $invoke_3(this$static.val$success2.jso);
}

function JsFacade$6(val$success, val$error){
  this.val$success2 = val$success;
  this.val$error3 = val$error;
}

defineClass(528, 1, {}, JsFacade$6);
var Lim_actor_model_js_JsFacade$6_2_classLit = createForClass('im.actor.model.js', 'JsFacade/6', 528, Ljava_lang_Object_2_classLit);
function $export(){
  if (!exported) {
    exported = true;
    $export0();
  }
}

function $export0(){
  var pkg = declarePackage('actor.ActorApp');
  var __0;
  $wnd.actor.ActorApp = $entry(function(){
    var g, j = this;
    isAssignableToInstance(Lim_actor_model_js_JsFacade_2_classLit, arguments)?(g = arguments[0]):arguments.length == 0 && (g = new JsFacade);
    j.g = g;
    setWrapper(g, j);
    return j;
  }
  );
  __0 = $wnd.actor.ActorApp.prototype = new Object;
  __0.bindChat = $entry(function(a0, a1){
    this.g.bindChat_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.angular.AngularListCallback?a1.g:makeClosure(a1));
  }
  );
  __0.bindDialogs = $entry(function(a0){
    this.g.bindDialogs_0(a0 == null?null:a0.constructor == $wnd.im.actor.model.js.angular.AngularListCallback?a0.g:makeClosure(a0));
  }
  );
  __0.bindGroup = $entry(function(a0, a1){
    this.g.bindGroup_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback?a1.g:makeClosure_0(a1));
  }
  );
  __0.bindUser = $entry(function(a0, a1){
    this.g.bindUser_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback?a1.g:makeClosure_0(a1));
  }
  );
  __0.clearChat = $entry(function(a0, a1, a2){
    this.g.clearChat_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.entity.JsClosure?a1.g:makeClosure_3(a1), a2 == null?null:a2.constructor == $wnd.im.actor.model.js.entity.JsClosure?a2.g:makeClosure_3(a2));
  }
  );
  __0.deleteChat = $entry(function(a0, a1, a2){
    this.g.deleteChat_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.entity.JsClosure?a1.g:makeClosure_3(a1), a2 == null?null:a2.constructor == $wnd.im.actor.model.js.entity.JsClosure?a2.g:makeClosure_3(a2));
  }
  );
  __0.getAuthPhone = $entry(function(){
    return this.g.getAuthPhone_0();
  }
  );
  __0.getAuthState = $entry(function(){
    return this.g.getAuthState_0();
  }
  );
  __0.getGroup = $entry(function(a0){
    return this.g.getGroup_0(a0);
  }
  );
  __0.getUid = $entry(function(){
    return this.g.getUid_0();
  }
  );
  __0.getUser = $entry(function(a0){
    return this.g.getUser_0(a0);
  }
  );
  __0.isLoggedIn = $entry(function(){
    return this.g.isLoggedIn_0();
  }
  );
  __0.loadDraft = $entry(function(a0){
    return this.g.loadDraft_0(a0);
  }
  );
  __0.onAppHidden = $entry(function(){
    this.g.onAppHidden_0();
  }
  );
  __0.onAppVisible = $entry(function(){
    this.g.onAppVisible_0();
  }
  );
  __0.onConversationClosed = $entry(function(a0){
    this.g.onConversationClosed_0(a0);
  }
  );
  __0.onConversationOpen = $entry(function(a0){
    this.g.onConversationOpen_0(a0);
  }
  );
  __0.onDialogsClosed = $entry(function(){
    this.g.onDialogsClosed_0();
  }
  );
  __0.onDialogsOpen = $entry(function(){
    this.g.onDialogsOpen_0();
  }
  );
  __0.onProfileClosed = $entry(function(a0){
    this.g.onProfileClosed_0(a0);
  }
  );
  __0.onProfileOpen = $entry(function(a0){
    this.g.onProfileOpen_0(a0);
  }
  );
  __0.onTyping = $entry(function(a0){
    this.g.onTyping_0(a0);
  }
  );
  __0.requestSms = $entry(function(a0, a1, a2){
    this.g.requestSms_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.entity.JsAuthSuccessClosure?a1.g:makeClosure_2(a1), a2 == null?null:a2.constructor == $wnd.im.actor.model.js.entity.JsAuthErrorClosure?a2.g:makeClosure_1(a2));
  }
  );
  __0.saveDraft = $entry(function(a0, a1){
    this.g.saveDraft_0(a0, a1);
  }
  );
  __0.sendCode = $entry(function(a0, a1, a2){
    this.g.sendCode_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.entity.JsAuthSuccessClosure?a1.g:makeClosure_2(a1), a2 == null?null:a2.constructor == $wnd.im.actor.model.js.entity.JsAuthErrorClosure?a2.g:makeClosure_1(a2));
  }
  );
  __0.sendMessage = $entry(function(a0, a1){
    this.g.sendMessage_0(a0, a1);
  }
  );
  __0.unbindChat = $entry(function(a0, a1){
    this.g.unbindChat_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.angular.AngularListCallback?a1.g:makeClosure(a1));
  }
  );
  __0.unbindDialogs = $entry(function(a0){
    this.g.unbindDialogs_0(a0 == null?null:a0.constructor == $wnd.im.actor.model.js.angular.AngularListCallback?a0.g:makeClosure(a0));
  }
  );
  __0.unbindGroup = $entry(function(a0, a1){
    this.g.unbindGroup_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback?a1.g:makeClosure_0(a1));
  }
  );
  __0.unbindUser = $entry(function(a0, a1){
    this.g.unbindUser_0(a0, a1 == null?null:a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback?a1.g:makeClosure_0(a1));
  }
  );
  addTypeMap(Lim_actor_model_js_JsFacade_2_classLit, $wnd.actor.ActorApp);
  if (pkg)
    for (p in pkg)
      $wnd.actor.ActorApp[p] === undefined && ($wnd.actor.ActorApp[p] = pkg[p]);
}

function JsFacadeExporterImpl(){
  $export();
}

defineClass(519, 1, {}, JsFacadeExporterImpl);
var exported = false;
var Lim_actor_model_js_JsFacadeExporterImpl_2_classLit = createForClass('im.actor.model.js', 'JsFacadeExporterImpl', 519, Ljava_lang_Object_2_classLit);
function $buildPeerInfo(this$static, peer){
  var groupVM, userVM;
  if (peer.peerType == ($clinit_PeerType_0() , PRIVATE_0)) {
    userVM = dynamicCast($get_1($getUsers(this$static), fromInt(peer.peerId)), 53);
    return {peer:create_5(peer), title:dynamicCastToString(userVM.name_0.value_0), avatar:$getSmallAvatarUrl(this$static, dynamicCast(userVM.avatar.value_0, 60)), placeholder:getPlaceholder(peer.peerId)};
  }
   else if (peer.peerType == GROUP_0) {
    groupVM = dynamicCast($get_1($getGroups(this$static), fromInt(peer.peerId)), 94);
    return {peer:create_5(peer), title:dynamicCastToString(groupVM.name_0.value_0), avatar:$getSmallAvatarUrl(this$static, dynamicCast(groupVM.avatar.value_0, 60)), placeholder:getPlaceholder(peer.peerId)};
  }
   else {
    throw new RuntimeException;
  }
}

function $getConversationList(this$static, peer){
  return $getMessagesList(this$static.angularModule, peer);
}

function $getFileUrl(this$static, fileReference){
  return $getFileUrl_0(this$static.angularFilesModule, fileReference.fileId, fileReference.accessHash);
}

function $getGroup(this$static, gid){
  return $getGroup_0(this$static.angularModule, gid);
}

function $getSmallAvatarUrl(this$static, avatar){
  if (!!avatar && !!avatar.smallImage) {
    return $getFileUrl(this$static, avatar.smallImage.fileReference);
  }
  return null;
}

function $getUser(this$static, uid){
  return $getUser_0(this$static.angularModule, uid);
}

function JsMessenger(configuration){
  var timing;
  setLog(configuration.log_0);
  setThreadingProvider(configuration.threadingProvider);
  setDispatcherProvider(configuration.dispatcherProvider);
  timing = new Timing('MESSENGER_INIT');
  $section(timing, 'Crypto');
  $clinit_CryptoUtils();
  $section(timing, 'MVVM');
  $section(timing, 'Actors');
  $setTraceInterface(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new ActorTrace);
  $addDispatcher_0((null , mainSystem));
  $section(timing, 'Modules:Create');
  this.modules = new Modules(this, configuration);
  $section(timing, 'Modules:Run');
  $run_0(this.modules.auth);
  $end(timing);
  this.angularModule = new AngularModule(this, this.modules);
  this.angularFilesModule = new AngularFilesModule(this.modules);
}

defineClass(532, 531, {}, JsMessenger);
var Lim_actor_model_js_JsMessenger_2_classLit = createForClass('im.actor.model.js', 'JsMessenger', 532, Lim_actor_model_Messenger_2_classLit);
function $conversationActor(this$static, peer){
  return $getConversationActor(this$static.modules.messages, peer);
}

function $request(this$static, request, callback){
  $request_1(this$static.modules.actorApi, request, callback);
}

function $runOnUiThread(runnable){
  $scheduleDeferred(($clinit_SchedulerImpl() , INSTANCE), new JsMainThreadProvider$1(runnable));
}

function BaseModule(modules){
  this.modules = modules;
}

defineClass(25, 1, {});
var Lim_actor_model_modules_BaseModule_2_classLit = createForClass('im.actor.model.modules', 'BaseModule', 25, Ljava_lang_Object_2_classLit);
function $getFileUrl_0(this$static, id_0, accessHash){
  var cachedFileUrl;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AngularFilesModule' + ':' + ('Get file url for #' + toString_2(id_0)));
  cachedFileUrl = dynamicCast($getValue(this$static.keyValueStorage, id_0), 251);
  if (cachedFileUrl) {
    return cachedFileUrl.url_0;
  }
  $requestFileUrl(this$static, id_0, accessHash);
  return null;
}

function $requestFileUrl(this$static, id_0, accessHash){
  if ($contains_0(this$static.requestedFiles, valueOf_0(id_0))) {
    return;
  }
  $add_1(this$static.requestedFiles, valueOf_0(id_0));
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AngularFilesModule' + ':' + ('Loading file #' + toString_2(id_0) + ' url...'));
  $request(this$static, new RequestGetFileUrl_0(new FileLocation_0(id_0, accessHash)), new AngularFilesModule$2(this$static, id_0));
}

function AngularFilesModule(modules){
  BaseModule.call(this, modules);
  this.requestedFiles = new HashSet;
  this.keyValueStorage = new AngularFilesModule$1($createKeyValue(modules.configuration.storageProvider, 'file_url_cache'));
}

defineClass(605, 25, {}, AngularFilesModule);
var Lim_actor_model_js_angular_AngularFilesModule_2_classLit = createForClass('im.actor.model.js.angular', 'AngularFilesModule', 605, Lim_actor_model_modules_BaseModule_2_classLit);
function $getValue(this$static, id_0){
  var data_0, res;
  if ($containsKey(this$static.cache, valueOf_0(id_0))) {
    return dynamicCast($get_2(this$static.cache, valueOf_0(id_0)), 116);
  }
  data_0 = $getValue_0(this$static.storage, id_0);
  if (data_0 != null) {
    res = $deserialize(data_0);
    $put_1(this$static.cache, valueOf_0(res.fid), res);
    return res;
  }
   else {
    return null;
  }
}

function BaseKeyValueEngine(storage){
  this.cache = new HashMap;
  this.storage = storage;
}

defineClass(447, 1, {});
var Lim_actor_model_modules_utils_BaseKeyValueEngine_2_classLit = createForClass('im.actor.model.modules.utils', 'BaseKeyValueEngine', 447, Ljava_lang_Object_2_classLit);
function $deserialize(data_0){
  var e;
  try {
    return dynamicCast(parse_159(new CachedFileUrl, data_0), 251);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      return null;
    }
     else 
      throw unwrap($e0);
  }
}

function AngularFilesModule$1($anonymous0){
  BaseKeyValueEngine.call(this, $anonymous0);
}

defineClass(606, 447, {}, AngularFilesModule$1);
var Lim_actor_model_js_angular_AngularFilesModule$1_2_classLit = createForClass('im.actor.model.js.angular', 'AngularFilesModule/1', 606, Lim_actor_model_modules_utils_BaseKeyValueEngine_2_classLit);
function $onResult_3(this$static, response){
  $remove_5(this$static.this$01.requestedFiles, valueOf_0(this$static.val$id2));
  d_0('AngularFilesModule', 'File #' + toString_2(this$static.val$id2) + ' url loaded: ' + response.url_0);
}

function AngularFilesModule$2(this$0, val$id){
  this.this$01 = this$0;
  this.val$id2 = val$id;
}

defineClass(607, 1, {}, AngularFilesModule$2);
_.onError_0 = function onError(e){
  $remove_5(this.this$01.requestedFiles, valueOf_0(this.val$id2));
  d_0('AngularFilesModule', 'File #' + toString_2(this.val$id2) + ' url load error');
}
;
_.onResult_0 = function onResult(response){
  $onResult_3(this, dynamicCast(response, 320));
}
;
_.val$id2 = {l:0, m:0, h:0};
var Lim_actor_model_js_angular_AngularFilesModule$2_2_classLit = createForClass('im.actor.model.js.angular', 'AngularFilesModule/2', 607, Ljava_lang_Object_2_classLit);
function $clear_0(array){
  array.splice(0, array.length);
}

function $insert(array, index_0, obj){
  array.splice(index_0, 0, obj);
}

function $notifySubscribers(this$static){
  var callback, callback$iterator;
  for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
    callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 802));
    $onCollectionChanged(callback, this$static.jsValues);
  }
}

function $onClear(this$static){
  this$static.values.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
  $clear_0(this$static.jsValues);
  $notifySubscribers(this$static);
}

function $onItemAddedOrUpdated(this$static, item_0){
  var i_0, i0, id_0, sortKey;
  try {
    id_0 = item_0.getEngineId();
    sortKey = item_0.getEngineSort();
    for (i0 = 0; i0 < this$static.values.array.length; i0++) {
      if (eq(dynamicCast($get_3(this$static.values, i0), 149).getEngineId(), id_0)) {
        this$static.values.remove_1(i0);
        $remove(this$static.jsValues, i0);
        break;
      }
    }
    if (this$static.isInverted) {
      for (i_0 = this$static.values.array.length - 1; i_0 >= 0; i_0--) {
        if (gt(dynamicCast($get_3(this$static.values, i_0), 149).getEngineSort(), sortKey)) {
          $add(this$static.values, i_0, item_0);
          $insert(this$static.jsValues, i_0, this$static.entityConverter.convert(item_0, this$static.messenger));
          return;
        }
      }
    }
     else {
      for (i_0 = 0; i_0 < this$static.values.array.length; i_0++) {
        if (lt(dynamicCast($get_3(this$static.values, i_0), 149).getEngineSort(), sortKey)) {
          $add(this$static.values, i_0, item_0);
          $insert(this$static.jsValues, i_0, this$static.entityConverter.convert(item_0, this$static.messenger));
          return;
        }
      }
    }
    $add_0(this$static.values, item_0);
    $push(this$static.jsValues, this$static.entityConverter.convert(item_0, this$static.messenger));
  }
   finally {
    $notifySubscribers(this$static);
  }
}

function $onItemRemoved(this$static, id_0){
  var i_0;
  for (i_0 = 0; i_0 < this$static.values.array.length; i_0++) {
    if (eq(dynamicCast($get_3(this$static.values, i_0), 149).getEngineId(), id_0)) {
      this$static.values.remove_1(i_0);
      $remove(this$static.jsValues, i_0);
      break;
    }
  }
  $notifySubscribers(this$static);
}

function $remove(array, index_0){
  array.splice(index_0, 1);
}

function $subscribe(this$static, callback){
  $indexOf_0(this$static.callbacks, callback, 0) != -1 || $add_0(this$static.callbacks, callback);
  $onCollectionChanged(callback, this$static.jsValues);
}

function $unsubscribe(this$static, callback){
  $remove_4(this$static.callbacks, callback);
}

function AngularList(listEngine, isInverted, entityConverter, messenger){
  var item_0, rid, rid$index, rid$max, rids;
  this.callbacks = new ArrayList;
  this.messenger = messenger;
  this.entityConverter = entityConverter;
  this.isInverted = isInverted;
  this.values = new ArrayList;
  this.jsValues = [];
  rids = $getOrderedIds(listEngine.storage);
  for (rid$index = 0 , rid$max = rids.length; rid$index < rid$max; ++rid$index) {
    rid = rids[rid$index];
    item_0 = $getValue_1(listEngine, rid);
    if (isInverted) {
      $insert(this.jsValues, 0, entityConverter.convert(item_0, messenger));
      $add(this.values, 0, item_0);
    }
     else {
      $add_0(this.values, item_0);
      $push(this.jsValues, entityConverter.convert(item_0, messenger));
    }
  }
  $indexOf_0(listEngine.callbacks, this, 0) != -1 || $add_0(listEngine.callbacks, this);
}

defineClass(225, 1, {225:1, 150:1}, AngularList);
_.isInverted = false;
var Lim_actor_model_js_angular_AngularList_2_classLit = createForClass('im.actor.model.js.angular', 'AngularList', 225, Ljava_lang_Object_2_classLit);
var Lim_actor_model_js_angular_AngularListCallback_2_classLit = createForInterface('im.actor.model.js.angular', 'AngularListCallback');
function $export_0(){
  if (!exported_0) {
    exported_0 = true;
    $export0_0();
  }
}

function $export0_0(){
  var pkg = declarePackage('im.actor.model.js.angular.AngularListCallback');
  var __0;
  $wnd.im.actor.model.js.angular.AngularListCallback = $entry(function(){
    var g, j = this;
    isAssignableToInstance(Lim_actor_model_js_angular_AngularListCallback_2_classLit, arguments) && (g = arguments[0]);
    j.g = g;
    setWrapper(g, j);
    return j;
  }
  );
  __0 = $wnd.im.actor.model.js.angular.AngularListCallback.prototype = new Object;
  __0.onCollectionChanged = $entry(function(a0){
    this.g.onCollectionChanged_0(a0);
  }
  );
  addTypeMap(Lim_actor_model_js_angular_AngularListCallback_2_classLit, $wnd.im.actor.model.js.angular.AngularListCallback);
  if (pkg)
    for (p in pkg)
      $wnd.im.actor.model.js.angular.AngularListCallback[p] === undefined && ($wnd.im.actor.model.js.angular.AngularListCallback[p] = pkg[p]);
}

function $invoke(closure, a0){
  closure.apply(a0, [a0]);
}

function $onCollectionChanged(this$static, a0){
  $invoke(this$static.jso, a0);
}

function AngularListCallbackExporterImpl(){
  $export_0();
}

function AngularListCallbackExporterImpl_0(jso){
  this.jso = jso;
}

function makeClosure(closure){
  return new AngularListCallbackExporterImpl_0(closure);
}

defineClass(155, 1, {802:1, 155:1, 222:1}, AngularListCallbackExporterImpl, AngularListCallbackExporterImpl_0);
_.equals$ = function equals_6(obj){
  return obj != null && instanceOf(obj, 155) && this.jso == dynamicCast(obj, 155).jso;
}
;
_.onCollectionChanged_0 = function onCollectionChanged(a0){
  $onCollectionChanged(this, a0);
}
;
var exported_0 = false;
var Lim_actor_model_js_angular_AngularListCallbackExporterImpl_2_classLit = createForClass('im.actor.model.js.angular', 'AngularListCallbackExporterImpl', 155, Ljava_lang_Object_2_classLit);
function $getDialogsList(this$static){
  !this$static.dialogsList && (this$static.dialogsList = new AngularList(this$static.modules.messages.dialogs, false, ($clinit_JsDialog() , CONVERTER), this$static.messenger));
  return this$static.dialogsList;
}

function $getGroup_0(this$static, gid){
  var groupVM, value_0;
  if (!$containsKey(this$static.groups, valueOf(gid))) {
    groupVM = dynamicCast($get_1(this$static.modules.groups.collection, fromInt(gid)), 94);
    value_0 = new AngularValue({uid:groupVM.id_0, title:dynamicCastToString(groupVM.name_0.value_0), adminId:groupVM.creatorId});
    $subscribe_3(groupVM, new AngularModule$2(value_0));
    $put_1(this$static.groups, valueOf(gid), value_0);
  }
  return dynamicCast($get_2(this$static.groups, valueOf(gid)), 161);
}

function $getMessagesList(this$static, peer){
  $containsKey(this$static.messagesList, peer) || $put_1(this$static.messagesList, peer, new AngularList($getConversationEngine(this$static.modules.messages, peer), true, ($clinit_JsMessage() , CONVERTER_0), this$static.messenger));
  return dynamicCast($get_2(this$static.messagesList, peer), 225);
}

function $getUser_0(this$static, uid){
  var userVM, value_0;
  if (!$containsKey(this$static.users, valueOf(uid))) {
    userVM = dynamicCast($get_1(this$static.modules.users.collection, fromInt(uid)), 53);
    value_0 = new AngularValue({uid:userVM.id_0, name:dynamicCastToString(userVM.name_0.value_0), isContact:dynamicCast(userVM.isContact.value_0, 51).value_0});
    $subscribe_4(userVM, new AngularModule$1(value_0));
    $put_1(this$static.users, valueOf(uid), value_0);
  }
  return dynamicCast($get_2(this$static.users, valueOf(uid)), 161);
}

function AngularModule(messenger, modules){
  BaseModule.call(this, modules);
  this.messagesList = new HashMap;
  this.messenger = messenger;
  this.users = new HashMap;
  this.groups = new HashMap;
}

defineClass(537, 25, {}, AngularModule);
var Lim_actor_model_js_angular_AngularModule_2_classLit = createForClass('im.actor.model.js.angular', 'AngularModule', 537, Lim_actor_model_modules_BaseModule_2_classLit);
function $onChanged(this$static, model){
  $changeValue(this$static.val$value2, {uid:model.id_0, name:dynamicCastToString(model.name_0.value_0), isContact:dynamicCast(model.isContact.value_0, 51).value_0});
}

function AngularModule$1(val$value){
  this.val$value2 = val$value;
}

defineClass(538, 1, {471:1}, AngularModule$1);
_.onChanged_0 = function onChanged(model){
  $onChanged(this, dynamicCast(model, 53));
}
;
var Lim_actor_model_js_angular_AngularModule$1_2_classLit = createForClass('im.actor.model.js.angular', 'AngularModule/1', 538, Ljava_lang_Object_2_classLit);
function $onChanged_0(this$static, model){
  $changeValue(this$static.val$value2, {uid:model.id_0, title:dynamicCastToString(model.name_0.value_0), adminId:model.creatorId});
}

function AngularModule$2(val$value){
  this.val$value2 = val$value;
}

defineClass(539, 1, {471:1}, AngularModule$2);
_.onChanged_0 = function onChanged_0(model){
  $onChanged_0(this, dynamicCast(model, 94));
}
;
var Lim_actor_model_js_angular_AngularModule$2_2_classLit = createForClass('im.actor.model.js.angular', 'AngularModule/2', 539, Ljava_lang_Object_2_classLit);
function $changeValue(this$static, value_0){
  var callback, callback$iterator;
  this$static.value_0 = value_0;
  for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
    callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 803));
    $invoke_0(callback.jso, value_0);
  }
}

function $subscribe_0(this$static, callback){
  if ($indexOf_0(this$static.callbacks, callback, 0) == -1) {
    $add_0(this$static.callbacks, callback);
    $onChanged_1(callback, this$static.value_0);
  }
}

function $unsubscribe_0(this$static, callback){
  $remove_4(this$static.callbacks, callback);
}

function AngularValue(value_0){
  this.callbacks = new ArrayList;
  this.value_0 = value_0;
}

defineClass(161, 1, {161:1}, AngularValue);
var Lim_actor_model_js_angular_AngularValue_2_classLit = createForClass('im.actor.model.js.angular', 'AngularValue', 161, Ljava_lang_Object_2_classLit);
var Lim_actor_model_js_angular_AngularValueCallback_2_classLit = createForInterface('im.actor.model.js.angular', 'AngularValueCallback');
function $export_1(){
  if (!exported_1) {
    exported_1 = true;
    $export0_1();
  }
}

function $export0_1(){
  var pkg = declarePackage('im.actor.model.js.angular.AngularValueCallback');
  var __0;
  $wnd.im.actor.model.js.angular.AngularValueCallback = $entry(function(){
    var g, j = this;
    isAssignableToInstance(Lim_actor_model_js_angular_AngularValueCallback_2_classLit, arguments) && (g = arguments[0]);
    j.g = g;
    setWrapper(g, j);
    return j;
  }
  );
  __0 = $wnd.im.actor.model.js.angular.AngularValueCallback.prototype = new Object;
  __0.onChanged = $entry(function(a0){
    runDispatch(this.g, Lim_actor_model_js_angular_AngularValueCallback_2_classLit, 0, arguments, false, false)[0];
  }
  );
  addTypeMap(Lim_actor_model_js_angular_AngularValueCallback_2_classLit, $wnd.im.actor.model.js.angular.AngularValueCallback);
  if (pkg)
    for (p in pkg)
      $wnd.im.actor.model.js.angular.AngularValueCallback[p] === undefined && ($wnd.im.actor.model.js.angular.AngularValueCallback[p] = pkg[p]);
}

function $invoke_0(closure, a0){
  closure.apply(null, [a0]);
}

function $onChanged_1(this$static, a0){
  $invoke_0(this$static.jso, a0);
}

function AngularValueCallbackExporterImpl(){
  $export_1();
}

function AngularValueCallbackExporterImpl_0(jso){
  this.jso = jso;
}

function makeClosure_0(closure){
  return new AngularValueCallbackExporterImpl_0(closure);
}

defineClass(156, 1, {803:1, 156:1, 222:1}, AngularValueCallbackExporterImpl, AngularValueCallbackExporterImpl_0);
_.equals$ = function equals_7(obj){
  return obj != null && instanceOf(obj, 156) && this.jso == dynamicCast(obj, 156).jso;
}
;
var exported_1 = false;
var Lim_actor_model_js_angular_AngularValueCallbackExporterImpl_2_classLit = createForClass('im.actor.model.js.angular', 'AngularValueCallbackExporterImpl', 156, Ljava_lang_Object_2_classLit);
function CachedFileUrl(){
}

defineClass(251, 4, {4:1, 116:1, 251:1}, CachedFileUrl);
_.getEngineId = function getEngineId_6(){
  return this.fid;
}
;
_.parse_0 = function parse_188(values){
  this.fid = fromInt(convertInt($getLong(values, 1)));
  this.url_0 = convertString($getBytes(values, 2));
}
;
_.serialize = function serialize_179(writer){
  $writeVarIntField(writer, 1, this.fid);
  $writeString(writer, 2, this.url_0);
}
;
_.fid = {l:0, m:0, h:0};
var Lim_actor_model_js_angular_entity_CachedFileUrl_2_classLit = createForClass('im.actor.model.js.angular.entity', 'CachedFileUrl', 251, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function convert(state){
  switch (state.ordinal) {
    default:case 0:
      return 'start';
    case 1:
      return 'code';
    case 2:
      return 'signup';
    case 3:
      return 'logged_in';
  }
}

function convert_0(state){
  switch (state.ordinal) {
    default:case 5:
      return 'unknown';
    case 0:
      return 'pending';
    case 1:
      return 'sent';
    case 4:
      return 'error';
    case 3:
      return 'read';
    case 2:
      return 'received';
  }
}

var Lim_actor_model_js_entity_JsAuthErrorClosure_2_classLit = createForInterface('im.actor.model.js.entity', 'JsAuthErrorClosure');
function $export_2(){
  if (!exported_2) {
    exported_2 = true;
    $export0_2();
  }
}

function $export0_2(){
  var pkg = declarePackage('im.actor.model.js.entity.JsAuthErrorClosure');
  var __0;
  $wnd.im.actor.model.js.entity.JsAuthErrorClosure = $entry(function(){
    var g, j = this;
    isAssignableToInstance(Lim_actor_model_js_entity_JsAuthErrorClosure_2_classLit, arguments) && (g = arguments[0]);
    j.g = g;
    setWrapper(g, j);
    return j;
  }
  );
  __0 = $wnd.im.actor.model.js.entity.JsAuthErrorClosure.prototype = new Object;
  __0.onError = $entry(function(a0, a1, a2, a3){
    this.g.onError_1(a0, a1, a2, a3);
  }
  );
  addTypeMap(Lim_actor_model_js_entity_JsAuthErrorClosure_2_classLit, $wnd.im.actor.model.js.entity.JsAuthErrorClosure);
  if (pkg)
    for (p in pkg)
      $wnd.im.actor.model.js.entity.JsAuthErrorClosure[p] === undefined && ($wnd.im.actor.model.js.entity.JsAuthErrorClosure[p] = pkg[p]);
}

function $invoke_1(closure, a0, a1, a2, a3){
  closure.apply(null, [a0, a1, a2, a3]);
}

function $onError_3(this$static, a0, a1, a2, a3){
  $invoke_1(this$static.jso, a0, a1, a2, a3);
}

function JsAuthErrorClosureExporterImpl(){
  $export_2();
}

function JsAuthErrorClosureExporterImpl_0(jso){
  this.jso = jso;
}

function makeClosure_1(closure){
  return new JsAuthErrorClosureExporterImpl_0(closure);
}

defineClass(157, 1, {157:1, 222:1}, JsAuthErrorClosureExporterImpl, JsAuthErrorClosureExporterImpl_0);
_.equals$ = function equals_8(obj){
  return obj != null && instanceOf(obj, 157) && this.jso == dynamicCast(obj, 157).jso;
}
;
_.onError_1 = function onError_0(a0, a1, a2, a3){
  $onError_3(this, a0, a1, a2, a3);
}
;
var exported_2 = false;
var Lim_actor_model_js_entity_JsAuthErrorClosureExporterImpl_2_classLit = createForClass('im.actor.model.js.entity', 'JsAuthErrorClosureExporterImpl', 157, Ljava_lang_Object_2_classLit);
var Lim_actor_model_js_entity_JsAuthSuccessClosure_2_classLit = createForInterface('im.actor.model.js.entity', 'JsAuthSuccessClosure');
function $export_3(){
  if (!exported_3) {
    exported_3 = true;
    $export0_3();
  }
}

function $export0_3(){
  var pkg = declarePackage('im.actor.model.js.entity.JsAuthSuccessClosure');
  var __0;
  $wnd.im.actor.model.js.entity.JsAuthSuccessClosure = $entry(function(){
    var g, j = this;
    isAssignableToInstance(Lim_actor_model_js_entity_JsAuthSuccessClosure_2_classLit, arguments) && (g = arguments[0]);
    j.g = g;
    setWrapper(g, j);
    return j;
  }
  );
  __0 = $wnd.im.actor.model.js.entity.JsAuthSuccessClosure.prototype = new Object;
  __0.onResult = $entry(function(a0){
    this.g.onResult_1(a0);
  }
  );
  addTypeMap(Lim_actor_model_js_entity_JsAuthSuccessClosure_2_classLit, $wnd.im.actor.model.js.entity.JsAuthSuccessClosure);
  if (pkg)
    for (p in pkg)
      $wnd.im.actor.model.js.entity.JsAuthSuccessClosure[p] === undefined && ($wnd.im.actor.model.js.entity.JsAuthSuccessClosure[p] = pkg[p]);
}

function $invoke_2(closure, a0){
  closure.apply(null, [a0]);
}

function $onResult_4(this$static, a0){
  $invoke_2(this$static.jso, a0);
}

function JsAuthSuccessClosureExporterImpl(){
  $export_3();
}

function JsAuthSuccessClosureExporterImpl_0(jso){
  this.jso = jso;
}

function makeClosure_2(closure){
  return new JsAuthSuccessClosureExporterImpl_0(closure);
}

defineClass(158, 1, {158:1, 222:1}, JsAuthSuccessClosureExporterImpl, JsAuthSuccessClosureExporterImpl_0);
_.equals$ = function equals_9(obj){
  return obj != null && instanceOf(obj, 158) && this.jso == dynamicCast(obj, 158).jso;
}
;
_.onResult_1 = function onResult_0(a0){
  $onResult_4(this, a0);
}
;
var exported_3 = false;
var Lim_actor_model_js_entity_JsAuthSuccessClosureExporterImpl_2_classLit = createForClass('im.actor.model.js.entity', 'JsAuthSuccessClosureExporterImpl', 158, Ljava_lang_Object_2_classLit);
var Lim_actor_model_js_entity_JsClosure_2_classLit = createForInterface('im.actor.model.js.entity', 'JsClosure');
function $export_4(){
  if (!exported_4) {
    exported_4 = true;
    $export0_4();
  }
}

function $export0_4(){
  var pkg = declarePackage('im.actor.model.js.entity.JsClosure');
  var __0;
  $wnd.im.actor.model.js.entity.JsClosure = $entry(function(){
    var g, j = this;
    isAssignableToInstance(Lim_actor_model_js_entity_JsClosure_2_classLit, arguments) && (g = arguments[0]);
    j.g = g;
    setWrapper(g, j);
    return j;
  }
  );
  __0 = $wnd.im.actor.model.js.entity.JsClosure.prototype = new Object;
  __0.callback = $entry(function(){
    this.g.callback_1();
  }
  );
  addTypeMap(Lim_actor_model_js_entity_JsClosure_2_classLit, $wnd.im.actor.model.js.entity.JsClosure);
  if (pkg)
    for (p in pkg)
      $wnd.im.actor.model.js.entity.JsClosure[p] === undefined && ($wnd.im.actor.model.js.entity.JsClosure[p] = pkg[p]);
}

function $invoke_3(closure){
  closure.apply(null, []);
}

function JsClosureExporterImpl(){
  $export_4();
}

function JsClosureExporterImpl_0(jso){
  this.jso = jso;
}

function makeClosure_3(closure){
  return new JsClosureExporterImpl_0(closure);
}

defineClass(159, 1, {159:1, 222:1}, JsClosureExporterImpl, JsClosureExporterImpl_0);
_.callback_1 = function callback_0(){
  $invoke_3(this.jso);
}
;
_.equals$ = function equals_10(obj){
  return obj != null && instanceOf(obj, 159) && this.jso == dynamicCast(obj, 159).jso;
}
;
var exported_4 = false;
var Lim_actor_model_js_entity_JsClosureExporterImpl_2_classLit = createForClass('im.actor.model.js.entity', 'JsClosureExporterImpl', 159, Ljava_lang_Object_2_classLit);
function $clinit_JsDialog(){
  $clinit_JsDialog = emptyMethod;
  CONVERTER = new JsDialog$1;
}

function create_4(peer, date, sender, showSender, text_0, isHighlighted, state, counter){
  $clinit_JsDialog();
  return {peer:peer, text:text_0, date:date, sender:sender, showSender:showSender, isHighlighted:isHighlighted, state:state, counter:counter};
}

var CONVERTER;
function $convert(src_0, messenger){
  var date, highlightContent, messageText, peerInfo, senderName, showSender;
  showSender = false;
  src_0.peer.peerType == ($clinit_PeerType_0() , GROUP_0) && src_0.messageType != ($clinit_ContentType() , SERVICE) && (showSender = true);
  senderName = null;
  showSender && (senderName = dynamicCastToString(dynamicCast($get_1($getUsers(messenger), fromInt(src_0.senderId)), 53).name_0.value_0));
  date = $formatShortDate(messenger.modules.i18nEngine, src_0.date);
  highlightContent = src_0.messageType != ($clinit_ContentType() , TEXT_0);
  messageText = $formatContentDialogText(messenger.modules.i18nEngine, src_0.senderId, src_0.messageType, src_0.text_0, src_0.relatedUid);
  peerInfo = {peer:create_5(src_0.peer), title:src_0.dialogTitle, avatar:null, placeholder:getPlaceholder(src_0.peer.peerId)};
  return create_4(peerInfo, date, senderName, showSender, messageText, highlightContent, convert_0(src_0.status_0), src_0.unreadCount);
}

function JsDialog$1(){
}

defineClass(609, 1, {}, JsDialog$1);
_.convert = function convert_1(src_0, messenger){
  return $convert(dynamicCast(src_0, 49), messenger);
}
;
var Lim_actor_model_js_entity_JsDialog$1_2_classLit = createForClass('im.actor.model.js.entity', 'JsDialog/1', 609, Ljava_lang_Object_2_classLit);
function $clinit_JsMessage(){
  $clinit_JsMessage = emptyMethod;
  CONVERTER_0 = new JsMessage$1;
}

var CONVERTER_0;
function $convert_0(value_0, modules){
  var content_0, date, isOut, rid, sender;
  rid = toString_2(value_0.rid) + '';
  sender = $buildPeerInfo(modules, user_0(value_0.senderId));
  isOut = value_0.senderId == modules.modules.auth.myUid;
  date = $formatTime(modules.modules.i18nEngine, value_0.date);
  instanceOf(value_0.content_0, 55)?(content_0 = {content:'text', text:dynamicCast(value_0.content_0, 55).text_0}):instanceOf(value_0.content_0, 37)?(content_0 = {content:'service', text:$formatFullServiceMessage(modules.modules.i18nEngine, value_0.senderId, dynamicCast(value_0.content_0, 37))}):(content_0 = {content:'unsupported'});
  return $clinit_JsMessage() , {rid:rid, sender:sender, isOut:isOut, date:date, content:content_0};
}

function JsMessage$1(){
}

defineClass(608, 1, {}, JsMessage$1);
_.convert = function convert_2(value_0, modules){
  return $convert_0(dynamicCast(value_0, 26), modules);
}
;
var Lim_actor_model_js_entity_JsMessage$1_2_classLit = createForClass('im.actor.model.js.entity', 'JsMessage/1', 608, Ljava_lang_Object_2_classLit);
function create_5(peer){
  switch (peer.peerType.ordinal) {
    default:case 0:
      return create_6('user', peer.peerId, 'u' + peer.peerId);
    case 1:
      return create_6('group', peer.peerId, 'g' + peer.peerId);
  }
}

function create_6(peerType, peerId, peerKey){
  return {type:peerType, id:peerId, key:peerKey};
}

function getPlaceholder(id_0){
  var index_0;
  index_0 = (id_0 < 0?-id_0:id_0) % 7;
  return initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['lblue', 'blue', 'purple', 'red', 'orange', 'yellow', 'green'])[index_0];
}

function JsCryptoProvider(){
  $clinit_BouncyCastleProvider();
  new JsRandomProvider;
}

defineClass(600, 786, {}, JsCryptoProvider);
var Lim_actor_model_js_providers_JsCryptoProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsCryptoProvider', 600, Lim_actor_model_crypto_bouncycastle_BouncyCastleProvider_2_classLit);
function JsDispatcherProvider(){
}

defineClass(601, 1, {}, JsDispatcherProvider);
var Lim_actor_model_js_providers_JsDispatcherProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsDispatcherProvider', 601, Ljava_lang_Object_2_classLit);
function $applyFile(this$static, text_0){
  var kv, lines, s, s$index, s$max;
  lines = $split(text_0, '\n', 0);
  for (s$index = 0 , s$max = lines.length; s$index < s$max; ++s$index) {
    s = lines[s$index];
    s = $trim(s);
    if (s.length == 0) {
      continue;
    }
     else if ($equals_3(s.substr(0, 1), '#')) {
      continue;
    }
    kv = $split(s, '=', 2);
    $putStringValue(this$static.locale, $trim(kv[0]), $trim(kv[1]));
  }
}

function JsLocaleProvider(){
  this.locale = new HashMap;
  $applyFile(this, 'TimeShortYesterday=Yest\nTimeShortNow=Now\nTimeShortMinutes={minutes} min\nTimeShortHours={hours} hrs\n\nOnlineOn=online\nOnlineOff=offline\n\nOnlineNow=last seen just now\nOnlineLastSeenToday=last seen today at {time}\nOnlineLastSeenYesterday=last seen yesterday at {time}\nOnlineLastSeenDate=last seen {date}\nOnlineLastSeenDateTime=last seen {date} at {time}\n\nGroupOnline={count} online\nGroupMembers={count} members\n\nTyping=typing...\nTypingUser={user} is typing...\nTypingMultiple = {count} people are typing...\n\nFileB={bytes} B\nFileKb={kbytes} KB\nFileMb={mbytes} MB\nFileGb={gbytes} GB\n\nYou=You\nThee=You\n\nContentUnsupported=Unsupported content\nContentDocument=Document\nContentPhoto=Photo\nContentVideo=Video\n\nServiceRegistered=Joined Actor\nServiceRegisteredFull={name} joined Actor Network\nServiceNewDevice=Added new device\nServiceNewDeviceFull={name} added new device to it\'s account\nServiceGroupCreated={name} created the group\nServiceGroupCreatedFull={name} created the group "{title}"\nServiceGroupLeaved={name} left group\nServiceGroupAdded={name} added {name_added}\nServiceGroupKicked={name} kicked {name_kicked}\nServiceGroupTitle={name} changed the group name\nServiceGroupTitleFull={name} changed the group name to "{title}"\nServiceGroupAvatarChanged={name} changed the group photo\nServiceGroupAvatarRemoved={name} removed the group photo');
  $applyFile(this, 'JanShort=Jan\nFebShort=Feb\nMarShort=Mar\nAprShort=Apr\nMayShort=May\nJunShort=Jun\nJulShort=Jul\nAugShort=Aug\nSepShort=Sep\nOctShort=Oct\nNovShort=Nov\nDecShort=Dec\n\nJanFull=January\nFebFull=February\nMarFull=March\nAprFull=April\nMayFull=May\nJunFull=June\nJulFull=July\nAugFull=August\nSepFull=September\nOctFull=October\nNovFull=November\nDecFull=December');
}

defineClass(599, 1, {}, JsLocaleProvider);
var Lim_actor_model_js_providers_JsLocaleProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsLocaleProvider', 599, Ljava_lang_Object_2_classLit);
function $clinit_JsLogProvider(){
  $clinit_JsLogProvider = emptyMethod;
  dateTimeFormat = ($clinit_DateTimeFormat_0() , getFormat($getDateTimeFormatInfo(($clinit_LocaleInfo() , $clinit_LocaleInfo() , instance_0))));
}

function JsLogProvider(){
  $clinit_JsLogProvider();
}

function error_0(message){
  $clinit_JsLogProvider();
  window.console.error(message);
}

function log_1(message){
  $clinit_JsLogProvider();
  window.console.log(message);
}

function warn(message){
  $clinit_JsLogProvider();
  window.console.warn(message);
}

defineClass(597, 1, {}, JsLogProvider);
var dateTimeFormat;
var Lim_actor_model_js_providers_JsLogProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsLogProvider', 597, Ljava_lang_Object_2_classLit);
function $postToMainThread(runnable){
  $scheduleDeferred(($clinit_SchedulerImpl() , INSTANCE), new JsMainThreadProvider$1(runnable));
}

function JsMainThreadProvider(){
}

defineClass(598, 1, {}, JsMainThreadProvider);
var Lim_actor_model_js_providers_JsMainThreadProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsMainThreadProvider', 598, Ljava_lang_Object_2_classLit);
function JsMainThreadProvider$1(val$runnable){
  this.val$runnable2 = val$runnable;
}

defineClass(344, 1, {}, JsMainThreadProvider$1);
_.execute_0 = function execute_2(){
  this.val$runnable2.run();
}
;
var Lim_actor_model_js_providers_JsMainThreadProvider$1_2_classLit = createForClass('im.actor.model.js.providers', 'JsMainThreadProvider/1', 344, Ljava_lang_Object_2_classLit);
function $createConnection(this$static, connectionId, endpoint, callback, createCallback){
  var managedConnection;
  managedConnection = new ManagedConnection(connectionId, endpoint, callback, new ManagedNetworkProvider$1(this$static, createCallback));
  $add_0(this$static.pendingConnections, managedConnection);
}

defineClass(594, 1, {});
var Lim_actor_model_network_connection_ManagedNetworkProvider_2_classLit = createForClass('im.actor.model.network.connection', 'ManagedNetworkProvider', 594, Ljava_lang_Object_2_classLit);
function JsNetworkingProvider(){
  this.pendingConnections = new ArrayList;
}

defineClass(595, 594, {}, JsNetworkingProvider);
var Lim_actor_model_js_providers_JsNetworkingProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsNetworkingProvider', 595, Lim_actor_model_network_connection_ManagedNetworkProvider_2_classLit);
function $onNotification(messenger, topNotifications){
  var contentMessage, notification, peerTitle;
  notification = (checkElementIndex(0, topNotifications.array.length) , dynamicCast(topNotifications.array[0], 373));
  contentMessage = $formatContentDialogText(messenger.modules.i18nEngine, notification.sender, notification.contentDescription.contentType, notification.contentDescription.text_0, notification.contentDescription.relatedUser);
  if (notification.peer.peerType == ($clinit_PeerType_0() , PRIVATE_0)) {
    peerTitle = dynamicCastToString(dynamicCast($get_1($getUsers(messenger), fromInt(notification.peer.peerId)), 53).name_0.value_0);
  }
   else {
    peerTitle = dynamicCastToString(dynamicCast($get_1($getGroups(messenger), fromInt(notification.peer.peerId)), 94).name_0.value_0);
    contentMessage = dynamicCastToString(dynamicCast($get_1($getUsers(messenger), fromInt(notification.peer.peerId)), 53).name_0.value_0) + ': ' + contentMessage;
  }
  $showNotification(peerTitle, contentMessage);
}

function $showNotification(title_0, message){
  if (!Notification) {
    return;
  }
  if (Notification.permission !== 'granted') {
    Notification.requestPermission();
    return;
  }
  var notification = new Notification(title_0, {body:message});
}

function JsNotificationsProvider(){
}

defineClass(604, 1, {}, JsNotificationsProvider);
var Lim_actor_model_js_providers_JsNotificationsProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsNotificationsProvider', 604, Ljava_lang_Object_2_classLit);
function JsPhoneBookProvider(){
}

defineClass(602, 1, {}, JsPhoneBookProvider);
var Lim_actor_model_js_providers_JsPhoneBookProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsPhoneBookProvider', 602, Ljava_lang_Object_2_classLit);
function JsRandomProvider(){
  new Random;
}

defineClass(627, 1, {}, JsRandomProvider);
var Lim_actor_model_js_providers_JsRandomProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsRandomProvider', 627, Ljava_lang_Object_2_classLit);
function $createKeyValue(this$static, name_0){
  return new JsKeyValueStorage(name_0, this$static.storage);
}

function $createList(this$static, name_0){
  return new JsListStorage(name_0, this$static.storage);
}

function JsStorageProvider(){
  this.storage = (!localStorage_0 && ($clinit_Storage$StorageSupportDetector() , localStorageSupported) && (localStorage_0 = new Storage_0) , localStorage_0);
}

defineClass(603, 1, {}, JsStorageProvider);
var Lim_actor_model_js_providers_JsStorageProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsStorageProvider', 603, Ljava_lang_Object_2_classLit);
function JsThreadingProvider(){
}

defineClass(593, 1, {}, JsThreadingProvider);
var Lim_actor_model_js_providers_JsThreadingProvider_2_classLit = createForClass('im.actor.model.js.providers', 'JsThreadingProvider', 593, Ljava_lang_Object_2_classLit);
function $addOrUpdateItem(this$static, id_0, data_0){
  $setItem(this$static.storage, 'kv_' + this$static.prefix + '_' + toString_2(id_0), toBase64(data_0));
  if (!$contains_0(this$static.items, valueOf_0(id_0))) {
    $add_1(this$static.items, valueOf_0(id_0));
    $saveIndex(this$static);
  }
}

function $addOrUpdateItems(this$static, values){
  var id_0, isAdded, record, record$iterator;
  isAdded = false;
  for (record$iterator = new AbstractList$IteratorImpl(values); record$iterator.i < record$iterator.this$01.size_1();) {
    record = (checkCriticalElement(record$iterator.i < record$iterator.this$01.size_1()) , dynamicCast(record$iterator.this$01.get_1(record$iterator.last = record$iterator.i++), 377));
    id_0 = record.id_0;
    $setItem(this$static.storage, 'kv_' + this$static.prefix + '_' + toString_2(id_0), toBase64(record.data_0));
    if (!$contains_0(this$static.items, valueOf_0(id_0))) {
      $add_1(this$static.items, valueOf_0(id_0));
      isAdded = true;
    }
  }
  isAdded && $saveIndex(this$static);
}

function $getValue_0(this$static, id_0){
  var res;
  res = $getItem(this$static.storage, 'kv_' + this$static.prefix + '_' + toString_2(id_0));
  return res == null?null:fromBase64(res);
}

function $saveIndex(this$static){
  var dataOutput, l, l$iterator;
  dataOutput = new DataOutput;
  $writeInt_0(dataOutput, this$static.items.map_0.size_0);
  for (l$iterator = $iterator(new AbstractMap$1(this$static.items.map_0)); $hasNext(l$iterator.val$outerIter2);) {
    l = dynamicCast($next_1(l$iterator), 23).value_0;
    $writeLong_0(dataOutput, l);
  }
  $setItem(this$static.storage, 'kv_' + this$static.prefix + '_index', toBase64($toByteArray_0(dataOutput)));
}

function JsKeyValueStorage(prefix, storage){
  var count, data_0, dataInput, e, i_0, index_0;
  this.items = new HashSet;
  this.storage = storage;
  this.prefix = prefix;
  try {
    index_0 = $getItem_0(storage.storage, 'kv_' + prefix + '_index');
    if (index_0 != null) {
      data_0 = fromBase64(index_0);
      dataInput = new DataInput_0(data_0, 0, data_0.length);
      count = $readInt(dataInput);
      for (i_0 = 0; i_0 < count; i_0++) {
        $add_1(this.items, valueOf_0($readLong(dataInput)));
      }
    }
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 14)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
    }
     else 
      throw unwrap($e0);
  }
}

defineClass(643, 1, {}, JsKeyValueStorage);
var Lim_actor_model_js_providers_storage_JsKeyValueStorage_2_classLit = createForClass('im.actor.model.js.providers.storage', 'JsKeyValueStorage', 643, Ljava_lang_Object_2_classLit);
function $addOrUpdateItem_0(this$static, item_0){
  var callback, callback$iterator;
  $put_1(this$static.cache, valueOf_0(item_0.getEngineId()), item_0);
  $updateOrAdd(this$static.storage, new ListEngineRecord(item_0.getEngineId(), item_0.getEngineSort(), (item_0.getEngineSearch() , $toByteArray(item_0))));
  for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
    callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 150));
    $onItemAddedOrUpdated(callback, item_0);
  }
}

function $addOrUpdateItems_0(this$static, items){
  var callback, callback$iterator, records, t, t$iterator, t$iterator0;
  records = new ArrayList;
  for (t$iterator0 = new AbstractList$IteratorImpl(items); t$iterator0.i < t$iterator0.this$01.size_1();) {
    t = (checkCriticalElement(t$iterator0.i < t$iterator0.this$01.size_1()) , dynamicCast(t$iterator0.this$01.get_1(t$iterator0.last = t$iterator0.i++), 4));
    $put_1(this$static.cache, valueOf_0(t.getEngineId()), t);
    $add_0(records, new ListEngineRecord(t.getEngineId(), t.getEngineSort(), (t.getEngineSearch() , $toByteArray(t))));
  }
  $updateOrAdd_0(this$static.storage, records);
  for (t$iterator = new AbstractList$IteratorImpl(items); t$iterator.i < t$iterator.this$01.size_1();) {
    t = (checkCriticalElement(t$iterator.i < t$iterator.this$01.size_1()) , dynamicCast(t$iterator.this$01.get_1(t$iterator.last = t$iterator.i++), 4));
    for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
      callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 150));
      $onItemAddedOrUpdated(callback, t);
    }
  }
}

function $clear_1(this$static){
  var callback, callback$iterator;
  $reset(this$static.cache);
  $clear_2(this$static.storage);
  for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
    callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 150));
    $onClear(callback);
  }
}

function $getHeadValue(this$static){
  var id_0;
  id_0 = $getHeadId(this$static.storage);
  if (id_0) {
    return $getValue_1(this$static, id_0.value_0);
  }
  return null;
}

function $getValue_1(this$static, key){
  var e, record, res;
  if ($containsKey(this$static.cache, valueOf_0(key))) {
    return dynamicCast($get_2(this$static.cache, valueOf_0(key)), 4);
  }
  record = $loadItem(this$static.storage, key);
  if (record) {
    try {
      res = parse_159(this$static.creator.createInstance(), record.data_0);
      $put_1(this$static.cache, valueOf_0(key), res);
      return res;
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
  return null;
}

function $removeItem_1(this$static, key){
  var callback, callback$iterator;
  $remove_0(this$static.cache, valueOf_0(key));
  $delete(this$static.storage, key);
  for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
    callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 150));
    $onItemRemoved(callback, key);
  }
}

function $removeItems(this$static, keys_0){
  var callback, callback$iterator, key, key$index, key$index0, key$max, key$max0;
  for (key$index0 = 0 , key$max0 = keys_0.length; key$index0 < key$max0; ++key$index0) {
    key = keys_0[key$index0];
    $remove_0(this$static.cache, valueOf_0(key));
  }
  $delete_0(this$static.storage, keys_0);
  for (key$index = 0 , key$max = keys_0.length; key$index < key$max; ++key$index) {
    key = keys_0[key$index];
    for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
      callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 150));
      $onItemRemoved(callback, key);
    }
  }
}

function $replaceItems(this$static, items){
  var callback, callback$iterator, callback$iterator0, records, t, t$iterator, t$iterator0;
  $reset(this$static.cache);
  $clear_2(this$static.storage);
  for (callback$iterator0 = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator0.i < callback$iterator0.this$01.size_1();) {
    callback = (checkCriticalElement(callback$iterator0.i < callback$iterator0.this$01.size_1()) , dynamicCast(callback$iterator0.this$01.get_1(callback$iterator0.last = callback$iterator0.i++), 150));
    $onClear(callback);
  }
  records = new ArrayList;
  for (t$iterator0 = new AbstractList$IteratorImpl(items); t$iterator0.i < t$iterator0.this$01.size_1();) {
    t = (checkCriticalElement(t$iterator0.i < t$iterator0.this$01.size_1()) , dynamicCast(t$iterator0.this$01.get_1(t$iterator0.last = t$iterator0.i++), 4));
    $put_1(this$static.cache, valueOf_0(t.getEngineId()), t);
    $add_0(records, new ListEngineRecord(t.getEngineId(), t.getEngineSort(), (t.getEngineSearch() , $toByteArray(t))));
  }
  $updateOrAdd_0(this$static.storage, records);
  for (t$iterator = new AbstractList$IteratorImpl(items); t$iterator.i < t$iterator.this$01.size_1();) {
    t = (checkCriticalElement(t$iterator.i < t$iterator.this$01.size_1()) , dynamicCast(t$iterator.this$01.get_1(t$iterator.last = t$iterator.i++), 4));
    for (callback$iterator = new AbstractList$IteratorImpl(this$static.callbacks); callback$iterator.i < callback$iterator.this$01.size_1();) {
      callback = (checkCriticalElement(callback$iterator.i < callback$iterator.this$01.size_1()) , dynamicCast(callback$iterator.this$01.get_1(callback$iterator.last = callback$iterator.i++), 150));
      $onItemAddedOrUpdated(callback, t);
    }
  }
}

function JsListEngine(storage, creator){
  this.cache = new HashMap;
  this.callbacks = new ArrayList;
  this.storage = storage;
  this.creator = creator;
}

defineClass(163, 1, {760:1}, JsListEngine);
var Lim_actor_model_js_providers_storage_JsListEngine_2_classLit = createForClass('im.actor.model.js.providers.storage', 'JsListEngine', 163, Ljava_lang_Object_2_classLit);
function $clear_2(this$static){
  var i_0, i$iterator;
  for (i$iterator = new AbstractList$IteratorImpl(this$static.index_0); i$iterator.i < i$iterator.this$01.size_1();) {
    i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 48));
    $removeItem(this$static.storage, $getId(this$static, i_0.id_0));
  }
  this$static.index_0.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
  $updateIndex(this$static);
}

function $delete(this$static, key){
  var i_0, i$iterator;
  for (i$iterator = new AbstractList$IteratorImpl(this$static.index_0); i$iterator.i < i$iterator.this$01.size_1();) {
    i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 48));
    if (eq(i_0.id_0, key)) {
      $remove_4(this$static.index_0, i_0);
      $removeItem(this$static.storage, 'list_' + this$static.prefix + '_' + toString_2(key));
      $updateIndex(this$static);
      break;
    }
  }
}

function $delete_0(this$static, keys_0){
  var i_0, i$iterator, key, key$index, key$max;
  for (key$index = 0 , key$max = keys_0.length; key$index < key$max; ++key$index) {
    key = keys_0[key$index];
    for (i$iterator = new AbstractList$IteratorImpl(this$static.index_0); i$iterator.i < i$iterator.this$01.size_1();) {
      i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 48));
      if (eq(i_0.id_0, key)) {
        $remove_4(this$static.index_0, i_0);
        $removeItem(this$static.storage, 'list_' + this$static.prefix + '_' + toString_2(key));
        $updateIndex(this$static);
        break;
      }
    }
  }
}

function $getHeadId(this$static){
  return this$static.index_0.array.length > 0?valueOf_0(dynamicCast($get_3(this$static.index_0, 0), 48).id_0):null;
}

function $getId(this$static, id_0){
  return 'list_' + this$static.prefix + '_' + toString_2(id_0);
}

function $getOrderedIds(this$static){
  var i_0, res;
  res = initDim(J_classLit, $intern_4, 0, this$static.index_0.array.length, 6, 1);
  for (i_0 = 0; i_0 < res.length; i_0++) {
    res[i_0] = dynamicCast($get_3(this$static.index_0, i_0), 48).id_0;
  }
  return res;
}

function $loadItem(this$static, key){
  var i_0, i$iterator, indexValue, item_0, res;
  indexValue = null;
  for (i$iterator = new AbstractList$IteratorImpl(this$static.index_0); i$iterator.i < i$iterator.this$01.size_1();) {
    i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 48));
    if (eq(i_0.id_0, key)) {
      indexValue = i_0;
      break;
    }
  }
  if (!indexValue) {
    return null;
  }
  item_0 = $getItem(this$static.storage, 'list_' + this$static.prefix + '_' + toString_2(key));
  if (item_0 != null) {
    res = fromBase64(item_0);
    return new ListEngineRecord(key, indexValue.sortKey, res);
  }
  return null;
}

function $updateIndex(this$static){
  var dataOutput, i_0, i$iterator;
  sort_0(this$static.index_0, this$static.comparator);
  dataOutput = new DataOutput;
  $writeInt_0(dataOutput, this$static.index_0.array.length);
  for (i$iterator = new AbstractList$IteratorImpl(this$static.index_0); i$iterator.i < i$iterator.this$01.size_1();) {
    i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 48));
    $writeLong_0(dataOutput, i_0.id_0);
    $writeLong_0(dataOutput, i_0.sortKey);
  }
  $setItem(this$static.storage, 'list_' + this$static.prefix + '_index', toBase64($toByteArray_0(dataOutput)));
}

function $updateOrAdd(this$static, record){
  var i_0, i$iterator;
  for (i$iterator = new AbstractList$IteratorImpl(this$static.index_0); i$iterator.i < i$iterator.this$01.size_1();) {
    i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 48));
    if (eq(i_0.id_0, record.key)) {
      $remove_4(this$static.index_0, i_0);
      break;
    }
  }
  $add_0(this$static.index_0, new JsListStorage$Index(record.key, record.order));
  $updateIndex(this$static);
  $setItem(this$static.storage, $getId(this$static, record.key), toBase64(record.data_0));
}

function $updateOrAdd_0(this$static, items){
  var i_0, i$iterator, record, record$iterator, record$iterator0;
  for (record$iterator0 = new AbstractList$IteratorImpl(items); record$iterator0.i < record$iterator0.this$01.size_1();) {
    record = (checkCriticalElement(record$iterator0.i < record$iterator0.this$01.size_1()) , dynamicCast(record$iterator0.this$01.get_1(record$iterator0.last = record$iterator0.i++), 97));
    for (i$iterator = new AbstractList$IteratorImpl(this$static.index_0); i$iterator.i < i$iterator.this$01.size_1();) {
      i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 48));
      if (eq(i_0.id_0, record.key)) {
        $remove_4(this$static.index_0, i_0);
        break;
      }
    }
    $add_0(this$static.index_0, new JsListStorage$Index(record.key, record.order));
  }
  $updateIndex(this$static);
  for (record$iterator = new AbstractList$IteratorImpl(items); record$iterator.i < record$iterator.this$01.size_1();) {
    record = (checkCriticalElement(record$iterator.i < record$iterator.this$01.size_1()) , dynamicCast(record$iterator.this$01.get_1(record$iterator.last = record$iterator.i++), 97));
    $setItem(this$static.storage, $getId(this$static, record.key), toBase64(record.data_0));
  }
}

function JsListStorage(prefix, storage){
  var count, data_0, dataInput, e, i_0, id_0, indexData, order;
  this.index_0 = new ArrayList;
  this.comparator = new JsListStorage$1;
  this.storage = storage;
  this.prefix = prefix;
  indexData = $getItem_0(storage.storage, 'list_' + prefix + '_index');
  if (indexData != null) {
    try {
      data_0 = fromBase64(indexData);
      dataInput = new DataInput_0(data_0, 0, data_0.length);
      count = $readInt(dataInput);
      for (i_0 = 0; i_0 < count; i_0++) {
        id_0 = $readLong(dataInput);
        order = $readLong(dataInput);
        $add_0(this.index_0, new JsListStorage$Index(id_0, order));
      }
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 14)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
  $updateIndex(this);
}

defineClass(644, 1, {}, JsListStorage);
var Lim_actor_model_js_providers_storage_JsListStorage_2_classLit = createForClass('im.actor.model.js.providers.storage', 'JsListStorage', 644, Ljava_lang_Object_2_classLit);
function $compare(x_0, y_0){
  return lt(x_0, y_0)?-1:eq(x_0, y_0)?0:1;
}

function $compare_0(o1, o2){
  return -$compare(o1.sortKey, o2.sortKey);
}

function JsListStorage$1(){
}

defineClass(645, 1, {}, JsListStorage$1);
_.compare = function compare_1(o1, o2){
  return $compare_0(dynamicCast(o1, 48), dynamicCast(o2, 48));
}
;
var Lim_actor_model_js_providers_storage_JsListStorage$1_2_classLit = createForClass('im.actor.model.js.providers.storage', 'JsListStorage/1', 645, Ljava_lang_Object_2_classLit);
function JsListStorage$Index(id_0, sortKey){
  this.id_0 = id_0;
  this.sortKey = sortKey;
}

defineClass(48, 1, {48:1}, JsListStorage$Index);
_.id_0 = {l:0, m:0, h:0};
_.sortKey = {l:0, m:0, h:0};
var Lim_actor_model_js_providers_storage_JsListStorage$Index_2_classLit = createForClass('im.actor.model.js.providers.storage', 'JsListStorage/Index', 48, Ljava_lang_Object_2_classLit);
function $getBool(this$static, key, def){
  var v;
  v = $getItem(this$static.storage, 'prefs_' + key);
  return v != null?($clinit_Boolean() , $equalsIgnoreCase('true', v)):def;
}

function $getBytes_1(this$static, key){
  var v;
  v = $getItem(this$static.storage, 'prefs_' + key);
  return v != null?fromBase64(v):null;
}

function $getInt(this$static, key, def){
  var v;
  v = $getItem(this$static.storage, 'prefs_' + key);
  return v != null?__parseAndValidateInt(v):def;
}

function $getLong_1(this$static, key, def){
  var v;
  v = $getItem(this$static.storage, 'prefs_' + key);
  return v != null?__parseAndValidateLong(v):def;
}

function $getString(this$static, key){
  return $getItem(this$static.storage, 'prefs_' + key);
}

function $putBool(this$static, key, v){
  $setItem(this$static.storage, 'prefs_' + key, '' + v);
}

function $putBytes(this$static, key, v){
  $setItem(this$static.storage, 'prefs_' + key, toBase64(v));
}

function $putInt(this$static, key, v){
  $setItem(this$static.storage, 'prefs_' + key, '' + v);
}

function $putLong(this$static, key, v){
  $setItem(this$static.storage, 'prefs_' + key, '' + toString_2(v));
}

function $putString(this$static, key, v){
  $setItem(this$static.storage, 'prefs_' + key, v);
}

function JsPreferencesStorage(storage){
  this.storage = storage;
}

defineClass(647, 1, {}, JsPreferencesStorage);
var Lim_actor_model_js_providers_storage_JsPreferencesStorage_2_classLit = createForClass('im.actor.model.js.providers.storage', 'JsPreferencesStorage', 647, Ljava_lang_Object_2_classLit);
defineClass(794, 1, {});
var Lim_actor_model_util_AtomicIntegerCompat_2_classLit = createForClass('im.actor.model.util', 'AtomicIntegerCompat', 794, Ljava_lang_Object_2_classLit);
function $compareAndSet(this$static, exp_0){
  this$static.value_0 == exp_0 && (this$static.value_0 = 50);
}

function JsAtomicInteger(){
  this.value_0 = 1;
}

defineClass(720, 794, {}, JsAtomicInteger);
_.value_0 = 0;
var Lim_actor_model_js_providers_threading_JsAtomicInteger_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsAtomicInteger', 720, Lim_actor_model_util_AtomicIntegerCompat_2_classLit);
defineClass(791, 1, {});
var Lim_actor_model_util_AtomicLongCompat_2_classLit = createForClass('im.actor.model.util', 'AtomicLongCompat', 791, Ljava_lang_Object_2_classLit);
function JsAtomicLong(){
  this.value_0 = {l:1, m:0, h:0};
}

defineClass(711, 791, {}, JsAtomicLong);
_.value_0 = {l:0, m:0, h:0};
var Lim_actor_model_js_providers_threading_JsAtomicLong_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsAtomicLong', 711, Lim_actor_model_util_AtomicLongCompat_2_classLit);
function JsDispatch(actorSystem){
  this.endpoints = new HashMap;
  this.scopes = new HashMap;
  this.actorSystem = actorSystem;
  $initDispatcher(this, new JsThreads(new MailboxesQueue, new JsDispatch$1(this)));
}

defineClass(455, 244, {244:1}, JsDispatch);
var Lim_actor_model_js_providers_threading_JsDispatch_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsDispatch', 455, Lim_actor_model_droidkit_actors_mailbox_ActorDispatcher_2_classLit);
function $dispatchMessage_0(this$static, message){
  $processEnvelope(this$static.this$01, message);
}

function JsDispatch$1(this$0){
  this.this$01 = this$0;
}

defineClass(687, 1, {}, JsDispatch$1);
var Lim_actor_model_js_providers_threading_JsDispatch$1_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsDispatch/1', 687, Ljava_lang_Object_2_classLit);
defineClass(798, 1, {});
var Lim_actor_model_util_ThreadLocalCompat_2_classLit = createForClass('im.actor.model.util', 'ThreadLocalCompat', 798, Ljava_lang_Object_2_classLit);
function $set(this$static, v){
  this$static.obj = v;
}

function JsThreadLocal(){
}

defineClass(723, 798, {}, JsThreadLocal);
var Lim_actor_model_js_providers_threading_JsThreadLocal_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsThreadLocal', 723, Lim_actor_model_util_ThreadLocalCompat_2_classLit);
function $doIteration(this$static){
  var action, time;
  time = getActorTime();
  action = $dispatch(this$static.queue, time);
  if (action.isResult) {
    $dispatchMessage(this$static, action.res);
    return {l:$intern_7, m:$intern_7, h:$intern_8};
  }
   else {
    return lt(action.delay, {l:1, m:0, h:0})?{l:1, m:0, h:0}:action.delay;
  }
}

function $notifyDispatcher(this$static){
  if (!this$static.isSchedulled) {
    $cancel(this$static.timer);
    $scheduleIncremental(($clinit_SchedulerImpl() , INSTANCE), this$static.repeatingCommand);
    this$static.isSchedulled = true;
  }
}

function JsThreads(queue, dispatch){
  this.queue = queue;
  this.dispatch = dispatch;
  $setListener(this.queue, new AbstractDispatcher$1(this));
  this.isSchedulled = false;
  this.timer = new JsThreads$1(this);
  new JsThreads$2(this);
  this.repeatingCommand = new JsThreads$3(this);
}

defineClass(706, 658, {}, JsThreads);
_.isSchedulled = false;
var Lim_actor_model_js_providers_threading_JsThreads_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsThreads', 706, Lim_actor_model_droidkit_actors_dispatch_AbstractDispatcher_2_classLit);
function JsThreads$1(this$0){
  this.this$01 = this$0;
}

defineClass(708, 707, {}, JsThreads$1);
var Lim_actor_model_js_providers_threading_JsThreads$1_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsThreads/1', 708, Lcom_google_gwt_user_client_Timer_2_classLit);
function JsThreads$2(this$0){
  this.this$01 = this$0;
}

defineClass(709, 1, {}, JsThreads$2);
_.execute_0 = function execute_3(){
  $doIteration(this.this$01);
}
;
var Lim_actor_model_js_providers_threading_JsThreads$2_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsThreads/2', 709, Ljava_lang_Object_2_classLit);
function JsThreads$3(this$0){
  this.this$01 = this$0;
}

defineClass(710, 1, {}, JsThreads$3);
_.execute = function execute_4(){
  var delay;
  delay = $doIteration(this.this$01);
  if (lt(delay, {l:0, m:0, h:0})) {
    this.this$01.isSchedulled = true;
    return true;
  }
   else {
    gt(delay, {l:15000, m:0, h:0}) && (delay = {l:15000, m:0, h:0});
    lt(delay, {l:1, m:0, h:0}) && (delay = {l:1, m:0, h:0});
    $schedule(this.this$01.timer, toInt(delay));
    this.this$01.isSchedulled = false;
    return false;
  }
}
;
var Lim_actor_model_js_providers_threading_JsThreads$3_2_classLit = createForClass('im.actor.model.js.providers.threading', 'JsThreads/3', 710, Ljava_lang_Object_2_classLit);
defineClass(743, 1, {});
var Lim_actor_model_network_connection_AsyncConnection_2_classLit = createForClass('im.actor.model.network.connection', 'AsyncConnection', 743, Ljava_lang_Object_2_classLit);
function $createJSWebSocket(url_0, webSocket){
  var jsWebSocket = new WebSocket(url_0);
  jsWebSocket.binaryType = 'arraybuffer';
  jsWebSocket.onopen = function(){
    webSocket.onRawConnected();
  }
  ;
  jsWebSocket.onclose = function(){
    webSocket.onRawClosed();
  }
  ;
  jsWebSocket.onerror = function(){
    webSocket.onRawClosed();
  }
  ;
  jsWebSocket.onmessage = function(socketResponse){
    socketResponse.data && webSocket.onRawMessage(socketResponse.data);
  }
  ;
  return jsWebSocket;
}

function $doConnect(this$static){
  var url_0;
  if (this$static.endpoint.type_0 == ($clinit_ConnectionEndpoint$Type() , WS)) {
    url_0 = 'ws://' + this$static.endpoint.host + ':' + this$static.endpoint.port + '/';
  }
   else if (this$static.endpoint.type_0 == WS_TLS) {
    url_0 = 'wss://' + this$static.endpoint.host + ':' + this$static.endpoint.port + '/';
  }
   else {
    throw new RuntimeException;
  }
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'WS' + ':' + ('Connecting to ' + url_0));
  this$static.jsWebSocket = $createJSWebSocket(url_0, this$static);
}

function $doSend(this$static, data_0){
  var i_0, push_0;
  push_0 = create_2(data_0.length);
  for (i_0 = 0; i_0 < data_0.length; i_0++) {
    push_0[i_0] = data_0[i_0];
  }
  $send_3(this$static, push_0);
}

function $send_3(this$static, message){
  if (message == null)
    return;
  this$static.jsWebSocket.send(message);
}

function WebSocketConnection(endpoint, connection){
  this.connection = connection;
  this.endpoint = endpoint;
}

defineClass(751, 743, {}, WebSocketConnection);
_.onRawClosed = function onRawClosed(){
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'WS' + ':' + 'Closed');
  $close(this.connection.this$01);
}
;
_.onRawConnected = function onRawConnected(){
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'WS' + ':' + 'Connected');
  $onRawConnected(this.connection.this$01);
}
;
_.onRawMessage = function onRawMessage(message){
  var array, i_0, res;
  array = new Uint8Array(message);
  res = initDim(B_classLit, $intern_17, 0, array.length, 7, 1);
  for (i_0 = 0; i_0 < res.length; i_0++) {
    res[i_0] = narrow_byte(array[i_0]);
  }
  $onReceived(this.connection, res);
}
;
var Lim_actor_model_js_providers_websocket_WebSocketConnection_2_classLit = createForClass('im.actor.model.js.providers.websocket', 'WebSocketConnection', 751, Lim_actor_model_network_connection_AsyncConnection_2_classLit);
function getUniqueId(){
  var i_0, id_0, rnd, storage;
  storage = (!localStorage_0 && ($clinit_Storage$StorageSupportDetector() , localStorageSupported) && (localStorage_0 = new Storage_0) , localStorage_0);
  id_0 = $getItem_0(storage.storage, 'tech_unique_id');
  if (id_0 != null) {
    return id_0;
  }
  rnd = new Random;
  id_0 = '';
  for (i_0 = 0; i_0 < 128; i_0++) {
    id_0 += charToString(97 + $nextInt(rnd, 25) & $intern_5);
  }
  $setItem_0(storage.storage, 'tech_unique_id', id_0);
  return id_0;
}

function d_0(tag, message){
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + tag + ':' + message);
}

function setLog(log_0){
  log_2 = log_0;
}

function w_0(tag, message){
  !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + tag + ':' + message);
}

var log_2;
function Analytics(modules){
  BaseModule.call(this, modules);
}

defineClass(619, 25, {}, Analytics);
var Lim_actor_model_modules_Analytics_2_classLit = createForClass('im.actor.model.modules', 'Analytics', 619, Lim_actor_model_modules_BaseModule_2_classLit);
function $onContactsUpdate(this$static, isEmpty){
  $send_1(this$static.listStatesActor, new ListsStatesActor$OnContactsChanged(isEmpty));
}

function $onDialogsUpdate(this$static, isEmpty){
  $send_1(this$static.listStatesActor, new ListsStatesActor$OnDialogsChanged(isEmpty));
}

function $run(this$static){
  this$static.listStatesActor = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new AppStateModule$1(this$static), null), 'actor/app/state');
}

function AppStateModule(modules){
  BaseModule.call(this, modules);
  this.appStateVM = new AppStateVM(modules);
}

defineClass(640, 25, {}, AppStateModule);
var Lim_actor_model_modules_AppStateModule_2_classLit = createForClass('im.actor.model.modules', 'AppStateModule', 640, Lim_actor_model_modules_BaseModule_2_classLit);
function AppStateModule$1(this$0){
  this.this$01 = this$0;
}

defineClass(642, 1, {}, AppStateModule$1);
_.create_0 = function create_7(){
  return new ListsStatesActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_AppStateModule$1_2_classLit = createForClass('im.actor.model.modules', 'AppStateModule/1', 642, Ljava_lang_Object_2_classLit);
function $onLoggedIn(this$static, callback, response){
  $putBool(this$static.modules.preferences, 'auth_yes', true);
  this$static.state = ($clinit_AuthState() , LOGGED_IN);
  this$static.myUid = response.user.id_0;
  $putInt(this$static.modules.preferences, 'auth_uid', this$static.myUid);
  $onLoggedIn_0(this$static.modules);
  $onUpdateReceived_0(this$static.modules.updates, new LoggedIn(response, new Auth$1(this$static, callback)));
}

function $run_0(this$static){
  var contactRecord, contactRecord$iterator, records, user;
  if ($getBool(this$static.modules.preferences, 'auth_yes', false)) {
    this$static.state = ($clinit_AuthState() , LOGGED_IN);
    $onLoggedIn_0(this$static.modules);
    user = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(this$static.myUid)), 11);
    records = new ArrayList;
    for (contactRecord$iterator = user.records.iterator(); contactRecord$iterator.hasNext();) {
      contactRecord = dynamicCast(contactRecord$iterator.next(), 124);
      contactRecord.recordType == 0 && $add_0(records, valueOf_0(__parseAndValidateLong(contactRecord.recordData)));
    }
    hex(this$static.deviceHash);
    dynamicCast($toArray_0(records, initDim(Ljava_lang_Long_2_classLit, $intern_21, 23, 0, 0, 1)), 221);
  }
   else {
    this$static.state = ($clinit_AuthState() , AUTH_START);
    hex(this$static.deviceHash);
  }
}

function Auth(modules){
  BaseModule.call(this, modules);
  this.myUid = $getInt(this.modules.preferences, 'auth_uid', 0);
  this.deviceHash = $getBytes_1(this.modules.preferences, 'device_hash');
  if (this.deviceHash == null) {
    this.deviceHash = ($clinit_CryptoUtils() , $SHA256(getBytesUtf8(modules.configuration.apiConfiguration.deviceString)));
    $putBytes(this.modules.preferences, 'device_hash', this.deviceHash);
  }
  this.apiConfiguration = modules.configuration.apiConfiguration;
}

defineClass(575, 25, {}, Auth);
_.myUid = 0;
var Lim_actor_model_modules_Auth_2_classLit = createForClass('im.actor.model.modules', 'Auth', 575, Lim_actor_model_modules_BaseModule_2_classLit);
function Auth$1(this$0, val$callback){
  this.this$01 = this$0;
  this.val$callback2 = val$callback;
}

defineClass(576, 1, $intern_20, Auth$1);
_.run = function run_1(){
  var contactRecord, contactRecord$iterator, records, user;
  this.this$01.state = ($clinit_AuthState() , LOGGED_IN);
  $onResult_0(this.val$callback2, this.this$01.state);
  user = dynamicCast($getValue_2(this.this$01.modules.users.users, fromInt(this.this$01.myUid)), 11);
  records = new ArrayList;
  for (contactRecord$iterator = user.records.iterator(); contactRecord$iterator.hasNext();) {
    contactRecord = dynamicCast(contactRecord$iterator.next(), 124);
    contactRecord.recordType == 0 && $add_0(records, valueOf_0(__parseAndValidateLong(contactRecord.recordData)));
  }
  hex(this.this$01.deviceHash);
  dynamicCast($toArray_0(records, initDim(Ljava_lang_Long_2_classLit, $intern_21, 23, 0, 0, 1)), 221);
}
;
var Lim_actor_model_modules_Auth$1_2_classLit = createForClass('im.actor.model.modules', 'Auth/1', 576, Ljava_lang_Object_2_classLit);
function $start(this$static, callback){
  $request(this$static.this$01, new RequestSendAuthCode_0(this$static.val$phone2, this$static.this$01.apiConfiguration.appId, this$static.this$01.apiConfiguration.appKey), new Auth$2$1(this$static, this$static.val$phone2, callback));
}

function Auth$2(this$0, val$phone){
  this.this$01 = this$0;
  this.val$phone2 = val$phone;
}

defineClass(577, 1, {}, Auth$2);
_.val$phone2 = {l:0, m:0, h:0};
var Lim_actor_model_modules_Auth$2_2_classLit = createForClass('im.actor.model.modules', 'Auth/2', 577, Ljava_lang_Object_2_classLit);
function $onResult_5(this$static, response){
  $putLong(this$static.this$11.this$01.modules.preferences, 'auth_phone', this$static.val$phone2);
  $putString(this$static.this$11.this$01.modules.preferences, 'auth_sms_hash', response.smsHash);
  this$static.this$11.this$01.state = ($clinit_AuthState() , CODE_VALIDATION);
  $runOnUiThread(new Auth$2$1$1(this$static, this$static.val$callback4));
}

function Auth$2$1(this$1, val$phone, val$callback){
  this.this$11 = this$1;
  this.val$phone2 = val$phone;
  this.val$callback4 = val$callback;
}

defineClass(578, 1, {}, Auth$2$1);
_.onError_0 = function onError_1(e){
  $runOnUiThread(new Auth$2$1$2(this.val$callback4));
}
;
_.onResult_0 = function onResult_1(response){
  $onResult_5(this, dynamicCast(response, 315));
}
;
_.val$phone2 = {l:0, m:0, h:0};
var Lim_actor_model_modules_Auth$2$1_2_classLit = createForClass('im.actor.model.modules', 'Auth/2/1', 578, Ljava_lang_Object_2_classLit);
function Auth$2$1$1(this$2, val$callback){
  this.this$21 = this$2;
  this.val$callback2 = val$callback;
}

defineClass(579, 1, $intern_20, Auth$2$1$1);
_.run = function run_2(){
  $onResult(this.val$callback2, this.this$21.this$11.this$01.state);
}
;
var Lim_actor_model_modules_Auth$2$1$1_2_classLit = createForClass('im.actor.model.modules', 'Auth/2/1/1', 579, Ljava_lang_Object_2_classLit);
function Auth$2$1$2(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(580, 1, $intern_20, Auth$2$1$2);
_.run = function run_3(){
  $onError(this.val$callback2);
}
;
var Lim_actor_model_modules_Auth$2$1$2_2_classLit = createForClass('im.actor.model.modules', 'Auth/2/1/2', 580, Ljava_lang_Object_2_classLit);
function $start_0(this$static, callback){
  $request(this$static.this$01, new RequestSignIn_0($getLong_1(this$static.this$01.modules.preferences, 'auth_phone', {l:0, m:0, h:0}), $getString(this$static.this$01.modules.preferences, 'auth_sms_hash'), this$static.val$code2 + '', this$static.this$01.deviceHash, this$static.this$01.apiConfiguration.appTitle, this$static.this$01.apiConfiguration.appId, this$static.this$01.apiConfiguration.appKey), new Auth$3$1(this$static, callback, this$static.val$code2));
}

function Auth$3(this$0, val$code){
  this.this$01 = this$0;
  this.val$code2 = val$code;
}

defineClass(581, 1, {}, Auth$3);
_.val$code2 = 0;
var Lim_actor_model_modules_Auth$3_2_classLit = createForClass('im.actor.model.modules', 'Auth/3', 581, Ljava_lang_Object_2_classLit);
function $onResult_6(this$static, response){
  $onLoggedIn(this$static.this$11.this$01, this$static.val$callback2, response);
}

function Auth$3$1(this$1, val$callback, val$code){
  this.this$11 = this$1;
  this.val$callback2 = val$callback;
  this.val$code3 = val$code;
}

defineClass(582, 1, {}, Auth$3$1);
_.onError_0 = function onError_2(e){
  if ($equals_3('PHONE_CODE_EXPIRED', e.tag)) {
    this.this$11.this$01.state = ($clinit_AuthState() , AUTH_START);
  }
   else if ($equals_3('PHONE_NUMBER_UNOCCUPIED', e.tag)) {
    $putInt(this.this$11.this$01.modules.preferences, 'auth_sms_code', this.val$code3);
    this.this$11.this$01.state = ($clinit_AuthState() , SIGN_UP);
    $onResult_0(this.val$callback2, SIGN_UP);
    return;
  }
  $runOnUiThread(new Auth$3$1$1(this.val$callback2));
}
;
_.onResult_0 = function onResult_2(response){
  $onResult_6(this, dynamicCast(response, 316));
}
;
_.val$code3 = 0;
var Lim_actor_model_modules_Auth$3$1_2_classLit = createForClass('im.actor.model.modules', 'Auth/3/1', 582, Ljava_lang_Object_2_classLit);
function Auth$3$1$1(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(583, 1, $intern_20, Auth$3$1$1);
_.run = function run_4(){
  $onError_0(this.val$callback2);
}
;
var Lim_actor_model_modules_Auth$3$1$1_2_classLit = createForClass('im.actor.model.modules', 'Auth/3/1/1', 583, Ljava_lang_Object_2_classLit);
function $isUserContact(this$static, uid){
  return $getBool(this$static.modules.preferences, 'contact_' + uid, false);
}

function $markContact(this$static, uid){
  $putBool(this$static.modules.preferences, 'contact_' + uid, true);
}

function $markNonContact(this$static, uid){
  $putBool(this$static.modules.preferences, 'contact_' + uid, false);
}

function $run_1(this$static){
  $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Contacts$1(this$static), null), 'actor/book_import');
  this$static.contactSyncActor = $actorOf_0((null , mainSystem), new Props(new Contacts$2(this$static), null), 'actor/contacts_sync');
}

function Contacts(modules){
  BaseModule.call(this, modules);
  this.contacts = new JsListEngine($createList(modules.configuration.storageProvider, 'contacts'), ($clinit_Contact() , CREATOR));
}

defineClass(676, 25, {}, Contacts);
var Lim_actor_model_modules_Contacts_2_classLit = createForClass('im.actor.model.modules', 'Contacts', 676, Lim_actor_model_modules_BaseModule_2_classLit);
function Contacts$1(this$0){
  this.this$01 = this$0;
}

defineClass(679, 1, {}, Contacts$1);
_.create_0 = function create_8(){
  return new BookImportActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Contacts$1_2_classLit = createForClass('im.actor.model.modules', 'Contacts/1', 679, Ljava_lang_Object_2_classLit);
function Contacts$2(this$0){
  this.this$01 = this$0;
}

defineClass(680, 1, {}, Contacts$2);
_.create_0 = function create_9(){
  return new ContactsSyncActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Contacts$2_2_classLit = createForClass('im.actor.model.modules', 'Contacts/2', 680, Ljava_lang_Object_2_classLit);
function $run_2(this$static){
  $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Files$2(this$static), null), 'actor/download/manager');
  $actorOf_0((null , mainSystem), new Props(new Files$3(this$static), null), 'actor/upload/manager');
}

function Files(modules){
  BaseModule.call(this, modules);
  new Files$1($createKeyValue(modules.configuration.storageProvider, 'downloads'));
}

defineClass(670, 25, {}, Files);
var Lim_actor_model_modules_Files_2_classLit = createForClass('im.actor.model.modules', 'Files', 670, Lim_actor_model_modules_BaseModule_2_classLit);
function Files$1($anonymous0){
  BaseKeyValueEngine.call(this, $anonymous0);
}

defineClass(673, 447, {}, Files$1);
var Lim_actor_model_modules_Files$1_2_classLit = createForClass('im.actor.model.modules', 'Files/1', 673, Lim_actor_model_modules_utils_BaseKeyValueEngine_2_classLit);
function Files$2(this$0){
  this.this$01 = this$0;
}

defineClass(674, 1, {}, Files$2);
_.create_0 = function create_10(){
  return new DownloadManager(this.this$01.modules);
}
;
var Lim_actor_model_modules_Files$2_2_classLit = createForClass('im.actor.model.modules', 'Files/2', 674, Ljava_lang_Object_2_classLit);
function Files$3(this$0){
  this.this$01 = this$0;
}

defineClass(675, 1, {}, Files$3);
_.create_0 = function create_11(){
  return new UploadManager(this.this$01.modules);
}
;
var Lim_actor_model_modules_Files$3_2_classLit = createForClass('im.actor.model.modules', 'Files/3', 675, Ljava_lang_Object_2_classLit);
function Groups(modules){
  BaseModule.call(this, modules);
  this.collection = new Groups$1($createKeyValue(modules.configuration.storageProvider, 'groups'));
  this.groups = this.collection.proxyKeyValueEngine;
  new HashMap;
  $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Groups$2(modules), null), 'actor/avatar/group');
}

defineClass(612, 25, {}, Groups);
var Lim_actor_model_modules_Groups_2_classLit = createForClass('im.actor.model.modules', 'Groups', 612, Lim_actor_model_modules_BaseModule_2_classLit);
function $get_1(this$static, id_0){
  var res;
  if ($get_2(this$static.values, valueOf_0(id_0)) == null) {
    res = $getValue_2(this$static.proxyKeyValueEngine, id_0);
    if (res) {
      $put_1(this$static.values, valueOf_0(id_0), this$static.createNew(res));
    }
     else {
      throw new RuntimeException_0('Unable to find user #' + toString_2(id_0));
    }
  }
  return dynamicCast($get_2(this$static.values, valueOf_0(id_0)), 164);
}

function MVVMCollection(collectionStorage){
  this.values = new HashMap;
  this.collectionStorage = collectionStorage;
  this.proxyKeyValueEngine = new MVVMCollection$ProxyKeyValueEngine(this);
}

defineClass(448, 1, {});
var Lim_actor_model_mvvm_MVVMCollection_2_classLit = createForClass('im.actor.model.mvvm', 'MVVMCollection', 448, Ljava_lang_Object_2_classLit);
function $deserialize_0(raw){
  var e;
  try {
    return dynamicCast(parse_159(new Group_0, raw), 21);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      return null;
    }
     else 
      throw unwrap($e0);
  }
}

function Groups$1($anonymous0){
  MVVMCollection.call(this, $anonymous0);
}

defineClass(614, 448, {}, Groups$1);
_.createNew = function createNew(raw){
  return new GroupVM(dynamicCast(raw, 21));
}
;
_.deserialize = function deserialize_0(raw){
  return $deserialize_0(raw);
}
;
_.serialize_0 = function serialize_180(raw){
  return $toByteArray(dynamicCast(raw, 21));
}
;
var Lim_actor_model_modules_Groups$1_2_classLit = createForClass('im.actor.model.modules', 'Groups/1', 614, Lim_actor_model_mvvm_MVVMCollection_2_classLit);
function Groups$2(val$modules){
  this.val$modules2 = val$modules;
}

defineClass(615, 1, {}, Groups$2);
_.create_0 = function create_12(){
  return new GroupAvatarChangeActor(this.val$modules2);
}
;
var Lim_actor_model_modules_Groups$2_2_classLit = createForClass('im.actor.model.modules', 'Groups/2', 615, Ljava_lang_Object_2_classLit);
function $assumeConvActor(this$static, peer){
  if (!$containsKey(this$static.conversationActors, peer)) {
    $put_1(this$static.conversationActors, peer, $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Messages$8(this$static, peer), null), 'actor/conv_' + peer.peerType + '_' + peer.peerId));
    $put_1(this$static.conversationHistoryActors, peer, $actorOf_0((null , mainSystem), new Props(new Messages$9(this$static, peer), null), 'actor/conv_' + peer.peerType + '_' + peer.peerId + '/history'));
  }
}

function $getConversationActor(this$static, peer){
  $assumeConvActor(this$static, peer);
  return dynamicCast($get_2(this$static.conversationActors, peer), 226);
}

function $getConversationEngine(this$static, peer){
  var storage;
  if (!$containsKey(this$static.conversationEngines, peer)) {
    storage = $createList(this$static.modules.configuration.storageProvider, 'chat_' + toString_2($getUnuqueId(peer)));
    $put_1(this$static.conversationEngines, peer, new JsListEngine(storage, ($clinit_Message() , CREATOR_1)));
  }
  return dynamicCast($get_2(this$static.conversationEngines, peer), 760);
}

function $getConversationHistoryActor(this$static, peer){
  $assumeConvActor(this$static, peer);
  return dynamicCast($get_2(this$static.conversationHistoryActors, peer), 226);
}

function $getMediaEngine(this$static, peer){
  var storage;
  if (!$containsKey(this$static.conversationMediaEngines, peer)) {
    storage = $createList(this$static.modules.configuration.storageProvider, 'chat_media_' + toString_2($getUnuqueId(peer)));
    $put_1(this$static.conversationMediaEngines, peer, new JsListEngine(storage, ($clinit_Message() , CREATOR_1)));
  }
  return dynamicCast($get_2(this$static.conversationMediaEngines, peer), 760);
}

function $loadDraft_0(this$static, peer){
  var res;
  res = $getString(this$static.modules.preferences, 'draft_' + toString_2($getUnuqueId(peer)));
  return res == null?'':res;
}

function $loadReadState(this$static, peer){
  return $getLong_1(this$static.modules.preferences, 'read_state_' + toString_2($getUnuqueId(peer)), {l:0, m:0, h:0});
}

function $run_3(this$static){
  this$static.dialogsActor = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Messages$1(this$static), null), 'actor/dialogs');
  this$static.dialogsHistoryActor = $actorOf_0((null , mainSystem), new Props(new Messages$2(this$static), null), 'actor/dialogs/history');
  this$static.ownReadActor = $actorOf_0((null , mainSystem), new Props(new Messages$3(this$static), null), 'actor/read/own');
  this$static.plainReadActor = $actorOf_0((null , mainSystem), new Props(new Messages$4(this$static), null), 'actor/plain/read');
  this$static.plainReceiverActor = $actorOf_0((null , mainSystem), new Props(new Messages$5(this$static), null), 'actor/plain/receive');
  this$static.sendMessageActor = $actorOf_0((null , mainSystem), new Props(new Messages$6(this$static), null), 'actor/sender/small');
  $actorOf_0((null , mainSystem), new Props(new Messages$7(this$static), null), 'actor/deletions');
}

function $saveDraft_0(this$static, peer, draft){
  $putString(this$static.modules.preferences, 'draft_' + toString_2($getUnuqueId(peer)), $trim(draft));
}

function $saveReadState(this$static, peer, lastReadDate){
  $putLong(this$static.modules.preferences, 'read_state_' + toString_2($getUnuqueId(peer)), lastReadDate);
}

function $sendMessage_1(this$static, peer, message){
  $send_2(this$static.sendMessageActor, new SenderActor$SendText(peer, message), null);
}

function Messages(messenger){
  BaseModule.call(this, messenger);
  this.conversationEngines = new HashMap;
  this.conversationMediaEngines = new HashMap;
  this.conversationActors = new HashMap;
  this.conversationHistoryActors = new HashMap;
  this.conversationPending = new SyncKeyValue($createKeyValue(this.modules.configuration.storageProvider, 'chat_pending'));
  this.cursorStorage = new SyncKeyValue($createKeyValue(this.modules.configuration.storageProvider, 'chat_cursor'));
  this.dialogs = new JsListEngine($createList(this.modules.configuration.storageProvider, 'dialogs'), ($clinit_Dialog() , CREATOR_0));
}

defineClass(540, 25, {}, Messages);
var Lim_actor_model_modules_Messages_2_classLit = createForClass('im.actor.model.modules', 'Messages', 540, Lim_actor_model_modules_BaseModule_2_classLit);
function Messages$1(this$0){
  this.this$01 = this$0;
}

defineClass(550, 1, {}, Messages$1);
_.create_0 = function create_13(){
  return new DialogsActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$1_2_classLit = createForClass('im.actor.model.modules', 'Messages/1', 550, Ljava_lang_Object_2_classLit);
function $start_1(this$static, callback){
  var apiPeer, group, outPeer, user;
  if (this$static.val$peer2.peerType == ($clinit_PeerType_0() , PRIVATE_0)) {
    user = dynamicCast($getValue_2(this$static.this$01.modules.users.users, fromInt(this$static.val$peer2.peerId)), 11);
    if (!user) {
      $runOnUiThread(new Messages$10$1(callback));
      return;
    }
    outPeer = new OutPeer_0(($clinit_PeerType() , PRIVATE), user.uid, user.accessHash);
    apiPeer = new Peer_0(PRIVATE, user.uid);
  }
   else if (this$static.val$peer2.peerType == GROUP_0) {
    group = dynamicCast($getValue_2(this$static.this$01.modules.groups.groups, fromInt(this$static.val$peer2.peerId)), 21);
    if (!group) {
      $runOnUiThread(new Messages$10$2(callback));
      return;
    }
    outPeer = new OutPeer_0(($clinit_PeerType() , GROUP), group.groupId, group.accessHash);
    apiPeer = new Peer_0(GROUP, group.groupId);
  }
   else {
    $runOnUiThread(new Messages$10$3(callback));
    return;
  }
  $request(this$static.this$01, new RequestDeleteChat_0(outPeer), new Messages$10$4(this$static, apiPeer, callback));
}

function Messages$10(this$0, val$peer){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
}

defineClass(559, 1, {}, Messages$10);
var Lim_actor_model_modules_Messages$10_2_classLit = createForClass('im.actor.model.modules', 'Messages/10', 559, Ljava_lang_Object_2_classLit);
function Messages$10$1(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(560, 1, $intern_20, Messages$10$1);
_.run = function run_5(){
  $onError_1(this.val$callback2, new RpcInternalException);
}
;
var Lim_actor_model_modules_Messages$10$1_2_classLit = createForClass('im.actor.model.modules', 'Messages/10/1', 560, Ljava_lang_Object_2_classLit);
function Messages$10$2(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(561, 1, $intern_20, Messages$10$2);
_.run = function run_6(){
  $onError_1(this.val$callback2, new RpcInternalException);
}
;
var Lim_actor_model_modules_Messages$10$2_2_classLit = createForClass('im.actor.model.modules', 'Messages/10/2', 561, Ljava_lang_Object_2_classLit);
function Messages$10$3(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(562, 1, $intern_20, Messages$10$3);
_.run = function run_7(){
  $onError_1(this.val$callback2, new RpcInternalException);
}
;
var Lim_actor_model_modules_Messages$10$3_2_classLit = createForClass('im.actor.model.modules', 'Messages/10/3', 562, Ljava_lang_Object_2_classLit);
function $onResult_7(this$static, response){
  $onUpdateReceived_0(this$static.this$11.this$01.modules.updates, new SeqUpdate_0(response.seq, response.state, 48, $toByteArray(new UpdateChatDelete_0(this$static.val$apiPeer2))));
  $runOnUiThread(new Messages$10$4$1(this$static.val$callback3));
}

function Messages$10$4(this$1, val$apiPeer, val$callback){
  this.this$11 = this$1;
  this.val$apiPeer2 = val$apiPeer;
  this.val$callback3 = val$callback;
}

defineClass(563, 1, {}, Messages$10$4);
_.onError_0 = function onError_3(e){
  $runOnUiThread(new Messages$10$4$2(this.val$callback3));
}
;
_.onResult_0 = function onResult_3(response){
  $onResult_7(this, dynamicCast(response, 89));
}
;
var Lim_actor_model_modules_Messages$10$4_2_classLit = createForClass('im.actor.model.modules', 'Messages/10/4', 563, Ljava_lang_Object_2_classLit);
function Messages$10$4$1(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(564, 1, $intern_20, Messages$10$4$1);
_.run = function run_8(){
  $onResult_1(this.val$callback2, $clinit_Boolean());
}
;
var Lim_actor_model_modules_Messages$10$4$1_2_classLit = createForClass('im.actor.model.modules', 'Messages/10/4/1', 564, Ljava_lang_Object_2_classLit);
function Messages$10$4$2(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(565, 1, $intern_20, Messages$10$4$2);
_.run = function run_9(){
  $invoke_3(this.val$callback2.val$error3.jso);
}
;
var Lim_actor_model_modules_Messages$10$4$2_2_classLit = createForClass('im.actor.model.modules', 'Messages/10/4/2', 565, Ljava_lang_Object_2_classLit);
function $start_2(this$static, callback){
  var apiPeer, group, outPeer, user;
  if (this$static.val$peer2.peerType == ($clinit_PeerType_0() , PRIVATE_0)) {
    user = dynamicCast($getValue_2(this$static.this$01.modules.users.users, fromInt(this$static.val$peer2.peerId)), 11);
    if (!user) {
      $runOnUiThread(new Messages$11$1(callback));
      return;
    }
    outPeer = new OutPeer_0(($clinit_PeerType() , PRIVATE), user.uid, user.accessHash);
    apiPeer = new Peer_0(PRIVATE, user.uid);
  }
   else if (this$static.val$peer2.peerType == GROUP_0) {
    group = dynamicCast($getValue_2(this$static.this$01.modules.groups.groups, fromInt(this$static.val$peer2.peerId)), 21);
    if (!group) {
      $runOnUiThread(new Messages$11$2(callback));
      return;
    }
    outPeer = new OutPeer_0(($clinit_PeerType() , GROUP), group.groupId, group.accessHash);
    apiPeer = new Peer_0(GROUP, group.groupId);
  }
   else {
    $runOnUiThread(new Messages$11$3(callback));
    return;
  }
  $request(this$static.this$01, new RequestClearChat_0(outPeer), new Messages$11$4(this$static, apiPeer, callback));
}

function Messages$11(this$0, val$peer){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
}

defineClass(566, 1, {}, Messages$11);
var Lim_actor_model_modules_Messages$11_2_classLit = createForClass('im.actor.model.modules', 'Messages/11', 566, Ljava_lang_Object_2_classLit);
function Messages$11$1(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(567, 1, $intern_20, Messages$11$1);
_.run = function run_10(){
  $onError_2(this.val$callback2, new RpcInternalException);
}
;
var Lim_actor_model_modules_Messages$11$1_2_classLit = createForClass('im.actor.model.modules', 'Messages/11/1', 567, Ljava_lang_Object_2_classLit);
function Messages$11$2(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(568, 1, $intern_20, Messages$11$2);
_.run = function run_11(){
  $onError_2(this.val$callback2, new RpcInternalException);
}
;
var Lim_actor_model_modules_Messages$11$2_2_classLit = createForClass('im.actor.model.modules', 'Messages/11/2', 568, Ljava_lang_Object_2_classLit);
function Messages$11$3(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(569, 1, $intern_20, Messages$11$3);
_.run = function run_12(){
  $onError_2(this.val$callback2, new RpcInternalException);
}
;
var Lim_actor_model_modules_Messages$11$3_2_classLit = createForClass('im.actor.model.modules', 'Messages/11/3', 569, Ljava_lang_Object_2_classLit);
function $onResult_8(this$static, response){
  $onUpdateReceived_0(this$static.this$11.this$01.modules.updates, new SeqUpdate_0(response.seq, response.state, 47, $toByteArray(new UpdateChatClear_0(this$static.val$apiPeer2))));
  $runOnUiThread(new Messages$11$4$1(this$static.val$callback3));
}

function Messages$11$4(this$1, val$apiPeer, val$callback){
  this.this$11 = this$1;
  this.val$apiPeer2 = val$apiPeer;
  this.val$callback3 = val$callback;
}

defineClass(570, 1, {}, Messages$11$4);
_.onError_0 = function onError_4(e){
  $runOnUiThread(new Messages$11$4$2(this.val$callback3));
}
;
_.onResult_0 = function onResult_4(response){
  $onResult_8(this, dynamicCast(response, 89));
}
;
var Lim_actor_model_modules_Messages$11$4_2_classLit = createForClass('im.actor.model.modules', 'Messages/11/4', 570, Ljava_lang_Object_2_classLit);
function Messages$11$4$1(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(571, 1, $intern_20, Messages$11$4$1);
_.run = function run_13(){
  $onResult_2(this.val$callback2, $clinit_Boolean());
}
;
var Lim_actor_model_modules_Messages$11$4$1_2_classLit = createForClass('im.actor.model.modules', 'Messages/11/4/1', 571, Ljava_lang_Object_2_classLit);
function Messages$11$4$2(val$callback){
  this.val$callback2 = val$callback;
}

defineClass(572, 1, $intern_20, Messages$11$4$2);
_.run = function run_14(){
  $invoke_3(this.val$callback2.val$error3.jso);
}
;
var Lim_actor_model_modules_Messages$11$4$2_2_classLit = createForClass('im.actor.model.modules', 'Messages/11/4/2', 572, Ljava_lang_Object_2_classLit);
function Messages$2(this$0){
  this.this$01 = this$0;
}

defineClass(551, 1, {}, Messages$2);
_.create_0 = function create_14(){
  return new DialogsHistoryActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$2_2_classLit = createForClass('im.actor.model.modules', 'Messages/2', 551, Ljava_lang_Object_2_classLit);
function Messages$3(this$0){
  this.this$01 = this$0;
}

defineClass(552, 1, {}, Messages$3);
_.create_0 = function create_15(){
  return new OwnReadActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$3_2_classLit = createForClass('im.actor.model.modules', 'Messages/3', 552, Ljava_lang_Object_2_classLit);
function Messages$4(this$0){
  this.this$01 = this$0;
}

defineClass(553, 1, {}, Messages$4);
_.create_0 = function create_16(){
  return new CursorReaderActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$4_2_classLit = createForClass('im.actor.model.modules', 'Messages/4', 553, Ljava_lang_Object_2_classLit);
function Messages$5(this$0){
  this.this$01 = this$0;
}

defineClass(554, 1, {}, Messages$5);
_.create_0 = function create_17(){
  return new CursorReceiverActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$5_2_classLit = createForClass('im.actor.model.modules', 'Messages/5', 554, Ljava_lang_Object_2_classLit);
function Messages$6(this$0){
  this.this$01 = this$0;
}

defineClass(555, 1, {}, Messages$6);
_.create_0 = function create_18(){
  return new SenderActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$6_2_classLit = createForClass('im.actor.model.modules', 'Messages/6', 555, Ljava_lang_Object_2_classLit);
function Messages$7(this$0){
  this.this$01 = this$0;
}

defineClass(556, 1, {}, Messages$7);
_.create_0 = function create_19(){
  return new MessageDeleteActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$7_2_classLit = createForClass('im.actor.model.modules', 'Messages/7', 556, Ljava_lang_Object_2_classLit);
function Messages$8(this$0, val$peer){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
}

defineClass(557, 1, {}, Messages$8);
_.create_0 = function create_20(){
  return new ConversationActor(this.val$peer2, this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$8_2_classLit = createForClass('im.actor.model.modules', 'Messages/8', 557, Ljava_lang_Object_2_classLit);
function Messages$9(this$0, val$peer){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
}

defineClass(558, 1, {}, Messages$9);
_.create_0 = function create_21(){
  return new ConversationHistoryActor(this.val$peer2, this.this$01.modules);
}
;
var Lim_actor_model_modules_Messages$9_2_classLit = createForClass('im.actor.model.modules', 'Messages/9', 558, Ljava_lang_Object_2_classLit);
function $onAppHidden(this$static){
  this$static.isAppVisible = false;
  if (this$static.presence) {
    $send_1(this$static.presence.myPresence, new OwnPresenceActor$OnAppHidden);
    $send_1(this$static.notifications.notificationsActor, new NotificationsActor$OnAppHidden);
  }
}

function $onAppVisible(this$static){
  this$static.isAppVisible = true;
  if (this$static.presence) {
    $send_1(this$static.presence.myPresence, new OwnPresenceActor$OnAppVisible);
    $send_1(this$static.notifications.notificationsActor, new NotificationsActor$OnAppVisible);
  }
}

function $onLoggedIn_0(this$static){
  var timing;
  timing = new Timing('ACCOUNT_CREATE');
  $section(timing, 'Users');
  this$static.users = new Users(this$static);
  $section(timing, 'Groups');
  this$static.groups = new Groups(this$static);
  $section(timing, 'Search');
  this$static.search = new SearchModule(this$static);
  $section(timing, 'Security');
  this$static.security = new Security(this$static);
  $section(timing, 'Messages');
  this$static.messages = new Messages(this$static);
  $section(timing, 'Updates');
  this$static.updates = new Updates(this$static);
  $section(timing, 'Presence');
  this$static.presence = new Presence(this$static);
  $section(timing, 'Typing');
  this$static.typing = new Typing(this$static);
  $section(timing, 'Files');
  this$static.filesModule = new Files(this$static);
  $section(timing, 'Notifications');
  this$static.notifications = new Notifications(this$static);
  $section(timing, 'Contacts');
  this$static.contacts = new Contacts(this$static);
  $section(timing, 'Settings');
  this$static.settings = new Settings(this$static);
  $section(timing, 'Profile');
  this$static.profile = new Profile(this$static);
  $end(timing);
  timing = new Timing('ACCOUNT_RUN');
  $section(timing, 'Settings');
  $run_6(this$static.settings);
  $section(timing, 'Files');
  $run_2(this$static.filesModule);
  $section(timing, 'Search');
  $run_5(this$static.search);
  $section(timing, 'Notifications');
  $run_4(this$static.notifications);
  $section(timing, 'AppState');
  $run(this$static.appStateModule);
  $section(timing, 'Contacts');
  $run_1(this$static.contacts);
  $section(timing, 'Messages');
  $run_3(this$static.messages);
  $section(timing, 'Updates');
  $run_7(this$static.updates);
  $section(timing, 'Presence');
  $send_1(this$static.presence.myPresence, new OwnPresenceActor$OnAppVisible);
  $end(timing);
  if (this$static.isAppVisible) {
    $send_1(this$static.presence.myPresence, new OwnPresenceActor$OnAppVisible);
    $send_1(this$static.notifications.notificationsActor, new NotificationsActor$OnAppVisible);
  }
   else {
    $send_1(this$static.notifications.notificationsActor, new NotificationsActor$OnAppHidden);
  }
}

function Modules(messenger, configuration){
  var timing;
  this.messenger = messenger;
  this.configuration = configuration;
  timing = new Timing('MODULES_INIT');
  $section(timing, 'I18N');
  this.i18nEngine = new I18nEngine(configuration.localeProvider, this);
  $section(timing, 'Preferences');
  this.preferences = new JsPreferencesStorage(configuration.storageProvider.storage);
  $section(timing, 'Analytics');
  new Analytics(this);
  $section(timing, 'API');
  this.actorApi = new ActorApi(new Endpoints(configuration.endpoints), new PreferenceApiStorage(this.preferences), new Modules$ActorApiCallbackImpl(this), configuration.networkProvider);
  $section(timing, 'Auth');
  this.auth = new Auth(this);
  $section(timing, 'Pushes');
  this.pushes = new Pushes(this);
  $section(timing, 'App State');
  this.appStateModule = new AppStateModule(this);
  $end(timing);
}

defineClass(573, 1, {}, Modules);
_.isAppVisible = false;
var Lim_actor_model_modules_Modules_2_classLit = createForClass('im.actor.model.modules', 'Modules', 573, Ljava_lang_Object_2_classLit);
function $onNewSessionCreated(this$static){
  !!this$static.this$01.updates && $send_1(this$static.this$01.updates.updateActor, new SequenceActor$Invalidate);
  !!this$static.this$01.presence && $send_1(this$static.this$01.presence.presence, new PresenceActor$SessionCreated);
}

function $onUpdateReceived(this$static, obj){
  !!this$static.this$01.updates && $onUpdateReceived_0(this$static.this$01.updates, obj);
}

function Modules$ActorApiCallbackImpl(this$0){
  this.this$01 = this$0;
}

defineClass(574, 1, {}, Modules$ActorApiCallbackImpl);
var Lim_actor_model_modules_Modules$ActorApiCallbackImpl_2_classLit = createForClass('im.actor.model.modules', 'Modules/ActorApiCallbackImpl', 574, Ljava_lang_Object_2_classLit);
function $onConversationClose(this$static, peer){
  $send_2(this$static.notificationsActor, new NotificationsActor$OnConversationHidden(peer), null);
}

function $onConversationOpen_0(this$static, peer){
  $send_2(this$static.notificationsActor, new NotificationsActor$OnConversationVisible(peer), null);
}

function $onInMessage(this$static, peer, sender, sortDate, contentDescription){
  $send_2(this$static.notificationsActor, new NotificationsActor$NewMessage(peer, sender, sortDate, contentDescription), null);
}

function $onOwnRead(this$static, peer, fromDate){
  $send_2(this$static.notificationsActor, new NotificationsActor$MessagesRead(peer, fromDate), null);
}

function $run_4(this$static){
  this$static.notificationsActor = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Notifications$1(this$static), null), 'actor/notifications');
}

function Notifications(modules){
  BaseModule.call(this, modules);
  this.notificationsStorage = new SyncKeyValue($createKeyValue(this.modules.configuration.storageProvider, 'notifications'));
}

defineClass(587, 25, {}, Notifications);
var Lim_actor_model_modules_Notifications_2_classLit = createForClass('im.actor.model.modules', 'Notifications', 587, Lim_actor_model_modules_BaseModule_2_classLit);
function Notifications$1(this$0){
  this.this$01 = this$0;
}

defineClass(589, 1, {}, Notifications$1);
_.create_0 = function create_22(){
  return new NotificationsActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Notifications$1_2_classLit = createForClass('im.actor.model.modules', 'Notifications/1', 589, Ljava_lang_Object_2_classLit);
function $subscribe_1(this$static, peer){
  $send_2(this$static.presence, new PresenceActor$Subscribe(peer), null);
}

function Presence(modules){
  BaseModule.call(this, modules);
  this.myPresence = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Presence$1(modules), null), 'actor/presence/own');
  this.presence = $actorOf_0((null , mainSystem), new Props(new PresenceActor$1(modules), new PresenceActor$2), 'actor/presence/users');
}

defineClass(584, 25, {}, Presence);
var Lim_actor_model_modules_Presence_2_classLit = createForClass('im.actor.model.modules', 'Presence', 584, Lim_actor_model_modules_BaseModule_2_classLit);
function Presence$1(val$modules){
  this.val$modules2 = val$modules;
}

defineClass(586, 1, {}, Presence$1);
_.create_0 = function create_23(){
  return new OwnPresenceActor(this.val$modules2);
}
;
var Lim_actor_model_modules_Presence$1_2_classLit = createForClass('im.actor.model.modules', 'Presence/1', 586, Ljava_lang_Object_2_classLit);
function Profile(modules){
  BaseModule.call(this, modules);
  new OwnAvatarVM;
  $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Profile$1(modules), null), 'actor/avatar/my');
}

defineClass(684, 25, {}, Profile);
var Lim_actor_model_modules_Profile_2_classLit = createForClass('im.actor.model.modules', 'Profile', 684, Lim_actor_model_modules_BaseModule_2_classLit);
function Profile$1(val$modules){
  this.val$modules2 = val$modules;
}

defineClass(686, 1, {}, Profile$1);
_.create_0 = function create_24(){
  return new OwnAvatarChangeActor(this.val$modules2);
}
;
var Lim_actor_model_modules_Profile$1_2_classLit = createForClass('im.actor.model.modules', 'Profile/1', 686, Ljava_lang_Object_2_classLit);
function Pushes(modules){
  BaseModule.call(this, modules);
  $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Pushes$1(this), null), 'actor/push');
}

defineClass(637, 25, {}, Pushes);
var Lim_actor_model_modules_Pushes_2_classLit = createForClass('im.actor.model.modules', 'Pushes', 637, Lim_actor_model_modules_BaseModule_2_classLit);
function Pushes$1(this$0){
  this.this$01 = this$0;
}

defineClass(639, 1, {}, Pushes$1);
_.create_0 = function create_25(){
  return new PushRegisterActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Pushes$1_2_classLit = createForClass('im.actor.model.modules', 'Pushes/1', 639, Ljava_lang_Object_2_classLit);
function $onContactsChanged(this$static, contacts){
  var i_0, res;
  res = initDim(I_classLit, $intern_4, 0, contacts.length, 7, 1);
  for (i_0 = 0; i_0 < res.length; i_0++) {
    res[i_0] = contacts[i_0].value_0;
  }
  $send_1(this$static.actorRef, new SearchActor$OnContactsUpdated(res));
}

function $onDialogsChanged(this$static, dialogs){
  $send_1(this$static.actorRef, new SearchActor$OnDialogsUpdated(dialogs));
}

function $run_5(this$static){
  this$static.actorRef = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new SearchModule$1(this$static), null), 'actor/search');
}

function SearchModule(modules){
  BaseModule.call(this, modules);
  this.searchList = new JsListEngine($createList(this.modules.configuration.storageProvider, 'search'), ($clinit_SearchEntity() , CREATOR_2));
}

defineClass(663, 25, {}, SearchModule);
var Lim_actor_model_modules_SearchModule_2_classLit = createForClass('im.actor.model.modules', 'SearchModule', 663, Lim_actor_model_modules_BaseModule_2_classLit);
function SearchModule$1(this$0){
  this.this$01 = this$0;
}

defineClass(665, 1, {}, SearchModule$1);
_.create_0 = function create_26(){
  return new SearchActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_SearchModule$1_2_classLit = createForClass('im.actor.model.modules', 'SearchModule/1', 665, Ljava_lang_Object_2_classLit);
function Security(modules){
  BaseModule.call(this, modules);
}

defineClass(666, 25, {}, Security);
var Lim_actor_model_modules_Security_2_classLit = createForClass('im.actor.model.modules', 'Security', 666, Lim_actor_model_modules_BaseModule_2_classLit);
function $getChatKey(peer){
  if (peer.peerType == ($clinit_PeerType_0() , PRIVATE_0)) {
    return 'PRIVATE_' + peer.peerId;
  }
   else if (peer.peerType == GROUP_0) {
    return 'GROUP_' + peer.peerId;
  }
   else {
    throw new RuntimeException_0('Unsupported peer');
  }
}

function $loadValue(this$static, key){
  var sValue;
  sValue = $getString(this$static.modules.preferences, key);
  return $equals_3('true', sValue) || $equals_3('false', sValue) || true;
}

function $run_6(this$static){
  $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Settings$1(this$static), null), 'actor/settings');
}

function Settings(modules){
  BaseModule.call(this, modules);
}

defineClass(681, 25, {}, Settings);
var Lim_actor_model_modules_Settings_2_classLit = createForClass('im.actor.model.modules', 'Settings', 681, Lim_actor_model_modules_BaseModule_2_classLit);
function Settings$1(this$0){
  this.this$01 = this$0;
}

defineClass(683, 1, {}, Settings$1);
_.create_0 = function create_27(){
  return new SettingsSyncActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Settings$1_2_classLit = createForClass('im.actor.model.modules', 'Settings/1', 683, Ljava_lang_Object_2_classLit);
function $getGroupTyping(this$static, gid){
  $containsKey(this$static.groups, valueOf(gid)) || $put_1(this$static.groups, valueOf(gid), new GroupTypingVM);
  return dynamicCast($get_2(this$static.groups, valueOf(gid)), 376);
}

function $getTyping(this$static, uid){
  $containsKey(this$static.uids, valueOf(uid)) || $put_1(this$static.uids, valueOf(uid), new UserTypingVM);
  return dynamicCast($get_2(this$static.uids, valueOf(uid)), 375);
}

function $onTyping_0(this$static, peer){
  $send_2(this$static.ownTypingActor, new OwnTypingActor$Typing(peer), null);
}

function Typing(messenger){
  BaseModule.call(this, messenger);
  this.uids = new HashMap;
  this.groups = new HashMap;
  this.ownTypingActor = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Typing$1(messenger), null), 'actor/typing/own');
  $actorOf_0((null , mainSystem), new Props(new TypingActor$1(messenger), new TypingActor$2), 'actor/typing');
}

defineClass(590, 25, {}, Typing);
var Lim_actor_model_modules_Typing_2_classLit = createForClass('im.actor.model.modules', 'Typing', 590, Lim_actor_model_modules_BaseModule_2_classLit);
function Typing$1(val$messenger){
  this.val$messenger2 = val$messenger;
}

defineClass(592, 1, {}, Typing$1);
_.create_0 = function create_28(){
  return new OwnTypingActor(this.val$messenger2);
}
;
var Lim_actor_model_modules_Typing$1_2_classLit = createForClass('im.actor.model.modules', 'Typing/1', 592, Ljava_lang_Object_2_classLit);
function $onUpdateReceived_0(this$static, update){
  $send_1(this$static.updateActor, update);
}

function $run_7(this$static){
  this$static.updateActor = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new Updates$1(this$static), null), 'actor/updates');
}

function Updates(messenger){
  BaseModule.call(this, messenger);
}

defineClass(667, 25, {}, Updates);
var Lim_actor_model_modules_Updates_2_classLit = createForClass('im.actor.model.modules', 'Updates', 667, Lim_actor_model_modules_BaseModule_2_classLit);
function Updates$1(this$0){
  this.this$01 = this$0;
}

defineClass(669, 1, {}, Updates$1);
_.create_0 = function create_29(){
  return new SequenceActor(this.this$01.modules);
}
;
var Lim_actor_model_modules_Updates$1_2_classLit = createForClass('im.actor.model.modules', 'Updates/1', 669, Ljava_lang_Object_2_classLit);
function Users(messenger){
  BaseModule.call(this, messenger);
  this.collection = new Users$1(this, $createKeyValue(messenger.configuration.storageProvider, 'users'));
  this.users = this.collection.proxyKeyValueEngine;
}

defineClass(617, 25, {}, Users);
var Lim_actor_model_modules_Users_2_classLit = createForClass('im.actor.model.modules', 'Users', 617, Lim_actor_model_modules_BaseModule_2_classLit);
function $createNew(this$static, raw){
  return new UserVM(raw, this$static.this$01.modules);
}

function $deserialize_1(raw){
  var e;
  try {
    return dynamicCast(parse_159(new User_0, raw), 11);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      return null;
    }
     else 
      throw unwrap($e0);
  }
}

function Users$1(this$0, $anonymous0){
  this.this$01 = this$0;
  MVVMCollection.call(this, $anonymous0);
}

defineClass(618, 448, {}, Users$1);
_.createNew = function createNew_0(raw){
  return $createNew(this, dynamicCast(raw, 11));
}
;
_.deserialize = function deserialize_1(raw){
  return $deserialize_1(raw);
}
;
_.serialize_0 = function serialize_181(raw){
  return $toByteArray(dynamicCast(raw, 11));
}
;
var Lim_actor_model_modules_Users$1_2_classLit = createForClass('im.actor.model.modules', 'Users/1', 618, Lim_actor_model_mvvm_MVVMCollection_2_classLit);
function $buidOutPeer(this$static, peer){
  var group, user;
  if (peer.peerType == ($clinit_PeerType_0() , PRIVATE_0)) {
    user = $getUser_1(this$static, peer.peerId);
    if (!user) {
      return null;
    }
    return new OutPeer_0(($clinit_PeerType() , PRIVATE), user.uid, user.accessHash);
  }
   else if (peer.peerType == GROUP_0) {
    group = $getGroup_1(this$static, peer.peerId);
    if (!group) {
      return null;
    }
    return new OutPeer_0(($clinit_PeerType() , GROUP), group.groupId, group.accessHash);
  }
   else {
    throw new RuntimeException_0('Unknown peer: ' + peer);
  }
}

function $getConversationActor_0(this$static, peer){
  return $getConversationActor(this$static.modules.messages, peer);
}

function $getGroup_1(this$static, gid){
  return dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(gid)), 21);
}

function $getUser_1(this$static, uid){
  return dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11);
}

function $getUserVM(this$static, uid){
  return dynamicCast($get_1(this$static.modules.users.collection, fromInt(uid)), 53);
}

function $media(this$static, peer){
  return $getMediaEngine(this$static.modules.messages, peer);
}

function $messages(this$static, peer){
  return $getConversationEngine(this$static.modules.messages, peer);
}

function $request_0(this$static, request, callback){
  $request_1(this$static.modules.actorApi, request, new ModuleActor$2(this$static, callback));
}

function ModuleActor(modules){
  this.modules = modules;
}

defineClass(27, 785, {});
var Lim_actor_model_modules_utils_ModuleActor_2_classLit = createForClass('im.actor.model.modules.utils', 'ModuleActor', 27, Lim_actor_model_droidkit_actors_Actor_2_classLit);
function GroupAvatarChangeActor(modules){
  ModuleActor.call(this, modules);
  new HashMap;
  new HashMap;
}

defineClass(613, 27, {}, GroupAvatarChangeActor);
_.onReceive = function onReceive_1(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
var Lim_actor_model_modules_avatar_GroupAvatarChangeActor_2_classLit = createForClass('im.actor.model.modules.avatar', 'GroupAvatarChangeActor', 613, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function OwnAvatarChangeActor(modules){
  ModuleActor.call(this, modules);
}

defineClass(685, 27, {}, OwnAvatarChangeActor);
_.onReceive = function onReceive_2(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
var Lim_actor_model_modules_avatar_OwnAvatarChangeActor_2_classLit = createForClass('im.actor.model.modules.avatar', 'OwnAvatarChangeActor', 685, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $isImported(this$static, phone){
  return $getBool(this$static.modules.preferences, 'book_phone_' + toString_2(phone), false);
}

function $isImported_0(this$static){
  return $getBool(this$static.modules.preferences, 'book_email_' + null.nullMethod(), false);
}

function $markImported(this$static, phone){
  $putBool(this$static.modules.preferences, 'book_phone_' + toString_2(phone), true);
}

function $markImported_0(this$static, email){
  $putBool(this$static.modules.preferences, 'book_email_' + email.toLowerCase(), true);
}

function $onPhoneBookLoaded(this$static, phoneBook){
  var count, emailToImport, emailToImport$iterator, emailToImports, emailToImportsPart, phoneToImport, phoneToImport$iterator, phoneToImports, phoneToImportsPart, record$iterator;
  this$static.isSyncInProgress = false;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsImport' + ':' + 'Book load completed');
  phoneToImports = new ArrayList;
  emailToImports = new ArrayList;
  for (record$iterator = new AbstractList$IteratorImpl(phoneBook); record$iterator.i < record$iterator.this$01.size_1();) {
    checkCriticalElement(record$iterator.i < record$iterator.this$01.size_1());
    throwClassCastExceptionUnlessNull(record$iterator.this$01.get_1(record$iterator.last = record$iterator.i++));
    for (null.nullMethod().nullMethod(); null.nullMethod();) {
      null.nullMethod();
      if ($isImported(this$static, null.nullMethod())) {
        continue;
      }
      if ($contains_0(this$static.importingPhones, valueOf_0(null.nullMethod()))) {
        continue;
      }
      $add_1(this$static.importingPhones, valueOf_0(null.nullMethod()));
      $add_0(phoneToImports, new PhoneToImport_0(null.nullMethod(), null.nullMethod()));
    }
    for (null.nullMethod().nullMethod(); null.nullMethod();) {
      null.nullMethod();
      if ($isImported_0(this$static, null.nullMethod().nullMethod())) {
        continue;
      }
      if ($contains_0(this$static.importingEmails, null.nullMethod().nullMethod())) {
        continue;
      }
      $add_1(this$static.importingEmails, null.nullMethod().nullMethod());
      $add_0(emailToImports, new EmailToImport_0(null.nullMethod().nullMethod(), null.nullMethod()));
    }
  }
  if (phoneToImports.array.length == 0 && emailToImports.array.length == 0) {
    this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsImport' + ':' + 'No new contacts found');
    $send_1(this$static.modules.appStateModule.listStatesActor, new ListsStatesActor$OnBookImported);
    return;
  }
   else {
    this$static.ENABLE_LOG && d_0('ContactsImport', 'Founded new ' + (phoneToImports.array.length + emailToImports.array.length) + ' contact records');
  }
  phoneToImportsPart = new ArrayList;
  emailToImportsPart = new ArrayList;
  count = 0;
  for (phoneToImport$iterator = new AbstractList$IteratorImpl(phoneToImports); phoneToImport$iterator.i < phoneToImport$iterator.this$01.size_1();) {
    phoneToImport = (checkCriticalElement(phoneToImport$iterator.i < phoneToImport$iterator.this$01.size_1()) , dynamicCast(phoneToImport$iterator.this$01.get_1(phoneToImport$iterator.last = phoneToImport$iterator.i++), 183));
    setCheck(phoneToImportsPart.array, phoneToImportsPart.array.length, phoneToImport);
    ++count;
    if (count >= 50) {
      $performImport(this$static, phoneToImportsPart, emailToImportsPart);
      phoneToImportsPart.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
      emailToImportsPart.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
      count = 0;
    }
  }
  for (emailToImport$iterator = new AbstractList$IteratorImpl(emailToImports); emailToImport$iterator.i < emailToImport$iterator.this$01.size_1();) {
    emailToImport = (checkCriticalElement(emailToImport$iterator.i < emailToImport$iterator.this$01.size_1()) , dynamicCast(emailToImport$iterator.this$01.get_1(emailToImport$iterator.last = emailToImport$iterator.i++), 184));
    setCheck(emailToImportsPart.array, emailToImportsPart.array.length, emailToImport);
    ++count;
    if (count >= 50) {
      $performImport(this$static, phoneToImportsPart, emailToImportsPart);
      phoneToImportsPart.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
      emailToImportsPart.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
      count = 0;
    }
  }
  count > 0 && $performImport(this$static, phoneToImportsPart, emailToImportsPart);
}

function $performImport(this$static, phoneToImportsPart, emailToImportsPart){
  var emailToImports, phones;
  this$static.ENABLE_LOG && d_0('ContactsImport', 'Performing import part with ' + phoneToImportsPart.array.length + ' phones and ' + emailToImportsPart.array.length + ' emails');
  phones = dynamicCast($toArray_0(phoneToImportsPart, initDim(Lim_actor_model_api_PhoneToImport_2_classLit, {810:1, 3:1, 6:1}, 183, phoneToImportsPart.array.length, 0, 1)), 810);
  emailToImports = dynamicCast($toArray_0(emailToImportsPart, initDim(Lim_actor_model_api_EmailToImport_2_classLit, {811:1, 3:1, 6:1}, 184, emailToImportsPart.array.length, 0, 1)), 811);
  $request_0(this$static, new RequestImportContacts_0(new ArrayList_0(phoneToImportsPart), new ArrayList_0(emailToImportsPart)), new BookImportActor$2(this$static, phones, emailToImports));
}

function $performSync(this$static){
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsImport' + ':' + 'Checking sync...');
  if (this$static.isSyncInProgress) {
    this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsImport' + ':' + 'Sync already in progress');
    return;
  }
  this$static.isSyncInProgress = true;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsImport' + ':' + 'Starting book loading...');
  $onLoaded(new BookImportActor$1(this$static), new ArrayList);
}

function BookImportActor(messenger){
  ModuleActor.call(this, messenger);
  this.importingPhones = new HashSet;
  this.importingEmails = new HashSet;
  this.ENABLE_LOG = messenger.configuration.enableContactsLogging;
}

defineClass(677, 27, {}, BookImportActor);
_.onReceive = function onReceive_3(message){
  instanceOf(message, 361)?$performSync(this):instanceOf(message, 258)?$onPhoneBookLoaded(this, dynamicCast(message, 258).phoneBook):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
_.preStart = function preStart_0(){
  $send_1(this.context.actorScope.actorRef, new BookImportActor$PerformSync);
}
;
_.ENABLE_LOG = false;
_.isSyncInProgress = false;
var Lim_actor_model_modules_contacts_BookImportActor_2_classLit = createForClass('im.actor.model.modules.contacts', 'BookImportActor', 677, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onLoaded(this$static, contacts){
  $send_1(this$static.this$01.context.actorScope.actorRef, new BookImportActor$PhoneBookLoaded(contacts));
}

function BookImportActor$1(this$0){
  this.this$01 = this$0;
}

defineClass(694, 1, {}, BookImportActor$1);
var Lim_actor_model_modules_contacts_BookImportActor$1_2_classLit = createForClass('im.actor.model.modules.contacts', 'BookImportActor/1', 694, Ljava_lang_Object_2_classLit);
function $onResult_9(this$static, response){
  var emailToImport, emailToImport$array, emailToImport$index, emailToImport$max, phoneToImport, phoneToImport$array, phoneToImport$index, phoneToImport$max, u, u$iterator, uids;
  for (phoneToImport$array = this$static.val$phones2 , phoneToImport$index = 0 , phoneToImport$max = phoneToImport$array.length; phoneToImport$index < phoneToImport$max; ++phoneToImport$index) {
    phoneToImport = phoneToImport$array[phoneToImport$index];
    $markImported(this$static.this$01, phoneToImport.phoneNumber);
    $remove_5(this$static.this$01.importingPhones, valueOf_0(phoneToImport.phoneNumber));
  }
  for (emailToImport$array = this$static.val$emailToImports3 , emailToImport$index = 0 , emailToImport$max = emailToImport$array.length; emailToImport$index < emailToImport$max; ++emailToImport$index) {
    emailToImport = emailToImport$array[emailToImport$index];
    $markImported_0(this$static.this$01, emailToImport.email);
    $remove_5(this$static.this$01.importingEmails, emailToImport.email);
  }
  this$static.this$01.importingEmails.map_0.size_0 == 0 && this$static.this$01.importingPhones.map_0.size_0 == 0 && $send_1(this$static.this$01.modules.appStateModule.listStatesActor, new ListsStatesActor$OnBookImported);
  if (response.users.array.length == 0) {
    this$static.this$01.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsImport' + ':' + 'Import success, but no new contacts found');
    return;
  }
  this$static.this$01.ENABLE_LOG && d_0('ContactsImport', 'Import success with ' + response.users.array.length + ' new contacts');
  uids = new ArrayList;
  for (u$iterator = new AbstractList$IteratorImpl(response.users); u$iterator.i < u$iterator.this$01.size_1();) {
    u = (checkCriticalElement(u$iterator.i < u$iterator.this$01.size_1()) , dynamicCast(u$iterator.this$01.get_1(u$iterator.last = u$iterator.i++), 58));
    $add_0(uids, valueOf(u.id_0));
  }
  $onUpdateReceived_0(this$static.this$01.modules.updates, new FatSeqUpdate_0(response.seq, response.state, $toByteArray(new UpdateContactsAdded_0(uids)), response.users, new ArrayList, new ArrayList, new ArrayList));
}

function BookImportActor$2(this$0, val$phones, val$emailToImports){
  this.this$01 = this$0;
  this.val$phones2 = val$phones;
  this.val$emailToImports3 = val$emailToImports;
}

defineClass(695, 1, {}, BookImportActor$2);
_.onError_0 = function onError_5(e){
  this.this$01.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsImport' + ':' + 'Import failure');
  $printStackTrace(e, ($clinit_System() , err));
}
;
_.onResult_0 = function onResult_5(response){
  $onResult_9(this, dynamicCast(response, 317));
}
;
var Lim_actor_model_modules_contacts_BookImportActor$2_2_classLit = createForClass('im.actor.model.modules.contacts', 'BookImportActor/2', 695, Ljava_lang_Object_2_classLit);
function BookImportActor$PerformSync(){
}

defineClass(361, 1, {361:1}, BookImportActor$PerformSync);
var Lim_actor_model_modules_contacts_BookImportActor$PerformSync_2_classLit = createForClass('im.actor.model.modules.contacts', 'BookImportActor/PerformSync', 361, Ljava_lang_Object_2_classLit);
function BookImportActor$PhoneBookLoaded(phoneBook){
  this.phoneBook = phoneBook;
}

defineClass(258, 1, {258:1}, BookImportActor$PhoneBookLoaded);
var Lim_actor_model_modules_contacts_BookImportActor$PhoneBookLoaded_2_classLit = createForClass('im.actor.model.modules.contacts', 'BookImportActor/PhoneBookLoaded', 258, Ljava_lang_Object_2_classLit);
function $onContactsAdded(this$static, uids){
  var uid, uid$index, uid$max;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'OnContactsAdded received');
  for (uid$index = 0 , uid$max = uids.length; uid$index < uid$max; ++uid$index) {
    uid = uids[uid$index];
    this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + ('Adding: #' + uid));
    $add_0(this$static.contacts, valueOf(uid));
    $markContact(this$static.modules.contacts, uid);
    $change(dynamicCast($get_1(this$static.modules.users.collection, fromInt(uid)), 53).isContact, ($clinit_Boolean() , $clinit_Boolean() , TRUE));
  }
  $saveList(this$static);
  $updateEngineList(this$static);
  $send_1(this$static.context.actorScope.actorRef, new ContactsSyncActor$PerformSync);
}

function $onContactsLoaded(this$static, result){
  var u, u$iterator, u$iterator0, uid, uid$array, uid$index, uid$max;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Sync result received');
  this$static.isInProgress = false;
  $send_1(this$static.modules.appStateModule.listStatesActor, new ListsStatesActor$OnContactsLoaded);
  if (result.isNotChanged) {
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Sync: Not changed');
    this$static.isInvalidated && $performSync_0(this$static);
    return;
  }
  this$static.ENABLE_LOG && d_0('ContactsServerSync', 'Sync received ' + result.users.array.length + ' contacts');
  outer: for (uid$array = dynamicCast($toArray_0(this$static.contacts, initDim(Ljava_lang_Integer_2_classLit, $intern_22, 33, this$static.contacts.array.length, 0, 1)), 91) , uid$index = 0 , uid$max = uid$array.length; uid$index < uid$max; ++uid$index) {
    uid = uid$array[uid$index];
    for (u$iterator0 = new AbstractList$IteratorImpl(result.users); u$iterator0.i < u$iterator0.this$01.size_1();) {
      u = (checkCriticalElement(u$iterator0.i < u$iterator0.this$01.size_1()) , dynamicCast(u$iterator0.this$01.get_1(u$iterator0.last = u$iterator0.i++), 58));
      if (u.id_0 == uid.value_0) {
        continue outer;
      }
    }
    this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + ('Removing: #' + uid));
    $remove_4(this$static.contacts, uid);
    !!$getUser_1(this$static, uid.value_0) && $change($getUserVM(this$static, uid.value_0).isContact, ($clinit_Boolean() , $clinit_Boolean() , FALSE));
    $markNonContact(this$static.modules.contacts, uid.value_0);
  }
  for (u$iterator = new AbstractList$IteratorImpl(result.users); u$iterator.i < u$iterator.this$01.size_1();) {
    u = (checkCriticalElement(u$iterator.i < u$iterator.this$01.size_1()) , dynamicCast(u$iterator.this$01.get_1(u$iterator.last = u$iterator.i++), 58));
    if ($indexOf_0(this$static.contacts, valueOf(u.id_0), 0) != -1) {
      continue;
    }
    this$static.ENABLE_LOG && d_0('ContactsServerSync', 'Adding: #' + u.id_0);
    $add_0(this$static.contacts, valueOf(u.id_0));
    !!$getUser_1(this$static, u.id_0) && $change($getUserVM(this$static, u.id_0).isContact, ($clinit_Boolean() , $clinit_Boolean() , TRUE));
    $markContact(this$static.modules.contacts, u.id_0);
  }
  $saveList(this$static);
  $updateEngineList(this$static);
  this$static.isInvalidated && $send_1(this$static.context.actorScope.actorRef, new ContactsSyncActor$PerformSync);
}

function $onContactsRemoved(this$static, uids){
  var uid, uid$index, uid$max;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'OnContactsRemoved received');
  for (uid$index = 0 , uid$max = uids.length; uid$index < uid$max; ++uid$index) {
    uid = uids[uid$index];
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + ('Removing: #' + uid));
    $remove_4(this$static.contacts, valueOf(uid));
    $markNonContact(this$static.modules.contacts, uid);
    $change(dynamicCast($get_1(this$static.modules.users.collection, fromInt(uid)), 53).isContact, ($clinit_Boolean() , $clinit_Boolean() , FALSE));
  }
  $saveList(this$static);
  $updateEngineList(this$static);
  $send_1(this$static.context.actorScope.actorRef, new ContactsSyncActor$PerformSync);
}

function $onUserChanged(this$static, user){
  this$static.ENABLE_LOG && d_0('ContactsServerSync', 'OnUserChanged #' + user.uid + ' received');
  if ($indexOf_0(this$static.contacts, valueOf(user.uid), 0) == -1) {
    return;
  }
  $updateEngineList(this$static);
}

function $performSync_0(this$static){
  var hash, hashData, hashValue, u, u$index, u$max, uids;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Checking sync');
  if (this$static.isInProgress) {
    this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Sync in progress, invalidating current sync');
    this$static.isInvalidated = true;
    return;
  }
  this$static.isInProgress = true;
  this$static.isInvalidated = false;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Starting sync');
  uids = dynamicCast($toArray_0(this$static.contacts, initDim(Ljava_lang_Integer_2_classLit, $intern_22, 33, 0, 0, 1)), 91);
  mergeSort(uids, 0, uids.length, ($clinit_Comparators() , $clinit_Comparators() , NATURAL));
  hash = '';
  for (u$index = 0 , u$max = uids.length; u$index < u$max; ++u$index) {
    u = fromInt(uids[u$index].value_0);
    hash.length != 0 && (hash += ',');
    hash += toString_2(u);
  }
  try {
    hashData = getBytesUtf8(hash);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (!instanceOf($e0, 355))
      throw unwrap($e0);
  }
  hashValue = hex(($clinit_CryptoUtils() , $SHA256(hashData)));
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + ('Performing sync with uids: ' + hash));
  d_0('ContactsServerSync', 'Performing sync with hash: ' + hashValue + ', hashData:' + hashData.length);
  $request_0(this$static, new RequestGetContacts_0(hashValue), new ContactsSyncActor$1(this$static));
}

function $saveList(this$static){
  var dataOutput, l, l$iterator;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Saving contacts ids to storage');
  dataOutput = new DataOutput;
  $writeInt_0(dataOutput, this$static.contacts.array.length);
  for (l$iterator = new AbstractList$IteratorImpl(this$static.contacts); l$iterator.i < l$iterator.this$01.size_1();) {
    l = (checkCriticalElement(l$iterator.i < l$iterator.this$01.size_1()) , dynamicCast(l$iterator.this$01.get_1(l$iterator.last = l$iterator.i++), 33)).value_0;
    $writeInt_0(dataOutput, l);
  }
  $putBytes(this$static.modules.preferences, 'contact_list', $toByteArray_0(dataOutput));
}

function $updateEngineList(this$static){
  var contact, index_0, registeredContacts, sindex, sorted, u, u$iterator, userList, userModel, userModel$iterator, userModel$iterator0;
  this$static.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Saving contact EngineList');
  userList = new ArrayList;
  for (u$iterator = new AbstractList$IteratorImpl(this$static.contacts); u$iterator.i < u$iterator.this$01.size_1();) {
    u = (checkCriticalElement(u$iterator.i < u$iterator.this$01.size_1()) , dynamicCast(u$iterator.this$01.get_1(u$iterator.last = u$iterator.i++), 33)).value_0;
    $add_0(userList, dynamicCast($getValue_2(this$static.modules.users.users, fromInt(u)), 11));
  }
  sort_0(userList, new ContactsSyncActor$2);
  registeredContacts = new ArrayList;
  index_0 = -1;
  for (userModel$iterator0 = new AbstractList$IteratorImpl(userList); userModel$iterator0.i < userModel$iterator0.this$01.size_1();) {
    userModel = (checkCriticalElement(userModel$iterator0.i < userModel$iterator0.this$01.size_1()) , dynamicCast(userModel$iterator0.this$01.get_1(userModel$iterator0.last = userModel$iterator0.i++), 11));
    contact = new Contact_0(userModel.uid, fromInt(index_0--), userModel.avatar, userModel.localName == null?userModel.name_0:userModel.localName);
    setCheck(registeredContacts.array, registeredContacts.array.length, contact);
  }
  $replaceItems(this$static.modules.contacts.contacts, registeredContacts);
  sorted = initDim(Ljava_lang_Integer_2_classLit, $intern_22, 33, this$static.contacts.array.length, 0, 1);
  sindex = 0;
  for (userModel$iterator = new AbstractList$IteratorImpl(userList); userModel$iterator.i < userModel$iterator.this$01.size_1();) {
    userModel = (checkCriticalElement(userModel$iterator.i < userModel$iterator.this$01.size_1()) , dynamicCast(userModel$iterator.this$01.get_1(userModel$iterator.last = userModel$iterator.i++), 11));
    sorted[sindex++] = valueOf(userModel.uid);
  }
  $onContactsChanged(this$static.modules.search, sorted);
  $onContactsUpdate(this$static.modules.appStateModule, this$static.modules.contacts.contacts.storage.index_0.array.length == 0);
}

function ContactsSyncActor(messenger){
  ModuleActor.call(this, messenger);
  this.contacts = new ArrayList;
  this.ENABLE_LOG = messenger.configuration.enableContactsLogging;
}

defineClass(678, 27, {}, ContactsSyncActor);
_.onReceive = function onReceive_4(message){
  instanceOf(message, 259)?$onContactsLoaded(this, dynamicCast(message, 259).result):instanceOf(message, 260)?$onContactsAdded(this, dynamicCast(message, 260).uids):instanceOf(message, 261)?$onContactsRemoved(this, dynamicCast(message, 261).uids):instanceOf(message, 100)?$onUserChanged(this, dynamicCast(message, 100).user):instanceOf(message, 127)?$performSync_0(this):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
_.preStart = function preStart_1(){
  var count, data_0, dataInput, e, i_0;
  this.ENABLE_LOG && !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ContactsServerSync' + ':' + 'Loading contacts ids from storage...');
  data_0 = $getBytes_1(this.modules.preferences, 'contact_list');
  if (data_0 != null) {
    try {
      dataInput = new DataInput_0(data_0, 0, data_0.length);
      count = $readInt(dataInput);
      for (i_0 = 0; i_0 < count; i_0++) {
        $add_0(this.contacts, valueOf($readInt(dataInput)));
      }
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
  $onContactsUpdate(this.modules.appStateModule, this.modules.contacts.contacts.storage.index_0.array.length == 0);
  $send_1(this.context.actorScope.actorRef, new ContactsSyncActor$PerformSync);
}
;
_.ENABLE_LOG = false;
_.isInProgress = false;
_.isInvalidated = false;
var Lim_actor_model_modules_contacts_ContactsSyncActor_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor', 678, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_10(this$static, response){
  $onUpdateReceived_0(this$static.this$01.modules.updates, new ContactsLoaded(response));
}

function ContactsSyncActor$1(this$0){
  this.this$01 = this$0;
}

defineClass(696, 1, {}, ContactsSyncActor$1);
_.onError_0 = function onError_6(e){
  this.this$01.isInProgress = false;
  $printStackTrace(e, ($clinit_System() , err));
}
;
_.onResult_0 = function onResult_6(response){
  $onResult_10(this, dynamicCast(response, 284));
}
;
var Lim_actor_model_modules_contacts_ContactsSyncActor$1_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor/1', 696, Ljava_lang_Object_2_classLit);
function $compare_1(lhs, rhs){
  return compareTo_4(lhs.localName == null?lhs.name_0:lhs.localName, rhs.localName == null?rhs.name_0:rhs.localName);
}

function ContactsSyncActor$2(){
}

defineClass(697, 1, {}, ContactsSyncActor$2);
_.compare = function compare_2(lhs, rhs){
  return $compare_1(dynamicCast(lhs, 11), dynamicCast(rhs, 11));
}
;
var Lim_actor_model_modules_contacts_ContactsSyncActor$2_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor/2', 697, Ljava_lang_Object_2_classLit);
function ContactsSyncActor$ContactsAdded(uids){
  this.uids = uids;
}

defineClass(260, 1, {260:1}, ContactsSyncActor$ContactsAdded);
var Lim_actor_model_modules_contacts_ContactsSyncActor$ContactsAdded_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor/ContactsAdded', 260, Ljava_lang_Object_2_classLit);
function ContactsSyncActor$ContactsLoaded(result){
  this.result = result;
}

defineClass(259, 1, {259:1}, ContactsSyncActor$ContactsLoaded);
var Lim_actor_model_modules_contacts_ContactsSyncActor$ContactsLoaded_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor/ContactsLoaded', 259, Ljava_lang_Object_2_classLit);
function ContactsSyncActor$ContactsRemoved(uids){
  this.uids = uids;
}

defineClass(261, 1, {261:1}, ContactsSyncActor$ContactsRemoved);
var Lim_actor_model_modules_contacts_ContactsSyncActor$ContactsRemoved_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor/ContactsRemoved', 261, Ljava_lang_Object_2_classLit);
function ContactsSyncActor$PerformSync(){
}

defineClass(127, 1, {127:1}, ContactsSyncActor$PerformSync);
var Lim_actor_model_modules_contacts_ContactsSyncActor$PerformSync_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor/PerformSync', 127, Ljava_lang_Object_2_classLit);
function ContactsSyncActor$UserChanged(user){
  this.user = user;
}

defineClass(100, 1, {100:1}, ContactsSyncActor$UserChanged);
var Lim_actor_model_modules_contacts_ContactsSyncActor$UserChanged_2_classLit = createForClass('im.actor.model.modules.contacts', 'ContactsSyncActor/UserChanged', 100, Ljava_lang_Object_2_classLit);
function DownloadManager(messenger){
  ModuleActor.call(this, messenger);
  new ArrayList;
}

defineClass(671, 27, {}, DownloadManager);
_.onReceive = function onReceive_5(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
_.preStart = function preStart_2(){
}
;
var Lim_actor_model_modules_file_DownloadManager_2_classLit = createForClass('im.actor.model.modules.file', 'DownloadManager', 671, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function UploadManager(messenger){
  ModuleActor.call(this, messenger);
  new ArrayList;
}

defineClass(672, 27, {}, UploadManager);
_.onReceive = function onReceive_6(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
var Lim_actor_model_modules_file_UploadManager_2_classLit = createForClass('im.actor.model.modules.file', 'UploadManager', 672, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onHistoryLoaded(this$static, history_0){
  var historyMessage, historyMessage$iterator, isPendingChanged, updated, updatedMedia, updatedMsg, updatedMsg$iterator;
  updated = new ArrayList;
  isPendingChanged = false;
  for (historyMessage$iterator = new AbstractList$IteratorImpl(history_0); historyMessage$iterator.i < historyMessage$iterator.this$01.size_1();) {
    historyMessage = (checkCriticalElement(historyMessage$iterator.i < historyMessage$iterator.this$01.size_1()) , dynamicCast(historyMessage$iterator.this$01.get_1(historyMessage$iterator.last = historyMessage$iterator.i++), 26));
    if ($getValue_1(this$static.messages, historyMessage.rid)) {
      continue;
    }
    setCheck(updated.array, updated.array.length, historyMessage);
    if (historyMessage.messageState == ($clinit_MessageState_0() , SENT_0)) {
      $add_0(this$static.messagesStorage.messages, new OutUnreadMessage_0(historyMessage.rid, historyMessage.date));
      isPendingChanged = true;
    }
  }
  isPendingChanged && $put_0(this$static.pendingKeyValue, $getUnuqueId(this$static.peer), $toByteArray(this$static.messagesStorage));
  if (updated.array.length > 0) {
    $addOrUpdateItems_0(this$static.messages, updated);
    updatedMedia = new ArrayList;
    new ArrayList;
    for (updatedMsg$iterator = new AbstractList$IteratorImpl(updated); updatedMsg$iterator.i < updatedMsg$iterator.this$01.size_1();) {
      updatedMsg = (checkCriticalElement(updatedMsg$iterator.i < updatedMsg$iterator.this$01.size_1()) , dynamicCast(updatedMsg$iterator.this$01.get_1(updatedMsg$iterator.last = updatedMsg$iterator.i++), 26));
      instanceOf(updatedMsg.content_0, 62) || instanceOf(updatedMsg.content_0, 63)?(setCheck(updatedMedia.array, updatedMedia.array.length, updatedMsg) , true):updatedMsg.content_0;
    }
    $addOrUpdateItems_0(this$static.media_0, updatedMedia);
  }
}

function $onInMessage_0(this$static, message){
  if ($getValue_1(this$static.messages, message.rid)) {
    return;
  }
  $addOrUpdateItem_0(this$static.messages, message);
  instanceOf(message.content_0, 62) || instanceOf(message.content_0, 63)?$addOrUpdateItem_0(this$static.media_0, message):message.content_0;
  $send_1(this$static.dialogsActor, new DialogsActor$InMessage(this$static.peer, message));
  if (message.senderId == this$static.modules.auth.myUid) {
    $add_0(this$static.messagesStorage.messages, new OutUnreadMessage_0(message.rid, message.date));
    $put_0(this$static.pendingKeyValue, $getUnuqueId(this$static.peer), $toByteArray(this$static.messagesStorage));
  }
}

function $onMessageError(this$static, rid){
  var msg, updatedMsg;
  msg = dynamicCast($getValue_1(this$static.messages, rid), 26);
  if (!!msg && (msg.messageState == ($clinit_MessageState_0() , PENDING) || msg.messageState == SENT_0)) {
    updatedMsg = $changeState(msg, ($clinit_MessageState_0() , ERROR));
    $addOrUpdateItem_0(this$static.messages, updatedMsg);
    instanceOf(updatedMsg.content_0, 62) || instanceOf(updatedMsg.content_0, 63)?$addOrUpdateItem_0(this$static.media_0, updatedMsg):updatedMsg.content_0;
    $send_1(this$static.dialogsActor, new DialogsActor$MessageStateChanged(this$static.peer, rid, ERROR));
  }
}

function $onMessagePlainRead(this$static, date){
  var msg, p_0, p$array, p$index, p$max, removed, updatedMsg;
  removed = false;
  for (p$array = dynamicCast($toArray_0(this$static.messagesStorage.messages, initDim(Lim_actor_model_modules_messages_entity_OutUnreadMessage_2_classLit, {809:1, 3:1, 6:1}, 82, 0, 0, 1)), 809) , p$index = 0 , p$max = p$array.length; p$index < p$max; ++p$index) {
    p_0 = p$array[p$index];
    if (lte(p_0.date, date)) {
      msg = dynamicCast($getValue_1(this$static.messages, p_0.rid), 26);
      if (!!msg && (msg.messageState == ($clinit_MessageState_0() , SENT_0) || msg.messageState == RECEIVED_0)) {
        updatedMsg = $changeState(msg, ($clinit_MessageState_0() , READ_0));
        $addOrUpdateItem_0(this$static.messages, updatedMsg);
        instanceOf(updatedMsg.content_0, 62) || instanceOf(updatedMsg.content_0, 63)?$addOrUpdateItem_0(this$static.media_0, updatedMsg):updatedMsg.content_0;
        $send_1(this$static.dialogsActor, new DialogsActor$MessageStateChanged(this$static.peer, p_0.rid, READ_0));
        removed = true;
        $remove_4(this$static.messagesStorage.messages, p_0);
      }
    }
  }
  removed && $put_0(this$static.pendingKeyValue, $getUnuqueId(this$static.peer), $toByteArray(this$static.messagesStorage));
}

function $onMessagePlainReceived(this$static, date){
  var msg, p_0, p$iterator, updatedMsg;
  for (p$iterator = new AbstractList$IteratorImpl(this$static.messagesStorage.messages); p$iterator.i < p$iterator.this$01.size_1();) {
    p_0 = (checkCriticalElement(p$iterator.i < p$iterator.this$01.size_1()) , dynamicCast(p$iterator.this$01.get_1(p$iterator.last = p$iterator.i++), 82));
    if (lte(p_0.date, date)) {
      msg = dynamicCast($getValue_1(this$static.messages, p_0.rid), 26);
      if (!!msg && msg.messageState == ($clinit_MessageState_0() , SENT_0)) {
        updatedMsg = $changeState(msg, ($clinit_MessageState_0() , RECEIVED_0));
        $addOrUpdateItem_0(this$static.messages, updatedMsg);
        instanceOf(updatedMsg.content_0, 62) || instanceOf(updatedMsg.content_0, 63)?$addOrUpdateItem_0(this$static.media_0, updatedMsg):updatedMsg.content_0;
        $send_1(this$static.dialogsActor, new DialogsActor$MessageStateChanged(this$static.peer, p_0.rid, RECEIVED_0));
      }
    }
  }
}

function $onMessageSent(this$static, rid, date){
  var msg, p_0, p$iterator, updatedMsg;
  msg = dynamicCast($getValue_1(this$static.messages, rid), 26);
  if (!!msg && msg.messageState == ($clinit_MessageState_0() , PENDING)) {
    for (p$iterator = new AbstractList$IteratorImpl(this$static.messagesStorage.messages); p$iterator.i < p$iterator.this$01.size_1();) {
      p_0 = (checkCriticalElement(p$iterator.i < p$iterator.this$01.size_1()) , dynamicCast(p$iterator.this$01.get_1(p$iterator.last = p$iterator.i++), 82));
      if (eq(p_0.rid, rid)) {
        $remove_4(this$static.messagesStorage.messages, p_0);
        $add_0(this$static.messagesStorage.messages, new OutUnreadMessage_0(rid, date));
        break;
      }
    }
    $put_0(this$static.pendingKeyValue, $getUnuqueId(this$static.peer), $toByteArray(this$static.messagesStorage));
    updatedMsg = $changeState(new Message_0(msg.rid, msg.sortDate, date, msg.senderId, msg.messageState, msg.content_0), ($clinit_MessageState_0() , SENT_0));
    $addOrUpdateItem_0(this$static.messages, updatedMsg);
    instanceOf(updatedMsg.content_0, 62) || instanceOf(updatedMsg.content_0, 63)?$addOrUpdateItem_0(this$static.media_0, updatedMsg):updatedMsg.content_0;
    $send_1(this$static.dialogsActor, new DialogsActor$MessageSent(this$static.peer, rid, date));
  }
}

function $onMessagesDeleted(this$static, rids){
  var i_0, rids2, topMessage;
  rids2 = initDim(J_classLit, $intern_4, 0, rids.array.length, 6, 1);
  for (i_0 = 0; i_0 < rids2.length; i_0++) {
    rids2[i_0] = (checkElementIndex(i_0, rids.array.length) , dynamicCast(rids.array[i_0], 23)).value_0;
  }
  $removeItems(this$static.messages, rids2);
  $removeItems(this$static.media_0, rids2);
  topMessage = dynamicCast($getHeadValue(this$static.messages), 26);
  $send_1(this$static.dialogsActor, new DialogsActor$MessageDeleted(this$static.peer, topMessage));
}

function ConversationActor(peer, messenger){
  ModuleActor.call(this, messenger);
  this.peer = peer;
  this.pendingKeyValue = messenger.messages.conversationPending;
}

defineClass(548, 27, {}, ConversationActor);
_.onReceive = function onReceive_7(message){
  var messageError, sent;
  if (instanceOf(message, 26)) {
    $onInMessage_0(this, dynamicCast(message, 26));
  }
   else if (instanceOf(message, 241)) {
    sent = dynamicCast(message, 241);
    $onMessageSent(this, sent.rid, sent.date);
  }
   else if (instanceOf(message, 242)) {
    messageError = dynamicCast(message, 242);
    $onMessageError(this, messageError.rid);
  }
   else 
    instanceOf(message, 240)?$onMessagePlainRead(this, dynamicCast(message, 240).date):instanceOf(message, 239)?$onMessagePlainReceived(this, dynamicCast(message, 239).date):instanceOf(message, 238)?$onHistoryLoaded(this, dynamicCast(message, 238).messages):instanceOf(message, 351)?($clear_1(this.messages) , $clear_1(this.media_0) , $send_1(this.dialogsActor, new DialogsActor$ChatClear(this.peer)) , undefined):instanceOf(message, 352)?($clear_1(this.messages) , $clear_1(this.media_0) , $send_1(this.dialogsActor, new DialogsActor$ChatDelete(this.peer)) , undefined):instanceOf(message, 174)?$onMessagesDeleted(this, dynamicCast(message, 174).rids):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
_.preStart = function preStart_3(){
  var data_0, e;
  this.messages = $messages(this, this.peer);
  this.media_0 = $media(this, this.peer);
  this.messagesStorage = new OutUnreadMessagesStorage;
  data_0 = $get_0(this.pendingKeyValue, $getUnuqueId(this.peer));
  if (data_0 != null) {
    try {
      this.messagesStorage = dynamicCast(parse_159(new OutUnreadMessagesStorage, data_0), 280);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
  this.dialogsActor = this.modules.messages.dialogsActor;
}
;
var Lim_actor_model_modules_messages_ConversationActor_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor', 548, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function ConversationActor$ClearConversation(){
}

defineClass(351, 1, {351:1}, ConversationActor$ClearConversation);
var Lim_actor_model_modules_messages_ConversationActor$ClearConversation_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/ClearConversation', 351, Ljava_lang_Object_2_classLit);
function ConversationActor$DeleteConversation(){
}

defineClass(352, 1, {352:1}, ConversationActor$DeleteConversation);
var Lim_actor_model_modules_messages_ConversationActor$DeleteConversation_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/DeleteConversation', 352, Ljava_lang_Object_2_classLit);
function ConversationActor$HistoryLoaded(messages){
  this.messages = messages;
}

defineClass(238, 1, {238:1}, ConversationActor$HistoryLoaded);
var Lim_actor_model_modules_messages_ConversationActor$HistoryLoaded_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/HistoryLoaded', 238, Ljava_lang_Object_2_classLit);
function ConversationActor$MessageError(rid){
  this.rid = rid;
}

defineClass(242, 1, {242:1}, ConversationActor$MessageError);
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_ConversationActor$MessageError_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/MessageError', 242, Ljava_lang_Object_2_classLit);
function ConversationActor$MessageRead(date){
  this.date = date;
}

defineClass(240, 1, {240:1}, ConversationActor$MessageRead);
_.date = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_ConversationActor$MessageRead_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/MessageRead', 240, Ljava_lang_Object_2_classLit);
function ConversationActor$MessageReceived(date){
  this.date = date;
}

defineClass(239, 1, {239:1}, ConversationActor$MessageReceived);
_.date = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_ConversationActor$MessageReceived_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/MessageReceived', 239, Ljava_lang_Object_2_classLit);
function ConversationActor$MessageSent(rid, date){
  this.rid = rid;
  this.date = date;
}

defineClass(241, 1, {241:1}, ConversationActor$MessageSent);
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_ConversationActor$MessageSent_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/MessageSent', 241, Ljava_lang_Object_2_classLit);
function ConversationActor$MessagesDeleted(rids){
  this.rids = rids;
}

defineClass(174, 1, {174:1}, ConversationActor$MessagesDeleted);
var Lim_actor_model_modules_messages_ConversationActor$MessagesDeleted_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationActor/MessagesDeleted', 174, Ljava_lang_Object_2_classLit);
function $onLoadMore(this$static){
  if (this$static.historyLoaded) {
    return;
  }
  if (this$static.isLoading) {
    return;
  }
  this$static.isLoading = true;
  $request_0(this$static, new RequestLoadHistory_0($buidOutPeer(this$static, this$static.peer), this$static.historyMaxDate), new ConversationHistoryActor$1(this$static));
}

function $onLoadedMore(this$static, loaded, maxLoadedDate){
  this$static.isLoading = false;
  if (loaded < 20) {
    this$static.historyLoaded = true;
  }
   else {
    this$static.historyLoaded = false;
    this$static.historyMaxDate = maxLoadedDate;
  }
  $putLong(this$static.modules.preferences, this$static.KEY_LOADED_DATE, maxLoadedDate);
  $putBool(this$static.modules.preferences, this$static.KEY_LOADED, this$static.historyLoaded);
  $putBool(this$static.modules.preferences, this$static.KEY_LOADED_INIT, true);
}

function ConversationHistoryActor(peer, modules){
  ModuleActor.call(this, modules);
  this.peer = peer;
  this.KEY_LOADED_DATE = 'conv_' + peer + '_history_date';
  this.KEY_LOADED = 'conv_' + peer + '_history_loaded';
  this.KEY_LOADED_INIT = 'conv_' + peer + '_history_inited';
}

defineClass(549, 27, {}, ConversationHistoryActor);
_.onReceive = function onReceive_8(message){
  var loadedMore;
  if (instanceOf(message, 353)) {
    $onLoadMore(this);
  }
   else if (instanceOf(message, 243)) {
    loadedMore = dynamicCast(message, 243);
    $onLoadedMore(this, loadedMore.loaded, loadedMore.maxLoadedDate);
  }
   else {
    !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this, new DeadLetter(message));
  }
}
;
_.preStart = function preStart_4(){
  this.historyMaxDate = $getLong_1(this.modules.preferences, this.KEY_LOADED_DATE, {l:$intern_7, m:$intern_7, h:524287});
  this.historyLoaded = $getBool(this.modules.preferences, this.KEY_LOADED, false);
  $getBool(this.modules.preferences, this.KEY_LOADED_INIT, false) || $sendOnce(this.context.actorScope.actorRef, new ConversationHistoryActor$LoadMore);
}
;
_.historyLoaded = false;
_.historyMaxDate = {l:0, m:0, h:0};
_.isLoading = false;
var Lim_actor_model_modules_messages_ConversationHistoryActor_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationHistoryActor', 549, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_11(this$static, response){
  $onUpdateReceived_0(this$static.this$01.modules.updates, new MessagesHistoryLoaded(this$static.this$01.peer, response));
}

function ConversationHistoryActor$1(this$0){
  this.this$01 = this$0;
}

defineClass(631, 1, {}, ConversationHistoryActor$1);
_.onError_0 = function onError_7(e){
  $printStackTrace(e, ($clinit_System() , err));
}
;
_.onResult_0 = function onResult_7(response){
  $onResult_11(this, dynamicCast(response, 318));
}
;
var Lim_actor_model_modules_messages_ConversationHistoryActor$1_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationHistoryActor/1', 631, Ljava_lang_Object_2_classLit);
function ConversationHistoryActor$LoadMore(){
}

defineClass(353, 1, {353:1}, ConversationHistoryActor$LoadMore);
var Lim_actor_model_modules_messages_ConversationHistoryActor$LoadMore_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationHistoryActor/LoadMore', 353, Ljava_lang_Object_2_classLit);
function ConversationHistoryActor$LoadedMore(loaded, maxLoadedDate){
  this.loaded = loaded;
  this.maxLoadedDate = maxLoadedDate;
}

defineClass(243, 1, {243:1}, ConversationHistoryActor$LoadedMore);
_.loaded = 0;
_.maxLoadedDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_ConversationHistoryActor$LoadedMore_2_classLit = createForClass('im.actor.model.modules.messages', 'ConversationHistoryActor/LoadedMore', 243, Ljava_lang_Object_2_classLit);
function $moveCursor(this$static, peer, date){
  var cursor;
  cursor = $getCursor(this$static.plainCursorsStorage, peer);
  if (lte(date, cursor.sortDate)) {
    return;
  }
  if (lte(date, cursor.pendingSortDate)) {
    return;
  }
  date = max_1(cursor.pendingSortDate, date);
  $putCursor(this$static.plainCursorsStorage, new PlainCursor_0(cursor.peer, cursor.sortDate, date));
  $put_0(this$static.keyValue, this$static.cursorId, $toByteArray(this$static.plainCursorsStorage));
  if ($contains_0(this$static.inProgress, peer)) {
    return;
  }
  $add_1(this$static.inProgress, peer);
  this$static.perform(peer, date);
}

function $onCompleted(this$static, peer, date){
  $send_1(this$static.context.actorScope.actorRef, new CursorActor$OnCompleted(peer, date));
}

function $onMoved(this$static, peer, date){
  var cursor;
  $remove_5(this$static.inProgress, peer);
  cursor = $getCursor(this$static.plainCursorsStorage, peer);
  cursor = $changeSortDate(cursor, max_1(date, cursor.sortDate));
  $putCursor(this$static.plainCursorsStorage, cursor);
  $put_0(this$static.keyValue, this$static.cursorId, $toByteArray(this$static.plainCursorsStorage));
  if (lt(cursor.sortDate, cursor.pendingSortDate)) {
    $add_1(this$static.inProgress, peer);
    this$static.perform(peer, cursor.pendingSortDate);
  }
}

function $onReceive(this$static, message){
  var completed;
  if (instanceOf(message, 272)) {
    completed = dynamicCast(message, 272);
    $onMoved(this$static, completed.peer, completed.date);
  }
   else {
    !!this$static.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this$static, new DeadLetter(message));
  }
}

function CursorActor(cursorId, messenger){
  ModuleActor.call(this, messenger);
  this.inProgress = new HashSet;
  this.cursorId = cursorId;
  this.keyValue = messenger.messages.cursorStorage;
}

defineClass(445, 27, {});
_.onReceive = function onReceive_9(message){
  $onReceive(this, message);
}
;
_.preStart = function preStart_5(){
  var cursor, cursor$iterator, data_0, e;
  this.plainCursorsStorage = new PlainCursorsStorage;
  data_0 = $get_0(this.keyValue, this.cursorId);
  if (data_0 != null) {
    try {
      this.plainCursorsStorage = dynamicCast(parse_159(new PlainCursorsStorage, data_0), 286);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
  for (cursor$iterator = $iterator_0(new AbstractMap$2(this.plainCursorsStorage.cursors)); cursor$iterator.val$outerIter2.hasNext();) {
    cursor = dynamicCast($next_2(cursor$iterator), 77);
    if (lt(cursor.sortDate, cursor.pendingSortDate)) {
      $add_1(this.inProgress, cursor.peer);
      this.perform(cursor.peer, cursor.pendingSortDate);
    }
  }
}
;
_.cursorId = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_CursorActor_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorActor', 445, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function CursorActor$OnCompleted(peer, date){
  this.peer = peer;
  this.date = date;
}

defineClass(272, 1, {272:1}, CursorActor$OnCompleted);
_.date = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_CursorActor$OnCompleted_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorActor/OnCompleted', 272, Ljava_lang_Object_2_classLit);
function CursorReaderActor(messenger){
  CursorActor.call(this, {l:1, m:0, h:0}, messenger);
}

defineClass(544, 445, {}, CursorReaderActor);
_.onReceive = function onReceive_10(message){
  var markRead;
  if (instanceOf(message, 271)) {
    markRead = dynamicCast(message, 271);
    $moveCursor(this, markRead.peer, markRead.date);
  }
   else {
    $onReceive(this, message);
  }
}
;
_.perform = function perform(peer, date){
  var outPeer;
  outPeer = $buidOutPeer(this, peer);
  if (!outPeer) {
    return;
  }
  $request_0(this, new RequestMessageRead_0(outPeer, date), new CursorReaderActor$1(this, peer, date));
}
;
var Lim_actor_model_modules_messages_CursorReaderActor_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorReaderActor', 544, Lim_actor_model_modules_messages_CursorActor_2_classLit);
function $onResult_12(this$static){
  $onCompleted(this$static.this$01, this$static.val$peer2, this$static.val$date3);
}

function CursorReaderActor$1(this$0, val$peer, val$date){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
  this.val$date3 = val$date;
}

defineClass(699, 1, {}, CursorReaderActor$1);
_.onError_0 = function onError_8(e){
}
;
_.onResult_0 = function onResult_8(response){
  $onResult_12(this, dynamicCast(response, 88));
}
;
_.val$date3 = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_CursorReaderActor$1_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorReaderActor/1', 699, Ljava_lang_Object_2_classLit);
function CursorReaderActor$MarkRead(peer, date){
  this.peer = peer;
  this.date = date;
}

defineClass(271, 1, {271:1}, CursorReaderActor$MarkRead);
_.date = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_CursorReaderActor$MarkRead_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorReaderActor/MarkRead', 271, Ljava_lang_Object_2_classLit);
function CursorReceiverActor(messenger){
  CursorActor.call(this, {l:0, m:0, h:0}, messenger);
}

defineClass(545, 445, {}, CursorReceiverActor);
_.onReceive = function onReceive_11(message){
  var received;
  if (instanceOf(message, 273)) {
    received = dynamicCast(message, 273);
    $moveCursor(this, received.peer, received.date);
  }
   else {
    $onReceive(this, message);
  }
}
;
_.perform = function perform_0(peer, date){
  var outPeer;
  outPeer = $buidOutPeer(this, peer);
  if (!outPeer) {
    return;
  }
  $request_0(this, new RequestMessageReceived_0(outPeer, date), new CursorReceiverActor$1(this, peer, date));
}
;
var Lim_actor_model_modules_messages_CursorReceiverActor_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorReceiverActor', 545, Lim_actor_model_modules_messages_CursorActor_2_classLit);
function $onResult_13(this$static){
  $onCompleted(this$static.this$01, this$static.val$peer2, this$static.val$date3);
}

function CursorReceiverActor$1(this$0, val$peer, val$date){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
  this.val$date3 = val$date;
}

defineClass(700, 1, {}, CursorReceiverActor$1);
_.onError_0 = function onError_9(e){
}
;
_.onResult_0 = function onResult_9(response){
  $onResult_13(this, dynamicCast(response, 88));
}
;
_.val$date3 = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_CursorReceiverActor$1_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorReceiverActor/1', 700, Ljava_lang_Object_2_classLit);
function CursorReceiverActor$MarkReceived(peer, date){
  this.peer = peer;
  this.date = date;
}

defineClass(273, 1, {273:1}, CursorReceiverActor$MarkReceived);
_.date = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_CursorReceiverActor$MarkReceived_2_classLit = createForClass('im.actor.model.modules.messages', 'CursorReceiverActor/MarkReceived', 273, Ljava_lang_Object_2_classLit);
function $addOrUpdateItem_1(this$static, dialog){
  var d;
  $addOrUpdateItem_0(this$static.dialogs, dialog);
  d = new ArrayList;
  setCheck(d.array, d.array.length, dialog);
  $onDialogsChanged(this$static.modules.search, d);
}

function $buildPeerDesc(this$static, peer){
  var g, u;
  switch (peer.peerType.ordinal) {
    case 0:
      u = $getUser_1(this$static, peer.peerId);
      return new DialogsActor$PeerDesc(u.localName == null?u.name_0:u.localName, u.avatar);
    case 1:
      g = $getGroup_1(this$static, peer.peerId);
      return new DialogsActor$PeerDesc(g.title_0, g.avatar);
    default:return null;
  }
}

function $notifyState(this$static){
  var isEmpty;
  isEmpty = this$static.dialogs.storage.index_0.array.length == 0;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'NOTIFY_DIALOGS' + ':' + ('isEmpty: ' + isEmpty));
  $onDialogsUpdate(this$static.modules.appStateModule, isEmpty);
}

function $onChatClear(this$static, peer){
  var dialog;
  dialog = dynamicCast($getValue_1(this$static.dialogs, $getUnuqueId(peer)), 49);
  !!dialog && $addOrUpdateItem_1(this$static, $createDialog($setStatus($setSenderId($setRid($setUnreadCount($setTime($setText($setMessageType(new DialogBuilder_0(dialog), ($clinit_ContentType() , EMPTY)), ''), {l:0, m:0, h:0}), 0), {l:0, m:0, h:0}), 0), ($clinit_MessageState_0() , UNKNOWN_0))));
}

function $onChatDeleted(this$static, peer){
  $removeItem_1(this$static.dialogs, $getUnuqueId(peer));
  $notifyState(this$static);
}

function $onCounterChanged(this$static, peer, count){
  var dialog;
  dialog = dynamicCast($getValue_1(this$static.dialogs, $getUnuqueId(peer)), 49);
  !!dialog && $addOrUpdateItem_1(this$static, $createDialog($setUnreadCount(new DialogBuilder_0(dialog), count)));
}

function $onGroupChanged(this$static, group){
  var dialog;
  dialog = dynamicCast($getValue_1(this$static.dialogs, $getUnuqueId(new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId))), 49);
  if (dialog) {
    if ($equals_3(dialog.dialogTitle, group.title_0) && equalsE(dialog.dialogAvatar, group.avatar)) {
      return;
    }
    $addOrUpdateItem_1(this$static, $editPeerInfo(dialog, group.title_0, group.avatar));
  }
}

function $onHistoryLoaded_0(this$static, history_0){
  var description, dialogHistory, dialogHistory$iterator, peerDesc, updated;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AppStateVM' + ':' + 'onHistoryLoaded');
  updated = new ArrayList;
  for (dialogHistory$iterator = new AbstractList$IteratorImpl(history_0); dialogHistory$iterator.i < dialogHistory$iterator.this$01.size_1();) {
    dialogHistory = (checkCriticalElement(dialogHistory$iterator.i < dialogHistory$iterator.this$01.size_1()) , dynamicCast(dialogHistory$iterator.this$01.get_1(dialogHistory$iterator.last = dialogHistory$iterator.i++), 374));
    if ($getValue_1(this$static.dialogs, $getUnuqueId(dialogHistory.peer))) {
      continue;
    }
    peerDesc = $buildPeerDesc(this$static, dialogHistory.peer);
    if (!peerDesc) {
      continue;
    }
    description = fromContent(dialogHistory.content_0);
    $add_0(updated, new Dialog_1(dialogHistory.peer, dialogHistory.sortDate, peerDesc.title_0, peerDesc.avatar, dialogHistory.unreadCount, dialogHistory.rid, description.contentType, description.text_0, dialogHistory.status_0, dialogHistory.senderId, dialogHistory.date, description.relatedUser));
  }
  $addOrUpdateItems_0(this$static.dialogs, updated);
  $onDialogsChanged(this$static.modules.search, updated);
  $send_1(this$static.modules.appStateModule.listStatesActor, new ListsStatesActor$OnDialogsLoaded);
  $notifyState(this$static);
}

function $onMessage(this$static, peer, message, forceWrite){
  var builder, contentDescription, dialog, peerDesc;
  peerDesc = $buildPeerDesc(this$static, peer);
  if (!peerDesc) {
    return;
  }
  if (!message) {
    if (!forceWrite) {
      return;
    }
    $onChatClear(this$static, peer);
  }
   else {
    dialog = dynamicCast($getValue_1(this$static.dialogs, $getUnuqueId(peer)), 49);
    contentDescription = fromContent(message.content_0);
    builder = $setSenderId($setStatus($setRelatedUid($setText($setMessageType($setTime($setRid(new DialogBuilder, message.rid), message.date), contentDescription.contentType), contentDescription.text_0), contentDescription.relatedUser), message.messageState), message.senderId);
    if (dialog) {
      if (!forceWrite && gt(dialog.sortDate, message.sortDate)) {
        return;
      }
      $setSortKey($setUnreadCount($setDialogAvatar($setDialogTitle($setPeer(builder, dialog.peer), dialog.dialogTitle), dialog.dialogAvatar), dialog.unreadCount), dialog.sortDate);
      contentDescription.isSilent || $setSortKey(builder, message.sortDate);
    }
     else {
      if (contentDescription.isSilent) {
        return;
      }
      $setSortKey($setUnreadCount($setDialogAvatar($setDialogTitle((builder.peer = peer , builder), peerDesc.title_0), peerDesc.avatar), 0), message.sortDate);
    }
    $addOrUpdateItem_1(this$static, new Dialog_1(builder.peer, builder.sortKey, builder.dialogTitle, builder.dialogAvatar, builder.unreadCount, builder.rid, builder.messageType, builder.text_0, builder.status_0, builder.senderId, builder.time, builder.relatedUid));
    $notifyState(this$static);
  }
}

function $onMessageSent_0(this$static, peer, rid, date){
  var dialog;
  dialog = dynamicCast($getValue_1(this$static.dialogs, $getUnuqueId(peer)), 49);
  !!dialog && eq(dialog.rid, rid) && $addOrUpdateItem_1(this$static, $createDialog($setTime($setStatus(new DialogBuilder_0(dialog), ($clinit_MessageState_0() , SENT_0)), date)));
}

function $onMessageStatusChanged(this$static, peer, rid, state){
  var dialog;
  dialog = dynamicCast($getValue_1(this$static.dialogs, $getUnuqueId(peer)), 49);
  !!dialog && eq(dialog.rid, rid) && $addOrUpdateItem_1(this$static, $createDialog($setStatus(new DialogBuilder_0(dialog), state)));
}

function $onUserChanged_0(this$static, user){
  var dialog;
  dialog = dynamicCast($getValue_1(this$static.dialogs, $getUnuqueId(new Peer_2(($clinit_PeerType_0() , PRIVATE_0), user.uid))), 49);
  if (dialog) {
    if ($equals_3(dialog.dialogTitle, user.localName == null?user.name_0:user.localName) && equalsE(dialog.dialogAvatar, user.avatar)) {
      return;
    }
    $addOrUpdateItem_1(this$static, $editPeerInfo(dialog, user.localName == null?user.name_0:user.localName, user.avatar));
  }
}

function DialogsActor(messenger){
  ModuleActor.call(this, messenger);
}

defineClass(541, 27, {}, DialogsActor);
_.onReceive = function onReceive_12(message){
  var counterChanged, deleted, groupChanged, historyLoaded, inMessage, messageSent, messageStateChanged, userChanged;
  if (instanceOf(message, 262)) {
    inMessage = dynamicCast(message, 262);
    $onMessage(this, inMessage.peer, inMessage.message_0, false);
  }
   else if (instanceOf(message, 101)) {
    userChanged = dynamicCast(message, 101);
    $onUserChanged_0(this, userChanged.user);
  }
   else if (instanceOf(message, 263)) {
    $onChatClear(this, dynamicCast(message, 263).peer);
  }
   else if (instanceOf(message, 264)) {
    $onChatDeleted(this, dynamicCast(message, 264).peer);
  }
   else if (instanceOf(message, 129)) {
    messageStateChanged = dynamicCast(message, 129);
    $onMessageStatusChanged(this, messageStateChanged.peer, messageStateChanged.rid, messageStateChanged.state);
  }
   else if (instanceOf(message, 130)) {
    counterChanged = dynamicCast(message, 130);
    $onCounterChanged(this, counterChanged.peer, counterChanged.count);
  }
   else if (instanceOf(message, 266)) {
    deleted = dynamicCast(message, 266);
    $onMessage(this, deleted.peer, deleted.topMessage, true);
  }
   else if (instanceOf(message, 267)) {
    historyLoaded = dynamicCast(message, 267);
    $onHistoryLoaded_0(this, historyLoaded.history_0);
  }
   else if (instanceOf(message, 128)) {
    groupChanged = dynamicCast(message, 128);
    $onGroupChanged(this, groupChanged.group);
  }
   else if (instanceOf(message, 265)) {
    messageSent = dynamicCast(message, 265);
    $onMessageSent_0(this, messageSent.peer, messageSent.rid, messageSent.date);
  }
   else {
    !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this, new DeadLetter(message));
  }
}
;
_.preStart = function preStart_6(){
  this.dialogs = this.modules.messages.dialogs;
  $notifyState(this);
}
;
var Lim_actor_model_modules_messages_DialogsActor_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor', 541, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function DialogsActor$ChatClear(peer){
  this.peer = peer;
}

defineClass(263, 1, {263:1}, DialogsActor$ChatClear);
var Lim_actor_model_modules_messages_DialogsActor$ChatClear_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/ChatClear', 263, Ljava_lang_Object_2_classLit);
function DialogsActor$ChatDelete(peer){
  this.peer = peer;
}

defineClass(264, 1, {264:1}, DialogsActor$ChatDelete);
var Lim_actor_model_modules_messages_DialogsActor$ChatDelete_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/ChatDelete', 264, Ljava_lang_Object_2_classLit);
function DialogsActor$CounterChanged(peer, count){
  this.peer = peer;
  this.count = count;
}

defineClass(130, 1, {130:1}, DialogsActor$CounterChanged);
_.count = 0;
var Lim_actor_model_modules_messages_DialogsActor$CounterChanged_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/CounterChanged', 130, Ljava_lang_Object_2_classLit);
function DialogsActor$GroupChanged(group){
  this.group = group;
}

defineClass(128, 1, {128:1}, DialogsActor$GroupChanged);
var Lim_actor_model_modules_messages_DialogsActor$GroupChanged_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/GroupChanged', 128, Ljava_lang_Object_2_classLit);
function DialogsActor$HistoryLoaded(history_0){
  this.history_0 = history_0;
}

defineClass(267, 1, {267:1}, DialogsActor$HistoryLoaded);
var Lim_actor_model_modules_messages_DialogsActor$HistoryLoaded_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/HistoryLoaded', 267, Ljava_lang_Object_2_classLit);
function DialogsActor$InMessage(peer, message){
  this.peer = peer;
  this.message_0 = message;
}

defineClass(262, 1, {262:1}, DialogsActor$InMessage);
var Lim_actor_model_modules_messages_DialogsActor$InMessage_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/InMessage', 262, Ljava_lang_Object_2_classLit);
function DialogsActor$MessageDeleted(peer, topMessage){
  this.peer = peer;
  this.topMessage = topMessage;
}

defineClass(266, 1, {266:1}, DialogsActor$MessageDeleted);
var Lim_actor_model_modules_messages_DialogsActor$MessageDeleted_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/MessageDeleted', 266, Ljava_lang_Object_2_classLit);
function DialogsActor$MessageSent(peer, rid, date){
  this.peer = peer;
  this.rid = rid;
  this.date = date;
}

defineClass(265, 1, {265:1}, DialogsActor$MessageSent);
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_DialogsActor$MessageSent_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/MessageSent', 265, Ljava_lang_Object_2_classLit);
function DialogsActor$MessageStateChanged(peer, rid, state){
  this.peer = peer;
  this.rid = rid;
  this.state = state;
}

defineClass(129, 1, {129:1}, DialogsActor$MessageStateChanged);
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_DialogsActor$MessageStateChanged_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/MessageStateChanged', 129, Ljava_lang_Object_2_classLit);
function DialogsActor$PeerDesc(title_0, avatar){
  this.title_0 = title_0;
  this.avatar = avatar;
}

defineClass(459, 1, {}, DialogsActor$PeerDesc);
var Lim_actor_model_modules_messages_DialogsActor$PeerDesc_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/PeerDesc', 459, Ljava_lang_Object_2_classLit);
function DialogsActor$UserChanged(user){
  this.user = user;
}

defineClass(101, 1, {101:1}, DialogsActor$UserChanged);
var Lim_actor_model_modules_messages_DialogsActor$UserChanged_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsActor/UserChanged', 101, Ljava_lang_Object_2_classLit);
function $onLoadMore_0(this$static){
  if (this$static.historyLoaded) {
    return;
  }
  if (this$static.isLoading) {
    return;
  }
  this$static.isLoading = true;
  d_0('DialogsHistoryActor', 'Loading history... after ' + toString_2(this$static.historyMaxDate));
  $request_0(this$static, new RequestLoadDialogs_0(this$static.historyMaxDate), new DialogsHistoryActor$1(this$static));
}

function $onLoadedMore_0(this$static, loaded, maxLoadedDate){
  this$static.isLoading = false;
  if (loaded < 20) {
    this$static.historyLoaded = true;
  }
   else {
    this$static.historyLoaded = false;
    this$static.historyMaxDate = maxLoadedDate;
  }
  $putLong(this$static.modules.preferences, 'dialogs_history_date', maxLoadedDate);
  $putBool(this$static.modules.preferences, 'dialogs_history_loaded', this$static.historyLoaded);
  $putBool(this$static.modules.preferences, 'dialogs_history_inited', true);
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'DialogsHistoryActor' + ':' + ('History loaded, time = ' + toString_2(maxLoadedDate)));
}

function DialogsHistoryActor(messenger){
  ModuleActor.call(this, messenger);
}

defineClass(542, 27, {}, DialogsHistoryActor);
_.onReceive = function onReceive_13(message){
  var loaded;
  if (instanceOf(message, 362)) {
    $onLoadMore_0(this);
  }
   else if (instanceOf(message, 268)) {
    loaded = dynamicCast(message, 268);
    $onLoadedMore_0(this, loaded.loaded, loaded.maxLoadedDate);
  }
   else {
    !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this, new DeadLetter(message));
  }
}
;
_.preStart = function preStart_7(){
  this.historyMaxDate = $getLong_1(this.modules.preferences, 'dialogs_history_date', {l:$intern_7, m:$intern_7, h:524287});
  this.historyLoaded = $getBool(this.modules.preferences, 'dialogs_history_loaded', false);
  $getBool(this.modules.preferences, 'dialogs_history_inited', false) || $sendOnce(this.context.actorScope.actorRef, new DialogsHistoryActor$LoadMore);
}
;
_.historyLoaded = false;
_.historyMaxDate = {l:0, m:0, h:0};
_.isLoading = false;
var Lim_actor_model_modules_messages_DialogsHistoryActor_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsHistoryActor', 542, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_14(this$static, response){
  $onUpdateReceived_0(this$static.this$01.modules.updates, new DialogHistoryLoaded(response));
}

function DialogsHistoryActor$1(this$0){
  this.this$01 = this$0;
}

defineClass(698, 1, {}, DialogsHistoryActor$1);
_.onError_0 = function onError_10(e){
  $printStackTrace(e, ($clinit_System() , err));
}
;
_.onResult_0 = function onResult_10(response){
  $onResult_14(this, dynamicCast(response, 319));
}
;
var Lim_actor_model_modules_messages_DialogsHistoryActor$1_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsHistoryActor/1', 698, Ljava_lang_Object_2_classLit);
function DialogsHistoryActor$LoadMore(){
}

defineClass(362, 1, {362:1}, DialogsHistoryActor$LoadMore);
var Lim_actor_model_modules_messages_DialogsHistoryActor$LoadMore_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsHistoryActor/LoadMore', 362, Ljava_lang_Object_2_classLit);
function DialogsHistoryActor$LoadedMore(loaded, maxLoadedDate){
  this.loaded = loaded;
  this.maxLoadedDate = maxLoadedDate;
}

defineClass(268, 1, {268:1}, DialogsHistoryActor$LoadedMore);
_.loaded = 0;
_.maxLoadedDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_DialogsHistoryActor$LoadedMore_2_classLit = createForClass('im.actor.model.modules.messages', 'DialogsHistoryActor/LoadedMore', 268, Ljava_lang_Object_2_classLit);
function $performDelete(this$static, peer, rids){
  var apiPeer, outPeer;
  outPeer = $buidOutPeer(this$static, peer);
  apiPeer = peer.peerType == ($clinit_PeerType_0() , PRIVATE_0)?new Peer_0(($clinit_PeerType() , PRIVATE), peer.peerId):peer.peerType == GROUP_0?new Peer_0(($clinit_PeerType() , GROUP), peer.peerId):null;
  $request_0(this$static, new RequestDeleteMessage_0(outPeer, rids), new MessageDeleteActor$1(this$static, peer, rids, apiPeer));
}

function $saveStorage(this$static){
  $put_0(this$static.syncKeyValue, {l:3, m:0, h:0}, $toByteArray(this$static.deleteStorage));
}

function MessageDeleteActor(modules){
  ModuleActor.call(this, modules);
  this.syncKeyValue = modules.messages.cursorStorage;
}

defineClass(547, 27, {}, MessageDeleteActor);
_.onReceive = function onReceive_14(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
_.preStart = function preStart_8(){
  var data_0, delete_$, e, peer, peer$iterator;
  data_0 = $get_0(this.syncKeyValue, {l:3, m:0, h:0});
  if (data_0 != null) {
    try {
      this.deleteStorage = dynamicCast(parse_159(new DeleteStorage, data_0), 193);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
        this.deleteStorage = new DeleteStorage;
      }
       else 
        throw unwrap($e0);
    }
  }
   else {
    this.deleteStorage = new DeleteStorage;
  }
  for (peer$iterator = $iterator(new AbstractMap$1(this.deleteStorage.pendingDeletions)); $hasNext(peer$iterator.val$outerIter2);) {
    peer = dynamicCast($next_1(peer$iterator), 19);
    delete_$ = dynamicCast($get_2(this.deleteStorage.pendingDeletions, peer), 192);
    delete_$.rids.array.length > 0 && $performDelete(this, peer, delete_$.rids);
  }
}
;
var Lim_actor_model_modules_messages_MessageDeleteActor_2_classLit = createForClass('im.actor.model.modules.messages', 'MessageDeleteActor', 547, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_15(this$static, response){
  if ($containsKey(this$static.this$01.deleteStorage.pendingDeletions, this$static.val$peer2)) {
    dynamicCast($get_2(this$static.this$01.deleteStorage.pendingDeletions, this$static.val$peer2), 192).rids.removeAll(this$static.val$rids3);
    $saveStorage(this$static.this$01);
  }
  $onUpdateReceived_0(this$static.this$01.modules.updates, new SeqUpdate_0(response.seq, response.state, 46, $toByteArray(new UpdateMessageDelete_0(this$static.val$apiPeer4, this$static.val$rids3))));
}

function MessageDeleteActor$1(this$0, val$peer, val$rids, val$apiPeer){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
  this.val$rids3 = val$rids;
  this.val$apiPeer4 = val$apiPeer;
}

defineClass(701, 1, {}, MessageDeleteActor$1);
_.onError_0 = function onError_11(e){
}
;
_.onResult_0 = function onResult_11(response){
  $onResult_15(this, dynamicCast(response, 89));
}
;
var Lim_actor_model_modules_messages_MessageDeleteActor$1_2_classLit = createForClass('im.actor.model.modules.messages', 'MessageDeleteActor/1', 701, Ljava_lang_Object_2_classLit);
function $onMessageDelete(this$static, peer, rids){
  var isRemoved, u, u$array, u$index, u$max, unread;
  unread = $getUnread(this$static.messagesStorage, peer);
  isRemoved = false;
  for (u$array = dynamicCast(unread.toArray_0(initDim(Lim_actor_model_modules_messages_entity_UnreadMessage_2_classLit, $intern_23, 71, 0, 0, 1)), 336) , u$index = 0 , u$max = u$array.length; u$index < u$max; ++u$index) {
    u = u$array[u$index];
    if ($indexOf_0(rids, valueOf_0(u.rid), 0) != -1) {
      $remove_0(unread.map_0, u) != null;
      isRemoved = true;
    }
  }
  if (!isRemoved) {
    return;
  }
  $put_0(this$static.syncKeyValue, {l:2, m:0, h:0}, $toByteArray(this$static.messagesStorage));
  $send_1(this$static.modules.messages.dialogsActor, new DialogsActor$CounterChanged(peer, unread.map_0.size_0));
}

function $onMessageRead(this$static, peer, sortingDate){
  var maxPlainReadDate, readState, removed, u, u$array, u$index, u$max, unread;
  readState = $loadReadState(this$static.modules.messages, peer);
  if (lte(sortingDate, readState)) {
    return;
  }
  unread = $getUnread(this$static.messagesStorage, peer);
  maxPlainReadDate = sortingDate;
  removed = false;
  for (u$array = dynamicCast($toArray(unread, initDim(Lim_actor_model_modules_messages_entity_UnreadMessage_2_classLit, $intern_23, 71, 0, 0, 1)), 336) , u$index = 0 , u$max = u$array.length; u$index < u$max; ++u$index) {
    u = u$array[u$index];
    if (lte(u.sortDate, sortingDate)) {
      maxPlainReadDate = max_1(u.sortDate, maxPlainReadDate);
      removed = true;
      $remove_0(unread.map_0, u) != null;
    }
  }
  removed && $put_0(this$static.syncKeyValue, {l:2, m:0, h:0}, $toByteArray(this$static.messagesStorage));
  gt(maxPlainReadDate, {l:0, m:0, h:0}) && $send_1(this$static.modules.messages.plainReadActor, new CursorReaderActor$MarkRead(peer, maxPlainReadDate));
  $saveReadState(this$static.modules.messages, peer, sortingDate);
  $send_1(this$static.modules.messages.dialogsActor, new DialogsActor$CounterChanged(peer, unread.map_0.size_0));
  $onOwnRead(this$static.modules.notifications, peer, sortingDate);
}

function $onMessageReadByMe(this$static, peer, sortingDate){
  var msgSortingDate, u, u$array, u$index, u$max, unread;
  msgSortingDate = {l:0, m:0, h:0};
  unread = $getUnread(this$static.messagesStorage, peer);
  for (u$array = dynamicCast(unread.toArray_0(initDim(Lim_actor_model_modules_messages_entity_UnreadMessage_2_classLit, $intern_23, 71, 0, 0, 1)), 336) , u$index = 0 , u$max = u$array.length; u$index < u$max; ++u$index) {
    u = u$array[u$index];
    lte(u.sortDate, sortingDate) && gt(u.sortDate, msgSortingDate) && (msgSortingDate = u.sortDate);
  }
  gt(msgSortingDate, {l:0, m:0, h:0}) && $onMessageRead(this$static, peer, msgSortingDate);
}

function $onNewInMessage(this$static, peer, rid, sortingDate){
  var readState, unread;
  readState = $loadReadState(this$static.modules.messages, peer);
  if (lte(sortingDate, readState)) {
    return;
  }
  unread = $getUnread(this$static.messagesStorage, peer);
  $add_1(unread, new UnreadMessage_0(peer, rid, sortingDate));
  $put_0(this$static.syncKeyValue, {l:2, m:0, h:0}, $toByteArray(this$static.messagesStorage));
  $send_1(this$static.modules.messages.dialogsActor, new DialogsActor$CounterChanged(peer, unread.map_0.size_0));
}

function OwnReadActor(messenger){
  ModuleActor.call(this, messenger);
  this.syncKeyValue = messenger.messages.cursorStorage;
}

defineClass(543, 27, {}, OwnReadActor);
_.onReceive = function onReceive_15(message){
  var deleted, messageRead, newMessage, readByMe;
  if (instanceOf(message, 186)) {
    newMessage = dynamicCast(message, 186);
    $onNewInMessage(this, newMessage.peer, newMessage.rid, newMessage.sortingDate);
  }
   else if (instanceOf(message, 185)) {
    messageRead = dynamicCast(message, 185);
    $onMessageRead(this, messageRead.peer, messageRead.sortingDate);
  }
   else if (instanceOf(message, 269)) {
    readByMe = dynamicCast(message, 269);
    $onMessageReadByMe(this, readByMe.peer, readByMe.sortDate);
  }
   else if (instanceOf(message, 270)) {
    deleted = dynamicCast(message, 270);
    $onMessageDelete(this, deleted.peer, deleted.rids);
  }
   else {
    !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this, new DeadLetter(message));
  }
}
;
_.preStart = function preStart_9(){
  var e, st;
  this.messagesStorage = new UnreadMessagesStorage;
  st = $get_0(this.syncKeyValue, {l:2, m:0, h:0});
  if (st != null) {
    try {
      this.messagesStorage = dynamicCast(parse_159(new UnreadMessagesStorage, st), 285);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
}
;
var Lim_actor_model_modules_messages_OwnReadActor_2_classLit = createForClass('im.actor.model.modules.messages', 'OwnReadActor', 543, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function OwnReadActor$MessageDeleted(peer, rids){
  this.peer = peer;
  this.rids = rids;
}

defineClass(270, 1, {270:1}, OwnReadActor$MessageDeleted);
var Lim_actor_model_modules_messages_OwnReadActor$MessageDeleted_2_classLit = createForClass('im.actor.model.modules.messages', 'OwnReadActor/MessageDeleted', 270, Ljava_lang_Object_2_classLit);
function OwnReadActor$MessageRead(peer, sortingDate){
  this.peer = peer;
  this.sortingDate = sortingDate;
}

defineClass(185, 1, {185:1}, OwnReadActor$MessageRead);
_.sortingDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_OwnReadActor$MessageRead_2_classLit = createForClass('im.actor.model.modules.messages', 'OwnReadActor/MessageRead', 185, Ljava_lang_Object_2_classLit);
function OwnReadActor$MessageReadByMe(peer, sortDate){
  this.peer = peer;
  this.sortDate = sortDate;
}

defineClass(269, 1, {269:1}, OwnReadActor$MessageReadByMe);
_.sortDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_OwnReadActor$MessageReadByMe_2_classLit = createForClass('im.actor.model.modules.messages', 'OwnReadActor/MessageReadByMe', 269, Ljava_lang_Object_2_classLit);
function OwnReadActor$NewMessage(peer, rid, sortingDate){
  this.peer = peer;
  this.rid = rid;
  this.sortingDate = sortingDate;
}

defineClass(186, 1, {186:1}, OwnReadActor$NewMessage);
_.rid = {l:0, m:0, h:0};
_.sortingDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_OwnReadActor$NewMessage_2_classLit = createForClass('im.actor.model.modules.messages', 'OwnReadActor/NewMessage', 186, Ljava_lang_Object_2_classLit);
function $doSendText(this$static, peer, text_0){
  var date, message, rid;
  rid = ($clinit_RandomUtils() , $nextLong(RANDOM));
  date = getCurrentTime();
  message = new Message_0(rid, date, date, this$static.modules.auth.myUid, ($clinit_MessageState_0() , PENDING), new TextContent_0(text_0));
  $send_1($getConversationActor(this$static.modules.messages, peer), message);
  $add_0(this$static.pendingMessages.pendingMessages, new PendingMessage_0(peer, rid, new TextContent_0(text_0)));
  $performSendContent(this$static, peer, rid, new TextContent_0(text_0));
}

function $onError_4(this$static, peer, rid){
  var pending, pending$iterator;
  for (pending$iterator = new AbstractList$IteratorImpl(this$static.pendingMessages.pendingMessages); pending$iterator.i < pending$iterator.this$01.size_1();) {
    pending = (checkCriticalElement(pending$iterator.i < pending$iterator.this$01.size_1()) , dynamicCast(pending$iterator.this$01.get_1(pending$iterator.last = pending$iterator.i++), 96));
    if (eq(pending.rid, rid) && $equals_2(pending.peer, peer)) {
      $remove_4(this$static.pendingMessages.pendingMessages, pending);
      break;
    }
  }
  $putBytes(this$static.modules.preferences, 'sender_pending', $toByteArray(this$static.pendingMessages));
  $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$MessageError(rid));
}

function $onSent(this$static, peer, rid){
  var pending, pending$iterator;
  for (pending$iterator = new AbstractList$IteratorImpl(this$static.pendingMessages.pendingMessages); pending$iterator.i < pending$iterator.this$01.size_1();) {
    pending = (checkCriticalElement(pending$iterator.i < pending$iterator.this$01.size_1()) , dynamicCast(pending$iterator.this$01.get_1(pending$iterator.last = pending$iterator.i++), 96));
    if (eq(pending.rid, rid) && $equals_2(pending.peer, peer)) {
      $remove_4(this$static.pendingMessages.pendingMessages, pending);
      break;
    }
  }
  $putBytes(this$static.modules.preferences, 'sender_pending', $toByteArray(this$static.pendingMessages));
}

function $performSendContent(this$static, peer, rid, content_0){
  var apiPeer, documentContent, documentEx, fastThumb, message, outPeer, photoContent, source, videoContent;
  outPeer = $buidOutPeer(this$static, peer);
  apiPeer = peer.peerType == ($clinit_PeerType_0() , PRIVATE_0)?new Peer_0(($clinit_PeerType() , PRIVATE), peer.peerId):peer.peerType == GROUP_0?new Peer_0(($clinit_PeerType() , GROUP), peer.peerId):null;
  if (!outPeer || !apiPeer) {
    return;
  }
  if (instanceOf(content_0, 55)) {
    message = new TextMessage_0(dynamicCast(content_0, 55).text_0);
  }
   else if (instanceOf(content_0, 61)) {
    documentContent = dynamicCast(content_0, 61);
    source = dynamicCast(documentContent.source, 190);
    documentEx = null;
    if (instanceOf(content_0, 62)) {
      photoContent = dynamicCast(content_0, 62);
      documentEx = new DocumentExPhoto_0(photoContent.w, photoContent.h_0);
    }
     else if (instanceOf(content_0, 63)) {
      videoContent = dynamicCast(content_0, 63);
      documentEx = new DocumentExVideo_0(videoContent.w, videoContent.h_0, videoContent.duration);
    }
    fastThumb = null;
    !!documentContent.fastThumb && (fastThumb = new FastThumb_0(documentContent.fastThumb.w, documentContent.fastThumb.h_0, documentContent.fastThumb.image));
    message = new DocumentMessage_0(source.fileReference.fileId, source.fileReference.accessHash, source.fileReference.fileSize, source.fileReference.fileName, documentContent.mimetype, fastThumb, documentEx);
  }
   else {
    return;
  }
  $request_0(this$static, new RequestSendMessage_0(outPeer, rid, message), new SenderActor$1(this$static, peer, rid, apiPeer));
}

function SenderActor(messenger){
  ModuleActor.call(this, messenger);
}

defineClass(546, 27, {}, SenderActor);
_.onReceive = function onReceive_16(message){
  var messageError, messageSent, sendText;
  if (instanceOf(message, 235)) {
    sendText = dynamicCast(message, 235);
    $doSendText(this, sendText.peer, sendText.text_0);
  }
   else if (instanceOf(message, 171)) {
    messageSent = dynamicCast(message, 171);
    $onSent(this, messageSent.peer, messageSent.rid);
  }
   else if (instanceOf(message, 236)) {
    messageError = dynamicCast(message, 236);
    $onError_4(this, messageError.peer, messageError.rid);
  }
   else {
    !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this, new DeadLetter(message));
  }
}
;
_.preStart = function preStart_10(){
  var documentContent, e, isChanged, p_0, pending, pending$array, pending$index, pending$max, rids;
  this.pendingMessages = new PendingMessagesStorage;
  p_0 = $getBytes_1(this.modules.preferences, 'sender_pending');
  if (p_0 != null) {
    try {
      this.pendingMessages = dynamicCast(parse_159(new PendingMessagesStorage, p_0), 287);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
  isChanged = false;
  for (pending$array = dynamicCast($toArray_0(this.pendingMessages.pendingMessages, initDim(Lim_actor_model_modules_messages_entity_PendingMessage_2_classLit, {807:1, 3:1, 6:1}, 96, 0, 0, 1)), 807) , pending$index = 0 , pending$max = pending$array.length; pending$index < pending$max; ++pending$index) {
    pending = pending$array[pending$index];
    if (instanceOf(pending.content_0, 55)) {
      $performSendContent(this, pending.peer, pending.rid, pending.content_0);
    }
     else if (instanceOf(pending.content_0, 61)) {
      documentContent = dynamicCast(pending.content_0, 61);
      if (instanceOf(documentContent.source, 279)) {
        rids = new ArrayList;
        $add_0(rids, valueOf_0(pending.rid));
        $send_1($getConversationActor_0(this, pending.peer), new ConversationActor$MessagesDeleted(rids));
        $remove_4(this.pendingMessages.pendingMessages, pending);
        isChanged = true;
      }
       else {
        $performSendContent(this, pending.peer, pending.rid, pending.content_0);
      }
    }
  }
  isChanged && $putBytes(this.modules.preferences, 'sender_pending', $toByteArray(this.pendingMessages));
}
;
var Lim_actor_model_modules_messages_SenderActor_2_classLit = createForClass('im.actor.model.modules.messages', 'SenderActor', 546, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_16(this$static, response){
  $send_1(this$static.this$01.context.actorScope.actorRef, new SenderActor$MessageSent(this$static.val$peer2, this$static.val$rid3));
  $onUpdateReceived_0(this$static.this$01.modules.updates, new SeqUpdate_0(response.seq, response.state, 4, $toByteArray(new UpdateMessageSent_0(this$static.val$apiPeer5, this$static.val$rid3, response.date))));
}

function SenderActor$1(this$0, val$peer, val$rid, val$apiPeer){
  this.this$01 = this$0;
  this.val$peer2 = val$peer;
  this.val$rid3 = val$rid;
  this.val$apiPeer5 = val$apiPeer;
}

defineClass(626, 1, {}, SenderActor$1);
_.onError_0 = function onError_12(e){
  $send_1(this.this$01.context.actorScope.actorRef, new SenderActor$MessageError(this.val$peer2, this.val$rid3));
}
;
_.onResult_0 = function onResult_12(response){
  $onResult_16(this, dynamicCast(response, 323));
}
;
_.val$rid3 = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_SenderActor$1_2_classLit = createForClass('im.actor.model.modules.messages', 'SenderActor/1', 626, Ljava_lang_Object_2_classLit);
function SenderActor$MessageError(peer, rid){
  this.peer = peer;
  this.rid = rid;
}

defineClass(236, 1, {236:1}, SenderActor$MessageError);
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_SenderActor$MessageError_2_classLit = createForClass('im.actor.model.modules.messages', 'SenderActor/MessageError', 236, Ljava_lang_Object_2_classLit);
function SenderActor$MessageSent(peer, rid){
  this.peer = peer;
  this.rid = rid;
}

defineClass(171, 1, {171:1}, SenderActor$MessageSent);
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_SenderActor$MessageSent_2_classLit = createForClass('im.actor.model.modules.messages', 'SenderActor/MessageSent', 171, Ljava_lang_Object_2_classLit);
function SenderActor$SendText(peer, text_0){
  this.peer = peer;
  this.text_0 = text_0;
}

defineClass(235, 1, {235:1}, SenderActor$SendText);
var Lim_actor_model_modules_messages_SenderActor$SendText_2_classLit = createForClass('im.actor.model.modules.messages', 'SenderActor/SendText', 235, Ljava_lang_Object_2_classLit);
function Delete(){
}

defineClass(192, 4, {4:1, 192:1}, Delete);
_.parse_0 = function parse_189(values){
  this.peer = fromBytes_6($getBytes(values, 1));
  this.rids = $getRepeatedLong(values, 2);
}
;
_.serialize = function serialize_182(writer){
  $writeObject(writer, 1, this.peer);
  $writeRepeatedLong(writer, 2, this.rids);
}
;
var Lim_actor_model_modules_messages_entity_Delete_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'Delete', 192, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function DeleteStorage(){
  this.pendingDeletions = new HashMap;
}

defineClass(193, 4, {4:1, 193:1}, DeleteStorage);
_.parse_0 = function parse_190(values){
  var count, d, d$iterator, i_0, tmp;
  $reset(this.pendingDeletions);
  count = $getRepeatedCount(values, 1);
  tmp = new ArrayList;
  for (i_0 = 0; i_0 < count; i_0++) {
    $add_0(tmp, new Delete);
  }
  for (d$iterator = new AbstractList$IteratorImpl($getRepeatedObj(values, 1, tmp)); d$iterator.i < d$iterator.this$01.size_1();) {
    d = (checkCriticalElement(d$iterator.i < d$iterator.this$01.size_1()) , dynamicCast(d$iterator.this$01.get_1(d$iterator.last = d$iterator.i++), 192));
    $put_1(this.pendingDeletions, d.peer, d);
  }
}
;
_.serialize = function serialize_183(writer){
  $writeRepeatedObj(writer, 1, new ArrayList_0(new AbstractMap$2(this.pendingDeletions)));
}
;
var Lim_actor_model_modules_messages_entity_DeleteStorage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'DeleteStorage', 193, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function DialogHistory(peer, unreadCount, sortDate, rid, date, senderId, content_0, status_0){
  this.peer = peer;
  this.unreadCount = unreadCount;
  this.sortDate = sortDate;
  this.rid = rid;
  this.date = date;
  this.senderId = senderId;
  this.content_0 = content_0;
  this.status_0 = status_0;
}

defineClass(374, 1, {374:1}, DialogHistory);
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
_.senderId = 0;
_.sortDate = {l:0, m:0, h:0};
_.unreadCount = 0;
var Lim_actor_model_modules_messages_entity_DialogHistory_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'DialogHistory', 374, Ljava_lang_Object_2_classLit);
function convert_3(avatar){
  if (!avatar) {
    return null;
  }
  return new Avatar_1(convert_4(avatar.smallImage), convert_4(avatar.largeImage), convert_4(avatar.fullImage));
}

function convert_4(avatarImage){
  if (!avatarImage) {
    return null;
  }
  return new AvatarImage_1(avatarImage.width_0, avatarImage.height, convert_6(avatarImage.fileLocation, avatarImage.fileSize));
}

function convert_5(fastThumb){
  if (!fastThumb) {
    return null;
  }
  return new FastThumb_2(fastThumb.w, fastThumb.h_0, fastThumb.thumb);
}

function convert_6(location_0, size_0){
  return new FileReference_0(location_0.fileId, location_0.accessHash, size_0, 'avatar.jpg');
}

function convert_7(content_0){
  var avatar, documentMessage, ex, exUserKicked, fastThumb, fileReference, message, mimeType, name_0, photo, source, title_0, userAdded, video;
  if (instanceOf(content_0, 195)) {
    message = dynamicCast(content_0, 195);
    return new TextContent_0(message.text_0);
  }
   else if (instanceOf(content_0, 328)) {
    message = dynamicCast(content_0, 328);
    ex = message.ext;
    if (instanceOf(ex, 329)) {
      avatar = dynamicCast(ex, 329);
      return new ServiceGroupAvatarChanged_0(convert_3(avatar.avatar));
    }
     else if (instanceOf(ex, 330)) {
      title_0 = dynamicCast(ex, 330);
      return new ServiceGroupTitleChanged_0(title_0.title_0);
    }
     else if (instanceOf(ex, 331)) {
      userAdded = dynamicCast(ex, 331);
      return new ServiceGroupUserAdded_0(userAdded.addedUid);
    }
     else if (instanceOf(ex, 332)) {
      exUserKicked = dynamicCast(ex, 332);
      return new ServiceGroupUserKicked_0(exUserKicked.kickedUid);
    }
     else 
      return instanceOf(ex, 431)?new ServiceGroupUserLeave:instanceOf(ex, 432)?new ServiceGroupCreated_0(''):new ServiceContent_0(message.text_0);
  }
   else if (instanceOf(content_0, 198)) {
    documentMessage = dynamicCast(content_0, 198);
    mimeType = documentMessage.mimeType;
    name_0 = documentMessage.name_0;
    fastThumb = convert_5(documentMessage.thumb);
    fileReference = new FileReference_0(documentMessage.fileId, documentMessage.accessHash, documentMessage.fileSize, documentMessage.name_0);
    source = new FileRemoteSource_0(fileReference);
    if (instanceOf(documentMessage.ext, 196)) {
      photo = dynamicCast(documentMessage.ext, 196);
      return new PhotoContent_0(source, mimeType, name_0, fastThumb, photo.w, photo.h_0);
    }
     else if (instanceOf(documentMessage.ext, 197)) {
      video = dynamicCast(documentMessage.ext, 197);
      return new VideoContent_0(source, mimeType, name_0, fastThumb, video.duration, video.w, video.h_0);
    }
     else {
      return new DocumentContent_0(source, mimeType, name_0, fastThumb);
    }
  }
  return null;
}

function convert_8(state){
  if (!state) {
    return $clinit_MessageState_0() , UNKNOWN_0;
  }
  switch (state.ordinal) {
    case 2:
      return $clinit_MessageState_0() , READ_0;
    case 1:
      return $clinit_MessageState_0() , RECEIVED_0;
    case 0:
      return $clinit_MessageState_0() , SENT_0;
    default:return $clinit_MessageState_0() , UNKNOWN_0;
  }
}

function convert_9(peer){
  return new Peer_2(convert_10(peer.type_0), peer.id_0);
}

function convert_10(peerType){
  switch (peerType.ordinal) {
    case 2:
      return $clinit_PeerType_0() , EMAIL_0;
    case 1:
      return $clinit_PeerType_0() , GROUP_0;
    default:case 0:
      return $clinit_PeerType_0() , PRIVATE_0;
  }
}

function convert_11(sex){
  if (!sex) {
    return $clinit_Sex_0() , UNKNOWN_1;
  }
  switch (sex.ordinal) {
    case 2:
      return $clinit_Sex_0() , FEMALE_0;
    case 1:
      return $clinit_Sex_0() , MALE_0;
    default:case 0:
      return $clinit_Sex_0() , UNKNOWN_1;
  }
}

function convert_12(user){
  var res;
  res = new ArrayList;
  $add_0(res, new ContactRecord_0('' + toString_2(user.phone)));
  return new User_1(user.id_0, user.accessHash, user.name_0, user.localName, convert_3(user.avatar), convert_11(user.sex), res);
}

function convert_13(members, admin){
  var m, m$iterator, res;
  res = new ArrayList;
  for (m$iterator = new AbstractList$IteratorImpl(members); m$iterator.i < m$iterator.this$01.size_1();) {
    m = (checkCriticalElement(m$iterator.i < m$iterator.this$01.size_1()) , dynamicCast(m$iterator.this$01.get_1(m$iterator.last = m$iterator.i++), 216));
    $add_0(res, new GroupMember_0(m.uid, m.inviterUid, m.date, m.uid == admin));
  }
  return res;
}

function OutUnreadMessage(){
}

function OutUnreadMessage_0(rid, date){
  this.rid = rid;
  this.date = date;
}

defineClass(82, 4, {4:1, 82:1}, OutUnreadMessage, OutUnreadMessage_0);
_.parse_0 = function parse_191(values){
  this.rid = $getLong(values, 1);
  this.date = $getLong(values, 2);
}
;
_.serialize = function serialize_184(writer){
  $writeLong(writer, 1, this.rid);
  $writeLong(writer, 2, this.date);
}
;
_.date = {l:0, m:0, h:0};
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_entity_OutUnreadMessage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'OutUnreadMessage', 82, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function OutUnreadMessagesStorage(){
  this.messages = new ArrayList;
}

defineClass(280, 4, {4:1, 280:1}, OutUnreadMessagesStorage);
_.parse_0 = function parse_192(values){
  var count, i_0, tmp;
  this.messages.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
  count = $getRepeatedCount(values, 1);
  tmp = new ArrayList;
  for (i_0 = 0; i_0 < count; i_0++) {
    $add_0(tmp, new OutUnreadMessage);
  }
  $addAll_0(this.messages, $getRepeatedObj(values, 1, tmp));
}
;
_.serialize = function serialize_185(writer){
  $writeRepeatedObj(writer, 1, this.messages);
}
;
var Lim_actor_model_modules_messages_entity_OutUnreadMessagesStorage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'OutUnreadMessagesStorage', 280, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function PendingMessage(){
}

function PendingMessage_0(peer, rid, content_0){
  this.peer = peer;
  this.rid = rid;
  this.content_0 = content_0;
}

defineClass(96, 4, {4:1, 96:1}, PendingMessage, PendingMessage_0);
_.parse_0 = function parse_193(values){
  this.peer = fromUniqueId($getLong(values, 1));
  this.rid = $getLong(values, 2);
  this.content_0 = contentFromBytes($getBytes(values, 3));
  this.isError = neq($getLong_0(values, 4), {l:0, m:0, h:0});
}
;
_.serialize = function serialize_186(writer){
  $writeLong(writer, 1, $getUnuqueId(this.peer));
  $writeLong(writer, 2, this.rid);
  $writeBytes(writer, 3, $toByteArray(this.content_0));
  $writeBool(writer, 4, this.isError);
}
;
_.isError = false;
_.rid = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_entity_PendingMessage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'PendingMessage', 96, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function PendingMessagesStorage(){
  this.pendingMessages = new ArrayList;
}

defineClass(287, 4, {4:1, 287:1}, PendingMessagesStorage);
_.parse_0 = function parse_194(values){
  var data_0, data$iterator, e;
  for (data$iterator = new AbstractList$IteratorImpl($getRepeatedBytes(values, 1)); data$iterator.i < data$iterator.this$01.size_1();) {
    data_0 = (checkCriticalElement(data$iterator.i < data$iterator.this$01.size_1()) , dynamicCast(data$iterator.this$01.get_1(data$iterator.last = data$iterator.i++), 36));
    try {
      $add_0(this.pendingMessages, dynamicCast(parse_159(new PendingMessage, data_0), 96));
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
}
;
_.serialize = function serialize_187(writer){
  $writeRepeatedObj(writer, 1, this.pendingMessages);
}
;
var Lim_actor_model_modules_messages_entity_PendingMessagesStorage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'PendingMessagesStorage', 287, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $changeSortDate(this$static, date){
  return new PlainCursor_0(this$static.peer, date, this$static.pendingSortDate);
}

function PlainCursor(){
}

function PlainCursor_0(peer, sortDate, pendingSortDate){
  this.peer = peer;
  this.sortDate = sortDate;
  this.pendingSortDate = pendingSortDate;
}

defineClass(77, 4, {4:1, 77:1}, PlainCursor, PlainCursor_0);
_.parse_0 = function parse_195(values){
  this.peer = fromUniqueId($getLong(values, 1));
  this.sortDate = $getLong(values, 2);
  this.pendingSortDate = $getLong(values, 3);
}
;
_.serialize = function serialize_188(writer){
  $writeLong(writer, 1, $getUnuqueId(this.peer));
  $writeLong(writer, 2, this.sortDate);
  $writeLong(writer, 3, this.pendingSortDate);
}
;
_.pendingSortDate = {l:0, m:0, h:0};
_.sortDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_entity_PlainCursor_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'PlainCursor', 77, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $getCursor(this$static, peer){
  $containsKey(this$static.cursors, peer) || $put_1(this$static.cursors, peer, new PlainCursor_0(peer, {l:0, m:0, h:0}, {l:0, m:0, h:0}));
  return dynamicCast($get_2(this$static.cursors, peer), 77);
}

function $putCursor(this$static, cursor){
  $put_1(this$static.cursors, cursor.peer, cursor);
}

function PlainCursorsStorage(){
  this.cursors = new HashMap;
}

defineClass(286, 4, {4:1, 286:1}, PlainCursorsStorage);
_.parse_0 = function parse_196(values){
  var data_0, data$iterator, e, plainCursor;
  for (data$iterator = new AbstractList$IteratorImpl($getRepeatedBytes(values, 1)); data$iterator.i < data$iterator.this$01.size_1();) {
    data_0 = (checkCriticalElement(data$iterator.i < data$iterator.this$01.size_1()) , dynamicCast(data$iterator.this$01.get_1(data$iterator.last = data$iterator.i++), 36));
    try {
      plainCursor = dynamicCast(parse_159(new PlainCursor, data_0), 77);
      $put_1(this.cursors, plainCursor.peer, plainCursor);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
}
;
_.serialize = function serialize_189(writer){
  var cursor, cursor$iterator;
  for (cursor$iterator = $iterator_0(new AbstractMap$2(this.cursors)); cursor$iterator.val$outerIter2.hasNext();) {
    cursor = dynamicCast($next_2(cursor$iterator), 77);
    $writeObject(writer, 1, cursor);
  }
}
;
var Lim_actor_model_modules_messages_entity_PlainCursorsStorage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'PlainCursorsStorage', 286, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function UnreadMessage(){
}

function UnreadMessage_0(peer, rid, sortDate){
  this.peer = peer;
  this.rid = rid;
  this.sortDate = sortDate;
}

defineClass(71, 4, {4:1, 71:1}, UnreadMessage, UnreadMessage_0);
_.equals$ = function equals_11(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_messages_entity_UnreadMessage_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 71);
  if (neq(this.rid, that.rid))
    return false;
  if (!$equals_2(this.peer, that.peer))
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_7(){
  var result;
  result = $hashCode_1(this.peer);
  result = 31 * result + toInt(xor(this.rid, shru(this.rid, 32)));
  return result;
}
;
_.parse_0 = function parse_197(values){
  this.peer = fromUniqueId($getLong(values, 1));
  this.rid = $getLong(values, 2);
  this.sortDate = $getLong(values, 3);
}
;
_.serialize = function serialize_190(writer){
  $writeLong(writer, 1, $getUnuqueId(this.peer));
  $writeLong(writer, 2, this.rid);
  $writeLong(writer, 3, this.sortDate);
}
;
_.rid = {l:0, m:0, h:0};
_.sortDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_messages_entity_UnreadMessage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'UnreadMessage', 71, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $getUnread(this$static, peer){
  $containsKey(this$static.unreadMessages, peer) || $put_1(this$static.unreadMessages, peer, new HashSet);
  return dynamicCast($get_2(this$static.unreadMessages, peer), 34);
}

function UnreadMessagesStorage(){
  this.unreadMessages = new HashMap;
}

defineClass(285, 4, {4:1, 285:1}, UnreadMessagesStorage);
_.parse_0 = function parse_198(values){
  var d, d$iterator, data_0, e, u;
  data_0 = $getRepeatedBytes(values, 1);
  for (d$iterator = new AbstractList$IteratorImpl(data_0); d$iterator.i < d$iterator.this$01.size_1();) {
    d = (checkCriticalElement(d$iterator.i < d$iterator.this$01.size_1()) , dynamicCast(d$iterator.this$01.get_1(d$iterator.last = d$iterator.i++), 36));
    try {
      u = dynamicCast(parse_159(new UnreadMessage, d), 71);
      $add_1($getUnread(this, u.peer), u);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
}
;
_.serialize = function serialize_191(writer){
  var peer, peer$iterator, u, u$iterator;
  for (peer$iterator = $iterator(new AbstractMap$1(this.unreadMessages)); $hasNext(peer$iterator.val$outerIter2);) {
    peer = dynamicCast($next_1(peer$iterator), 19);
    for (u$iterator = $iterator(new AbstractMap$1(dynamicCast($get_2(this.unreadMessages, peer), 34).map_0)); $hasNext(u$iterator.val$outerIter2);) {
      u = dynamicCast($next_1(u$iterator), 71);
      $writeObject(writer, 1, u);
    }
  }
}
;
var Lim_actor_model_modules_messages_entity_UnreadMessagesStorage_2_classLit = createForClass('im.actor.model.modules.messages.entity', 'UnreadMessagesStorage', 285, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $onConversationHidden(this$static, peer){
  !!this$static.visiblePeer && $equals_2(this$static.visiblePeer, peer) && (this$static.visiblePeer = null);
}

function $onConversationVisible(this$static, peer){
  this$static.visiblePeer = peer;
}

function $onMessagesRead(this$static, peer, fromDate){
  var isChanged, p_0, p$array, p$index, p$max;
  isChanged = false;
  for (p$array = dynamicCast($toArray_0(this$static.pendingStorage.notifications, initDim(Lim_actor_model_modules_notifications_entity_PendingNotification_2_classLit, {805:1, 3:1, 6:1}, 81, 0, 0, 1)), 805) , p$index = 0 , p$max = p$array.length; p$index < p$max; ++p$index) {
    p_0 = p$array[p$index];
    if ($equals_2(p_0.peer, peer) && lte(p_0.date, fromDate)) {
      $remove_4(this$static.pendingStorage.notifications, p_0);
      isChanged = true;
    }
  }
  isChanged && $put_0(this$static.storage, {l:0, m:0, h:0}, $toByteArray(this$static.pendingStorage));
}

function $onNewMessage(this$static, peer, sender, date, description){
  var allPending, destNotifications, i_0, isEnabled, p_0, p$iterator, p$iterator0, peers, res;
  isEnabled = $loadValue(this$static.modules.settings, 'sync.notification.chat.' + $getChatKey(peer) + '.enabled');
  allPending = this$static.pendingStorage.notifications;
  if (isEnabled) {
    $add_0(allPending, new PendingNotification_0(peer, sender, date, description));
    $put_0(this$static.storage, {l:0, m:0, h:0}, $toByteArray(this$static.pendingStorage));
  }
  if (this$static.modules.configuration.notificationProvider) {
    if (!!this$static.visiblePeer && $equals_2(this$static.visiblePeer, peer)) {
      $loadValue(this$static.modules.settings, 'app.tones_enabled') && this$static.modules.messenger;
      return;
    }
    if (this$static.isDialogsVisible) {
      $loadValue(this$static.modules.settings, 'app.tones_enabled') && this$static.modules.messenger;
      return;
    }
    if (!isEnabled) {
      return;
    }
    if (allPending.array.length <= 10) {
      destNotifications = new ArrayList;
      for (i_0 = 0; i_0 < allPending.array.length; i_0++) {
        $add_0(destNotifications, dynamicCast($get_3(allPending, allPending.array.length - 1 - i_0), 81));
      }
    }
     else {
      destNotifications = new ArrayList;
      for (i_0 = 0; i_0 < 10; i_0++) {
        $add_0(destNotifications, dynamicCast($get_3(allPending, allPending.array.length - 1 - i_0), 81));
      }
    }
    res = new ArrayList;
    for (p$iterator0 = new AbstractList$IteratorImpl(destNotifications); p$iterator0.i < p$iterator0.this$01.size_1();) {
      p_0 = (checkCriticalElement(p$iterator0.i < p$iterator0.this$01.size_1()) , dynamicCast(p$iterator0.this$01.get_1(p$iterator0.last = p$iterator0.i++), 81));
      $add_0(res, new Notification_0(p_0.peer, p_0.sender, p_0.content_0));
    }
    peers = new HashSet;
    for (p$iterator = new AbstractList$IteratorImpl(allPending); p$iterator.i < p$iterator.this$01.size_1();) {
      p_0 = (checkCriticalElement(p$iterator.i < p$iterator.this$01.size_1()) , dynamicCast(p$iterator.this$01.get_1(p$iterator.last = p$iterator.i++), 81));
      $add_1(peers, p_0.peer);
    }
    $onNotification(this$static.modules.messenger, res);
  }
}

function NotificationsActor(messenger){
  ModuleActor.call(this, messenger);
  this.storage = messenger.notifications.notificationsStorage;
}

defineClass(588, 27, {}, NotificationsActor);
_.onReceive = function onReceive_17(message){
  var newMessage, read;
  if (instanceOf(message, 227)) {
    newMessage = dynamicCast(message, 227);
    $onNewMessage(this, newMessage.peer, newMessage.sender, newMessage.sortDate, newMessage.contentDescription);
  }
   else if (instanceOf(message, 228)) {
    read = dynamicCast(message, 228);
    $onMessagesRead(this, read.peer, read.fromDate);
  }
   else 
    instanceOf(message, 229)?$onConversationVisible(this, dynamicCast(message, 229).peer):instanceOf(message, 230)?$onConversationHidden(this, dynamicCast(message, 230).peer):instanceOf(message, 232)?undefined:instanceOf(message, 231)?undefined:instanceOf(message, 345)?(this.isDialogsVisible = true , !!this.modules.configuration.notificationProvider && this.modules.messenger):instanceOf(message, 346)?(this.isDialogsVisible = false):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
_.preStart = function preStart_11(){
  var e, storage;
  this.pendingStorage = new PendingStorage;
  storage = $get_0(this.storage, {l:0, m:0, h:0});
  if (storage != null) {
    try {
      this.pendingStorage = dynamicCast(parse_159(new PendingStorage, storage), 283);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
}
;
_.isDialogsVisible = false;
var Lim_actor_model_modules_notifications_NotificationsActor_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor', 588, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function NotificationsActor$MessagesRead(peer, fromDate){
  this.peer = peer;
  this.fromDate = fromDate;
}

defineClass(228, 1, {228:1}, NotificationsActor$MessagesRead);
_.fromDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_notifications_NotificationsActor$MessagesRead_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/MessagesRead', 228, Ljava_lang_Object_2_classLit);
function NotificationsActor$NewMessage(peer, sender, sortDate, contentDescription){
  this.peer = peer;
  this.sender = sender;
  this.sortDate = sortDate;
  this.contentDescription = contentDescription;
}

defineClass(227, 1, {227:1}, NotificationsActor$NewMessage);
_.sender = 0;
_.sortDate = {l:0, m:0, h:0};
var Lim_actor_model_modules_notifications_NotificationsActor$NewMessage_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/NewMessage', 227, Ljava_lang_Object_2_classLit);
function NotificationsActor$OnAppHidden(){
}

defineClass(232, 1, {232:1}, NotificationsActor$OnAppHidden);
var Lim_actor_model_modules_notifications_NotificationsActor$OnAppHidden_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/OnAppHidden', 232, Ljava_lang_Object_2_classLit);
function NotificationsActor$OnAppVisible(){
}

defineClass(231, 1, {231:1}, NotificationsActor$OnAppVisible);
var Lim_actor_model_modules_notifications_NotificationsActor$OnAppVisible_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/OnAppVisible', 231, Ljava_lang_Object_2_classLit);
function NotificationsActor$OnConversationHidden(peer){
  this.peer = peer;
}

defineClass(230, 1, {230:1}, NotificationsActor$OnConversationHidden);
var Lim_actor_model_modules_notifications_NotificationsActor$OnConversationHidden_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/OnConversationHidden', 230, Ljava_lang_Object_2_classLit);
function NotificationsActor$OnConversationVisible(peer){
  this.peer = peer;
}

defineClass(229, 1, {229:1}, NotificationsActor$OnConversationVisible);
var Lim_actor_model_modules_notifications_NotificationsActor$OnConversationVisible_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/OnConversationVisible', 229, Ljava_lang_Object_2_classLit);
function NotificationsActor$OnDialogsHidden(){
}

defineClass(346, 1, {346:1}, NotificationsActor$OnDialogsHidden);
var Lim_actor_model_modules_notifications_NotificationsActor$OnDialogsHidden_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/OnDialogsHidden', 346, Ljava_lang_Object_2_classLit);
function NotificationsActor$OnDialogsVisible(){
}

defineClass(345, 1, {345:1}, NotificationsActor$OnDialogsVisible);
var Lim_actor_model_modules_notifications_NotificationsActor$OnDialogsVisible_2_classLit = createForClass('im.actor.model.modules.notifications', 'NotificationsActor/OnDialogsVisible', 345, Ljava_lang_Object_2_classLit);
function PendingNotification(){
}

function PendingNotification_0(peer, sender, date, content_0){
  this.peer = peer;
  this.sender = sender;
  this.date = date;
  this.content_0 = content_0;
}

defineClass(81, 4, {4:1, 81:1}, PendingNotification, PendingNotification_0);
_.parse_0 = function parse_199(values){
  this.peer = fromUniqueId($getLong(values, 1));
  this.sender = convertInt($getLong(values, 2));
  this.date = $getLong(values, 4);
  this.content_0 = fromBytes_4($getBytes(values, 5));
}
;
_.serialize = function serialize_192(writer){
  $writeLong(writer, 1, $getUnuqueId(this.peer));
  $writeInt(writer, 2, this.sender);
  $writeLong(writer, 4, this.date);
  $writeObject(writer, 5, this.content_0);
}
;
_.date = {l:0, m:0, h:0};
_.sender = 0;
var Lim_actor_model_modules_notifications_entity_PendingNotification_2_classLit = createForClass('im.actor.model.modules.notifications.entity', 'PendingNotification', 81, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function PendingStorage(){
  this.notifications = new ArrayList;
}

defineClass(283, 4, {4:1, 283:1}, PendingStorage);
_.parse_0 = function parse_200(values){
  var count, i_0, stubs;
  count = $getRepeatedCount(values, 1);
  if (count > 0) {
    stubs = new ArrayList;
    for (i_0 = 0; i_0 < count; i_0++) {
      $add_0(stubs, new PendingNotification);
    }
    this.notifications = $getRepeatedObj(values, 1, stubs);
  }
}
;
_.serialize = function serialize_193(writer){
  $writeRepeatedObj(writer, 1, this.notifications);
}
;
var Lim_actor_model_modules_notifications_entity_PendingStorage_2_classLit = createForClass('im.actor.model.modules.notifications.entity', 'PendingStorage', 283, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function OwnPresenceActor(messenger){
  ModuleActor.call(this, messenger);
}

defineClass(585, 27, {}, OwnPresenceActor);
_.onReceive = function onReceive_18(message){
  instanceOf(message, 172)?(this.isVisible = true , $sendOnce(this.context.actorScope.actorRef, new OwnPresenceActor$PerformOnline)):instanceOf(message, 350)?(this.isVisible = false , $sendOnce(this.context.actorScope.actorRef, new OwnPresenceActor$PerformOnline)):instanceOf(message, 173)?($request_0(this, new RequestSetOnline_0(this.isVisible), new OwnPresenceActor$1) , this.isVisible && $sendOnce_0(this.context.actorScope.actorRef, new OwnPresenceActor$PerformOnline, {l:60000, m:0, h:0})):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
_.isVisible = false;
var Lim_actor_model_modules_presence_OwnPresenceActor_2_classLit = createForClass('im.actor.model.modules.presence', 'OwnPresenceActor', 585, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function OwnPresenceActor$1(){
}

defineClass(630, 1, {}, OwnPresenceActor$1);
_.onError_0 = function onError_13(e){
}
;
_.onResult_0 = function onResult_13(response){
  dynamicCast(response, 88);
}
;
var Lim_actor_model_modules_presence_OwnPresenceActor$1_2_classLit = createForClass('im.actor.model.modules.presence', 'OwnPresenceActor/1', 630, Ljava_lang_Object_2_classLit);
function OwnPresenceActor$OnAppHidden(){
}

defineClass(350, 1, {350:1}, OwnPresenceActor$OnAppHidden);
var Lim_actor_model_modules_presence_OwnPresenceActor$OnAppHidden_2_classLit = createForClass('im.actor.model.modules.presence', 'OwnPresenceActor/OnAppHidden', 350, Ljava_lang_Object_2_classLit);
function OwnPresenceActor$OnAppVisible(){
}

defineClass(172, 1, {172:1}, OwnPresenceActor$OnAppVisible);
var Lim_actor_model_modules_presence_OwnPresenceActor$OnAppVisible_2_classLit = createForClass('im.actor.model.modules.presence', 'OwnPresenceActor/OnAppVisible', 172, Ljava_lang_Object_2_classLit);
function OwnPresenceActor$PerformOnline(){
}

defineClass(173, 1, {173:1}, OwnPresenceActor$PerformOnline);
var Lim_actor_model_modules_presence_OwnPresenceActor$PerformOnline_2_classLit = createForClass('im.actor.model.modules.presence', 'OwnPresenceActor/PerformOnline', 173, Ljava_lang_Object_2_classLit);
function $onGroupOnline(this$static, gid, count){
  var vm;
  vm = dynamicCast($get_1(this$static.modules.groups.collection, fromInt(gid)), 94);
  !!vm && $change(vm.presence, valueOf(count));
}

function $onNewSessionCreated_0(this$static){
  var gid, gid$iterator, group, groupPeers, uid, uid$iterator, user, userPeers;
  userPeers = new ArrayList;
  for (uid$iterator = $iterator(new AbstractMap$1(this$static.uids.map_0)); $hasNext(uid$iterator.val$outerIter2);) {
    uid = dynamicCast($next_1(uid$iterator), 33).value_0;
    user = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11);
    if (!user) {
      continue;
    }
    $add_0(userPeers, new UserOutPeer_0(uid, user.accessHash));
  }
  userPeers.array.length > 0 && $request_0(this$static, new RequestSubscribeToOnline_0(userPeers), new ModuleActor$1);
  groupPeers = new ArrayList;
  for (gid$iterator = $iterator(new AbstractMap$1(this$static.gids.map_0)); $hasNext(gid$iterator.val$outerIter2);) {
    gid = dynamicCast($next_1(gid$iterator), 33).value_0;
    group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(gid)), 21);
    if (!group) {
      continue;
    }
    $add_0(groupPeers, new GroupOutPeer_0(group.groupId, group.accessHash));
  }
  groupPeers.array.length > 0 && $request_0(this$static, new RequestSubscribeToGroupOnline_0(groupPeers), new ModuleActor$1);
}

function $onUserLastSeen(this$static, uid){
  var vm;
  vm = dynamicCast($get_1(this$static.modules.users.collection, fromInt(uid)), 53);
  !!vm && $change(vm.presence, new UserPresence_0);
}

function $onUserOffline(this$static, uid){
  var vm;
  vm = dynamicCast($get_1(this$static.modules.users.collection, fromInt(uid)), 53);
  !!vm && $change(vm.presence, new UserPresence);
}

function $onUserOnline(this$static, uid){
  var vm;
  vm = dynamicCast($get_1(this$static.modules.users.collection, fromInt(uid)), 53);
  !!vm && $change(vm.presence, new UserPresence);
  $sendOnce_0(this$static.context.actorScope.actorRef, new PresenceActor$UserOffline(uid), {l:300000, m:0, h:0});
}

function $subscribe_2(this$static, peer){
  var group, peers, user;
  if (peer.peerType == ($clinit_PeerType_0() , PRIVATE_0)) {
    if ($contains_0(this$static.uids, valueOf(peer.peerId))) {
      return;
    }
    user = $getUser_1(this$static, peer.peerId);
    if (!user) {
      return;
    }
    $add_1(this$static.uids, valueOf(user.uid));
    peers = new ArrayList;
    $add_0(peers, new UserOutPeer_0(user.uid, user.accessHash));
    $request_0(this$static, new RequestSubscribeToOnline_0(peers), new ModuleActor$1);
  }
   else if (peer.peerType == GROUP_0) {
    if ($contains_0(this$static.gids, valueOf(peer.peerId))) {
      return;
    }
    group = $getGroup_1(this$static, peer.peerId);
    if (!group) {
      return;
    }
    $add_1(this$static.gids, valueOf(peer.peerId));
    peers = new ArrayList;
    $add_0(peers, new GroupOutPeer_0(group.groupId, group.accessHash));
    $request_0(this$static, new RequestSubscribeToGroupOnline_0(peers), new ModuleActor$1);
  }
}

function PresenceActor(messenger){
  ModuleActor.call(this, messenger);
  this.uids = new HashSet;
  this.gids = new HashSet;
}

defineClass(623, 27, {}, PresenceActor);
_.onReceive = function onReceive_19(message){
  var groupOnline, lastSeen, offline, online;
  if (instanceOf(message, 166)) {
    online = dynamicCast(message, 166);
    $onUserOnline(this, online.uid);
  }
   else if (instanceOf(message, 122)) {
    offline = dynamicCast(message, 122);
    $onUserOffline(this, offline.uid);
  }
   else if (instanceOf(message, 167)) {
    lastSeen = dynamicCast(message, 167);
    $onUserLastSeen(this, lastSeen.uid);
  }
   else if (instanceOf(message, 168)) {
    groupOnline = dynamicCast(message, 168);
    $onGroupOnline(this, groupOnline.gid, groupOnline.count);
  }
   else 
    instanceOf(message, 233)?$subscribe_2(this, dynamicCast(message, 233).peer):instanceOf(message, 347)?$onNewSessionCreated_0(this):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
var Lim_actor_model_modules_presence_PresenceActor_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor', 623, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function PresenceActor$1(val$messenger){
  this.val$messenger1 = val$messenger;
}

defineClass(450, 1, {}, PresenceActor$1);
_.create_0 = function create_30(){
  return new PresenceActor(this.val$messenger1);
}
;
var Lim_actor_model_modules_presence_PresenceActor$1_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/1', 450, Ljava_lang_Object_2_classLit);
function PresenceActor$2(){
}

defineClass(451, 1, {}, PresenceActor$2);
_.createMailbox = function createMailbox(queue){
  return new PresenceActor$2$1(queue);
}
;
var Lim_actor_model_modules_presence_PresenceActor$2_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/2', 451, Ljava_lang_Object_2_classLit);
function PresenceActor$2$1($anonymous0){
  Mailbox.call(this, $anonymous0);
}

defineClass(624, 348, {}, PresenceActor$2$1);
_.isEqualEnvelope = function isEqualEnvelope_0(a, b){
  if (equals_Ljava_lang_Object__Z__devirtual$(a.message_0, b.message_0)) {
    return true;
  }
  return getClass__Ljava_lang_Class___devirtual$(a.message_0) == getClass__Ljava_lang_Class___devirtual$(b.message_0);
}
;
var Lim_actor_model_modules_presence_PresenceActor$2$1_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/2/1', 624, Lim_actor_model_droidkit_actors_mailbox_Mailbox_2_classLit);
function PresenceActor$GroupOnline(gid, count){
  this.gid = gid;
  this.count = count;
}

defineClass(168, 1, {168:1}, PresenceActor$GroupOnline);
_.equals$ = function equals_12(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_presence_PresenceActor$GroupOnline_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 168);
  if (this.count != that.count)
    return false;
  if (this.gid != that.gid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_8(){
  var result;
  result = this.gid;
  result = 31 * result + this.count;
  return result;
}
;
_.count = 0;
_.gid = 0;
var Lim_actor_model_modules_presence_PresenceActor$GroupOnline_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/GroupOnline', 168, Ljava_lang_Object_2_classLit);
function PresenceActor$SessionCreated(){
}

defineClass(347, 1, {347:1}, PresenceActor$SessionCreated);
var Lim_actor_model_modules_presence_PresenceActor$SessionCreated_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/SessionCreated', 347, Ljava_lang_Object_2_classLit);
function PresenceActor$Subscribe(peer){
  this.peer = peer;
}

defineClass(233, 1, {233:1}, PresenceActor$Subscribe);
var Lim_actor_model_modules_presence_PresenceActor$Subscribe_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/Subscribe', 233, Ljava_lang_Object_2_classLit);
function PresenceActor$UserLastSeen(uid, date){
  this.uid = uid;
  this.date = date;
}

defineClass(167, 1, {167:1}, PresenceActor$UserLastSeen);
_.equals$ = function equals_13(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_presence_PresenceActor$UserLastSeen_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 167);
  if (neq(this.date, that.date))
    return false;
  if (this.uid != that.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_9(){
  var result;
  result = this.uid;
  result = 31 * result + toInt(xor(this.date, shru(this.date, 32)));
  return result;
}
;
_.date = {l:0, m:0, h:0};
_.uid = 0;
var Lim_actor_model_modules_presence_PresenceActor$UserLastSeen_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/UserLastSeen', 167, Ljava_lang_Object_2_classLit);
function PresenceActor$UserOffline(uid){
  this.uid = uid;
}

defineClass(122, 1, {122:1}, PresenceActor$UserOffline);
_.equals$ = function equals_14(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_presence_PresenceActor$UserOffline_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 122);
  if (this.uid != that.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_10(){
  return this.uid;
}
;
_.uid = 0;
var Lim_actor_model_modules_presence_PresenceActor$UserOffline_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/UserOffline', 122, Ljava_lang_Object_2_classLit);
function PresenceActor$UserOnline(uid){
  this.uid = uid;
}

defineClass(166, 1, {166:1}, PresenceActor$UserOnline);
_.equals$ = function equals_15(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_presence_PresenceActor$UserOnline_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 166);
  if (this.uid != that.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_11(){
  return this.uid;
}
;
_.uid = 0;
var Lim_actor_model_modules_presence_PresenceActor$UserOnline_2_classLit = createForClass('im.actor.model.modules.presence', 'PresenceActor/UserOnline', 166, Ljava_lang_Object_2_classLit);
function PushRegisterActor(modules){
  ModuleActor.call(this, modules);
}

defineClass(638, 27, {}, PushRegisterActor);
_.onReceive = function onReceive_20(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
_.preStart = function preStart_12(){
  var apnsId, projectId, token;
  if ($getBool(this.modules.preferences, 'push.google', false)) {
    if (!$getBool(this.modules.preferences, 'push.google.registered', false)) {
      projectId = $getLong_1(this.modules.preferences, 'push.google.id', {l:0, m:0, h:0});
      token = $getString(this.modules.preferences, 'push.google.token');
      $putBool(this.modules.preferences, 'push.google', true);
      $putBool(this.modules.preferences, 'push.google.registered', false);
      $putLong(this.modules.preferences, 'push.google.id', projectId);
      $putString(this.modules.preferences, 'push.google.token', token);
      $request_0(this, new RequestRegisterGooglePush_0(projectId, token), new PushRegisterActor$1(this));
    }
  }
  if ($getBool(this.modules.preferences, 'push.apple', false)) {
    if (!$getBool(this.modules.preferences, 'push.apple.registered', false)) {
      apnsId = $getInt(this.modules.preferences, 'push.apple.id', 0);
      token = $getString(this.modules.preferences, 'push.apple.token');
      $putBool(this.modules.preferences, 'push.apple', true);
      $putBool(this.modules.preferences, 'push.apple.registered', false);
      $putInt(this.modules.preferences, 'push.apple.id', apnsId);
      $putString(this.modules.preferences, 'push.apple.token', token);
      $request_0(this, new RequestRegisterApplePush_0(apnsId, token), new PushRegisterActor$2(this));
    }
  }
}
;
var Lim_actor_model_modules_push_PushRegisterActor_2_classLit = createForClass('im.actor.model.modules.push', 'PushRegisterActor', 638, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_17(this$static){
  $putBool(this$static.this$01.modules.preferences, 'push.google.registered', true);
}

function PushRegisterActor$1(this$0){
  this.this$01 = this$0;
}

defineClass(654, 1, {}, PushRegisterActor$1);
_.onError_0 = function onError_14(e){
}
;
_.onResult_0 = function onResult_14(response){
  $onResult_17(this, dynamicCast(response, 88));
}
;
var Lim_actor_model_modules_push_PushRegisterActor$1_2_classLit = createForClass('im.actor.model.modules.push', 'PushRegisterActor/1', 654, Ljava_lang_Object_2_classLit);
function $onResult_18(this$static){
  $putBool(this$static.this$01.modules.preferences, 'push.apple.registered', true);
}

function PushRegisterActor$2(this$0){
  this.this$01 = this$0;
}

defineClass(655, 1, {}, PushRegisterActor$2);
_.onError_0 = function onError_15(e){
}
;
_.onResult_0 = function onResult_15(response){
  $onResult_18(this, dynamicCast(response, 88));
}
;
var Lim_actor_model_modules_push_PushRegisterActor$2_2_classLit = createForClass('im.actor.model.modules.push', 'PushRegisterActor/2', 655, Ljava_lang_Object_2_classLit);
function $onContactsUpdated(this$static, contactsList){
  var i_0, updated, user;
  updated = new ArrayList;
  for (i_0 = 0; i_0 < contactsList.length; i_0++) {
    user = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(contactsList[i_0])), 11);
    $add_0(updated, new SearchEntity_0(user_0(user.uid), add_0({l:0, m:1024, h:0}, fromInt(i_0)), user.avatar, user.localName == null?user.name_0:user.localName));
  }
  $addOrUpdateItems_0(this$static.listEngine, updated);
}

function $onDialogsUpdated(this$static, dialogs){
  var d, d$iterator, updated;
  updated = new ArrayList;
  for (d$iterator = new AbstractList$IteratorImpl(dialogs); d$iterator.i < d$iterator.this$01.size_1();) {
    d = (checkCriticalElement(d$iterator.i < d$iterator.this$01.size_1()) , dynamicCast(d$iterator.this$01.get_1(d$iterator.last = d$iterator.i++), 49));
    $add_0(updated, new SearchEntity_0(d.peer, d.sortDate, d.dialogAvatar, d.dialogTitle));
  }
  $addOrUpdateItems_0(this$static.listEngine, updated);
}

function SearchActor(modules){
  ModuleActor.call(this, modules);
}

defineClass(664, 27, {}, SearchActor);
_.onReceive = function onReceive_21(message){
  var contactsUpdated, onDialogsUpdated;
  if (instanceOf(message, 256)) {
    onDialogsUpdated = dynamicCast(message, 256);
    $onDialogsUpdated(this, onDialogsUpdated.dialogs);
  }
   else if (instanceOf(message, 257)) {
    contactsUpdated = dynamicCast(message, 257);
    $onContactsUpdated(this, contactsUpdated.contactsList);
  }
   else {
    !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this, new DeadLetter(message));
  }
}
;
_.preStart = function preStart_13(){
  this.listEngine = this.modules.search.searchList;
}
;
var Lim_actor_model_modules_search_SearchActor_2_classLit = createForClass('im.actor.model.modules.search', 'SearchActor', 664, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function SearchActor$OnContactsUpdated(contactsList){
  this.contactsList = contactsList;
}

defineClass(257, 1, {257:1}, SearchActor$OnContactsUpdated);
var Lim_actor_model_modules_search_SearchActor$OnContactsUpdated_2_classLit = createForClass('im.actor.model.modules.search', 'SearchActor/OnContactsUpdated', 257, Ljava_lang_Object_2_classLit);
function SearchActor$OnDialogsUpdated(dialogs){
  this.dialogs = dialogs;
}

defineClass(256, 1, {256:1}, SearchActor$OnDialogsUpdated);
var Lim_actor_model_modules_search_SearchActor$OnDialogsUpdated_2_classLit = createForClass('im.actor.model.modules.search', 'SearchActor/OnDialogsUpdated', 256, Ljava_lang_Object_2_classLit);
function $saveState(this$static){
  $putBytes(this$static.modules.preferences, 'settings_sync_state', $toByteArray(this$static.syncState));
}

function SettingsSyncActor(modules){
  ModuleActor.call(this, modules);
}

defineClass(682, 27, {}, SettingsSyncActor);
_.onReceive = function onReceive_22(message){
  !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
  $reply(this, new DeadLetter(message));
}
;
_.preStart = function preStart_14(){
  var action, action$iterator, data_0, e;
  this.syncState = new SettingsSyncState;
  data_0 = $getBytes_1(this.modules.preferences, 'settings_sync_state');
  if (data_0 != null) {
    try {
      this.syncState = dynamicCast(parse_159(new SettingsSyncState, data_0), 281);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
      }
       else 
        throw unwrap($e0);
    }
  }
  for (action$iterator = new AbstractList$IteratorImpl(this.syncState.pendingActions); action$iterator.i < action$iterator.this$01.size_1();) {
    action = (checkCriticalElement(action$iterator.i < action$iterator.this$01.size_1()) , dynamicCast(action$iterator.this$01.get_1(action$iterator.last = action$iterator.i++), 191));
    $request_0(this, new RequestEditParameter_0(action.key, action.value_0), new SettingsSyncActor$2(this, action));
  }
  $getBool(this.modules.preferences, 'settings_sync_state_loaded', false) || $request_0(this, new RequestGetParameters, new SettingsSyncActor$1(this));
}
;
var Lim_actor_model_modules_settings_SettingsSyncActor_2_classLit = createForClass('im.actor.model.modules.settings', 'SettingsSyncActor', 682, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_19(this$static, response){
  var p_0, p$iterator;
  for (p$iterator = new AbstractList$IteratorImpl(response.parameters); p$iterator.i < p$iterator.this$01.size_1();) {
    p_0 = (checkCriticalElement(p$iterator.i < p$iterator.this$01.size_1()) , dynamicCast(p$iterator.this$01.get_1(p$iterator.last = p$iterator.i++), 427));
    $putString(this$static.this$01.modules.preferences, p_0.key, p_0.value_0);
  }
  $putBool(this$static.this$01.modules.preferences, 'settings_sync_state_loaded', true);
}

function SettingsSyncActor$1(this$0){
  this.this$01 = this$0;
}

defineClass(692, 1, {}, SettingsSyncActor$1);
_.onError_0 = function onError_16(e){
}
;
_.onResult_0 = function onResult_16(response){
  $onResult_19(this, dynamicCast(response, 321));
}
;
var Lim_actor_model_modules_settings_SettingsSyncActor$1_2_classLit = createForClass('im.actor.model.modules.settings', 'SettingsSyncActor/1', 692, Ljava_lang_Object_2_classLit);
function $onResult_20(this$static, response){
  $remove_4(this$static.this$01.syncState.pendingActions, this$static.val$action2);
  $saveState(this$static.this$01);
  $onUpdateReceived_0(this$static.this$01.modules.updates, new SeqUpdate_0(response.seq, response.state, 131, $toByteArray(new UpdateParameterChanged_0(this$static.val$action2.key, this$static.val$action2.value_0))));
}

function SettingsSyncActor$2(this$0, val$action){
  this.this$01 = this$0;
  this.val$action2 = val$action;
}

defineClass(693, 1, {}, SettingsSyncActor$2);
_.onError_0 = function onError_17(e){
}
;
_.onResult_0 = function onResult_17(response){
  $onResult_20(this, dynamicCast(response, 89));
}
;
var Lim_actor_model_modules_settings_SettingsSyncActor$2_2_classLit = createForClass('im.actor.model.modules.settings', 'SettingsSyncActor/2', 693, Ljava_lang_Object_2_classLit);
function SettingsSyncAction(){
}

function fromBytes_8(data_0){
  return dynamicCast(parse_159(new SettingsSyncAction, data_0), 191);
}

defineClass(191, 4, {4:1, 191:1}, SettingsSyncAction);
_.parse_0 = function parse_201(values){
  this.key = convertString($getBytes(values, 1));
  this.value_0 = convertString($getBytes_0(values, 2));
}
;
_.serialize = function serialize_194(writer){
  $writeString(writer, 1, this.key);
  this.value_0 != null && $writeString(writer, 2, this.value_0);
}
;
var Lim_actor_model_modules_settings_entity_SettingsSyncAction_2_classLit = createForClass('im.actor.model.modules.settings.entity', 'SettingsSyncAction', 191, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function SettingsSyncState(){
  this.pendingActions = new ArrayList;
}

defineClass(281, 4, {4:1, 281:1}, SettingsSyncState);
_.parse_0 = function parse_202(values){
  var i_0, pending;
  pending = $getRepeatedBytes(values, 1);
  for (i_0 = 0; i_0 < pending.array.length; i_0++) {
    $add_0(this.pendingActions, fromBytes_8((checkElementIndex(i_0, pending.array.length) , dynamicCast(pending.array[i_0], 36))));
  }
}
;
_.serialize = function serialize_195(writer){
  var action, action$iterator;
  for (action$iterator = new AbstractList$IteratorImpl(this.pendingActions); action$iterator.i < action$iterator.this$01.size_1();) {
    action = (checkCriticalElement(action$iterator.i < action$iterator.this$01.size_1()) , dynamicCast(action$iterator.this$01.get_1(action$iterator.last = action$iterator.i++), 191));
    $writeObject(writer, 1, action);
  }
}
;
var Lim_actor_model_modules_settings_entity_SettingsSyncState_2_classLit = createForClass('im.actor.model.modules.settings.entity', 'SettingsSyncState', 281, Lim_actor_model_droidkit_bser_BserObject_2_classLit);
function $onContactsChanged_0(this$static, isEmpty){
  $onContactsChanged_1(this$static.modules.appStateModule.appStateVM, isEmpty);
}

function $onDialogsChanged_0(this$static, isEmpty){
  $onDialogsChanged_1(this$static.modules.appStateModule.appStateVM, isEmpty);
}

function ListsStatesActor(modules){
  ModuleActor.call(this, modules);
}

defineClass(641, 27, {}, ListsStatesActor);
_.onReceive = function onReceive_23(message){
  instanceOf(message, 254)?$onContactsChanged_0(this, dynamicCast(message, 254).isEmpty):instanceOf(message, 255)?$onDialogsChanged_0(this, dynamicCast(message, 255).isEmpty):instanceOf(message, 252)?$onPhoneImported(this.modules.appStateModule.appStateVM):instanceOf(message, 358)?$onContactsLoaded_0(this.modules.appStateModule.appStateVM):instanceOf(message, 253)?$onDialogsLoaded_0(this.modules.appStateModule.appStateVM):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
var Lim_actor_model_modules_state_ListsStatesActor_2_classLit = createForClass('im.actor.model.modules.state', 'ListsStatesActor', 641, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function ListsStatesActor$OnBookImported(){
}

defineClass(252, 1, {252:1}, ListsStatesActor$OnBookImported);
var Lim_actor_model_modules_state_ListsStatesActor$OnBookImported_2_classLit = createForClass('im.actor.model.modules.state', 'ListsStatesActor/OnBookImported', 252, Ljava_lang_Object_2_classLit);
function ListsStatesActor$OnContactsChanged(isEmpty){
  this.isEmpty = isEmpty;
}

defineClass(254, 1, {254:1}, ListsStatesActor$OnContactsChanged);
_.isEmpty = false;
var Lim_actor_model_modules_state_ListsStatesActor$OnContactsChanged_2_classLit = createForClass('im.actor.model.modules.state', 'ListsStatesActor/OnContactsChanged', 254, Ljava_lang_Object_2_classLit);
function ListsStatesActor$OnContactsLoaded(){
}

defineClass(358, 1, {358:1}, ListsStatesActor$OnContactsLoaded);
var Lim_actor_model_modules_state_ListsStatesActor$OnContactsLoaded_2_classLit = createForClass('im.actor.model.modules.state', 'ListsStatesActor/OnContactsLoaded', 358, Ljava_lang_Object_2_classLit);
function ListsStatesActor$OnDialogsChanged(isEmpty){
  this.isEmpty = isEmpty;
}

defineClass(255, 1, {255:1}, ListsStatesActor$OnDialogsChanged);
_.isEmpty = false;
var Lim_actor_model_modules_state_ListsStatesActor$OnDialogsChanged_2_classLit = createForClass('im.actor.model.modules.state', 'ListsStatesActor/OnDialogsChanged', 255, Ljava_lang_Object_2_classLit);
function ListsStatesActor$OnDialogsLoaded(){
}

defineClass(253, 1, {253:1}, ListsStatesActor$OnDialogsLoaded);
var Lim_actor_model_modules_state_ListsStatesActor$OnDialogsLoaded_2_classLit = createForClass('im.actor.model.modules.state', 'ListsStatesActor/OnDialogsLoaded', 253, Ljava_lang_Object_2_classLit);
function $onTyping_1(this$static, peer){
  var outPeer;
  if (lt(sub_0(getActorTime(), this$static.lastTypingTime), {l:3000, m:0, h:0})) {
    return;
  }
  this$static.lastTypingTime = getActorTime();
  outPeer = $buidOutPeer(this$static, peer);
  if (!outPeer) {
    return;
  }
  $request_0(this$static, new RequestTyping_0(outPeer, ($clinit_TypingType() , TEXT)), new ModuleActor$1);
}

function OwnTypingActor(messenger){
  ModuleActor.call(this, messenger);
}

defineClass(591, 27, {}, OwnTypingActor);
_.onReceive = function onReceive_24(message){
  instanceOf(message, 234)?$onTyping_1(this, dynamicCast(message, 234).peer):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
_.lastTypingTime = {l:0, m:0, h:0};
var Lim_actor_model_modules_typing_OwnTypingActor_2_classLit = createForClass('im.actor.model.modules.typing', 'OwnTypingActor', 591, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function OwnTypingActor$Typing(peer){
  this.peer = peer;
}

defineClass(234, 1, {234:1}, OwnTypingActor$Typing);
var Lim_actor_model_modules_typing_OwnTypingActor$Typing_2_classLit = createForClass('im.actor.model.modules.typing', 'OwnTypingActor/Typing', 234, Ljava_lang_Object_2_classLit);
function $groupTyping(this$static, gid, uid, type_0){
  var i_0, ids, ids2, set_0, src_0;
  if (type_0 != ($clinit_TypingType() , TEXT)) {
    return;
  }
  if (!dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(gid)), 21)) {
    return;
  }
  if (!dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11)) {
    return;
  }
  if ($containsKey(this$static.groupTypings, valueOf(gid))) {
    src_0 = dynamicCast($get_2(this$static.groupTypings, valueOf(gid)), 34);
    if (!$contains_0(src_0, valueOf(uid))) {
      $add_1(src_0, valueOf(uid));
      ids = dynamicCast($toArray(src_0, initDim(Ljava_lang_Integer_2_classLit, $intern_22, 33, src_0.map_0.size_0, 0, 1)), 91);
      ids2 = initDim(I_classLit, $intern_4, 0, ids.length, 7, 1);
      for (i_0 = 0; i_0 < ids.length; i_0++) {
        ids2[i_0] = ids[i_0].value_0;
      }
      $change($getGroupTyping(this$static.modules.typing, gid).active, ids2);
    }
  }
   else {
    set_0 = new HashSet;
    $add_1(set_0, valueOf(uid));
    $put_1(this$static.groupTypings, valueOf(gid), set_0);
    $change($getGroupTyping(this$static.modules.typing, gid).active, initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [uid]));
  }
  $sendOnce_0(this$static.context.actorScope.actorRef, new TypingActor$StopGroupTyping(gid, uid), {l:3000, m:0, h:0});
}

function $privateTyping(this$static, uid, type_0){
  if (type_0 != ($clinit_TypingType() , TEXT)) {
    return;
  }
  if (!dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11)) {
    return;
  }
  if (!$contains_0(this$static.typings, valueOf(uid))) {
    $add_1(this$static.typings, valueOf(uid));
    $change($getTyping(this$static.modules.typing, uid).userTyping, ($clinit_Boolean() , $clinit_Boolean() , TRUE));
  }
  $sendOnce_0(this$static.context.actorScope.actorRef, new TypingActor$StopTyping(uid), {l:3000, m:0, h:0});
}

function $stopGroupTyping(this$static, gid, uid){
  var i_0, ids, ids2, set_0;
  if (!$containsKey(this$static.groupTypings, valueOf(gid))) {
    return;
  }
  set_0 = dynamicCast($get_2(this$static.groupTypings, valueOf(gid)), 34);
  if ($contains_0(set_0, valueOf(uid))) {
    $remove_5(set_0, valueOf(uid));
    ids = dynamicCast($toArray(set_0, initDim(Ljava_lang_Integer_2_classLit, $intern_22, 33, set_0.map_0.size_0, 0, 1)), 91);
    ids2 = initDim(I_classLit, $intern_4, 0, ids.length, 7, 1);
    for (i_0 = 0; i_0 < ids.length; i_0++) {
      ids2[i_0] = ids[i_0].value_0;
    }
    $change($getGroupTyping(this$static.modules.typing, gid).active, ids2);
  }
}

function $stopPrivateTyping(this$static, uid){
  if ($contains_0(this$static.typings, valueOf(uid))) {
    $remove_5(this$static.typings, valueOf(uid));
    $change($getTyping(this$static.modules.typing, uid).userTyping, ($clinit_Boolean() , $clinit_Boolean() , FALSE));
  }
}

function TypingActor(messenger){
  ModuleActor.call(this, messenger);
  this.typings = new HashSet;
  this.groupTypings = new HashMap;
}

defineClass(689, 27, {}, TypingActor);
_.onReceive = function onReceive_25(message){
  var typing;
  if (instanceOf(message, 181)) {
    typing = dynamicCast(message, 181);
    $privateTyping(this, typing.uid, typing.type_0);
  }
   else if (instanceOf(message, 182)) {
    typing = dynamicCast(message, 182);
    $groupTyping(this, typing.gid, typing.uid, typing.type_0);
  }
   else if (instanceOf(message, 125)) {
    typing = dynamicCast(message, 125);
    $stopPrivateTyping(this, typing.uid);
  }
   else if (instanceOf(message, 126)) {
    typing = dynamicCast(message, 126);
    $stopGroupTyping(this, typing.gid, typing.uid);
  }
   else {
    !!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message));
    $reply(this, new DeadLetter(message));
  }
}
;
var Lim_actor_model_modules_typing_TypingActor_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor', 689, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function TypingActor$1(val$messenger){
  this.val$messenger1 = val$messenger;
}

defineClass(457, 1, {}, TypingActor$1);
_.create_0 = function create_31(){
  return new TypingActor(this.val$messenger1);
}
;
var Lim_actor_model_modules_typing_TypingActor$1_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor/1', 457, Ljava_lang_Object_2_classLit);
function TypingActor$2(){
}

defineClass(458, 1, {}, TypingActor$2);
_.createMailbox = function createMailbox_0(queue){
  return new TypingActor$2$1(queue);
}
;
var Lim_actor_model_modules_typing_TypingActor$2_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor/2', 458, Ljava_lang_Object_2_classLit);
function TypingActor$2$1($anonymous0){
  Mailbox.call(this, $anonymous0);
}

defineClass(690, 348, {}, TypingActor$2$1);
_.isEqualEnvelope = function isEqualEnvelope_1(a, b){
  if (equals_Ljava_lang_Object__Z__devirtual$(a.message_0, b.message_0)) {
    return true;
  }
  return getClass__Ljava_lang_Class___devirtual$(a.message_0) == getClass__Ljava_lang_Class___devirtual$(b.message_0);
}
;
var Lim_actor_model_modules_typing_TypingActor$2$1_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor/2/1', 690, Lim_actor_model_droidkit_actors_mailbox_Mailbox_2_classLit);
function TypingActor$GroupTyping(gid, uid, type_0){
  this.gid = gid;
  this.uid = uid;
  this.type_0 = type_0;
}

defineClass(182, 1, {182:1}, TypingActor$GroupTyping);
_.equals$ = function equals_16(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_typing_TypingActor$GroupTyping_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 182);
  if (this.gid != that.gid)
    return false;
  if (this.type_0 != that.type_0)
    return false;
  if (this.uid != that.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_12(){
  var result;
  result = this.gid;
  result = 31 * result + this.uid;
  result = 31 * result + this.type_0.value_0;
  return result;
}
;
_.gid = 0;
_.uid = 0;
var Lim_actor_model_modules_typing_TypingActor$GroupTyping_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor/GroupTyping', 182, Ljava_lang_Object_2_classLit);
function TypingActor$PrivateTyping(uid, type_0){
  this.uid = uid;
  this.type_0 = type_0;
}

defineClass(181, 1, {181:1}, TypingActor$PrivateTyping);
_.equals$ = function equals_17(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_typing_TypingActor$PrivateTyping_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 181);
  if (this.type_0 != that.type_0)
    return false;
  if (this.uid != that.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_13(){
  var result;
  result = this.uid;
  result = 31 * result + this.type_0.value_0;
  return result;
}
;
_.uid = 0;
var Lim_actor_model_modules_typing_TypingActor$PrivateTyping_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor/PrivateTyping', 181, Ljava_lang_Object_2_classLit);
function TypingActor$StopGroupTyping(gid, uid){
  this.gid = gid;
  this.uid = uid;
}

defineClass(126, 1, {126:1}, TypingActor$StopGroupTyping);
_.equals$ = function equals_18(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_typing_TypingActor$StopGroupTyping_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 126);
  if (this.gid != that.gid)
    return false;
  if (this.uid != that.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_14(){
  var result;
  result = this.gid;
  result = 31 * result + this.uid;
  return result;
}
;
_.gid = 0;
_.uid = 0;
var Lim_actor_model_modules_typing_TypingActor$StopGroupTyping_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor/StopGroupTyping', 126, Ljava_lang_Object_2_classLit);
function TypingActor$StopTyping(uid){
  this.uid = uid;
}

defineClass(125, 1, {125:1}, TypingActor$StopTyping);
_.equals$ = function equals_19(o){
  var that;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_modules_typing_TypingActor$StopTyping_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  that = dynamicCast(o, 125);
  if (this.uid != that.uid)
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_15(){
  return this.uid;
}
;
_.uid = 0;
var Lim_actor_model_modules_typing_TypingActor$StopTyping_2_classLit = createForClass('im.actor.model.modules.typing', 'TypingActor/StopTyping', 125, Ljava_lang_Object_2_classLit);
function $onContactsAdded_0(this$static, uid){
  $send_1(this$static.contactsSyncActor, new ContactsSyncActor$ContactsAdded(uid));
}

function $onContactsRemoved_0(this$static, uid){
  $send_1(this$static.contactsSyncActor, new ContactsSyncActor$ContactsRemoved(uid));
}

function ContactsProcessor(modules){
  BaseModule.call(this, modules);
  this.contactsSyncActor = this.modules.contacts.contactSyncActor;
}

defineClass(734, 25, {}, ContactsProcessor);
var Lim_actor_model_modules_updates_ContactsProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'ContactsProcessor', 734, Lim_actor_model_modules_BaseModule_2_classLit);
function $applyGroups(this$static, updated, forced){
  var batch, group, group$iterator, saved, upd;
  batch = new ArrayList;
  for (group$iterator = new AbstractList$IteratorImpl(updated); group$iterator.i < group$iterator.this$01.size_1();) {
    group = (checkCriticalElement(group$iterator.i < group$iterator.this$01.size_1()) , dynamicCast(group$iterator.this$01.get_1(group$iterator.last = group$iterator.i++), 215));
    saved = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(group.id_0)), 21);
    if (!saved) {
      $add_0(batch, new Group_1(group.id_0, group.accessHash, group.title_0, convert_3(group.avatar), convert_13(group.members, group.creatorUid), group.creatorUid, group.isMember));
    }
     else if (forced) {
      upd = new Group_1(group.id_0, group.accessHash, group.title_0, convert_3(group.avatar), convert_13(group.members, group.creatorUid), group.creatorUid, group.isMember);
      setCheck(batch.array, batch.array.length, upd);
      (!equalsE(upd.avatar, saved.avatar) || !$equals_3(upd.title_0, saved.title_0)) && $send(this$static.modules.messages.dialogsActor, new DialogsActor$GroupChanged(upd));
    }
  }
  batch.array.length > 0 && $addOrUpdateItems_1(this$static.modules.groups.groups, batch);
}

function $hasGroups(this$static, gids){
  var uid, uid$iterator;
  for (uid$iterator = $iterator(new AbstractMap$1(gids.map_0)); $hasNext(uid$iterator.val$outerIter2);) {
    uid = dynamicCast($next_1(uid$iterator), 33);
    if (!$getValue_2(this$static.modules.groups.groups, fromInt(uid.value_0))) {
      return false;
    }
  }
  return true;
}

function $onAvatarChanged(this$static, groupId, rid, uid, avatar, date, isSilent){
  var group, message, upd;
  group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(groupId)), 21);
  if (group) {
    if (!equalsE(group.avatar, avatar)) {
      upd = new Group_1(group.groupId, group.accessHash, group.title_0, avatar, group.members, group.adminId, group.isMember);
      $addOrUpdateItem_2(this$static.modules.groups.groups, upd);
      $send(this$static.modules.messages.dialogsActor, new DialogsActor$GroupChanged(upd));
    }
    if (!isSilent) {
      message = new Message_0(rid, date, date, uid, uid == this$static.modules.auth.myUid?($clinit_MessageState_0() , SENT_0):($clinit_MessageState_0() , UNKNOWN_0), new ServiceGroupAvatarChanged_0(avatar));
      $send_1($conversationActor(this$static, new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId)), message);
    }
  }
}

function $onGroupInvite(this$static, groupId, rid, inviterId, date, isSilent){
  var group, message;
  group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(groupId)), 21);
  if (group) {
    $addOrUpdateItem_2(this$static.modules.groups.groups, $addMember(new Group_1(group.groupId, group.accessHash, group.title_0, group.avatar, group.members, group.adminId, true), this$static.modules.auth.myUid, inviterId, date, inviterId == this$static.modules.auth.myUid));
    if (!isSilent) {
      if (inviterId == this$static.modules.auth.myUid) {
        message = new Message_0(rid, date, date, inviterId, ($clinit_MessageState_0() , UNKNOWN_0), new ServiceGroupCreated_0(group.title_0));
        $send_1($conversationActor(this$static, new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId)), message);
      }
       else {
        message = new Message_0(rid, date, date, inviterId, ($clinit_MessageState_0() , SENT_0), new ServiceGroupUserAdded_0(this$static.modules.auth.myUid));
        $send_1($conversationActor(this$static, new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId)), message);
      }
    }
  }
}

function $onMembersUpdated(this$static, groupId, members){
  var group, m, m$iterator;
  group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(groupId)), 21);
  if (group) {
    group = new Group_1(group.groupId, group.accessHash, group.title_0, group.avatar, new ArrayList, group.adminId, group.isMember);
    for (m$iterator = new AbstractList$IteratorImpl(members); m$iterator.i < m$iterator.this$01.size_1();) {
      m = (checkCriticalElement(m$iterator.i < m$iterator.this$01.size_1()) , dynamicCast(m$iterator.this$01.get_1(m$iterator.last = m$iterator.i++), 216));
      group = $addMember(group, m.uid, m.inviterUid, m.date, m.uid == group.adminId);
    }
    $addOrUpdateItem_2(this$static.modules.groups.groups, group);
  }
}

function $onTitleChanged(this$static, groupId, rid, uid, title_0, date, isSilent){
  var group, message, upd;
  group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(groupId)), 21);
  if (group) {
    if (!$equals_3(group.title_0, title_0)) {
      upd = new Group_1(group.groupId, group.accessHash, title_0, group.avatar, group.members, group.adminId, group.isMember);
      $addOrUpdateItem_2(this$static.modules.groups.groups, upd);
      $send(this$static.modules.messages.dialogsActor, new DialogsActor$GroupChanged(upd));
    }
    if (!isSilent) {
      message = new Message_0(rid, date, date, uid, uid == this$static.modules.auth.myUid?($clinit_MessageState_0() , SENT_0):($clinit_MessageState_0() , UNKNOWN_0), new ServiceGroupTitleChanged_0(title_0));
      $send_1($conversationActor(this$static, new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId)), message);
    }
  }
}

function $onUserAdded(this$static, groupId, rid, uid, adder, date, isSilent){
  var group, message;
  group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(groupId)), 21);
  if (group) {
    $addOrUpdateItem_2(this$static.modules.groups.groups, $addMember(group, uid, adder, date, false));
    if (!isSilent) {
      message = new Message_0(rid, date, date, adder, adder == this$static.modules.auth.myUid?($clinit_MessageState_0() , SENT_0):($clinit_MessageState_0() , UNKNOWN_0), new ServiceGroupUserAdded_0(uid));
      $send_1($conversationActor(this$static, new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId)), message);
    }
  }
}

function $onUserKicked(this$static, groupId, rid, uid, kicker, date, isSilent){
  var group, message;
  group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(groupId)), 21);
  if (group) {
    uid == this$static.modules.auth.myUid?$addOrUpdateItem_2(this$static.modules.groups.groups, $changeMember(new Group_1(group.groupId, group.accessHash, group.title_0, group.avatar, new ArrayList, group.adminId, group.isMember))):$addOrUpdateItem_2(this$static.modules.groups.groups, $removeMember(group, uid));
    if (!isSilent) {
      message = new Message_0(rid, date, date, kicker, kicker == this$static.modules.auth.myUid?($clinit_MessageState_0() , SENT_0):($clinit_MessageState_0() , UNKNOWN_0), new ServiceGroupUserKicked_0(uid));
      $send_1($conversationActor(this$static, new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId)), message);
    }
  }
}

function $onUserLeave(this$static, groupId, rid, uid, date, isSilent){
  var group, message;
  group = dynamicCast($getValue_2(this$static.modules.groups.groups, fromInt(groupId)), 21);
  if (group) {
    uid == this$static.modules.auth.myUid?$addOrUpdateItem_2(this$static.modules.groups.groups, $changeMember(new Group_1(group.groupId, group.accessHash, group.title_0, group.avatar, new ArrayList, group.adminId, group.isMember))):$addOrUpdateItem_2(this$static.modules.groups.groups, $removeMember(group, uid));
    if (!isSilent) {
      message = new Message_0(rid, date, date, uid, uid == this$static.modules.auth.myUid?($clinit_MessageState_0() , SENT_0):($clinit_MessageState_0() , UNKNOWN_0), new ServiceGroupUserLeave);
      $send_1($conversationActor(this$static, new Peer_2(($clinit_PeerType_0() , GROUP_0), group.groupId)), message);
    }
  }
}

function GroupsProcessor(modules){
  BaseModule.call(this, modules);
}

defineClass(731, 25, {}, GroupsProcessor);
var Lim_actor_model_modules_updates_GroupsProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'GroupsProcessor', 731, Lim_actor_model_modules_BaseModule_2_classLit);
function $onChatClear_0(this$static, _peer){
  var peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$ClearConversation);
}

function $onChatDelete(this$static, _peer){
  var peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$DeleteConversation);
}

function $onDialogsLoaded(this$static, dialogsResponse){
  var dialog, dialog$iterator, dialogs, maxLoadedDate, msgContent, peer;
  dialogs = new ArrayList;
  maxLoadedDate = {l:$intern_7, m:$intern_7, h:524287};
  for (dialog$iterator = new AbstractList$IteratorImpl(dialogsResponse.dialogs); dialog$iterator.i < dialog$iterator.this$01.size_1();) {
    dialog = (checkCriticalElement(dialog$iterator.i < dialog$iterator.this$01.size_1()) , dynamicCast(dialog$iterator.this$01.get_1(dialog$iterator.last = dialog$iterator.i++), 428));
    maxLoadedDate = min_1(dialog.sortDate, maxLoadedDate);
    peer = convert_9(dialog.peer);
    msgContent = convert_7(dialog.message_0);
    if (!msgContent) {
      continue;
    }
    $add_0(dialogs, new DialogHistory(peer, dialog.unreadCount, dialog.sortDate, dialog.rid, dialog.date, dialog.senderUid, msgContent, convert_8(dialog.state)));
  }
  dialogs.array.length > 0?$send_1(this$static.modules.messages.dialogsActor, new DialogsActor$HistoryLoaded(dialogs)):$send_1(this$static.modules.appStateModule.listStatesActor, new ListsStatesActor$OnDialogsLoaded);
  $send_1(this$static.modules.messages.dialogsHistoryActor, new DialogsHistoryActor$LoadedMore(dialogsResponse.dialogs.array.length, maxLoadedDate));
}

function $onMessage_0(this$static, _peer, senderUid, date, rid, content_0){
  var msgContent, peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  msgContent = convert_7(content_0);
  if (!msgContent) {
    return;
  }
  $onMessage_1(this$static, peer, senderUid, date, rid, msgContent);
}

function $onMessage_1(this$static, peer, senderUid, date, rid, msgContent){
  var isOut, message;
  isOut = this$static.modules.auth.myUid == senderUid;
  message = new Message_0(rid, date, date, senderUid, isOut?($clinit_MessageState_0() , SENT_0):($clinit_MessageState_0() , UNKNOWN_0), msgContent);
  $send_1($getConversationActor(this$static.modules.messages, peer), message);
  if (isOut) {
    $send_1(this$static.modules.messages.ownReadActor, new OwnReadActor$MessageRead(peer, date));
  }
   else {
    $send_1(this$static.modules.messages.ownReadActor, new OwnReadActor$NewMessage(peer, rid, date));
    $onInMessage(this$static.modules.notifications, peer, senderUid, date, fromContent(message.content_0));
    $send_1(this$static.modules.messages.plainReceiverActor, new CursorReceiverActor$MarkReceived(peer, date));
  }
}

function $onMessageDelete_0(this$static, _peer, rids){
  var peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$MessagesDeleted(rids));
  $send_1(this$static.modules.messages.ownReadActor, new OwnReadActor$MessageDeleted(peer, rids));
}

function $onMessageRead_0(this$static, _peer, startDate){
  var peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$MessageRead(startDate));
}

function $onMessageReadByMe_0(this$static, _peer, startDate){
  var peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  $send_1(this$static.modules.messages.ownReadActor, new OwnReadActor$MessageReadByMe(peer, startDate));
}

function $onMessageReceived(this$static, _peer, startDate){
  var peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$MessageReceived(startDate));
}

function $onMessageSent_1(this$static, _peer, rid, date){
  var peer;
  peer = new Peer_2(convert_10(_peer.type_0), _peer.id_0);
  $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$MessageSent(rid, date));
  $send_1(this$static.modules.messages.sendMessageActor, new SenderActor$MessageSent(peer, rid));
  $send_1(this$static.modules.messages.ownReadActor, new OwnReadActor$MessageRead(peer, date));
}

function $onMessagesLoaded(this$static, peer, historyResponse){
  var content_0, historyMessage, historyMessage$iterator, maxLoadedDate, messages, state;
  messages = new ArrayList;
  maxLoadedDate = {l:$intern_7, m:$intern_7, h:524287};
  for (historyMessage$iterator = new AbstractList$IteratorImpl(historyResponse.history_0); historyMessage$iterator.i < historyMessage$iterator.this$01.size_1();) {
    historyMessage = (checkCriticalElement(historyMessage$iterator.i < historyMessage$iterator.this$01.size_1()) , dynamicCast(historyMessage$iterator.this$01.get_1(historyMessage$iterator.last = historyMessage$iterator.i++), 429));
    maxLoadedDate = min_1(historyMessage.date, maxLoadedDate);
    content_0 = convert_7(historyMessage.message_0);
    if (!content_0) {
      continue;
    }
    state = convert_8(historyMessage.state);
    $add_0(messages, new Message_0(historyMessage.rid, historyMessage.date, historyMessage.date, historyMessage.senderUid, state, content_0));
  }
  messages.array.length > 0 && $send_1($getConversationActor(this$static.modules.messages, peer), new ConversationActor$HistoryLoaded(messages));
  $send_1($getConversationHistoryActor(this$static.modules.messages, peer), new ConversationHistoryActor$LoadedMore(historyResponse.history_0.array.length, maxLoadedDate));
}

function $onUserRegistered(this$static, uid, date){
  var message, rid;
  rid = ($clinit_RandomUtils() , $nextLong(RANDOM));
  message = new Message_0(rid, date, date, uid, ($clinit_MessageState_0() , UNKNOWN_0), new ServiceUserRegistered);
  $send_1(this$static.modules.messages.ownReadActor, new OwnReadActor$NewMessage(new Peer_2(($clinit_PeerType_0() , PRIVATE_0), uid), rid, date));
  $send_1($conversationActor(this$static, new Peer_2(PRIVATE_0, uid)), message);
}

function MessagesProcessor(messenger){
  BaseModule.call(this, messenger);
}

defineClass(730, 25, {}, MessagesProcessor);
var Lim_actor_model_modules_updates_MessagesProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'MessagesProcessor', 730, Lim_actor_model_modules_BaseModule_2_classLit);
function $onGroupOnline_0(this$static, gid, count){
  $sendOnce(this$static.presenceActor, new PresenceActor$GroupOnline(gid, count));
}

function $onUserLastSeen_0(this$static, uid, date){
  $sendOnce(this$static.presenceActor, new PresenceActor$UserLastSeen(uid, date));
}

function $onUserOffline_0(this$static, uid){
  $sendOnce(this$static.presenceActor, new PresenceActor$UserOffline(uid));
}

function $onUserOnline_0(this$static, uid){
  $sendOnce(this$static.presenceActor, new PresenceActor$UserOnline(uid));
}

function PresenceProcessor(modules){
  BaseModule.call(this, modules);
  this.presenceActor = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new PresenceActor$1(modules), new PresenceActor$2), 'actor/presence/users');
}

defineClass(732, 25, {}, PresenceProcessor);
var Lim_actor_model_modules_updates_PresenceProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'PresenceProcessor', 732, Lim_actor_model_modules_BaseModule_2_classLit);
function $checkFuture(this$static){
  var i_0;
  for (i_0 = this$static.seq + 1;; i_0++) {
    if ($containsKey(this$static.further, valueOf(i_0))) {
      $onReceive_0(this$static, $remove_0(this$static.further, valueOf(i_0)));
    }
     else {
      break;
    }
  }
  $reset(this$static.further);
}

function $checkRunnables(this$static){
  var e, e$array, e$index, e$max;
  if (this$static.pendingRunnables.array.length > 0) {
    for (e$array = dynamicCast($toArray_0(this$static.pendingRunnables, initDim(Lim_actor_model_modules_updates_internal_ExecuteAfter_2_classLit, {812:1, 3:1, 6:1}, 903, this$static.pendingRunnables.array.length, 0, 1)), 812) , e$index = 0 , e$max = e$array.length; e$index < e$max; ++e$index) {
      e = e$array[e$index];
      if (null.nullField <= this$static.seq) {
        null.nullMethod();
        $remove_4(this$static.pendingRunnables, e);
      }
    }
  }
}

function $invalidate(this$static){
  if (!this$static.isValidated) {
    return;
  }
  this$static.isValidated = false;
  if (this$static.seq < 0) {
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Updates' + ':' + 'Loading fresh state...');
    $request_0(this$static, new RequestGetState, new SequenceActor$1(this$static));
  }
   else {
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Updates' + ':' + 'Loading difference...');
    $request_0(this$static, new RequestGetDifference_0(this$static.seq, this$static.state), new SequenceActor$2(this$static));
  }
}

function $onReceive_0(this$static, message){
  instanceOf(message, 274) || instanceOf(message, 288) || instanceOf(message, 131)?$invalidate(this$static):instanceOf(message, 56)?$onUpdateReceived_1(this$static, message):instanceOf(message, 57)?$onUpdateReceived_1(this$static, message):instanceOf(message, 136)?$onUpdateReceived_1(this$static, message):instanceOf(message, 85)?$onUpdateReceived_1(this$static, message):(!!this$static.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this$static, new DeadLetter(message)));
}

function $onUpdateReceived_1(this$static, u){
  var body_0, e, fatSeqUpdate, seq, state, type_0, update, w;
  if (instanceOf(u, 56)) {
    seq = dynamicCast(u, 56).seq;
    state = dynamicCast(u, 56).state;
    type_0 = dynamicCast(u, 56).updateHeader;
    body_0 = dynamicCast(u, 56).update;
  }
   else if (instanceOf(u, 57)) {
    seq = dynamicCast(u, 57).seq;
    state = dynamicCast(u, 57).state;
    type_0 = dynamicCast(u, 57).updateHeader;
    body_0 = dynamicCast(u, 57).update;
  }
   else if (instanceOf(u, 136)) {
    w = dynamicCast(u, 136);
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Updates' + ':' + 'Received weak update');
    try {
      $processUpdate(this$static.processor, $read_0(w.updateHeader, w.update));
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
        !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Updates' + ':' + 'Unable to parse update: ignoring');
      }
       else 
        throw unwrap($e0);
    }
    return;
  }
   else if (instanceOf(u, 85)) {
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Updates' + ':' + 'Received internal update');
    $processInternalUpdate(this$static.processor, dynamicCast(u, 85));
    return;
  }
   else {
    return;
  }
  if (seq <= this$static.seq) {
    d_0('Updates', 'Ignored SeqUpdate {seq:' + seq + ', currentSeq: ' + this$static.seq + '}');
    return;
  }
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Updates' + ':' + ('SeqUpdate {seq:' + seq + '}'));
  if (!this$static.isValidated) {
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Updates' + ':' + 'Caching in further map');
    $put_1(this$static.further, valueOf(seq), u);
    return;
  }
  if (!(this$static.seq <= 0 || seq == this$static.seq + 1)) {
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Updates' + ':' + 'Out of sequence: starting timer for invalidation');
    $put_1(this$static.further, valueOf(seq), u);
    $sendOnce_0(this$static.context.actorScope.actorRef, new SequenceActor$ForceInvalidate, {l:2000, m:0, h:0});
    return;
  }
  try {
    update = $read_0(type_0, body_0);
  }
   catch ($e1) {
    $e1 = wrap($e1);
    if (instanceOf($e1, 5)) {
      e = $e1;
      !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Updates' + ':' + 'Unable to parse update: ignoring');
      $printStackTrace(e, ($clinit_System() , err));
      return;
    }
     else 
      throw unwrap($e1);
  }
  if ($isCausesInvalidation(this$static.processor, update)) {
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Updates' + ':' + 'Message causes invalidation');
    $invalidate(this$static);
    return;
  }
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Updates' + ':' + ('Processing update: ' + update));
  if (instanceOf(u, 57)) {
    fatSeqUpdate = dynamicCast(u, 57);
    $applyRelated(this$static.processor, fatSeqUpdate.users, fatSeqUpdate.groups, false);
  }
  $processUpdate(this$static.processor, update);
  if (instanceOf(u, 57)) {
    fatSeqUpdate = dynamicCast(u, 57);
    $applyRelated(this$static.processor, fatSeqUpdate.users, fatSeqUpdate.groups, true);
  }
  this$static.seq = seq;
  this$static.state = state;
  $putInt(this$static.modules.preferences, 'updates_seq', seq);
  $putBytes(this$static.modules.preferences, 'updates_state', state);
  $checkRunnables(this$static);
  $checkFuture(this$static);
  $sendOnce_0(this$static.context.actorScope.actorRef, new SequenceActor$ForceInvalidate, {l:2513920, m:20, h:0});
}

function SequenceActor(modules){
  ModuleActor.call(this, modules);
  this.further = new HashMap;
  this.pendingRunnables = new ArrayList;
}

defineClass(668, 27, {}, SequenceActor);
_.onReceive = function onReceive_26(message){
  $onReceive_0(this, message);
}
;
_.preStart = function preStart_15(){
  this.seq = $getInt(this.modules.preferences, 'updates_seq', -1);
  this.state = $getBytes_1(this.modules.preferences, 'updates_state');
  this.processor = new UpdateProcessor(this.modules);
  $send_1(this.context.actorScope.actorRef, new SequenceActor$Invalidate);
}
;
_.isValidated = true;
_.seq = 0;
var Lim_actor_model_modules_updates_SequenceActor_2_classLit = createForClass('im.actor.model.modules.updates', 'SequenceActor', 668, Lim_actor_model_modules_utils_ModuleActor_2_classLit);
function $onResult_21(this$static, response){
  if (this$static.this$01.isValidated) {
    return;
  }
  this$static.this$01.seq = response.seq;
  this$static.this$01.state = response.state;
  this$static.this$01.isValidated = true;
  $putInt(this$static.this$01.modules.preferences, 'updates_seq', this$static.this$01.seq);
  $putBytes(this$static.this$01.modules.preferences, 'updates_state', this$static.this$01.state);
  d_0('Updates', 'State loaded {seq=' + this$static.this$01.seq + '}');
  $checkRunnables(this$static.this$01);
  $checkFuture(this$static.this$01);
  $sendOnce_0(this$static.this$01.context.actorScope.actorRef, new SequenceActor$ForceInvalidate, {l:2513920, m:20, h:0});
}

function SequenceActor$1(this$0){
  this.this$01 = this$0;
}

defineClass(702, 1, {}, SequenceActor$1);
_.onError_0 = function onError_18(e){
  if (this.this$01.isValidated) {
    return;
  }
  this.this$01.isValidated = true;
  $invalidate(this.this$01);
}
;
_.onResult_0 = function onResult_18(response){
  $onResult_21(this, dynamicCast(response, 89));
}
;
var Lim_actor_model_modules_updates_SequenceActor$1_2_classLit = createForClass('im.actor.model.modules.updates', 'SequenceActor/1', 702, Ljava_lang_Object_2_classLit);
function $onResult_22(this$static, response){
  var e, u, u$iterator, update;
  if (this$static.this$01.isValidated) {
    return;
  }
  d_0('Updates', 'Difference loaded {seq=' + response.seq + '}');
  $applyRelated(this$static.this$01.processor, response.users, response.groups, false);
  for (u$iterator = new AbstractList$IteratorImpl(response.updates); u$iterator.i < u$iterator.this$01.size_1();) {
    u = (checkCriticalElement(u$iterator.i < u$iterator.this$01.size_1()) , dynamicCast(u$iterator.this$01.get_1(u$iterator.last = u$iterator.i++), 430));
    try {
      update = $read_0(u.updateHeader, u.update);
      $processUpdate(this$static.this$01.processor, update);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        $printStackTrace(e, ($clinit_System() , err));
        d_0('Updates', 'Broken update #' + u.updateHeader + ': ignoring');
      }
       else 
        throw unwrap($e0);
    }
  }
  $applyRelated(this$static.this$01.processor, response.users, response.groups, true);
  this$static.this$01.seq = response.seq;
  this$static.this$01.state = response.state;
  this$static.this$01.isValidated = true;
  $putInt(this$static.this$01.modules.preferences, 'updates_seq', this$static.this$01.seq);
  $putBytes(this$static.this$01.modules.preferences, 'updates_state', this$static.this$01.state);
  $checkRunnables(this$static.this$01);
  $checkFuture(this$static.this$01);
  $sendOnce_0(this$static.this$01.context.actorScope.actorRef, new SequenceActor$ForceInvalidate, {l:2513920, m:20, h:0});
  response.needMore && $invalidate(this$static.this$01);
}

function SequenceActor$2(this$0){
  this.this$01 = this$0;
}

defineClass(703, 1, {}, SequenceActor$2);
_.onError_0 = function onError_19(e){
  if (this.this$01.isValidated) {
    return;
  }
  this.this$01.isValidated = true;
  $invalidate(this.this$01);
}
;
_.onResult_0 = function onResult_19(response){
  $onResult_22(this, dynamicCast(response, 322));
}
;
var Lim_actor_model_modules_updates_SequenceActor$2_2_classLit = createForClass('im.actor.model.modules.updates', 'SequenceActor/2', 703, Ljava_lang_Object_2_classLit);
function SequenceActor$ForceInvalidate(){
}

defineClass(131, 1, {131:1}, SequenceActor$ForceInvalidate);
var Lim_actor_model_modules_updates_SequenceActor$ForceInvalidate_2_classLit = createForClass('im.actor.model.modules.updates', 'SequenceActor/ForceInvalidate', 131, Ljava_lang_Object_2_classLit);
function SequenceActor$Invalidate(){
}

defineClass(274, 1, {274:1}, SequenceActor$Invalidate);
var Lim_actor_model_modules_updates_SequenceActor$Invalidate_2_classLit = createForClass('im.actor.model.modules.updates', 'SequenceActor/Invalidate', 274, Ljava_lang_Object_2_classLit);
function $onSettingsChanged(this$static, key, value_0){
  $putString(this$static.modules.preferences, key, value_0);
}

function SettingsProcessor(modules){
  BaseModule.call(this, modules);
}

defineClass(728, 25, {}, SettingsProcessor);
var Lim_actor_model_modules_updates_SettingsProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'SettingsProcessor', 728, Lim_actor_model_modules_BaseModule_2_classLit);
function $onMessage_2(this$static, peer, uid){
  peer.type_0 == ($clinit_PeerType() , PRIVATE)?$sendOnce(this$static.typingActor, new TypingActor$StopTyping(uid)):peer.type_0 == GROUP && $sendOnce(this$static.typingActor, new TypingActor$StopGroupTyping(peer.id_0, uid));
}

function $onTyping_2(this$static, peer, uid, type_0){
  peer.type_0 == ($clinit_PeerType() , PRIVATE)?$sendOnce(this$static.typingActor, new TypingActor$PrivateTyping(uid, type_0)):peer.type_0 == GROUP && $sendOnce(this$static.typingActor, new TypingActor$GroupTyping(peer.id_0, uid, type_0));
}

function TypingProcessor(modules){
  BaseModule.call(this, modules);
  this.typingActor = $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new TypingActor$1(modules), new TypingActor$2), 'actor/typing');
}

defineClass(733, 25, {}, TypingProcessor);
var Lim_actor_model_modules_updates_TypingProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'TypingProcessor', 733, Lim_actor_model_modules_BaseModule_2_classLit);
function $applyRelated(this$static, users, groups, force){
  $applyUsers(this$static.usersProcessor, users, force);
  $applyGroups(this$static.groupsProcessor, groups, force);
}

function $isCausesInvalidation(this$static, update){
  var added, contactRegistered, groupInvite, groups, kick, leave, localNameChanged, updateMessage, users;
  users = new HashSet;
  groups = new HashSet;
  if (instanceOf(update, 109)) {
    updateMessage = dynamicCast(update, 109);
    $add_1(users, valueOf(updateMessage.senderUid));
    updateMessage.peer.type_0 == ($clinit_PeerType() , GROUP) && $add_1(groups, valueOf(updateMessage.peer.id_0));
    updateMessage.peer.type_0 == PRIVATE && $add_1(users, valueOf(updateMessage.peer.id_0));
  }
   else if (instanceOf(update, 107)) {
    contactRegistered = dynamicCast(update, 107);
    $add_1(users, valueOf(contactRegistered.uid));
  }
   else if (instanceOf(update, 110)) {
    groupInvite = dynamicCast(update, 110);
    $add_1(users, valueOf(groupInvite.inviteUid));
    $add_1(groups, valueOf(groupInvite.groupId));
  }
   else if (instanceOf(update, 111)) {
    added = dynamicCast(update, 111);
    $add_1(users, valueOf(added.inviterUid));
    $add_1(users, valueOf(added.uid));
    $add_1(groups, valueOf(added.groupId));
  }
   else if (instanceOf(update, 113)) {
    kick = dynamicCast(update, 113);
    $add_1(users, valueOf(kick.kickerUid));
    $add_1(users, valueOf(kick.uid));
    $add_1(groups, valueOf(kick.groupId));
  }
   else if (instanceOf(update, 112)) {
    leave = dynamicCast(update, 112);
    $add_1(users, valueOf(leave.uid));
    $add_1(groups, valueOf(leave.groupId));
  }
   else if (instanceOf(update, 90)) {
    $addAll(users, dynamicCast(update, 90).uids);
  }
   else if (instanceOf(update, 108)) {
    $addAll(users, dynamicCast(update, 108).uids);
  }
   else if (instanceOf(update, 106)) {
    localNameChanged = dynamicCast(update, 106);
    $add_1(users, valueOf(localNameChanged.uid));
  }
  if (!$hasUsers(this$static.usersProcessor, users)) {
    return true;
  }
  if (!$hasGroups(this$static.groupsProcessor, groups)) {
    return true;
  }
  return false;
}

function $processInternalUpdate(this$static, update){
  var contactsLoaded, dialogs, historyLoaded, users;
  if (instanceOf(update, 326)) {
    dialogs = dynamicCast(update, 326).dialogs;
    $applyRelated(this$static, dialogs.users, dialogs.groups, false);
    $onDialogsLoaded(this$static.messagesProcessor, dialogs);
  }
   else if (instanceOf(update, 324)) {
    historyLoaded = dynamicCast(update, 324);
    $applyRelated(this$static, historyLoaded.loadHistory.users, new ArrayList, false);
    $onMessagesLoaded(this$static.messagesProcessor, historyLoaded.peer, historyLoaded.loadHistory);
  }
   else if (instanceOf(update, 214)) {
    users = new ArrayList;
    $add_0(users, dynamicCast(update, 214).auth.user);
    $applyRelated(this$static, users, new ArrayList, true);
    $runOnUiThread(dynamicCast(update, 214).runnable);
  }
   else if (instanceOf(update, 325)) {
    contactsLoaded = dynamicCast(update, 325);
    $applyRelated(this$static, contactsLoaded.contacts.users, new ArrayList, false);
    $send_1(this$static.modules.contacts.contactSyncActor, new ContactsSyncActor$ContactsLoaded(contactsLoaded.contacts));
  }
}

function $processUpdate(this$static, update){
  var avatarChanged, chatClear, chatDelete, contactsAdded, contactsRemoved, groupInvite, groupOnline, i_0, lastSeen, leave, localNameChanged, message, messageDelete, messageRead, messageReadByMe, messageSent, offline, received, registered, res, titleChanged, typing, userAdded, userKick, userNameChanged, userOnline;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Updates' + ':' + (update + ''));
  if (instanceOf(update, 203)) {
    userNameChanged = dynamicCast(update, 203);
    $onUserNameChanged(this$static.usersProcessor, userNameChanged.uid, userNameChanged.name_0);
  }
   else if (instanceOf(update, 106)) {
    localNameChanged = dynamicCast(update, 106);
    $onUserLocalNameChanged(this$static.usersProcessor, localNameChanged.uid, localNameChanged.localName);
  }
   else if (instanceOf(update, 202)) {
    avatarChanged = dynamicCast(update, 202);
    $onUserAvatarChanged(this$static.usersProcessor, avatarChanged.uid, avatarChanged.avatar);
  }
   else if (instanceOf(update, 109)) {
    message = dynamicCast(update, 109);
    $onMessage_0(this$static.messagesProcessor, message.peer, message.senderUid, message.date, message.rid, message.message_0);
    $onMessage_2(this$static.typingProcessor, message.peer, message.senderUid);
  }
   else if (instanceOf(update, 205)) {
    messageRead = dynamicCast(update, 205);
    $onMessageRead_0(this$static.messagesProcessor, messageRead.peer, messageRead.startDate);
  }
   else if (instanceOf(update, 206)) {
    messageReadByMe = dynamicCast(update, 206);
    $onMessageReadByMe_0(this$static.messagesProcessor, messageReadByMe.peer, messageReadByMe.startDate);
  }
   else if (instanceOf(update, 204)) {
    received = dynamicCast(update, 204);
    $onMessageReceived(this$static.messagesProcessor, received.peer, received.startDate);
  }
   else if (instanceOf(update, 139)) {
    messageDelete = dynamicCast(update, 139);
    $onMessageDelete_0(this$static.messagesProcessor, messageDelete.peer, messageDelete.rids);
  }
   else if (instanceOf(update, 138)) {
    messageSent = dynamicCast(update, 138);
    $onMessageSent_1(this$static.messagesProcessor, messageSent.peer, messageSent.rid, messageSent.date);
  }
   else if (instanceOf(update, 140)) {
    chatClear = dynamicCast(update, 140);
    $onChatClear_0(this$static.messagesProcessor, chatClear.peer);
  }
   else if (instanceOf(update, 141)) {
    chatDelete = dynamicCast(update, 141);
    $onChatDelete(this$static.messagesProcessor, chatDelete.peer);
  }
   else if (instanceOf(update, 107)) {
    registered = dynamicCast(update, 107);
    registered.isSilent || $onUserRegistered(this$static.messagesProcessor, registered.uid, registered.date);
  }
   else if (instanceOf(update, 210)) {
    userOnline = dynamicCast(update, 210);
    $onUserOnline_0(this$static.presenceProcessor, userOnline.uid);
  }
   else if (instanceOf(update, 211)) {
    offline = dynamicCast(update, 211);
    $onUserOffline_0(this$static.presenceProcessor, offline.uid);
  }
   else if (instanceOf(update, 212)) {
    lastSeen = dynamicCast(update, 212);
    $onUserLastSeen_0(this$static.presenceProcessor, lastSeen.uid, lastSeen.date);
  }
   else if (instanceOf(update, 213)) {
    groupOnline = dynamicCast(update, 213);
    $onGroupOnline_0(this$static.presenceProcessor, groupOnline.groupId, groupOnline.count);
  }
   else if (instanceOf(update, 209)) {
    typing = dynamicCast(update, 209);
    $onTyping_2(this$static.typingProcessor, typing.peer, typing.uid, typing.typingType);
  }
   else if (instanceOf(update, 207)) {
    titleChanged = dynamicCast(update, 207);
    $onTitleChanged(this$static.groupsProcessor, titleChanged.groupId, titleChanged.rid, titleChanged.uid, titleChanged.title_0, titleChanged.date, false);
  }
   else if (instanceOf(update, 208)) {
    avatarChanged = dynamicCast(update, 208);
    $onAvatarChanged(this$static.groupsProcessor, avatarChanged.groupId, avatarChanged.rid, avatarChanged.uid, convert_3(avatarChanged.avatar), avatarChanged.date, false);
  }
   else if (instanceOf(update, 110)) {
    groupInvite = dynamicCast(update, 110);
    $onGroupInvite(this$static.groupsProcessor, groupInvite.groupId, groupInvite.rid, groupInvite.inviteUid, groupInvite.date, false);
  }
   else if (instanceOf(update, 112)) {
    leave = dynamicCast(update, 112);
    $onUserLeave(this$static.groupsProcessor, leave.groupId, leave.rid, leave.uid, leave.date, false);
  }
   else if (instanceOf(update, 113)) {
    userKick = dynamicCast(update, 113);
    $onUserKicked(this$static.groupsProcessor, userKick.groupId, userKick.rid, userKick.uid, userKick.kickerUid, userKick.date, false);
  }
   else if (instanceOf(update, 111)) {
    userAdded = dynamicCast(update, 111);
    $onUserAdded(this$static.groupsProcessor, userAdded.groupId, userAdded.rid, userAdded.uid, userAdded.inviterUid, userAdded.date, false);
  }
   else if (instanceOf(update, 90)) {
    contactsAdded = dynamicCast(update, 90);
    res = initDim(I_classLit, $intern_4, 0, contactsAdded.uids.array.length, 7, 1);
    for (i_0 = 0; i_0 < res.length; i_0++) {
      res[i_0] = dynamicCast($get_3(contactsAdded.uids, i_0), 33).value_0;
    }
    $onContactsAdded_0(this$static.contactsProcessor, res);
  }
   else if (instanceOf(update, 108)) {
    contactsRemoved = dynamicCast(update, 108);
    res = initDim(I_classLit, $intern_4, 0, contactsRemoved.uids.array.length, 7, 1);
    for (i_0 = 0; i_0 < res.length; i_0++) {
      res[i_0] = dynamicCast($get_3(contactsRemoved.uids, i_0), 33).value_0;
    }
    $onContactsRemoved_0(this$static.contactsProcessor, res);
  }
   else 
    instanceOf(update, 142)?$onMembersUpdated(this$static.groupsProcessor, dynamicCast(update, 142).groupId, dynamicCast(update, 142).members):instanceOf(update, 114) && $onSettingsChanged(this$static.settingsProcessor, dynamicCast(update, 114).key, dynamicCast(update, 114).value_0);
}

function UpdateProcessor(modules){
  BaseModule.call(this, modules);
  this.settingsProcessor = new SettingsProcessor(modules);
  this.usersProcessor = new UsersProcessor(modules);
  this.messagesProcessor = new MessagesProcessor(modules);
  this.groupsProcessor = new GroupsProcessor(modules);
  this.presenceProcessor = new PresenceProcessor(modules);
  this.typingProcessor = new TypingProcessor(modules);
  this.contactsProcessor = new ContactsProcessor(modules);
}

defineClass(724, 25, {}, UpdateProcessor);
var Lim_actor_model_modules_updates_UpdateProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'UpdateProcessor', 724, Lim_actor_model_modules_BaseModule_2_classLit);
function $applyUsers(this$static, updated, forced){
  var batch, saved, u, u$iterator, upd;
  batch = new ArrayList;
  for (u$iterator = new AbstractList$IteratorImpl(updated); u$iterator.i < u$iterator.this$01.size_1();) {
    u = (checkCriticalElement(u$iterator.i < u$iterator.this$01.size_1()) , dynamicCast(u$iterator.this$01.get_1(u$iterator.last = u$iterator.i++), 58));
    d_0('UsersProcessor', 'UserUpdated: ' + u.id_0);
    saved = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(u.id_0)), 11);
    if (!saved) {
      $add_0(batch, convert_12(u));
    }
     else if (forced) {
      upd = convert_12(u);
      setCheck(batch.array, batch.array.length, upd);
      (!$equals_3(upd.localName == null?upd.name_0:upd.localName, saved.localName == null?saved.name_0:saved.localName) || !equalsE(upd.avatar, saved.avatar)) && ($send(this$static.modules.messages.dialogsActor, new DialogsActor$UserChanged(upd)) , $send(this$static.modules.contacts.contactSyncActor, new ContactsSyncActor$UserChanged(upd)));
    }
  }
  batch.array.length > 0 && $addOrUpdateItems_1(this$static.modules.users.users, batch);
}

function $hasUsers(this$static, uids){
  var uid, uid$iterator;
  for (uid$iterator = $iterator(new AbstractMap$1(uids.map_0)); $hasNext(uid$iterator.val$outerIter2);) {
    uid = dynamicCast($next_1(uid$iterator), 33);
    if (!$getValue_2(this$static.modules.users.users, fromInt(uid.value_0))) {
      return false;
    }
  }
  return true;
}

function $onUserAvatarChanged(this$static, uid, _avatar){
  var avatar, u;
  avatar = convert_3(_avatar);
  u = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11);
  if (u) {
    if (equalsE(u.avatar, avatar)) {
      return;
    }
    u = new User_1(u.uid, u.accessHash, u.name_0, u.localName, avatar, u.sex, u.records);
    $addOrUpdateItem_2(this$static.modules.users.users, u);
    $send(this$static.modules.messages.dialogsActor, new DialogsActor$UserChanged(u));
    $send(this$static.modules.contacts.contactSyncActor, new ContactsSyncActor$UserChanged(u));
  }
}

function $onUserLocalNameChanged(this$static, uid, name_0){
  var u;
  u = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11);
  if (u) {
    if (equalsE(u.localName, name_0)) {
      return;
    }
    u = new User_1(u.uid, u.accessHash, u.name_0, name_0, u.avatar, u.sex, u.records);
    $addOrUpdateItem_2(this$static.modules.users.users, u);
    $send(this$static.modules.messages.dialogsActor, new DialogsActor$UserChanged(u));
    $send(this$static.modules.contacts.contactSyncActor, new ContactsSyncActor$UserChanged(u));
  }
}

function $onUserNameChanged(this$static, uid, name_0){
  var u;
  u = dynamicCast($getValue_2(this$static.modules.users.users, fromInt(uid)), 11);
  if (u) {
    if ($equals_3(u.name_0, name_0)) {
      return;
    }
    u = new User_1(u.uid, u.accessHash, name_0, u.localName, u.avatar, u.sex, u.records);
    $addOrUpdateItem_2(this$static.modules.users.users, u);
    u.localName == null && ($send(this$static.modules.messages.dialogsActor, new DialogsActor$UserChanged(u)) , $send(this$static.modules.contacts.contactSyncActor, new ContactsSyncActor$UserChanged(u)));
  }
}

function UsersProcessor(messenger){
  BaseModule.call(this, messenger);
}

defineClass(729, 25, {}, UsersProcessor);
var Lim_actor_model_modules_updates_UsersProcessor_2_classLit = createForClass('im.actor.model.modules.updates', 'UsersProcessor', 729, Lim_actor_model_modules_BaseModule_2_classLit);
defineClass(85, 1, {85:1});
var Lim_actor_model_modules_updates_internal_InternalUpdate_2_classLit = createForClass('im.actor.model.modules.updates.internal', 'InternalUpdate', 85, Ljava_lang_Object_2_classLit);
function ContactsLoaded(contacts){
  this.contacts = contacts;
}

defineClass(325, 85, {325:1, 85:1}, ContactsLoaded);
var Lim_actor_model_modules_updates_internal_ContactsLoaded_2_classLit = createForClass('im.actor.model.modules.updates.internal', 'ContactsLoaded', 325, Lim_actor_model_modules_updates_internal_InternalUpdate_2_classLit);
function DialogHistoryLoaded(dialogs){
  this.dialogs = dialogs;
}

defineClass(326, 85, {326:1, 85:1}, DialogHistoryLoaded);
var Lim_actor_model_modules_updates_internal_DialogHistoryLoaded_2_classLit = createForClass('im.actor.model.modules.updates.internal', 'DialogHistoryLoaded', 326, Lim_actor_model_modules_updates_internal_InternalUpdate_2_classLit);
function LoggedIn(auth, runnable){
  this.auth = auth;
  this.runnable = runnable;
}

defineClass(214, 85, {85:1, 214:1}, LoggedIn);
var Lim_actor_model_modules_updates_internal_LoggedIn_2_classLit = createForClass('im.actor.model.modules.updates.internal', 'LoggedIn', 214, Lim_actor_model_modules_updates_internal_InternalUpdate_2_classLit);
function MessagesHistoryLoaded(peer, loadHistory){
  this.peer = peer;
  this.loadHistory = loadHistory;
}

defineClass(324, 85, {85:1, 324:1}, MessagesHistoryLoaded);
var Lim_actor_model_modules_updates_internal_MessagesHistoryLoaded_2_classLit = createForClass('im.actor.model.modules.updates.internal', 'MessagesHistoryLoaded', 324, Lim_actor_model_modules_updates_internal_InternalUpdate_2_classLit);
function ModuleActor$1(){
}

defineClass(165, 1, {}, ModuleActor$1);
_.onError_0 = function onError_20(e){
}
;
_.onResult_0 = function onResult_20(response){
}
;
var Lim_actor_model_modules_utils_ModuleActor$1_2_classLit = createForClass('im.actor.model.modules.utils', 'ModuleActor/1', 165, Ljava_lang_Object_2_classLit);
function ModuleActor$2(this$0, val$callback){
  this.this$01 = this$0;
  this.val$callback2 = val$callback;
}

defineClass(620, 1, {}, ModuleActor$2);
_.onError_0 = function onError_21(e){
  $send_1(this.this$01.context.actorScope.actorRef, new ModuleActor$2$2(this.val$callback2, e));
}
;
_.onResult_0 = function onResult_21(response){
  $send_1(this.this$01.context.actorScope.actorRef, new ModuleActor$2$1(this.val$callback2, response));
}
;
var Lim_actor_model_modules_utils_ModuleActor$2_2_classLit = createForClass('im.actor.model.modules.utils', 'ModuleActor/2', 620, Ljava_lang_Object_2_classLit);
function ModuleActor$2$1(val$callback, val$response){
  this.val$callback2 = val$callback;
  this.val$response3 = val$response;
}

defineClass(621, 1, $intern_20, ModuleActor$2$1);
_.run = function run_15(){
  this.val$callback2.onResult_0(this.val$response3);
}
;
var Lim_actor_model_modules_utils_ModuleActor$2$1_2_classLit = createForClass('im.actor.model.modules.utils', 'ModuleActor/2/1', 621, Ljava_lang_Object_2_classLit);
function ModuleActor$2$2(val$callback, val$e){
  this.val$callback2 = val$callback;
  this.val$e3 = val$e;
}

defineClass(622, 1, $intern_20, ModuleActor$2$2);
_.run = function run_16(){
  this.val$callback2.onError_0(this.val$e3);
}
;
var Lim_actor_model_modules_utils_ModuleActor$2$2_2_classLit = createForClass('im.actor.model.modules.utils', 'ModuleActor/2/2', 622, Ljava_lang_Object_2_classLit);
function $saveAuthKey(this$static, key){
  $putLong(this$static.preferencesStorage, 'auth_id', key);
}

function PreferenceApiStorage(preferencesStorage){
  this.preferencesStorage = preferencesStorage;
}

defineClass(636, 1, {}, PreferenceApiStorage);
var Lim_actor_model_modules_utils_PreferenceApiStorage_2_classLit = createForClass('im.actor.model.modules.utils', 'PreferenceApiStorage', 636, Ljava_lang_Object_2_classLit);
function $clinit_RandomUtils(){
  $clinit_RandomUtils = emptyMethod;
  RANDOM = new Random;
}

var RANDOM;
defineClass(164, 1, {164:1});
var Lim_actor_model_mvvm_BaseValueModel_2_classLit = createForClass('im.actor.model.mvvm', 'BaseValueModel', 164, Ljava_lang_Object_2_classLit);
function MVVMCollection$1(this$0, val$items){
  this.this$01 = this$0;
  this.val$items2 = val$items;
}

defineClass(449, 1, $intern_20, MVVMCollection$1);
_.run = function run_17(){
  var i_0, i$iterator;
  for (i$iterator = new AbstractList$IteratorImpl(this.val$items2); i$iterator.i < i$iterator.this$01.size_1();) {
    i_0 = (checkCriticalElement(i$iterator.i < i$iterator.this$01.size_1()) , dynamicCast(i$iterator.this$01.get_1(i$iterator.last = i$iterator.i++), 116));
    $containsKey(this.this$01.values, valueOf_0(i_0.getEngineId())) && dynamicCast($get_2(this.this$01.values, valueOf_0(i_0.getEngineId())), 164).updateValues(i_0);
  }
}
;
var Lim_actor_model_mvvm_MVVMCollection$1_2_classLit = createForClass('im.actor.model.mvvm', 'MVVMCollection/1', 449, Ljava_lang_Object_2_classLit);
function $addOrUpdateItem_2(this$static, item_0){
  var data_0, res;
  $put_1(this$static.cache, valueOf_0(item_0.getEngineId()), item_0);
  res = new ArrayList;
  setCheck(res.array, res.array.length, item_0);
  runOnUiThread(new MVVMCollection$1(this$static.this$01, res));
  data_0 = this$static.this$01.serialize_0(item_0);
  $addOrUpdateItem(this$static.this$01.collectionStorage, item_0.getEngineId(), data_0);
}

function $addOrUpdateItems_1(this$static, values){
  var records, t, t$iterator, v, v$iterator;
  for (t$iterator = new AbstractList$IteratorImpl(values); t$iterator.i < t$iterator.this$01.size_1();) {
    t = (checkCriticalElement(t$iterator.i < t$iterator.this$01.size_1()) , dynamicCast(t$iterator.this$01.get_1(t$iterator.last = t$iterator.i++), 116));
    $put_1(this$static.cache, valueOf_0(t.getEngineId()), t);
  }
  runOnUiThread(new MVVMCollection$1(this$static.this$01, values));
  records = new ArrayList;
  for (v$iterator = new AbstractList$IteratorImpl(values); v$iterator.i < v$iterator.this$01.size_1();) {
    v = (checkCriticalElement(v$iterator.i < v$iterator.this$01.size_1()) , dynamicCast(v$iterator.this$01.get_1(v$iterator.last = v$iterator.i++), 116));
    $add_0(records, new KeyValueRecord(v.getEngineId(), this$static.this$01.serialize_0(v)));
  }
  $addOrUpdateItems(this$static.this$01.collectionStorage, records);
}

function $getValue_2(this$static, id_0){
  var data_0, res;
  if ($containsKey(this$static.cache, valueOf_0(id_0))) {
    return dynamicCast($get_2(this$static.cache, valueOf_0(id_0)), 116);
  }
  data_0 = $getValue_0(this$static.this$01.collectionStorage, id_0);
  if (data_0 != null) {
    res = this$static.this$01.deserialize(data_0);
    $put_1(this$static.cache, valueOf_0(res.getEngineId()), res);
    return res;
  }
   else {
    return null;
  }
}

function MVVMCollection$ProxyKeyValueEngine(this$0){
  this.this$01 = this$0;
  this.cache = new HashMap;
}

defineClass(611, 1, {}, MVVMCollection$ProxyKeyValueEngine);
var Lim_actor_model_mvvm_MVVMCollection$ProxyKeyValueEngine_2_classLit = createForClass('im.actor.model.mvvm', 'MVVMCollection/ProxyKeyValueEngine', 611, Ljava_lang_Object_2_classLit);
function runOnUiThread(runnable){
  $scheduleDeferred(($clinit_SchedulerImpl() , INSTANCE), new JsMainThreadProvider$1(runnable));
}

var Lim_actor_model_mvvm_ModelChangedListener_2_classLit = createForInterface('im.actor.model.mvvm', 'ModelChangedListener');
function $change(this$static, value_0){
  if (this$static.value_0 != null && value_0 != null && equals_Ljava_lang_Object__Z__devirtual$(value_0, this$static.value_0)) {
    return false;
  }
  this$static.value_0 = value_0;
  $postToMainThread(new ValueModel$1(this$static));
  return true;
}

function ValueModel(defaultValue){
  this.listeners = new ArrayList;
  this.value_0 = defaultValue;
}

defineClass(40, 1, {}, ValueModel);
_.toString$ = function toString_159(){
  return this.value_0 + '';
}
;
var Lim_actor_model_mvvm_ValueModel_2_classLit = createForClass('im.actor.model.mvvm', 'ValueModel', 40, Ljava_lang_Object_2_classLit);
function ValueModel$1(this$0){
  this.this$01 = this$0;
}

defineClass(629, 1, $intern_20, ValueModel$1);
_.run = function run_18(){
  var listener$array, listener$index, listener$max;
  for (listener$array = dynamicCast($toArray_0(this.this$01.listeners, initDim(Lim_actor_model_mvvm_ValueChangedListener_2_classLit, {808:1, 3:1, 6:1}, 880, this.this$01.listeners.array.length, 0, 1)), 808) , listener$index = 0 , listener$max = listener$array.length; listener$index < listener$max; ++listener$index) {
    null.nullMethod();
  }
}
;
var Lim_actor_model_mvvm_ValueModel$1_2_classLit = createForClass('im.actor.model.mvvm', 'ValueModel/1', 629, Ljava_lang_Object_2_classLit);
function $request_1(this$static, request, callback){
  $send_1(this$static.apiBroker, new ApiBroker$PerformRequest(request, callback));
}

function ActorApi(endpoints, keyStorage, callback, networkProvider){
  this.apiBroker = ($clinit_ApiBroker() , $actorOf_0(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new Props(new ApiBroker$1(endpoints, keyStorage, callback, networkProvider), null), 'api/broker'));
}

defineClass(634, 1, {}, ActorApi);
var Lim_actor_model_network_ActorApi_2_classLit = createForClass('im.actor.model.network', 'ActorApi', 634, Ljava_lang_Object_2_classLit);
function ConnectionEndpoint(host, port, type_0){
  this.host = host;
  this.port = port;
  this.type_0 = type_0;
}

defineClass(343, 1, {343:1}, ConnectionEndpoint);
_.port = 0;
var Lim_actor_model_network_ConnectionEndpoint_2_classLit = createForClass('im.actor.model.network', 'ConnectionEndpoint', 343, Ljava_lang_Object_2_classLit);
function $clinit_ConnectionEndpoint$Type(){
  $clinit_ConnectionEndpoint$Type = emptyMethod;
  TCP = new ConnectionEndpoint$Type('TCP', 0);
  TCP_TLS = new ConnectionEndpoint$Type('TCP_TLS', 1);
  WS = new ConnectionEndpoint$Type('WS', 2);
  WS_TLS = new ConnectionEndpoint$Type('WS_TLS', 3);
}

function ConnectionEndpoint$Type(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_12(){
  $clinit_ConnectionEndpoint$Type();
  return initValues(getClassLiteralForArray(Lim_actor_model_network_ConnectionEndpoint$Type_2_classLit, 1), $intern_6, 121, 0, [TCP, TCP_TLS, WS, WS_TLS]);
}

defineClass(121, 18, {121:1, 3:1, 29:1, 18:1}, ConnectionEndpoint$Type);
var TCP, TCP_TLS, WS, WS_TLS;
var Lim_actor_model_network_ConnectionEndpoint$Type_2_classLit = createForEnum('im.actor.model.network', 'ConnectionEndpoint/Type', 121, Ljava_lang_Enum_2_classLit, values_12);
function $fetchEndpoint(this$static){
  this$static.roundRobin = (this$static.roundRobin + 1) % this$static.endpoints.length;
  return this$static.endpoints[this$static.roundRobin];
}

function Endpoints(endpoints){
  this.endpoints = endpoints;
}

defineClass(635, 1, {}, Endpoints);
_.roundRobin = 0;
var Lim_actor_model_network_Endpoints_2_classLit = createForClass('im.actor.model.network', 'Endpoints', 635, Ljava_lang_Object_2_classLit);
function RpcException(tag, message){
  Exception.call(this);
  this.tag = tag;
  this.message_0 = message;
}

defineClass(454, 14, $intern_2, RpcException);
_.getMessage = function getMessage_1(){
  return this.message_0;
}
;
var Lim_actor_model_network_RpcException_2_classLit = createForClass('im.actor.model.network', 'RpcException', 454, Ljava_lang_Exception_2_classLit);
function RpcInternalException(){
  RpcException.call(this, 'INTERNAL_ERROR', 'Internal server error');
}

defineClass(99, 454, $intern_2, RpcInternalException);
var Lim_actor_model_network_RpcInternalException_2_classLit = createForClass('im.actor.model.network', 'RpcInternalException', 99, Lim_actor_model_network_RpcException_2_classLit);
function $clinit_ApiBroker(){
  $clinit_ApiBroker = emptyMethod;
  NEXT_RPC_ID = createAtomicLong();
}

function $createMtProto(this$static, key){
  var holder, holder$iterator;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ApiBroker' + ':' + 'Creating proto');
  $saveAuthKey(this$static.keyStorage, key);
  this$static.proto = new MTProto(key, $nextLong(new Random), this$static.endpoints, new ApiBroker$3(this$static), this$static.networkProvider);
  for (holder$iterator = $iterator_0(new AbstractMap$2(this$static.requests)); holder$iterator.val$outerIter2.hasNext();) {
    holder = dynamicCast($next_2(holder$iterator), 180);
    holder.protoId = $sendRpcMessage(this$static.proto, holder.message_0);
    $put_1(this$static.idMap, valueOf_0(holder.protoId), valueOf_0(holder.publicId));
  }
}

function $forceResend(this$static, randomId){
  var holder;
  holder = dynamicCast($get_2(this$static.requests, valueOf_0(randomId)), 180);
  if (holder) {
    if (neq(holder.protoId, {l:0, m:0, h:0})) {
      $remove_0(this$static.idMap, valueOf_0(holder.protoId));
      $cancelRpc(this$static.proto, holder.protoId);
    }
    $sendRpcMessage(this$static.proto, holder.message_0);
  }
}

function $performRequest(this$static, randomId, message, callback){
  var holder, mid;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ApiBroker' + ':' + ('-> request#' + toString_2(randomId) + ': ' + message));
  holder = new ApiBroker$RequestHolder(randomId, new RpcRequest(message.getHeaderKey(), $toByteArray(message)), callback);
  $put_1(this$static.requests, valueOf_0(holder.publicId), holder);
  if (this$static.proto) {
    mid = $sendRpcMessage(this$static.proto, holder.message_0);
    holder.protoId = mid;
    $put_1(this$static.idMap, valueOf_0(mid), valueOf_0(randomId));
  }
}

function $processResponse(this$static, mid, content_0){
  var e, f, holder, ok, protoStruct, response, rid;
  try {
    protoStruct = readRpcResponsePayload(content_0);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ApiBroker' + ':' + ('Broken response mid#' + toString_2(mid)));
      return;
    }
     else 
      throw unwrap($e0);
  }
  if ($containsKey(this$static.idMap, valueOf_0(mid))) {
    rid = dynamicCast($get_2(this$static.idMap, valueOf_0(mid)), 23).value_0;
  }
   else {
    return;
  }
  if ($containsKey(this$static.requests, valueOf_0(rid))) {
    holder = dynamicCast($get_2(this$static.requests, valueOf_0(rid)), 180);
  }
   else {
    return;
  }
  if (instanceOf(protoStruct, 293)) {
    ok = dynamicCast(protoStruct, 293);
    $remove_0(this$static.requests, valueOf_0(rid));
    neq(holder.protoId, {l:0, m:0, h:0}) && $remove_0(this$static.idMap, valueOf_0(holder.protoId));
    try {
      response = dynamicCast($read(ok.responseType, ok.payload), 35);
    }
     catch ($e1) {
      $e1 = wrap($e1);
      if (instanceOf($e1, 5)) {
        e = $e1;
        $printStackTrace(e, ($clinit_System() , err));
        return;
      }
       else 
        throw unwrap($e1);
    }
    d_0('ApiBroker', '<- response#' + toString_2(holder.publicId) + ': ' + response);
    holder.callback_0.onResult_0(response);
  }
   else if (instanceOf(protoStruct, 294)) {
    e = dynamicCast(protoStruct, 294);
    $remove_0(this$static.requests, valueOf_0(rid));
    neq(holder.protoId, {l:0, m:0, h:0}) && $remove_0(this$static.idMap, valueOf_0(holder.protoId));
    w_0('ApiBroker', '<- error#' + toString_2(holder.publicId) + ': ' + e.errorTag + ' ' + e.errorCode + ' ' + e.userMessage);
    holder.callback_0.onError_0(new RpcException(e.errorTag, e.userMessage));
  }
   else if (instanceOf(protoStruct, 295)) {
    e = dynamicCast(protoStruct, 295);
    d_0('ApiBroker', '<- internal_error#' + toString_2(holder.publicId));
    if (e.canTryAgain) {
      $send_0(this$static.context.actorScope.actorRef, new ApiBroker$ForceResend(rid), mul(fromInt(e.tryAgainDelay), {l:1000, m:0, h:0}));
    }
     else {
      $remove_0(this$static.requests, valueOf_0(rid));
      neq(holder.protoId, {l:0, m:0, h:0}) && $remove_0(this$static.idMap, valueOf_0(holder.protoId));
      holder.callback_0.onError_0(new RpcInternalException);
    }
  }
   else if (instanceOf(protoStruct, 296)) {
    f = dynamicCast(protoStruct, 296);
    d_0('ApiBroker', '<- flood_wait#' + toString_2(holder.publicId) + ' ' + f.delay + ' sec');
    $send_0(this$static.context.actorScope.actorRef, new ApiBroker$ForceResend(rid), mul(fromInt(f.delay), {l:1000, m:0, h:0}));
  }
}

function $processUpdate_0(this$static, content_0){
  var body_0, e, protoStruct, type_0, updateBox;
  try {
    protoStruct = new Push(new DataInput_0(content_0, 0, content_0.length));
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ApiBroker' + ':' + 'Broken mt update');
      return;
    }
     else 
      throw unwrap($e0);
  }
  type_0 = protoStruct.updateType;
  body_0 = protoStruct.body_0;
  try {
    updateBox = $read(type_0, body_0);
  }
   catch ($e1) {
    $e1 = wrap($e1);
    if (instanceOf($e1, 5)) {
      e = $e1;
      $printStackTrace(e, ($clinit_System() , err));
      !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ApiBroker' + ':' + 'Broken update box');
      return;
    }
     else 
      throw unwrap($e1);
  }
  $onUpdateReceived(this$static.callback_0, updateBox);
}

function ApiBroker(endpoints, keyStorage, callback, networkProvider){
  $clinit_ApiBroker();
  this.requests = new HashMap;
  this.idMap = new HashMap;
  this.authIdBackOff = new ExponentialBackoff;
  this.endpoints = endpoints;
  this.keyStorage = keyStorage;
  this.callback_0 = callback;
  this.networkProvider = networkProvider;
}

defineClass(648, 785, {}, ApiBroker);
_.onReceive = function onReceive_27(message){
  var $tmp;
  instanceOf(message, 249)?(!!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ApiBroker' + ':' + 'Creating auth key...') , requestAuthId(this.endpoints, this.networkProvider, new ApiBroker$2(this))):instanceOf(message, 177)?$createMtProto(this, dynamicCast(message, 177).authId):instanceOf(message, 176)?$performRequest(this, ($tmp = NEXT_RPC_ID.value_0 , NEXT_RPC_ID.value_0 = add_0(NEXT_RPC_ID.value_0, {l:1, m:0, h:0}) , $tmp), dynamicCast(message, 176).message_0, dynamicCast(message, 176).callback_0):instanceOf(message, 178)?$processResponse(this, dynamicCast(message, 178).responseId, dynamicCast(message, 178).data_0):instanceOf(message, 179)?$forceResend(this, dynamicCast(message, 179).id_0):instanceOf(message, 250) && $processUpdate_0(this, dynamicCast(message, 250).data_0);
}
;
_.preStart = function preStart_16(){
  if (eq($getLong_1(this.keyStorage.preferencesStorage, 'auth_id', {l:0, m:0, h:0}), {l:0, m:0, h:0})) {
    $send_1(this.context.actorScope.actorRef, new ApiBroker$RequestAuthId);
  }
   else {
    d_0('ApiBroker', 'Key loaded: ' + toString_2($getLong_1(this.keyStorage.preferencesStorage, 'auth_id', {l:0, m:0, h:0})));
    $send_1(this.context.actorScope.actorRef, new ApiBroker$InitMTProto($getLong_1(this.keyStorage.preferencesStorage, 'auth_id', {l:0, m:0, h:0})));
  }
}
;
var NEXT_RPC_ID;
var Lim_actor_model_network_api_ApiBroker_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker', 648, Lim_actor_model_droidkit_actors_Actor_2_classLit);
function ApiBroker$1(val$endpoints, val$keyStorage, val$callback, val$networkProvider){
  this.val$endpoints1 = val$endpoints;
  this.val$keyStorage2 = val$keyStorage;
  this.val$callback3 = val$callback;
  this.val$networkProvider4 = val$networkProvider;
}

defineClass(649, 1, {}, ApiBroker$1);
_.create_0 = function create_32(){
  return new ApiBroker(this.val$endpoints1, this.val$keyStorage2, this.val$callback3, this.val$networkProvider4);
}
;
var Lim_actor_model_network_api_ApiBroker$1_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/1', 649, Ljava_lang_Object_2_classLit);
function $onFailure(this$static){
  var delay;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ApiBroker' + ':' + 'Key creation failure');
  $onFailure_0(this$static.this$01.authIdBackOff);
  delay = $exponentialWait(this$static.this$01.authIdBackOff);
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ApiBroker' + ':' + ('Key creation delay in ' + toString_2(delay) + ' ms'));
  $send_0(this$static.this$01.context.actorScope.actorRef, new ApiBroker$RequestAuthId, delay);
}

function $onSuccess(this$static, authId){
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ApiBroker' + ':' + ('Key created: ' + toString_2(authId)));
  $send_1(this$static.this$01.context.actorScope.actorRef, new ApiBroker$InitMTProto(authId));
}

function ApiBroker$2(this$0){
  this.this$01 = this$0;
}

defineClass(650, 1, {}, ApiBroker$2);
var Lim_actor_model_network_api_ApiBroker$2_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/2', 650, Ljava_lang_Object_2_classLit);
function $onRpcResponse(this$static, mid, content_0){
  $send_1(this$static.this$01.context.actorScope.actorRef, new ApiBroker$ProtoResponse(mid, content_0));
}

function $onUpdate(this$static, content_0){
  $send_1(this$static.this$01.context.actorScope.actorRef, new ApiBroker$ProtoUpdate(content_0));
}

function ApiBroker$3(this$0){
  this.this$01 = this$0;
}

defineClass(651, 1, {}, ApiBroker$3);
var Lim_actor_model_network_api_ApiBroker$3_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/3', 651, Ljava_lang_Object_2_classLit);
function ApiBroker$ForceResend(id_0){
  this.id_0 = id_0;
}

defineClass(179, 1, {179:1}, ApiBroker$ForceResend);
_.id_0 = {l:0, m:0, h:0};
var Lim_actor_model_network_api_ApiBroker$ForceResend_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/ForceResend', 179, Ljava_lang_Object_2_classLit);
function ApiBroker$InitMTProto(authId){
  this.authId = authId;
}

defineClass(177, 1, {177:1}, ApiBroker$InitMTProto);
_.authId = {l:0, m:0, h:0};
var Lim_actor_model_network_api_ApiBroker$InitMTProto_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/InitMTProto', 177, Ljava_lang_Object_2_classLit);
function ApiBroker$PerformRequest(message, callback){
  this.message_0 = message;
  this.callback_0 = callback;
}

defineClass(176, 1, {176:1}, ApiBroker$PerformRequest);
var Lim_actor_model_network_api_ApiBroker$PerformRequest_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/PerformRequest', 176, Ljava_lang_Object_2_classLit);
function ApiBroker$ProtoResponse(responseId, data_0){
  this.responseId = responseId;
  this.data_0 = data_0;
}

defineClass(178, 1, {178:1}, ApiBroker$ProtoResponse);
_.responseId = {l:0, m:0, h:0};
var Lim_actor_model_network_api_ApiBroker$ProtoResponse_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/ProtoResponse', 178, Ljava_lang_Object_2_classLit);
function ApiBroker$ProtoUpdate(data_0){
  this.data_0 = data_0;
}

defineClass(250, 1, {250:1}, ApiBroker$ProtoUpdate);
var Lim_actor_model_network_api_ApiBroker$ProtoUpdate_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/ProtoUpdate', 250, Ljava_lang_Object_2_classLit);
function ApiBroker$RequestAuthId(){
}

defineClass(249, 1, {249:1}, ApiBroker$RequestAuthId);
var Lim_actor_model_network_api_ApiBroker$RequestAuthId_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/RequestAuthId', 249, Ljava_lang_Object_2_classLit);
function ApiBroker$RequestHolder(publicId, message, callback){
  this.message_0 = message;
  this.publicId = publicId;
  this.callback_0 = callback;
}

defineClass(180, 1, {180:1}, ApiBroker$RequestHolder);
_.protoId = {l:0, m:0, h:0};
_.publicId = {l:0, m:0, h:0};
var Lim_actor_model_network_api_ApiBroker$RequestHolder_2_classLit = createForClass('im.actor.model.network.api', 'ApiBroker/RequestHolder', 180, Ljava_lang_Object_2_classLit);
function $clinit_ManagedConnection(){
  $clinit_ManagedConnection = emptyMethod;
  RANDOM_0 = new Random;
}

function $close(this$static){
  var id_0, id$iterator, ping, ping$iterator;
  if (this$static.isClosed) {
    return;
  }
  this$static.isClosed = true;
  this$static.rawConnection.jsWebSocket.close();
  for (id$iterator = $iterator(new AbstractMap$1(this$static.packageTimers)); $hasNext(id$iterator.val$outerIter2);) {
    id_0 = dynamicCast($next_1(id$iterator), 33);
    $cancel_0(dynamicCast($get_2(this$static.packageTimers, id_0), 59));
  }
  for (ping$iterator = $iterator(new AbstractMap$1(this$static.schedulledPings)); $hasNext(ping$iterator.val$outerIter2);) {
    ping = dynamicCast($next_1(ping$iterator), 23);
    $cancel_0(dynamicCast($get_2(this$static.schedulledPings, ping), 59));
  }
  $reset(this$static.schedulledPings);
  $reset(this$static.packageTimers);
  $cancel_0(this$static.pingTask);
  $cancel_0(this$static.connectionTimeout);
  $cancel_0(this$static.handshakeTimeout);
  !this$static.isOpened || !this$static.isHandshakePerformed?$onConnectionCreateError(this$static.factoryCallback, this$static):this$static.callback_0.onConnectionDie();
}

function $onAckPackage(this$static, data_0){
  var ackContent, frameId, timerCompat;
  ackContent = new DataInput(data_0);
  frameId = $readInt(ackContent);
  timerCompat = dynamicCast($remove_0(this$static.packageTimers, valueOf(frameId)), 59);
  if (!timerCompat) {
    return;
  }
  $send(($clinit_TimerCompat() , TIMER_ACTOR), new TimerActor$Cancel(timerCompat));
  $refreshTimeouts(this$static);
}

function $onDropPackage(this$static, data_0){
  var drop, message, messageLen;
  drop = new DataInput(data_0);
  $readLong(drop);
  $readByte(drop);
  messageLen = $readInt(drop);
  message = _String_0($readBytes(drop, messageLen));
  d_0(this$static.TAG, 'Drop received: ' + message);
  throw new IOException_0('Drop received: ' + message);
}

function $onHandshakePackage(this$static, data_0){
  var apiMajor, apiMinor, handshakeResponse, localSha256, protoVersion, sha256;
  d_0(this$static.TAG, 'Handshake response received');
  handshakeResponse = new DataInput(data_0);
  protoVersion = $readByte(handshakeResponse);
  apiMajor = $readByte(handshakeResponse);
  apiMinor = $readByte(handshakeResponse);
  sha256 = $readBytes(handshakeResponse, 32);
  localSha256 = ($clinit_CryptoUtils() , $SHA256(this$static.handshakeRandomData));
  if (!equals_32(sha256, localSha256)) {
    d_0(this$static.TAG, 'SHA 256 is incorrect');
    d_0(this$static.TAG, 'Random data: ' + hex(this$static.handshakeRandomData));
    d_0(this$static.TAG, 'Remote SHA256: ' + hex(sha256));
    d_0(this$static.TAG, 'Local SHA256: ' + hex(localSha256));
    throw new IOException_0('SHA 256 is incorrect');
  }
  if (protoVersion != 1) {
    d_0(this$static.TAG, 'Incorrect Proto Version, expected: 1, got ' + protoVersion + ';');
    throw new IOException_0('Incorrect Proto Version, expected: 1, got ' + protoVersion + ';');
  }
  if (apiMajor != 1) {
    d_0(this$static.TAG, 'Incorrect Api Major Version, expected: 1, got ' + apiMajor + ';');
    throw new IOException_0('Incorrect Api Major Version, expected: 1, got ' + apiMajor + ';');
  }
  if (apiMinor != 0) {
    d_0(this$static.TAG, 'Incorrect Api Minor Version, expected: 0, got ' + apiMinor + ';');
    throw new IOException_0('Incorrect Api Minor Version, expected: 0, got ' + apiMinor + ';');
  }
  d_0(this$static.TAG, 'Handshake successful');
  this$static.isHandshakePerformed = true;
  $onConnectionCreated(this$static.factoryCallback, this$static);
  $cancel_0(this$static.handshakeTimeout);
  $schedule_0(this$static.pingTask, {l:300000, m:0, h:0});
}

function $onPongPackage(this$static, data_0){
  var dataInput, pingId, size_0, timeoutTask;
  dataInput = new DataInput(data_0);
  size_0 = $readInt(dataInput);
  if (size_0 != 8) {
    d_0(this$static.TAG, 'Received incorrect pong');
    throw new IOException_0('Incorrect pong payload size');
  }
  pingId = $readLong(dataInput);
  d_0(this$static.TAG, 'Received pong #' + toString_2(pingId) + '...');
  timeoutTask = dynamicCast($remove_0(this$static.schedulledPings, valueOf_0(pingId)), 59);
  if (!timeoutTask) {
    return;
  }
  $send(($clinit_TimerCompat() , TIMER_ACTOR), new TimerActor$Cancel(timeoutTask));
  $refreshTimeouts(this$static);
}

function $onRawConnected(this$static){
  var handshakeRequest;
  d_0(this$static.TAG, 'onConnected');
  if (this$static.isClosed) {
    d_0(this$static.TAG, 'onConnected:isClosed');
    return;
  }
  if (this$static.isOpened) {
    d_0(this$static.TAG, 'onConnected:isOpened');
    return;
  }
  this$static.isOpened = true;
  $cancel_0(this$static.connectionTimeout);
  d_0(this$static.TAG, 'Starting handshake');
  handshakeRequest = new DataOutput;
  $writeByte(handshakeRequest, this$static.mtprotoVersion);
  $writeByte(handshakeRequest, this$static.apiMajorVersion);
  $writeByte(handshakeRequest, this$static.apiMinorVersion);
  this$static.handshakeRandomData = initDim(B_classLit, $intern_17, 0, 32, 7, 1);
  $nextBytes(RANDOM_0, this$static.handshakeRandomData);
  $writeInt_0(handshakeRequest, this$static.handshakeRandomData.length);
  $writeBytes_1(handshakeRequest, this$static.handshakeRandomData, 0, this$static.handshakeRandomData.length);
  $schedule_0(this$static.handshakeTimeout, {l:5000, m:0, h:0});
  $rawPost(this$static, 255, $toByteArray_0(handshakeRequest));
}

function $onRawReceived(this$static, data_0){
  var content_0, crc32, dataInput, dataLength, e, header, packageIndex;
  if (this$static.isClosed) {
    return;
  }
  w_0(this$static.TAG, 'onRawReceived');
  try {
    dataInput = new DataInput(data_0);
    packageIndex = $readInt(dataInput);
    if (this$static.receivedPackages != packageIndex) {
      w_0(this$static.TAG, 'Invalid package index. Expected: ' + this$static.receivedPackages + ', got: ' + packageIndex);
      throw new IOException_0('Invalid package index. Expected: ' + this$static.receivedPackages + ', got: ' + packageIndex);
    }
    ++this$static.receivedPackages;
    header = $readByte(dataInput);
    dataLength = $readInt(dataInput);
    content_0 = $readBytes(dataInput, dataLength);
    crc32 = $readInt(dataInput);
    this$static.CRC32_ENGINE.crc = 0;
    $update_0(this$static.CRC32_ENGINE, content_0);
    if (toInt(and(fromInt(this$static.CRC32_ENGINE.crc), {l:$intern_7, m:1023, h:0})) != crc32) {
      w_0(this$static.TAG, 'Incorrect CRC32');
      throw new IOException_0('Incorrect CRC32');
    }
    w_0(this$static.TAG, 'Received package: ' + header);
    if (header == 254) {
      if (this$static.isHandshakePerformed) {
        throw new IOException_0('Double Handshake');
      }
      $onHandshakePackage(this$static, content_0);
    }
     else {
      if (!this$static.isHandshakePerformed) {
        throw new IOException_0('Package before Handshake');
      }
      if (header == 0) {
        this$static.callback_0.onMessage(content_0, 0, content_0.length);
        $refreshTimeouts(this$static);
        $sendAckPackage(this$static, packageIndex);
      }
       else 
        header == 1?($rawPost_0(this$static, 2, content_0, content_0.length) , $refreshTimeouts(this$static)):header == 2?$onPongPackage(this$static, content_0):header == 3?$onDropPackage(this$static, content_0):header == 6?$onAckPackage(this$static, content_0):w_0(this$static.TAG, 'Received unknown package #' + header);
    }
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      $close(this$static);
    }
     else 
      throw unwrap($e0);
  }
}

function $post(this$static, data_0, len){
  var e;
  if (this$static.isClosed) {
    return;
  }
  try {
    $sendProtoPackage(this$static, data_0, len);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
      $close(this$static);
    }
     else 
      throw unwrap($e0);
  }
}

function $rawPost(this$static, header, data_0){
  $rawPost_0(this$static, header, data_0, data_0.length);
}

function $rawPost_0(this$static, header, data_0, len){
  var dataOutput, packageId, timeoutTask;
  packageId = this$static.sentPackages++;
  dataOutput = new DataOutput;
  $writeInt_0(dataOutput, packageId);
  $writeByte(dataOutput, header);
  $writeInt_0(dataOutput, data_0.length);
  $writeBytes_1(dataOutput, data_0, 0, len);
  this$static.CRC32_ENGINE.crc = 0;
  $update_1(this$static.CRC32_ENGINE, data_0, 0, len);
  $writeInt_0(dataOutput, toInt(and(fromInt(this$static.CRC32_ENGINE.crc), {l:$intern_7, m:1023, h:0})));
  if (header == 0) {
    timeoutTask = new TimerCompat(new ManagedConnection$TimeoutRunnable(this$static));
    $put_1(this$static.packageTimers, valueOf(packageId), timeoutTask);
    $send(($clinit_TimerCompat() , TIMER_ACTOR), new TimerActor$Schedule(timeoutTask, {l:5000, m:0, h:0}));
  }
  $doSend(this$static.rawConnection, $toByteArray_0(dataOutput));
}

function $refreshTimeouts(this$static){
  var ackTimeout, ackTimeout$iterator, ping, ping$iterator;
  for (ping$iterator = $iterator_0(new AbstractMap$2(this$static.schedulledPings)); ping$iterator.val$outerIter2.hasNext();) {
    ping = dynamicCast($next_2(ping$iterator), 59);
    $send(($clinit_TimerCompat() , TIMER_ACTOR), new TimerActor$Schedule(ping, {l:5000, m:0, h:0}));
  }
  for (ackTimeout$iterator = $iterator_0(new AbstractMap$2(this$static.packageTimers)); ackTimeout$iterator.val$outerIter2.hasNext();) {
    ackTimeout = dynamicCast($next_2(ackTimeout$iterator), 59);
    $send(($clinit_TimerCompat() , TIMER_ACTOR), new TimerActor$Schedule(ackTimeout, {l:5000, m:0, h:0}));
  }
  $schedule_0(this$static.pingTask, {l:300000, m:0, h:0});
}

function $sendAckPackage(this$static, receivedIndex){
  var ackPackage;
  if (this$static.isClosed) {
    return;
  }
  ackPackage = new DataOutput;
  $writeInt_0(ackPackage, receivedIndex);
  $rawPost(this$static, 6, $toByteArray_0(ackPackage));
}

function $sendPingMessage(this$static){
  var dataOutput, pingId, pingTimeoutTask;
  if (this$static.isClosed) {
    return;
  }
  pingId = $nextLong(RANDOM_0);
  dataOutput = new DataOutput;
  $writeInt_0(dataOutput, 8);
  $writeLong_0(dataOutput, pingId);
  pingTimeoutTask = new TimerCompat(new ManagedConnection$TimeoutRunnable(this$static));
  $put_1(this$static.schedulledPings, valueOf_0(pingId), pingTimeoutTask);
  $send(($clinit_TimerCompat() , TIMER_ACTOR), new TimerActor$Schedule(pingTimeoutTask, {l:5000, m:0, h:0}));
  d_0(this$static.TAG, 'Performing ping #' + toString_2(pingId) + '... ' + pingTimeoutTask);
  $rawPost(this$static, 1, $toByteArray_0(dataOutput));
}

function $sendProtoPackage(this$static, data_0, len){
  if (this$static.isClosed) {
    return;
  }
  $rawPost_0(this$static, 0, data_0, len);
}

function ManagedConnection(connectionId, endpoint, callback, factoryCallback){
  $clinit_ManagedConnection();
  this.connectionInterface = new ManagedConnection$ConnectionInterface(this);
  this.CRC32_ENGINE = new CRC32;
  this.schedulledPings = new HashMap;
  this.packageTimers = new HashMap;
  this.TAG = 'Connection#' + connectionId;
  this.mtprotoVersion = 1;
  this.apiMajorVersion = 1;
  this.apiMinorVersion = 0;
  this.callback_0 = callback;
  this.factoryCallback = factoryCallback;
  this.rawConnection = new WebSocketConnection(endpoint, this.connectionInterface);
  d_0(this.TAG, 'Starting connection');
  this.handshakeTimeout = new TimerCompat(new ManagedConnection$TimeoutRunnable(this));
  this.pingTask = new TimerCompat(new ManagedConnection$PingRunnable(this));
  this.connectionTimeout = new TimerCompat(new ManagedConnection$TimeoutRunnable(this));
  $schedule_0(this.connectionTimeout, {l:5000, m:0, h:0});
  $doConnect(this.rawConnection);
}

defineClass(740, 1, {}, ManagedConnection);
_.apiMajorVersion = 0;
_.apiMinorVersion = 0;
_.isClosed = false;
_.isHandshakePerformed = false;
_.isOpened = false;
_.mtprotoVersion = 0;
_.receivedPackages = 0;
_.sentPackages = 0;
var RANDOM_0;
var Lim_actor_model_network_connection_ManagedConnection_2_classLit = createForClass('im.actor.model.network.connection', 'ManagedConnection', 740, Ljava_lang_Object_2_classLit);
function $onReceived(this$static, data_0){
  $onRawReceived(this$static.this$01, data_0);
}

function ManagedConnection$ConnectionInterface(this$0){
  this.this$01 = this$0;
}

defineClass(741, 1, {}, ManagedConnection$ConnectionInterface);
var Lim_actor_model_network_connection_ManagedConnection$ConnectionInterface_2_classLit = createForClass('im.actor.model.network.connection', 'ManagedConnection/ConnectionInterface', 741, Ljava_lang_Object_2_classLit);
function ManagedConnection$PingRunnable(this$0){
  this.this$01 = this$0;
}

defineClass(742, 1, $intern_20, ManagedConnection$PingRunnable);
_.run = function run_19(){
  $sendPingMessage(this.this$01);
}
;
var Lim_actor_model_network_connection_ManagedConnection$PingRunnable_2_classLit = createForClass('im.actor.model.network.connection', 'ManagedConnection/PingRunnable', 742, Ljava_lang_Object_2_classLit);
function ManagedConnection$TimeoutRunnable(this$0){
  this.this$01 = this$0;
}

defineClass(327, 1, $intern_20, ManagedConnection$TimeoutRunnable);
_.run = function run_20(){
  d_0(this.this$01.TAG, 'Timeout ' + this);
  $close(this.this$01);
}
;
var Lim_actor_model_network_connection_ManagedConnection$TimeoutRunnable_2_classLit = createForClass('im.actor.model.network.connection', 'ManagedConnection/TimeoutRunnable', 327, Ljava_lang_Object_2_classLit);
function $onConnectionCreateError(this$static, connection){
  this$static.val$createCallback2.onConnectionCreateError();
  $remove_4(this$static.this$01.pendingConnections, connection);
}

function $onConnectionCreated(this$static, connection){
  this$static.val$createCallback2.onConnectionCreated(connection);
  $remove_4(this$static.this$01.pendingConnections, connection);
}

function ManagedNetworkProvider$1(this$0, val$createCallback){
  this.this$01 = this$0;
  this.val$createCallback2 = val$createCallback;
}

defineClass(596, 1, {}, ManagedNetworkProvider$1);
var Lim_actor_model_network_connection_ManagedNetworkProvider$1_2_classLit = createForClass('im.actor.model.network.connection', 'ManagedNetworkProvider/1', 596, Ljava_lang_Object_2_classLit);
function requestAuthId(endpoints, networkProvider, callback){
  var backoff, isFinished;
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AuthId' + ':' + 'Requesting AuthId');
  isFinished = initDim(Z_classLit, $intern_4, 0, 1, 8, 1);
  backoff = new ExponentialBackoff;
  isFinished[0] = false;
  $createConnection(networkProvider, 0, (endpoints.roundRobin = (endpoints.roundRobin + 1) % endpoints.endpoints.length , endpoints.endpoints[endpoints.roundRobin]), new AuthIdRetriever$1(isFinished, callback), new AuthIdRetriever$2(isFinished, backoff, callback));
}

function AuthIdRetriever$1(val$isFinished, val$callback){
  this.val$isFinished1 = val$isFinished;
  this.val$callback2 = val$callback;
}

defineClass(652, 1, {}, AuthIdRetriever$1);
_.onConnectionDie = function onConnectionDie(){
  if (!this.val$isFinished1[0]) {
    this.val$isFinished1[0] = true;
    $onFailure(this.val$callback2);
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AuthId' + ':' + 'Connection dies');
  }
}
;
_.onMessage = function onMessage(data_0, offset, len){
  var authId, dataInput, e, msg, payload;
  if (this.val$isFinished1[0]) {
    return;
  }
  try {
    dataInput = new DataInput_0(data_0, offset, len);
    $readLong(dataInput);
    $readLong(dataInput);
    $readLong(dataInput);
    payload = $readProtoBytes(dataInput);
    msg = new DataInput_0(payload, 0, payload.length);
    $readByte(msg);
    authId = $readLong(msg);
    if (!this.val$isFinished1[0]) {
      this.val$isFinished1[0] = true;
      $onSuccess(this.val$callback2, authId);
      !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AuthId' + ':' + ('Auth Id loaded: ' + toString_2(authId)));
      return;
    }
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 14)) {
      e = $e0;
      !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AuthId' + ':' + 'Error during parsing auth id response');
      $printStackTrace(e, ($clinit_System() , err));
    }
     else 
      throw unwrap($e0);
  }
  throw new RuntimeException;
}
;
var Lim_actor_model_network_mtp_AuthIdRetriever$1_2_classLit = createForClass('im.actor.model.network.mtp', 'AuthIdRetriever/1', 652, Ljava_lang_Object_2_classLit);
function AuthIdRetriever$2(val$isFinished, val$backoff, val$callback){
  this.val$isFinished1 = val$isFinished;
  this.val$backoff2 = val$backoff;
  this.val$callback3 = val$callback;
}

defineClass(653, 1, {}, AuthIdRetriever$2);
_.onConnectionCreateError = function onConnectionCreateError(){
  if (!this.val$isFinished1[0]) {
    this.val$isFinished1[0] = true;
    $onFailure(this.val$callback3);
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AuthId' + ':' + 'Unable to create connection');
  }
}
;
_.onConnectionCreated = function onConnectionCreated(connection){
  var data_0, e, output;
  if (this.val$isFinished1[0]) {
    return;
  }
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AuthId' + ':' + 'Connection created');
  this.val$backoff2.currentFailureCount.value_0 = 0;
  try {
    output = new DataOutput;
    $writeLong_0(output, {l:0, m:0, h:0});
    $writeLong_0(output, {l:0, m:0, h:0});
    $writeLong_0(output, {l:0, m:0, h:0});
    $writeVarInt_0(output, {l:1, m:0, h:0});
    $writeByte(output, 240);
    data_0 = $toByteArray_0(output);
    $post(connection, data_0, data_0.length);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 14)) {
      e = $e0;
      !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'AuthId' + ':' + 'Error during requesting auth id');
      $printStackTrace(e, ($clinit_System() , err));
      if (!this.val$isFinished1[0]) {
        this.val$isFinished1[0] = true;
        $onFailure(this.val$callback3);
      }
    }
     else 
      throw unwrap($e0);
  }
}
;
var Lim_actor_model_network_mtp_AuthIdRetriever$2_2_classLit = createForClass('im.actor.model.network.mtp', 'AuthIdRetriever/2', 653, Ljava_lang_Object_2_classLit);
function $cancelRpc(this$static, mtId){
  $send_1(this$static.sender, new SenderActor$ForgetMessage(mtId));
}

function $sendRpcMessage(this$static, protoStruct){
  var $tmp, mtId;
  mtId = ($clinit_MTUids() , $tmp = NEXT_ID_0.value_0 , NEXT_ID_0.value_0 = add_0(NEXT_ID_0.value_0, {l:1, m:0, h:0}) , $tmp);
  $send_1(this$static.sender, new SenderActor$SendMessage(mtId, $toByteArray_1(new MTRpcRequest_0($toByteArray_1(protoStruct)))));
  return mtId;
}

function MTProto(authId, sessionId, endpoints, callback, networkProvider){
  this.authId = authId;
  this.sessionId = sessionId;
  this.endpoints = endpoints;
  this.callback_0 = callback;
  this.networkProvider = networkProvider;
  $clinit_ManagerActor();
  $actorOf(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new ActorSelection(new Props(new ManagerActor$1(this), null), 'mtproto/manager'));
  this.sender = $actorOf((null , mainSystem), new ActorSelection(new Props(new SenderActor$1_0(this), null), 'mtproto/sender'));
  $actorOf((null , mainSystem), new ActorSelection(new Props(new ReceiverActor$1(this), null), 'mtproto/receiver'));
}

defineClass(725, 1, {}, MTProto);
_.authId = {l:0, m:0, h:0};
_.sessionId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_MTProto_2_classLit = createForClass('im.actor.model.network.mtp', 'MTProto', 725, Ljava_lang_Object_2_classLit);
function $clinit_ManagerActor(){
  $clinit_ManagerActor = emptyMethod;
  NEXT_CONNECTION = createAtomicInt();
}

function $checkConnection(this$static){
  var id_0;
  if (this$static.isCheckingConnections) {
    return;
  }
  if (!this$static.currentConnection) {
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Manager' + ':' + 'Trying to create connection...');
    this$static.isCheckingConnections = true;
    id_0 = NEXT_CONNECTION.value_0++;
    $createConnection(this$static.mtProto.networkProvider, id_0, $fetchEndpoint(this$static.endpoints), new ManagerActor$2(this$static, id_0), new ManagerActor$3(this$static, id_0));
  }
}

function $onConnectionCreated_0(this$static, id_0, connection){
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Manager' + ':' + ('Connection #' + id_0 + ' created'));
  if (connection.isClosed) {
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Manager' + ':' + ('Unable to register connection #' + id_0 + ': already closed'));
    return;
  }
  if (this$static.currentConnectionId == id_0) {
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Manager' + ':' + ('Unable to register connection #' + id_0 + ': already have connection'));
    return;
  }
  if (this$static.currentConnection) {
    $close(this$static.currentConnection);
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Manager' + ':' + 'Set connection #0');
    this$static.currentConnectionId = 0;
  }
  !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Manager' + ':' + ('Set connection #' + id_0));
  this$static.currentConnectionId = id_0;
  this$static.currentConnection = connection;
  this$static.backoff.currentFailureCount.value_0 = 0;
  this$static.isCheckingConnections = false;
  $requestCheckConnection(this$static, {l:0, m:0, h:0});
  $send_1(this$static.sender, new SenderActor$ConnectionCreated);
}

function $onConnectionDie(this$static, id_0){
  !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Manager' + ':' + ('Connection #' + id_0 + ' dies'));
  if (this$static.currentConnectionId == id_0) {
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Manager' + ':' + 'Set connection #0');
    this$static.currentConnectionId = 0;
    this$static.currentConnection = null;
    $requestCheckConnection(this$static, {l:0, m:0, h:0});
  }
   else {
    w_0('Manager', 'Unable to unregister connection #' + id_0 + ': connection not found, expected: #' + this$static.currentConnectionId);
  }
}

function $onInMessage_1(this$static, data_0, offset, len){
  var authId, bis, e, messageId, payload, sessionId;
  bis = new DataInput_0(data_0, offset, len);
  try {
    authId = $readLong(bis);
    sessionId = $readLong(bis);
    if (neq(authId, this$static.authId) || neq(sessionId, this$static.sessionId)) {
      throw new IOException_0('Incorrect header');
    }
    messageId = $readLong(bis);
    payload = $readProtoBytes(bis);
    $send_1(this$static.receiver, new ProtoMessage(messageId, payload));
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Manager' + ':' + 'Closing connection: incorrect package');
      $printStackTrace(e, ($clinit_System() , err));
      if (this$static.currentConnection) {
        $close(this$static.currentConnection);
        this$static.currentConnection = null;
        this$static.currentConnectionId = 0;
        !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Manager' + ':' + 'Set connection #0');
      }
      $checkConnection(this$static);
    }
     else 
      throw unwrap($e0);
  }
}

function $onOutMessage(this$static, data_0, offset, len){
  var bos, pkg;
  if (!!this$static.currentConnection && this$static.currentConnection.isClosed) {
    this$static.currentConnection = null;
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'Manager' + ':' + 'Set connection #0');
    this$static.currentConnectionId = 0;
    $checkConnection(this$static);
  }
  if (this$static.currentConnection) {
    bos = new DataOutput;
    $writeLong_0(bos, this$static.authId);
    $writeLong_0(bos, this$static.sessionId);
    $writeBytes_1(bos, data_0, offset, len);
    pkg = $toByteArray_0(bos);
    $post(this$static.currentConnection, pkg, pkg.length);
  }
}

function $requestCheckConnection(this$static, wait){
  if (!this$static.isCheckingConnections) {
    !this$static.currentConnection && (eq(wait, {l:0, m:0, h:0})?!!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Manager' + ':' + 'Requesting connection creating'):!!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Manager' + ':' + ('Requesting connection creating in ' + toString_2(wait) + ' ms')));
    $sendOnce_0(this$static.context.actorScope.actorRef, new ManagerActor$PerformConnectionCheck, wait);
  }
}

function ManagerActor(mtProto){
  $clinit_ManagerActor();
  this.backoff = new ExponentialBackoff;
  this.mtProto = mtProto;
  this.endpoints = mtProto.endpoints;
  this.authId = mtProto.authId;
  this.sessionId = mtProto.sessionId;
}

function manager(mtProto){
  $clinit_ManagerActor();
  return $actorOf(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new ActorSelection(new Props(new ManagerActor$1(mtProto), null), 'mtproto/manager'));
}

defineClass(735, 785, {}, ManagerActor);
_.onReceive = function onReceive_28(message){
  var c, m;
  if (instanceOf(message, 312)) {
    c = dynamicCast(message, 312);
    $onConnectionCreated_0(this, c.connectionId, c.connection);
  }
   else if (instanceOf(message, 379)) {
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'Manager' + ':' + 'Connection create failure');
    $onFailure_0(this.backoff);
    this.isCheckingConnections = false;
    $requestCheckConnection(this, $exponentialWait(this.backoff));
  }
   else if (instanceOf(message, 311)) {
    $onConnectionDie(this, dynamicCast(message, 311).connectionId);
  }
   else if (instanceOf(message, 378)) {
    $checkConnection(this);
  }
   else if (instanceOf(message, 309)) {
    m = dynamicCast(message, 309);
    $onOutMessage(this, m.message_0, m.offset, m.len);
  }
   else if (instanceOf(message, 310)) {
    m = dynamicCast(message, 310);
    $onInMessage_1(this, m.data_0, m.offset, m.len);
  }
}
;
_.preStart = function preStart_17(){
  this.receiver = receiver(this.mtProto);
  this.sender = senderActor(this.mtProto);
  $checkConnection(this);
}
;
_.authId = {l:0, m:0, h:0};
_.currentConnectionId = 0;
_.isCheckingConnections = false;
_.sessionId = {l:0, m:0, h:0};
var NEXT_CONNECTION;
var Lim_actor_model_network_mtp_actors_ManagerActor_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor', 735, Lim_actor_model_droidkit_actors_Actor_2_classLit);
function ManagerActor$1(val$mtProto){
  this.val$mtProto1 = val$mtProto;
}

defineClass(463, 1, {}, ManagerActor$1);
_.create_0 = function create_33(){
  return new ManagerActor(this.val$mtProto1);
}
;
var Lim_actor_model_network_mtp_actors_ManagerActor$1_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/1', 463, Ljava_lang_Object_2_classLit);
function ManagerActor$2(this$0, val$id){
  this.this$01 = this$0;
  this.val$id2 = val$id;
}

defineClass(736, 1, {}, ManagerActor$2);
_.onConnectionDie = function onConnectionDie_0(){
  $send_1(this.this$01.context.actorScope.actorRef, new ManagerActor$ConnectionDie(this.val$id2));
}
;
_.onMessage = function onMessage_0(data_0, offset, len){
  $send_1(this.this$01.context.actorScope.actorRef, new ManagerActor$InMessage(data_0, offset, len));
}
;
_.val$id2 = 0;
var Lim_actor_model_network_mtp_actors_ManagerActor$2_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/2', 736, Ljava_lang_Object_2_classLit);
function ManagerActor$3(this$0, val$id){
  this.this$01 = this$0;
  this.val$id2 = val$id;
}

defineClass(737, 1, {}, ManagerActor$3);
_.onConnectionCreateError = function onConnectionCreateError_0(){
  $send_1(this.this$01.context.actorScope.actorRef, new ManagerActor$ConnectionCreateFailure);
}
;
_.onConnectionCreated = function onConnectionCreated_0(connection){
  $send_1(this.this$01.context.actorScope.actorRef, new ManagerActor$ConnectionCreated(this.val$id2, connection));
}
;
_.val$id2 = 0;
var Lim_actor_model_network_mtp_actors_ManagerActor$3_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/3', 737, Ljava_lang_Object_2_classLit);
function ManagerActor$ConnectionCreateFailure(){
}

defineClass(379, 1, {379:1}, ManagerActor$ConnectionCreateFailure);
var Lim_actor_model_network_mtp_actors_ManagerActor$ConnectionCreateFailure_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/ConnectionCreateFailure', 379, Ljava_lang_Object_2_classLit);
function ManagerActor$ConnectionCreated(connectionId, connection){
  this.connectionId = connectionId;
  this.connection = connection;
}

defineClass(312, 1, {312:1}, ManagerActor$ConnectionCreated);
_.connectionId = 0;
var Lim_actor_model_network_mtp_actors_ManagerActor$ConnectionCreated_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/ConnectionCreated', 312, Ljava_lang_Object_2_classLit);
function ManagerActor$ConnectionDie(connectionId){
  this.connectionId = connectionId;
}

defineClass(311, 1, {311:1}, ManagerActor$ConnectionDie);
_.connectionId = 0;
var Lim_actor_model_network_mtp_actors_ManagerActor$ConnectionDie_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/ConnectionDie', 311, Ljava_lang_Object_2_classLit);
function ManagerActor$InMessage(data_0, offset, len){
  this.data_0 = data_0;
  this.offset = offset;
  this.len = len;
}

defineClass(310, 1, {310:1}, ManagerActor$InMessage);
_.len = 0;
_.offset = 0;
var Lim_actor_model_network_mtp_actors_ManagerActor$InMessage_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/InMessage', 310, Ljava_lang_Object_2_classLit);
function ManagerActor$OutMessage(message, len){
  this.message_0 = message;
  this.offset = 0;
  this.len = len;
}

defineClass(309, 1, {309:1}, ManagerActor$OutMessage);
_.len = 0;
_.offset = 0;
var Lim_actor_model_network_mtp_actors_ManagerActor$OutMessage_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/OutMessage', 309, Ljava_lang_Object_2_classLit);
function ManagerActor$PerformConnectionCheck(){
}

defineClass(378, 1, {378:1}, ManagerActor$PerformConnectionCheck);
var Lim_actor_model_network_mtp_actors_ManagerActor$PerformConnectionCheck_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ManagerActor/PerformConnectionCheck', 378, Ljava_lang_Object_2_classLit);
function $onReceive_1(this$static, message){
  var $tmp, ack, ackMsgId, ackMsgId$array, ackMsgId$index, ackMsgId$max, box, container, disableConfirm, e, m, m$array, m$index, m$max, obj, responseBox, unsent;
  disableConfirm = false;
  try {
    if ($indexOf_0(this$static.receivedMessages, valueOf_0(message.messageId), 0) != -1) {
      w_0('ProtoReceiver', 'Already received message #' + toString_2(message.messageId) + ': ignoring');
      return;
    }
    if (this$static.receivedMessages.array.length >= 1000) {
      this$static.receivedMessages.remove_1(0);
      $add_0(this$static.receivedMessages, valueOf_0(message.messageId));
    }
    try {
      obj = readMessagePayload_0(message.payload);
    }
     catch ($e0) {
      $e0 = wrap($e0);
      if (instanceOf($e0, 5)) {
        e = $e0;
        !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ProtoReceiver' + ':' + 'Unable to parse message: ignoring');
        $printStackTrace(e, ($clinit_System() , err));
        return;
      }
       else 
        throw unwrap($e0);
    }
    if (instanceOf(obj, 433)) {
      $send_1(this$static.sender, new SenderActor$NewSession);
      $onNewSessionCreated(this$static.proto.callback_0.this$01.callback_0);
    }
     else if (instanceOf(obj, 147)) {
      container = dynamicCast(obj, 147);
      for (m$array = container.messages , m$index = 0 , m$max = m$array.length; m$index < m$max; ++m$index) {
        m = m$array[m$index];
        $send_2(this$static.context.actorScope.actorRef, m, this$static.context.actorScope.sender);
      }
    }
     else if (instanceOf(obj, 333)) {
      responseBox = dynamicCast(obj, 333);
      $send_1(this$static.sender, new SenderActor$ForgetMessage(responseBox.messageId));
      $onRpcResponse(this$static.proto.callback_0, responseBox.messageId, responseBox.payload);
    }
     else if (instanceOf(obj, 219)) {
      ack = dynamicCast(obj, 219);
      for (ackMsgId$array = ack.messagesIds , ackMsgId$index = 0 , ackMsgId$max = ackMsgId$array.length; ackMsgId$index < ackMsgId$max; ++ackMsgId$index) {
        ackMsgId = ackMsgId$array[ackMsgId$index];
        $send_1(this$static.sender, new SenderActor$ForgetMessage(ackMsgId));
      }
    }
     else if (instanceOf(obj, 334)) {
      box = dynamicCast(obj, 334);
      $onUpdate(this$static.proto.callback_0, box.payload);
    }
     else if (instanceOf(obj, 220)) {
      unsent = dynamicCast(obj, 220);
      if ($indexOf_0(this$static.receivedMessages, valueOf_0(unsent.responseMessageId), 0) == -1) {
        disableConfirm = true;
        $send_1(this$static.sender, new SenderActor$SendMessage(($clinit_MTUids() , $tmp = NEXT_ID_0.value_0 , NEXT_ID_0.value_0 = add_0(NEXT_ID_0.value_0, {l:1, m:0, h:0}) , $tmp), $toByteArray_1(new RequestResend(unsent.messageId))));
      }
    }
     else if (instanceOf(obj, 335)) {
      unsent = dynamicCast(obj, 335);
      if ($indexOf_0(this$static.receivedMessages, valueOf_0(unsent.messageId), 0) == -1) {
        disableConfirm = true;
        $send_1(this$static.sender, new SenderActor$SendMessage(($clinit_MTUids() , $tmp = NEXT_ID_0.value_0 , NEXT_ID_0.value_0 = add_0(NEXT_ID_0.value_0, {l:1, m:0, h:0}) , $tmp), $toByteArray_1(new RequestResend(unsent.messageId))));
      }
    }
     else {
      !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ProtoReceiver' + ':' + ('Unsupported package ' + obj));
    }
  }
   catch ($e1) {
    $e1 = wrap($e1);
    if (instanceOf($e1, 14)) {
      !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ProtoReceiver' + ':' + 'Parsing error');
    }
     else 
      throw unwrap($e1);
  }
   finally {
    disableConfirm || $send_1(this$static.sender, new SenderActor$ConfirmMessage(message.messageId));
  }
}

function ReceiverActor(proto){
  this.receivedMessages = new ArrayList;
  this.proto = proto;
}

function receiver(proto){
  return $actorOf(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new ActorSelection(new Props(new ReceiverActor$1(proto), null), 'mtproto/receiver'));
}

defineClass(739, 785, {}, ReceiverActor);
_.onReceive = function onReceive_29(message){
  instanceOf(message, 42)?$onReceive_1(this, dynamicCast(message, 42)):(!!this.context.actorScope.actorSystem.traceInterface && !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ACTOR_SYSTEM' + ':' + ('Drop: ' + message)) , $reply(this, new DeadLetter(message)));
}
;
_.preStart = function preStart_18(){
  this.sender = senderActor(this.proto);
}
;
var Lim_actor_model_network_mtp_actors_ReceiverActor_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ReceiverActor', 739, Lim_actor_model_droidkit_actors_Actor_2_classLit);
function ReceiverActor$1(val$proto){
  this.val$proto1 = val$proto;
}

defineClass(465, 1, {}, ReceiverActor$1);
_.create_0 = function create_34(){
  return new ReceiverActor(this.val$proto1);
}
;
var Lim_actor_model_network_mtp_actors_ReceiverActor$1_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'ReceiverActor/1', 465, Ljava_lang_Object_2_classLit);
function $buildAck(this$static){
  var i_0, ids, ids2;
  ids = initDim(J_classLit, $intern_4, 0, this$static.confirm_0.map_0.size_0, 6, 1);
  ids2 = dynamicCast($toArray(this$static.confirm_0, initDim(Ljava_lang_Long_2_classLit, $intern_21, 23, this$static.confirm_0.map_0.size_0, 0, 1)), 221);
  for (i_0 = 0; i_0 < ids.length; i_0++) {
    ids[i_0] = ids2[i_0].value_0;
  }
  return new MessageAck_0(ids);
}

function $doSend_0(this$static, message){
  var mtpMessages;
  if (this$static.confirm_0.map_0.size_0 > 0) {
    mtpMessages = new ArrayList;
    setCheck(mtpMessages.array, mtpMessages.array.length, message);
    $doSend_1(this$static, mtpMessages);
  }
   else {
    $performSend(this$static, message);
  }
}

function $doSend_1(this$static, items){
  var $tmp, container, currentPayload, i_0, message, messages;
  if (items.array.length > 0) {
    if (this$static.confirm_0.map_0.size_0 > 0) {
      $add(items, 0, new ProtoMessage(($clinit_MTUids() , $tmp = NEXT_ID_0.value_0 , NEXT_ID_0.value_0 = add_0(NEXT_ID_0.value_0, {l:1, m:0, h:0}) , $tmp), $toByteArray_1($buildAck(this$static))));
      $reset(this$static.confirm_0.map_0);
    }
  }
  if (items.array.length == 1) {
    $doSend_0(this$static, (checkElementIndex(0, items.array.length) , dynamicCast(items.array[0], 42)));
  }
   else if (items.array.length > 1) {
    messages = new ArrayList;
    currentPayload = 0;
    for (i_0 = 0; i_0 < items.array.length; i_0++) {
      message = (checkElementIndex(i_0, items.array.length) , dynamicCast(items.array[i_0], 42));
      currentPayload += message.payload.length;
      setCheck(messages.array, messages.array.length, message);
      if (currentPayload > 1024) {
        container = new Container_0(dynamicCast($toArray_0(messages, initDim(Lim_actor_model_network_mtp_entity_ProtoMessage_2_classLit, $intern_24, 42, messages.array.length, 0, 1)), 435));
        $performSend(this$static, new ProtoMessage(($clinit_MTUids() , $tmp = NEXT_ID_0.value_0 , NEXT_ID_0.value_0 = add_0(NEXT_ID_0.value_0, {l:1, m:0, h:0}) , $tmp), $toByteArray_1(container)));
        messages.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
        currentPayload = 0;
      }
    }
    if (messages.array.length > 0) {
      container = new Container_0(dynamicCast($toArray_0(messages, initDim(Lim_actor_model_network_mtp_entity_ProtoMessage_2_classLit, $intern_24, 42, messages.array.length, 0, 1)), 435));
      $performSend(this$static, new ProtoMessage(($clinit_MTUids() , $tmp = NEXT_ID_0.value_0 , NEXT_ID_0.value_0 = add_0(NEXT_ID_0.value_0, {l:1, m:0, h:0}) , $tmp), $toByteArray_1(container)));
    }
  }
}

function $performSend(this$static, message){
  var data_0;
  data_0 = $toByteArray_1(message);
  $send_1(this$static.manager, new ManagerActor$OutMessage(data_0, data_0.length));
}

function SenderActor_0(proto){
  this.proto = proto;
  this.unsentPackages = new HashMap;
  this.confirm_0 = new HashSet;
}

function senderActor(proto){
  return $actorOf(($clinit_ActorSystem() , $clinit_ActorSystem() , mainSystem), new ActorSelection(new Props(new SenderActor$1_0(proto), null), 'mtproto/sender'));
}

defineClass(738, 785, {}, SenderActor_0);
_.onReceive = function onReceive_30(message){
  var $tmp, acks, holder, l, l$iterator, messageAck, sendMessage, toSend, unsentPackage, unsentPackage$iterator;
  if (instanceOf(message, 104)) {
    d_0('ProtoSender', 'Received SendMessage #' + toString_2(dynamicCast(message, 104).mid));
    sendMessage = dynamicCast(message, 104);
    holder = new ProtoMessage(sendMessage.mid, sendMessage.message_0);
    $put_1(this.unsentPackages, valueOf_0(holder.messageId), holder);
    $doSend_0(this, holder);
  }
   else if (instanceOf(message, 380)) {
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ProtoSender' + ':' + 'Received ConnectionCreated');
    toSend = new ArrayList;
    for (unsentPackage$iterator = $iterator_0(new AbstractMap$2(this.unsentPackages)); unsentPackage$iterator.val$outerIter2.hasNext();) {
      unsentPackage = dynamicCast($next_2(unsentPackage$iterator), 42);
      d_0('ProtoSender', 'ReSending #' + toString_2(unsentPackage.messageId));
      setCheck(toSend.array, toSend.array.length, unsentPackage);
    }
    $doSend_1(this, toSend);
  }
   else if (instanceOf(message, 105)) {
    d_0('ProtoSender', 'Received ForgetMessage #' + toString_2(dynamicCast(message, 105).mid));
    $remove_0(this.unsentPackages, valueOf_0(dynamicCast(message, 105).mid));
  }
   else if (instanceOf(message, 201)) {
    d_0('ProtoSender', 'Confirming message #' + toString_2(dynamicCast(message, 201).mid));
    $add_1(this.confirm_0, valueOf_0(dynamicCast(message, 201).mid));
    this.confirm_0.map_0.size_0 >= 10?$sendOnce(this.context.actorScope.actorRef, new SenderActor$ForceAck):this.confirm_0.map_0.size_0 == 1 && $sendOnce_0(this.context.actorScope.actorRef, new SenderActor$ForceAck, {l:10000, m:0, h:0});
  }
   else if (instanceOf(message, 313)) {
    if (this.confirm_0.map_0.size_0 == 0) {
      return;
    }
    acks = '';
    for (l$iterator = $iterator(new AbstractMap$1(this.confirm_0.map_0)); $hasNext(l$iterator.val$outerIter2);) {
      l = dynamicCast($next_1(l$iterator), 23);
      acks.length != 0 && (acks += ',');
      acks += '#' + l;
    }
    !!log_2 && log_1($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[D] ' + 'ProtoSender' + ':' + ('Sending acks ' + acks));
    messageAck = $buildAck(this);
    $reset(this.confirm_0.map_0);
    $doSend_0(this, new ProtoMessage(($clinit_MTUids() , $tmp = NEXT_ID_0.value_0 , NEXT_ID_0.value_0 = add_0(NEXT_ID_0.value_0, {l:1, m:0, h:0}) , $tmp), $toByteArray_1(messageAck)));
  }
   else if (instanceOf(message, 381)) {
    !!log_2 && warn($format(($clinit_JsLogProvider() , dateTimeFormat), new Date_0, null) + '[W] ' + 'ProtoSender' + ':' + 'Received NewSessionCreated');
    toSend = new ArrayList;
    for (unsentPackage$iterator = $iterator_0(new AbstractMap$2(this.unsentPackages)); unsentPackage$iterator.val$outerIter2.hasNext();) {
      unsentPackage = dynamicCast($next_2(unsentPackage$iterator), 42);
      d_0('ProtoSender', 'ReSending #' + toString_2(unsentPackage.messageId));
      setCheck(toSend.array, toSend.array.length, unsentPackage);
    }
    $doSend_1(this, toSend);
  }
}
;
_.preStart = function preStart_19(){
  this.manager = manager(this.proto);
}
;
var Lim_actor_model_network_mtp_actors_SenderActor_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor', 738, Lim_actor_model_droidkit_actors_Actor_2_classLit);
function SenderActor$1_0(val$proto){
  this.val$proto1 = val$proto;
}

defineClass(464, 1, {}, SenderActor$1_0);
_.create_0 = function create_35(){
  return new SenderActor_0(this.val$proto1);
}
;
var Lim_actor_model_network_mtp_actors_SenderActor$1_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor/1', 464, Ljava_lang_Object_2_classLit);
function SenderActor$ConfirmMessage(rid){
  this.mid = rid;
}

defineClass(201, 1, {201:1}, SenderActor$ConfirmMessage);
_.mid = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_actors_SenderActor$ConfirmMessage_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor/ConfirmMessage', 201, Ljava_lang_Object_2_classLit);
function SenderActor$ConnectionCreated(){
}

defineClass(380, 1, {380:1}, SenderActor$ConnectionCreated);
var Lim_actor_model_network_mtp_actors_SenderActor$ConnectionCreated_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor/ConnectionCreated', 380, Ljava_lang_Object_2_classLit);
function SenderActor$ForceAck(){
}

defineClass(313, 1, {313:1}, SenderActor$ForceAck);
var Lim_actor_model_network_mtp_actors_SenderActor$ForceAck_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor/ForceAck', 313, Ljava_lang_Object_2_classLit);
function SenderActor$ForgetMessage(rid){
  this.mid = rid;
}

defineClass(105, 1, {105:1}, SenderActor$ForgetMessage);
_.mid = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_actors_SenderActor$ForgetMessage_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor/ForgetMessage', 105, Ljava_lang_Object_2_classLit);
function SenderActor$NewSession(){
}

defineClass(381, 1, {381:1}, SenderActor$NewSession);
var Lim_actor_model_network_mtp_actors_SenderActor$NewSession_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor/NewSession', 381, Ljava_lang_Object_2_classLit);
function SenderActor$SendMessage(rid, message){
  this.mid = rid;
  this.message_0 = message;
}

defineClass(104, 1, {104:1}, SenderActor$SendMessage);
_.mid = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_actors_SenderActor$SendMessage_2_classLit = createForClass('im.actor.model.network.mtp.actors', 'SenderActor/SendMessage', 104, Ljava_lang_Object_2_classLit);
function $toByteArray_1(this$static){
  var e, outputStream;
  outputStream = new DataOutput;
  try {
    this$static.writeObject(outputStream);
  }
   catch ($e0) {
    $e0 = wrap($e0);
    if (instanceOf($e0, 5)) {
      e = $e0;
      $printStackTrace(e, ($clinit_System() , err));
    }
     else 
      throw unwrap($e0);
  }
  return $toByteArray_0(outputStream);
}

function ProtoObject(stream){
  this.readObject(stream);
}

defineClass(32, 1, $intern_25);
var Lim_actor_model_network_mtp_entity_ProtoObject_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'ProtoObject', 32, Ljava_lang_Object_2_classLit);
function ProtoStruct(stream){
  ProtoObject.call(this, stream);
}

defineClass(45, 32, $intern_25);
_.readObject = function readObject(bs){
  this.readBody(bs);
  return this;
}
;
_.writeObject = function writeObject(bs){
  var header;
  header = this.getHeader_0();
  header != 0 && (bs.data_0.length <= bs.offset + 1 && $expand(bs, bs.offset + 1) , bs.data_0[bs.offset++] = header);
  this.writeBody(bs);
}
;
var Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'ProtoStruct', 45, Lim_actor_model_network_mtp_entity_ProtoObject_2_classLit);
function Container(stream){
  ProtoStruct.call(this, stream);
}

function Container_0(messages){
  this.messages = messages;
}

defineClass(147, 45, {147:1, 32:1}, Container, Container_0);
_.getHeader_0 = function getHeader_15(){
  return 10;
}
;
_.readBody = function readBody(bs){
  var i_0, size_0;
  size_0 = toInt($readVarInt(bs));
  this.messages = initDim(Lim_actor_model_network_mtp_entity_ProtoMessage_2_classLit, $intern_24, 42, size_0, 0, 1);
  for (i_0 = 0; i_0 < size_0; ++i_0) {
    this.messages[i_0] = new ProtoMessage_0(bs);
  }
}
;
_.toString$ = function toString_160(){
  return 'Conatiner[' + this.messages.length + ' items]';
}
;
_.writeBody = function writeBody(bs){
  var m, m$array, m$index, m$max;
  if (this.messages != null && this.messages.length > 0) {
    $writeVarInt_0(bs, fromInt(this.messages.length));
    for (m$array = this.messages , m$index = 0 , m$max = m$array.length; m$index < m$max; ++m$index) {
      m = m$array[m$index];
      $writeLong_0(bs, m.messageId);
      $writeProtoBytes(bs, m.payload, m.payload.length);
    }
  }
   else {
    $writeVarInt_0(bs, {l:0, m:0, h:0});
  }
}
;
var Lim_actor_model_network_mtp_entity_Container_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'Container', 147, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function Drop(stream){
  ProtoStruct.call(this, stream);
}

defineClass(758, 45, $intern_25, Drop);
_.getHeader_0 = function getHeader_16(){
  return 13;
}
;
_.readBody = function readBody_0(bs){
  this.messageId = $readLong(bs);
  this.message_0 = $readProtoString(bs);
}
;
_.toString$ = function toString_161(){
  return 'Drop[' + this.message_0 + ']';
}
;
_.writeBody = function writeBody_0(bs){
  $writeLong_0(bs, this.messageId);
  $writeProtoString(bs, this.message_0);
}
;
_.messageId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_Drop_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'Drop', 758, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function MTPush(stream){
  ProtoStruct.call(this, stream);
}

defineClass(334, 45, {334:1, 32:1}, MTPush);
_.getHeader_0 = function getHeader_17(){
  return 5;
}
;
_.readBody = function readBody_1(bs){
  this.payload = $readProtoBytes(bs);
}
;
_.toString$ = function toString_162(){
  return 'UpdateBox';
}
;
_.writeBody = function writeBody_1(bs){
  $writeProtoBytes(bs, this.payload, this.payload.length);
}
;
var Lim_actor_model_network_mtp_entity_MTPush_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'MTPush', 334, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function MTRpcRequest(stream){
  ProtoStruct.call(this, stream);
}

function MTRpcRequest_0(payload){
  this.payload = payload;
}

defineClass(466, 45, $intern_25, MTRpcRequest, MTRpcRequest_0);
_.getHeader_0 = function getHeader_18(){
  return 3;
}
;
_.readBody = function readBody_2(bs){
  this.payload = $readProtoBytes(bs);
}
;
_.toString$ = function toString_163(){
  return 'RequestBox';
}
;
_.writeBody = function writeBody_2(bs){
  $writeProtoBytes(bs, this.payload, this.payload.length);
}
;
var Lim_actor_model_network_mtp_entity_MTRpcRequest_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'MTRpcRequest', 466, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function MTRpcResponse(stream){
  ProtoStruct.call(this, stream);
}

defineClass(333, 45, {333:1, 32:1}, MTRpcResponse);
_.getHeader_0 = function getHeader_19(){
  return 4;
}
;
_.readBody = function readBody_3(bs){
  this.messageId = $readLong(bs);
  this.payload = $readProtoBytes(bs);
}
;
_.toString$ = function toString_164(){
  return 'ResponseBox [' + toString_2(this.messageId) + ']';
}
;
_.writeBody = function writeBody_3(bs){
  $writeLong_0(bs, this.messageId);
  $writeProtoBytes(bs, this.payload, this.payload.length);
}
;
_.messageId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_MTRpcResponse_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'MTRpcResponse', 333, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function MessageAck(stream){
  ProtoStruct.call(this, stream);
}

function MessageAck_0(messagesIds){
  this.messagesIds = messagesIds;
}

defineClass(219, 45, {219:1, 32:1}, MessageAck, MessageAck_0);
_.getHeader_0 = function getHeader_20(){
  return 6;
}
;
_.readBody = function readBody_4(bs){
  this.messagesIds = $readProtoLongs(bs);
}
;
_.toString$ = function toString_165(){
  return 'Ack ' + toString_188(this.messagesIds) + '';
}
;
_.writeBody = function writeBody_4(bs){
  $writeProtoLongs(bs, this.messagesIds);
}
;
var Lim_actor_model_network_mtp_entity_MessageAck_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'MessageAck', 219, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function NewSessionCreated(stream){
  ProtoStruct.call(this, stream);
}

defineClass(433, 45, {433:1, 32:1}, NewSessionCreated);
_.getHeader_0 = function getHeader_21(){
  return 12;
}
;
_.readBody = function readBody_5(bs){
  this.sessionId = $readLong(bs);
  this.messageId = $readLong(bs);
}
;
_.toString$ = function toString_166(){
  return 'NewSession {' + toString_2(this.sessionId) + '}';
}
;
_.writeBody = function writeBody_5(bs){
  $writeLong_0(bs, this.sessionId);
  $writeLong_0(bs, this.messageId);
}
;
_.messageId = {l:0, m:0, h:0};
_.sessionId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_NewSessionCreated_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'NewSessionCreated', 433, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function Ping(stream){
  ProtoStruct.call(this, stream);
}

defineClass(756, 45, $intern_25, Ping);
_.getHeader_0 = function getHeader_22(){
  return 1;
}
;
_.readBody = function readBody_6(bs){
  this.randomId = $readLong(bs);
}
;
_.toString$ = function toString_167(){
  return 'Ping{' + toString_2(this.randomId) + '}';
}
;
_.writeBody = function writeBody_6(bs){
  $writeLong_0(bs, this.randomId);
}
;
_.randomId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_Ping_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'Ping', 756, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function Pong(stream){
  ProtoStruct.call(this, stream);
}

defineClass(757, 45, $intern_25, Pong);
_.getHeader_0 = function getHeader_23(){
  return 2;
}
;
_.readBody = function readBody_7(bs){
  this.randomId = $readLong(bs);
}
;
_.toString$ = function toString_168(){
  return 'Pong{' + toString_2(this.randomId) + '}';
}
;
_.writeBody = function writeBody_7(bs){
  $writeLong_0(bs, this.randomId);
}
;
_.randomId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_Pong_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'Pong', 757, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function ProtoMessage(messageId, payload){
  this.messageId = messageId;
  this.payload = payload;
}

function ProtoMessage_0(stream){
  ProtoObject.call(this, stream);
}

defineClass(42, 32, {42:1, 32:1}, ProtoMessage, ProtoMessage_0);
_.readObject = function readObject_0(bs){
  this.messageId = $readLong(bs);
  this.payload = $readProtoBytes(bs);
  return this;
}
;
_.toString$ = function toString_169(){
  return 'ProtoMessage [#' + toString_2(this.messageId) + ']';
}
;
_.writeObject = function writeObject_0(bs){
  $writeLong_0(bs, this.messageId);
  $writeProtoBytes(bs, this.payload, this.payload.length);
}
;
_.messageId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_ProtoMessage_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'ProtoMessage', 42, Lim_actor_model_network_mtp_entity_ProtoObject_2_classLit);
function readMessagePayload(bs){
  var header;
  header = $readByte(bs);
  switch (header) {
    case 1:
      return new Ping(bs);
    case 2:
      return new Pong(bs);
    case 13:
      return new Drop(bs);
    case 10:
      return new Container(bs);
    case 3:
      return new MTRpcRequest(bs);
    case 4:
      return new MTRpcResponse(bs);
    case 6:
      return new MessageAck(bs);
    case 12:
      return new NewSessionCreated(bs);
    case 5:
      return new MTPush(bs);
    case 7:
      return new UnsentMessage(bs);
    case 9:
    case 8:
      return new UnsentResponse(bs);
  }
  throw new IOException_0('Unable to read proto object with header #' + header);
}

function readMessagePayload_0(bs){
  return readMessagePayload(new DataInput_0(bs, 0, bs.length));
}

function readRpcResponsePayload(data_0){
  var bs, header;
  bs = new DataInput_0(data_0, 0, data_0.length);
  header = $readByte(bs);
  switch (header) {
    case 1:
      return new RpcOk(bs);
    case 2:
      return new RpcError(bs);
    case 3:
      return new RpcFloodWait(bs);
    case 4:
      return new RpcInternalError(bs);
  }
  throw new IOException_0('Unable to read proto object');
}

function RequestResend(messageId){
  this.messageId = messageId;
}

defineClass(469, 45, $intern_25, RequestResend);
_.getHeader_0 = function getHeader_24(){
  return 9;
}
;
_.readBody = function readBody_8(bs){
  this.messageId = $readLong(bs);
}
;
_.writeBody = function writeBody_8(bs){
  $writeLong_0(bs, this.messageId);
}
;
_.messageId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_RequestResend_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'RequestResend', 469, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function UnsentMessage(stream){
  ProtoStruct.call(this, stream);
}

defineClass(335, 45, {32:1, 335:1}, UnsentMessage);
_.getHeader_0 = function getHeader_25(){
  return 7;
}
;
_.readBody = function readBody_9(bs){
  this.messageId = $readLong(bs);
  this.len = $readInt(bs);
}
;
_.toString$ = function toString_170(){
  return 'UnsentMessage[' + toString_2(this.messageId) + ']';
}
;
_.writeBody = function writeBody_9(bs){
  $writeLong_0(bs, this.messageId);
  $writeInt_0(bs, this.len);
}
;
_.len = 0;
_.messageId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_UnsentMessage_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'UnsentMessage', 335, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function UnsentResponse(stream){
  ProtoStruct.call(this, stream);
}

defineClass(220, 45, {32:1, 220:1}, UnsentResponse);
_.getHeader_0 = function getHeader_26(){
  return 8;
}
;
_.readBody = function readBody_10(bs){
  this.messageId = $readLong(bs);
  this.responseMessageId = $readLong(bs);
  this.len = $readInt(bs);
}
;
_.toString$ = function toString_171(){
  return 'UnsentResponse[' + toString_2(this.messageId) + '->' + toString_2(this.responseMessageId) + ']';
}
;
_.writeBody = function writeBody_10(bs){
  $writeLong_0(bs, this.messageId);
  $writeLong_0(bs, this.responseMessageId);
  $writeInt_0(bs, this.len);
}
;
_.len = 0;
_.messageId = {l:0, m:0, h:0};
_.responseMessageId = {l:0, m:0, h:0};
var Lim_actor_model_network_mtp_entity_UnsentResponse_2_classLit = createForClass('im.actor.model.network.mtp.entity', 'UnsentResponse', 220, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function Push(stream){
  ProtoStruct.call(this, stream);
}

defineClass(727, 45, $intern_25, Push);
_.getHeader_0 = function getHeader_27(){
  return 0;
}
;
_.readBody = function readBody_11(bs){
  this.updateType = $readInt(bs);
  this.body_0 = $readProtoBytes(bs);
}
;
_.toString$ = function toString_172(){
  return 'Update[' + this.updateType + ']';
}
;
_.writeBody = function writeBody_11(bs){
  $writeInt_0(bs, this.updateType);
  $writeProtoBytes(bs, this.body_0, this.body_0.length);
}
;
_.updateType = 0;
var Lim_actor_model_network_mtp_entity_rpc_Push_2_classLit = createForClass('im.actor.model.network.mtp.entity.rpc', 'Push', 727, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function RpcError(stream){
  ProtoStruct.call(this, stream);
}

defineClass(294, 45, {32:1, 294:1}, RpcError);
_.getHeader_0 = function getHeader_28(){
  return 2;
}
;
_.readBody = function readBody_12(bs){
  this.errorCode = $readInt(bs);
  this.errorTag = $readProtoString(bs);
  this.userMessage = $readProtoString(bs);
  this.canTryAgain = $readByte(bs) != 0;
  this.relatedData = $readProtoBytes(bs);
}
;
_.toString$ = function toString_173(){
  return 'RpcError [#' + this.errorCode + ' ' + this.errorTag + ']';
}
;
_.writeBody = function writeBody_12(bs){
  $writeInt_0(bs, this.errorCode);
  $writeProtoString(bs, this.errorTag);
  $writeProtoString(bs, this.userMessage);
  $writeByte(bs, this.canTryAgain?1:0);
  $writeProtoBytes(bs, this.relatedData, this.relatedData.length);
}
;
_.canTryAgain = false;
_.errorCode = 0;
var Lim_actor_model_network_mtp_entity_rpc_RpcError_2_classLit = createForClass('im.actor.model.network.mtp.entity.rpc', 'RpcError', 294, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function RpcFloodWait(stream){
  ProtoStruct.call(this, stream);
}

defineClass(296, 45, {32:1, 296:1}, RpcFloodWait);
_.getHeader_0 = function getHeader_29(){
  return 3;
}
;
_.readBody = function readBody_13(bs){
  this.delay = $readInt(bs);
}
;
_.writeBody = function writeBody_13(bs){
  $writeInt_0(bs, this.delay);
}
;
_.delay = 0;
var Lim_actor_model_network_mtp_entity_rpc_RpcFloodWait_2_classLit = createForClass('im.actor.model.network.mtp.entity.rpc', 'RpcFloodWait', 296, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function RpcInternalError(stream){
  ProtoStruct.call(this, stream);
}

defineClass(295, 45, {32:1, 295:1}, RpcInternalError);
_.getHeader_0 = function getHeader_30(){
  return 4;
}
;
_.readBody = function readBody_14(bs){
  this.canTryAgain = $readByte(bs) != 0;
  this.tryAgainDelay = $readInt(bs);
}
;
_.writeBody = function writeBody_14(bs){
  $writeByte(bs, this.canTryAgain?1:0);
  $writeInt_0(bs, this.tryAgainDelay);
}
;
_.canTryAgain = false;
_.tryAgainDelay = 0;
var Lim_actor_model_network_mtp_entity_rpc_RpcInternalError_2_classLit = createForClass('im.actor.model.network.mtp.entity.rpc', 'RpcInternalError', 295, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function RpcOk(stream){
  ProtoStruct.call(this, stream);
}

defineClass(293, 45, {32:1, 293:1}, RpcOk);
_.getHeader_0 = function getHeader_31(){
  return 1;
}
;
_.readBody = function readBody_15(bs){
  this.responseType = $readInt(bs);
  this.payload = $readProtoBytes(bs);
}
;
_.toString$ = function toString_174(){
  return 'RpcOk{' + this.responseType + ']';
}
;
_.writeBody = function writeBody_15(bs){
  $writeInt_0(bs, this.responseType);
  $writeProtoBytes(bs, this.payload, this.payload.length);
}
;
_.responseType = 0;
var Lim_actor_model_network_mtp_entity_rpc_RpcOk_2_classLit = createForClass('im.actor.model.network.mtp.entity.rpc', 'RpcOk', 293, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function RpcRequest(requestType, payload){
  this.requestType = requestType;
  this.payload = payload;
}

defineClass(726, 45, $intern_25, RpcRequest);
_.getHeader_0 = function getHeader_32(){
  return 1;
}
;
_.readBody = function readBody_16(bs){
  this.requestType = $readInt(bs);
  this.payload = $readProtoBytes(bs);
}
;
_.toString$ = function toString_175(){
  return 'RpcRequest[' + this.requestType + ']';
}
;
_.writeBody = function writeBody_16(bs){
  $writeInt_0(bs, this.requestType);
  $writeProtoBytes(bs, this.payload, this.payload.length);
}
;
_.requestType = 0;
var Lim_actor_model_network_mtp_entity_rpc_RpcRequest_2_classLit = createForClass('im.actor.model.network.mtp.entity.rpc', 'RpcRequest', 726, Lim_actor_model_network_mtp_entity_ProtoStruct_2_classLit);
function $clinit_MTUids(){
  $clinit_MTUids = emptyMethod;
  NEXT_ID_0 = createAtomicLong();
}

var NEXT_ID_0;
function $onEnvelopeProcessed(envelope, duration){
  gt(duration, {l:300, m:0, h:0}) && w_0('ACTOR_SYSTEM', 'Too long ' + envelope.scope_0.path + ' {' + envelope.message_0 + '}');
}

function ActorTrace(){
}

defineClass(628, 1, {}, ActorTrace);
var Lim_actor_model_util_ActorTrace_2_classLit = createForClass('im.actor.model.util', 'ActorTrace', 628, Ljava_lang_Object_2_classLit);
function $clinit_Base64Utils(){
  $clinit_Base64Utils = emptyMethod;
  var i_0;
  base64Chars = initValues(getClassLiteralForArray(C_classLit, 1), $intern_4, 0, 7, [65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 36, 95]);
  base64Values = initDim(B_classLit, $intern_17, 0, 128, 7, 1);
  for (i_0 = 0; i_0 < base64Chars.length; i_0++) {
    base64Values[base64Chars[i_0]] = narrow_byte(i_0);
  }
}

function fromBase64(data_0){
  $clinit_Base64Utils();
  var bytes, c0, c1, c2, c24, c3, chars, iidx, len, oidx, olen;
  if (data_0 == null) {
    return null;
  }
  len = data_0.length;
  if (len == 0) {
    return initDim(B_classLit, $intern_17, 0, 0, 7, 1);
  }
  chars = initDim(C_classLit, $intern_4, 0, len, 7, 1);
  $getChars(data_0, len, chars, 0);
  olen = 3 * ~~(len / 4);
  chars[len - 2] == 61 && --olen;
  chars[len - 1] == 61 && --olen;
  bytes = initDim(B_classLit, $intern_17, 0, olen, 7, 1);
  iidx = 0;
  oidx = 0;
  while (iidx < len) {
    c0 = base64Values[chars[iidx++] & 255];
    c1 = base64Values[chars[iidx++] & 255];
    c2 = base64Values[chars[iidx++] & 255];
    c3 = base64Values[chars[iidx++] & 255];
    c24 = c0 << 18 | c1 << 12 | c2 << 6 | c3;
    bytes[oidx++] = narrow_byte(c24 >> 16);
    if (oidx == olen) {
      break;
    }
    bytes[oidx++] = narrow_byte(c24 >> 8);
    if (oidx == olen) {
      break;
    }
    bytes[oidx++] = narrow_byte(c24);
  }
  return bytes;
}

function toBase64(data_0){
  $clinit_Base64Utils();
  var b0, b1, b2, b24, c0, c1, c2, c3, chars, charsLeft, iidx, len, oidx, olen;
  if (data_0 == null) {
    return null;
  }
  len = data_0.length;
  if (len == 0) {
    return '';
  }
  olen = 4 * ~~((len + 2) / 3);
  chars = initDim(C_classLit, $intern_4, 0, olen, 7, 1);
  iidx = 0;
  oidx = 0;
  charsLeft = len;
  while (charsLeft > 0) {
    b0 = data_0[iidx++] & 255;
    b1 = charsLeft > 1?data_0[iidx++] & 255:0;
    b2 = charsLeft > 2?data_0[iidx++] & 255:0;
    b24 = b0 << 16 | b1 << 8 | b2;
    c0 = b24 >> 18 & 63;
    c1 = b24 >> 12 & 63;
    c2 = b24 >> 6 & 63;
    c3 = b24 & 63;
    chars[oidx++] = base64Chars[c0];
    chars[oidx++] = base64Chars[c1];
    chars[oidx++] = charsLeft > 1?base64Chars[c2]:61;
    chars[oidx++] = charsLeft > 2?base64Chars[c3]:61;
    charsLeft -= 3;
  }
  return __valueOf(chars, 0, chars.length);
}

var base64Chars, base64Values;
function $clinit_CRC32(){
  $clinit_CRC32 = emptyMethod;
  crc_table_0 = make_crc_table();
}

function $update_0(this$static, buf){
  $update_1(this$static, buf, 0, buf.length);
}

function $update_1(this$static, buf, off, len){
  var c;
  c = ~this$static.crc;
  while (--len >= 0)
    c = crc_table_0[(c ^ buf[off++]) & 255] ^ c >>> 8;
  this$static.crc = ~c;
}

function CRC32(){
  $clinit_CRC32();
}

function make_crc_table(){
  var c, crc_table, k_0, n;
  crc_table = initDim(I_classLit, $intern_4, 0, 256, 7, 1);
  for (n = 0; n < 256; n++) {
    c = n;
    for (k_0 = 8; --k_0 >= 0;) {
      (c & 1) != 0?(c = -306674912 ^ c >>> 1):(c = c >>> 1);
    }
    crc_table[n] = c;
  }
  return crc_table;
}

defineClass(750, 1, {}, CRC32);
_.crc = 0;
var crc_table_0;
var Lim_actor_model_util_CRC32_2_classLit = createForClass('im.actor.model.util', 'CRC32', 750, Ljava_lang_Object_2_classLit);
function $exponentialWait(this$static){
  var maxDelay;
  maxDelay = fromInt(100 + 298 * this$static.currentFailureCount.value_0);
  return fromDouble($nextInternal(this$static.random, 24) * $intern_26 * toDouble(maxDelay));
}

function $onFailure_0(this$static){
  var val;
  val = ++this$static.currentFailureCount.value_0;
  val > 50 && $compareAndSet(this$static.currentFailureCount, val);
}

function ExponentialBackoff(){
  this.currentFailureCount = createAtomicInt();
  this.random = new Random;
}

defineClass(369, 1, {}, ExponentialBackoff);
var Lim_actor_model_util_ExponentialBackoff_2_classLit = createForClass('im.actor.model.util', 'ExponentialBackoff', 369, Ljava_lang_Object_2_classLit);
function equalsE(a, b){
  if (a == null && b == null) {
    return true;
  }
  if (a != null && b == null) {
    return false;
  }
  if (b != null && a == null) {
    return false;
  }
  return equals_Ljava_lang_Object__Z__devirtual$(a, b);
}

function $end(this$static){
  if (this$static.sectionName != null) {
    d_0(this$static.TAG, '' + this$static.sectionName + ' loaded in ' + toString_2(sub_0(getActorTime(), this$static.sectionStart)) + ' ms');
    this$static.sectionName = null;
  }
}

function $section(this$static, sectionName){
  $end(this$static);
  this$static.sectionName = sectionName;
  this$static.sectionStart = getActorTime();
}

function Timing(tag){
  this.TAG = tag;
}

defineClass(237, 1, {}, Timing);
_.sectionStart = {l:0, m:0, h:0};
var Lim_actor_model_util_Timing_2_classLit = createForClass('im.actor.model.util', 'Timing', 237, Ljava_lang_Object_2_classLit);
function $onContactsChanged_1(this$static, isEmpty){
  if (dynamicCast(this$static.isContactsEmpty.value_0, 51).value_0 != isEmpty) {
    $putBool(this$static.modules.preferences, 'app.contacts.empty', isEmpty);
    $change(this$static.isContactsEmpty, ($clinit_Boolean() , isEmpty?TRUE:FALSE));
  }
  if (!isEmpty) {
    if (dynamicCast(this$static.isAppEmpty.value_0, 51).value_0) {
      $putBool(this$static.modules.preferences, 'app.empty', false);
      $change(this$static.isAppEmpty, ($clinit_Boolean() , $clinit_Boolean() , FALSE));
    }
  }
}

function $onContactsLoaded_0(this$static){
  if (!this$static.isContactsLoaded) {
    this$static.isContactsLoaded = true;
    $putBool(this$static.modules.preferences, 'app.contacts.loaded', true);
    $updateLoaded(this$static);
  }
}

function $onDialogsChanged_1(this$static, isEmpty){
  if (dynamicCast(this$static.isDialogsEmpty.value_0, 51).value_0 != isEmpty) {
    $putBool(this$static.modules.preferences, 'app.dialogs.empty', isEmpty);
    $change(this$static.isDialogsEmpty, ($clinit_Boolean() , isEmpty?TRUE:FALSE));
  }
  if (!isEmpty) {
    if (dynamicCast(this$static.isAppEmpty.value_0, 51).value_0) {
      $putBool(this$static.modules.preferences, 'app.empty', false);
      $change(this$static.isAppEmpty, ($clinit_Boolean() , $clinit_Boolean() , FALSE));
    }
  }
}

function $onDialogsLoaded_0(this$static){
  if (!this$static.isDialogsLoaded) {
    this$static.isDialogsLoaded = true;
    $putBool(this$static.modules.preferences, 'app.dialogs.loaded', true);
    $updateLoaded(this$static);
  }
}

function $onPhoneImported(this$static){
  if (!this$static.isBookImported) {
    this$static.isBookImported = true;
    $putBool(this$static.modules.preferences, 'app.contacts.imported', true);
    $updateLoaded(this$static);
  }
}

function $updateLoaded(this$static){
  var val;
  val = this$static.isBookImported && this$static.isDialogsLoaded && this$static.isContactsLoaded;
  dynamicCast(this$static.isAppLoaded.value_0, 51).value_0 != val && $change(this$static.isAppLoaded, ($clinit_Boolean() , val?TRUE:FALSE));
}

function AppStateVM(modules){
  this.modules = modules;
  this.isDialogsEmpty = new ValueModel(($clinit_Boolean() , $getBool(modules.preferences, 'app.dialogs.empty', true)?TRUE:FALSE));
  this.isContactsEmpty = new ValueModel($getBool(modules.preferences, 'app.contacts.empty', true)?TRUE:FALSE);
  this.isAppEmpty = new ValueModel($getBool(modules.preferences, 'app.empty', true)?TRUE:FALSE);
  this.isBookImported = $getBool(modules.preferences, 'app.contacts.imported', false);
  this.isDialogsLoaded = $getBool(modules.preferences, 'app.dialogs.loaded', false);
  this.isContactsLoaded = $getBool(modules.preferences, 'app.contacts.loaded', false);
  this.isAppLoaded = new ValueModel(this.isBookImported && this.isDialogsLoaded && this.isContactsLoaded?TRUE:FALSE);
}

defineClass(656, 1, {}, AppStateVM);
_.isBookImported = false;
_.isContactsLoaded = false;
_.isDialogsLoaded = false;
var Lim_actor_model_viewmodel_AppStateVM_2_classLit = createForClass('im.actor.model.viewmodel', 'AppStateVM', 656, Ljava_lang_Object_2_classLit);
function AvatarUploadState(){
}

defineClass(718, 1, {}, AvatarUploadState);
var Lim_actor_model_viewmodel_AvatarUploadState_2_classLit = createForClass('im.actor.model.viewmodel', 'AvatarUploadState', 718, Ljava_lang_Object_2_classLit);
function GroupTypingVM(){
  this.active = new ValueModel(initDim(I_classLit, $intern_4, 0, 0, 7, 1));
}

defineClass(376, 1, {376:1}, GroupTypingVM);
var Lim_actor_model_viewmodel_GroupTypingVM_2_classLit = createForClass('im.actor.model.viewmodel', 'GroupTypingVM', 376, Ljava_lang_Object_2_classLit);
function $subscribe_3(this$static, listener){
  if ($indexOf_0(this$static.listeners, listener, 0) != -1) {
    return;
  }
  $add_0(this$static.listeners, listener);
  $changeValue(listener.val$value2, {uid:this$static.id_0, title:dynamicCastToString(this$static.name_0.value_0), adminId:this$static.creatorId});
}

function $updateValues(this$static, rawObj){
  var isChanged;
  isChanged = false;
  isChanged = isChanged | $change(this$static.name_0, rawObj.title_0);
  isChanged = isChanged | $change(this$static.avatar, rawObj.avatar);
  isChanged = isChanged | $change(this$static.isMember, ($clinit_Boolean() , rawObj.isMember?TRUE:FALSE));
  isChanged = isChanged | $change(this$static.members, new HashSet_0(rawObj.members));
  isChanged && $postToMainThread(new GroupVM$1(this$static));
}

function GroupVM(rawObj){
  this.listeners = new ArrayList;
  this.id_0 = rawObj.groupId;
  this.creatorId = rawObj.adminId;
  this.name_0 = new ValueModel(rawObj.title_0);
  this.avatar = new ValueModel(rawObj.avatar);
  this.isMember = new ValueModel(($clinit_Boolean() , rawObj.isMember?TRUE:FALSE));
  this.members = new ValueModel(new HashSet_0(rawObj.members));
  this.presence = new ValueModel(valueOf(0));
}

defineClass(94, 164, {164:1, 94:1}, GroupVM);
_.updateValues = function updateValues(rawObj){
  $updateValues(this, dynamicCast(rawObj, 21));
}
;
_.creatorId = 0;
_.id_0 = 0;
var Lim_actor_model_viewmodel_GroupVM_2_classLit = createForClass('im.actor.model.viewmodel', 'GroupVM', 94, Lim_actor_model_mvvm_BaseValueModel_2_classLit);
function GroupVM$1(this$0){
  this.this$01 = this$0;
}

defineClass(610, 1, $intern_20, GroupVM$1);
_.run = function run_21(){
  var l, l$array, l$index, l$max;
  for (l$array = dynamicCast($toArray_0(this.this$01.listeners, initDim(Lim_actor_model_mvvm_ModelChangedListener_2_classLit, $intern_27, 471, 0, 0, 1)), 472) , l$index = 0 , l$max = l$array.length; l$index < l$max; ++l$index) {
    l = l$array[l$index];
    l.onChanged_0(this.this$01);
  }
}
;
var Lim_actor_model_viewmodel_GroupVM$1_2_classLit = createForClass('im.actor.model.viewmodel', 'GroupVM/1', 610, Ljava_lang_Object_2_classLit);
function OwnAvatarVM(){
  new ValueModel(new AvatarUploadState);
}

defineClass(691, 1, {}, OwnAvatarVM);
var Lim_actor_model_viewmodel_OwnAvatarVM_2_classLit = createForClass('im.actor.model.viewmodel', 'OwnAvatarVM', 691, Ljava_lang_Object_2_classLit);
function UserPhone(phone){
  this.phone = phone;
}

defineClass(368, 1, {368:1}, UserPhone);
_.equals$ = function equals_20(o){
  var userPhone;
  if (this === o)
    return true;
  if (o == null || Lim_actor_model_viewmodel_UserPhone_2_classLit != getClass__Ljava_lang_Class___devirtual$(o))
    return false;
  userPhone = dynamicCast(o, 368);
  if (neq(this.phone, userPhone.phone))
    return false;
  return true;
}
;
_.hashCode$ = function hashCode_16(){
  return toInt(xor(this.phone, shru(this.phone, 32)));
}
;
_.phone = {l:0, m:0, h:0};
var Lim_actor_model_viewmodel_UserPhone_2_classLit = createForClass('im.actor.model.viewmodel', 'UserPhone', 368, Ljava_lang_Object_2_classLit);
function UserPresence(){
}

function UserPresence_0(){
}

defineClass(276, 1, {}, UserPresence, UserPresence_0);
var Lim_actor_model_viewmodel_UserPresence_2_classLit = createForClass('im.actor.model.viewmodel', 'UserPresence', 276, Ljava_lang_Object_2_classLit);
function UserTypingVM(){
  this.userTyping = new ValueModel(($clinit_Boolean() , $clinit_Boolean() , FALSE));
}

defineClass(375, 1, {375:1}, UserTypingVM);
var Lim_actor_model_viewmodel_UserTypingVM_2_classLit = createForClass('im.actor.model.viewmodel', 'UserTypingVM', 375, Ljava_lang_Object_2_classLit);
function $buildPhones(records){
  var r, r$iterator, res;
  res = new ArrayList;
  for (r$iterator = records.iterator(); r$iterator.hasNext();) {
    r = dynamicCast(r$iterator.next(), 124);
    r.recordType == 0 && $add_0(res, new UserPhone(__parseAndValidateLong(r.recordData)));
  }
  return res;
}

function $subscribe_4(this$static, listener){
  if ($indexOf_0(this$static.listeners, listener, 0) != -1) {
    return;
  }
  $add_0(this$static.listeners, listener);
  $changeValue(listener.val$value2, {uid:this$static.id_0, name:dynamicCastToString(this$static.name_0.value_0), isContact:dynamicCast(this$static.isContact.value_0, 51).value_0});
}

function $updateValues_0(this$static, rawObj){
  var isChanged;
  isChanged = false;
  isChanged = isChanged | $change(this$static.name_0, rawObj.localName == null?rawObj.name_0:rawObj.localName);
  isChanged = isChanged | $change(this$static.avatar, rawObj.avatar);
  isChanged = isChanged | $change(this$static.phones, $buildPhones(rawObj.records));
  isChanged && $postToMainThread(new UserVM$1(this$static));
}

function UserVM(user, modules){
  this.listeners = new ArrayList;
  this.id_0 = user.uid;
  this.name_0 = new ValueModel(user.localName == null?user.name_0:user.localName);
  this.avatar = new ValueModel(user.avatar);
  this.isContact = new ValueModel(($clinit_Boolean() , $isUserContact(modules.contacts, this.id_0)?TRUE:FALSE));
  this.presence = new ValueModel(new UserPresence);
  this.phones = new ValueModel($buildPhones(user.records));
}

defineClass(53, 164, {164:1, 53:1}, UserVM);
_.updateValues = function updateValues_0(rawObj){
  $updateValues_0(this, dynamicCast(rawObj, 11));
}
;
_.id_0 = 0;
var Lim_actor_model_viewmodel_UserVM_2_classLit = createForClass('im.actor.model.viewmodel', 'UserVM', 53, Lim_actor_model_mvvm_BaseValueModel_2_classLit);
function UserVM$1(this$0){
  this.this$01 = this$0;
}

defineClass(616, 1, $intern_20, UserVM$1);
_.run = function run_22(){
  var l, l$array, l$index, l$max;
  for (l$array = dynamicCast($toArray_0(this.this$01.listeners, initDim(Lim_actor_model_mvvm_ModelChangedListener_2_classLit, $intern_27, 471, 0, 0, 1)), 472) , l$index = 0 , l$max = l$array.length; l$index < l$max; ++l$index) {
    l = l$array[l$index];
    l.onChanged_0(this.this$01);
  }
}
;
var Lim_actor_model_viewmodel_UserVM$1_2_classLit = createForClass('im.actor.model.viewmodel', 'UserVM/1', 616, Ljava_lang_Object_2_classLit);
function IOException(){
  Exception.call(this);
}

function IOException_0(message){
  Exception_0.call(this, message);
}

defineClass(5, 14, {5:1, 3:1, 14:1, 13:1}, IOException, IOException_0);
var Ljava_io_IOException_2_classLit = createForClass('java.io', 'IOException', 5, Ljava_lang_Exception_2_classLit);
function UnsupportedEncodingException(msg){
  IOException_0.call(this, msg);
}

defineClass(355, 5, {5:1, 3:1, 355:1, 14:1, 13:1}, UnsupportedEncodingException);
var Ljava_io_UnsupportedEncodingException_2_classLit = createForClass('java.io', 'UnsupportedEncodingException', 355, Ljava_io_IOException_2_classLit);
function AbstractStringBuilder(string){
  this.string = string;
}

defineClass(338, 1, {});
_.toString$ = function toString_176(){
  return this.string;
}
;
var Ljava_lang_AbstractStringBuilder_2_classLit = createForClass('java.lang', 'AbstractStringBuilder', 338, Ljava_lang_Object_2_classLit);
function ArithmeticException(){
  RuntimeException_0.call(this, 'divide by zero');
}

defineClass(482, 10, $intern_2, ArithmeticException);
var Ljava_lang_ArithmeticException_2_classLit = createForClass('java.lang', 'ArithmeticException', 482, Ljava_lang_RuntimeException_2_classLit);
function ArrayStoreException(){
  RuntimeException.call(this);
}

function ArrayStoreException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(151, 10, $intern_2, ArrayStoreException, ArrayStoreException_0);
var Ljava_lang_ArrayStoreException_2_classLit = createForClass('java.lang', 'ArrayStoreException', 151, Ljava_lang_RuntimeException_2_classLit);
function $clinit_Boolean(){
  $clinit_Boolean = emptyMethod;
  FALSE = new Boolean_0(false);
  TRUE = new Boolean_0(true);
}

function $compareTo_0(this$static, b){
  return compare_3(this$static.value_0, b.value_0);
}

function Boolean_0(value_0){
  this.value_0 = value_0;
}

function compare_3(x_0, y_0){
  return x_0 == y_0?0:x_0?1:-1;
}

defineClass(51, 1, {3:1, 51:1, 29:1}, Boolean_0);
_.compareTo = function compareTo_0(b){
  return $compareTo_0(this, dynamicCast(b, 51));
}
;
_.equals$ = function equals_21(o){
  return instanceOf(o, 51) && dynamicCast(o, 51).value_0 == this.value_0;
}
;
_.hashCode$ = function hashCode_17(){
  return this.value_0?1231:1237;
}
;
_.toString$ = function toString_177(){
  return '' + this.value_0;
}
;
_.value_0 = false;
var FALSE, TRUE;
var Ljava_lang_Boolean_2_classLit = createForClass('java.lang', 'Boolean', 51, Ljava_lang_Object_2_classLit);
function codePointAt(cs, index_0, limit){
  var hiSurrogate, loSurrogate;
  hiSurrogate = $charAt(cs, index_0++);
  if (hiSurrogate >= 55296 && hiSurrogate <= 56319 && index_0 < limit && isLowSurrogate(loSurrogate = cs.charCodeAt(index_0))) {
    return $intern_28 + ((hiSurrogate & 1023) << 10) + (loSurrogate & 1023);
  }
  return hiSurrogate;
}

function digit(c){
  if (c >= 48 && c < 58) {
    return c - 48;
  }
  if (c >= 97 && c < 97) {
    return c - 97 + 10;
  }
  if (c >= 65 && c < 65) {
    return c - 65 + 10;
  }
  return -1;
}

function isLowSurrogate(ch_0){
  return ch_0 >= 56320 && ch_0 <= 57343;
}

function toChars(codePoint, dst, dstIndex){
  checkCriticalArgument(codePoint >= 0 && codePoint <= 1114111);
  if (codePoint >= $intern_28) {
    dst[dstIndex++] = 55296 + (codePoint - $intern_28 >> 10 & 1023) & $intern_5;
    dst[dstIndex] = 56320 + (codePoint - $intern_28 & 1023) & $intern_5;
    return 2;
  }
   else {
    dst[dstIndex] = codePoint & $intern_5;
    return 1;
  }
}

function ClassCastException(){
  RuntimeException.call(this);
}

defineClass(117, 10, $intern_2, ClassCastException);
var Ljava_lang_ClassCastException_2_classLit = createForClass('java.lang', 'ClassCastException', 117, Ljava_lang_RuntimeException_2_classLit);
function __parseAndValidateInt(s){
  var i_0, isTooLow, length_0, startIndex, toReturn;
  if (s == null) {
    throw new NumberFormatException('null');
  }
  length_0 = s.length;
  startIndex = length_0 > 0 && (s.charCodeAt(0) == 45 || s.charCodeAt(0) == 43)?1:0;
  for (i_0 = startIndex; i_0 < length_0; i_0++) {
    if (digit(s.charCodeAt(i_0)) == -1) {
      throw new NumberFormatException('For input string: "' + s + '"');
    }
  }
  toReturn = parseInt(s, 10);
  isTooLow = toReturn < $intern_29;
  if (isNaN(toReturn)) {
    throw new NumberFormatException('For input string: "' + s + '"');
  }
   else if (isTooLow || toReturn > $intern_0) {
    throw new NumberFormatException('For input string: "' + s + '"');
  }
  return toReturn;
}

function __parseAndValidateLong(s){
  var c, firstTime, head, i_0, length_0, maxDigits, minValue, negative, orig, radixPower, toReturn;
  if (s == null) {
    throw new NumberFormatException('null');
  }
  orig = s;
  length_0 = s.length;
  negative = false;
  if (length_0 > 0) {
    c = s.charCodeAt(0);
    if (c == 45 || c == 43) {
      s = __substr(s, 1, s.length - 1);
      --length_0;
      negative = c == 45;
    }
  }
  if (length_0 == 0) {
    throw new NumberFormatException('For input string: "' + orig + '"');
  }
  while (s.length > 0 && s.charCodeAt(0) == 48) {
    s = __substr(s, 1, s.length - 1);
    --length_0;
  }
  if (length_0 > ($clinit_Number$__ParseLong() , maxLengthForRadix)[10]) {
    throw new NumberFormatException('For input string: "' + orig + '"');
  }
  for (i_0 = 0; i_0 < length_0; i_0++) {
    if (digit(s.charCodeAt(i_0)) == -1) {
      throw new NumberFormatException('For input string: "' + orig + '"');
    }
  }
  toReturn = {l:0, m:0, h:0};
  maxDigits = maxDigitsForRadix[10];
  radixPower = fromInt(maxDigitsRadixPower[10]);
  minValue = neg(maxValueForRadix[10]);
  firstTime = true;
  head = length_0 % maxDigits;
  if (head > 0) {
    toReturn = fromInt(-__parseInt(s.substr(0, head), 10));
    s = __substr(s, head, s.length - head);
    length_0 -= head;
    firstTime = false;
  }
  while (length_0 >= maxDigits) {
    head = __parseInt(s.substr(0, maxDigits), 10);
    s = __substr(s, maxDigits, s.length - maxDigits);
    length_0 -= maxDigits;
    if (firstTime) {
      firstTime = false;
    }
     else {
      if (lt(toReturn, minValue)) {
        throw new NumberFormatException('For input string: "' + orig + '"');
      }
      toReturn = mul(toReturn, radixPower);
    }
    toReturn = sub_0(toReturn, fromInt(head));
  }
  if (gt(toReturn, {l:0, m:0, h:0})) {
    throw new NumberFormatException('For input string: "' + orig + '"');
  }
  if (!negative) {
    toReturn = neg(toReturn);
    if (lt(toReturn, {l:0, m:0, h:0})) {
      throw new NumberFormatException('For input string: "' + orig + '"');
    }
  }
  return toReturn;
}

function __parseInt(s, radix){
  return parseInt(s, radix);
}

defineClass(152, 1, {3:1, 152:1});
var Ljava_lang_Number_2_classLit = createForClass('java.lang', 'Number', 152, Ljava_lang_Object_2_classLit);
function $clinit_Double(){
  $clinit_Double = emptyMethod;
  powers = initValues(getClassLiteralForArray(D_classLit, 1), $intern_4, 0, 7, [1.3407807929942597E154, 1.157920892373162E77, 3.4028236692093846E38, 1.8446744073709552E19, 4294967296, $intern_28, 256, 16, 4, 2]);
  invPowers = initValues(getClassLiteralForArray(D_classLit, 1), $intern_4, 0, 7, [7.458340731200207E-155, 8.636168555094445E-78, 2.9387358770557188E-39, 5.421010862427522E-20, 2.3283064365386963E-10, $intern_30, 0.00390625, 0.0625, 0.25, 0.5]);
}

function $compareTo_1(this$static, b){
  return compare_4(this$static.value_0, b.value_0);
}

function Double(value_0){
  $clinit_Double();
  this.value_0 = value_0;
}

function compare_4(x_0, y_0){
  if (x_0 < y_0) {
    return -1;
  }
  if (x_0 > y_0) {
    return 1;
  }
  if (x_0 == y_0) {
    return 0;
  }
  return isNaN_0(x_0)?isNaN_0(y_0)?0:1:-1;
}

function doubleToLongBits(value_0){
  $clinit_Double();
  var bit, exp_0, i_0, ihi, ilo, negative;
  if (isNaN_0(value_0)) {
    return {l:0, m:0, h:524160};
  }
  negative = false;
  if (value_0 == 0) {
    return 1 / value_0 == -Infinity?{l:0, m:0, h:$intern_9}:{l:0, m:0, h:0};
  }
  if (value_0 < 0) {
    negative = true;
    value_0 = -value_0;
  }
  if (!isFinite(value_0) && !isNaN(value_0)) {
    return negative?{l:0, m:0, h:1048320}:{l:0, m:0, h:524032};
  }
  exp_0 = 0;
  if (value_0 < 1) {
    bit = 512;
    for (i_0 = 0; i_0 < 10; ++i_0 , bit >>= 1) {
      if (value_0 < invPowers[i_0] && exp_0 - bit >= -1023) {
        value_0 *= powers[i_0];
        exp_0 -= bit;
      }
    }
    if (value_0 < 1 && exp_0 - 1 >= -1023) {
      value_0 *= 2;
      --exp_0;
    }
  }
   else if (value_0 >= 2) {
    bit = 512;
    for (i_0 = 0; i_0 < 10; ++i_0 , bit >>= 1) {
      if (value_0 >= powers[i_0]) {
        value_0 *= invPowers[i_0];
        exp_0 += bit;
      }
    }
  }
  exp_0 > -1023?(value_0 -= 1):(value_0 *= 0.5);
  ihi = fromDouble(value_0 * $intern_18);
  value_0 -= toDouble(ihi) * $intern_31;
  ilo = fromDouble(value_0 * 4503599627370496);
  ihi = or(ihi, fromInt(exp_0 + 1023 << 20));
  negative && (ihi = or(ihi, {l:0, m:512, h:0}));
  return or(shl(ihi, 32), ilo);
}

function isNaN_0(x_0){
  $clinit_Double();
  return isNaN(x_0);
}

function longBitsToDouble(bits){
  $clinit_Double();
  var bit, d, d0, exp_0, i_0, ihi, ilo, negative;
  ihi = shr(bits, 32);
  ilo = and(bits, {l:$intern_7, m:1023, h:0});
  lt(ihi, {l:0, m:0, h:0}) && (ihi = add_0(ihi, {l:0, m:1024, h:0}));
  lt(ilo, {l:0, m:0, h:0}) && (ilo = add_0(ilo, {l:0, m:1024, h:0}));
  negative = neq(and(ihi, {l:0, m:4193792, h:$intern_8}), {l:0, m:0, h:0});
  exp_0 = toInt(and(shr(ihi, 20), {l:2047, m:0, h:0}));
  ihi = and(ihi, {l:$intern_8, m:0, h:0});
  if (exp_0 == 0) {
    d0 = toDouble(ihi) * $intern_31 + toDouble(ilo) * $intern_32;
    d0 *= 2.2250738585072014E-308;
    return negative?d0 == 0?-0.:-d0:d0;
  }
   else if (exp_0 == 2047) {
    return eq(ihi, {l:0, m:0, h:0}) && eq(ilo, {l:0, m:0, h:0})?negative?-Infinity:Infinity:NaN;
  }
  exp_0 -= 1023;
  d = 1 + toDouble(ihi) * $intern_31 + toDouble(ilo) * $intern_32;
  if (exp_0 > 0) {
    bit = 512;
    for (i_0 = 0; i_0 < 10; ++i_0 , bit >>= 1) {
      if (exp_0 >= bit) {
        d *= powers[i_0];
        exp_0 -= bit;
      }
    }
  }
   else if (exp_0 < 0) {
    while (exp_0 < 0) {
      bit = 512;
      for (i_0 = 0; i_0 < 10; ++i_0 , bit >>= 1) {
        if (exp_0 <= -bit) {
          d *= invPowers[i_0];
          exp_0 += bit;
        }
      }
    }
  }
  return negative?-d:d;
}

defineClass(93, 152, {3:1, 29:1, 93:1, 152:1}, Double);
_.compareTo = function compareTo_1(b){
  return $compareTo_1(this, dynamicCast(b, 93));
}
;
_.equals$ = function equals_22(o){
  return instanceOf(o, 93) && dynamicCast(o, 93).value_0 == this.value_0;
}
;
_.hashCode$ = function hashCode_18(){
  return round_int(this.value_0);
}
;
_.toString$ = function toString_179(){
  return '' + this.value_0;
}
;
_.value_0 = 0;
var invPowers, powers;
var Ljava_lang_Double_2_classLit = createForClass('java.lang', 'Double', 93, Ljava_lang_Number_2_classLit);
function IllegalArgumentException(){
  RuntimeException.call(this);
}

function IllegalArgumentException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(15, 10, $intern_2, IllegalArgumentException, IllegalArgumentException_0);
var Ljava_lang_IllegalArgumentException_2_classLit = createForClass('java.lang', 'IllegalArgumentException', 15, Ljava_lang_RuntimeException_2_classLit);
function IllegalStateException(){
  RuntimeException.call(this);
}

defineClass(529, 10, $intern_2, IllegalStateException);
var Ljava_lang_IllegalStateException_2_classLit = createForClass('java.lang', 'IllegalStateException', 529, Ljava_lang_RuntimeException_2_classLit);
function IndexOutOfBoundsException(){
  RuntimeException.call(this);
}

function IndexOutOfBoundsException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(160, 10, $intern_2, IndexOutOfBoundsException, IndexOutOfBoundsException_0);
var Ljava_lang_IndexOutOfBoundsException_2_classLit = createForClass('java.lang', 'IndexOutOfBoundsException', 160, Ljava_lang_RuntimeException_2_classLit);
function $compareTo_2(this$static, b){
  return compare_5(this$static.value_0, b.value_0);
}

function Integer(value_0){
  this.value_0 = value_0;
}

function compare_5(x_0, y_0){
  return x_0 < y_0?-1:x_0 > y_0?1:0;
}

function numberOfLeadingZeros_0(i_0){
  var m, n, y_0;
  if (i_0 < 0) {
    return 0;
  }
   else if (i_0 == 0) {
    return 32;
  }
   else {
    y_0 = -(i_0 >> 16);
    m = y_0 >> 16 & 16;
    n = 16 - m;
    i_0 = i_0 >> m;
    y_0 = i_0 - 256;
    m = y_0 >> 16 & 8;
    n += m;
    i_0 <<= m;
    y_0 = i_0 - 4096;
    m = y_0 >> 16 & 4;
    n += m;
    i_0 <<= m;
    y_0 = i_0 - 16384;
    m = y_0 >> 16 & 2;
    n += m;
    i_0 <<= m;
    y_0 = i_0 >> 14;
    m = y_0 & ~(y_0 >> 1);
    return n + 2 - m;
  }
}

function numberOfTrailingZeros(i_0){
  var r, rtn;
  if (i_0 == 0) {
    return 32;
  }
   else {
    rtn = 0;
    for (r = 1; (r & i_0) == 0; r <<= 1) {
      ++rtn;
    }
    return rtn;
  }
}

function toUnsignedRadixString(value_0, radix){
  return (value_0 >>> 0).toString(radix);
}

function valueOf(i_0){
  var rebase, result;
  if (i_0 > -129 && i_0 < 128) {
    rebase = i_0 + 128;
    result = ($clinit_Integer$BoxedValues() , boxedValues_0)[rebase];
    !result && (result = boxedValues_0[rebase] = new Integer(i_0));
    return result;
  }
  return new Integer(i_0);
}

defineClass(33, 152, {3:1, 29:1, 33:1, 152:1}, Integer);
_.compareTo = function compareTo_2(b){
  return $compareTo_2(this, dynamicCast(b, 33));
}
;
_.equals$ = function equals_23(o){
  return instanceOf(o, 33) && dynamicCast(o, 33).value_0 == this.value_0;
}
;
_.hashCode$ = function hashCode_19(){
  return this.value_0;
}
;
_.toString$ = function toString_180(){
  return '' + this.value_0;
}
;
_.value_0 = 0;
var Ljava_lang_Integer_2_classLit = createForClass('java.lang', 'Integer', 33, Ljava_lang_Number_2_classLit);
function $clinit_Integer$BoxedValues(){
  $clinit_Integer$BoxedValues = emptyMethod;
  boxedValues_0 = initDim(Ljava_lang_Integer_2_classLit, $intern_22, 33, 256, 0, 1);
}

var boxedValues_0;
function $compareTo_3(this$static, b){
  return compare_6(this$static.value_0, b.value_0);
}

function Long(value_0){
  this.value_0 = value_0;
}

function compare_6(x_0, y_0){
  return lt(x_0, y_0)?-1:gt(x_0, y_0)?1:0;
}

function valueOf_0(i_0){
  var rebase, result;
  if (gt(i_0, {l:4194175, m:$intern_7, h:$intern_8}) && lt(i_0, {l:128, m:0, h:0})) {
    rebase = toInt(i_0) + 128;
    result = ($clinit_Long$BoxedValues() , boxedValues_1)[rebase];
    !result && (result = boxedValues_1[rebase] = new Long(i_0));
    return result;
  }
  return new Long(i_0);
}

defineClass(23, 152, {3:1, 29:1, 23:1, 152:1}, Long);
_.compareTo = function compareTo_3(b){
  return $compareTo_3(this, dynamicCast(b, 23));
}
;
_.equals$ = function equals_24(o){
  return instanceOf(o, 23) && eq(dynamicCast(o, 23).value_0, this.value_0);
}
;
_.hashCode$ = function hashCode_20(){
  return toInt(this.value_0);
}
;
_.toString$ = function toString_181(){
  return '' + toString_2(this.value_0);
}
;
_.value_0 = {l:0, m:0, h:0};
var Ljava_lang_Long_2_classLit = createForClass('java.lang', 'Long', 23, Ljava_lang_Number_2_classLit);
function $clinit_Long$BoxedValues(){
  $clinit_Long$BoxedValues = emptyMethod;
  boxedValues_1 = initDim(Ljava_lang_Long_2_classLit, $intern_21, 23, 256, 0, 1);
}

var boxedValues_1;
function floor_0(x_0){
  return Math.floor(x_0);
}

function max_0(y_0){
  return 0 > y_0?0:y_0;
}

function max_1(x_0, y_0){
  return gt(x_0, y_0)?x_0:y_0;
}

function min_0(x_0, y_0){
  return x_0 < y_0?x_0:y_0;
}

function min_1(x_0, y_0){
  return lt(x_0, y_0)?x_0:y_0;
}

function pow_0(x_0, exp_0){
  return Math.pow(x_0, exp_0);
}

function NullPointerException(){
  RuntimeException.call(this);
}

function NullPointerException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(340, 10, $intern_2, NullPointerException, NullPointerException_0);
var Ljava_lang_NullPointerException_2_classLit = createForClass('java.lang', 'NullPointerException', 340, Ljava_lang_RuntimeException_2_classLit);
function $clinit_Number$__ParseLong(){
  $clinit_Number$__ParseLong = emptyMethod;
  var i_0;
  maxDigitsForRadix = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [-1, -1, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5]);
  maxDigitsRadixPower = initDim(I_classLit, $intern_4, 0, 37, 7, 1);
  maxLengthForRadix = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [-1, -1, 63, 40, 32, 28, 25, 23, 21, 20, 19, 19, 18, 18, 17, 17, 16, 16, 16, 15, 15, 15, 15, 14, 14, 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 13, 13]);
  maxValueForRadix = initDim(J_classLit, $intern_4, 0, 37, 6, 1);
  for (i_0 = 2; i_0 <= 36; i_0++) {
    maxDigitsRadixPower[i_0] = round_int(pow_0(i_0, maxDigitsForRadix[i_0]));
    maxValueForRadix[i_0] = div({l:$intern_7, m:$intern_7, h:524287}, fromInt(maxDigitsRadixPower[i_0]));
  }
}

var maxDigitsForRadix, maxDigitsRadixPower, maxLengthForRadix, maxValueForRadix;
function NumberFormatException(message){
  IllegalArgumentException_0.call(this, message);
}

defineClass(66, 15, $intern_2, NumberFormatException);
var Ljava_lang_NumberFormatException_2_classLit = createForClass('java.lang', 'NumberFormatException', 66, Ljava_lang_IllegalArgumentException_2_classLit);
function StackTraceElement(methodName, fileName, lineNumber){
  this.className = 'Unknown';
  this.methodName = methodName;
  this.fileName = fileName;
  this.lineNumber = lineNumber;
}

defineClass(80, 1, {3:1, 80:1}, StackTraceElement);
_.equals$ = function equals_25(other){
  var st;
  if (instanceOf(other, 80)) {
    st = dynamicCast(other, 80);
    return this.lineNumber == st.lineNumber && equals_38(this.methodName, st.methodName) && equals_38(this.className, st.className) && equals_38(this.fileName, st.fileName);
  }
  return false;
}
;
_.hashCode$ = function hashCode_21(){
  return hashCode_28(initValues(getClassLiteralForArray(Ljava_lang_Object_2_classLit, 1), $intern_6, 1, 3, [valueOf(this.lineNumber), this.className, this.methodName, this.fileName]));
}
;
_.toString$ = function toString_182(){
  return this.className + '.' + this.methodName + '(' + (this.fileName != null?this.fileName:'Unknown Source') + (this.lineNumber >= 0?':' + this.lineNumber:'') + ')';
}
;
_.lineNumber = 0;
var Ljava_lang_StackTraceElement_2_classLit = createForClass('java.lang', 'StackTraceElement', 80, Ljava_lang_Object_2_classLit);
function $charAt(this$static, index_0){
  return this$static.charCodeAt(index_0);
}

function $equals_3(this$static, other){
  return this$static === other;
}

function $equalsIgnoreCase(this$static, other){
  if (other == null) {
    return false;
  }
  if (this$static == other) {
    return true;
  }
  return this$static.length == other.length && this$static.toLowerCase() == other.toLowerCase();
}

function $getChars(this$static, srcEnd, dst, dstBegin){
  var srcIdx;
  for (srcIdx = 0; srcIdx < srcEnd; ++srcIdx) {
    dst[dstBegin++] = this$static.charCodeAt(srcIdx);
  }
}

function $indexOf(this$static, str){
  return this$static.indexOf(str);
}

function $lastIndexOf(this$static, str){
  return this$static.lastIndexOf(str);
}

function $lastIndexOf_0(this$static, str, start_0){
  return this$static.lastIndexOf(str, start_0);
}

function $replace(this$static, from, to){
  var regex, replacement;
  regex = $replaceAll(from, '([/\\\\\\.\\*\\+\\?\\|\\(\\)\\[\\]\\{\\}$^])', '\\\\$1');
  replacement = $replaceAll($replaceAll(to, '\\\\', '\\\\\\\\'), '\\$', '\\\\$');
  return $replaceAll(this$static, regex, replacement);
}

function $replaceAll(this$static, regex, replace){
  replace = __translateReplaceString(replace);
  return this$static.replace(RegExp(regex, 'g'), replace);
}

function $split(this$static, regex, maxMatch){
  var compiled = new RegExp(regex, 'g');
  var out = [];
  var count = 0;
  var trail = this$static;
  var lastTrail = null;
  while (true) {
    var matchObj = compiled.exec(trail);
    if (matchObj == null || trail == '' || count == maxMatch - 1 && maxMatch > 0) {
      out[count] = trail;
      break;
    }
     else {
      out[count] = trail.substring(0, matchObj.index);
      trail = trail.substring(matchObj.index + matchObj[0].length, trail.length);
      compiled.lastIndex = 0;
      if (lastTrail == trail) {
        out[count] = trail.substring(0, 1);
        trail = trail.substring(1);
      }
      lastTrail = trail;
      count++;
    }
  }
  if (maxMatch == 0 && this$static.length > 0) {
    var lastNonEmpty = out.length;
    while (lastNonEmpty > 0 && out[lastNonEmpty - 1] == '') {
      --lastNonEmpty;
    }
    lastNonEmpty < out.length && out.splice(lastNonEmpty, out.length - lastNonEmpty);
  }
  var jr = __createArray(out.length);
  for (var i_0 = 0; i_0 < out.length; ++i_0) {
    jr[i_0] = out[i_0];
  }
  return jr;
}

function $substring(this$static, beginIndex){
  return __substr(this$static, beginIndex, this$static.length - beginIndex);
}

function $substring_0(this$static, beginIndex, endIndex){
  return this$static.substr(beginIndex, endIndex - beginIndex);
}

function $trim(this$static){
  if (this$static.length == 0 || this$static[0] > ' ' && this$static[this$static.length - 1] > ' ') {
    return this$static;
  }
  return this$static.replace(/^[\u0000-\u0020]*|[\u0000-\u0020]*$/g, '');
}

function _String(bytes, len, charset){
  if ($equalsIgnoreCase('UTF-8', charset)) {
    return utf8ToString(bytes, len);
  }
   else if ($equalsIgnoreCase('ISO-8859-1', charset) || $equalsIgnoreCase('ISO-LATIN-1', charset)) {
    return latin1ToString(bytes, len);
  }
   else {
    throw new UnsupportedEncodingException('Charset ' + charset + ' not supported');
  }
}

function _String_0(bytes){
  return _String(bytes, bytes.length, 'UTF-8');
}

function __checkBounds(legalCount, start_0, end){
  if (start_0 < 0) {
    throw new StringIndexOutOfBoundsException(start_0);
  }
  if (end < start_0) {
    throw new StringIndexOutOfBoundsException(end - start_0);
  }
  if (end > legalCount) {
    throw new StringIndexOutOfBoundsException(end);
  }
}

function __createArray(numElements){
  return initDim(Ljava_lang_String_2_classLit, $intern_6, 2, numElements, 4, 1);
}

function __substr(str, beginIndex, len){
  return str.substr(beginIndex, len);
}

function __translateReplaceString(replaceStr){
  var pos;
  pos = 0;
  while (0 <= (pos = replaceStr.indexOf('\\', pos))) {
    replaceStr.charCodeAt(pos + 1) == 36?(replaceStr = replaceStr.substr(0, pos) + '$' + $substring(replaceStr, ++pos)):(replaceStr = replaceStr.substr(0, pos) + $substring(replaceStr, ++pos));
  }
  return replaceStr;
}

function __valueOf(x_0, start_0, end){
  var s = '';
  for (var batchStart = start_0; batchStart < end;) {
    var batchEnd = Math.min(batchStart + 10000, end);
    s += String.fromCharCode.apply(null, x_0.slice(batchStart, batchEnd));
    batchStart = batchEnd;
  }
  return s;
}

function compareTo_4(thisStr, otherStr){
  if (thisStr == otherStr) {
    return 0;
  }
  return thisStr < otherStr?-1:1;
}

function compareTo_Ljava_lang_Object__I__devirtual$(this$static, other){
  return isJavaString(this$static)?compareTo_4(this$static, dynamicCastToString(other)):this$static.compareTo(other);
}

function encodeUtf8(bytes, ofs, codePoint){
  if (codePoint < 128) {
    bytes[ofs] = narrow_byte(codePoint & 127);
    return 1;
  }
   else if (codePoint < 2048) {
    bytes[ofs++] = narrow_byte(codePoint >> 6 & 31 | 192);
    bytes[ofs] = narrow_byte(codePoint & 63 | 128);
    return 2;
  }
   else if (codePoint < $intern_28) {
    bytes[ofs++] = narrow_byte(codePoint >> 12 & 15 | 224);
    bytes[ofs++] = narrow_byte(codePoint >> 6 & 63 | 128);
    bytes[ofs] = narrow_byte(codePoint & 63 | 128);
    return 3;
  }
   else if (codePoint < 2097152) {
    bytes[ofs++] = narrow_byte(codePoint >> 18 & 7 | 240);
    bytes[ofs++] = narrow_byte(codePoint >> 12 & 63 | 128);
    bytes[ofs++] = narrow_byte(codePoint >> 6 & 63 | 128);
    bytes[ofs] = narrow_byte(codePoint & 63 | 128);
    return 4;
  }
   else if (codePoint < 67108864) {
    bytes[ofs++] = narrow_byte(codePoint >> 24 & 3 | 248);
    bytes[ofs++] = narrow_byte(codePoint >> 18 & 63 | 128);
    bytes[ofs++] = narrow_byte(codePoint >> 12 & 63 | 128);
    bytes[ofs++] = narrow_byte(codePoint >> 6 & 63 | 128);
    bytes[ofs] = narrow_byte(codePoint & 63 | 128);
    return 5;
  }
  throw new IllegalArgumentException_0('Character out of range: ' + codePoint);
}

function fromCodePoint(codePoint){
  var hiSurrogate, loSurrogate;
  if (codePoint >= $intern_28) {
    hiSurrogate = 55296 + (codePoint - $intern_28 >> 10 & 1023) & $intern_5;
    loSurrogate = 56320 + (codePoint - $intern_28 & 1023) & $intern_5;
    return valueOf_1(hiSurrogate) + valueOf_1(loSurrogate);
  }
   else {
    return String.fromCharCode(codePoint & $intern_5);
  }
}

function getBytesUtf8(str){
  var byteCount, bytes, ch_0, i_0, i0, n, out;
  n = str.length;
  byteCount = 0;
  for (i0 = 0; i0 < n;) {
    ch_0 = codePointAt(str, i0, str.length);
    i0 += ch_0 >= $intern_28?2:1;
    ch_0 < 128?++byteCount:ch_0 < 2048?(byteCount += 2):ch_0 < $intern_28?(byteCount += 3):ch_0 < 2097152?(byteCount += 4):ch_0 < 67108864 && (byteCount += 5);
  }
  bytes = initDim(B_classLit, $intern_17, 0, byteCount, 7, 1);
  out = 0;
  for (i_0 = 0; i_0 < n;) {
    ch_0 = codePointAt(str, i_0, str.length);
    i_0 += ch_0 >= $intern_28?2:1;
    out += encodeUtf8(bytes, out, ch_0);
  }
  return bytes;
}

function latin1ToString(bytes, len){
  var chars, i_0;
  chars = initDim(C_classLit, $intern_4, 0, len, 7, 1);
  for (i_0 = 0; i_0 < len; ++i_0) {
    chars[i_0] = bytes[i_0] & 255 & $intern_5;
  }
  return __valueOf(chars, 0, chars.length);
}

function utf8ToString(bytes, len){
  var b, ch_0, charCount, chars, count, i_0, i0, outIdx;
  charCount = 0;
  for (i0 = 0; i0 < len;) {
    ++charCount;
    ch_0 = bytes[i0];
    if ((ch_0 & 192) == 128) {
      throw new IllegalArgumentException_0('Invalid UTF8 sequence');
    }
     else if ((ch_0 & 128) == 0) {
      ++i0;
    }
     else if ((ch_0 & 224) == 192) {
      i0 += 2;
    }
     else if ((ch_0 & 240) == 224) {
      i0 += 3;
    }
     else if ((ch_0 & 248) == 240) {
      i0 += 4;
    }
     else {
      throw new IllegalArgumentException_0('Invalid UTF8 sequence');
    }
    if (i0 > len) {
      throw new IndexOutOfBoundsException_0('Invalid UTF8 sequence');
    }
  }
  chars = initDim(C_classLit, $intern_4, 0, charCount, 7, 1);
  outIdx = 0;
  count = 0;
  for (i_0 = 0; i_0 < len;) {
    ch_0 = bytes[i_0++];
    if ((ch_0 & 128) == 0) {
      count = 1;
      ch_0 &= 127;
    }
     else if ((ch_0 & 224) == 192) {
      count = 2;
      ch_0 &= 31;
    }
     else if ((ch_0 & 240) == 224) {
      count = 3;
      ch_0 &= 15;
    }
     else if ((ch_0 & 248) == 240) {
      count = 4;
      ch_0 &= 7;
    }
     else if ((ch_0 & 252) == 248) {
      count = 5;
      ch_0 &= 3;
    }
    while (--count > 0) {
      b = bytes[i_0++];
      if ((b & 192) != 128) {
        throw new IllegalArgumentException_0('Invalid UTF8 sequence at ' + (i_0 - 1) + ', byte=' + toUnsignedRadixString(b, 16));
      }
      ch_0 = ch_0 << 6 | b & 63;
    }
    outIdx += toChars(ch_0, chars, outIdx);
  }
  return __valueOf(chars, 0, chars.length);
}

function valueOf_1(x_0){
  return String.fromCharCode(x_0);
}

function valueOf_2(x_0){
  return __valueOf(x_0, 0, x_0.length);
}

function valueOf_3(x_0, offset, count){
  var end;
  end = offset + count;
  __checkBounds(x_0.length, offset, end);
  return __valueOf(x_0, offset, end);
}

var Ljava_lang_String_2_classLit = createForClass('java.lang', 'String', 2, Ljava_lang_Object_2_classLit);
function $clinit_String$HashCache(){
  $clinit_String$HashCache = emptyMethod;
  back_0 = {};
  front = {};
}

function compute(str){
  var hashCode, i_0, n, nBatch;
  hashCode = 0;
  n = str.length;
  nBatch = n - 4;
  i_0 = 0;
  while (i_0 < nBatch) {
    hashCode = str.charCodeAt(i_0 + 3) + 31 * (str.charCodeAt(i_0 + 2) + 31 * (str.charCodeAt(i_0 + 1) + 31 * (str.charCodeAt(i_0) + 31 * hashCode)));
    hashCode = ~~hashCode;
    i_0 += 4;
  }
  while (i_0 < n) {
    hashCode = hashCode * 31 + $charAt(str, i_0++);
  }
  hashCode = ~~hashCode;
  return hashCode;
}

function getHashCode_0(str){
  $clinit_String$HashCache();
  var key = ':' + str;
  var result = front[key];
  if (result != null) {
    return result;
  }
  result = back_0[key];
  result == null && (result = compute(str));
  increment();
  return front[key] = result;
}

function increment(){
  if (count_0 == 256) {
    back_0 = front;
    front = {};
    count_0 = 0;
  }
  ++count_0;
}

var back_0, count_0 = 0, front;
function $append(this$static, x_0){
  this$static.string += charToString(x_0);
  return this$static;
}

function $append_0(this$static, x_0){
  this$static.string += x_0;
  return this$static;
}

function $append_1(this$static, x_0){
  this$static.string += x_0;
  return this$static;
}

function StringBuilder(){
  AbstractStringBuilder.call(this, '');
}

function StringBuilder_0(){
  AbstractStringBuilder.call(this, '');
}

function StringBuilder_1(s){
  AbstractStringBuilder.call(this, s);
}

defineClass(64, 338, {801:1}, StringBuilder, StringBuilder_0, StringBuilder_1);
var Ljava_lang_StringBuilder_2_classLit = createForClass('java.lang', 'StringBuilder', 64, Ljava_lang_AbstractStringBuilder_2_classLit);
function StringIndexOutOfBoundsException(index_0){
  IndexOutOfBoundsException_0.call(this, 'String index out of range: ' + index_0);
}

defineClass(359, 160, $intern_2, StringIndexOutOfBoundsException);
var Ljava_lang_StringIndexOutOfBoundsException_2_classLit = createForClass('java.lang', 'StringIndexOutOfBoundsException', 359, Ljava_lang_IndexOutOfBoundsException_2_classLit);
function $clinit_System(){
  $clinit_System = emptyMethod;
  err = new PrintStream;
}

function arraycopy(src_0, srcOfs, dest, destOfs, len){
  $clinit_System();
  var destArray, destComp, destEnd, destType, destlen, srcArray, srcComp, srcType, srclen;
  checkNotNull_0(src_0, 'src');
  checkNotNull_0(dest, 'dest');
  srcType = getClass__Ljava_lang_Class___devirtual$(src_0);
  destType = getClass__Ljava_lang_Class___devirtual$(dest);
  checkArrayType((srcType.modifiers & 4) != 0, 'srcType is not an array');
  checkArrayType((destType.modifiers & 4) != 0, 'destType is not an array');
  srcComp = srcType.componentType;
  destComp = destType.componentType;
  checkArrayType((srcComp.modifiers & 1) != 0?srcComp == destComp:(destComp.modifiers & 1) == 0, "Array types don't match");
  srclen = src_0.length;
  destlen = dest.length;
  if (srcOfs < 0 || destOfs < 0 || len < 0 || srcOfs + len > srclen || destOfs + len > destlen) {
    throw new IndexOutOfBoundsException;
  }
  if (((srcComp.modifiers & 1) == 0 || (srcComp.modifiers & 4) != 0) && srcType != destType) {
    srcArray = dynamicCast(src_0, 6);
    destArray = dynamicCast(dest, 6);
    if (maskUndefined(src_0) === maskUndefined(dest) && srcOfs < destOfs) {
      srcOfs += len;
      for (destEnd = destOfs + len; destEnd-- > destOfs;) {
        setCheck(destArray, destEnd, srcArray[--srcOfs]);
      }
    }
     else {
      for (destEnd = destOfs + len; destOfs < destEnd;) {
        setCheck(destArray, destOfs++, srcArray[srcOfs++]);
      }
    }
  }
   else 
    len > 0 && nativeArraySplice(src_0, srcOfs, dest, destOfs, len, true);
}

var err;
function UnsupportedOperationException(){
  RuntimeException.call(this);
}

function UnsupportedOperationException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(67, 10, $intern_2, UnsupportedOperationException, UnsupportedOperationException_0);
var Ljava_lang_UnsupportedOperationException_2_classLit = createForClass('java.lang', 'UnsupportedOperationException', 67, Ljava_lang_RuntimeException_2_classLit);
function $clinit_BigInteger(){
  $clinit_BigInteger = emptyMethod;
  var i_0;
  ONE_0 = new BigInteger(1, 1);
  TEN = new BigInteger(1, 10);
  ZERO_0 = new BigInteger(0, 0);
  MINUS_ONE = new BigInteger(-1, 1);
  SMALL_VALUES = initValues(getClassLiteralForArray(Ljava_math_BigInteger_2_classLit, 1), $intern_6, 17, 0, [ZERO_0, ONE_0, new BigInteger(1, 2), new BigInteger(1, 3), new BigInteger(1, 4), new BigInteger(1, 5), new BigInteger(1, 6), new BigInteger(1, 7), new BigInteger(1, 8), new BigInteger(1, 9), TEN]);
  TWO_POWS = initDim(Ljava_math_BigInteger_2_classLit, $intern_6, 17, 32, 0, 1);
  for (i_0 = 0; i_0 < TWO_POWS.length; i_0++) {
    TWO_POWS[i_0] = valueOf_4(shl({l:1, m:0, h:0}, i_0));
  }
}

function $$init_0(this$static){
}

function $compareTo_4(this$static, val){
  if (this$static.sign > val.sign) {
    return 1;
  }
  if (this$static.sign < val.sign) {
    return -1;
  }
  if (this$static.numberLength > val.numberLength) {
    return this$static.sign;
  }
  if (this$static.numberLength < val.numberLength) {
    return -val.sign;
  }
  return this$static.sign * compareArrays(this$static.digits, val.digits, this$static.numberLength);
}

function $cutOffLeadingZeroes(this$static){
  while (this$static.numberLength > 0 && this$static.digits[--this$static.numberLength] == 0)
  ;
  this$static.digits[this$static.numberLength++] == 0 && (this$static.sign = 0);
}

function $equalsArrays(this$static, b){
  var i_0;
  for (i_0 = this$static.numberLength - 1; i_0 >= 0 && this$static.digits[i_0] == b[i_0]; i_0--)
  ;
  return i_0 < 0;
}

function $multiply(this$static, val){
  if (val.sign == 0) {
    return ZERO_0;
  }
  if (this$static.sign == 0) {
    return ZERO_0;
  }
  return $clinit_Multiplication() , karatsuba(this$static, val);
}

function $shiftLeft(this$static, n){
  if (n == 0 || this$static.sign == 0) {
    return this$static;
  }
  return n > 0?shiftLeft(this$static, n):shiftRight(this$static, -n);
}

function $shiftRight(this$static, n){
  if (n == 0 || this$static.sign == 0) {
    return this$static;
  }
  return n > 0?shiftRight(this$static, n):shiftLeft(this$static, -n);
}

function BigInteger(sign, value_0){
  $clinit_BigInteger();
  $$init_0(this);
  this.sign = sign;
  this.numberLength = 1;
  this.digits = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [value_0]);
}

function BigInteger_0(sign, numberLength, digits){
  $clinit_BigInteger();
  $$init_0(this);
  this.sign = sign;
  this.numberLength = numberLength;
  this.digits = digits;
}

function BigInteger_1(sign, val){
  $$init_0(this);
  this.sign = sign;
  if (eq(and(val, {l:0, m:4193280, h:$intern_8}), {l:0, m:0, h:0})) {
    this.numberLength = 1;
    this.digits = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [toInt(val)]);
  }
   else {
    this.numberLength = 2;
    this.digits = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [toInt(val), toInt(shr(val, 32))]);
  }
}

function BigInteger_2(){
  $clinit_BigInteger();
  checkNotNull('3');
  setFromString(this);
}

function setFromString(bi){
  var bigRadix, bigRadixDigit, bigRadixDigitsLength, charsPerInt, digitIndex, digits, newDigit, substrEnd, substrStart, topChars;
  charsPerInt = ($clinit_Conversion() , digitFitInInt)[10];
  bigRadixDigitsLength = ~~(1 / charsPerInt);
  topChars = 1 % charsPerInt;
  topChars != 0 && ++bigRadixDigitsLength;
  digits = initDim(I_classLit, $intern_4, 0, bigRadixDigitsLength, 7, 1);
  bigRadix = bigRadices[8];
  digitIndex = 0;
  substrEnd = topChars == 0?charsPerInt:topChars;
  for (substrStart = 0; substrStart < 1; substrStart = substrEnd , substrEnd = substrEnd + charsPerInt) {
    bigRadixDigit = __parseAndValidateInt('3'.substr(substrStart, substrEnd - substrStart));
    newDigit = ($clinit_Multiplication() , multiplyByInt(digits, digits, digitIndex, bigRadix));
    newDigit += inplaceAdd(digits, digitIndex, bigRadixDigit);
    digits[digitIndex++] = newDigit;
  }
  bi.sign = 1;
  bi.numberLength = digitIndex;
  bi.digits = digits;
  $cutOffLeadingZeroes(bi);
}

function valueOf_4(val){
  $clinit_BigInteger();
  if (lt(val, {l:0, m:0, h:0})) {
    if (neq(val, {l:$intern_7, m:$intern_7, h:$intern_8})) {
      return new BigInteger_1(-1, neg(val));
    }
    return MINUS_ONE;
  }
   else 
    return lte(val, {l:10, m:0, h:0})?SMALL_VALUES[toInt(val)]:new BigInteger_1(1, val);
}

defineClass(17, 152, {3:1, 29:1, 152:1, 17:1}, BigInteger, BigInteger_0, BigInteger_1, BigInteger_2);
_.compareTo = function compareTo_5(val){
  return $compareTo_4(this, dynamicCast(val, 17));
}
;
_.equals$ = function equals_26(x_0){
  var x1;
  if (this === x_0) {
    return true;
  }
  if (instanceOf(x_0, 17)) {
    x1 = dynamicCast(x_0, 17);
    return this.sign == x1.sign && this.numberLength == x1.numberLength && $equalsArrays(this, x1.digits);
  }
  return false;
}
;
_.hashCode$ = function hashCode_22(){
  var i_0;
  if (this.hashCode != 0) {
    return this.hashCode;
  }
  for (i_0 = 0; i_0 < this.digits.length; i_0++) {
    this.hashCode = this.hashCode * 33 + (this.digits[i_0] & -1);
  }
  this.hashCode = this.hashCode * this.sign;
  return this.hashCode;
}
;
_.toString$ = function toString_183(){
  return toDecimalScaledString(this, 0);
}
;
_.hashCode = 0;
_.numberLength = 0;
_.sign = 0;
var MINUS_ONE, ONE_0, SMALL_VALUES, TEN, TWO_POWS, ZERO_0;
var Ljava_math_BigInteger_2_classLit = createForClass('java.math', 'BigInteger', 17, Ljava_lang_Number_2_classLit);
function shiftLeft(source, count){
  var intCount, resDigits, resLength, result;
  intCount = count >> 5;
  count &= 31;
  resLength = source.numberLength + intCount + (count == 0?0:1);
  resDigits = initDim(I_classLit, $intern_4, 0, resLength, 7, 1);
  shiftLeft_0(resDigits, source.digits, intCount, count);
  result = new BigInteger_0(source.sign, resLength, resDigits);
  $cutOffLeadingZeroes(result);
  return result;
}

function shiftLeft_0(result, source, intCount, count){
  var i_0, i0, rightShiftCount;
  if (count == 0) {
    arraycopy(source, 0, result, intCount, result.length - intCount);
  }
   else {
    rightShiftCount = 32 - count;
    result[result.length - 1] = 0;
    for (i0 = result.length - 1; i0 > intCount; i0--) {
      result[i0] |= source[i0 - intCount - 1] >>> rightShiftCount;
      result[i0 - 1] = source[i0 - intCount - 1] << count;
    }
  }
  for (i_0 = 0; i_0 < intCount; i_0++) {
    result[i_0] = 0;
  }
}

function shiftLeftOneBit(result, source, srcLen){
  var carry, i_0, val;
  carry = 0;
  for (i_0 = 0; i_0 < srcLen; i_0++) {
    val = source[i_0];
    result[i_0] = val << 1 | carry;
    carry = val >>> 31;
  }
  carry != 0 && (result[srcLen] = carry);
}

function shiftRight(source, count){
  var i_0, intCount, resDigits, resLength, result;
  intCount = count >> 5;
  count &= 31;
  if (intCount >= source.numberLength) {
    return source.sign < 0?($clinit_BigInteger() , MINUS_ONE):($clinit_BigInteger() , ZERO_0);
  }
  resLength = source.numberLength - intCount;
  resDigits = initDim(I_classLit, $intern_4, 0, resLength + 1, 7, 1);
  shiftRight_0(resDigits, resLength, source.digits, intCount, count);
  if (source.sign < 0) {
    for (i_0 = 0; i_0 < intCount && source.digits[i_0] == 0; i_0++)
    ;
    if (i_0 < intCount || count > 0 && source.digits[i_0] << 32 - count != 0) {
      for (i_0 = 0; i_0 < resLength && resDigits[i_0] == -1; i_0++) {
        resDigits[i_0] = 0;
      }
      i_0 == resLength && ++resLength;
      ++resDigits[i_0];
    }
  }
  result = new BigInteger_0(source.sign, resLength, resDigits);
  $cutOffLeadingZeroes(result);
  return result;
}

function shiftRight_0(result, resultLen, source, intCount, count){
  var allZero, i_0, leftShiftCount;
  allZero = true;
  for (i_0 = 0; i_0 < intCount; i_0++) {
    allZero = allZero & source[i_0] == 0;
  }
  if (count == 0) {
    arraycopy(source, intCount, result, 0, resultLen);
  }
   else {
    leftShiftCount = 32 - count;
    allZero = allZero & source[i_0] << leftShiftCount == 0;
    for (i_0 = 0; i_0 < resultLen - 1; i_0++) {
      result[i_0] = source[i_0 + intCount] >>> count | source[i_0 + intCount + 1] << leftShiftCount;
    }
    result[i_0] = source[i_0 + intCount] >>> count;
    ++i_0;
  }
  return allZero;
}

function $clinit_Conversion(){
  $clinit_Conversion = emptyMethod;
  bigRadices = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [$intern_29, 1162261467, $intern_33, 1220703125, 362797056, 1977326743, $intern_33, 387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056, 170859375, 268435456, 410338673, 612220032, 893871739, 1280000000, 1801088541, 113379904, 148035889, 191102976, 244140625, 308915776, 387420489, 481890304, 594823321, 729000000, 887503681, $intern_33, 1291467969, 1544804416, 1838265625, 60466176]);
  digitFitInInt = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [-1, -1, 31, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5]);
}

function divideLongByBillion(a){
  var aPos, quot, rem;
  if (gte_0(a, {l:0, m:0, h:0})) {
    quot = div(a, {l:1755648, m:238, h:0});
    rem = mod(a, {l:1755648, m:238, h:0});
  }
   else {
    aPos = shru(a, 1);
    quot = div(aPos, {l:877824, m:119, h:0});
    rem = mod(aPos, {l:877824, m:119, h:0});
    rem = add_0(shl(rem, 1), and(a, {l:1, m:0, h:0}));
  }
  return or(shl(rem, 32), and(quot, {l:$intern_7, m:1023, h:0}));
}

function toDecimalScaledString(val, scale){
  $clinit_Conversion();
  var currentChar, delta, digits, exponent, highDigit, i_0, i1, insertPoint, j, j0, negNumber, numberLength, prev, previous, res, resDigit, resLengthInChars, result, result1, result10, result11, sign, startPoint, temp, temp1, tempLen, v;
  sign = val.sign;
  numberLength = val.numberLength;
  digits = val.digits;
  if (sign == 0) {
    switch (scale) {
      case 0:
        return '0';
      case 1:
        return '0.0';
      case 2:
        return '0.00';
      case 3:
        return '0.000';
      case 4:
        return '0.0000';
      case 5:
        return '0.00000';
      case 6:
        return '0.000000';
      default:result10 = new StringBuilder;
        scale < 0?(result10.string += '0E+' , result10):(result10.string += '0E' , result10);
        result10.string += -scale;
        return result10.string;
    }
  }
  resLengthInChars = numberLength * 10 + 1 + 7;
  result = initDim(C_classLit, $intern_4, 0, resLengthInChars + 1, 7, 1);
  currentChar = resLengthInChars;
  if (numberLength == 1) {
    highDigit = digits[0];
    if (highDigit < 0) {
      v = and(fromInt(highDigit), {l:$intern_7, m:1023, h:0});
      do {
        prev = v;
        v = div(v, {l:10, m:0, h:0});
        result[--currentChar] = 48 + toInt(sub_0(prev, mul(v, {l:10, m:0, h:0}))) & $intern_5;
      }
       while (neq(v, {l:0, m:0, h:0}));
    }
     else {
      v = highDigit;
      do {
        prev = v;
        v = ~~(v / 10);
        result[--currentChar] = 48 + (prev - v * 10) & $intern_5;
      }
       while (v != 0);
    }
  }
   else {
    temp = initDim(I_classLit, $intern_4, 0, numberLength, 7, 1);
    tempLen = numberLength;
    arraycopy(digits, 0, temp, 0, numberLength);
    BIG_LOOP: while (true) {
      result11 = {l:0, m:0, h:0};
      for (i1 = tempLen - 1; i1 >= 0; i1--) {
        temp1 = add_0(shl(result11, 32), and(fromInt(temp[i1]), {l:$intern_7, m:1023, h:0}));
        res = divideLongByBillion(temp1);
        temp[i1] = toInt(res);
        result11 = fromInt(toInt(shr(res, 32)));
      }
      resDigit = toInt(result11);
      previous = currentChar;
      do {
        result[--currentChar] = 48 + resDigit % 10 & $intern_5;
      }
       while ((resDigit = ~~(resDigit / 10)) != 0 && currentChar != 0);
      delta = 9 - previous + currentChar;
      for (i_0 = 0; i_0 < delta && currentChar > 0; i_0++) {
        result[--currentChar] = 48;
      }
      j = tempLen - 1;
      for (; temp[j] == 0; j--) {
        if (j == 0) {
          break BIG_LOOP;
        }
      }
      tempLen = j + 1;
    }
    while (result[currentChar] == 48) {
      ++currentChar;
    }
  }
  negNumber = sign < 0;
  exponent = resLengthInChars - currentChar - scale - 1;
  if (scale == 0) {
    negNumber && (result[--currentChar] = 45);
    return valueOf_3(result, currentChar, resLengthInChars - currentChar);
  }
  if (scale > 0 && exponent >= -6) {
    if (exponent >= 0) {
      insertPoint = currentChar + exponent;
      for (j0 = resLengthInChars - 1; j0 >= insertPoint; j0--) {
        result[j0 + 1] = result[j0];
      }
      result[++insertPoint] = 46;
      negNumber && (result[--currentChar] = 45);
      return valueOf_3(result, currentChar, resLengthInChars - currentChar + 1);
    }
    for (j = 2; j < -exponent + 1; j++) {
      result[--currentChar] = 48;
    }
    result[--currentChar] = 46;
    result[--currentChar] = 48;
    negNumber && (result[--currentChar] = 45);
    return valueOf_3(result, currentChar, resLengthInChars - currentChar);
  }
  startPoint = currentChar + 1;
  result1 = new StringBuilder_0;
  negNumber && (result1.string += '-' , result1);
  if (resLengthInChars - startPoint >= 1) {
    $append(result1, result[currentChar]);
    result1.string += '.';
    result1.string += valueOf_3(result, currentChar + 1, resLengthInChars - currentChar - 1);
  }
   else {
    result1.string += valueOf_3(result, currentChar, resLengthInChars - currentChar);
  }
  result1.string += 'E';
  exponent > 0 && (result1.string += '+' , result1);
  result1.string += '' + exponent;
  return result1.string;
}

var bigRadices, digitFitInInt;
function add_1(op1, op2){
  var a, b, cmp, op1Len, op1Sign, op2Len, op2Sign, res, res0, resDigits, resSign, valueHi, valueLo;
  op1Sign = op1.sign;
  op2Sign = op2.sign;
  if (op1Sign == 0) {
    return op2;
  }
  if (op2Sign == 0) {
    return op1;
  }
  op1Len = op1.numberLength;
  op2Len = op2.numberLength;
  if (op1Len + op2Len == 2) {
    a = and(fromInt(op1.digits[0]), {l:$intern_7, m:1023, h:0});
    b = and(fromInt(op2.digits[0]), {l:$intern_7, m:1023, h:0});
    if (op1Sign == op2Sign) {
      res0 = add_0(a, b);
      valueLo = toInt(res0);
      valueHi = toInt(shru(res0, 32));
      return valueHi == 0?new BigInteger(op1Sign, valueLo):new BigInteger_0(op1Sign, 2, initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [valueLo, valueHi]));
    }
    return valueOf_4(op1Sign < 0?sub_0(b, a):sub_0(a, b));
  }
   else if (op1Sign == op2Sign) {
    resSign = op1Sign;
    resDigits = op1Len >= op2Len?add_2(op1.digits, op1Len, op2.digits, op2Len):add_2(op2.digits, op2Len, op1.digits, op1Len);
  }
   else {
    cmp = op1Len != op2Len?op1Len > op2Len?1:-1:compareArrays(op1.digits, op2.digits, op1Len);
    if (cmp == 0) {
      return $clinit_BigInteger() , ZERO_0;
    }
    if (cmp == 1) {
      resSign = op1Sign;
      resDigits = subtract_0(op1.digits, op1Len, op2.digits, op2Len);
    }
     else {
      resSign = op2Sign;
      resDigits = subtract_0(op2.digits, op2Len, op1.digits, op1Len);
    }
  }
  res = new BigInteger_0(resSign, resDigits.length, resDigits);
  $cutOffLeadingZeroes(res);
  return res;
}

function add_2(a, aSize, b, bSize){
  var res;
  res = initDim(I_classLit, $intern_4, 0, aSize + 1, 7, 1);
  add_3(res, a, aSize, b, bSize);
  return res;
}

function add_3(res, a, aSize, b, bSize){
  var carry, i_0;
  carry = add_0(and(fromInt(a[0]), {l:$intern_7, m:1023, h:0}), and(fromInt(b[0]), {l:$intern_7, m:1023, h:0}));
  res[0] = toInt(carry);
  carry = shr(carry, 32);
  if (aSize >= bSize) {
    for (i_0 = 1; i_0 < bSize; i_0++) {
      carry = add_0(carry, add_0(and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}), and(fromInt(b[i_0]), {l:$intern_7, m:1023, h:0})));
      res[i_0] = toInt(carry);
      carry = shr(carry, 32);
    }
    for (; i_0 < aSize; i_0++) {
      carry = add_0(carry, and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}));
      res[i_0] = toInt(carry);
      carry = shr(carry, 32);
    }
  }
   else {
    for (i_0 = 1; i_0 < aSize; i_0++) {
      carry = add_0(carry, add_0(and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}), and(fromInt(b[i_0]), {l:$intern_7, m:1023, h:0})));
      res[i_0] = toInt(carry);
      carry = shr(carry, 32);
    }
    for (; i_0 < bSize; i_0++) {
      carry = add_0(carry, and(fromInt(b[i_0]), {l:$intern_7, m:1023, h:0}));
      res[i_0] = toInt(carry);
      carry = shr(carry, 32);
    }
  }
  neq(carry, {l:0, m:0, h:0}) && (res[i_0] = toInt(carry));
}

function compareArrays(a, b, size_0){
  var i_0;
  for (i_0 = size_0 - 1; i_0 >= 0 && a[i_0] == b[i_0]; i_0--)
  ;
  return i_0 < 0?0:lt(and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}), and(fromInt(b[i_0]), {l:$intern_7, m:1023, h:0}))?-1:1;
}

function inplaceAdd(a, aSize, addend){
  var carry, i_0;
  carry = and(fromInt(addend), {l:$intern_7, m:1023, h:0});
  for (i_0 = 0; neq(carry, {l:0, m:0, h:0}) && i_0 < aSize; i_0++) {
    carry = add_0(carry, and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}));
    a[i_0] = toInt(carry);
    carry = shr(carry, 32);
  }
  return toInt(carry);
}

function subtract(op1, op2){
  var a, b, cmp, op1Len, op1Sign, op2Len, op2Sign, res, resDigits, resSign;
  op1Sign = op1.sign;
  op2Sign = op2.sign;
  if (op2Sign == 0) {
    return op1;
  }
  if (op1Sign == 0) {
    return op2.sign == 0?op2:new BigInteger_0(-op2.sign, op2.numberLength, op2.digits);
  }
  op1Len = op1.numberLength;
  op2Len = op2.numberLength;
  if (op1Len + op2Len == 2) {
    a = and(fromInt(op1.digits[0]), {l:$intern_7, m:1023, h:0});
    b = and(fromInt(op2.digits[0]), {l:$intern_7, m:1023, h:0});
    op1Sign < 0 && (a = neg(a));
    op2Sign < 0 && (b = neg(b));
    return valueOf_4(sub_0(a, b));
  }
  cmp = op1Len != op2Len?op1Len > op2Len?1:-1:compareArrays(op1.digits, op2.digits, op1Len);
  if (cmp == -1) {
    resSign = -op2Sign;
    resDigits = op1Sign == op2Sign?subtract_0(op2.digits, op2Len, op1.digits, op1Len):add_2(op2.digits, op2Len, op1.digits, op1Len);
  }
   else {
    resSign = op1Sign;
    if (op1Sign == op2Sign) {
      if (cmp == 0) {
        return $clinit_BigInteger() , ZERO_0;
      }
      resDigits = subtract_0(op1.digits, op1Len, op2.digits, op2Len);
    }
     else {
      resDigits = add_2(op1.digits, op1Len, op2.digits, op2Len);
    }
  }
  res = new BigInteger_0(resSign, resDigits.length, resDigits);
  $cutOffLeadingZeroes(res);
  return res;
}

function subtract_0(a, aSize, b, bSize){
  var res;
  res = initDim(I_classLit, $intern_4, 0, aSize, 7, 1);
  subtract_1(res, a, aSize, b, bSize);
  return res;
}

function subtract_1(res, a, aSize, b, bSize){
  var borrow, i_0;
  borrow = {l:0, m:0, h:0};
  for (i_0 = 0; i_0 < bSize; i_0++) {
    borrow = add_0(borrow, sub_0(and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}), and(fromInt(b[i_0]), {l:$intern_7, m:1023, h:0})));
    res[i_0] = toInt(borrow);
    borrow = shr(borrow, 32);
  }
  for (; i_0 < aSize; i_0++) {
    borrow = add_0(borrow, and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}));
    res[i_0] = toInt(borrow);
    borrow = shr(borrow, 32);
  }
}

function $clinit_Multiplication(){
  $clinit_Multiplication = emptyMethod;
  var fivePow, i_0;
  bigFivePows = initDim(Ljava_math_BigInteger_2_classLit, $intern_6, 17, 32, 0, 1);
  bigTenPows = initDim(Ljava_math_BigInteger_2_classLit, $intern_6, 17, 32, 0, 1);
  fivePow = {l:1, m:0, h:0};
  for (i_0 = 0; i_0 <= 18; i_0++) {
    bigFivePows[i_0] = valueOf_4(fivePow);
    bigTenPows[i_0] = valueOf_4(shl(fivePow, i_0));
    fivePow = mul(fivePow, {l:5, m:0, h:0});
  }
  for (; i_0 < bigTenPows.length; i_0++) {
    bigFivePows[i_0] = $multiply(bigFivePows[i_0 - 1], bigFivePows[1]);
    bigTenPows[i_0] = $multiply(bigTenPows[i_0 - 1], ($clinit_BigInteger() , TEN));
  }
}

function karatsuba(op1, op2){
  $clinit_Multiplication();
  var lower, lowerOp1, lowerOp2, middle, ndiv2, temp, upper, upperOp1, upperOp2;
  if (op2.numberLength > op1.numberLength) {
    temp = op1;
    op1 = op2;
    op2 = temp;
  }
  if (op2.numberLength < 63) {
    return multiplyPAP(op1, op2);
  }
  ndiv2 = (op1.numberLength & -2) << 4;
  upperOp1 = $shiftRight(op1, ndiv2);
  upperOp2 = $shiftRight(op2, ndiv2);
  lowerOp1 = subtract(op1, $shiftLeft(upperOp1, ndiv2));
  lowerOp2 = subtract(op2, $shiftLeft(upperOp2, ndiv2));
  upper = karatsuba(upperOp1, upperOp2);
  lower = karatsuba(lowerOp1, lowerOp2);
  middle = karatsuba(subtract(upperOp1, lowerOp1), subtract(lowerOp2, upperOp2));
  middle = add_1(add_1(middle, upper), lower);
  middle = $shiftLeft(middle, ndiv2);
  upper = $shiftLeft(upper, ndiv2 << 1);
  return add_1(add_1(upper, middle), lower);
}

function multArraysPAP(aDigits, aLen, bDigits, bLen, resDigits){
  if (aLen == 0 || bLen == 0) {
    return;
  }
  aLen == 1?(resDigits[bLen] = multiplyByInt(resDigits, bDigits, bLen, aDigits[0])):bLen == 1?(resDigits[aLen] = multiplyByInt(resDigits, aDigits, aLen, bDigits[0])):multPAP(aDigits, bDigits, resDigits, aLen, bLen);
}

function multPAP(a, b, t, aLen, bLen){
  var aI, carry, i_0, j;
  if (maskUndefined(a) === maskUndefined(b) && aLen == bLen) {
    square(a, aLen, t);
    return;
  }
  for (i_0 = 0; i_0 < aLen; i_0++) {
    carry = {l:0, m:0, h:0};
    aI = a[i_0];
    for (j = 0; j < bLen; j++) {
      carry = add_0(add_0(mul(and(fromInt(aI), {l:$intern_7, m:1023, h:0}), and(fromInt(b[j]), {l:$intern_7, m:1023, h:0})), and(fromInt(t[i_0 + j]), {l:$intern_7, m:1023, h:0})), and(fromInt(toInt(carry)), {l:$intern_7, m:1023, h:0}));
      t[i_0 + j] = toInt(carry);
      carry = shru(carry, 32);
    }
    t[i_0 + bLen] = toInt(carry);
  }
}

function multiplyByInt(res, a, aSize, factor){
  $clinit_Multiplication();
  var carry, i_0;
  carry = {l:0, m:0, h:0};
  for (i_0 = 0; i_0 < aSize; i_0++) {
    carry = add_0(mul(and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}), and(fromInt(factor), {l:$intern_7, m:1023, h:0})), and(fromInt(toInt(carry)), {l:$intern_7, m:1023, h:0}));
    res[i_0] = toInt(carry);
    carry = shru(carry, 32);
  }
  return toInt(carry);
}

function multiplyPAP(a, b){
  var aDigits, aLen, bDigits, bLen, resDigits, resLength, resSign, result, val, valueHi, valueLo;
  aLen = a.numberLength;
  bLen = b.numberLength;
  resLength = aLen + bLen;
  resSign = a.sign != b.sign?-1:1;
  if (resLength == 2) {
    val = mul(and(fromInt(a.digits[0]), {l:$intern_7, m:1023, h:0}), and(fromInt(b.digits[0]), {l:$intern_7, m:1023, h:0}));
    valueLo = toInt(val);
    valueHi = toInt(shru(val, 32));
    return valueHi == 0?new BigInteger(resSign, valueLo):new BigInteger_0(resSign, 2, initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [valueLo, valueHi]));
  }
  aDigits = a.digits;
  bDigits = b.digits;
  resDigits = initDim(I_classLit, $intern_4, 0, resLength, 7, 1);
  multArraysPAP(aDigits, aLen, bDigits, bLen, resDigits);
  result = new BigInteger_0(resSign, resLength, resDigits);
  $cutOffLeadingZeroes(result);
  return result;
}

function square(a, aLen, res){
  var carry, i_0, i0, index_0, j;
  for (i0 = 0; i0 < aLen; i0++) {
    carry = {l:0, m:0, h:0};
    for (j = i0 + 1; j < aLen; j++) {
      carry = add_0(add_0(mul(and(fromInt(a[i0]), {l:$intern_7, m:1023, h:0}), and(fromInt(a[j]), {l:$intern_7, m:1023, h:0})), and(fromInt(res[i0 + j]), {l:$intern_7, m:1023, h:0})), and(fromInt(toInt(carry)), {l:$intern_7, m:1023, h:0}));
      res[i0 + j] = toInt(carry);
      carry = shru(carry, 32);
    }
    res[i0 + aLen] = toInt(carry);
  }
  shiftLeftOneBit(res, res, aLen << 1);
  carry = {l:0, m:0, h:0};
  for (i_0 = 0 , index_0 = 0; i_0 < aLen; ++i_0 , index_0++) {
    carry = add_0(add_0(mul(and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0}), and(fromInt(a[i_0]), {l:$intern_7, m:1023, h:0})), and(fromInt(res[index_0]), {l:$intern_7, m:1023, h:0})), and(fromInt(toInt(carry)), {l:$intern_7, m:1023, h:0}));
    res[index_0] = toInt(carry);
    carry = shru(carry, 32);
    ++index_0;
    carry = add_0(carry, and(fromInt(res[index_0]), {l:$intern_7, m:1023, h:0}));
    res[index_0] = toInt(carry);
    carry = shru(carry, 32);
  }
  return res;
}

var bigFivePows, bigTenPows;
function $addAll(this$static, c){
  var changed, e, e$iterator;
  checkNotNull(c);
  changed = false;
  for (e$iterator = c.iterator(); e$iterator.hasNext();) {
    e = e$iterator.next();
    changed = changed | $add_1(this$static, e);
  }
  return changed;
}

function $advanceToFind(this$static, o, remove){
  var e, iter;
  for (iter = this$static.iterator(); iter.hasNext();) {
    e = iter.next();
    if (maskUndefined(o) === maskUndefined(e) || o != null && equals_Ljava_lang_Object__Z__devirtual$(o, e)) {
      remove && iter.remove_0();
      return true;
    }
  }
  return false;
}

function $containsAll(this$static, c){
  var e, e$iterator;
  checkNotNull(c);
  for (e$iterator = c.iterator(); e$iterator.hasNext();) {
    e = e$iterator.next();
    if (!this$static.contains(e)) {
      return false;
    }
  }
  return true;
}

function $toArray(this$static, a){
  var i_0, it, size_0;
  size_0 = this$static.size_1();
  a.length < size_0 && (a = createFrom(a, size_0));
  it = this$static.iterator();
  for (i_0 = 0; i_0 < size_0; ++i_0) {
    setCheck(a, i_0, it.next());
  }
  a.length > size_0 && setCheck(a, size_0, null);
  return a;
}

function $toString(this$static){
  var comma, e, e$iterator, sb;
  sb = new StringBuilder_1('[');
  comma = false;
  for (e$iterator = this$static.iterator(); e$iterator.hasNext();) {
    e = e$iterator.next();
    comma?(sb.string += ', ' , sb):(comma = true);
    sb.string += e === this$static?'(this Collection)':'' + e;
  }
  sb.string += ']';
  return sb.string;
}

defineClass(775, 1, {});
_.contains = function contains(o){
  return $advanceToFind(this, o, false);
}
;
_.remove = function remove_0(o){
  return $advanceToFind(this, o, true);
}
;
_.removeAll = function removeAll(c){
  var changed, iter, o;
  checkNotNull(c);
  changed = false;
  for (iter = this.iterator(); iter.hasNext();) {
    o = iter.next();
    if (c.contains(o)) {
      iter.remove_0();
      changed = true;
    }
  }
  return changed;
}
;
_.toArray = function toArray(){
  return this.toArray_0(initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, this.size_1(), 3, 1));
}
;
_.toArray_0 = function toArray_0(a){
  return $toArray(this, a);
}
;
_.toString$ = function toString_184(){
  return $toString(this);
}
;
var Ljava_util_AbstractCollection_2_classLit = createForClass('java.util', 'AbstractCollection', 775, Ljava_lang_Object_2_classLit);
function $containsEntry(this$static, entry){
  var key, ourValue, value_0;
  key = entry.getKey();
  value_0 = entry.getValue();
  ourValue = this$static.get_0(key);
  if (!(maskUndefined(value_0) === maskUndefined(ourValue) || value_0 != null && equals_Ljava_lang_Object__Z__devirtual$(value_0, ourValue))) {
    return false;
  }
  if (ourValue == null && !this$static.containsKey(key)) {
    return false;
  }
  return true;
}

function $implFindEntry(this$static, key){
  var entry, iter, k_0;
  for (iter = this$static.entrySet_0().iterator(); iter.hasNext();) {
    entry = dynamicCast(iter.next(), 20);
    k_0 = entry.getKey();
    if (maskUndefined(key) === maskUndefined(k_0) || key != null && equals_Ljava_lang_Object__Z__devirtual$(key, k_0)) {
      return entry;
    }
  }
  return null;
}

function $toString_0(this$static, o){
  return o === this$static?'(this Map)':'' + o;
}

function getEntryValueOrNull(entry){
  return !entry?null:entry.getValue();
}

defineClass(774, 1, $intern_34);
_.containsEntry = function containsEntry(entry){
  return $containsEntry(this, entry);
}
;
_.containsKey = function containsKey(key){
  return !!$implFindEntry(this, key);
}
;
_.containsValue = function containsValue(value_0){
  var entry, entry$iterator, v;
  for (entry$iterator = this.entrySet_0().iterator(); entry$iterator.hasNext();) {
    entry = dynamicCast(entry$iterator.next(), 20);
    v = entry.getValue();
    if (maskUndefined(value_0) === maskUndefined(v) || value_0 != null && equals_Ljava_lang_Object__Z__devirtual$(value_0, v)) {
      return true;
    }
  }
  return false;
}
;
_.equals$ = function equals_27(obj){
  var entry, entry$iterator, otherMap;
  if (obj === this) {
    return true;
  }
  if (!instanceOf(obj, 148)) {
    return false;
  }
  otherMap = dynamicCast(obj, 148);
  if (this.size_1() != otherMap.size_1()) {
    return false;
  }
  for (entry$iterator = otherMap.entrySet_0().iterator(); entry$iterator.hasNext();) {
    entry = dynamicCast(entry$iterator.next(), 20);
    if (!this.containsEntry(entry)) {
      return false;
    }
  }
  return true;
}
;
_.get_0 = function get_0(key){
  return getEntryValueOrNull($implFindEntry(this, key));
}
;
_.hashCode$ = function hashCode_23(){
  return hashCode_29(this.entrySet_0());
}
;
_.put = function put_0(key, value_0){
  throw new UnsupportedOperationException_0('Put not supported on this map');
}
;
_.size_1 = function size_1(){
  return this.entrySet_0().size_1();
}
;
_.toString$ = function toString_185(){
  var comma, entry, entry$iterator, sb;
  sb = new StringBuilder_1('{');
  comma = false;
  for (entry$iterator = this.entrySet_0().iterator(); entry$iterator.hasNext();) {
    entry = dynamicCast(entry$iterator.next(), 20);
    comma?(sb.string += ', ' , sb):(comma = true);
    $append_1(sb, $toString_0(this, entry.getKey()));
    sb.string += '=';
    $append_1(sb, $toString_0(this, entry.getValue()));
  }
  sb.string += '}';
  return sb.string;
}
;
var Ljava_util_AbstractMap_2_classLit = createForClass('java.util', 'AbstractMap', 774, Ljava_lang_Object_2_classLit);
function $containsKey(this$static, key){
  return isJavaString(key)?$hasStringValue(this$static, key):!!$getEntry(this$static.hashCodeMap, key);
}

function $elementAdded(this$static){
  ++this$static.size_0;
  structureChanged(this$static);
}

function $elementRemoved(this$static){
  --this$static.size_0;
  structureChanged(this$static);
}

function $get_2(this$static, key){
  return isJavaString(key)?$getStringValue(this$static, key):getEntryValueOrNull($getEntry(this$static.hashCodeMap, key));
}

function $getStringValue(this$static, key){
  return key == null?getEntryValueOrNull($getEntry(this$static.hashCodeMap, null)):this$static.stringMap.get_2(key);
}

function $hasStringValue(this$static, key){
  return key == null?!!$getEntry(this$static.hashCodeMap, null):!(this$static.stringMap.get_2(key) === undefined);
}

function $put_1(this$static, key, value_0){
  return isJavaString(key)?$putStringValue(this$static, key, value_0):$put_2(this$static.hashCodeMap, key, value_0);
}

function $putStringValue(this$static, key, value_0){
  return key == null?$put_2(this$static.hashCodeMap, null, value_0):this$static.stringMap.put_0(key, value_0);
}

function $remove_0(this$static, key){
  return isJavaString(key)?$removeStringValue(this$static, key):$remove_6(this$static.hashCodeMap, key);
}

function $removeStringValue(this$static, key){
  return key == null?$remove_6(this$static.hashCodeMap, null):this$static.stringMap.remove_2(key);
}

function $reset(this$static){
  $clinit_InternalJsMapFactory$BackwardCompatibleJsMapFactory();
  this$static.hashCodeMap = delegate.createJsHashCodeMap();
  this$static.hashCodeMap.host = this$static;
  this$static.stringMap = delegate.createJsStringMap();
  this$static.stringMap.host = this$static;
  this$static.size_0 = 0;
  structureChanged(this$static);
}

defineClass(339, 774, $intern_34);
_.containsKey = function containsKey_0(key){
  return $containsKey(this, key);
}
;
_.containsValue = function containsValue_0(value_0){
  return this.stringMap.containsValue(value_0) || this.hashCodeMap.containsValue(value_0);
}
;
_.entrySet_0 = function entrySet(){
  return new AbstractHashMap$EntrySet(this);
}
;
_.get_0 = function get_1(key){
  return $get_2(this, key);
}
;
_.put = function put_1(key, value_0){
  return $putStringValue(this, key, value_0);
}
;
_.size_1 = function size_2(){
  return this.size_0;
}
;
_.size_0 = 0;
var Ljava_util_AbstractHashMap_2_classLit = createForClass('java.util', 'AbstractHashMap', 339, Ljava_util_AbstractMap_2_classLit);
defineClass(776, 775, $intern_35);
_.equals$ = function equals_28(o){
  var other;
  if (o === this) {
    return true;
  }
  if (!instanceOf(o, 92)) {
    return false;
  }
  other = dynamicCast(o, 92);
  if (other.size_1() != this.size_1()) {
    return false;
  }
  return $containsAll(this, other);
}
;
_.hashCode$ = function hashCode_24(){
  return hashCode_29(this);
}
;
_.removeAll = function removeAll_0(c){
  var iter, o, o$iterator, size_0;
  checkNotNull(c);
  size_0 = this.size_1();
  if (size_0 < c.size_1()) {
    for (iter = this.iterator(); iter.hasNext();) {
      o = iter.next();
      c.contains(o) && iter.remove_0();
    }
  }
   else {
    for (o$iterator = c.iterator(); o$iterator.hasNext();) {
      o = o$iterator.next();
      this.remove(o);
    }
  }
  return size_0 != this.size_1();
}
;
var Ljava_util_AbstractSet_2_classLit = createForClass('java.util', 'AbstractSet', 776, Ljava_util_AbstractCollection_2_classLit);
function $contains(this$static, o){
  if (instanceOf(o, 20)) {
    return $containsEntry(this$static.this$01, dynamicCast(o, 20));
  }
  return false;
}

function AbstractHashMap$EntrySet(this$0){
  this.this$01 = this$0;
}

defineClass(436, 776, $intern_35, AbstractHashMap$EntrySet);
_.contains = function contains_0(o){
  return $contains(this, o);
}
;
_.iterator = function iterator_0(){
  return new AbstractHashMap$EntrySetIterator(this.this$01);
}
;
_.remove = function remove_1(entry){
  var key;
  if ($contains(this, entry)) {
    key = dynamicCast(entry, 20).getKey();
    $remove_0(this.this$01, key);
    return true;
  }
  return false;
}
;
_.size_1 = function size_3(){
  return this.this$01.size_0;
}
;
var Ljava_util_AbstractHashMap$EntrySet_2_classLit = createForClass('java.util', 'AbstractHashMap/EntrySet', 436, Ljava_util_AbstractSet_2_classLit);
function $hasNext(this$static){
  if (this$static.current.hasNext()) {
    return true;
  }
  if (this$static.current != this$static.stringMapEntries) {
    return false;
  }
  this$static.current = this$static.this$01.hashCodeMap.entries();
  return this$static.current.hasNext();
}

function $next(this$static){
  return checkStructuralChange(this$static.this$01, this$static) , checkCriticalElement($hasNext(this$static)) , this$static.last = this$static.current , dynamicCast(this$static.current.next(), 20);
}

function $remove_1(this$static){
  checkState(!!this$static.last);
  checkStructuralChange(this$static.this$01, this$static);
  this$static.last.remove_0();
  this$static.last = null;
  recordLastKnownStructure(this$static.this$01, this$static);
}

function AbstractHashMap$EntrySetIterator(this$0){
  this.this$01 = this$0;
  this.stringMapEntries = this.this$01.stringMap.entries();
  this.current = this.stringMapEntries;
  setModCount(this, this$0._gwt_modCount);
}

defineClass(437, 1, {}, AbstractHashMap$EntrySetIterator);
_.hasNext = function hasNext(){
  return $hasNext(this);
}
;
_.next = function next_0(){
  return $next(this);
}
;
_.remove_0 = function remove_2(){
  $remove_1(this);
}
;
var Ljava_util_AbstractHashMap$EntrySetIterator_2_classLit = createForClass('java.util', 'AbstractHashMap/EntrySetIterator', 437, Ljava_lang_Object_2_classLit);
defineClass(777, 775, {43:1});
_.add_0 = function add_4(index_0, element){
  throw new UnsupportedOperationException_0('Add not supported on this list');
}
;
_.add_1 = function add_5(obj){
  this.add_0(this.size_1(), obj);
  return true;
}
;
_.equals$ = function equals_29(o){
  var elem, elem$iterator, elemOther, iterOther, other;
  if (o === this) {
    return true;
  }
  if (!instanceOf(o, 43)) {
    return false;
  }
  other = dynamicCast(o, 43);
  if (this.size_1() != other.size_1()) {
    return false;
  }
  iterOther = other.iterator();
  for (elem$iterator = new AbstractList$IteratorImpl(this); elem$iterator.i < elem$iterator.this$01.size_1();) {
    elem = (checkCriticalElement(elem$iterator.i < elem$iterator.this$01.size_1()) , elem$iterator.this$01.get_1(elem$iterator.last = elem$iterator.i++));
    elemOther = iterOther.next();
    if (!(maskUndefined(elem) === maskUndefined(elemOther) || elem != null && equals_Ljava_lang_Object__Z__devirtual$(elem, elemOther))) {
      return false;
    }
  }
  return true;
}
;
_.hashCode$ = function hashCode_25(){
  return hashCode_30(this);
}
;
_.iterator = function iterator_1(){
  return new AbstractList$IteratorImpl(this);
}
;
_.remove_1 = function remove_3(index_0){
  throw new UnsupportedOperationException_0('Remove not supported on this list');
}
;
var Ljava_util_AbstractList_2_classLit = createForClass('java.util', 'AbstractList', 777, Ljava_util_AbstractCollection_2_classLit);
function $hasNext_0(this$static){
  return this$static.i < this$static.this$01.size_1();
}

function $next_0(this$static){
  checkCriticalElement(this$static.i < this$static.this$01.size_1());
  return this$static.this$01.get_1(this$static.last = this$static.i++);
}

function $remove_2(this$static){
  checkState(this$static.last != -1);
  this$static.this$01.remove_1(this$static.last);
  this$static.i = this$static.last;
  this$static.last = -1;
}

function AbstractList$IteratorImpl(this$0){
  this.this$01 = this$0;
}

defineClass(9, 1, {}, AbstractList$IteratorImpl);
_.hasNext = function hasNext_0(){
  return $hasNext_0(this);
}
;
_.next = function next_1(){
  return $next_0(this);
}
;
_.remove_0 = function remove_4(){
  $remove_2(this);
}
;
_.i = 0;
_.last = -1;
var Ljava_util_AbstractList$IteratorImpl_2_classLit = createForClass('java.util', 'AbstractList/IteratorImpl', 9, Ljava_lang_Object_2_classLit);
function AbstractList$ListIteratorImpl(this$0){
  AbstractList$IteratorImpl.call(this, this$0);
  checkPositionIndex(0, this$0.array.length);
  this.i = 0;
}

defineClass(489, 9, {}, AbstractList$ListIteratorImpl);
var Ljava_util_AbstractList$ListIteratorImpl_2_classLit = createForClass('java.util', 'AbstractList/ListIteratorImpl', 489, Ljava_util_AbstractList$IteratorImpl_2_classLit);
function $iterator(this$static){
  var outerIter;
  outerIter = new AbstractHashMap$EntrySetIterator((new AbstractHashMap$EntrySet(this$static.this$01)).this$01);
  return new AbstractMap$1$1(outerIter);
}

function AbstractMap$1(this$0){
  this.this$01 = this$0;
}

defineClass(52, 776, $intern_35, AbstractMap$1);
_.contains = function contains_1(key){
  return $containsKey(this.this$01, key);
}
;
_.iterator = function iterator_2(){
  return $iterator(this);
}
;
_.remove = function remove_5(key){
  if ($containsKey(this.this$01, key)) {
    $remove_0(this.this$01, key);
    return true;
  }
  return false;
}
;
_.size_1 = function size_4(){
  return this.this$01.size_0;
}
;
var Ljava_util_AbstractMap$1_2_classLit = createForClass('java.util', 'AbstractMap/1', 52, Ljava_util_AbstractSet_2_classLit);
function $next_1(this$static){
  var entry;
  entry = $next(this$static.val$outerIter2);
  return entry.getKey();
}

function AbstractMap$1$1(val$outerIter){
  this.val$outerIter2 = val$outerIter;
}

defineClass(480, 1, {}, AbstractMap$1$1);
_.hasNext = function hasNext_1(){
  return $hasNext(this.val$outerIter2);
}
;
_.next = function next_2(){
  return $next_1(this);
}
;
_.remove_0 = function remove_6(){
  $remove_1(this.val$outerIter2);
}
;
var Ljava_util_AbstractMap$1$1_2_classLit = createForClass('java.util', 'AbstractMap/1/1', 480, Ljava_lang_Object_2_classLit);
function $iterator_0(this$static){
  var outerIter;
  outerIter = this$static.this$01.entrySet_0().iterator();
  return new AbstractMap$2$1(outerIter);
}

function AbstractMap$2(this$0){
  this.this$01 = this$0;
}

defineClass(70, 775, {}, AbstractMap$2);
_.contains = function contains_2(value_0){
  return this.this$01.containsValue(value_0);
}
;
_.iterator = function iterator_3(){
  return $iterator_0(this);
}
;
_.size_1 = function size_5(){
  return this.this$01.size_1();
}
;
var Ljava_util_AbstractMap$2_2_classLit = createForClass('java.util', 'AbstractMap/2', 70, Ljava_util_AbstractCollection_2_classLit);
function $next_2(this$static){
  var entry;
  entry = dynamicCast(this$static.val$outerIter2.next(), 20);
  return entry.getValue();
}

function AbstractMap$2$1(val$outerIter){
  this.val$outerIter2 = val$outerIter;
}

defineClass(481, 1, {}, AbstractMap$2$1);
_.hasNext = function hasNext_2(){
  return this.val$outerIter2.hasNext();
}
;
_.next = function next_3(){
  return $next_2(this);
}
;
_.remove_0 = function remove_7(){
  this.val$outerIter2.remove_0();
}
;
var Ljava_util_AbstractMap$2$1_2_classLit = createForClass('java.util', 'AbstractMap/2/1', 481, Ljava_lang_Object_2_classLit);
function $setValue(this$static, value_0){
  var oldValue;
  oldValue = this$static.value_0;
  this$static.value_0 = value_0;
  return oldValue;
}

function AbstractMap$AbstractEntry(key, value_0){
  this.key = key;
  this.value_0 = value_0;
}

defineClass(118, 1, $intern_36);
_.equals$ = function equals_30(other){
  var entry;
  if (!instanceOf(other, 20)) {
    return false;
  }
  entry = dynamicCast(other, 20);
  return equals_38(this.key, entry.getKey()) && equals_38(this.value_0, entry.getValue());
}
;
_.getKey = function getKey(){
  return this.key;
}
;
_.getValue = function getValue(){
  return this.value_0;
}
;
_.hashCode$ = function hashCode_26(){
  return hashCode_36(this.key) ^ hashCode_36(this.value_0);
}
;
_.setValue = function setValue(value_0){
  return $setValue(this, value_0);
}
;
_.toString$ = function toString_186(){
  return this.key + '=' + this.value_0;
}
;
var Ljava_util_AbstractMap$AbstractEntry_2_classLit = createForClass('java.util', 'AbstractMap/AbstractEntry', 118, Ljava_lang_Object_2_classLit);
function AbstractMap$SimpleEntry(key, value_0){
  AbstractMap$AbstractEntry.call(this, key, value_0);
}

defineClass(223, 118, {118:1, 223:1, 20:1}, AbstractMap$SimpleEntry);
var Ljava_util_AbstractMap$SimpleEntry_2_classLit = createForClass('java.util', 'AbstractMap/SimpleEntry', 223, Ljava_util_AbstractMap$AbstractEntry_2_classLit);
function AbstractMap$SimpleImmutableEntry(entry){
  AbstractMap$AbstractEntry.call(this, entry.key, entry.value_0);
}

defineClass(479, 118, $intern_36, AbstractMap$SimpleImmutableEntry);
_.setValue = function setValue_0(value_0){
  throw new UnsupportedOperationException;
}
;
var Ljava_util_AbstractMap$SimpleImmutableEntry_2_classLit = createForClass('java.util', 'AbstractMap/SimpleImmutableEntry', 479, Ljava_util_AbstractMap$AbstractEntry_2_classLit);
defineClass(778, 1, {20:1});
_.equals$ = function equals_31(other){
  var entry;
  if (!instanceOf(other, 20)) {
    return false;
  }
  entry = dynamicCast(other, 20);
  return equals_38(this.getKey(), entry.getKey()) && equals_38(this.getValue(), entry.getValue());
}
;
_.hashCode$ = function hashCode_27(){
  return hashCode_36(this.getKey()) ^ hashCode_36(this.getValue());
}
;
_.toString$ = function toString_187(){
  return this.getKey() + '=' + this.getValue();
}
;
var Ljava_util_AbstractMapEntry_2_classLit = createForClass('java.util', 'AbstractMapEntry', 778, Ljava_lang_Object_2_classLit);
function $containsEntry_0(this$static, entry){
  var key, lookupEntry;
  key = entry.getKey();
  lookupEntry = $getEntry_0(this$static, key);
  return !!lookupEntry && equals_38(lookupEntry.value_0, entry.getValue());
}

function copyOf(entry){
  return !entry?null:new AbstractMap$SimpleImmutableEntry(entry);
}

function getKeyOrNSE(entry){
  if (!entry) {
    throw new NoSuchElementException;
  }
  return entry.key;
}

defineClass(793, 774, $intern_34);
_.containsEntry = function containsEntry_0(entry){
  return $containsEntry_0(this, entry);
}
;
_.containsKey = function containsKey_1(k_0){
  return !!$getEntry_0(this, k_0);
}
;
_.entrySet_0 = function entrySet_0(){
  return new AbstractNavigableMap$EntrySet(this);
}
;
_.get_0 = function get_2(k_0){
  return getEntryValueOrNull($getEntry_0(this, k_0));
}
;
var Ljava_util_AbstractNavigableMap_2_classLit = createForClass('java.util', 'AbstractNavigableMap', 793, Ljava_util_AbstractMap_2_classLit);
function AbstractNavigableMap$EntrySet(this$0){
  this.this$01 = this$0;
}

defineClass(461, 776, $intern_35, AbstractNavigableMap$EntrySet);
_.contains = function contains_3(o){
  return instanceOf(o, 20) && $containsEntry_0(this.this$01, dynamicCast(o, 20));
}
;
_.iterator = function iterator_4(){
  return new TreeMap$EntryIterator(this.this$01);
}
;
_.remove = function remove_8(o){
  var entry;
  if (instanceOf(o, 20)) {
    entry = dynamicCast(o, 20);
    return $removeEntry(this.this$01, entry);
  }
  return false;
}
;
_.size_1 = function size_6(){
  return this.this$01.size_0;
}
;
var Ljava_util_AbstractNavigableMap$EntrySet_2_classLit = createForClass('java.util', 'AbstractNavigableMap/EntrySet', 461, Ljava_util_AbstractSet_2_classLit);
function $$init_1(this$static){
  this$static.array = initDim(Ljava_lang_Object_2_classLit, $intern_6, 1, 0, 3, 1);
}

function $add(this$static, index_0, o){
  checkPositionIndex(index_0, this$static.array.length);
  splice_0(this$static.array, index_0, 0, o);
}

function $add_0(this$static, o){
  setCheck(this$static.array, this$static.array.length, o);
  return true;
}

function $addAll_0(this$static, c){
  var cArray, len;
  cArray = cloneSubrange(c.array, c.array.length);
  len = cArray.length;
  if (len == 0) {
    return false;
  }
  $insertAt(this$static, this$static.array.length, cArray);
  return true;
}

function $get_3(this$static, index_0){
  checkElementIndex(index_0, this$static.array.length);
  return this$static.array[index_0];
}

function $indexOf_0(this$static, o, index_0){
  for (; index_0 < this$static.array.length; ++index_0) {
    if (equals_38(o, this$static.array[index_0])) {
      return index_0;
    }
  }
  return -1;
}

function $insertAt(this$static, index_0, values){
  nativeArraySplice(values, 0, this$static.array, index_0, values.length, false);
}

function $remove_3(this$static, index_0){
  var previous;
  previous = (checkElementIndex(index_0, this$static.array.length) , this$static.array[index_0]);
  splice(this$static.array, index_0, 1);
  return previous;
}

function $remove_4(this$static, o){
  var i_0;
  i_0 = $indexOf_0(this$static, o, 0);
  if (i_0 == -1) {
    return false;
  }
  this$static.remove_1(i_0);
  return true;
}

function $toArray_0(this$static, out){
  var i_0, size_0;
  size_0 = this$static.array.length;
  out.length < size_0 && (out = createFrom(out, size_0));
  for (i_0 = 0; i_0 < size_0; ++i_0) {
    setCheck(out, i_0, this$static.array[i_0]);
  }
  out.length > size_0 && setCheck(out, size_0, null);
  return out;
}

function ArrayList(){
  $$init_1(this);
}

function ArrayList_0(c){
  $$init_1(this);
  $insertAt(this, 0, c.toArray());
}

function splice(array, index_0, deleteCount){
  array.splice(index_0, deleteCount);
}

function splice_0(array, index_0, deleteCount, value_0){
  array.splice(index_0, deleteCount, value_0);
}

defineClass(8, 777, $intern_37, ArrayList, ArrayList_0);
_.add_0 = function add_6(index_0, o){
  $add(this, index_0, o);
}
;
_.add_1 = function add_7(o){
  return $add_0(this, o);
}
;
_.contains = function contains_4(o){
  return $indexOf_0(this, o, 0) != -1;
}
;
_.get_1 = function get_3(index_0){
  return $get_3(this, index_0);
}
;
_.remove_1 = function remove_9(index_0){
  return $remove_3(this, index_0);
}
;
_.remove = function remove_10(o){
  return $remove_4(this, o);
}
;
_.size_1 = function size_7(){
  return this.array.length;
}
;
_.toArray = function toArray_1(){
  return cloneSubrange(this.array, this.array.length);
}
;
_.toArray_0 = function toArray_2(out){
  return $toArray_0(this, out);
}
;
var Ljava_util_ArrayList_2_classLit = createForClass('java.util', 'ArrayList', 8, Ljava_util_AbstractList_2_classLit);
function equals_32(array1, array2){
  var i_0;
  if (array1 === array2) {
    return true;
  }
  if (array1.length != array2.length) {
    return false;
  }
  for (i_0 = 0; i_0 < array1.length; ++i_0) {
    if (array1[i_0] != array2[i_0]) {
      return false;
    }
  }
  return true;
}

function hashCode_28(a){
  var e, e$index, e$max, hashCode;
  hashCode = 1;
  for (e$index = 0 , e$max = a.length; e$index < e$max; ++e$index) {
    e = a[e$index];
    hashCode = 31 * hashCode + (e != null?hashCode__I__devirtual$(e):0);
    hashCode = ~~hashCode;
  }
  return hashCode;
}

function insertionSort(array, low, high, comp){
  var i_0, j, t;
  for (i_0 = low + 1; i_0 < high; ++i_0) {
    for (j = i_0; j > low && comp.compare(array[j - 1], array[j]) > 0; --j) {
      t = array[j];
      setCheck(array, j, array[j - 1]);
      setCheck(array, j - 1, t);
    }
  }
}

function merge(src_0, srcLow, srcMid, srcHigh, dest, destLow, destHigh, comp){
  var topIdx;
  topIdx = srcMid;
  while (destLow < destHigh) {
    topIdx >= srcHigh || srcLow < srcMid && comp.compare(src_0[srcLow], src_0[topIdx]) <= 0?setCheck(dest, destLow++, src_0[srcLow++]):setCheck(dest, destLow++, src_0[topIdx++]);
  }
}

function mergeSort(x_0, fromIndex, toIndex, comp){
  var temp, newLength, length_0, copy;
  !comp && (comp = ($clinit_Comparators() , $clinit_Comparators() , NATURAL));
  temp = (newLength = (length_0 = toIndex - fromIndex , checkCriticalArgument_1(length_0 >= 0, initValues(getClassLiteralForArray(Ljava_lang_Object_2_classLit, 1), $intern_6, 1, 3, [valueOf(fromIndex), valueOf(toIndex)])) , length_0) , copy = createFrom(x_0, newLength) , arraycopy(x_0, fromIndex, copy, 0, min_0(x_0.length - fromIndex, newLength)) , copy);
  mergeSort_0(temp, x_0, fromIndex, toIndex, -fromIndex, comp);
}

function mergeSort_0(temp, array, low, high, ofs, comp){
  var length_0, tempHigh, tempLow, tempMid;
  length_0 = high - low;
  if (length_0 < 7) {
    insertionSort(array, low, high, comp);
    return;
  }
  tempLow = low + ofs;
  tempHigh = high + ofs;
  tempMid = tempLow + (tempHigh - tempLow >> 1);
  mergeSort_0(array, temp, tempLow, tempMid, -ofs, comp);
  mergeSort_0(array, temp, tempMid, tempHigh, -ofs, comp);
  if (comp.compare(temp[tempMid - 1], temp[tempMid]) <= 0) {
    while (low < high) {
      setCheck(array, low++, temp[tempLow++]);
    }
    return;
  }
  merge(temp, tempLow, tempMid, tempHigh, array, low, high, comp);
}

function toString_188(a){
  var b, i_0;
  if (a == null) {
    return 'null';
  }
  b = new StringBuilder_1('[');
  for (i_0 = 0; i_0 < a.length; i_0++) {
    i_0 != 0 && (b.string += ', ' , b);
    $append_1(b, '' + toString_2(a[i_0]));
  }
  b.string += ']';
  return b.string;
}

function hashCode_29(collection){
  var e, e$iterator, hashCode;
  hashCode = 0;
  for (e$iterator = collection.iterator(); e$iterator.hasNext();) {
    e = e$iterator.next();
    hashCode = hashCode + (e != null?hashCode__I__devirtual$(e):0);
    hashCode = ~~hashCode;
  }
  return hashCode;
}

function hashCode_30(list){
  var e, e$iterator, hashCode;
  hashCode = 1;
  for (e$iterator = new AbstractList$IteratorImpl(list); e$iterator.i < e$iterator.this$01.size_1();) {
    e = (checkCriticalElement(e$iterator.i < e$iterator.this$01.size_1()) , e$iterator.this$01.get_1(e$iterator.last = e$iterator.i++));
    hashCode = 31 * hashCode + (e != null?hashCode__I__devirtual$(e):0);
    hashCode = ~~hashCode;
  }
  return hashCode;
}

function replaceContents(target, x_0){
  var i_0, size_0, previous;
  size_0 = target.array.length;
  for (i_0 = 0; i_0 < size_0; i_0++) {
    previous = (checkElementIndex(i_0, target.array.length) , target.array[i_0]);
    setCheck(target.array, i_0, x_0[i_0]);
  }
}

function sort_0(target, c){
  var x_0;
  x_0 = cloneSubrange(target.array, target.array.length);
  mergeSort(x_0, 0, x_0.length, c);
  replaceContents(target, x_0);
}

function unmodifiableList(list){
  return instanceOf(list, 434)?new Collections$UnmodifiableRandomAccessList(list):new Collections$UnmodifiableList(list);
}

function Collections$UnmodifiableCollection(coll){
  this.coll = coll;
}

defineClass(438, 1, {});
_.add_1 = function add_8(o){
  throw new UnsupportedOperationException;
}
;
_.contains = function contains_5(o){
  return this.coll.contains(o);
}
;
_.iterator = function iterator_5(){
  return new Collections$UnmodifiableCollectionIterator(this.coll.iterator());
}
;
_.removeAll = function removeAll_1(c){
  throw new UnsupportedOperationException;
}
;
_.size_1 = function size_8(){
  return this.coll.size_1();
}
;
_.toArray_0 = function toArray_3(a){
  return this.coll.toArray_0(a);
}
;
_.toString$ = function toString_189(){
  return this.coll.toString$();
}
;
var Ljava_util_Collections$UnmodifiableCollection_2_classLit = createForClass('java.util', 'Collections/UnmodifiableCollection', 438, Ljava_lang_Object_2_classLit);
function Collections$UnmodifiableCollectionIterator(it){
  this.it = it;
}

defineClass(487, 1, {}, Collections$UnmodifiableCollectionIterator);
_.hasNext = function hasNext_3(){
  return this.it.hasNext();
}
;
_.next = function next_4(){
  return this.it.next();
}
;
_.remove_0 = function remove_11(){
  throw new UnsupportedOperationException;
}
;
var Ljava_util_Collections$UnmodifiableCollectionIterator_2_classLit = createForClass('java.util', 'Collections/UnmodifiableCollectionIterator', 487, Ljava_lang_Object_2_classLit);
function Collections$UnmodifiableList(list){
  Collections$UnmodifiableCollection.call(this, list);
  this.list = list;
}

defineClass(439, 438, {43:1}, Collections$UnmodifiableList);
_.equals$ = function equals_33(o){
  return this.list.equals$(o);
}
;
_.get_1 = function get_4(index_0){
  return this.list.get_1(index_0);
}
;
_.hashCode$ = function hashCode_31(){
  return this.list.hashCode$();
}
;
var Ljava_util_Collections$UnmodifiableList_2_classLit = createForClass('java.util', 'Collections/UnmodifiableList', 439, Ljava_util_Collections$UnmodifiableCollection_2_classLit);
function Collections$UnmodifiableMap(map_0){
  this.map_0 = map_0;
}

defineClass(483, 1, $intern_34, Collections$UnmodifiableMap);
_.entrySet_0 = function entrySet_1(){
  !this.entrySet && (this.entrySet = new Collections$UnmodifiableMap$UnmodifiableEntrySet(this.map_0.entrySet_0()));
  return this.entrySet;
}
;
_.equals$ = function equals_34(o){
  return this.map_0.equals$(o);
}
;
_.get_0 = function get_5(key){
  return this.map_0.get_0(key);
}
;
_.hashCode$ = function hashCode_32(){
  return this.map_0.hashCode$();
}
;
_.put = function put_2(key, value_0){
  throw new UnsupportedOperationException;
}
;
_.size_1 = function size_9(){
  return this.map_0.size_1();
}
;
_.toString$ = function toString_190(){
  return this.map_0.toString$();
}
;
var Ljava_util_Collections$UnmodifiableMap_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap', 483, Ljava_lang_Object_2_classLit);
defineClass(484, 438, $intern_35);
_.equals$ = function equals_35(o){
  return this.coll.equals$(o);
}
;
_.hashCode$ = function hashCode_33(){
  return this.coll.hashCode$();
}
;
var Ljava_util_Collections$UnmodifiableSet_2_classLit = createForClass('java.util', 'Collections/UnmodifiableSet', 484, Ljava_util_Collections$UnmodifiableCollection_2_classLit);
function $wrap(array, size_0){
  var i_0;
  for (i_0 = 0; i_0 < size_0; ++i_0) {
    setCheck(array, i_0, new Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry(dynamicCast(array[i_0], 20)));
  }
}

function Collections$UnmodifiableMap$UnmodifiableEntrySet(s){
  Collections$UnmodifiableCollection.call(this, s);
}

defineClass(485, 484, $intern_35, Collections$UnmodifiableMap$UnmodifiableEntrySet);
_.contains = function contains_6(o){
  return this.coll.contains(o);
}
;
_.iterator = function iterator_6(){
  var it;
  it = this.coll.iterator();
  return new Collections$UnmodifiableMap$UnmodifiableEntrySet$1(it);
}
;
_.toArray_0 = function toArray_4(a){
  var result;
  result = this.coll.toArray_0(a);
  $wrap(result, this.coll.size_1());
  return result;
}
;
var Ljava_util_Collections$UnmodifiableMap$UnmodifiableEntrySet_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap/UnmodifiableEntrySet', 485, Ljava_util_Collections$UnmodifiableSet_2_classLit);
function Collections$UnmodifiableMap$UnmodifiableEntrySet$1(val$it){
  this.val$it2 = val$it;
}

defineClass(488, 1, {}, Collections$UnmodifiableMap$UnmodifiableEntrySet$1);
_.hasNext = function hasNext_4(){
  return this.val$it2.hasNext();
}
;
_.next = function next_5(){
  return new Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry(dynamicCast(this.val$it2.next(), 20));
}
;
_.remove_0 = function remove_12(){
  throw new UnsupportedOperationException;
}
;
var Ljava_util_Collections$UnmodifiableMap$UnmodifiableEntrySet$1_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap/UnmodifiableEntrySet/1', 488, Ljava_lang_Object_2_classLit);
function Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry(entry){
  this.entry = entry;
}

defineClass(440, 1, {20:1}, Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry);
_.equals$ = function equals_36(o){
  return this.entry.equals$(o);
}
;
_.getKey = function getKey_0(){
  return this.entry.getKey();
}
;
_.getValue = function getValue_0(){
  return this.entry.getValue();
}
;
_.hashCode$ = function hashCode_34(){
  return this.entry.hashCode$();
}
;
_.setValue = function setValue_1(value_0){
  throw new UnsupportedOperationException;
}
;
_.toString$ = function toString_191(){
  return this.entry.toString$();
}
;
var Ljava_util_Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap/UnmodifiableEntrySet/UnmodifiableEntry', 440, Ljava_lang_Object_2_classLit);
function Collections$UnmodifiableRandomAccessList(list){
  Collections$UnmodifiableList.call(this, list);
}

defineClass(486, 439, {43:1, 434:1}, Collections$UnmodifiableRandomAccessList);
var Ljava_util_Collections$UnmodifiableRandomAccessList_2_classLit = createForClass('java.util', 'Collections/UnmodifiableRandomAccessList', 486, Ljava_util_Collections$UnmodifiableList_2_classLit);
function $clinit_Comparators(){
  $clinit_Comparators = emptyMethod;
  NATURAL = new Comparators$1;
}

var NATURAL;
function $compare_2(o1, o2){
  checkNotNull(o1);
  checkNotNull(o2);
  return compareTo_Ljava_lang_Object__I__devirtual$(dynamicCast(o1, 29), o2);
}

function Comparators$1(){
}

defineClass(719, 1, {}, Comparators$1);
_.compare = function compare_7(o1, o2){
  return $compare_2(o1, o2);
}
;
var Ljava_util_Comparators$1_2_classLit = createForClass('java.util', 'Comparators/1', 719, Ljava_lang_Object_2_classLit);
function checkStructuralChange(host, iterator){
  if (iterator._gwt_modCount != host._gwt_modCount) {
    throw new ConcurrentModificationException;
  }
}

function recordLastKnownStructure(host, iterator){
  setModCount(iterator, host._gwt_modCount);
}

function setModCount(o, modCount){
  o._gwt_modCount = modCount;
}

function structureChanged(map_0){
  var modCount;
  modCount = map_0._gwt_modCount | 0;
  setModCount(map_0, modCount + 1);
}

function ConcurrentModificationException(){
  RuntimeException.call(this);
}

defineClass(530, 10, $intern_2, ConcurrentModificationException);
var Ljava_util_ConcurrentModificationException_2_classLit = createForClass('java.util', 'ConcurrentModificationException', 530, Ljava_lang_RuntimeException_2_classLit);
function $compareTo_5(this$static, other){
  return compare_6(fromDouble(this$static.jsdate.getTime()), fromDouble(other.jsdate.getTime()));
}

function $toString_1(this$static){
  var hourOffset, minuteOffset, offset;
  offset = -this$static.jsdate.getTimezoneOffset();
  hourOffset = (offset >= 0?'+':'') + ~~(offset / 60);
  minuteOffset = (offset < 0?-offset:offset) % 60 < 10?'0' + (offset < 0?-offset:offset) % 60:'' + (offset < 0?-offset:offset) % 60;
  return ($clinit_Date$StringData() , DAYS)[this$static.jsdate.getDay()] + ' ' + MONTHS[this$static.jsdate.getMonth()] + ' ' + padTwo(this$static.jsdate.getDate()) + ' ' + padTwo(this$static.jsdate.getHours()) + ':' + padTwo(this$static.jsdate.getMinutes()) + ':' + padTwo(this$static.jsdate.getSeconds()) + ' GMT' + hourOffset + minuteOffset + ' ' + this$static.jsdate.getFullYear();
}

function Date_0(){
  this.jsdate = new Date;
}

function Date_1(date){
  this.jsdate = create(toDouble(date));
}

function padTwo(number){
  return number < 10?'0' + number:'' + number;
}

defineClass(7, 1, {3:1, 29:1, 7:1}, Date_0, Date_1);
_.compareTo = function compareTo_6(other){
  return $compareTo_5(this, dynamicCast(other, 7));
}
;
_.equals$ = function equals_37(obj){
  return instanceOf(obj, 7) && eq(fromDouble(this.jsdate.getTime()), fromDouble(dynamicCast(obj, 7).jsdate.getTime()));
}
;
_.hashCode$ = function hashCode_35(){
  var time;
  time = fromDouble(this.jsdate.getTime());
  return toInt(xor(time, shru(time, 32)));
}
;
_.toString$ = function toString_192(){
  return $toString_1(this);
}
;
var Ljava_util_Date_2_classLit = createForClass('java.util', 'Date', 7, Ljava_lang_Object_2_classLit);
function $clinit_Date$StringData(){
  $clinit_Date$StringData = emptyMethod;
  DAYS = initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']);
  MONTHS = initValues(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_6, 2, 4, ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']);
}

var DAYS, MONTHS;
function $equals_4(value1, value2){
  return maskUndefined(value1) === maskUndefined(value2) || value1 != null && equals_Ljava_lang_Object__Z__devirtual$(value1, value2);
}

function $getHashCode(key){
  var hashCode;
  hashCode = hashCode__I__devirtual$(key);
  return ~~hashCode;
}

function HashMap(){
  $reset(this);
}

function HashMap_0(ignored){
  checkCriticalArgument_0(ignored >= 0, 'Negative initial capacity');
  checkCriticalArgument_0(true, 'Non-positive load factor');
  $reset(this);
}

defineClass(12, 339, $intern_38, HashMap, HashMap_0);
var Ljava_util_HashMap_2_classLit = createForClass('java.util', 'HashMap', 12, Ljava_util_AbstractHashMap_2_classLit);
function $add_1(this$static, o){
  var old;
  old = $put_1(this$static.map_0, o, this$static);
  return old == null;
}

function $contains_0(this$static, o){
  return $containsKey(this$static.map_0, o);
}

function $remove_5(this$static, o){
  return $remove_0(this$static.map_0, o) != null;
}

function HashSet(){
  this.map_0 = new HashMap;
}

function HashSet_0(c){
  this.map_0 = new HashMap_0(c.size_1());
  $addAll(this, c);
}

defineClass(34, 776, {3:1, 34:1, 92:1}, HashSet, HashSet_0);
_.contains = function contains_7(o){
  return $contains_0(this, o);
}
;
_.iterator = function iterator_7(){
  return $iterator(new AbstractMap$1(this.map_0));
}
;
_.remove = function remove_13(o){
  return $remove_5(this, o);
}
;
_.size_1 = function size_10(){
  return this.map_0.size_0;
}
;
_.toString$ = function toString_193(){
  return $toString(new AbstractMap$1(this.map_0));
}
;
var Ljava_util_HashSet_2_classLit = createForClass('java.util', 'HashSet', 34, Ljava_util_AbstractSet_2_classLit);
function $ensureChain(this$static, hashCode){
  var map_0 = this$static.backingMap;
  return map_0[hashCode] || (map_0[hashCode] = []);
}

function $getChain(this$static, hashCode){
  return this$static.backingMap[hashCode];
}

function $getChainOrEmpty(this$static, hashCode){
  return this$static.backingMap[hashCode] || [];
}

function $getEntry(this$static, key){
  var entry, entry$array, entry$index, entry$max;
  for (entry$array = $getChainOrEmpty(this$static, key == null?'0':'' + $getHashCode(key)) , entry$index = 0 , entry$max = entry$array.length; entry$index < entry$max; ++entry$index) {
    entry = entry$array[entry$index];
    if ($equals_4(key, entry.getKey())) {
      return entry;
    }
  }
  return null;
}

function $keys(this$static){
  return Object.getOwnPropertyNames(this$static.backingMap);
}

function $put_2(this$static, key, value_0){
  var chain, entry, entry$index, entry$max;
  chain = $ensureChain(this$static, key == null?'0':'' + $getHashCode(key));
  for (entry$index = 0 , entry$max = chain.length; entry$index < entry$max; ++entry$index) {
    entry = chain[entry$index];
    if ($equals_4(key, entry.getKey())) {
      return entry.setValue(value_0);
    }
  }
  setCheck(chain, chain.length, new AbstractMap$SimpleEntry(key, value_0));
  $elementAdded(this$static.host);
  return null;
}

function $remove_6(this$static, key){
  var chain, entry, hashCode, i_0;
  hashCode = key == null?'0':'' + $getHashCode(key);
  chain = $getChainOrEmpty(this$static, hashCode);
  for (i_0 = 0; i_0 < chain.length; i_0++) {
    entry = chain[i_0];
    if ($equals_4(key, entry.getKey())) {
      chain.length == 1?(delete this$static.backingMap[hashCode] , undefined):(chain.splice(i_0, 1) , undefined);
      $elementRemoved(this$static.host);
      return entry.getValue();
    }
  }
  return null;
}

function InternalJsHashCodeMap(){
  this.backingMap = this.createMap();
}

defineClass(442, 1, {}, InternalJsHashCodeMap);
_.containsValue = function containsValue_1(value_0){
  var entry, entry$array, entry$index, entry$max, hashCode, hashCode$array, hashCode$index, hashCode$max;
  for (hashCode$array = $keys(this) , hashCode$index = 0 , hashCode$max = hashCode$array.length; hashCode$index < hashCode$max; ++hashCode$index) {
    hashCode = hashCode$array[hashCode$index];
    for (entry$array = this.backingMap[hashCode] , entry$index = 0 , entry$max = entry$array.length; entry$index < entry$max; ++entry$index) {
      entry = entry$array[entry$index];
      if ($equals_4(value_0, entry.getValue())) {
        return true;
      }
    }
  }
  return false;
}
;
_.createMap = function createMap(){
  return Object.create(null);
}
;
_.entries = function entries(){
  return new InternalJsHashCodeMap$1(this);
}
;
var Ljava_util_InternalJsHashCodeMap_2_classLit = createForClass('java.util', 'InternalJsHashCodeMap', 442, Ljava_lang_Object_2_classLit);
function $hasNext_1(this$static){
  if (this$static.itemIndex < this$static.chain.length) {
    return true;
  }
  if (this$static.chainIndex < this$static.keys_0.length - 1) {
    this$static.chain = $getChain(this$static.this$01, this$static.keys_0[++this$static.chainIndex]);
    this$static.itemIndex = 0;
    return true;
  }
  return false;
}

function InternalJsHashCodeMap$1(this$0){
  this.this$01 = this$0;
  this.keys_0 = $keys(this.this$01);
  this.chain = initDim(Ljava_util_Map$Entry_2_classLit, $intern_6, 20, 0, 0, 1);
}

defineClass(502, 1, {}, InternalJsHashCodeMap$1);
_.hasNext = function hasNext_5(){
  return $hasNext_1(this);
}
;
_.next = function next_6(){
  return checkCriticalElement($hasNext_1(this)) , this.lastChain = this.chain , this.lastEntry = this.chain[this.itemIndex++] , this.lastEntry;
}
;
_.remove_0 = function remove_14(){
  checkState(!!this.lastEntry);
  $remove_6(this.this$01, this.lastEntry.getKey());
  maskUndefined(this.chain) === maskUndefined(this.lastChain) && this.chain.length != 1 && --this.itemIndex;
  this.lastEntry = null;
}
;
_.chainIndex = -1;
_.itemIndex = 0;
_.lastChain = null;
_.lastEntry = null;
var Ljava_util_InternalJsHashCodeMap$1_2_classLit = createForClass('java.util', 'InternalJsHashCodeMap/1', 502, Ljava_lang_Object_2_classLit);
function InternalJsHashCodeMap$InternalJsHashCodeMapLegacy(){
  InternalJsHashCodeMap.call(this);
}

defineClass(500, 442, {}, InternalJsHashCodeMap$InternalJsHashCodeMapLegacy);
_.containsValue = function containsValue_2(value_0){
  var map_0 = this.backingMap;
  for (var hashCode in map_0) {
    if (hashCode == parseInt(hashCode, 10)) {
      var array = map_0[hashCode];
      for (var i_0 = 0, c = array.length; i_0 < c; ++i_0) {
        var entry = array[i_0];
        var entryValue = entry.getValue();
        if (this.equalsBridge(value_0, entryValue)) {
          return true;
        }
      }
    }
  }
  return false;
}
;
_.createMap = function createMap_0(){
  return {};
}
;
_.entries = function entries_0(){
  var list = this.newEntryList();
  var map_0 = this.backingMap;
  for (var hashCode in map_0) {
    if (hashCode == parseInt(hashCode, 10)) {
      var array = map_0[hashCode];
      for (var i_0 = 0, c = array.length; i_0 < c; ++i_0) {
        list.add_1(array[i_0]);
      }
    }
  }
  return list.iterator();
}
;
_.equalsBridge = function equalsBridge(value1, value2){
  return maskUndefined(value1) === maskUndefined(value2) || value1 != null && equals_Ljava_lang_Object__Z__devirtual$(value1, value2);
}
;
_.newEntryList = function newEntryList(){
  return new InternalJsHashCodeMap$InternalJsHashCodeMapLegacy$1(this);
}
;
var Ljava_util_InternalJsHashCodeMap$InternalJsHashCodeMapLegacy_2_classLit = createForClass('java.util', 'InternalJsHashCodeMap/InternalJsHashCodeMapLegacy', 500, Ljava_util_InternalJsHashCodeMap_2_classLit);
function InternalJsHashCodeMap$InternalJsHashCodeMapLegacy$1(this$1){
  this.this$11 = this$1;
  ArrayList.call(this);
}

defineClass(501, 8, $intern_37, InternalJsHashCodeMap$InternalJsHashCodeMapLegacy$1);
_.remove_1 = function remove_15(index_0){
  var removed;
  return removed = dynamicCast($remove_3(this, index_0), 20) , $remove_6(this.this$11, removed.getKey()) , removed;
}
;
var Ljava_util_InternalJsHashCodeMap$InternalJsHashCodeMapLegacy$1_2_classLit = createForClass('java.util', 'InternalJsHashCodeMap/InternalJsHashCodeMapLegacy/1', 501, Ljava_util_ArrayList_2_classLit);
function InternalJsMapFactory(){
}

defineClass(497, 1, {}, InternalJsMapFactory);
_.createJsHashCodeMap = function createJsHashCodeMap(){
  return new InternalJsHashCodeMap;
}
;
_.createJsStringMap = function createJsStringMap(){
  return new InternalJsStringMap;
}
;
var Ljava_util_InternalJsMapFactory_2_classLit = createForClass('java.util', 'InternalJsMapFactory', 497, Ljava_lang_Object_2_classLit);
function $clinit_InternalJsMapFactory$BackwardCompatibleJsMapFactory(){
  $clinit_InternalJsMapFactory$BackwardCompatibleJsMapFactory = emptyMethod;
  delegate = createFactory();
}

function canHandleProto(){
  var protoField = '__proto__';
  var map_0 = Object.create(null);
  if (map_0[protoField] !== undefined) {
    return false;
  }
  var keys_0 = Object.getOwnPropertyNames(map_0);
  if (keys_0.length != 0) {
    return false;
  }
  map_0[protoField] = 42;
  if (map_0[protoField] !== 42) {
    return false;
  }
  return true;
}

function createFactory(){
  var map_0;
  if (Object.create && Object.getOwnPropertyNames && canHandleProto()) {
    return (map_0 = Object.create(null) , map_0['__proto__'] = 42 , Object.getOwnPropertyNames(map_0).length == 0)?new InternalJsMapFactory$KeysWorkaroundJsMapFactory:new InternalJsMapFactory;
  }
  return new InternalJsMapFactory$LegacyInternalJsMapFactory;
}

var delegate;
function InternalJsMapFactory$KeysWorkaroundJsMapFactory(){
}

defineClass(499, 497, {}, InternalJsMapFactory$KeysWorkaroundJsMapFactory);
_.createJsStringMap = function createJsStringMap_0(){
  return new InternalJsStringMap$InternalJsStringMapWithKeysWorkaround;
}
;
var Ljava_util_InternalJsMapFactory$KeysWorkaroundJsMapFactory_2_classLit = createForClass('java.util', 'InternalJsMapFactory/KeysWorkaroundJsMapFactory', 499, Ljava_util_InternalJsMapFactory_2_classLit);
function InternalJsMapFactory$LegacyInternalJsMapFactory(){
}

defineClass(498, 497, {}, InternalJsMapFactory$LegacyInternalJsMapFactory);
_.createJsHashCodeMap = function createJsHashCodeMap_0(){
  return new InternalJsHashCodeMap$InternalJsHashCodeMapLegacy;
}
;
_.createJsStringMap = function createJsStringMap_1(){
  return new InternalJsStringMap$InternalJsStringMapLegacy;
}
;
var Ljava_util_InternalJsMapFactory$LegacyInternalJsMapFactory_2_classLit = createForClass('java.util', 'InternalJsMapFactory/LegacyInternalJsMapFactory', 498, Ljava_util_InternalJsMapFactory_2_classLit);
function $containsValue(this$static, value_0){
  var map_0 = this$static.backingMap;
  for (var key in map_0) {
    if (this$static.equalsBridge_0(value_0, map_0[key])) {
      return true;
    }
  }
  return false;
}

function $keys_0(this$static){
  return Object.getOwnPropertyNames(this$static.backingMap);
}

function $put_3(this$static, key, value_0){
  var oldValue;
  oldValue = this$static.backingMap[key];
  oldValue === undefined && $elementAdded(this$static.host);
  $set_0(this$static, key, value_0 === undefined?null:value_0);
  return oldValue;
}

function $remove_7(this$static, key){
  var value_0;
  value_0 = this$static.backingMap[key];
  if (!(value_0 === undefined)) {
    delete this$static.backingMap[key];
    $elementRemoved(this$static.host);
  }
  return value_0;
}

function $set_0(this$static, key, value_0){
  this$static.backingMap[key] = value_0;
}

function InternalJsStringMap(){
  this.backingMap = this.createMap_0();
}

defineClass(341, 1, {}, InternalJsStringMap);
_.containsValue = function containsValue_3(value_0){
  return $containsValue(this, value_0);
}
;
_.createMap_0 = function createMap_1(){
  return Object.create(null);
}
;
_.entries = function entries_1(){
  var keys_0;
  keys_0 = this.keys_1();
  return new InternalJsStringMap$1(this, keys_0);
}
;
_.equalsBridge_0 = function equalsBridge_0(value1, value2){
  return maskUndefined(value1) === maskUndefined(value2) || value1 != null && equals_Ljava_lang_Object__Z__devirtual$(value1, value2);
}
;
_.get_2 = function get_6(key){
  return this.backingMap[key];
}
;
_.keys_1 = function keys_1(){
  return $keys_0(this);
}
;
_.newMapEntry = function newMapEntry(key){
  return new InternalJsStringMap$2(this, key);
}
;
_.put_0 = function put_3(key, value_0){
  return $put_3(this, key, value_0);
}
;
_.remove_2 = function remove_16(key){
  return $remove_7(this, key);
}
;
var Ljava_util_InternalJsStringMap_2_classLit = createForClass('java.util', 'InternalJsStringMap', 341, Ljava_lang_Object_2_classLit);
function InternalJsStringMap$1(this$0, val$keys){
  this.this$01 = this$0;
  this.val$keys2 = val$keys;
}

defineClass(493, 1, {}, InternalJsStringMap$1);
_.hasNext = function hasNext_6(){
  return this.i < this.val$keys2.length;
}
;
_.next = function next_7(){
  return checkCriticalElement(this.i < this.val$keys2.length) , new InternalJsStringMap$2(this.this$01, this.val$keys2[this.last = this.i++]);
}
;
_.remove_0 = function remove_17(){
  checkState(this.last != -1);
  this.this$01.remove_2(this.val$keys2[this.last]);
  this.last = -1;
}
;
_.i = 0;
_.last = -1;
var Ljava_util_InternalJsStringMap$1_2_classLit = createForClass('java.util', 'InternalJsStringMap/1', 493, Ljava_lang_Object_2_classLit);
function InternalJsStringMap$2(this$0, val$key){
  this.this$01 = this$0;
  this.val$key2 = val$key;
}

defineClass(441, 778, {20:1}, InternalJsStringMap$2);
_.getKey = function getKey_1(){
  return this.val$key2;
}
;
_.getValue = function getValue_1(){
  return this.this$01.get_2(this.val$key2);
}
;
_.setValue = function setValue_2(object){
  return this.this$01.put_0(this.val$key2, object);
}
;
var Ljava_util_InternalJsStringMap$2_2_classLit = createForClass('java.util', 'InternalJsStringMap/2', 441, Ljava_util_AbstractMapEntry_2_classLit);
function InternalJsStringMap$InternalJsStringMapLegacy(){
  InternalJsStringMap.call(this);
}

defineClass(490, 341, {}, InternalJsStringMap$InternalJsStringMapLegacy);
_.containsValue = function containsValue_4(value_0){
  var map_0 = this.backingMap;
  for (var key in map_0) {
    if (key.charCodeAt(0) == 58) {
      var entryValue = map_0[key];
      if (this.equalsBridge_0(value_0, entryValue)) {
        return true;
      }
    }
  }
  return false;
}
;
_.createMap_0 = function createMap_2(){
  return {};
}
;
_.entries = function entries_2(){
  var list = this.newEntryList_0();
  for (var key in this.backingMap) {
    if (key.charCodeAt(0) == 58) {
      var entry = this.newMapEntry(key.substring(1));
      list.add_1(entry);
    }
  }
  return list.iterator();
}
;
_.get_2 = function get_7(key){
  return this.backingMap[':' + key];
}
;
_.newEntryList_0 = function newEntryList_0(){
  return new InternalJsStringMap$InternalJsStringMapLegacy$1(this);
}
;
_.put_0 = function put_4(key, value_0){
  return $put_3(this, ':' + key, value_0);
}
;
_.remove_2 = function remove_18(key){
  return $remove_7(this, ':' + key);
}
;
var Ljava_util_InternalJsStringMap$InternalJsStringMapLegacy_2_classLit = createForClass('java.util', 'InternalJsStringMap/InternalJsStringMapLegacy', 490, Ljava_util_InternalJsStringMap_2_classLit);
function InternalJsStringMap$InternalJsStringMapLegacy$1(this$1){
  this.this$11 = this$1;
  ArrayList.call(this);
}

defineClass(492, 8, $intern_37, InternalJsStringMap$InternalJsStringMapLegacy$1);
_.remove_1 = function remove_19(index_0){
  var removed;
  return removed = dynamicCast($remove_3(this, index_0), 20) , $remove_7(this.this$11, ':' + dynamicCastToString(removed.getKey())) , removed;
}
;
var Ljava_util_InternalJsStringMap$InternalJsStringMapLegacy$1_2_classLit = createForClass('java.util', 'InternalJsStringMap/InternalJsStringMapLegacy/1', 492, Ljava_util_ArrayList_2_classLit);
function InternalJsStringMap$InternalJsStringMapWithKeysWorkaround(){
  InternalJsStringMap.call(this);
}

defineClass(491, 341, {}, InternalJsStringMap$InternalJsStringMapWithKeysWorkaround);
_.containsValue = function containsValue_5(value_0){
  var protoValue;
  protoValue = this.backingMap['__proto__'];
  if (!(protoValue === undefined) && (maskUndefined(value_0) === maskUndefined(protoValue) || value_0 != null && equals_Ljava_lang_Object__Z__devirtual$(value_0, protoValue))) {
    return true;
  }
  return $containsValue(this, value_0);
}
;
_.keys_1 = function keys_2(){
  var keys_0;
  keys_0 = $keys_0(this);
  !(this.backingMap['__proto__'] === undefined) && (keys_0[keys_0.length] = '__proto__');
  return keys_0;
}
;
var Ljava_util_InternalJsStringMap$InternalJsStringMapWithKeysWorkaround_2_classLit = createForClass('java.util', 'InternalJsStringMap/InternalJsStringMapWithKeysWorkaround', 491, Ljava_util_InternalJsStringMap_2_classLit);
var Ljava_util_Map$Entry_2_classLit = createForInterface('java.util', 'Map/Entry');
function NoSuchElementException(){
  RuntimeException.call(this);
}

defineClass(444, 10, $intern_2, NoSuchElementException);
var Ljava_util_NoSuchElementException_2_classLit = createForClass('java.util', 'NoSuchElementException', 444, Ljava_lang_RuntimeException_2_classLit);
function equals_38(a, b){
  return maskUndefined(a) === maskUndefined(b) || a != null && equals_Ljava_lang_Object__Z__devirtual$(a, b);
}

function hashCode_36(o){
  return o != null?hashCode__I__devirtual$(o):0;
}

function $clinit_Random(){
  $clinit_Random = emptyMethod;
  var i_0, i0, twoToTheXMinus24Tmp, twoToTheXMinus48Tmp;
  twoToTheXMinus24 = initDim(D_classLit, $intern_4, 0, 25, 7, 1);
  twoToTheXMinus48 = initDim(D_classLit, $intern_4, 0, 33, 7, 1);
  twoToTheXMinus48Tmp = $intern_30;
  for (i0 = 32; i0 >= 0; i0--) {
    twoToTheXMinus48[i0] = twoToTheXMinus48Tmp;
    twoToTheXMinus48Tmp *= 0.5;
  }
  twoToTheXMinus24Tmp = 1;
  for (i_0 = 24; i_0 >= 0; i_0--) {
    twoToTheXMinus24[i_0] = twoToTheXMinus24Tmp;
    twoToTheXMinus24Tmp *= 0.5;
  }
}

function $nextBytes(this$static, buf){
  var count, loop, rand;
  checkNotNull(buf);
  rand = 0;
  count = 0;
  loop = 0;
  while (count < buf.length) {
    if (loop == 0) {
      rand = round_int($nextInternal(this$static, 32));
      loop = 3;
    }
     else {
      --loop;
    }
    buf[count++] = narrow_byte(rand);
    rand >>= 8;
  }
}

function $nextInt(this$static, n){
  var bits, val;
  checkCriticalArgument(n > 0);
  if ((n & -n) == n) {
    return round_int(n * $nextInternal(this$static, 31) * 4.6566128730773926E-10);
  }
  do {
    bits = $nextInternal(this$static, 31);
    val = bits % n;
  }
   while (bits - val + (n - 1) < 0);
  return round_int(val);
}

function $nextInternal(this$static, bits){
  var carry, dval, h, hi, l, lo;
  hi = this$static.seedhi * $intern_39 + this$static.seedlo * 1502;
  lo = this$static.seedlo * $intern_39 + 11;
  carry = Math.floor(lo * $intern_26);
  hi += carry;
  lo -= carry * $intern_40;
  hi %= $intern_40;
  this$static.seedhi = hi;
  this$static.seedlo = lo;
  if (bits <= 24) {
    return floor_0(this$static.seedhi * twoToTheXMinus24[bits]);
  }
   else {
    h = this$static.seedhi * (1 << bits - 24);
    l = floor_0(this$static.seedlo * twoToTheXMinus48[bits]);
    dval = h + l;
    dval >= 2147483648 && (dval -= 4294967296);
    return dval;
  }
}

function $nextLong(this$static){
  return add_0(shl(fromDouble($nextInternal(this$static, 32)), 32), fromDouble($nextInternal(this$static, 32)));
}

function Random(){
  $clinit_Random();
  var hi, lo, seed;
  seed = uniqueSeed++ + now_1();
  hi = round_int(Math.floor(seed * $intern_26)) & 16777215;
  lo = round_int(seed - hi * $intern_40);
  this.seedhi = hi ^ 1502;
  this.seedlo = lo ^ $intern_39;
}

defineClass(120, 1, {}, Random);
_.seedhi = 0;
_.seedlo = 0;
var twoToTheXMinus24, twoToTheXMinus48, uniqueSeed = 0;
var Ljava_util_Random_2_classLit = createForClass('java.util', 'Random', 120, Ljava_lang_Object_2_classLit);
function $clear_3(this$static){
  this$static.root = null;
  this$static.size_0 = 0;
}

function $getEntry_0(this$static, key){
  var c, childNum, tree;
  tree = this$static.root;
  while (tree) {
    c = $compare_2(key, tree.key);
    if (c == 0) {
      return tree;
    }
    childNum = c < 0?0:1;
    tree = tree.child[childNum];
  }
  return null;
}

function $getFirstEntry(this$static){
  var nextNode, node;
  if (!this$static.root) {
    return null;
  }
  node = this$static.root;
  while (nextNode = node.child[0]) {
    node = nextNode;
  }
  return node;
}

function $inOrderAdd(this$static, list, type_0, current, fromKey, fromInclusive, toKey, toInclusive){
  var leftNode, rightNode;
  if (!current) {
    return;
  }
  leftNode = current.child[0];
  !!leftNode && $inOrderAdd(this$static, list, type_0, leftNode, fromKey, fromInclusive, toKey, toInclusive);
  $inRange(this$static, type_0, current.key, fromKey, fromInclusive, toKey, toInclusive) && list.add_1(current);
  rightNode = current.child[1];
  !!rightNode && $inOrderAdd(this$static, list, type_0, rightNode, fromKey, fromInclusive, toKey, toInclusive);
}

function $inRange(this$static, type_0, key, fromKey, fromInclusive, toKey, toInclusive){
  var compare, compare_0;
  if (type_0.fromKeyValid() && (compare = $compare_2(key, fromKey) , compare < 0 || !fromInclusive && compare == 0)) {
    return false;
  }
  if (type_0.toKeyValid() && (compare_0 = $compare_2(key, toKey) , compare_0 > 0 || !toInclusive && compare_0 == 0)) {
    return false;
  }
  return true;
}

function $insert_0(this$static, tree, newNode, state){
  var c, childNum;
  if (!tree) {
    return newNode;
  }
   else {
    c = $compare_2(newNode.key, tree.key);
    if (c == 0) {
      state.value_0 = $setValue(tree, newNode.value_0);
      state.found = true;
      return tree;
    }
    childNum = c < 0?0:1;
    tree.child[childNum] = $insert_0(this$static, tree.child[childNum], newNode, state);
    if ($isRed(tree.child[childNum])) {
      if ($isRed(tree.child[1 - childNum])) {
        tree.isRed = true;
        tree.child[0].isRed = false;
        tree.child[1].isRed = false;
      }
       else {
        $isRed(tree.child[childNum].child[childNum])?(tree = $rotateSingle(tree, 1 - childNum)):$isRed(tree.child[childNum].child[1 - childNum]) && (tree = $rotateDouble(tree, 1 - childNum));
      }
    }
  }
  return tree;
}

function $isRed(node){
  return !!node && node.isRed;
}

function $put_4(this$static, key, value_0){
  var node, state;
  node = new TreeMap$Node(key, value_0);
  state = new TreeMap$State;
  this$static.root = $insert_0(this$static, this$static.root, node, state);
  state.found || ++this$static.size_0;
  this$static.root.isRed = false;
  return state.value_0;
}

function $remove_8(this$static, k_0){
  var state;
  state = new TreeMap$State;
  $removeWithState(this$static, k_0, state);
  return state.value_0;
}

function $removeEntry(this$static, entry){
  var state;
  state = new TreeMap$State;
  state.matchValue = true;
  state.value_0 = entry.getValue();
  return $removeWithState(this$static, entry.getKey(), state);
}

function $removeWithState(this$static, key, state){
  var c, dir_0, dir2, found, grandparent, head, last, newNode, node, parent_0, sibling;
  if (!this$static.root) {
    return false;
  }
  found = null;
  parent_0 = null;
  head = new TreeMap$Node(null, null);
  dir_0 = 1;
  head.child[1] = this$static.root;
  node = head;
  while (node.child[dir_0]) {
    last = dir_0;
    grandparent = parent_0;
    parent_0 = node;
    node = node.child[dir_0];
    c = $compare_2(key, node.key);
    dir_0 = c < 0?0:1;
    c == 0 && (!state.matchValue || equals_38(node.value_0, state.value_0)) && (found = node);
    if (!(!!node && node.isRed) && !$isRed(node.child[dir_0])) {
      if ($isRed(node.child[1 - dir_0])) {
        parent_0 = parent_0.child[last] = $rotateSingle(node, dir_0);
      }
       else if (!$isRed(node.child[1 - dir_0])) {
        sibling = parent_0.child[1 - last];
        if (sibling) {
          if (!$isRed(sibling.child[1 - last]) && !$isRed(sibling.child[last])) {
            parent_0.isRed = false;
            sibling.isRed = true;
            node.isRed = true;
          }
           else {
            dir2 = grandparent.child[1] == parent_0?1:0;
            $isRed(sibling.child[last])?(grandparent.child[dir2] = $rotateDouble(parent_0, last)):$isRed(sibling.child[1 - last]) && (grandparent.child[dir2] = $rotateSingle(parent_0, last));
            node.isRed = grandparent.child[dir2].isRed = true;
            grandparent.child[dir2].child[0].isRed = false;
            grandparent.child[dir2].child[1].isRed = false;
          }
        }
      }
    }
  }
  if (found) {
    state.found = true;
    state.value_0 = found.value_0;
    if (node != found) {
      newNode = new TreeMap$Node(node.key, node.value_0);
      $replaceNode(this$static, head, found, newNode);
      parent_0 == found && (parent_0 = newNode);
    }
    parent_0.child[parent_0.child[1] == node?1:0] = node.child[!node.child[0]?1:0];
    --this$static.size_0;
  }
  this$static.root = head.child[1];
  !!this$static.root && (this$static.root.isRed = false);
  return state.found;
}

function $replaceNode(this$static, head, node, newNode){
  var direction, parent_0;
  parent_0 = head;
  direction = parent_0.key == null || $compare_2(node.key, parent_0.key) > 0?1:0;
  while (parent_0.child[direction] != node) {
    parent_0 = parent_0.child[direction];
    direction = $compare_2(node.key, parent_0.key) > 0?1:0;
  }
  parent_0.child[direction] = newNode;
  newNode.isRed = node.isRed;
  newNode.child[0] = node.child[0];
  newNode.child[1] = node.child[1];
  node.child[0] = null;
  node.child[1] = null;
}

function $rotateDouble(tree, rotateDirection){
  var otherChildDir;
  otherChildDir = 1 - rotateDirection;
  tree.child[otherChildDir] = $rotateSingle(tree.child[otherChildDir], otherChildDir);
  return $rotateSingle(tree, rotateDirection);
}

function $rotateSingle(tree, rotateDirection){
  var otherChildDir, save;
  otherChildDir = 1 - rotateDirection;
  save = tree.child[otherChildDir];
  tree.child[otherChildDir] = save.child[rotateDirection];
  save.child[rotateDirection] = tree;
  tree.isRed = true;
  save.isRed = false;
  return save;
}

function TreeMap(){
  TreeMap_0.call(this, null);
}

function TreeMap_0(c){
  this.root = null;
  !c && (c = ($clinit_Comparators() , $clinit_Comparators() , NATURAL));
  this.cmp = c;
}

defineClass(365, 793, $intern_38, TreeMap);
_.entrySet_0 = function entrySet_2(){
  return new TreeMap$EntrySet(this);
}
;
_.put = function put_5(key, value_0){
  return $put_4(this, key, value_0);
}
;
_.size_1 = function size_11(){
  return this.size_0;
}
;
_.size_0 = 0;
var Ljava_util_TreeMap_2_classLit = createForClass('java.util', 'TreeMap', 365, Ljava_util_AbstractNavigableMap_2_classLit);
function $remove_9(this$static){
  $remove_2(this$static.iter);
  $removeEntry(this$static.this$01, this$static.last);
  this$static.last = null;
}

function TreeMap$EntryIterator(this$0){
  TreeMap$EntryIterator_0.call(this, this$0, ($clinit_TreeMap$SubMapType() , All));
}

function TreeMap$EntryIterator_0(this$0, type_0){
  var list;
  this.this$01 = this$0;
  list = new ArrayList;
  $inOrderAdd(this$0, list, type_0, this$0.root, null, false, null, false);
  this.iter = new AbstractList$ListIteratorImpl(list);
}

defineClass(275, 1, {}, TreeMap$EntryIterator);
_.hasNext = function hasNext_7(){
  return $hasNext_0(this.iter);
}
;
_.next = function next_8(){
  return this.last = dynamicCast($next_0(this.iter), 20);
}
;
_.remove_0 = function remove_20(){
  $remove_9(this);
}
;
var Ljava_util_TreeMap$EntryIterator_2_classLit = createForClass('java.util', 'TreeMap/EntryIterator', 275, Ljava_lang_Object_2_classLit);
function TreeMap$EntrySet(this$0){
  AbstractNavigableMap$EntrySet.call(this, this$0);
}

defineClass(366, 461, $intern_35, TreeMap$EntrySet);
var Ljava_util_TreeMap$EntrySet_2_classLit = createForClass('java.util', 'TreeMap/EntrySet', 366, Ljava_util_AbstractNavigableMap$EntrySet_2_classLit);
function TreeMap$Node(key, value_0){
  AbstractMap$SimpleEntry.call(this, key, value_0);
  this.child = initDim(Ljava_util_TreeMap$Node_2_classLit, $intern_6, 135, 2, 0, 1);
  this.isRed = true;
}

defineClass(135, 223, {118:1, 223:1, 20:1, 135:1}, TreeMap$Node);
_.isRed = false;
var Ljava_util_TreeMap$Node_2_classLit = createForClass('java.util', 'TreeMap/Node', 135, Ljava_util_AbstractMap$SimpleEntry_2_classLit);
function TreeMap$State(){
}

defineClass(367, 1, {}, TreeMap$State);
_.toString$ = function toString_194(){
  return 'State: mv=' + this.matchValue + ' value=' + this.value_0 + ' done=' + this.done + ' found=' + this.found;
}
;
_.done = false;
_.found = false;
_.matchValue = false;
var Ljava_util_TreeMap$State_2_classLit = createForClass('java.util', 'TreeMap/State', 367, Ljava_lang_Object_2_classLit);
function $clinit_TreeMap$SubMapType(){
  $clinit_TreeMap$SubMapType = emptyMethod;
  All = new TreeMap$SubMapType('All', 0);
  Head = new TreeMap$SubMapType$1;
  Range_0 = new TreeMap$SubMapType$2;
  Tail = new TreeMap$SubMapType$3;
}

function TreeMap$SubMapType(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_13(){
  $clinit_TreeMap$SubMapType();
  return initValues(getClassLiteralForArray(Ljava_util_TreeMap$SubMapType_2_classLit, 1), $intern_6, 76, 0, [All, Head, Range_0, Tail]);
}

defineClass(76, 18, $intern_41, TreeMap$SubMapType);
_.fromKeyValid = function fromKeyValid(){
  return false;
}
;
_.toKeyValid = function toKeyValid(){
  return false;
}
;
var All, Head, Range_0, Tail;
var Ljava_util_TreeMap$SubMapType_2_classLit = createForEnum('java.util', 'TreeMap/SubMapType', 76, Ljava_lang_Enum_2_classLit, values_13);
function TreeMap$SubMapType$1(){
  TreeMap$SubMapType.call(this, 'Head', 1);
}

defineClass(714, 76, $intern_41, TreeMap$SubMapType$1);
_.toKeyValid = function toKeyValid_0(){
  return true;
}
;
var Ljava_util_TreeMap$SubMapType$1_2_classLit = createForEnum('java.util', 'TreeMap/SubMapType/1', 714, Ljava_util_TreeMap$SubMapType_2_classLit, null);
function TreeMap$SubMapType$2(){
  TreeMap$SubMapType.call(this, 'Range', 2);
}

defineClass(715, 76, $intern_41, TreeMap$SubMapType$2);
_.fromKeyValid = function fromKeyValid_0(){
  return true;
}
;
_.toKeyValid = function toKeyValid_1(){
  return true;
}
;
var Ljava_util_TreeMap$SubMapType$2_2_classLit = createForEnum('java.util', 'TreeMap/SubMapType/2', 715, Ljava_util_TreeMap$SubMapType_2_classLit, null);
function TreeMap$SubMapType$3(){
  TreeMap$SubMapType.call(this, 'Tail', 3);
}

defineClass(716, 76, $intern_41, TreeMap$SubMapType$3);
_.fromKeyValid = function fromKeyValid_1(){
  return true;
}
;
var Ljava_util_TreeMap$SubMapType$3_2_classLit = createForEnum('java.util', 'TreeMap/SubMapType/3', 716, Ljava_util_TreeMap$SubMapType_2_classLit, null);
function $clinit_Level(){
  $clinit_Level = emptyMethod;
  ALL = new Level$LevelAll;
  CONFIG = new Level$LevelConfig;
  FINE = new Level$LevelFine;
  FINER = new Level$LevelFiner;
  FINEST = new Level$LevelFinest;
  INFO = new Level$LevelInfo;
  OFF = new Level$LevelOff;
  SEVERE = new Level$LevelSevere;
  WARNING = new Level$LevelWarning;
}

defineClass(782, 1, $intern_4);
_.getName = function getName_0(){
  return 'DUMMY';
}
;
_.intValue = function intValue(){
  return -1;
}
;
_.toString$ = function toString_195(){
  return this.getName();
}
;
var ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING;
var Ljava_util_logging_Level_2_classLit = createForClass('java.util.logging', 'Level', 782, Ljava_lang_Object_2_classLit);
function Level$LevelAll(){
}

defineClass(507, 782, $intern_4, Level$LevelAll);
_.getName = function getName_1(){
  return 'ALL';
}
;
_.intValue = function intValue_0(){
  return $intern_29;
}
;
var Ljava_util_logging_Level$LevelAll_2_classLit = createForClass('java.util.logging', 'Level/LevelAll', 507, Ljava_util_logging_Level_2_classLit);
function Level$LevelConfig(){
}

defineClass(508, 782, $intern_4, Level$LevelConfig);
_.getName = function getName_2(){
  return 'CONFIG';
}
;
_.intValue = function intValue_1(){
  return 700;
}
;
var Ljava_util_logging_Level$LevelConfig_2_classLit = createForClass('java.util.logging', 'Level/LevelConfig', 508, Ljava_util_logging_Level_2_classLit);
function Level$LevelFine(){
}

defineClass(509, 782, $intern_4, Level$LevelFine);
_.getName = function getName_3(){
  return 'FINE';
}
;
_.intValue = function intValue_2(){
  return 500;
}
;
var Ljava_util_logging_Level$LevelFine_2_classLit = createForClass('java.util.logging', 'Level/LevelFine', 509, Ljava_util_logging_Level_2_classLit);
function Level$LevelFiner(){
}

defineClass(510, 782, $intern_4, Level$LevelFiner);
_.getName = function getName_4(){
  return 'FINER';
}
;
_.intValue = function intValue_3(){
  return 400;
}
;
var Ljava_util_logging_Level$LevelFiner_2_classLit = createForClass('java.util.logging', 'Level/LevelFiner', 510, Ljava_util_logging_Level_2_classLit);
function Level$LevelFinest(){
}

defineClass(511, 782, $intern_4, Level$LevelFinest);
_.getName = function getName_5(){
  return 'FINEST';
}
;
_.intValue = function intValue_4(){
  return 300;
}
;
var Ljava_util_logging_Level$LevelFinest_2_classLit = createForClass('java.util.logging', 'Level/LevelFinest', 511, Ljava_util_logging_Level_2_classLit);
function Level$LevelInfo(){
}

defineClass(512, 782, $intern_4, Level$LevelInfo);
_.getName = function getName_6(){
  return 'INFO';
}
;
_.intValue = function intValue_5(){
  return 800;
}
;
var Ljava_util_logging_Level$LevelInfo_2_classLit = createForClass('java.util.logging', 'Level/LevelInfo', 512, Ljava_util_logging_Level_2_classLit);
function Level$LevelOff(){
}

defineClass(513, 782, $intern_4, Level$LevelOff);
_.getName = function getName_7(){
  return 'OFF';
}
;
_.intValue = function intValue_6(){
  return $intern_0;
}
;
var Ljava_util_logging_Level$LevelOff_2_classLit = createForClass('java.util.logging', 'Level/LevelOff', 513, Ljava_util_logging_Level_2_classLit);
function Level$LevelSevere(){
}

defineClass(514, 782, $intern_4, Level$LevelSevere);
_.getName = function getName_8(){
  return 'SEVERE';
}
;
_.intValue = function intValue_7(){
  return 1000;
}
;
var Ljava_util_logging_Level$LevelSevere_2_classLit = createForClass('java.util.logging', 'Level/LevelSevere', 514, Ljava_util_logging_Level_2_classLit);
function Level$LevelWarning(){
}

defineClass(515, 782, $intern_4, Level$LevelWarning);
_.getName = function getName_9(){
  return 'WARNING';
}
;
_.intValue = function intValue_8(){
  return 900;
}
;
var Ljava_util_logging_Level$LevelWarning_2_classLit = createForClass('java.util.logging', 'Level/LevelWarning', 515, Ljava_util_logging_Level_2_classLit);
function $addLoggerImpl(this$static, logger){
  $putStringValue(this$static.loggerMap, logger.impl.name_0, logger);
}

function $ensureLogger(this$static, name_0){
  var logger, newLogger, name_1, parentName;
  logger = dynamicCast($getStringValue(this$static.loggerMap, name_0), 224);
  if (!logger) {
    newLogger = new Logger(name_0);
    name_1 = newLogger.impl.name_0;
    parentName = $substring_0(name_1, 0, max_0($lastIndexOf(name_1, fromCodePoint(46))));
    $setParent_0(newLogger, $ensureLogger(this$static, parentName));
    $putStringValue(this$static.loggerMap, newLogger.impl.name_0, newLogger);
    return newLogger;
  }
  return logger;
}

function LogManager(){
  this.loggerMap = new HashMap;
}

function getLogManager(){
  var rootLogger;
  if (!singleton) {
    singleton = new LogManager;
    rootLogger = new Logger('');
    $setLevel_1(rootLogger, ($clinit_Level() , INFO));
    $addLoggerImpl(singleton, rootLogger);
  }
  return singleton;
}

defineClass(494, 1, {}, LogManager);
var singleton;
var Ljava_util_logging_LogManager_2_classLit = createForClass('java.util.logging', 'LogManager', 494, Ljava_lang_Object_2_classLit);
function $setLoggerName(this$static, newName){
  this$static.loggerName = newName;
}

function LogRecord(msg){
  this.msg = msg;
  this.millis = ($clinit_System() , fromDouble(now_1()));
}

defineClass(521, 1, $intern_4, LogRecord);
_.loggerName = '';
_.millis = {l:0, m:0, h:0};
_.thrown = null;
var Ljava_util_logging_LogRecord_2_classLit = createForClass('java.util.logging', 'LogRecord', 521, Ljava_lang_Object_2_classLit);
function $log_1(this$static, msg, thrown){
  $log(this$static.impl, msg, thrown);
}

function $setLevel_1(this$static, newLevel){
  $setLevel_0(this$static.impl, newLevel);
}

function $setParent_0(this$static, newParent){
  $setParent(this$static.impl, newParent);
}

function Logger(name_0){
  this.impl = new LoggerImplRegular;
  $setName(this.impl, name_0);
}

function getLogger(name_0){
  new LoggerImplRegular;
  return $ensureLogger(getLogManager(), name_0);
}

defineClass(224, 1, {224:1}, Logger);
var Ljava_util_logging_Logger_2_classLit = createForClass('java.util.logging', 'Logger', 224, Ljava_lang_Object_2_classLit);
function $finish(this$static){
  var bitLength;
  bitLength = shl(this$static.byteCount, 3);
  $update_2(this$static, -128);
  while (this$static.xBufOff != 0) {
    $update_2(this$static, 0);
  }
  this$static.processLength(bitLength);
  this$static.processBlock();
}

function $reset_0(this$static){
  var i_0;
  this$static.byteCount = {l:0, m:0, h:0};
  this$static.xBufOff = 0;
  for (i_0 = 0; i_0 < this$static.xBuf.length; i_0++) {
    this$static.xBuf[i_0] = 0;
  }
}

function $update_2(this$static, in_$){
  this$static.xBuf[this$static.xBufOff++] = in_$;
  if (this$static.xBufOff == this$static.xBuf.length) {
    this$static.processWord(this$static.xBuf, 0);
    this$static.xBufOff = 0;
  }
  this$static.byteCount = add_0(this$static.byteCount, {l:1, m:0, h:0});
}

function $update_3(this$static, in_$, inOff, len){
  while (this$static.xBufOff != 0 && len > 0) {
    $update_2(this$static, in_$[inOff]);
    ++inOff;
    --len;
  }
  while (len > this$static.xBuf.length) {
    this$static.processWord(in_$, inOff);
    inOff += this$static.xBuf.length;
    len -= this$static.xBuf.length;
    this$static.byteCount = add_0(this$static.byteCount, fromInt(this$static.xBuf.length));
  }
  while (len > 0) {
    $update_2(this$static, in_$[inOff]);
    ++inOff;
    --len;
  }
}

function GeneralDigest(){
  this.xBuf = initDim(B_classLit, $intern_17, 0, 4, 7, 1);
  this.xBufOff = 0;
}

defineClass(456, 1, {});
_.byteCount = {l:0, m:0, h:0};
_.xBufOff = 0;
var Lorg_bouncycastle_crypto_digests_GeneralDigest_2_classLit = createForClass('org.bouncycastle.crypto.digests', 'GeneralDigest', 456, Ljava_lang_Object_2_classLit);
function $processBlock(this$static){
  var a, b, c, d, i_0;
  a = this$static.H1;
  b = this$static.H2;
  c = this$static.H3;
  d = this$static.H4;
  a = $rotateLeft(a + (b & c | ~b & d) + this$static.X[0] + -680876936, 7) + b;
  d = $rotateLeft(d + (a & b | ~a & c) + this$static.X[1] + -389564586, 12) + a;
  c = $rotateLeft(c + (d & a | ~d & b) + this$static.X[2] + 606105819, 17) + d;
  b = $rotateLeft(b + (c & d | ~c & a) + this$static.X[3] + -1044525330, 22) + c;
  a = $rotateLeft(a + (b & c | ~b & d) + this$static.X[4] + -176418897, 7) + b;
  d = $rotateLeft(d + (a & b | ~a & c) + this$static.X[5] + 1200080426, 12) + a;
  c = $rotateLeft(c + (d & a | ~d & b) + this$static.X[6] + -1473231341, 17) + d;
  b = $rotateLeft(b + (c & d | ~c & a) + this$static.X[7] + -45705983, 22) + c;
  a = $rotateLeft(a + (b & c | ~b & d) + this$static.X[8] + 1770035416, 7) + b;
  d = $rotateLeft(d + (a & b | ~a & c) + this$static.X[9] + -1958414417, 12) + a;
  c = $rotateLeft(c + (d & a | ~d & b) + this$static.X[10] + -42063, 17) + d;
  b = $rotateLeft(b + (c & d | ~c & a) + this$static.X[11] + -1990404162, 22) + c;
  a = $rotateLeft(a + (b & c | ~b & d) + this$static.X[12] + 1804603682, 7) + b;
  d = $rotateLeft(d + (a & b | ~a & c) + this$static.X[13] + -40341101, 12) + a;
  c = $rotateLeft(c + (d & a | ~d & b) + this$static.X[14] + -1502002290, 17) + d;
  b = $rotateLeft(b + (c & d | ~c & a) + this$static.X[15] + 1236535329, 22) + c;
  a = $rotateLeft(a + (b & d | c & ~d) + this$static.X[1] + -165796510, 5) + b;
  d = $rotateLeft(d + (a & c | b & ~c) + this$static.X[6] + -1069501632, 9) + a;
  c = $rotateLeft(c + (d & b | a & ~b) + this$static.X[11] + 643717713, 14) + d;
  b = $rotateLeft(b + (c & a | d & ~a) + this$static.X[0] + -373897302, 20) + c;
  a = $rotateLeft(a + (b & d | c & ~d) + this$static.X[5] + -701558691, 5) + b;
  d = $rotateLeft(d + (a & c | b & ~c) + this$static.X[10] + 38016083, 9) + a;
  c = $rotateLeft(c + (d & b | a & ~b) + this$static.X[15] + -660478335, 14) + d;
  b = $rotateLeft(b + (c & a | d & ~a) + this$static.X[4] + -405537848, 20) + c;
  a = $rotateLeft(a + (b & d | c & ~d) + this$static.X[9] + 568446438, 5) + b;
  d = $rotateLeft(d + (a & c | b & ~c) + this$static.X[14] + -1019803690, 9) + a;
  c = $rotateLeft(c + (d & b | a & ~b) + this$static.X[3] + -187363961, 14) + d;
  b = $rotateLeft(b + (c & a | d & ~a) + this$static.X[8] + 1163531501, 20) + c;
  a = $rotateLeft(a + (b & d | c & ~d) + this$static.X[13] + -1444681467, 5) + b;
  d = $rotateLeft(d + (a & c | b & ~c) + this$static.X[2] + -51403784, 9) + a;
  c = $rotateLeft(c + (d & b | a & ~b) + this$static.X[7] + 1735328473, 14) + d;
  b = $rotateLeft(b + (c & a | d & ~a) + this$static.X[12] + -1926607734, 20) + c;
  a = $rotateLeft(a + (b ^ c ^ d) + this$static.X[5] + -378558, 4) + b;
  d = $rotateLeft(d + (a ^ b ^ c) + this$static.X[8] + -2022574463, 11) + a;
  c = $rotateLeft(c + (d ^ a ^ b) + this$static.X[11] + 1839030562, 16) + d;
  b = $rotateLeft(b + (c ^ d ^ a) + this$static.X[14] + -35309556, 23) + c;
  a = $rotateLeft(a + (b ^ c ^ d) + this$static.X[1] + -1530992060, 4) + b;
  d = $rotateLeft(d + (a ^ b ^ c) + this$static.X[4] + 1272893353, 11) + a;
  c = $rotateLeft(c + (d ^ a ^ b) + this$static.X[7] + -155497632, 16) + d;
  b = $rotateLeft(b + (c ^ d ^ a) + this$static.X[10] + -1094730640, 23) + c;
  a = $rotateLeft(a + (b ^ c ^ d) + this$static.X[13] + 681279174, 4) + b;
  d = $rotateLeft(d + (a ^ b ^ c) + this$static.X[0] + -358537222, 11) + a;
  c = $rotateLeft(c + (d ^ a ^ b) + this$static.X[3] + -722521979, 16) + d;
  b = $rotateLeft(b + (c ^ d ^ a) + this$static.X[6] + 76029189, 23) + c;
  a = $rotateLeft(a + (b ^ c ^ d) + this$static.X[9] + -640364487, 4) + b;
  d = $rotateLeft(d + (a ^ b ^ c) + this$static.X[12] + -421815835, 11) + a;
  c = $rotateLeft(c + (d ^ a ^ b) + this$static.X[15] + 530742520, 16) + d;
  b = $rotateLeft(b + (c ^ d ^ a) + this$static.X[2] + -995338651, 23) + c;
  a = $rotateLeft(a + (c ^ (b | ~d)) + this$static.X[0] + -198630844, 6) + b;
  d = $rotateLeft(d + (b ^ (a | ~c)) + this$static.X[7] + 1126891415, 10) + a;
  c = $rotateLeft(c + (a ^ (d | ~b)) + this$static.X[14] + -1416354905, 15) + d;
  b = $rotateLeft(b + (d ^ (c | ~a)) + this$static.X[5] + -57434055, 21) + c;
  a = $rotateLeft(a + (c ^ (b | ~d)) + this$static.X[12] + 1700485571, 6) + b;
  d = $rotateLeft(d + (b ^ (a | ~c)) + this$static.X[3] + -1894986606, 10) + a;
  c = $rotateLeft(c + (a ^ (d | ~b)) + this$static.X[10] + -1051523, 15) + d;
  b = $rotateLeft(b + (d ^ (c | ~a)) + this$static.X[1] + -2054922799, 21) + c;
  a = $rotateLeft(a + (c ^ (b | ~d)) + this$static.X[8] + 1873313359, 6) + b;
  d = $rotateLeft(d + (b ^ (a | ~c)) + this$static.X[15] + -30611744, 10) + a;
  c = $rotateLeft(c + (a ^ (d | ~b)) + this$static.X[6] + -1560198380, 15) + d;
  b = $rotateLeft(b + (d ^ (c | ~a)) + this$static.X[13] + 1309151649, 21) + c;
  a = $rotateLeft(a + (c ^ (b | ~d)) + this$static.X[4] + -145523070, 6) + b;
  d = $rotateLeft(d + (b ^ (a | ~c)) + this$static.X[11] + -1120210379, 10) + a;
  c = $rotateLeft(c + (a ^ (d | ~b)) + this$static.X[2] + 718787259, 15) + d;
  b = $rotateLeft(b + (d ^ (c | ~a)) + this$static.X[9] + -343485551, 21) + c;
  this$static.H1 += a;
  this$static.H2 += b;
  this$static.H3 += c;
  this$static.H4 += d;
  this$static.xOff = 0;
  for (i_0 = 0; i_0 != this$static.X.length; i_0++) {
    this$static.X[i_0] = 0;
  }
}

function $reset_1(this$static){
  var i_0;
  $reset_0(this$static);
  this$static.H1 = 1732584193;
  this$static.H2 = -271733879;
  this$static.H3 = -1732584194;
  this$static.H4 = 271733878;
  this$static.xOff = 0;
  for (i_0 = 0; i_0 != this$static.X.length; i_0++) {
    this$static.X[i_0] = 0;
  }
}

function $rotateLeft(x_0, n){
  return x_0 << n | x_0 >>> 32 - n;
}

function $unpackWord(word, out, outOff){
  out[outOff] = narrow_byte(word);
  out[outOff + 1] = narrow_byte(word >>> 8);
  out[outOff + 2] = narrow_byte(word >>> 16);
  out[outOff + 3] = narrow_byte(word >>> 24);
}

function MD5Digest(){
  GeneralDigest.call(this);
  this.X = initDim(I_classLit, $intern_4, 0, 16, 7, 1);
  $reset_1(this);
}

defineClass(753, 456, {}, MD5Digest);
_.processBlock = function processBlock(){
  $processBlock(this);
}
;
_.processLength = function processLength(bitLength){
  this.xOff > 14 && $processBlock(this);
  this.X[14] = toInt(and(bitLength, {l:$intern_7, m:$intern_7, h:$intern_8}));
  this.X[15] = toInt(shru(bitLength, 32));
}
;
_.processWord = function processWord(in_$, inOff){
  this.X[this.xOff++] = in_$[inOff] & 255 | (in_$[inOff + 1] & 255) << 8 | (in_$[inOff + 2] & 255) << 16 | (in_$[inOff + 3] & 255) << 24;
  this.xOff == 16 && $processBlock(this);
}
;
_.H1 = 0;
_.H2 = 0;
_.H3 = 0;
_.H4 = 0;
_.xOff = 0;
var Lorg_bouncycastle_crypto_digests_MD5Digest_2_classLit = createForClass('org.bouncycastle.crypto.digests', 'MD5Digest', 753, Lorg_bouncycastle_crypto_digests_GeneralDigest_2_classLit);
function $clinit_SHA256Digest(){
  $clinit_SHA256Digest = emptyMethod;
  K = initValues(getClassLiteralForArray(I_classLit, 1), $intern_4, 0, 7, [1116352408, 1899447441, -1245643825, -373957723, 961987163, 1508970993, -1841331548, -1424204075, -670586216, 310598401, 607225278, 1426881987, 1925078388, -2132889090, -1680079193, -1046744716, -459576895, -272742522, 264347078, 604807628, 770255983, 1249150122, 1555081692, 1996064986, -1740746414, -1473132947, -1341970488, -1084653625, -958395405, -710438585, 113926993, 338241895, 666307205, 773529912, 1294757372, 1396182291, 1695183700, 1986661051, -2117940946, -1838011259, -1564481375, -1474664885, -1035236496, -949202525, -778901479, -694614492, -200395387, 275423344, 430227734, 506948616, 659060556, 883997877, 958139571, 1322822218, 1537002063, 1747873779, 1955562222, 2024104815, -2067236844, -1933114872, -1866530822, -1538233109, -1090935817, -965641998]);
}

function $Theta0(x_0){
  return (x_0 >>> 7 | x_0 << 25) ^ (x_0 >>> 18 | x_0 << 14) ^ x_0 >>> 3;
}

function $Theta1(x_0){
  return (x_0 >>> 17 | x_0 << 15) ^ (x_0 >>> 19 | x_0 << 13) ^ x_0 >>> 10;
}

function $processBlock_0(this$static){
  var a, b, c, d, e, f, g, h, i_0, i0, t, t0;
  for (t0 = 16; t0 <= 63; t0++) {
    this$static.X[t0] = $Theta1(this$static.X[t0 - 2]) + this$static.X[t0 - 7] + $Theta0(this$static.X[t0 - 15]) + this$static.X[t0 - 16];
  }
  a = this$static.H1;
  b = this$static.H2;
  c = this$static.H3;
  d = this$static.H4;
  e = this$static.H5;
  f = this$static.H6;
  g = this$static.H7;
  h = this$static.H8;
  t = 0;
  for (i0 = 0; i0 < 8; i0++) {
    h += ((e >>> 6 | e << 26) ^ (e >>> 11 | e << 21) ^ (e >>> 25 | e << 7)) + (e & f ^ ~e & g) + K[t] + this$static.X[t];
    d += h;
    h += ((a >>> 2 | a << 30) ^ (a >>> 13 | a << 19) ^ (a >>> 22 | a << 10)) + (a & b ^ a & c ^ b & c);
    ++t;
    g += ((d >>> 6 | d << 26) ^ (d >>> 11 | d << 21) ^ (d >>> 25 | d << 7)) + (d & e ^ ~d & f) + K[t] + this$static.X[t];
    c += g;
    g += ((h >>> 2 | h << 30) ^ (h >>> 13 | h << 19) ^ (h >>> 22 | h << 10)) + (h & a ^ h & b ^ a & b);
    ++t;
    f += ((c >>> 6 | c << 26) ^ (c >>> 11 | c << 21) ^ (c >>> 25 | c << 7)) + (c & d ^ ~c & e) + K[t] + this$static.X[t];
    b += f;
    f += ((g >>> 2 | g << 30) ^ (g >>> 13 | g << 19) ^ (g >>> 22 | g << 10)) + (g & h ^ g & a ^ h & a);
    ++t;
    e += ((b >>> 6 | b << 26) ^ (b >>> 11 | b << 21) ^ (b >>> 25 | b << 7)) + (b & c ^ ~b & d) + K[t] + this$static.X[t];
    a += e;
    e += ((f >>> 2 | f << 30) ^ (f >>> 13 | f << 19) ^ (f >>> 22 | f << 10)) + (f & g ^ f & h ^ g & h);
    ++t;
    d += ((a >>> 6 | a << 26) ^ (a >>> 11 | a << 21) ^ (a >>> 25 | a << 7)) + (a & b ^ ~a & c) + K[t] + this$static.X[t];
    h += d;
    d += ((e >>> 2 | e << 30) ^ (e >>> 13 | e << 19) ^ (e >>> 22 | e << 10)) + (e & f ^ e & g ^ f & g);
    ++t;
    c += ((h >>> 6 | h << 26) ^ (h >>> 11 | h << 21) ^ (h >>> 25 | h << 7)) + (h & a ^ ~h & b) + K[t] + this$static.X[t];
    g += c;
    c += ((d >>> 2 | d << 30) ^ (d >>> 13 | d << 19) ^ (d >>> 22 | d << 10)) + (d & e ^ d & f ^ e & f);
    ++t;
    b += ((g >>> 6 | g << 26) ^ (g >>> 11 | g << 21) ^ (g >>> 25 | g << 7)) + (g & h ^ ~g & a) + K[t] + this$static.X[t];
    f += b;
    b += ((c >>> 2 | c << 30) ^ (c >>> 13 | c << 19) ^ (c >>> 22 | c << 10)) + (c & d ^ c & e ^ d & e);
    ++t;
    a += ((f >>> 6 | f << 26) ^ (f >>> 11 | f << 21) ^ (f >>> 25 | f << 7)) + (f & g ^ ~f & h) + K[t] + this$static.X[t];
    e += a;
    a += ((b >>> 2 | b << 30) ^ (b >>> 13 | b << 19) ^ (b >>> 22 | b << 10)) + (b & c ^ b & d ^ c & d);
    ++t;
  }
  this$static.H1 = this$static.H1 + a & -1;
  this$static.H2 = this$static.H2 + b & -1;
  this$static.H3 = this$static.H3 + c & -1;
  this$static.H4 = this$static.H4 + d & -1;
  this$static.H5 = this$static.H5 + e & -1;
  this$static.H6 = this$static.H6 + f & -1;
  this$static.H7 = this$static.H7 + g & -1;
  this$static.H8 = this$static.H8 + h & -1;
  this$static.xOff = 0;
  for (i_0 = 0; i_0 < 16; i_0++) {
    this$static.X[i_0] = 0;
  }
}

function $reset_2(this$static){
  var i_0;
  $reset_0(this$static);
  this$static.H1 = 1779033703;
  this$static.H2 = -1150833019;
  this$static.H3 = 1013904242;
  this$static.H4 = -1521486534;
  this$static.H5 = 1359893119;
  this$static.H6 = -1694144372;
  this$static.H7 = 528734635;
  this$static.H8 = 1541459225;
  this$static.xOff = 0;
  for (i_0 = 0; i_0 != this$static.X.length; i_0++) {
    this$static.X[i_0] = 0;
  }
}

function SHA256Digest(){
  $clinit_SHA256Digest();
  GeneralDigest.call(this);
  this.X = initDim(I_classLit, $intern_4, 0, 64, 7, 1);
  $reset_2(this);
}

defineClass(688, 456, {}, SHA256Digest);
_.processBlock = function processBlock_0(){
  $processBlock_0(this);
}
;
_.processLength = function processLength_0(bitLength){
  this.xOff > 14 && $processBlock_0(this);
  this.X[14] = toInt(shru(bitLength, 32));
  this.X[15] = toInt(and(bitLength, {l:$intern_7, m:$intern_7, h:$intern_8}));
}
;
_.processWord = function processWord_0(in_$, inOff){
  var n;
  n = in_$[inOff] << 24;
  n |= (in_$[++inOff] & 255) << 16;
  n |= (in_$[++inOff] & 255) << 8;
  n |= in_$[++inOff] & 255;
  this.X[this.xOff] = n;
  ++this.xOff == 16 && $processBlock_0(this);
}
;
_.H1 = 0;
_.H2 = 0;
_.H3 = 0;
_.H4 = 0;
_.H5 = 0;
_.H6 = 0;
_.H7 = 0;
_.H8 = 0;
_.xOff = 0;
var K;
var Lorg_bouncycastle_crypto_digests_SHA256Digest_2_classLit = createForClass('org.bouncycastle.crypto.digests', 'SHA256Digest', 688, Lorg_bouncycastle_crypto_digests_GeneralDigest_2_classLit);
function intToBigEndian(n, bs, off){
  bs[off] = narrow_byte(n >>> 24);
  bs[++off] = narrow_byte(n >>> 16);
  bs[++off] = narrow_byte(n >>> 8);
  bs[++off] = narrow_byte(n);
}

function ExportAllExporterImpl(){
  new AngularListCallbackExporterImpl;
  new AngularValueCallbackExporterImpl;
  new JsAuthErrorClosureExporterImpl;
  new JsAuthSuccessClosureExporterImpl;
  new JsClosureExporterImpl;
  new JsFacadeExporterImpl;
}

defineClass(496, 1, {}, ExportAllExporterImpl);
var Lorg_timepedia_exporter_client_ExportAllExporterImpl_2_classLit = createForClass('org.timepedia.exporter.client', 'ExportAllExporterImpl', 496, Ljava_lang_Object_2_classLit);
var Lorg_timepedia_exporter_client_Exportable_2_classLit = createForInterface('org.timepedia.exporter.client', 'Exportable');
defineClass(779, 1, {});
var Lorg_timepedia_exporter_client_ExporterBaseImpl_2_classLit = createForClass('org.timepedia.exporter.client', 'ExporterBaseImpl', 779, Ljava_lang_Object_2_classLit);
function $addTypeMap(this$static, type_0, exportedConstructor){
  $put_1(this$static.typeMap, type_0, exportedConstructor);
}

function $computeVarArguments(len, args){
  var ret = [];
  for (i = 0; i < len - 1; i++)
    ret.push(args[i]);
  var alen = args.length;
  var p_0 = len - 1;
  if (alen >= len && Object.prototype.toString.apply(args[p_0]) === '[object Array]') {
    ret.push(args[p_0]);
  }
   else {
    var a = [];
    for (i = p_0; i < alen; i++)
      a.push(args[i]);
    ret.push(a);
  }
  return ret;
}

function $declarePackage(qualifiedExportName){
  var i_0, l, o, prefix, superPackages;
  superPackages = $split(qualifiedExportName, '\\.', 0);
  prefix = $wnd;
  i_0 = 0;
  for (l = superPackages.length - 1; i_0 < l; i_0++) {
    if (!$equals_3(superPackages[i_0], 'client')) {
      prefix[superPackages[i_0]] || (prefix[superPackages[i_0]] = {});
      prefix = getProp(prefix, superPackages[i_0]);
    }
  }
  o = getProp(prefix, superPackages[i_0]);
  return o;
}

function $getMaxArity(jsoMap, meth){
  var o = jsoMap[meth];
  var r = 0;
  for (k in o)
    r = Math.max(r, k);
  return r;
}

function $runDispatch(this$static, instance, clazz, meth, arguments_0, isStatic, isVarArgs){
  var args, dmap, i_0, l, ret;
  dmap = isStatic?this$static.staticDispatchMap:this$static.dispatchMap;
  if (isVarArgs) {
    for (l = $getMaxArity(dynamicCastJso(getEntryValueOrNull($getEntry(dmap.hashCodeMap, clazz))), meth) , i_0 = l; i_0 >= 1; i_0--) {
      args = $computeVarArguments(i_0, arguments_0);
      ret = $runDispatch_0(instance, dmap, clazz, meth, args);
      if (!ret) {
        args = $unshift(instance, args);
        ret = $runDispatch_0(instance, dmap, clazz, meth, args);
      }
      if (ret) {
        return ret;
      }
    }
  }
   else {
    ret = $runDispatch_0(instance, dmap, clazz, meth, arguments_0);
    if (!ret) {
      arguments_0 = $unshift(instance, arguments_0);
      ret = $runDispatch_0(instance, dmap, clazz, meth, arguments_0);
    }
    if (ret) {
      return ret;
    }
  }
  throw new RuntimeException_0("Can't find exported method for given arguments: " + meth + ':' + arguments_0.length + '\n' + '');
}

function $runDispatch_0(instance, dmap, clazz, meth, arguments_0){
  var aFunc, i_0, jFunc, l, r, sig, sigs, wFunc, x_0;
  sigs = dynamicCastJso(getEntryValueOrNull($getEntry(dmap.hashCodeMap, clazz)))[meth][arguments_0.length];
  jFunc = null;
  wFunc = null;
  aFunc = null;
  for (i_0 = 0 , l = !sigs?0:sigs.length; i_0 < l; i_0++) {
    sig = sigs[i_0];
    if ($matches(sig, arguments_0)) {
      jFunc = sig[0];
      wFunc = sig[1];
      aFunc = sig[2];
      break;
    }
  }
  if (!jFunc) {
    return null;
  }
   else {
    arguments_0 = aFunc?aFunc(instance, arguments_0):arguments_0;
    r = (x_0 = jFunc.apply(instance, arguments_0) , [wFunc?wFunc(x_0):x_0]);
    return r;
  }
}

function $unshift(o, arr){
  var ret = [o];
  for (i = 0; i < arr.length; i++)
    ret.push(arr[i]);
  return ret;
}

function ExporterBaseActual(){
  this.typeMap = new HashMap;
  this.dispatchMap = new HashMap;
  this.staticDispatchMap = new HashMap;
}

function getGwtInstance(o){
  return o && o.g?o.g:null;
}

function getProp(jso, key){
  return jso != null?jso[key]:null;
}

function isAssignableToClass(o, clazz){
  var sup_0;
  if (Ljava_lang_Object_2_classLit == clazz) {
    return true;
  }
  if (Lorg_timepedia_exporter_client_Exportable_2_classLit == clazz && o != null && (!isJavaString(o) && !hasTypeMarker(o) || canCast(o, 222))) {
    return true;
  }
  if (o != null) {
    for (sup_0 = getClass__Ljava_lang_Class___devirtual$(o); !!sup_0 && sup_0 != Ljava_lang_Object_2_classLit; sup_0 = sup_0.superclass) {
      if (sup_0 == clazz) {
        return true;
      }
    }
  }
  return false;
}

defineClass(495, 779, {}, ExporterBaseActual);
var Lorg_timepedia_exporter_client_ExporterBaseActual_2_classLit = createForClass('org.timepedia.exporter.client', 'ExporterBaseActual', 495, Lorg_timepedia_exporter_client_ExporterBaseImpl_2_classLit);
function $matches(this$static, arguments_0){
  var argJsType, gwt, i_0, isBoolean, isClass, isNumber, isPrimitive, jsType, l, o;
  for (i_0 = 0 , l = arguments_0.length; i_0 < l; i_0++) {
    jsType = this$static[i_0 + 3];
    argJsType = typeof_$(arguments_0, i_0);
    if ($equals_3(argJsType, jsType)) {
      continue;
    }
    if ($equals_3('string', jsType) && $equals_3('null', argJsType)) {
      continue;
    }
    isNumber = $equals_3('number', argJsType);
    isBoolean = $equals_3('boolean', argJsType);
    if (maskUndefined(Ljava_lang_Object_2_classLit) === maskUndefined(jsType)) {
      isNumber && (arguments_0[i_0] = ($clinit_Double() , new Double(arguments_0[i_0])) , undefined);
      isBoolean && (arguments_0[i_0] = ($clinit_Boolean() , arguments_0[i_0]?TRUE:FALSE) , undefined);
      continue;
    }
    isPrimitive = isNumber || isBoolean;
    isClass = !isPrimitive && jsType != null && getClass__Ljava_lang_Class___devirtual$(jsType) == Ljava_lang_Class_2_classLit;
    if (isClass) {
      o = arguments_0[i_0];
      if (o == null || isAssignableToClass(o, dynamicCast(jsType, 153))) {
        continue;
      }
      if (instanceOfJso(o)) {
        gwt = getGwtInstance(dynamicCastJso(o));
        if (gwt != null) {
          if (isAssignableToClass(gwt, dynamicCast(jsType, 153))) {
            arguments_0[i_0] = gwt;
            continue;
          }
        }
      }
    }
    if ($equals_3('object', jsType) && !isNumber && !isBoolean) {
      continue;
    }
    return false;
  }
  return true;
}

function typeof_$(args, i_0){
  var o = args[i_0];
  var t = o == null?'null':typeof o;
  if (t == 'object') {
    return Object.prototype.toString.call(o) == '[object Array]' || typeof o.length == 'number'?'array':t;
  }
  return t;
}

function $clinit_ExporterUtil(){
  $clinit_ExporterUtil = emptyMethod;
  impl_0 = new ExporterBaseActual;
}

function addTypeMap(type_0, exportedConstructor){
  $clinit_ExporterUtil();
  $addTypeMap(impl_0, type_0, exportedConstructor);
}

function declarePackage(qualifiedExportName){
  $clinit_ExporterUtil();
  return $declarePackage(qualifiedExportName);
}

function isAssignableToInstance(clazz, args){
  var o;
  $clinit_ExporterUtil();
  return o = args && args[0] && (typeof args[0] == 'object' || typeof args[0] == 'function')?args[0]:null , isAssignableToClass(o, clazz);
}

function runDispatch(instance, clazz, meth, arguments_0, isStatic, isVarArgs){
  $clinit_ExporterUtil();
  return $runDispatch(impl_0, instance, clazz, meth, arguments_0, isStatic, isVarArgs);
}

function setWrapper(instance, wrapper){
  $clinit_ExporterUtil();
  instance['__gwtex_wrap'] = wrapper;
}

var impl_0;
var I_classLit = createForPrimitive('int', 'I'), Z_classLit = createForPrimitive('boolean', 'Z'), Lcom_google_gwt_lang_CollapsedPropertyHolder_2_classLit = createForClass('com.google.gwt.lang', 'CollapsedPropertyHolder', 763, Ljava_lang_Object_2_classLit), Lcom_google_gwt_lang_JavaClassHierarchySetupUtil_2_classLit = createForClass('com.google.gwt.lang', 'JavaClassHierarchySetupUtil', 765, Ljava_lang_Object_2_classLit), Lcom_google_gwt_lang_LongLibBase$LongEmul_2_classLit = createForClass('com.google.gwt.lang', 'LongLibBase/LongEmul', null, Ljava_lang_Object_2_classLit), Lcom_google_gwt_lang_ModuleUtils_2_classLit = createForClass('com.google.gwt.lang', 'ModuleUtils', 768, Ljava_lang_Object_2_classLit), B_classLit = createForPrimitive('byte', 'B'), J_classLit = createForPrimitive('long', 'J'), D_classLit = createForPrimitive('double', 'D'), C_classLit = createForPrimitive('char', 'C'), Lorg_timepedia_exporter_client_Exportable_2_classLit = createForInterface('org.timepedia.exporter.client', 'Exportable'), Ljava_util_Map$Entry_2_classLit = createForInterface('java.util', 'Map/Entry'), Lim_actor_model_js_angular_AngularListCallback_2_classLit = createForInterface('im.actor.model.js.angular', 'AngularListCallback'), Lim_actor_model_js_angular_AngularValueCallback_2_classLit = createForInterface('im.actor.model.js.angular', 'AngularValueCallback'), Lim_actor_model_js_entity_JsAuthErrorClosure_2_classLit = createForInterface('im.actor.model.js.entity', 'JsAuthErrorClosure'), Lim_actor_model_js_entity_JsAuthSuccessClosure_2_classLit = createForInterface('im.actor.model.js.entity', 'JsAuthSuccessClosure'), Lim_actor_model_js_entity_JsClosure_2_classLit = createForInterface('im.actor.model.js.entity', 'JsClosure'), Lim_actor_model_mvvm_ModelChangedListener_2_classLit = createForInterface('im.actor.model.mvvm', 'ModelChangedListener'), Lim_actor_model_mvvm_ValueChangedListener_2_classLit = createForInterface('im.actor.model.mvvm', 'ValueChangedListener'), Lim_actor_model_modules_updates_internal_ExecuteAfter_2_classLit = createForClass('im.actor.model.modules.updates.internal', 'ExecuteAfter', null, Ljava_lang_Object_2_classLit);
var $entry = registerEntry();
var gwtOnLoad = gwtOnLoad = gwtOnLoad_0;
addInitFunctions(init);
setGwtProperty('permProps', [[['locale', 'default'], ['user.agent', 'ie9']]]);
$sendStats('moduleStartup', 'moduleEvalEnd');
gwtOnLoad(__gwtModuleFunction.__errFn, __gwtModuleFunction.__moduleName, __gwtModuleFunction.__moduleBase, __gwtModuleFunction.__softPermutationId,__gwtModuleFunction.__computePropValue);
$sendStats('moduleStartup', 'end');
$gwt && $gwt.permProps && __gwtModuleFunction.__moduleStartupDone($gwt.permProps);
//# sourceURL=ActorMessenger-0.js

