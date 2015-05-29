//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/i18n/I18nEngine.java
//


#include "IOSObjectArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/LocaleProvider.h"
#include "im/actor/model/droidkit/actors/Environment.h"
#include "im/actor/model/droidkit/engine/KeyValueEngine.h"
#include "im/actor/model/entity/Avatar.h"
#include "im/actor/model/entity/ContentDescription.h"
#include "im/actor/model/entity/ContentType.h"
#include "im/actor/model/entity/Dialog.h"
#include "im/actor/model/entity/Message.h"
#include "im/actor/model/entity/Notification.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/PeerType.h"
#include "im/actor/model/entity/Sex.h"
#include "im/actor/model/entity/User.h"
#include "im/actor/model/entity/content/AbsContent.h"
#include "im/actor/model/entity/content/ServiceContent.h"
#include "im/actor/model/entity/content/ServiceGroupAvatarChanged.h"
#include "im/actor/model/entity/content/ServiceGroupCreated.h"
#include "im/actor/model/entity/content/ServiceGroupTitleChanged.h"
#include "im/actor/model/entity/content/ServiceGroupUserAdded.h"
#include "im/actor/model/entity/content/ServiceGroupUserKicked.h"
#include "im/actor/model/entity/content/ServiceGroupUserLeave.h"
#include "im/actor/model/entity/content/ServiceUserRegistered.h"
#include "im/actor/model/entity/content/TextContent.h"
#include "im/actor/model/i18n/I18nEngine.h"
#include "im/actor/model/log/Log.h"
#include "im/actor/model/modules/Auth.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/Users.h"
#include "im/actor/model/viewmodel/UserPresence.h"
#include "java/lang/Character.h"
#include "java/util/Arrays.h"
#include "java/util/Comparator.h"
#include "java/util/Date.h"
#include "java/util/HashMap.h"

@interface AMI18nEngine () {
 @public
  ImActorModelModulesModules *modules_;
  JavaUtilHashMap *locale_;
  jboolean is24Hours_;
  IOSObjectArray *MONTHS_SHORT_;
  IOSObjectArray *MONTHS_;
}

- (NSString *)formatTwoDigitWithInt:(jint)v;

+ (jboolean)areSameDaysWithLong:(jlong)a
                       withLong:(jlong)b;

- (NSString *)getTemplateNamedWithInt:(jint)senderId
                         withNSString:(NSString *)baseString;

- (NSString *)getTemplateWithInt:(jint)senderId
                    withNSString:(NSString *)baseString;

- (AMUser *)getUserWithInt:(jint)uid;

@end

J2OBJC_FIELD_SETTER(AMI18nEngine, modules_, ImActorModelModulesModules *)
J2OBJC_FIELD_SETTER(AMI18nEngine, locale_, JavaUtilHashMap *)
J2OBJC_FIELD_SETTER(AMI18nEngine, MONTHS_SHORT_, IOSObjectArray *)
J2OBJC_FIELD_SETTER(AMI18nEngine, MONTHS_, IOSObjectArray *)

__attribute__((unused)) static NSString *AMI18nEngine_formatTwoDigitWithInt_(AMI18nEngine *self, jint v);

__attribute__((unused)) static jboolean AMI18nEngine_areSameDaysWithLong_withLong_(jlong a, jlong b);

__attribute__((unused)) static NSString *AMI18nEngine_getTemplateNamedWithInt_withNSString_(AMI18nEngine *self, jint senderId, NSString *baseString);

__attribute__((unused)) static NSString *AMI18nEngine_getTemplateWithInt_withNSString_(AMI18nEngine *self, jint senderId, NSString *baseString);

__attribute__((unused)) static AMUser *AMI18nEngine_getUserWithInt_(AMI18nEngine *self, jint uid);

@interface AMI18nEngine_$1 : NSObject < JavaUtilComparator >

- (jint)compareWithLong:(jlong)lhs
               withLong:(jlong)rhs;

- (jint)compareWithId:(AMMessage *)lhs
               withId:(AMMessage *)rhs;

- (instancetype)init;

@end

J2OBJC_EMPTY_STATIC_INIT(AMI18nEngine_$1)

__attribute__((unused)) static void AMI18nEngine_$1_init(AMI18nEngine_$1 *self);

__attribute__((unused)) static AMI18nEngine_$1 *new_AMI18nEngine_$1_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMI18nEngine_$1)

@implementation AMI18nEngine

