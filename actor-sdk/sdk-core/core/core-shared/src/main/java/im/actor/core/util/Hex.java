package im.actor.core.util;

public class Hex {
    public static byte[] fromHex(String hex) {
        byte[] res = new byte[hex.length() / 2];
        for (int j = 0; j < hex.length() / 2; j++) {
            String dg = hex.charAt(j * 2) + "" + hex.charAt(j * 2 + 1);
            res[j] = (byte) Integer.parseInt(dg, 16);
        }
        return res;
    }
}
