//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/typing/OwnTypingActor.java
//

#ifndef _ImActorModelModulesTypingOwnTypingActor_H_
#define _ImActorModelModulesTypingOwnTypingActor_H_

#include "J2ObjC_header.h"
#include "im/actor/model/modules/utils/ModuleActor.h"

@class AMPeer;
@class ImActorModelModulesModules;

@interface ImActorModelModulesTypingOwnTypingActor : ImActorModelModulesUtilsModuleActor

#pragma mark Public

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)messenger;

- (void)onReceiveWithId:(id)message;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesTypingOwnTypingActor)

FOUNDATION_EXPORT void ImActorModelModulesTypingOwnTypingActor_initWithImActorModelModulesModules_(ImActorModelModulesTypingOwnTypingActor *self, ImActorModelModulesModules *messenger);

FOUNDATION_EXPORT ImActorModelModulesTypingOwnTypingActor *new_ImActorModelModulesTypingOwnTypingActor_initWithImActorModelModulesModules_(ImActorModelModulesModules *messenger) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesTypingOwnTypingActor)

@interface ImActorModelModulesTypingOwnTypingActor_Typing : NSObject

#pragma mark Public

- (instancetype)initWithAMPeer:(AMPeer *)peer;

- (AMPeer *)getPeer;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesTypingOwnTypingActor_Typing)

FOUNDATION_EXPORT void ImActorModelModulesTypingOwnTypingActor_Typing_initWithAMPeer_(ImActorModelModulesTypingOwnTypingActor_Typing *self, AMPeer *peer);

FOUNDATION_EXPORT ImActorModelModulesTypingOwnTypingActor_Typing *new_ImActorModelModulesTypingOwnTypingActor_Typing_initWithAMPeer_(AMPeer *peer) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesTypingOwnTypingActor_Typing)

#endif // _ImActorModelModulesTypingOwnTypingActor_H_
