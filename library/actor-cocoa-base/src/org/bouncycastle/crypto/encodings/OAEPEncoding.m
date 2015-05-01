//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/encodings/OAEPEncoding.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/crypto/encodings/OAEPEncoding.java"

#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/crypto/bouncycastle/RandomProvider.h"
#include "java/lang/System.h"
#include "org/bouncycastle/crypto/AsymmetricBlockCipher.h"
#include "org/bouncycastle/crypto/Digest.h"
#include "org/bouncycastle/crypto/InvalidCipherTextException.h"
#include "org/bouncycastle/crypto/digests/SHA1Digest.h"
#include "org/bouncycastle/crypto/encodings/OAEPEncoding.h"
#include "org/bouncycastle/crypto/params/ParametersWithRandom.h"

@interface OrgBouncycastleCryptoEncodingsOAEPEncoding () {
 @public
  IOSByteArray *defHash_;
  id<OrgBouncycastleCryptoDigest> mgf1Hash_;
  id<OrgBouncycastleCryptoAsymmetricBlockCipher> engine_;
  id<BCRandomProvider> random_;
  jboolean forEncryption_;
}

- (void)ItoOSPWithInt:(jint)i
        withByteArray:(IOSByteArray *)sp;

- (IOSByteArray *)maskGeneratorFunction1WithByteArray:(IOSByteArray *)Z
                                              withInt:(jint)zOff
                                              withInt:(jint)zLen
                                              withInt:(jint)length;

@end

J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoEncodingsOAEPEncoding, defHash_, IOSByteArray *)
J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoEncodingsOAEPEncoding, mgf1Hash_, id<OrgBouncycastleCryptoDigest>)
J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoEncodingsOAEPEncoding, engine_, id<OrgBouncycastleCryptoAsymmetricBlockCipher>)
J2OBJC_FIELD_SETTER(OrgBouncycastleCryptoEncodingsOAEPEncoding, random_, id<BCRandomProvider>)

__attribute__((unused)) static void OrgBouncycastleCryptoEncodingsOAEPEncoding_ItoOSPWithInt_withByteArray_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, jint i, IOSByteArray *sp);

__attribute__((unused)) static IOSByteArray *OrgBouncycastleCryptoEncodingsOAEPEncoding_maskGeneratorFunction1WithByteArray_withInt_withInt_withInt_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, IOSByteArray *Z, jint zOff, jint zLen, jint length);


#line 13
@implementation OrgBouncycastleCryptoEncodingsOAEPEncoding


#line 22
- (instancetype)initWithOrgBouncycastleCryptoAsymmetricBlockCipher:(id<OrgBouncycastleCryptoAsymmetricBlockCipher>)cipher {
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_(self, cipher);
  return self;
}

- (instancetype)initWithOrgBouncycastleCryptoAsymmetricBlockCipher:(id<OrgBouncycastleCryptoAsymmetricBlockCipher>)cipher
                                   withOrgBouncycastleCryptoDigest:(id<OrgBouncycastleCryptoDigest>)hash_ {
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_(self, cipher, hash_);
  return self;
}

- (instancetype)initWithOrgBouncycastleCryptoAsymmetricBlockCipher:(id<OrgBouncycastleCryptoAsymmetricBlockCipher>)cipher
                                   withOrgBouncycastleCryptoDigest:(id<OrgBouncycastleCryptoDigest>)hash_
                                                     withByteArray:(IOSByteArray *)encodingParams {
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withByteArray_(self, cipher, hash_, encodingParams);
  return self;
}

- (instancetype)initWithOrgBouncycastleCryptoAsymmetricBlockCipher:(id<OrgBouncycastleCryptoAsymmetricBlockCipher>)cipher
                                   withOrgBouncycastleCryptoDigest:(id<OrgBouncycastleCryptoDigest>)hash_
                                   withOrgBouncycastleCryptoDigest:(id<OrgBouncycastleCryptoDigest>)mgf1Hash
                                                     withByteArray:(IOSByteArray *)encodingParams {
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withOrgBouncycastleCryptoDigest_withByteArray_(self, cipher, hash_, mgf1Hash, encodingParams);
  return self;
}


#line 58
- (id<OrgBouncycastleCryptoAsymmetricBlockCipher>)getUnderlyingCipher {
  return engine_;
}

- (void)init__WithBoolean:(jboolean)forEncryption
withOrgBouncycastleCryptoParamsParametersWithRandom:(OrgBouncycastleCryptoParamsParametersWithRandom *)param {
  
#line 65
  self->random_ = [((OrgBouncycastleCryptoParamsParametersWithRandom *) nil_chk(param)) getRandom];
  
#line 67
  [((id<OrgBouncycastleCryptoAsymmetricBlockCipher>) nil_chk(engine_)) init__WithBoolean:forEncryption withOrgBouncycastleCryptoParamsParametersWithRandom:param];
  
#line 69
  self->forEncryption_ = forEncryption;
}


