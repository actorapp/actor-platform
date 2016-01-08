package im.actor.crypto.primitives.padding;

import im.actor.crypto.primitives.Padding;

/**
 * Padding with zero bytes
 *
 * @author Steve Kite (steve@actor.im)
 */
public class ZeroPadding implements Padding {

    @Override
    public void padding(byte[] src, int offset, int length) {
        for (int i = 0; i < length; i++) {
            src[i + offset] = 0;
        }
    }

    @Override
    public boolean validate(byte[] src, int offset, int length) {
        boolean isOk = true;
        for (int i = 0; i < length; i++) {
            isOk &= src[i + offset] == 0;
        }
        return isOk;
    }
}
