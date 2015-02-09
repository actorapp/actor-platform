package com.droidkit.bser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class BserWriter {
    private static final int TYPE_VARINT = 0;
    private static final int TYPE_32BIT = 5;
    private static final int TYPE_64BIT = 1;
    private static final int TYPE_LENGTH_DELIMITED = 2;

    private OutputStream stream;

    public BserWriter(OutputStream stream) {
        this.stream = stream;
    }


    public void writeBytes(int fieldNumber, byte[] value) throws IOException {
        writeBytesField(fieldNumber, value);
    }

    public void writeString(int fieldNumber, String value) throws IOException {
        writeBytesField(fieldNumber, value.getBytes());
    }

    public void writeBool(int fieldNumber, boolean value) throws IOException {
        writeVarIntField(fieldNumber, value ? 1 : 0);
    }

    public void writeInt(int fieldNumber, int value) throws IOException {
        writeVarIntField(fieldNumber, value);
    }

    public void writeIntFixed(int fieldNumber, int value) throws IOException {
        writeVar32Field(fieldNumber, value);
    }

    public void writeDouble(int fieldNumber, double value) throws IOException {
        writeVar64Field(fieldNumber, Double.doubleToLongBits(value));
    }

    public void writeLongFixed(int fieldNumber, long value) throws IOException {
        writeVar64Field(fieldNumber, Double.doubleToLongBits(value));
    }

    public void writeLong(int fieldNumber, long value) throws IOException {
        writeVarIntField(fieldNumber, value);
    }

    public void writeRepeatedLong(int fieldNumber, List<Long> values) throws IOException {
        for (long l : values) {
            writeVar64Field(fieldNumber, l);
        }
    }

    public void writeRepeatedInt(int fieldNumber, List<Integer> values) throws IOException {
        for (long l : values) {
            writeVar32Field(fieldNumber, l);
        }
    }

    public void writeRepeatedBool(int fieldNumber, List<Boolean> values) throws IOException {
        for (Boolean l : values) {
            writeBool(fieldNumber, l);
        }
    }

    public <T extends BserObject> void writeRepeatedObj(int fieldNumber, List<T> values) throws IOException {
        for (T l : values) {
            writeObject(fieldNumber, l);
        }
    }

    public void writeObject(int fieldNumber, BserObject value) throws IOException {
        writeTag(fieldNumber, TYPE_LENGTH_DELIMITED);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BserWriter writer = new BserWriter(outputStream);
        value.serialize(writer);
        writeBytes(outputStream.toByteArray());
    }

    public <T extends BserComposite> void writeComposite(BserCompositeFieldDescription<T> field, T val) throws IOException {
        field.writeObject(val, this);
    }

    private void writeTag(int fieldNumber, int wireType) throws IOException {
        byte tag = (byte) (fieldNumber << 3 | wireType);
        stream.write(tag);
    }

    private void writeVarIntField(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, TYPE_VARINT);
        writeVarInt(value);
    }

    private void writeBytesField(int fieldNumber, byte[] value) throws IOException {
        writeTag(fieldNumber, TYPE_LENGTH_DELIMITED);
        writeBytes(value);
    }

    private void writeVar64Field(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, TYPE_64BIT);
        writeLong(value);
    }

    private void writeVar32Field(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, TYPE_32BIT);
        writeInt(value);
    }


    private void writeVarInt(long value) throws IOException {
        while ((value & 0xffffffffffffff80l) != 0l) {
            stream.write((byte) ((value & 0x7f) | 0x80));
            value >>>= 7;
        }

        stream.write((byte) (value & 0x7f));
    }

    private void writeLong(long v) throws IOException {
        stream.write((byte) (v & 0xFF));
        stream.write((byte) ((v >> 8) & 0xFF));
        stream.write((byte) ((v >> 16) & 0xFF));
        stream.write((byte) ((v >> 24) & 0xFF));
        stream.write((byte) ((v >> 32) & 0xFF));
        stream.write((byte) ((v >> 40) & 0xFF));
        stream.write((byte) ((v >> 48) & 0xFF));
        stream.write((byte) ((v >> 56) & 0xFF));
    }

    private void writeInt(long v) throws IOException {
        stream.write((byte) (v & 0xFF));
        stream.write((byte) ((v >> 8) & 0xFF));
        stream.write((byte) ((v >> 16) & 0xFF));
        stream.write((byte) ((v >> 24) & 0xFF));
    }

    private void writeBytes(byte[] data) throws IOException {
        writeVarInt(data.length);
        stream.write(data);
    }
}
