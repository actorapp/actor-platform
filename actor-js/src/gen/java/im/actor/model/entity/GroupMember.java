package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class GroupMember extends BserObject {

    public static GroupMember fromBytes(byte[] data) throws IOException {
        return Bser.parse(new GroupMember(), data);
    }

    private int uid;

    private int inviterUid;

    private long inviteDate;

    private boolean isAdministrator;

    public GroupMember(int uid, int inviterUid, long inviteDate,
                       boolean isAdministrator) {
        this.uid = uid;
        this.inviterUid = inviterUid;
        this.inviteDate = inviteDate;
        this.isAdministrator = isAdministrator;
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

    public boolean isAdministrator() {
        return isAdministrator;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        inviterUid = values.getInt(2);
        inviteDate = values.getLong(3);
        isAdministrator = values.getBool(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeInt(2, inviterUid);
        writer.writeLong(3, inviteDate);
        writer.writeBool(4, isAdministrator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupMember member = (GroupMember) o;

        if (uid != member.uid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uid;
    }
}