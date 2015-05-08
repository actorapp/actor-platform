//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/search/SearchActor.java
//


#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/actors/Actor.h"
#include "im/actor/model/droidkit/engine/KeyValueEngine.h"
#include "im/actor/model/droidkit/engine/ListEngine.h"
#include "im/actor/model/entity/Avatar.h"
#include "im/actor/model/entity/Dialog.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/SearchEntity.h"
#include "im/actor/model/entity/User.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/SearchModule.h"
#include "im/actor/model/modules/search/SearchActor.h"
#include "im/actor/model/modules/utils/ModuleActor.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"

#define ImActorModelModulesSearchSearchActor_CONTACTS_PREFIX 4294967296LL

@interface ImActorModelModulesSearchSearchActor () {
 @public
  id<DKListEngine> listEngine_;
}

- (void)onDialogsUpdatedWithJavaUtilList:(id<JavaUtilList>)dialogs;

- (void)onContactsUpdatedWithIntArray:(IOSIntArray *)contactsList;

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesSearchSearchActor, listEngine_, id<DKListEngine>)

J2OBJC_STATIC_FIELD_GETTER(ImActorModelModulesSearchSearchActor, CONTACTS_PREFIX, jlong)

__attribute__((unused)) static void ImActorModelModulesSearchSearchActor_onDialogsUpdatedWithJavaUtilList_(ImActorModelModulesSearchSearchActor *self, id<JavaUtilList> dialogs);

__attribute__((unused)) static void ImActorModelModulesSearchSearchActor_onContactsUpdatedWithIntArray_(ImActorModelModulesSearchSearchActor *self, IOSIntArray *contactsList);

@interface ImActorModelModulesSearchSearchActor_OnDialogsUpdated () {
 @public
  id<JavaUtilList> dialogs_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesSearchSearchActor_OnDialogsUpdated, dialogs_, id<JavaUtilList>)

@interface ImActorModelModulesSearchSearchActor_OnContactsUpdated () {
 @public
  IOSIntArray *contactsList_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesSearchSearchActor_OnContactsUpdated, contactsList_, IOSIntArray *)

@implementation ImActorModelModulesSearchSearchActor

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules {
  ImActorModelModulesSearchSearchActor_initWithImActorModelModulesModules_(self, modules);
  return self;
}

- (void)preStart {
  [super preStart];
  listEngine_ = [((ImActorModelModulesSearchModule *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getSearch])) getSearchList];
}

- (void)onDialogsUpdatedWithJavaUtilList:(id<JavaUtilList>)dialogs {
  ImActorModelModulesSearchSearchActor_onDialogsUpdatedWithJavaUtilList_(self, dialogs);
}

- (void)onContactsUpdatedWithIntArray:(IOSIntArray *)contactsList {
  ImActorModelModulesSearchSearchActor_onContactsUpdatedWithIntArray_(self, contactsList);
}

- (void)onReceiveWithId:(id)message {
  if ([message isKindOfClass:[ImActorModelModulesSearchSearchActor_OnDialogsUpdated class]]) {
    ImActorModelModulesSearchSearchActor_OnDialogsUpdated *onDialogsUpdated = (ImActorModelModulesSearchSearchActor_OnDialogsUpdated *) check_class_cast(message, [ImActorModelModulesSearchSearchActor_OnDialogsUpdated class]);
    ImActorModelModulesSearchSearchActor_onDialogsUpdatedWithJavaUtilList_(self, [((ImActorModelModulesSearchSearchActor_OnDialogsUpdated *) nil_chk(onDialogsUpdated)) getDialogs]);
  }
  else if ([message isKindOfClass:[ImActorModelModulesSearchSearchActor_OnContactsUpdated class]]) {
    ImActorModelModulesSearchSearchActor_OnContactsUpdated *contactsUpdated = (ImActorModelModulesSearchSearchActor_OnContactsUpdated *) check_class_cast(message, [ImActorModelModulesSearchSearchActor_OnContactsUpdated class]);
    ImActorModelModulesSearchSearchActor_onContactsUpdatedWithIntArray_(self, [((ImActorModelModulesSearchSearchActor_OnContactsUpdated *) nil_chk(contactsUpdated)) getContactsList]);
  }
  else {
    [self dropWithId:message];
  }
}

@end

void ImActorModelModulesSearchSearchActor_initWithImActorModelModulesModules_(ImActorModelModulesSearchSearchActor *self, ImActorModelModulesModules *modules) {
  (void) ImActorModelModulesUtilsModuleActor_initWithImActorModelModulesModules_(self, modules);
}

