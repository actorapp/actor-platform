//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/network/connection/ManagedConnection.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/concurrency/AbsTimerCompat.h"
#include "im/actor/model/crypto/CryptoUtils.h"
#include "im/actor/model/droidkit/actors/Environment.h"
#include "im/actor/model/droidkit/bser/DataInput.h"
#include "im/actor/model/droidkit/bser/DataOutput.h"
#include "im/actor/model/log/Log.h"
#include "im/actor/model/network/ConnectionCallback.h"
#include "im/actor/model/network/ConnectionEndpoint.h"
#include "im/actor/model/network/connection/AsyncConnection.h"
#include "im/actor/model/network/connection/AsyncConnectionFactory.h"
#include "im/actor/model/network/connection/AsyncConnectionInterface.h"
#include "im/actor/model/network/connection/ManagedConnection.h"
#include "im/actor/model/network/connection/ManagedConnectionCreateCallback.h"
#include "im/actor/model/util/CRC32.h"
#include "java/io/IOException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/lang/Runnable.h"
#include "java/util/Arrays.h"
#include "java/util/Collection.h"
#include "java/util/HashMap.h"
#include "java/util/Random.h"
#include "java/util/Set.h"

#define AMManagedConnection_HANDSHAKE_TIMEOUT 15000
#define AMManagedConnection_RESPONSE_TIMEOUT 15000
#define AMManagedConnection_PING_TIMEOUT 300000
#define AMManagedConnection_HEADER_PROTO 0
#define AMManagedConnection_HEADER_PING 1
#define AMManagedConnection_HEADER_PONG 2
#define AMManagedConnection_HEADER_DROP 3
#define AMManagedConnection_HEADER_REDIRECT 4
#define AMManagedConnection_HEADER_ACK 6
#define AMManagedConnection_HEADER_HANDSHAKE_REQUEST 255
#define AMManagedConnection_HEADER_HANDSHAKE_RESPONSE 254

@interface AMManagedConnection () {
 @public
  id<AMAsyncConnectionInterface> connectionInterface_;
  AMCRC32 *CRC32_ENGINE_;
  NSString *TAG_;
  AMAsyncConnection *rawConnection_;
  id<AMConnectionCallback> callback_;
  id<AMManagedConnectionCreateCallback> factoryCallback_;
  jint connectionId_;
  jint mtprotoVersion_;
  jint apiMajorVersion_;
  jint apiMinorVersion_;
  jint receivedPackages_;
  jint sentPackages_;
  jboolean isClosed__;
  jboolean isOpened_;
  jboolean isHandshakePerformed_;
  IOSByteArray *handshakeRandomData_;
  AMAbsTimerCompat *connectionTimeout_;
  AMAbsTimerCompat *handshakeTimeout_;
  AMAbsTimerCompat *pingTask_;
  JavaUtilHashMap *schedulledPings_;
  JavaUtilHashMap *packageTimers_;
}

- (void)sendHandshakeRequest;

- (void)onHandshakePackageWithByteArray:(IOSByteArray *)data;

- (void)onProtoPackageWithByteArray:(IOSByteArray *)data;

- (void)sendProtoPackageWithByteArray:(IOSByteArray *)data
                              withInt:(jint)offset
                              withInt:(jint)len;

- (void)onPingPackageWithByteArray:(IOSByteArray *)data;

- (void)onPongPackageWithByteArray:(IOSByteArray *)data;

- (void)sendPingMessage;

- (void)refreshTimeouts;

- (void)onAckPackageWithByteArray:(IOSByteArray *)data;

- (void)sendAckPackageWithInt:(jint)receivedIndex;

- (void)onDropPackageWithByteArray:(IOSByteArray *)data;

- (void)onRawConnected;

- (void)onRawReceivedWithByteArray:(IOSByteArray *)data;

- (void)onRawClosed;

- (void)rawPostWithInt:(jint)header
         withByteArray:(IOSByteArray *)data;

- (void)rawPostWithInt:(jint)header
         withByteArray:(IOSByteArray *)data
               withInt:(jint)offset
               withInt:(jint)len;

@end

