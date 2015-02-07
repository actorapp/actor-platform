package im.actor.model.util;

import java.io.*;

/**
 * Helper class for writing and reading data for tl (de-)serialization.
 *
 * @author Korshakov Stepan (me@ex3ndr.com)
 */
public class StreamingUtils {

    public static byte[] intToBytes(int v) {
        return new byte[]{
                (byte) ((v >> 24) & 0xFF),
                (byte) ((v >> 16) & 0xFF),
                (byte) ((v >> 8) & 0xFF),
                (byte) (v & 0xFF)
        };
    }

    /**
     * Writing byte to stream
     *
     * @param v      value
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeByte(int v, OutputStream stream) throws IOException {
        stream.write(v);
    }

    /**
     * Writing byte to stream
     *
     * @param v      value
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeByte(byte v, OutputStream stream) throws IOException {
        stream.write(v);
    }

    /**
     * Writing int to stream
     *
     * @param v      value
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeInt(int v, OutputStream stream) throws IOException {
        writeByte((byte) ((v >> 24) & 0xFF), stream);
        writeByte((byte) ((v >> 16) & 0xFF), stream);
        writeByte((byte) ((v >> 8) & 0xFF), stream);
        writeByte((byte) (v & 0xFF), stream);
    }

    /**
     * Writing long to stream
     *
     * @param v      value
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeLong(long v, OutputStream stream) throws IOException {
        writeByte((byte) ((v >> 56) & 0xFF), stream);
        writeByte((byte) ((v >> 48) & 0xFF), stream);
        writeByte((byte) ((v >> 40) & 0xFF), stream);
        writeByte((byte) ((v >> 32) & 0xFF), stream);

        writeByte((byte) ((v >> 24) & 0xFF), stream);
        writeByte((byte) ((v >> 16) & 0xFF), stream);
        writeByte((byte) ((v >> 8) & 0xFF), stream);
        writeByte((byte) (v & 0xFF), stream);
    }

    /**
     * Writing double to stream
     *
     * @param v      value
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeDouble(double v, OutputStream stream) throws IOException {
        writeLong(Double.doubleToLongBits(v), stream);
    }

    /**
     * Writing byte array to stream
     *
     * @param data   data
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeBytes(byte[] data, OutputStream stream) throws IOException {
        stream.write(data);
    }

    /**
     * Writing byte array to stream
     *
     * @param data   data
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeBytes(byte[] data, int offset, int len, OutputStream stream) throws IOException {
        stream.write(data, offset, len);
    }

    /**
     * Reading int from stream
     *
     * @param stream source stream
     * @return value
     * @throws java.io.IOException reading exception
     */
    public static int readInt(InputStream stream) throws IOException {
        int a = stream.read();
        if (a < 0) {
            throw new IOException();
        }
        int b = stream.read();
        if (b < 0) {
            throw new IOException();
        }
        int c = stream.read();
        if (c < 0) {
            throw new IOException();
        }
        int d = stream.read();
        if (d < 0) {
            throw new IOException();
        }

        return d + (c << 8) + (b << 16) + (a << 24);
    }

    /**
     * Reading uint from stream
     *
     * @param stream source stream
     * @return value
     * @throws java.io.IOException reading exception
     */
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

