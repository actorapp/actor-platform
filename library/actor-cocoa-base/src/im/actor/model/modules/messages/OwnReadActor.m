//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/messages/OwnReadActor.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/messages/OwnReadActor.java"

#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/actors/Actor.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/engine/SyncKeyValue.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/modules/Messages.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/Notifications.h"
#include "im/actor/model/modules/messages/CursorReaderActor.h"
#include "im/actor/model/modules/messages/DialogsActor.h"
#include "im/actor/model/modules/messages/OwnReadActor.h"
#include "im/actor/model/modules/messages/entity/UnreadMessage.h"
#include "im/actor/model/modules/messages/entity/UnreadMessagesStorage.h"
#include "im/actor/model/modules/utils/ModuleActor.h"
#include "java/io/IOException.h"
#include "java/lang/Long.h"
#include "java/lang/Math.h"
#include "java/util/HashSet.h"
#include "java/util/List.h"
#include "java/util/Set.h"

@interface ImActorModelModulesMessagesOwnReadActor () {
 @public
  ImActorModelModulesMessagesEntityUnreadMessagesStorage *messagesStorage_;
  DKSyncKeyValue *syncKeyValue_;
}

- (void)saveStorage;

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesMessagesOwnReadActor, messagesStorage_, ImActorModelModulesMessagesEntityUnreadMessagesStorage *)
J2OBJC_FIELD_SETTER(ImActorModelModulesMessagesOwnReadActor, syncKeyValue_, DKSyncKeyValue *)

__attribute__((unused)) static void ImActorModelModulesMessagesOwnReadActor_saveStorage(ImActorModelModulesMessagesOwnReadActor *self);


#line 18
@implementation ImActorModelModulesMessagesOwnReadActor


#line 23
- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)messenger {
  ImActorModelModulesMessagesOwnReadActor_initWithImActorModelModulesModules_(self, messenger);
  return self;
}


#line 29
- (void)preStart {
  [super preStart];
  
#line 32
  messagesStorage_ = new_ImActorModelModulesMessagesEntityUnreadMessagesStorage_init();
  IOSByteArray *st = [((DKSyncKeyValue *) nil_chk(syncKeyValue_)) getWithLong:ImActorModelModulesUtilsModuleActor_CURSOR_OWN_READ];
  if (st != nil) {
    @try {
      messagesStorage_ = ImActorModelModulesMessagesEntityUnreadMessagesStorage_fromBytesWithByteArray_(st);
    }
    @catch (
#line 37
    JavaIoIOException *e) {
      [((JavaIoIOException *) nil_chk(e)) printStackTrace];
    }
  }
}


#line 43
- (void)onNewInMessageWithAMPeer:(AMPeer *)peer
                        withLong:(jlong)rid
                        withLong:(jlong)sortingDate {
  
#line 45
  jlong readState = [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) loadReadStateWithAMPeer:peer];
  if (sortingDate <= readState) {
    
#line 48
    return;
  }
  
#line 52
  JavaUtilHashSet *unread = [((ImActorModelModulesMessagesEntityUnreadMessagesStorage *) nil_chk(messagesStorage_)) getUnreadWithAMPeer:peer];
  [((JavaUtilHashSet *) nil_chk(unread)) addWithId:new_ImActorModelModulesMessagesEntityUnreadMessage_initWithAMPeer_withLong_withLong_(peer, rid, sortingDate)];
  ImActorModelModulesMessagesOwnReadActor_saveStorage(self);
  
