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
import im.actor.core.api.ApiFullUser;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiGroupFull;
import im.actor.core.api.ApiMapValue;
import im.actor.core.api.ApiMember;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

public class Group extends WrapperExtEntity<ApiGroupFull, ApiGroup> implements KeyValueItem {

    private static final int RECORD_ID = 10;
    private static final int RECORD_EXT_ID = 11;

    public static BserCreator<Group> CREATOR = Group::new;

    //
    // Main
    //

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
    private boolean isHidden;
    @Property("readonly, nonatomic")
    private int membersCount;
    @Property("readonly, nonatomic")
    private boolean isMember;
    @Property("readonly, nonatomic")
    private boolean canWrite;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private GroupType groupType;

    //
    // Ext
    //

    @Property("readonly, nonatomic")
    private Integer ownerId;
    @Nullable
    @Property("readonly, nonatomic")
    private String topic;
    @Nullable
    @Property("readonly, nonatomic")
    private String about;
    @Nullable
    @Property("readonly, nonatomic")
    private String shortName;
    @NotNull
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private List<GroupMember> members;
    @Property("readonly, nonatomic")
    private boolean isAsyncMembers;
    @Property("readonly, nonatomic")
    private boolean isCanInviteMembers;
    @Property("readonly, nonatomic")
    private boolean isCanInviteViaLink;
    @Property("readonly, nonatomic")
    private boolean isCanViewMembers;
    @Property("readonly, nonatomic")
    private boolean isSharedHistory;
    @Property("readonly, nonatomic")
    private boolean isCanEditInfo;
    @Property("readonly, nonatomic")
    private boolean isCanEditAdministration;
    @Property("readonly, nonatomic")
    private boolean isCanEditShortName;
    @Property("readonly, nonatomic")
    private boolean isCanViewAdmins;
    @Property("readonly, nonatomic")
    private boolean isCanEditAdmins;
    @Property("readonly, nonatomic")
    private boolean isCanLeave;
    @Property("readonly, nonatomic")
    private boolean isCanDelete;

    @Property("readonly, nonatomic")
    private boolean haveExtension;

    //
    // Constructors
    //

    public Group(@NotNull ApiGroup group, @Nullable ApiGroupFull ext) {
        super(RECORD_ID, RECORD_EXT_ID, group, ext);
    }

    public Group(@NotNull byte[] data) throws IOException {
        super(RECORD_ID, RECORD_EXT_ID, data);
    }

    private Group() {
        super(RECORD_ID, RECORD_EXT_ID);
    }

    //
    // Getters
    //

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

    public boolean isHidden() {
        return isHidden;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public boolean isMember() {
        return isMember;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    @NotNull
    public GroupType getGroupType() {
        return groupType;
    }

    //
    // Ext
    //

    @NotNull
    public List<GroupMember> getMembers() {
        return members;
    }

    @Nullable
    public String getTopic() {
        return topic;
    }

    @Nullable
    public String getAbout() {
        return about;
    }

    @Nullable
    public String getShortName() {
        return shortName;
    }

    @Nullable
    public Integer getOwnerId() {
        return ownerId;
    }

    public boolean isAsyncMembers() {
        return isAsyncMembers;
    }

    public boolean isCanInviteMembers() {
        return isCanInviteMembers;
    }

    public boolean isCanViewMembers() {
        return isCanViewMembers;
    }

    public boolean isCanEditInfo() {
        return isCanEditInfo;
    }

    public boolean isSharedHistory() {
        return isSharedHistory;
    }

    public boolean isHaveExtension() {
        return haveExtension;
    }

    public boolean isCanEditShortName() {
        return isCanEditShortName;
    }

    public boolean isCanEditAdministration() {
        return isCanEditAdministration;
    }

    public boolean isCanViewAdmins() {
        return isCanViewAdmins;
    }

    public boolean isCanEditAdmins() {
        return isCanEditAdmins;
    }

    public boolean isCanInviteViaLink() {
        return isCanInviteViaLink;
    }

    public boolean isCanLeave() {
        return isCanLeave;
    }

    public boolean isCanDelete() {
        return isCanDelete;
    }

    public Group updateExt(@Nullable ApiGroupFull ext) {
        return new Group(getWrapped(), ext);
    }

    //
    // Editing Main
    //

    public Group editTitle(String title) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                title,
                w.getAvatar(),
                w.getMembersCount(),
                w.isMember(),
                w.isHidden(),
                w.getGroupType(),
                w.canSendMessage(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res, getWrappedExt());
    }

    public Group editAvatar(ApiAvatar avatar) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                avatar,
                w.getMembersCount(),
                w.isMember(),
                w.isHidden(),
                w.getGroupType(),
                w.canSendMessage(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res, getWrappedExt());
    }

