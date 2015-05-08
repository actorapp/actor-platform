//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/PeerType.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/entity/PeerType.h"
#include "java/lang/Enum.h"
#include "java/lang/IllegalArgumentException.h"

__attribute__((unused)) static void AMPeerTypeEnum_initWithNSString_withInt_(AMPeerTypeEnum *self, NSString *__name, jint __ordinal);

__attribute__((unused)) static AMPeerTypeEnum *new_AMPeerTypeEnum_initWithNSString_withInt_(NSString *__name, jint __ordinal) NS_RETURNS_RETAINED;

J2OBJC_INITIALIZED_DEFN(AMPeerTypeEnum)

AMPeerTypeEnum *AMPeerTypeEnum_values_[3];

@implementation AMPeerTypeEnum

- (instancetype)initWithNSString:(NSString *)__name
                         withInt:(jint)__ordinal {
  AMPeerTypeEnum_initWithNSString_withInt_(self, __name, __ordinal);
  return self;
}

IOSObjectArray *AMPeerTypeEnum_values() {
  AMPeerTypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:AMPeerTypeEnum_values_ count:3 type:AMPeerTypeEnum_class_()];
}

+ (IOSObjectArray *)values {
  return AMPeerTypeEnum_values();
}

+ (AMPeerTypeEnum *)valueOfWithNSString:(NSString *)name {
  return AMPeerTypeEnum_valueOfWithNSString_(name);
}

AMPeerTypeEnum *AMPeerTypeEnum_valueOfWithNSString_(NSString *name) {
  AMPeerTypeEnum_initialize();
  for (int i = 0; i < 3; i++) {
    AMPeerTypeEnum *e = AMPeerTypeEnum_values_[i];
    if ([name isEqual:[e name]]) {
      return e;
    }
  }
  @throw [[JavaLangIllegalArgumentException alloc] initWithNSString:name];
  return nil;
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

+ (void)initialize {
  if (self == [AMPeerTypeEnum class]) {
    AMPeerTypeEnum_PRIVATE = new_AMPeerTypeEnum_initWithNSString_withInt_(@"PRIVATE", 0);
    AMPeerTypeEnum_GROUP = new_AMPeerTypeEnum_initWithNSString_withInt_(@"GROUP", 1);
    AMPeerTypeEnum_EMAIL = new_AMPeerTypeEnum_initWithNSString_withInt_(@"EMAIL", 2);
    J2OBJC_SET_INITIALIZED(AMPeerTypeEnum)
  }
}

@end

void AMPeerTypeEnum_initWithNSString_withInt_(AMPeerTypeEnum *self, NSString *__name, jint __ordinal) {
  (void) JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
}

AMPeerTypeEnum *new_AMPeerTypeEnum_initWithNSString_withInt_(NSString *__name, jint __ordinal) {
  AMPeerTypeEnum *self = [AMPeerTypeEnum alloc];
  AMPeerTypeEnum_initWithNSString_withInt_(self, __name, __ordinal);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMPeerTypeEnum)
