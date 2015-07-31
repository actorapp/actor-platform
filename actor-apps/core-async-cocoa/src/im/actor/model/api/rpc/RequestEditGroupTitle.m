//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/api/rpc/RequestEditGroupTitle.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/GroupOutPeer.h"
#include "im/actor/model/api/rpc/RequestEditGroupTitle.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@interface APRequestEditGroupTitle () {
 @public
  APGroupOutPeer *groupPeer_;
  jlong rid_;
  NSString *title_;
}

@end

J2OBJC_FIELD_SETTER(APRequestEditGroupTitle, groupPeer_, APGroupOutPeer *)
J2OBJC_FIELD_SETTER(APRequestEditGroupTitle, title_, NSString *)

@implementation APRequestEditGroupTitle

+ (APRequestEditGroupTitle *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APRequestEditGroupTitle_fromBytesWithByteArray_(data);
}

- (instancetype)initWithAPGroupOutPeer:(APGroupOutPeer *)groupPeer
                              withLong:(jlong)rid
                          withNSString:(NSString *)title {
  APRequestEditGroupTitle_initWithAPGroupOutPeer_withLong_withNSString_(self, groupPeer, rid, title);
  return self;
}

- (instancetype)init {
  APRequestEditGroupTitle_init(self);
  return self;
}

- (APGroupOutPeer *)getGroupPeer {
  return self->groupPeer_;
}

- (jlong)getRid {
  return self->rid_;
}

- (NSString *)getTitle {
  return self->title_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->groupPeer_ = [((BSBserValues *) nil_chk(values)) getObjWithInt:1 withBSBserObject:new_APGroupOutPeer_init()];
  self->rid_ = [values getLongWithInt:4];
  self->title_ = [values getStringWithInt:3];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->groupPeer_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:1 withBSBserObject:self->groupPeer_];
  [writer writeLongWithInt:4 withLong:self->rid_];
  if (self->title_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeStringWithInt:3 withNSString:self->title_];
}

- (NSString *)description {
  NSString *res = @"rpc EditGroupTitle{";
  res = JreStrcat("$$", res, JreStrcat("$@", @"groupPeer=", self->groupPeer_));
  res = JreStrcat("$$", res, JreStrcat("$J", @", rid=", self->rid_));
  res = JreStrcat("$$", res, JreStrcat("$$", @", title=", self->title_));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APRequestEditGroupTitle_HEADER;
}

@end

APRequestEditGroupTitle *APRequestEditGroupTitle_fromBytesWithByteArray_(IOSByteArray *data) {
  APRequestEditGroupTitle_initialize();
  return ((APRequestEditGroupTitle *) BSBser_parseWithBSBserObject_withByteArray_(new_APRequestEditGroupTitle_init(), data));
}

void APRequestEditGroupTitle_initWithAPGroupOutPeer_withLong_withNSString_(APRequestEditGroupTitle *self, APGroupOutPeer *groupPeer, jlong rid, NSString *title) {
  (void) APRequest_init(self);
  self->groupPeer_ = groupPeer;
  self->rid_ = rid;
  self->title_ = title;
}

APRequestEditGroupTitle *new_APRequestEditGroupTitle_initWithAPGroupOutPeer_withLong_withNSString_(APGroupOutPeer *groupPeer, jlong rid, NSString *title) {
  APRequestEditGroupTitle *self = [APRequestEditGroupTitle alloc];
  APRequestEditGroupTitle_initWithAPGroupOutPeer_withLong_withNSString_(self, groupPeer, rid, title);
  return self;
}

void APRequestEditGroupTitle_init(APRequestEditGroupTitle *self) {
  (void) APRequest_init(self);
}

APRequestEditGroupTitle *new_APRequestEditGroupTitle_init() {
  APRequestEditGroupTitle *self = [APRequestEditGroupTitle alloc];
  APRequestEditGroupTitle_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APRequestEditGroupTitle)
