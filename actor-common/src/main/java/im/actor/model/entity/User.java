package im.actor.model.entity;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class User {
    private final int uid;
    private final long accessHash;
    private final String name;
    private final String localName;

    public User(int uid, long accessHash, String name, String localName) {
        this.uid = uid;
        this.accessHash = accessHash;
        this.name = name;
        this.localName = localName;
    }

    public int getUid() {
        return uid;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getName() {
        return name;
    }

    public String getLocalName() {
        return localName;
    }

    public User editName(String name) {
        return new User(uid, accessHash, name, localName);
    }

    public User editLocalName(String localName) {
        return new User(uid, accessHash, name, localName);
    }
}