J2OBJC_FIELD_SETTER(AMManagedConnection, connectionInterface_, id<AMAsyncConnectionInterface>)
J2OBJC_FIELD_SETTER(AMManagedConnection, CRC32_ENGINE_, AMCRC32 *)
J2OBJC_FIELD_SETTER(AMManagedConnection, TAG_, NSString *)
J2OBJC_FIELD_SETTER(AMManagedConnection, rawConnection_, AMAsyncConnection *)
J2OBJC_FIELD_SETTER(AMManagedConnection, callback_, id<AMConnectionCallback>)
J2OBJC_FIELD_SETTER(AMManagedConnection, factoryCallback_, id<AMManagedConnectionCreateCallback>)
J2OBJC_FIELD_SETTER(AMManagedConnection, handshakeRandomData_, IOSByteArray *)
J2OBJC_FIELD_SETTER(AMManagedConnection, connectionTimeout_, AMAbsTimerCompat *)
J2OBJC_FIELD_SETTER(AMManagedConnection, handshakeTimeout_, AMAbsTimerCompat *)
J2OBJC_FIELD_SETTER(AMManagedConnection, pingTask_, AMAbsTimerCompat *)
J2OBJC_FIELD_SETTER(AMManagedConnection, schedulledPings_, JavaUtilHashMap *)
J2OBJC_FIELD_SETTER(AMManagedConnection, packageTimers_, JavaUtilHashMap *)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HANDSHAKE_TIMEOUT, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, RESPONSE_TIMEOUT, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, PING_TIMEOUT, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_PROTO, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_PING, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_PONG, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_DROP, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_REDIRECT, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_ACK, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_HANDSHAKE_REQUEST, jint)

J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, HEADER_HANDSHAKE_RESPONSE, jint)

static JavaUtilRandom *AMManagedConnection_RANDOM_;
J2OBJC_STATIC_FIELD_GETTER(AMManagedConnection, RANDOM_, JavaUtilRandom *)

__attribute__((unused)) static void AMManagedConnection_sendHandshakeRequest(AMManagedConnection *self);

__attribute__((unused)) static void AMManagedConnection_onHandshakePackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_onProtoPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_sendProtoPackageWithByteArray_withInt_withInt_(AMManagedConnection *self, IOSByteArray *data, jint offset, jint len);

__attribute__((unused)) static void AMManagedConnection_onPingPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_onPongPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_sendPingMessage(AMManagedConnection *self);

__attribute__((unused)) static void AMManagedConnection_refreshTimeouts(AMManagedConnection *self);

__attribute__((unused)) static void AMManagedConnection_onAckPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_sendAckPackageWithInt_(AMManagedConnection *self, jint receivedIndex);

__attribute__((unused)) static void AMManagedConnection_onDropPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_onRawConnected(AMManagedConnection *self);

