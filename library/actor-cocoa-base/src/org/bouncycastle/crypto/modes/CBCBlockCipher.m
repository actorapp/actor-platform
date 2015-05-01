//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/modes/CBCBlockCipher.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/modes/CBCBlockCipher.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/System.h"
#include "org/bouncycastle/crypto/BlockCipher.h"
#include "org/bouncycastle/crypto/CipherParameters.h"
#include "org/bouncycastle/crypto/DataLengthException.h"
#include "org/bouncycastle/crypto/modes/CBCBlockCipher.h"
#include "org/bouncycastle/crypto/params/ParametersWithIV.h"
#include "org/bouncycastle/util/Arrays.h"

@interface OrgBouncycastleCryptoModesCBCBlockCipher () {
 @public
  IOSByteArray *IV_;
  IOSByteArray *cbcV_;
  IOSByteArray *cbcNextV_;
  jint blockSize_;
  id<OrgBouncycastleCryptoBlockCipher> cipher_;
  jboolean encrypting_;
}

- (jint)encryptBlockWithByteArray:(IOSByteArray *)inArg
                          withInt:(jint)inOff
                    withByteArray:(IOSByteArray *)outArg
                          withInt:(jint)outOff;

- (jint)decryptBlockWithByteArray:(IOSByteArray *)inArg
                          withInt:(jint)inOff
                    withByteArray:(IOSByteArray *)outArg
                          withInt:(jint)outOff;

@end

J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoModesCBCBlockCipher, IV_, IOSByteArray *)
J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoModesCBCBlockCipher, cbcV_, IOSByteArray *)
J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoModesCBCBlockCipher, cbcNextV_, IOSByteArray *)
J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoModesCBCBlockCipher, cipher_, id<OrgBouncycastleCryptoBlockCipher>)

__attribute__((unused)) static jint OrgBouncycastleCryptoModesCBCBlockCipher_encryptBlockWithByteArray_withInt_withByteArray_withInt_(OrgBouncycastleCryptoModesCBCBlockCipher *self, IOSByteArray *inArg, jint inOff, IOSByteArray *outArg, jint outOff);

__attribute__((unused)) static jint OrgBouncycastleCryptoModesCBCBlockCipher_decryptBlockWithByteArray_withInt_withByteArray_withInt_(OrgBouncycastleCryptoModesCBCBlockCipher *self, IOSByteArray *inArg, jint inOff, IOSByteArray *outArg, jint outOff);


#line 12
@implementation OrgBouncycastleCryptoModesCBCBlockCipher


#line 28
- (instancetype)initWithOrgBouncycastleCryptoBlockCipher:(id<OrgBouncycastleCryptoBlockCipher>)cipher {
  OrgBouncycastleCryptoModesCBCBlockCipher_initWithOrgBouncycastleCryptoBlockCipher_(self, cipher);
  return self;
}


#line 44
- (id<OrgBouncycastleCryptoBlockCipher>)getUnderlyingCipher {
  
#line 46
  return cipher_;
}


#line 59
- (void)init__WithBoolean:(jboolean)encrypting
withOrgBouncycastleCryptoCipherParameters:(id<OrgBouncycastleCryptoCipherParameters>)params {
  
#line 64
  jboolean oldEncrypting = self->encrypting_;
  
#line 66
  self->encrypting_ = encrypting;
  
#line 68
  if ([params isKindOfClass:[OrgBouncycastleCryptoParamsParametersWithIV class]]) {
    
#line 70
    OrgBouncycastleCryptoParamsParametersWithIV *ivParam = (OrgBouncycastleCryptoParamsParametersWithIV *) check_class_cast(params, [OrgBouncycastleCryptoParamsParametersWithIV class]);
    IOSByteArray *iv = [((OrgBouncycastleCryptoParamsParametersWithIV *) nil_chk(ivParam)) getIV];
    
#line 73
    if (((IOSByteArray *) nil_chk(iv))->size_ != blockSize_) {
      
#line 75
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"initialisation vector must be the same length as block size");
    }
    
#line 78
    JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(iv, 0, IV_, 0, iv->size_);
    
#line 80
    [self reset];
    
#line 83
    if ([ivParam getParameters] != nil) {
      
#line 85
      [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(cipher_)) init__WithBoolean:encrypting withOrgBouncycastleCryptoCipherParameters:[ivParam getParameters]];
    }
    else if (oldEncrypting != encrypting) {
      
#line 89
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"cannot change encrypting state without providing key.");
    }
  }
  else {
    
#line 94
    [self reset];
    
#line 97
    if (params != nil) {
      
#line 99
      [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(cipher_)) init__WithBoolean:encrypting withOrgBouncycastleCryptoCipherParameters:params];
    }
    else if (oldEncrypting != encrypting) {
      
#line 103
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"cannot change encrypting state without providing key.");
    }
  }
}


#line 113
- (NSString *)getAlgorithmName {
  
#line 115
  return JreStrcat("$$", [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(cipher_)) getAlgorithmName], @"/CBC");
}


#line 123
- (jint)getBlockSize {
  
#line 125
  return [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(cipher_)) getBlockSize];
}


#line 141
- (jint)processBlockWithByteArray:(IOSByteArray *)inArg
                          withInt:(jint)inOff
                    withByteArray:(IOSByteArray *)outArg
                          withInt:(jint)outOff {
  
#line 148
  return (encrypting_) ? OrgBouncycastleCryptoModesCBCBlockCipher_encryptBlockWithByteArray_withInt_withByteArray_withInt_(self, inArg, inOff, outArg, outOff) : OrgBouncycastleCryptoModesCBCBlockCipher_decryptBlockWithByteArray_withInt_withByteArray_withInt_(self, inArg, inOff, outArg, outOff);
}


