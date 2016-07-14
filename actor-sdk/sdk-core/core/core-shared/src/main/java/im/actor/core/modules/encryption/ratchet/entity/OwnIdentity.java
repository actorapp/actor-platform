package im.actor.core.modules.encryption.ratchet.entity;

import im.actor.runtime.actors.ask.AskResult;

public class OwnIdentity extends AskResult {

    private int keyGroup;
    private PrivateKey identityKey;

    public OwnIdentity(int keyGroup, PrivateKey identityKey) {
        this.keyGroup = keyGroup;
        this.identityKey = identityKey;
    }

    public int getKeyGroup() {
        return keyGroup;
    }

    public PrivateKey getIdentityKey() {
        return identityKey;
    }
}
