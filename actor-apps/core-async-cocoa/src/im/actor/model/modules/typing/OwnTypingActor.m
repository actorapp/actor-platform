//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/modules/typing/OwnTypingActor.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/api/OutPeer.h"
#include "im/actor/model/api/TypingType.h"
#include "im/actor/model/api/rpc/RequestTyping.h"
#include "im/actor/model/droidkit/actors/Actor.h"
#include "im/actor/model/droidkit/actors/ActorTime.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/typing/OwnTypingActor.h"
#include "im/actor/model/modules/utils/ModuleActor.h"

#define ImActorModelModulesTypingOwnTypingActor_TYPING_DELAY 3000LL

@interface ImActorModelModulesTypingOwnTypingActor () {
 @public
  jlong lastTypingTime_;
}

- (void)onTypingWithAMPeer:(AMPeer *)peer;

@end

J2OBJC_STATIC_FIELD_GETTER(ImActorModelModulesTypingOwnTypingActor, TYPING_DELAY, jlong)

__attribute__((unused)) static void ImActorModelModulesTypingOwnTypingActor_onTypingWithAMPeer_(ImActorModelModulesTypingOwnTypingActor *self, AMPeer *peer);

@interface ImActorModelModulesTypingOwnTypingActor_Typing () {
 @public
  AMPeer *peer_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesTypingOwnTypingActor_Typing, peer_, AMPeer *)

@implementation ImActorModelModulesTypingOwnTypingActor

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)messenger {
  ImActorModelModulesTypingOwnTypingActor_initWithImActorModelModulesModules_(self, messenger);
  return self;
}

- (void)onTypingWithAMPeer:(AMPeer *)peer {
  ImActorModelModulesTypingOwnTypingActor_onTypingWithAMPeer_(self, peer);
}

- (void)onReceiveWithId:(id)message {
  if ([message isKindOfClass:[ImActorModelModulesTypingOwnTypingActor_Typing class]]) {
    ImActorModelModulesTypingOwnTypingActor_onTypingWithAMPeer_(self, [((ImActorModelModulesTypingOwnTypingActor_Typing *) nil_chk(((ImActorModelModulesTypingOwnTypingActor_Typing *) check_class_cast(message, [ImActorModelModulesTypingOwnTypingActor_Typing class])))) getPeer]);
  }
  else {
    [self dropWithId:message];
  }
}

@end

void ImActorModelModulesTypingOwnTypingActor_initWithImActorModelModulesModules_(ImActorModelModulesTypingOwnTypingActor *self, ImActorModelModulesModules *messenger) {
  (void) ImActorModelModulesUtilsModuleActor_initWithImActorModelModulesModules_(self, messenger);
  self->lastTypingTime_ = 0;
}

ImActorModelModulesTypingOwnTypingActor *new_ImActorModelModulesTypingOwnTypingActor_initWithImActorModelModulesModules_(ImActorModelModulesModules *messenger) {
  ImActorModelModulesTypingOwnTypingActor *self = [ImActorModelModulesTypingOwnTypingActor alloc];
  ImActorModelModulesTypingOwnTypingActor_initWithImActorModelModulesModules_(self, messenger);
  return self;
}

void ImActorModelModulesTypingOwnTypingActor_onTypingWithAMPeer_(ImActorModelModulesTypingOwnTypingActor *self, AMPeer *peer) {
  if (DKActorTime_currentTime() - self->lastTypingTime_ < ImActorModelModulesTypingOwnTypingActor_TYPING_DELAY) {
    return;
  }
  self->lastTypingTime_ = DKActorTime_currentTime();
  APOutPeer *outPeer = [self buidOutPeerWithAMPeer:peer];
  if (outPeer == nil) {
    return;
  }
  [self requestWithAPRequest:new_APRequestTyping_initWithAPOutPeer_withAPTypingTypeEnum_(outPeer, APTypingTypeEnum_get_TEXT())];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesTypingOwnTypingActor)

@implementation ImActorModelModulesTypingOwnTypingActor_Typing

- (instancetype)initWithAMPeer:(AMPeer *)peer {
  ImActorModelModulesTypingOwnTypingActor_Typing_initWithAMPeer_(self, peer);
  return self;
}

- (AMPeer *)getPeer {
  return peer_;
}

@end

void ImActorModelModulesTypingOwnTypingActor_Typing_initWithAMPeer_(ImActorModelModulesTypingOwnTypingActor_Typing *self, AMPeer *peer) {
  (void) NSObject_init(self);
  self->peer_ = peer;
}

ImActorModelModulesTypingOwnTypingActor_Typing *new_ImActorModelModulesTypingOwnTypingActor_Typing_initWithAMPeer_(AMPeer *peer) {
  ImActorModelModulesTypingOwnTypingActor_Typing *self = [ImActorModelModulesTypingOwnTypingActor_Typing alloc];
  ImActorModelModulesTypingOwnTypingActor_Typing_initWithAMPeer_(self, peer);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesTypingOwnTypingActor_Typing)
