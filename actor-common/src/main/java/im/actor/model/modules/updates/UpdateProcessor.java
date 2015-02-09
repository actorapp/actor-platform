package im.actor.model.modules.updates;

import im.actor.model.Messenger;
import im.actor.model.api.ContactRecord;
import im.actor.model.api.Group;
import im.actor.model.api.PeerType;
import im.actor.model.api.User;
import im.actor.model.api.updates.*;
import im.actor.model.network.parser.Update;

import java.util.HashSet;
import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class UpdateProcessor {

    private Messenger messenger;
    private UsersProcessor usersProcessor;
    private MessagesProcessor messagesProcessor;
    private GroupsProcessor groupsProcessor;

    public UpdateProcessor(Messenger messenger) {
        this.messenger = messenger;
        this.usersProcessor = new UsersProcessor(messenger);
        this.messagesProcessor = new MessagesProcessor();
        this.groupsProcessor = new GroupsProcessor();
    }

    public void applyRelated(List<User> users,
                             List<Group> groups,
                             List<ContactRecord> contactRecords,
                             boolean force) {
        usersProcessor.applyUsers(users, force);
    }

    public void processUpdate(Update update) {
        // Users updates
        if (update instanceof UpdateUserContactAdded) {
            // TODO: Implement
        } else if (update instanceof UpdateUserContactRemoved) {
            // TODO: Implement
        } else if (update instanceof UpdateUserNameChanged) {
            UpdateUserNameChanged userNameChanged = (UpdateUserNameChanged) update;
            usersProcessor.onUserNameChanged(userNameChanged.getUid(), userNameChanged.getName());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            usersProcessor.onUserLocalNameChanged(localNameChanged.getUid(), localNameChanged.getLocalName());
        } else if (update instanceof UpdateUserAvatarChanged) {
            UpdateUserAvatarChanged avatarChanged = (UpdateUserAvatarChanged) update;
            usersProcessor.onUserAvatarChanged(avatarChanged.getUid(), avatarChanged.getAvatar());
        } else if (update instanceof UpdateUserStateChanged) {
            UpdateUserStateChanged stateChanged = (UpdateUserStateChanged) update;
            usersProcessor.onUserStateChanged(stateChanged.getUid(), stateChanged.getState());
        }
    }


    public boolean isCausesInvalidation(Update update) {
        HashSet<Integer> users = new HashSet<Integer>();
        HashSet<Integer> groups = new HashSet<Integer>();
        HashSet<Integer> contacts = new HashSet<Integer>();

        // TODO: Improve list
        if (update instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) update;
            users.add(updateMessage.getSenderUid());
            if (updateMessage.getPeer().getType() == PeerType.GROUP) {
                groups.add(updateMessage.getPeer().getId());
            }
            if (updateMessage.getPeer().getType() == PeerType.PRIVATE) {
                users.add(updateMessage.getPeer().getId());
            }
        } else if (update instanceof UpdateEncryptedMessage) {
            UpdateEncryptedMessage updateMessage = (UpdateEncryptedMessage) update;
            users.add(updateMessage.getSenderUid());
            if (updateMessage.getPeer().getType() == PeerType.GROUP) {
                groups.add(updateMessage.getPeer().getId());
            }
            if (updateMessage.getPeer().getType() == PeerType.PRIVATE) {
                users.add(updateMessage.getPeer().getId());
            }
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered contactRegistered = (UpdateContactRegistered) update;
            users.add(contactRegistered.getUid());
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            users.add(groupInvite.getInviteUid());
            groups.add(groupInvite.getGroupId());
        } else if (update instanceof UpdateGroupUserAdded) {
            UpdateGroupUserAdded added = (UpdateGroupUserAdded) update;
            users.add(added.getInviterUid());
            users.add(added.getUid());
            groups.add(added.getGroupId());
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick kick = (UpdateGroupUserKick) update;
            users.add(kick.getKickerUid());
            users.add(kick.getUid());
            groups.add(kick.getGroupId());
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) update;
            users.add(leave.getUid());
            groups.add(leave.getGroupId());
        } else if (update instanceof UpdateContactsAdded) {
            users.addAll(((UpdateContactsAdded) update).getUids());
        } else if (update instanceof UpdateContactsRemoved) {
            users.addAll(((UpdateContactsRemoved) update).getUids());
        } else if (update instanceof UpdateUserContactAdded) {
            UpdateUserContactAdded contactAdded = (UpdateUserContactAdded) update;
            users.add(contactAdded.getUid());
            contacts.add(contactAdded.getContactId());
        } else if (update instanceof UpdateUserContactRemoved) {
            UpdateUserContactRemoved contactAdded = (UpdateUserContactRemoved) update;
            users.add(contactAdded.getUid());
            contacts.add(contactAdded.getContactId());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            users.add(localNameChanged.getUid());
        }

        if (!usersProcessor.hasUsers(users)) {
            return true;
        }

        return false;
    }
}
