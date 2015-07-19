//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core-crypto/src/main/java/org/bouncycastle/math/ec/SimpleBigDecimal.java
//


#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/StringBuffer.h"
#include "java/math/BigInteger.h"
#include "org/bouncycastle/math/ec/ECConstants.h"
#include "org/bouncycastle/math/ec/SimpleBigDecimal.h"

#define OrgBouncycastleMathEcSimpleBigDecimal_serialVersionUID 1LL

@interface OrgBouncycastleMathEcSimpleBigDecimal () {
 @public
  JavaMathBigInteger *bigInt_;
  jint scale__;
}

- (void)checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal:(OrgBouncycastleMathEcSimpleBigDecimal *)b;

@end

J2OBJC_FIELD_SETTER(OrgBouncycastleMathEcSimpleBigDecimal, bigInt_, JavaMathBigInteger *)

J2OBJC_STATIC_FIELD_GETTER(OrgBouncycastleMathEcSimpleBigDecimal, serialVersionUID, jlong)

__attribute__((unused)) static void OrgBouncycastleMathEcSimpleBigDecimal_checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal_(OrgBouncycastleMathEcSimpleBigDecimal *self, OrgBouncycastleMathEcSimpleBigDecimal *b);

@implementation OrgBouncycastleMathEcSimpleBigDecimal

+ (OrgBouncycastleMathEcSimpleBigDecimal *)getInstanceWithJavaMathBigInteger:(JavaMathBigInteger *)value
                                                                     withInt:(jint)scale_ {
  return OrgBouncycastleMathEcSimpleBigDecimal_getInstanceWithJavaMathBigInteger_withInt_(value, scale_);
}

- (instancetype)initWithJavaMathBigInteger:(JavaMathBigInteger *)bigInt
                                   withInt:(jint)scale_ {
  OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_(self, bigInt, scale_);
  return self;
}

- (void)checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal:(OrgBouncycastleMathEcSimpleBigDecimal *)b {
  OrgBouncycastleMathEcSimpleBigDecimal_checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal_(self, b);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)adjustScaleWithInt:(jint)newScale {
  if (newScale < 0) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"scale may not be negative");
  }
  if (newScale == scale__) {
    return self;
  }
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) shiftLeftWithInt:newScale - scale__], newScale);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)addWithOrgBouncycastleMathEcSimpleBigDecimal:(OrgBouncycastleMathEcSimpleBigDecimal *)b {
  OrgBouncycastleMathEcSimpleBigDecimal_checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal_(self, b);
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) addWithJavaMathBigInteger:((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk(b))->bigInt_], scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)addWithJavaMathBigInteger:(JavaMathBigInteger *)b {
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) addWithJavaMathBigInteger:[((JavaMathBigInteger *) nil_chk(b)) shiftLeftWithInt:scale__]], scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)negate {
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) negate], scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)subtractWithOrgBouncycastleMathEcSimpleBigDecimal:(OrgBouncycastleMathEcSimpleBigDecimal *)b {
  return [self addWithOrgBouncycastleMathEcSimpleBigDecimal:[((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk(b)) negate]];
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)subtractWithJavaMathBigInteger:(JavaMathBigInteger *)b {
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) subtractWithJavaMathBigInteger:[((JavaMathBigInteger *) nil_chk(b)) shiftLeftWithInt:scale__]], scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)multiplyWithOrgBouncycastleMathEcSimpleBigDecimal:(OrgBouncycastleMathEcSimpleBigDecimal *)b {
  OrgBouncycastleMathEcSimpleBigDecimal_checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal_(self, b);
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) multiplyWithJavaMathBigInteger:((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk(b))->bigInt_], scale__ + scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)multiplyWithJavaMathBigInteger:(JavaMathBigInteger *)b {
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) multiplyWithJavaMathBigInteger:b], scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)divideWithOrgBouncycastleMathEcSimpleBigDecimal:(OrgBouncycastleMathEcSimpleBigDecimal *)b {
  OrgBouncycastleMathEcSimpleBigDecimal_checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal_(self, b);
  JavaMathBigInteger *dividend = [((JavaMathBigInteger *) nil_chk(bigInt_)) shiftLeftWithInt:scale__];
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(dividend)) divideWithJavaMathBigInteger:((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk(b))->bigInt_], scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)divideWithJavaMathBigInteger:(JavaMathBigInteger *)b {
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) divideWithJavaMathBigInteger:b], scale__);
}

- (OrgBouncycastleMathEcSimpleBigDecimal *)shiftLeftWithInt:(jint)n {
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(bigInt_)) shiftLeftWithInt:n], scale__);
}

