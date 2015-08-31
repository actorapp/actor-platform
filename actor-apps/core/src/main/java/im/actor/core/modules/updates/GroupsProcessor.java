/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiMember;
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
import im.actor.core.modules.internal.messages.DialogsActor;
import im.actor.core.modules.internal.messages.entity.EntityConverter;
import im.actor.runtime.annotations.Verified;

import static im.actor.core.util.JavaUtil.equalsE;

public class GroupsProcessor extends AbsModule {

    public GroupsProcessor(ModuleContext context) {
        super(context);
    }

    @Verified
    public void applyGroups(Collection<ApiGroup> updated, boolean forced) {
        ArrayList<Group> batch = new ArrayList<Group>();
        for (ApiGroup group : updated) {
            Group saved = groups().getValue(group.getId());
            if (saved == null) {
                batch.add(EntityConverter.convert(group));
            } else if (forced) {
                Group upd = EntityConverter.convert(group);
                batch.add(upd);

                // Sending changes to dialogs
                if (!equalsE(upd.getAvatar(), saved.getAvatar()) ||
                        !upd.getTitle().equals(saved.getTitle())) {
                    onGroupDescChanged(upd);
                }
            }
        }

        if (batch.size() > 0) {
            groups().addOrUpdateItems(batch);
        }
    }

    @Verified
    public void onGroupInvite(int groupId, long rid, int inviterId, long date, boolean isSilent) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            // Updating group
            groups().addOrUpdateItem(group
                    .changeMember(true)
                    .addMember(myUid(), inviterId, date));

            if (!isSilent) {
                if (inviterId == myUid()) {
                    // If current user invite himself, add create group message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.UNKNOWN, ServiceGroupCreated.create());
                    conversationActor(group.peer()).send(message);
                } else {
                    // else add invite message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.SENT, ServiceGroupUserInvited.create(myUid()));
                    conversationActor(group.peer()).send(message);
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
                        .clearMembers()
                        .changeMember(false));
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
                conversationActor(group.peer()).send(message);
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
                        .clearMembers()
                        .changeMember(false));
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
                conversationActor(group.peer()).send(message);
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
                conversationActor(group.peer()).send(message);
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
                conversationActor(group.peer()).send(message);
            }
        }
    }

    @Verified
    public void onTopicChanged(int groupId, String topic) {
        Group group = groups().getValue(groupId);
        if (group != null) {

            // We can't just ignore not changed avatar
            // because we need to make message in conversation
            // about avatar change

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

            // We can't just ignore not changed avatar
            // because we need to make message in conversation
            // about avatar change

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
                conversationActor(group.peer()).send(message);
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
    public boolean hasGroups(Collection<Integer> gids) {
        for (Integer uid : gids) {
            if (groups().getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }

    @Verified
    private void onGroupDescChanged(Group group) {
        context().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.GroupChanged(group));
    }
}