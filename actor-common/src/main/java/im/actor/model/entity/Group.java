package im.actor.model.entity;

import java.util.List;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class Group {
    private final int groupId;
    private final long accessHash;
    private final String title;
    private final Avatar avatar;
    private final List<GroupMember> members;
    private final int adminId;
    private final GroupState groupState;

    public Group(int groupId, long accessHash, String title, Avatar avatar,
                 List<GroupMember> members, int adminId, GroupState groupState) {
        this.groupId = groupId;
        this.accessHash = accessHash;
        this.title = title;
        this.avatar = avatar;
        this.members = members;
        this.adminId = adminId;
        this.groupState = groupState;
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
}
