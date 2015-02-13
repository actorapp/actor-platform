package im.actor.messenger.core.actors.updates;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;

import im.actor.api.parser.Update;
import im.actor.api.scheme.Peer;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.updates.UpdateChatClear;
import im.actor.api.scheme.updates.UpdateChatDelete;
import im.actor.api.scheme.updates.UpdateContactRegistered;
import im.actor.api.scheme.updates.UpdateContactsAdded;
import im.actor.api.scheme.updates.UpdateContactsRemoved;
import im.actor.api.scheme.updates.UpdateEncryptedMessage;
import im.actor.api.scheme.updates.UpdateEncryptedRead;
import im.actor.api.scheme.updates.UpdateEncryptedReadByMe;
import im.actor.api.scheme.updates.UpdateEncryptedReceived;
import im.actor.api.scheme.updates.UpdateGroupAvatarChanged;
import im.actor.api.scheme.updates.UpdateGroupInvite;
import im.actor.api.scheme.updates.UpdateGroupOnline;
import im.actor.api.scheme.updates.UpdateGroupTitleChanged;
import im.actor.api.scheme.updates.UpdateGroupUserAdded;
import im.actor.api.scheme.updates.UpdateGroupUserKick;
import im.actor.api.scheme.updates.UpdateGroupUserLeave;
import im.actor.api.scheme.updates.UpdateMessage;
import im.actor.api.scheme.updates.UpdateMessageDelete;
import im.actor.api.scheme.updates.UpdateMessageRead;
import im.actor.api.scheme.updates.UpdateMessageReadByMe;
import im.actor.api.scheme.updates.UpdateMessageReceived;
import im.actor.api.scheme.updates.UpdateMessageSent;
import im.actor.api.scheme.updates.UpdateNewDevice;
import im.actor.api.scheme.updates.UpdateRemovedDevice;
import im.actor.api.scheme.updates.UpdateTyping;
import im.actor.api.scheme.updates.UpdateUserAvatarChanged;
import im.actor.api.scheme.updates.UpdateUserLastSeen;
import im.actor.api.scheme.updates.UpdateUserLocalNameChanged;
import im.actor.api.scheme.updates.UpdateUserNameChanged;
import im.actor.api.scheme.updates.UpdateUserOffline;
import im.actor.api.scheme.updates.UpdateUserOnline;
import im.actor.messenger.core.LogTag;
import im.actor.messenger.core.actors.contacts.ContactsActor;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.core.actors.groups.GroupsInt;
import im.actor.messenger.core.actors.messages.InMessagesActor;
import im.actor.messenger.core.actors.messages.InMessagesInt;
import im.actor.messenger.core.actors.presence.GroupPresenceActor;
import im.actor.messenger.core.actors.presence.UsersPresence;
import im.actor.messenger.core.actors.typing.TypingUpdateActor;
import im.actor.messenger.core.actors.users.UserActor;
import im.actor.messenger.util.BoxUtil;
import im.actor.messenger.util.Logger;

