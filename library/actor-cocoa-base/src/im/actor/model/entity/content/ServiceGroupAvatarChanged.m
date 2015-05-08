//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/content/ServiceGroupAvatarChanged.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/entity/Avatar.h"
#include "im/actor/model/entity/content/AbsContent.h"
#include "im/actor/model/entity/content/ServiceContent.h"
#include "im/actor/model/entity/content/ServiceGroupAvatarChanged.h"
#include "java/io/IOException.h"

@interface AMServiceGroupAvatarChanged () {
 @public
  AMAvatar *newAvatar_;
}

- (instancetype)init;

@end

J2OBJC_FIELD_SETTER(AMServiceGroupAvatarChanged, newAvatar_, AMAvatar *)

__attribute__((unused)) static void AMServiceGroupAvatarChanged_init(AMServiceGroupAvatarChanged *self);

__attribute__((unused)) static AMServiceGroupAvatarChanged *new_AMServiceGroupAvatarChanged_init() NS_RETURNS_RETAINED;

@implementation AMServiceGroupAvatarChanged

+ (AMServiceGroupAvatarChanged *)fromBytesWithByteArray:(IOSByteArray *)data {
  return AMServiceGroupAvatarChanged_fromBytesWithByteArray_(data);
}

- (instancetype)initWithAMAvatar:(AMAvatar *)newAvatar {
  AMServiceGroupAvatarChanged_initWithAMAvatar_(self, newAvatar);
  return self;
}

- (instancetype)init {
  AMServiceGroupAvatarChanged_init(self);
  return self;
}

- (AMAvatar *)getNewAvatar {
  return newAvatar_;
}

- (AMAbsContent_ContentTypeEnum *)getContentType {
  return AMAbsContent_ContentTypeEnum_get_SERVICE_AVATAR();
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  [super parseWithBSBserValues:values];
  IOSByteArray *data = [((BSBserValues *) nil_chk(values)) optBytesWithInt:10];
  if (data != nil) {
    newAvatar_ = AMAvatar_fromBytesWithByteArray_(data);
  }
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [super serializeWithBSBserWriter:writer];
  if (newAvatar_ != nil) {
    [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:10 withBSBserObject:newAvatar_];
  }
}

@end

AMServiceGroupAvatarChanged *AMServiceGroupAvatarChanged_fromBytesWithByteArray_(IOSByteArray *data) {
  AMServiceGroupAvatarChanged_initialize();
  return ((AMServiceGroupAvatarChanged *) BSBser_parseWithBSBserObject_withByteArray_(new_AMServiceGroupAvatarChanged_init(), data));
}

void AMServiceGroupAvatarChanged_initWithAMAvatar_(AMServiceGroupAvatarChanged *self, AMAvatar *newAvatar) {
  (void) AMServiceContent_initWithNSString_(self, @"Group avatar changed");
  self->newAvatar_ = newAvatar;
}

AMServiceGroupAvatarChanged *new_AMServiceGroupAvatarChanged_initWithAMAvatar_(AMAvatar *newAvatar) {
  AMServiceGroupAvatarChanged *self = [AMServiceGroupAvatarChanged alloc];
  AMServiceGroupAvatarChanged_initWithAMAvatar_(self, newAvatar);
  return self;
}

void AMServiceGroupAvatarChanged_init(AMServiceGroupAvatarChanged *self) {
  (void) AMServiceContent_init(self);
}

AMServiceGroupAvatarChanged *new_AMServiceGroupAvatarChanged_init() {
  AMServiceGroupAvatarChanged *self = [AMServiceGroupAvatarChanged alloc];
  AMServiceGroupAvatarChanged_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMServiceGroupAvatarChanged)
