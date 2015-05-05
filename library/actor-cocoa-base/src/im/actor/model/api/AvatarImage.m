//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/AvatarImage.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/AvatarImage.java"

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/AvatarImage.h"
#include "im/actor/model/api/FileLocation.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"

@interface ImActorModelApiAvatarImage () {
 @public
  ImActorModelApiFileLocation *fileLocation_;
  jint width_;
  jint height_;
  jint fileSize_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiAvatarImage, fileLocation_, ImActorModelApiFileLocation *)


#line 23
@implementation ImActorModelApiAvatarImage


#line 30
- (instancetype)initWithImActorModelApiFileLocation:(ImActorModelApiFileLocation *)fileLocation
                                            withInt:(jint)width
                                            withInt:(jint)height
                                            withInt:(jint)fileSize {
  ImActorModelApiAvatarImage_initWithImActorModelApiFileLocation_withInt_withInt_withInt_(self, fileLocation, width, height, fileSize);
  return self;
}


#line 37
- (instancetype)init {
  ImActorModelApiAvatarImage_init(self);
  return self;
}


#line 41
- (ImActorModelApiFileLocation *)getFileLocation {
  return self->fileLocation_;
}

- (jint)getWidth {
  return self->width_;
}

- (jint)getHeight {
  return self->height_;
}

- (jint)getFileSize {
  return self->fileSize_;
}


#line 58
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->fileLocation_ = [((BSBserValues *) nil_chk(values)) getObjWithInt:1 withBSBserObject:new_ImActorModelApiFileLocation_init()];
  self->width_ = [values getIntWithInt:2];
  self->height_ = [values getIntWithInt:3];
  self->fileSize_ = [values getIntWithInt:4];
}


#line 66
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->fileLocation_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:1 withBSBserObject:self->fileLocation_];
  [writer writeIntWithInt:2 withInt:self->width_];
  [writer writeIntWithInt:3 withInt:self->height_];
  [writer writeIntWithInt:4 withInt:self->fileSize_];
}


#line 77
- (NSString *)description {
  NSString *res = @"struct AvatarImage{";
  res = JreStrcat("$$", res, JreStrcat("$@", @"fileLocation=", self->fileLocation_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", width=", self->width_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", height=", self->height_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", fileSize=", self->fileSize_));
  res = JreStrcat("$C", res, '}');
  return res;
}

@end


#line 30
void ImActorModelApiAvatarImage_initWithImActorModelApiFileLocation_withInt_withInt_withInt_(ImActorModelApiAvatarImage *self, ImActorModelApiFileLocation *fileLocation, jint width, jint height, jint fileSize) {
  (void) BSBserObject_init(self);
  
#line 31
  self->fileLocation_ = fileLocation;
  self->width_ = width;
  self->height_ = height;
  self->fileSize_ = fileSize;
}


#line 30
ImActorModelApiAvatarImage *new_ImActorModelApiAvatarImage_initWithImActorModelApiFileLocation_withInt_withInt_withInt_(ImActorModelApiFileLocation *fileLocation, jint width, jint height, jint fileSize) {
  ImActorModelApiAvatarImage *self = [ImActorModelApiAvatarImage alloc];
  ImActorModelApiAvatarImage_initWithImActorModelApiFileLocation_withInt_withInt_withInt_(self, fileLocation, width, height, fileSize);
  return self;
}


#line 37
void ImActorModelApiAvatarImage_init(ImActorModelApiAvatarImage *self) {
  (void) BSBserObject_init(self);
}


#line 37
ImActorModelApiAvatarImage *new_ImActorModelApiAvatarImage_init() {
  ImActorModelApiAvatarImage *self = [ImActorModelApiAvatarImage alloc];
  ImActorModelApiAvatarImage_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiAvatarImage)
