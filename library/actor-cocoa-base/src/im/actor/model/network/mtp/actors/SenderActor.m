//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/actors/SenderActor.java
//


#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/actors/Actor.h"
#include "im/actor/model/droidkit/actors/ActorCreator.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/actors/ActorSelection.h"
#include "im/actor/model/droidkit/actors/ActorSystem.h"
#include "im/actor/model/droidkit/actors/Props.h"
#include "im/actor/model/log/Log.h"
#include "im/actor/model/network/mtp/MTProto.h"
#include "im/actor/model/network/mtp/actors/ManagerActor.h"
#include "im/actor/model/network/mtp/actors/SenderActor.h"
#include "im/actor/model/network/mtp/entity/Container.h"
#include "im/actor/model/network/mtp/entity/MessageAck.h"
#include "im/actor/model/network/mtp/entity/ProtoMessage.h"
#include "im/actor/model/network/util/MTUids.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collection.h"
#include "java/util/HashMap.h"
#include "java/util/HashSet.h"
#include "java/util/List.h"

#define MTSenderActor_ACK_THRESHOLD 10
#define MTSenderActor_ACK_DELAY 10000
#define MTSenderActor_MAX_WORKLOAD_SIZE 1024

@interface MTSenderActor () {
 @public
  MTMTProto *proto_;
  DKActorRef *manager_;
  JavaUtilHashMap *unsentPackages_;
  JavaUtilHashSet *confirm_;
}

- (MTMessageAck *)buildAck;

- (void)doSendWithJavaUtilList:(id<JavaUtilList>)items;

- (void)doSendWithMTProtoMessage:(MTProtoMessage *)message;

- (void)performSendWithMTProtoMessage:(MTProtoMessage *)message;

@end

J2OBJC_FIELD_SETTER(MTSenderActor, proto_, MTMTProto *)
J2OBJC_FIELD_SETTER(MTSenderActor, manager_, DKActorRef *)
J2OBJC_FIELD_SETTER(MTSenderActor, unsentPackages_, JavaUtilHashMap *)
J2OBJC_FIELD_SETTER(MTSenderActor, confirm_, JavaUtilHashSet *)

static NSString *MTSenderActor_TAG_ = @"ProtoSender";
J2OBJC_STATIC_FIELD_GETTER(MTSenderActor, TAG_, NSString *)

J2OBJC_STATIC_FIELD_GETTER(MTSenderActor, ACK_THRESHOLD, jint)

J2OBJC_STATIC_FIELD_GETTER(MTSenderActor, ACK_DELAY, jint)

J2OBJC_STATIC_FIELD_GETTER(MTSenderActor, MAX_WORKLOAD_SIZE, jint)

__attribute__((unused)) static MTMessageAck *MTSenderActor_buildAck(MTSenderActor *self);

__attribute__((unused)) static void MTSenderActor_doSendWithJavaUtilList_(MTSenderActor *self, id<JavaUtilList> items);

__attribute__((unused)) static void MTSenderActor_doSendWithMTProtoMessage_(MTSenderActor *self, MTProtoMessage *message);

__attribute__((unused)) static void MTSenderActor_performSendWithMTProtoMessage_(MTSenderActor *self, MTProtoMessage *message);

@interface MTSenderActor_SendMessage () {
 @public
  jlong mid_;
  IOSByteArray *message_;
}

@end

J2OBJC_FIELD_SETTER(MTSenderActor_SendMessage, message_, IOSByteArray *)

@interface MTSenderActor_ForgetMessage () {
 @public
  jlong mid_;
}

@end

@interface MTSenderActor_ConfirmMessage () {
 @public
  jlong mid_;
}

@end

@interface MTSenderActor_$1 : NSObject < DKActorCreator > {
 @public
  MTMTProto *val$proto_;
}

- (MTSenderActor *)create;

- (instancetype)initWithMTMTProto:(MTMTProto *)capture$0;

@end

J2OBJC_EMPTY_STATIC_INIT(MTSenderActor_$1)

J2OBJC_FIELD_SETTER(MTSenderActor_$1, val$proto_, MTMTProto *)

__attribute__((unused)) static void MTSenderActor_$1_initWithMTMTProto_(MTSenderActor_$1 *self, MTMTProto *capture$0);

