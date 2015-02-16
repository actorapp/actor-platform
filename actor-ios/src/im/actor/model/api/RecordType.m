//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/api/RecordType.java
//

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/RecordType.h"
#include "java/io/IOException.h"
#include "java/lang/IllegalArgumentException.h"

@interface ImActorModelApiRecordTypeEnum () {
 @public
  jint value_;
}
@end

BOOL ImActorModelApiRecordTypeEnum_initialized = NO;

ImActorModelApiRecordTypeEnum *ImActorModelApiRecordTypeEnum_values_[2];

@implementation ImActorModelApiRecordTypeEnum

- (instancetype)initWithInt:(jint)value
               withNSString:(NSString *)__name
                    withInt:(jint)__ordinal {
  if (self = [super initWithNSString:__name withInt:__ordinal]) {
    self->value_ = value;
  }
  return self;
}

- (jint)getValue {
  return value_;
}

+ (ImActorModelApiRecordTypeEnum *)parseWithInt:(jint)value {
  return ImActorModelApiRecordTypeEnum_parseWithInt_(value);
}

IOSObjectArray *ImActorModelApiRecordTypeEnum_values() {
  ImActorModelApiRecordTypeEnum_init();
  return [IOSObjectArray arrayWithObjects:ImActorModelApiRecordTypeEnum_values_ count:2 type:ImActorModelApiRecordTypeEnum_class_()];
}
+ (IOSObjectArray *)values {
  return ImActorModelApiRecordTypeEnum_values();
}

+ (ImActorModelApiRecordTypeEnum *)valueOfWithNSString:(NSString *)name {
  return ImActorModelApiRecordTypeEnum_valueOfWithNSString_(name);
}

ImActorModelApiRecordTypeEnum *ImActorModelApiRecordTypeEnum_valueOfWithNSString_(NSString *name) {
  ImActorModelApiRecordTypeEnum_init();
  for (int i = 0; i < 2; i++) {
    ImActorModelApiRecordTypeEnum *e = ImActorModelApiRecordTypeEnum_values_[i];
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
  if (self == [ImActorModelApiRecordTypeEnum class]) {
    ImActorModelApiRecordTypeEnum_PHONE = [[ImActorModelApiRecordTypeEnum alloc] initWithInt:1 withNSString:@"PHONE" withInt:0];
    ImActorModelApiRecordTypeEnum_EMAIL = [[ImActorModelApiRecordTypeEnum alloc] initWithInt:2 withNSString:@"EMAIL" withInt:1];
    J2OBJC_SET_INITIALIZED(ImActorModelApiRecordTypeEnum)
  }
}

@end

ImActorModelApiRecordTypeEnum *ImActorModelApiRecordTypeEnum_parseWithInt_(jint value) {
  ImActorModelApiRecordTypeEnum_init();
  switch (value) {
    case 1:
    return ImActorModelApiRecordTypeEnum_PHONE;
    case 2:
    return ImActorModelApiRecordTypeEnum_EMAIL;
  }
  @throw [[JavaIoIOException alloc] init];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRecordTypeEnum)
