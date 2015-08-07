package im.actor.model.droidkit.bser;

import im.actor.model.tests.MockSerialization;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import im.actor.model.droidkit.bser.util.SparseArray;

import static im.actor.model.tests.Util.*;
import static org.junit.Assert.*;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class UtilsTest {

    @Test
    public void testSimpleTest() throws Exception {
        SparseArray<Object> values = DEFAULT.buildArray();
        assertEquals(1, values.size());
        assertTrue(values.get(1) instanceof List);
        assertContent(values.get(1), Long.class);
        assertSize(values.get(1), 5);
    }

    @Test
    public void testUtils() throws Exception {

        // CryptoUtils.init(new BouncyCastleProvider());

        assertEquals(1, Utils.bytesToInt(Utils.intToBytes(1)));
        assertEquals(0, Utils.bytesToInt(Utils.intToBytes(0)));
        assertEquals(-1, Utils.bytesToInt(Utils.intToBytes(-1)));
        assertEquals(Integer.MAX_VALUE, Utils.bytesToInt(Utils.intToBytes(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, Utils.bytesToInt(Utils.intToBytes(Integer.MIN_VALUE)));
        assertEquals(Integer.MIN_VALUE / 2, Utils.bytesToInt(Utils.intToBytes(Integer.MIN_VALUE / 2)));
        assertEquals(Integer.MAX_VALUE / 2, Utils.bytesToInt(Utils.intToBytes(Integer.MAX_VALUE / 2)));


        assertEquals(1, Utils.bytesToLong(Utils.longToBytes(1)));
        assertEquals(0, Utils.bytesToLong(Utils.longToBytes(0)));
        assertEquals(-1, Utils.bytesToLong(Utils.longToBytes(-1)));
        assertEquals(Long.MAX_VALUE, Utils.bytesToLong(Utils.longToBytes(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, Utils.bytesToLong(Utils.longToBytes(Long.MIN_VALUE)));
        assertEquals(Long.MIN_VALUE / 2, Utils.bytesToLong(Utils.longToBytes(Long.MIN_VALUE / 2)));
        assertEquals(Long.MAX_VALUE / 2, Utils.bytesToLong(Utils.longToBytes(Long.MAX_VALUE / 2)));

        assertEquals("null", Utils.byteArrayToString(null));
        assertEquals("null", Utils.byteArrayToStringCompact(null));

        // Only lowcase
        assertEquals("05a2b1", Utils.byteArrayToString(new byte[]{0x05, (byte) 0xA2, (byte) 0xB1}));
        assertNotEquals("05A2B1", Utils.byteArrayToString(new byte[]{0x05, (byte) 0xA2, (byte) 0xB1}));

        // Check MD5 hash
        // assertEquals("416776ba8261e73b0e3f46bb74f45f25", Utils.byteArrayToStringCompact("sadk jaskdjasdkadsjasd".getBytes("ASCII")));

        assertNull(Utils.convertString(null));
        assertNotNull(Utils.convertString(new byte[0]));

        Utils.convertInt(Integer.MAX_VALUE);
        Utils.convertInt(Integer.MIN_VALUE);
        Utils.convertInt(0);

        try {
            Utils.convertInt(Long.MAX_VALUE);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            Utils.convertInt(Long.MIN_VALUE);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }
    }


    private final MockSerialization DEFAULT = new MockSerialization() {
        @Override
        protected void perform(BserWriter writer) throws IOException {
            writer.writeInt(1, 1);
            writer.writeInt(1, 2);
            writer.writeInt(1, 3);
            writer.writeInt(1, 4);
            writer.writeInt(1, 5);
        }
    };
}