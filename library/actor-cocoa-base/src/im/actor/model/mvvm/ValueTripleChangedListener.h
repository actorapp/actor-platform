//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/mvvm/ValueTripleChangedListener.java
//

#ifndef _AMValueTripleChangedListener_H_
#define _AMValueTripleChangedListener_H_

#include "J2ObjC_header.h"

@class AMValueModel;

@protocol AMValueTripleChangedListener < NSObject, JavaObject >

- (void)onChangedWithId:(id)val
       withAMValueModel:(AMValueModel *)valueModel
                 withId:(id)val2
       withAMValueModel:(AMValueModel *)valueModel2
                 withId:(id)val3
       withAMValueModel:(AMValueModel *)valueModel3;

@end

J2OBJC_EMPTY_STATIC_INIT(AMValueTripleChangedListener)

J2OBJC_TYPE_LITERAL_HEADER(AMValueTripleChangedListener)

#define ImActorModelMvvmValueTripleChangedListener AMValueTripleChangedListener

#endif // _AMValueTripleChangedListener_H_
