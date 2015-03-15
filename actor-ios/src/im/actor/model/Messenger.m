//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/Messenger.java
//

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/AuthState.h"
#include "im/actor/model/Configuration.h"
#include "im/actor/model/CryptoProvider.h"
#include "im/actor/model/LogProvider.h"
#include "im/actor/model/MainThreadProvider.h"
#include "im/actor/model/Messenger.h"
#include "im/actor/model/ThreadingProvider.h"
#include "im/actor/model/concurrency/Command.h"
#include "im/actor/model/crypto/CryptoUtils.h"
#include "im/actor/model/droidkit/actors/Actor.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/actors/ActorScope.h"
#include "im/actor/model/droidkit/actors/ActorSystem.h"
#include "im/actor/model/droidkit/actors/Environment.h"
#include "im/actor/model/droidkit/actors/mailbox/Envelope.h"
#include "im/actor/model/droidkit/engine/ListEngine.h"
#include "im/actor/model/entity/FileReference.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/content/FastThumb.h"
#include "im/actor/model/files/FileReference.h"
#include "im/actor/model/i18n/I18nEngine.h"
#include "im/actor/model/log/Log.h"
#include "im/actor/model/modules/Auth.h"
#include "im/actor/model/modules/Contacts.h"
#include "im/actor/model/modules/DisplayLists.h"
#include "im/actor/model/modules/Files.h"
#include "im/actor/model/modules/Groups.h"
#include "im/actor/model/modules/Messages.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/Notifications.h"
#include "im/actor/model/modules/Presence.h"
#include "im/actor/model/modules/Profile.h"
#include "im/actor/model/modules/Settings.h"
#include "im/actor/model/modules/Typing.h"
#include "im/actor/model/modules/Users.h"
#include "im/actor/model/modules/file/DownloadCallback.h"
#include "im/actor/model/modules/file/UploadCallback.h"
#include "im/actor/model/mvvm/BindedDisplayList.h"
#include "im/actor/model/mvvm/MVVMCollection.h"
#include "im/actor/model/mvvm/MVVMEngine.h"
#include "im/actor/model/viewmodel/FileVM.h"
#include "im/actor/model/viewmodel/FileVMCallback.h"
#include "im/actor/model/viewmodel/GroupTypingVM.h"
#include "im/actor/model/viewmodel/OwnAvatarVM.h"
#include "im/actor/model/viewmodel/UploadFileVM.h"
#include "im/actor/model/viewmodel/UploadFileVMCallback.h"
#include "im/actor/model/viewmodel/UserTypingVM.h"
#include "java/lang/Exception.h"

@interface AMMessenger () {
 @public
  ImActorModelModulesModules *modules_;
}
@end

J2OBJC_FIELD_SETTER(AMMessenger, modules_, ImActorModelModulesModules *)

@implementation AMMessenger

NSString * AMMessenger_TAG_ = @"CORE_INIT";

