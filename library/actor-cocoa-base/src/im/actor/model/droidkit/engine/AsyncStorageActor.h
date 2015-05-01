//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/droidkit/engine/AsyncStorageActor.java
//

#ifndef _DKAsyncStorageActor_H_
#define _DKAsyncStorageActor_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/actors/Actor.h"

@class BSBserObject;
@class IOSLongArray;
@class JavaLangLong;
@protocol BSBserCreator;
@protocol DKAsyncStorageActor_LoadCountCallback;
@protocol DKAsyncStorageActor_LoadItemCallback;
@protocol DKListEngineDisplayLoadCallback;
@protocol DKListEngineItem;
@protocol DKListStorageDisplayEx;
@protocol JavaUtilList;

@interface DKAsyncStorageActor : DKActor

#pragma mark Public

- (instancetype)initWithDKListStorageDisplayEx:(id<DKListStorageDisplayEx>)storage
                             withBSBserCreator:(id<BSBserCreator>)creator;

- (void)addOrUpdateWithJavaUtilList:(id<JavaUtilList>)items;

- (void)clear;

- (void)loadBackwardWithNSString:(NSString *)query
                withJavaLangLong:(JavaLangLong *)topSortKey
                         withInt:(jint)limit
withDKListEngineDisplayLoadCallback:(id<DKListEngineDisplayLoadCallback>)callback;

- (void)loadCountWithDKAsyncStorageActor_LoadCountCallback:(id<DKAsyncStorageActor_LoadCountCallback>)callback;

- (void)loadForwardWithNSString:(NSString *)query
               withJavaLangLong:(JavaLangLong *)topSortKey
                        withInt:(jint)limit
withDKListEngineDisplayLoadCallback:(id<DKListEngineDisplayLoadCallback>)callback;

- (void)loadHeadWithDKAsyncStorageActor_LoadItemCallback:(id<DKAsyncStorageActor_LoadItemCallback>)callback;

- (void)loadItemWithLong:(jlong)key
withDKAsyncStorageActor_LoadItemCallback:(id<DKAsyncStorageActor_LoadItemCallback>)callback;

- (void)onReceiveWithId:(id)message;

- (void)removeWithLongArray:(IOSLongArray *)keys;

- (void)replaceWithJavaUtilList:(id<JavaUtilList>)items;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor)

FOUNDATION_EXPORT void DKAsyncStorageActor_initWithDKListStorageDisplayEx_withBSBserCreator_(DKAsyncStorageActor *self, id<DKListStorageDisplayEx> storage, id<BSBserCreator> creator);

FOUNDATION_EXPORT DKAsyncStorageActor *new_DKAsyncStorageActor_initWithDKListStorageDisplayEx_withBSBserCreator_(id<DKListStorageDisplayEx> storage, id<BSBserCreator> creator) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor)

typedef DKAsyncStorageActor ImActorModelDroidkitEngineAsyncStorageActor;

@interface DKAsyncStorageActor_AddOrUpdate : NSObject

#pragma mark Public

- (instancetype)initWithJavaUtilList:(id<JavaUtilList>)items;

- (id<JavaUtilList>)getItems;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_AddOrUpdate)

FOUNDATION_EXPORT void DKAsyncStorageActor_AddOrUpdate_initWithJavaUtilList_(DKAsyncStorageActor_AddOrUpdate *self, id<JavaUtilList> items);

FOUNDATION_EXPORT DKAsyncStorageActor_AddOrUpdate *new_DKAsyncStorageActor_AddOrUpdate_initWithJavaUtilList_(id<JavaUtilList> items) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_AddOrUpdate)

@interface DKAsyncStorageActor_Replace : NSObject

#pragma mark Public

- (instancetype)initWithJavaUtilList:(id<JavaUtilList>)items;

- (id<JavaUtilList>)getItems;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_Replace)

FOUNDATION_EXPORT void DKAsyncStorageActor_Replace_initWithJavaUtilList_(DKAsyncStorageActor_Replace *self, id<JavaUtilList> items);

FOUNDATION_EXPORT DKAsyncStorageActor_Replace *new_DKAsyncStorageActor_Replace_initWithJavaUtilList_(id<JavaUtilList> items) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_Replace)

@interface DKAsyncStorageActor_Remove : NSObject

#pragma mark Public

- (instancetype)initWithLongArray:(IOSLongArray *)keys;

- (IOSLongArray *)getKeys;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_Remove)

FOUNDATION_EXPORT void DKAsyncStorageActor_Remove_initWithLongArray_(DKAsyncStorageActor_Remove *self, IOSLongArray *keys);

FOUNDATION_EXPORT DKAsyncStorageActor_Remove *new_DKAsyncStorageActor_Remove_initWithLongArray_(IOSLongArray *keys) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_Remove)

@interface DKAsyncStorageActor_Clear : NSObject

#pragma mark Public

- (instancetype)init;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_Clear)

FOUNDATION_EXPORT void DKAsyncStorageActor_Clear_init(DKAsyncStorageActor_Clear *self);

FOUNDATION_EXPORT DKAsyncStorageActor_Clear *new_DKAsyncStorageActor_Clear_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_Clear)

@interface DKAsyncStorageActor_LoadItem : NSObject

#pragma mark Public

- (instancetype)initWithLong:(jlong)key
withDKAsyncStorageActor_LoadItemCallback:(id<DKAsyncStorageActor_LoadItemCallback>)callback;

- (id<DKAsyncStorageActor_LoadItemCallback>)getCallback;

