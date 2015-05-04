//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/entity/ProtoObject.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/network/mtp/entity/ProtoObject.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/DataInput.h"
#include "im/actor/model/droidkit/bser/DataOutput.h"
#include "im/actor/model/network/mtp/entity/ProtoObject.h"
#include "java/io/IOException.h"

#pragma clang diagnostic ignored "-Wprotocol"
#pragma clang diagnostic ignored "-Wincomplete-implementation"


#line 12
@implementation MTProtoObject

- (instancetype)initWithBSDataInput:(BSDataInput *)stream {
  MTProtoObject_initWithBSDataInput_(self, stream);
  return self;
}


#line 18
- (instancetype)init {
  MTProtoObject_init(self);
  return self;
}


#line 26
- (IOSByteArray *)toByteArray {
  BSDataOutput *outputStream = new_BSDataOutput_init();
  @try {
    [self writeObjectWithBSDataOutput:outputStream];
  }
  @catch (
#line 30
  JavaIoIOException *e) {
    [((JavaIoIOException *) nil_chk(e)) printStackTrace];
  }
  return [outputStream toByteArray];
}

@end


#line 14
void MTProtoObject_initWithBSDataInput_(MTProtoObject *self, BSDataInput *stream) {
  (void) NSObject_init(self);
  
#line 15
  (void) [self readObjectWithBSDataInput:stream];
}


#line 18
void MTProtoObject_init(MTProtoObject *self) {
  (void) NSObject_init(self);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(MTProtoObject)