- (instancetype)initWithConfig:(AMConfiguration *)configuration {
  [super init]AMLog_setLogWithAMLogProvider_([((AMConfiguration *) nil_chk(configuration)) getLog]);
  jlong start = [((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime];
  DKEnvironment_setThreadingProviderWithAMThreadingProvider_([configuration getThreadingProvider]);
  AMLog_dWithNSString_withNSString_(AMMessenger_TAG_, JreStrcat("$J$", @"Loading stage1 in ", ([((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime] - start), @" ms"));
  start = [((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime];
  ImActorModelCryptoCryptoUtils_init__WithAMCryptoProvider_([configuration getCryptoProvider]);
  AMLog_dWithNSString_withNSString_(AMMessenger_TAG_, JreStrcat("$J$", @"Loading stage2 in ", ([((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime] - start), @" ms"));
  start = [((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime];
  AMMVVMEngine_init__WithAMMainThreadProvider_([configuration getMainThreadProvider]);
  AMLog_dWithNSString_withNSString_(AMMessenger_TAG_, JreStrcat("$J$", @"Loading stage3 in ", ([((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime] - start), @" ms"));
  start = [((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime];
  [((DKActorSystem *) nil_chk(DKActorSystem_system())) setTraceInterfaceWithImActorModelDroidkitActorsDebugTraceInterface:[[AMMessenger_$1 alloc] init]];
  [((DKActorSystem *) nil_chk(DKActorSystem_system())) addDispatcherWithNSString:@"db" withInt:1];
  AMLog_dWithNSString_withNSString_(AMMessenger_TAG_, JreStrcat("$J$", @"Loading stage4 in ", ([((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime] - start), @" ms"));
  start = [((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime];
  self->modules_ = [[ImActorModelModulesModules alloc] initWithAMConfiguration:configuration];
  AMLog_dWithNSString_withNSString_(AMMessenger_TAG_, JreStrcat("$J$", @"Loading stage5 in ", ([((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime] - start), @" ms"));
  start = [((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime];
  [self->modules_ run];
  AMLog_dWithNSString_withNSString_(AMMessenger_TAG_, JreStrcat("$J$", @"Loading stage6 in ", ([((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime] - start), @" ms"));
  start = [((id<AMThreadingProvider>) nil_chk([configuration getThreadingProvider])) getActorTime];
}

- (AMAuthStateEnum *)getAuthState {
  return [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) getAuthState];
}

- (jboolean)isLoggedIn {
  return [self getAuthState] == AMAuthStateEnum_get_LOGGED_IN();
}

- (id<AMCommand>)requestSmsWithLong:(jlong)phone {
  return [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) requestSmsWithLong:phone];
}

- (id<AMCommand>)sendCodeWithInt:(jint)code {
  return [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) sendCodeWithInt:code];
}

- (id<AMCommand>)signUpWithNSString:(NSString *)firstName
                       withNSString:(NSString *)avatarPath
                        withBoolean:(jboolean)isSilent {
  return [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) signUpWithNSString:firstName withNSString:avatarPath withBoolean:isSilent];
}

- (jlong)getAuthPhone {
  return [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) getPhone];
}

- (void)resetAuth {
  [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) resetAuth];
}

- (jint)myUid {
  return [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) myUid];
}

- (AMI18nEngine *)getFormatter {
  return [((ImActorModelModulesModules *) nil_chk(modules_)) getI18nEngine];
}

- (AMMVVMCollection *)getUsers {
  return [((ImActorModelModulesUsers *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getUsersModule])) getUsersCollection];
}

- (AMMVVMCollection *)getGroups {
  return [((ImActorModelModulesGroups *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getGroupsModule])) getGroupsCollection];
}

- (id<ImActorModelDroidkitEngineListEngine>)getDialogs {
  return [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) getDialogsEngine];
}

- (id<ImActorModelDroidkitEngineListEngine>)getMessagesWithAMPeer:(AMPeer *)peer {
  return [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) getConversationEngineWithAMPeer:peer];
}

- (AMUserTypingVM *)getTyping:(jint)uid {
  return [((ImActorModelModulesTyping *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getTypingModule])) getTypingWithInt:uid];
}

- (AMGroupTypingVM *)getGroupTyping:(jint)gid {
  return [((ImActorModelModulesTyping *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getTypingModule])) getGroupTypingWithInt:gid];
}

- (void)onAppVisible {
  if ([((ImActorModelModulesModules *) nil_chk(modules_)) getPresenceModule] != nil) {
    [((ImActorModelModulesPresence *) nil_chk([modules_ getPresenceModule])) onAppVisible];
    [((ImActorModelModulesNotifications *) nil_chk([modules_ getNotifications])) onAppVisible];
  }
}

- (void)onAppHidden {
  if ([((ImActorModelModulesModules *) nil_chk(modules_)) getPresenceModule] != nil) {
    [((ImActorModelModulesPresence *) nil_chk([modules_ getPresenceModule])) onAppHidden];
    [((ImActorModelModulesNotifications *) nil_chk([modules_ getNotifications])) onAppHidden];
  }
}

- (void)onConversationOpen:(AMPeer *)peer {
  [((ImActorModelModulesPresence *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getPresenceModule])) subscribeWithAMPeer:peer];
  [((ImActorModelModulesNotifications *) nil_chk([modules_ getNotifications])) onConversationOpenWithAMPeer:peer];
  [((ImActorModelModulesMessages *) nil_chk([modules_ getMessagesModule])) onConversationOpenWithAMPeer:peer];
}

- (void)onConversationClosed:(AMPeer *)peer {
  [((ImActorModelModulesNotifications *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getNotifications])) onConversationCloseWithAMPeer:peer];
}

- (void)onDialogsOpen {
  [((ImActorModelModulesNotifications *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getNotifications])) onDialogsOpen];
}

- (void)onDialogsClosed {
  [((ImActorModelModulesNotifications *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getNotifications])) onDialogsClosed];
}

- (void)onProfileOpen:(jint)uid {
  [((ImActorModelModulesPresence *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getPresenceModule])) subscribeWithAMPeer:AMPeer_userWithInt_(uid)];
}

- (void)onProfileClosed:(jint)uid {
}

- (void)onInMessageShown:(AMPeer *)peer withRid:(jlong)rid withDate:(jlong)sortDate withEncrypted:(jboolean)isEncrypted {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) onInMessageShownWithAMPeer:peer withLong:rid withLong:sortDate withBoolean:isEncrypted];
}

- (void)onTyping:(AMPeer *)peer {
  [((ImActorModelModulesTyping *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getTypingModule])) onTypingWithAMPeer:peer];
}

- (void)onPhoneBookChanged {
  if ([((ImActorModelModulesModules *) nil_chk(modules_)) getContactsModule] != nil) {
    [((ImActorModelModulesContacts *) nil_chk([modules_ getContactsModule])) onPhoneBookChanged];
  }
}

- (jlong)loadLastReadDate:(AMPeer *)peer {
  return [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) loadReadStateWithAMPeer:peer];
}

- (void)saveDraft:(AMPeer *)peer withText:(NSString *)draft {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) saveDraftWithAMPeer:peer withNSString:draft];
}

