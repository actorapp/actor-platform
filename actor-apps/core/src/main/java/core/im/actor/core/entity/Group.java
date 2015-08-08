/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.Member;
import im.actor.core.entity.compat.ObsoleteGroup;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class Group extends WrapperEntity<im.actor.core.api.Group> implements KeyValueItem {

    private static final int RECORD_ID = 10;

    public static BserCreator<Group> CREATOR = new BserCreator<Group>() {
        @Override
        public Group createInstance() {
            return new Group();
        }
    };

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

    public Group(@NotNull im.actor.core.api.Group group) {
        super(RECORD_ID, group);
    }

    public Group(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
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
        im.actor.core.api.Group w = getWrapped();
        im.actor.core.api.Group res = new im.actor.core.api.Group(
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
                w.isAdmin(),
                w.getTheme(),
                w.getAbout());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group clearMembers() {
        im.actor.core.api.Group w = getWrapped();
        im.actor.core.api.Group res = new im.actor.core.api.Group(
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
                w.isAdmin(),
                w.getTheme(),
                w.getAbout());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group removeMember(int uid) {
        im.actor.core.api.Group w = getWrapped();
        ArrayList<Member> nMembers = new ArrayList<Member>();
        for (Member member : w.getMembers()) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        im.actor.core.api.Group res = new im.actor.core.api.Group(
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
                w.isAdmin(),
                w.getTheme(),
                w.getAbout());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group addMember(int uid, int inviterUid, long inviteDate) {
        im.actor.core.api.Group w = getWrapped();
        ArrayList<Member> nMembers = new ArrayList<Member>();
        for (Member member : w.getMembers()) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        nMembers.add(new Member(uid, inviterUid, inviteDate, null));
        im.actor.core.api.Group res = new im.actor.core.api.Group(
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
                w.isAdmin(),
                w.getTheme(),
                w.getAbout());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group updateMembers(List<Member> nMembers) {
        im.actor.core.api.Group w = getWrapped();
        im.actor.core.api.Group res = new im.actor.core.api.Group(
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
                w.isAdmin(),
                w.getTheme(),
                w.getAbout());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editTitle(String title) {
        im.actor.core.api.Group w = getWrapped();
        im.actor.core.api.Group res = new im.actor.core.api.Group(
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
                w.isAdmin(),
                w.getTheme(),
                w.getAbout());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editAvatar(im.actor.core.api.Avatar avatar) {
        im.actor.core.api.Group w = getWrapped();
        im.actor.core.api.Group res = new im.actor.core.api.Group(
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
                w.isAdmin(),
                w.getTheme(),
                w.getAbout());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    @Override
    protected void applyWrapped(@NotNull im.actor.core.api.Group wrapped) {
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
    protected im.actor.core.api.Group createInstance() {
        return new im.actor.core.api.Group();
    }

}
