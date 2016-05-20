package im.actor.server.api.rpc.service.files;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

public class CalcSignature {

    /**
     * Returns url with calculated signature for specific file with specific file builder parameters
     * @param baseUri base uri from file url builder
     * @param seed seed provided by file url builder. Must be included in url
     * @param signatureSecret secret used to sign request
     * @param fileId id of file to download
     * @param fileAccessHash access hash of file to download
     * @return file url
     */
    public static String fileBuilderUrl(String baseUri, String seed, byte[] signatureSecret, long fileId, long fileAccessHash) {
        byte[] seedBytes = decodeHex(seed.toCharArray());
        byte[] fileIdBytes = getBytes(fileId);
        byte[] accessHashBytes = getBytes(fileAccessHash);

        byte[] bytesToSign = ArrayUtils.addAll(ArrayUtils.addAll(seedBytes, fileIdBytes), accessHashBytes);

        String signPart = HmacUtils.hmacSha256Hex(signatureSecret, bytesToSign);

        String signature = seed + "_" + signPart;

        return baseUri + "/" + fileId + "?signature=" + signature;
    }

    private static byte[] decodeHex(final char[] data) {

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    private static int toDigit(final char ch, final int index) {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    private static byte[] getBytes(long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

}
