//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/updates/GroupsProcessor.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/api/Avatar.h"
#include "im/actor/model/api/Group.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/engine/KeyValueEngine.h"
#include "im/actor/model/entity/Avatar.h"
#include "im/actor/model/entity/Group.h"
#include "im/actor/model/entity/Message.h"
#include "im/actor/model/entity/MessageState.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/content/ServiceGroupAvatarChanged.h"
#include "im/actor/model/entity/content/ServiceGroupCreated.h"
#include "im/actor/model/entity/content/ServiceGroupTitleChanged.h"
#include "im/actor/model/entity/content/ServiceGroupUserAdded.h"
#include "im/actor/model/entity/content/ServiceGroupUserKicked.h"
#include "im/actor/model/entity/content/ServiceGroupUserLeave.h"
#include "im/actor/model/modules/BaseModule.h"
#include "im/actor/model/modules/Messages.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/messages/DialogsActor.h"
#include "im/actor/model/modules/messages/entity/EntityConverter.h"
#include "im/actor/model/modules/updates/GroupsProcessor.h"
#include "im/actor/model/util/JavaUtil.h"
#include "java/lang/Integer.h"
#include "java/util/ArrayList.h"
#include "java/util/Collection.h"
#include "java/util/List.h"

@interface ImActorModelModulesUpdatesGroupsProcessor ()

- (void)onGroupDescChangedWithAMGroup:(AMGroup *)group;

@end

__attribute__((unused)) static void ImActorModelModulesUpdatesGroupsProcessor_onGroupDescChangedWithAMGroup_(ImActorModelModulesUpdatesGroupsProcessor *self, AMGroup *group);

@implementation ImActorModelModulesUpdatesGroupsProcessor

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules {
  ImActorModelModulesUpdatesGroupsProcessor_initWithImActorModelModulesModules_(self, modules);
  return self;
}

- (void)applyGroupsWithJavaUtilCollection:(id<JavaUtilCollection>)updated
                              withBoolean:(jboolean)forced {
  JavaUtilArrayList *batch = new_JavaUtilArrayList_init();
  for (APGroup * __strong group in nil_chk(updated)) {
    AMGroup *saved = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:[((APGroup *) nil_chk(group)) getId]];
    if (saved == nil) {
      [batch addWithId:ImActorModelModulesMessagesEntityEntityConverter_convertWithAPGroup_(group)];
    }
    else if (forced) {
      AMGroup *upd = ImActorModelModulesMessagesEntityEntityConverter_convertWithAPGroup_(group);
      [batch addWithId:upd];
      if (!AMJavaUtil_equalsEWithId_withId_([((AMGroup *) nil_chk(upd)) getAvatar], [saved getAvatar]) || ![((NSString *) nil_chk([upd getTitle])) isEqual:[saved getTitle]]) {
        ImActorModelModulesUpdatesGroupsProcessor_onGroupDescChangedWithAMGroup_(self, upd);
      }
    }
  }
  if ([batch size] > 0) {
    [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItems:batch];
  }
}

- (void)onGroupInviteWithInt:(jint)groupId
                    withLong:(jlong)rid
                     withInt:(jint)inviterId
                    withLong:(jlong)date
                 withBoolean:(jboolean)isSilent {
  AMGroup *group = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:groupId];
  if (group != nil) {
    [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:[((AMGroup *) nil_chk([group changeMemberWithBoolean:YES])) addMemberWithInt:[self myUid] withInt:inviterId withLong:date]];
    if (!isSilent) {
      if (inviterId == [self myUid]) {
        AMMessage *message = new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid, date, date, inviterId, AMMessageStateEnum_get_UNKNOWN(), AMServiceGroupCreated_create());
        [((DKActorRef *) nil_chk([self conversationActorWithAMPeer:[group peer]])) sendWithId:message];
      }
      else {
        AMMessage *message = new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid, date, date, inviterId, AMMessageStateEnum_get_SENT(), AMServiceGroupUserAdded_createWithInt_([self myUid]));
        [((DKActorRef *) nil_chk([self conversationActorWithAMPeer:[group peer]])) sendWithId:message];
      }
    }
  }
}

- (void)onUserLeaveWithInt:(jint)groupId
                  withLong:(jlong)rid
                   withInt:(jint)uid
                  withLong:(jlong)date
               withBoolean:(jboolean)isSilent {
  AMGroup *group = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:groupId];
  if (group != nil) {
    if (uid == [self myUid]) {
      [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:[((AMGroup *) nil_chk([group clearMembers])) changeMemberWithBoolean:NO]];
    }
    else {
      [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:[group removeMemberWithInt:uid]];
    }
    if (!isSilent) {
      AMMessage *message = new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid, date, date, uid, uid == [self myUid] ? AMMessageStateEnum_get_SENT() : AMMessageStateEnum_get_UNKNOWN(), AMServiceGroupUserLeave_create());
      [((DKActorRef *) nil_chk([self conversationActorWithAMPeer:[group peer]])) sendWithId:message];
    }
  }
}

