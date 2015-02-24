//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/modules/messages/entity/DialogHistory.java
//

#ifndef _ImActorModelModulesMessagesEntityDialogHistory_H_
#define _ImActorModelModulesMessagesEntityDialogHistory_H_

@class AMMessageStateEnum;
@class AMPeer;
@class ImActorModelEntityContentAbsContent;

#include "J2ObjC_header.h"

@interface ImActorModelModulesMessagesEntityDialogHistory : NSObject {
}

- (instancetype)initWithAMPeer:(AMPeer *)peer
                       withInt:(jint)unreadCount
                      withLong:(jlong)sortDate
                      withLong:(jlong)rid
                      withLong:(jlong)date
                       withInt:(jint)senderId
withImActorModelEntityContentAbsContent:(ImActorModelEntityContentAbsContent *)content
        withAMMessageStateEnum:(AMMessageStateEnum *)status;

- (AMPeer *)getPeer;

- (jint)getUnreadCount;

- (jlong)getSortDate;

- (jlong)getRid;

- (jlong)getDate;

- (jint)getSenderId;

- (ImActorModelEntityContentAbsContent *)getContent;

- (AMMessageStateEnum *)getStatus;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelModulesMessagesEntityDialogHistory)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelModulesMessagesEntityDialogHistory)

#endif // _ImActorModelModulesMessagesEntityDialogHistory_H_
