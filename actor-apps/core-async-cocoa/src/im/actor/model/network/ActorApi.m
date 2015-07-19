//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/network/ActorApi.java
//


#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/NetworkProvider.h"
#include "im/actor/model/api/ApiVersion.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/actors/Environment.h"
#include "im/actor/model/network/ActorApi.h"
#include "im/actor/model/network/ActorApiCallback.h"
#include "im/actor/model/network/AuthKeyStorage.h"
#include "im/actor/model/network/Endpoints.h"
#include "im/actor/model/network/NetworkState.h"
#include "im/actor/model/network/RpcCallback.h"
#include "im/actor/model/network/api/ApiBroker.h"
#include "im/actor/model/network/parser/Request.h"
#include "im/actor/model/util/AtomicIntegerCompat.h"
#include "java/lang/RuntimeException.h"

@interface AMActorApi () {
 @public
  AMEndpoints *endpoints_;
  id<AMAuthKeyStorage> keyStorage_;
  id<AMActorApiCallback> callback_;
  id<AMNetworkProvider> networkProvider_;
  jboolean isEnableLog_;
  jint minDelay_;
  jint maxDelay_;
  jint maxFailureCount_;
  DKActorRef *apiBroker_;
}

@end

J2OBJC_FIELD_SETTER(AMActorApi, endpoints_, AMEndpoints *)
J2OBJC_FIELD_SETTER(AMActorApi, keyStorage_, id<AMAuthKeyStorage>)
J2OBJC_FIELD_SETTER(AMActorApi, callback_, id<AMActorApiCallback>)
J2OBJC_FIELD_SETTER(AMActorApi, networkProvider_, id<AMNetworkProvider>)
J2OBJC_FIELD_SETTER(AMActorApi, apiBroker_, DKActorRef *)

static AMAtomicIntegerCompat *AMActorApi_NEXT_ID_;
J2OBJC_STATIC_FIELD_GETTER(AMActorApi, NEXT_ID_, AMAtomicIntegerCompat *)

J2OBJC_INITIALIZED_DEFN(AMActorApi)

@implementation AMActorApi

- (instancetype)initWithAMEndpoints:(AMEndpoints *)endpoints
               withAMAuthKeyStorage:(id<AMAuthKeyStorage>)keyStorage
             withAMActorApiCallback:(id<AMActorApiCallback>)callback
              withAMNetworkProvider:(id<AMNetworkProvider>)networkProvider
                        withBoolean:(jboolean)isEnableLog
                            withInt:(jint)minDelay
                            withInt:(jint)maxDelay
                            withInt:(jint)maxFailureCount {
  AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_withBoolean_withInt_withInt_withInt_(self, endpoints, keyStorage, callback, networkProvider, isEnableLog, minDelay, maxDelay, maxFailureCount);
  return self;
}

- (void)requestWithAPRequest:(APRequest *)request
           withAMRpcCallback:(id<AMRpcCallback>)callback {
  @synchronized(self) {
    if (request == nil) {
      @throw new_JavaLangRuntimeException_initWithNSString_(@"Request can't be null");
    }
    (void) [((APRequest *) nil_chk(request)) toByteArray];
    [((DKActorRef *) nil_chk(self->apiBroker_)) sendWithId:new_ImActorModelNetworkApiApiBroker_PerformRequest_initWithAPRequest_withAMRpcCallback_(request, callback)];
  }
}

- (void)onNetworkChangedWithAMNetworkStateEnum:(AMNetworkStateEnum *)state {
  @synchronized(self) {
    [((DKActorRef *) nil_chk(self->apiBroker_)) sendWithId:new_ImActorModelNetworkApiApiBroker_NetworkChanged_initWithAMNetworkStateEnum_(state)];
  }
}

- (void)forceNetworkCheck {
  @synchronized(self) {
    [((DKActorRef *) nil_chk(self->apiBroker_)) sendWithId:new_ImActorModelNetworkApiApiBroker_ForceNetworkCheck_init()];
  }
}

+ (void)initialize {
  if (self == [AMActorApi class]) {
    AMActorApi_NEXT_ID_ = DKEnvironment_createAtomicIntWithInt_(1);
    J2OBJC_SET_INITIALIZED(AMActorApi)
  }
}

@end

void AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_withBoolean_withInt_withInt_withInt_(AMActorApi *self, AMEndpoints *endpoints, id<AMAuthKeyStorage> keyStorage, id<AMActorApiCallback> callback, id<AMNetworkProvider> networkProvider, jboolean isEnableLog, jint minDelay, jint maxDelay, jint maxFailureCount) {
  (void) NSObject_init(self);
  self->endpoints_ = endpoints;
  self->keyStorage_ = keyStorage;
  self->callback_ = callback;
  self->networkProvider_ = networkProvider;
  self->isEnableLog_ = isEnableLog;
  self->minDelay_ = minDelay;
  self->maxDelay_ = maxDelay;
  self->maxFailureCount_ = maxFailureCount;
  self->apiBroker_ = ImActorModelNetworkApiApiBroker_getWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_withBoolean_withInt_withInt_withInt_withInt_(endpoints, keyStorage, callback, networkProvider, isEnableLog, [((AMAtomicIntegerCompat *) nil_chk(AMActorApi_NEXT_ID_)) get], minDelay, maxDelay, maxFailureCount);
}

AMActorApi *new_AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_withBoolean_withInt_withInt_withInt_(AMEndpoints *endpoints, id<AMAuthKeyStorage> keyStorage, id<AMActorApiCallback> callback, id<AMNetworkProvider> networkProvider, jboolean isEnableLog, jint minDelay, jint maxDelay, jint maxFailureCount) {
  AMActorApi *self = [AMActorApi alloc];
  AMActorApi_initWithAMEndpoints_withAMAuthKeyStorage_withAMActorApiCallback_withAMNetworkProvider_withBoolean_withInt_withInt_withInt_(self, endpoints, keyStorage, callback, networkProvider, isEnableLog, minDelay, maxDelay, maxFailureCount);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMActorApi)
