//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/content/FastThumb.java
//

#ifndef _AMFastThumb_H_
#define _AMFastThumb_H_

#include "J2ObjC_header.h"

@class APFastThumb;
@class IOSByteArray;
@class ImActorModelEntityContentInternalLocalFastThumb;

@interface AMFastThumb : NSObject

#pragma mark Public

- (instancetype)initWithAPFastThumb:(APFastThumb *)fastThumb;

- (instancetype)initWithInt:(jint)w
                    withInt:(jint)h
              withByteArray:(IOSByteArray *)image;

- (instancetype)initWithImActorModelEntityContentInternalLocalFastThumb:(ImActorModelEntityContentInternalLocalFastThumb *)localFastThumb;

- (jint)getH;

- (IOSByteArray *)getImage;

- (jint)getW;

@end

J2OBJC_EMPTY_STATIC_INIT(AMFastThumb)

FOUNDATION_EXPORT void AMFastThumb_initWithImActorModelEntityContentInternalLocalFastThumb_(AMFastThumb *self, ImActorModelEntityContentInternalLocalFastThumb *localFastThumb);

FOUNDATION_EXPORT AMFastThumb *new_AMFastThumb_initWithImActorModelEntityContentInternalLocalFastThumb_(ImActorModelEntityContentInternalLocalFastThumb *localFastThumb) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void AMFastThumb_initWithAPFastThumb_(AMFastThumb *self, APFastThumb *fastThumb);

FOUNDATION_EXPORT AMFastThumb *new_AMFastThumb_initWithAPFastThumb_(APFastThumb *fastThumb) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void AMFastThumb_initWithInt_withInt_withByteArray_(AMFastThumb *self, jint w, jint h, IOSByteArray *image);

FOUNDATION_EXPORT AMFastThumb *new_AMFastThumb_initWithInt_withInt_withByteArray_(jint w, jint h, IOSByteArray *image) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMFastThumb)

typedef AMFastThumb ImActorModelEntityContentFastThumb;

#endif // _AMFastThumb_H_