#line 155
- (void)reset {
  
#line 157
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(IV_, 0, cbcV_, 0, ((IOSByteArray *) nil_chk(IV_))->size_);
  OrgBouncycastleUtilArrays_fillWithByteArray_withByte_(cbcNextV_, (jbyte) 0);
  
#line 160
  [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(cipher_)) reset];
}


#line 175
- (jint)encryptBlockWithByteArray:(IOSByteArray *)inArg
                          withInt:(jint)inOff
                    withByteArray:(IOSByteArray *)outArg
                          withInt:(jint)outOff {
  return OrgBouncycastleCryptoModesCBCBlockCipher_encryptBlockWithByteArray_withInt_withByteArray_withInt_(self, inArg, inOff, outArg, outOff);
}


#line 218
- (jint)decryptBlockWithByteArray:(IOSByteArray *)inArg
                          withInt:(jint)inOff
                    withByteArray:(IOSByteArray *)outArg
                          withInt:(jint)outOff {
  return OrgBouncycastleCryptoModesCBCBlockCipher_decryptBlockWithByteArray_withInt_withByteArray_withInt_(self, inArg, inOff, outArg, outOff);
}

@end


#line 28
void OrgBouncycastleCryptoModesCBCBlockCipher_initWithOrgBouncycastleCryptoBlockCipher_(OrgBouncycastleCryptoModesCBCBlockCipher *self, id<OrgBouncycastleCryptoBlockCipher> cipher) {
  (void) NSObject_init(self);
  self->cipher_ =
#line 20
  nil;
  
#line 31
  self->cipher_ = cipher;
  self->blockSize_ = [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(cipher)) getBlockSize];
  
#line 34
  self->IV_ = [IOSByteArray newArrayWithLength:self->blockSize_];
  self->cbcV_ = [IOSByteArray newArrayWithLength:self->blockSize_];
  self->cbcNextV_ = [IOSByteArray newArrayWithLength:self->blockSize_];
}


#line 28
OrgBouncycastleCryptoModesCBCBlockCipher *new_OrgBouncycastleCryptoModesCBCBlockCipher_initWithOrgBouncycastleCryptoBlockCipher_(id<OrgBouncycastleCryptoBlockCipher> cipher) {
  OrgBouncycastleCryptoModesCBCBlockCipher *self = [OrgBouncycastleCryptoModesCBCBlockCipher alloc];
  OrgBouncycastleCryptoModesCBCBlockCipher_initWithOrgBouncycastleCryptoBlockCipher_(self, cipher);
  return self;
}


#line 175
jint OrgBouncycastleCryptoModesCBCBlockCipher_encryptBlockWithByteArray_withInt_withByteArray_withInt_(OrgBouncycastleCryptoModesCBCBlockCipher *self, IOSByteArray *inArg, jint inOff, IOSByteArray *outArg, jint outOff) {
  
#line 182
  if ((inOff + self->blockSize_) > ((IOSByteArray *) nil_chk(inArg))->size_) {
    
#line 184
    @throw new_OrgBouncycastleCryptoDataLengthException_initWithNSString_(@"input buffer too short");
  }
  
#line 191
  for (jint i = 0; i < self->blockSize_; i++) {
    
#line 193
    *IOSByteArray_GetRef(nil_chk(self->cbcV_), i) ^= IOSByteArray_Get(inArg, inOff + i);
  }
  
#line 196
  jint length = [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(self->cipher_)) processBlockWithByteArray:self->cbcV_ withInt:0 withByteArray:outArg withInt:outOff];
  
#line 201
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(outArg, outOff, self->cbcV_, 0, ((IOSByteArray *) nil_chk(self->cbcV_))->size_);
  
#line 203
  return length;
}


#line 218
jint OrgBouncycastleCryptoModesCBCBlockCipher_decryptBlockWithByteArray_withInt_withByteArray_withInt_(OrgBouncycastleCryptoModesCBCBlockCipher *self, IOSByteArray *inArg, jint inOff, IOSByteArray *outArg, jint outOff) {
  
#line 225
  if ((inOff + self->blockSize_) > ((IOSByteArray *) nil_chk(inArg))->size_) {
    
#line 227
    @throw new_OrgBouncycastleCryptoDataLengthException_initWithNSString_(@"input buffer too short");
  }
  
#line 230
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(inArg, inOff, self->cbcNextV_, 0, self->blockSize_);
  
#line 232
  jint length = [((id<OrgBouncycastleCryptoBlockCipher>) nil_chk(self->cipher_)) processBlockWithByteArray:inArg withInt:inOff withByteArray:outArg withInt:outOff];
  
#line 237
  for (jint i = 0; i < self->blockSize_; i++) {
    
#line 239
    *IOSByteArray_GetRef(nil_chk(outArg), outOff + i) ^= IOSByteArray_Get(nil_chk(self->cbcV_), i);
  }
  
#line 245
  IOSByteArray *tmp;
  
#line 247
  tmp = self->cbcV_;
  self->cbcV_ = self->cbcNextV_;
  self->cbcNextV_ = tmp;
  
#line 251
  return length;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(OrgBouncycastleCryptoModesCBCBlockCipher)