import java.util.List;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class UpdateBroker extends Actor {

    private static final String TAG = LogTag.SEQUENCE;

    private InMessagesInt messageReceiver;
    private ActorRef userPresence;
    private ActorRef groupPresence;
    private GroupsInt groupActor;

    public static ActorSelection sequenceBroker() {
        return new ActorSelection(Props.create(UpdateBroker.class)
                .changeDispatcher("updates"), "sequence/broker");
    }

    @Override
    public void preStart() {
        messageReceiver = InMessagesActor.messageReceiver();
        groupActor = GroupsActor.groupUpdates();
        userPresence = UsersPresence.presence();
        groupPresence = GroupPresenceActor.groupPresence();
    }

    @Override
    public void onReceive(Object message) {
        try {
            if (message instanceof Update) {
                Update u = (Update) message;
                onUpdate(u);
            } else if (message instanceof List) {
                for (Update u : (List<Update>) message) {
                    onUpdate(u);
                }
            } else {
                drop(message);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void onUpdate(Update message) {

        // Messages Updates

        if (message instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) message;
            messageReceiver.onMessage(updateMessage.getPeer(), updateMessage.getSenderUid(), updateMessage.getDate(),
                    updateMessage.getRid(), updateMessage.getMessage());
            TypingUpdateActor.typingUpdates().onInMessage(updateMessage.getPeer(), updateMessage.getSenderUid());
            Logger.d(TAG, toString(updateMessage.getPeer()) + " UpdateMessage");
            return;
        }

        if (message instanceof UpdateEncryptedMessage) {
            UpdateEncryptedMessage updateMessage = (UpdateEncryptedMessage) message;
            messageReceiver.onEncryptedMessage(updateMessage.getPeer(), updateMessage.getSenderUid(), updateMessage.getDate(),
                    updateMessage.getAesEncryptedKey(), updateMessage.getMessage());
            TypingUpdateActor.typingUpdates().onInMessage(updateMessage.getPeer(), updateMessage.getSenderUid());
            Logger.d(TAG, toString(updateMessage.getPeer()) + " UpdateEncryptedMessage");
            return;
        }

        if (message instanceof UpdateMessageSent) {
            UpdateMessageSent messageSent = (UpdateMessageSent) message;
            messageReceiver.onMessageSent(messageSent.getPeer(), messageSent.getRid(), messageSent.getDate());
            Logger.d(TAG, toString(messageSent.getPeer()) + " UpdateMessageSent");
            return;
        }

        if (message instanceof UpdateEncryptedReceived) {
            UpdateEncryptedReceived encryptedReceived = (UpdateEncryptedReceived) message;
            messageReceiver.onMessageEncryptedReceived(encryptedReceived.getPeer(), encryptedReceived.getRid());
            Logger.d(TAG, toString(encryptedReceived.getPeer()) + " UpdateEncryptedReceived");
            return;
        }

        if (message instanceof UpdateEncryptedRead) {
            UpdateEncryptedRead updateEncryptedRead = (UpdateEncryptedRead) message;
            messageReceiver.onMessageEncryptedRead(updateEncryptedRead.getPeer(), updateEncryptedRead.getRid());
            Logger.d(TAG, toString(updateEncryptedRead.getPeer()) + " UpdateEncryptedRead");
            return;
        }

        if (message instanceof UpdateEncryptedReadByMe) {
            UpdateEncryptedReadByMe updateEncryptedRead = (UpdateEncryptedReadByMe) message;
            messageReceiver.onMessageEncryptedReadByMe(updateEncryptedRead.getPeer(), updateEncryptedRead.getRid());
            Logger.d(TAG, toString(updateEncryptedRead.getPeer()) + " UpdateEncryptedReadByMe");
            return;
        }

        if (message instanceof UpdateMessageReceived) {
            UpdateMessageReceived messageReceived = (UpdateMessageReceived) message;
            messageReceiver.onMessageReceived(messageReceived.getPeer(), messageReceived.getStartDate());
            Logger.d(TAG, toString(messageReceived.getPeer()) + " UpdateMessageReceived");
            return;
        }

        if (message instanceof UpdateMessageRead) {
            UpdateMessageRead messageReceived = (UpdateMessageRead) message;
            messageReceiver.onMessageRead(messageReceived.getPeer(), messageReceived.getStartDate());
            Logger.d(TAG, toString(messageReceived.getPeer()) + " UpdateMessageRead");
            return;
        }

        if (message instanceof UpdateMessageReadByMe) {
            UpdateMessageReadByMe updateMessageReadByMe = (UpdateMessageReadByMe) message;
            messageReceiver.onMessageReadByMe(updateMessageReadByMe.getPeer(), updateMessageReadByMe.getStartDate());
            Logger.d(TAG, toString(updateMessageReadByMe.getPeer()) + " UpdateMessageReadByMe");
            return;
        }

        if (message instanceof UpdateMessageDelete) {
            UpdateMessageDelete updateDelete = (UpdateMessageDelete) message;
            messageReceiver.onMessageDeleted(updateDelete.getPeer(), updateDelete.getRids());
            Logger.d(TAG, toString(updateDelete.getPeer()) + " UpdateMessageDelete");
            return;
        }

        if (message instanceof UpdateChatClear) {
            UpdateChatClear chatClear = (UpdateChatClear) message;
            messageReceiver.onChatClear(chatClear.getPeer());
            Logger.d(TAG, toString(chatClear.getPeer()) + " UpdateChatClear");
            return;
        }

        if (message instanceof UpdateChatDelete) {
            UpdateChatDelete chatDelete = (UpdateChatDelete) message;
            messageReceiver.onChatDelete(chatDelete.getPeer());
            Logger.d(TAG, toString(chatDelete.getPeer()) + " UpdateChatDelete");
            return;
        }


        // Group Updates

        if (message instanceof UpdateGroupInvite) {
            UpdateGroupInvite updateMessage = (UpdateGroupInvite) message;
            groupActor.onInvite(updateMessage.getGroupId(), updateMessage.getRid(), updateMessage.getInviteUid(),
                    updateMessage.getDate());
            Logger.d(TAG, "{group:" + updateMessage.getGroupId() + "} UpdateGroupInvite");
            return;
        }

        if (message instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) message;
            groupActor.onUserLeave(leave.getGroupId(), leave.getRid(), leave.getUid(), leave.getDate());
            Logger.d(TAG, "{group:" + leave.getGroupId() + "} UpdateGroupUserLeave");
            return;
        }

        if (message instanceof UpdateGroupUserAdded) {
            UpdateGroupUserAdded add = (UpdateGroupUserAdded) message;
            groupActor.onUserAdded(add.getGroupId(), add.getRid(), add.getUid(), add.getInviterUid(), add.getDate());
            Logger.d(TAG, "{group:" + add.getGroupId() + "} UpdateGroupUserAdded");
            return;
        }

        if (message instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick leave = (UpdateGroupUserKick) message;
            groupActor.onUserKicked(leave.getGroupId(), leave.getRid(), leave.getUid(), leave.getKickerUid(), leave.getDate());
            Logger.d(TAG, "{group:" + leave.getGroupId() + "} UpdateGroupUserKick");
            return;
        }

        if (message instanceof UpdateGroupTitleChanged) {
            UpdateGroupTitleChanged changed = (UpdateGroupTitleChanged) message;
            groupActor.onTitleChanged(changed.getGroupId(), changed.getRid(), changed.getUid(), changed.getTitle(), changed.getDate());
            Logger.d(TAG, "{group:" + changed.getGroupId() + "} UpdateGroupTitleChanged");
            return;
        }

        if (message instanceof UpdateGroupAvatarChanged) {
            UpdateGroupAvatarChanged changed = (UpdateGroupAvatarChanged) message;
            groupActor.onAvatarChanged(changed.getGroupId(), changed.getRid(), changed.getUid(), changed.getAvatar(), changed.getDate());
            Logger.d(TAG, "{group:" + changed.getGroupId() + "} UpdateGroupAvatarChanged");
            return;
        }

        // Key updates

        if (message instanceof UpdateNewDevice) {
            UpdateNewDevice newDevice = (UpdateNewDevice) message;
            UserActor.userActor().onDeviceAdded(newDevice.getUid(), newDevice.getKeyHash(), null);
            Logger.d(TAG, "{user:" + newDevice.getUid() + "} UpdateNewDevice " + newDevice.getKeyHash());
            return;
        }

        if (message instanceof UpdateRemovedDevice) {
            UpdateRemovedDevice removeDevice = (UpdateRemovedDevice) message;
            UserActor.userActor().onDeviceRemoved(removeDevice.getUid(), removeDevice.getKeyHash());
            Logger.d(TAG, "{user:" + removeDevice.getUid() + "} UpdateRemovedDevice " + removeDevice.getKeyHash());
            return;
        }

        // User updates

        if (message instanceof UpdateUserNameChanged) {
            UpdateUserNameChanged changed = (UpdateUserNameChanged) message;
            UserActor.userActor().onServerNameChanged(changed.getUid(), changed.getName());
            Logger.d(TAG, "{user:" + changed.getUid() + "} UpdateUserNameChanged " + changed.getName());
            return;
        }

        if (message instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged changed = (UpdateUserLocalNameChanged) message;
            UserActor.userActor().onLocalNameChanged(changed.getUid(), changed.getLocalName());
            Logger.d(TAG, "{user:" + changed.getUid() + "} UpdateUserLocalNameChanged " + changed.getLocalName());
            return;
        }

        if (message instanceof UpdateUserAvatarChanged) {
            UpdateUserAvatarChanged changed = (UpdateUserAvatarChanged) message;
            UserActor.userActor().onAvatarChanged(changed.getUid(), changed.getAvatar());
            Logger.d(TAG, "{user:" + changed.getUid() + "} UpdateUserAvatarChanged");
            return;
        }

        // Contacts

        if (message instanceof UpdateContactRegistered) {
            UpdateContactRegistered changed = (UpdateContactRegistered) message;
            UserActor.userActor().onUserRegistered(changed.getUid());
            ContactsActor.contactsList().onContactsAdded(new int[]{changed.getUid()});
            Logger.d(TAG, "{contacts} UpdateContactRegistered");
            return;
        }

        if (message instanceof UpdateContactsAdded) {
            UpdateContactsAdded contactsAdded = (UpdateContactsAdded) message;
            ContactsActor.contactsList().onContactsAdded(BoxUtil.unbox(contactsAdded.getUids().toArray(new Integer[0])));
            Logger.d(TAG, "{contacts} UpdateContactsAdded {count:" + contactsAdded.getUids().size() + "}");
            return;
        }

        if (message instanceof UpdateContactsRemoved) {
            UpdateContactsRemoved contactsRemoved = (UpdateContactsRemoved) message;
            ContactsActor.contactsList().onContactsRemoved(BoxUtil.unbox(contactsRemoved.getUids().toArray(new Integer[0])));
            Logger.d(TAG, "{contacts} UpdateContactsRemoved {count:" + contactsRemoved.getUids().size() + "}");
            return;
        }

        // User presence

        if (message instanceof UpdateUserOnline) {
            UpdateUserOnline userOnline = (UpdateUserOnline) message;
            userPresence.send(new UsersPresence.UserGoesOnline(userOnline.getUid()));
            Logger.d(TAG, "{user:" + userOnline.getUid() + "} UpdateUserOnline");
            return;
        }

        if (message instanceof UpdateUserOffline) {
            UpdateUserOffline userOnline = (UpdateUserOffline) message;
            userPresence.send(new UsersPresence.UserGoesOffline(userOnline.getUid()));
            Logger.d(TAG, "{user:" + userOnline.getUid() + "} UpdateUserOffline");
            return;
        }

        if (message instanceof UpdateUserLastSeen) {
            UpdateUserLastSeen lastSeenUpdate = (UpdateUserLastSeen) message;
            userPresence.send(new UsersPresence.UserLastSeen(lastSeenUpdate.getUid(), lastSeenUpdate.getDate()));
            Logger.d(TAG, "{user:" + lastSeenUpdate.getUid() + "} UpdateUserLastSeen " + lastSeenUpdate.getDate());
            return;
        }

        if (message instanceof UpdateGroupOnline) {
            UpdateGroupOnline groupOnline = (UpdateGroupOnline) message;
            groupPresence.send(new GroupPresenceActor.OnGroupOnline(groupOnline.getGroupId(), groupOnline.getCount()));
            Logger.d(TAG, "{group:" + groupOnline.getGroupId() + "} UpdateGroupOnline " + groupOnline.getCount());
            return;
        }

        // Typing

        if (message instanceof UpdateTyping) {
            UpdateTyping typing = (UpdateTyping) message;
            TypingUpdateActor.typingUpdates().onTypingUpdate(typing.getPeer(), typing.getUid());
            Logger.d(TAG, toString(typing.getPeer()) + " UpdateTyping " + typing.getUid());
            return;
        }

        // Unsupported
        Logger.d(TAG, "Unsupported update: " + message.getClass().getName());
    }

    private static String toString(Peer peer) {
        if (peer.getType() == PeerType.PRIVATE) {
            return "{user:" + peer.getId() + "}";
        } else {
            return "{group:" + peer.getId() + "}";
        }
    }
}
