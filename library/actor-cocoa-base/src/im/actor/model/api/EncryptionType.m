//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/EncryptionType.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/EncryptionType.h"
#include "java/io/IOException.h"
#include "java/lang/Enum.h"
#include "java/lang/IllegalArgumentException.h"

@interface ImActorModelApiEncryptionTypeEnum () {
 @public
  jint value_;
}

@end

__attribute__((unused)) static void ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(ImActorModelApiEncryptionTypeEnum *self, jint value, NSString *__name, jint __ordinal);

__attribute__((unused)) static ImActorModelApiEncryptionTypeEnum *new_ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(jint value, NSString *__name, jint __ordinal) NS_RETURNS_RETAINED;

J2OBJC_INITIALIZED_DEFN(ImActorModelApiEncryptionTypeEnum)

ImActorModelApiEncryptionTypeEnum *ImActorModelApiEncryptionTypeEnum_values_[4];

@implementation ImActorModelApiEncryptionTypeEnum

- (instancetype)initWithInt:(jint)value
               withNSString:(NSString *)__name
                    withInt:(jint)__ordinal {
  ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(self, value, __name, __ordinal);
  return self;
}

- (jint)getValue {
  return value_;
}

+ (ImActorModelApiEncryptionTypeEnum *)parseWithInt:(jint)value {
  return ImActorModelApiEncryptionTypeEnum_parseWithInt_(value);
}

IOSObjectArray *ImActorModelApiEncryptionTypeEnum_values() {
  ImActorModelApiEncryptionTypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:ImActorModelApiEncryptionTypeEnum_values_ count:4 type:ImActorModelApiEncryptionTypeEnum_class_()];
}

+ (IOSObjectArray *)values {
  return ImActorModelApiEncryptionTypeEnum_values();
}

+ (ImActorModelApiEncryptionTypeEnum *)valueOfWithNSString:(NSString *)name {
  return ImActorModelApiEncryptionTypeEnum_valueOfWithNSString_(name);
}

ImActorModelApiEncryptionTypeEnum *ImActorModelApiEncryptionTypeEnum_valueOfWithNSString_(NSString *name) {
  ImActorModelApiEncryptionTypeEnum_initialize();
  for (int i = 0; i < 4; i++) {
    ImActorModelApiEncryptionTypeEnum *e = ImActorModelApiEncryptionTypeEnum_values_[i];
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
  if (self == [ImActorModelApiEncryptionTypeEnum class]) {
    ImActorModelApiEncryptionTypeEnum_NONE = new_ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(0, @"NONE", 0);
    ImActorModelApiEncryptionTypeEnum_AES = new_ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(1, @"AES", 1);
    ImActorModelApiEncryptionTypeEnum_AES_THEN_MAC = new_ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(2, @"AES_THEN_MAC", 2);
    ImActorModelApiEncryptionTypeEnum_UNSUPPORTED_VALUE = new_ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(-1, @"UNSUPPORTED_VALUE", 3);
    J2OBJC_SET_INITIALIZED(ImActorModelApiEncryptionTypeEnum)
  }
}

@end

void ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(ImActorModelApiEncryptionTypeEnum *self, jint value, NSString *__name, jint __ordinal) {
  (void) JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
  self->value_ = value;
}

ImActorModelApiEncryptionTypeEnum *new_ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(jint value, NSString *__name, jint __ordinal) {
  ImActorModelApiEncryptionTypeEnum *self = [ImActorModelApiEncryptionTypeEnum alloc];
  ImActorModelApiEncryptionTypeEnum_initWithInt_withNSString_withInt_(self, value, __name, __ordinal);
  return self;
}

ImActorModelApiEncryptionTypeEnum *ImActorModelApiEncryptionTypeEnum_parseWithInt_(jint value) {
  ImActorModelApiEncryptionTypeEnum_initialize();
  switch (value) {
    case 0:
    return ImActorModelApiEncryptionTypeEnum_NONE;
    case 1:
    return ImActorModelApiEncryptionTypeEnum_AES;
    case 2:
    return ImActorModelApiEncryptionTypeEnum_AES_THEN_MAC;
    default:
    return ImActorModelApiEncryptionTypeEnum_UNSUPPORTED_VALUE;
  }
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiEncryptionTypeEnum)
