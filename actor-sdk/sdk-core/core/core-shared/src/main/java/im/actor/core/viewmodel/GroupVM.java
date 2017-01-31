/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import im.actor.core.api.ApiMapValue;
import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.GroupType;
import im.actor.core.viewmodel.generics.AvatarValueModel;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.core.viewmodel.generics.IntValueModel;
import im.actor.core.viewmodel.generics.StringValueModel;
import im.actor.runtime.annotations.MainThread;
import im.actor.runtime.mvvm.BaseValueModel;
import im.actor.runtime.mvvm.ModelChangedListener;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.mvvm.ValueModelCreator;

/**
 * Group View Model
 */
public class GroupVM extends BaseValueModel<Group> {

    public static ValueModelCreator<Group, GroupVM> CREATOR = GroupVM::new;

    @Property("nonatomic, readonly")
    private int groupId;
    @NotNull
    @Property("nonatomic, readonly")
    private GroupType groupType;
    @NotNull
    @Property("nonatomic, readonly")
    private StringValueModel name;
    @NotNull
    @Property("nonatomic, readonly")
    private AvatarValueModel avatar;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isMember;
    @NotNull
    @Property("nonatomic, readonly")
    private IntValueModel membersCount;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanWriteMessage;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanCall;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanClear;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanJoin;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanViewInfo;

    @NotNull
    @Property("nonatomic, readonly")
    private ValueModel<HashSet<GroupMember>> members;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isAsyncMembers;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanViewMembers;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanInviteMembers;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanEditInfo;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isHistoryShared;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanEditAdministration;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanEditAdmins;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanViewAdmins;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanLeave;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanDelete;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanInviteViaLink;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanKickInvited;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanKickAnyone;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanEditForeign;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isCanDeleteForeign;
    @NotNull
    @Property("nonatomic, readonly")
    private BooleanValueModel isDeleted;

    @NotNull
    @Property("nonatomic, readonly")
    private StringValueModel theme;
    @NotNull
    @Property("nonatomic, readonly")
    private StringValueModel about;
    @NotNull
    @Property("nonatomic, readonly")
    private StringValueModel shortName;
    @Property("nonatomic, readonly")
    private IntValueModel ownerId;
    @NotNull
    @Property("nonatomic, readonly")
    private ValueModel<Integer> presence;
    @NotNull
    @Property("nonatomic, readonly")
    private ValueModel<ApiMapValue> ext;

    @NotNull
    private ArrayList<ModelChangedListener<GroupVM>> listeners = new ArrayList<>();

