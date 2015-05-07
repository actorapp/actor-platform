//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/crypto/asn1/ASN1OctetString.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/crypto/asn1/ASN1OctetString.h"
#include "im/actor/model/crypto/asn1/ASN1Primitive.h"
#include "im/actor/model/droidkit/bser/DataInput.h"
#include "im/actor/model/droidkit/bser/DataOutput.h"
#include "java/io/IOException.h"

@interface BCASN1OctetString () {
 @public
  IOSByteArray *data_;
}

@end

J2OBJC_FIELD_SETTER(BCASN1OctetString, data_, IOSByteArray *)

@implementation BCASN1OctetString

+ (BCASN1OctetString *)readOctetStringWithBSDataInput:(BSDataInput *)dataInput {
  return BCASN1OctetString_readOctetStringWithBSDataInput_(dataInput);
}

- (instancetype)initWithByteArray:(IOSByteArray *)data {
  BCASN1OctetString_initWithByteArray_(self, data);
  return self;
}

- (IOSByteArray *)getData {
  return data_;
}

- (void)serializeWithBSDataOutput:(BSDataOutput *)dataOutput {
  [((BSDataOutput *) nil_chk(dataOutput)) writeByteWithInt:BCASN1Primitive_TAG_OCTET_STRING];
  [dataOutput writeASN1LengthWithInt:((IOSByteArray *) nil_chk(data_))->size_];
  [dataOutput writeBytesWithByteArray:data_ withInt:0 withInt:data_->size_];
}

@end

BCASN1OctetString *BCASN1OctetString_readOctetStringWithBSDataInput_(BSDataInput *dataInput) {
  BCASN1OctetString_initialize();
  return new_BCASN1OctetString_initWithByteArray_([dataInput readBytesWithInt:[((BSDataInput *) nil_chk(dataInput)) getRemaining]]);
}

void BCASN1OctetString_initWithByteArray_(BCASN1OctetString *self, IOSByteArray *data) {
  (void) BCASN1Primitive_init(self);
  self->data_ = data;
}

BCASN1OctetString *new_BCASN1OctetString_initWithByteArray_(IOSByteArray *data) {
  BCASN1OctetString *self = [BCASN1OctetString alloc];
  BCASN1OctetString_initWithByteArray_(self, data);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(BCASN1OctetString)
