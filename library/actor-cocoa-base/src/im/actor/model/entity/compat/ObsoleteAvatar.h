//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/compat/ObsoleteAvatar.java
//

#ifndef _ImActorModelEntityCompatObsoleteAvatar_H_
#define _ImActorModelEntityCompatObsoleteAvatar_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"

@class APAvatar;
@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;
@class ImActorModelEntityCompatObsoleteAvatarImage;

@interface ImActorModelEntityCompatObsoleteAvatar : BSBserObject

#pragma mark Public

- (instancetype)initWithBSBserValues:(BSBserValues *)values;

- (instancetype)initWithByteArray:(IOSByteArray *)data;

- (ImActorModelEntityCompatObsoleteAvatarImage *)getFullImage;

- (ImActorModelEntityCompatObsoleteAvatarImage *)getLargeImage;

- (ImActorModelEntityCompatObsoleteAvatarImage *)getSmallImage;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (APAvatar *)toApiAvatar;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelEntityCompatObsoleteAvatar)

FOUNDATION_EXPORT void ImActorModelEntityCompatObsoleteAvatar_initWithByteArray_(ImActorModelEntityCompatObsoleteAvatar *self, IOSByteArray *data);

FOUNDATION_EXPORT ImActorModelEntityCompatObsoleteAvatar *new_ImActorModelEntityCompatObsoleteAvatar_initWithByteArray_(IOSByteArray *data) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void ImActorModelEntityCompatObsoleteAvatar_initWithBSBserValues_(ImActorModelEntityCompatObsoleteAvatar *self, BSBserValues *values);

FOUNDATION_EXPORT ImActorModelEntityCompatObsoleteAvatar *new_ImActorModelEntityCompatObsoleteAvatar_initWithBSBserValues_(BSBserValues *values) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelEntityCompatObsoleteAvatar)

#endif // _ImActorModelEntityCompatObsoleteAvatar_H_
