//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/api/Message.java
//

#ifndef _APMessage_H_
#define _APMessage_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"

@class IOSByteArray;

@interface APMessage : BSBserObject

#pragma mark Public

- (instancetype)init;

- (IOSByteArray *)buildContainer;

+ (APMessage *)fromBytesWithByteArray:(IOSByteArray *)src;

- (jint)getHeader;

@end

J2OBJC_EMPTY_STATIC_INIT(APMessage)

FOUNDATION_EXPORT APMessage *APMessage_fromBytesWithByteArray_(IOSByteArray *src);

FOUNDATION_EXPORT void APMessage_init(APMessage *self);

J2OBJC_TYPE_LITERAL_HEADER(APMessage)

typedef APMessage ImActorModelApiMessage;

#endif // _APMessage_H_
