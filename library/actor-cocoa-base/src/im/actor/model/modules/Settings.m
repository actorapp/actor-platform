//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/Settings.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/AppCategory.h"
#include "im/actor/model/Configuration.h"
#include "im/actor/model/DeviceCategory.h"
#include "im/actor/model/droidkit/actors/ActorCreator.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/actors/ActorSystem.h"
#include "im/actor/model/droidkit/actors/Props.h"
#include "im/actor/model/droidkit/engine/PreferencesStorage.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/PeerType.h"
#include "im/actor/model/modules/BaseModule.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/Settings.h"
#include "im/actor/model/modules/settings/SettingsSyncActor.h"
#include "java/lang/RuntimeException.h"

@interface ImActorModelModulesSettings () {
 @public
  NSString *STORAGE_PREFIX_;
  NSString *KEY_NOTIFICATION_TONES_;
  NSString *KEY_CHAT_SEND_BY_ENTER_;
  NSString *KEY_NOTIFICATION_ENABLED_;
  NSString *KEY_NOTIFICATION_SOUND_;
  NSString *KEY_NOTIFICATION_SOUND_ENABLED_;
  NSString *KEY_NOTIFICATION_VIBRATION_;
  NSString *KEY_NOTIFICATION_IN_APP_ENABLED_;
  NSString *KEY_NOTIFICATION_IN_APP_SOUND_;
  NSString *KEY_NOTIFICATION_IN_APP_VIBRATION_;
  NSString *KEY_NOTIFICATION_TEXT_;
  NSString *KEY_NOTIFICATION_CHAT_PREFIX_;
  NSString *KEY_GROUP_INVITE_LINK_;
  DKActorRef *settingsSync_;
}

- (NSString *)getChatKeyWithAMPeer:(AMPeer *)peer;

- (jboolean)loadValueWithNSString:(NSString *)key
                      withBoolean:(jboolean)defaultVal;

- (void)changeValueWithNSString:(NSString *)key
                    withBoolean:(jboolean)val;

- (void)changeValueWithNSString:(NSString *)key
                   withNSString:(NSString *)val;

- (void)writeValueWithNSString:(NSString *)key
                  withNSString:(NSString *)val;

- (NSString *)readValueWithNSString:(NSString *)key;

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, STORAGE_PREFIX_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_TONES_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_CHAT_SEND_BY_ENTER_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_ENABLED_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_SOUND_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_SOUND_ENABLED_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_VIBRATION_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_IN_APP_ENABLED_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_IN_APP_SOUND_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_IN_APP_VIBRATION_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_TEXT_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_NOTIFICATION_CHAT_PREFIX_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, KEY_GROUP_INVITE_LINK_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettings, settingsSync_, DKActorRef *)

__attribute__((unused)) static NSString *ImActorModelModulesSettings_getChatKeyWithAMPeer_(ImActorModelModulesSettings *self, AMPeer *peer);

__attribute__((unused)) static jboolean ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(ImActorModelModulesSettings *self, NSString *key, jboolean defaultVal);

__attribute__((unused)) static void ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(ImActorModelModulesSettings *self, NSString *key, jboolean val);

__attribute__((unused)) static void ImActorModelModulesSettings_changeValueWithNSString_withNSString_(ImActorModelModulesSettings *self, NSString *key, NSString *val);

__attribute__((unused)) static void ImActorModelModulesSettings_writeValueWithNSString_withNSString_(ImActorModelModulesSettings *self, NSString *key, NSString *val);

__attribute__((unused)) static NSString *ImActorModelModulesSettings_readValueWithNSString_(ImActorModelModulesSettings *self, NSString *key);

@interface ImActorModelModulesSettings_$1 : NSObject < DKActorCreator > {
 @public
  ImActorModelModulesSettings *this$0_;
}

- (ImActorModelModulesSettingsSettingsSyncActor *)create;

- (instancetype)initWithImActorModelModulesSettings:(ImActorModelModulesSettings *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesSettings_$1)

J2OBJC_FIELD_SETTER(ImActorModelModulesSettings_$1, this$0_, ImActorModelModulesSettings *)

