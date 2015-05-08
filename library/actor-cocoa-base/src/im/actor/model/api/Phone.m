//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/Phone.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/Phone.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"

@interface ImActorModelApiPhone () {
 @public
  jint id__;
  jlong accessHash_;
  jlong phone_;
  NSString *phoneTitle_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiPhone, phoneTitle_, NSString *)

@implementation ImActorModelApiPhone

- (instancetype)initWithInt:(jint)id_
                   withLong:(jlong)accessHash
                   withLong:(jlong)phone
               withNSString:(NSString *)phoneTitle {
  ImActorModelApiPhone_initWithInt_withLong_withLong_withNSString_(self, id_, accessHash, phone, phoneTitle);
  return self;
}

- (instancetype)init {
  ImActorModelApiPhone_init(self);
  return self;
}

- (jint)getId {
  return self->id__;
}

- (jlong)getAccessHash {
  return self->accessHash_;
}

- (jlong)getPhone {
  return self->phone_;
}

- (NSString *)getPhoneTitle {
  return self->phoneTitle_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->id__ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->accessHash_ = [values getLongWithInt:2];
  self->phone_ = [values getLongWithInt:3];
  self->phoneTitle_ = [values getStringWithInt:4];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->id__];
  [writer writeLongWithInt:2 withLong:self->accessHash_];
  [writer writeLongWithInt:3 withLong:self->phone_];
  if (self->phoneTitle_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeStringWithInt:4 withNSString:self->phoneTitle_];
}

- (NSString *)description {
  NSString *res = @"struct Phone{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"id=", self->id__));
  res = JreStrcat("$$", res, JreStrcat("$J", @", phone=", self->phone_));
  res = JreStrcat("$$", res, JreStrcat("$$", @", phoneTitle=", self->phoneTitle_));
  res = JreStrcat("$C", res, '}');
  return res;
}

@end

void ImActorModelApiPhone_initWithInt_withLong_withLong_withNSString_(ImActorModelApiPhone *self, jint id_, jlong accessHash, jlong phone, NSString *phoneTitle) {
  (void) BSBserObject_init(self);
  self->id__ = id_;
  self->accessHash_ = accessHash;
  self->phone_ = phone;
  self->phoneTitle_ = phoneTitle;
}

ImActorModelApiPhone *new_ImActorModelApiPhone_initWithInt_withLong_withLong_withNSString_(jint id_, jlong accessHash, jlong phone, NSString *phoneTitle) {
  ImActorModelApiPhone *self = [ImActorModelApiPhone alloc];
  ImActorModelApiPhone_initWithInt_withLong_withLong_withNSString_(self, id_, accessHash, phone, phoneTitle);
  return self;
}

void ImActorModelApiPhone_init(ImActorModelApiPhone *self) {
  (void) BSBserObject_init(self);
}

ImActorModelApiPhone *new_ImActorModelApiPhone_init() {
  ImActorModelApiPhone *self = [ImActorModelApiPhone alloc];
  ImActorModelApiPhone_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiPhone)