    /**
     * <p>INTERNAL API</p>
     * Create Group View Model
     *
     * @param rawObj initial value of Group
     */
    public GroupVM(@NotNull Group rawObj) {
        super(rawObj);
        this.groupId = rawObj.getGroupId();
        this.groupType = rawObj.getGroupType();
        this.name = new StringValueModel("group." + groupId + ".title", rawObj.getTitle());
        this.avatar = new AvatarValueModel("group." + groupId + ".avatar", rawObj.getAvatar());
        this.isMember = new BooleanValueModel("group." + groupId + ".isMember", rawObj.isMember());
        this.membersCount = new IntValueModel("group." + groupId + ".membersCount", rawObj.getMembersCount());
        this.isCanWriteMessage = new BooleanValueModel("group." + groupId + ".can_write", rawObj.isCanSendMessage());
        this.isCanCall = new BooleanValueModel("group." + groupId + ".can_call", rawObj.isCanCall());
        this.isCanViewMembers = new BooleanValueModel("group." + groupId + ".can_view_members", rawObj.isCanViewMembers());
        this.isCanInviteMembers = new BooleanValueModel("group." + groupId + ".can_invite_members", rawObj.isCanInviteMembers());
        this.isCanEditInfo = new BooleanValueModel("group." + groupId + ".can_edit_info", rawObj.isCanEditInfo());
        this.isAsyncMembers = new BooleanValueModel("group." + groupId + ".isAsyncMembers", rawObj.isAsyncMembers());
        this.isCanEditAdministration = new BooleanValueModel("group." + groupId + ".isCanEditAdministration", rawObj.isCanEditAdministration());
        this.isHistoryShared = new BooleanValueModel("group." + groupId + ".isHistoryShared", rawObj.isSharedHistory());
        this.isCanEditAdmins = new BooleanValueModel("group." + groupId + ".isCanEditAdmins", rawObj.isCanEditAdmins());
        this.isCanViewAdmins = new BooleanValueModel("group." + groupId + ".isCanViewAdmins", rawObj.isCanViewAdmins());
        this.isCanLeave = new BooleanValueModel("group." + groupId + ".isCanLeave", rawObj.isCanLeave());
        this.isCanDelete = new BooleanValueModel("group." + groupId + ".isCanDelete", rawObj.isCanDelete());
        this.isCanInviteViaLink = new BooleanValueModel("group." + groupId + ".isCanInviteViaLink", rawObj.isCanInviteViaLink());
        this.isCanKickInvited = new BooleanValueModel("group." + groupId + ".isCanKickInvited", rawObj.isCanKickInvited());
        this.isCanKickAnyone = new BooleanValueModel("group." + groupId + ".isCanKickAnyone", rawObj.isCanKickAnyone());
        this.isCanEditForeign = new BooleanValueModel("group." + groupId + ".isCanEditForeign", rawObj.isCanEditForeign());
        this.isCanDeleteForeign = new BooleanValueModel("group." + groupId + ".isCanDeleteForeign", rawObj.isCanDeleteForeign());
        this.isDeleted = new BooleanValueModel("group." + groupId + ".isDeleted", rawObj.isDeleted());
        this.isCanClear = new BooleanValueModel("group." + groupId + ".isCanClear", rawObj.isCanClear());
        this.isCanJoin = new BooleanValueModel("group." + groupId + ".isCanJoin", rawObj.isCanJoin());
        this.isCanViewInfo = new BooleanValueModel("group." + groupId + ".isCanViewInfo", rawObj.isCanViewInfo());

        this.ownerId = new IntValueModel("group." + groupId + ".membersCount", rawObj.getOwnerId());
        this.members = new ValueModel<>("group." + groupId + ".members", new HashSet<>(rawObj.getMembers()));
        this.presence = new ValueModel<>("group." + groupId + ".presence", 0);
        this.theme = new StringValueModel("group." + groupId + ".theme", rawObj.getTopic());
        this.about = new StringValueModel("group." + groupId + ".about", rawObj.getAbout());
        this.shortName = new StringValueModel("group." + groupId + ".shortname", rawObj.getShortName());
        this.ext = new ValueModel<>("group." + groupId + ".ext", rawObj.getExt());
    }

    /**
     * Get Group Id
     *
     * @return Group Id
     */
    @ObjectiveCName("getId")
    public int getId() {
        return groupId;
    }

    /**
     * Get Group Type
     *
     * @return Group Type
     */
    @NotNull
    @ObjectiveCName("getGroupType")
    public GroupType getGroupType() {
        return groupType;
    }

    /**
     * Get Name Value Model
     *
     * @return Value Model of String
     */
    @NotNull
    @ObjectiveCName("getNameModel")
    public StringValueModel getName() {
        return name;
    }

    /**
     * Get Avatar Value Model
     *
     * @return Value Model of Avatar
     */
    @NotNull
    @ObjectiveCName("getAvatarModel")
    public AvatarValueModel getAvatar() {
        return avatar;
    }

    /**
     * Get About Value Model
     *
     * @return Value Model of String
     */
    @NotNull
    @ObjectiveCName("getAboutModel")
    public StringValueModel getAbout() {
        return about;
    }

    /**
     * Get Theme Value Model
     *
     * @return Value Model of String
     */
    @NotNull
    @ObjectiveCName("getThemeModel")
    public StringValueModel getTheme() {
        return theme;
    }

    /**
     * Get Short Name Model
     *
     * @return Value Model of String
     */
    @NotNull
    @ObjectiveCName("getShortNameModel")
    public StringValueModel getShortName() {
        return shortName;
    }

    /**
     * Get membership Value Model
     *
     * @return Value Model of Boolean
     */
    @NotNull
    @ObjectiveCName("isMemberModel")
    public BooleanValueModel isMember() {
        return isMember;
    }

    /**
     * Get Group members count
     *
     * @return members count
     */
    @NotNull
    @ObjectiveCName("getMembersCountModel")
    public IntValueModel getMembersCount() {
        return membersCount;
    }

    /**
     * Can current user write message to a group
     *
     * @return can write message model
     */
    @NotNull
    @ObjectiveCName("isCanWriteMessageModel")
    public BooleanValueModel getIsCanWriteMessage() {
        return isCanWriteMessage;
    }

    /**
     * Can current user view members of a group
     *
     * @return can view members model
     */
    @NotNull
    @ObjectiveCName("getIsCanViewMembersModel")
    public BooleanValueModel getIsCanViewMembers() {
        return isCanViewMembers;
    }