#line 57
  [((DKActorRef *) nil_chk([((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getDialogsActor])) sendWithId:new_ImActorModelModulesMessagesDialogsActor_CounterChanged_initWithAMPeer_withInt_(
#line 58
  peer, [unread size])];
}


#line 61
- (void)onMessageReadWithAMPeer:(AMPeer *)peer
                       withLong:(jlong)sortingDate {
  
#line 63
  jlong readState = [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) loadReadStateWithAMPeer:peer];
  if (sortingDate <= readState) {
    
#line 66
    return;
  }
  
#line 70
  JavaUtilHashSet *unread = [((ImActorModelModulesMessagesEntityUnreadMessagesStorage *) nil_chk(messagesStorage_)) getUnreadWithAMPeer:peer];
  
#line 72
  jlong maxPlainReadDate = sortingDate;
  jboolean removed = NO;
  {
    IOSObjectArray *a__ =
#line 74
    [((JavaUtilHashSet *) nil_chk(unread)) toArrayWithNSObjectArray:[IOSObjectArray newArrayWithLength:0 type:ImActorModelModulesMessagesEntityUnreadMessage_class_()]];
    ImActorModelModulesMessagesEntityUnreadMessage * const *b__ = ((IOSObjectArray *) nil_chk(a__))->buffer_;
    ImActorModelModulesMessagesEntityUnreadMessage * const *e__ = b__ + a__->size_;
    while (b__ < e__) {
      ImActorModelModulesMessagesEntityUnreadMessage *u = *b__++;
      
#line 75
      if ([((ImActorModelModulesMessagesEntityUnreadMessage *) nil_chk(u)) getSortDate] <= sortingDate) {
        maxPlainReadDate = JavaLangMath_maxWithLong_withLong_([u getSortDate], maxPlainReadDate);
        removed = YES;
        [unread removeWithId:u];
      }
    }
  }
  
#line 81
  if (removed) {
    ImActorModelModulesMessagesOwnReadActor_saveStorage(self);
  }
  
#line 85
  if (maxPlainReadDate > 0) {
    [((DKActorRef *) nil_chk([((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getPlainReadActor])) sendWithId:new_ImActorModelModulesMessagesCursorReaderActor_MarkRead_initWithAMPeer_withLong_(
#line 87
    peer, maxPlainReadDate)];
  }
  
#line 91
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) saveReadStateWithAMPeer:peer withLong:sortingDate];
  
#line 94
  [((DKActorRef *) nil_chk([((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getDialogsActor])) sendWithId:new_ImActorModelModulesMessagesDialogsActor_CounterChanged_initWithAMPeer_withInt_(
#line 95
  peer, [unread size])];
  
#line 97
  [((ImActorModelModulesNotifications *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getNotifications])) onOwnReadWithAMPeer:peer withLong:sortingDate];
}


#line 100
- (void)onMessageReadByMeWithAMPeer:(AMPeer *)peer
                           withLong:(jlong)sortingDate {
  
#line 102
  jlong msgSortingDate = 0;
  
#line 105
  id<JavaUtilSet> unread = [((ImActorModelModulesMessagesEntityUnreadMessagesStorage *) nil_chk(messagesStorage_)) getUnreadWithAMPeer:peer];
  {
    IOSObjectArray *a__ =
#line 106
    [((id<JavaUtilSet>) nil_chk(unread)) toArrayWithNSObjectArray:[IOSObjectArray newArrayWithLength:0 type:ImActorModelModulesMessagesEntityUnreadMessage_class_()]];
    ImActorModelModulesMessagesEntityUnreadMessage * const *b__ = ((IOSObjectArray *) nil_chk(a__))->buffer_;
    ImActorModelModulesMessagesEntityUnreadMessage * const *e__ = b__ + a__->size_;
    while (b__ < e__) {
      ImActorModelModulesMessagesEntityUnreadMessage *u = *b__++;
      
#line 107
      if ([((ImActorModelModulesMessagesEntityUnreadMessage *) nil_chk(u)) getSortDate] <= sortingDate && [u getSortDate] > msgSortingDate) {
        msgSortingDate = [u getSortDate];
      }
    }
  }
  if (msgSortingDate > 0) {
    [self onMessageReadWithAMPeer:peer withLong:msgSortingDate];
  }
}


#line 117
- (void)onMessageDeleteWithAMPeer:(AMPeer *)peer
                 withJavaUtilList:(id<JavaUtilList>)rids {
  id<JavaUtilSet> unread = [((ImActorModelModulesMessagesEntityUnreadMessagesStorage *) nil_chk(messagesStorage_)) getUnreadWithAMPeer:peer];
  jboolean isRemoved = NO;
  {
    IOSObjectArray *a__ =
#line 120
    [((id<JavaUtilSet>) nil_chk(unread)) toArrayWithNSObjectArray:[IOSObjectArray newArrayWithLength:0 type:ImActorModelModulesMessagesEntityUnreadMessage_class_()]];
    ImActorModelModulesMessagesEntityUnreadMessage * const *b__ = ((IOSObjectArray *) nil_chk(a__))->buffer_;
    ImActorModelModulesMessagesEntityUnreadMessage * const *e__ = b__ + a__->size_;
    while (b__ < e__) {
      ImActorModelModulesMessagesEntityUnreadMessage *u = *b__++;
      
#line 121
      if ([((id<JavaUtilList>) nil_chk(rids)) containsWithId:JavaLangLong_valueOfWithLong_([((ImActorModelModulesMessagesEntityUnreadMessage *) nil_chk(u)) getRid])]) {
        [unread removeWithId:u];
        isRemoved = YES;
      }
    }
  }
  
#line 126
  if (!isRemoved) {
    return;
  }
  
#line 130
  ImActorModelModulesMessagesOwnReadActor_saveStorage(self);
  
#line 133
  [((DKActorRef *) nil_chk([((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getDialogsActor])) sendWithId:new_ImActorModelModulesMessagesDialogsActor_CounterChanged_initWithAMPeer_withInt_(
#line 134
  peer, [unread size])];
}


#line 139
- (void)saveStorage {
  ImActorModelModulesMessagesOwnReadActor_saveStorage(self);
}


#line 146
- (void)onReceiveWithId:(id)message {
  if ([message isKindOfClass:[ImActorModelModulesMessagesOwnReadActor_NewMessage class]]) {
    ImActorModelModulesMessagesOwnReadActor_NewMessage *newMessage = (ImActorModelModulesMessagesOwnReadActor_NewMessage *) check_class_cast(message, [ImActorModelModulesMessagesOwnReadActor_NewMessage class]);
    [self onNewInMessageWithAMPeer:[((ImActorModelModulesMessagesOwnReadActor_NewMessage *) nil_chk(newMessage)) getPeer] withLong:[newMessage getRid] withLong:[newMessage getSortingDate]];
  }
  else
#line 150
  if ([message isKindOfClass:[ImActorModelModulesMessagesOwnReadActor_MessageRead class]]) {
    ImActorModelModulesMessagesOwnReadActor_MessageRead *messageRead = (ImActorModelModulesMessagesOwnReadActor_MessageRead *) check_class_cast(message, [ImActorModelModulesMessagesOwnReadActor_MessageRead class]);
    [self onMessageReadWithAMPeer:[((ImActorModelModulesMessagesOwnReadActor_MessageRead *) nil_chk(messageRead)) getPeer] withLong:[messageRead getSortingDate]];
  }
  else
#line 153
  if ([message isKindOfClass:[ImActorModelModulesMessagesOwnReadActor_MessageReadByMe class]]) {
    ImActorModelModulesMessagesOwnReadActor_MessageReadByMe *readByMe = (ImActorModelModulesMessagesOwnReadActor_MessageReadByMe *) check_class_cast(message, [ImActorModelModulesMessagesOwnReadActor_MessageReadByMe class]);
    [self onMessageReadByMeWithAMPeer:[((ImActorModelModulesMessagesOwnReadActor_MessageReadByMe *) nil_chk(readByMe)) getPeer] withLong:[readByMe getSortDate]];
  }
  else
#line 156
  if ([message isKindOfClass:[ImActorModelModulesMessagesOwnReadActor_MessageDeleted class]]) {
    ImActorModelModulesMessagesOwnReadActor_MessageDeleted *deleted = (ImActorModelModulesMessagesOwnReadActor_MessageDeleted *) check_class_cast(message, [ImActorModelModulesMessagesOwnReadActor_MessageDeleted class]);
    [self onMessageDeleteWithAMPeer:[((ImActorModelModulesMessagesOwnReadActor_MessageDeleted *) nil_chk(deleted)) getPeer] withJavaUtilList:[deleted getRids]];
  }
  else {
    
#line 160
    [self dropWithId:message];
  }
}

@end


#line 23
void ImActorModelModulesMessagesOwnReadActor_initWithImActorModelModulesModules_(ImActorModelModulesMessagesOwnReadActor *self, ImActorModelModulesModules *messenger) {
  (void) ImActorModelModulesUtilsModuleActor_initWithImActorModelModulesModules_(self, messenger);
  self->syncKeyValue_ = [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(messenger)) getMessagesModule])) getCursorStorage];
}


#line 23
ImActorModelModulesMessagesOwnReadActor *new_ImActorModelModulesMessagesOwnReadActor_initWithImActorModelModulesModules_(ImActorModelModulesModules *messenger) {
  ImActorModelModulesMessagesOwnReadActor *self = [ImActorModelModulesMessagesOwnReadActor alloc];
  ImActorModelModulesMessagesOwnReadActor_initWithImActorModelModulesModules_(self, messenger);
  return self;
}


#line 139
void ImActorModelModulesMessagesOwnReadActor_saveStorage(ImActorModelModulesMessagesOwnReadActor *self) {
  [((DKSyncKeyValue *) nil_chk(self->syncKeyValue_)) putWithLong:ImActorModelModulesUtilsModuleActor_CURSOR_OWN_READ withByteArray:[((ImActorModelModulesMessagesEntityUnreadMessagesStorage *) nil_chk(self->messagesStorage_)) toByteArray]];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesOwnReadActor)


#line 164
@implementation ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted


#line 168
- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)rid {
  ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted_initWithAMPeer_withLong_(self, peer, rid);
  return self;
}


#line 173
- (AMPeer *)getPeer {
  return peer_;
}

- (jlong)getRid {
  return rid_;
}

@end


#line 168
void ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted_initWithAMPeer_withLong_(ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted *self, AMPeer *peer, jlong rid) {
  (void) NSObject_init(self);
  
#line 169
  self->peer_ = peer;
  self->rid_ = rid;
}


