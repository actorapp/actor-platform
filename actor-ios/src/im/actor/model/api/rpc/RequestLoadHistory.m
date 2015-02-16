//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/api/rpc/RequestLoadHistory.java
//

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/OutPeer.h"
#include "im/actor/model/api/rpc/RequestLoadHistory.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"

@interface ImActorModelApiRpcRequestLoadHistory () {
 @public
  ImActorModelApiOutPeer *peer_;
  jlong startDate_;
  jint limit_;
}
@end

J2OBJC_FIELD_SETTER(ImActorModelApiRpcRequestLoadHistory, peer_, ImActorModelApiOutPeer *)

@implementation ImActorModelApiRpcRequestLoadHistory

+ (ImActorModelApiRpcRequestLoadHistory *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiRpcRequestLoadHistory_fromBytesWithByteArray_(data);
}

- (instancetype)initWithImActorModelApiOutPeer:(ImActorModelApiOutPeer *)peer
                                      withLong:(jlong)startDate
                                       withInt:(jint)limit {
  if (self = [super init]) {
    self->peer_ = peer;
    self->startDate_ = startDate;
    self->limit_ = limit;
  }
  return self;
}

- (instancetype)init {
  return [super init];
}

- (ImActorModelApiOutPeer *)getPeer {
  return self->peer_;
}

- (jlong)getStartDate {
  return self->startDate_;
}

- (jint)getLimit {
  return self->limit_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->peer_ = [((BSBserValues *) nil_chk(values)) getObjWithInt:1 withBSBserObject:[[ImActorModelApiOutPeer alloc] init]];
  self->startDate_ = [values getLongWithInt:3];
  self->limit_ = [values getIntWithInt:4];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->peer_ == nil) {
    @throw [[JavaIoIOException alloc] init];
  }
  [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:1 withBSBserObject:self->peer_];
  [writer writeLongWithInt:3 withLong:self->startDate_];
  [writer writeIntWithInt:4 withInt:self->limit_];
}

- (jint)getHeaderKey {
  return ImActorModelApiRpcRequestLoadHistory_HEADER;
}

- (void)copyAllFieldsTo:(ImActorModelApiRpcRequestLoadHistory *)other {
  [super copyAllFieldsTo:other];
  other->peer_ = peer_;
  other->startDate_ = startDate_;
  other->limit_ = limit_;
}

@end

ImActorModelApiRpcRequestLoadHistory *ImActorModelApiRpcRequestLoadHistory_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiRpcRequestLoadHistory_init();
  return ((ImActorModelApiRpcRequestLoadHistory *) BSBser_parseWithBSBserObject_withByteArray_([[ImActorModelApiRpcRequestLoadHistory alloc] init], data));
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRpcRequestLoadHistory)
