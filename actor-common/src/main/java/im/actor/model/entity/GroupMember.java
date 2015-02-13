package im.actor.model.entity;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class GroupMember {
    private final int uid;

    private final int inviterUid;

    private final long inviteDate;

    public GroupMember(int uid, int inviterUid, long inviteDate) {
        this.uid = uid;
        this.inviterUid = inviterUid;
        this.inviteDate = inviteDate;
    }

    public int getUid() {
        return uid;
    }

    public int getInviterUid() {
        return inviterUid;
    }

    public long getInviteDate() {
        return inviteDate;
    }
}
