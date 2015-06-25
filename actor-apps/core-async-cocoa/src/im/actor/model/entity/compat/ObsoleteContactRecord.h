//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/entity/compat/ObsoleteContactRecord.java
//

#ifndef _ImActorModelEntityCompatObsoleteContactRecord_H_
#define _ImActorModelEntityCompatObsoleteContactRecord_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"

@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;

@interface ImActorModelEntityCompatObsoleteContactRecord : BSBserObject

#pragma mark Public

- (instancetype)initWithBSBserValues:(BSBserValues *)values;

- (instancetype)initWithByteArray:(IOSByteArray *)data;

- (jlong)getAccessHash;

- (jint)getId;

- (NSString *)getRecordData;

- (NSString *)getRecordTitle;

- (jint)getRecordType;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelEntityCompatObsoleteContactRecord)

FOUNDATION_EXPORT void ImActorModelEntityCompatObsoleteContactRecord_initWithByteArray_(ImActorModelEntityCompatObsoleteContactRecord *self, IOSByteArray *data);

FOUNDATION_EXPORT ImActorModelEntityCompatObsoleteContactRecord *new_ImActorModelEntityCompatObsoleteContactRecord_initWithByteArray_(IOSByteArray *data) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void ImActorModelEntityCompatObsoleteContactRecord_initWithBSBserValues_(ImActorModelEntityCompatObsoleteContactRecord *self, BSBserValues *values);

FOUNDATION_EXPORT ImActorModelEntityCompatObsoleteContactRecord *new_ImActorModelEntityCompatObsoleteContactRecord_initWithBSBserValues_(BSBserValues *values) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelEntityCompatObsoleteContactRecord)

#endif // _ImActorModelEntityCompatObsoleteContactRecord_H_
