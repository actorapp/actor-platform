//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/updates/UpdateUserEmailAdded.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/updates/UpdateUserEmailAdded.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Update.h"
#include "java/io/IOException.h"

@interface ImActorModelApiUpdatesUpdateUserEmailAdded () {
 @public
  jint uid_;
  jint emailId_;
}

@end

@implementation ImActorModelApiUpdatesUpdateUserEmailAdded

+ (ImActorModelApiUpdatesUpdateUserEmailAdded *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiUpdatesUpdateUserEmailAdded_fromBytesWithByteArray_(data);
}

- (instancetype)initWithInt:(jint)uid
                    withInt:(jint)emailId {
  ImActorModelApiUpdatesUpdateUserEmailAdded_initWithInt_withInt_(self, uid, emailId);
  return self;
}

- (instancetype)init {
  ImActorModelApiUpdatesUpdateUserEmailAdded_init(self);
  return self;
}

- (jint)getUid {
  return self->uid_;
}

- (jint)getEmailId {
  return self->emailId_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->uid_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->emailId_ = [values getIntWithInt:2];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->uid_];
  [writer writeIntWithInt:2 withInt:self->emailId_];
}

- (NSString *)description {
  NSString *res = @"update UserEmailAdded{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"uid=", self->uid_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", emailId=", self->emailId_));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return ImActorModelApiUpdatesUpdateUserEmailAdded_HEADER;
}

@end

ImActorModelApiUpdatesUpdateUserEmailAdded *ImActorModelApiUpdatesUpdateUserEmailAdded_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiUpdatesUpdateUserEmailAdded_initialize();
  return ((ImActorModelApiUpdatesUpdateUserEmailAdded *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiUpdatesUpdateUserEmailAdded_init(), data));
}

void ImActorModelApiUpdatesUpdateUserEmailAdded_initWithInt_withInt_(ImActorModelApiUpdatesUpdateUserEmailAdded *self, jint uid, jint emailId) {
  (void) ImActorModelNetworkParserUpdate_init(self);
  self->uid_ = uid;
  self->emailId_ = emailId;
}

ImActorModelApiUpdatesUpdateUserEmailAdded *new_ImActorModelApiUpdatesUpdateUserEmailAdded_initWithInt_withInt_(jint uid, jint emailId) {
  ImActorModelApiUpdatesUpdateUserEmailAdded *self = [ImActorModelApiUpdatesUpdateUserEmailAdded alloc];
  ImActorModelApiUpdatesUpdateUserEmailAdded_initWithInt_withInt_(self, uid, emailId);
  return self;
}

void ImActorModelApiUpdatesUpdateUserEmailAdded_init(ImActorModelApiUpdatesUpdateUserEmailAdded *self) {
  (void) ImActorModelNetworkParserUpdate_init(self);
}

ImActorModelApiUpdatesUpdateUserEmailAdded *new_ImActorModelApiUpdatesUpdateUserEmailAdded_init() {
  ImActorModelApiUpdatesUpdateUserEmailAdded *self = [ImActorModelApiUpdatesUpdateUserEmailAdded alloc];
  ImActorModelApiUpdatesUpdateUserEmailAdded_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiUpdatesUpdateUserEmailAdded)
