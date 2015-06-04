//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestClearChat.java
//

#ifndef _APRequestClearChat_H_
#define _APRequestClearChat_H_

#include "J2ObjC_header.h"
#include "im/actor/model/network/parser/Request.h"

@class APOutPeer;
@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;

#define APRequestClearChat_HEADER 99

@interface APRequestClearChat : APRequest

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithAPOutPeer:(APOutPeer *)peer;

+ (APRequestClearChat *)fromBytesWithByteArray:(IOSByteArray *)data;

- (jint)getHeaderKey;

- (APOutPeer *)getPeer;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(APRequestClearChat)

J2OBJC_STATIC_FIELD_GETTER(APRequestClearChat, HEADER, jint)

FOUNDATION_EXPORT APRequestClearChat *APRequestClearChat_fromBytesWithByteArray_(IOSByteArray *data);

FOUNDATION_EXPORT void APRequestClearChat_initWithAPOutPeer_(APRequestClearChat *self, APOutPeer *peer);

FOUNDATION_EXPORT APRequestClearChat *new_APRequestClearChat_initWithAPOutPeer_(APOutPeer *peer) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void APRequestClearChat_init(APRequestClearChat *self);

FOUNDATION_EXPORT APRequestClearChat *new_APRequestClearChat_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(APRequestClearChat)

typedef APRequestClearChat ImActorModelApiRpcRequestClearChat;

#endif // _APRequestClearChat_H_
