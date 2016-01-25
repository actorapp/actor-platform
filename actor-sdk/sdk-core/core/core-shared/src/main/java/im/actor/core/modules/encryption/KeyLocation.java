package im.actor.core.modules.encryption;

public class KeyLocation {

    private int uid;
    private int keyGroup;
    private long keyId;

    public KeyLocation(int uid, int keyGroup, long keyId) {
        this.uid = uid;
        this.keyGroup = keyGroup;
        this.keyId = keyId;
    }

    public int getUid() {
        return uid;
    }

    public int getKeyGroup() {
        return keyGroup;
    }

    public long getKeyId() {
        return keyId;
    }
}
