//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/network/RpcTimeoutException.java
//

#ifndef _AMRpcTimeoutException_H_
#define _AMRpcTimeoutException_H_

#include "J2ObjC_header.h"
#include "im/actor/model/network/RpcException.h"

@interface AMRpcTimeoutException : AMRpcException

#pragma mark Public

- (instancetype)init;

@end

J2OBJC_EMPTY_STATIC_INIT(AMRpcTimeoutException)

FOUNDATION_EXPORT void AMRpcTimeoutException_init(AMRpcTimeoutException *self);

FOUNDATION_EXPORT AMRpcTimeoutException *new_AMRpcTimeoutException_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMRpcTimeoutException)

typedef AMRpcTimeoutException ImActorModelNetworkRpcTimeoutException;

#endif // _AMRpcTimeoutException_H_
