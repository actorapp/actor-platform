package im.actor.model.entity;

public class PublicGroup {

    private int id;
    private long accessHash;
    private String title;
    private Avatar avatar;
    private String description;
    private int members;
    private int friends;

    public PublicGroup(int id, long accessHash, String title, Avatar avatar, String description, int members, int friends) {
        this.id = id;
        this.accessHash = accessHash;
        this.title = title;
        this.avatar = avatar;
        this.description = description;
        this.members = members;
        this.friends = friends;
    }

    public int getId() {
        return id;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getTitle() {
        return title;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public String getDescription() {
        return description;
    }

    public int getMembers() {
        return members;
    }

    public int getFriends() {
        return friends;
    }
}
