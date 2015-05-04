//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/droidkit/actors/mailbox/Mailbox.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/droidkit/actors/mailbox/Mailbox.java"

#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/actors/mailbox/Envelope.h"
#include "im/actor/model/droidkit/actors/mailbox/Mailbox.h"
#include "im/actor/model/droidkit/actors/mailbox/MailboxesQueue.h"
#include "im/actor/model/droidkit/actors/mailbox/collections/EnvelopeCollection.h"
#include "im/actor/model/droidkit/actors/mailbox/collections/EnvelopeRoot.h"
#include "java/lang/RuntimeException.h"

@interface DKMailbox () {
 @public
  ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *envelopes_;
  id<ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection_EnvelopeComparator> comparator_;
}

@end

J2OBJC_FIELD_SETTER(DKMailbox, envelopes_, ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *)
J2OBJC_FIELD_SETTER(DKMailbox, comparator_, id<ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection_EnvelopeComparator>)

@interface DKMailbox_$1 : NSObject < ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection_EnvelopeComparator > {
 @public
  DKMailbox *this$0_;
}

- (jboolean)equalsWithDKEnvelope:(DKEnvelope *)a
                  withDKEnvelope:(DKEnvelope *)b;

- (instancetype)initWithDKMailbox:(DKMailbox *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(DKMailbox_$1)

J2OBJC_FIELD_SETTER(DKMailbox_$1, this$0_, DKMailbox *)

__attribute__((unused)) static void DKMailbox_$1_initWithDKMailbox_(DKMailbox_$1 *self, DKMailbox *outer$);

__attribute__((unused)) static DKMailbox_$1 *new_DKMailbox_$1_initWithDKMailbox_(DKMailbox *outer$) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKMailbox_$1)


#line 12
@implementation DKMailbox


#line 27
- (instancetype)initWithDKMailboxesQueue:(DKMailboxesQueue *)queue {
  DKMailbox_initWithDKMailboxesQueue_(self, queue);
  return self;
}


#line 38
- (void)scheduleWithDKEnvelope:(DKEnvelope *)envelope
                      withLong:(jlong)time {
  if ([((DKEnvelope *) nil_chk(envelope)) getMailbox] != self) {
    @throw new_JavaLangRuntimeException_initWithNSString_(@"envelope.mailbox != this mailbox");
  }
  
#line 43
  [((ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *) nil_chk(envelopes_)) putEnvelopeWithDKEnvelope:envelope withLong:time];
}


#line 52
- (void)scheduleOnceWithDKEnvelope:(DKEnvelope *)envelope
                          withLong:(jlong)time {
  if ([((DKEnvelope *) nil_chk(envelope)) getMailbox] != self) {
    @throw new_JavaLangRuntimeException_initWithNSString_(@"envelope.mailbox != this mailbox");
  }
  
#line 57
  [((ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *) nil_chk(envelopes_)) putEnvelopeOnceWithDKEnvelope:envelope withLong:time withImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection_EnvelopeComparator:comparator_];
}


#line 65
- (void)unscheduleWithDKEnvelope:(DKEnvelope *)envelope {
  [((ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *) nil_chk(envelopes_)) removeEnvelopeWithDKEnvelope:envelope withImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection_EnvelopeComparator:comparator_];
}


#line 74
- (IOSObjectArray *)allEnvelopes {
  return [((ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *) nil_chk(envelopes_)) allEnvelopes];
}


#line 81
- (void)clear {
  [((ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *) nil_chk(envelopes_)) clear];
}


#line 93
- (jboolean)isEqualEnvelopeWithDKEnvelope:(DKEnvelope *)a
                           withDKEnvelope:(DKEnvelope *)b {
  return [nil_chk([((DKEnvelope *) nil_chk(a)) getMessage]) getClass] == [nil_chk([((DKEnvelope *) nil_chk(b)) getMessage]) getClass];
}


#line 97
- (ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection *)getEnvelopes {
  return envelopes_;
}

@end


#line 27
void DKMailbox_initWithDKMailboxesQueue_(DKMailbox *self, DKMailboxesQueue *queue) {
  (void) NSObject_init(self);
  self->comparator_ = new_DKMailbox_$1_initWithDKMailbox_(self);
  
#line 28
  self->envelopes_ = new_ImActorModelDroidkitActorsMailboxCollectionsEnvelopeCollection_initWithImActorModelDroidkitActorsMailboxCollectionsEnvelopeRoot_([((DKMailboxesQueue *) nil_chk(queue)) getEnvelopeRoot]);
}


#line 27
DKMailbox *new_DKMailbox_initWithDKMailboxesQueue_(DKMailboxesQueue *queue) {
  DKMailbox *self = [DKMailbox alloc];
  DKMailbox_initWithDKMailboxesQueue_(self, queue);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKMailbox)

@implementation DKMailbox_$1


#line 17
- (jboolean)equalsWithDKEnvelope:(DKEnvelope *)a
                  withDKEnvelope:(DKEnvelope *)b {
  return [this$0_ isEqualEnvelopeWithDKEnvelope:a withDKEnvelope:b];
}

- (instancetype)initWithDKMailbox:(DKMailbox *)outer$ {
  DKMailbox_$1_initWithDKMailbox_(self, outer$);
  return self;
}

@end

void DKMailbox_$1_initWithDKMailbox_(DKMailbox_$1 *self, DKMailbox *outer$) {
  self->this$0_ = outer$;
  (void) NSObject_init(self);
}

DKMailbox_$1 *new_DKMailbox_$1_initWithDKMailbox_(DKMailbox *outer$) {
  DKMailbox_$1 *self = [DKMailbox_$1 alloc];
  DKMailbox_$1_initWithDKMailbox_(self, outer$);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKMailbox_$1)
