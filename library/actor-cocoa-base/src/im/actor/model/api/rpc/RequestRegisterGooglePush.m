//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestRegisterGooglePush.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestRegisterGooglePush.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/RequestRegisterGooglePush.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@interface ImActorModelApiRpcRequestRegisterGooglePush () {
 @public
  jlong projectId_;
  NSString *token_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiRpcRequestRegisterGooglePush, token_, NSString *)


#line 20
@implementation ImActorModelApiRpcRequestRegisterGooglePush


#line 23
+ (ImActorModelApiRpcRequestRegisterGooglePush *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiRpcRequestRegisterGooglePush_fromBytesWithByteArray_(data);
}


#line 30
- (instancetype)initWithLong:(jlong)projectId
                withNSString:(NSString *)token {
  ImActorModelApiRpcRequestRegisterGooglePush_initWithLong_withNSString_(self, projectId, token);
  return self;
}


#line 35
- (instancetype)init {
  ImActorModelApiRpcRequestRegisterGooglePush_init(self);
  return self;
}


#line 39
- (jlong)getProjectId {
  return self->projectId_;
}

- (NSString *)getToken {
  return self->token_;
}


#line 48
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->projectId_ = [((BSBserValues *) nil_chk(values)) getLongWithInt:1];
  self->token_ = [values getStringWithInt:2];
}


#line 54
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeLongWithInt:1 withLong:self->projectId_];
  if (self->token_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeStringWithInt:2 withNSString:self->token_];
}


#line 63
- (NSString *)description {
  NSString *res = @"rpc RegisterGooglePush{";
  res = JreStrcat("$C", res, '}');
  return res;
}


#line 70
- (jint)getHeaderKey {
  return ImActorModelApiRpcRequestRegisterGooglePush_HEADER;
}

@end


#line 23
ImActorModelApiRpcRequestRegisterGooglePush *ImActorModelApiRpcRequestRegisterGooglePush_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiRpcRequestRegisterGooglePush_initialize();
  
#line 24
  return ((ImActorModelApiRpcRequestRegisterGooglePush *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiRpcRequestRegisterGooglePush_init(), data));
}

void ImActorModelApiRpcRequestRegisterGooglePush_initWithLong_withNSString_(ImActorModelApiRpcRequestRegisterGooglePush *self, jlong projectId, NSString *token) {
  (void) ImActorModelNetworkParserRequest_init(self);
  
#line 31
  self->projectId_ = projectId;
  self->token_ = token;
}


#line 30
ImActorModelApiRpcRequestRegisterGooglePush *new_ImActorModelApiRpcRequestRegisterGooglePush_initWithLong_withNSString_(jlong projectId, NSString *token) {
  ImActorModelApiRpcRequestRegisterGooglePush *self = [ImActorModelApiRpcRequestRegisterGooglePush alloc];
  ImActorModelApiRpcRequestRegisterGooglePush_initWithLong_withNSString_(self, projectId, token);
  return self;
}


#line 35
void ImActorModelApiRpcRequestRegisterGooglePush_init(ImActorModelApiRpcRequestRegisterGooglePush *self) {
  (void) ImActorModelNetworkParserRequest_init(self);
}


#line 35
ImActorModelApiRpcRequestRegisterGooglePush *new_ImActorModelApiRpcRequestRegisterGooglePush_init() {
  ImActorModelApiRpcRequestRegisterGooglePush *self = [ImActorModelApiRpcRequestRegisterGooglePush alloc];
  ImActorModelApiRpcRequestRegisterGooglePush_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRpcRequestRegisterGooglePush)
