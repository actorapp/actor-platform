//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/droidkit/actors/Actor.java
//

#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/actors/Actor.h"
#include "im/actor/model/droidkit/actors/ActorContext.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/actors/ActorSelection.h"
#include "im/actor/model/droidkit/actors/ActorSystem.h"
#include "im/actor/model/droidkit/actors/debug/TraceInterface.h"
#include "im/actor/model/droidkit/actors/extensions/RunnableExtension.h"
#include "im/actor/model/droidkit/actors/mailbox/Mailbox.h"
#include "im/actor/model/droidkit/actors/messages/DeadLetter.h"
#include "im/actor/model/droidkit/actors/tasks/ActorAskImpl.h"
#include "im/actor/model/droidkit/actors/tasks/AskCallback.h"
#include "im/actor/model/droidkit/actors/tasks/AskFuture.h"
#include "java/util/ArrayList.h"

__attribute__((unused)) static DKActorSystem *DKActor_system(DKActor *self);
__attribute__((unused)) static DKActorRef *DKActor_self__(DKActor *self);
__attribute__((unused)) static DKActorRef *DKActor_sender(DKActor *self);

@interface DKActor () {
 @public
  NSString *path_;
  DKActorContext *context__;
  DKMailbox *mailbox_;
  ImActorModelDroidkitActorsTasksActorAskImpl *askPattern_;
  JavaUtilArrayList *extensions_;
}
@end

J2OBJC_FIELD_SETTER(DKActor, path_, NSString *)
J2OBJC_FIELD_SETTER(DKActor, context__, DKActorContext *)
J2OBJC_FIELD_SETTER(DKActor, mailbox_, DKMailbox *)
J2OBJC_FIELD_SETTER(DKActor, askPattern_, ImActorModelDroidkitActorsTasksActorAskImpl *)
J2OBJC_FIELD_SETTER(DKActor, extensions_, JavaUtilArrayList *)

@implementation DKActor

- (instancetype)init {
  if (self = [super init]) {
    extensions_ = [[JavaUtilArrayList alloc] init];
  }
  return self;
}

- (void)initActorWithNSString:(NSString *)path
           withDKActorContext:(DKActorContext *)context
                withDKMailbox:(DKMailbox *)mailbox {
  self->path_ = path;
  self->context__ = context;
  self->mailbox_ = mailbox;
  self->askPattern_ = [[ImActorModelDroidkitActorsTasksActorAskImpl alloc] initWithDKActorRef:DKActor_self__(self)];
  [((JavaUtilArrayList *) nil_chk(self->extensions_)) addWithId:askPattern_];
  [self->extensions_ addWithId:[[ImActorModelDroidkitActorsExtensionsRunnableExtension alloc] init]];
}

- (JavaUtilArrayList *)getExtensions {
  return extensions_;
}

- (DKActorSystem *)system {
  return DKActor_system(self);
}

- (DKActorRef *)self__ {
  return DKActor_self__(self);
}

- (DKActorContext *)context {
  return context__;
}

- (DKActorRef *)sender {
  return DKActor_sender(self);
}

- (NSString *)getPath {
  return path_;
}

- (DKMailbox *)getMailbox {
  return mailbox_;
}

- (void)preStart {
}

- (void)onReceiveWithId:(id)message {
  [self dropWithId:message];
}

- (void)postStop {
}

- (void)finallyStop {
}

- (void)replyWithId:(id)message {
  if ([((DKActorContext *) nil_chk(context__)) sender] != nil) {
    [((DKActorRef *) nil_chk([context__ sender])) sendWithId:message withDKActorRef:DKActor_self__(self)];
  }
}

