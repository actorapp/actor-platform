//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/mvvm/ValueChangedListener.java
//

#ifndef _AMValueChangedListener_H_
#define _AMValueChangedListener_H_

#include "J2ObjC_header.h"

@class AMValueModel;

@protocol AMValueChangedListener < NSObject, JavaObject >

- (void)onChanged:(id)val
        withModel:(AMValueModel *)valueModel;

@end

J2OBJC_EMPTY_STATIC_INIT(AMValueChangedListener)

J2OBJC_TYPE_LITERAL_HEADER(AMValueChangedListener)

#define ImActorModelMvvmValueChangedListener AMValueChangedListener

#endif // _AMValueChangedListener_H_
