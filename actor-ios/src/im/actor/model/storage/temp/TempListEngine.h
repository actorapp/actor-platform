//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/storage/temp/TempListEngine.java
//

#ifndef _ImActorModelStorageTempTempListEngine_H_
#define _ImActorModelStorageTempTempListEngine_H_

@class IOSLongArray;
@class JavaUtilArrayList;
@class JavaUtilHashMap;
@protocol AMListEngineItem;
@protocol ImActorModelStorageTempTempListEngine_EngineListener;
@protocol JavaUtilList;

#include "J2ObjC_header.h"
#include "im/actor/model/storage/ListEngine.h"
#include "java/util/Comparator.h"

@interface ImActorModelStorageTempTempListEngine : NSObject < AMListEngine > {
}

- (instancetype)init;

- (void)addListenerWithImActorModelStorageTempTempListEngine_EngineListener:(id<ImActorModelStorageTempTempListEngine_EngineListener>)l;

- (void)removeListenerWithImActorModelStorageTempTempListEngine_EngineListener:(id<ImActorModelStorageTempTempListEngine_EngineListener>)l;

- (JavaUtilArrayList *)getList;

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

J2OBJC_EMPTY_STATIC_INIT(ImActorModelStorageTempTempListEngine)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelStorageTempTempListEngine)

@protocol ImActorModelStorageTempTempListEngine_EngineListener < NSObject, JavaObject >

- (void)onItemsChanged;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelStorageTempTempListEngine_EngineListener)

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelStorageTempTempListEngine_EngineListener)

@interface ImActorModelStorageTempTempListEngine_$1 : NSObject < JavaUtilComparator > {
}

- (jint)compareWithId:(id<AMListEngineItem>)o1
               withId:(id<AMListEngineItem>)o2;

- (jint)compareWithLong:(jlong)x
               withLong:(jlong)y;

- (instancetype)init;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelStorageTempTempListEngine_$1)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelStorageTempTempListEngine_$1)

#endif // _ImActorModelStorageTempTempListEngine_H_
