//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/messages/entity/PlainCursor.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/messages/entity/PlainCursor.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/modules/messages/entity/PlainCursor.h"
#include "java/io/IOException.h"

@interface ImActorModelModulesMessagesEntityPlainCursor () {
 @public
  AMPeer *peer_;
  jlong sortDate_;
  jlong pendingSortDate_;
}

- (instancetype)init;

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesMessagesEntityPlainCursor, peer_, AMPeer *)

__attribute__((unused)) static void ImActorModelModulesMessagesEntityPlainCursor_init(ImActorModelModulesMessagesEntityPlainCursor *self);

__attribute__((unused)) static ImActorModelModulesMessagesEntityPlainCursor *new_ImActorModelModulesMessagesEntityPlainCursor_init() NS_RETURNS_RETAINED;


#line 15
@implementation ImActorModelModulesMessagesEntityPlainCursor

+ (ImActorModelModulesMessagesEntityPlainCursor *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelModulesMessagesEntityPlainCursor_fromBytesWithByteArray_(data);
}


#line 25
- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)sortDate
                      withLong:(jlong)pendingSortDate {
  ImActorModelModulesMessagesEntityPlainCursor_initWithAMPeer_withLong_withLong_(self, peer, sortDate, pendingSortDate);
  return self;
}


#line 31
- (instancetype)init {
  ImActorModelModulesMessagesEntityPlainCursor_init(self);
  return self;
}


#line 35
- (AMPeer *)getPeer {
  return peer_;
}

- (jlong)getSortDate {
  return sortDate_;
}

- (jlong)getPendingSortDate {
  return pendingSortDate_;
}

- (ImActorModelModulesMessagesEntityPlainCursor *)changeSortDateWithLong:(jlong)date {
  return new_ImActorModelModulesMessagesEntityPlainCursor_initWithAMPeer_withLong_withLong_(peer_, date, pendingSortDate_);
}

- (ImActorModelModulesMessagesEntityPlainCursor *)changePendingSortDateWithLong:(jlong)pendingDate {
  return new_ImActorModelModulesMessagesEntityPlainCursor_initWithAMPeer_withLong_withLong_(peer_, sortDate_, pendingDate);
}


#line 56
- (void)parseWithBSBserValues:(BSBserValues *)values {
  peer_ = AMPeer_fromUniqueIdWithLong_([((BSBserValues *) nil_chk(values)) getLongWithInt:1]);
  sortDate_ = [values getLongWithInt:2];
  pendingSortDate_ = [values getLongWithInt:3];
}


#line 63
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeLongWithInt:1 withLong:[((AMPeer *) nil_chk(peer_)) getUnuqueId]];
  [writer writeLongWithInt:2 withLong:sortDate_];
  [writer writeLongWithInt:3 withLong:pendingSortDate_];
}

@end


#line 17
ImActorModelModulesMessagesEntityPlainCursor *ImActorModelModulesMessagesEntityPlainCursor_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelModulesMessagesEntityPlainCursor_initialize();
  
#line 18
  return ((ImActorModelModulesMessagesEntityPlainCursor *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelModulesMessagesEntityPlainCursor_init(), data));
}


#line 25
void ImActorModelModulesMessagesEntityPlainCursor_initWithAMPeer_withLong_withLong_(ImActorModelModulesMessagesEntityPlainCursor *self, AMPeer *peer, jlong sortDate, jlong pendingSortDate) {
  (void) BSBserObject_init(self);
  
#line 26
  self->peer_ = peer;
  self->sortDate_ = sortDate;
  self->pendingSortDate_ = pendingSortDate;
}


#line 25
ImActorModelModulesMessagesEntityPlainCursor *new_ImActorModelModulesMessagesEntityPlainCursor_initWithAMPeer_withLong_withLong_(AMPeer *peer, jlong sortDate, jlong pendingSortDate) {
  ImActorModelModulesMessagesEntityPlainCursor *self = [ImActorModelModulesMessagesEntityPlainCursor alloc];
  ImActorModelModulesMessagesEntityPlainCursor_initWithAMPeer_withLong_withLong_(self, peer, sortDate, pendingSortDate);
  return self;
}

void ImActorModelModulesMessagesEntityPlainCursor_init(ImActorModelModulesMessagesEntityPlainCursor *self) {
  (void) BSBserObject_init(self);
}


#line 31
ImActorModelModulesMessagesEntityPlainCursor *new_ImActorModelModulesMessagesEntityPlainCursor_init() {
  ImActorModelModulesMessagesEntityPlainCursor *self = [ImActorModelModulesMessagesEntityPlainCursor alloc];
  ImActorModelModulesMessagesEntityPlainCursor_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesEntityPlainCursor)
