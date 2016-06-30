/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import java.io.IOException;

import im.actor.runtime.Crypto;

public final class Utils {

    public static int convertInt(long val) throws IOException {
        if (val < Integer.MIN_VALUE) {
            throw new IOException("Too small value");
        } else if (val > Integer.MAX_VALUE) {
            throw new IOException("Too big value");
        }
        return (int) val;
    }

    public static String convertString(byte[] data) throws IOException {
        if (data == null) {
            return null;
        } else {
            return new String(data, "utf-8");
        }
    }

    public static String byteArrayToString(byte[] data) {
        if (data == null) {
            return "null";
        } else {
            return Crypto.hex(data);
        }
    }

    public static String byteArrayToStringCompact(byte[] data) {
        if (data == null) {
            return "null";
        } else {
            return Crypto.hex(Crypto.MD5(data));
        }
    }

    public static byte[] intToBytes(int v) {
        byte[] data = new byte[4];
        int offset = 0;
        data[offset++] = (byte) ((v >> 24) & 0xFF);
        data[offset++] = (byte) ((v >> 16) & 0xFF);
        data[offset++] = (byte) ((v >> 8) & 0xFF);
        data[offset++] = (byte) (v & 0xFF);
        return data;
    }

    public static long bytesToInt(byte[] data) {
        int offset = 0;
        int a1 = data[offset + 3] & 0xFF;
        int a2 = data[offset + 2] & 0xFF;
        int a3 = data[offset + 1] & 0xFF;
        int a4 = data[offset + 0] & 0xFF;
        return (a1) + (a2 << 8) + (a3 << 16) + (a4 << 24);
    }

    public static byte[] longToBytes(long v) {
        byte[] data = new byte[8];
        int offset = 0;
        data[offset++] = (byte) ((v >> 56) & 0xFF);
        data[offset++] = (byte) ((v >> 48) & 0xFF);
        data[offset++] = (byte) ((v >> 40) & 0xFF);
        data[offset++] = (byte) ((v >> 32) & 0xFF);
        data[offset++] = (byte) ((v >> 24) & 0xFF);
        data[offset++] = (byte) ((v >> 16) & 0xFF);
        data[offset++] = (byte) ((v >> 8) & 0xFF);
        data[offset++] = (byte) (v & 0xFF);
        return data;
    }

    public static long bytesToLong(byte[] data) {
        int offset = 0;
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

    private Utils() {

    }
}
