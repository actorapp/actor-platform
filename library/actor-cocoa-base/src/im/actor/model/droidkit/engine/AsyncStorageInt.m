//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/droidkit/engine/AsyncStorageInt.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/droidkit/engine/AsyncStorageInt.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/actors/ActorCreator.h"
#include "im/actor/model/droidkit/actors/ActorRef.h"
#include "im/actor/model/droidkit/actors/ActorSystem.h"
#include "im/actor/model/droidkit/actors/Props.h"
#include "im/actor/model/droidkit/bser/BserCreator.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/engine/AsyncStorageActor.h"
#include "im/actor/model/droidkit/engine/AsyncStorageInt.h"
#include "im/actor/model/droidkit/engine/ListEngineDisplayLoadCallback.h"
#include "im/actor/model/droidkit/engine/ListEngineItem.h"
#include "im/actor/model/droidkit/engine/ListStorageDisplayEx.h"
#include "java/lang/Integer.h"
#include "java/lang/InterruptedException.h"
#include "java/lang/Long.h"
#include "java/lang/RuntimeException.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"

@interface DKAsyncStorageInt () {
 @public
  DKActorRef *storageActor_;
}

@end

J2OBJC_FIELD_SETTER(DKAsyncStorageInt, storageActor_, DKActorRef *)

static jint DKAsyncStorageInt_NEXT_ID_ = 
#line 20
0;
J2OBJC_STATIC_FIELD_GETTER(DKAsyncStorageInt, NEXT_ID_, jint)
J2OBJC_STATIC_FIELD_REF_GETTER(DKAsyncStorageInt, NEXT_ID_, jint)

@interface DKAsyncStorageInt_$1 : NSObject < DKActorCreator > {
 @public
  id<DKListStorageDisplayEx> val$storage_;
  id<BSBserCreator> val$creator_;
}

- (DKAsyncStorageActor *)create;

- (instancetype)initWithDKListStorageDisplayEx:(id<DKListStorageDisplayEx>)capture$0
                             withBSBserCreator:(id<BSBserCreator>)capture$1;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageInt_$1)

J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$1, val$storage_, id<DKListStorageDisplayEx>)
J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$1, val$creator_, id<BSBserCreator>)

__attribute__((unused)) static void DKAsyncStorageInt_$1_initWithDKListStorageDisplayEx_withBSBserCreator_(DKAsyncStorageInt_$1 *self, id<DKListStorageDisplayEx> capture$0, id<BSBserCreator> capture$1);

__attribute__((unused)) static DKAsyncStorageInt_$1 *new_DKAsyncStorageInt_$1_initWithDKListStorageDisplayEx_withBSBserCreator_(id<DKListStorageDisplayEx> capture$0, id<BSBserCreator> capture$1) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageInt_$1)

@interface DKAsyncStorageInt_$2 : NSObject < DKAsyncStorageActor_LoadItemCallback > {
 @public
  id val$lock_;
  id<JavaUtilList> val$resultList_;
}

- (void)onLoadedWithBSBserObject:(BSBserObject<DKListEngineItem> *)item;

- (instancetype)initWithId:(id)capture$0
          withJavaUtilList:(id<JavaUtilList>)capture$1;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageInt_$2)

J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$2, val$lock_, id)
J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$2, val$resultList_, id<JavaUtilList>)

__attribute__((unused)) static void DKAsyncStorageInt_$2_initWithId_withJavaUtilList_(DKAsyncStorageInt_$2 *self, id capture$0, id<JavaUtilList> capture$1);

__attribute__((unused)) static DKAsyncStorageInt_$2 *new_DKAsyncStorageInt_$2_initWithId_withJavaUtilList_(id capture$0, id<JavaUtilList> capture$1) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageInt_$2)

@interface DKAsyncStorageInt_$3 : NSObject < DKAsyncStorageActor_LoadItemCallback > {
 @public
  id val$lock_;
  id<JavaUtilList> val$resultList_;
}

- (void)onLoadedWithBSBserObject:(BSBserObject<DKListEngineItem> *)item;

- (instancetype)initWithId:(id)capture$0
          withJavaUtilList:(id<JavaUtilList>)capture$1;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageInt_$3)

J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$3, val$lock_, id)
J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$3, val$resultList_, id<JavaUtilList>)

