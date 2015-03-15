//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/mvvm/MVVMCollection.java
//

#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/engine/KeyValueEngine.h"
#include "im/actor/model/droidkit/engine/KeyValueItem.h"
#include "im/actor/model/droidkit/engine/KeyValueRecord.h"
#include "im/actor/model/droidkit/engine/KeyValueStorage.h"
#include "im/actor/model/mvvm/BaseValueModel.h"
#include "im/actor/model/mvvm/MVVMCollection.h"
#include "im/actor/model/mvvm/MVVMEngine.h"
#include "java/lang/Long.h"
#include "java/lang/RuntimeException.h"
#include "java/util/ArrayList.h"
#include "java/util/HashMap.h"
#include "java/util/List.h"

#pragma clang diagnostic ignored "-Wprotocol"
#pragma clang diagnostic ignored "-Wincomplete-implementation"

__attribute__((unused)) static void AMMVVMCollection_notifyChangeWithJavaUtilList_(AMMVVMCollection *self, id<JavaUtilList> items);
__attribute__((unused)) static void AMMVVMCollection_notifyRemoveWithLongArray_(AMMVVMCollection *self, IOSLongArray *ids);
__attribute__((unused)) static void AMMVVMCollection_notifyClear(AMMVVMCollection *self);

@interface AMMVVMCollection () {
 @public
  JavaUtilHashMap *values_;
  id<ImActorModelDroidkitEngineKeyValueStorage> collectionStorage_;
  AMMVVMCollection_ProxyKeyValueEngine *proxyKeyValueEngine_;
}

- (void)notifyChangeWithJavaUtilList:(id<JavaUtilList>)items;

- (void)notifyRemoveWithLongArray:(IOSLongArray *)ids;

- (void)notifyClear;
@end

J2OBJC_FIELD_SETTER(AMMVVMCollection, values_, JavaUtilHashMap *)
J2OBJC_FIELD_SETTER(AMMVVMCollection, collectionStorage_, id<ImActorModelDroidkitEngineKeyValueStorage>)
J2OBJC_FIELD_SETTER(AMMVVMCollection, proxyKeyValueEngine_, AMMVVMCollection_ProxyKeyValueEngine *)

@interface AMMVVMCollection_ProxyKeyValueEngine () {
 @public
  AMMVVMCollection *this$0_;
  JavaUtilHashMap *cache_;
}
@end

J2OBJC_FIELD_SETTER(AMMVVMCollection_ProxyKeyValueEngine, this$0_, AMMVVMCollection *)
J2OBJC_FIELD_SETTER(AMMVVMCollection_ProxyKeyValueEngine, cache_, JavaUtilHashMap *)

@interface AMMVVMCollection_$1 () {
 @public
  AMMVVMCollection *this$0_;
  id<JavaUtilList> val$items_;
}
@end

J2OBJC_FIELD_SETTER(AMMVVMCollection_$1, this$0_, AMMVVMCollection *)
J2OBJC_FIELD_SETTER(AMMVVMCollection_$1, val$items_, id<JavaUtilList>)

@interface AMMVVMCollection_$2 () {
 @public
  AMMVVMCollection *this$0_;
  IOSLongArray *val$ids_;
}
@end

J2OBJC_FIELD_SETTER(AMMVVMCollection_$2, this$0_, AMMVVMCollection *)
J2OBJC_FIELD_SETTER(AMMVVMCollection_$2, val$ids_, IOSLongArray *)

@interface AMMVVMCollection_$3 () {
 @public
  AMMVVMCollection *this$0_;
}
@end

J2OBJC_FIELD_SETTER(AMMVVMCollection_$3, this$0_, AMMVVMCollection *)

@implementation AMMVVMCollection

- (instancetype)initWithImActorModelDroidkitEngineKeyValueStorage:(id<ImActorModelDroidkitEngineKeyValueStorage>)collectionStorage {
  if (self = [super init]) {
    values_ = [[JavaUtilHashMap alloc] init];
    self->collectionStorage_ = collectionStorage;
    self->proxyKeyValueEngine_ = [[AMMVVMCollection_ProxyKeyValueEngine alloc] initWithAMMVVMCollection:self];
  }
  return self;
}

- (id<ImActorModelDroidkitEngineKeyValueEngine>)getEngine {
  return proxyKeyValueEngine_;
}

- (id)getWithLong:(jlong)id_ {
  if ([((JavaUtilHashMap *) nil_chk(values_)) getWithId:JavaLangLong_valueOfWithLong_(id_)] == nil) {
    id<ImActorModelDroidkitEngineKeyValueItem> res = [((AMMVVMCollection_ProxyKeyValueEngine *) nil_chk(proxyKeyValueEngine_)) getValueWithLong:id_];
    if (res != nil) {
      (void) [values_ putWithId:JavaLangLong_valueOfWithLong_(id_) withId:[self createNewWithImActorModelDroidkitEngineKeyValueItem:res]];
    }
    else {
      @throw [[JavaLangRuntimeException alloc] initWithNSString:JreStrcat("$J", @"Unable to find user #", id_)];
    }
  }
  return [values_ getWithId:JavaLangLong_valueOfWithLong_(id_)];
}

- (void)notifyChangeWithJavaUtilList:(id<JavaUtilList>)items {
  AMMVVMCollection_notifyChangeWithJavaUtilList_(self, items);
}

- (void)notifyRemoveWithLongArray:(IOSLongArray *)ids {
  AMMVVMCollection_notifyRemoveWithLongArray_(self, ids);
}

- (void)notifyClear {
  AMMVVMCollection_notifyClear(self);
}

- (void)copyAllFieldsTo:(AMMVVMCollection *)other {
  [super copyAllFieldsTo:other];
  other->values_ = values_;
  other->collectionStorage_ = collectionStorage_;
  other->proxyKeyValueEngine_ = proxyKeyValueEngine_;
}

@end

void AMMVVMCollection_notifyChangeWithJavaUtilList_(AMMVVMCollection *self, id<JavaUtilList> items) {
  AMMVVMEngine_runOnUiThreadWithJavaLangRunnable_([[AMMVVMCollection_$1 alloc] initWithAMMVVMCollection:self withJavaUtilList:items]);
}

void AMMVVMCollection_notifyRemoveWithLongArray_(AMMVVMCollection *self, IOSLongArray *ids) {
  AMMVVMEngine_runOnUiThreadWithJavaLangRunnable_([[AMMVVMCollection_$2 alloc] initWithAMMVVMCollection:self withLongArray:ids]);
}

void AMMVVMCollection_notifyClear(AMMVVMCollection *self) {
  AMMVVMEngine_runOnUiThreadWithJavaLangRunnable_([[AMMVVMCollection_$3 alloc] initWithAMMVVMCollection:self]);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMVVMCollection)

@implementation AMMVVMCollection_ProxyKeyValueEngine

- (void)addOrUpdateItemWithImActorModelDroidkitEngineKeyValueItem:(id<ImActorModelDroidkitEngineKeyValueItem>)item {
  @synchronized(self) {
    (void) [((JavaUtilHashMap *) nil_chk(cache_)) putWithId:JavaLangLong_valueOfWithLong_([((id<ImActorModelDroidkitEngineKeyValueItem>) nil_chk(item)) getEngineId]) withId:item];
    JavaUtilArrayList *res = [[JavaUtilArrayList alloc] init];
    [res addWithId:item];
    AMMVVMCollection_notifyChangeWithJavaUtilList_(this$0_, res);
    IOSByteArray *data = [this$0_ serializeWithImActorModelDroidkitEngineKeyValueItem:item];
    [((id<ImActorModelDroidkitEngineKeyValueStorage>) nil_chk(this$0_->collectionStorage_)) addOrUpdateItemWithLong:[item getEngineId] withByteArray:data];
  }
}

- (void)addOrUpdateItemsWithJavaUtilList:(id<JavaUtilList>)values {
  @synchronized(self) {
    for (id<ImActorModelDroidkitEngineKeyValueItem> __strong t in nil_chk(values)) {
      (void) [((JavaUtilHashMap *) nil_chk(cache_)) putWithId:JavaLangLong_valueOfWithLong_([((id<ImActorModelDroidkitEngineKeyValueItem>) nil_chk(t)) getEngineId]) withId:t];
    }
    AMMVVMCollection_notifyChangeWithJavaUtilList_(this$0_, values);
    JavaUtilArrayList *records = [[JavaUtilArrayList alloc] init];
    for (id<ImActorModelDroidkitEngineKeyValueItem> __strong v in values) {
      [records addWithId:[[ImActorModelDroidkitEngineKeyValueRecord alloc] initWithLong:[((id<ImActorModelDroidkitEngineKeyValueItem>) nil_chk(v)) getEngineId] withByteArray:[this$0_ serializeWithImActorModelDroidkitEngineKeyValueItem:v]]];
    }
    [((id<ImActorModelDroidkitEngineKeyValueStorage>) nil_chk(this$0_->collectionStorage_)) addOrUpdateItemsWithJavaUtilList:records];
  }
}

- (void)removeItemWithLong:(jlong)id_ {
  @synchronized(self) {
    (void) [((JavaUtilHashMap *) nil_chk(cache_)) removeWithId:JavaLangLong_valueOfWithLong_(id_)];
    AMMVVMCollection_notifyRemoveWithLongArray_(this$0_, [IOSLongArray newArrayWithLongs:(jlong[]){ id_ } count:1]);
    [((id<ImActorModelDroidkitEngineKeyValueStorage>) nil_chk(this$0_->collectionStorage_)) removeItemWithLong:id_];
  }
}

