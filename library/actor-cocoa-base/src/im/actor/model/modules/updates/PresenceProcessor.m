//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/updates/PresenceProcessor.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/updates/PresenceProcessor.java"

#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/modules/BaseModule.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/presence/PresenceActor.h"
#include "im/actor/model/modules/updates/PresenceProcessor.h"

@interface ImActorModelModulesUpdatesPresenceProcessor () {
 @public
  DKActorRef *presenceActor_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesUpdatesPresenceProcessor, presenceActor_, DKActorRef *)


#line 13
@implementation ImActorModelModulesUpdatesPresenceProcessor


#line 17
- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules {
  ImActorModelModulesUpdatesPresenceProcessor_initWithImActorModelModulesModules_(self, modules);
  return self;
}


#line 23
- (void)onUserOnlineWithInt:(jint)uid {
  [((DKActorRef *) nil_chk(presenceActor_)) sendOnceWithId:new_ImActorModelModulesPresencePresenceActor_UserOnline_initWithInt_(uid)];
}


#line 28
- (void)onUserOfflineWithInt:(jint)uid {
  [((DKActorRef *) nil_chk(presenceActor_)) sendOnceWithId:new_ImActorModelModulesPresencePresenceActor_UserOffline_initWithInt_(uid)];
}


#line 33
- (void)onUserLastSeenWithInt:(jint)uid
                     withLong:(jlong)date {
  [((DKActorRef *) nil_chk(presenceActor_)) sendOnceWithId:new_ImActorModelModulesPresencePresenceActor_UserLastSeen_initWithInt_withLong_(uid, date)];
}

- (void)onGroupOnlineWithInt:(jint)gid
                     withInt:(jint)count {
  [((DKActorRef *) nil_chk(presenceActor_)) sendOnceWithId:new_ImActorModelModulesPresencePresenceActor_GroupOnline_initWithInt_withInt_(gid, count)];
}

@end


#line 17
void ImActorModelModulesUpdatesPresenceProcessor_initWithImActorModelModulesModules_(ImActorModelModulesUpdatesPresenceProcessor *self, ImActorModelModulesModules *modules) {
  (void) ImActorModelModulesBaseModule_initWithImActorModelModulesModules_(self, modules);
  self->presenceActor_ = ImActorModelModulesPresencePresenceActor_getWithImActorModelModulesModules_(modules);
}


#line 17
ImActorModelModulesUpdatesPresenceProcessor *new_ImActorModelModulesUpdatesPresenceProcessor_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) {
  ImActorModelModulesUpdatesPresenceProcessor *self = [ImActorModelModulesUpdatesPresenceProcessor alloc];
  ImActorModelModulesUpdatesPresenceProcessor_initWithImActorModelModulesModules_(self, modules);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesUpdatesPresenceProcessor)
