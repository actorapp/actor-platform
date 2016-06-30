/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import java.io.IOException;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

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

    public boolean isEOF() {
        return maxOffset <= offset;
    }

    public int getOffset() {
        return offset;
    }

//    public int getRemaining() {
//        return maxOffset - offset;
//    }
//
//    public void skip(int size) throws IOException {
//        if (offset + size > maxOffset) {
//            throw new IOException();
//        }
//        offset += size;
//    }

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

        if (count > BserLimits.MAX_BLOCK_SIZE) {
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
        if (len > BserLimits.MAX_BLOCK_SIZE) {
            throw new IOException();
        }
        return readBytes((int) len);
    }

    public long[] readProtoLongs() throws IOException {
        long len = readVarInt();
        if (len < 0) {
            throw new IOException();
        }
        if (len > BserLimits.MAX_PROTO_REPEATED) {
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
}
