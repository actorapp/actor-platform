package im.actor.core.modules.encryption.entity;

import im.actor.core.modules.encryption.entity.PrivateKey;
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
