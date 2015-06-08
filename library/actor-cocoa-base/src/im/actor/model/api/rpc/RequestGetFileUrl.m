//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestGetFileUrl.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/FileLocation.h"
#include "im/actor/model/api/rpc/RequestGetFileUrl.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@interface APRequestGetFileUrl () {
 @public
  APFileLocation *file_;
}

@end

J2OBJC_FIELD_SETTER(APRequestGetFileUrl, file_, APFileLocation *)

@implementation APRequestGetFileUrl

+ (APRequestGetFileUrl *)fromBytesWithByteArray:(IOSByteArray *)data {
  return APRequestGetFileUrl_fromBytesWithByteArray_(data);
}

- (instancetype)initWithAPFileLocation:(APFileLocation *)file {
  APRequestGetFileUrl_initWithAPFileLocation_(self, file);
  return self;
}

- (instancetype)init {
  APRequestGetFileUrl_init(self);
  return self;
}

- (APFileLocation *)getFile {
  return self->file_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->file_ = [((BSBserValues *) nil_chk(values)) getObjWithInt:1 withBSBserObject:new_APFileLocation_init()];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->file_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:1 withBSBserObject:self->file_];
}

- (NSString *)description {
  NSString *res = @"rpc GetFileUrl{";
  res = JreStrcat("$$", res, JreStrcat("$@", @"file=", self->file_));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return APRequestGetFileUrl_HEADER;
}

@end

APRequestGetFileUrl *APRequestGetFileUrl_fromBytesWithByteArray_(IOSByteArray *data) {
  APRequestGetFileUrl_initialize();
  return ((APRequestGetFileUrl *) BSBser_parseWithBSBserObject_withByteArray_(new_APRequestGetFileUrl_init(), data));
}

void APRequestGetFileUrl_initWithAPFileLocation_(APRequestGetFileUrl *self, APFileLocation *file) {
  (void) APRequest_init(self);
  self->file_ = file;
}

APRequestGetFileUrl *new_APRequestGetFileUrl_initWithAPFileLocation_(APFileLocation *file) {
  APRequestGetFileUrl *self = [APRequestGetFileUrl alloc];
  APRequestGetFileUrl_initWithAPFileLocation_(self, file);
  return self;
}

void APRequestGetFileUrl_init(APRequestGetFileUrl *self) {
  (void) APRequest_init(self);
}

APRequestGetFileUrl *new_APRequestGetFileUrl_init() {
  APRequestGetFileUrl *self = [APRequestGetFileUrl alloc];
  APRequestGetFileUrl_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APRequestGetFileUrl)