__attribute__((unused)) static void DKAsyncStorageInt_$3_initWithId_withJavaUtilList_(DKAsyncStorageInt_$3 *self, id capture$0, id<JavaUtilList> capture$1);

__attribute__((unused)) static DKAsyncStorageInt_$3 *new_DKAsyncStorageInt_$3_initWithId_withJavaUtilList_(id capture$0, id<JavaUtilList> capture$1) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageInt_$3)

@interface DKAsyncStorageInt_$4 : NSObject < DKAsyncStorageActor_LoadCountCallback > {
 @public
  id val$lock_;
  id<JavaUtilList> val$resultList_;
}

- (void)onLoadedWithInt:(jint)count;

- (instancetype)initWithId:(id)capture$0
          withJavaUtilList:(id<JavaUtilList>)capture$1;

@end

J2OBJC_EMPTY_STATIC_INIT(DKAsyncStorageInt_$4)

J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$4, val$lock_, id)
J2OBJC_FIELD_SETTER(DKAsyncStorageInt_$4, val$resultList_, id<JavaUtilList>)

__attribute__((unused)) static void DKAsyncStorageInt_$4_initWithId_withJavaUtilList_(DKAsyncStorageInt_$4 *self, id capture$0, id<JavaUtilList> capture$1);

__attribute__((unused)) static DKAsyncStorageInt_$4 *new_DKAsyncStorageInt_$4_initWithId_withJavaUtilList_(id capture$0, id<JavaUtilList> capture$1) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(DKAsyncStorageInt_$4)


#line 18
@implementation DKAsyncStorageInt


#line 24
- (instancetype)initWithDKListStorageDisplayEx:(id<DKListStorageDisplayEx>)storage
                             withBSBserCreator:(id<BSBserCreator>)creator {
  DKAsyncStorageInt_initWithDKListStorageDisplayEx_withBSBserCreator_(self, storage, creator);
  return self;
}


#line 33
- (void)addOrUpdateItemsWithJavaUtilList:(id<JavaUtilList>)items {
  [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_AddOrUpdate_initWithJavaUtilList_(items)];
}

- (void)replaceItemsWithJavaUtilList:(id<JavaUtilList>)items {
  [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_Replace_initWithJavaUtilList_(items)];
}

- (void)removeWithLongArray:(IOSLongArray *)keys {
  [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_Remove_initWithLongArray_(keys)];
}

- (void)clear {
  [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_Clear_init()];
}


#line 51
- (id)getValueWithLong:(jlong)value {
  id lock = new_NSObject_init();
  id<JavaUtilList> resultList = new_JavaUtilArrayList_init();
  @synchronized(lock) {
    [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_LoadItem_initWithLong_withDKAsyncStorageActor_LoadItemCallback_(value, new_DKAsyncStorageInt_$2_initWithId_withJavaUtilList_(lock, resultList))];
    
#line 67
    @try {
      [lock wait];
    }
    @catch (
#line 69
    JavaLangInterruptedException *e) {
      return nil;
    }
    
#line 73
    if ([resultList size] > 0) {
      return [resultList getWithInt:0];
    }
    else {
      
#line 76
      return nil;
    }
  }
}


#line 81
- (id)getHeadValue {
  id lock = new_NSObject_init();
  id<JavaUtilList> resultList = new_JavaUtilArrayList_init();
  @synchronized(lock) {
    [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_LoadHead_initWithDKAsyncStorageActor_LoadItemCallback_(new_DKAsyncStorageInt_$3_initWithId_withJavaUtilList_(lock, resultList))];
    
#line 97
    @try {
      [lock wait];
    }
    @catch (
#line 99
    JavaLangInterruptedException *e) {
      return nil;
    }
    
#line 103
    if ([resultList size] > 0) {
      return [resultList getWithInt:0];
    }
    else {
      
#line 106
      return nil;
    }
  }
}


#line 111
- (jint)getCount {
  id lock = new_NSObject_init();
  id<JavaUtilList> resultList = new_JavaUtilArrayList_init();
  @synchronized(lock) {
    [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_LoadCount_initWithDKAsyncStorageActor_LoadCountCallback_(new_DKAsyncStorageInt_$4_initWithId_withJavaUtilList_(lock, resultList))];
    
#line 125
    @try {
      [lock wait];
    }
    @catch (
#line 127
    JavaLangInterruptedException *e) {
      return 0;
    }
    
#line 131
    if ([resultList size] > 0) {
      return [((JavaLangInteger *) nil_chk([resultList getWithInt:0])) intValue];
    }
    else {
      
#line 134
      return 0;
    }
  }
}


