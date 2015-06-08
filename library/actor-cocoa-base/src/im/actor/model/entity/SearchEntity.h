//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/SearchEntity.java
//

#ifndef _AMSearchEntity_H_
#define _AMSearchEntity_H_

#include "J2ObjC_header.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/engine/ListEngineItem.h"

@class AMAvatar;
@class AMPeer;
@class BSBserValues;
@class BSBserWriter;
@protocol BSBserCreator;

@interface AMSearchEntity : BSBserObject < DKListEngineItem >

#pragma mark Public

- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)order
                  withAMAvatar:(AMAvatar *)avatar
                  withNSString:(NSString *)title;

- (AMAvatar *)getAvatar;

- (jlong)getEngineId;

- (NSString *)getEngineSearch;

- (jlong)getEngineSort;

- (jlong)getOrder;

- (AMPeer *)getPeer;

- (NSString *)getTitle;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

@end

J2OBJC_STATIC_INIT(AMSearchEntity)

FOUNDATION_EXPORT id<BSBserCreator> AMSearchEntity_CREATOR_;
J2OBJC_STATIC_FIELD_GETTER(AMSearchEntity, CREATOR_, id<BSBserCreator>)

FOUNDATION_EXPORT void AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(AMSearchEntity *self, AMPeer *peer, jlong order, AMAvatar *avatar, NSString *title);

FOUNDATION_EXPORT AMSearchEntity *new_AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(AMPeer *peer, jlong order, AMAvatar *avatar, NSString *title) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMSearchEntity)

typedef AMSearchEntity ImActorModelEntitySearchEntity;

#endif // _AMSearchEntity_H_
