//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/entity/Notification.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/entity/ContentDescription.h"
#include "im/actor/model/entity/Notification.h"
#include "im/actor/model/entity/Peer.h"

@interface AMNotification () {
 @public
  AMPeer *peer_;
  jint sender_;
  AMContentDescription *contentDescription_;
}

@end

J2OBJC_FIELD_SETTER(AMNotification, peer_, AMPeer *)
J2OBJC_FIELD_SETTER(AMNotification, contentDescription_, AMContentDescription *)

@implementation AMNotification

- (instancetype)initWithAMPeer:(AMPeer *)peer
                       withInt:(jint)sender
      withAMContentDescription:(AMContentDescription *)contentDescription {
  AMNotification_initWithAMPeer_withInt_withAMContentDescription_(self, peer, sender, contentDescription);
  return self;
}

- (AMPeer *)getPeer {
  return peer_;
}

- (jint)getSender {
  return sender_;
}

- (AMContentDescription *)getContentDescription {
  return contentDescription_;
}

@end

void AMNotification_initWithAMPeer_withInt_withAMContentDescription_(AMNotification *self, AMPeer *peer, jint sender, AMContentDescription *contentDescription) {
  (void) NSObject_init(self);
  self->peer_ = peer;
  self->sender_ = sender;
  self->contentDescription_ = contentDescription;
}

AMNotification *new_AMNotification_initWithAMPeer_withInt_withAMContentDescription_(AMPeer *peer, jint sender, AMContentDescription *contentDescription) {
  AMNotification *self = [AMNotification alloc];
  AMNotification_initWithAMPeer_withInt_withAMContentDescription_(self, peer, sender, contentDescription);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMNotification)
