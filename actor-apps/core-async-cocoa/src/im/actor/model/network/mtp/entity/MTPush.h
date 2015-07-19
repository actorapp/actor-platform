//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/network/mtp/entity/MTPush.java
//

#ifndef _MTMTPush_H_
#define _MTMTPush_H_

#include "J2ObjC_header.h"
#include "im/actor/model/network/mtp/entity/ProtoStruct.h"

@class BSDataInput;
@class BSDataOutput;
@class IOSByteArray;

#define MTMTPush_HEADER 5

@interface MTMTPush : MTProtoStruct

#pragma mark Public

- (instancetype)initWithBSDataInput:(BSDataInput *)stream;

- (IOSByteArray *)getPayload;

- (NSString *)description;

#pragma mark Protected

- (jbyte)getHeader;

- (void)readBodyWithBSDataInput:(BSDataInput *)bs;

- (void)writeBodyWithBSDataOutput:(BSDataOutput *)bs;

@end

J2OBJC_EMPTY_STATIC_INIT(MTMTPush)

J2OBJC_STATIC_FIELD_GETTER(MTMTPush, HEADER, jbyte)

FOUNDATION_EXPORT void MTMTPush_initWithBSDataInput_(MTMTPush *self, BSDataInput *stream);

FOUNDATION_EXPORT MTMTPush *new_MTMTPush_initWithBSDataInput_(BSDataInput *stream) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(MTMTPush)

typedef MTMTPush ImActorModelNetworkMtpEntityMTPush;

#endif // _MTMTPush_H_