#line 72
- (jint)getInputBlockSize {
  jint baseBlockSize = [((id<OrgBouncycastleCryptoAsymmetricBlockCipher>) nil_chk(engine_)) getInputBlockSize];
  
#line 75
  if (forEncryption_) {
    return baseBlockSize - 1 - 2 * ((IOSByteArray *) nil_chk(defHash_))->size_;
  }
  else {
    
#line 78
    return baseBlockSize;
  }
}


#line 82
- (jint)getOutputBlockSize {
  jint baseBlockSize = [((id<OrgBouncycastleCryptoAsymmetricBlockCipher>) nil_chk(engine_)) getOutputBlockSize];
  
#line 85
  if (forEncryption_) {
    return baseBlockSize;
  }
  else {
    
#line 88
    return baseBlockSize - 1 - 2 * ((IOSByteArray *) nil_chk(defHash_))->size_;
  }
}


#line 92
- (IOSByteArray *)processBlockWithByteArray:(IOSByteArray *)inArg
                                    withInt:(jint)inOff
                                    withInt:(jint)inLen {
  
#line 97
  if (forEncryption_) {
    return [self encodeBlockWithByteArray:inArg withInt:inOff withInt:inLen];
  }
  else {
    
#line 100
    return [self decodeBlockWithByteArray:inArg withInt:inOff withInt:inLen];
  }
}


#line 104
- (IOSByteArray *)encodeBlockWithByteArray:(IOSByteArray *)inArg
                                   withInt:(jint)inOff
                                   withInt:(jint)inLen {
  
#line 109
  IOSByteArray *block = [IOSByteArray newArrayWithLength:[self getInputBlockSize] + 1 + 2 * ((IOSByteArray *) nil_chk(defHash_))->size_];
  
#line 114
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(inArg, inOff, block, block->size_ - inLen, inLen);
  
#line 119
  *IOSByteArray_GetRef(block, block->size_ - inLen - 1) = (jint) 0x01;
  
#line 128
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(defHash_, 0, block, defHash_->size_, defHash_->size_);
  
#line 133
  IOSByteArray *seed = [IOSByteArray newArrayWithLength:defHash_->size_];
  
#line 135
  [((id<BCRandomProvider>) nil_chk(random_)) nextBytesWithByteArray:seed];
  
#line 140
  IOSByteArray *mask = OrgBouncycastleCryptoEncodingsOAEPEncoding_maskGeneratorFunction1WithByteArray_withInt_withInt_withInt_(self, seed, 0, seed->size_, block->size_ - defHash_->size_);
  
#line 142
  for (jint i = defHash_->size_; i != block->size_; i++) {
    *IOSByteArray_GetRef(block, i) ^= IOSByteArray_Get(nil_chk(mask), i - defHash_->size_);
  }
  
#line 149
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(seed, 0, block, 0, defHash_->size_);
  
#line 154
  mask = OrgBouncycastleCryptoEncodingsOAEPEncoding_maskGeneratorFunction1WithByteArray_withInt_withInt_withInt_(self,
#line 155
  block, defHash_->size_, block->size_ - defHash_->size_, defHash_->size_);
  
#line 157
  for (jint i = 0; i != defHash_->size_; i++) {
    *IOSByteArray_GetRef(block, i) ^= IOSByteArray_Get(nil_chk(mask), i);
  }
  
#line 161
  return [((id<OrgBouncycastleCryptoAsymmetricBlockCipher>) nil_chk(engine_)) processBlockWithByteArray:block withInt:0 withInt:block->size_];
}