- (jlong)getKey;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_LoadItem)

FOUNDATION_EXPORT void DKAsyncStorageActor_LoadItem_initWithLong_withDKAsyncStorageActor_LoadItemCallback_(DKAsyncStorageActor_LoadItem *self, jlong key, id<DKAsyncStorageActor_LoadItemCallback> callback);

FOUNDATION_EXPORT DKAsyncStorageActor_LoadItem *new_DKAsyncStorageActor_LoadItem_initWithLong_withDKAsyncStorageActor_LoadItemCallback_(jlong key, id<DKAsyncStorageActor_LoadItemCallback> callback) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_LoadItem)

@interface DKAsyncStorageActor_LoadCount : NSObject

#pragma mark Public

- (instancetype)initWithDKAsyncStorageActor_LoadCountCallback:(id<DKAsyncStorageActor_LoadCountCallback>)callback;

- (id<DKAsyncStorageActor_LoadCountCallback>)getCallback;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_LoadCount)

FOUNDATION_EXPORT void DKAsyncStorageActor_LoadCount_initWithDKAsyncStorageActor_LoadCountCallback_(DKAsyncStorageActor_LoadCount *self, id<DKAsyncStorageActor_LoadCountCallback> callback);

FOUNDATION_EXPORT DKAsyncStorageActor_LoadCount *new_DKAsyncStorageActor_LoadCount_initWithDKAsyncStorageActor_LoadCountCallback_(id<DKAsyncStorageActor_LoadCountCallback> callback) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_LoadCount)

@interface DKAsyncStorageActor_LoadHead : NSObject

#pragma mark Public

- (instancetype)initWithDKAsyncStorageActor_LoadItemCallback:(id<DKAsyncStorageActor_LoadItemCallback>)callback;

- (id<DKAsyncStorageActor_LoadItemCallback>)getCallback;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_LoadHead)

FOUNDATION_EXPORT void DKAsyncStorageActor_LoadHead_initWithDKAsyncStorageActor_LoadItemCallback_(DKAsyncStorageActor_LoadHead *self, id<DKAsyncStorageActor_LoadItemCallback> callback);

FOUNDATION_EXPORT DKAsyncStorageActor_LoadHead *new_DKAsyncStorageActor_LoadHead_initWithDKAsyncStorageActor_LoadItemCallback_(id<DKAsyncStorageActor_LoadItemCallback> callback) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_LoadHead)

@interface DKAsyncStorageActor_LoadForward : NSObject

#pragma mark Public

- (instancetype)initWithNSString:(NSString *)query
                withJavaLangLong:(JavaLangLong *)topSortKey
                         withInt:(jint)limit
withDKListEngineDisplayLoadCallback:(id<DKListEngineDisplayLoadCallback>)callback;

- (id<DKListEngineDisplayLoadCallback>)getCallback;

- (jint)getLimit;

- (NSString *)getQuery;

- (JavaLangLong *)getTopSortKey;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_LoadForward)

FOUNDATION_EXPORT void DKAsyncStorageActor_LoadForward_initWithNSString_withJavaLangLong_withInt_withDKListEngineDisplayLoadCallback_(DKAsyncStorageActor_LoadForward *self, NSString *query, JavaLangLong *topSortKey, jint limit, id<DKListEngineDisplayLoadCallback> callback);

FOUNDATION_EXPORT DKAsyncStorageActor_LoadForward *new_DKAsyncStorageActor_LoadForward_initWithNSString_withJavaLangLong_withInt_withDKListEngineDisplayLoadCallback_(NSString *query, JavaLangLong *topSortKey, jint limit, id<DKListEngineDisplayLoadCallback> callback) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_LoadForward)

@interface DKAsyncStorageActor_LoadBackward : NSObject

#pragma mark Public

- (instancetype)initWithNSString:(NSString *)query
                withJavaLangLong:(JavaLangLong *)topSortKey
                         withInt:(jint)limit
withDKListEngineDisplayLoadCallback:(id<DKListEngineDisplayLoadCallback>)callback;

- (id<DKListEngineDisplayLoadCallback>)getCallback;

- (jint)getLimit;

- (NSString *)getQuery;

- (JavaLangLong *)getTopSortKey;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_LoadBackward)

FOUNDATION_EXPORT void DKAsyncStorageActor_LoadBackward_initWithNSString_withJavaLangLong_withInt_withDKListEngineDisplayLoadCallback_(DKAsyncStorageActor_LoadBackward *self, NSString *query, JavaLangLong *topSortKey, jint limit, id<DKListEngineDisplayLoadCallback> callback);

FOUNDATION_EXPORT DKAsyncStorageActor_LoadBackward *new_DKAsyncStorageActor_LoadBackward_initWithNSString_withJavaLangLong_withInt_withDKListEngineDisplayLoadCallback_(NSString *query, JavaLangLong *topSortKey, jint limit, id<DKListEngineDisplayLoadCallback> callback) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_LoadBackward)

@protocol DKAsyncStorageActor_LoadItemCallback < NSObject, JavaObject >

- (void)onLoadedWithBSBserObject:(BSBserObject<DKListEngineItem> *)item;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_LoadItemCallback)

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_LoadItemCallback)

@protocol DKAsyncStorageActor_LoadCountCallback < NSObject, JavaObject >

- (void)onLoadedWithInt:(jint)count;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageActor_LoadCountCallback)

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageActor_LoadCountCallback)

#endif // _DKAsyncStorageActor_H_
