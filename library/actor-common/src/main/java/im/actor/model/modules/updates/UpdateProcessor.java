/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.updates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.model.api.Group;
import im.actor.model.api.PeerType;
import im.actor.model.api.User;
import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.api.updates.UpdateChatClear;
import im.actor.model.api.updates.UpdateChatDelete;
import im.actor.model.api.updates.UpdateContactRegistered;
import im.actor.model.api.updates.UpdateContactsAdded;
import im.actor.model.api.updates.UpdateContactsRemoved;
import im.actor.model.api.updates.UpdateGroupAvatarChanged;
import im.actor.model.api.updates.UpdateGroupInvite;
import im.actor.model.api.updates.UpdateGroupMembersUpdate;
import im.actor.model.api.updates.UpdateGroupOnline;
import im.actor.model.api.updates.UpdateGroupTitleChanged;
import im.actor.model.api.updates.UpdateGroupUserAdded;
import im.actor.model.api.updates.UpdateGroupUserKick;
import im.actor.model.api.updates.UpdateGroupUserLeave;
import im.actor.model.api.updates.UpdateMessage;
import im.actor.model.api.updates.UpdateMessageContentChanged;
import im.actor.model.api.updates.UpdateMessageDateChanged;
import im.actor.model.api.updates.UpdateMessageDelete;
import im.actor.model.api.updates.UpdateMessageRead;
import im.actor.model.api.updates.UpdateMessageReadByMe;
import im.actor.model.api.updates.UpdateMessageReceived;
import im.actor.model.api.updates.UpdateMessageSent;
import im.actor.model.api.updates.UpdateParameterChanged;
import im.actor.model.api.updates.UpdateTyping;
import im.actor.model.api.updates.UpdateUserAvatarChanged;
import im.actor.model.api.updates.UpdateUserLastSeen;
import im.actor.model.api.updates.UpdateUserLocalNameChanged;
import im.actor.model.api.updates.UpdateUserNameChanged;
import im.actor.model.api.updates.UpdateUserOffline;
import im.actor.model.api.updates.UpdateUserOnline;
import im.actor.model.log.Log;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.contacts.ContactsSyncActor;
import im.actor.model.modules.updates.internal.ContactsLoaded;
import im.actor.model.modules.updates.internal.DialogHistoryLoaded;
import im.actor.model.modules.updates.internal.GroupCreated;
import im.actor.model.modules.updates.internal.InternalUpdate;
import im.actor.model.modules.updates.internal.LoggedIn;
import im.actor.model.modules.updates.internal.MessagesHistoryLoaded;
import im.actor.model.modules.updates.internal.UsersFounded;
import im.actor.model.network.parser.Update;
import im.actor.model.viewmodel.UserVM;

public class UpdateProcessor extends BaseModule {

    private static final String TAG = "Updates";

    private SettingsProcessor settingsProcessor;
    private UsersProcessor usersProcessor;
    private MessagesProcessor messagesProcessor;
    private GroupsProcessor groupsProcessor;
    private PresenceProcessor presenceProcessor;
    private TypingProcessor typingProcessor;
    private ContactsProcessor contactsProcessor;

    public UpdateProcessor(Modules modules) {
        super(modules);
        this.settingsProcessor = new SettingsProcessor(modules);
        this.usersProcessor = new UsersProcessor(modules);
        this.messagesProcessor = new MessagesProcessor(modules);
        this.groupsProcessor = new GroupsProcessor(modules);
        this.presenceProcessor = new PresenceProcessor(modules);
        this.typingProcessor = new TypingProcessor(modules);
        this.contactsProcessor = new ContactsProcessor(modules);
    }

    public void applyRelated(List<User> users,
                             List<Group> groups,
                             boolean force) {
        usersProcessor.applyUsers(users, force);
        groupsProcessor.applyGroups(groups, force);
    }

