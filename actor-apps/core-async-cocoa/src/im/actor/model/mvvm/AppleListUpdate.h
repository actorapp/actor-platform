//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core-async/src/main/java/im/actor/model/mvvm/AppleListUpdate.java
//

#ifndef _AMAppleListUpdate_H_
#define _AMAppleListUpdate_H_

#include "J2ObjC_header.h"

@class AMChangeDescription;
@class JavaUtilArrayList;

@interface AMAppleListUpdate : NSObject

#pragma mark Public

- (instancetype)initWithChanges:(JavaUtilArrayList *)changes;

- (AMChangeDescription *)changeAt:(jint)index;

- (JavaUtilArrayList *)changes;

- (jint)size;

@end

J2OBJC_EMPTY_STATIC_INIT(AMAppleListUpdate)

FOUNDATION_EXPORT void AMAppleListUpdate_initWithChanges_(AMAppleListUpdate *self, JavaUtilArrayList *changes);

FOUNDATION_EXPORT AMAppleListUpdate *new_AMAppleListUpdate_initWithChanges_(JavaUtilArrayList *changes) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMAppleListUpdate)

typedef AMAppleListUpdate ImActorModelMvvmAppleListUpdate;

#endif // _AMAppleListUpdate_H_
