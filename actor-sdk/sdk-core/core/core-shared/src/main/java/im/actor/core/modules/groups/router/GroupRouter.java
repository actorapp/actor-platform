package im.actor.core.modules.groups.router;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import im.actor.core.api.ApiAvatar;
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
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.groups.router.entity.GroupUpdate;
import im.actor.core.modules.messaging.router.RouterInt;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;

public class GroupRouter extends ModuleActor {

    private boolean isFreezed = false;

    public GroupRouter(ModuleContext context) {
        super(context);
    }

    @Verified
    public Promise<Void> onGroupInvite(int groupId, long rid, int inviterId, long date, boolean isSilent) {
        return forGroup(groupId, g -> {
            // Updating group
            groups().addOrUpdateItem(g
                    .addMember(myUid(), inviterId, date));

            if (!isSilent) {
                if (inviterId == myUid()) {
                    // If current user invite himself, add create group message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.UNKNOWN, ServiceGroupCreated.create());
                    return getRouter().onNewMessage(g.peer(), message);
                } else {
                    // else add invite message
                    Message message = new Message(rid, date, date, inviterId,
                            MessageState.SENT, ServiceGroupUserInvited.create(myUid()));
                    return getRouter().onNewMessage(g.peer(), message);
                }
            }

            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onUserLeave(int groupId, long rid, int uid, long date, boolean isSilent) {
        return forGroup(groupId, group -> {
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
                return getRouter().onNewMessage(group.peer(), message);
            }

            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onUserKicked(int groupId, long rid, int uid, int kicker, long date, boolean isSilent) {
        return forGroup(groupId, group -> {

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
                return getRouter().onNewMessage(group.peer(), message);
            }
            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onUserAdded(int groupId, long rid, int uid, int adder, long date, boolean isSilent) {
        return forGroup(groupId, group -> {

            // Adding member
            groups().addOrUpdateItem(group.addMember(uid, adder, date));

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, adder,
                        adder == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupUserInvited.create(uid));
                return getRouter().onNewMessage(group.peer(), message);
            }
            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onTitleChanged(int groupId, long rid, int uid, String title, long date,
                                        boolean isSilent) {
        return forGroup(groupId, group -> {

            // Change group title
            Group upd = group.editTitle(title);

            // Update group
            groups().addOrUpdateItem(upd);

            // Notify about group change
            Promise<Void> src = onGroupDescChanged(upd);

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, uid,
                        uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupTitleChanged.create(title));
                src = src.chain(v -> getRouter().onNewMessage(group.peer(), message));
            }
            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onTopicChanged(int groupId, String topic) {
        return forGroup(groupId, group -> {

            // Change group title
            Group upd = group.editTheme(topic);

            // Update group
            groups().addOrUpdateItem(upd);

            // Notify about group change
            return onGroupDescChanged(upd);
        });
    }

    @Verified
    public Promise<Void> onAboutChanged(int groupId, String about) {
        return forGroup(groupId, group -> {

            // Change group title
            Group upd = group.editAbout(about);

            // Update group
            groups().addOrUpdateItem(upd);

            return Promise.success(null);
        });
    }

    @Verified
    public Promise<Void> onAvatarChanged(int groupId, long rid, int uid, @Nullable ApiAvatar avatar, long date,
                                         boolean isSilent) {

        return forGroup(groupId, group -> {

            // Change group avatar
            Group upd = group.editAvatar(avatar);

            // Update group
            groups().addOrUpdateItem(upd);

            // Notify about group change
            Promise<Void> src = onGroupDescChanged(upd);

            // Create message if needed
            if (!isSilent) {
                Message message = new Message(rid, date, date, uid,
                        uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                        ServiceGroupAvatarChanged.create(avatar));
                src.chain(v -> getRouter().onNewMessage(group.peer(), message));
            }
            return src;
        });
    }


    @Verified
    public Promise<Void> onMembersUpdated(int groupId, List<ApiMember> members) {
        return forGroup(groupId, group -> {

            // Updating members list
            group = group.updateMembers(members);

            // Update group
            groups().addOrUpdateItem(group);

            return Promise.success(null);
        });
    }

    private Promise<Void> forGroup(int groupId, Function<Group, Promise<Void>> func) {
        isFreezed = true;
        return groups().getValueAsync(groupId)
                .fallback(e -> null)
                .flatMap(g -> {
                    if (g != null) {
                        return func.apply(g);
                    }
                    return Promise.success(null);
                })
                .then(v -> {
                    isFreezed = false;
                    unstashAll();
                });
    }

    @Verified
    private Promise<Void> onGroupDescChanged(Group group) {
        return getRouter().onGroupChanged(group);
    }

    private RouterInt getRouter() {
        return context().getMessagesModule().getRouter();
    }


    //
    // Messages
    //

    private Promise<Void> onUpdate(Update update) {
        if (update instanceof UpdateGroupTitleChanged) {
            UpdateGroupTitleChanged titleChanged = (UpdateGroupTitleChanged) update;
            return onTitleChanged(titleChanged.getGroupId(), titleChanged.getRid(),
                    titleChanged.getUid(), titleChanged.getTitle(), titleChanged.getDate(),
                    false);
        } else if (update instanceof UpdateGroupTopicChanged) {
            UpdateGroupTopicChanged topicChanged = (UpdateGroupTopicChanged) update;
            return onTopicChanged(topicChanged.getGroupId(), topicChanged.getTopic());
        } else if (update instanceof UpdateGroupAboutChanged) {
            UpdateGroupAboutChanged aboutChanged = (UpdateGroupAboutChanged) update;
            return onAboutChanged(aboutChanged.getGroupId(), aboutChanged.getAbout());
        } else if (update instanceof UpdateGroupAvatarChanged) {
            UpdateGroupAvatarChanged avatarChanged = (UpdateGroupAvatarChanged) update;
            return onAvatarChanged(avatarChanged.getGroupId(), avatarChanged.getRid(),
                    avatarChanged.getUid(), avatarChanged.getAvatar(),
                    avatarChanged.getDate(), false);
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            return onGroupInvite(groupInvite.getGroupId(),
                    groupInvite.getRid(), groupInvite.getInviteUid(), groupInvite.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) update;
            return onUserLeave(leave.getGroupId(), leave.getRid(), leave.getUid(),
                    leave.getDate(), false);
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick userKick = (UpdateGroupUserKick) update;
            return onUserKicked(userKick.getGroupId(),
                    userKick.getRid(), userKick.getUid(), userKick.getKickerUid(), userKick.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserInvited) {
            UpdateGroupUserInvited userInvited = (UpdateGroupUserInvited) update;
            return onUserAdded(userInvited.getGroupId(),
                    userInvited.getRid(), userInvited.getUid(), userInvited.getInviterUid(), userInvited.getDate(),
                    false);
        } else if (update instanceof UpdateGroupMembersUpdate) {
            return onMembersUpdated(((UpdateGroupMembersUpdate) update).getGroupId(),
                    ((UpdateGroupMembersUpdate) update).getMembers());
        }
        return Promise.success(null);
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof GroupUpdate) {
            if (isFreezed) {
                stash();
                return null;
            }
            return onUpdate(((GroupUpdate) message).getUpdate());
        } else {
            return super.onAsk(message);
        }
    }
}
