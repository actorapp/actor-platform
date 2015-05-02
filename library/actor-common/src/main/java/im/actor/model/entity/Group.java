/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.KeyValueItem;

public class Group extends BserObject implements KeyValueItem {

    public static Group fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Group(), data);
    }

    private int groupId;
    private long accessHash;
    private String title;
    private Avatar avatar;
    private int adminId;
    private boolean isMember;
    private List<GroupMember> members;

    public Group(int groupId, long accessHash, String title, Avatar avatar,
                 List<GroupMember> members, int adminId, boolean isMember) {
        this.groupId = groupId;
        this.accessHash = accessHash;
        this.title = title;
        this.avatar = avatar;
        this.members = members;
        this.adminId = adminId;
        this.isMember = isMember;
    }

    private Group() {

    }

    public Peer peer() {
        return new Peer(PeerType.GROUP, groupId);
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

    public boolean isMember() {
        return isMember;
    }

    public Group changeMember(boolean isMember) {
        return new Group(groupId, accessHash, title, avatar, members, adminId, isMember);
    }

    public Group clearMembers() {
        return new Group(groupId, accessHash, title, avatar, new ArrayList<GroupMember>(), adminId, isMember);
    }

    public Group removeMember(int uid) {
        ArrayList<GroupMember> nMembers = new ArrayList<GroupMember>();
        for (GroupMember member : members) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        return new Group(groupId, accessHash, title, avatar, nMembers, adminId, isMember);
    }

    public Group addMember(int uid, int inviterUid, long inviteDate, boolean isAdmin) {
        ArrayList<GroupMember> nMembers = new ArrayList<GroupMember>();
        for (GroupMember member : members) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        nMembers.add(new GroupMember(uid, inviterUid, inviteDate, isAdmin));
        return new Group(groupId, accessHash, title, avatar, nMembers, adminId, isMember);
    }

    public Group editTitle(String title) {
        return new Group(groupId, accessHash, title, avatar, members, adminId, isMember);
    }

    public Group editAvatar(Avatar avatar) {
        return new Group(groupId, accessHash, title, avatar, members, adminId, isMember);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        groupId = values.getInt(1);
        accessHash = values.getLong(2);
        title = values.getString(3);
        if (values.optBytes(4) != null) {
            avatar = Avatar.fromBytes(values.getBytes(4));
        }
        adminId = values.getInt(5);

        int count = values.getRepeatedCount(6);
        if (count > 0) {
            ArrayList<GroupMember> res = new ArrayList<GroupMember>();
            for (int i = 0; i < count; i++) {
                res.add(new GroupMember());
            }
            members = values.getRepeatedObj(6, res);
        } else {
            members = new ArrayList<GroupMember>();
        }

        isMember = values.getBool(7);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, groupId);
        writer.writeLong(2, accessHash);
        writer.writeString(3, title);
        if (avatar != null) {
            writer.writeObject(4, avatar);
        }
        writer.writeInt(5, adminId);
        writer.writeRepeatedObj(6, members);
        writer.writeBool(7, isMember);
    }

    @Override
    public long getEngineId() {
        return groupId;
    }
}
