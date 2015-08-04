//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-proprietary/actor-apps/core/src/main/java/im/actor/model/droidkit/bser/BserWriter.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/droidkit/bser/DataOutput.h"
#include "im/actor/model/droidkit/bser/Limits.h"
#include "im/actor/model/droidkit/bser/WireTypes.h"
#include "im/actor/model/droidkit/bser/util/SparseArray.h"
#include "java/io/IOException.h"
#include "java/lang/Boolean.h"
#include "java/lang/Double.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/List.h"

@interface BSBserWriter () {
 @public
  BSDataOutput *stream_;
  ImActorModelDroidkitBserUtilSparseArray *writtenFields_;
}

- (void)writeTagWithInt:(jint)fieldNumber
                withInt:(jint)wireType;

- (void)writeVarIntFieldWithInt:(jint)fieldNumber
                       withLong:(jlong)value;

- (void)writeBytesFieldWithInt:(jint)fieldNumber
                 withByteArray:(IOSByteArray *)value;

- (void)writeVar64FixedWithInt:(jint)fieldNumber
                      withLong:(jlong)value;

- (void)writeVar32FixedWithInt:(jint)fieldNumber
                      withLong:(jlong)value;

- (void)writeVarIntWithLong:(jlong)value;

- (void)writeLongWithLong:(jlong)v;

- (void)writeIntWithLong:(jlong)v;

- (void)writeBytesWithByteArray:(IOSByteArray *)data;

@end

J2OBJC_FIELD_SETTER(BSBserWriter, stream_, BSDataOutput *)
J2OBJC_FIELD_SETTER(BSBserWriter, writtenFields_, ImActorModelDroidkitBserUtilSparseArray *)

__attribute__((unused)) static void BSBserWriter_writeTagWithInt_withInt_(BSBserWriter *self, jint fieldNumber, jint wireType);

__attribute__((unused)) static void BSBserWriter_writeVarIntFieldWithInt_withLong_(BSBserWriter *self, jint fieldNumber, jlong value);

__attribute__((unused)) static void BSBserWriter_writeBytesFieldWithInt_withByteArray_(BSBserWriter *self, jint fieldNumber, IOSByteArray *value);

__attribute__((unused)) static void BSBserWriter_writeVar64FixedWithInt_withLong_(BSBserWriter *self, jint fieldNumber, jlong value);

__attribute__((unused)) static void BSBserWriter_writeVar32FixedWithInt_withLong_(BSBserWriter *self, jint fieldNumber, jlong value);

__attribute__((unused)) static void BSBserWriter_writeVarIntWithLong_(BSBserWriter *self, jlong value);

__attribute__((unused)) static void BSBserWriter_writeLongWithLong_(BSBserWriter *self, jlong v);

__attribute__((unused)) static void BSBserWriter_writeIntWithLong_(BSBserWriter *self, jlong v);

__attribute__((unused)) static void BSBserWriter_writeBytesWithByteArray_(BSBserWriter *self, IOSByteArray *data);

@implementation BSBserWriter

- (instancetype)initWithBSDataOutput:(BSDataOutput *)stream {
  BSBserWriter_initWithBSDataOutput_(self, stream);
  return self;
}

- (void)writeBytesWithInt:(jint)fieldNumber
            withByteArray:(IOSByteArray *)value {
  if (value == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
  }
  if (((IOSByteArray *) nil_chk(value))->size_ > BSLimits_MAX_BLOCK_SIZE) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Unable to write more than 1 MB");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  BSBserWriter_writeBytesFieldWithInt_withByteArray_(self, fieldNumber, value);
}

