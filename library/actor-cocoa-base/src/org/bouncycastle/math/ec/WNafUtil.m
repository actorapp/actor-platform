//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/math/ec/WNafUtil.java
//


#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/org/bouncycastle/math/ec/WNafUtil.java"

#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/Math.h"
#include "java/lang/System.h"
#include "java/math/BigInteger.h"
#include "org/bouncycastle/math/ec/ECCurve.h"
#include "org/bouncycastle/math/ec/ECPoint.h"
#include "org/bouncycastle/math/ec/ECPointMap.h"
#include "org/bouncycastle/math/ec/PreCompInfo.h"
#include "org/bouncycastle/math/ec/WNafPreCompInfo.h"
#include "org/bouncycastle/math/ec/WNafUtil.h"

@interface OrgBouncycastleMathEcWNafUtil ()

+ (IOSByteArray *)trimWithByteArray:(IOSByteArray *)a
                            withInt:(jint)length;

+ (IOSIntArray *)trimWithIntArray:(IOSIntArray *)a
                          withInt:(jint)length;

+ (IOSObjectArray *)resizeTableWithOrgBouncycastleMathEcECPointArray:(IOSObjectArray *)a
                                                             withInt:(jint)length;

@end

static IOSIntArray *OrgBouncycastleMathEcWNafUtil_DEFAULT_WINDOW_SIZE_CUTOFFS_;
J2OBJC_STATIC_FIELD_GETTER(OrgBouncycastleMathEcWNafUtil, DEFAULT_WINDOW_SIZE_CUTOFFS_, IOSIntArray *)

static IOSByteArray *OrgBouncycastleMathEcWNafUtil_EMPTY_BYTES_;
J2OBJC_STATIC_FIELD_GETTER(OrgBouncycastleMathEcWNafUtil, EMPTY_BYTES_, IOSByteArray *)

static IOSIntArray *OrgBouncycastleMathEcWNafUtil_EMPTY_INTS_;
J2OBJC_STATIC_FIELD_GETTER(OrgBouncycastleMathEcWNafUtil, EMPTY_INTS_, IOSIntArray *)

__attribute__((unused)) static IOSByteArray *OrgBouncycastleMathEcWNafUtil_trimWithByteArray_withInt_(IOSByteArray *a, jint length);

__attribute__((unused)) static IOSIntArray *OrgBouncycastleMathEcWNafUtil_trimWithIntArray_withInt_(IOSIntArray *a, jint length);

__attribute__((unused)) static IOSObjectArray *OrgBouncycastleMathEcWNafUtil_resizeTableWithOrgBouncycastleMathEcECPointArray_withInt_(IOSObjectArray *a, jint length);

J2OBJC_INITIALIZED_DEFN(OrgBouncycastleMathEcWNafUtil)

NSString *OrgBouncycastleMathEcWNafUtil_PRECOMP_NAME_ = 
#line 7
@"bc_wnaf";


#line 5
@implementation OrgBouncycastleMathEcWNafUtil


#line 14
+ (IOSIntArray *)generateCompactNafWithJavaMathBigInteger:(JavaMathBigInteger *)k {
  return OrgBouncycastleMathEcWNafUtil_generateCompactNafWithJavaMathBigInteger_(k);
}


#line 57
+ (IOSIntArray *)generateCompactWindowNafWithInt:(jint)width
                          withJavaMathBigInteger:(JavaMathBigInteger *)k {
  return OrgBouncycastleMathEcWNafUtil_generateCompactWindowNafWithInt_withJavaMathBigInteger_(width, k);
}


#line 123
+ (IOSByteArray *)generateJSFWithJavaMathBigInteger:(JavaMathBigInteger *)g
                             withJavaMathBigInteger:(JavaMathBigInteger *)h {
  return OrgBouncycastleMathEcWNafUtil_generateJSFWithJavaMathBigInteger_withJavaMathBigInteger_(g, h);
}