- (NSString *)loadDraft:(AMPeer *)peer {
  return [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) loadDraftWithAMPeer:peer];
}

- (void)sendMessage:(AMPeer *)peer withText:(NSString *)text {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) sendMessageWithAMPeer:peer withNSString:text];
}

- (void)sendPhotoWithAMPeer:(AMPeer *)peer
               withNSString:(NSString *)fileName
                    withInt:(jint)w
                    withInt:(jint)h
            withAMFastThumb:(AMFastThumb *)fastThumb
withImActorModelFilesFileReference:(id<ImActorModelFilesFileReference>)fileReference {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) sendPhotoWithAMPeer:peer withNSString:fileName withInt:w withInt:h withAMFastThumb:fastThumb withImActorModelFilesFileReference:fileReference];
}

- (void)sendVideoWithAMPeer:(AMPeer *)peer
               withNSString:(NSString *)fileName
                    withInt:(jint)w
                    withInt:(jint)h
                    withInt:(jint)duration
            withAMFastThumb:(AMFastThumb *)fastThumb
withImActorModelFilesFileReference:(id<ImActorModelFilesFileReference>)fileReference {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) sendVideoWithAMPeer:peer withNSString:fileName withInt:w withInt:h withInt:duration withAMFastThumb:fastThumb withImActorModelFilesFileReference:fileReference];
}

- (void)sendDocumentWithAMPeer:(AMPeer *)peer
                  withNSString:(NSString *)fileName
                  withNSString:(NSString *)mimeType
withImActorModelFilesFileReference:(id<ImActorModelFilesFileReference>)fileReference {
  [self sendDocumentWithAMPeer:peer withNSString:fileName withNSString:mimeType withImActorModelFilesFileReference:fileReference withAMFastThumb:nil];
}

- (void)sendDocumentWithAMPeer:(AMPeer *)peer
                  withNSString:(NSString *)fileName
                  withNSString:(NSString *)mimeType
withImActorModelFilesFileReference:(id<ImActorModelFilesFileReference>)fileReference
               withAMFastThumb:(AMFastThumb *)fastThumb {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) sendDocumentWithAMPeer:peer withNSString:fileName withNSString:mimeType withAMFastThumb:fastThumb withImActorModelFilesFileReference:fileReference];
}

- (id<AMCommand>)editMyNameWithNSString:(NSString *)newName {
  return [((ImActorModelModulesUsers *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getUsersModule])) editMyNameWithNSString:newName];
}

- (id<AMCommand>)editNameWithInt:(jint)uid
                    withNSString:(NSString *)name {
  return [((ImActorModelModulesUsers *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getUsersModule])) editNameWithInt:uid withNSString:name];
}

- (id<AMCommand>)createGroupWithNSString:(NSString *)title
                            withIntArray:(IOSIntArray *)uids {
  return [((ImActorModelModulesGroups *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getGroupsModule])) createGroupWithNSString:title withIntArray:uids];
}

- (id<AMCommand>)editGroupTitleWithInt:(jint)gid
                          withNSString:(NSString *)title {
  return [((ImActorModelModulesGroups *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getGroupsModule])) editTitleWithInt:gid withNSString:title];
}