__attribute__((unused)) static MTSenderActor_$1 *new_MTSenderActor_$1_initWithMTMTProto_(MTMTProto *capture$0) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(MTSenderActor_$1)

@implementation MTSenderActor

+ (DKActorRef *)senderActorWithMTMTProto:(MTMTProto *)proto {
  return MTSenderActor_senderActorWithMTMTProto_(proto);
}

- (instancetype)initWithMTMTProto:(MTMTProto *)proto {
  MTSenderActor_initWithMTMTProto_(self, proto);
  return self;
}

- (void)preStart {
  manager_ = MTManagerActor_managerWithMTMTProto_(proto_);
}

- (void)onReceiveWithId:(id)message {
  if ([message isKindOfClass:[MTSenderActor_SendMessage class]]) {
    AMLog_dWithNSString_withNSString_(MTSenderActor_TAG_, JreStrcat("$J", @"Received SendMessage #", ((MTSenderActor_SendMessage *) nil_chk(((MTSenderActor_SendMessage *) check_class_cast(message, [MTSenderActor_SendMessage class]))))->mid_));
    MTSenderActor_SendMessage *sendMessage = (MTSenderActor_SendMessage *) check_class_cast(message, [MTSenderActor_SendMessage class]);
    MTProtoMessage *holder = new_MTProtoMessage_initWithLong_withByteArray_(((MTSenderActor_SendMessage *) nil_chk(sendMessage))->mid_, sendMessage->message_);
    (void) [((JavaUtilHashMap *) nil_chk(unsentPackages_)) putWithId:JavaLangLong_valueOfWithLong_([holder getMessageId]) withId:holder];
    MTSenderActor_doSendWithMTProtoMessage_(self, holder);
  }
  else if ([message isKindOfClass:[MTSenderActor_ConnectionCreated class]]) {
    AMLog_dWithNSString_withNSString_(MTSenderActor_TAG_, @"Received ConnectionCreated");
    JavaUtilArrayList *toSend = new_JavaUtilArrayList_init();
    for (MTProtoMessage * __strong unsentPackage in nil_chk([((JavaUtilHashMap *) nil_chk(unsentPackages_)) values])) {
      AMLog_dWithNSString_withNSString_(MTSenderActor_TAG_, JreStrcat("$J", @"ReSending #", [((MTProtoMessage *) nil_chk(unsentPackage)) getMessageId]));
      [toSend addWithId:unsentPackage];
    }
    MTSenderActor_doSendWithJavaUtilList_(self, toSend);
  }
  else if ([message isKindOfClass:[MTSenderActor_ForgetMessage class]]) {
    AMLog_dWithNSString_withNSString_(MTSenderActor_TAG_, JreStrcat("$J", @"Received ForgetMessage #", ((MTSenderActor_ForgetMessage *) nil_chk(((MTSenderActor_ForgetMessage *) check_class_cast(message, [MTSenderActor_ForgetMessage class]))))->mid_));
    (void) [((JavaUtilHashMap *) nil_chk(unsentPackages_)) removeWithId:JavaLangLong_valueOfWithLong_(((MTSenderActor_ForgetMessage *) nil_chk(((MTSenderActor_ForgetMessage *) check_class_cast(message, [MTSenderActor_ForgetMessage class]))))->mid_)];
  }
  else if ([message isKindOfClass:[MTSenderActor_ConfirmMessage class]]) {
    AMLog_dWithNSString_withNSString_(MTSenderActor_TAG_, JreStrcat("$J", @"Confirming message #", ((MTSenderActor_ConfirmMessage *) nil_chk(((MTSenderActor_ConfirmMessage *) check_class_cast(message, [MTSenderActor_ConfirmMessage class]))))->mid_));
    [((JavaUtilHashSet *) nil_chk(confirm_)) addWithId:JavaLangLong_valueOfWithLong_(((MTSenderActor_ConfirmMessage *) nil_chk(((MTSenderActor_ConfirmMessage *) check_class_cast(message, [MTSenderActor_ConfirmMessage class]))))->mid_)];
    if ([confirm_ size] >= MTSenderActor_ACK_THRESHOLD) {
      [((DKActorRef *) nil_chk([self self__])) sendOnceWithId:new_MTSenderActor_ForceAck_init()];
    }
    else if ([confirm_ size] == 1) {
      [((DKActorRef *) nil_chk([self self__])) sendOnceWithId:new_MTSenderActor_ForceAck_init() withLong:MTSenderActor_ACK_DELAY];
    }
  }
  else if ([message isKindOfClass:[MTSenderActor_ForceAck class]]) {
    if ([((JavaUtilHashSet *) nil_chk(confirm_)) size] == 0) {
      return;
    }
    NSString *acks = @"";
    for (JavaLangLong * __strong l in confirm_) {
      if (((jint) [acks length]) != 0) {
        acks = JreStrcat("$C", acks, ',');
      }
      acks = JreStrcat("$$", acks, JreStrcat("C@", '#', l));
    }
    AMLog_dWithNSString_withNSString_(MTSenderActor_TAG_, JreStrcat("$$", @"Sending acks ", acks));
    MTMessageAck *messageAck = MTSenderActor_buildAck(self);
    [confirm_ clear];
    MTSenderActor_doSendWithMTProtoMessage_(self, new_MTProtoMessage_initWithLong_withByteArray_(ImActorModelNetworkUtilMTUids_nextId(), [((MTMessageAck *) nil_chk(messageAck)) toByteArray]));
  }
  else if ([message isKindOfClass:[MTSenderActor_NewSession class]]) {
    AMLog_wWithNSString_withNSString_(MTSenderActor_TAG_, @"Received NewSessionCreated");
    JavaUtilArrayList *toSend = new_JavaUtilArrayList_init();
    for (MTProtoMessage * __strong unsentPackage in nil_chk([((JavaUtilHashMap *) nil_chk(unsentPackages_)) values])) {
      AMLog_dWithNSString_withNSString_(MTSenderActor_TAG_, JreStrcat("$J", @"ReSending #", [((MTProtoMessage *) nil_chk(unsentPackage)) getMessageId]));
      [toSend addWithId:unsentPackage];
    }
    MTSenderActor_doSendWithJavaUtilList_(self, toSend);
  }
}

