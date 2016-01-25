package im.actor.core.modules.encryption.entity;

public class SessionKeys {

    private final OwnPrivateKey ownIdentityKey;
    private final OwnPrivateKey ownPreKey;
    private final UserPublicKey theirIdentityKey;
    private final UserPublicKey theirPreKey;

    public SessionKeys(OwnPrivateKey ownIdentityKey, OwnPrivateKey ownPreKey, UserPublicKey theirIdentityKey, UserPublicKey theirPreKey) {
        this.ownIdentityKey = ownIdentityKey;
        this.ownPreKey = ownPreKey;
        this.theirIdentityKey = theirIdentityKey;
        this.theirPreKey = theirPreKey;
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
}
