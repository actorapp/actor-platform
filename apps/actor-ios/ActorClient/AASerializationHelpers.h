//
//  SASerializationHelpers.h
//  SecretProto
//
//  Created by Антон Буков on 30.07.14.
//  Copyright (c) 2014 Anton Bukov. All rights reserved.
//

#import <Foundation/Foundation.h>

#define GETTER_CREATE_IF_NIL(type, field) \
- (type *)field \
{ \
if (_##field == nil) \
_##field = [[type alloc] init]; \
return _##field; \
}

@interface NSDictionary (Merged)
- (instancetype)merged:(NSDictionary *)dict;
@end

BOOL try_extract_varint(const uint8_t *data, NSInteger length,
                        NSInteger *result, NSInteger *resultLength);

int8_t extract_byte(const uint8_t *data, NSInteger *offset);
int32_t extract_int(const uint8_t *data, NSInteger *offset);
int64_t extract_long(const uint8_t *data, NSInteger *offset);
uint8_t extract_ubyte(const uint8_t *data, NSInteger *offset);
uint32_t extract_uint(const uint8_t *data, NSInteger *offset);
uint64_t extract_ulong(const uint8_t *data, NSInteger *offset);
NSInteger extract_varint(const uint8_t *data, NSInteger *offset);
NSData *extract_bytes(const uint8_t *data, NSInteger *offset);
NSString *extract_string(const uint8_t *data, NSInteger *offset);

void insert_byte(NSMutableData *data, int8_t value);
void insert_int(NSMutableData *data, int32_t value);
void insert_long(NSMutableData *data, int64_t value);
void insert_ubyte(NSMutableData *data, uint8_t value);
void insert_uint(NSMutableData *data, uint32_t value);
void insert_ulong(NSMutableData *data, uint64_t value);
void insert_variant(NSMutableData *data, NSInteger value);
void insert_bytes(NSMutableData *data, NSData *value);
void insert_string(NSMutableData *data, NSString *value);
