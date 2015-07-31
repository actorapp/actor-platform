//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core-crypto/src/main/java/org/bouncycastle/crypto/AsymmetricCipherKeyPair.java
//

#ifndef _OrgBouncycastleCryptoAsymmetricCipherKeyPair_H_
#define _OrgBouncycastleCryptoAsymmetricCipherKeyPair_H_

#include "J2ObjC_header.h"

@class OrgBouncycastleCryptoParamsAsymmetricKeyParameter;
@protocol OrgBouncycastleCryptoCipherParameters;

@interface OrgBouncycastleCryptoAsymmetricCipherKeyPair : NSObject

#pragma mark Public

- (instancetype)initWithOrgBouncycastleCryptoParamsAsymmetricKeyParameter:(OrgBouncycastleCryptoParamsAsymmetricKeyParameter *)publicParam
                    withOrgBouncycastleCryptoParamsAsymmetricKeyParameter:(OrgBouncycastleCryptoParamsAsymmetricKeyParameter *)privateParam;

- (instancetype)initWithOrgBouncycastleCryptoCipherParameters:(id<OrgBouncycastleCryptoCipherParameters>)publicParam
                    withOrgBouncycastleCryptoCipherParameters:(id<OrgBouncycastleCryptoCipherParameters>)privateParam;

- (OrgBouncycastleCryptoParamsAsymmetricKeyParameter *)getPrivate;

- (OrgBouncycastleCryptoParamsAsymmetricKeyParameter *)getPublic;

@end

J2OBJC_EMPTY_STATIC_INIT(OrgBouncycastleCryptoAsymmetricCipherKeyPair)

FOUNDATION_EXPORT void OrgBouncycastleCryptoAsymmetricCipherKeyPair_initWithOrgBouncycastleCryptoParamsAsymmetricKeyParameter_withOrgBouncycastleCryptoParamsAsymmetricKeyParameter_(OrgBouncycastleCryptoAsymmetricCipherKeyPair *self, OrgBouncycastleCryptoParamsAsymmetricKeyParameter *publicParam, OrgBouncycastleCryptoParamsAsymmetricKeyParameter *privateParam);

FOUNDATION_EXPORT OrgBouncycastleCryptoAsymmetricCipherKeyPair *new_OrgBouncycastleCryptoAsymmetricCipherKeyPair_initWithOrgBouncycastleCryptoParamsAsymmetricKeyParameter_withOrgBouncycastleCryptoParamsAsymmetricKeyParameter_(OrgBouncycastleCryptoParamsAsymmetricKeyParameter *publicParam, OrgBouncycastleCryptoParamsAsymmetricKeyParameter *privateParam) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT void OrgBouncycastleCryptoAsymmetricCipherKeyPair_initWithOrgBouncycastleCryptoCipherParameters_withOrgBouncycastleCryptoCipherParameters_(OrgBouncycastleCryptoAsymmetricCipherKeyPair *self, id<OrgBouncycastleCryptoCipherParameters> publicParam, id<OrgBouncycastleCryptoCipherParameters> privateParam);

FOUNDATION_EXPORT OrgBouncycastleCryptoAsymmetricCipherKeyPair *new_OrgBouncycastleCryptoAsymmetricCipherKeyPair_initWithOrgBouncycastleCryptoCipherParameters_withOrgBouncycastleCryptoCipherParameters_(id<OrgBouncycastleCryptoCipherParameters> publicParam, id<OrgBouncycastleCryptoCipherParameters> privateParam) NS_RETURNS_RETAINED;

J2OBJC_TYPE_LITERAL_HEADER(OrgBouncycastleCryptoAsymmetricCipherKeyPair)

#endif // _OrgBouncycastleCryptoAsymmetricCipherKeyPair_H_
