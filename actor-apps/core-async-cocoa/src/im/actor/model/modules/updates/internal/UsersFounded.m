//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/modules/updates/internal/UsersFounded.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/concurrency/CommandCallback.h"
#include "im/actor/model/modules/updates/internal/InternalUpdate.h"
#include "im/actor/model/modules/updates/internal/UsersFounded.h"
#include "java/util/List.h"

@interface ImActorModelModulesUpdatesInternalUsersFounded () {
 @public
  id<JavaUtilList> users_;
  id<AMCommandCallback> commandCallback_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesUpdatesInternalUsersFounded, users_, id<JavaUtilList>)
J2OBJC_FIELD_SETTER(ImActorModelModulesUpdatesInternalUsersFounded, commandCallback_, id<AMCommandCallback>)

@implementation ImActorModelModulesUpdatesInternalUsersFounded

- (instancetype)initWithJavaUtilList:(id<JavaUtilList>)users
               withAMCommandCallback:(id<AMCommandCallback>)commandCallback {
  ImActorModelModulesUpdatesInternalUsersFounded_initWithJavaUtilList_withAMCommandCallback_(self, users, commandCallback);
  return self;
}

- (id<JavaUtilList>)getUsers {
  return users_;
}

- (id<AMCommandCallback>)getCommandCallback {
  return commandCallback_;
}

@end

void ImActorModelModulesUpdatesInternalUsersFounded_initWithJavaUtilList_withAMCommandCallback_(ImActorModelModulesUpdatesInternalUsersFounded *self, id<JavaUtilList> users, id<AMCommandCallback> commandCallback) {
  (void) ImActorModelModulesUpdatesInternalInternalUpdate_init(self);
  self->users_ = users;
  self->commandCallback_ = commandCallback;
}

ImActorModelModulesUpdatesInternalUsersFounded *new_ImActorModelModulesUpdatesInternalUsersFounded_initWithJavaUtilList_withAMCommandCallback_(id<JavaUtilList> users, id<AMCommandCallback> commandCallback) {
  ImActorModelModulesUpdatesInternalUsersFounded *self = [ImActorModelModulesUpdatesInternalUsersFounded alloc];
  ImActorModelModulesUpdatesInternalUsersFounded_initWithJavaUtilList_withAMCommandCallback_(self, users, commandCallback);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesUpdatesInternalUsersFounded)
