package im.actor.model.modules.updates;

import im.actor.model.Messenger;
import im.actor.model.api.ContactRecord;
import im.actor.model.api.Group;
import im.actor.model.api.PeerType;
import im.actor.model.api.User;
import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.api.updates.*;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.updates.internal.DialogHistoryLoaded;
import im.actor.model.modules.updates.internal.InternalUpdate;
import im.actor.model.modules.updates.internal.LoggedIn;
import im.actor.model.network.parser.Update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class UpdateProcessor {

    private static final String TAG = "Updates";

    private Modules modules;

    private UsersProcessor usersProcessor;
    private MessagesProcessor messagesProcessor;
    private GroupsProcessor groupsProcessor;
    private PresenceProcessor presenceProcessor;
    private TypingProcessor typingProcessor;

    public UpdateProcessor(Modules modules) {
        this.modules = modules;
        this.usersProcessor = new UsersProcessor(modules);
        this.messagesProcessor = new MessagesProcessor(modules);
        this.groupsProcessor = new GroupsProcessor();
        this.presenceProcessor = new PresenceProcessor(modules);
        this.typingProcessor = new TypingProcessor(modules);
    }

    public void applyRelated(List<User> users,
                             List<Group> groups,
                             List<ContactRecord> contactRecords,
                             boolean force) {
        usersProcessor.applyUsers(users, force);
    }

    public void processInternalUpdate(InternalUpdate update) {
        if (update instanceof DialogHistoryLoaded) {
            ResponseLoadDialogs dialogs = ((DialogHistoryLoaded) update).getDialogs();
            applyRelated(dialogs.getUsers(), dialogs.getGroups(),
                    null, false);
            messagesProcessor.onDialogsLoaded(dialogs);
        } else if (update instanceof LoggedIn) {
            ArrayList<User> users = new ArrayList<User>();
            users.add(((LoggedIn) update).getAuth().getUser());
            applyRelated(users, new ArrayList<Group>(), ((LoggedIn) update).getAuth().getContacts(), true);
            modules.getConfiguration().getMainThread().runOnUiThread(((LoggedIn) update).getRunnable());
        }
    }

    public void processUpdate(Update update) {
        Log.d(TAG, update + "");
        if (update instanceof UpdateUserContactAdded) {
            // TODO: Implement
        } else if (update instanceof UpdateUserContactRemoved) {
            // TODO: Implement
        } else if (update instanceof UpdateContactMoved) {
            // TODO: Implement
        } else if (update instanceof UpdateContactTitleChanged) {
            // TODO: Implement
        } else if (update instanceof UpdateUserContactsChanged) {
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
            // TODO: Remove
        } else if (update instanceof UpdateMessage) {
            UpdateMessage message = (UpdateMessage) update;
            messagesProcessor.onMessage(message.getPeer(), message.getSenderUid(), message.getDate(), message.getRid(),
                    message.getMessage());
            typingProcessor.onMessage(message.getPeer(), message.getSenderUid());
        } else if (update instanceof UpdateMessageRead) {
            UpdateMessageRead messageRead = (UpdateMessageRead) update;
            messagesProcessor.onMessageRead(messageRead.getPeer(), messageRead.getStartDate(), messageRead.getReadDate());
        } else if (update instanceof UpdateMessageReadByMe) {
            UpdateMessageReadByMe messageReadByMe = (UpdateMessageReadByMe) update;
            messagesProcessor.onMessageReadByMe(messageReadByMe.getPeer(), messageReadByMe.getStartDate());
        } else if (update instanceof UpdateMessageReceived) {
            UpdateMessageReceived received = (UpdateMessageReceived) update;
            messagesProcessor.onMessageReceived(received.getPeer(), received.getStartDate(), received.getReceivedDate());
        } else if (update instanceof UpdateMessageDelete) {
            UpdateMessageDelete messageDelete = (UpdateMessageDelete) update;
            messagesProcessor.onMessageDelete(messageDelete.getPeer(), messageDelete.getRids());
        } else if (update instanceof UpdateMessageSent) {
            UpdateMessageSent messageSent = (UpdateMessageSent) update;
            messagesProcessor.onMessageSent(messageSent.getPeer(), messageSent.getRid(), messageSent.getDate());
        } else if (update instanceof UpdateEncryptedMessage) {
            UpdateEncryptedMessage encryptedMessage = (UpdateEncryptedMessage) update;
            // TODO: Implement
            typingProcessor.onMessage(encryptedMessage.getPeer(), encryptedMessage.getSenderUid());
        } else if (update instanceof UpdateEncryptedRead) {
            UpdateEncryptedRead encryptedRead = (UpdateEncryptedRead) update;
            messagesProcessor.onMessageEncryptedRead(encryptedRead.getPeer(), encryptedRead.getRid(), encryptedRead.getReadDate());
        } else if (update instanceof UpdateEncryptedReadByMe) {
            UpdateEncryptedReadByMe encryptedRead = (UpdateEncryptedReadByMe) update;
            messagesProcessor.onMessageEncryptedReadByMe(encryptedRead.getPeer(), encryptedRead.getRid());
        } else if (update instanceof UpdateEncryptedReceived) {
            UpdateEncryptedReceived received = (UpdateEncryptedReceived) update;
            messagesProcessor.onMessageEncryptedReceived(received.getPeer(), received.getRid(), received.getReceivedDate());
        } else if (update instanceof UpdateChatClear) {
            UpdateChatClear chatClear = (UpdateChatClear) update;
            messagesProcessor.onChatClear(chatClear.getPeer());
        } else if (update instanceof UpdateChatDelete) {
            UpdateChatDelete chatDelete = (UpdateChatDelete) update;
            messagesProcessor.onChatDelete(chatDelete.getPeer());
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered registered = (UpdateContactRegistered) update;
            if (!registered.isSilent()) {
                messagesProcessor.onUserRegistered(registered.getUid(), registered.getDate());
            }
        } else if (update instanceof UpdateUserOnline) {
            UpdateUserOnline userOnline = (UpdateUserOnline) update;
            presenceProcessor.onUserOnline(userOnline.getUid());
        } else if (update instanceof UpdateUserOffline) {
            UpdateUserOffline offline = (UpdateUserOffline) update;
            presenceProcessor.onUserOffline(offline.getUid());
        } else if (update instanceof UpdateUserLastSeen) {
            UpdateUserLastSeen lastSeen = (UpdateUserLastSeen) update;
            presenceProcessor.onUserLastSeen(lastSeen.getUid(), lastSeen.getDate());
        } else if (update instanceof UpdateGroupOnline) {
            UpdateGroupOnline groupOnline = (UpdateGroupOnline) update;
            presenceProcessor.onGroupOnline(groupOnline.getGroupId(), groupOnline.getCount());
        } else if (update instanceof UpdateTyping) {
            UpdateTyping typing = (UpdateTyping) update;
            typingProcessor.onTyping(typing.getPeer(), typing.getUid(), typing.getTypingType());
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