__attribute__((unused)) static void ImActorModelModulesSettings_$1_initWithImActorModelModulesSettings_(ImActorModelModulesSettings_$1 *self, ImActorModelModulesSettings *outer$);

__attribute__((unused)) static ImActorModelModulesSettings_$1 *new_ImActorModelModulesSettings_$1_initWithImActorModelModulesSettings_(ImActorModelModulesSettings *outer$) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesSettings_$1)

@implementation ImActorModelModulesSettings

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules {
  ImActorModelModulesSettings_initWithImActorModelModulesModules_(self, modules);
  return self;
}

- (void)run {
  settingsSync_ = [((DKActorSystem *) nil_chk(DKActorSystem_system())) actorOfWithDKProps:DKProps_createWithIOSClass_withDKActorCreator_(ImActorModelModulesSettingsSettingsSyncActor_class_(), new_ImActorModelModulesSettings_$1_initWithImActorModelModulesSettings_(self)) withNSString:@"actor/settings"];
}

- (void)onUpdatedSettingWithNSString:(NSString *)key
                        withNSString:(NSString *)value {
  ImActorModelModulesSettings_writeValueWithNSString_withNSString_(self, key, value);
}

- (jboolean)isConversationTonesEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_TONES_, YES);
}

- (void)changeConversationTonesEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_TONES_, val);
}

- (jboolean)isNotificationsEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_ENABLED_, YES);
}

- (void)changeNotificationsEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_ENABLED_, val);
}

- (jboolean)isNotificationSoundEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_SOUND_ENABLED_, YES);
}

- (void)changeNotificationSoundEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_SOUND_ENABLED_, val);
}

- (NSString *)getNotificationSound {
  return ImActorModelModulesSettings_readValueWithNSString_(self, KEY_NOTIFICATION_SOUND_);
}

- (void)changeNotificationSoundWithNSString:(NSString *)sound {
  ImActorModelModulesSettings_changeValueWithNSString_withNSString_(self, KEY_NOTIFICATION_SOUND_, sound);
}

- (jboolean)isVibrationEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_VIBRATION_, YES);
}

- (void)changeNotificationVibrationEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_VIBRATION_, val);
}

- (jboolean)isShowNotificationsText {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_TEXT_, YES);
}

- (void)changeShowNotificationTextEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_TEXT_, val);
}

- (jboolean)isInAppEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_IN_APP_ENABLED_, YES);
}

- (void)changeInAppEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_IN_APP_ENABLED_, val);
}

- (jboolean)isInAppSoundEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_IN_APP_SOUND_, YES);
}

- (void)changeInAppSoundEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_IN_APP_SOUND_, val);
}

- (jboolean)isInAppVibrationEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_IN_APP_VIBRATION_, YES);
}

- (void)changeInAppVibrationEnabledWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_NOTIFICATION_IN_APP_VIBRATION_, val);
}

- (jboolean)isSendByEnterEnabled {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, KEY_CHAT_SEND_BY_ENTER_, YES);
}

- (void)changeSendByEnterWithBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, KEY_CHAT_SEND_BY_ENTER_, val);
}

- (jboolean)isNotificationsEnabledWithAMPeer:(AMPeer *)peer {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, JreStrcat("$$$", KEY_NOTIFICATION_CHAT_PREFIX_, ImActorModelModulesSettings_getChatKeyWithAMPeer_(self, peer), @".enabled"), YES);
}

- (void)changeNotificationsEnabledWithAMPeer:(AMPeer *)peer
                                 withBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, JreStrcat("$$$", KEY_NOTIFICATION_CHAT_PREFIX_, ImActorModelModulesSettings_getChatKeyWithAMPeer_(self, peer), @".enabled"), val);
}

- (NSString *)getNotificationSoundWithAMPeer:(AMPeer *)peer {
  return ImActorModelModulesSettings_readValueWithNSString_(self, JreStrcat("$$$", KEY_NOTIFICATION_CHAT_PREFIX_, ImActorModelModulesSettings_getChatKeyWithAMPeer_(self, peer), @".sound"));
}

- (void)changeNotificationSoundWithAMPeer:(AMPeer *)peer
                             withNSString:(NSString *)sound {
  ImActorModelModulesSettings_changeValueWithNSString_withNSString_(self, JreStrcat("$$$", KEY_NOTIFICATION_CHAT_PREFIX_, ImActorModelModulesSettings_getChatKeyWithAMPeer_(self, peer), @".sound"), sound);
}

