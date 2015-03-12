package im.actor.model.droidkit.bser;

import java.io.IOException;
import java.util.List;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class BserWriter {

    private DataOutput stream;

    public BserWriter(DataOutput stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream can not be null");
        }

        this.stream = stream;
    }

    public void writeBytes(int fieldNumber, byte[] value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
        if (value.length > Limits.MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException("Unable to write more than 1 MB");
        }
        writeBytesField(fieldNumber, value);
    }

    public void writeString(int fieldNumber, String value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
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
        if (values == null) {
            throw new IllegalArgumentException("Values can not be null");
        }
        if (values.size() > Limits.MAX_PROTO_REPEATED) {
            throw new IllegalArgumentException("Too many values");
        }
        for (Long l : values) {
            if (l == null) {
                throw new IllegalArgumentException("Value can not be null");
            }
            writeVar64Field(fieldNumber, l);
        }
    }

    public void writeRepeatedInt(int fieldNumber, List<Integer> values) throws IOException {
        if (values == null) {
            throw new IllegalArgumentException("Values can not be null");
        }
        if (values.size() > Limits.MAX_PROTO_REPEATED) {
            throw new IllegalArgumentException("Too many values");
        }
        for (Integer l : values) {
            if (l == null) {
                throw new IllegalArgumentException("Value can not be null");
            }
            writeVar32Field(fieldNumber, l);
        }
    }

    public void writeRepeatedBool(int fieldNumber, List<Boolean> values) throws IOException {
        if (values == null) {
            throw new IllegalArgumentException("Values can not be null");
        }
        if (values.size() > Limits.MAX_PROTO_REPEATED) {
            throw new IllegalArgumentException("Too many values");
        }
        for (Boolean l : values) {
            if (l == null) {
                throw new IllegalArgumentException("Value can not be null");
            }
            writeBool(fieldNumber, l);
        }
    }

    public <T extends BserObject> void writeRepeatedObj(int fieldNumber, List<T> values) throws IOException {
        if (values == null) {
            throw new IllegalArgumentException("Values can not be null");
        }
        if (values.size() > Limits.MAX_PROTO_REPEATED) {
            throw new IllegalArgumentException("Too many values");
        }
        for (T l : values) {
            if (l == null) {
                throw new IllegalArgumentException("Value can not be null");
            }
            writeObject(fieldNumber, l);
        }
    }

    public void writeObject(int fieldNumber, BserObject value) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null");
        }

        writeTag(fieldNumber, WireTypes.TYPE_LENGTH_DELIMITED);
        DataOutput outputStream = new DataOutput();
        BserWriter writer = new BserWriter(outputStream);
        value.serialize(writer);
        writeBytes(outputStream.toByteArray());
    }

    public void writeRaw(byte[] raw) throws IOException {
        if (raw == null) {
            throw new IllegalArgumentException("Raw can not be null");
        }

        stream.writeBytes(raw, 0, raw.length);
    }

    private void writeTag(int fieldNumber, int wireType) throws IOException {
        if (fieldNumber <= 0) {
            throw new IllegalArgumentException("fieldNumber can't be less or eq to zero");
        }

        long tag = ((long) (fieldNumber << 3) | wireType);
        stream.writeVarInt(tag);
    }

    private void writeVarIntField(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, WireTypes.TYPE_VARINT);
        writeVarInt(value);
    }

    private void writeBytesField(int fieldNumber, byte[] value) throws IOException {
        writeTag(fieldNumber, WireTypes.TYPE_LENGTH_DELIMITED);
        writeBytes(value);
    }

    private void writeVar64Field(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, WireTypes.TYPE_64BIT);
        writeLong(value);
    }

    private void writeVar32Field(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, WireTypes.TYPE_32BIT);
        writeInt(value);
    }


    private void writeVarInt(long value) throws IOException {
        stream.writeVarInt(value);
    }

    private void writeLong(long v) throws IOException {
        stream.writeLong(v);
    }

    private void writeInt(long v) throws IOException {
        stream.writeInt((int) v);
    }

    private void writeBytes(byte[] data) throws IOException {
        stream.writeProtoBytes(data, 0, data.length);
    }
}
