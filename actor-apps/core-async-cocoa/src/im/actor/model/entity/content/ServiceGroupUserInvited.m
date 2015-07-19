//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/entity/content/ServiceGroupUserInvited.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/api/Message.h"
#include "im/actor/model/api/ServiceEx.h"
#include "im/actor/model/api/ServiceExUserInvited.h"
#include "im/actor/model/api/ServiceMessage.h"
#include "im/actor/model/entity/content/ServiceContent.h"
#include "im/actor/model/entity/content/ServiceGroupUserInvited.h"
#include "im/actor/model/entity/content/internal/ContentRemoteContainer.h"

@interface AMServiceGroupUserInvited () {
 @public
  jint addedUid_;
}

@end

@implementation AMServiceGroupUserInvited

+ (AMServiceGroupUserInvited *)createWithInt:(jint)uid {
  return AMServiceGroupUserInvited_createWithInt_(uid);
}

- (instancetype)initWithImActorModelEntityContentInternalContentRemoteContainer:(ImActorModelEntityContentInternalContentRemoteContainer *)contentContainer {
  AMServiceGroupUserInvited_initWithImActorModelEntityContentInternalContentRemoteContainer_(self, contentContainer);
  return self;
}

- (jint)getAddedUid {
  return addedUid_;
}

@end

AMServiceGroupUserInvited *AMServiceGroupUserInvited_createWithInt_(jint uid) {
  AMServiceGroupUserInvited_initialize();
  return new_AMServiceGroupUserInvited_initWithImActorModelEntityContentInternalContentRemoteContainer_(new_ImActorModelEntityContentInternalContentRemoteContainer_initWithAPMessage_(new_APServiceMessage_initWithNSString_withAPServiceEx_(@"User added", new_APServiceExUserInvited_initWithInt_(uid))));
}

void AMServiceGroupUserInvited_initWithImActorModelEntityContentInternalContentRemoteContainer_(AMServiceGroupUserInvited *self, ImActorModelEntityContentInternalContentRemoteContainer *contentContainer) {
  (void) AMServiceContent_initWithImActorModelEntityContentInternalContentRemoteContainer_(self, contentContainer);
  APServiceMessage *serviceMessage = (APServiceMessage *) check_class_cast([((ImActorModelEntityContentInternalContentRemoteContainer *) nil_chk(contentContainer)) getMessage], [APServiceMessage class]);
  self->addedUid_ = [((APServiceExUserInvited *) nil_chk(((APServiceExUserInvited *) check_class_cast([((APServiceMessage *) nil_chk(serviceMessage)) getExt], [APServiceExUserInvited class])))) getInvitedUid];
}

AMServiceGroupUserInvited *new_AMServiceGroupUserInvited_initWithImActorModelEntityContentInternalContentRemoteContainer_(ImActorModelEntityContentInternalContentRemoteContainer *contentContainer) {
  AMServiceGroupUserInvited *self = [AMServiceGroupUserInvited alloc];
  AMServiceGroupUserInvited_initWithImActorModelEntityContentInternalContentRemoteContainer_(self, contentContainer);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMServiceGroupUserInvited)
