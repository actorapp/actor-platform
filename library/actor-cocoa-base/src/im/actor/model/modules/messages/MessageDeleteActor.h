//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/messages/MessageDeleteActor.java
//

#ifndef _ImActorModelModulesMessagesMessageDeleteActor_H_
#define _ImActorModelModulesMessagesMessageDeleteActor_H_

@class AMPeer;
@class AMRpcException;
@class DKSyncKeyValue;
@class IOSLongArray;
@class ImActorModelApiPeer;
@class ImActorModelApiRpcResponseSeq;
@class ImActorModelModulesMessagesEntityDeleteStorage;
@class ImActorModelModulesModules;
@protocol JavaUtilList;

#include "J2ObjC_header.h"
#include "im/actor/model/modules/utils/ModuleActor.h"
#include "im/actor/model/network/RpcCallback.h"

@interface ImActorModelModulesMessagesMessageDeleteActor : ImActorModelModulesUtilsModuleActor {
}

- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules;

- (void)preStart;

- (void)saveStorage;

- (void)performDeleteWithAMPeer:(AMPeer *)peer
               withJavaUtilList:(id<JavaUtilList>)rids;

- (void)onDeleteMessageWithAMPeer:(AMPeer *)peer
                 withJavaUtilList:(id<JavaUtilList>)rids;

- (void)onReceiveWithId:(id)message;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesMessagesMessageDeleteActor)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesMessagesMessageDeleteActor)

@interface ImActorModelModulesMessagesMessageDeleteActor_DeleteMessage : NSObject {
}

- (instancetype)initWithAMPeer:(AMPeer *)peer
                 withLongArray:(IOSLongArray *)rids;

- (AMPeer *)getPeer;

- (IOSLongArray *)getRids;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesMessagesMessageDeleteActor_DeleteMessage)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesMessagesMessageDeleteActor_DeleteMessage)

@interface ImActorModelModulesMessagesMessageDeleteActor_$1 : NSObject < AMRpcCallback > {
}

- (void)onResultWithImActorModelNetworkParserResponse:(ImActorModelApiRpcResponseSeq *)response;

- (void)onErrorWithAMRpcException:(AMRpcException *)e;

- (instancetype)initWithImActorModelModulesMessagesMessageDeleteActor:(ImActorModelModulesMessagesMessageDeleteActor *)outer$
                                                           withAMPeer:(AMPeer *)capture$0
                                                     withJavaUtilList:(id<JavaUtilList>)capture$1
                                              withImActorModelApiPeer:(ImActorModelApiPeer *)capture$2;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesMessagesMessageDeleteActor_$1)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesMessagesMessageDeleteActor_$1)

#endif // _ImActorModelModulesMessagesMessageDeleteActor_H_
