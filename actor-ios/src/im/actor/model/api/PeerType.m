//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/api/PeerType.java
//

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/PeerType.h"
#include "java/io/IOException.h"
#include "java/lang/IllegalArgumentException.h"

@interface ImActorModelApiPeerTypeEnum () {
 @public
  jint value_;
}
@end

BOOL ImActorModelApiPeerTypeEnum_initialized = NO;

ImActorModelApiPeerTypeEnum *ImActorModelApiPeerTypeEnum_values_[3];

@implementation ImActorModelApiPeerTypeEnum

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

+ (ImActorModelApiPeerTypeEnum *)parseWithInt:(jint)value {
  return ImActorModelApiPeerTypeEnum_parseWithInt_(value);
}

IOSObjectArray *ImActorModelApiPeerTypeEnum_values() {
  ImActorModelApiPeerTypeEnum_init();
  return [IOSObjectArray arrayWithObjects:ImActorModelApiPeerTypeEnum_values_ count:3 type:ImActorModelApiPeerTypeEnum_class_()];
}
+ (IOSObjectArray *)values {
  return ImActorModelApiPeerTypeEnum_values();
}

+ (ImActorModelApiPeerTypeEnum *)valueOfWithNSString:(NSString *)name {
  return ImActorModelApiPeerTypeEnum_valueOfWithNSString_(name);
}

ImActorModelApiPeerTypeEnum *ImActorModelApiPeerTypeEnum_valueOfWithNSString_(NSString *name) {
  ImActorModelApiPeerTypeEnum_init();
  for (int i = 0; i < 3; i++) {
    ImActorModelApiPeerTypeEnum *e = ImActorModelApiPeerTypeEnum_values_[i];
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
  if (self == [ImActorModelApiPeerTypeEnum class]) {
    ImActorModelApiPeerTypeEnum_PRIVATE = [[ImActorModelApiPeerTypeEnum alloc] initWithInt:1 withNSString:@"PRIVATE" withInt:0];
    ImActorModelApiPeerTypeEnum_GROUP = [[ImActorModelApiPeerTypeEnum alloc] initWithInt:2 withNSString:@"GROUP" withInt:1];
    ImActorModelApiPeerTypeEnum_EMAIL = [[ImActorModelApiPeerTypeEnum alloc] initWithInt:3 withNSString:@"EMAIL" withInt:2];
    J2OBJC_SET_INITIALIZED(ImActorModelApiPeerTypeEnum)
  }
}

@end

ImActorModelApiPeerTypeEnum *ImActorModelApiPeerTypeEnum_parseWithInt_(jint value) {
  ImActorModelApiPeerTypeEnum_init();
  switch (value) {
    case 1:
    return ImActorModelApiPeerTypeEnum_PRIVATE;
    case 2:
    return ImActorModelApiPeerTypeEnum_GROUP;
    case 3:
    return ImActorModelApiPeerTypeEnum_EMAIL;
  }
  @throw [[JavaIoIOException alloc] init];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiPeerTypeEnum)