- (void)dropWithId:(id)message {
  if ([((DKActorSystem *) nil_chk(DKActor_system(self))) getTraceInterface] != nil) {
    [((id<ImActorModelDroidkitActorsDebugTraceInterface>) nil_chk([((DKActorSystem *) nil_chk(DKActor_system(self))) getTraceInterface])) onDropWithDKActorRef:DKActor_sender(self) withId:message withDKActor:self];
  }
  [self replyWithId:[[ImActorModelDroidkitActorsMessagesDeadLetter alloc] initWithId:message]];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)combineWithImActorModelDroidkitActorsTasksAskFutureArray:(IOSObjectArray *)futures {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) combineWithImActorModelDroidkitActorsTasksAskFutureArray:futures];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)combineWithImActorModelDroidkitActorsTasksAskCallback:(id<ImActorModelDroidkitActorsTasksAskCallback>)callback
                                                  withImActorModelDroidkitActorsTasksAskFutureArray:(IOSObjectArray *)futures {
  ImActorModelDroidkitActorsTasksAskFuture *future = [self combineWithImActorModelDroidkitActorsTasksAskFutureArray:futures];
  [((ImActorModelDroidkitActorsTasksAskFuture *) nil_chk(future)) addListenerWithImActorModelDroidkitActorsTasksAskCallback:callback];
  return future;
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorSelection:(DKActorSelection *)selection {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:[((DKActorSystem *) nil_chk(DKActor_system(self))) actorOfWithDKActorSelection:selection] withLong:0 withImActorModelDroidkitActorsTasksAskCallback:nil];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorSelection:(DKActorSelection *)selection
                                                             withLong:(jlong)timeout {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:[((DKActorSystem *) nil_chk(DKActor_system(self))) actorOfWithDKActorSelection:selection] withLong:timeout withImActorModelDroidkitActorsTasksAskCallback:nil];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorSelection:(DKActorSelection *)selection
                       withImActorModelDroidkitActorsTasksAskCallback:(id<ImActorModelDroidkitActorsTasksAskCallback>)callback {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:[((DKActorSystem *) nil_chk(DKActor_system(self))) actorOfWithDKActorSelection:selection] withLong:0 withImActorModelDroidkitActorsTasksAskCallback:callback];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorSelection:(DKActorSelection *)selection
                                                             withLong:(jlong)timeout
                       withImActorModelDroidkitActorsTasksAskCallback:(id<ImActorModelDroidkitActorsTasksAskCallback>)callback {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:[((DKActorSystem *) nil_chk(DKActor_system(self))) actorOfWithDKActorSelection:selection] withLong:timeout withImActorModelDroidkitActorsTasksAskCallback:callback];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorRef:(DKActorRef *)ref {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:ref withLong:0 withImActorModelDroidkitActorsTasksAskCallback:nil];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorRef:(DKActorRef *)ref
                                                       withLong:(jlong)timeout {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:ref withLong:timeout withImActorModelDroidkitActorsTasksAskCallback:nil];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorRef:(DKActorRef *)ref
                 withImActorModelDroidkitActorsTasksAskCallback:(id<ImActorModelDroidkitActorsTasksAskCallback>)callback {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:ref withLong:0 withImActorModelDroidkitActorsTasksAskCallback:callback];
}

- (ImActorModelDroidkitActorsTasksAskFuture *)askWithDKActorRef:(DKActorRef *)ref
                                                       withLong:(jlong)timeout
                 withImActorModelDroidkitActorsTasksAskCallback:(id<ImActorModelDroidkitActorsTasksAskCallback>)callback {
  return [((ImActorModelDroidkitActorsTasksActorAskImpl *) nil_chk(askPattern_)) askWithDKActorRef:ref withLong:timeout withImActorModelDroidkitActorsTasksAskCallback:callback];
}

- (void)copyAllFieldsTo:(DKActor *)other {
  [super copyAllFieldsTo:other];
  other->path_ = path_;
  other->context__ = context__;
  other->mailbox_ = mailbox_;
  other->askPattern_ = askPattern_;
  other->extensions_ = extensions_;
}

@end

DKActorSystem *DKActor_system(DKActor *self) {
  return [((DKActorContext *) nil_chk(self->context__)) getSystem];
}

DKActorRef *DKActor_self__(DKActor *self) {
  return [((DKActorContext *) nil_chk(self->context__)) getSelf];
}

DKActorRef *DKActor_sender(DKActor *self) {
  return [((DKActorContext *) nil_chk(self->context__)) sender];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKActor)
