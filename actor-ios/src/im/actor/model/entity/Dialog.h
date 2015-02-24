//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/entity/Dialog.java
//

#ifndef _AMDialog_H_
#define _AMDialog_H_

@class AMAvatar;
@class AMDialog_ContentTypeEnum;
@class AMMessageStateEnum;
@class AMPeer;
@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/storage/ListEngineItem.h"
#include "java/lang/Enum.h"

@interface AMDialog : BSBserObject < AMListEngineItem > {
}

+ (AMDialog *)fromBytesWithByteArray:(IOSByteArray *)date;

- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)sortKey
                  withNSString:(NSString *)dialogTitle
                  withAMAvatar:(AMAvatar *)dialogAvatar
                       withInt:(jint)unreadCount
                      withLong:(jlong)rid
  withAMDialog_ContentTypeEnum:(AMDialog_ContentTypeEnum *)messageType
                  withNSString:(NSString *)text
        withAMMessageStateEnum:(AMMessageStateEnum *)status
                       withInt:(jint)senderId
                      withLong:(jlong)date
                       withInt:(jint)relatedUid;

- (AMPeer *)getPeer;

- (jlong)getListId;

- (jlong)getListSortKey;

- (NSString *)getDialogTitle;

- (jint)getUnreadCount;

- (jlong)getRid;

- (jlong)getSortDate;

- (jint)getSenderId;

- (jlong)getDate;

- (AMDialog_ContentTypeEnum *)getMessageType;

- (NSString *)getText;

- (AMMessageStateEnum *)getStatus;

- (jint)getRelatedUid;

- (AMAvatar *)getDialogAvatar;

- (AMDialog *)editPeerInfoWithNSString:(NSString *)title
                          withAMAvatar:(AMAvatar *)dialogAvatar;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

@end

J2OBJC_EMPTY_STATIC_INIT(AMDialog)

CF_EXTERN_C_BEGIN

FOUNDATION_EXPORT AMDialog *AMDialog_fromBytesWithByteArray_(IOSByteArray *date);
CF_EXTERN_C_END

typedef AMDialog ImActorModelEntityDialog;

J2OBJC_TYPE_LITERAL_HEADER(AMDialog)

typedef NS_ENUM(NSUInteger, AMDialog_ContentType) {
  AMDialog_ContentType_TEXT = 0,
  AMDialog_ContentType_EMPTY = 1,
  AMDialog_ContentType_DOCUMENT = 2,
  AMDialog_ContentType_DOCUMENT_PHOTO = 3,
  AMDialog_ContentType_DOCUMENT_VIDEO = 4,
  AMDialog_ContentType_SERVICE = 5,
  AMDialog_ContentType_SERVICE_ADD = 6,
  AMDialog_ContentType_SERVICE_KICK = 7,
  AMDialog_ContentType_SERVICE_LEAVE = 8,
  AMDialog_ContentType_SERVICE_REGISTERED = 9,
  AMDialog_ContentType_SERVICE_CREATED = 10,
  AMDialog_ContentType_SERVICE_TITLE = 11,
  AMDialog_ContentType_SERVICE_AVATAR = 12,
  AMDialog_ContentType_SERVICE_AVATAR_REMOVED = 13,
};

@interface AMDialog_ContentTypeEnum : JavaLangEnum < NSCopying > {
 @public
  jint value_;
}

- (instancetype)initWithInt:(jint)value
               withNSString:(NSString *)__name
                    withInt:(jint)__ordinal;

- (jint)getValue;

+ (AMDialog_ContentTypeEnum *)fromValueWithInt:(jint)value;

+ (IOSObjectArray *)values;
FOUNDATION_EXPORT IOSObjectArray *AMDialog_ContentTypeEnum_values();

+ (AMDialog_ContentTypeEnum *)valueOfWithNSString:(NSString *)name;

FOUNDATION_EXPORT AMDialog_ContentTypeEnum *AMDialog_ContentTypeEnum_valueOfWithNSString_(NSString *name);
- (id)copyWithZone:(NSZone *)zone;

@end

FOUNDATION_EXPORT BOOL AMDialog_ContentTypeEnum_initialized;
J2OBJC_STATIC_INIT(AMDialog_ContentTypeEnum)

FOUNDATION_EXPORT AMDialog_ContentTypeEnum *AMDialog_ContentTypeEnum_fromValueWithInt_(jint value);

FOUNDATION_EXPORT AMDialog_ContentTypeEnum *AMDialog_ContentTypeEnum_values_[];

#define AMDialog_ContentTypeEnum_TEXT AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_TEXT]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, TEXT)

#define AMDialog_ContentTypeEnum_EMPTY AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_EMPTY]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, EMPTY)

#define AMDialog_ContentTypeEnum_DOCUMENT AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_DOCUMENT]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, DOCUMENT)

#define AMDialog_ContentTypeEnum_DOCUMENT_PHOTO AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_DOCUMENT_PHOTO]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, DOCUMENT_PHOTO)

#define AMDialog_ContentTypeEnum_DOCUMENT_VIDEO AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_DOCUMENT_VIDEO]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, DOCUMENT_VIDEO)

#define AMDialog_ContentTypeEnum_SERVICE AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE)

#define AMDialog_ContentTypeEnum_SERVICE_ADD AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_ADD]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_ADD)

#define AMDialog_ContentTypeEnum_SERVICE_KICK AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_KICK]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_KICK)

#define AMDialog_ContentTypeEnum_SERVICE_LEAVE AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_LEAVE]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_LEAVE)

#define AMDialog_ContentTypeEnum_SERVICE_REGISTERED AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_REGISTERED]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_REGISTERED)

#define AMDialog_ContentTypeEnum_SERVICE_CREATED AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_CREATED]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_CREATED)

#define AMDialog_ContentTypeEnum_SERVICE_TITLE AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_TITLE]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_TITLE)

#define AMDialog_ContentTypeEnum_SERVICE_AVATAR AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_AVATAR]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_AVATAR)

#define AMDialog_ContentTypeEnum_SERVICE_AVATAR_REMOVED AMDialog_ContentTypeEnum_values_[AMDialog_ContentType_SERVICE_AVATAR_REMOVED]
J2OBJC_ENUM_CONSTANT_GETTER(AMDialog_ContentTypeEnum, SERVICE_AVATAR_REMOVED)

J2OBJC_TYPE_LITERAL_HEADER(AMDialog_ContentTypeEnum)

#endif // _AMDialog_H_