__attribute__((unused)) static void AMManagedConnection_onRawReceivedWithByteArray_(AMManagedConnection *self, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_onRawClosed(AMManagedConnection *self);

__attribute__((unused)) static void AMManagedConnection_rawPostWithInt_withByteArray_(AMManagedConnection *self, jint header, IOSByteArray *data);

__attribute__((unused)) static void AMManagedConnection_rawPostWithInt_withByteArray_withInt_withInt_(AMManagedConnection *self, jint header, IOSByteArray *data, jint offset, jint len);

@interface AMManagedConnection_ConnectionInterface : NSObject < AMAsyncConnectionInterface > {
 @public
  AMManagedConnection *this$0_;
}

- (void)onConnected;

- (void)onReceived:(IOSByteArray *)data;

- (void)onClosed;

- (instancetype)initWithAMManagedConnection:(AMManagedConnection *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(AMManagedConnection_ConnectionInterface)

J2OBJC_FIELD_SETTER(AMManagedConnection_ConnectionInterface, this$0_, AMManagedConnection *)

__attribute__((unused)) static void AMManagedConnection_ConnectionInterface_initWithAMManagedConnection_(AMManagedConnection_ConnectionInterface *self, AMManagedConnection *outer$);

__attribute__((unused)) static AMManagedConnection_ConnectionInterface *new_AMManagedConnection_ConnectionInterface_initWithAMManagedConnection_(AMManagedConnection *outer$) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMManagedConnection_ConnectionInterface)

@interface AMManagedConnection_PingRunnable : NSObject < JavaLangRunnable > {
 @public
  AMManagedConnection *this$0_;
}

- (void)run;

- (instancetype)initWithAMManagedConnection:(AMManagedConnection *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(AMManagedConnection_PingRunnable)

J2OBJC_FIELD_SETTER(AMManagedConnection_PingRunnable, this$0_, AMManagedConnection *)

__attribute__((unused)) static void AMManagedConnection_PingRunnable_initWithAMManagedConnection_(AMManagedConnection_PingRunnable *self, AMManagedConnection *outer$);

__attribute__((unused)) static AMManagedConnection_PingRunnable *new_AMManagedConnection_PingRunnable_initWithAMManagedConnection_(AMManagedConnection *outer$) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMManagedConnection_PingRunnable)

@interface AMManagedConnection_TimeoutRunnable : NSObject < JavaLangRunnable > {
 @public
  AMManagedConnection *this$0_;
}

- (void)run;

- (instancetype)initWithAMManagedConnection:(AMManagedConnection *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(AMManagedConnection_TimeoutRunnable)

J2OBJC_FIELD_SETTER(AMManagedConnection_TimeoutRunnable, this$0_, AMManagedConnection *)

__attribute__((unused)) static void AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(AMManagedConnection_TimeoutRunnable *self, AMManagedConnection *outer$);

__attribute__((unused)) static AMManagedConnection_TimeoutRunnable *new_AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(AMManagedConnection *outer$) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMManagedConnection_TimeoutRunnable)

J2OBJC_INITIALIZED_DEFN(AMManagedConnection)

@implementation AMManagedConnection

- (instancetype)initWithInt:(jint)connectionId
                    withInt:(jint)mtprotoVersion
                    withInt:(jint)apiMajorVersion
                    withInt:(jint)apiMinorVersion
   withAMConnectionEndpoint:(AMConnectionEndpoint *)endpoint
   withAMConnectionCallback:(id<AMConnectionCallback>)callback
withAMManagedConnectionCreateCallback:(id<AMManagedConnectionCreateCallback>)factoryCallback
withAMAsyncConnectionFactory:(id<AMAsyncConnectionFactory>)connectionFactory {
  AMManagedConnection_initWithInt_withInt_withInt_withInt_withAMConnectionEndpoint_withAMConnectionCallback_withAMManagedConnectionCreateCallback_withAMAsyncConnectionFactory_(self, connectionId, mtprotoVersion, apiMajorVersion, apiMinorVersion, endpoint, callback, factoryCallback, connectionFactory);
  return self;
}

- (void)sendHandshakeRequest {
  AMManagedConnection_sendHandshakeRequest(self);
}

- (void)onHandshakePackageWithByteArray:(IOSByteArray *)data {
  AMManagedConnection_onHandshakePackageWithByteArray_(self, data);
}

- (void)onProtoPackageWithByteArray:(IOSByteArray *)data {
  AMManagedConnection_onProtoPackageWithByteArray_(self, data);
}

- (void)sendProtoPackageWithByteArray:(IOSByteArray *)data
                              withInt:(jint)offset
                              withInt:(jint)len {
  AMManagedConnection_sendProtoPackageWithByteArray_withInt_withInt_(self, data, offset, len);
}

- (void)onPingPackageWithByteArray:(IOSByteArray *)data {
  AMManagedConnection_onPingPackageWithByteArray_(self, data);
}

- (void)onPongPackageWithByteArray:(IOSByteArray *)data {
  AMManagedConnection_onPongPackageWithByteArray_(self, data);
}

- (void)sendPingMessage {
  AMManagedConnection_sendPingMessage(self);
}

- (void)refreshTimeouts {
  AMManagedConnection_refreshTimeouts(self);
}

- (void)onAckPackageWithByteArray:(IOSByteArray *)data {
  AMManagedConnection_onAckPackageWithByteArray_(self, data);
}

- (void)sendAckPackageWithInt:(jint)receivedIndex {
  AMManagedConnection_sendAckPackageWithInt_(self, receivedIndex);
}

- (void)onDropPackageWithByteArray:(IOSByteArray *)data {
  AMManagedConnection_onDropPackageWithByteArray_(self, data);
}

- (void)onRawConnected {
  AMManagedConnection_onRawConnected(self);
}

- (void)onRawReceivedWithByteArray:(IOSByteArray *)data {
  AMManagedConnection_onRawReceivedWithByteArray_(self, data);
}

- (void)onRawClosed {
  AMManagedConnection_onRawClosed(self);
}

- (void)rawPostWithInt:(jint)header
         withByteArray:(IOSByteArray *)data {
  AMManagedConnection_rawPostWithInt_withByteArray_(self, header, data);
}

- (void)rawPostWithInt:(jint)header
         withByteArray:(IOSByteArray *)data
               withInt:(jint)offset
               withInt:(jint)len {
  AMManagedConnection_rawPostWithInt_withByteArray_withInt_withInt_(self, header, data, offset, len);
}

- (void)postWithData:(IOSByteArray *)data
          withOffset:(jint)offset
          withLength:(jint)len {
  @synchronized(self) {
    if (isClosed__) {
      return;
    }
    @try {
      AMManagedConnection_sendProtoPackageWithByteArray_withInt_withInt_(self, data, offset, len);
    }
    @catch (JavaIoIOException *e) {
      [((JavaIoIOException *) nil_chk(e)) printStackTrace];
      [self close];
    }
  }
}

- (jboolean)isClosed {
  @synchronized(self) {
    return isClosed__;
  }
}

- (void)close {
  @synchronized(self) {
    if (isClosed__) {
      return;
    }
    isClosed__ = YES;
    [((AMAsyncConnection *) nil_chk(rawConnection_)) doClose];
    @synchronized(packageTimers_) {
      for (JavaLangInteger * __strong id_ in nil_chk([((JavaUtilHashMap *) nil_chk(packageTimers_)) keySet])) {
        [((AMAbsTimerCompat *) nil_chk([packageTimers_ getWithId:id_])) cancel];
      }
      for (JavaLangLong * __strong ping in nil_chk([((JavaUtilHashMap *) nil_chk(schedulledPings_)) keySet])) {
        [((AMAbsTimerCompat *) nil_chk([schedulledPings_ getWithId:ping])) cancel];
      }
      [schedulledPings_ clear];
      [packageTimers_ clear];
    }
    [((AMAbsTimerCompat *) nil_chk(pingTask_)) cancel];
    [((AMAbsTimerCompat *) nil_chk(connectionTimeout_)) cancel];
    [((AMAbsTimerCompat *) nil_chk(handshakeTimeout_)) cancel];
    if (!isOpened_ || !isHandshakePerformed_) {
      [((id<AMManagedConnectionCreateCallback>) nil_chk(factoryCallback_)) onConnectionCreateError:self];
    }
    else {
      [((id<AMConnectionCallback>) nil_chk(callback_)) onConnectionDie];
    }
  }
}

- (void)checkConnection {
  [((AMAbsTimerCompat *) nil_chk(pingTask_)) scheduleWithLong:0];
}

+ (void)initialize {
  if (self == [AMManagedConnection class]) {
    AMManagedConnection_RANDOM_ = new_JavaUtilRandom_init();
    J2OBJC_SET_INITIALIZED(AMManagedConnection)
  }
}

@end

void AMManagedConnection_initWithInt_withInt_withInt_withInt_withAMConnectionEndpoint_withAMConnectionCallback_withAMManagedConnectionCreateCallback_withAMAsyncConnectionFactory_(AMManagedConnection *self, jint connectionId, jint mtprotoVersion, jint apiMajorVersion, jint apiMinorVersion, AMConnectionEndpoint *endpoint, id<AMConnectionCallback> callback, id<AMManagedConnectionCreateCallback> factoryCallback, id<AMAsyncConnectionFactory> connectionFactory) {
  (void) NSObject_init(self);
  self->connectionInterface_ = new_AMManagedConnection_ConnectionInterface_initWithAMManagedConnection_(self);
  self->CRC32_ENGINE_ = new_AMCRC32_init();
  self->receivedPackages_ = 0;
  self->sentPackages_ = 0;
  self->isClosed__ = NO;
  self->isOpened_ = NO;
  self->isHandshakePerformed_ = NO;
  self->schedulledPings_ = new_JavaUtilHashMap_init();
  self->packageTimers_ = new_JavaUtilHashMap_init();
  self->TAG_ = JreStrcat("$I", @"Connection#", connectionId);
  self->connectionId_ = connectionId;
  self->mtprotoVersion_ = mtprotoVersion;
  self->apiMajorVersion_ = apiMajorVersion;
  self->apiMinorVersion_ = apiMinorVersion;
  self->callback_ = callback;
  self->factoryCallback_ = factoryCallback;
  self->rawConnection_ = [((id<AMAsyncConnectionFactory>) nil_chk(connectionFactory)) createConnectionWithConnectionId:connectionId withEndpoint:endpoint withInterface:self->connectionInterface_];
  self->handshakeTimeout_ = DKEnvironment_createTimerWithJavaLangRunnable_(new_AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(self));
  self->pingTask_ = DKEnvironment_createTimerWithJavaLangRunnable_(new_AMManagedConnection_PingRunnable_initWithAMManagedConnection_(self));
  self->connectionTimeout_ = DKEnvironment_createTimerWithJavaLangRunnable_(new_AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(self));
  [((AMAbsTimerCompat *) nil_chk(self->connectionTimeout_)) scheduleWithLong:AMManagedConnection_CONNECTION_TIMEOUT];
  [((AMAsyncConnection *) nil_chk(self->rawConnection_)) doConnect];
}

AMManagedConnection *new_AMManagedConnection_initWithInt_withInt_withInt_withInt_withAMConnectionEndpoint_withAMConnectionCallback_withAMManagedConnectionCreateCallback_withAMAsyncConnectionFactory_(jint connectionId, jint mtprotoVersion, jint apiMajorVersion, jint apiMinorVersion, AMConnectionEndpoint *endpoint, id<AMConnectionCallback> callback, id<AMManagedConnectionCreateCallback> factoryCallback, id<AMAsyncConnectionFactory> connectionFactory) {
  AMManagedConnection *self = [AMManagedConnection alloc];
  AMManagedConnection_initWithInt_withInt_withInt_withInt_withAMConnectionEndpoint_withAMConnectionCallback_withAMManagedConnectionCreateCallback_withAMAsyncConnectionFactory_(self, connectionId, mtprotoVersion, apiMajorVersion, apiMinorVersion, endpoint, callback, factoryCallback, connectionFactory);
  return self;
}

void AMManagedConnection_sendHandshakeRequest(AMManagedConnection *self) {
  @synchronized(self) {
    BSDataOutput *handshakeRequest = new_BSDataOutput_init();
    [handshakeRequest writeByteWithInt:self->mtprotoVersion_];
    [handshakeRequest writeByteWithInt:self->apiMajorVersion_];
    [handshakeRequest writeByteWithInt:self->apiMinorVersion_];
    self->handshakeRandomData_ = [IOSByteArray newArrayWithLength:32];
    @synchronized(AMManagedConnection_RANDOM_) {
      [((JavaUtilRandom *) nil_chk(AMManagedConnection_RANDOM_)) nextBytesWithByteArray:self->handshakeRandomData_];
    }
    [handshakeRequest writeIntWithInt:self->handshakeRandomData_->size_];
    [handshakeRequest writeBytesWithByteArray:self->handshakeRandomData_ withInt:0 withInt:self->handshakeRandomData_->size_];
    [((AMAbsTimerCompat *) nil_chk(self->handshakeTimeout_)) scheduleWithLong:AMManagedConnection_HANDSHAKE_TIMEOUT];
    AMManagedConnection_rawPostWithInt_withByteArray_(self, AMManagedConnection_HEADER_HANDSHAKE_REQUEST, [handshakeRequest toByteArray]);
  }
}

void AMManagedConnection_onHandshakePackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data) {
  @synchronized(self) {
    BSDataInput *handshakeResponse = new_BSDataInput_initWithByteArray_(data);
    jint protoVersion = [handshakeResponse readByte];
    jint apiMajor = [handshakeResponse readByte];
    jint apiMinor = [handshakeResponse readByte];
    IOSByteArray *sha256 = [handshakeResponse readBytesWithInt:32];
    IOSByteArray *localSha256 = AMCryptoUtils_SHA256WithByteArray_(self->handshakeRandomData_);
    if (!JavaUtilArrays_equalsWithByteArray_withByteArray_(sha256, localSha256)) {
      AMLog_wWithNSString_withNSString_(self->TAG_, @"SHA 256 is incorrect");
      @throw new_JavaIoIOException_initWithNSString_(@"SHA 256 is incorrect");
    }
    if (protoVersion != self->mtprotoVersion_) {
      AMLog_wWithNSString_withNSString_(self->TAG_, JreStrcat("$I$IC", @"Incorrect Proto Version, expected: ", self->mtprotoVersion_, @", got ", protoVersion, ';'));
      @throw new_JavaIoIOException_initWithNSString_(JreStrcat("$I$IC", @"Incorrect Proto Version, expected: ", self->mtprotoVersion_, @", got ", protoVersion, ';'));
    }
    if (apiMajor != self->apiMajorVersion_) {
      AMLog_wWithNSString_withNSString_(self->TAG_, JreStrcat("$I$IC", @"Incorrect Api Major Version, expected: ", apiMajor, @", got ", apiMajor, ';'));
      @throw new_JavaIoIOException_initWithNSString_(JreStrcat("$I$IC", @"Incorrect Api Major Version, expected: ", apiMajor, @", got ", apiMajor, ';'));
    }
    if (apiMinor != self->apiMinorVersion_) {
      AMLog_wWithNSString_withNSString_(self->TAG_, JreStrcat("$I$IC", @"Incorrect Api Minor Version, expected: ", apiMinor, @", got ", apiMinor, ';'));
      @throw new_JavaIoIOException_initWithNSString_(JreStrcat("$I$IC", @"Incorrect Api Minor Version, expected: ", apiMinor, @", got ", apiMinor, ';'));
    }
    self->isHandshakePerformed_ = YES;
    [((id<AMManagedConnectionCreateCallback>) nil_chk(self->factoryCallback_)) onConnectionCreated:self];
    [((AMAbsTimerCompat *) nil_chk(self->handshakeTimeout_)) cancel];
    [((AMAbsTimerCompat *) nil_chk(self->pingTask_)) scheduleWithLong:AMManagedConnection_PING_TIMEOUT];
  }
}