#line 168
- (IOSByteArray *)decodeBlockWithByteArray:(IOSByteArray *)inArg
                                   withInt:(jint)inOff
                                   withInt:(jint)inLen {
  
#line 173
  IOSByteArray *data = [((id<OrgBouncycastleCryptoAsymmetricBlockCipher>) nil_chk(engine_)) processBlockWithByteArray:inArg withInt:inOff withInt:inLen];
  IOSByteArray *block;
  
#line 181
  if (((IOSByteArray *) nil_chk(data))->size_ < [engine_ getOutputBlockSize]) {
    block = [IOSByteArray newArrayWithLength:[engine_ getOutputBlockSize]];
    
#line 184
    JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(data, 0, block, block->size_ - data->size_, data->size_);
  }
  else {
    
#line 186
    block = data;
  }
  
#line 189
  if (((IOSByteArray *) nil_chk(block))->size_ < (2 * ((IOSByteArray *) nil_chk(defHash_))->size_) + 1) {
    @throw new_OrgBouncycastleCryptoInvalidCipherTextException_initWithNSString_(@"data too short");
  }
  
#line 196
  IOSByteArray *mask = OrgBouncycastleCryptoEncodingsOAEPEncoding_maskGeneratorFunction1WithByteArray_withInt_withInt_withInt_(self,
#line 197
  block, defHash_->size_, block->size_ - defHash_->size_, defHash_->size_);
  
#line 199
  for (jint i = 0; i != defHash_->size_; i++) {
    *IOSByteArray_GetRef(block, i) ^= IOSByteArray_Get(nil_chk(mask), i);
  }
  
#line 206
  mask = OrgBouncycastleCryptoEncodingsOAEPEncoding_maskGeneratorFunction1WithByteArray_withInt_withInt_withInt_(self, block, 0, defHash_->size_, block->size_ - defHash_->size_);
  
#line 208
  for (jint i = defHash_->size_; i != block->size_; i++) {
    *IOSByteArray_GetRef(block, i) ^= IOSByteArray_Get(nil_chk(mask), i - defHash_->size_);
  }
  
#line 216
  jboolean defHashWrong = NO;
  
#line 218
  for (jint i = 0; i != defHash_->size_; i++) {
    if (IOSByteArray_Get(defHash_, i) != IOSByteArray_Get(block, defHash_->size_ + i)) {
      defHashWrong = YES;
    }
  }
  
#line 224
  if (defHashWrong) {
    @throw new_OrgBouncycastleCryptoInvalidCipherTextException_initWithNSString_(@"data hash wrong");
  }
  
#line 231
  jint start;
  
#line 233
  for (start = 2 * defHash_->size_; start != block->size_; start++) {
    if (IOSByteArray_Get(block, start) != 0) {
      break;
    }
  }
  
#line 239
  if (start >= (block->size_ - 1) || IOSByteArray_Get(block, start) != 1) {
    @throw new_OrgBouncycastleCryptoInvalidCipherTextException_initWithNSString_(JreStrcat("$I", @"data start wrong ", start));
  }
  
#line 243
  start++;
  
#line 248
  IOSByteArray *output = [IOSByteArray newArrayWithLength:block->size_ - start];
  
#line 250
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(block, start, output, 0, output->size_);
  
#line 252
  return output;
}


#line 258
- (void)ItoOSPWithInt:(jint)i
        withByteArray:(IOSByteArray *)sp {
  OrgBouncycastleCryptoEncodingsOAEPEncoding_ItoOSPWithInt_withByteArray_(self, i, sp);
}


#line 270
- (IOSByteArray *)maskGeneratorFunction1WithByteArray:(IOSByteArray *)Z
                                              withInt:(jint)zOff
                                              withInt:(jint)zLen
                                              withInt:(jint)length {
  return OrgBouncycastleCryptoEncodingsOAEPEncoding_maskGeneratorFunction1WithByteArray_withInt_withInt_withInt_(self, Z, zOff, zLen, length);
}

@end


#line 22
void OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher) {
  (void) OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withByteArray_(self,
#line 24
  cipher, new_OrgBouncycastleCryptoDigestsSHA1Digest_init(), nil);
}


#line 22
OrgBouncycastleCryptoEncodingsOAEPEncoding *new_OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_(id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher) {
  OrgBouncycastleCryptoEncodingsOAEPEncoding *self = [OrgBouncycastleCryptoEncodingsOAEPEncoding alloc];
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_(self, cipher);
  return self;
}


#line 27
void OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher, id<OrgBouncycastleCryptoDigest> hash_) {
  (void) OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withByteArray_(self,
#line 30
  cipher, hash_, nil);
}


#line 27
OrgBouncycastleCryptoEncodingsOAEPEncoding *new_OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_(id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher, id<OrgBouncycastleCryptoDigest> hash_) {
  OrgBouncycastleCryptoEncodingsOAEPEncoding *self = [OrgBouncycastleCryptoEncodingsOAEPEncoding alloc];
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_(self, cipher, hash_);
  return self;
}

void OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withByteArray_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher, id<OrgBouncycastleCryptoDigest> hash_, IOSByteArray *encodingParams) {
  (void) OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withOrgBouncycastleCryptoDigest_withByteArray_(self,
#line 37
  cipher, hash_, hash_, encodingParams);
}


#line 33
OrgBouncycastleCryptoEncodingsOAEPEncoding *new_OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withByteArray_(id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher, id<OrgBouncycastleCryptoDigest> hash_, IOSByteArray *encodingParams) {
  OrgBouncycastleCryptoEncodingsOAEPEncoding *self = [OrgBouncycastleCryptoEncodingsOAEPEncoding alloc];
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withByteArray_(self, cipher, hash_, encodingParams);
  return self;
}


