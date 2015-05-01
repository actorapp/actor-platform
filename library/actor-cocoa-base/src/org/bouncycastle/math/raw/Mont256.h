//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/math/raw/Mont256.java
//

#ifndef _OrgBouncycastleMathRawMont256_H_
#define _OrgBouncycastleMathRawMont256_H_

#include "J2ObjC_header.h"

@class IOSIntArray;

@interface OrgBouncycastleMathRawMont256 : NSObject

#pragma mark Public

- (instancetype)init;

+ (jint)inverse32WithInt:(jint)x;

+ (void)multAddWithIntArray:(IOSIntArray *)x
               withIntArray:(IOSIntArray *)y
               withIntArray:(IOSIntArray *)z
               withIntArray:(IOSIntArray *)m
                    withInt:(jint)mInv32;

+ (void)multAddXFWithIntArray:(IOSIntArray *)x
                 withIntArray:(IOSIntArray *)y
                 withIntArray:(IOSIntArray *)z
                 withIntArray:(IOSIntArray *)m;

+ (void)reduceWithIntArray:(IOSIntArray *)z
              withIntArray:(IOSIntArray *)m
                   withInt:(jint)mInv32;

+ (void)reduceXFWithIntArray:(IOSIntArray *)z
                withIntArray:(IOSIntArray *)m;

@end

J2OBJC_EMPTY_STATIC_INIT(OrgBouncycastleMathRawMont256)

FOUNDATION_EXPORT jint OrgBouncycastleMathRawMont256_inverse32WithInt_(jint x);

FOUNDATION_EXPORT void OrgBouncycastleMathRawMont256_multAddWithIntArray_withIntArray_withIntArray_withIntArray_withInt_(IOSIntArray *x, IOSIntArray *y, IOSIntArray *z, IOSIntArray *m, jint mInv32);

FOUNDATION_EXPORT void OrgBouncycastleMathRawMont256_multAddXFWithIntArray_withIntArray_withIntArray_withIntArray_(IOSIntArray *x, IOSIntArray *y, IOSIntArray *z, IOSIntArray *m);

FOUNDATION_EXPORT void OrgBouncycastleMathRawMont256_reduceWithIntArray_withIntArray_withInt_(IOSIntArray *z, IOSIntArray *m, jint mInv32);

FOUNDATION_EXPORT void OrgBouncycastleMathRawMont256_reduceXFWithIntArray_withIntArray_(IOSIntArray *z, IOSIntArray *m);

FOUNDATION_EXPORT void OrgBouncycastleMathRawMont256_init(OrgBouncycastleMathRawMont256 *self);

J2OBJC_TYPE_LITERAL_HEADER(OrgBouncycastleMathRawMont256)

#endif // _OrgBouncycastleMathRawMont256_H_
