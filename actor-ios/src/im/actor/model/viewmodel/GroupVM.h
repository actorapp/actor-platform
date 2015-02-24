//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/actor-ios/build/java/im/actor/model/viewmodel/GroupVM.java
//

#ifndef _ImActorModelViewmodelGroupVM_H_
#define _ImActorModelViewmodelGroupVM_H_

@class AMGroup;
@class AMValueModel;
@class JavaUtilArrayList;
@protocol AMModelChangedListener;

#include "J2ObjC_header.h"
#include "im/actor/model/mvvm/BaseValueModel.h"
#include "java/lang/Runnable.h"

@interface ImActorModelViewmodelGroupVM : AMBaseValueModel {
}

- (instancetype)initWithAMGroup:(AMGroup *)rawObj;

- (jint)getId;

- (jlong)getHash;

- (jlong)getCreatorId;

- (AMValueModel *)getName;

- (AMValueModel *)getAvatar;

- (AMValueModel *)isMember;

- (AMValueModel *)getMembers;

- (AMValueModel *)getPresence;

- (void)updateValuesWithId:(AMGroup *)rawObj;

- (void)subscribeWithAMModelChangedListener:(id<AMModelChangedListener>)listener;

- (void)unsubscribeWithAMModelChangedListener:(id<AMModelChangedListener>)listener;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelViewmodelGroupVM)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelViewmodelGroupVM)

@interface ImActorModelViewmodelGroupVM_$1 : NSObject < JavaLangRunnable > {
}

- (void)run;

- (instancetype)initWithImActorModelViewmodelGroupVM:(ImActorModelViewmodelGroupVM *)outer$;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelViewmodelGroupVM_$1)

CF_EXTERN_C_BEGIN
CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelViewmodelGroupVM_$1)

#endif // _ImActorModelViewmodelGroupVM_H_
