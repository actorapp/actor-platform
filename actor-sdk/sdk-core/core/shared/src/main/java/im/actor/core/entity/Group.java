/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiMember;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

public class Group extends WrapperEntity<ApiGroup> implements KeyValueItem {

    private static final int RECORD_ID = 10;

    public static BserCreator<Group> CREATOR = new BserCreator<Group>() {
        @Override
        public Group createInstance() {
            return new Group();
        }
    };

    @Property("readonly, nonatomic")
    private int groupId;
    private long accessHash;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private String title;
    @Nullable
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @Property("readonly, nonatomic")
    private int creatorId;
    @Property("readonly, nonatomic")
    private boolean isMember;
    private boolean isHidden;
    @Nullable
    @Property("readonly, nonatomic")
    private String theme;
    @Nullable
    @Property("readonly, nonatomic")
    private String about;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private List<GroupMember> members;

    public Group(@NotNull ApiGroup group) {
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

    @Nullable
    public String getTheme() {
        return theme;
    }

    @Nullable
    public String getAbout() {
        return about;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public boolean isMember() {
        return isMember;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public Group changeMember(boolean isMember) {
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
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group clearMembers() {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.isMember(),
                w.getCreatorUid(),
                new ArrayList<ApiMember>(),
                w.getCreateDate(),
                w.disableEdit(),
                w.disableInviteView(),
                w.disableInviteRevoke(),
                w.disableIntegrationView(),
                w.disableIntegrationsRevoke(),
                w.isAdmin(),
                w.getTheme(),
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group removeMember(int uid) {
        ApiGroup w = getWrapped();
        ArrayList<ApiMember> nMembers = new ArrayList<ApiMember>();
        for (ApiMember member : w.getMembers()) {
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
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group addMember(int uid, int inviterUid, long inviteDate) {
        ApiGroup w = getWrapped();
        ArrayList<ApiMember> nMembers = new ArrayList<ApiMember>();
        for (ApiMember member : w.getMembers()) {
            if (member.getUid() != uid) {
                nMembers.add(member);
            }
        }
        nMembers.add(new ApiMember(uid, inviterUid, inviteDate, null));
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
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group updateMembers(List<ApiMember> nMembers) {
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
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editTitle(String title) {
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
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editTheme(String theme) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
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
                theme,
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editAbout(String about) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
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
                about,
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    public Group editAvatar(ApiAvatar avatar) {
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
                w.getAbout(),
                w.isHidden(),
                w.getExtensions());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res);
    }

    @Override
    protected void applyWrapped(@NotNull ApiGroup wrapped) {
        this.groupId = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.title = wrapped.getTitle();
        this.avatar = wrapped.getAvatar() != null ? new Avatar(wrapped.getAvatar()) : null;
        this.creatorId = wrapped.getCreatorUid();
        this.members = new ArrayList<GroupMember>();
        for (ApiMember m : wrapped.getMembers()) {
            this.members.add(new GroupMember(m.getUid(), m.getInviterUid(), m.getDate(), m.isAdmin() != null ? m.isAdmin() : m.getUid() == this.creatorId));
        }
        this.isMember = wrapped.isMember();
        this.isHidden = wrapped.isHidden() != null ? wrapped.isHidden() : false;
        this.about = wrapped.getAbout();
        this.theme = wrapped.getTheme();
    }

    @Override
    public void parse(BserValues values) throws IOException {
        // Is Wrapper Layout
        if (values.getBool(9, false)) {
            // Parse wrapper layout
            super.parse(values);
        } else {
            // Convert old layout
            throw new IOException("Unsupported obsolete format");
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