        return d + (c << 8) + (b << 16) + (a << 24);
    }

    /**
     * Reading long from stream
     *
     * @param stream source stream
     * @return value
     * @throws java.io.IOException reading exception
     */
    public static long readLong(InputStream stream) throws IOException {
        long a = readUInt(stream);
        long b = readUInt(stream);

        return b + (a << 32);
    }

    /**
     * Reading double from stream
     *
     * @param stream source stream
     * @return value
     * @throws java.io.IOException reading exception
     */
    public static double readDouble(InputStream stream) throws IOException {
        return Double.longBitsToDouble(readLong(stream));
    }

    /**
     * Reading bytes from stream
     *
     * @param count  bytes count
     * @param stream source stream
     * @return readed bytes
     * @throws java.io.IOException reading exception
     */
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

    /**
     * Reading bytes from RandomAccessFile
     *
     * @param count bytes count
     * @param file  source file
     * @return readed bytes
     * @throws java.io.IOException reading exception
     */
    public static byte[] readBytes(int count, RandomAccessFile file) throws IOException {
        byte[] res = new byte[count];
        int offset = 0;
        while (offset < res.length) {
            int readed = file.read(res, offset, res.length - offset);
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

    /**
     * Reading bytes from stream
     *
     * @param count  bytes count
     * @param stream source stream
     * @throws java.io.IOException reading exception
     */
    public static void skipBytes(int count, InputStream stream) throws IOException {
        stream.skip(count);
    }

    /**
     * Reading bytes from stream
     *
     * @param count  bytes count
     * @param stream source stream
     * @throws java.io.IOException reading exception
     */
    public static void readBytes(byte[] buffer, int offset, int count, InputStream stream) throws IOException {
        int woffset = 0;
        while (woffset < count) {
            int readed = stream.read(buffer, woffset + offset, count - woffset);
            if (readed > 0) {
                woffset += readed;
            } else if (readed < 0) {
                throw new IOException();
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * Reading single byte from stream
     *
     * @param stream source stream
     * @return read byte
     * @throws java.io.IOException
     */
    public static byte readByte(InputStream stream) throws IOException {
        int res = stream.read();
        if (res < 0) {
            throw new IOException();
        }
        return (byte) res;
    }

    /**
     * Reading int from bytes array
     *
     * @param src source bytes
     * @return int value
     */
    public static int readInt(byte[] src) {
        return readInt(src, 0);
    }

    /**
     * Reading int from bytes array
     *
     * @param src    source bytes
     * @param offset offset in array
     * @return int value
     */
    public static int readInt(byte[] src, int offset) {
        int a = src[offset + 0] & 0xFF;
        int b = src[offset + 1] & 0xFF;
        int c = src[offset + 2] & 0xFF;
        int d = src[offset + 3] & 0xFF;

        return a + (b << 8) + (c << 16) + (d << 24);
    }

    /**
     * Reading uint from bytes array
     *
     * @param src source bytes
     * @return uint value
     */
    public static long readUInt(byte[] src) {
        return readUInt(src, 0);
    }

    /**
     * Reading uint from bytes array
     *
     * @param src    source bytes
     * @param offset offset in array
     * @return uint value
     */
    public static long readUInt(byte[] src, int offset) {
        long a = src[offset + 0] & 0xFF;
        long b = src[offset + 1] & 0xFF;
        long c = src[offset + 2] & 0xFF;
        long d = src[offset + 3] & 0xFF;

        return d + (c << 8) + (b << 16) + (a << 24);
    }

    /**
     * Reading long value from bytes array
     *
     * @param src    source bytes
     * @param offset offset in array
     * @return long value
     */
    public static long readLong(byte[] src, int offset) {
        long a = readUInt(src, offset);
        long b = readUInt(src, offset + 4);

        return (b & 0xFFFFFFFF) + ((a & 0xFFFFFFFF) << 32);
    }

    /**
     * Reading protobuf-like varint
     *
     * @param stream source stream
     * @return varint
     * @throws java.io.IOException
     */
    public static long readVarInt(InputStream stream) throws IOException {
        long value = 0;
        long i = 0;
        int b;

        do {
            b = readByte(stream);

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

    /**
     * Writing protobuf-like varint
     *
     * @param i      value
     * @param stream destination stream
     * @throws java.io.IOException
     */
    public static void writeVarInt(long i, OutputStream stream) throws IOException {
        while ((i & 0xffffffffffffff80l) != 0l) {
            stream.write((byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }

        stream.write((byte) (i & 0x7f));
    }

    /**
     * Calculating varint serialization size
     *
     * @param value varint
     * @return size in bytes
     */
    public static int varintSize(long value) {
        if (value < 0) {
            throw new RuntimeException("VarInt must be >= 0");
        }

        if (value <= 0x7f) {
            return 1;
        } else if (value <= 0x3fff) {
            return 2;
        } else if (value <= 0x1fffff) {
            return 3;
        } else if (value <= 0xfffffff) {
            return 4;
        } else if (value <= 0x7fffffff) {
            return 5;
        } else if (value <= 0x7ffffffffl) {
            return 6;
        } else if (value <= 0x3ffffffffffl) {
            return 7;
        } else if (value <= 0x1ffffffffffffl) {
            return 8;
        } else if (value <= 0xffffffffffffffl) {
            return 9;
        } else {
            return 10;
        }
    }

    public static int stringSize(String s) {
        int stringSize = 0;
        if (s != null) {
            try {
                stringSize = s.getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return varintSize(stringSize) + stringSize;
    }

    public static byte[] readProtoBytes(InputStream stream) throws IOException {
        int arrayLength = (int) readVarInt(stream);
        return readBytes(arrayLength, stream);
    }

    public static void writeProtoBytes(byte[] data, OutputStream stream) throws IOException {
        writeVarInt(data.length, stream);
        writeBytes(data, stream);
    }

    public static void writeProtoBytes(byte[] data, int offset, int len, OutputStream stream) throws IOException {
        writeVarInt(len, stream);
        writeBytes(data, offset, len, stream);
    }

    public static long[] readProtoLongs(InputStream stream) throws IOException {
        int len = (int) readVarInt(stream);
        long[] res = new long[len];
        for (int i = 0; i < res.length; i++) {
            res[i] = readLong(stream);
        }
        return res;
    }

    public static void writeProtoLongs(long[] values, OutputStream stream) throws IOException {
        writeVarInt(values.length, stream);
        for (long l : values) {
            writeLong(l, stream);
        }
    }

    public static String readProtoString(InputStream stream) throws IOException {
        byte[] data = readProtoBytes(stream);
        return new String(data, "UTF-8");
    }

    public static void writeProtoString(String value, OutputStream stream) throws IOException {
        byte[] data = value.getBytes("UTF-8");
        writeProtoBytes(data, stream);
    }

    public static boolean readProtoBool(InputStream stream) throws IOException {
        return readByte(stream) != 0;
    }

    public static void writeProtoBool(boolean v, OutputStream stream) throws IOException {
        writeByte(v ? 1 : 0, stream);
    }
}