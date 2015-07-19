//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core-async/src/main/java/im/actor/model/jvm/threads/JavaAtomicLong.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/jvm/threads/JavaAtomicLong.h"
#include "im/actor/model/util/AtomicLongCompat.h"
#include "java/util/concurrent/atomic/AtomicLong.h"

@implementation ImActorModelJvmThreadsJavaAtomicLong

- (instancetype)initWithLong:(jlong)value {
  ImActorModelJvmThreadsJavaAtomicLong_initWithLong_(self, value);
  return self;
}

- (jlong)get {
  return [((JavaUtilConcurrentAtomicAtomicLong *) nil_chk(atomicLong_)) get];
}

- (jlong)incrementAndGet {
  return [((JavaUtilConcurrentAtomicAtomicLong *) nil_chk(atomicLong_)) incrementAndGet];
}

- (jlong)getAndIncrement {
  return [((JavaUtilConcurrentAtomicAtomicLong *) nil_chk(atomicLong_)) getAndIncrement];
}

- (void)setWithLong:(jlong)v {
  [((JavaUtilConcurrentAtomicAtomicLong *) nil_chk(atomicLong_)) setWithLong:v];
}

@end

void ImActorModelJvmThreadsJavaAtomicLong_initWithLong_(ImActorModelJvmThreadsJavaAtomicLong *self, jlong value) {
  (void) AMAtomicLongCompat_init(self);
  self->atomicLong_ = new_JavaUtilConcurrentAtomicAtomicLong_initWithLong_(value);
}

ImActorModelJvmThreadsJavaAtomicLong *new_ImActorModelJvmThreadsJavaAtomicLong_initWithLong_(jlong value) {
  ImActorModelJvmThreadsJavaAtomicLong *self = [ImActorModelJvmThreadsJavaAtomicLong alloc];
  ImActorModelJvmThreadsJavaAtomicLong_initWithLong_(self, value);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelJvmThreadsJavaAtomicLong)
