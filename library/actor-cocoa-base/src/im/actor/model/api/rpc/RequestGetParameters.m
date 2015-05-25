//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestGetParameters.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/RequestGetParameters.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@implementation APRequestGetParameters

+ (APRequestGetParameters *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APRequestGetParameters_fromBytesWithByteArray_(data);
}

- (instancetype)init {
  APRequestGetParameters_init(self);
  return self;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
}

- (NSString *)description {
  NSString *res = @"rpc GetParameters{";
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APRequestGetParameters_HEADER;
}

@end

APRequestGetParameters *APRequestGetParameters_fromBytesWithByteArray_(IOSByteArray *data) {
  APRequestGetParameters_initialize();
  return ((APRequestGetParameters *) BSBser_parseWithBSBserObject_withByteArray_(new_APRequestGetParameters_init(), data));
}

void APRequestGetParameters_init(APRequestGetParameters *self) {
  (void) APRequest_init(self);
}

APRequestGetParameters *new_APRequestGetParameters_init() {
  APRequestGetParameters *self = [APRequestGetParameters alloc];
  APRequestGetParameters_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APRequestGetParameters)
