//
//  SASerializationHelpers.m
//  SecretProto
//
//  Created by Антон Буков on 30.07.14.
//  Copyright (c) 2014 Anton Bukov. All rights reserved.
//

#import "AASerializationHelpers.h"

@implementation NSDictionary (AddKeyValue)
- (instancetype)merged:(NSDictionary *)dict
{
    NSMutableDictionary *mc = [self mutableCopy];
    [mc addEntriesFromDictionary:dict];
    return mc;
}
@end

BOOL try_extract_varint(const uint8_t *data, NSInteger length,
                        NSInteger *result, NSInteger *resultLength)
{
    const uint8_t *bytes = (const uint8_t *)data;
    
    *result = 0;
    *resultLength = 0;
    NSInteger shift = 0;
    do {
        if (*resultLength > length)
            return NO;
        *result += (bytes[*resultLength] & 0x7F) << shift;
        shift += 7;
    } while (bytes[(*resultLength)++] & 0x80);
    
    return YES;
}

int8_t extract_byte(const uint8_t *data, NSInteger *offset)
{
    int8_t value = *(int8_t *)data+*offset;
    *offset += sizeof(value);
    return value;
}

int32_t extract_int(const uint8_t *data, NSInteger *offset)
{
    int32_t value = ntohl(*(int32_t *)(data+*offset));
    *offset += sizeof(value);
    return value;
}

int64_t extract_long(const uint8_t *data, NSInteger *offset)
{
    int64_t value = ntohll(*(int64_t *)(data+*offset));
    *offset += sizeof(value);
    return value;
}

uint8_t extract_ubyte(const uint8_t *data, NSInteger *offset)
{
    uint8_t value = *(uint8_t *)(data+*offset);
    *offset += sizeof(value);
    return value;
}

uint32_t extract_uint(const uint8_t *data, NSInteger *offset)
{
    uint32_t value = ntohl(*(uint32_t *)(data+*offset));
    *offset += sizeof(value);
    return value;
}

uint64_t extract_ulong(const uint8_t *data, NSInteger *offset)
{
    uint64_t value = ntohll(*(uint64_t *)(data+*offset));
    *offset += sizeof(value);
    return value;
}

NSInteger extract_varint(const uint8_t *data, NSInteger *offset)
{
    NSInteger size;
    NSInteger sizeLen;
    try_extract_varint(data+*offset, 8, &size, &sizeLen);
    *offset += sizeLen;
    return size;
}

NSData *extract_bytes(const uint8_t *data, NSInteger *offset)
{
    NSInteger size = extract_varint(data, offset);
    NSData *dt = [NSData dataWithBytes:data+*offset length:size];
    *offset += size;
    return dt;
}

NSString *extract_string(const uint8_t *data, NSInteger *offset)
{
    NSInteger size = extract_varint(data, offset);
    NSString *str = [[NSString alloc] initWithBytes:data+*offset length:size encoding:NSUTF8StringEncoding];;
    *offset += size;
    return str;
}

void insert_byte(NSMutableData *data, int8_t value)
{
    [data appendBytes:&value length:sizeof(value)];
}

void insert_int(NSMutableData *data, int32_t value)
{
    int32_t raw = htonl(value);
    [data appendBytes:&raw length:sizeof(raw)];
}

void insert_long(NSMutableData *data, int64_t value)
{
    int64_t raw = htonll(value);
    [data appendBytes:&raw length:sizeof(raw)];
}

void insert_ubyte(NSMutableData *data, uint8_t value)
{
    [data appendBytes:&value length:sizeof(value)];
}

void insert_uint(NSMutableData *data, uint32_t value)
{
    uint32_t raw = htonl(value);
    [data appendBytes:&raw length:sizeof(raw)];
}

void insert_ulong(NSMutableData *data, uint64_t value)
{
    uint64_t raw = htonll(value);
    [data appendBytes:&raw length:sizeof(raw)];
}

void insert_variant(NSMutableData *data, NSInteger value)
{
    while (value) {
        uint8_t b = (value & 0x7F);
        b |= (value >>= 7) ? 0x80 : 0x00;
        [data appendBytes:&b length:1];
    }
}

void insert_bytes(NSMutableData *data, NSData *value)
{
    insert_variant(data, value.length);
    [data appendData:value];
}

void insert_string(NSMutableData *data, NSString *value)
{
    NSData *val = [value dataUsingEncoding:NSUTF8StringEncoding];
    insert_variant(data, val.length);
    [data appendData:val];
}