#line 184
+ (IOSByteArray *)generateNafWithJavaMathBigInteger:(JavaMathBigInteger *)k {
  return OrgBouncycastleMathEcWNafUtil_generateNafWithJavaMathBigInteger_(k);
}


#line 224
+ (IOSByteArray *)generateWindowNafWithInt:(jint)width
                    withJavaMathBigInteger:(JavaMathBigInteger *)k {
  return OrgBouncycastleMathEcWNafUtil_generateWindowNafWithInt_withJavaMathBigInteger_(width, k);
}


#line 286
+ (jint)getNafWeightWithJavaMathBigInteger:(JavaMathBigInteger *)k {
  return OrgBouncycastleMathEcWNafUtil_getNafWeightWithJavaMathBigInteger_(k);
}


#line 299
+ (OrgBouncycastleMathEcWNafPreCompInfo *)getWNafPreCompInfoWithOrgBouncycastleMathEcECPoint:(OrgBouncycastleMathEcECPoint *)p {
  return OrgBouncycastleMathEcWNafUtil_getWNafPreCompInfoWithOrgBouncycastleMathEcECPoint_(p);
}


#line 304
+ (OrgBouncycastleMathEcWNafPreCompInfo *)getWNafPreCompInfoWithOrgBouncycastleMathEcPreCompInfo:(id<OrgBouncycastleMathEcPreCompInfo>)preCompInfo {
  return OrgBouncycastleMathEcWNafUtil_getWNafPreCompInfoWithOrgBouncycastleMathEcPreCompInfo_(preCompInfo);
}


#line 320
+ (jint)getWindowSizeWithInt:(jint)bits {
  return OrgBouncycastleMathEcWNafUtil_getWindowSizeWithInt_(bits);
}


#line 332
+ (jint)getWindowSizeWithInt:(jint)bits
                withIntArray:(IOSIntArray *)windowSizeCutoffs {
  return OrgBouncycastleMathEcWNafUtil_getWindowSizeWithInt_withIntArray_(bits, windowSizeCutoffs);
}


#line 345
+ (OrgBouncycastleMathEcECPoint *)mapPointWithPrecompWithOrgBouncycastleMathEcECPoint:(OrgBouncycastleMathEcECPoint *)p
                                                                              withInt:(jint)width
                                                                          withBoolean:(jboolean)includeNegated
                                                  withOrgBouncycastleMathEcECPointMap:(id<OrgBouncycastleMathEcECPointMap>)pointMap {
  return OrgBouncycastleMathEcWNafUtil_mapPointWithPrecompWithOrgBouncycastleMathEcECPoint_withInt_withBoolean_withOrgBouncycastleMathEcECPointMap_(p, width, includeNegated, pointMap);
}


#line 384
+ (OrgBouncycastleMathEcWNafPreCompInfo *)precomputeWithOrgBouncycastleMathEcECPoint:(OrgBouncycastleMathEcECPoint *)p
                                                                             withInt:(jint)width
                                                                         withBoolean:(jboolean)includeNegated {
  return OrgBouncycastleMathEcWNafUtil_precomputeWithOrgBouncycastleMathEcECPoint_withInt_withBoolean_(p, width, includeNegated);
}


#line 465
+ (IOSByteArray *)trimWithByteArray:(IOSByteArray *)a
                            withInt:(jint)length {
  return OrgBouncycastleMathEcWNafUtil_trimWithByteArray_withInt_(a, length);
}


#line 472
+ (IOSIntArray *)trimWithIntArray:(IOSIntArray *)a
                          withInt:(jint)length {
  return OrgBouncycastleMathEcWNafUtil_trimWithIntArray_withInt_(a, length);
}


#line 479
+ (IOSObjectArray *)resizeTableWithOrgBouncycastleMathEcECPointArray:(IOSObjectArray *)a
                                                             withInt:(jint)length {
  return OrgBouncycastleMathEcWNafUtil_resizeTableWithOrgBouncycastleMathEcECPointArray_withInt_(a, length);
}