- (instancetype)initWithProvider:(id<AMLocaleProvider>)provider
                     withModules:(ImActorModelModulesModules *)modules {
  AMI18nEngine_initWithProvider_withModules_(self, provider, modules);
  return self;
}

- (NSString *)formatTwoDigitWithInt:(jint)v {
  return AMI18nEngine_formatTwoDigitWithInt_(self, v);
}

+ (jboolean)areSameDaysWithLong:(jlong)a
                       withLong:(jlong)b {
  return AMI18nEngine_areSameDaysWithLong_withLong_(a, b);
}

- (NSString *)formatShortDate:(jlong)date {
  jlong delta = [new_JavaUtilDate_init() getTime] - date;
  if (delta < 60 * 1000) {
    return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"TimeShortNow"];
  }
  else if (delta < 60 * 60 * 1000) {
    return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"TimeShortMinutes"])) replace:@"{minutes}" withSequence:JreStrcat("J", delta / 60000)];
  }
  else if (delta < 24 * 60 * 60 * 1000) {
    return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"TimeShortHours"])) replace:@"{hours}" withSequence:JreStrcat("J", delta / 3600000)];
  }
  else if (delta < 2 * 24 * 60 * 60 * 1000) {
    return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"TimeShortYesterday"])) replace:@"{hours}" withSequence:JreStrcat("J", delta / 3600000)];
  }
  else {
    JavaUtilDate *date1 = new_JavaUtilDate_initWithLong_(date);
    jint month = [date1 getMonth];
    jint d = [date1 getDate];
    return JreStrcat("IC$", d, ' ', [((NSString *) nil_chk(IOSObjectArray_Get(nil_chk(MONTHS_SHORT_), month))) uppercaseString]);
  }
}

- (NSString *)formatTyping {
  return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"Typing"];
}

- (NSString *)formatTypingWithName:(NSString *)name {
  return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"TypingUser"])) replace:@"{user}" withSequence:name];
}

- (NSString *)formatTypingWithCount:(jint)count {
  return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"TypingMultiple"])) replace:@"{count}" withSequence:JreStrcat("I", count)];
}

- (NSString *)formatFileSize:(jint)bytes {
  if (bytes < 0) {
    bytes = 0;
  }
  if (bytes < 1024) {
    return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"FileB"])) replace:@"{bytes}" withSequence:JreStrcat("I", bytes)];
  }
  else if (bytes < 1024 * 1024) {
    return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"FileKb"])) replace:@"{kbytes}" withSequence:JreStrcat("I", (bytes / 1024))];
  }
  else if (bytes < 1024 * 1024 * 1024) {
    return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"FileMb"])) replace:@"{mbytes}" withSequence:JreStrcat("I", (bytes / (1024 * 1024)))];
  }
  else {
    return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"FileGb"])) replace:@"{gbytes}" withSequence:JreStrcat("I", (bytes / (1024 * 1024 * 1024)))];
  }
}

- (NSString *)formatTime:(jlong)date {
  JavaUtilDate *dateVal = new_JavaUtilDate_initWithLong_(date);
  if (is24Hours_) {
    return JreStrcat("IC$", [dateVal getHours], ':', AMI18nEngine_formatTwoDigitWithInt_(self, [dateVal getMinutes]));
  }
  else {
    jint hours = [dateVal getHours];
    if (hours > 12) {
      return JreStrcat("IC$$", (hours - 12), ':', AMI18nEngine_formatTwoDigitWithInt_(self, [dateVal getMinutes]), @" PM");
    }
    else {
      return JreStrcat("IC$$", hours, ':', AMI18nEngine_formatTwoDigitWithInt_(self, [dateVal getMinutes]), @" AM");
    }
  }
}

- (NSString *)formatDate:(jlong)date {
  JavaUtilDate *dateVal = new_JavaUtilDate_initWithLong_(date);
  return JreStrcat("ICIC$", [dateVal getDate], '/', ([dateVal getMonth] + 1), '/', AMI18nEngine_formatTwoDigitWithInt_(self, [dateVal getYear]));
}