- (void)onUserKickedWithInt:(jint)groupId
                   withLong:(jlong)rid
                    withInt:(jint)uid
                    withInt:(jint)kicker
                   withLong:(jlong)date
                withBoolean:(jboolean)isSilent {
  AMGroup *group = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:groupId];
  if (group != nil) {
    if (uid == [self myUid]) {
      [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:[((AMGroup *) nil_chk([group clearMembers])) changeMemberWithBoolean:NO]];
    }
    else {
      [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:[group removeMemberWithInt:uid]];
    }
    if (!isSilent) {
      AMMessage *message = new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid, date, date, kicker, kicker == [self myUid] ? AMMessageStateEnum_get_SENT() : AMMessageStateEnum_get_UNKNOWN(), AMServiceGroupUserKicked_createWithInt_(uid));
      [((DKActorRef *) nil_chk([self conversationActorWithAMPeer:[group peer]])) sendWithId:message];
    }
  }
}

- (void)onUserAddedWithInt:(jint)groupId
                  withLong:(jlong)rid
                   withInt:(jint)uid
                   withInt:(jint)adder
                  withLong:(jlong)date
               withBoolean:(jboolean)isSilent {
  AMGroup *group = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:groupId];
  if (group != nil) {
    [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:[group addMemberWithInt:uid withInt:adder withLong:date]];
    if (!isSilent) {
      AMMessage *message = new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid, date, date, adder, adder == [self myUid] ? AMMessageStateEnum_get_SENT() : AMMessageStateEnum_get_UNKNOWN(), AMServiceGroupUserAdded_createWithInt_(uid));
      [((DKActorRef *) nil_chk([self conversationActorWithAMPeer:[group peer]])) sendWithId:message];
    }
  }
}

- (void)onTitleChangedWithInt:(jint)groupId
                     withLong:(jlong)rid
                      withInt:(jint)uid
                 withNSString:(NSString *)title
                     withLong:(jlong)date
                  withBoolean:(jboolean)isSilent {
  AMGroup *group = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:groupId];
  if (group != nil) {
    if (![((NSString *) nil_chk([group getTitle])) isEqual:title]) {
      AMGroup *upd = [group editTitleWithNSString:title];
      [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:upd];
      ImActorModelModulesUpdatesGroupsProcessor_onGroupDescChangedWithAMGroup_(self, upd);
    }
    if (!isSilent) {
      AMMessage *message = new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid, date, date, uid, uid == [self myUid] ? AMMessageStateEnum_get_SENT() : AMMessageStateEnum_get_UNKNOWN(), AMServiceGroupTitleChanged_createWithNSString_(title));
      [((DKActorRef *) nil_chk([self conversationActorWithAMPeer:[group peer]])) sendWithId:message];
    }
  }
}

- (void)onAvatarChangedWithInt:(jint)groupId
                      withLong:(jlong)rid
                       withInt:(jint)uid
                  withAPAvatar:(APAvatar *)avatar
                      withLong:(jlong)date
                   withBoolean:(jboolean)isSilent {
  AMGroup *group = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:groupId];
  if (group != nil) {
    AMGroup *upd = [group editAvatarWithAPAvatar:avatar];
    [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:upd];
    ImActorModelModulesUpdatesGroupsProcessor_onGroupDescChangedWithAMGroup_(self, upd);
    if (!isSilent) {
      AMMessage *message = new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid, date, date, uid, uid == [self myUid] ? AMMessageStateEnum_get_SENT() : AMMessageStateEnum_get_UNKNOWN(), AMServiceGroupAvatarChanged_createWithAPAvatar_(avatar));
      [((DKActorRef *) nil_chk([self conversationActorWithAMPeer:[group peer]])) sendWithId:message];
    }
  }
}

- (void)onMembersUpdatedWithInt:(jint)groupId
               withJavaUtilList:(id<JavaUtilList>)members {
  AMGroup *group = [((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:groupId];
  if (group != nil) {
    group = [group updateMembersWithJavaUtilList:members];
    [((id<DKKeyValueEngine>) nil_chk([self groups])) addOrUpdateItem:group];
  }
}

- (jboolean)hasGroupsWithJavaUtilCollection:(id<JavaUtilCollection>)gids {
  for (JavaLangInteger * __strong uid in nil_chk(gids)) {
    if ([((id<DKKeyValueEngine>) nil_chk([self groups])) getValueWithKey:[((JavaLangInteger *) nil_chk(uid)) intValue]] == nil) {
      return NO;
    }
  }
  return YES;
}

- (void)onGroupDescChangedWithAMGroup:(AMGroup *)group {
  ImActorModelModulesUpdatesGroupsProcessor_onGroupDescChangedWithAMGroup_(self, group);
}

@end

void ImActorModelModulesUpdatesGroupsProcessor_initWithImActorModelModulesModules_(ImActorModelModulesUpdatesGroupsProcessor *self, ImActorModelModulesModules *modules) {
  (void) ImActorModelModulesBaseModule_initWithImActorModelModulesModules_(self, modules);
}

ImActorModelModulesUpdatesGroupsProcessor *new_ImActorModelModulesUpdatesGroupsProcessor_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) {
  ImActorModelModulesUpdatesGroupsProcessor *self = [ImActorModelModulesUpdatesGroupsProcessor alloc];
  ImActorModelModulesUpdatesGroupsProcessor_initWithImActorModelModulesModules_(self, modules);
  return self;
}

void ImActorModelModulesUpdatesGroupsProcessor_onGroupDescChangedWithAMGroup_(ImActorModelModulesUpdatesGroupsProcessor *self, AMGroup *group) {
  [((DKActorRef *) nil_chk([((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getDialogsActor])) sendWithId:new_ImActorModelModulesMessagesDialogsActor_GroupChanged_initWithAMGroup_(group)];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesUpdatesGroupsProcessor)
