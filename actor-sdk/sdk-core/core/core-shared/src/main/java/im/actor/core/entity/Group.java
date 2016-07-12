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
    private GroupType groupType;

    //
    // Ext
    //

    @Property("readonly, nonatomic")
    private int creatorId;
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

    public boolean isHaveExtension() {
        return haveExtension;
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

    //
    // Editing Ext
    //

    public Group editTheme(String theme) {
//        ApiGroup w = getWrapped();
//        ApiGroup res = new ApiGroup(
//                w.getId(),
//                w.getAccessHash(),
//                w.getTitle(),
//                w.getAvatar(),
//                w.getMembersCount(),
//                w.isMember(),
//                w.isHidden(),
//                w.getGroupType(),
//                w.canSendMessage(),
//                w.getExt(),
//
//                w.isAdmin(),
//                w.getCreatorUid(),
//                w.getMembers(),
//                w.getCreateDate(),
//                theme,
//                w.getAbout());
//        res.setUnmappedObjects(w.getUnmappedObjects());
//        return new Group(res);
        return this;
    }

    public Group editAbout(String about) {
//        ApiGroup w = getWrapped();
//        ApiGroup res = new ApiGroup(
//                w.getId(),
//                w.getAccessHash(),
//                w.getTitle(),
//                w.getAvatar(),
//                w.getMembersCount(),
//                w.isMember(),
//                w.isHidden(),
//                w.getGroupType(),
//                w.canSendMessage(),
//                w.getExt(),
//
//                w.isAdmin(),
//                w.getCreatorUid(),
//                w.getMembers(),
//                w.getCreateDate(),
//                w.getTheme(),
//                about);
//        res.setUnmappedObjects(w.getUnmappedObjects());
//        return new Group(res);
        return this;
    }


    public Group clearMembers() {
//        ApiGroup w = getWrapped();
//        ApiGroup res = new ApiGroup(
//                w.getId(),
//                w.getAccessHash(),
//                w.getTitle(),
//                w.getAvatar(),
//                w.getMembersCount(),
//                w.isMember(),
//                w.isHidden(),
//                w.getGroupType(),
//                w.canSendMessage(),
//                w.getExt(),
//
//                w.isAdmin(),
//                w.getCreatorUid(),
//                new ArrayList<>(),
//                w.getCreateDate(),
//                w.getTheme(),
//                w.getAbout());
//        res.setUnmappedObjects(w.getUnmappedObjects());
//        return new Group(res);
        return this;
    }

    public Group removeMember(int uid) {
//        ApiGroup w = getWrapped();
//        ArrayList<ApiMember> nMembers = new ArrayList<>();
//        for (ApiMember member : w.getMembers()) {
//            if (member.getUid() != uid) {
//                nMembers.add(member);
//            }
//        }
//
//        ApiGroup res = new ApiGroup(
//                w.getId(),
//                w.getAccessHash(),
//                w.getTitle(),
//                w.getAvatar(),
//                w.getMembersCount(),
//                w.isMember(),
//                w.isHidden(),
//                w.getGroupType(),
//                w.canSendMessage(),
//                w.getExt(),
//
//                w.isAdmin(),
//                w.getCreatorUid(),
//                nMembers,
//                w.getCreateDate(),
//                w.getTheme(),
//                w.getAbout());
//        res.setUnmappedObjects(w.getUnmappedObjects());
//        return new Group(res);
        return this;
    }

    public Group addMember(int uid, int inviterUid, long inviteDate) {
//        ApiGroup w = getWrapped();
//        ArrayList<ApiMember> nMembers = new ArrayList<>();
//        for (ApiMember member : w.getMembers()) {
//            if (member.getUid() != uid) {
//                nMembers.add(member);
//            }
//        }
//        nMembers.add(new ApiMember(uid, inviterUid, inviteDate, null));
//        ApiGroup res = new ApiGroup(
//                w.getId(),
//                w.getAccessHash(),
//                w.getTitle(),
//                w.getAvatar(),
//                w.getMembersCount(),
//                w.isMember(),
//                w.isHidden(),
//                w.getGroupType(),
//                w.canSendMessage(),
//                w.getExt(),
//
//                w.isAdmin(),
//                w.getCreatorUid(),
//                nMembers,
//                w.getCreateDate(),
//                w.getTheme(),
//                w.getAbout());
//        res.setUnmappedObjects(w.getUnmappedObjects());
//        return new Group(res);
        return this;
    }

    public Group updateMembers(List<ApiMember> nMembers) {
//        ApiGroup w = getWrapped();
//        ApiGroup res = new ApiGroup(
//                w.getId(),
//                w.getAccessHash(),
//                w.getTitle(),
//                w.getAvatar(),
//                w.getMembersCount(),
//                w.isMember(),
//                w.isHidden(),
//                w.getGroupType(),
//                w.canSendMessage(),
//                w.getExt(),
//
//                w.isAdmin(),
//                w.getCreatorUid(),
//                nMembers,
//                w.getCreateDate(),
//                w.getTheme(),
//                w.getAbout());
//        res.setUnmappedObjects(w.getUnmappedObjects());
//        return new Group(res);
        return this;
    }

    @Override
    protected void applyWrapped(@NotNull ApiGroup wrapped, @Nullable ApiGroupFull ext) {

        this.groupId = wrapped.getId();
        this.accessHash = wrapped.getAccessHash();
        this.title = wrapped.getTitle();
        this.avatar = wrapped.getAvatar() != null ? new Avatar(wrapped.getAvatar()) : null;
        this.isHidden = wrapped.isHidden() != null ? wrapped.isHidden() : false;
        this.membersCount = wrapped.getMembersCount();
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
            haveExtension = true;
        } else {
            haveExtension = false;
        }

        // this.creatorId = wrapped.getCreatorUid();
        // this.members = new ArrayList<>();
        // for (ApiMember m : wrapped.getMembers()) {
        //this.members.add(new GroupMember(m.getUid(), m.getInviterUid(), m.getDate(), m.isAdmin() != null ? m.isAdmin() : m.getUid() == this.creatorId));
        // }
        // this.about = wrapped.getAbout();
        // this.theme = wrapped.getTheme();
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