void AMManagedConnection_onProtoPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data) {
  @synchronized(self) {
    [((id<AMConnectionCallback>) nil_chk(self->callback_)) onMessageWithData:data withOffset:0 withLength:((IOSByteArray *) nil_chk(data))->size_];
    AMManagedConnection_refreshTimeouts(self);
  }
}

void AMManagedConnection_sendProtoPackageWithByteArray_withInt_withInt_(AMManagedConnection *self, IOSByteArray *data, jint offset, jint len) {
  @synchronized(self) {
    if (self->isClosed__) {
      return;
    }
    AMManagedConnection_rawPostWithInt_withByteArray_withInt_withInt_(self, AMManagedConnection_HEADER_PROTO, data, offset, len);
  }
}

void AMManagedConnection_onPingPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data) {
  @synchronized(self) {
    AMManagedConnection_rawPostWithInt_withByteArray_(self, AMManagedConnection_HEADER_PONG, data);
    AMManagedConnection_refreshTimeouts(self);
  }
}

void AMManagedConnection_onPongPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data) {
  @synchronized(self) {
    BSDataInput *dataInput = new_BSDataInput_initWithByteArray_(data);
    jint size = [dataInput readInt];
    if (size != 8) {
      AMLog_wWithNSString_withNSString_(self->TAG_, @"Received incorrect pong");
      @throw new_JavaIoIOException_initWithNSString_(@"Incorrect pong payload size");
    }
    jlong pingId = [dataInput readLong];
    AMAbsTimerCompat *timeoutTask = [((JavaUtilHashMap *) nil_chk(self->schedulledPings_)) removeWithId:JavaLangLong_valueOfWithLong_(pingId)];
    if (timeoutTask == nil) {
      return;
    }
    [((AMAbsTimerCompat *) nil_chk(timeoutTask)) cancel];
    AMManagedConnection_refreshTimeouts(self);
  }
}

