package im.actor.model.droidkit.bser;

import org.junit.Test;

import java.util.Arrays;

import static im.actor.model.tests.Util.concat;
import static im.actor.model.tests.Util.fromBinary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class DataInputTest {

    @Test
    public void testVarInt() throws Exception {
        // From official documentation
        {
            DataInput dataInput = new DataInput(fromBinary("00000001"));
            assertEquals(1, dataInput.readVarInt());
        }

        {
            DataInput dataInput = new DataInput(fromBinary("1010110000000010"));
            assertEquals(300, dataInput.readVarInt());
        }

        {
            DataInput dataInput = new DataInput(fromBinary("1010110000000010"));
            assertEquals(300, dataInput.readVarInt32());
        }

        // Incomplete VarInt
        try {
            byte[] data = new byte[]{(byte) 0xFF, (byte) 0x80};
            DataInput dataInput = new DataInput(data);
            dataInput.readVarInt();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        // Too long VarInt
        try {
            byte[] data = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x80};
            DataInput dataInput = new DataInput(data);
            dataInput.readVarInt();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        // Too long VarInt 32
        try {
            byte[] data = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    0x00};
            DataInput dataInput = new DataInput(data);
            dataInput.readVarInt32();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }
    }

    @Test
    public void testInt() throws Exception {

        // Int

        try {
            DataInput dataInput = new DataInput(new byte[]{0, 1, 2});
            dataInput.readInt();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        {
            DataInput dataInput = new DataInput(new byte[]{0, 1, 2, 3});
            dataInput.readInt();
        }

        // UInt

        try {
            DataInput dataInput = new DataInput(new byte[]{0, 1, 2});
            dataInput.readUInt();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        {
            DataInput dataInput = new DataInput(new byte[]{0, 1, 2, 3});
            dataInput.readUInt();
        }

        // Long

        try {
            DataInput dataInput = new DataInput(new byte[]{0, 1, 2, 3, 4, 5, 6});
            dataInput.readLong();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        {
            DataInput dataInput = new DataInput(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
            dataInput.readLong();
        }

        // Byte

        {
            DataInput dataInput = new DataInput(new byte[]{(byte) 0xA4});
            assertEquals(0xA4, dataInput.readByte());
        }

        try {
            DataInput dataInput = new DataInput(new byte[0]);
            dataInput.readByte();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        // Bytes

        {
            byte[] src = new byte[]{(byte) 0xA4, (byte) 0xA6, (byte) 0xB4, (byte) 0xC1, (byte) 0x04};
            DataInput dataInput = new DataInput(src);
            assertTrue(Arrays.equals(src, dataInput.readBytes(src.length)));
        }

        try {
            DataInput dataInput = new DataInput(new byte[9]);
            dataInput.readBytes(10);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[9]);
            dataInput.readBytes(2048 * 1024);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[9]);
            dataInput.readBytes(-10);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }


    }

    @Test
    public void testProtoRead() throws Exception {
        // Proto Bytes

        try {
            DataInput dataInput = new DataInput(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    0});
            dataInput.readProtoBytes();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0});
            dataInput.readProtoBytes();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        {
            DataInput dataInput = new DataInput(new byte[]{(byte) 1, (byte) 0xFF});
            dataInput.readProtoBytes();
        }

        // Proto Longs

        try {
            DataInput dataInput = new DataInput(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    0});
            dataInput.readProtoLongs();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0});
            dataInput.readProtoLongs();
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        {
            DataInput dataInput = new DataInput(new byte[]{(byte) 1, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
            dataInput.readProtoLongs();
        }

        // Proto Bool
        {
            DataInput dataInput = new DataInput(new byte[]{(byte) 1});
            assertTrue(dataInput.readProtoBool());
        }

        {
            DataInput dataInput = new DataInput(new byte[]{(byte) 0});
            assertFalse(dataInput.readProtoBool());
        }

        // Proto String
        {
            String str = "asda sdasasd";
            DataInput dataInput = new DataInput(concat(new byte[]{(byte) str.length()}, str.getBytes("ASCII")));
            assertEquals(str, dataInput.readProtoString());
        }
    }

    @Test
    public void testStream() throws Exception {
        try {
            DataInput dataInput = new DataInput(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(null, 0, 0);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[1], -1, 0);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[1], 0, -1);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[1], 0, 10);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // It is OK
        }

        try {
            DataInput dataInput = new DataInput(new byte[1], 10, 1);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // It is OK
        }
    }
}