- (MTMessageAck *)buildAck {
  return MTSenderActor_buildAck(self);
}

- (void)doSendWithJavaUtilList:(id<JavaUtilList>)items {
  MTSenderActor_doSendWithJavaUtilList_(self, items);
}

- (void)doSendWithMTProtoMessage:(MTProtoMessage *)message {
  MTSenderActor_doSendWithMTProtoMessage_(self, message);
}

- (void)performSendWithMTProtoMessage:(MTProtoMessage *)message {
  MTSenderActor_performSendWithMTProtoMessage_(self, message);
}

@end

DKActorRef *MTSenderActor_senderActorWithMTMTProto_(MTMTProto *proto) {
  MTSenderActor_initialize();
  return [((DKActorSystem *) nil_chk(DKActorSystem_system())) actorOfWithDKActorSelection:new_DKActorSelection_initWithDKProps_withNSString_(DKProps_createWithIOSClass_withDKActorCreator_(MTSenderActor_class_(), new_MTSenderActor_$1_initWithMTMTProto_(proto)), JreStrcat("$$", [((MTMTProto *) nil_chk(proto)) getActorPath], @"/sender"))];
}

void MTSenderActor_initWithMTMTProto_(MTSenderActor *self, MTMTProto *proto) {
  (void) DKActor_init(self);
  self->proto_ = proto;
  self->unsentPackages_ = new_JavaUtilHashMap_init();
  self->confirm_ = new_JavaUtilHashSet_init();
}

MTSenderActor *new_MTSenderActor_initWithMTMTProto_(MTMTProto *proto) {
  MTSenderActor *self = [MTSenderActor alloc];
  MTSenderActor_initWithMTMTProto_(self, proto);
  return self;
}

MTMessageAck *MTSenderActor_buildAck(MTSenderActor *self) {
  IOSLongArray *ids = [IOSLongArray newArrayWithLength:[((JavaUtilHashSet *) nil_chk(self->confirm_)) size]];
  IOSObjectArray *ids2 = [self->confirm_ toArrayWithNSObjectArray:[IOSObjectArray newArrayWithLength:[self->confirm_ size] type:JavaLangLong_class_()]];
  for (jint i = 0; i < ids->size_; i++) {
    *IOSLongArray_GetRef(ids, i) = [((JavaLangLong *) nil_chk(IOSObjectArray_Get(nil_chk(ids2), i))) longLongValue];
  }
  return new_MTMessageAck_initWithLongArray_(ids);
}

