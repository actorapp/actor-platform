//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/ActorApi.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/NetworkProvider.h"
#include "im/actor/model/api/ApiVersion.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/network/ActorApi.h"
#include "im/actor/model/network/ActorApiCallback.h"
#include "im/actor/model/network/AuthKeyStorage.h"
#include "im/actor/model/network/Endpoints.h"
#include "im/actor/model/network/RpcCallback.h"
#include "im/actor/model/network/api/ApiBroker.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/lang/RuntimeException.h"

@interface AMActorApi () {
 @public
  DKActorRef *apiBroker_;
}

@end

J2OBJC_FIELD_SETTER(AMActorApi, apiBroker_, DKActorRef *)

@implementation AMActorApi

- (instancetype)initWithAMEndpoints:(AMEndpoints *)endpoints
               withAMAuthKeyStorage:(id<AMAuthKeyStorage>)keyStorage
             withAMActorApiCallback:(id<AMActorApiCallback>)callback
              withAMNetworkProvider:(id<AMNetworkProvider>)networkProvider {
  AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_(self, endpoints, keyStorage, callback, networkProvider);
  return self;
}

- (void)requestWithImActorModelNetworkParserRequest:(ImActorModelNetworkParserRequest *)request
                                  withAMRpcCallback:(id<AMRpcCallback>)callback {
  if (request == nil) {
    @throw new_JavaLangRuntimeException_initWithNSString_(@"Request can't be null");
  }
  [((DKActorRef *) nil_chk(self->apiBroker_)) sendWithId:new_ImActorModelNetworkApiApiBroker_PerformRequest_initWithImActorModelNetworkParserRequest_withAMRpcCallback_(request, callback)];
}

- (void)onNetworkChanged {
  [((DKActorRef *) nil_chk(self->apiBroker_)) sendWithId:new_ImActorModelNetworkApiApiBroker_NetworkChanged_init()];
}

@end

void AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_(AMActorApi *self, AMEndpoints *endpoints, id<AMAuthKeyStorage> keyStorage, id<AMActorApiCallback> callback, id<AMNetworkProvider> networkProvider) {
  (void) NSObject_init(self);
  self->apiBroker_ = ImActorModelNetworkApiApiBroker_getWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_(endpoints, keyStorage, callback, networkProvider);
}

AMActorApi *new_AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_(AMEndpoints *endpoints, id<AMAuthKeyStorage> keyStorage, id<AMActorApiCallback> callback, id<AMNetworkProvider> networkProvider) {
  AMActorApi *self = [AMActorApi alloc];
  AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_(self, endpoints, keyStorage, callback, networkProvider);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMActorApi)
