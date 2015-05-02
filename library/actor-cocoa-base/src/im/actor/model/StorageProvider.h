//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/StorageProvider.java
//

#ifndef _AMStorageProvider_H_
#define _AMStorageProvider_H_

#include "J2ObjC_header.h"

@class AMPeer;
@protocol DKKeyValueStorage;
@protocol DKListEngine;
@protocol DKListStorage;
@protocol DKPreferencesStorage;

@protocol AMStorageProvider < NSObject, JavaObject >

- (id<DKPreferencesStorage>)createPreferencesStorage;

- (id<DKKeyValueStorage>)createKeyValueWithName:(NSString *)name;

- (id<DKListStorage>)createListWithName:(NSString *)name;

- (id<DKListEngine>)createSearchListWithDKListStorage:(id<DKListStorage>)storage;

- (id<DKListEngine>)createContactsListWithDKListStorage:(id<DKListStorage>)storage;

- (id<DKListEngine>)createDialogsListWithDKListStorage:(id<DKListStorage>)storage;

- (id<DKListEngine>)createMessagesListWithAMPeer:(AMPeer *)peer
                               withDKListStorage:(id<DKListStorage>)storage;

@end

J2OBJC_EMPTY_STATIC_INIT(AMStorageProvider)

J2OBJC_TYPE_LITERAL_HEADER(AMStorageProvider)

#define ImActorModelStorageProvider AMStorageProvider

#endif // _AMStorageProvider_H_