    public void processInternalUpdate(InternalUpdate update) {
        if (update instanceof DialogHistoryLoaded) {
            ResponseLoadDialogs dialogs = ((DialogHistoryLoaded) update).getDialogs();
            applyRelated(dialogs.getUsers(), dialogs.getGroups(), false);
            messagesProcessor.onDialogsLoaded(dialogs);
        } else if (update instanceof MessagesHistoryLoaded) {
            MessagesHistoryLoaded historyLoaded = (MessagesHistoryLoaded) update;
            applyRelated(historyLoaded.getLoadHistory().getUsers(), new ArrayList<Group>(), false);
            messagesProcessor.onMessagesLoaded(historyLoaded.getPeer(), historyLoaded.getLoadHistory());
        } else if (update instanceof LoggedIn) {
            ArrayList<User> users = new ArrayList<User>();
            users.add(((LoggedIn) update).getAuth().getUser());
            applyRelated(users, new ArrayList<Group>(), true);
            runOnUiThread(((LoggedIn) update).getRunnable());
        } else if (update instanceof ContactsLoaded) {
            ContactsLoaded contactsLoaded = (ContactsLoaded) update;
            applyRelated(contactsLoaded.getContacts().getUsers(), new ArrayList<Group>(), false);
            modules().getContactsModule().getContactSyncActor()
                    .send(new ContactsSyncActor.ContactsLoaded(contactsLoaded.getContacts()));
        } else if (update instanceof UsersFounded) {
            final UsersFounded founded = (UsersFounded) update;
            applyRelated(((UsersFounded) update).getUsers(), new ArrayList<Group>(), false);
            final ArrayList<UserVM> users = new ArrayList<UserVM>();
            for (User u : founded.getUsers()) {
                users.add(modules().getUsersModule().getUsersCollection().get(u.getId()));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    founded.getCommandCallback().onResult(users.toArray(new UserVM[users.size()]));
                }
            });
        } else if (update instanceof GroupCreated) {
            final GroupCreated created = (GroupCreated) update;
            ArrayList<Group> groups = new ArrayList<Group>();
            groups.add(created.getGroup());
            applyRelated(created.getUsers(), groups, false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    created.getCallback().onResult(created.getGroup().getId());
                }
            });
        }
    }

    public void applyDifferenceUpdate(List<User> users, List<Group> groups, List<Update> updates) {
        modules().getNotifications().pauseNotifications();
        applyRelated(users, groups, false);
        for (Update u : updates) {
            processUpdate(u);
        }
        applyRelated(users, groups, true);
        modules().getNotifications().resumeNotifications();
    }

    public void processWeakUpdate(Update update, long date) {
        if (update instanceof UpdateUserOnline) {
            UpdateUserOnline userOnline = (UpdateUserOnline) update;
            presenceProcessor.onUserOnline(userOnline.getUid(), date);
        } else if (update instanceof UpdateUserOffline) {
            UpdateUserOffline offline = (UpdateUserOffline) update;
            presenceProcessor.onUserOffline(offline.getUid(), date);
        } else if (update instanceof UpdateUserLastSeen) {
            UpdateUserLastSeen lastSeen = (UpdateUserLastSeen) update;
            presenceProcessor.onUserLastSeen(lastSeen.getUid(), lastSeen.getDate(), date);
        } else if (update instanceof UpdateGroupOnline) {
            UpdateGroupOnline groupOnline = (UpdateGroupOnline) update;
            presenceProcessor.onGroupOnline(groupOnline.getGroupId(), groupOnline.getCount(), date);
        } else if (update instanceof UpdateTyping) {
            UpdateTyping typing = (UpdateTyping) update;
            typingProcessor.onTyping(typing.getPeer(), typing.getUid(), typing.getTypingType());
        }
    }

    public void processUpdate(Update update) {
        Log.d(TAG, update + "");
        if (update instanceof UpdateUserNameChanged) {
            UpdateUserNameChanged userNameChanged = (UpdateUserNameChanged) update;
            usersProcessor.onUserNameChanged(userNameChanged.getUid(), userNameChanged.getName());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            usersProcessor.onUserLocalNameChanged(localNameChanged.getUid(), localNameChanged.getLocalName());
        } else if (update instanceof UpdateUserAvatarChanged) {
            UpdateUserAvatarChanged avatarChanged = (UpdateUserAvatarChanged) update;
            usersProcessor.onUserAvatarChanged(avatarChanged.getUid(), avatarChanged.getAvatar());
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
        } else if (update instanceof UpdateMessageDateChanged) {
            UpdateMessageDateChanged dateChanged = (UpdateMessageDateChanged) update;
            messagesProcessor.onMessageDateChanged(dateChanged.getPeer(), dateChanged.getRid(), dateChanged.getDate());
        } else if (update instanceof UpdateMessageContentChanged) {
            UpdateMessageContentChanged contentChanged = (UpdateMessageContentChanged) update;
            messagesProcessor.onMessageContentChanged(contentChanged.getPeer(),
                    contentChanged.getRid(), contentChanged.getMessage());
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
        } else if (update instanceof UpdateGroupTitleChanged) {
            UpdateGroupTitleChanged titleChanged = (UpdateGroupTitleChanged) update;
            groupsProcessor.onTitleChanged(titleChanged.getGroupId(), titleChanged.getRid(),
                    titleChanged.getUid(), titleChanged.getTitle(), titleChanged.getDate(),
                    false);
        } else if (update instanceof UpdateGroupAvatarChanged) {
            UpdateGroupAvatarChanged avatarChanged = (UpdateGroupAvatarChanged) update;
            groupsProcessor.onAvatarChanged(avatarChanged.getGroupId(), avatarChanged.getRid(),
                    avatarChanged.getUid(), avatarChanged.getAvatar(),
                    avatarChanged.getDate(), false);
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            groupsProcessor.onGroupInvite(groupInvite.getGroupId(),
                    groupInvite.getRid(), groupInvite.getInviteUid(), groupInvite.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave leave = (UpdateGroupUserLeave) update;
            groupsProcessor.onUserLeave(leave.getGroupId(), leave.getRid(), leave.getUid(),
                    leave.getDate(), false);
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick userKick = (UpdateGroupUserKick) update;
            groupsProcessor.onUserKicked(userKick.getGroupId(),
                    userKick.getRid(), userKick.getUid(), userKick.getKickerUid(), userKick.getDate(),
                    false);
        } else if (update instanceof UpdateGroupUserAdded) {
            UpdateGroupUserAdded userAdded = (UpdateGroupUserAdded) update;
            groupsProcessor.onUserAdded(userAdded.getGroupId(),
                    userAdded.getRid(), userAdded.getUid(), userAdded.getInviterUid(), userAdded.getDate(),
                    false);
        } else if (update instanceof UpdateContactsAdded) {
            UpdateContactsAdded contactsAdded = (UpdateContactsAdded) update;
            int[] res = new int[contactsAdded.getUids().size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = contactsAdded.getUids().get(i);
            }
            contactsProcessor.onContactsAdded(res);
        } else if (update instanceof UpdateContactsRemoved) {
            UpdateContactsRemoved contactsRemoved = (UpdateContactsRemoved) update;
            int[] res = new int[contactsRemoved.getUids().size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = contactsRemoved.getUids().get(i);
            }
            contactsProcessor.onContactsRemoved(res);
        } else if (update instanceof UpdateGroupMembersUpdate) {
            groupsProcessor.onMembersUpdated(((UpdateGroupMembersUpdate) update).getGroupId(),
                    ((UpdateGroupMembersUpdate) update).getMembers());
        } else if (update instanceof UpdateParameterChanged) {
            settingsProcessor.onSettingsChanged(
                    ((UpdateParameterChanged) update).getKey(),
                    ((UpdateParameterChanged) update).getValue());
        }
    }


    public boolean isCausesInvalidation(Update update) {
        HashSet<Integer> users = new HashSet<Integer>();
        HashSet<Integer> groups = new HashSet<Integer>();

        if (update instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) update;
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
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            users.add(localNameChanged.getUid());
        }

        if (!usersProcessor.hasUsers(users)) {
            return true;
        }

        if (!groupsProcessor.hasGroups(groups)) {
            return true;
        }

        return false;
    }
}
