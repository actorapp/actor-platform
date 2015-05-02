//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/updates/UpdateParameterChanged.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/updates/UpdateParameterChanged.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/updates/UpdateParameterChanged.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Update.h"
#include "java/io/IOException.h"

@interface ImActorModelApiUpdatesUpdateParameterChanged () {
 @public
  NSString *key_;
  NSString *value_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelApiUpdatesUpdateParameterChanged, key_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelApiUpdatesUpdateParameterChanged, value_, NSString *)


#line 20
@implementation ImActorModelApiUpdatesUpdateParameterChanged


#line 23
+ (ImActorModelApiUpdatesUpdateParameterChanged *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiUpdatesUpdateParameterChanged_fromBytesWithByteArray_(data);
}


#line 30
- (instancetype)initWithNSString:(NSString *)key
                    withNSString:(NSString *)value {
  ImActorModelApiUpdatesUpdateParameterChanged_initWithNSString_withNSString_(self, key, value);
  return self;
}


#line 35
- (instancetype)init {
  ImActorModelApiUpdatesUpdateParameterChanged_init(self);
  return self;
}


#line 39
- (NSString *)getKey {
  return self->key_;
}

- (NSString *)getValue {
  return self->value_;
}


#line 48
- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->key_ = [((BSBserValues *) nil_chk(values)) getStringWithInt:1];
  self->value_ = [values optStringWithInt:2];
}


#line 54
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if (self->key_ == nil) {
    @throw new_JavaIoIOException_init();
  }
  [((BSBserWriter *) nil_chk(writer)) writeStringWithInt:1 withNSString:self->key_];
  if (self->value_ != nil) {
    [writer writeStringWithInt:2 withNSString:self->value_];
  }
}


#line 65
- (NSString *)description {
  NSString *res = @"update ParameterChanged{";
  res = JreStrcat("$C", res, '}');
  return res;
}


#line 72
- (jint)getHeaderKey {
  return ImActorModelApiUpdatesUpdateParameterChanged_HEADER;
}

@end


#line 23
ImActorModelApiUpdatesUpdateParameterChanged *ImActorModelApiUpdatesUpdateParameterChanged_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiUpdatesUpdateParameterChanged_initialize();
  
#line 24
  return ((ImActorModelApiUpdatesUpdateParameterChanged *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiUpdatesUpdateParameterChanged_init(), data));
}

void ImActorModelApiUpdatesUpdateParameterChanged_initWithNSString_withNSString_(ImActorModelApiUpdatesUpdateParameterChanged *self, NSString *key, NSString *value) {
  (void) ImActorModelNetworkParserUpdate_init(self);
  
#line 31
  self->key_ = key;
  self->value_ = value;
}


#line 30
ImActorModelApiUpdatesUpdateParameterChanged *new_ImActorModelApiUpdatesUpdateParameterChanged_initWithNSString_withNSString_(NSString *key, NSString *value) {
  ImActorModelApiUpdatesUpdateParameterChanged *self = [ImActorModelApiUpdatesUpdateParameterChanged alloc];
  ImActorModelApiUpdatesUpdateParameterChanged_initWithNSString_withNSString_(self, key, value);
  return self;
}


#line 35
void ImActorModelApiUpdatesUpdateParameterChanged_init(ImActorModelApiUpdatesUpdateParameterChanged *self) {
  (void) ImActorModelNetworkParserUpdate_init(self);
}


#line 35
ImActorModelApiUpdatesUpdateParameterChanged *new_ImActorModelApiUpdatesUpdateParameterChanged_init() {
  ImActorModelApiUpdatesUpdateParameterChanged *self = [ImActorModelApiUpdatesUpdateParameterChanged alloc];
  ImActorModelApiUpdatesUpdateParameterChanged_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiUpdatesUpdateParameterChanged)
