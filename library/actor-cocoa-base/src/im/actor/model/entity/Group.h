//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/Group.java
//

#ifndef _AMGroup_H_
#define _AMGroup_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/engine/KeyValueItem.h"
#include "im/actor/model/entity/WrapperEntity.h"

@class AMAvatar;
@class AMPeer;
@class BSBserValues;
@class BSBserWriter;
@class IOSByteArray;
@class ImActorModelApiAvatar;
@class ImActorModelApiGroup;
@protocol JavaUtilList;

@interface AMGroup : AMWrapperEntity < DKKeyValueItem >

#pragma mark Public

- (instancetype)initWithImActorModelApiGroup:(ImActorModelApiGroup *)group;

- (AMGroup *)addMemberWithInt:(jint)uid
                      withInt:(jint)inviterUid
                     withLong:(jlong)inviteDate
                  withBoolean:(jboolean)isAdmin;

- (AMGroup *)changeMemberWithBoolean:(jboolean)isMember;

- (AMGroup *)clearMembers;

- (AMGroup *)editAvatarWithImActorModelApiAvatar:(ImActorModelApiAvatar *)avatar;

- (AMGroup *)editTitleWithNSString:(NSString *)title;

+ (AMGroup *)fromBytesWithByteArray:(IOSByteArray *)data;

- (jlong)getAccessHash;

- (jint)getAdminId;

- (AMAvatar *)getAvatar;

- (jlong)getEngineId;

- (jint)getGroupId;

- (id<JavaUtilList>)getMembers;

- (NSString *)getTitle;

- (jboolean)isMember;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (AMPeer *)peer;

- (AMGroup *)removeMemberWithInt:(jint)uid;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

#pragma mark Protected

- (void)applyWrappedWithBSBserObject:(ImActorModelApiGroup *)wrapped;

- (ImActorModelApiGroup *)createInstance;

@end

J2OBJC_EMPTY_STATIC_INIT(AMGroup)

FOUNDATION_EXPORT AMGroup *AMGroup_fromBytesWithByteArray_(IOSByteArray *data);

FOUNDATION_EXPORT void AMGroup_initWithImActorModelApiGroup_(AMGroup *self, ImActorModelApiGroup *group);

FOUNDATION_EXPORT AMGroup *new_AMGroup_initWithImActorModelApiGroup_(ImActorModelApiGroup *group) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMGroup)

typedef AMGroup ImActorModelEntityGroup;

@interface AMGroup_ObsoleteGroupMember : BSBserObject

#pragma mark Public

- (jlong)getInviteDate;

- (jint)getInviterUid;

- (jint)getUid;

- (jboolean)isAdministrator;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

#pragma mark Package-Private

- (instancetype)initWithAMGroup:(AMGroup *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(AMGroup_ObsoleteGroupMember)

FOUNDATION_EXPORT void AMGroup_ObsoleteGroupMember_initWithAMGroup_(AMGroup_ObsoleteGroupMember *self, AMGroup *outer$);

FOUNDATION_EXPORT AMGroup_ObsoleteGroupMember *new_AMGroup_ObsoleteGroupMember_initWithAMGroup_(AMGroup *outer$) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMGroup_ObsoleteGroupMember)

#endif // _AMGroup_H_
