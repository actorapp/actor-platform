//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/entity/AvatarImage.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/AvatarImage.h"
#include "im/actor/model/api/FileLocation.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/entity/AvatarImage.h"
#include "im/actor/model/entity/FileReference.h"
#include "im/actor/model/entity/WrapperEntity.h"
#include "im/actor/model/entity/compat/ObsoleteAvatarImage.h"
#include "im/actor/model/entity/compat/ObsoleteFileReference.h"
#include "java/io/IOException.h"

#define AMAvatarImage_RECORD_ID 10

@interface AMAvatarImage () {
 @public
  jint width_;
  jint height_;
  AMFileReference *fileReference_;
}

@end

J2OBJC_FIELD_SETTER(AMAvatarImage, fileReference_, AMFileReference *)

J2OBJC_STATIC_FIELD_GETTER(AMAvatarImage, RECORD_ID, jint)

@implementation AMAvatarImage

- (instancetype)initWithAPAvatarImage:(APAvatarImage *)wrapped {
  AMAvatarImage_initWithAPAvatarImage_(self, wrapped);
  return self;
}

- (instancetype)initWithByteArray:(IOSByteArray *)data {
  AMAvatarImage_initWithByteArray_(self, data);
  return self;
}

- (jint)getWidth {
  return width_;
}

- (jint)getHeight {
  return height_;
}

- (AMFileReference *)getFileReference {
  return fileReference_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  if ([((BSBserValues *) nil_chk(values)) getBoolWithInt:5 withBoolean:NO]) {
    [super parseWithBSBserValues:values];
  }
  else {
    ImActorModelEntityCompatObsoleteAvatarImage *obsoleteAvatarImage = new_ImActorModelEntityCompatObsoleteAvatarImage_initWithBSBserValues_(values);
    [self setWrappedWithBSBserObject:new_APAvatarImage_initWithAPFileLocation_withInt_withInt_withInt_(new_APFileLocation_initWithLong_withLong_([((ImActorModelEntityCompatObsoleteFileReference *) nil_chk([obsoleteAvatarImage getFileReference])) getFileId], [((ImActorModelEntityCompatObsoleteFileReference *) nil_chk([obsoleteAvatarImage getFileReference])) getAccessHash]), [obsoleteAvatarImage getWidth], [obsoleteAvatarImage getHeight], [((ImActorModelEntityCompatObsoleteFileReference *) nil_chk([obsoleteAvatarImage getFileReference])) getFileSize])];
  }
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeBoolWithInt:5 withBoolean:YES];
  [super serializeWithBSBserWriter:writer];
}

- (void)applyWrappedWithBSBserObject:(APAvatarImage *)wrapped {
  self->width_ = [((APAvatarImage *) nil_chk(wrapped)) getWidth];
  self->height_ = [wrapped getHeight];
  self->fileReference_ = new_AMFileReference_initWithAPFileLocation_withNSString_withInt_([wrapped getFileLocation], @"avatar.jpg", [wrapped getFileSize]);
}

- (jboolean)isEqual:(id)o {
  if (self == o) return YES;
  if (o == nil || [self getClass] != [o getClass]) return NO;
  AMAvatarImage *that = (AMAvatarImage *) check_class_cast(o, [AMAvatarImage class]);
  if (height_ != ((AMAvatarImage *) nil_chk(that))->height_) return NO;
  if (width_ != that->width_) return NO;
  if (![((AMFileReference *) nil_chk(fileReference_)) isEqual:that->fileReference_]) return NO;
  return YES;
}

- (NSUInteger)hash {
  jint result = width_;
  result = 31 * result + height_;
  result = 31 * result + ((jint) [((AMFileReference *) nil_chk(fileReference_)) hash]);
  return result;
}

- (APAvatarImage *)createInstance {
  return new_APAvatarImage_init();
}

@end

void AMAvatarImage_initWithAPAvatarImage_(AMAvatarImage *self, APAvatarImage *wrapped) {
  (void) AMWrapperEntity_initWithInt_withBSBserObject_(self, AMAvatarImage_RECORD_ID, wrapped);
}

AMAvatarImage *new_AMAvatarImage_initWithAPAvatarImage_(APAvatarImage *wrapped) {
  AMAvatarImage *self = [AMAvatarImage alloc];
  AMAvatarImage_initWithAPAvatarImage_(self, wrapped);
  return self;
}

void AMAvatarImage_initWithByteArray_(AMAvatarImage *self, IOSByteArray *data) {
  (void) AMWrapperEntity_initWithInt_withByteArray_(self, AMAvatarImage_RECORD_ID, data);
}

AMAvatarImage *new_AMAvatarImage_initWithByteArray_(IOSByteArray *data) {
  AMAvatarImage *self = [AMAvatarImage alloc];
  AMAvatarImage_initWithByteArray_(self, data);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMAvatarImage)