#line 168
ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted *new_ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted_initWithAMPeer_withLong_(AMPeer *peer, jlong rid) {
  ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted *self = [ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted alloc];
  ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted_initWithAMPeer_withLong_(self, peer, rid);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesOwnReadActor_MessageReadByMeEncrypted)


#line 182
@implementation ImActorModelModulesMessagesOwnReadActor_MessageReadByMe


#line 186
- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)sortDate {
  ImActorModelModulesMessagesOwnReadActor_MessageReadByMe_initWithAMPeer_withLong_(self, peer, sortDate);
  return self;
}


#line 191
- (AMPeer *)getPeer {
  return peer_;
}

- (jlong)getSortDate {
  return sortDate_;
}

@end


#line 186
void ImActorModelModulesMessagesOwnReadActor_MessageReadByMe_initWithAMPeer_withLong_(ImActorModelModulesMessagesOwnReadActor_MessageReadByMe *self, AMPeer *peer, jlong sortDate) {
  (void) NSObject_init(self);
  
#line 187
  self->peer_ = peer;
  self->sortDate_ = sortDate;
}


#line 186
ImActorModelModulesMessagesOwnReadActor_MessageReadByMe *new_ImActorModelModulesMessagesOwnReadActor_MessageReadByMe_initWithAMPeer_withLong_(AMPeer *peer, jlong sortDate) {
  ImActorModelModulesMessagesOwnReadActor_MessageReadByMe *self = [ImActorModelModulesMessagesOwnReadActor_MessageReadByMe alloc];
  ImActorModelModulesMessagesOwnReadActor_MessageReadByMe_initWithAMPeer_withLong_(self, peer, sortDate);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesOwnReadActor_MessageReadByMe)


#line 200
@implementation ImActorModelModulesMessagesOwnReadActor_MessageRead


#line 204
- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)sortingDate {
  ImActorModelModulesMessagesOwnReadActor_MessageRead_initWithAMPeer_withLong_(self, peer, sortingDate);
  return self;
}


#line 209
- (AMPeer *)getPeer {
  return peer_;
}

- (jlong)getSortingDate {
  return sortingDate_;
}

@end


