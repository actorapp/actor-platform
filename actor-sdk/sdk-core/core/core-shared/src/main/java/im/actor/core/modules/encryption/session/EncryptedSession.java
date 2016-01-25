package im.actor.core.modules.encryption.session;

import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.runtime.crypto.ratchet.RatchetMasterSecret;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;

public class EncryptedSession {
    private OwnPrivateKey ownIdentityKey;
    private OwnPrivateKey ownPreKey;
    private UserPublicKey theirIdentityKey;
    private UserPublicKey theirPreKey;
    private int peerKeyGroupId;
    private byte[] masterKey;

    public EncryptedSession(OwnPrivateKey ownIdentityKey, OwnPrivateKey ownPreKey, UserPublicKey theirIdentityKey, UserPublicKey theirPreKey, int peerKeyGroupId) {
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

    public OwnPrivateKey getOwnIdentityKey() {
        return ownIdentityKey;
    }

    public OwnPrivateKey getOwnPreKey() {
        return ownPreKey;
    }

    public UserPublicKey getTheirIdentityKey() {
        return theirIdentityKey;
    }

    public UserPublicKey getTheirPreKey() {
        return theirPreKey;
    }

    public int getPeerKeyGroupId() {
        return peerKeyGroupId;
    }

    public byte[] getMasterKey() {
        return masterKey;
    }
}