void AMManagedConnection_sendPingMessage(AMManagedConnection *self) {
  @synchronized(self) {
    if (self->isClosed__) {
      return;
    }
    jlong pingId = [((JavaUtilRandom *) nil_chk(AMManagedConnection_RANDOM_)) nextLong];
    BSDataOutput *dataOutput = new_BSDataOutput_init();
    [dataOutput writeIntWithInt:8];
    @synchronized(AMManagedConnection_RANDOM_) {
      [dataOutput writeLongWithLong:pingId];
    }
    AMAbsTimerCompat *pingTimeoutTask = DKEnvironment_createTimerWithJavaLangRunnable_(new_AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(self));
    (void) [((JavaUtilHashMap *) nil_chk(self->schedulledPings_)) putWithId:JavaLangLong_valueOfWithLong_(pingId) withId:pingTimeoutTask];
    [((AMAbsTimerCompat *) nil_chk(pingTimeoutTask)) scheduleWithLong:AMManagedConnection_RESPONSE_TIMEOUT];
    AMManagedConnection_rawPostWithInt_withByteArray_(self, AMManagedConnection_HEADER_PING, [dataOutput toByteArray]);
  }
}

void AMManagedConnection_refreshTimeouts(AMManagedConnection *self) {
  for (AMAbsTimerCompat * __strong ping in nil_chk([((JavaUtilHashMap *) nil_chk(self->schedulledPings_)) values])) {
    [((AMAbsTimerCompat *) nil_chk(ping)) scheduleWithLong:AMManagedConnection_RESPONSE_TIMEOUT];
  }
  for (AMAbsTimerCompat * __strong ackTimeout in nil_chk([((JavaUtilHashMap *) nil_chk(self->packageTimers_)) values])) {
    [((AMAbsTimerCompat *) nil_chk(ackTimeout)) scheduleWithLong:AMManagedConnection_RESPONSE_TIMEOUT];
  }
  [((AMAbsTimerCompat *) nil_chk(self->pingTask_)) scheduleWithLong:AMManagedConnection_PING_TIMEOUT];
}

