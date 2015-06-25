//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/api/ServiceExPhoneMissed.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/ServiceEx.h"
#include "im/actor/model/api/ServiceExPhoneMissed.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/droidkit/bser/util/SparseArray.h"
#include "java/io/IOException.h"

@implementation APServiceExPhoneMissed

- (instancetype)init {
  APServiceExPhoneMissed_init(self);
  return self;
}

- (jint)getHeader {
  return 9;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  if ([((BSBserValues *) nil_chk(values)) hasRemaining]) {
    [self setUnmappedObjectsWithImActorModelDroidkitBserUtilSparseArray:[values buildRemaining]];
  }
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  if ([self getUnmappedObjects] != nil) {
    ImActorModelDroidkitBserUtilSparseArray *unmapped = [self getUnmappedObjects];
    for (jint i = 0; i < [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(unmapped)) size]; i++) {
      jint key = [unmapped keyAtWithInt:i];
      [((BSBserWriter *) nil_chk(writer)) writeUnmappedWithInt:key withId:[unmapped getWithInt:key]];
    }
  }
}

- (NSString *)description {
  NSString *res = @"struct ServiceExPhoneMissed{";
  res = JreStrcat("$C", res, '}');
  return res;
}

@end

void APServiceExPhoneMissed_init(APServiceExPhoneMissed *self) {
  (void) APServiceEx_init(self);
}

APServiceExPhoneMissed *new_APServiceExPhoneMissed_init() {
  APServiceExPhoneMissed *self = [APServiceExPhoneMissed alloc];
  APServiceExPhoneMissed_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APServiceExPhoneMissed)
