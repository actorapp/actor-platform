//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/entity/compat/ObsoleteFileReference.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/entity/compat/ObsoleteFileReference.h"
#include "java/io/IOException.h"
#include "java/lang/UnsupportedOperationException.h"

@interface ImActorModelEntityCompatObsoleteFileReference () {
 @public
  jlong fileId_;
  jlong accessHash_;
  jint fileSize_;
  NSString *fileName_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelEntityCompatObsoleteFileReference, fileName_, NSString *)

@implementation ImActorModelEntityCompatObsoleteFileReference

- (instancetype)initWithByteArray:(IOSByteArray *)data {
  ImActorModelEntityCompatObsoleteFileReference_initWithByteArray_(self, data);
  return self;
}

- (instancetype)initWithBSBserValues:(BSBserValues *)values {
  ImActorModelEntityCompatObsoleteFileReference_initWithBSBserValues_(self, values);
  return self;
}

- (jlong)getFileId {
  return fileId_;
}

- (jlong)getAccessHash {
  return accessHash_;
}

- (jint)getFileSize {
  return fileSize_;
}

- (NSString *)getFileName {
  return fileName_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  fileId_ = [((BSBserValues *) nil_chk(values)) getLongWithInt:1];
  accessHash_ = [values getLongWithInt:2];
  fileSize_ = [values getIntWithInt:3];
  fileName_ = [values getStringWithInt:4];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  @throw new_JavaLangUnsupportedOperationException_init();
}

@end

void ImActorModelEntityCompatObsoleteFileReference_initWithByteArray_(ImActorModelEntityCompatObsoleteFileReference *self, IOSByteArray *data) {
  (void) BSBserObject_init(self);
  [self load__WithByteArray:data];
}

ImActorModelEntityCompatObsoleteFileReference *new_ImActorModelEntityCompatObsoleteFileReference_initWithByteArray_(IOSByteArray *data) {
  ImActorModelEntityCompatObsoleteFileReference *self = [ImActorModelEntityCompatObsoleteFileReference alloc];
  ImActorModelEntityCompatObsoleteFileReference_initWithByteArray_(self, data);
  return self;
}

void ImActorModelEntityCompatObsoleteFileReference_initWithBSBserValues_(ImActorModelEntityCompatObsoleteFileReference *self, BSBserValues *values) {
  (void) BSBserObject_init(self);
  [self parseWithBSBserValues:values];
}

ImActorModelEntityCompatObsoleteFileReference *new_ImActorModelEntityCompatObsoleteFileReference_initWithBSBserValues_(BSBserValues *values) {
  ImActorModelEntityCompatObsoleteFileReference *self = [ImActorModelEntityCompatObsoleteFileReference alloc];
  ImActorModelEntityCompatObsoleteFileReference_initWithBSBserValues_(self, values);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelEntityCompatObsoleteFileReference)
