package im.actor.model.droidkit.bser;

import im.actor.model.droidkit.bser.util.SparseArray;
import im.actor.model.tests.EmptyBserObj;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class BserValuesTest {
    @Test
    public void testLongBased() throws Exception {
        List<Long> sampleList = new ArrayList<Long>();
        sampleList.add(1L);
        List<byte[]> sampleList2 = new ArrayList<byte[]>();
        sampleList2.add(new byte[0]);
        BserValues values = new BserValues(buildArray(1, 1L, 3, new byte[0], 5, Double.doubleToLongBits(1.0), 6, 0L, 7, sampleList,
                8, sampleList2));

        assertEquals(1L, values.getLong(1));
        assertEquals(1L, values.optLong(1));
        assertEquals(1L, values.getLong(1, 2L));
        assertEquals(2L, values.getLong(2, 2L));

        assertEquals(1, values.getInt(1));
        assertEquals(1, values.optInt(1));
        assertEquals(1, values.getInt(1, 2));
        assertEquals(2, values.getInt(2, 2));

        assertEquals(true, values.getBool(1));
        assertEquals(true, values.optBool(1));
        assertEquals(true, values.getBool(1, false));
        assertEquals(false, values.getBool(2, false));

        assertEquals(false, values.getBool(6));
        assertEquals(false, values.optBool(6));
        assertEquals(false, values.getBool(6, true));
        assertEquals(true, values.getBool(2, true));

        assertEquals(1.0, values.getDouble(5), 0.01);
        assertEquals(1.0, values.optDouble(5), 0.01);
        assertEquals(1.0, values.getDouble(5, 1.0), 0.01);
        assertEquals(2.0, values.getDouble(2, 2.0), 0.01);

        try {
            values.getLong(4);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getLong(3);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getBytes(1);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getBytes(4);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getObj(4, new EmptyBserObj());
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getObj(2, new EmptyBserObj());
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedLong(3);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedInt(3);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedLong(8);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedInt(8);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedBytes(1);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedString(1);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedObj(7, repeated(1));
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedBytes(7);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedString(7);
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        try {
            values.getRepeatedObj(7, repeated(0));
            throw new AssertionError();
        } catch (Exception e) {
            // It is OK
        }

        assertEquals(0, values.getBytes(3).length);
        assertEquals(0, values.optBytes(3).length);
        assertEquals(0, values.getBytes(3, new byte[1]).length);
        assertEquals(1, values.getBytes(2, new byte[1]).length);

        assertEquals(0, values.getString(3).length());
        assertEquals(0, values.optString(3).length());
        assertEquals(0, values.getString(3, "?").length());
        assertEquals(1, values.getString(2, "?").length());

        assertNotNull(values.getObj(3, new EmptyBserObj()));
        assertNotNull(values.optObj(3, new EmptyBserObj()));
        assertNull(values.optObj(2, new EmptyBserObj()));

        assertEquals(0, values.getRepeatedCount(2));
        assertEquals(1, values.getRepeatedCount(1));
        assertEquals(1, values.getRepeatedCount(7));

        assertEquals(1, values.getRepeatedLong(7).size());
        assertEquals(1, values.getRepeatedInt(7).size());

        assertEquals(1, values.getRepeatedLong(1).size());
        assertEquals(1, values.getRepeatedInt(1).size());

        assertEquals(0, values.getRepeatedLong(2).size());
        assertEquals(0, values.getRepeatedInt(2).size());

        assertEquals(0, values.getRepeatedBytes(2).size());
        assertEquals(0, values.getRepeatedString(2).size());

        assertEquals(1, values.getRepeatedBytes(8).size());
        assertEquals(1, values.getRepeatedString(8).size());
        assertEquals(1, values.getRepeatedObj(8,repeated(1)).size());

        assertEquals(1, values.getRepeatedBytes(3).size());
        assertEquals(1, values.getRepeatedString(3).size());
        assertEquals(1, values.getRepeatedObj(3, repeated(1)).size());
    }

    private ArrayList<EmptyBserObj> repeated(int size) {
        ArrayList<EmptyBserObj> res = new ArrayList<EmptyBserObj>();
        for (int i = 0; i < size; i++) {
            res.add(new EmptyBserObj());
        }
        return res;
    }

    private SparseArray<Object> buildArray(Object... params) {
        SparseArray<Object> res = new SparseArray<Object>();

        for (int i = 0; i < params.length / 2; i++) {
            res.put((Integer) params[i * 2], params[i * 2 + 1]);
        }

        return res;
    }
}
