//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/Pushes.java
//

#ifndef _ImActorModelModulesPushes_H_
#define _ImActorModelModulesPushes_H_

#include "J2ObjC_header.h"
#include "im/actor/model/modules/BaseModule.h"

@class ImActorModelModulesModules;

@interface ImActorModelModulesPushes : ImActorModelModulesBaseModule

#pragma mark Public

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules;

- (void)registerApplePushWithInt:(jint)apnsKey
                    withNSString:(NSString *)token;

- (void)registerGooglePushWithLong:(jlong)projectId
                      withNSString:(NSString *)token;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesPushes)

FOUNDATION_EXPORT void ImActorModelModulesPushes_initWithImActorModelModulesModules_(ImActorModelModulesPushes *self, ImActorModelModulesModules *modules);

FOUNDATION_EXPORT ImActorModelModulesPushes *new_ImActorModelModulesPushes_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesPushes)

#endif // _ImActorModelModulesPushes_H_
