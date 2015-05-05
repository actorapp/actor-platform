//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/SearchEntity.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/SearchEntity.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/Bser.h"
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


#line 16
@implementation AMSearchEntity

+ (AMSearchEntity *)fromBytesWithByteArray:(IOSByteArray *)data {
  return AMSearchEntity_fromBytesWithByteArray_(data);
}


#line 35
- (instancetype)initWithAMPeer:(AMPeer *)peer
                      withLong:(jlong)order
                  withAMAvatar:(AMAvatar *)avatar
                  withNSString:(NSString *)title {
  AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(self, peer, order, avatar, title);
  return self;
}


#line 42
- (instancetype)init {
  AMSearchEntity_init(self);
  return self;
}


#line 46
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


#line 63
- (void)parseWithBSBserValues:(BSBserValues *)values {
  peer_ = AMPeer_fromBytesWithByteArray_([((BSBserValues *) nil_chk(values)) getBytesWithInt:1]);
  order_ = [values getLongWithInt:2];
  if ([values optBytesWithInt:3] != nil) {
    avatar_ = AMAvatar_fromBytesWithByteArray_([values getBytesWithInt:3]);
  }
  else {
    
#line 69
    avatar_ = nil;
  }
  title_ = [values getStringWithInt:4];
}


#line 75
- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeObjectWithInt:1 withBSBserObject:peer_];
  [writer writeLongWithInt:2 withLong:order_];
  if (avatar_ != nil) {
    [writer writeObjectWithInt:3 withBSBserObject:avatar_];
  }
  [writer writeStringWithInt:4 withNSString:title_];
}


#line 85
- (jlong)getEngineId {
  return [((AMPeer *) nil_chk(peer_)) getUnuqueId];
}


#line 90
- (jlong)getEngineSort {
  return order_;
}


#line 95
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


#line 18
AMSearchEntity *AMSearchEntity_fromBytesWithByteArray_(IOSByteArray *data) {
  AMSearchEntity_initialize();
  
#line 19
  return ((AMSearchEntity *) BSBser_parseWithBSBserObject_withByteArray_(new_AMSearchEntity_init(), data));
}


#line 35
void AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(AMSearchEntity *self, AMPeer *peer, jlong order, AMAvatar *avatar, NSString *title) {
  (void) BSBserObject_init(self);
  
#line 36
  self->peer_ = peer;
  self->order_ = order;
  self->avatar_ = avatar;
  self->title_ = title;
}


#line 35
AMSearchEntity *new_AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(AMPeer *peer, jlong order, AMAvatar *avatar, NSString *title) {
  AMSearchEntity *self = [AMSearchEntity alloc];
  AMSearchEntity_initWithAMPeer_withLong_withAMAvatar_withNSString_(self, peer, order, avatar, title);
  return self;
}


#line 42
void AMSearchEntity_init(AMSearchEntity *self) {
  (void) BSBserObject_init(self);
}


#line 42
AMSearchEntity *new_AMSearchEntity_init() {
  AMSearchEntity *self = [AMSearchEntity alloc];
  AMSearchEntity_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMSearchEntity)

@implementation AMSearchEntity_$1


#line 24
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
