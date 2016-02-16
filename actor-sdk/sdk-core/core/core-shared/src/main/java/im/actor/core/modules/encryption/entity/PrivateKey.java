package im.actor.core.modules.encryption.entity;

import java.io.IOException;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiEncryptionKeySignature;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.Crypto;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.Curve25519KeyPair;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetKeySignature;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.function.Supplier;

public class PrivateKey extends BserObject {

    public static final Predicate<PrivateKey> PRE_KEY_EQUALS(final byte[] publicKey) {
        return new Predicate<PrivateKey>() {
            @Override
            public boolean apply(PrivateKey privateKey) {
                return ByteStrings.isEquals(publicKey, privateKey.getPublicKey());
            }
        };
    }

    public static final Predicate<PrivateKey> PRE_KEY_EQUALS_ID(final long id) {
        return new Predicate<PrivateKey>() {
            @Override
            public boolean apply(PrivateKey privateKey) {
                return privateKey.getKeyId() == id;
            }
        };
    }

    public static final Function<PrivateKey, ApiEncryptionKey> TO_API = new Function<PrivateKey, ApiEncryptionKey>() {
        @Override
        public ApiEncryptionKey apply(PrivateKey privateKey) {
            return privateKey.toApiKey();
        }
    };

    public static final Function<PrivateKey, ApiEncryptionKeySignature> SIGN(final PrivateKey identity) {
        return new Function<PrivateKey, ApiEncryptionKeySignature>() {
            @Override
            public ApiEncryptionKeySignature apply(PrivateKey privateKey) {
                byte[] signature = Curve25519.calculateSignature(Crypto.randomBytes(64), identity.getKey(),
                        RatchetKeySignature.hashForSignature(privateKey.getKeyId(),
                                privateKey.getKeyAlg(), privateKey.getPublicKey()));
                return new ApiEncryptionKeySignature(privateKey.getKeyId(), "Ed25519", signature);
            }
        };
    }

    public static final Supplier<PrivateKey> GENERATOR = new Supplier<PrivateKey>() {
        @Override
        public PrivateKey get() {
            Curve25519KeyPair keyPair = Curve25519.keyGen(Crypto.randomBytes(64));
            return new PrivateKey(
                    RandomUtils.nextRid(),
                    "curve25519",
                    keyPair.getPrivateKey(),
                    keyPair.getPublicKey(),
                    false);
        }
    };

    public static final Predicate<PrivateKey> NOT_UPLOADED = new Predicate<PrivateKey>() {
        @Override
        public boolean apply(PrivateKey privateKey) {
            return !privateKey.isUploaded();
        }
    };

    public static final Predicate<PrivateKey> UPLOADED = new Predicate<PrivateKey>() {
        @Override
        public boolean apply(PrivateKey privateKey) {
            return privateKey.isUploaded();
        }
    };

    private long keyId;
    private String keyAlg;
    private byte[] key;
    private byte[] publicKey;
    private Boolean isUploaded;
    private boolean wasRegenerated = false;

    public PrivateKey(long keyId, String keyAlg, byte[] privateKey, byte[] publicKey, Boolean isUploaded) {
        this.keyId = keyId;
        this.keyAlg = keyAlg;
        this.key = privateKey;
        this.isUploaded = isUploaded;
        this.publicKey = publicKey;
    }

    public PrivateKey(long keyId, String keyAlg, byte[] privateKey, byte[] publicKey) {
        this(keyId, keyAlg, privateKey, publicKey, null);
    }

    public PrivateKey(byte[] data) throws IOException {
        load(data);
    }

    public long getKeyId() {
        return keyId;
    }

    public String getKeyAlg() {
        return keyAlg;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public boolean isUploaded() {
        if (isUploaded == null) {
            return false;
        }
        return isUploaded;
    }

    public boolean isWasRegenerated() {
        return wasRegenerated;
    }

    public ApiEncryptionKey toApiKey() {
        return new ApiEncryptionKey(keyId, keyAlg, publicKey, null);
    }

    public PrivateKey markAsUploaded() {
        return new PrivateKey(getKeyId(), getKeyAlg(), getKey(), getPublicKey(), true);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        keyId = values.getLong(1);
        keyAlg = values.getString(2);
        key = values.getBytes(3);
        isUploaded = values.optBool(4);
        publicKey = values.optBytes(5);
        if (publicKey == null) {
            wasRegenerated = true;
            publicKey = Curve25519.keyGenPublic(key);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, keyId);
        writer.writeString(2, keyAlg);
        writer.writeBytes(3, key);
        writer.writeBytes(5, publicKey);
        if (isUploaded != null) {
            writer.writeBool(4, isUploaded);
        }
    }
}
