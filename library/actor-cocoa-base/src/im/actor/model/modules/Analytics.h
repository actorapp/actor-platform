//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/Analytics.java
//

#ifndef _ImActorModelModulesAnalytics_H_
#define _ImActorModelModulesAnalytics_H_

#include "J2ObjC_header.h"
#include "im/actor/model/modules/BaseModule.h"

@class AMPeer;
@class IOSObjectArray;
@class ImActorModelModulesModules;

@interface ImActorModelModulesAnalytics : ImActorModelModulesBaseModule

#pragma mark Public

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules;

- (void)onLoggedInWithNSString:(NSString *)deviceId
                       withInt:(jint)uid
         withJavaLangLongArray:(IOSObjectArray *)phoneNumbers
                  withNSString:(NSString *)userName;

- (void)onLoggedInPerformedWithNSString:(NSString *)deviceId
                                withInt:(jint)uid
                  withJavaLangLongArray:(IOSObjectArray *)phoneNumber
                           withNSString:(NSString *)userName;

- (void)onLoggedOutWithNSString:(NSString *)deviceId;

- (void)trackWithNSString:(NSString *)event;

- (void)trackWithNSString:(NSString *)event
        withNSStringArray:(IOSObjectArray *)args;

- (void)trackActionCancelWithNSString:(NSString *)action;

- (void)trackActionErrorWithNSString:(NSString *)action
                        withNSString:(NSString *)tag
                        withNSString:(NSString *)message;

- (void)trackActionSuccessWithNSString:(NSString *)action;

- (void)trackActionTryAgainWithNSString:(NSString *)action;

- (void)trackAddContactPressed;

- (void)trackAppHidden;

- (void)trackAppVisible;

- (void)trackAuthCodeClosed;

- (void)trackAuthCodeOpen;

- (void)trackAuthCodeTypeWithNSString:(NSString *)newValue;

- (void)trackAuthCodeWrongNumber;

- (void)trackAuthCodeWrongNumberCancel;

- (void)trackAuthCodeWrongNumberChange;

- (void)trackAuthCountryClosed;

- (void)trackAuthCountryOpen;

- (void)trackAuthCountryPickedWithNSString:(NSString *)country;

- (void)trackAuthPhoneInfoOpen;

- (void)trackAuthPhoneOpen;

- (void)trackAuthPhoneTypeWithNSString:(NSString *)newValue;

- (void)trackAuthSignupAvatarCanelled;

- (void)trackAuthSignupAvatarDeleted;

- (void)trackAuthSignupAvatarPicked;

- (void)trackAuthSignupClosed;

- (void)trackAuthSignupClosedNameTypeWithNSString:(NSString *)newValue;

- (void)trackAuthSignupOpen;

- (void)trackAuthSignupPressedAvatar;

- (void)trackAuthSuccess;

- (void)trackBackPressed;

- (void)trackChatClosedWithAMPeer:(AMPeer *)peer;

- (void)trackChatOpenWithAMPeer:(AMPeer *)peer;

- (void)trackCodeRequest;

- (void)trackCodeRequestWithLong:(jlong)phone;

- (void)trackContactsClosed;

- (void)trackContactsOpen;

- (void)trackDialogsClosed;

- (void)trackDialogsOpen;

- (void)trackDocumentSendWithAMPeer:(AMPeer *)peer;

- (void)trackInvitePressed;

- (void)trackMainScreensClosed;

- (void)trackMainScreensOpen;

- (void)trackOwnProfileClosed;

- (void)trackOwnProfileOpen;

- (void)trackPhotoSendWithAMPeer:(AMPeer *)peer;

- (void)trackProfileClosedWithInt:(jint)uid;

- (void)trackProfileOpenWithInt:(jint)uid;

- (void)trackTextSendWithAMPeer:(AMPeer *)peer;

- (void)trackUpPressed;

- (void)trackVideoSendWithAMPeer:(AMPeer *)peer;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesAnalytics)

FOUNDATION_EXPORT void ImActorModelModulesAnalytics_initWithImActorModelModulesModules_(ImActorModelModulesAnalytics *self, ImActorModelModulesModules *modules);

FOUNDATION_EXPORT ImActorModelModulesAnalytics *new_ImActorModelModulesAnalytics_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesAnalytics)

#endif // _ImActorModelModulesAnalytics_H_
