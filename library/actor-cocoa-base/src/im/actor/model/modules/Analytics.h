//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/Analytics.java
//

#ifndef _ImActorModelModulesAnalytics_H_
#define _ImActorModelModulesAnalytics_H_

#include "J2ObjC_header.h"
#include "im/actor/model/modules/BaseModule.h"

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

- (void)trackAppHidden;

- (void)trackAppVisible;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesAnalytics)

FOUNDATION_EXPORT void ImActorModelModulesAnalytics_initWithImActorModelModulesModules_(ImActorModelModulesAnalytics *self, ImActorModelModulesModules *modules);

FOUNDATION_EXPORT ImActorModelModulesAnalytics *new_ImActorModelModulesAnalytics_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesAnalytics)

#endif // _ImActorModelModulesAnalytics_H_