- (instancetype)init {
  OrgBouncycastleMathEcWNafUtil_init(self);
  return self;
}

+ (void)initialize {
  if (self == [OrgBouncycastleMathEcWNafUtil class]) {
    OrgBouncycastleMathEcWNafUtil_DEFAULT_WINDOW_SIZE_CUTOFFS_ = [IOSIntArray newArrayWithInts:(jint[]){
#line 9
      13, 41, 121, 337, 897, 2305 } count:6];
      OrgBouncycastleMathEcWNafUtil_EMPTY_BYTES_ = [IOSByteArray newArrayWithLength:
#line 11
      0];
      OrgBouncycastleMathEcWNafUtil_EMPTY_INTS_ = [IOSIntArray newArrayWithLength:
#line 12
      0];
      J2OBJC_SET_INITIALIZED(OrgBouncycastleMathEcWNafUtil)
    }
  }

@end


#line 14
IOSIntArray *OrgBouncycastleMathEcWNafUtil_generateCompactNafWithJavaMathBigInteger_(JavaMathBigInteger *k) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  if ((URShift32([((JavaMathBigInteger *) nil_chk(k)) bitLength], 16)) != 0) {
    
#line 18
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"'k' must have bitlength < 2^16");
  }
  if ([k signum] == 0) {
    
#line 22
    return OrgBouncycastleMathEcWNafUtil_EMPTY_INTS_;
  }
  
#line 25
  JavaMathBigInteger *_3k = [((JavaMathBigInteger *) nil_chk([k shiftLeftWithInt:1])) addWithJavaMathBigInteger:k];
  
#line 27
  jint bits = [((JavaMathBigInteger *) nil_chk(_3k)) bitLength];
  IOSIntArray *naf = [IOSIntArray newArrayWithLength:RShift32(bits, 1)];
  
#line 30
  JavaMathBigInteger *diff = [_3k xor__WithJavaMathBigInteger:k];
  
#line 32
  jint highBit = bits - 1, length = 0, zeroes = 0;
  for (jint i = 1; i < highBit; ++i) {
    
#line 35
    if (![((JavaMathBigInteger *) nil_chk(diff)) testBitWithInt:i]) {
      
#line 37
      ++zeroes;
      continue;
    }
    
#line 41
    jint digit = [k testBitWithInt:i] ? -1 : 1;
    *IOSIntArray_GetRef(naf, length++) = (LShift32(digit, 16)) | zeroes;
    zeroes = 1;
    ++i;
  }
  
#line 47
  *IOSIntArray_GetRef(naf, length++) = (LShift32(1, 16)) | zeroes;
  
#line 49
  if (naf->size_ > length) {
    
#line 51
    naf = OrgBouncycastleMathEcWNafUtil_trimWithIntArray_withInt_(naf, length);
  }
  
#line 54
  return naf;
}


#line 57
IOSIntArray *OrgBouncycastleMathEcWNafUtil_generateCompactWindowNafWithInt_withJavaMathBigInteger_(jint width, JavaMathBigInteger *k) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  if (width == 2) {
    
#line 61
    return OrgBouncycastleMathEcWNafUtil_generateCompactNafWithJavaMathBigInteger_(k);
  }
  
#line 64
  if (width < 2 || width > 16) {
    
#line 66
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"'width' must be in the range [2, 16]");
  }
  if ((URShift32([((JavaMathBigInteger *) nil_chk(k)) bitLength], 16)) != 0) {
    
#line 70
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"'k' must have bitlength < 2^16");
  }
  if ([k signum] == 0) {
    
#line 74
    return OrgBouncycastleMathEcWNafUtil_EMPTY_INTS_;
  }
  
#line 77
  IOSIntArray *wnaf = [IOSIntArray newArrayWithLength:[k bitLength] / width + 1];
  
#line 80
  jint pow2 = LShift32(1, width);
  jint mask = pow2 - 1;
  jint sign = URShift32(pow2, 1);
  
#line 84
  jboolean carry = NO;
  jint length = 0, pos = 0;
  
