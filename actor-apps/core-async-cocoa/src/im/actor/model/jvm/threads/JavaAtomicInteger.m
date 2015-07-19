//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core-async/src/main/java/im/actor/model/jvm/threads/JavaAtomicInteger.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/jvm/threads/JavaAtomicInteger.h"
#include "im/actor/model/util/AtomicIntegerCompat.h"
#include "java/util/concurrent/atomic/AtomicInteger.h"

@interface ImActorModelJvmThreadsJavaAtomicInteger () {
 @public
  JavaUtilConcurrentAtomicAtomicInteger *atomicInteger_;
}

@end

J2OBJC_FIELD_SETTER(ImActorModelJvmThreadsJavaAtomicInteger, atomicInteger_, JavaUtilConcurrentAtomicAtomicInteger *)

@implementation ImActorModelJvmThreadsJavaAtomicInteger

- (instancetype)initWithInt:(jint)value {
  ImActorModelJvmThreadsJavaAtomicInteger_initWithInt_(self, value);
  return self;
}

- (jint)get {
  return [((JavaUtilConcurrentAtomicAtomicInteger *) nil_chk(atomicInteger_)) get];
}

- (jint)incrementAndGet {
  return [((JavaUtilConcurrentAtomicAtomicInteger *) nil_chk(atomicInteger_)) incrementAndGet];
}

- (jint)getAndIncrement {
  return [((JavaUtilConcurrentAtomicAtomicInteger *) nil_chk(atomicInteger_)) getAndIncrement];
}

- (void)compareAndSetWithInt:(jint)exp
                     withInt:(jint)v {
  [((JavaUtilConcurrentAtomicAtomicInteger *) nil_chk(atomicInteger_)) compareAndSetWithInt:exp withInt:v];
}

- (void)setWithInt:(jint)v {
  [((JavaUtilConcurrentAtomicAtomicInteger *) nil_chk(atomicInteger_)) setWithInt:v];
}

@end

void ImActorModelJvmThreadsJavaAtomicInteger_initWithInt_(ImActorModelJvmThreadsJavaAtomicInteger *self, jint value) {
  (void) AMAtomicIntegerCompat_init(self);
  self->atomicInteger_ = new_JavaUtilConcurrentAtomicAtomicInteger_initWithInt_(value);
}

ImActorModelJvmThreadsJavaAtomicInteger *new_ImActorModelJvmThreadsJavaAtomicInteger_initWithInt_(jint value) {
  ImActorModelJvmThreadsJavaAtomicInteger *self = [ImActorModelJvmThreadsJavaAtomicInteger alloc];
  ImActorModelJvmThreadsJavaAtomicInteger_initWithInt_(self, value);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelJvmThreadsJavaAtomicInteger)
