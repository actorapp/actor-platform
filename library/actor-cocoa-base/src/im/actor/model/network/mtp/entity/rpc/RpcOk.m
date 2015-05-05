//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/entity/rpc/RpcOk.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/entity/rpc/RpcOk.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/DataInput.h"
#include "im/actor/model/droidkit/bser/DataOutput.h"
#include "im/actor/model/network/mtp/entity/ProtoStruct.h"
#include "im/actor/model/network/mtp/entity/rpc/RpcOk.h"
#include "java/io/IOException.h"


#line 13
@implementation MTRpcOk


#line 20
- (instancetype)initWithBSDataInput:(BSDataInput *)stream {
  MTRpcOk_initWithBSDataInput_(self, stream);
  return self;
}


#line 24
- (instancetype)initWithInt:(jint)responseType
              withByteArray:(IOSByteArray *)payload {
  MTRpcOk_initWithInt_withByteArray_(self, responseType, payload);
  return self;
}


#line 29
- (jint)getResponseType {
  return responseType_;
}

- (IOSByteArray *)getPayload {
  return payload_;
}


#line 38
- (jbyte)getHeader {
  return MTRpcOk_HEADER;
}


#line 43
- (void)writeBodyWithBSDataOutput:(BSDataOutput *)bs {
  [((BSDataOutput *) nil_chk(bs)) writeIntWithInt:responseType_];
  [bs writeProtoBytesWithByteArray:payload_ withInt:0 withInt:((IOSByteArray *) nil_chk(payload_))->size_];
}


#line 49
- (void)readBodyWithBSDataInput:(BSDataInput *)bs {
  responseType_ = [((BSDataInput *) nil_chk(bs)) readInt];
  payload_ = [bs readProtoBytes];
}


#line 56
- (NSString *)description {
  return JreStrcat("$IC", @"RpcOk{", responseType_, ']');
}

@end


#line 20
void MTRpcOk_initWithBSDataInput_(MTRpcOk *self, BSDataInput *stream) {
  (void) MTProtoStruct_initWithBSDataInput_(self, stream);
}


#line 20
MTRpcOk *new_MTRpcOk_initWithBSDataInput_(BSDataInput *stream) {
  MTRpcOk *self = [MTRpcOk alloc];
  MTRpcOk_initWithBSDataInput_(self, stream);
  return self;
}


#line 24
void MTRpcOk_initWithInt_withByteArray_(MTRpcOk *self, jint responseType, IOSByteArray *payload) {
  (void) MTProtoStruct_init(self);
  
#line 25
  self->responseType_ = responseType;
  self->payload_ = payload;
}


#line 24
MTRpcOk *new_MTRpcOk_initWithInt_withByteArray_(jint responseType, IOSByteArray *payload) {
  MTRpcOk *self = [MTRpcOk alloc];
  MTRpcOk_initWithInt_withByteArray_(self, responseType, payload);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTRpcOk)
