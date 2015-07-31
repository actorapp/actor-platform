//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core-crypto/src/main/java/org/bouncycastle/crypto/paddings/BlockCipherPadding.java
//

#ifndef _OrgBouncycastleCryptoPaddingsBlockCipherPadding_H_
#define _OrgBouncycastleCryptoPaddingsBlockCipherPadding_H_

#include "J2ObjC_header.h"

@class IOSByteArray;
@protocol BCRandomProvider;

@protocol OrgBouncycastleCryptoPaddingsBlockCipherPadding < NSObject, JavaObject >

- (void)init__WithBCRandomProvider:(id<BCRandomProvider>)random OBJC_METHOD_FAMILY_NONE;

- (NSString *)getPaddingName;

- (jint)addPaddingWithByteArray:(IOSByteArray *)inArg
                        withInt:(jint)inOff;

- (jint)padCountWithByteArray:(IOSByteArray *)inArg;

@end

J2OBJC_EMPTY_STATIC_INIT(OrgBouncycastleCryptoPaddingsBlockCipherPadding)

J2OBJC_TYPE_LITERAL_HEADER(OrgBouncycastleCryptoPaddingsBlockCipherPadding)

#endif // _OrgBouncycastleCryptoPaddingsBlockCipherPadding_H_
