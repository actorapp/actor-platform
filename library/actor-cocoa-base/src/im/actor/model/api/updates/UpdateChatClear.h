//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/updates/UpdateChatClear.java
//

#ifndef _ImActorModelApiUpdatesUpdateChatClear_H_
#define _ImActorModelApiUpdatesUpdateChatClear_H_

#include "J2ObjC_header.h"
#include "im/actor/model/network/parser/Update.h"

@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;
@class ImActorModelApiPeer;

#define ImActorModelApiUpdatesUpdateChatClear_HEADER 47

@interface ImActorModelApiUpdatesUpdateChatClear : ImActorModelNetworkParserUpdate

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithImActorModelApiPeer:(ImActorModelApiPeer *)peer;

+ (ImActorModelApiUpdatesUpdateChatClear *)fromBytesWithByteArray:(IOSByteArray *)data;

- (jint)getHeaderKey;

- (ImActorModelApiPeer *)getPeer;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelApiUpdatesUpdateChatClear)

J2OBJC_STATIC_FIELD_GETTER(ImActorModelApiUpdatesUpdateChatClear, HEADER, jint)

FOUNDATION_EXPORT ImActorModelApiUpdatesUpdateChatClear *ImActorModelApiUpdatesUpdateChatClear_fromBytesWithByteArray_(IOSByteArray *data);

FOUNDATION_EXPORT void ImActorModelApiUpdatesUpdateChatClear_initWithImActorModelApiPeer_(ImActorModelApiUpdatesUpdateChatClear *self, ImActorModelApiPeer *peer);

FOUNDATION_EXPORT ImActorModelApiUpdatesUpdateChatClear *new_ImActorModelApiUpdatesUpdateChatClear_initWithImActorModelApiPeer_(ImActorModelApiPeer *peer) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void ImActorModelApiUpdatesUpdateChatClear_init(ImActorModelApiUpdatesUpdateChatClear *self);

FOUNDATION_EXPORT ImActorModelApiUpdatesUpdateChatClear *new_ImActorModelApiUpdatesUpdateChatClear_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelApiUpdatesUpdateChatClear)

#endif // _ImActorModelApiUpdatesUpdateChatClear_H_
