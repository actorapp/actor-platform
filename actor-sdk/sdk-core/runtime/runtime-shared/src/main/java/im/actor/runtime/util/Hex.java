package im.actor.runtime.util;

public class Hex {

    final protected static char[] HEXES_SMALL = "0123456789abcdef".toCharArray();

    private static final String HEXES = "0123456789ABCDEF";

    public static String toHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }


    /**
     * Calculating lowcase hex string
     *
     * @param bytes data for hex
     * @return hex string
     */
    public static String hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEXES_SMALL[v >>> 4];
            hexChars[j * 2 + 1] = HEXES_SMALL[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static int fromHexShort(char a) {
        if (a >= '0' && a <= '9') {
            return a - '0';
        }
        if (a >= 'a' && a <= 'f') {
            return 10 + (a - 'a');
        }

        throw new RuntimeException();
    }

    public static byte[] fromHex(String hex) {
        byte[] res = new byte[hex.length() / 2];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) ((fromHexShort(hex.charAt(i * 2)) << 4) + fromHexShort(hex.charAt(i * 2 + 1)));
        }
        return res;
    }
}
