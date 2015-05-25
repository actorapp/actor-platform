//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/DocumentMessage.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/DocumentEx.h"
#include "im/actor/model/api/DocumentMessage.h"
#include "im/actor/model/api/FastThumb.h"
#include "im/actor/model/api/Message.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/droidkit/bser/util/SparseArray.h"
#include "java/io/IOException.h"

@interface APDocumentMessage () {
 @public
  jlong fileId_;
  jlong accessHash_;
  jint fileSize_;
  NSString *name_;
  NSString *mimeType_;
  APFastThumb *thumb_;
  APDocumentEx *ext_;
}

@end

J2OBJC_FIELD_SETTER(APDocumentMessage, name_, NSString *)
J2OBJC_FIELD_SETTER(APDocumentMessage, mimeType_, NSString *)
J2OBJC_FIELD_SETTER(APDocumentMessage, thumb_, APFastThumb *)
J2OBJC_FIELD_SETTER(APDocumentMessage, ext_, APDocumentEx *)

@implementation APDocumentMessage

- (instancetype)initWithLong:(jlong)fileId
                    withLong:(jlong)accessHash
                     withInt:(jint)fileSize
                withNSString:(NSString *)name
                withNSString:(NSString *)mimeType
             withAPFastThumb:(APFastThumb *)thumb
            withAPDocumentEx:(APDocumentEx *)ext {
  APDocumentMessage_initWithLong_withLong_withInt_withNSString_withNSString_withAPFastThumb_withAPDocumentEx_(self, fileId, accessHash, fileSize, name, mimeType, thumb, ext);
  return self;
}

- (instancetype)init {
  APDocumentMessage_init(self);
  return self;
}

- (jint)getHeader {
  return 3;
}

- (jlong)getFileId {
  return self->fileId_;
}

- (jlong)getAccessHash {
  return self->accessHash_;
}

- (jint)getFileSize {
  return self->fileSize_;
}

- (NSString *)getName {
  return self->name_;
}

- (NSString *)getMimeType {
  return self->mimeType_;
}

- (APFastThumb *)getThumb {
  return self->thumb_;
}

- (APDocumentEx *)getExt {
  return self->ext_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->fileId_ = [((BSBserValues *) nil_chk(values)) getLongWithInt:1];
  self->accessHash_ = [values getLongWithInt:2];
  self->fileSize_ = [values getIntWithInt:3];
  self->name_ = [values getStringWithInt:4];
  self->mimeType_ = [values getStringWithInt:5];
  self->thumb_ = [values optObjWithInt:6 withBSBserObject:new_APFastThumb_init()];
  if ([values optBytesWithInt:8] != nil) {
    self->ext_ = APDocumentEx_fromBytesWithByteArray_([values getBytesWithInt:8]);
  }
  if ([values hasRemaining]) {
    [self setUnmappedObjectsWithImActorModelDroidkitBserUtilSparseArray:[values buildRemaining]];
  }
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeLongWithInt:1 withLong:self->fileId_];
  [writer writeLongWithInt:2 withLong:self->accessHash_];
  [writer writeIntWithInt:3 withInt:self->fileSize_];
  if (self->name_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeStringWithInt:4 withNSString:self->name_];
  if (self->mimeType_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [writer writeStringWithInt:5 withNSString:self->mimeType_];
  if (self->thumb_ != nil) {
    [writer writeObjectWithInt:6 withBSBserObject:self->thumb_];
  }
  if (self->ext_ != nil) {
    [writer writeBytesWithInt:8 withByteArray:[self->ext_ buildContainer]];
  }
  if ([self getUnmappedObjects] != nil) {
    ImActorModelDroidkitBserUtilSparseArray *unmapped = [self getUnmappedObjects];
    for (jint i = 0; i < [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(unmapped)) size]; i++) {
      jint key = [unmapped keyAtWithInt:i];
      [writer writeUnmappedWithInt:key withId:[unmapped getWithInt:key]];
    }
  }
}

- (NSString *)description {
  NSString *res = @"struct DocumentMessage{";
  res = JreStrcat("$$", res, JreStrcat("$J", @"fileId=", self->fileId_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", fileSize=", self->fileSize_));
  res = JreStrcat("$$", res, JreStrcat("$$", @", name=", self->name_));
  res = JreStrcat("$$", res, JreStrcat("$$", @", mimeType=", self->mimeType_));
  res = JreStrcat("$$", res, JreStrcat("$$", @", thumb=", (self->thumb_ != nil ? @"set" : @"empty")));
  res = JreStrcat("$$", res, JreStrcat("$$", @", ext=", (self->ext_ != nil ? @"set" : @"empty")));
  res = JreStrcat("$C", res, '}');
  return res;
}

@end

void APDocumentMessage_initWithLong_withLong_withInt_withNSString_withNSString_withAPFastThumb_withAPDocumentEx_(APDocumentMessage *self, jlong fileId, jlong accessHash, jint fileSize, NSString *name, NSString *mimeType, APFastThumb *thumb, APDocumentEx *ext) {
  (void) APMessage_init(self);
  self->fileId_ = fileId;
  self->accessHash_ = accessHash;
  self->fileSize_ = fileSize;
  self->name_ = name;
  self->mimeType_ = mimeType;
  self->thumb_ = thumb;
  self->ext_ = ext;
}

APDocumentMessage *new_APDocumentMessage_initWithLong_withLong_withInt_withNSString_withNSString_withAPFastThumb_withAPDocumentEx_(jlong fileId, jlong accessHash, jint fileSize, NSString *name, NSString *mimeType, APFastThumb *thumb, APDocumentEx *ext) {
  APDocumentMessage *self = [APDocumentMessage alloc];
  APDocumentMessage_initWithLong_withLong_withInt_withNSString_withNSString_withAPFastThumb_withAPDocumentEx_(self, fileId, accessHash, fileSize, name, mimeType, thumb, ext);
  return self;
}

void APDocumentMessage_init(APDocumentMessage *self) {
  (void) APMessage_init(self);
}

APDocumentMessage *new_APDocumentMessage_init() {
  APDocumentMessage *self = [APDocumentMessage alloc];
  APDocumentMessage_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APDocumentMessage)
