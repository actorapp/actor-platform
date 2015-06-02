/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.updates;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import im.actor.model.annotation.Verified;
import im.actor.model.api.Member;
import im.actor.model.entity.Group;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;

import im.actor.model.entity.content.ServiceGroupUserInvited;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.entity.EntityConverter;

import static im.actor.model.util.JavaUtil.equalsE;

public class GroupsProcessor extends BaseModule {

    public GroupsProcessor(Modules modules) {
        super(modules);
    }

    @Verified
    public void applyGroups(Collection<im.actor.model.api.Group> updated, boolean forced) {
        ArrayList<Group> batch = new ArrayList<Group>();
        for (im.actor.model.api.Group group : updated) {
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
                        ServiceGroupUserInvited.create(myUid()));
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
    public void onAvatarChanged(int groupId, long rid, int uid, @Nullable im.actor.model.api.Avatar avatar, long date,
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
    public void onMembersUpdated(int groupId, List<Member> members) {
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
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.GroupChanged(group));
    }
}