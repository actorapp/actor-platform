//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/ApiConfiguration.java
//

#ifndef _AMApiConfiguration_H_
#define _AMApiConfiguration_H_

#include "J2ObjC_header.h"

@interface AMApiConfiguration : NSObject

#pragma mark Public

- (instancetype)initWithNSString:(NSString *)appTitle
                         withInt:(jint)appId
                    withNSString:(NSString *)appKey
                    withNSString:(NSString *)deviceTitle
                    withNSString:(NSString *)deviceString;

- (jint)getAppId;

- (NSString *)getAppKey;

- (NSString *)getAppTitle;

- (NSString *)getDeviceString;

- (NSString *)getDeviceTitle;

@end

J2OBJC_EMPTY_STATIC_INIT(AMApiConfiguration)

FOUNDATION_EXPORT void AMApiConfiguration_initWithNSString_withInt_withNSString_withNSString_withNSString_(AMApiConfiguration *self, NSString *appTitle, jint appId, NSString *appKey, NSString *deviceTitle, NSString *deviceString);

FOUNDATION_EXPORT AMApiConfiguration *new_AMApiConfiguration_initWithNSString_withInt_withNSString_withNSString_withNSString_(NSString *appTitle, jint appId, NSString *appKey, NSString *deviceTitle, NSString *deviceString) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(AMApiConfiguration)

typedef AMApiConfiguration ImActorModelApiConfiguration;

#endif // _AMApiConfiguration_H_
