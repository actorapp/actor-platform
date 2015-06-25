//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/api/ServiceExChangedTitle.java
//

#ifndef _APServiceExChangedTitle_H_
#define _APServiceExChangedTitle_H_

#include "J2ObjC_header.h"
#include "im/actor/model/api/ServiceEx.h"

@class BSBserValues;
@class BSBserWriter;

@interface APServiceExChangedTitle : APServiceEx

#pragma mark Public

- (instancetype)init;

- (instancetype)initWithNSString:(NSString *)title;

- (jint)getHeader;

- (NSString *)getTitle;

- (void)parseWithBSBserValues:(BSBserValues *)values;

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer;

- (NSString *)description;

@end

J2OBJC_EMPTY_STATIC_INIT(APServiceExChangedTitle)

FOUNDATION_EXPORT void APServiceExChangedTitle_initWithNSString_(APServiceExChangedTitle *self, NSString *title);

FOUNDATION_EXPORT APServiceExChangedTitle *new_APServiceExChangedTitle_initWithNSString_(NSString *title) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void APServiceExChangedTitle_init(APServiceExChangedTitle *self);

FOUNDATION_EXPORT APServiceExChangedTitle *new_APServiceExChangedTitle_init() NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(APServiceExChangedTitle)

typedef APServiceExChangedTitle ImActorModelApiServiceExChangedTitle;

#endif // _APServiceExChangedTitle_H_