- (NSString *)formatPresence:(AMUserPresence *)value
                     withSex:(AMSexEnum *)sex {
  if (value == nil) {
    return nil;
  }
  if ([((AMUserPresence *) nil_chk(value)) getState] == AMUserPresence_StateEnum_get_OFFLINE()) {
    jlong currentTime = DKEnvironment_getCurrentSyncedTime() / 1000LL;
    jint delta = (jint) (currentTime - [value getLastSeen]);
    if (delta < 60) {
      if ([((JavaUtilHashMap *) nil_chk(locale_)) containsKeyWithId:@"OnlineNowMale"] && [locale_ containsKeyWithId:@"OnlineNowFemale"]) {
        return sex == AMSexEnum_get_UNKNOWN() ? [locale_ getWithId:@"OnlineNow"] : sex == AMSexEnum_get_MALE() ? [locale_ getWithId:@"OnlineNowMale"] : [locale_ getWithId:@"OnlineNowFemale"];
      }
      else {
        return [locale_ getWithId:@"OnlineNow"];
      }
    }
    else if (delta < 24 * 60 * 60) {
      NSString *time = [self formatTime:[value getLastSeen] * 1000LL];
      if (AMI18nEngine_areSameDaysWithLong_withLong_([value getLastSeen] * 1000LL, [new_JavaUtilDate_init() getTime])) {
        if ([((JavaUtilHashMap *) nil_chk(locale_)) containsKeyWithId:@"OnlineLastSeenTodayMale"] && [locale_ containsKeyWithId:@"OnlineLastSeenTodayMale"]) {
          return [(sex == AMSexEnum_get_UNKNOWN() ? [locale_ getWithId:@"OnlineLastSeenToday"] : sex == AMSexEnum_get_MALE() ? [locale_ getWithId:@"OnlineLastSeenTodayMale"] : [locale_ getWithId:@"OnlineLastSeenTodayFemale"]) replace:@"{time}" withSequence:time];
        }
        else {
          return [((NSString *) nil_chk([locale_ getWithId:@"OnlineLastSeenToday"])) replace:@"{time}" withSequence:time];
        }
      }
      else {
        if ([((JavaUtilHashMap *) nil_chk(locale_)) containsKeyWithId:@"OnlineLastSeenYesterdayMale"] && [locale_ containsKeyWithId:@"OnlineLastSeenYesterdayMale"]) {
          return [(sex == AMSexEnum_get_UNKNOWN() ? [locale_ getWithId:@"OnlineLastSeenYesterday"] : sex == AMSexEnum_get_MALE() ? [locale_ getWithId:@"OnlineLastSeenYesterdayMale"] : [locale_ getWithId:@"OnlineLastSeenYesterdayFemale"]) replace:@"{time}" withSequence:time];
        }
        else {
          return [((NSString *) nil_chk([locale_ getWithId:@"OnlineLastSeenYesterday"])) replace:@"{time}" withSequence:time];
        }
      }
    }
    else if (delta < 14 * 24 * 60 * 60) {
      NSString *time = [self formatTime:[value getLastSeen] * 1000LL];
      NSString *date = [self formatDate:[value getLastSeen] * 1000LL];
      if ([((JavaUtilHashMap *) nil_chk(locale_)) containsKeyWithId:@"OnlineLastSeenDateTimeMale"] && [locale_ containsKeyWithId:@"OnlineLastSeenDateTimeMale"]) {
        return [((NSString *) nil_chk([(sex == AMSexEnum_get_UNKNOWN() ? [locale_ getWithId:@"OnlineLastSeenDateTime"] : sex == AMSexEnum_get_MALE() ? [locale_ getWithId:@"OnlineLastSeenDateTimeMale"] : [locale_ getWithId:@"OnlineLastSeenDateTimeFemale"]) replace:@"{time}" withSequence:time])) replace:@"{date}" withSequence:date];
      }
      else {
        return [((NSString *) nil_chk([((NSString *) nil_chk([locale_ getWithId:@"OnlineLastSeenDateTime"])) replace:@"{time}" withSequence:time])) replace:@"{date}" withSequence:date];
      }
    }
    else if (delta < 6 * 30 * 24 * 60 * 60) {
      NSString *date = [self formatDate:[value getLastSeen] * 1000LL];
      if ([((JavaUtilHashMap *) nil_chk(locale_)) containsKeyWithId:@"OnlineLastSeenDateMale"] && [locale_ containsKeyWithId:@"OnlineLastSeenDateMale"]) {
        return [(sex == AMSexEnum_get_UNKNOWN() ? [locale_ getWithId:@"OnlineLastSeenDate"] : sex == AMSexEnum_get_MALE() ? [locale_ getWithId:@"OnlineLastSeenDateMale"] : [locale_ getWithId:@"OnlineLastSeenDateFemale"]) replace:@"{date}" withSequence:date];
      }
      else {
        return [((NSString *) nil_chk([locale_ getWithId:@"OnlineLastSeenDate"])) replace:@"{date}" withSequence:date];
      }
    }
    else {
      return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"OnlineOff"];
    }
  }
  else if ([value getState] == AMUserPresence_StateEnum_get_ONLINE()) {
    return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"OnlineOn"];
  }
  return nil;
}

