/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.Member;
import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.KeyValueItem;

public class Group extends WrapperEntity<im.actor.model.api.Group> implements KeyValueItem {

    public static Group fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Group(), data);
    }

    private static final int RECORD_ID = 10;

    private int groupId;
    private long accessHash;
    private String title;
    private Avatar avatar;
    private int adminId;
    private boolean isMember;
    private List<GroupMember> members;

    public Group(im.actor.model.api.Group group) {
        super(RECORD_ID, group);
    }

    private Group() {
        super(RECORD_ID);
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
        im.actor.model.api.Group w = getWrapped();
        im.actor.model.api.Group res = new im.actor.model.api.Group(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                isMember,
                w.getCreatorUid(),
                w.getMembers(),
                w.getCreateDate());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group clearMembers() {
        im.actor.model.api.Group w = getWrapped();
        im.actor.model.api.Group res = new im.actor.model.api.Group(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.isMember(),
                w.getCreatorUid(),
                new ArrayList<Member>(),
                w.getCreateDate());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group removeMember(int uid) {
        im.actor.model.api.Group w = getWrapped();
        ArrayList<Member> nMembers = new ArrayList<Member>();
        for (Member member : w.getMembers()) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        im.actor.model.api.Group res = new im.actor.model.api.Group(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.isMember(),
                w.getCreatorUid(),
                nMembers,
                w.getCreateDate());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group addMember(int uid, int inviterUid, long inviteDate, boolean isAdmin) {
        im.actor.model.api.Group w = getWrapped();
        ArrayList<Member> nMembers = new ArrayList<Member>();
        for (Member member : w.getMembers()) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        nMembers.add(new Member(uid, inviterUid, inviteDate));
        im.actor.model.api.Group res = new im.actor.model.api.Group(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.isMember(),
                w.getCreatorUid(),
                nMembers,
                w.getCreateDate());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editTitle(String title) {
        im.actor.model.api.Group w = getWrapped();
        im.actor.model.api.Group res = new im.actor.model.api.Group(
                w.getId(),
                w.getAccessHash(),
                title,
                w.getAvatar(),
                w.isMember(),
                w.getCreatorUid(),
                w.getMembers(),
                w.getCreateDate());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editAvatar(im.actor.model.api.Avatar avatar) {
        im.actor.model.api.Group w = getWrapped();
        im.actor.model.api.Group res = new im.actor.model.api.Group(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                avatar,
                w.isMember(),
                w.getCreatorUid(),
                w.getMembers(),
                w.getCreateDate());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    @Override
    protected void applyWrapped(im.actor.model.api.Group wrapped) {
        this.groupId = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.title = wrapped.getTitle();
        if (wrapped.getAvatar() != null) {
            this.avatar = new Avatar(wrapped.getAvatar());
        } else {
            avatar = new Avatar();
        }
        this.adminId = wrapped.getCreatorUid();
        this.members = new ArrayList<GroupMember>();
        for (Member m : wrapped.getMembers()) {
            this.members.add(new GroupMember(m.getUid(), m.getInviterUid(), m.getDate(), m.getUid() == this.adminId));
        }
        this.isMember = wrapped.isMember();
    }

    @Override
    public void parse(BserValues values) throws IOException {
        if (!values.getBool(9, false)) {
            int groupId = values.getInt(1);
            long accessHash = values.getLong(2);
            String title = values.getString(3);
            im.actor.model.api.Avatar avatar = new im.actor.model.api.Avatar();
            if (values.optBytes(4) != null) {
                avatar = Avatar.fromBytes(values.getBytes(4)).toWrapped();
            }
            int adminId = values.getInt(5);

            int count = values.getRepeatedCount(6);
            List<Member> members = new ArrayList<Member>();
            if (count > 0) {
                List<ObsoleteGroupMember> res = new ArrayList<ObsoleteGroupMember>();
                for (int i = 0; i < count; i++) {
                    res.add(new ObsoleteGroupMember());
                }
                res = values.getRepeatedObj(6, res);

                for (ObsoleteGroupMember o : res) {
                    members.add(new Member(o.getUid(), o.getInviterUid(), o.getInviteDate()));
                }
            }

            boolean isMember = values.getBool(7);

            setWrapped(new im.actor.model.api.Group(groupId, accessHash,
                    title, avatar, isMember, adminId, members, 0/*In old Layout doesn't contain group creation date*/));
        }

        super.parse(values);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Mark as New Layout
        writer.writeBool(9, true);
        // Serialize wrapper
        super.serialize(writer);
    }

    @Override
    public long getEngineId() {
        return groupId;
    }

    @Override
    protected im.actor.model.api.Group createInstance() {
        return new im.actor.model.api.Group();
    }

    public class ObsoleteGroupMember extends BserObject {
        private int uid;

        private int inviterUid;

        private long inviteDate;

        private boolean isAdministrator;

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
    }
}
