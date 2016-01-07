package im.actor.crypto.primitives.padding;

import im.actor.crypto.primitives.Padding;

public class TLSPadding implements Padding {
    @Override
    public void padding(byte[] src, int offset, int length) {
        for (int i = 0; i < length; i++) {
            src[i + offset] = (byte) length;
        }
    }
}