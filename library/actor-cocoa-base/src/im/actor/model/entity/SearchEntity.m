//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/SearchEntity.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/BserCreator.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/entity/Avatar.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/SearchEntity.h"
#include "java/io/IOException.h"

@interface AMSearchEntity () {
 @public
  AMPeer *peer_;
  jlong order_;
  AMAvatar *avatar_;
  NSString *title_;
}

- (instancetype)init;

@end

J2OBJC_FIELD_SETTER(AMSearchEntity, peer_, AMPeer *)
J2OBJC_FIELD_SETTER(AMSearchEntity, avatar_, AMAvatar *)
J2OBJC_FIELD_SETTER(AMSearchEntity, title_, NSString *)

__attribute__((unused)) static void AMSearchEntity_init(AMSearchEntity *self);

__attribute__((unused)) static AMSearchEntity *new_AMSearchEntity_init() NS_RETURNS_RETAINED;

@interface AMSearchEntity_$1 : NSObject < BSBserCreator >

- (AMSearchEntity *)createInstance;

- (instancetype)init;

@end

J2OBJC_EMPTY_STATIC_INIT(AMSearchEntity_$1)

__attribute__((unused)) static void AMSearchEntity_$1_init(AMSearchEntity_$1 *self);

__attribute__((unused)) static AMSearchEntity_$1 *new_AMSearchEntity_$1_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMSearchEntity_$1)

J2OBJC_INITIALIZED_DEFN(AMSearchEntity)

id<BSBserCreator> AMSearchEntity_CREATOR_;

@implementation AMSearchEntity

- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)order
                  withAMAvatar:(AMAvatar *)avatar
                  withNSString:(NSString *)title {
  AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(self, peer, order, avatar, title);
  return self;
}

- (instancetype)init {
  AMSearchEntity_init(self);
  return self;
}

- (AMPeer *)getPeer {
  return peer_;
}

- (AMAvatar *)getAvatar {
  return avatar_;
}

- (NSString *)getTitle {
  return title_;
}

- (jlong)getOrder {
  return order_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  peer_ = AMPeer_fromBytesWithByteArray_([((BSBserValues *) nil_chk(values)) getBytesWithInt:1]);
  order_ = [values getLongWithInt:2];
  if ([values optBytesWithInt:3] != nil) {
    avatar_ = new_AMAvatar_initWithByteArray_([values getBytesWithInt:3]);
  }
  title_ = [values getStringWithInt:4];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:1 withBSBserObject:peer_];
  [writer writeLongWithInt:2 withLong:order_];
  if (avatar_ != nil) {
    [writer writeObjectWithInt:3 withBSBserObject:avatar_];
  }
  [writer writeStringWithInt:4 withNSString:title_];
}

- (jlong)getEngineId {
  return [((AMPeer *) nil_chk(peer_)) getUnuqueId];
}

- (jlong)getEngineSort {
  return order_;
}

- (NSString *)getEngineSearch {
  return title_;
}

+ (void)initialize {
  if (self == [AMSearchEntity class]) {
    AMSearchEntity_CREATOR_ = new_AMSearchEntity_$1_init();
    J2OBJC_SET_INITIALIZED(AMSearchEntity)
  }
}

@end

void AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(AMSearchEntity *self, AMPeer *peer, jlong order, AMAvatar *avatar, NSString *title) {
  (void) BSBserObject_init(self);
  self->peer_ = peer;
  self->order_ = order;
  self->avatar_ = avatar;
  self->title_ = title;
}

AMSearchEntity *new_AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(AMPeer *peer, jlong order, AMAvatar *avatar, NSString *title) {
  AMSearchEntity *self = [AMSearchEntity alloc];
  AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(self, peer, order, avatar, title);
  return self;
}

void AMSearchEntity_init(AMSearchEntity *self) {
  (void) BSBserObject_init(self);
}

AMSearchEntity *new_AMSearchEntity_init() {
  AMSearchEntity *self = [AMSearchEntity alloc];
  AMSearchEntity_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMSearchEntity)

@implementation AMSearchEntity_$1

- (AMSearchEntity *)createInstance {
  return new_AMSearchEntity_init();
}

- (instancetype)init {
  AMSearchEntity_$1_init(self);
  return self;
}

@end

void AMSearchEntity_$1_init(AMSearchEntity_$1 *self) {
  (void) NSObject_init(self);
}

AMSearchEntity_$1 *new_AMSearchEntity_$1_init() {
  AMSearchEntity_$1 *self = [AMSearchEntity_$1 alloc];
  AMSearchEntity_$1_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMSearchEntity_$1)