    /**
     * Can current user edit group info
     *
     * @return can edit group info
     */
    @NotNull
    @ObjectiveCName("isCanEditInfoModel")
    public BooleanValueModel getIsCanEditInfo() {
        return isCanEditInfo;
    }

    /**
     * Can current user invite members to a group
     *
     * @return can invite members model
     */
    @NotNull
    @ObjectiveCName("getIsCanInviteMembersModel")
    public BooleanValueModel getIsCanInviteMembers() {
        return isCanInviteMembers;
    }


    /**
     * Is members should be fetched async
     *
     * @return is members async model
     */
    @NotNull
    @ObjectiveCName("getIsAsyncMembersModel")
    public BooleanValueModel getIsAsyncMembers() {
        return isAsyncMembers;
    }

    /**
     * Is history shared in this group
     *
     * @return is history shared model
     */
    @NotNull
    @ObjectiveCName("getIsHistorySharedModel")
    public BooleanValueModel getIsHistoryShared() {
        return isHistoryShared;
    }

    /**
     * Is current user can edit administration settings
     *
     * @return is can edit administration model
     */
    @NotNull
    @ObjectiveCName("getIsCanEditAdministrationModel")
    public BooleanValueModel getIsCanEditAdministration() {
        return isCanEditAdministration;
    }

    /**
     * Is current user can leave group
     *
     * @return is current user can leave model
     */
    @NotNull
    @ObjectiveCName("getIsCanLeaveModel")
    public BooleanValueModel getIsCanLeave() {
        return isCanLeave;
    }

    /**
     * Is current user can delete group
     *
     * @return is current user can delete model
     */
    @NotNull
    @ObjectiveCName("getIsCanDeleteModel")
    public BooleanValueModel getIsCanDelete() {
        return isCanDelete;
    }

    /**
     * Is current user can call in this group
     *
     * @return is current user can call model
     */
    @NotNull
    @ObjectiveCName("getIsCanCallModel")
    public BooleanValueModel getIsCanCall() {
        return isCanCall;
    }

    /**
     * Is current user can invite via link
     *
     * @return is current user can invite via link model
     */
    @NotNull
    @ObjectiveCName("getIsCanInviteViaLinkModel")
    public BooleanValueModel getIsCanInviteViaLink() {
        return isCanInviteViaLink;
    }

    /**
     * Is current user can kick invited members
     *
     * @return is current user can kick invited model
     */
    @NotNull
    @ObjectiveCName("getIsCanKickInvitedModel")
    public BooleanValueModel getIsCanKickInvited() {
        return isCanKickInvited;
    }

    /**
     * Is current user can kick anyone
     *
     * @return is current user can kick anyone model
     */
    @NotNull
    @ObjectiveCName("getIsCanKickAnyoneModel")
    public BooleanValueModel getIsCanKickAnyone() {
        return isCanKickAnyone;
    }

    /**
     * Is current user can edit foreign messages
     *
     * @return is current user can edit foreign messages model
     */
    @NotNull
    @ObjectiveCName("getIsCanEditForeignModel")
    public BooleanValueModel getIsCanEditForeign() {
        return isCanEditForeign;
    }

    /**
     * Is current user can delete foreign messages
     *
     * @return is current user can delete foreign messages model
     */
    @NotNull
    @ObjectiveCName("getIsCanDeleteForeignModel")
    public BooleanValueModel getIsCanDeleteForeign() {
        return isCanDeleteForeign;
    }

    /**
     * Is current user can clear messages
     *
     * @return is current user can clear messages model
     */
    @NotNull
    @ObjectiveCName("getIsCanClearModel")
    public BooleanValueModel getIsCanClear() {
        return isCanClear;
    }

    /**
     * Is current user can view info
     *
     * @return is current user can view info model
     */
    @NotNull
    @ObjectiveCName("getIsCanViewInfoModel")
    public BooleanValueModel getIsCanViewInfo() {
        return isCanViewInfo;
    }

    /**
     * Is current user can join
     *
     * @return is current user can join model
     */
    @NotNull
    public BooleanValueModel getIsCanJoin() {
        return isCanJoin;
    }

    /**
     * Is current user can edit admins
     *
     * @return is current user can edit admins
     */
    @NotNull
    public BooleanValueModel getIsCanEditAdmins() {
        return isCanEditAdmins;
    }

