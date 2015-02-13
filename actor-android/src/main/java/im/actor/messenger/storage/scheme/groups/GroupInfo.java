package im.actor.messenger.storage.scheme.groups;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;
import com.droidkit.engine.keyvalue.KeyValueIdentity;

import java.io.IOException;
import java.util.List;

import im.actor.messenger.storage.scheme.avatar.Avatar;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class GroupInfo extends BserObject implements KeyValueIdentity {
    private int groupId;
    private long accessHash;
    private String title;
    private Avatar avatar;
    private List<GroupMember> members;
    private int adminId;
    private GroupState groupState;

    public GroupInfo(int groupId, long accessHash, String title, Avatar avatar,
                     List<GroupMember> members, int adminId, GroupState groupState) {
        this.groupId = groupId;
        this.accessHash = accessHash;
        this.title = title;
        this.avatar = avatar;
        this.members = members;
        this.adminId = adminId;
        this.groupState = groupState;
    }

    public GroupInfo() {

    }

    public int getGroupId() {
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

    public List<GroupMember> getMembers() {
        return members;
    }

    public int getAdminId() {
        return adminId;
    }

    public GroupState getGroupState() {
        return groupState;
    }

    public GroupInfo editTitle(String title) {
        return new GroupInfo(groupId, accessHash, title, avatar, members, adminId, groupState);
    }

    public GroupInfo editAvatar(Avatar avatar) {
        return new GroupInfo(groupId, accessHash, title, avatar, members, adminId, groupState);
    }

    public GroupInfo updateMembers(List<GroupMember> members) {
        return new GroupInfo(groupId, accessHash, title, avatar, members, adminId, groupState);
    }

    public GroupInfo updateState(GroupState groupState) {
        return new GroupInfo(groupId, accessHash, title, avatar, members, adminId, groupState);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        groupId = values.getInt(1);
        accessHash = values.getLong(2);
        title = values.getString(3);
        avatar = values.optObj(4, Avatar.class);
        members = values.getRepeatedObj(5, GroupMember.class);
        adminId = values.getInt(6);
        switch (values.getInt(7)) {
            default:
            case 0:
                groupState = GroupState.JOINED;
                break;
            case 1:
                groupState = GroupState.KICKED;
                break;
            case 3:
                groupState = GroupState.DELETED;
                break;
            case 4:
                groupState = GroupState.DELETED_PENDING;
                break;
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, groupId);
        writer.writeLong(2, accessHash);
        writer.writeString(3, title);
        if (avatar != null) {
            writer.writeObject(4, avatar);
        }
        writer.writeRepeatedObj(5, members);
        writer.writeInt(6, adminId);
        switch (groupState) {
            case JOINED:
                writer.writeInt(7, 0);
                break;
            case KICKED:
                writer.writeInt(7, 1);
                break;
            case DELETED:
                writer.writeInt(7, 3);
                break;
            case DELETED_PENDING:
                writer.writeInt(7, 4);
                break;
        }
    }

    @Override
    public long getKeyValueId() {
        return groupId;
    }
}
