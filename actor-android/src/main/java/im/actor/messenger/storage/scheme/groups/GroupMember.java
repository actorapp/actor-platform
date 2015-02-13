package im.actor.messenger.storage.scheme.groups;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 03.12.14.
 */
public class GroupMember extends BserObject {

    private int uid;

    private int inviterUid;

    private long inviteDate;

    public GroupMember(int uid, int invitedUid, long inviteDate) {
        this.uid = uid;
        this.inviterUid = invitedUid;
        this.inviteDate = inviteDate;
    }

    public GroupMember() {
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

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        inviterUid = values.getInt(2);
        inviteDate = values.getLong(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeInt(2, inviterUid);
        writer.writeLong(3, inviteDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupMember that = (GroupMember) o;

        if (inviteDate != that.inviteDate) return false;
        if (inviterUid != that.inviterUid) return false;
        if (uid != that.uid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + inviterUid;
        result = 31 * result + (int) (inviteDate ^ (inviteDate >>> 32));
        return result;
    }
}
