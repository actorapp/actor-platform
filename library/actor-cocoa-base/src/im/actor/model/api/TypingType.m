//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/TypingType.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/TypingType.h"
#include "java/io/IOException.h"
#include "java/lang/Enum.h"
#include "java/lang/IllegalArgumentException.h"

@interface APTypingTypeEnum () {
 @public
  jint value_;
}

@end

__attribute__((unused)) static void APTypingTypeEnum_initWithInt_withNSString_withInt_(APTypingTypeEnum *self, jint value, NSString *__name, jint __ordinal);

__attribute__((unused)) static APTypingTypeEnum *new_APTypingTypeEnum_initWithInt_withNSString_withInt_(jint value, NSString *__name, jint __ordinal) NS_RETURNS_RETAINED;

J2OBJC_INITIALIZED_DEFN(APTypingTypeEnum)

APTypingTypeEnum *APTypingTypeEnum_values_[2];

@implementation APTypingTypeEnum

- (instancetype)initWithInt:(jint)value
               withNSString:(NSString *)__name
                    withInt:(jint)__ordinal {
  APTypingTypeEnum_initWithInt_withNSString_withInt_(self, value, __name, __ordinal);
  return self;
}

- (jint)getValue {
  return value_;
}

+ (APTypingTypeEnum *)parseWithInt:(jint)value {
  return APTypingTypeEnum_parseWithInt_(value);
}

IOSObjectArray *APTypingTypeEnum_values() {
  APTypingTypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:APTypingTypeEnum_values_ count:2 type:APTypingTypeEnum_class_()];
}

+ (IOSObjectArray *)values {
  return APTypingTypeEnum_values();
}

+ (APTypingTypeEnum *)valueOfWithNSString:(NSString *)name {
  return APTypingTypeEnum_valueOfWithNSString_(name);
}

APTypingTypeEnum *APTypingTypeEnum_valueOfWithNSString_(NSString *name) {
  APTypingTypeEnum_initialize();
  for (int i = 0; i < 2; i++) {
    APTypingTypeEnum *e = APTypingTypeEnum_values_[i];
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
  if (self == [APTypingTypeEnum class]) {
    APTypingTypeEnum_TEXT = new_APTypingTypeEnum_initWithInt_withNSString_withInt_(0, @"TEXT", 0);
    APTypingTypeEnum_UNSUPPORTED_VALUE = new_APTypingTypeEnum_initWithInt_withNSString_withInt_(-1, @"UNSUPPORTED_VALUE", 1);
    J2OBJC_SET_INITIALIZED(APTypingTypeEnum)
  }
}

@end

void APTypingTypeEnum_initWithInt_withNSString_withInt_(APTypingTypeEnum *self, jint value, NSString *__name, jint __ordinal) {
  (void) JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
  self->value_ = value;
}

APTypingTypeEnum *new_APTypingTypeEnum_initWithInt_withNSString_withInt_(jint value, NSString *__name, jint __ordinal) {
  APTypingTypeEnum *self = [APTypingTypeEnum alloc];
  APTypingTypeEnum_initWithInt_withNSString_withInt_(self, value, __name, __ordinal);
  return self;
}

APTypingTypeEnum *APTypingTypeEnum_parseWithInt_(jint value) {
  APTypingTypeEnum_initialize();
  switch (value) {
    case 0:
    return APTypingTypeEnum_TEXT;
    default:
    return APTypingTypeEnum_UNSUPPORTED_VALUE;
  }
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(APTypingTypeEnum)
