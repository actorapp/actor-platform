//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/ServiceExUserInvited.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/ServiceEx.h"
#include "im/actor/model/api/ServiceExUserInvited.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/droidkit/bser/util/SparseArray.h"
#include "java/io/IOException.h"

@interface APServiceExUserInvited () {
 @public
  jint invitedUid_;
}

@end

@implementation APServiceExUserInvited

- (instancetype)initWithInt:(jint)invitedUid {
  APServiceExUserInvited_initWithInt_(self, invitedUid);
  return self;
}

- (instancetype)init {
  APServiceExUserInvited_init(self);
  return self;
}

- (jint)getHeader {
  return 1;
}

- (jint)getInvitedUid {
  return self->invitedUid_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->invitedUid_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  if ([values hasRemaining]) {
    [self setUnmappedObjectsWithImActorModelDroidkitBserUtilSparseArray:[values buildRemaining]];
  }
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->invitedUid_];
  if ([self getUnmappedObjects] != nil) {
    ImActorModelDroidkitBserUtilSparseArray *unmapped = [self getUnmappedObjects];
    for (jint i = 0; i < [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(unmapped)) size]; i++) {
      jint key = [unmapped keyAtWithInt:i];
      [writer writeUnmappedWithInt:key withId:[unmapped getWithInt:key]];
    }
  }
}

- (NSString *)description {
  NSString *res = @"struct ServiceExUserInvited{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"invitedUid=", self->invitedUid_));
  res = JreStrcat("$C", res, '}');
  return res;
}

@end

void APServiceExUserInvited_initWithInt_(APServiceExUserInvited *self, jint invitedUid) {
  (void) APServiceEx_init(self);
  self->invitedUid_ = invitedUid;
}

APServiceExUserInvited *new_APServiceExUserInvited_initWithInt_(jint invitedUid) {
  APServiceExUserInvited *self = [APServiceExUserInvited alloc];
  APServiceExUserInvited_initWithInt_(self, invitedUid);
  return self;
}

void APServiceExUserInvited_init(APServiceExUserInvited *self) {
  (void) APServiceEx_init(self);
}

APServiceExUserInvited *new_APServiceExUserInvited_init() {
  APServiceExUserInvited *self = [APServiceExUserInvited alloc];
  APServiceExUserInvited_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APServiceExUserInvited)