void AMManagedConnection_onAckPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data) {
  @synchronized(self) {
    BSDataInput *ackContent = new_BSDataInput_initWithByteArray_(data);
    jint frameId = [ackContent readInt];
    AMAbsTimerCompat *timerCompat = [((JavaUtilHashMap *) nil_chk(self->packageTimers_)) removeWithId:JavaLangInteger_valueOfWithInt_(frameId)];
    if (timerCompat == nil) {
      return;
    }
    [((AMAbsTimerCompat *) nil_chk(timerCompat)) cancel];
    AMManagedConnection_refreshTimeouts(self);
  }
}

void AMManagedConnection_sendAckPackageWithInt_(AMManagedConnection *self, jint receivedIndex) {
  @synchronized(self) {
    if (self->isClosed__) {
      return;
    }
    BSDataOutput *ackPackage = new_BSDataOutput_init();
    [ackPackage writeIntWithInt:receivedIndex];
    AMManagedConnection_rawPostWithInt_withByteArray_(self, AMManagedConnection_HEADER_ACK, [ackPackage toByteArray]);
  }
}

void AMManagedConnection_onDropPackageWithByteArray_(AMManagedConnection *self, IOSByteArray *data) {
  @synchronized(self) {
    BSDataInput *drop = new_BSDataInput_initWithByteArray_(data);
    jlong messageId = [drop readLong];
    jint errorCode = [drop readByte];
    jint messageLen = [drop readInt];
    NSString *message = [NSString stringWithBytes:[drop readBytesWithInt:messageLen] charsetName:@"UTF-8"];
    AMLog_wWithNSString_withNSString_(self->TAG_, JreStrcat("$$", @"Drop received: ", message));
    @throw new_JavaIoIOException_initWithNSString_(JreStrcat("$$", @"Drop received: ", message));
  }
}

