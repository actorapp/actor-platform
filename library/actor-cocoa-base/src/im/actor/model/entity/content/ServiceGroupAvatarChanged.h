//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/content/ServiceGroupAvatarChanged.java
//

#ifndef _AMServiceGroupAvatarChanged_H_
#define _AMServiceGroupAvatarChanged_H_

#include "J2ObjC_header.h"
#include "im/actor/model/entity/content/ServiceContent.h"

@class AMAbsContent_ContentTypeEnum;
@class AMAvatar;
@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;

@interface AMServiceGroupAvatarChanged : AMServiceContent

#pragma mark Public

- (instancetype)initWithAMAvatar:(AMAvatar *)newAvatar;

+ (AMServiceGroupAvatarChanged *)fromBytesWithByteArray:(IOSByteArray *)data;

- (AMAvatar *)getNewAvatar;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

#pragma mark Protected

- (AMAbsContent_ContentTypeEnum *)getContentType;

@end

J2OBJC_EMPTY_STATIC_INIT(AMServiceGroupAvatarChanged)

FOUNDATION_EXPORT AMServiceGroupAvatarChanged *AMServiceGroupAvatarChanged_fromBytesWithByteArray_(IOSByteArray *data);

FOUNDATION_EXPORT void AMServiceGroupAvatarChanged_initWithAMAvatar_(AMServiceGroupAvatarChanged *self, AMAvatar *newAvatar);

FOUNDATION_EXPORT AMServiceGroupAvatarChanged *new_AMServiceGroupAvatarChanged_initWithAMAvatar_(AMAvatar *newAvatar) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMServiceGroupAvatarChanged)

typedef AMServiceGroupAvatarChanged ImActorModelEntityContentServiceGroupAvatarChanged;

#endif // _AMServiceGroupAvatarChanged_H_