#line 40
void OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withOrgBouncycastleCryptoDigest_withByteArray_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher, id<OrgBouncycastleCryptoDigest> hash_, id<OrgBouncycastleCryptoDigest> mgf1Hash, IOSByteArray *encodingParams) {
  (void) NSObject_init(self);
  
#line 45
  self->engine_ = cipher;
  self->mgf1Hash_ = mgf1Hash;
  self->defHash_ = [IOSByteArray newArrayWithLength:[((id<OrgBouncycastleCryptoDigest>) nil_chk(hash_)) getDigestSize]];
  
#line 49
  [hash_ reset];
  
#line 51
  if (encodingParams != nil) {
    [hash_ updateWithByteArray:encodingParams withInt:0 withInt:encodingParams->size_];
  }
  
#line 55
  [hash_ doFinalWithByteArray:self->defHash_ withInt:0];
}


#line 40
OrgBouncycastleCryptoEncodingsOAEPEncoding *new_OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withOrgBouncycastleCryptoDigest_withByteArray_(id<OrgBouncycastleCryptoAsymmetricBlockCipher> cipher, id<OrgBouncycastleCryptoDigest> hash_, id<OrgBouncycastleCryptoDigest> mgf1Hash, IOSByteArray *encodingParams) {
  OrgBouncycastleCryptoEncodingsOAEPEncoding *self = [OrgBouncycastleCryptoEncodingsOAEPEncoding alloc];
  OrgBouncycastleCryptoEncodingsOAEPEncoding_initWithOrgBouncycastleCryptoAsymmetricBlockCipher_withOrgBouncycastleCryptoDigest_withOrgBouncycastleCryptoDigest_withByteArray_(self, cipher, hash_, mgf1Hash, encodingParams);
  return self;
}


#line 258
void OrgBouncycastleCryptoEncodingsOAEPEncoding_ItoOSPWithInt_withByteArray_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, jint i, IOSByteArray *sp) {
  
#line 261
  *IOSByteArray_GetRef(nil_chk(sp), 0) = (jbyte) (URShift32(i, 24));
  *IOSByteArray_GetRef(sp, 1) = (jbyte) (URShift32(i, 16));
  *IOSByteArray_GetRef(sp, 2) = (jbyte) (URShift32(i, 8));
  *IOSByteArray_GetRef(sp, 3) = (jbyte) (URShift32(i, 0));
}


#line 270
IOSByteArray *OrgBouncycastleCryptoEncodingsOAEPEncoding_maskGeneratorFunction1WithByteArray_withInt_withInt_withInt_(OrgBouncycastleCryptoEncodingsOAEPEncoding *self, IOSByteArray *Z, jint zOff, jint zLen, jint length) {
  
#line 275
  IOSByteArray *mask = [IOSByteArray newArrayWithLength:length];
  IOSByteArray *hashBuf = [IOSByteArray newArrayWithLength:[((id<OrgBouncycastleCryptoDigest>) nil_chk(self->mgf1Hash_)) getDigestSize]];
  IOSByteArray *C = [IOSByteArray newArrayWithLength:4];
  jint counter = 0;
  
#line 280
  [self->mgf1Hash_ reset];
  
#line 282
  while (counter < (length / hashBuf->size_)) {
    OrgBouncycastleCryptoEncodingsOAEPEncoding_ItoOSPWithInt_withByteArray_(self, counter, C);
    
#line 285
    [self->mgf1Hash_ updateWithByteArray:Z withInt:zOff withInt:zLen];
    [self->mgf1Hash_ updateWithByteArray:C withInt:0 withInt:C->size_];
    [self->mgf1Hash_ doFinalWithByteArray:hashBuf withInt:0];
    
#line 289
    JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(hashBuf, 0, mask, counter * hashBuf->size_, hashBuf->size_);
    
#line 291
    counter++;
  }
  
#line 294
  if ((counter * hashBuf->size_) < length) {
    OrgBouncycastleCryptoEncodingsOAEPEncoding_ItoOSPWithInt_withByteArray_(self, counter, C);
    
#line 297
    [self->mgf1Hash_ updateWithByteArray:Z withInt:zOff withInt:zLen];
    [self->mgf1Hash_ updateWithByteArray:C withInt:0 withInt:C->size_];
    [self->mgf1Hash_ doFinalWithByteArray:hashBuf withInt:0];
    
#line 301
    JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(hashBuf, 0, mask, counter * hashBuf->size_, mask->size_ - (counter * hashBuf->size_));
  }
  
#line 304
  return mask;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(OrgBouncycastleCryptoEncodingsOAEPEncoding)
