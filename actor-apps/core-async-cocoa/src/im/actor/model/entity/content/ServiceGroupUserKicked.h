//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/entity/content/ServiceGroupUserKicked.java
//

#ifndef _AMServiceGroupUserKicked_H_
#define _AMServiceGroupUserKicked_H_

#include "J2ObjC_header.h"
#include "im/actor/model/entity/content/ServiceContent.h"

@class ImActorModelEntityContentInternalContentRemoteContainer;

@interface AMServiceGroupUserKicked : AMServiceContent

#pragma mark Public

- (instancetype)initWithImActorModelEntityContentInternalContentRemoteContainer:(ImActorModelEntityContentInternalContentRemoteContainer *)contentContainer;

+ (AMServiceGroupUserKicked *)createWithInt:(jint)uid;

- (jint)getKickedUid;

@end

J2OBJC_EMPTY_STATIC_INIT(AMServiceGroupUserKicked)

FOUNDATION_EXPORT AMServiceGroupUserKicked *AMServiceGroupUserKicked_createWithInt_(jint uid);

FOUNDATION_EXPORT void AMServiceGroupUserKicked_initWithImActorModelEntityContentInternalContentRemoteContainer_(AMServiceGroupUserKicked *self, ImActorModelEntityContentInternalContentRemoteContainer *contentContainer);

FOUNDATION_EXPORT AMServiceGroupUserKicked *new_AMServiceGroupUserKicked_initWithImActorModelEntityContentInternalContentRemoteContainer_(ImActorModelEntityContentInternalContentRemoteContainer *contentContainer) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMServiceGroupUserKicked)

typedef AMServiceGroupUserKicked ImActorModelEntityContentServiceGroupUserKicked;

#endif // _AMServiceGroupUserKicked_H_