- (jint)compareToWithOrgBouncycastleMathEcSimpleBigDecimal:(OrgBouncycastleMathEcSimpleBigDecimal *)val {
  OrgBouncycastleMathEcSimpleBigDecimal_checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal_(self, val);
  return [((JavaMathBigInteger *) nil_chk(bigInt_)) compareToWithId:((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk(val))->bigInt_];
}

- (jint)compareToWithJavaMathBigInteger:(JavaMathBigInteger *)val {
  return [((JavaMathBigInteger *) nil_chk(bigInt_)) compareToWithId:[((JavaMathBigInteger *) nil_chk(val)) shiftLeftWithInt:scale__]];
}

- (JavaMathBigInteger *)floor {
  return [((JavaMathBigInteger *) nil_chk(bigInt_)) shiftRightWithInt:scale__];
}

- (JavaMathBigInteger *)round {
  OrgBouncycastleMathEcSimpleBigDecimal *oneHalf = new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_(OrgBouncycastleMathEcECConstants_get_ONE_(), 1);
  return [((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk([self addWithOrgBouncycastleMathEcSimpleBigDecimal:[oneHalf adjustScaleWithInt:scale__]])) floor];
}

- (jint)intValue {
  return [((JavaMathBigInteger *) nil_chk([self floor])) intValue];
}

- (jlong)longValue {
  return [((JavaMathBigInteger *) nil_chk([self floor])) longLongValue];
}

- (jint)getScale {
  return scale__;
}

- (NSString *)description {
  if (scale__ == 0) {
    return [((JavaMathBigInteger *) nil_chk(bigInt_)) description];
  }
  JavaMathBigInteger *floorBigInt = [self floor];
  JavaMathBigInteger *fract = [((JavaMathBigInteger *) nil_chk(bigInt_)) subtractWithJavaMathBigInteger:[((JavaMathBigInteger *) nil_chk(floorBigInt)) shiftLeftWithInt:scale__]];
  if ([bigInt_ signum] == -1) {
    fract = [((JavaMathBigInteger *) nil_chk([((JavaMathBigInteger *) nil_chk(OrgBouncycastleMathEcECConstants_get_ONE_())) shiftLeftWithInt:scale__])) subtractWithJavaMathBigInteger:fract];
  }
  if (([floorBigInt signum] == -1) && (!([((JavaMathBigInteger *) nil_chk(fract)) isEqual:OrgBouncycastleMathEcECConstants_get_ZERO_()]))) {
    floorBigInt = [floorBigInt addWithJavaMathBigInteger:OrgBouncycastleMathEcECConstants_get_ONE_()];
  }
  NSString *leftOfPoint = [((JavaMathBigInteger *) nil_chk(floorBigInt)) description];
  IOSCharArray *fractCharArr = [IOSCharArray newArrayWithLength:scale__];
  NSString *fractStr = [((JavaMathBigInteger *) nil_chk(fract)) toStringWithInt:2];
  jint fractLen = ((jint) [((NSString *) nil_chk(fractStr)) length]);
  jint zeroes = scale__ - fractLen;
  for (jint i = 0; i < zeroes; i++) {
    *IOSCharArray_GetRef(fractCharArr, i) = '0';
  }
  for (jint j = 0; j < fractLen; j++) {
    *IOSCharArray_GetRef(fractCharArr, zeroes + j) = [fractStr charAtWithInt:j];
  }
  NSString *rightOfPoint = [NSString stringWithCharacters:fractCharArr];
  JavaLangStringBuffer *sb = new_JavaLangStringBuffer_initWithNSString_(leftOfPoint);
  (void) [sb appendWithNSString:@"."];
  (void) [sb appendWithNSString:rightOfPoint];
  return [sb description];
}

- (jboolean)isEqual:(id)o {
  if (self == o) {
    return YES;
  }
  if (!([o isKindOfClass:[OrgBouncycastleMathEcSimpleBigDecimal class]])) {
    return NO;
  }
  OrgBouncycastleMathEcSimpleBigDecimal *other = (OrgBouncycastleMathEcSimpleBigDecimal *) check_class_cast(o, [OrgBouncycastleMathEcSimpleBigDecimal class]);
  return (([((JavaMathBigInteger *) nil_chk(bigInt_)) isEqual:((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk(other))->bigInt_]) && (scale__ == other->scale__));
}

- (NSUInteger)hash {
  return ((jint) [((JavaMathBigInteger *) nil_chk(bigInt_)) hash]) ^ scale__;
}

@end

OrgBouncycastleMathEcSimpleBigDecimal *OrgBouncycastleMathEcSimpleBigDecimal_getInstanceWithJavaMathBigInteger_withInt_(JavaMathBigInteger *value, jint scale_) {
  OrgBouncycastleMathEcSimpleBigDecimal_initialize();
  return new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_([((JavaMathBigInteger *) nil_chk(value)) shiftLeftWithInt:scale_], scale_);
}

void OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_(OrgBouncycastleMathEcSimpleBigDecimal *self, JavaMathBigInteger *bigInt, jint scale_) {
  (void) NSObject_init(self);
  if (scale_ < 0) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"scale may not be negative");
  }
  self->bigInt_ = bigInt;
  self->scale__ = scale_;
}

OrgBouncycastleMathEcSimpleBigDecimal *new_OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_(JavaMathBigInteger *bigInt, jint scale_) {
  OrgBouncycastleMathEcSimpleBigDecimal *self = [OrgBouncycastleMathEcSimpleBigDecimal alloc];
  OrgBouncycastleMathEcSimpleBigDecimal_initWithJavaMathBigInteger_withInt_(self, bigInt, scale_);
  return self;
}

void OrgBouncycastleMathEcSimpleBigDecimal_checkScaleWithOrgBouncycastleMathEcSimpleBigDecimal_(OrgBouncycastleMathEcSimpleBigDecimal *self, OrgBouncycastleMathEcSimpleBigDecimal *b) {
  if (self->scale__ != ((OrgBouncycastleMathEcSimpleBigDecimal *) nil_chk(b))->scale__) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Only SimpleBigDecimal of same scale allowed in arithmetic operations");
  }
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(OrgBouncycastleMathEcSimpleBigDecimal)
