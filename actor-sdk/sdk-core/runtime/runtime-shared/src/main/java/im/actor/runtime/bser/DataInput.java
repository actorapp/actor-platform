/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import java.io.IOException;

public class DataInput {
    private byte[] data;
    private int offset;
    private int maxOffset;

    public DataInput(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data can't be null");
        }

        this.data = data;
        this.offset = 0;
        this.maxOffset = data.length;
    }

    public DataInput(byte[] data, int offset, int len) {
        if (data == null) {
            throw new IllegalArgumentException("data can't be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset can't be negative");
        }
        if (len < 0) {
            throw new IllegalArgumentException("Length can't be negative");
        }
        if (data.length < offset + len) {
            throw new IllegalArgumentException("Inconsistent lengths, total: " + data.length + ", offset: " + offset + ", len: " + len);
        }

        this.data = data;
        this.offset = offset;
        this.maxOffset = offset + len;
    }

    public byte[] getData() {
        return data;
    }

    public int getMaxOffset() {
        return maxOffset;
    }

    public boolean isEOF() {
        return maxOffset <= offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getRemaining() {
        return maxOffset - offset;
    }

    public void skip(int size) throws IOException {
        if (offset + size > maxOffset) {
            throw new IOException();
        }
        offset += size;
    }

    public int readByte() throws IOException {
        if (offset == maxOffset) {
            throw new IOException();
        }
        return data[offset++] & 0xFF;
    }

    public int readInt() throws IOException {
        if (offset + 4 > maxOffset) {
            throw new IOException();
        }

        int res = (data[offset + 3] & 0xFF) +
                ((data[offset + 2] & 0xFF) << 8) +
                ((data[offset + 1] & 0xFF) << 16) +
                ((data[offset] & 0xFF) << 24);
        offset += 4;
        return res;
    }

    public long readLong() throws IOException {
        if (offset + 8 > maxOffset) {
            throw new IOException();
        }

        long a1 = data[offset + 3] & 0xFF;
        long a2 = data[offset + 2] & 0xFF;
        long a3 = data[offset + 1] & 0xFF;
        long a4 = data[offset + 0] & 0xFF;

        long res1 = (a1) + (a2 << 8) + (a3 << 16) + (a4 << 24);
        offset += 4;

        long b1 = data[offset + 3] & 0xFF;
        long b2 = data[offset + 2] & 0xFF;
        long b3 = data[offset + 1] & 0xFF;
        long b4 = data[offset + 0] & 0xFF;

        long res2 = (b1) + (b2 << 8) + (b3 << 16) + (b4 << 24);
        offset += 4;

        return res2 + (res1 << 32);
    }

    public long readUInt() throws IOException {
        if (offset + 4 > maxOffset) {
            throw new IOException();
        }

        long a1 = data[offset + 3] & 0xFF;
        long a2 = data[offset + 2] & 0xFF;
        long a3 = data[offset + 1] & 0xFF;
        long a4 = data[offset + 0] & 0xFF;
        offset += 4;
        return (a1) + (a2 << 8) + (a3 << 16) + (a4 << 24);
    }

    public byte[] readBytes(int count) throws IOException {

        if (count < 0) {
            throw new IOException("Count can't be negative");
        }

        if (count > Limits.MAX_BLOCK_SIZE) {
            throw new IOException("Unable to read more than 1 MB");
        }

        if (offset + count > maxOffset) {
            throw new IOException("Too many to read, max len: " + maxOffset + ", required len: " + (offset + count));
        }

        byte[] res = new byte[count];
        for (int i = 0; i < count; i++) {
            res[i] = data[offset++];
        }
        return res;
    }

    public int readVarInt32() throws IOException {
        long varInt = readVarInt();
        if (varInt > Integer.MAX_VALUE || varInt < Integer.MIN_VALUE) {
            throw new IOException("Too big VarInt32");
        }
        return (int) varInt;
    }

    public long readVarInt() throws IOException {
        long value = 0;
        long i = 0;
        long b;

        do {
            if (offset == maxOffset) {
                throw new IOException();
            }

            b = data[offset++] & 0xFF;

            if ((b & 0x80) != 0) {
                value |= (b & 0x7F) << i;
                i += 7;
                if (i > 70) {
                    throw new IOException();
                }
            } else {
                break;
            }
        } while (true);

        return value | (b << i);
    }

    public byte[] readProtoBytes() throws IOException {
        long len = readVarInt();
        if (len < 0) {
            throw new IOException();
        }
        if (len > Limits.MAX_BLOCK_SIZE) {
            throw new IOException();
        }
        return readBytes((int) len);
    }

    public long[] readProtoLongs() throws IOException {
        long len = readVarInt();
        if (len < 0) {
            throw new IOException();
        }
        if (len > Limits.MAX_PROTO_REPEATED) {
            throw new IOException();
        }

        long[] res = new long[(int) len];
        for (int i = 0; i < res.length; i++) {
            res[i] = readLong();
        }
        return res;
    }

    public String readProtoString() throws IOException {
        byte[] data = readProtoBytes();
        return new String(data, "UTF-8");
    }

    public boolean readProtoBool() throws IOException {
        return readByte() != 0;
    }

    public int readASN1Length() throws IOException {
        int length = readByte();
        if (length < 0) {
            throw new IOException("EOF found when length expected");
        }

        if (length == 0x80) {
            return -1;      // indefinite-length encoding
        }

        if (length > 127) {
            int size = length & 0x7f;

            // Note: The invalid long form "0xff" (see X.690 8.1.3.5c) will be caught here
            if (size > 4) {
                throw new IOException("DER length more than 4 bytes: " + size);
            }

            length = 0;
            for (int i = 0; i < size; i++) {
                int next = readByte();
                length = (length << 8) + next;
            }

            if (length < 0) {
                throw new IOException("corrupted stream - negative length found");
            }

            // after all we must have read at least 1 byte
//            if (length >= limit) {
//                throw new IOException("corrupted stream - out of bounds length found");
//            }
        }

        return length;
    }

    public int readASN1Tag() throws IOException {
        int tag = readByte();
        if (tag == 0) {
            throw new IOException();
        }
        return tag;
    }

    public int readASN1TagNumber(int tag) throws IOException {

        int tagNo = tag & 0x1f;

        //
        // with tagged object tag number is bottom 5 bits, or stored at the start of the content
        //
        if (tagNo == 0x1f) {
            tagNo = 0;

            int b = readByte();

            // X.690-0207 8.1.2.4.2
            // "c) bits 7 to 1 of the first subsequent octet shall not all be zero."
            if ((b & 0x7f) == 0) // Note: -1 will pass
            {
                throw new IOException("corrupted stream - invalid high tag number found");
            }

            while ((b >= 0) && ((b & 0x80) != 0)) {
                tagNo |= (b & 0x7f);
                tagNo <<= 7;
                b = readByte();
            }

            if (b < 0) {
                throw new IOException("EOF found inside tag value.");
            }

            tagNo |= (b & 0x7f);
        }

        return tagNo;
    }
}
