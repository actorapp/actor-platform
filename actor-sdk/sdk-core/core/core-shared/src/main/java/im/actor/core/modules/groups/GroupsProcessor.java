/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.groups;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiMember;
import im.actor.core.api.updates.UpdateGroupAboutChanged;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTopicChanged;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserLeave;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.content.ServiceGroupAvatarChanged;
import im.actor.core.entity.content.ServiceGroupCreated;
import im.actor.core.entity.content.ServiceGroupTitleChanged;
import im.actor.core.entity.content.ServiceGroupUserInvited;
import im.actor.core.entity.content.ServiceGroupUserKicked;
import im.actor.core.entity.content.ServiceGroupUserLeave;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.entity.EntityConverter;
import im.actor.core.modules.messaging.router.RouterInt;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.promise.Promise;

public class GroupsProcessor extends AbsModule implements SequenceProcessor {

    public GroupsProcessor(ModuleContext context) {
        super(context);
    }

    @Verified
    public void onGroupInvite(int groupId, long rid, int inviterId, long date, boolean isSilent) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            // Updating group
            groups().addOrUpdateItem(group
                    .addMember(myUid(), inviterId, date));

            if (!isSilent) {
                if (inviterId == myUid()) {
                    // If current user invite himself, add create group message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.UNKNOWN, ServiceGroupCreated.create());
                    getRouter().onNewMessage(group.peer(), message);
                } else {
                    // else add invite message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.SENT, ServiceGroupUserInvited.create(myUid()));
                    getRouter().onNewMessage(group.peer(), message);
                }
            }
        }
    }

    @Verified
    public void onUserLeave(int groupId, long rid, int uid, long date, boolean isSilent) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            if (uid == myUid()) {
                // If current user leave, clear members and change member state
                groups().addOrUpdateItem(group
                        .clearMembers());
            } else {
                // else remove leaved user
                groups().addOrUpdateItem(group
                        .removeMember(uid));
            }

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, uid,
                        uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupUserLeave.create());
                getRouter().onNewMessage(group.peer(), message);
            }
        }
    }

    @Verified
    public void onUserKicked(int groupId, long rid, int uid, int kicker, long date, boolean isSilent) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            if (uid == myUid()) {
                // If kicked me, clear members and change member state
                groups().addOrUpdateItem(group
                        .clearMembers());
            } else {
                // else remove kicked user
                groups().addOrUpdateItem(group
                        .removeMember(uid));
            }

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, kicker,
                        kicker == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupUserKicked.create(uid));
                getRouter().onNewMessage(group.peer(), message);
            }
        }
    }

    @Verified
    public void onUserAdded(int groupId, long rid, int uid, int adder, long date, boolean isSilent) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            // Adding member
            groups().addOrUpdateItem(group.addMember(uid, adder, date));

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, adder,
                        adder == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupUserInvited.create(uid));
                getRouter().onNewMessage(group.peer(), message);
            }
        }
    }

    @Verified
    public void onTitleChanged(int groupId, long rid, int uid, String title, long date,
                               boolean isSilent) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            // We can't just ignore not changed avatar
            // because we need to make message in conversation
            // about avatar change

            if (!group.getTitle().equals(title)) {
                // Change group title
                Group upd = group.editTitle(title);

                // Update group
                groups().addOrUpdateItem(upd);

                // Notify about group change
                onGroupDescChanged(upd);
            }

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, uid,
                        uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupTitleChanged.create(title));
                getRouter().onNewMessage(group.peer(), message);
            }
        }
    }

    @Verified
    public void onTopicChanged(int groupId, String topic) {
        Group group = groups().getValue(groupId);
        if (group != null) {


            if (group.getTheme() == null || !group.getTheme().equals(topic)) {
                // Change group title
                Group upd = group.editTheme(topic);

                // Update group
                groups().addOrUpdateItem(upd);

                // Notify about group change
                onGroupDescChanged(upd);
            }
        }
    }

    @Verified
    public void onAboutChanged(int groupId, String about) {
        Group group = groups().getValue(groupId);
        if (group != null) {


            if (group.getAbout() == null || !group.getAbout().equals(about)) {
                // Change group title
                Group upd = group.editAbout(about);

                // Update group
                groups().addOrUpdateItem(upd);

                // Notify about group change
                onGroupDescChanged(upd);
            }


        }
    }

    @Verified
    public void onAvatarChanged(int groupId, long rid, int uid, @Nullable ApiAvatar avatar, long date,
                                boolean isSilent) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            // We can't just ignore not changed avatar
            // because we need to make message in conversation
            // about avatar change

            // Check is disabled because it is unable to compare
            // without losing future compatibility
            // if (!equalsE(group.getRawAvatar(), avatar)) {

            // Change group avatar
            Group upd = group.editAvatar(avatar);

            // Update group
            groups().addOrUpdateItem(upd);

            // Notify about group change
            onGroupDescChanged(upd);

            // }

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, uid,
                        uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupAvatarChanged.create(avatar));
                getRouter().onNewMessage(group.peer(), message);
            }
        }
    }


    @Verified
    public void onMembersUpdated(int groupId, List<ApiMember> members) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            // Updating members list
            group = group.updateMembers(members);

            // Update group
            groups().addOrUpdateItem(group);
        }
    }

    @Verified
    private void onGroupDescChanged(Group group) {
        getRouter().onGroupChanged(group);
    }

    private RouterInt getRouter() {
        return context().getMessagesModule().getRouter();
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateGroupTitleChanged) {
            UpdateGroupTitleChanged titleChanged = (UpdateGroupTitleChanged) update;
            onTitleChanged(titleChanged.getGroupId(), titleChanged.getRid(),
                    titleChanged.getUid(), titleChanged.getTitle(), titleChanged.getDate(),
                    false);
            return Promise.success(null);
        } else if (update instanceof UpdateGroupTopicChanged) {
            UpdateGroupTopicChanged topicChanged = (UpdateGroupTopicChanged) update;
            onTopicChanged(topicChanged.getGroupId(), topicChanged.getTopic());
            return Promise.success(null);
        } else if (update instanceof UpdateGroupAboutChanged) {
            UpdateGroupAboutChanged aboutChanged = (UpdateGroupAboutChanged) update;
            onAboutChanged(aboutChanged.getGroupId(), aboutChanged.getAbout());
            return Promise.success(null);
        } else if (update instanceof UpdateGroupAvatarChanged) {
            UpdateGroupAvatarChanged avatarChanged = (UpdateGroupAvatarChanged) update;
            onAvatarChanged(avatarChanged.getGroupId(), avatarChanged.getRid(),
                    avatarChanged.getUid(), avatarChanged.getAvatar(),
                    avatarChanged.getDate(), false);
            return Promise.success(null);
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            onGroupInvite(groupInvite.getGroupId(),
                    groupInvite.getRid(), groupInvite.getInviteUid(), groupInvite.getDate(),
                    false);
            return Promise.success(null);
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) update;
            onUserLeave(leave.getGroupId(), leave.getRid(), leave.getUid(),
                    leave.getDate(), false);
            return Promise.success(null);
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick userKick = (UpdateGroupUserKick) update;
            onUserKicked(userKick.getGroupId(),
                    userKick.getRid(), userKick.getUid(), userKick.getKickerUid(), userKick.getDate(),
                    false);
            return Promise.success(null);
        } else if (update instanceof UpdateGroupUserInvited) {
            UpdateGroupUserInvited userInvited = (UpdateGroupUserInvited) update;
            onUserAdded(userInvited.getGroupId(),
                    userInvited.getRid(), userInvited.getUid(), userInvited.getInviterUid(), userInvited.getDate(),
                    false);
            return Promise.success(null);
        } else if (update instanceof UpdateGroupMembersUpdate) {
            onMembersUpdated(((UpdateGroupMembersUpdate) update).getGroupId(),
                    ((UpdateGroupMembersUpdate) update).getMembers());
            return Promise.success(null);
        }
        return null;
    }
}