//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core-async-ui/src/main/java/im/actor/model/cocoa/CocoaThreadingProvider.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/cocoa/CocoaThreadingProvider.h"
#include "im/actor/model/jvm/JavaThreadingProvider.h"

@implementation AMCocoaThreadingProvider

- (instancetype)init {
  AMCocoaThreadingProvider_init(self);
  return self;
}

@end

void AMCocoaThreadingProvider_init(AMCocoaThreadingProvider *self) {
  (void) AMJavaThreadingProvider_init(self);
}

AMCocoaThreadingProvider *new_AMCocoaThreadingProvider_init() {
  AMCocoaThreadingProvider *self = [AMCocoaThreadingProvider alloc];
  AMCocoaThreadingProvider_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMCocoaThreadingProvider)