- (NSString *)formatDuration:(jint)duration {
  if (duration < 60) {
    return JreStrcat("$C$", AMI18nEngine_formatTwoDigitWithInt_(self, 0), ':', AMI18nEngine_formatTwoDigitWithInt_(self, duration));
  }
  else if (duration < 60 * 60) {
    return JreStrcat("$C$", AMI18nEngine_formatTwoDigitWithInt_(self, duration / 60), ':', AMI18nEngine_formatTwoDigitWithInt_(self, duration % 60));
  }
  else {
    return JreStrcat("$C$C$", AMI18nEngine_formatTwoDigitWithInt_(self, duration / 3600), ':', AMI18nEngine_formatTwoDigitWithInt_(self, duration / 60), ':', AMI18nEngine_formatTwoDigitWithInt_(self, duration % 60));
  }
}

- (NSString *)formatGroupMembers:(jint)count {
  return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"GroupMembers"])) replace:@"{count}" withSequence:JreStrcat("I", count)];
}

- (NSString *)formatGroupOnline:(jint)count {
  return [((NSString *) nil_chk([((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"GroupOnline"])) replace:@"{count}" withSequence:JreStrcat("I", count)];
}

- (NSString *)formatDialogText:(AMDialog *)dialog {
  if ([((AMDialog *) nil_chk(dialog)) getSenderId] == 0) {
    return @"";
  }
  else {
    NSString *contentText = [self formatContentTextWithSenderId:[dialog getSenderId] withContentType:[dialog getMessageType] withText:[dialog getText] withRelatedUid:[dialog getRelatedUid]];
    if ([((AMPeer *) nil_chk([dialog getPeer])) getPeerType] == AMPeerTypeEnum_get_GROUP()) {
      if ([self isLargeDialogMessage:[dialog getMessageType]]) {
        return JreStrcat("$$$", [self formatPerformerNameWithUid:[dialog getSenderId]], @": ", contentText);
      }
      else {
        return contentText;
      }
    }
    else {
      return contentText;
    }
  }
}

- (NSString *)formatNotificationText:(AMNotification *)pendingNotification {
  return [self formatContentTextWithSenderId:[((AMNotification *) nil_chk(pendingNotification)) getSender] withContentType:[((AMContentDescription *) nil_chk([pendingNotification getContentDescription])) getContentType] withText:[((AMContentDescription *) nil_chk([pendingNotification getContentDescription])) getText] withRelatedUid:[((AMContentDescription *) nil_chk([pendingNotification getContentDescription])) getRelatedUser]];
}

- (NSString *)formatContentTextWithSenderId:(jint)senderId
                            withContentType:(AMContentTypeEnum *)contentType
                                   withText:(NSString *)text
                             withRelatedUid:(jint)relatedUid {
  switch ([contentType ordinal]) {
    case AMContentType_TEXT:
    return text;
    case AMContentType_DOCUMENT:
    if (text == nil || ((jint) [text length]) == 0) {
      return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"ContentDocument"];
    }
    return text;
    case AMContentType_DOCUMENT_PHOTO:
    return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"ContentPhoto"];
    case AMContentType_DOCUMENT_VIDEO:
    return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"ContentVideo"];
    case AMContentType_SERVICE:
    return text;
    case AMContentType_SERVICE_REGISTERED:
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceRegistered");
    case AMContentType_SERVICE_CREATED:
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupCreated");
    case AMContentType_SERVICE_ADD:
    return [((NSString *) nil_chk(AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupAdded"))) replace:@"{name_added}" withSequence:[self getSubjectNameWithUid:relatedUid]];
    case AMContentType_SERVICE_LEAVE:
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupLeaved");
    case AMContentType_SERVICE_KICK:
    return [((NSString *) nil_chk(AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupKicked"))) replace:@"{name_kicked}" withSequence:[self getSubjectNameWithUid:relatedUid]];
    case AMContentType_SERVICE_AVATAR:
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupAvatarChanged");
    case AMContentType_SERVICE_AVATAR_REMOVED:
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupAvatarRemoved");
    case AMContentType_SERVICE_TITLE:
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupTitle");
    case AMContentType_EMPTY:
    return @"";
    default:
    case AMContentType_UNKNOWN_CONTENT:
    return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"ContentUnsupported"];
  }
}

- (jboolean)isLargeDialogMessage:(AMContentTypeEnum *)contentType {
  switch ([contentType ordinal]) {
    case AMContentType_SERVICE:
    case AMContentType_SERVICE_AVATAR:
    case AMContentType_SERVICE_AVATAR_REMOVED:
    case AMContentType_SERVICE_CREATED:
    case AMContentType_SERVICE_TITLE:
    case AMContentType_SERVICE_LEAVE:
    case AMContentType_SERVICE_REGISTERED:
    case AMContentType_SERVICE_KICK:
    case AMContentType_SERVICE_ADD:
    return YES;
    default:
    return NO;
  }
}

- (NSString *)formatFullServiceMessageWithSenderId:(jint)senderId
                                       withContent:(AMServiceContent *)content {
  if ([content isKindOfClass:[AMServiceUserRegistered class]]) {
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceRegisteredFull");
  }
  else if ([content isKindOfClass:[AMServiceGroupCreated class]]) {
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupCreatedFull");
  }
  else if ([content isKindOfClass:[AMServiceGroupUserAdded class]]) {
    return [((NSString *) nil_chk(AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupAdded"))) replace:@"{name_added}" withSequence:[self getSubjectNameWithUid:[((AMServiceGroupUserAdded *) nil_chk(((AMServiceGroupUserAdded *) check_class_cast(content, [AMServiceGroupUserAdded class])))) getAddedUid]]];
  }
  else if ([content isKindOfClass:[AMServiceGroupUserKicked class]]) {
    return [((NSString *) nil_chk(AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupKicked"))) replace:@"{name_kicked}" withSequence:[self getSubjectNameWithUid:[((AMServiceGroupUserKicked *) nil_chk(((AMServiceGroupUserKicked *) check_class_cast(content, [AMServiceGroupUserKicked class])))) getKickedUid]]];
  }
  else if ([content isKindOfClass:[AMServiceGroupUserLeave class]]) {
    return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupLeaved");
  }
  else if ([content isKindOfClass:[AMServiceGroupTitleChanged class]]) {
    return [((NSString *) nil_chk(AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupTitleFull"))) replace:@"{title}" withSequence:[((AMServiceGroupTitleChanged *) nil_chk(((AMServiceGroupTitleChanged *) check_class_cast(content, [AMServiceGroupTitleChanged class])))) getNewTitle]];
  }
  else if ([content isKindOfClass:[AMServiceGroupAvatarChanged class]]) {
    if ([((AMServiceGroupAvatarChanged *) nil_chk(((AMServiceGroupAvatarChanged *) check_class_cast(content, [AMServiceGroupAvatarChanged class])))) getNewAvatar] != nil) {
      return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupAvatarChanged");
    }
    else {
      return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, @"ServiceGroupAvatarRemoved");
    }
  }
  AMLog_wWithNSString_withNSString_(@"i18NEngine", JreStrcat("$@", @"Unknown service content: ", content));
  return [((AMServiceContent *) nil_chk(content)) getCompatText];
}

- (NSString *)formatPerformerNameWithUid:(jint)uid {
  if (uid == [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) myUid]) {
    return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"You"];
  }
  else {
    return [((AMUser *) nil_chk(AMI18nEngine_getUserWithInt_(self, uid))) getName];
  }
}

- (NSString *)getSubjectNameWithUid:(jint)uid {
  if (uid == [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getAuthModule])) myUid]) {
    return [((JavaUtilHashMap *) nil_chk(locale_)) getWithId:@"Thee"];
  }
  else {
    return [((AMUser *) nil_chk(AMI18nEngine_getUserWithInt_(self, uid))) getName];
  }
}

- (NSString *)formatMessagesExport:(IOSObjectArray *)messages {
  NSString *text = @"";
  JavaUtilArrays_sortWithNSObjectArray_withJavaUtilComparator_(messages, new_AMI18nEngine_$1_init());
  if (((IOSObjectArray *) nil_chk(messages))->size_ == 1) {
    {
      IOSObjectArray *a__ = messages;
      AMMessage * const *b__ = a__->buffer_;
      AMMessage * const *e__ = b__ + a__->size_;
      while (b__ < e__) {
        AMMessage *model = *b__++;
        if (!([[((AMMessage *) nil_chk(model)) getContent] isKindOfClass:[AMTextContent class]])) {
          continue;
        }
        text = JreStrcat("$$", text, [((AMTextContent *) nil_chk(((AMTextContent *) check_class_cast([model getContent], [AMTextContent class])))) getText]);
      }
    }
  }
  else {
    {
      IOSObjectArray *a__ = messages;
      AMMessage * const *b__ = a__->buffer_;
      AMMessage * const *e__ = b__ + a__->size_;
      while (b__ < e__) {
        AMMessage *model = *b__++;
        if (!([[((AMMessage *) nil_chk(model)) getContent] isKindOfClass:[AMTextContent class]])) {
          continue;
        }
        if (((jint) [text length]) > 0) {
          text = JreStrcat("$C", text, 0x000a);
        }
        text = JreStrcat("$$", text, JreStrcat("$$", [((AMUser *) nil_chk([((id<DKKeyValueEngine>) nil_chk([((ImActorModelModulesUsers *) nil_chk([((ImActorModelModulesModules *) nil_chk(modules_)) getUsersModule])) getUsers])) getValueWithKey:[model getSenderId]])) getName], @": "));
        text = JreStrcat("$$", text, [((AMTextContent *) nil_chk(((AMTextContent *) check_class_cast([model getContent], [AMTextContent class])))) getText]);
      }
    }
  }
  return text;
}

- (NSString *)formatFastName:(NSString *)name {
  if (((jint) [((NSString *) nil_chk(name)) length]) > 1) {
    if (JavaLangCharacter_isLetterWithChar_([name charAtWithInt:0])) {
      return [((NSString *) nil_chk([name substring:0 endIndex:1])) uppercaseString];
    }
    else {
      return @"#";
    }
  }
  else {
    return @"#";
  }
}

- (NSString *)getTemplateNamedWithInt:(jint)senderId
                         withNSString:(NSString *)baseString {
  return AMI18nEngine_getTemplateNamedWithInt_withNSString_(self, senderId, baseString);
}

- (NSString *)getTemplateWithInt:(jint)senderId
                    withNSString:(NSString *)baseString {
  return AMI18nEngine_getTemplateWithInt_withNSString_(self, senderId, baseString);
}

- (AMUser *)getUserWithInt:(jint)uid {
  return AMI18nEngine_getUserWithInt_(self, uid);
}

@end

void AMI18nEngine_initWithProvider_withModules_(AMI18nEngine *self, id<AMLocaleProvider> provider, ImActorModelModulesModules *modules) {
  (void) NSObject_init(self);
  self->modules_ = modules;
  self->locale_ = [((id<AMLocaleProvider>) nil_chk(provider)) loadLocale];
  self->is24Hours_ = [provider is24Hours];
  self->MONTHS_SHORT_ = [IOSObjectArray newArrayWithObjects:(id[]){ [((JavaUtilHashMap *) nil_chk(self->locale_)) getWithId:@"JanShort"], [self->locale_ getWithId:@"FebShort"], [self->locale_ getWithId:@"MarShort"], [self->locale_ getWithId:@"AprShort"], [self->locale_ getWithId:@"MayShort"], [self->locale_ getWithId:@"JunShort"], [self->locale_ getWithId:@"JulShort"], [self->locale_ getWithId:@"AugShort"], [self->locale_ getWithId:@"SepShort"], [self->locale_ getWithId:@"OctShort"], [self->locale_ getWithId:@"NovShort"], [self->locale_ getWithId:@"DecShort"] } count:12 type:NSString_class_()];
  self->MONTHS_ = [IOSObjectArray newArrayWithObjects:(id[]){ [self->locale_ getWithId:@"JanFull"], [self->locale_ getWithId:@"FebFull"], [self->locale_ getWithId:@"MarFull"], [self->locale_ getWithId:@"AprFull"], [self->locale_ getWithId:@"MayFull"], [self->locale_ getWithId:@"JunFull"], [self->locale_ getWithId:@"JulFull"], [self->locale_ getWithId:@"AugFull"], [self->locale_ getWithId:@"SepFull"], [self->locale_ getWithId:@"OctFull"], [self->locale_ getWithId:@"NovFull"], [self->locale_ getWithId:@"DecFull"] } count:12 type:NSString_class_()];
}

AMI18nEngine *new_AMI18nEngine_initWithProvider_withModules_(id<AMLocaleProvider> provider, ImActorModelModulesModules *modules) {
  AMI18nEngine *self = [AMI18nEngine alloc];
  AMI18nEngine_initWithProvider_withModules_(self, provider, modules);
  return self;
}

NSString *AMI18nEngine_formatTwoDigitWithInt_(AMI18nEngine *self, jint v) {
  if (v < 0) {
    return @"00";
  }
  else if (v < 10) {
    return JreStrcat("CI", '0', v);
  }
  else if (v < 100) {
    return JreStrcat("I", v);
  }
  else {
    NSString *res = JreStrcat("I", v);
    return [res substring:((jint) [res length]) - 2];
  }
}

jboolean AMI18nEngine_areSameDaysWithLong_withLong_(jlong a, jlong b) {
  AMI18nEngine_initialize();
  JavaUtilDate *date1 = new_JavaUtilDate_initWithLong_(a);
  jint y1 = [date1 getYear];
  jint m1 = [date1 getMonth];
  jint d1 = [date1 getDate];
  JavaUtilDate *date2 = new_JavaUtilDate_initWithLong_(b);
  jint y2 = [date2 getYear];
  jint m2 = [date2 getMonth];
  jint d2 = [date2 getDate];
  return y1 == y2 && m1 == m2 && d1 == d2;
}

NSString *AMI18nEngine_getTemplateNamedWithInt_withNSString_(AMI18nEngine *self, jint senderId, NSString *baseString) {
  return [((NSString *) nil_chk(AMI18nEngine_getTemplateWithInt_withNSString_(self, senderId, baseString))) replace:@"{name}" withSequence:[self formatPerformerNameWithUid:senderId]];
}

NSString *AMI18nEngine_getTemplateWithInt_withNSString_(AMI18nEngine *self, jint senderId, NSString *baseString) {
  if (senderId == [((ImActorModelModulesAuth *) nil_chk([((ImActorModelModulesModules *) nil_chk(self->modules_)) getAuthModule])) myUid]) {
    if ([((JavaUtilHashMap *) nil_chk(self->locale_)) containsKeyWithId:JreStrcat("$$", baseString, @"You")]) {
      return [self->locale_ getWithId:JreStrcat("$$", baseString, @"You")];
    }
  }
  if ([((JavaUtilHashMap *) nil_chk(self->locale_)) containsKeyWithId:JreStrcat("$$", baseString, @"Male")] && [self->locale_ containsKeyWithId:JreStrcat("$$", baseString, @"Female")]) {
    AMUser *u = AMI18nEngine_getUserWithInt_(self, senderId);
    if ([((AMUser *) nil_chk(u)) getSex] == AMSexEnum_get_MALE()) {
      return [self->locale_ getWithId:JreStrcat("$$", baseString, @"Male")];
    }
    else if ([u getSex] == AMSexEnum_get_FEMALE()) {
      return [self->locale_ getWithId:JreStrcat("$$", baseString, @"Female")];
    }
  }
  return [self->locale_ getWithId:baseString];
}

AMUser *AMI18nEngine_getUserWithInt_(AMI18nEngine *self, jint uid) {
  return [((id<DKKeyValueEngine>) nil_chk([((ImActorModelModulesUsers *) nil_chk([((ImActorModelModulesModules *) nil_chk(self->modules_)) getUsersModule])) getUsers])) getValueWithKey:uid];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMI18nEngine)

@implementation AMI18nEngine_$1

- (jint)compareWithLong:(jlong)lhs
               withLong:(jlong)rhs {
  return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
}

- (jint)compareWithId:(AMMessage *)lhs
               withId:(AMMessage *)rhs {
  return [self compareWithLong:[((AMMessage *) nil_chk(lhs)) getEngineSort] withLong:[((AMMessage *) nil_chk(rhs)) getEngineSort]];
}

- (instancetype)init {
  AMI18nEngine_$1_init(self);
  return self;
}

@end

void AMI18nEngine_$1_init(AMI18nEngine_$1 *self) {
  (void) NSObject_init(self);
}

AMI18nEngine_$1 *new_AMI18nEngine_$1_init() {
  AMI18nEngine_$1 *self = [AMI18nEngine_$1 alloc];
  AMI18nEngine_$1_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMI18nEngine_$1)
