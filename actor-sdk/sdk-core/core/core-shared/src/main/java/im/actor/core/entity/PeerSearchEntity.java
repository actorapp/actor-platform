package im.actor.core.entity;

public class PeerSearchEntity {

    private Peer peer;
    private String title;
    private String description;
    private Integer membersCount;
    private Long date;
    private Integer creatorUid;
    private Boolean isPublic;
    private Boolean isJoined;

    public PeerSearchEntity(Peer peer, String title, String description, Integer membersCount,
                            Long date, Integer creatorUid, Boolean isPublic, Boolean isJoined) {
        this.peer = peer;
        this.title = title;
        this.description = description;
        this.membersCount = membersCount;
        this.date = date;
        this.creatorUid = creatorUid;
        this.isPublic = isPublic;
        this.isJoined = isJoined;
    }

    public Peer getPeer() {
        return peer;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMembersCount() {
        return membersCount;
    }

    public Long getDate() {
        return date;
    }

    public Integer getCreatorUid() {
        return creatorUid;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public Boolean isJoined() {
        return isJoined;
    }
}