#line 87
  while (pos <= [k bitLength]) {
    
#line 89
    if ([k testBitWithInt:pos] == carry) {
      
#line 91
      ++pos;
      continue;
    }
    
#line 95
    k = [k shiftRightWithInt:pos];
    
#line 97
    jint digit = [((JavaMathBigInteger *) nil_chk(k)) intValue] & mask;
    if (carry) {
      
#line 100
      ++digit;
    }
    
#line 103
    carry = ((digit & sign) != 0);
    if (carry) {
      
#line 106
      digit -= pow2;
    }
    
#line 109
    jint zeroes = length > 0 ? pos - 1 : pos;
    *IOSIntArray_GetRef(wnaf, length++) = (LShift32(digit, 16)) | zeroes;
    pos = width;
  }
  
#line 115
  if (wnaf->size_ > length) {
    
#line 117
    wnaf = OrgBouncycastleMathEcWNafUtil_trimWithIntArray_withInt_(wnaf, length);
  }
  
#line 120
  return wnaf;
}


#line 123
IOSByteArray *OrgBouncycastleMathEcWNafUtil_generateJSFWithJavaMathBigInteger_withJavaMathBigInteger_(JavaMathBigInteger *g, JavaMathBigInteger *h) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  jint digits = JavaLangMath_maxWithInt_withInt_([((JavaMathBigInteger *) nil_chk(g)) bitLength], [((JavaMathBigInteger *) nil_chk(h)) bitLength]) + 1;
  IOSByteArray *jsf = [IOSByteArray newArrayWithLength:digits];
  
#line 128
  JavaMathBigInteger *k0 = g, *k1 = h;
  jint j = 0, d0 = 0, d1 = 0;
  
#line 131
  jint offset = 0;
  while ((d0 | d1) != 0 || [k0 bitLength] > offset || [k1 bitLength] > offset) {
    
#line 134
    jint n0 = ((URShift32([k0 intValue], offset)) + d0) & 7, n1 = ((URShift32([k1 intValue], offset)) + d1) & 7;
    
#line 136
    jint u0 = n0 & 1;
    if (u0 != 0) {
      
#line 139
      u0 -= (n0 & 2);
      if ((n0 + u0) == 4 && (n1 & 3) == 2) {
        
#line 142
        u0 = -u0;
      }
    }
    
#line 146
    jint u1 = n1 & 1;
    if (u1 != 0) {
      
#line 149
      u1 -= (n1 & 2);
      if ((n1 + u1) == 4 && (n0 & 3) == 2) {
        
#line 152
        u1 = -u1;
      }
    }
    
#line 156
    if ((LShift32(d0, 1)) == 1 + u0) {
      
#line 158
      d0 ^= 1;
    }
    if ((LShift32(d1, 1)) == 1 + u1) {
      
#line 162
      d1 ^= 1;
    }
    
#line 165
    if (++offset == 30) {
      
#line 167
      offset = 0;
      k0 = [k0 shiftRightWithInt:30];
      k1 = [k1 shiftRightWithInt:30];
    }
    
#line 172
    *IOSByteArray_GetRef(jsf, j++) = (jbyte) ((LShift32(u0, 4)) | (u1 & (jint) 0xF));
  }
  
#line 176
  if (jsf->size_ > j) {
    
#line 178
    jsf = OrgBouncycastleMathEcWNafUtil_trimWithByteArray_withInt_(jsf, j);
  }
  
#line 181
  return jsf;
}


#line 184
IOSByteArray *OrgBouncycastleMathEcWNafUtil_generateNafWithJavaMathBigInteger_(JavaMathBigInteger *k) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  if ([((JavaMathBigInteger *) nil_chk(k)) signum] == 0) {
    
#line 188
    return OrgBouncycastleMathEcWNafUtil_EMPTY_BYTES_;
  }
  
#line 191
  JavaMathBigInteger *_3k = [((JavaMathBigInteger *) nil_chk([k shiftLeftWithInt:1])) addWithJavaMathBigInteger:k];
  