void MTSenderActor_doSendWithJavaUtilList_(MTSenderActor *self, id<JavaUtilList> items) {
  if ([((id<JavaUtilList>) nil_chk(items)) size] > 0) {
    if ([((JavaUtilHashSet *) nil_chk(self->confirm_)) size] > 0) {
      [items addWithInt:0 withId:new_MTProtoMessage_initWithLong_withByteArray_(ImActorModelNetworkUtilMTUids_nextId(), [((MTMessageAck *) nil_chk(MTSenderActor_buildAck(self))) toByteArray])];
      [self->confirm_ clear];
    }
  }
  if ([items size] == 1) {
    MTSenderActor_doSendWithMTProtoMessage_(self, [items getWithInt:0]);
  }
  else if ([items size] > 1) {
    JavaUtilArrayList *messages = new_JavaUtilArrayList_init();
    jint currentPayload = 0;
    for (jint i = 0; i < [items size]; i++) {
      MTProtoMessage *message = [items getWithInt:i];
      currentPayload += ((IOSByteArray *) nil_chk([((MTProtoMessage *) nil_chk(message)) getPayload]))->size_;
      [messages addWithId:message];
      if (currentPayload > MTSenderActor_MAX_WORKLOAD_SIZE) {
        MTContainer *container = new_MTContainer_initWithMTProtoMessageArray_([messages toArrayWithNSObjectArray:[IOSObjectArray newArrayWithLength:[messages size] type:MTProtoMessage_class_()]]);
        MTSenderActor_performSendWithMTProtoMessage_(self, new_MTProtoMessage_initWithLong_withByteArray_(ImActorModelNetworkUtilMTUids_nextId(), [container toByteArray]));
        [messages clear];
        currentPayload = 0;
      }
    }
    if ([messages size] > 0) {
      MTContainer *container = new_MTContainer_initWithMTProtoMessageArray_([messages toArrayWithNSObjectArray:[IOSObjectArray newArrayWithLength:[messages size] type:MTProtoMessage_class_()]]);
      MTSenderActor_performSendWithMTProtoMessage_(self, new_MTProtoMessage_initWithLong_withByteArray_(ImActorModelNetworkUtilMTUids_nextId(), [container toByteArray]));
    }
  }
}

void MTSenderActor_doSendWithMTProtoMessage_(MTSenderActor *self, MTProtoMessage *message) {
  if ([((JavaUtilHashSet *) nil_chk(self->confirm_)) size] > 0) {
    JavaUtilArrayList *mtpMessages = new_JavaUtilArrayList_init();
    [mtpMessages addWithId:message];
    MTSenderActor_doSendWithJavaUtilList_(self, mtpMessages);
  }
  else {
    MTSenderActor_performSendWithMTProtoMessage_(self, message);
  }
}

void MTSenderActor_performSendWithMTProtoMessage_(MTSenderActor *self, MTProtoMessage *message) {
  IOSByteArray *data = [((MTProtoMessage *) nil_chk(message)) toByteArray];
  [((DKActorRef *) nil_chk(self->manager_)) sendWithId:new_MTManagerActor_OutMessage_initWithByteArray_withInt_withInt_(data, 0, ((IOSByteArray *) nil_chk(data))->size_)];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor)

@implementation MTSenderActor_SendMessage

- (instancetype)initWithLong:(jlong)rid
               withByteArray:(IOSByteArray *)message {
  MTSenderActor_SendMessage_initWithLong_withByteArray_(self, rid, message);
  return self;
}

@end

void MTSenderActor_SendMessage_initWithLong_withByteArray_(MTSenderActor_SendMessage *self, jlong rid, IOSByteArray *message) {
  (void) NSObject_init(self);
  self->mid_ = rid;
  self->message_ = message;
}

