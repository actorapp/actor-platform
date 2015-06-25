//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/entity/Message.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserCreator.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/entity/Message.h"
#include "im/actor/model/entity/MessageState.h"
#include "im/actor/model/entity/content/AbsContent.h"
#include "java/io/IOException.h"

@interface AMMessage () {
 @public
  jlong rid_;
  jlong sortDate_;
  jlong date_;
  jint senderId_;
  AMMessageStateEnum *messageState_;
  AMAbsContent *content_;
}

- (instancetype)init;

@end

J2OBJC_FIELD_SETTER(AMMessage, messageState_, AMMessageStateEnum *)
J2OBJC_FIELD_SETTER(AMMessage, content_, AMAbsContent *)

__attribute__((unused)) static void AMMessage_init(AMMessage *self);

__attribute__((unused)) static AMMessage *new_AMMessage_init() NS_RETURNS_RETAINED;

@interface AMMessage_$1 : NSObject < BSBserCreator >

- (AMMessage *)createInstance;

- (instancetype)init;

@end

J2OBJC_EMPTY_STATIC_INIT(AMMessage_$1)

__attribute__((unused)) static void AMMessage_$1_init(AMMessage_$1 *self);

__attribute__((unused)) static AMMessage_$1 *new_AMMessage_$1_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMMessage_$1)

J2OBJC_INITIALIZED_DEFN(AMMessage)

id<BSBserCreator> AMMessage_CREATOR_;

@implementation AMMessage

+ (AMMessage *)fromBytesWithByteArray:(IOSByteArray *)data {
  return AMMessage_fromBytesWithByteArray_(data);
}

- (instancetype)initWithLong:(jlong)rid
                    withLong:(jlong)sortDate
                    withLong:(jlong)date
                     withInt:(jint)senderId
      withAMMessageStateEnum:(AMMessageStateEnum *)messageState
            withAMAbsContent:(AMAbsContent *)content {
  AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(self, rid, sortDate, date, senderId, messageState, content);
  return self;
}

- (instancetype)init {
  AMMessage_init(self);
  return self;
}

- (jlong)getRid {
  return rid_;
}

- (jlong)getSortDate {
  return sortDate_;
}

- (jlong)getDate {
  return date_;
}

- (jint)getSenderId {
  return senderId_;
}

- (AMMessageStateEnum *)getMessageState {
  return messageState_;
}

- (jboolean)isSent {
  return messageState_ == AMMessageStateEnum_get_SENT() || messageState_ == AMMessageStateEnum_get_SENT();
}

- (jboolean)isReceivedOrSent {
  return messageState_ == AMMessageStateEnum_get_SENT() || messageState_ == AMMessageStateEnum_get_RECEIVED();
}

- (jboolean)isPendingOrSent {
  return messageState_ == AMMessageStateEnum_get_SENT() || messageState_ == AMMessageStateEnum_get_PENDING();
}

- (jboolean)isOnServer {
  return messageState_ != AMMessageStateEnum_get_ERROR() && messageState_ != AMMessageStateEnum_get_PENDING();
}

- (AMAbsContent *)getContent {
  return content_;
}

- (AMMessage *)changeStateWithAMMessageStateEnum:(AMMessageStateEnum *)messageState {
  return new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid_, sortDate_, date_, senderId_, messageState, content_);
}

- (AMMessage *)changeDateWithLong:(jlong)date {
  return new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid_, sortDate_, date, senderId_, messageState_, content_);
}

- (AMMessage *)changeAllDateWithLong:(jlong)date {
  return new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid_, date, date, senderId_, messageState_, content_);
}

- (AMMessage *)changeContentWithAMAbsContent:(AMAbsContent *)content {
  return new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(rid_, sortDate_, date_, senderId_, messageState_, content);
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  rid_ = [((BSBserValues *) nil_chk(values)) getLongWithInt:1];
  sortDate_ = [values getLongWithInt:2];
  date_ = [values getLongWithInt:3];
  senderId_ = [values getIntWithInt:4];
  messageState_ = AMMessageStateEnum_fromValueWithInt_([values getIntWithInt:5]);
  content_ = AMAbsContent_parseWithByteArray_([values getBytesWithInt:6]);
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeLongWithInt:1 withLong:rid_];
  [writer writeLongWithInt:2 withLong:sortDate_];
  [writer writeLongWithInt:3 withLong:date_];
  [writer writeIntWithInt:4 withInt:senderId_];
  [writer writeIntWithInt:5 withInt:[((AMMessageStateEnum *) nil_chk(messageState_)) getValue]];
  [writer writeBytesWithInt:6 withByteArray:AMAbsContent_serializeWithAMAbsContent_(content_)];
}

- (jlong)getEngineId {
  return rid_;
}

- (jlong)getEngineSort {
  return sortDate_;
}

- (NSString *)getEngineSearch {
  return nil;
}

+ (void)initialize {
  if (self == [AMMessage class]) {
    AMMessage_CREATOR_ = new_AMMessage_$1_init();
    J2OBJC_SET_INITIALIZED(AMMessage)
  }
}

@end

AMMessage *AMMessage_fromBytesWithByteArray_(IOSByteArray *data) {
  AMMessage_initialize();
  return ((AMMessage *) BSBser_parseWithBSBserObject_withByteArray_(new_AMMessage_init(), data));
}

void AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(AMMessage *self, jlong rid, jlong sortDate, jlong date, jint senderId, AMMessageStateEnum *messageState, AMAbsContent *content) {
  (void) BSBserObject_init(self);
  self->rid_ = rid;
  self->sortDate_ = sortDate;
  self->date_ = date;
  self->senderId_ = senderId;
  self->messageState_ = messageState;
  self->content_ = content;
}

AMMessage *new_AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(jlong rid, jlong sortDate, jlong date, jint senderId, AMMessageStateEnum *messageState, AMAbsContent *content) {
  AMMessage *self = [AMMessage alloc];
  AMMessage_initWithLong_withLong_withLong_withInt_withAMMessageStateEnum_withAMAbsContent_(self, rid, sortDate, date, senderId, messageState, content);
  return self;
}

void AMMessage_init(AMMessage *self) {
  (void) BSBserObject_init(self);
}

AMMessage *new_AMMessage_init() {
  AMMessage *self = [AMMessage alloc];
  AMMessage_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMessage)

@implementation AMMessage_$1

- (AMMessage *)createInstance {
  return new_AMMessage_init();
}

- (instancetype)init {
  AMMessage_$1_init(self);
  return self;
}

@end

void AMMessage_$1_init(AMMessage_$1 *self) {
  (void) NSObject_init(self);
}

AMMessage_$1 *new_AMMessage_$1_init() {
  AMMessage_$1 *self = [AMMessage_$1 alloc];
  AMMessage_$1_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMessage_$1)