#line 193
  jint digits = [((JavaMathBigInteger *) nil_chk(_3k)) bitLength] - 1;
  IOSByteArray *naf = [IOSByteArray newArrayWithLength:digits];
  
#line 196
  JavaMathBigInteger *diff = [_3k xor__WithJavaMathBigInteger:k];
  
#line 198
  for (jint i = 1; i < digits; ++i) {
    
#line 200
    if ([((JavaMathBigInteger *) nil_chk(diff)) testBitWithInt:i]) {
      
#line 202
      *IOSByteArray_GetRef(naf, i - 1) = (jbyte) ([k testBitWithInt:i] ? -1 : 1);
      ++i;
    }
  }
  
#line 207
  *IOSByteArray_GetRef(naf, digits - 1) = 1;
  
#line 209
  return naf;
}


#line 224
IOSByteArray *OrgBouncycastleMathEcWNafUtil_generateWindowNafWithInt_withJavaMathBigInteger_(jint width, JavaMathBigInteger *k) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  if (width == 2) {
    
#line 228
    return OrgBouncycastleMathEcWNafUtil_generateNafWithJavaMathBigInteger_(k);
  }
  
#line 231
  if (width < 2 || width > 8) {
    
#line 233
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"'width' must be in the range [2, 8]");
  }
  if ([((JavaMathBigInteger *) nil_chk(k)) signum] == 0) {
    
#line 237
    return OrgBouncycastleMathEcWNafUtil_EMPTY_BYTES_;
  }
  
#line 240
  IOSByteArray *wnaf = [IOSByteArray newArrayWithLength:[k bitLength] + 1];
  
#line 243
  jint pow2 = LShift32(1, width);
  jint mask = pow2 - 1;
  jint sign = URShift32(pow2, 1);
  
#line 247
  jboolean carry = NO;
  jint length = 0, pos = 0;
  
#line 250
  while (pos <= [k bitLength]) {
    
#line 252
    if ([k testBitWithInt:pos] == carry) {
      
#line 254
      ++pos;
      continue;
    }
    
#line 258
    k = [k shiftRightWithInt:pos];
    
#line 260
    jint digit = [((JavaMathBigInteger *) nil_chk(k)) intValue] & mask;
    if (carry) {
      
#line 263
      ++digit;
    }
    
#line 266
    carry = ((digit & sign) != 0);
    if (carry) {
      
#line 269
      digit -= pow2;
    }
    
#line 272
    length += (length > 0) ? pos - 1 : pos;
    *IOSByteArray_GetRef(wnaf, length++) = (jbyte) digit;
    pos = width;
  }
  
#line 278
  if (wnaf->size_ > length) {
    
#line 280
    wnaf = OrgBouncycastleMathEcWNafUtil_trimWithByteArray_withInt_(wnaf, length);
  }
  
#line 283
  return wnaf;
}


#line 286
jint OrgBouncycastleMathEcWNafUtil_getNafWeightWithJavaMathBigInteger_(JavaMathBigInteger *k) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  if ([((JavaMathBigInteger *) nil_chk(k)) signum] == 0) {
    
#line 290
    return 0;
  }
  
#line 293
  JavaMathBigInteger *_3k = [((JavaMathBigInteger *) nil_chk([k shiftLeftWithInt:1])) addWithJavaMathBigInteger:k];
  JavaMathBigInteger *diff = [((JavaMathBigInteger *) nil_chk(_3k)) xor__WithJavaMathBigInteger:k];
  
#line 296
  return [((JavaMathBigInteger *) nil_chk(diff)) bitCount];
}


#line 299
OrgBouncycastleMathEcWNafPreCompInfo *OrgBouncycastleMathEcWNafUtil_getWNafPreCompInfoWithOrgBouncycastleMathEcECPoint_(OrgBouncycastleMathEcECPoint *p) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  return OrgBouncycastleMathEcWNafUtil_getWNafPreCompInfoWithOrgBouncycastleMathEcPreCompInfo_([((OrgBouncycastleMathEcECCurve *) nil_chk([((OrgBouncycastleMathEcECPoint *) nil_chk(p)) getCurve])) getPreCompInfoWithOrgBouncycastleMathEcECPoint:p withNSString:OrgBouncycastleMathEcWNafUtil_PRECOMP_NAME_]);
}

