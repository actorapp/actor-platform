//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/network/mtp/entity/ProtoSerializer.java
//

#ifndef _MTProtoSerializer_H_
#define _MTProtoSerializer_H_

#include "J2ObjC_header.h"

@class BSDataInput;
@class IOSByteArray;
@class MTProtoStruct;
@class MTPush;

@interface MTProtoSerializer : NSObject

#pragma mark Public

- (instancetype)init;

+ (MTProtoStruct *)readMessagePayloadWithByteArray:(IOSByteArray *)bs;

+ (MTProtoStruct *)readMessagePayloadWithBSDataInput:(BSDataInput *)bs;

+ (MTProtoStruct *)readRpcRequestPayloadWithBSDataInput:(BSDataInput *)bs;

+ (MTProtoStruct *)readRpcResponsePayloadWithByteArray:(IOSByteArray *)data;

+ (MTPush *)readUpdateWithByteArray:(IOSByteArray *)bs;

+ (MTPush *)readUpdateWithBSDataInput:(BSDataInput *)bs;

@end

J2OBJC_EMPTY_STATIC_INIT(MTProtoSerializer)

FOUNDATION_EXPORT MTProtoStruct *MTProtoSerializer_readMessagePayloadWithByteArray_(IOSByteArray *bs);

FOUNDATION_EXPORT MTProtoStruct *MTProtoSerializer_readMessagePayloadWithBSDataInput_(BSDataInput *bs);

FOUNDATION_EXPORT MTProtoStruct *MTProtoSerializer_readRpcResponsePayloadWithByteArray_(IOSByteArray *data);

FOUNDATION_EXPORT MTProtoStruct *MTProtoSerializer_readRpcRequestPayloadWithBSDataInput_(BSDataInput *bs);

FOUNDATION_EXPORT MTPush *MTProtoSerializer_readUpdateWithByteArray_(IOSByteArray *bs);

FOUNDATION_EXPORT MTPush *MTProtoSerializer_readUpdateWithBSDataInput_(BSDataInput *bs);

FOUNDATION_EXPORT void MTProtoSerializer_init(MTProtoSerializer *self);

FOUNDATION_EXPORT MTProtoSerializer *new_MTProtoSerializer_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(MTProtoSerializer)

typedef MTProtoSerializer ImActorModelNetworkMtpEntityProtoSerializer;

#endif // _MTProtoSerializer_H_
