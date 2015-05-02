//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/params/RSAKeyGenerationParameters.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/params/RSAKeyGenerationParameters.java"

#include "J2ObjC_source.h"
#include "im/actor/model/crypto/bouncycastle/RandomProvider.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/math/BigInteger.h"
#include "org/bouncycastle/crypto/KeyGenerationParameters.h"
#include "org/bouncycastle/crypto/params/RSAKeyGenerationParameters.h"

@interface OrgBouncycastleCryptoParamsRSAKeyGenerationParameters () {
 @public
  JavaMathBigInteger *publicExponent_;
  jint certainty_;
}

@end

J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoParamsRSAKeyGenerationParameters, publicExponent_, JavaMathBigInteger *)


#line 8
@implementation OrgBouncycastleCryptoParamsRSAKeyGenerationParameters


#line 14
- (instancetype)initWithJavaMathBigInteger:(JavaMathBigInteger *)publicExponent
                      withBCRandomProvider:(id<BCRandomProvider>)random
                                   withInt:(jint)strength
                                   withInt:(jint)certainty {
  OrgBouncycastleCryptoParamsRSAKeyGenerationParameters_initWithJavaMathBigInteger_withBCRandomProvider_withInt_withInt_(self, publicExponent, random, strength, certainty);
  return self;
}


#line 39
- (JavaMathBigInteger *)getPublicExponent {
  
#line 41
  return publicExponent_;
}


#line 44
- (jint)getCertainty {
  
#line 46
  return certainty_;
}

@end


#line 14
void OrgBouncycastleCryptoParamsRSAKeyGenerationParameters_initWithJavaMathBigInteger_withBCRandomProvider_withInt_withInt_(OrgBouncycastleCryptoParamsRSAKeyGenerationParameters *self, JavaMathBigInteger *publicExponent, id<BCRandomProvider> random, jint strength, jint certainty) {
  (void) OrgBouncycastleCryptoKeyGenerationParameters_initWithBCRandomProvider_withInt_(self,
#line 20
  random, strength);
  
#line 22
  if (strength < 12) {
    
#line 24
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"key strength too small");
  }
  
#line 30
  if (![((JavaMathBigInteger *) nil_chk(publicExponent)) testBitWithInt:0]) {
    
#line 32
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"public exponent cannot be even");
  }
  
#line 35
  self->publicExponent_ = publicExponent;
  self->certainty_ = certainty;
}


#line 14
OrgBouncycastleCryptoParamsRSAKeyGenerationParameters *new_OrgBouncycastleCryptoParamsRSAKeyGenerationParameters_initWithJavaMathBigInteger_withBCRandomProvider_withInt_withInt_(JavaMathBigInteger *publicExponent, id<BCRandomProvider> random, jint strength, jint certainty) {
  OrgBouncycastleCryptoParamsRSAKeyGenerationParameters *self = [OrgBouncycastleCryptoParamsRSAKeyGenerationParameters alloc];
  OrgBouncycastleCryptoParamsRSAKeyGenerationParameters_initWithJavaMathBigInteger_withBCRandomProvider_withInt_withInt_(self, publicExponent, random, strength, certainty);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(OrgBouncycastleCryptoParamsRSAKeyGenerationParameters)