OrgBouncycastleMathEcWNafPreCompInfo *OrgBouncycastleMathEcWNafUtil_getWNafPreCompInfoWithOrgBouncycastleMathEcPreCompInfo_(id<OrgBouncycastleMathEcPreCompInfo> preCompInfo) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  if ((preCompInfo != nil) && ([preCompInfo isKindOfClass:[OrgBouncycastleMathEcWNafPreCompInfo class]])) {
    
#line 308
    return (OrgBouncycastleMathEcWNafPreCompInfo *) check_class_cast(preCompInfo, [OrgBouncycastleMathEcWNafPreCompInfo class]);
  }
  
#line 311
  return new_OrgBouncycastleMathEcWNafPreCompInfo_init();
}


#line 320
jint OrgBouncycastleMathEcWNafUtil_getWindowSizeWithInt_(jint bits) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  return OrgBouncycastleMathEcWNafUtil_getWindowSizeWithInt_withIntArray_(bits, OrgBouncycastleMathEcWNafUtil_DEFAULT_WINDOW_SIZE_CUTOFFS_);
}


#line 332
jint OrgBouncycastleMathEcWNafUtil_getWindowSizeWithInt_withIntArray_(jint bits, IOSIntArray *windowSizeCutoffs) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  jint w = 0;
  for (; w < ((IOSIntArray *) nil_chk(windowSizeCutoffs))->size_; ++w) {
    
#line 337
    if (bits < IOSIntArray_Get(windowSizeCutoffs, w)) {
      
#line 339
      break;
    }
  }
  return w + 2;
}


#line 345
OrgBouncycastleMathEcECPoint *OrgBouncycastleMathEcWNafUtil_mapPointWithPrecompWithOrgBouncycastleMathEcECPoint_withInt_withBoolean_withOrgBouncycastleMathEcECPointMap_(OrgBouncycastleMathEcECPoint *p, jint width, jboolean includeNegated, id<OrgBouncycastleMathEcECPointMap> pointMap) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  
#line 348
  OrgBouncycastleMathEcECCurve *c = [((OrgBouncycastleMathEcECPoint *) nil_chk(p)) getCurve];
  OrgBouncycastleMathEcWNafPreCompInfo *wnafPreCompP = OrgBouncycastleMathEcWNafUtil_precomputeWithOrgBouncycastleMathEcECPoint_withInt_withBoolean_(p, width, includeNegated);
  
#line 351
  OrgBouncycastleMathEcECPoint *q = [((id<OrgBouncycastleMathEcECPointMap>) nil_chk(pointMap)) mapWithOrgBouncycastleMathEcECPoint:p];
  OrgBouncycastleMathEcWNafPreCompInfo *wnafPreCompQ = OrgBouncycastleMathEcWNafUtil_getWNafPreCompInfoWithOrgBouncycastleMathEcPreCompInfo_([((OrgBouncycastleMathEcECCurve *) nil_chk(c)) getPreCompInfoWithOrgBouncycastleMathEcECPoint:q withNSString:OrgBouncycastleMathEcWNafUtil_PRECOMP_NAME_]);
  
#line 354
  OrgBouncycastleMathEcECPoint *twiceP = [((OrgBouncycastleMathEcWNafPreCompInfo *) nil_chk(wnafPreCompP)) getTwice];
  if (twiceP != nil) {
    
#line 357
    OrgBouncycastleMathEcECPoint *twiceQ = [pointMap mapWithOrgBouncycastleMathEcECPoint:twiceP];
    [((OrgBouncycastleMathEcWNafPreCompInfo *) nil_chk(wnafPreCompQ)) setTwiceWithOrgBouncycastleMathEcECPoint:twiceQ];
  }
  
