//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/storage/ListEngine.java
//

#ifndef _AMListEngine_H_
#define _AMListEngine_H_

@class IOSLongArray;
@protocol AMListEngineItem;
@protocol JavaUtilList;

#include "J2ObjC_header.h"

@protocol AMListEngine < NSObject, JavaObject >

- (void)addOrUpdateItemWithAMListEngineItem:(id<AMListEngineItem>)item;

- (void)addOrUpdateItemsWithJavaUtilList:(id<JavaUtilList>)values;

- (void)replaceItemsWithJavaUtilList:(id<JavaUtilList>)values;

- (void)removeItemWithLong:(jlong)id_;

- (void)removeItemsWithLongArray:(IOSLongArray *)ids;

- (void)clear;

- (id)getValueWithLong:(jlong)id_;

- (id)getHeadValue;

- (jint)getCount;

@end

J2OBJC_EMPTY_STATIC_INIT(AMListEngine)

#define ImActorModelStorageListEngine AMListEngine

J2OBJC_TYPE_LITERAL_HEADER(AMListEngine)

#endif // _AMListEngine_H_