- (id<AMCommand>)leaveGroupWithInt:(jint)gid {
  return [((ImActorModelModulesGroups *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getGroupsModule])) leaveGroupWithInt:gid];
}

- (id<AMCommand>)addMemberToGroupWithInt:(jint)gid
                                 withInt:(jint)uid {
  return [((ImActorModelModulesGroups *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getGroupsModule])) addMemberToGroupWithInt:gid withInt:uid];
}

- (id<AMCommand>)kickMemberWithInt:(jint)gid
                           withInt:(jint)uid {
  return [((ImActorModelModulesGroups *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getGroupsModule])) kickMemberWithInt:gid withInt:uid];
}

- (id<AMCommand>)removeContactWithInt:(jint)uid {
  return [((ImActorModelModulesContacts *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getContactsModule])) removeContactWithInt:uid];
}

- (id<AMCommand>)addContactWithInt:(jint)uid {
  return [((ImActorModelModulesContacts *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getContactsModule])) addContactWithInt:uid];
}

- (id<AMCommand>)findUsersWithNSString:(NSString *)query {
  return [((ImActorModelModulesContacts *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getContactsModule])) findUsersWithNSString:query];
}

- (id<AMCommand>)deleteChatWithAMPeer:(AMPeer *)peer {
  return [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) deleteChatWithAMPeer:peer];
}

- (id<AMCommand>)clearChatWithAMPeer:(AMPeer *)peer {
  return [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) clearChatWithAMPeer:peer];
}

- (void)loadMoreDialogs {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) loadMoreDialogs];
}

- (void)loadMoreHistoryWithAMPeer:(AMPeer *)peer {
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getMessagesModule])) loadMoreHistoryWithAMPeer:peer];
}

- (AMFileVM *)bindFileWithAMFileReference:(AMFileReference *)fileReference
                              withBoolean:(jboolean)isAutoStart
                     withAMFileVMCallback:(id<AMFileVMCallback>)callback {
  return [[AMFileVM alloc] initWithAMFileReference:fileReference withBoolean:isAutoStart withImActorModelModulesModules:modules_ withAMFileVMCallback:callback];
}

- (AMUploadFileVM *)bindUploadWithLong:(jlong)rid
            withAMUploadFileVMCallback:(id<AMUploadFileVMCallback>)callback {
  return [[AMUploadFileVM alloc] initWithLong:rid withAMUploadFileVMCallback:callback withImActorModelModulesModules:modules_];
}

- (void)bindRawFileWithAMFileReference:(AMFileReference *)fileReference
                           withBoolean:(jboolean)isAutoStart
withImActorModelModulesFileDownloadCallback:(id<ImActorModelModulesFileDownloadCallback>)callback {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) bindFileWithAMFileReference:fileReference withBoolean:isAutoStart withImActorModelModulesFileDownloadCallback:callback];
}

- (void)unbindRawFileWithLong:(jlong)fileId
                  withBoolean:(jboolean)isAutoCancel
withImActorModelModulesFileDownloadCallback:(id<ImActorModelModulesFileDownloadCallback>)callback {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) unbindFileWithLong:fileId withImActorModelModulesFileDownloadCallback:callback withBoolean:isAutoCancel];
}

- (void)requestStateWithLong:(jlong)fileId
withImActorModelModulesFileDownloadCallback:(id<ImActorModelModulesFileDownloadCallback>)callback {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) requestStateWithLong:fileId withImActorModelModulesFileDownloadCallback:callback];
}

- (void)requestUploadStateWithLong:(jlong)rid
withImActorModelModulesFileUploadCallback:(id<ImActorModelModulesFileUploadCallback>)callback {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) requestUploadStateWithLong:rid withImActorModelModulesFileUploadCallback:callback];
}

- (void)cancelDownloadingWithLong:(jlong)fileId {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) cancelDownloadingWithLong:fileId];
}

- (void)startDownloadingWithAMFileReference:(AMFileReference *)location {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) startDownloadingWithAMFileReference:location];
}

- (void)resumeUploadWithLong:(jlong)rid {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) resumeUploadWithLong:rid];
}

- (void)pauseUploadWithLong:(jlong)rid {
  [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) pauseUploadWithLong:rid];
}

- (NSString *)getDownloadedDescriptorWithLong:(jlong)fileId {
  return [((ImActorModelModulesFiles *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getFilesModule])) getDownloadedDescriptorWithLong:fileId];
}