#line 361
  IOSObjectArray *preCompP = [wnafPreCompP getPreComp];
  IOSObjectArray *preCompQ = [IOSObjectArray newArrayWithLength:((IOSObjectArray *) nil_chk(preCompP))->size_ type:OrgBouncycastleMathEcECPoint_class_()];
  for (jint i = 0; i < preCompP->size_; ++i) {
    
#line 365
    (void) IOSObjectArray_Set(preCompQ, i, [pointMap mapWithOrgBouncycastleMathEcECPoint:IOSObjectArray_Get(preCompP, i)]);
  }
  [((OrgBouncycastleMathEcWNafPreCompInfo *) nil_chk(wnafPreCompQ)) setPreCompWithOrgBouncycastleMathEcECPointArray:preCompQ];
  
#line 369
  if (includeNegated) {
    
#line 371
    IOSObjectArray *preCompNegQ = [IOSObjectArray newArrayWithLength:preCompQ->size_ type:OrgBouncycastleMathEcECPoint_class_()];
    for (jint i = 0; i < preCompNegQ->size_; ++i) {
      
#line 374
      (void) IOSObjectArray_Set(preCompNegQ, i, [((OrgBouncycastleMathEcECPoint *) nil_chk(IOSObjectArray_Get(preCompQ, i))) negate]);
    }
    [wnafPreCompQ setPreCompNegWithOrgBouncycastleMathEcECPointArray:preCompNegQ];
  }
  
#line 379
  [c setPreCompInfoWithOrgBouncycastleMathEcECPoint:q withNSString:OrgBouncycastleMathEcWNafUtil_PRECOMP_NAME_ withOrgBouncycastleMathEcPreCompInfo:wnafPreCompQ];
  
#line 381
  return q;
}


#line 384
OrgBouncycastleMathEcWNafPreCompInfo *OrgBouncycastleMathEcWNafUtil_precomputeWithOrgBouncycastleMathEcECPoint_withInt_withBoolean_(OrgBouncycastleMathEcECPoint *p, jint width, jboolean includeNegated) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  OrgBouncycastleMathEcECCurve *c = [((OrgBouncycastleMathEcECPoint *) nil_chk(p)) getCurve];
  OrgBouncycastleMathEcWNafPreCompInfo *wnafPreCompInfo = OrgBouncycastleMathEcWNafUtil_getWNafPreCompInfoWithOrgBouncycastleMathEcPreCompInfo_([((OrgBouncycastleMathEcECCurve *) nil_chk(c)) getPreCompInfoWithOrgBouncycastleMathEcECPoint:p withNSString:OrgBouncycastleMathEcWNafUtil_PRECOMP_NAME_]);
  
#line 389
  IOSObjectArray *preComp = [((OrgBouncycastleMathEcWNafPreCompInfo *) nil_chk(wnafPreCompInfo)) getPreComp];
  if (preComp == nil) {
    
#line 392
    preComp = [IOSObjectArray newArrayWithObjects:(id[]){ p } count:1 type:OrgBouncycastleMathEcECPoint_class_()];
  }
  
#line 395
  jint preCompLen = ((IOSObjectArray *) nil_chk(preComp))->size_;
  jint reqPreCompLen = LShift32(1, JavaLangMath_maxWithInt_withInt_(0, width - 2));
  
