//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/modules/contacts/BookImportActor.java
//

#ifndef _ImActorModelModulesContactsBookImportActor_H_
#define _ImActorModelModulesContactsBookImportActor_H_

#include "J2ObjC_header.h"
#include "im/actor/model/modules/utils/ModuleActor.h"

@class ImActorModelModulesModules;

@interface ImActorModelModulesContactsBookImportActor : ImActorModelModulesUtilsModuleActor

#pragma mark Public

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)messenger;

- (void)onReceiveWithId:(id)message;

- (void)preStart;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesContactsBookImportActor)

FOUNDATION_EXPORT void ImActorModelModulesContactsBookImportActor_initWithImActorModelModulesModules_(ImActorModelModulesContactsBookImportActor *self, ImActorModelModulesModules *messenger);

FOUNDATION_EXPORT ImActorModelModulesContactsBookImportActor *new_ImActorModelModulesContactsBookImportActor_initWithImActorModelModulesModules_(ImActorModelModulesModules *messenger) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesContactsBookImportActor)

@interface ImActorModelModulesContactsBookImportActor_PerformSync : NSObject

#pragma mark Public

- (instancetype)init;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesContactsBookImportActor_PerformSync)

FOUNDATION_EXPORT void ImActorModelModulesContactsBookImportActor_PerformSync_init(ImActorModelModulesContactsBookImportActor_PerformSync *self);

FOUNDATION_EXPORT ImActorModelModulesContactsBookImportActor_PerformSync *new_ImActorModelModulesContactsBookImportActor_PerformSync_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesContactsBookImportActor_PerformSync)

#endif // _ImActorModelModulesContactsBookImportActor_H_