#line 141
- (void)loadForwardWithNSString:(NSString *)query
               withJavaLangLong:(JavaLangLong *)afterSortKey
                        withInt:(jint)limit
withDKListEngineDisplayLoadCallback:(id<DKListEngineDisplayLoadCallback>)callback {
  [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_LoadForward_initWithNSString_withJavaLangLong_withInt_withDKListEngineDisplayLoadCallback_(query, afterSortKey, limit, callback)];
}


#line 145
- (void)loadBackwardWithNSString:(NSString *)query
                withJavaLangLong:(JavaLangLong *)beforeSortKey
                         withInt:(jint)limit
withDKListEngineDisplayLoadCallback:(id<DKListEngineDisplayLoadCallback>)callback {
  [((DKActorRef *) nil_chk(storageActor_)) sendWithId:new_DKAsyncStorageActor_LoadBackward_initWithNSString_withJavaLangLong_withInt_withDKListEngineDisplayLoadCallback_(query, beforeSortKey, limit, callback)];
}


#line 149
- (void)loadCenterWithLong:(jlong)centerSortKey
                   withInt:(jint)limit
withDKListEngineDisplayLoadCallback:(id<DKListEngineDisplayLoadCallback>)callback {
  @throw new_JavaLangRuntimeException_initWithNSString_(@"Unsupported");
}

@end


#line 24
void DKAsyncStorageInt_initWithDKListStorageDisplayEx_withBSBserCreator_(DKAsyncStorageInt *self, id<DKListStorageDisplayEx> storage, id<BSBserCreator> creator) {
  (void) NSObject_init(self);
  
#line 25
  self->storageActor_ = [((DKActorSystem *) nil_chk(DKActorSystem_system())) actorOfWithDKProps:[((DKProps *) nil_chk(DKProps_createWithIOSClass_withDKActorCreator_(DKAsyncStorageActor_class_(), new_DKAsyncStorageInt_$1_initWithDKListStorageDisplayEx_withBSBserCreator_(storage, creator)))) changeDispatcherWithNSString:
#line 30
  @"db"] withNSString:JreStrcat("$I", @"list_engine/", DKAsyncStorageInt_NEXT_ID_++)];
}


#line 24
DKAsyncStorageInt *new_DKAsyncStorageInt_initWithDKListStorageDisplayEx_withBSBserCreator_(id<DKListStorageDisplayEx> storage, id<BSBserCreator> creator) {
  DKAsyncStorageInt *self = [DKAsyncStorageInt alloc];
  DKAsyncStorageInt_initWithDKListStorageDisplayEx_withBSBserCreator_(self, storage, creator);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKAsyncStorageInt)

@implementation DKAsyncStorageInt_$1


#line 27
- (DKAsyncStorageActor *)create {
  return new_DKAsyncStorageActor_initWithDKListStorageDisplayEx_withBSBserCreator_(val$storage_, val$creator_);
}

- (instancetype)initWithDKListStorageDisplayEx:(id<DKListStorageDisplayEx>)capture$0
                             withBSBserCreator:(id<BSBserCreator>)capture$1 {
  DKAsyncStorageInt_$1_initWithDKListStorageDisplayEx_withBSBserCreator_(self, capture$0, capture$1);
  return self;
}

@end

void DKAsyncStorageInt_$1_initWithDKListStorageDisplayEx_withBSBserCreator_(DKAsyncStorageInt_$1 *self, id<DKListStorageDisplayEx> capture$0, id<BSBserCreator> capture$1) {
  self->val$storage_ = capture$0;
  self->val$creator_ = capture$1;
  (void) NSObject_init(self);
}

DKAsyncStorageInt_$1 *new_DKAsyncStorageInt_$1_initWithDKListStorageDisplayEx_withBSBserCreator_(id<DKListStorageDisplayEx> capture$0, id<BSBserCreator> capture$1) {
  DKAsyncStorageInt_$1 *self = [DKAsyncStorageInt_$1 alloc];
  DKAsyncStorageInt_$1_initWithDKListStorageDisplayEx_withBSBserCreator_(self, capture$0, capture$1);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKAsyncStorageInt_$1)

