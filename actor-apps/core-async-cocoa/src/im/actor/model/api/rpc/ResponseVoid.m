//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/api/rpc/ResponseVoid.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/ResponseVoid.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Response.h"
#include "java/io/IOException.h"

@implementation APResponseVoid

+ (APResponseVoid *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APResponseVoid_fromBytesWithByteArray_(data);
}

- (instancetype)init {
  APResponseVoid_init(self);
  return self;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
}

- (NSString *)description {
  NSString *res = @"response Void{";
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APResponseVoid_HEADER;
}

@end

APResponseVoid *APResponseVoid_fromBytesWithByteArray_(IOSByteArray *data) {
  APResponseVoid_initialize();
  return ((APResponseVoid *) BSBser_parseWithBSBserObject_withByteArray_(new_APResponseVoid_init(), data));
}

void APResponseVoid_init(APResponseVoid *self) {
  (void) APResponse_init(self);
}

APResponseVoid *new_APResponseVoid_init() {
  APResponseVoid *self = [APResponseVoid alloc];
  APResponseVoid_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APResponseVoid)