- (void)writeStringWithInt:(jint)fieldNumber
              withNSString:(NSString *)value {
  if (value == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  BSBserWriter_writeBytesFieldWithInt_withByteArray_(self, fieldNumber, [((NSString *) nil_chk(value)) getBytes]);
}

- (void)writeBoolWithInt:(jint)fieldNumber
             withBoolean:(jboolean)value {
  BSBserWriter_writeVarIntFieldWithInt_withLong_(self, fieldNumber, value ? 1 : 0);
}

- (void)writeIntWithInt:(jint)fieldNumber
                withInt:(jint)value {
  BSBserWriter_writeVarIntFieldWithInt_withLong_(self, fieldNumber, value);
}

- (void)writeIntFixedWithInt:(jint)fieldNumber
                     withInt:(jint)value {
  BSBserWriter_writeVar32FixedWithInt_withLong_(self, fieldNumber, value);
}

- (void)writeDoubleWithInt:(jint)fieldNumber
                withDouble:(jdouble)value {
  BSBserWriter_writeVar64FixedWithInt_withLong_(self, fieldNumber, JavaLangDouble_doubleToLongBitsWithDouble_(value));
}

- (void)writeLongFixedWithInt:(jint)fieldNumber
                     withLong:(jlong)value {
  BSBserWriter_writeVar64FixedWithInt_withLong_(self, fieldNumber, JavaLangDouble_doubleToLongBitsWithDouble_(value));
}

- (void)writeLongWithInt:(jint)fieldNumber
                withLong:(jlong)value {
  BSBserWriter_writeVarIntFieldWithInt_withLong_(self, fieldNumber, value);
}

- (void)writeRepeatedLongWithInt:(jint)fieldNumber
                withJavaUtilList:(id<JavaUtilList>)values {
  if (values == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Values can not be null");
  }
  if ([((id<JavaUtilList>) nil_chk(values)) size] > BSLimits_MAX_PROTO_REPEATED) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Too many values");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  for (JavaLangLong * __strong l in values) {
    if (l == nil) {
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
    }
    BSBserWriter_writeVarIntFieldWithInt_withLong_(self, fieldNumber, [((JavaLangLong *) nil_chk(l)) longLongValue]);
  }
}

- (void)writeRepeatedIntWithInt:(jint)fieldNumber
               withJavaUtilList:(id<JavaUtilList>)values {
  if (values == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Values can not be null");
  }
  if ([((id<JavaUtilList>) nil_chk(values)) size] > BSLimits_MAX_PROTO_REPEATED) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Too many values");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  for (JavaLangInteger * __strong l in values) {
    if (l == nil) {
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
    }
    BSBserWriter_writeVarIntFieldWithInt_withLong_(self, fieldNumber, [((JavaLangInteger *) nil_chk(l)) intValue]);
  }
}

- (void)writeRepeatedBoolWithInt:(jint)fieldNumber
                withJavaUtilList:(id<JavaUtilList>)values {
  if (values == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Values can not be null");
  }
  if ([((id<JavaUtilList>) nil_chk(values)) size] > BSLimits_MAX_PROTO_REPEATED) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Too many values");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  for (JavaLangBoolean * __strong l in values) {
    if (l == nil) {
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
    }
    [self writeBoolWithInt:fieldNumber withBoolean:[((JavaLangBoolean *) nil_chk(l)) booleanValue]];
  }
}

- (void)writeRepeatedBytesWithInt:(jint)fieldNumber
                 withJavaUtilList:(id<JavaUtilList>)values {
  if (values == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Values can not be null");
  }
  if ([((id<JavaUtilList>) nil_chk(values)) size] > BSLimits_MAX_PROTO_REPEATED) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Too many values");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  for (IOSByteArray * __strong l in values) {
    if (l == nil) {
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
    }
    [self writeBytesWithInt:fieldNumber withByteArray:l];
  }
}

- (void)writeRepeatedObjWithInt:(jint)fieldNumber
               withJavaUtilList:(id<JavaUtilList>)values {
  if (values == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Values can not be null");
  }
  if ([((id<JavaUtilList>) nil_chk(values)) size] > BSLimits_MAX_PROTO_REPEATED) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Too many values");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  for (BSBserObject * __strong l in values) {
    if (l == nil) {
      @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
    }
    [self writeObjectWithInt:fieldNumber withBSBserObject:l];
  }
}

