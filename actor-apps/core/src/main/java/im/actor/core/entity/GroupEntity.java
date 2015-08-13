/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.Member;
import im.actor.core.entity.compat.ObsoleteGroup;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class GroupEntity extends WrapperEntity<ApiGroup> implements KeyValueItem {

    private static final int RECORD_ID = 10;

    public static BserCreator<GroupEntity> CREATOR = new BserCreator<GroupEntity>() {
        @Override
        public GroupEntity createInstance() {
            return new GroupEntity();
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

    public GroupEntity(@NotNull ApiGroup group) {
        super(RECORD_ID, group);
    }

    public GroupEntity(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, data);
    }

    private GroupEntity() {
        super(RECORD_ID);
    }

    public PeerEntity peer() {
        return new PeerEntity(PeerTypeEntity.GROUP, groupId);
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

    public GroupEntity changeMember(boolean isMember) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
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
        return new GroupEntity(res);
    }

    public GroupEntity clearMembers() {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
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
        return new GroupEntity(res);
    }

    public GroupEntity removeMember(int uid) {
        ApiGroup w = getWrapped();
        ArrayList<Member> nMembers = new ArrayList<Member>();
        for (Member member : w.getMembers()) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        ApiGroup res = new ApiGroup(
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
        return new GroupEntity(res);
    }

    public GroupEntity addMember(int uid, int inviterUid, long inviteDate) {
        ApiGroup w = getWrapped();
        ArrayList<Member> nMembers = new ArrayList<Member>();
        for (Member member : w.getMembers()) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        nMembers.add(new Member(uid, inviterUid, inviteDate, null));
        ApiGroup res = new ApiGroup(
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
        return new GroupEntity(res);
    }

    public GroupEntity updateMembers(List<Member> nMembers) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
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
        return new GroupEntity(res);
    }

    public GroupEntity editTitle(String title) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
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
        return new GroupEntity(res);
    }

    public GroupEntity editAvatar(ApiAvatar avatar) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
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
        return new GroupEntity(res);
    }

    @Override
    protected void applyWrapped(@NotNull ApiGroup wrapped) {
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
    protected ApiGroup createInstance() {
        return new ApiGroup();
    }

}
