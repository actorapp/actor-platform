//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/ContentType.java
//


#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/entity/ContentType.h"
#include "java/lang/Enum.h"
#include "java/lang/IllegalArgumentException.h"

__attribute__((unused)) static void AMContentTypeEnum_initWithInt_withNSString_withInt_(AMContentTypeEnum *self, jint value, NSString *__name, jint __ordinal);

__attribute__((unused)) static AMContentTypeEnum *new_AMContentTypeEnum_initWithInt_withNSString_withInt_(jint value, NSString *__name, jint __ordinal) NS_RETURNS_RETAINED;

J2OBJC_INITIALIZED_DEFN(AMContentTypeEnum)

AMContentTypeEnum *AMContentTypeEnum_values_[15];

@implementation AMContentTypeEnum

- (instancetype)initWithInt:(jint)value
               withNSString:(NSString *)__name
                    withInt:(jint)__ordinal {
  AMContentTypeEnum_initWithInt_withNSString_withInt_(self, value, __name, __ordinal);
  return self;
}

- (jint)getValue {
  return value_;
}

+ (AMContentTypeEnum *)fromValueWithInt:(jint)value {
  return AMContentTypeEnum_fromValueWithInt_(value);
}

IOSObjectArray *AMContentTypeEnum_values() {
  AMContentTypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:AMContentTypeEnum_values_ count:15 type:AMContentTypeEnum_class_()];
}

+ (IOSObjectArray *)values {
  return AMContentTypeEnum_values();
}

+ (AMContentTypeEnum *)valueOfWithNSString:(NSString *)name {
  return AMContentTypeEnum_valueOfWithNSString_(name);
}

AMContentTypeEnum *AMContentTypeEnum_valueOfWithNSString_(NSString *name) {
  AMContentTypeEnum_initialize();
  for (int i = 0; i < 15; i++) {
    AMContentTypeEnum *e = AMContentTypeEnum_values_[i];
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
  if (self == [AMContentTypeEnum class]) {
    AMContentTypeEnum_TEXT = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(2, @"TEXT", 0);
    AMContentTypeEnum_EMPTY = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(1, @"EMPTY", 1);
    AMContentTypeEnum_DOCUMENT = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(3, @"DOCUMENT", 2);
    AMContentTypeEnum_DOCUMENT_PHOTO = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(4, @"DOCUMENT_PHOTO", 3);
    AMContentTypeEnum_DOCUMENT_VIDEO = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(5, @"DOCUMENT_VIDEO", 4);
    AMContentTypeEnum_SERVICE = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(6, @"SERVICE", 5);
    AMContentTypeEnum_SERVICE_ADD = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(7, @"SERVICE_ADD", 6);
    AMContentTypeEnum_SERVICE_KICK = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(8, @"SERVICE_KICK", 7);
    AMContentTypeEnum_SERVICE_LEAVE = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(9, @"SERVICE_LEAVE", 8);
    AMContentTypeEnum_SERVICE_REGISTERED = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(10, @"SERVICE_REGISTERED", 9);
    AMContentTypeEnum_SERVICE_CREATED = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(11, @"SERVICE_CREATED", 10);
    AMContentTypeEnum_SERVICE_TITLE = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(12, @"SERVICE_TITLE", 11);
    AMContentTypeEnum_SERVICE_AVATAR = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(13, @"SERVICE_AVATAR", 12);
    AMContentTypeEnum_SERVICE_AVATAR_REMOVED = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(14, @"SERVICE_AVATAR_REMOVED", 13);
    AMContentTypeEnum_UNKNOWN_CONTENT = new_AMContentTypeEnum_initWithInt_withNSString_withInt_(15, @"UNKNOWN_CONTENT", 14);
    J2OBJC_SET_INITIALIZED(AMContentTypeEnum)
  }
}

@end

void AMContentTypeEnum_initWithInt_withNSString_withInt_(AMContentTypeEnum *self, jint value, NSString *__name, jint __ordinal) {
  (void) JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
  self->value_ = value;
}

AMContentTypeEnum *new_AMContentTypeEnum_initWithInt_withNSString_withInt_(jint value, NSString *__name, jint __ordinal) {
  AMContentTypeEnum *self = [AMContentTypeEnum alloc];
  AMContentTypeEnum_initWithInt_withNSString_withInt_(self, value, __name, __ordinal);
  return self;
}

AMContentTypeEnum *AMContentTypeEnum_fromValueWithInt_(jint value) {
  AMContentTypeEnum_initialize();
  switch (value) {
    default:
    case 1:
    return AMContentTypeEnum_EMPTY;
    case 2:
    return AMContentTypeEnum_TEXT;
    case 3:
    return AMContentTypeEnum_DOCUMENT;
    case 4:
    return AMContentTypeEnum_DOCUMENT_PHOTO;
    case 5:
    return AMContentTypeEnum_DOCUMENT_VIDEO;
    case 6:
    return AMContentTypeEnum_SERVICE;
    case 7:
    return AMContentTypeEnum_SERVICE_ADD;
    case 8:
    return AMContentTypeEnum_SERVICE_KICK;
    case 9:
    return AMContentTypeEnum_SERVICE_LEAVE;
    case 10:
    return AMContentTypeEnum_SERVICE_REGISTERED;
    case 11:
    return AMContentTypeEnum_SERVICE_CREATED;
    case 12:
    return AMContentTypeEnum_SERVICE_TITLE;
    case 13:
    return AMContentTypeEnum_SERVICE_AVATAR;
    case 14:
    return AMContentTypeEnum_SERVICE_AVATAR_REMOVED;
  }
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMContentTypeEnum)
