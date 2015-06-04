//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/content/FileRemoteSource.java
//

#ifndef _AMFileRemoteSource_H_
#define _AMFileRemoteSource_H_

#include "J2ObjC_header.h"
#include "im/actor/model/entity/content/FileSource.h"

@class AMFileReference;

@interface AMFileRemoteSource : AMFileSource

#pragma mark Public

- (instancetype)initWithAMFileReference:(AMFileReference *)fileReference;

- (NSString *)getFileName;

- (AMFileReference *)getFileReference;

- (jint)getSize;

@end

J2OBJC_EMPTY_STATIC_INIT(AMFileRemoteSource)

FOUNDATION_EXPORT void AMFileRemoteSource_initWithAMFileReference_(AMFileRemoteSource *self, AMFileReference *fileReference);

FOUNDATION_EXPORT AMFileRemoteSource *new_AMFileRemoteSource_initWithAMFileReference_(AMFileReference *fileReference) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMFileRemoteSource)

typedef AMFileRemoteSource ImActorModelEntityContentFileRemoteSource;

#endif // _AMFileRemoteSource_H_
