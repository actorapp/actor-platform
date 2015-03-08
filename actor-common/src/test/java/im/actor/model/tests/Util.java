package im.actor.model.tests;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class Util {

    public static byte[] fromBinary(String src) {
        byte[] res = new byte[src.length() / 8];
        for (int i = 0; i < res.length; i++) {
            int val = 0;
            for (int j = 0; j < 8; j++) {
                char c = src.charAt(i * 8 + j);
                val |= (((int) (c - '0')) << (7 - j));
            }
            res[i] = (byte) val;
        }
        return res;
    }

    public static void assertType(Object ob, Class type) {
        if (ob.getClass() != type) {
            throw new AssertionError();
        }
    }

    public static void assertSize(Object ob, int size) {
        if (ob instanceof List) {
            if (((List) ob).size() != size) {
                throw new AssertionError();
            }
        } else {
            if (size != 1) {
                throw new AssertionError();
            }
        }
    }

    public static void assertContent(List list) {
        if (list.size() == 0) {
            return;
        }
        Object main = list.get(0);
        assertContent(list, list.get(0).getClass());
    }

    public static void assertContent(Object list, Class type) {
        if (list instanceof List) {
            assertContent((List) list, type);
        } else {
            if (list.getClass() != type) {
                throw new AssertionError();
            }
        }
    }

    public static void assertContent(List list, Class type) {
        if (list.size() == 0) {
            return;
        }

        for (Object o : list) {
            if (o.getClass() != type) {
                throw new AssertionError();
            }
        }
    }

    public static void assertEmptyConstructor(Class clazz) throws Exception {
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        constructors[0].setAccessible(true);
        constructors[0].newInstance((Object[]) null);
    }

    public static byte[] concat(byte[]... v) {
        int len = 0;
        for (byte[] aV : v) {
            len += aV.length;
        }
        byte[] res = new byte[len];
        int offset = 0;
        for (byte[] aV : v) {
            System.arraycopy(aV, 0, res, offset, aV.length);
            offset += aV.length;
        }
        return res;
    }
}
