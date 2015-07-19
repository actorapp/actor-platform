//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/modules/Notifications.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/StorageProvider.h"
#include "im/actor/model/droidkit/actors/ActorCreator.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/actors/ActorSystem.h"
#include "im/actor/model/droidkit/actors/Props.h"
#include "im/actor/model/droidkit/engine/KeyValueStorage.h"
#include "im/actor/model/droidkit/engine/SyncKeyValue.h"
#include "im/actor/model/entity/ContentDescription.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/modules/BaseModule.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/Notifications.h"
#include "im/actor/model/modules/notifications/NotificationsActor.h"

@interface ImActorModelModulesNotifications () {
 @public
  DKActorRef *notificationsActor_;
  DKSyncKeyValue *notificationsStorage_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesNotifications, notificationsActor_, DKActorRef *)
J2OBJC_FIELD_SETTER(ImActorModelModulesNotifications, notificationsStorage_, DKSyncKeyValue *)

@interface ImActorModelModulesNotifications_$1 : NSObject < DKActorCreator > {
 @public
  ImActorModelModulesNotifications *this$0_;
}

- (ImActorModelModulesNotificationsNotificationsActor *)create;

- (instancetype)initWithImActorModelModulesNotifications:(ImActorModelModulesNotifications *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesNotifications_$1)

J2OBJC_FIELD_SETTER(ImActorModelModulesNotifications_$1, this$0_, ImActorModelModulesNotifications *)

__attribute__((unused)) static void ImActorModelModulesNotifications_$1_initWithImActorModelModulesNotifications_(ImActorModelModulesNotifications_$1 *self, ImActorModelModulesNotifications *outer$);

__attribute__((unused)) static ImActorModelModulesNotifications_$1 *new_ImActorModelModulesNotifications_$1_initWithImActorModelModulesNotifications_(ImActorModelModulesNotifications *outer$) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesNotifications_$1)

@implementation ImActorModelModulesNotifications

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules {
  ImActorModelModulesNotifications_initWithImActorModelModulesModules_(self, modules);
  return self;
}

- (void)run {
  self->notificationsActor_ = [((DKActorSystem *) nil_chk(DKActorSystem_system())) actorOfWithDKProps:DKProps_createWithIOSClass_withDKActorCreator_(ImActorModelModulesNotificationsNotificationsActor_class_(), new_ImActorModelModulesNotifications_$1_initWithImActorModelModulesNotifications_(self)) withNSString:@"actor/notifications"];
}

- (DKSyncKeyValue *)getNotificationsStorage {
  return notificationsStorage_;
}

- (void)onOwnReadWithAMPeer:(AMPeer *)peer
                   withLong:(jlong)fromDate {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_MessagesRead_initWithAMPeer_withLong_(peer, fromDate)];
}

- (void)onInMessageWithAMPeer:(AMPeer *)peer
                      withInt:(jint)sender
                     withLong:(jlong)sortDate
     withAMContentDescription:(AMContentDescription *)contentDescription
                  withBoolean:(jboolean)hasCurrentUserMention
                  withBoolean:(jboolean)isAlreadyRead
                  withBoolean:(jboolean)isLastInDiff {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_NewMessage_initWithAMPeer_withInt_withLong_withAMContentDescription_withBoolean_withBoolean_withBoolean_(peer, sender, sortDate, contentDescription, hasCurrentUserMention, isAlreadyRead, isLastInDiff)];
}

- (void)onConversationOpenWithAMPeer:(AMPeer *)peer {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_OnConversationVisible_initWithAMPeer_(peer)];
}

- (void)onConversationCloseWithAMPeer:(AMPeer *)peer {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_OnConversationHidden_initWithAMPeer_(peer)];
}

- (void)onDialogsOpen {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_OnDialogsVisible_init()];
}

- (void)onDialogsClosed {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_OnDialogsHidden_init()];
}

- (void)onAppVisible {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_OnAppVisible_init()];
}

- (void)onAppHidden {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_OnAppHidden_init()];
}

- (void)pauseNotifications {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_PauseNotifications_init()];
}

- (void)resumeNotifications {
  [((DKActorRef *) nil_chk(notificationsActor_)) sendWithId:new_ImActorModelModulesNotificationsNotificationsActor_ResumeNotifications_init()];
}

- (void)resetModule {
}

@end

void ImActorModelModulesNotifications_initWithImActorModelModulesModules_(ImActorModelModulesNotifications *self, ImActorModelModulesModules *modules) {
  (void) ImActorModelModulesBaseModule_initWithImActorModelModulesModules_(self, modules);
  self->notificationsStorage_ = new_DKSyncKeyValue_initWithDKKeyValueStorage_([((id<AMStorageProvider>) nil_chk([self storage])) createKeyValueWithName:ImActorModelModulesBaseModule_get_STORAGE_NOTIFICATIONS_()]);
}

ImActorModelModulesNotifications *new_ImActorModelModulesNotifications_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) {
  ImActorModelModulesNotifications *self = [ImActorModelModulesNotifications alloc];
  ImActorModelModulesNotifications_initWithImActorModelModulesModules_(self, modules);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesNotifications)

@implementation ImActorModelModulesNotifications_$1

- (ImActorModelModulesNotificationsNotificationsActor *)create {
  return new_ImActorModelModulesNotificationsNotificationsActor_initWithImActorModelModulesModules_([this$0_ modules]);
}

- (instancetype)initWithImActorModelModulesNotifications:(ImActorModelModulesNotifications *)outer$ {
  ImActorModelModulesNotifications_$1_initWithImActorModelModulesNotifications_(self, outer$);
  return self;
}

@end

void ImActorModelModulesNotifications_$1_initWithImActorModelModulesNotifications_(ImActorModelModulesNotifications_$1 *self, ImActorModelModulesNotifications *outer$) {
  self->this$0_ = outer$;
  (void) NSObject_init(self);
}

ImActorModelModulesNotifications_$1 *new_ImActorModelModulesNotifications_$1_initWithImActorModelModulesNotifications_(ImActorModelModulesNotifications *outer$) {
  ImActorModelModulesNotifications_$1 *self = [ImActorModelModulesNotifications_$1 alloc];
  ImActorModelModulesNotifications_$1_initWithImActorModelModulesNotifications_(self, outer$);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesNotifications_$1)
