package im.actor.core.api.updates;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;
import static im.actor.runtime.bser.Utils.*;
import im.actor.core.network.parser.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import im.actor.core.api.*;

public class UpdateGroupMembersCountChanged extends Update {

    public static final int HEADER = 0xa3e;
    public static UpdateGroupMembersCountChanged fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UpdateGroupMembersCountChanged(), data);
    }

    private int groupId;
    private int membersCount;

    public UpdateGroupMembersCountChanged(int groupId, int membersCount) {
        this.groupId = groupId;
        this.membersCount = membersCount;
    }

    public UpdateGroupMembersCountChanged() {

    }

    public int getGroupId() {
        return this.groupId;
    }

    public int getMembersCount() {
        return this.membersCount;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.groupId = values.getInt(1);
        this.membersCount = values.getInt(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.groupId);
        writer.writeInt(2, this.membersCount);
    }

    @Override
    public String toString() {
        String res = "update GroupMembersCountChanged{";
        res += "groupId=" + this.groupId;
        res += ", membersCount=" + this.membersCount;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