void AMManagedConnection_onRawConnected(AMManagedConnection *self) {
  @synchronized(self) {
    if (self->isClosed__) {
      return;
    }
    if (self->isOpened_) {
      return;
    }
    self->isOpened_ = YES;
    [((AMAbsTimerCompat *) nil_chk(self->connectionTimeout_)) cancel];
    AMManagedConnection_sendHandshakeRequest(self);
  }
}

void AMManagedConnection_onRawReceivedWithByteArray_(AMManagedConnection *self, IOSByteArray *data) {
  @synchronized(self) {
    if (self->isClosed__) {
      return;
    }
    @try {
      BSDataInput *dataInput = new_BSDataInput_initWithByteArray_(data);
      jint packageIndex = [dataInput readInt];
      if (self->receivedPackages_ != packageIndex) {
        AMLog_wWithNSString_withNSString_(self->TAG_, JreStrcat("$I$I", @"Invalid package index. Expected: ", self->receivedPackages_, @", got: ", packageIndex));
        @throw new_JavaIoIOException_initWithNSString_(JreStrcat("$I$I", @"Invalid package index. Expected: ", self->receivedPackages_, @", got: ", packageIndex));
      }
      self->receivedPackages_++;
      jint header = [dataInput readByte];
      jint dataLength = [dataInput readInt];
      IOSByteArray *content = [dataInput readBytesWithInt:dataLength];
      jint crc32 = [dataInput readInt];
      [((AMCRC32 *) nil_chk(self->CRC32_ENGINE_)) reset];
      [self->CRC32_ENGINE_ updateWithByteArray:content];
      if (((jint) [self->CRC32_ENGINE_ getValue]) != crc32) {
        AMLog_wWithNSString_withNSString_(self->TAG_, @"Incorrect CRC32");
        @throw new_JavaIoIOException_initWithNSString_(@"Incorrect CRC32");
      }
      if (header == AMManagedConnection_HEADER_HANDSHAKE_RESPONSE) {
        if (self->isHandshakePerformed_) {
          @throw new_JavaIoIOException_initWithNSString_(@"Double Handshake");
        }
        AMManagedConnection_onHandshakePackageWithByteArray_(self, content);
      }
      else {
        if (!self->isHandshakePerformed_) {
          @throw new_JavaIoIOException_initWithNSString_(@"Package before Handshake");
        }
        if (header == AMManagedConnection_HEADER_PROTO) {
          AMManagedConnection_onProtoPackageWithByteArray_(self, content);
          AMManagedConnection_sendAckPackageWithInt_(self, packageIndex);
        }
        else if (header == AMManagedConnection_HEADER_PING) {
          AMManagedConnection_onPingPackageWithByteArray_(self, content);
        }
        else if (header == AMManagedConnection_HEADER_PONG) {
          AMManagedConnection_onPongPackageWithByteArray_(self, content);
        }
        else if (header == AMManagedConnection_HEADER_DROP) {
          AMManagedConnection_onDropPackageWithByteArray_(self, content);
        }
        else if (header == AMManagedConnection_HEADER_ACK) {
          AMManagedConnection_onAckPackageWithByteArray_(self, content);
        }
        else {
          AMLog_wWithNSString_withNSString_(self->TAG_, JreStrcat("$I", @"Received unknown package #", header));
        }
      }
    }
    @catch (JavaIoIOException *e) {
      [((JavaIoIOException *) nil_chk(e)) printStackTrace];
      [self close];
    }
  }
}

void AMManagedConnection_onRawClosed(AMManagedConnection *self) {
  @synchronized(self) {
    [self close];
  }
}

void AMManagedConnection_rawPostWithInt_withByteArray_(AMManagedConnection *self, jint header, IOSByteArray *data) {
  @synchronized(self) {
    AMManagedConnection_rawPostWithInt_withByteArray_withInt_withInt_(self, header, data, 0, ((IOSByteArray *) nil_chk(data))->size_);
  }
}