ImActorModelModulesSearchSearchActor *new_ImActorModelModulesSearchSearchActor_initWithImActorModelModulesModules_(ImActorModelModulesModules *modules) {
  ImActorModelModulesSearchSearchActor *self = [ImActorModelModulesSearchSearchActor alloc];
  ImActorModelModulesSearchSearchActor_initWithImActorModelModulesModules_(self, modules);
  return self;
}

void ImActorModelModulesSearchSearchActor_onDialogsUpdatedWithJavaUtilList_(ImActorModelModulesSearchSearchActor *self, id<JavaUtilList> dialogs) {
  id<JavaUtilList> updated = new_JavaUtilArrayList_init();
  for (AMDialog * __strong d in nil_chk(dialogs)) {
    [updated addWithId:new_AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_([((AMDialog *) nil_chk(d)) getPeer], [d getSortDate], [d getDialogAvatar], [d getDialogTitle])];
  }
  [((id<DKListEngine>) nil_chk(self->listEngine_)) addOrUpdateItemsWithJavaUtilList:updated];
}

void ImActorModelModulesSearchSearchActor_onContactsUpdatedWithIntArray_(ImActorModelModulesSearchSearchActor *self, IOSIntArray *contactsList) {
  id<JavaUtilList> updated = new_JavaUtilArrayList_init();
  for (jint i = 0; i < ((IOSIntArray *) nil_chk(contactsList))->size_; i++) {
    AMUser *user = [((id<DKKeyValueEngine>) nil_chk([self users])) getValueWithLong:IOSIntArray_Get(contactsList, i)];
    [updated addWithId:new_AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(AMPeer_userWithInt_([((AMUser *) nil_chk(user)) getUid]), ImActorModelModulesSearchSearchActor_CONTACTS_PREFIX + i, [user getAvatar], [user getName])];
  }
  [((id<DKListEngine>) nil_chk(self->listEngine_)) addOrUpdateItemsWithJavaUtilList:updated];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesSearchSearchActor)

@implementation ImActorModelModulesSearchSearchActor_OnDialogsUpdated

- (instancetype)initWithJavaUtilList:(id<JavaUtilList>)dialogs {
  ImActorModelModulesSearchSearchActor_OnDialogsUpdated_initWithJavaUtilList_(self, dialogs);
  return self;
}

- (id<JavaUtilList>)getDialogs {
  return dialogs_;
}

@end

void ImActorModelModulesSearchSearchActor_OnDialogsUpdated_initWithJavaUtilList_(ImActorModelModulesSearchSearchActor_OnDialogsUpdated *self, id<JavaUtilList> dialogs) {
  (void) NSObject_init(self);
  self->dialogs_ = dialogs;
}

ImActorModelModulesSearchSearchActor_OnDialogsUpdated *new_ImActorModelModulesSearchSearchActor_OnDialogsUpdated_initWithJavaUtilList_(id<JavaUtilList> dialogs) {
  ImActorModelModulesSearchSearchActor_OnDialogsUpdated *self = [ImActorModelModulesSearchSearchActor_OnDialogsUpdated alloc];
  ImActorModelModulesSearchSearchActor_OnDialogsUpdated_initWithJavaUtilList_(self, dialogs);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesSearchSearchActor_OnDialogsUpdated)

@implementation ImActorModelModulesSearchSearchActor_OnContactsUpdated

- (instancetype)initWithIntArray:(IOSIntArray *)contactsList {
  ImActorModelModulesSearchSearchActor_OnContactsUpdated_initWithIntArray_(self, contactsList);
  return self;
}

- (IOSIntArray *)getContactsList {
  return contactsList_;
}

@end

void ImActorModelModulesSearchSearchActor_OnContactsUpdated_initWithIntArray_(ImActorModelModulesSearchSearchActor_OnContactsUpdated *self, IOSIntArray *contactsList) {
  (void) NSObject_init(self);
  self->contactsList_ = contactsList;
}

ImActorModelModulesSearchSearchActor_OnContactsUpdated *new_ImActorModelModulesSearchSearchActor_OnContactsUpdated_initWithIntArray_(IOSIntArray *contactsList) {
  ImActorModelModulesSearchSearchActor_OnContactsUpdated *self = [ImActorModelModulesSearchSearchActor_OnContactsUpdated alloc];
  ImActorModelModulesSearchSearchActor_OnContactsUpdated_initWithIntArray_(self, contactsList);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesSearchSearchActor_OnContactsUpdated)