@implementation DKAsyncStorageInt_$2


#line 57
- (void)onLoadedWithBSBserObject:(BSBserObject<DKListEngineItem> *)item {
  @synchronized(val$lock_) {
    if (item != nil) {
      [((id<JavaUtilList>) nil_chk(val$resultList_)) addWithId:item];
    }
    [nil_chk(val$lock_) notify];
  }
}

- (instancetype)initWithId:(id)capture$0
          withJavaUtilList:(id<JavaUtilList>)capture$1 {
  DKAsyncStorageInt_$2_initWithId_withJavaUtilList_(self, capture$0, capture$1);
  return self;
}

@end

void DKAsyncStorageInt_$2_initWithId_withJavaUtilList_(DKAsyncStorageInt_$2 *self, id capture$0, id<JavaUtilList> capture$1) {
  self->val$lock_ = capture$0;
  self->val$resultList_ = capture$1;
  (void) NSObject_init(self);
}

DKAsyncStorageInt_$2 *new_DKAsyncStorageInt_$2_initWithId_withJavaUtilList_(id capture$0, id<JavaUtilList> capture$1) {
  DKAsyncStorageInt_$2 *self = [DKAsyncStorageInt_$2 alloc];
  DKAsyncStorageInt_$2_initWithId_withJavaUtilList_(self, capture$0, capture$1);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKAsyncStorageInt_$2)

@implementation DKAsyncStorageInt_$3


#line 87
- (void)onLoadedWithBSBserObject:(BSBserObject<DKListEngineItem> *)item {
  @synchronized(val$lock_) {
    if (item != nil) {
      [((id<JavaUtilList>) nil_chk(val$resultList_)) addWithId:item];
    }
    [nil_chk(val$lock_) notify];
  }
}

- (instancetype)initWithId:(id)capture$0
          withJavaUtilList:(id<JavaUtilList>)capture$1 {
  DKAsyncStorageInt_$3_initWithId_withJavaUtilList_(self, capture$0, capture$1);
  return self;
}

@end

void DKAsyncStorageInt_$3_initWithId_withJavaUtilList_(DKAsyncStorageInt_$3 *self, id capture$0, id<JavaUtilList> capture$1) {
  self->val$lock_ = capture$0;
  self->val$resultList_ = capture$1;
  (void) NSObject_init(self);
}

DKAsyncStorageInt_$3 *new_DKAsyncStorageInt_$3_initWithId_withJavaUtilList_(id capture$0, id<JavaUtilList> capture$1) {
  DKAsyncStorageInt_$3 *self = [DKAsyncStorageInt_$3 alloc];
  DKAsyncStorageInt_$3_initWithId_withJavaUtilList_(self, capture$0, capture$1);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKAsyncStorageInt_$3)

@implementation DKAsyncStorageInt_$4


#line 117
- (void)onLoadedWithInt:(jint)count {
  @synchronized(val$lock_) {
    [((id<JavaUtilList>) nil_chk(val$resultList_)) addWithId:JavaLangInteger_valueOfWithInt_(count)];
    [nil_chk(val$lock_) notify];
  }
}

- (instancetype)initWithId:(id)capture$0
          withJavaUtilList:(id<JavaUtilList>)capture$1 {
  DKAsyncStorageInt_$4_initWithId_withJavaUtilList_(self, capture$0, capture$1);
  return self;
}

@end

void DKAsyncStorageInt_$4_initWithId_withJavaUtilList_(DKAsyncStorageInt_$4 *self, id capture$0, id<JavaUtilList> capture$1) {
  self->val$lock_ = capture$0;
  self->val$resultList_ = capture$1;
  (void) NSObject_init(self);
}

DKAsyncStorageInt_$4 *new_DKAsyncStorageInt_$4_initWithId_withJavaUtilList_(id capture$0, id<JavaUtilList> capture$1) {
  DKAsyncStorageInt_$4 *self = [DKAsyncStorageInt_$4 alloc];
  DKAsyncStorageInt_$4_initWithId_withJavaUtilList_(self, capture$0, capture$1);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(DKAsyncStorageInt_$4)
