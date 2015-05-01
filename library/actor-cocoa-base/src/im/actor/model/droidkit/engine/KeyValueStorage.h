//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/droidkit/engine/KeyValueStorage.java
//

#ifndef _DKKeyValueStorage_H_
#define _DKKeyValueStorage_H_

#include "J2ObjC_header.h"

@class IOSByteArray;
@class IOSLongArray;
@protocol JavaUtilList;

@protocol DKKeyValueStorage < NSObject, JavaObject >

- (void)addOrUpdateItemWithLong:(jlong)id_
                  withByteArray:(IOSByteArray *)data;

- (void)addOrUpdateItemsWithJavaUtilList:(id<JavaUtilList>)values;

- (void)removeItemWithLong:(jlong)id_;

- (void)removeItemsWithLongArray:(IOSLongArray *)ids;

- (void)clear;

- (IOSByteArray *)getValueWithLong:(jlong)id_;

@end

J2OBJC_EMPTY_STATIC_INIT(DKKeyValueStorage)

J2OBJC_TYPE_LITERAL_HEADER(DKKeyValueStorage)

#define ImActorModelDroidkitEngineKeyValueStorage DKKeyValueStorage

#endif // _DKKeyValueStorage_H_
