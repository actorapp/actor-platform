//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/params/TweakableBlockCipherParameters.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/params/TweakableBlockCipherParameters.java"

#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "org/bouncycastle/crypto/params/KeyParameter.h"
#include "org/bouncycastle/crypto/params/TweakableBlockCipherParameters.h"
#include "org/bouncycastle/util/Arrays.h"

@interface OrgBouncycastleCryptoParamsTweakableBlockCipherParameters () {
 @public
  IOSByteArray *tweak_;
  OrgBouncycastleCryptoParamsKeyParameter *key_;
}

@end

J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoParamsTweakableBlockCipherParameters, tweak_, IOSByteArray *)
J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoParamsTweakableBlockCipherParameters, key_, OrgBouncycastleCryptoParamsKeyParameter *)


#line 9
@implementation OrgBouncycastleCryptoParamsTweakableBlockCipherParameters


#line 15
- (instancetype)initWithOrgBouncycastleCryptoParamsKeyParameter:(OrgBouncycastleCryptoParamsKeyParameter *)key
                                                  withByteArray:(IOSByteArray *)tweak {
  OrgBouncycastleCryptoParamsTweakableBlockCipherParameters_initWithOrgBouncycastleCryptoParamsKeyParameter_withByteArray_(self, key, tweak);
  return self;
}


#line 26
- (OrgBouncycastleCryptoParamsKeyParameter *)getKey {
  
#line 28
  return key_;
}


#line 36
- (IOSByteArray *)getTweak {
  
#line 38
  return tweak_;
}

@end


#line 15
void OrgBouncycastleCryptoParamsTweakableBlockCipherParameters_initWithOrgBouncycastleCryptoParamsKeyParameter_withByteArray_(OrgBouncycastleCryptoParamsTweakableBlockCipherParameters *self, OrgBouncycastleCryptoParamsKeyParameter *key, IOSByteArray *tweak) {
  (void) NSObject_init(self);
  self->key_ = key;
  self->tweak_ = OrgBouncycastleUtilArrays_cloneWithByteArray_(tweak);
}


#line 15
OrgBouncycastleCryptoParamsTweakableBlockCipherParameters *new_OrgBouncycastleCryptoParamsTweakableBlockCipherParameters_initWithOrgBouncycastleCryptoParamsKeyParameter_withByteArray_(OrgBouncycastleCryptoParamsKeyParameter *key, IOSByteArray *tweak) {
  OrgBouncycastleCryptoParamsTweakableBlockCipherParameters *self = [OrgBouncycastleCryptoParamsTweakableBlockCipherParameters alloc];
  OrgBouncycastleCryptoParamsTweakableBlockCipherParameters_initWithOrgBouncycastleCryptoParamsKeyParameter_withByteArray_(self, key, tweak);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(OrgBouncycastleCryptoParamsTweakableBlockCipherParameters)
