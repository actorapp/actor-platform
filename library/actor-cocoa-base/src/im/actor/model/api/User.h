//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/User.java
//

#ifndef _ImActorModelApiUser_H_
#define _ImActorModelApiUser_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"

@class BSBserValues;
@class BSBserWriter;
@class ImActorModelApiAvatar;
@class ImActorModelApiSexEnum;
@protocol JavaUtilList;

@interface ImActorModelApiUser : BSBserObject

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithInt:(jint)id_
                   withLong:(jlong)accessHash
               withNSString:(NSString *)name
               withNSString:(NSString *)localName
 withImActorModelApiSexEnum:(ImActorModelApiSexEnum *)sex
           withJavaUtilList:(id<JavaUtilList>)keyHashes
                   withLong:(jlong)phone
  withImActorModelApiAvatar:(ImActorModelApiAvatar *)avatar
           withJavaUtilList:(id<JavaUtilList>)phones
           withJavaUtilList:(id<JavaUtilList>)emails;

- (jlong)getAccessHash;

- (ImActorModelApiAvatar *)getAvatar;

- (id<JavaUtilList>)getEmails;

- (jint)getId;

- (id<JavaUtilList>)getKeyHashes;

- (NSString *)getLocalName;

- (NSString *)getName;

- (jlong)getPhone;

- (id<JavaUtilList>)getPhones;

- (ImActorModelApiSexEnum *)getSex;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelApiUser)

FOUNDATION_EXPORT void ImActorModelApiUser_initWithInt_withLong_withNSString_withNSString_withImActorModelApiSexEnum_withJavaUtilList_withLong_withImActorModelApiAvatar_withJavaUtilList_withJavaUtilList_(ImActorModelApiUser *self, jint id_, jlong accessHash, NSString *name, NSString *localName, ImActorModelApiSexEnum *sex, id<JavaUtilList> keyHashes, jlong phone, ImActorModelApiAvatar *avatar, id<JavaUtilList> phones, id<JavaUtilList> emails);

FOUNDATION_EXPORT ImActorModelApiUser *new_ImActorModelApiUser_initWithInt_withLong_withNSString_withNSString_withImActorModelApiSexEnum_withJavaUtilList_withLong_withImActorModelApiAvatar_withJavaUtilList_withJavaUtilList_(jint id_, jlong accessHash, NSString *name, NSString *localName, ImActorModelApiSexEnum *sex, id<JavaUtilList> keyHashes, jlong phone, ImActorModelApiAvatar *avatar, id<JavaUtilList> phones, id<JavaUtilList> emails) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void ImActorModelApiUser_init(ImActorModelApiUser *self);

FOUNDATION_EXPORT ImActorModelApiUser *new_ImActorModelApiUser_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelApiUser)

#endif // _ImActorModelApiUser_H_
