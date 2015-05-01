//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestEditParameter.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestEditParameter.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/RequestEditParameter.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@interface ImActorModelApiRpcRequestEditParameter () {
 @public
  NSString *key_;
  NSString *value_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiRpcRequestEditParameter, key_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelApiRpcRequestEditParameter, value_, NSString *)


#line 20
@implementation ImActorModelApiRpcRequestEditParameter


#line 23
+ (ImActorModelApiRpcRequestEditParameter *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiRpcRequestEditParameter_fromBytesWithByteArray_(data);
}


#line 30
- (instancetype)initWithNSString:(NSString *)key
                    withNSString:(NSString *)value {
  ImActorModelApiRpcRequestEditParameter_initWithNSString_withNSString_(self, key, value);
  return self;
}


#line 35
- (instancetype)init {
  ImActorModelApiRpcRequestEditParameter_init(self);
  return self;
}


#line 39
- (NSString *)getKey {
  return self->key_;
}

- (NSString *)getValue {
  return self->value_;
}


#line 48
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->key_ = [((BSBserValues *) nil_chk(values)) getStringWithInt:1];
  self->value_ = [values getStringWithInt:2];
}


#line 54
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->key_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeStringWithInt:1 withNSString:self->key_];
  if (self->value_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeStringWithInt:2 withNSString:self->value_];
}


#line 66
- (NSString *)description {
  NSString *res = @"rpc EditParameter{";
  res = JreStrcat("$C", res, '}');
  return res;
}


#line 73
- (jint)getHeaderKey {
  return ImActorModelApiRpcRequestEditParameter_HEADER;
}

@end


#line 23
ImActorModelApiRpcRequestEditParameter *ImActorModelApiRpcRequestEditParameter_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiRpcRequestEditParameter_initialize();
  
#line 24
  return ((ImActorModelApiRpcRequestEditParameter *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiRpcRequestEditParameter_init(), data));
}

void ImActorModelApiRpcRequestEditParameter_initWithNSString_withNSString_(ImActorModelApiRpcRequestEditParameter *self, NSString *key, NSString *value) {
  (void) ImActorModelNetworkParserRequest_init(self);
  
#line 31
  self->key_ = key;
  self->value_ = value;
}


#line 30
ImActorModelApiRpcRequestEditParameter *new_ImActorModelApiRpcRequestEditParameter_initWithNSString_withNSString_(NSString *key, NSString *value) {
  ImActorModelApiRpcRequestEditParameter *self = [ImActorModelApiRpcRequestEditParameter alloc];
  ImActorModelApiRpcRequestEditParameter_initWithNSString_withNSString_(self, key, value);
  return self;
}


#line 35
void ImActorModelApiRpcRequestEditParameter_init(ImActorModelApiRpcRequestEditParameter *self) {
  (void) ImActorModelNetworkParserRequest_init(self);
}


#line 35
ImActorModelApiRpcRequestEditParameter *new_ImActorModelApiRpcRequestEditParameter_init() {
  ImActorModelApiRpcRequestEditParameter *self = [ImActorModelApiRpcRequestEditParameter alloc];
  ImActorModelApiRpcRequestEditParameter_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRpcRequestEditParameter)
