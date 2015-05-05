//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/DocumentExVoice.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/DocumentExVoice.java"

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/DocumentEx.h"
#include "im/actor/model/api/DocumentExVoice.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"

@interface ImActorModelApiDocumentExVoice () {
 @public
  jint duration_;
}

@end


#line 23
@implementation ImActorModelApiDocumentExVoice


#line 27
- (instancetype)initWithInt:(jint)duration {
  ImActorModelApiDocumentExVoice_initWithInt_(self, duration);
  return self;
}


#line 31
- (instancetype)init {
  ImActorModelApiDocumentExVoice_init(self);
  return self;
}


#line 35
- (jint)getHeader {
  return 3;
}

- (jint)getDuration {
  return self->duration_;
}


#line 44
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->duration_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
}


#line 49
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->duration_];
}


#line 54
- (NSString *)description {
  NSString *res = @"struct DocumentExVoice{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"duration=", self->duration_));
  res = JreStrcat("$C", res, '}');
  return res;
}

@end


#line 27
void ImActorModelApiDocumentExVoice_initWithInt_(ImActorModelApiDocumentExVoice *self, jint duration) {
  (void) ImActorModelApiDocumentEx_init(self);
  
#line 28
  self->duration_ = duration;
}


#line 27
ImActorModelApiDocumentExVoice *new_ImActorModelApiDocumentExVoice_initWithInt_(jint duration) {
  ImActorModelApiDocumentExVoice *self = [ImActorModelApiDocumentExVoice alloc];
  ImActorModelApiDocumentExVoice_initWithInt_(self, duration);
  return self;
}


#line 31
void ImActorModelApiDocumentExVoice_init(ImActorModelApiDocumentExVoice *self) {
  (void) ImActorModelApiDocumentEx_init(self);
}


#line 31
ImActorModelApiDocumentExVoice *new_ImActorModelApiDocumentExVoice_init() {
  ImActorModelApiDocumentExVoice *self = [ImActorModelApiDocumentExVoice alloc];
  ImActorModelApiDocumentExVoice_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiDocumentExVoice)
