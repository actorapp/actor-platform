//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core-crypto/src/main/java/org/bouncycastle/crypto/AsymmetricCipherKeyPairGenerator.java
//

#ifndef _OrgBouncycastleCryptoAsymmetricCipherKeyPairGenerator_H_
#define _OrgBouncycastleCryptoAsymmetricCipherKeyPairGenerator_H_

#include "J2ObjC_header.h"

@class OrgBouncycastleCryptoAsymmetricCipherKeyPair;
@class OrgBouncycastleCryptoKeyGenerationParameters;

@protocol OrgBouncycastleCryptoAsymmetricCipherKeyPairGenerator < NSObject, JavaObject >

- (void)init__WithOrgBouncycastleCryptoKeyGenerationParameters:(OrgBouncycastleCryptoKeyGenerationParameters *)param OBJC_METHOD_FAMILY_NONE;

- (OrgBouncycastleCryptoAsymmetricCipherKeyPair *)generateKeyPair;

@end

J2OBJC_EMPTY_STATIC_INIT(OrgBouncycastleCryptoAsymmetricCipherKeyPairGenerator)

J2OBJC_TYPE_LITERAL_HEADER(OrgBouncycastleCryptoAsymmetricCipherKeyPairGenerator)

#endif // _OrgBouncycastleCryptoAsymmetricCipherKeyPairGenerator_H_
