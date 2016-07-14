package im.actor.core.modules.encryption.ratchet.entity;

import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiEncyptedBoxKey;

public class EncryptedUserKeys {

    private int uid;
    private List<ApiEncyptedBoxKey> boxKeys;
    private HashSet<Integer> ignoredKeys;

    public EncryptedUserKeys(int uid, List<ApiEncyptedBoxKey> boxKeys, HashSet<Integer> ignoredKeys) {
        this.uid = uid;
        this.boxKeys = boxKeys;
        this.ignoredKeys = ignoredKeys;
    }

    public int getUid() {
        return uid;
    }

    public List<ApiEncyptedBoxKey> getBoxKeys() {
        return boxKeys;
    }

    public HashSet<Integer> getIgnoredKeys() {
        return ignoredKeys;
    }
}
