//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/OutPeer.java
//

#ifndef _ImActorModelApiOutPeer_H_
#define _ImActorModelApiOutPeer_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"

@class BSBserValues;
@class BSBserWriter;
@class ImActorModelApiPeerTypeEnum;

@interface ImActorModelApiOutPeer : BSBserObject

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithImActorModelApiPeerTypeEnum:(ImActorModelApiPeerTypeEnum *)type
                                            withInt:(jint)id_
                                           withLong:(jlong)accessHash;

- (jlong)getAccessHash;

- (jint)getId;

- (ImActorModelApiPeerTypeEnum *)getType;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelApiOutPeer)

FOUNDATION_EXPORT void ImActorModelApiOutPeer_initWithImActorModelApiPeerTypeEnum_withInt_withLong_(ImActorModelApiOutPeer *self, ImActorModelApiPeerTypeEnum *type, jint id_, jlong accessHash);

FOUNDATION_EXPORT ImActorModelApiOutPeer *new_ImActorModelApiOutPeer_initWithImActorModelApiPeerTypeEnum_withInt_withLong_(ImActorModelApiPeerTypeEnum *type, jint id_, jlong accessHash) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void ImActorModelApiOutPeer_init(ImActorModelApiOutPeer *self);

FOUNDATION_EXPORT ImActorModelApiOutPeer *new_ImActorModelApiOutPeer_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelApiOutPeer)

#endif // _ImActorModelApiOutPeer_H_
