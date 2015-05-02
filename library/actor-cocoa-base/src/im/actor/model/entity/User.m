//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/User.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/User.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/entity/Avatar.h"
#include "im/actor/model/entity/ContactRecord.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/PeerType.h"
#include "im/actor/model/entity/Sex.h"
#include "im/actor/model/entity/User.h"
#include "java/io/IOException.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"

@interface AMUser () {
 @public
  jint uid_;
  jlong accessHash_;
  NSString *name_;
  NSString *localName_;
  AMAvatar *avatar_;
  AMSexEnum *sex_;
  id<JavaUtilList> records_;
}

- (instancetype)init;

@end

J2OBJC_FIELD_SETTER(AMUser, name_, NSString *)
J2OBJC_FIELD_SETTER(AMUser, localName_, NSString *)
J2OBJC_FIELD_SETTER(AMUser, avatar_, AMAvatar *)
J2OBJC_FIELD_SETTER(AMUser, sex_, AMSexEnum *)
J2OBJC_FIELD_SETTER(AMUser, records_, id<JavaUtilList>)

__attribute__((unused)) static void AMUser_init(AMUser *self);

__attribute__((unused)) static AMUser *new_AMUser_init() NS_RETURNS_RETAINED;


#line 16
@implementation AMUser

+ (AMUser *)fromBytesWithByteArray:(IOSByteArray *)data {
  return AMUser_fromBytesWithByteArray_(data);
}


#line 30
- (instancetype)initWithInt:(jint)uid
                   withLong:(jlong)accessHash
               withNSString:(NSString *)name
               withNSString:(NSString *)localName
               withAMAvatar:(AMAvatar *)avatar
              withAMSexEnum:(AMSexEnum *)sex
           withJavaUtilList:(id<JavaUtilList>)records {
  AMUser_initWithInt_withLong_withNSString_withNSString_withAMAvatar_withAMSexEnum_withJavaUtilList_(self, uid, accessHash, name, localName, avatar, sex, records);
  return self;
}

- (instancetype)init {
  AMUser_init(self);
  return self;
}


#line 45
- (AMPeer *)peer {
  return new_AMPeer_initWithAMPeerTypeEnum_withInt_(AMPeerTypeEnum_get_PRIVATE(), uid_);
}

- (jint)getUid {
  return uid_;
}

- (jlong)getAccessHash {
  return accessHash_;
}

- (NSString *)getServerName {
  return name_;
}

- (NSString *)getLocalName {
  return localName_;
}

- (NSString *)getName {
  if (localName_ == nil) {
    return name_;
  }
  else {
    
#line 69
    return localName_;
  }
}


#line 73
- (AMAvatar *)getAvatar {
  return avatar_;
}

- (AMSexEnum *)getSex {
  return sex_;
}

- (id<JavaUtilList>)getRecords {
  return records_;
}

- (AMUser *)editNameWithNSString:(NSString *)name {
  return new_AMUser_initWithInt_withLong_withNSString_withNSString_withAMAvatar_withAMSexEnum_withJavaUtilList_(uid_, accessHash_, name, localName_, avatar_, sex_, records_);
}

- (AMUser *)editLocalNameWithNSString:(NSString *)localName {
  return new_AMUser_initWithInt_withLong_withNSString_withNSString_withAMAvatar_withAMSexEnum_withJavaUtilList_(uid_, accessHash_, name_, localName, avatar_, sex_, records_);
}

- (AMUser *)editAvatarWithAMAvatar:(AMAvatar *)avatar {
  return new_AMUser_initWithInt_withLong_withNSString_withNSString_withAMAvatar_withAMSexEnum_withJavaUtilList_(uid_, accessHash_, name_, localName_, avatar, sex_, records_);
}


#line 98
- (jlong)getEngineId {
  return [self getUid];
}


#line 103
- (void)parseWithBSBserValues:(BSBserValues *)values {
  uid_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  accessHash_ = [values getLongWithInt:2];
  name_ = [values getStringWithInt:3];
  localName_ = [values optStringWithInt:4];
  IOSByteArray *a = [values optBytesWithInt:5];
  if (a != nil) {
    avatar_ = AMAvatar_fromBytesWithByteArray_(a);
  }
  sex_ = AMSexEnum_fromValueWithInt_([values getIntWithInt:6]);
  jint count = [values getRepeatedCountWithInt:7];
  if (count > 0) {
    JavaUtilArrayList *rec = new_JavaUtilArrayList_init();
    for (jint i = 0; i < count; i++) {
      [rec addWithId:new_AMContactRecord_init()];
    }
    records_ = [values getRepeatedObjWithInt:7 withJavaUtilList:rec];
  }
}


#line 124
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:uid_];
  [writer writeLongWithInt:2 withLong:accessHash_];
  [writer writeStringWithInt:3 withNSString:name_];
  if (localName_ != nil) {
    [writer writeStringWithInt:4 withNSString:localName_];
  }
  if (avatar_ != nil) {
    [writer writeObjectWithInt:5 withBSBserObject:avatar_];
  }
  [writer writeIntWithInt:6 withInt:[((AMSexEnum *) nil_chk(sex_)) getValue]];
  [writer writeRepeatedObjWithInt:7 withJavaUtilList:records_];
}

@end


#line 18
AMUser *AMUser_fromBytesWithByteArray_(IOSByteArray *data) {
  AMUser_initialize();
  
#line 19
  return ((AMUser *) BSBser_parseWithBSBserObject_withByteArray_(new_AMUser_init(), data));
}


#line 30
void AMUser_initWithInt_withLong_withNSString_withNSString_withAMAvatar_withAMSexEnum_withJavaUtilList_(AMUser *self, jint uid, jlong accessHash, NSString *name, NSString *localName, AMAvatar *avatar, AMSexEnum *sex, id<JavaUtilList> records) {
  (void) BSBserObject_init(self);
  self->uid_ = uid;
  self->accessHash_ = accessHash;
  self->name_ = name;
  self->localName_ = localName;
  self->avatar_ = avatar;
  self->sex_ = sex;
  self->records_ = records;
}


#line 30
AMUser *new_AMUser_initWithInt_withLong_withNSString_withNSString_withAMAvatar_withAMSexEnum_withJavaUtilList_(jint uid, jlong accessHash, NSString *name, NSString *localName, AMAvatar *avatar, AMSexEnum *sex, id<JavaUtilList> records) {
  AMUser *self = [AMUser alloc];
  AMUser_initWithInt_withLong_withNSString_withNSString_withAMAvatar_withAMSexEnum_withJavaUtilList_(self, uid, accessHash, name, localName, avatar, sex, records);
  return self;
}


#line 41
void AMUser_init(AMUser *self) {
  (void) BSBserObject_init(self);
}


#line 41
AMUser *new_AMUser_init() {
  AMUser *self = [AMUser alloc];
  AMUser_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMUser)
