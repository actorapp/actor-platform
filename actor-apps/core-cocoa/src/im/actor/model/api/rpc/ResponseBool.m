//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/api/rpc/ResponseBool.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/ResponseBool.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Response.h"
#include "java/io/IOException.h"

@interface APResponseBool () {
 @public
  jboolean value__;
}

@end

@implementation APResponseBool

+ (APResponseBool *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APResponseBool_fromBytesWithByteArray_(data);
}

- (instancetype)initWithBoolean:(jboolean)value {
  APResponseBool_initWithBoolean_(self, value);
  return self;
}

- (instancetype)init {
  APResponseBool_init(self);
  return self;
}

- (jboolean)value {
  return self->value__;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->value__ = [((BSBserValues *) nil_chk(values)) getBoolWithInt:1];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeBoolWithInt:1 withBoolean:self->value__];
}

- (NSString *)description {
  NSString *res = @"response Bool{";
  res = JreStrcat("$$", res, JreStrcat("$Z", @"value=", self->value__));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APResponseBool_HEADER;
}

@end

APResponseBool *APResponseBool_fromBytesWithByteArray_(IOSByteArray *data) {
  APResponseBool_initialize();
  return ((APResponseBool *) BSBser_parseWithBSBserObject_withByteArray_(new_APResponseBool_init(), data));
}

void APResponseBool_initWithBoolean_(APResponseBool *self, jboolean value) {
  (void) APResponse_init(self);
  self->value__ = value;
}

APResponseBool *new_APResponseBool_initWithBoolean_(jboolean value) {
  APResponseBool *self = [APResponseBool alloc];
  APResponseBool_initWithBoolean_(self, value);
  return self;
}

void APResponseBool_init(APResponseBool *self) {
  (void) APResponse_init(self);
}

APResponseBool *new_APResponseBool_init() {
  APResponseBool *self = [APResponseBool alloc];
  APResponseBool_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APResponseBool)