- (NSString *)getChatKeyWithAMPeer:(AMPeer *)peer {
  return ImActorModelModulesSettings_getChatKeyWithAMPeer_(self, peer);
}

- (void)changeGroupInviteLinkWithAMPeer:(AMPeer *)peer
                           withNSString:(NSString *)url {
  ImActorModelModulesSettings_changeValueWithNSString_withNSString_(self, JreStrcat("$$", KEY_GROUP_INVITE_LINK_, ImActorModelModulesSettings_getChatKeyWithAMPeer_(self, peer)), url);
}

- (NSString *)getGroupInviteLinkWithAMPeer:(AMPeer *)peer {
  return ImActorModelModulesSettings_readValueWithNSString_(self, JreStrcat("$$", KEY_GROUP_INVITE_LINK_, ImActorModelModulesSettings_getChatKeyWithAMPeer_(self, peer)));
}

- (jboolean)loadValueWithNSString:(NSString *)key
                      withBoolean:(jboolean)defaultVal {
  return ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(self, key, defaultVal);
}

- (void)changeValueWithNSString:(NSString *)key
                    withBoolean:(jboolean)val {
  ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(self, key, val);
}

- (void)changeValueWithNSString:(NSString *)key
                   withNSString:(NSString *)val {
  ImActorModelModulesSettings_changeValueWithNSString_withNSString_(self, key, val);
}

- (void)writeValueWithNSString:(NSString *)key
                  withNSString:(NSString *)val {
  ImActorModelModulesSettings_writeValueWithNSString_withNSString_(self, key, val);
}

- (NSString *)readValueWithNSString:(NSString *)key {
  return ImActorModelModulesSettings_readValueWithNSString_(self, key);
}

@end

void ImActorModelModulesSettings_initWithImActorModelModulesModules_(ImActorModelModulesSettings *self, ImActorModelModulesModules *modules) {
  (void) ImActorModelModulesBaseModule_initWithImActorModelModulesModules_(self, modules);
  self->STORAGE_PREFIX_ = @"app.tones_enabled";
  NSString *configKey;
  switch ([[((AMConfiguration *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules)) getConfiguration])) getAppCategory] ordinal]) {
    case AMAppCategory_ANDROID:
    configKey = @"android";
    break;
    case AMAppCategory_IOS:
    configKey = @"ios";
    break;
    case AMAppCategory_WEB:
    configKey = @"web";
    break;
    default:
    case AMAppCategory_GENERIC:
    configKey = @"generic";
    break;
  }
  NSString *deviceTypeKey;
  switch ([[((AMConfiguration *) nil_chk([modules getConfiguration])) getDeviceCategory] ordinal]) {
    case AMDeviceCategory_DESKTOP:
    deviceTypeKey = @"desktop";
    break;
    case AMDeviceCategory_MOBILE:
    deviceTypeKey = @"mobile";
    break;
    default:
    case AMDeviceCategory_UNKNOWN:
    deviceTypeKey = @"generic";
    break;
  }
  self->KEY_NOTIFICATION_TONES_ = JreStrcat("$$$", @"app.", configKey, @".tones_enabled");
  self->KEY_CHAT_SEND_BY_ENTER_ = JreStrcat("$$$", @"app.", configKey, @".send_by_enter");
  self->KEY_NOTIFICATION_SOUND_ = @"account.notification.sound";
  self->KEY_NOTIFICATION_ENABLED_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".notification.enabled");
  self->KEY_NOTIFICATION_SOUND_ENABLED_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".notification.sound.enabled");
  self->KEY_NOTIFICATION_VIBRATION_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".notification.vibration.enabled");
  self->KEY_NOTIFICATION_TEXT_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".notification.show_text");
  self->KEY_NOTIFICATION_CHAT_PREFIX_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".notification.chat.");
  self->KEY_NOTIFICATION_IN_APP_ENABLED_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".in_app.enabled");
  self->KEY_NOTIFICATION_IN_APP_SOUND_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".in_app.sound.enabled");
  self->KEY_NOTIFICATION_IN_APP_VIBRATION_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".in_app.vibration.enabled");
  self->KEY_GROUP_INVITE_LINK_ = JreStrcat("$$$", @"category.", deviceTypeKey, @".group.inviteurl");
}

