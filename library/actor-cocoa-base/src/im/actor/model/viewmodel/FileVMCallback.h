//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/viewmodel/FileVMCallback.java
//

#ifndef _AMFileVMCallback_H_
#define _AMFileVMCallback_H_

#include "J2ObjC_header.h"

@protocol AMFileSystemReference;

@protocol AMFileVMCallback < NSObject, JavaObject >

- (void)onNotDownloaded;

- (void)onDownloadingWithFloat:(jfloat)progress;

- (void)onDownloadedWithAMFileSystemReference:(id<AMFileSystemReference>)reference;

@end

J2OBJC_EMPTY_STATIC_INIT(AMFileVMCallback)

J2OBJC_TYPE_LITERAL_HEADER(AMFileVMCallback)

#define ImActorModelViewmodelFileVMCallback AMFileVMCallback

#endif // _AMFileVMCallback_H_
