/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.model.annotation.MainThread;
import im.actor.model.entity.Group;
import im.actor.model.entity.GroupMember;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.mvvm.generics.AvatarValueModel;
import im.actor.model.mvvm.generics.BooleanValueModel;
import im.actor.model.mvvm.generics.StringValueModel;

/**
 * Group View Model
 */
public class GroupVM extends BaseValueModel<Group> {

    private int id;
    private int creatorId;
    @NotNull
    private AvatarValueModel avatar;
    @NotNull
    private StringValueModel name;
    @NotNull
    private BooleanValueModel isMember;
    @NotNull
    private ValueModel<HashSet<GroupMember>> members;
    @NotNull
    private ValueModel<Integer> presence;
    @NotNull
    private ArrayList<ModelChangedListener<GroupVM>> listeners = new ArrayList<ModelChangedListener<GroupVM>>();

    /**
     * <p>INTERNAL API</p>
     * Create Group View Model
     *
     * @param rawObj initial value of Group
     */
    public GroupVM(@NotNull Group rawObj) {
        super(rawObj);
        this.id = rawObj.getGroupId();
        this.creatorId = rawObj.getAdminId();
        this.name = new StringValueModel("group." + id + ".title", rawObj.getTitle());
        this.avatar = new AvatarValueModel("group." + id + ".avatar", rawObj.getAvatar());
        this.isMember = new BooleanValueModel("group." + id + ".isMember", rawObj.isMember());
        this.members = new ValueModel<HashSet<GroupMember>>("group." + id + ".members", new HashSet<GroupMember>(rawObj.getMembers()));
        this.presence = new ValueModel<Integer>("group." + id + ".presence", 0);
    }

    /**
     * Get Group Id
     *
     * @return Group Id
     */
    @ObjectiveCName("getId")
    public int getId() {
        return id;
    }

    /**
     * Get Group creator user id
     *
     * @return creator user id
     */
    @ObjectiveCName("getCreatorId")
    public int getCreatorId() {
        return creatorId;
    }

    /**
     * Get Group members count
     *
     * @return members count
     */
    @ObjectiveCName("getMembersCount")
    public int getMembersCount() {
        return members.get().size();
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

    @Override
    protected void updateValues(@NotNull Group rawObj) {
        boolean isChanged = name.change(rawObj.getTitle());
        isChanged |= avatar.change(rawObj.getAvatar());
        isChanged |= isMember.change(rawObj.isMember());
        isChanged |= members.change(new HashSet<GroupMember>(rawObj.getMembers()));

        if (isChanged) {
            notifyChange();
        }
    }

    /**
     * Subscribe for GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("subscribeWithListener:")
    public void subscribe(@NotNull ModelChangedListener<GroupVM> listener) {
        MVVMEngine.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    /**
     * Unsubscribe from GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("unsubscribeWithListener:")
    public void unsubscribe(@NotNull ModelChangedListener<GroupVM> listener) {
        MVVMEngine.checkMainThread();
        listeners.remove(listener);
    }

    private void notifyChange() {
        MVVMEngine.getMainThreadProvider().postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<GroupVM> l : listeners.toArray(new ModelChangedListener[listeners.size()])) {
                    l.onChanged(GroupVM.this);
                }
            }
        });
    }
}