    public Group editIsMember(boolean isMember) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.getMembersCount(),
                isMember,
                w.isHidden(),
                w.getGroupType(),
                w.canSendMessage(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res, getWrappedExt());
    }

    public Group editExt(ApiMapValue ext) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.getMembersCount(),
                w.isMember(),
                w.isHidden(),
                w.getGroupType(),
                w.canSendMessage(),
                ext);
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res, getWrappedExt());
    }

    public Group editCanWrite(boolean canWrite) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                w.getMembersCount(),
                w.isMember(),
                w.isHidden(),
                w.getGroupType(),
                canWrite,
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res, getWrappedExt());
    }

    //
    // Members
    //

    public Group editMembers(List<ApiMember> members) {
        ApiGroupFull fullExt = null;
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    members,
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
        }

        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                members.size(),
                w.isMember(),
                w.isHidden(),
                w.getGroupType(),
                w.canSendMessage(),
                w.getExt());

        return new Group(res, fullExt);
    }

    public Group editMembers(List<ApiMember> added, List<Integer> removed, int count) {
        ApiGroupFull fullExt = null;
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();

            ArrayList<ApiMember> nMembers = new ArrayList<>(e.getMembers());

            // Remove members
            for (Integer i : removed) {
                for (ApiMember m : nMembers) {
                    if (m.getUid() == i) {
                        nMembers.remove(m);
                        break;
                    }
                }
            }
            // Adding members
            outer:
            for (ApiMember a : added) {
                for (ApiMember m : nMembers) {
                    if (m.getUid() == a.getUid()) {
                        nMembers.remove(m);
                        continue outer;
                    }
                }
                nMembers.add(a);
            }

            fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    nMembers,
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
        }

        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                count,
                w.isMember(),
                w.isHidden(),
                w.getGroupType(),
                w.canSendMessage(),
                w.getExt());

        return new Group(res, fullExt);
    }

    public Group editMembersCount(int membersCount) {
        ApiGroup w = getWrapped();
        ApiGroup res = new ApiGroup(
                w.getId(),
                w.getAccessHash(),
                w.getTitle(),
                w.getAvatar(),
                membersCount,
                w.isMember(),
                w.isHidden(),
                w.getGroupType(),
                w.canSendMessage(),
                w.getExt());
        res.setUnmappedObjects(w.getUnmappedObjects());
        return new Group(res, getWrappedExt());
    }

    public Group editMembersBecameAsync() {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    new ArrayList<>(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    true,
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());

            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editMemberChangedAdmin(int uid, Boolean isAdmin) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();

            ArrayList<ApiMember> nMembers = new ArrayList<>(e.getMembers());
            for (int i = 0; i < nMembers.size(); i++) {
                ApiMember m = nMembers.get(i);
                if (m.getUid() == uid) {
                    nMembers.remove(m);
                    nMembers.add(i, new ApiMember(m.getUid(), m.getInviterUid(),
                            m.getDate(), isAdmin));
                    break;
                }
            }

            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    nMembers,
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());

            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    //
    // Editing Ext
    //

    public Group editTopic(String topic) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    topic,
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editAbout(String about) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    about,
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editShortName(String shortName) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    shortName,
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editFullExt(ApiMapValue ext) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    ext,
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanViewMembers(boolean canViewMembers) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    canViewMembers,
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanInviteViaLink(boolean canInviteViaLink) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    canInviteViaLink,
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanDelete(boolean canDelete) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    canDelete,
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanLeave(boolean canLeave) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    canLeave);
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanEditGroupInfo(boolean canEditGroupInfo) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    canEditGroupInfo,
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanEditShortName(boolean canEditShortName) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    canEditShortName,
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanEditAdminList(boolean canEditAdminList) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    canEditAdminList,
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanViewAdminList(boolean canViewAdminList) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    canViewAdminList,
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanEditAdminSettings(boolean canEditAdminSettings) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    canEditAdminSettings,
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editCanInviteMembers(boolean canInviteMembers) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    canInviteMembers,
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editOwner(int uid) {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    uid,
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    e.isSharedHistory(),
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }

    public Group editHistoryShared() {
        if (getWrappedExt() != null) {
            ApiGroupFull e = getWrappedExt();
            ApiGroupFull fullExt = new ApiGroupFull(e.getId(),
                    e.getCreateDate(),
                    e.getOwnerUid(),
                    e.getMembers(),
                    e.getTheme(),
                    e.getAbout(),
                    e.getExt(),
                    e.isAsyncMembers(),
                    e.canViewMembers(),
                    e.canInvitePeople(),
                    true,
                    e.canEditGroupInfo(),
                    e.getShortName(),
                    e.canEditShortName(),
                    e.canEditAdminList(),
                    e.canViewAdminList(),
                    e.canEditAdminSettings(),
                    e.canInviteViaLink(),
                    e.canDelete(),
                    e.canLeave());
            fullExt.setUnmappedObjects(e.getUnmappedObjects());
            return new Group(getWrapped(), fullExt);
        } else {
            return this;
        }
    }


    @Override
    protected void applyWrapped(@NotNull ApiGroup wrapped, @Nullable ApiGroupFull ext) {

        this.groupId = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.title = wrapped.getTitle();
        this.avatar = wrapped.getAvatar() != null ? new Avatar(wrapped.getAvatar()) : null;
        this.isHidden = wrapped.isHidden() != null ? wrapped.isHidden() : false;
        this.membersCount = wrapped.getMembersCount() != null ? wrapped.getMembersCount() : 0;
        this.isMember = wrapped.isMember() != null ? wrapped.isMember() : true;

        if (wrapped.getGroupType() == null) {
            this.groupType = GroupType.GROUP;
        } else {
            switch (wrapped.getGroupType()) {
                case CHANNEL:
                    this.groupType = GroupType.CHANNEL;
                    break;
                case GROUP:
                    this.groupType = GroupType.GROUP;
                    break;
                default:
                case UNSUPPORTED_VALUE:
                    this.groupType = GroupType.OTHER;
                    break;
            }
        }

        if (wrapped.canSendMessage() != null) {
            this.canWrite = wrapped.canSendMessage();
        } else {
            // True is default for groups and false otherwise
            this.canWrite = this.groupType == GroupType.GROUP;
        }

        //
        // Ext
        //

        if (ext != null) {
            this.haveExtension = true;
            this.ownerId = ext.getOwnerUid();
            this.about = ext.getAbout();
            this.topic = ext.getTheme();
            this.shortName = ext.getShortName();
            this.isAsyncMembers = ext.isAsyncMembers() != null ? ext.isAsyncMembers() : false;
            this.isCanViewMembers = ext.canViewMembers() != null ? ext.canViewMembers() : true;
            this.isCanInviteMembers = ext.canViewMembers() != null ? ext.canViewMembers() : true;
            this.isCanInviteViaLink = ext.canInviteViaLink() != null ? ext.canInviteViaLink() : false;
            this.isSharedHistory = ext.isSharedHistory() != null ? ext.isSharedHistory() : false;
            this.isCanEditInfo = ext.canEditGroupInfo() != null ? ext.canEditGroupInfo() : false;
            this.isCanEditShortName = ext.canEditShortName() != null ? ext.canEditShortName() : false;
            this.isCanEditAdministration = ext.canEditAdminSettings() != null ? ext.canEditAdminSettings() : false;
            this.isCanViewAdmins = ext.canViewAdminList() != null ? ext.canViewAdminList() : false;
            this.isCanEditAdmins = ext.canEditAdminList() != null ? ext.canEditAdminList() : false;
            this.isCanLeave = ext.canLeave() != null ? ext.canLeave() : true;
            this.isCanDelete = ext.canDelete() != null ? ext.canDelete() : false;

            this.members = new ArrayList<>();
            for (ApiMember m : ext.getMembers()) {
                this.members.add(new GroupMember(m.getUid(), m.getInviterUid(), m.getDate(),
                        m.isAdmin() != null ? m.isAdmin() : false));
            }
        } else {
            this.haveExtension = false;
            this.ownerId = 0;
            this.about = null;
            this.topic = null;
            this.shortName = null;
            this.members = new ArrayList<>();
            this.isAsyncMembers = false;
            this.isCanViewMembers = false;
            this.isCanInviteMembers = false;
            this.isSharedHistory = false;
            this.isCanEditInfo = false;
            this.isCanEditShortName = false;
            this.isCanEditAdministration = false;
            this.isCanViewAdmins = false;
            this.isCanEditAdmins = false;
            this.isCanDelete = false;
            this.isCanLeave = false;
            this.isCanInviteViaLink = false;
        }
    }

    @Override
    public long getEngineId() {
        return groupId;
    }

    public Peer peer() {
        return new Peer(PeerType.GROUP, groupId);
    }

    @Override
    @NotNull
    protected ApiGroup createInstance() {
        return new ApiGroup();
    }

    @Override
    protected ApiGroupFull createExtInstance() {
        return new ApiGroupFull();
    }
}