MTSenderActor_SendMessage *new_MTSenderActor_SendMessage_initWithLong_withByteArray_(jlong rid, IOSByteArray *message) {
  MTSenderActor_SendMessage *self = [MTSenderActor_SendMessage alloc];
  MTSenderActor_SendMessage_initWithLong_withByteArray_(self, rid, message);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor_SendMessage)

@implementation MTSenderActor_ForgetMessage

- (instancetype)initWithLong:(jlong)rid {
  MTSenderActor_ForgetMessage_initWithLong_(self, rid);
  return self;
}

@end

void MTSenderActor_ForgetMessage_initWithLong_(MTSenderActor_ForgetMessage *self, jlong rid) {
  (void) NSObject_init(self);
  self->mid_ = rid;
}

MTSenderActor_ForgetMessage *new_MTSenderActor_ForgetMessage_initWithLong_(jlong rid) {
  MTSenderActor_ForgetMessage *self = [MTSenderActor_ForgetMessage alloc];
  MTSenderActor_ForgetMessage_initWithLong_(self, rid);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor_ForgetMessage)

@implementation MTSenderActor_ConfirmMessage

- (instancetype)initWithLong:(jlong)rid {
  MTSenderActor_ConfirmMessage_initWithLong_(self, rid);
  return self;
}

@end

void MTSenderActor_ConfirmMessage_initWithLong_(MTSenderActor_ConfirmMessage *self, jlong rid) {
  (void) NSObject_init(self);
  self->mid_ = rid;
}

MTSenderActor_ConfirmMessage *new_MTSenderActor_ConfirmMessage_initWithLong_(jlong rid) {
  MTSenderActor_ConfirmMessage *self = [MTSenderActor_ConfirmMessage alloc];
  MTSenderActor_ConfirmMessage_initWithLong_(self, rid);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor_ConfirmMessage)

@implementation MTSenderActor_ConnectionCreated

- (instancetype)init {
  MTSenderActor_ConnectionCreated_init(self);
  return self;
}

@end

void MTSenderActor_ConnectionCreated_init(MTSenderActor_ConnectionCreated *self) {
  (void) NSObject_init(self);
}

MTSenderActor_ConnectionCreated *new_MTSenderActor_ConnectionCreated_init() {
  MTSenderActor_ConnectionCreated *self = [MTSenderActor_ConnectionCreated alloc];
  MTSenderActor_ConnectionCreated_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor_ConnectionCreated)

@implementation MTSenderActor_NewSession

- (instancetype)init {
  MTSenderActor_NewSession_init(self);
  return self;
}

@end

void MTSenderActor_NewSession_init(MTSenderActor_NewSession *self) {
  (void) NSObject_init(self);
}

MTSenderActor_NewSession *new_MTSenderActor_NewSession_init() {
  MTSenderActor_NewSession *self = [MTSenderActor_NewSession alloc];
  MTSenderActor_NewSession_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor_NewSession)

@implementation MTSenderActor_ForceAck

- (instancetype)init {
  MTSenderActor_ForceAck_init(self);
  return self;
}

@end

void MTSenderActor_ForceAck_init(MTSenderActor_ForceAck *self) {
  (void) NSObject_init(self);
}

MTSenderActor_ForceAck *new_MTSenderActor_ForceAck_init() {
  MTSenderActor_ForceAck *self = [MTSenderActor_ForceAck alloc];
  MTSenderActor_ForceAck_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor_ForceAck)

@implementation MTSenderActor_$1

- (MTSenderActor *)create {
  return new_MTSenderActor_initWithMTMTProto_(val$proto_);
}

- (instancetype)initWithMTMTProto:(MTMTProto *)capture$0 {
  MTSenderActor_$1_initWithMTMTProto_(self, capture$0);
  return self;
}

@end

void MTSenderActor_$1_initWithMTMTProto_(MTSenderActor_$1 *self, MTMTProto *capture$0) {
  self->val$proto_ = capture$0;
  (void) NSObject_init(self);
}

MTSenderActor_$1 *new_MTSenderActor_$1_initWithMTMTProto_(MTMTProto *capture$0) {
  MTSenderActor_$1 *self = [MTSenderActor_$1 alloc];
  MTSenderActor_$1_initWithMTMTProto_(self, capture$0);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTSenderActor_$1)
