package im.actor.crypto.box;

import im.actor.crypto.primitives.util.ByteStrings;

/**
 * Actor Box key. Used for encryption and decryption of Actor Boxes.
 *
 * @author Steve Kite (steve@actor.im)
 */
public class ActorBoxKey {

    private byte[] keyAES;
    private byte[] macAES;
    private byte[] keyKuz;
    private byte[] macKuz;

    public ActorBoxKey(byte[] keyAES, byte[] macAES, byte[] keyKuz, byte[] macKuz) {
        this.keyAES = keyAES;
        this.macAES = macAES;
        this.keyKuz = keyKuz;
        this.macKuz = macKuz;
    }

    public ActorBoxKey(byte[] key) {
        this(ByteStrings.substring(key, 0, 32),
                ByteStrings.substring(key, 32, 32),
                ByteStrings.substring(key, 64, 32),
                ByteStrings.substring(key, 96, 32));
    }

    public byte[] getKeyAES() {
        return keyAES;
    }

    public byte[] getMacAES() {
        return macAES;
    }

    public byte[] getKeyKuz() {
        return keyKuz;
    }

    public byte[] getMacKuz() {
        return macKuz;
    }

    public byte[] toByteArray() {
        return ByteStrings.merge(keyAES, macAES, keyKuz, macKuz);
    }
}