- (void)writeObjectWithInt:(jint)fieldNumber
          withBSBserObject:(BSBserObject *)value {
  if (value == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Value can not be null");
  }
  [((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) putWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(YES)];
  BSBserWriter_writeTagWithInt_withInt_(self, fieldNumber, BSWireTypes_TYPE_LENGTH_DELIMITED);
  BSDataOutput *outputStream = new_BSDataOutput_init();
  BSBserWriter *writer = new_BSBserWriter_initWithBSDataOutput_(outputStream);
  [((BSBserObject *) nil_chk(value)) serializeWithBSBserWriter:writer];
  BSBserWriter_writeBytesWithByteArray_(self, [outputStream toByteArray]);
}

- (void)writeUnmappedWithInt:(jint)fieldNumber
                      withId:(id)value {
  if ([((JavaLangBoolean *) nil_chk([((ImActorModelDroidkitBserUtilSparseArray *) nil_chk(writtenFields_)) getWithInt:fieldNumber withId:JavaLangBoolean_valueOfWithBoolean_(NO)])) booleanValue]) {
    return;
  }
  if ([value isKindOfClass:[JavaLangLong class]]) {
    [self writeLongWithInt:fieldNumber withLong:[((JavaLangLong *) nil_chk((JavaLangLong *) check_class_cast(value, [JavaLangLong class]))) longLongValue]];
  }
  else if ([value isKindOfClass:[IOSByteArray class]]) {
    [self writeBytesWithInt:fieldNumber withByteArray:(IOSByteArray *) check_class_cast(value, [IOSByteArray class])];
  }
  else if ([JavaUtilList_class_() isInstance:value]) {
    for (id __strong o in nil_chk((id<JavaUtilList>) check_protocol_cast(value, @protocol(JavaUtilList)))) {
      if ([o isKindOfClass:[JavaLangLong class]]) {
        [self writeLongWithInt:fieldNumber withLong:[((JavaLangLong *) nil_chk((JavaLangLong *) check_class_cast(o, [JavaLangLong class]))) longLongValue]];
      }
      else if ([o isKindOfClass:[IOSByteArray class]]) {
        [self writeBytesWithInt:fieldNumber withByteArray:(IOSByteArray *) check_class_cast(o, [IOSByteArray class])];
      }
      else {
        @throw new_JavaIoIOException_initWithNSString_(@"Incorrect unmapped value in List");
      }
    }
  }
  else {
    @throw new_JavaIoIOException_initWithNSString_(@"Incorrect unmapped value");
  }
}

- (void)writeRawWithByteArray:(IOSByteArray *)raw {
  if (raw == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Raw can not be null");
  }
  [((BSDataOutput *) nil_chk(stream_)) writeBytesWithByteArray:raw withInt:0 withInt:((IOSByteArray *) nil_chk(raw))->size_];
}

- (void)writeTagWithInt:(jint)fieldNumber
                withInt:(jint)wireType {
  BSBserWriter_writeTagWithInt_withInt_(self, fieldNumber, wireType);
}

- (void)writeVarIntFieldWithInt:(jint)fieldNumber
                       withLong:(jlong)value {
  BSBserWriter_writeVarIntFieldWithInt_withLong_(self, fieldNumber, value);
}

- (void)writeBytesFieldWithInt:(jint)fieldNumber
                 withByteArray:(IOSByteArray *)value {
  BSBserWriter_writeBytesFieldWithInt_withByteArray_(self, fieldNumber, value);
}

- (void)writeVar64FixedWithInt:(jint)fieldNumber
                      withLong:(jlong)value {
  BSBserWriter_writeVar64FixedWithInt_withLong_(self, fieldNumber, value);
}

- (void)writeVar32FixedWithInt:(jint)fieldNumber
                      withLong:(jlong)value {
  BSBserWriter_writeVar32FixedWithInt_withLong_(self, fieldNumber, value);
}

- (void)writeVarIntWithLong:(jlong)value {
  BSBserWriter_writeVarIntWithLong_(self, value);
}

- (void)writeLongWithLong:(jlong)v {
  BSBserWriter_writeLongWithLong_(self, v);
}

- (void)writeIntWithLong:(jlong)v {
  BSBserWriter_writeIntWithLong_(self, v);
}

- (void)writeBytesWithByteArray:(IOSByteArray *)data {
  BSBserWriter_writeBytesWithByteArray_(self, data);
}

@end

void BSBserWriter_initWithBSDataOutput_(BSBserWriter *self, BSDataOutput *stream) {
  (void) NSObject_init(self);
  self->writtenFields_ = new_ImActorModelDroidkitBserUtilSparseArray_init();
  if (stream == nil) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Stream can not be null");
  }
  self->stream_ = stream;
}

