//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/actors/ManagerActor.java
//

#ifndef _MTManagerActor_H_
#define _MTManagerActor_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/actors/Actor.h"

@class AMNetworkStateEnum;
@class DKActorRef;
@class IOSByteArray;
@class MTMTProto;

@interface MTManagerActor : DKActor

#pragma mark Public

- (instancetype)initWithMTMTProto:(MTMTProto *)mtProto;

+ (DKActorRef *)managerWithMTMTProto:(MTMTProto *)mtProto;

- (void)onReceiveWithId:(id)message;

- (void)preStart;

@end

J2OBJC_STATIC_INIT(MTManagerActor)

FOUNDATION_EXPORT DKActorRef *MTManagerActor_managerWithMTMTProto_(MTMTProto *mtProto);

FOUNDATION_EXPORT void MTManagerActor_initWithMTMTProto_(MTManagerActor *self, MTMTProto *mtProto);

FOUNDATION_EXPORT MTManagerActor *new_MTManagerActor_initWithMTMTProto_(MTMTProto *mtProto) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(MTManagerActor)

typedef MTManagerActor ImActorModelNetworkMtpActorsManagerActor;

@interface MTManagerActor_OutMessage : NSObject

#pragma mark Public

- (instancetype)initWithByteArray:(IOSByteArray *)message
                          withInt:(jint)offset
                          withInt:(jint)len;

- (jint)getLen;

- (IOSByteArray *)getMessage;

- (jint)getOffset;

@end

J2OBJC_EMPTY_STATIC_INIT(MTManagerActor_OutMessage)

FOUNDATION_EXPORT void MTManagerActor_OutMessage_initWithByteArray_withInt_withInt_(MTManagerActor_OutMessage *self, IOSByteArray *message, jint offset, jint len);

FOUNDATION_EXPORT MTManagerActor_OutMessage *new_MTManagerActor_OutMessage_initWithByteArray_withInt_withInt_(IOSByteArray *message, jint offset, jint len) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(MTManagerActor_OutMessage)

@interface MTManagerActor_InMessage : NSObject

#pragma mark Public

- (instancetype)initWithByteArray:(IOSByteArray *)data
                          withInt:(jint)offset
                          withInt:(jint)len;

- (IOSByteArray *)getData;

- (jint)getLen;

- (jint)getOffset;

@end

J2OBJC_EMPTY_STATIC_INIT(MTManagerActor_InMessage)

FOUNDATION_EXPORT void MTManagerActor_InMessage_initWithByteArray_withInt_withInt_(MTManagerActor_InMessage *self, IOSByteArray *data, jint offset, jint len);

FOUNDATION_EXPORT MTManagerActor_InMessage *new_MTManagerActor_InMessage_initWithByteArray_withInt_withInt_(IOSByteArray *data, jint offset, jint len) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(MTManagerActor_InMessage)

@interface MTManagerActor_NetworkChanged : NSObject

#pragma mark Public

- (instancetype)initWithAMNetworkStateEnum:(AMNetworkStateEnum *)state;

@end

J2OBJC_EMPTY_STATIC_INIT(MTManagerActor_NetworkChanged)

FOUNDATION_EXPORT void MTManagerActor_NetworkChanged_initWithAMNetworkStateEnum_(MTManagerActor_NetworkChanged *self, AMNetworkStateEnum *state);

FOUNDATION_EXPORT MTManagerActor_NetworkChanged *new_MTManagerActor_NetworkChanged_initWithAMNetworkStateEnum_(AMNetworkStateEnum *state) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(MTManagerActor_NetworkChanged)

#endif // _MTManagerActor_H_
