//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/updates/UpdateMessageDelete.java
//

#ifndef _ImActorModelApiUpdatesUpdateMessageDelete_H_
#define _ImActorModelApiUpdatesUpdateMessageDelete_H_

#include "J2ObjC_header.h"
#include "im/actor/model/network/parser/Update.h"

@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;
@class ImActorModelApiPeer;
@protocol JavaUtilList;

#define ImActorModelApiUpdatesUpdateMessageDelete_HEADER 46

@interface ImActorModelApiUpdatesUpdateMessageDelete : ImActorModelNetworkParserUpdate

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithImActorModelApiPeer:(ImActorModelApiPeer *)peer
                           withJavaUtilList:(id<JavaUtilList>)rids;

+ (ImActorModelApiUpdatesUpdateMessageDelete *)fromBytesWithByteArray:(IOSByteArray *)data;

- (jint)getHeaderKey;

- (ImActorModelApiPeer *)getPeer;

- (id<JavaUtilList>)getRids;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelApiUpdatesUpdateMessageDelete)

J2OBJC_STATIC_FIELD_GETTER(ImActorModelApiUpdatesUpdateMessageDelete, HEADER, jint)

FOUNDATION_EXPORT ImActorModelApiUpdatesUpdateMessageDelete *ImActorModelApiUpdatesUpdateMessageDelete_fromBytesWithByteArray_(IOSByteArray *data);

FOUNDATION_EXPORT void ImActorModelApiUpdatesUpdateMessageDelete_initWithImActorModelApiPeer_withJavaUtilList_(ImActorModelApiUpdatesUpdateMessageDelete *self, ImActorModelApiPeer *peer, id<JavaUtilList> rids);

FOUNDATION_EXPORT ImActorModelApiUpdatesUpdateMessageDelete *new_ImActorModelApiUpdatesUpdateMessageDelete_initWithImActorModelApiPeer_withJavaUtilList_(ImActorModelApiPeer *peer, id<JavaUtilList> rids) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void ImActorModelApiUpdatesUpdateMessageDelete_init(ImActorModelApiUpdatesUpdateMessageDelete *self);

FOUNDATION_EXPORT ImActorModelApiUpdatesUpdateMessageDelete *new_ImActorModelApiUpdatesUpdateMessageDelete_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelApiUpdatesUpdateMessageDelete)

#endif // _ImActorModelApiUpdatesUpdateMessageDelete_H_
