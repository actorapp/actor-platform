//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/api/rpc/RequestEnableInterests.java
//

#ifndef _APRequestEnableInterests_H_
#define _APRequestEnableInterests_H_

#include "J2ObjC_header.h"
#include "im/actor/model/network/parser/Request.h"

@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;
@protocol JavaUtilList;

#define APRequestEnableInterests_HEADER 157

@interface APRequestEnableInterests : APRequest

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithJavaUtilList:(id<JavaUtilList>)interests;

+ (APRequestEnableInterests *)fromBytesWithByteArray:(IOSByteArray *)data;

- (jint)getHeaderKey;

- (id<JavaUtilList>)getInterests;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(APRequestEnableInterests)

J2OBJC_STATIC_FIELD_GETTER(APRequestEnableInterests, HEADER, jint)

FOUNDATION_EXPORT APRequestEnableInterests *APRequestEnableInterests_fromBytesWithByteArray_(IOSByteArray *data);

FOUNDATION_EXPORT void APRequestEnableInterests_initWithJavaUtilList_(APRequestEnableInterests *self, id<JavaUtilList> interests);

FOUNDATION_EXPORT APRequestEnableInterests *new_APRequestEnableInterests_initWithJavaUtilList_(id<JavaUtilList> interests) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void APRequestEnableInterests_init(APRequestEnableInterests *self);

FOUNDATION_EXPORT APRequestEnableInterests *new_APRequestEnableInterests_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(APRequestEnableInterests)

typedef APRequestEnableInterests ImActorModelApiRpcRequestEnableInterests;

#endif // _APRequestEnableInterests_H_