    /**
     * Is group deleted
     *
     * @return is this group deleted model
     */
    @NotNull
    @ObjectiveCName("getIsDeletedModel")
    public BooleanValueModel getIsDeleted() {
        return isDeleted;
    }

    /**
     * Get Group owner user id model
     *
     * @return creator owner id model
     */
    @ObjectiveCName("getCreatorIdModel")
    public IntValueModel getOwnerId() {
        return ownerId;
    }

    /**
     * Get members Value Model
     *
     * @return Value Model of HashSet of GroupMember
     */
    @NotNull
    @ObjectiveCName("getMembersModel")
    public ValueModel<HashSet<GroupMember>> getMembers() {
        return members;
    }

    /**
     * Get Online Value Model
     *
     * @return Value Model of Integer
     */
    @NotNull
    @ObjectiveCName("getPresenceModel")
    public ValueModel<Integer> getPresence() {
        return presence;
    }

    /**
     * Get ext Value Model
     *
     * @return Value Model of ext
     */
    @NotNull
    public ValueModel<ApiMapValue> getExt() {
        return ext;
    }

    /**
     * Subscribe for GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("subscribeWithListener:")
    public synchronized void subscribe(@NotNull ModelChangedListener<GroupVM> listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    /**
     * Subscribe for GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("subscribeWithListener:withNotify:")
    public synchronized void subscribe(@NotNull ModelChangedListener<GroupVM> listener, boolean notify) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        if (notify) {
            listener.onChanged(this);
        }
    }

    /**
     * Unsubscribe from GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("unsubscribeWithListener:")
    public synchronized void unsubscribe(@NotNull ModelChangedListener<GroupVM> listener) {
        listeners.remove(listener);
    }

    //
    // Update handling
    //

    @Override
    protected void updateValues(@NotNull Group rawObj) {
        boolean isChanged = name.change(rawObj.getTitle());

        isChanged |= avatar.change(rawObj.getAvatar());
        isChanged |= membersCount.change(rawObj.getMembersCount());
        isChanged |= isMember.change(rawObj.isMember());
        isChanged |= isCanWriteMessage.change(rawObj.isCanSendMessage());

        isChanged |= theme.change(rawObj.getTopic());
        isChanged |= about.change(rawObj.getAbout());
        isChanged |= members.change(new HashSet<>(rawObj.getMembers()));
        isChanged |= ownerId.change(rawObj.getOwnerId());
        isChanged |= isCanViewMembers.change(rawObj.isCanViewMembers());
        isChanged |= isCanInviteMembers.change(rawObj.isCanInviteMembers());
        isChanged |= isCanEditInfo.change(rawObj.isCanEditInfo());
        isChanged |= shortName.change(rawObj.getShortName());
        isChanged |= isAsyncMembers.change(rawObj.isAsyncMembers());
        isChanged |= isHistoryShared.change(rawObj.isSharedHistory());
        isChanged |= isCanEditAdministration.change(rawObj.isCanEditAdministration());
        isChanged |= isCanEditAdmins.change(rawObj.isCanEditAdmins());
        isChanged |= isCanViewAdmins.change(rawObj.isCanViewAdmins());
        isChanged |= isCanLeave.change(rawObj.isCanLeave());
        isChanged |= isCanDelete.change(rawObj.isCanDelete());
        isChanged |= isCanInviteViaLink.change(rawObj.isCanInviteViaLink());
        isChanged |= isCanKickInvited.change(rawObj.isCanKickInvited());
        isChanged |= isCanKickAnyone.change(rawObj.isCanKickAnyone());
        isChanged |= isCanEditForeign.change(rawObj.isCanEditForeign());
        isChanged |= isCanDeleteForeign.change(rawObj.isCanDeleteForeign());
        isChanged |= isDeleted.change(rawObj.isDeleted());
        isChanged |= isCanClear.change(rawObj.isCanClear());
        isChanged |= isCanViewInfo.change(rawObj.isCanViewInfo());
        isChanged |= isCanJoin.change(rawObj.isCanJoin());
        isChanged |= isCanCall.change(rawObj.isCanCall());
        isChanged |= ext.change(rawObj.getExt());

        if (isChanged) {
            notifyIfNeeded();
        }
    }

    private synchronized void notifyIfNeeded() {
        if (listeners.size() > 0) {
            notifyChange();
        }
    }

    private void notifyChange() {
        im.actor.runtime.Runtime.postToMainThread(() -> {
            for (ModelChangedListener<GroupVM> l : listeners) {
                l.onChanged(GroupVM.this);
            }
        });
    }
}