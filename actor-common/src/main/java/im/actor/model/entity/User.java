package im.actor.model.entity;

import im.actor.model.mvvm.KeyValueItem;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class User implements KeyValueItem {
    private final int uid;
    private final long accessHash;
    private final String name;
    private final String localName;
    private final Avatar avatar;

    public User(int uid, long accessHash, String name, String localName,
                Avatar avatar) {
        this.uid = uid;
        this.accessHash = accessHash;
        this.name = name;
        this.localName = localName;
        this.avatar = avatar;
    }

    public Peer peer() {
        return new Peer(PeerType.PRIVATE, uid);
    }

    public int getUid() {
        return uid;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getServerName() {
        return name;
    }

    public String getLocalName() {
        return localName;
    }

    public String getName() {
        if (localName == null) {
            return name;
        } else {
            return localName;
        }
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public User editName(String name) {
        return new User(uid, accessHash, name, localName, avatar);
    }

    public User editLocalName(String localName) {
        return new User(uid, accessHash, name, localName, avatar);
    }

    public User editAvatar(Avatar avatar) {
        return new User(uid, accessHash, name, localName, avatar);
    }

    @Override
    public long getEngineId() {
        return getUid();
    }
}