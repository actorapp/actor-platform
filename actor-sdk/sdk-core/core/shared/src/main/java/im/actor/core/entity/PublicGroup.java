package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import im.actor.core.api.ApiPublicGroup;

public class PublicGroup {

    @Property("readonly, nonatomic")
    private int groupId;
    @Property("readonly, nonatomic")
    private long accessHash;
    @Property("readonly, nonatomic")
    private String title;
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @Property("readonly, nonatomic")
    private String description;
    @Property("readonly, nonatomic")
    private int members;
    @Property("readonly, nonatomic")
    private int friends;

    public PublicGroup(int id, long accessHash, String title, Avatar avatar, String description, int members, int friends) {
        this.groupId = id;
        this.accessHash = accessHash;
        this.title = title;
        this.avatar = avatar;
        this.description = description;
        this.members = members;
        this.friends = friends;
    }

    public PublicGroup(ApiPublicGroup raw) {
        this.groupId = raw.getId();
        this.accessHash = raw.getAccessHash();
        this.title = raw.getTitle();
        this.avatar = raw.getAvatar() == null ? null : new Avatar(raw.getAvatar());
        this.description = raw.getDescription();
        this.members = raw.getMembersCount();
        this.friends = raw.getFriendsCount();
    }

    public int getId() {
        return groupId;
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
