package im.actor.core.modules.encryption.session;

import im.actor.core.modules.encryption.entity.PrivateKey;
import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.runtime.crypto.ratchet.RatchetMasterSecret;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;

public class EncryptedSession {
    private PrivateKey ownIdentityKey;
    private PrivateKey ownPreKey;
    private PublicKey theirIdentityKey;
    private PublicKey theirPreKey;
    private int peerKeyGroupId;
    private byte[] masterKey;

    public EncryptedSession(PrivateKey ownIdentityKey, PrivateKey ownPreKey, PublicKey theirIdentityKey, PublicKey theirPreKey, int peerKeyGroupId) {
        this.ownIdentityKey = ownIdentityKey;
        this.ownPreKey = ownPreKey;
        this.theirIdentityKey = theirIdentityKey;
        this.theirPreKey = theirPreKey;
        this.peerKeyGroupId = peerKeyGroupId;
        this.masterKey = RatchetMasterSecret.calculateMasterSecret(
                new RatchetPrivateKey(ownIdentityKey.getKey()),
                new RatchetPrivateKey(ownPreKey.getKey()),
                new RatchetPublicKey(theirIdentityKey.getPublicKey()),
                new RatchetPublicKey(theirPreKey.getPublicKey()));
    }

    public PrivateKey getOwnIdentityKey() {
        return ownIdentityKey;
    }

    public PrivateKey getOwnPreKey() {
        return ownPreKey;
    }

    public PublicKey getTheirIdentityKey() {
        return theirIdentityKey;
    }

    public PublicKey getTheirPreKey() {
        return theirPreKey;
    }

    public int getPeerKeyGroupId() {
        return peerKeyGroupId;
    }

    public byte[] getMasterKey() {
        return masterKey;
    }
}