package com.droidkit.bser;

import com.droidkit.bser.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.droidkit.bser.WireTypes.*;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class BserParser {
    public static SparseArray<Object> deserialize(InputStream is) throws IOException {
        SparseArray<Object> hashMap = new SparseArray<Object>();
        int currentTag;
        while ((currentTag = readTag(is)) > 0) {
            int id = currentTag >> 3;
            int type = currentTag & 0x7;

            if (type == TYPE_VARINT) {
                put(id, readVarInt(is), hashMap);
            } else if (type == TYPE_LENGTH_DELIMITED) {
                int size = (int) readVarInt(is);
                put(id, readBytes(size, is), hashMap);
            } else if (type == TYPE_64BIT) {
                put(id, readLong(is), hashMap);
            } else if (type == TYPE_32BIT) {
                put(id, readUInt(is), hashMap);
            } else {
                throw new IOException("Unknown Wire Type #" + type);
            }
        }
        return hashMap;
    }

    private static void put(int id, Object res, SparseArray<Object> hashMap) {
        if (hashMap.get(id) != null) {
            if (hashMap.get(id) instanceof List) {
                ((List) hashMap.get(id)).add(res);
            } else {
                ArrayList<Object> list = new ArrayList<Object>();
                list.add(hashMap.get(id));
                list.add(res);
                hashMap.put(id, list);
            }
        } else {
            hashMap.put(id, res);
        }
    }

    private static int readTag(InputStream is) throws IOException {
        int res = is.read();
        if (res < 0) {
            return 0;
        }
        return res & 0xFF;
    }

    public static long readVarInt(InputStream stream) throws IOException {
        long value = 0;
        long i = 0;
        int b;

        do {
            b = stream.read();
            if (b < 0) {
                throw new IOException("Unexpected end of varint");
            }

            value |= (b & 0x7FL) << i;

            if ((b & 0x80) != 0) {
                i += 7;
                if (i > 70) {
                    throw new IOException();
                }
            } else {
                break;
            }
        } while (true);

        return value;
    }

    public static long readUInt(InputStream stream) throws IOException {
        long a = stream.read();
        if (a < 0) {
            throw new IOException();
        }
        long b = stream.read();
        if (b < 0) {
            throw new IOException();
        }
        long c = stream.read();
        if (c < 0) {
            throw new IOException();
        }
        long d = stream.read();
        if (d < 0) {
            throw new IOException();
        }

        return a + (b << 8) + (c << 16) + (d << 24);
    }

    public static long readLong(InputStream stream) throws IOException {
        long a = readUInt(stream);
        long b = readUInt(stream);

        return a + (b << 32);
    }

    public static byte[] readBytes(int count, InputStream stream) throws IOException {
        byte[] res = new byte[count];
        int offset = 0;
        while (offset < res.length) {
            int readed = stream.read(res, offset, res.length - offset);
            if (readed > 0) {
                offset += readed;
            } else if (readed < 0) {
                throw new IOException();
            } else {
                Thread.yield();
            }
        }
        return res;
    }
}