void AMManagedConnection_rawPostWithInt_withByteArray_withInt_withInt_(AMManagedConnection *self, jint header, IOSByteArray *data, jint offset, jint len) {
  @synchronized(self) {
    jint packageId = self->sentPackages_++;
    BSDataOutput *dataOutput = new_BSDataOutput_init();
    [dataOutput writeIntWithInt:packageId];
    [dataOutput writeByteWithInt:header];
    [dataOutput writeIntWithInt:((IOSByteArray *) nil_chk(data))->size_];
    [dataOutput writeBytesWithByteArray:data withInt:offset withInt:len];
    [((AMCRC32 *) nil_chk(self->CRC32_ENGINE_)) reset];
    [self->CRC32_ENGINE_ updateWithByteArray:data withInt:offset withInt:len];
    [dataOutput writeIntWithInt:(jint) [self->CRC32_ENGINE_ getValue]];
    if (header == AMManagedConnection_HEADER_PROTO) {
      AMAbsTimerCompat *timeoutTask = DKEnvironment_createTimerWithJavaLangRunnable_(new_AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(self));
      (void) [((JavaUtilHashMap *) nil_chk(self->packageTimers_)) putWithId:JavaLangInteger_valueOfWithInt_(packageId) withId:timeoutTask];
      [((AMAbsTimerCompat *) nil_chk(timeoutTask)) scheduleWithLong:AMManagedConnection_RESPONSE_TIMEOUT];
    }
    [((AMAsyncConnection *) nil_chk(self->rawConnection_)) doSend:[dataOutput toByteArray]];
  }
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMManagedConnection)

@implementation AMManagedConnection_ConnectionInterface

- (void)onConnected {
  AMManagedConnection_onRawConnected(this$0_);
}

- (void)onReceived:(IOSByteArray *)data {
  AMManagedConnection_onRawReceivedWithByteArray_(this$0_, data);
}

- (void)onClosed {
  AMManagedConnection_onRawClosed(this$0_);
}

- (instancetype)initWithAMManagedConnection:(AMManagedConnection *)outer$ {
  AMManagedConnection_ConnectionInterface_initWithAMManagedConnection_(self, outer$);
  return self;
}

@end

void AMManagedConnection_ConnectionInterface_initWithAMManagedConnection_(AMManagedConnection_ConnectionInterface *self, AMManagedConnection *outer$) {
  self->this$0_ = outer$;
  (void) NSObject_init(self);
}

AMManagedConnection_ConnectionInterface *new_AMManagedConnection_ConnectionInterface_initWithAMManagedConnection_(AMManagedConnection *outer$) {
  AMManagedConnection_ConnectionInterface *self = [AMManagedConnection_ConnectionInterface alloc];
  AMManagedConnection_ConnectionInterface_initWithAMManagedConnection_(self, outer$);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMManagedConnection_ConnectionInterface)

@implementation AMManagedConnection_PingRunnable

- (void)run {
  AMManagedConnection_sendPingMessage(this$0_);
}

- (instancetype)initWithAMManagedConnection:(AMManagedConnection *)outer$ {
  AMManagedConnection_PingRunnable_initWithAMManagedConnection_(self, outer$);
  return self;
}

@end

void AMManagedConnection_PingRunnable_initWithAMManagedConnection_(AMManagedConnection_PingRunnable *self, AMManagedConnection *outer$) {
  self->this$0_ = outer$;
  (void) NSObject_init(self);
}

AMManagedConnection_PingRunnable *new_AMManagedConnection_PingRunnable_initWithAMManagedConnection_(AMManagedConnection *outer$) {
  AMManagedConnection_PingRunnable *self = [AMManagedConnection_PingRunnable alloc];
  AMManagedConnection_PingRunnable_initWithAMManagedConnection_(self, outer$);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMManagedConnection_PingRunnable)

@implementation AMManagedConnection_TimeoutRunnable

- (void)run {
  [this$0_ close];
}

- (instancetype)initWithAMManagedConnection:(AMManagedConnection *)outer$ {
  AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(self, outer$);
  return self;
}

@end

void AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(AMManagedConnection_TimeoutRunnable *self, AMManagedConnection *outer$) {
  self->this$0_ = outer$;
  (void) NSObject_init(self);
}

AMManagedConnection_TimeoutRunnable *new_AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(AMManagedConnection *outer$) {
  AMManagedConnection_TimeoutRunnable *self = [AMManagedConnection_TimeoutRunnable alloc];
  AMManagedConnection_TimeoutRunnable_initWithAMManagedConnection_(self, outer$);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMManagedConnection_TimeoutRunnable)
