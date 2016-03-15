package im.actor.runtime.crypto.primitives.padding;

import im.actor.runtime.crypto.primitives.Padding;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

/**
 * TLS-like padding with padding-length value: xx 02 02 or xx 03 03 03
 *
 * @author Steve Kite (steve@actor.im)
 */
public class PKCS7Padding implements Padding {

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