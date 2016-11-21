package im.actor.core.network;

import java.io.IOException;

import im.actor.runtime.Crypto;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.crypto.Digest;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.util.Hex;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class TrustedKey extends BserObject {

    private boolean isLoaded = false;
    private String hexKey;
    private long keyId;
    private byte[] key;

    public TrustedKey() {
    }

    public TrustedKey(String hexKey) {
        this.hexKey = hexKey;
    }

    public long getKeyId() {
        load();
        return keyId;
    }

    public byte[] getKey() {
        load();
        return key;
    }

    private synchronized void load() {
        if (!isLoaded) {
            isLoaded = true;

            this.key = Hex.fromHex(hexKey);

            byte[] hash = new byte[32];
            Digest sha256 = Crypto.createSHA256();
            sha256.update(key, 0, key.length);
            sha256.doFinal(hash, 0);
            this.keyId = ByteStrings.bytesToLong(hash);
        }
    }

    public static TrustedKey fromBytes(byte[] data) throws IOException {
        return Bser.parse(new TrustedKey(), data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrustedKey that = (TrustedKey) o;

        return hexKey.equals(that.hexKey);

    }

    @Override
    public void parse(BserValues values) throws IOException {
        isLoaded = values.getBool(1);
        hexKey = values.getString(2);
        keyId = values.getLong(3);
        key = values.getBytes(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBool(1, isLoaded);
        writer.writeString(2, hexKey);
        writer.writeLong(3, keyId);
        writer.writeBytes(4, key);
    }
}
