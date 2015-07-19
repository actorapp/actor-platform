//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/modules/settings/entity/SettingsSyncAction.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/modules/settings/entity/SettingsSyncAction.h"
#include "java/io/IOException.h"

@interface ImActorModelModulesSettingsEntitySettingsSyncAction () {
 @public
  NSString *key_;
  NSString *value_;
}

- (instancetype)init;

@end

J2OBJC_FIELD_SETTER(ImActorModelModulesSettingsEntitySettingsSyncAction, key_, NSString *)
J2OBJC_FIELD_SETTER(ImActorModelModulesSettingsEntitySettingsSyncAction, value_, NSString *)

__attribute__((unused)) static void ImActorModelModulesSettingsEntitySettingsSyncAction_init(ImActorModelModulesSettingsEntitySettingsSyncAction *self);

__attribute__((unused)) static ImActorModelModulesSettingsEntitySettingsSyncAction *new_ImActorModelModulesSettingsEntitySettingsSyncAction_init() NS_RETURNS_RETAINED;

@implementation ImActorModelModulesSettingsEntitySettingsSyncAction

+ (ImActorModelModulesSettingsEntitySettingsSyncAction *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelModulesSettingsEntitySettingsSyncAction_fromBytesWithByteArray_(data);
}

- (instancetype)initWithNSString:(NSString *)key
                    withNSString:(NSString *)value {
  ImActorModelModulesSettingsEntitySettingsSyncAction_initWithNSString_withNSString_(self, key, value);
  return self;
}

- (instancetype)init {
  ImActorModelModulesSettingsEntitySettingsSyncAction_init(self);
  return self;
}

- (NSString *)getKey {
  return key_;
}

- (NSString *)getValue {
  return value_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  key_ = [((BSBserValues *) nil_chk(values)) getStringWithInt:1];
  value_ = [values optStringWithInt:2];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeStringWithInt:1 withNSString:key_];
  if (value_ != nil) {
    [writer writeStringWithInt:2 withNSString:value_];
  }
}

@end

ImActorModelModulesSettingsEntitySettingsSyncAction *ImActorModelModulesSettingsEntitySettingsSyncAction_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelModulesSettingsEntitySettingsSyncAction_initialize();
  return ((ImActorModelModulesSettingsEntitySettingsSyncAction *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelModulesSettingsEntitySettingsSyncAction_init(), data));
}

void ImActorModelModulesSettingsEntitySettingsSyncAction_initWithNSString_withNSString_(ImActorModelModulesSettingsEntitySettingsSyncAction *self, NSString *key, NSString *value) {
  (void) BSBserObject_init(self);
  self->key_ = key;
  self->value_ = value;
}

ImActorModelModulesSettingsEntitySettingsSyncAction *new_ImActorModelModulesSettingsEntitySettingsSyncAction_initWithNSString_withNSString_(NSString *key, NSString *value) {
  ImActorModelModulesSettingsEntitySettingsSyncAction *self = [ImActorModelModulesSettingsEntitySettingsSyncAction alloc];
  ImActorModelModulesSettingsEntitySettingsSyncAction_initWithNSString_withNSString_(self, key, value);
  return self;
}

void ImActorModelModulesSettingsEntitySettingsSyncAction_init(ImActorModelModulesSettingsEntitySettingsSyncAction *self) {
  (void) BSBserObject_init(self);
}

ImActorModelModulesSettingsEntitySettingsSyncAction *new_ImActorModelModulesSettingsEntitySettingsSyncAction_init() {
  ImActorModelModulesSettingsEntitySettingsSyncAction *self = [ImActorModelModulesSettingsEntitySettingsSyncAction alloc];
  ImActorModelModulesSettingsEntitySettingsSyncAction_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesSettingsEntitySettingsSyncAction)
