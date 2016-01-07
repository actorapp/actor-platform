package im.actor.crypto.primitives.padding;

import im.actor.crypto.primitives.Padding;

/**
 * TLS-like padding with padding-length value: xx 02 02 or xx 03 03 03
 *
 * @author Steve Kite (steve@actor.im)
 */
public class TLSPadding implements Padding {

    @Override
    public void padding(byte[] src, int offset, int length) {
        for (int i = 0; i < length; i++) {
            src[i + offset] = (byte) length;
        }
    }

    @Override
    public boolean validate(byte[] src, int offset, int length) {
        boolean isOk = true;
        for (int i = 0; i < length; i++) {
            isOk &= src[i + offset] == length;
        }
        return isOk;
    }
}