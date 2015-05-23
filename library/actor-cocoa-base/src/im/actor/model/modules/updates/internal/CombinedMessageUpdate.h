//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/updates/internal/CombinedMessageUpdate.java
//

#ifndef _ImActorModelModulesUpdatesInternalCombinedMessageUpdate_H_
#define _ImActorModelModulesUpdatesInternalCombinedMessageUpdate_H_

#include "J2ObjC_header.h"

@class ImActorModelApiMessage;
@class JavaUtilArrayList;

@interface ImActorModelModulesUpdatesInternalCombinedMessageUpdate : NSObject

#pragma mark Public

- (instancetype)init;

- (JavaUtilArrayList *)getMessages;

- (jlong)getReadKey;

- (jlong)getReceivedKey;

- (void)setReadKeyWithLong:(jlong)readKey;

- (void)setReceivedKeyWithLong:(jlong)receivedKey;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesUpdatesInternalCombinedMessageUpdate)

FOUNDATION_EXPORT void ImActorModelModulesUpdatesInternalCombinedMessageUpdate_init(ImActorModelModulesUpdatesInternalCombinedMessageUpdate *self);

FOUNDATION_EXPORT ImActorModelModulesUpdatesInternalCombinedMessageUpdate *new_ImActorModelModulesUpdatesInternalCombinedMessageUpdate_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesUpdatesInternalCombinedMessageUpdate)

@interface ImActorModelModulesUpdatesInternalCombinedMessageUpdate_CombinedMessage : NSObject

#pragma mark Public

- (instancetype)initWithLong:(jlong)rid
                     withInt:(jint)sender
                    withLong:(jlong)date
  withImActorModelApiMessage:(ImActorModelApiMessage *)message;

- (jlong)getDate;

- (ImActorModelApiMessage *)getMessage;

- (jlong)getRid;

- (jint)getSender;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesUpdatesInternalCombinedMessageUpdate_CombinedMessage)

FOUNDATION_EXPORT void ImActorModelModulesUpdatesInternalCombinedMessageUpdate_CombinedMessage_initWithLong_withInt_withLong_withImActorModelApiMessage_(ImActorModelModulesUpdatesInternalCombinedMessageUpdate_CombinedMessage *self, jlong rid, jint sender, jlong date, ImActorModelApiMessage *message);

FOUNDATION_EXPORT ImActorModelModulesUpdatesInternalCombinedMessageUpdate_CombinedMessage *new_ImActorModelModulesUpdatesInternalCombinedMessageUpdate_CombinedMessage_initWithLong_withInt_withLong_withImActorModelApiMessage_(jlong rid, jint sender, jlong date, ImActorModelApiMessage *message) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesUpdatesInternalCombinedMessageUpdate_CombinedMessage)

#endif // _ImActorModelModulesUpdatesInternalCombinedMessageUpdate_H_
