//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/entity/RequestResend.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/entity/RequestResend.java"

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/DataInput.h"
#include "im/actor/model/droidkit/bser/DataOutput.h"
#include "im/actor/model/network/mtp/entity/ProtoStruct.h"
#include "im/actor/model/network/mtp/entity/RequestResend.h"
#include "java/io/IOException.h"

@interface MTRequestResend () {
 @public
  jlong messageId_;
}

@end


#line 11
@implementation MTRequestResend


#line 17
- (instancetype)initWithLong:(jlong)messageId {
  MTRequestResend_initWithLong_(self, messageId);
  return self;
}


#line 21
- (instancetype)initWithBSDataInput:(BSDataInput *)stream {
  MTRequestResend_initWithBSDataInput_(self, stream);
  return self;
}


#line 25
- (jlong)getMessageId {
  return messageId_;
}


#line 30
- (jbyte)getHeader {
  return MTRequestResend_HEADER;
}


#line 35
- (void)writeBodyWithBSDataOutput:(BSDataOutput *)bs {
  [((BSDataOutput *) nil_chk(bs)) writeLongWithLong:messageId_];
}


#line 40
- (void)readBodyWithBSDataInput:(BSDataInput *)bs {
  messageId_ = [((BSDataInput *) nil_chk(bs)) readLong];
}

@end


#line 17
void MTRequestResend_initWithLong_(MTRequestResend *self, jlong messageId) {
  (void) MTProtoStruct_init(self);
  
#line 18
  self->messageId_ = messageId;
}


#line 17
MTRequestResend *new_MTRequestResend_initWithLong_(jlong messageId) {
  MTRequestResend *self = [MTRequestResend alloc];
  MTRequestResend_initWithLong_(self, messageId);
  return self;
}


#line 21
void MTRequestResend_initWithBSDataInput_(MTRequestResend *self, BSDataInput *stream) {
  (void) MTProtoStruct_initWithBSDataInput_(self, stream);
}


#line 21
MTRequestResend *new_MTRequestResend_initWithBSDataInput_(BSDataInput *stream) {
  MTRequestResend *self = [MTRequestResend alloc];
  MTRequestResend_initWithBSDataInput_(self, stream);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTRequestResend)