- (void)removeItemsWithLongArray:(IOSLongArray *)ids {
  @synchronized(self) {
    {
      IOSLongArray *a__ = ids;
      jlong const *b__ = ((IOSLongArray *) nil_chk(a__))->buffer_;
      jlong const *e__ = b__ + a__->size_;
      while (b__ < e__) {
        jlong l = *b__++;
        (void) [((JavaUtilHashMap *) nil_chk(cache_)) removeWithId:JavaLangLong_valueOfWithLong_(l)];
      }
    }
    AMMVVMCollection_notifyRemoveWithLongArray_(this$0_, ids);
    [((id<ImActorModelDroidkitEngineKeyValueStorage>) nil_chk(this$0_->collectionStorage_)) removeItemsWithLongArray:ids];
  }
}

- (void)clear {
  @synchronized(self) {
    [((JavaUtilHashMap *) nil_chk(cache_)) clear];
    AMMVVMCollection_notifyClear(this$0_);
    [((id<ImActorModelDroidkitEngineKeyValueStorage>) nil_chk(this$0_->collectionStorage_)) clear];
  }
}

- (id)getValueWithLong:(jlong)id_ {
  @synchronized(self) {
    if ([((JavaUtilHashMap *) nil_chk(cache_)) containsKeyWithId:JavaLangLong_valueOfWithLong_(id_)]) {
      return [cache_ getWithId:JavaLangLong_valueOfWithLong_(id_)];
    }
    IOSByteArray *data = [((id<ImActorModelDroidkitEngineKeyValueStorage>) nil_chk(this$0_->collectionStorage_)) getValueWithLong:id_];
    if (data != nil) {
      id<ImActorModelDroidkitEngineKeyValueItem> res = [this$0_ deserializeWithByteArray:data];
      (void) [cache_ putWithId:JavaLangLong_valueOfWithLong_([((id<ImActorModelDroidkitEngineKeyValueItem>) nil_chk(res)) getEngineId]) withId:res];
      return res;
    }
    else {
      return nil;
    }
  }
}

- (instancetype)initWithAMMVVMCollection:(AMMVVMCollection *)outer$ {
  this$0_ = outer$;
  if (self = [super init]) {
    cache_ = [[JavaUtilHashMap alloc] init];
  }
  return self;
}

- (void)copyAllFieldsTo:(AMMVVMCollection_ProxyKeyValueEngine *)other {
  [super copyAllFieldsTo:other];
  other->this$0_ = this$0_;
  other->cache_ = cache_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMVVMCollection_ProxyKeyValueEngine)

@implementation AMMVVMCollection_$1

- (void)run {
  for (id<ImActorModelDroidkitEngineKeyValueItem> __strong i in nil_chk(val$items_)) {
    if ([((JavaUtilHashMap *) nil_chk(this$0_->values_)) containsKeyWithId:JavaLangLong_valueOfWithLong_([((id<ImActorModelDroidkitEngineKeyValueItem>) nil_chk(i)) getEngineId])]) {
      [((AMBaseValueModel *) nil_chk([this$0_->values_ getWithId:JavaLangLong_valueOfWithLong_([i getEngineId])])) updateWithId:i];
    }
  }
}

- (instancetype)initWithAMMVVMCollection:(AMMVVMCollection *)outer$
                        withJavaUtilList:(id<JavaUtilList>)capture$0 {
  this$0_ = outer$;
  val$items_ = capture$0;
  return [super init];
}

- (void)copyAllFieldsTo:(AMMVVMCollection_$1 *)other {
  [super copyAllFieldsTo:other];
  other->this$0_ = this$0_;
  other->val$items_ = val$items_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMVVMCollection_$1)

@implementation AMMVVMCollection_$2

- (void)run {
  {
    IOSLongArray *a__ = val$ids_;
    jlong const *b__ = ((IOSLongArray *) nil_chk(a__))->buffer_;
    jlong const *e__ = b__ + a__->size_;
    while (b__ < e__) {
      jlong l = *b__++;
      (void) [((JavaUtilHashMap *) nil_chk(this$0_->values_)) removeWithId:JavaLangLong_valueOfWithLong_(l)];
    }
  }
}

- (instancetype)initWithAMMVVMCollection:(AMMVVMCollection *)outer$
                           withLongArray:(IOSLongArray *)capture$0 {
  this$0_ = outer$;
  val$ids_ = capture$0;
  return [super init];
}

- (void)copyAllFieldsTo:(AMMVVMCollection_$2 *)other {
  [super copyAllFieldsTo:other];
  other->this$0_ = this$0_;
  other->val$ids_ = val$ids_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMVVMCollection_$2)

@implementation AMMVVMCollection_$3

- (void)run {
  [((JavaUtilHashMap *) nil_chk(this$0_->values_)) clear];
}

- (instancetype)initWithAMMVVMCollection:(AMMVVMCollection *)outer$ {
  this$0_ = outer$;
  return [super init];
}

- (void)copyAllFieldsTo:(AMMVVMCollection_$3 *)other {
  [super copyAllFieldsTo:other];
  other->this$0_ = this$0_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMMVVMCollection_$3)
