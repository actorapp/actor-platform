package im.actor.model.droidkit.bser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class DataOutputTest {

    @Test
    public void testStream() throws Exception {
        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeByte(1);
            assertEquals(1, dataOutput.toByteArray().length);
        }

        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeInt(1);
            assertEquals(4, dataOutput.toByteArray().length);
        }

        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeLong(1);
            assertEquals(8, dataOutput.toByteArray().length);
        }

        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeVarInt(1);
            assertEquals(1, dataOutput.toByteArray().length);
        }

        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeVarInt(0xFF01);
            assertEquals(3, dataOutput.toByteArray().length);
        }

        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeLong(1);
            dataOutput.writeLong(1);
            dataOutput.writeLong(1);
            dataOutput.writeByte(1);
            dataOutput.writeInt(1);
            dataOutput.writeInt(1);
            assertEquals(33, dataOutput.toByteArray().length);
        }

        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoBool(true);
            dataOutput.writeProtoBool(false);
            assertEquals(2, dataOutput.toByteArray().length);
        }

        {
            String src = "asdasd asdad";
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoString(src);
            assertEquals(src.length() + 1, dataOutput.toByteArray().length);
        }

        {
            String src = "asdasd asdadas dasdasdasdadads";
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoString(src);
            assertEquals(src.length() + 1, dataOutput.toByteArray().length);
        }

        {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoLongs(new long[5]);
            assertEquals(1 + 5 * 8, dataOutput.toByteArray().length);
        }

        try {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoLongs(new long[2048 + 1]);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoBytes(new byte[0], 0, 2048 * 1024);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoBytes(new byte[0], -1, 0);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoBytes(new byte[0], 0, -1);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeProtoBytes(new byte[0], 0, 1);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

    }
}
