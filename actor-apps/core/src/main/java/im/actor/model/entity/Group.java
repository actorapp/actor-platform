/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.Member;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.KeyValueItem;
import im.actor.model.entity.compat.ObsoleteGroup;

public class Group extends WrapperEntity<im.actor.model.api.Group> implements KeyValueItem {

    private static final int RECORD_ID = 10;

    private int groupId;
    private long accessHash;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private String title;
    @Nullable
    private Avatar avatar;
    private int adminId;
    private boolean isMember;
    @NotNull
    @SuppressWarnings("NullableProblems")
    private List<GroupMember> members;

    public Group(@NotNull im.actor.model.api.Group group) {
        super(RECORD_ID, group);
    }

    public Group(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
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

    @NotNull
    public String getTitle() {
        return title;
    }

    @Nullable
    public Avatar getAvatar() {
        return avatar;
    }

    @NotNull
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
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin());
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
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin());
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
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group addMember(int uid, int inviterUid, long inviteDate) {
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
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group updateMembers(List<Member> nMembers) {
        im.actor.model.api.Group w = getWrapped();
        im.actor.model.api.Group res = new im.actor.model.api.Group(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.isMember(),
                w.getCreatorUid(),
                nMembers,
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin());
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
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin());
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
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    @Override
    protected void applyWrapped(@NotNull im.actor.model.api.Group wrapped) {
        this.groupId = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.title = wrapped.getTitle();
        this.avatar = wrapped.getAvatar() != null ? new Avatar(wrapped.getAvatar()) : null;
        this.adminId = wrapped.getCreatorUid();
        this.members = new ArrayList<GroupMember>();
        for (Member m : wrapped.getMembers()) {
            this.members.add(new GroupMember(m.getUid(), m.getInviterUid(), m.getDate(), m.getUid() == this.adminId));
        }
        this.isMember = wrapped.isMember();
    }

    @Override
    public void parse(BserValues values) throws IOException {
        // Is Wrapper Layout
        if (values.getBool(9, false)) {
            // Parse wrapper layout
            super.parse(values);
        } else {
            // Convert old layout
            setWrapped(new ObsoleteGroup(values).toApiGroup());
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Mark as wrapper layout
        writer.writeBool(9, true);
        // Serialize wrapper layout
        super.serialize(writer);
    }

    @Override
    public long getEngineId() {
        return groupId;
    }

    @Override
    @NotNull
    protected im.actor.model.api.Group createInstance() {
        return new im.actor.model.api.Group();
    }

}