- (jboolean)isConversationTonesEnabled {
  return [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) isConversationTonesEnabled];
}

- (void)changeConversationTonesEnabledWithBoolean:(jboolean)val {
  [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) changeConversationTonesEnabledWithBoolean:val];
}

- (jboolean)isNotificationSoundEnabled {
  return [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) isNotificationSoundEnabled];
}

- (void)changeNotificationSoundEnabledWithBoolean:(jboolean)val {
  [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) changeNotificationSoundEnabledWithBoolean:val];
}

- (jboolean)isNotificationVibrationEnabled {
  return [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) isVibrationEnabled];
}

- (void)changeNotificationVibrationEnabledWithBoolean:(jboolean)val {
  [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) changeNotificationVibrationEnabledWithBoolean:val];
}

- (jboolean)isShowNotificationsText {
  return [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) isShowNotificationsText];
}

- (void)changeShowNotificationTextEnabledWithBoolean:(jboolean)val {
  [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) changeShowNotificationTextEnabledWithBoolean:val];
}

- (jboolean)isSendByEnterEnabled {
  return [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) isSendByEnterEnabled];
}

- (void)changeSendByEnterWithBoolean:(jboolean)val {
  [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) changeSendByEnterWithBoolean:val];
}

- (jboolean)isNotificationsEnabledWithAMPeer:(AMPeer *)peer {
  return [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) isNotificationsEnabledWithAMPeer:peer];
}

- (void)changeNotificationsEnabledWithAMPeer:(AMPeer *)peer
                                 withBoolean:(jboolean)val {
  [((ImActorModelModulesSettings *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getSettings])) changeNotificationsEnabledWithAMPeer:peer withBoolean:val];
}

- (AMOwnAvatarVM *)getOwnAvatarVM {
  return [((ImActorModelModulesProfile *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getProfile])) getOwnAvatarVM];
}

- (void)changeAvatarWithNSString:(NSString *)descriptor {
  [((ImActorModelModulesProfile *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getProfile])) changeAvatarWithNSString:descriptor];
}

- (void)removeAvatar {
  [((ImActorModelModulesProfile *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getProfile])) removeAvatar];
}

- (AMBindedDisplayList *)getDialogsGlobalList {
  return [((ImActorModelModulesDisplayLists *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getDisplayLists])) getDialogsGlobalList];
}

- (AMBindedDisplayList *)getContactsGlobalList {
  return [((ImActorModelModulesDisplayLists *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getDisplayLists])) getContactsGlobalList];
}

- (AMBindedDisplayList *)buildContactDisplayList {
  return [((ImActorModelModulesDisplayLists *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getDisplayLists])) buildNewContactListWithBoolean:NO];
}

- (void)copyAllFieldsTo:(AMMessenger *)other {
  [super copyAllFieldsTo:other];
  other->modules_ = modules_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMessenger)

@implementation AMMessenger_$1

- (void)onEnvelopeDeliveredWithDKEnvelope:(DKEnvelope *)envelope {
}

- (void)onEnvelopeProcessedWithDKEnvelope:(DKEnvelope *)envelope
                                 withLong:(jlong)duration {
  if (duration > 300) {
    AMLog_wWithNSString_withNSString_(@"ACTOR_SYSTEM", JreStrcat("$$$@C", @"Too long ", [((DKActorScope *) nil_chk([((DKEnvelope *) nil_chk(envelope)) getScope])) getPath], @" {", [envelope getMessage], '}'));
  }
}

- (void)onDropWithDKActorRef:(DKActorRef *)sender
                      withId:(id)message
                 withDKActor:(DKActor *)actor {
  AMLog_wWithNSString_withNSString_(@"ACTOR_SYSTEM", JreStrcat("$@", @"Drop: ", message));
}

- (void)onDeadLetterWithDKActorRef:(DKActorRef *)receiver
                            withId:(id)message {
  AMLog_wWithNSString_withNSString_(@"ACTOR_SYSTEM", JreStrcat("$@", @"Dead Letter: ", message));
}

- (void)onActorDieWithDKActorRef:(DKActorRef *)ref
           withJavaLangException:(JavaLangException *)e {
  AMLog_wWithNSString_withNSString_(@"ACTOR_SYSTEM", JreStrcat("$@", @"Die: ", e));
  [((JavaLangException *) nil_chk(e)) printStackTrace];
}

- (instancetype)init {
  return [super init];
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMessenger_$1)
