//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/api/base/FatSeqUpdate.java
//

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/ContactRecord.h"
#include "im/actor/model/api/Group.h"
#include "im/actor/model/api/User.h"
#include "im/actor/model/api/base/FatSeqUpdate.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"

@interface ImActorModelApiBaseFatSeqUpdate () {
 @public
  jint seq_;
  IOSByteArray *state_;
  jint updateHeader_;
  IOSByteArray *update_;
  id<JavaUtilList> users_;
  id<JavaUtilList> groups_;
  id<JavaUtilList> contacts_;
}
@end

J2OBJC_FIELD_SETTER(ImActorModelApiBaseFatSeqUpdate, state_, IOSByteArray *)
J2OBJC_FIELD_SETTER(ImActorModelApiBaseFatSeqUpdate, update_, IOSByteArray *)
J2OBJC_FIELD_SETTER(ImActorModelApiBaseFatSeqUpdate, users_, id<JavaUtilList>)
J2OBJC_FIELD_SETTER(ImActorModelApiBaseFatSeqUpdate, groups_, id<JavaUtilList>)
J2OBJC_FIELD_SETTER(ImActorModelApiBaseFatSeqUpdate, contacts_, id<JavaUtilList>)

@implementation ImActorModelApiBaseFatSeqUpdate

+ (ImActorModelApiBaseFatSeqUpdate *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiBaseFatSeqUpdate_fromBytesWithByteArray_(data);
}

- (instancetype)initWithInt:(jint)seq
              withByteArray:(IOSByteArray *)state
                    withInt:(jint)updateHeader
              withByteArray:(IOSByteArray *)update
           withJavaUtilList:(id<JavaUtilList>)users
           withJavaUtilList:(id<JavaUtilList>)groups
           withJavaUtilList:(id<JavaUtilList>)contacts {
  if (self = [super init]) {
    self->seq_ = seq;
    self->state_ = state;
    self->updateHeader_ = updateHeader;
    self->update_ = update;
    self->users_ = users;
    self->groups_ = groups;
    self->contacts_ = contacts;
  }
  return self;
}

- (instancetype)init {
  return [super init];
}

- (jint)getSeq {
  return self->seq_;
}

- (IOSByteArray *)getState {
  return self->state_;
}

- (jint)getUpdateHeader {
  return self->updateHeader_;
}

- (IOSByteArray *)getUpdate {
  return self->update_;
}

- (id<JavaUtilList>)getUsers {
  return self->users_;
}

- (id<JavaUtilList>)getGroups {
  return self->groups_;
}

- (id<JavaUtilList>)getContacts {
  return self->contacts_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->seq_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->state_ = [values getBytesWithInt:2];
  self->updateHeader_ = [values getIntWithInt:3];
  self->update_ = [values getBytesWithInt:4];
  id<JavaUtilList> _users = [[JavaUtilArrayList alloc] init];
  for (jint i = 0; i < [values getRepeatedCountWithInt:5]; i++) {
    [_users addWithId:[[ImActorModelApiUser alloc] init]];
  }
  self->users_ = [values getRepeatedObjWithInt:5 withJavaUtilList:_users];
  id<JavaUtilList> _groups = [[JavaUtilArrayList alloc] init];
  for (jint i = 0; i < [values getRepeatedCountWithInt:6]; i++) {
    [_groups addWithId:[[ImActorModelApiGroup alloc] init]];
  }
  self->groups_ = [values getRepeatedObjWithInt:6 withJavaUtilList:_groups];
  id<JavaUtilList> _contacts = [[JavaUtilArrayList alloc] init];
  for (jint i = 0; i < [values getRepeatedCountWithInt:7]; i++) {
    [_contacts addWithId:[[ImActorModelApiContactRecord alloc] init]];
  }
  self->contacts_ = [values getRepeatedObjWithInt:7 withJavaUtilList:_contacts];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->seq_];
  if (self->state_ == nil) {
    @throw [[JavaIoIOException alloc] init];
  }
  [writer writeBytesWithInt:2 withByteArray:self->state_];
  [writer writeIntWithInt:3 withInt:self->updateHeader_];
  if (self->update_ == nil) {
    @throw [[JavaIoIOException alloc] init];
  }
  [writer writeBytesWithInt:4 withByteArray:self->update_];
  [writer writeRepeatedObjWithInt:5 withJavaUtilList:self->users_];
  [writer writeRepeatedObjWithInt:6 withJavaUtilList:self->groups_];
  [writer writeRepeatedObjWithInt:7 withJavaUtilList:self->contacts_];
}

- (jint)getHeaderKey {
  return ImActorModelApiBaseFatSeqUpdate_HEADER;
}

- (void)copyAllFieldsTo:(ImActorModelApiBaseFatSeqUpdate *)other {
  [super copyAllFieldsTo:other];
  other->seq_ = seq_;
  other->state_ = state_;
  other->updateHeader_ = updateHeader_;
  other->update_ = update_;
  other->users_ = users_;
  other->groups_ = groups_;
  other->contacts_ = contacts_;
}

@end

ImActorModelApiBaseFatSeqUpdate *ImActorModelApiBaseFatSeqUpdate_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiBaseFatSeqUpdate_init();
  return ((ImActorModelApiBaseFatSeqUpdate *) BSBser_parseWithBSBserObject_withByteArray_([[ImActorModelApiBaseFatSeqUpdate alloc] init], data));
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiBaseFatSeqUpdate)
