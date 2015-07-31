//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/api/rpc/ResponseEditGroupAvatar.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/Avatar.h"
#include "im/actor/model/api/rpc/ResponseEditGroupAvatar.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Response.h"
#include "java/io/IOException.h"

@interface APResponseEditGroupAvatar () {
 @public
  APAvatar *avatar_;
  jint seq_;
  IOSByteArray *state_;
  jlong date_;
}

@end

J2OBJC_FIELD_SETTER(APResponseEditGroupAvatar, avatar_, APAvatar *)
J2OBJC_FIELD_SETTER(APResponseEditGroupAvatar, state_, IOSByteArray *)

@implementation APResponseEditGroupAvatar

+ (APResponseEditGroupAvatar *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APResponseEditGroupAvatar_fromBytesWithByteArray_(data);
}

- (instancetype)initWithAPAvatar:(APAvatar *)avatar
                         withInt:(jint)seq
                   withByteArray:(IOSByteArray *)state
                        withLong:(jlong)date {
  APResponseEditGroupAvatar_initWithAPAvatar_withInt_withByteArray_withLong_(self, avatar, seq, state, date);
  return self;
}

- (instancetype)init {
  APResponseEditGroupAvatar_init(self);
  return self;
}

- (APAvatar *)getAvatar {
  return self->avatar_;
}

- (jint)getSeq {
  return self->seq_;
}

- (IOSByteArray *)getState {
  return self->state_;
}

- (jlong)getDate {
  return self->date_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->avatar_ = [((BSBserValues *) nil_chk(values)) getObjWithInt:1 withBSBserObject:new_APAvatar_init()];
  self->seq_ = [values getIntWithInt:2];
  self->state_ = [values getBytesWithInt:3];
  self->date_ = [values getLongWithInt:4];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->avatar_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:1 withBSBserObject:self->avatar_];
  [writer writeIntWithInt:2 withInt:self->seq_];
  if (self->state_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeBytesWithInt:3 withByteArray:self->state_];
  [writer writeLongWithInt:4 withLong:self->date_];
}

- (NSString *)description {
  NSString *res = @"tuple EditGroupAvatar{";
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APResponseEditGroupAvatar_HEADER;
}

@end

APResponseEditGroupAvatar *APResponseEditGroupAvatar_fromBytesWithByteArray_(IOSByteArray *data) {
  APResponseEditGroupAvatar_initialize();
  return ((APResponseEditGroupAvatar *) BSBser_parseWithBSBserObject_withByteArray_(new_APResponseEditGroupAvatar_init(), data));
}

void APResponseEditGroupAvatar_initWithAPAvatar_withInt_withByteArray_withLong_(APResponseEditGroupAvatar *self, APAvatar *avatar, jint seq, IOSByteArray *state, jlong date) {
  (void) APResponse_init(self);
  self->avatar_ = avatar;
  self->seq_ = seq;
  self->state_ = state;
  self->date_ = date;
}

APResponseEditGroupAvatar *new_APResponseEditGroupAvatar_initWithAPAvatar_withInt_withByteArray_withLong_(APAvatar *avatar, jint seq, IOSByteArray *state, jlong date) {
  APResponseEditGroupAvatar *self = [APResponseEditGroupAvatar alloc];
  APResponseEditGroupAvatar_initWithAPAvatar_withInt_withByteArray_withLong_(self, avatar, seq, state, date);
  return self;
}

void APResponseEditGroupAvatar_init(APResponseEditGroupAvatar *self) {
  (void) APResponse_init(self);
}

APResponseEditGroupAvatar *new_APResponseEditGroupAvatar_init() {
  APResponseEditGroupAvatar *self = [APResponseEditGroupAvatar alloc];
  APResponseEditGroupAvatar_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APResponseEditGroupAvatar)
