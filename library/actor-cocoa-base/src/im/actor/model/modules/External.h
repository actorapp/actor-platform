//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/External.java
//

#ifndef _ImActorModelModulesExternal_H_
#define _ImActorModelModulesExternal_H_

#include "J2ObjC_header.h"
#include "im/actor/model/modules/BaseModule.h"

@class APRequest;
@class ImActorModelModulesModules;
@protocol AMCommand;

@interface ImActorModelModulesExternal : ImActorModelModulesBaseModule

#pragma mark Public

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules;

- (id<AMCommand>)externalMethodWithAPRequest:(APRequest *)request;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesExternal)

FOUNDATION_EXPORT void ImActorModelModulesExternal_initWithImActorModelModulesModules_(ImActorModelModulesExternal *self, ImActorModelModulesModules *modules);

FOUNDATION_EXPORT ImActorModelModulesExternal *new_ImActorModelModulesExternal_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesExternal)

#endif // _ImActorModelModulesExternal_H_
