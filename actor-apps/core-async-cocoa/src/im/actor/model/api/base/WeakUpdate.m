//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/api/base/WeakUpdate.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/base/WeakUpdate.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/droidkit/bser/Utils.h"
#include "im/actor/model/network/parser/RpcScope.h"
#include "java/io/IOException.h"

@interface ImActorModelApiBaseWeakUpdate () {
 @public
  jlong date_;
  jint updateHeader_;
  IOSByteArray *update_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiBaseWeakUpdate, update_, IOSByteArray *)

@implementation ImActorModelApiBaseWeakUpdate

+ (ImActorModelApiBaseWeakUpdate *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiBaseWeakUpdate_fromBytesWithByteArray_(data);
}

- (instancetype)initWithLong:(jlong)date
                     withInt:(jint)updateHeader
               withByteArray:(IOSByteArray *)update {
  ImActorModelApiBaseWeakUpdate_initWithLong_withInt_withByteArray_(self, date, updateHeader, update);
  return self;
}

- (instancetype)init {
  ImActorModelApiBaseWeakUpdate_init(self);
  return self;
}

- (jlong)getDate {
  return self->date_;
}

- (jint)getUpdateHeader {
  return self->updateHeader_;
}

- (IOSByteArray *)getUpdate {
  return self->update_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->date_ = [((BSBserValues *) nil_chk(values)) getLongWithInt:1];
  self->updateHeader_ = [values getIntWithInt:2];
  self->update_ = [values getBytesWithInt:3];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeLongWithInt:1 withLong:self->date_];
  [writer writeIntWithInt:2 withInt:self->updateHeader_];
  if (self->update_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeBytesWithInt:3 withByteArray:self->update_];
}

- (NSString *)description {
  NSString *res = @"update box WeakUpdate{";
  res = JreStrcat("$$", res, JreStrcat("$J", @"date=", self->date_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", updateHeader=", self->updateHeader_));
  res = JreStrcat("$$", res, JreStrcat("$$", @", update=", BSUtils_byteArrayToStringCompactWithByteArray_(self->update_)));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return ImActorModelApiBaseWeakUpdate_HEADER;
}

@end

ImActorModelApiBaseWeakUpdate *ImActorModelApiBaseWeakUpdate_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiBaseWeakUpdate_initialize();
  return ((ImActorModelApiBaseWeakUpdate *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiBaseWeakUpdate_init(), data));
}

void ImActorModelApiBaseWeakUpdate_initWithLong_withInt_withByteArray_(ImActorModelApiBaseWeakUpdate *self, jlong date, jint updateHeader, IOSByteArray *update) {
  (void) APRpcScope_init(self);
  self->date_ = date;
  self->updateHeader_ = updateHeader;
  self->update_ = update;
}

ImActorModelApiBaseWeakUpdate *new_ImActorModelApiBaseWeakUpdate_initWithLong_withInt_withByteArray_(jlong date, jint updateHeader, IOSByteArray *update) {
  ImActorModelApiBaseWeakUpdate *self = [ImActorModelApiBaseWeakUpdate alloc];
  ImActorModelApiBaseWeakUpdate_initWithLong_withInt_withByteArray_(self, date, updateHeader, update);
  return self;
}

void ImActorModelApiBaseWeakUpdate_init(ImActorModelApiBaseWeakUpdate *self) {
  (void) APRpcScope_init(self);
}

ImActorModelApiBaseWeakUpdate *new_ImActorModelApiBaseWeakUpdate_init() {
  ImActorModelApiBaseWeakUpdate *self = [ImActorModelApiBaseWeakUpdate alloc];
  ImActorModelApiBaseWeakUpdate_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiBaseWeakUpdate)
