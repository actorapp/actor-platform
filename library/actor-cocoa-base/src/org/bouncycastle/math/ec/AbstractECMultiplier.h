//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/math/ec/AbstractECMultiplier.java
//

#ifndef _OrgBouncycastleMathEcAbstractECMultiplier_H_
#define _OrgBouncycastleMathEcAbstractECMultiplier_H_

#include "J2ObjC_header.h"
#include "org/bouncycastle/math/ec/ECMultiplier.h"

@class JavaMathBigInteger;
@class OrgBouncycastleMathEcECPoint;

@interface OrgBouncycastleMathEcAbstractECMultiplier : NSObject < OrgBouncycastleMathEcECMultiplier >

#pragma mark Public

- (instancetype)init;

- (OrgBouncycastleMathEcECPoint *)multiplyWithOrgBouncycastleMathEcECPoint:(OrgBouncycastleMathEcECPoint *)p
                                                    withJavaMathBigInteger:(JavaMathBigInteger *)k;

#pragma mark Protected

- (OrgBouncycastleMathEcECPoint *)multiplyPositiveWithOrgBouncycastleMathEcECPoint:(OrgBouncycastleMathEcECPoint *)p
                                                            withJavaMathBigInteger:(JavaMathBigInteger *)k;

@end

J2OBJC_EMPTY_STATIC_INIT(OrgBouncycastleMathEcAbstractECMultiplier)

FOUNDATION_EXPORT void OrgBouncycastleMathEcAbstractECMultiplier_init(OrgBouncycastleMathEcAbstractECMultiplier *self);

J2OBJC_TYPE_LITERAL_HEADER(OrgBouncycastleMathEcAbstractECMultiplier)

#endif // _OrgBouncycastleMathEcAbstractECMultiplier_H_
