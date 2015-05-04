//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/TextMessageEx.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/TextMessageEx.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/TextMessageEx.h"
#include "im/actor/model/api/TextMessageExUnsupported.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserParser.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/droidkit/bser/DataInput.h"
#include "im/actor/model/droidkit/bser/DataOutput.h"
#include "im/actor/model/droidkit/bser/util/SparseArray.h"
#include "java/io/IOException.h"

#pragma clang diagnostic ignored "-Wprotocol"
#pragma clang diagnostic ignored "-Wincomplete-implementation"


#line 23
@implementation ImActorModelApiTextMessageEx


#line 24
+ (ImActorModelApiTextMessageEx *)fromBytesWithByteArray:(IOSByteArray *)src {
  return ImActorModelApiTextMessageEx_fromBytesWithByteArray_(src);
}


#line 34
- (IOSByteArray *)buildContainer {
  BSDataOutput *res = new_BSDataOutput_init();
  BSBserWriter *writer = new_BSBserWriter_initWithBSDataOutput_(res);
  [writer writeIntWithInt:1 withInt:[self getHeader]];
  [writer writeBytesWithInt:2 withByteArray:[self toByteArray]];
  return [res toByteArray];
}

- (instancetype)init {
  ImActorModelApiTextMessageEx_init(self);
  return self;
}

@end


#line 24
ImActorModelApiTextMessageEx *ImActorModelApiTextMessageEx_fromBytesWithByteArray_(IOSByteArray *src) {
  ImActorModelApiTextMessageEx_initialize();
  
#line 25
  BSBserValues *values = new_BSBserValues_initWithImActorModelDroidkitBserUtilSparseArray_(BSBserParser_deserializeWithBSDataInput_(new_BSDataInput_initWithByteArray_withInt_withInt_(src, 0, ((IOSByteArray *) nil_chk(src))->size_)));
  jint key = [values getIntWithInt:1];
  IOSByteArray *content = [values getBytesWithInt:2];
  switch (key) {
    default:
    
#line 29
    return new_ImActorModelApiTextMessageExUnsupported_initWithInt_withByteArray_(key, content);
  }
}

void ImActorModelApiTextMessageEx_init(ImActorModelApiTextMessageEx *self) {
  (void) BSBserObject_init(self);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiTextMessageEx)
