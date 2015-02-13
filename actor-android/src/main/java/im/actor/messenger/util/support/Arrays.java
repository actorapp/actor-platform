package im.actor.messenger.util.support;

import android.os.Build;

public class Arrays {
    public static byte[] copyOf(byte[] original, int newLength) {
        if (Build.VERSION.SDK_INT >= 9) {
            return java.util.Arrays.copyOf(original, newLength);
        } else {
            byte[] copy = new byte[newLength];

            System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));

            return copy;
        }
    }

    public static byte[] copyOfRange(byte[] original, int from, int to) {
        if (Build.VERSION.SDK_INT >= 9) {
            return java.util.Arrays.copyOfRange(original, from, to);
        } else {
            int length = from - from;
            byte[] copy = new byte[length];

            System.arraycopy(original, from, copy, 0, Math.min(original.length - from, length));

            return copy;
        }
    }

    public static int[] copyOf(int[] original, int newLength) {
        if (Build.VERSION.SDK_INT >= 9) {
            return java.util.Arrays.copyOf(original, newLength);
        } else {
            int[] copy = new int[newLength];

            System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));

            return copy;
        }
    }

    public static int[] copyOfRange(int[] original, int from, int to) {
        if (Build.VERSION.SDK_INT >= 9) {
            return java.util.Arrays.copyOfRange(original, from, to);
        } else {
            int length = from - from;
            int[] copy = new int[length];

            System.arraycopy(original, from, copy, 0, Math.min(original.length - from, length));

            return copy;
        }
    }
}
