//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core/src/main/java/im/actor/model/PhoneBookProvider.java
//

#ifndef _AMPhoneBookProvider_H_
#define _AMPhoneBookProvider_H_

#include "J2ObjC_header.h"

@protocol AMPhoneBookProvider_Callback;
@protocol JavaUtilList;

@protocol AMPhoneBookProvider < NSObject, JavaObject >

- (void)loadPhoneBookWithCallback:(id<AMPhoneBookProvider_Callback>)callback;

@end

J2OBJC_EMPTY_STATIC_INIT(AMPhoneBookProvider)

J2OBJC_TYPE_LITERAL_HEADER(AMPhoneBookProvider)

#define ImActorModelPhoneBookProvider AMPhoneBookProvider

@protocol AMPhoneBookProvider_Callback < NSObject, JavaObject >

- (void)onLoadedWithContacts:(id<JavaUtilList>)contacts;

@end

J2OBJC_EMPTY_STATIC_INIT(AMPhoneBookProvider_Callback)

J2OBJC_TYPE_LITERAL_HEADER(AMPhoneBookProvider_Callback)

#endif // _AMPhoneBookProvider_H_