ImActorModelModulesSettings *new_ImActorModelModulesSettings_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) {
  ImActorModelModulesSettings *self = [ImActorModelModulesSettings alloc];
  ImActorModelModulesSettings_initWithImActorModelModulesModules_(self, modules);
  return self;
}

NSString *ImActorModelModulesSettings_getChatKeyWithAMPeer_(ImActorModelModulesSettings *self, AMPeer *peer) {
  if ([((AMPeer *) nil_chk(peer)) getPeerType] == AMPeerTypeEnum_get_PRIVATE()) {
    return JreStrcat("$I", @"PRIVATE_", [peer getPeerId]);
  }
  else if ([peer getPeerType] == AMPeerTypeEnum_get_GROUP()) {
    return JreStrcat("$I", @"GROUP_", [peer getPeerId]);
  }
  else {
    @throw new_JavaLangRuntimeException_initWithNSString_(@"Unsupported peer");
  }
}

jboolean ImActorModelModulesSettings_loadValueWithNSString_withBoolean_(ImActorModelModulesSettings *self, NSString *key, jboolean defaultVal) {
  NSString *sValue = ImActorModelModulesSettings_readValueWithNSString_(self, key);
  if ([@"true" isEqual:sValue]) {
    return YES;
  }
  else if ([@"false" isEqual:sValue]) {
    return NO;
  }
  else {
    return defaultVal;
  }
}

void ImActorModelModulesSettings_changeValueWithNSString_withBoolean_(ImActorModelModulesSettings *self, NSString *key, jboolean val) {
  NSString *sVal = val ? @"true" : @"false";
  ImActorModelModulesSettings_changeValueWithNSString_withNSString_(self, key, sVal);
}

void ImActorModelModulesSettings_changeValueWithNSString_withNSString_(ImActorModelModulesSettings *self, NSString *key, NSString *val) {
  ImActorModelModulesSettings_writeValueWithNSString_withNSString_(self, key, val);
  [((DKActorRef *) nil_chk(self->settingsSync_)) sendWithId:new_ImActorModelModulesSettingsSettingsSyncActor_ChangeSettings_initWithNSString_withNSString_(key, val)];
}

void ImActorModelModulesSettings_writeValueWithNSString_withNSString_(ImActorModelModulesSettings *self, NSString *key, NSString *val) {
  [((id<DKPreferencesStorage>) nil_chk([self preferences])) putStringWithKey:JreStrcat("$$", self->STORAGE_PREFIX_, key) withValue:val];
}

NSString *ImActorModelModulesSettings_readValueWithNSString_(ImActorModelModulesSettings *self, NSString *key) {
  return [((id<DKPreferencesStorage>) nil_chk([self preferences])) getStringWithKey:JreStrcat("$$", self->STORAGE_PREFIX_, key)];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesSettings)

@implementation ImActorModelModulesSettings_$1

- (ImActorModelModulesSettingsSettingsSyncActor *)create {
  return new_ImActorModelModulesSettingsSettingsSyncActor_initWithImActorModelModulesModules_([this$0_ modules]);
}

- (instancetype)initWithImActorModelModulesSettings:(ImActorModelModulesSettings *)outer$ {
  ImActorModelModulesSettings_$1_initWithImActorModelModulesSettings_(self, outer$);
  return self;
}

@end

void ImActorModelModulesSettings_$1_initWithImActorModelModulesSettings_(ImActorModelModulesSettings_$1 *self, ImActorModelModulesSettings *outer$) {
  self->this$0_ = outer$;
  (void) NSObject_init(self);
}

ImActorModelModulesSettings_$1 *new_ImActorModelModulesSettings_$1_initWithImActorModelModulesSettings_(ImActorModelModulesSettings *outer$) {
  ImActorModelModulesSettings_$1 *self = [ImActorModelModulesSettings_$1 alloc];
  ImActorModelModulesSettings_$1_initWithImActorModelModulesSettings_(self, outer$);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesSettings_$1)
