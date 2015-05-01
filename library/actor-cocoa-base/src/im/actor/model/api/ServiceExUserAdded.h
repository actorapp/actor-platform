//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/ServiceExUserAdded.java
//

#ifndef _ImActorModelApiServiceExUserAdded_H_
#define _ImActorModelApiServiceExUserAdded_H_

#include "J2ObjC_header.h"
#include "im/actor/model/api/ServiceEx.h"

@class BSBserValues;
@class BSBserWriter;

@interface ImActorModelApiServiceExUserAdded : ImActorModelApiServiceEx

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithInt:(jint)addedUid;

- (jint)getAddedUid;

- (jint)getHeader;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(ImActorModelApiServiceExUserAdded)

FOUNDATION_EXPORT void ImActorModelApiServiceExUserAdded_initWithInt_(ImActorModelApiServiceExUserAdded *self, jint addedUid);

FOUNDATION_EXPORT ImActorModelApiServiceExUserAdded *new_ImActorModelApiServiceExUserAdded_initWithInt_(jint addedUid) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void ImActorModelApiServiceExUserAdded_init(ImActorModelApiServiceExUserAdded *self);

FOUNDATION_EXPORT ImActorModelApiServiceExUserAdded *new_ImActorModelApiServiceExUserAdded_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(ImActorModelApiServiceExUserAdded)

#endif // _ImActorModelApiServiceExUserAdded_H_
