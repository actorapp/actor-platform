//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/api/AuthSession.java
//

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/AuthSession.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "java/io/IOException.h"
#include "java/lang/Double.h"

@interface ImActorModelApiAuthSession () {
 @public
  jint id__;
  jint authHolder_;
  jint appId_;
  NSString *appTitle_;
  NSString *deviceTitle_;
  jint authTime_;
  NSString *authLocation_;
  JavaLangDouble *latitude_;
  JavaLangDouble *longitude_;
}
@end

J2OBJC_FIELD_SETTER(ImActorModelApiAuthSession, appTitle_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelApiAuthSession, deviceTitle_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelApiAuthSession, authLocation_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelApiAuthSession, latitude_, JavaLangDouble *)
J2OBJC_FIELD_SETTER(ImActorModelApiAuthSession, longitude_, JavaLangDouble *)

@implementation ImActorModelApiAuthSession

- (instancetype)initWithInt:(jint)id_
                    withInt:(jint)authHolder
                    withInt:(jint)appId
               withNSString:(NSString *)appTitle
               withNSString:(NSString *)deviceTitle
                    withInt:(jint)authTime
               withNSString:(NSString *)authLocation
         withJavaLangDouble:(JavaLangDouble *)latitude
         withJavaLangDouble:(JavaLangDouble *)longitude {
  if (self = [super init]) {
    self->id__ = id_;
    self->authHolder_ = authHolder;
    self->appId_ = appId;
    self->appTitle_ = appTitle;
    self->deviceTitle_ = deviceTitle;
    self->authTime_ = authTime;
    self->authLocation_ = authLocation;
    self->latitude_ = latitude;
    self->longitude_ = longitude;
  }
  return self;
}

- (instancetype)init {
  return [super init];
}

- (jint)getId {
  return self->id__;
}

- (jint)getAuthHolder {
  return self->authHolder_;
}

- (jint)getAppId {
  return self->appId_;
}

- (NSString *)getAppTitle {
  return self->appTitle_;
}

- (NSString *)getDeviceTitle {
  return self->deviceTitle_;
}

- (jint)getAuthTime {
  return self->authTime_;
}

- (NSString *)getAuthLocation {
  return self->authLocation_;
}

- (JavaLangDouble *)getLatitude {
  return self->latitude_;
}

- (JavaLangDouble *)getLongitude {
  return self->longitude_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->id__ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->authHolder_ = [values getIntWithInt:2];
  self->appId_ = [values getIntWithInt:3];
  self->appTitle_ = [values getStringWithInt:4];
  self->deviceTitle_ = [values getStringWithInt:5];
  self->authTime_ = [values getIntWithInt:6];
  self->authLocation_ = [values getStringWithInt:7];
  self->latitude_ = JavaLangDouble_valueOfWithDouble_([values optDoubleWithInt:8]);
  self->longitude_ = JavaLangDouble_valueOfWithDouble_([values optDoubleWithInt:9]);
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->id__];
  [writer writeIntWithInt:2 withInt:self->authHolder_];
  [writer writeIntWithInt:3 withInt:self->appId_];
  if (self->appTitle_ == nil) {
    @throw [[JavaIoIOException alloc] init];
  }
  [writer writeStringWithInt:4 withNSString:self->appTitle_];
  if (self->deviceTitle_ == nil) {
    @throw [[JavaIoIOException alloc] init];
  }
  [writer writeStringWithInt:5 withNSString:self->deviceTitle_];
  [writer writeIntWithInt:6 withInt:self->authTime_];
  if (self->authLocation_ == nil) {
    @throw [[JavaIoIOException alloc] init];
  }
  [writer writeStringWithInt:7 withNSString:self->authLocation_];
  if (self->latitude_ != nil) {
    [writer writeDoubleWithInt:8 withDouble:[self->latitude_ doubleValue]];
  }
  if (self->longitude_ != nil) {
    [writer writeDoubleWithInt:9 withDouble:[self->longitude_ doubleValue]];
  }
}

- (void)copyAllFieldsTo:(ImActorModelApiAuthSession *)other {
  [super copyAllFieldsTo:other];
  other->id__ = id__;
  other->authHolder_ = authHolder_;
  other->appId_ = appId_;
  other->appTitle_ = appTitle_;
  other->deviceTitle_ = deviceTitle_;
  other->authTime_ = authTime_;
  other->authLocation_ = authLocation_;
  other->latitude_ = latitude_;
  other->longitude_ = longitude_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiAuthSession)
