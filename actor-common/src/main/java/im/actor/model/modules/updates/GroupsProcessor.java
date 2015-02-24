package im.actor.model.modules.updates;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.Group;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;
import im.actor.model.entity.content.ServiceGroupUserAdded;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.entity.EntityConverter;

import static im.actor.model.util.JavaUtil.equalsE;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class GroupsProcessor extends BaseModule {

    public GroupsProcessor(Modules modules) {
        super(modules);
    }

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
                    modules().getMessagesModule().getDialogsActor()
                            .send(new DialogsActor.GroupChanged(upd));
                }
            }
        }

        if (batch.size() > 0) {
            groups().addOrUpdateItems(batch);
        }
    }

    public void onGroupInvite(int groupId, long rid, int inviterId, long date) {
        Group group = groups().getValue(groupId);
        if (group == null) {
            return;
        }

        groups().addOrUpdateItem(group
                .changeMember(true)
                .addMember(myUid(), inviterId, date, inviterId == myUid()));

        if (inviterId == myUid()) {
            Message message = new Message(rid, date, date, inviterId,
                    MessageState.UNKNOWN, new ServiceGroupCreated(group.getTitle()));
            conversationActor(group.peer()).send(message);
        } else {
            Message message = new Message(rid, date, date, inviterId,
                    MessageState.SENT, new ServiceGroupUserAdded(myUid()));
            conversationActor(group.peer()).send(message);
        }
    }

    public void onUserLeave(int groupId, long rid, int uid, long date) {
        Group group = groups().getValue(groupId);
        if (group == null) {
            return;
        }

        if (uid == myUid()) {
            groups().addOrUpdateItem(group
                    .clearMembers()
                    .changeMember(false));
        } else {
            groups().addOrUpdateItem(group
                    .removeMember(uid));
        }

        Message message = new Message(rid, date, date, uid,
                uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                new ServiceGroupUserLeave());
        conversationActor(group.peer()).send(message);
    }

    public void onUserKicked(int groupId, long rid, int uid, int kicker, long date) {
        Group group = groups().getValue(groupId);
        if (group == null) {
            return;
        }

        if (uid == myUid()) {
            groups().addOrUpdateItem(group
                    .clearMembers()
                    .changeMember(false));
        } else {
            groups().addOrUpdateItem(group
                    .removeMember(uid));
        }

        Message message = new Message(rid, date, date, kicker,
                kicker == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                new ServiceGroupUserKicked(uid));
        conversationActor(group.peer()).send(message);
    }

    public void onUserAdded(int groupId, long rid, int uid, int adder, long date) {
        Group group = groups().getValue(groupId);
        if (group == null) {
            return;
        }

        groups().addOrUpdateItem(group
                .addMember(uid, adder, date, false));

        Message message = new Message(rid, date, date, adder,
                adder == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                new ServiceGroupUserAdded(uid));
        conversationActor(group.peer()).send(message);
    }

    public void onTitleChanged(int groupId, long rid, int uid, String title, long date) {
        Group group = groups().getValue(groupId);
        if (group == null) {
            return;
        }

        Group upd = group.editTitle(title);
        groups().addOrUpdateItem(upd);
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.GroupChanged(upd));

        Message message = new Message(rid, date, date, uid,
                uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                new ServiceGroupTitleChanged(title));
        conversationActor(group.peer()).send(message);
    }

    public void onAvatarChanged(int groupId, long rid, int uid, Avatar avatar, long date) {
        Group group = groups().getValue(groupId);
        if (group == null) {
            return;
        }

        Group upd = group.editAvatar(avatar);
        groups().addOrUpdateItem(upd);
        modules().getMessagesModule().getDialogsActor()
                .send(new DialogsActor.GroupChanged(upd));

        Message message = new Message(rid, date, date, uid,
                uid == myUid() ? MessageState.SENT : MessageState.UNKNOWN,
                new ServiceGroupAvatarChanged(avatar));
        conversationActor(group.peer()).send(message);
    }

    public boolean hasGroups(Collection<Integer> gids) {
        for (Integer uid : gids) {
            if (groups().getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }
}