#line 398
  if (preCompLen < reqPreCompLen) {
    
#line 400
    preComp = OrgBouncycastleMathEcWNafUtil_resizeTableWithOrgBouncycastleMathEcECPointArray_withInt_(preComp, reqPreCompLen);
    if (reqPreCompLen == 2) {
      
#line 403
      (void) IOSObjectArray_Set(nil_chk(preComp), 1, [((OrgBouncycastleMathEcECPoint *) nil_chk(IOSObjectArray_Get(preComp, 0))) threeTimes]);
    }
    else {
      
#line 407
      OrgBouncycastleMathEcECPoint *twiceP = [wnafPreCompInfo getTwice];
      if (twiceP == nil) {
        
#line 410
        twiceP = [((OrgBouncycastleMathEcECPoint *) nil_chk(IOSObjectArray_Get(nil_chk(preComp), 0))) twice];
        [wnafPreCompInfo setTwiceWithOrgBouncycastleMathEcECPoint:twiceP];
      }
      
#line 414
      for (jint i = preCompLen; i < reqPreCompLen; i++) {
        
#line 420
        (void) IOSObjectArray_Set(nil_chk(preComp), i, [((OrgBouncycastleMathEcECPoint *) nil_chk(twiceP)) addWithOrgBouncycastleMathEcECPoint:IOSObjectArray_Get(preComp, i - 1)]);
      }
    }
    
#line 427
    [c normalizeAllWithOrgBouncycastleMathEcECPointArray:preComp];
  }
  
#line 430
  [wnafPreCompInfo setPreCompWithOrgBouncycastleMathEcECPointArray:preComp];
  
#line 432
  if (includeNegated) {
    
#line 434
    IOSObjectArray *preCompNeg = [wnafPreCompInfo getPreCompNeg];
    
#line 436
    jint pos;
    if (preCompNeg == nil) {
      
#line 439
      pos = 0;
      preCompNeg = [IOSObjectArray newArrayWithLength:reqPreCompLen type:OrgBouncycastleMathEcECPoint_class_()];
    }
    else {
      
#line 444
      pos = preCompNeg->size_;
      if (pos < reqPreCompLen) {
        
#line 447
        preCompNeg = OrgBouncycastleMathEcWNafUtil_resizeTableWithOrgBouncycastleMathEcECPointArray_withInt_(preCompNeg, reqPreCompLen);
      }
    }
    
#line 451
    while (pos < reqPreCompLen) {
      
#line 453
      (void) IOSObjectArray_Set(nil_chk(preCompNeg), pos, [((OrgBouncycastleMathEcECPoint *) nil_chk(IOSObjectArray_Get(nil_chk(preComp), pos))) negate]);
      ++pos;
    }
    
#line 457
    [wnafPreCompInfo setPreCompNegWithOrgBouncycastleMathEcECPointArray:preCompNeg];
  }
  
#line 460
  [c setPreCompInfoWithOrgBouncycastleMathEcECPoint:p withNSString:OrgBouncycastleMathEcWNafUtil_PRECOMP_NAME_ withOrgBouncycastleMathEcPreCompInfo:wnafPreCompInfo];
  
#line 462
  return wnafPreCompInfo;
}


#line 465
IOSByteArray *OrgBouncycastleMathEcWNafUtil_trimWithByteArray_withInt_(IOSByteArray *a, jint length) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  IOSByteArray *result = [IOSByteArray newArrayWithLength:length];
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(a, 0, result, 0, result->size_);
  return result;
}

IOSIntArray *OrgBouncycastleMathEcWNafUtil_trimWithIntArray_withInt_(IOSIntArray *a, jint length) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  IOSIntArray *result = [IOSIntArray newArrayWithLength:length];
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(a, 0, result, 0, result->size_);
  return result;
}

IOSObjectArray *OrgBouncycastleMathEcWNafUtil_resizeTableWithOrgBouncycastleMathEcECPointArray_withInt_(IOSObjectArray *a, jint length) {
  OrgBouncycastleMathEcWNafUtil_initialize();
  IOSObjectArray *result = [IOSObjectArray newArrayWithLength:length type:OrgBouncycastleMathEcECPoint_class_()];
  JavaLangSystem_arraycopyWithId_withInt_withId_withInt_withInt_(a, 0, result, 0, ((IOSObjectArray *) nil_chk(a))->size_);
  return result;
}

void OrgBouncycastleMathEcWNafUtil_init(OrgBouncycastleMathEcWNafUtil *self) {
  (void) NSObject_init(self);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(OrgBouncycastleMathEcWNafUtil)