BSBserWriter *new_BSBserWriter_initWithBSDataOutput_(BSDataOutput *stream) {
  BSBserWriter *self = [BSBserWriter alloc];
  BSBserWriter_initWithBSDataOutput_(self, stream);
  return self;
}

void BSBserWriter_writeTagWithInt_withInt_(BSBserWriter *self, jint fieldNumber, jint wireType) {
  fieldNumber = (fieldNumber & (jint) 0xFFFF);
  if (fieldNumber <= 0) {
    @throw new_JavaLangIllegalArgumentException_initWithNSString_(@"Field Number must greater than zero");
  }
  jlong tag = ((jlong) (LShift32(fieldNumber, 3)) | wireType);
  [((BSDataOutput *) nil_chk(self->stream_)) writeVarIntWithLong:tag];
}

void BSBserWriter_writeVarIntFieldWithInt_withLong_(BSBserWriter *self, jint fieldNumber, jlong value) {
  BSBserWriter_writeTagWithInt_withInt_(self, fieldNumber, BSWireTypes_TYPE_VARINT);
  BSBserWriter_writeVarIntWithLong_(self, value);
}

void BSBserWriter_writeBytesFieldWithInt_withByteArray_(BSBserWriter *self, jint fieldNumber, IOSByteArray *value) {
  BSBserWriter_writeTagWithInt_withInt_(self, fieldNumber, BSWireTypes_TYPE_LENGTH_DELIMITED);
  BSBserWriter_writeBytesWithByteArray_(self, value);
}

void BSBserWriter_writeVar64FixedWithInt_withLong_(BSBserWriter *self, jint fieldNumber, jlong value) {
  BSBserWriter_writeTagWithInt_withInt_(self, fieldNumber, BSWireTypes_TYPE_64BIT);
  BSBserWriter_writeLongWithLong_(self, value);
}

void BSBserWriter_writeVar32FixedWithInt_withLong_(BSBserWriter *self, jint fieldNumber, jlong value) {
  BSBserWriter_writeTagWithInt_withInt_(self, fieldNumber, BSWireTypes_TYPE_32BIT);
  BSBserWriter_writeIntWithLong_(self, value);
}

void BSBserWriter_writeVarIntWithLong_(BSBserWriter *self, jlong value) {
  [((BSDataOutput *) nil_chk(self->stream_)) writeVarIntWithLong:value & (jint) 0xFFFFFFFF];
}

void BSBserWriter_writeLongWithLong_(BSBserWriter *self, jlong v) {
  [((BSDataOutput *) nil_chk(self->stream_)) writeLongWithLong:v & (jint) 0xFFFFFFFF];
}

void BSBserWriter_writeIntWithLong_(BSBserWriter *self, jlong v) {
  [((BSDataOutput *) nil_chk(self->stream_)) writeIntWithInt:(jint) (v & (jint) 0xFFFF)];
}

void BSBserWriter_writeBytesWithByteArray_(BSBserWriter *self, IOSByteArray *data) {
  [((BSDataOutput *) nil_chk(self->stream_)) writeProtoBytesWithByteArray:data withInt:0 withInt:((IOSByteArray *) nil_chk(data))->size_];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(BSBserWriter)
