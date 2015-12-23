# Encoding

Type encoding actually is taken from protobuf, but we describe it in details. Protocol encoding has this serialization primitives:

``varint`` - unsigned integer

Each byte in a varint, except the last byte, has the most significant bit (msb) set – this indicates that there are further bytes to come. The lower 7 bits of each byte are used to store the two's complement representation of the number in groups of 7 bits, least significant group first.

``int`` - signed 32bit integer

int value is encoded as 4-byte big endian signed integer

``long`` - signed 64bit intenger

long value is encoded as 8-byte big endian signed integer

``byte`` - unsigned byte

byte value is encoded as single big endian byte

``bytes`` - byte array

bytes is encoded as varint of bytes length and raw bytes

``longs`` - long array

bytes is encoded as varint of longs length and then raw long values

``string`` - byte array that contains UTF-8 text

string is encoded in the same way as bytes

### Structure

```
<struct_name> {
 HEADER = <header_id>; // optional
 <arg1>: <argType1>
 <arg3>: <argType2>
 ....
 <argN>: <argTypeN>
}
```

If structure contains HEADER than serialize HEADER value as single unsigned byte.
After HEADER scrutrure parameters is encoded in order from structure descr
