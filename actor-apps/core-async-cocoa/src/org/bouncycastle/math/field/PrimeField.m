//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-platform/actor-apps/core-crypto/src/main/java/org/bouncycastle/math/field/PrimeField.java
//


#include "J2ObjC_source.h"
#include "java/math/BigInteger.h"
#include "org/bouncycastle/math/field/PrimeField.h"

@implementation OrgBouncycastleMathFieldPrimeField

- (instancetype)initWithJavaMathBigInteger:(JavaMathBigInteger *)characteristic {
  OrgBouncycastleMathFieldPrimeField_initWithJavaMathBigInteger_(self, characteristic);
  return self;
}

- (JavaMathBigInteger *)getCharacteristic {
  return characteristic_;
}

- (jint)getDimension {
  return 1;
}

- (jboolean)isEqual:(id)obj {
  if (self == obj) {
    return YES;
  }
  if (!([obj isKindOfClass:[OrgBouncycastleMathFieldPrimeField class]])) {
    return NO;
  }
  OrgBouncycastleMathFieldPrimeField *other = (OrgBouncycastleMathFieldPrimeField *) check_class_cast(obj, [OrgBouncycastleMathFieldPrimeField class]);
  return [((JavaMathBigInteger *) nil_chk(characteristic_)) isEqual:((OrgBouncycastleMathFieldPrimeField *) nil_chk(other))->characteristic_];
}

- (NSUInteger)hash {
  return ((jint) [((JavaMathBigInteger *) nil_chk(characteristic_)) hash]);
}

@end

void OrgBouncycastleMathFieldPrimeField_initWithJavaMathBigInteger_(OrgBouncycastleMathFieldPrimeField *self, JavaMathBigInteger *characteristic) {
  (void) NSObject_init(self);
  self->characteristic_ = characteristic;
}

OrgBouncycastleMathFieldPrimeField *new_OrgBouncycastleMathFieldPrimeField_initWithJavaMathBigInteger_(JavaMathBigInteger *characteristic) {
  OrgBouncycastleMathFieldPrimeField *self = [OrgBouncycastleMathFieldPrimeField alloc];
  OrgBouncycastleMathFieldPrimeField_initWithJavaMathBigInteger_(self, characteristic);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(OrgBouncycastleMathFieldPrimeField)
