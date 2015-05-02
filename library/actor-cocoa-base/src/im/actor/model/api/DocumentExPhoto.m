//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/DocumentExPhoto.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/DocumentExPhoto.java"

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/DocumentEx.h"
#include "im/actor/model/api/DocumentExPhoto.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"

@interface ImActorModelApiDocumentExPhoto () {
 @public
  jint w_;
  jint h_;
}

@end


#line 19
@implementation ImActorModelApiDocumentExPhoto


#line 24
- (instancetype)initWithInt:(jint)w
                    withInt:(jint)h {
  ImActorModelApiDocumentExPhoto_initWithInt_withInt_(self, w, h);
  return self;
}


#line 29
- (instancetype)init {
  ImActorModelApiDocumentExPhoto_init(self);
  return self;
}


#line 33
- (jint)getHeader {
  return 1;
}

- (jint)getW {
  return self->w_;
}

- (jint)getH {
  return self->h_;
}


#line 46
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->w_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->h_ = [values getIntWithInt:2];
}


#line 52
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->w_];
  [writer writeIntWithInt:2 withInt:self->h_];
}


#line 58
- (NSString *)description {
  NSString *res = @"struct DocumentExPhoto{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"w=", self->w_));
  res = JreStrcat("$$", res, JreStrcat("$I", @", h=", self->h_));
  res = JreStrcat("$C", res, '}');
  return res;
}

@end


#line 24
void ImActorModelApiDocumentExPhoto_initWithInt_withInt_(ImActorModelApiDocumentExPhoto *self, jint w, jint h) {
  (void) ImActorModelApiDocumentEx_init(self);
  
#line 25
  self->w_ = w;
  self->h_ = h;
}


#line 24
ImActorModelApiDocumentExPhoto *new_ImActorModelApiDocumentExPhoto_initWithInt_withInt_(jint w, jint h) {
  ImActorModelApiDocumentExPhoto *self = [ImActorModelApiDocumentExPhoto alloc];
  ImActorModelApiDocumentExPhoto_initWithInt_withInt_(self, w, h);
  return self;
}


#line 29
void ImActorModelApiDocumentExPhoto_init(ImActorModelApiDocumentExPhoto *self) {
  (void) ImActorModelApiDocumentEx_init(self);
}


#line 29
ImActorModelApiDocumentExPhoto *new_ImActorModelApiDocumentExPhoto_init() {
  ImActorModelApiDocumentExPhoto *self = [ImActorModelApiDocumentExPhoto alloc];
  ImActorModelApiDocumentExPhoto_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiDocumentExPhoto)
