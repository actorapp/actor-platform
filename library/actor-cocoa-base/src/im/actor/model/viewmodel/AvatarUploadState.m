//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/viewmodel/AvatarUploadState.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/viewmodel/AvatarUploadState.java"

#include "J2ObjC_source.h"
#include "im/actor/model/viewmodel/AvatarUploadState.h"

@interface AMAvatarUploadState () {
 @public
  NSString *descriptor_;
  jboolean isUploading__;
}

@end

J2OBJC_FIELD_SETTER(AMAvatarUploadState, descriptor_, NSString *)


#line 6
@implementation AMAvatarUploadState


#line 10
- (instancetype)initWithNSString:(NSString *)descriptor
                     withBoolean:(jboolean)isUploading {
  AMAvatarUploadState_initWithNSString_withBoolean_(self, descriptor, isUploading);
  return self;
}


#line 20
- (NSString *)getDescriptor {
  return descriptor_;
}


#line 29
- (jboolean)isUploading {
  return isUploading__;
}

@end


#line 10
void AMAvatarUploadState_initWithNSString_withBoolean_(AMAvatarUploadState *self, NSString *descriptor, jboolean isUploading) {
  (void) NSObject_init(self);
  
#line 11
  self->descriptor_ = descriptor;
  self->isUploading__ = isUploading;
}


#line 10
AMAvatarUploadState *new_AMAvatarUploadState_initWithNSString_withBoolean_(NSString *descriptor, jboolean isUploading) {
  AMAvatarUploadState *self = [AMAvatarUploadState alloc];
  AMAvatarUploadState_initWithNSString_withBoolean_(self, descriptor, isUploading);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMAvatarUploadState)
