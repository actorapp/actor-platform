//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/compat/content/ObsoleteServiceRegistered.java
//

#ifndef _ImActorModelEntityCompatContentObsoleteServiceRegistered_H_
#define _ImActorModelEntityCompatContentObsoleteServiceRegistered_H_

#include "J2ObjC_header.h"
#include "im/actor/model/entity/compat/content/ObsoleteAbsContent.h"

@class APMessage;
@class BSBserValues;
@class BSBserWriter;

@interface ImActorModelEntityCompatContentObsoleteServiceRegistered : ImActorModelEntityCompatContentObsoleteAbsContent

#pragma mark Public

- (instancetype)init;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (APMessage *)toApiMessage;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelEntityCompatContentObsoleteServiceRegistered)

FOUNDATION_EXPORT void ImActorModelEntityCompatContentObsoleteServiceRegistered_init(ImActorModelEntityCompatContentObsoleteServiceRegistered *self);

FOUNDATION_EXPORT ImActorModelEntityCompatContentObsoleteServiceRegistered *new_ImActorModelEntityCompatContentObsoleteServiceRegistered_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelEntityCompatContentObsoleteServiceRegistered)

#endif // _ImActorModelEntityCompatContentObsoleteServiceRegistered_H_