#line 204
void ImActorModelModulesMessagesOwnReadActor_MessageRead_initWithAMPeer_withLong_(ImActorModelModulesMessagesOwnReadActor_MessageRead *self, AMPeer *peer, jlong sortingDate) {
  (void) NSObject_init(self);
  
#line 205
  self->peer_ = peer;
  self->sortingDate_ = sortingDate;
}


#line 204
ImActorModelModulesMessagesOwnReadActor_MessageRead *new_ImActorModelModulesMessagesOwnReadActor_MessageRead_initWithAMPeer_withLong_(AMPeer *peer, jlong sortingDate) {
  ImActorModelModulesMessagesOwnReadActor_MessageRead *self = [ImActorModelModulesMessagesOwnReadActor_MessageRead alloc];
  ImActorModelModulesMessagesOwnReadActor_MessageRead_initWithAMPeer_withLong_(self, peer, sortingDate);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesOwnReadActor_MessageRead)


#line 218
@implementation ImActorModelModulesMessagesOwnReadActor_NewMessage


#line 223
- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)rid
                      withLong:(jlong)sortingDate {
  ImActorModelModulesMessagesOwnReadActor_NewMessage_initWithAMPeer_withLong_withLong_(self, peer, rid, sortingDate);
  return self;
}


#line 229
- (AMPeer *)getPeer {
  return peer_;
}

- (jlong)getRid {
  return rid_;
}

- (jlong)getSortingDate {
  return sortingDate_;
}

@end


#line 223
void ImActorModelModulesMessagesOwnReadActor_NewMessage_initWithAMPeer_withLong_withLong_(ImActorModelModulesMessagesOwnReadActor_NewMessage *self, AMPeer *peer, jlong rid, jlong sortingDate) {
  (void) NSObject_init(self);
  
#line 224
  self->peer_ = peer;
  self->rid_ = rid;
  self->sortingDate_ = sortingDate;
}


#line 223
ImActorModelModulesMessagesOwnReadActor_NewMessage *new_ImActorModelModulesMessagesOwnReadActor_NewMessage_initWithAMPeer_withLong_withLong_(AMPeer *peer, jlong rid, jlong sortingDate) {
  ImActorModelModulesMessagesOwnReadActor_NewMessage *self = [ImActorModelModulesMessagesOwnReadActor_NewMessage alloc];
  ImActorModelModulesMessagesOwnReadActor_NewMessage_initWithAMPeer_withLong_withLong_(self, peer, rid, sortingDate);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesOwnReadActor_NewMessage)


#line 242
@implementation ImActorModelModulesMessagesOwnReadActor_MessageDeleted


#line 246
- (instancetype)initWithAMPeer:(AMPeer *)peer
              withJavaUtilList:(id<JavaUtilList>)rids {
  ImActorModelModulesMessagesOwnReadActor_MessageDeleted_initWithAMPeer_withJavaUtilList_(self, peer, rids);
  return self;
}


#line 251
- (AMPeer *)getPeer {
  return peer_;
}

- (id<JavaUtilList>)getRids {
  return rids_;
}

@end


#line 246
void ImActorModelModulesMessagesOwnReadActor_MessageDeleted_initWithAMPeer_withJavaUtilList_(ImActorModelModulesMessagesOwnReadActor_MessageDeleted *self, AMPeer *peer, id<JavaUtilList> rids) {
  (void) NSObject_init(self);
  
#line 247
  self->peer_ = peer;
  self->rids_ = rids;
}


#line 246
ImActorModelModulesMessagesOwnReadActor_MessageDeleted *new_ImActorModelModulesMessagesOwnReadActor_MessageDeleted_initWithAMPeer_withJavaUtilList_(AMPeer *peer, id<JavaUtilList> rids) {
  ImActorModelModulesMessagesOwnReadActor_MessageDeleted *self = [ImActorModelModulesMessagesOwnReadActor_MessageDeleted alloc];
  ImActorModelModulesMessagesOwnReadActor_MessageDeleted_initWithAMPeer_withJavaUtilList_(self, peer, rids);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesOwnReadActor_MessageDeleted)
