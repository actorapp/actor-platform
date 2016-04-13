/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiDialogShort;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiUser;
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateContactRegistered;
import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.api.updates.UpdateContactsRemoved;
import im.actor.core.api.updates.UpdateGroupAboutChanged;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupOnline;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTopicChanged;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserLeave;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateMessageContentChanged;
import im.actor.core.api.updates.UpdateMessageDelete;
import im.actor.core.api.updates.UpdateMessageRead;
import im.actor.core.api.updates.UpdateMessageReadByMe;
import im.actor.core.api.updates.UpdateMessageReceived;
import im.actor.core.api.updates.UpdateMessageSent;
import im.actor.core.api.updates.UpdateOwnStickersChanged;
import im.actor.core.api.updates.UpdateParameterChanged;
import im.actor.core.api.updates.UpdateReactionsUpdate;
import im.actor.core.api.updates.UpdateStickerCollectionsChanged;
import im.actor.core.api.updates.UpdateTyping;
import im.actor.core.api.updates.UpdateTypingStop;
import im.actor.core.api.updates.UpdateUserLastSeen;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserOffline;
import im.actor.core.api.updates.UpdateUserOnline;
import im.actor.core.entity.Peer;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.CallsProcessor;
import im.actor.core.modules.contacts.ContactsProcessor;
import im.actor.core.modules.encryption.EncryptedProcessor;
import im.actor.core.modules.eventbus.EventBusProcessor;
import im.actor.core.modules.contacts.ContactsSyncActor;
import im.actor.core.modules.groups.GroupsProcessor;
import im.actor.core.modules.presence.PresenceProcessor;
import im.actor.core.modules.settings.SettingsProcessor;
import im.actor.core.modules.stickers.StickersProcessor;
import im.actor.core.modules.typing.TypingProcessor;
import im.actor.core.modules.messaging.MessagesProcessor;
import im.actor.core.modules.sequence.internal.ArchivedDialogLoaded;
import im.actor.core.modules.sequence.internal.CombinedDifference;
import im.actor.core.modules.sequence.internal.ContactsLoaded;
import im.actor.core.modules.sequence.internal.GetDiffCombiner;
import im.actor.core.modules.sequence.internal.GroupCreated;
import im.actor.core.modules.sequence.internal.InternalUpdate;
import im.actor.core.modules.sequence.internal.LoggedIn;
import im.actor.core.modules.sequence.internal.RelatedResponse;
import im.actor.core.modules.sequence.internal.StickersLoaded;
import im.actor.core.modules.sequence.internal.UsersFounded;
import im.actor.core.modules.users.UsersProcessor;
import im.actor.core.network.parser.Update;
import im.actor.core.viewmodel.UserVM;

public class UpdateProcessor extends AbsModule {

    private static final String TAG = "Updates";

    private SettingsProcessor settingsProcessor;
    private UsersProcessor usersProcessor;
    private MessagesProcessor messagesProcessor;
    private GroupsProcessor groupsProcessor;
    private PresenceProcessor presenceProcessor;
    private TypingProcessor typingProcessor;
    private ContactsProcessor contactsProcessor;
    private StickersProcessor stickersProcessor;
    private CallsProcessor callsProcessor;
    private EncryptedProcessor encryptedProcessor;
    private EventBusProcessor eventBusProcessor;

    public UpdateProcessor(ModuleContext context) {
        super(context);
        this.contactsProcessor = new ContactsProcessor(context);
        this.settingsProcessor = new SettingsProcessor(context);
        this.usersProcessor = new UsersProcessor(context);
        this.messagesProcessor = new MessagesProcessor(context);
        this.groupsProcessor = new GroupsProcessor(context);
        this.presenceProcessor = new PresenceProcessor(context);
        this.typingProcessor = new TypingProcessor(context);
        this.stickersProcessor = new StickersProcessor(context);
        this.callsProcessor = new CallsProcessor(context);
        this.encryptedProcessor = new EncryptedProcessor(context);
        this.eventBusProcessor = new EventBusProcessor(context);
    }

    public void applyRelated(List<ApiUser> users,
                             List<ApiGroup> groups,
                             boolean force) {
        usersProcessor.applyUsers(users, force);
        groupsProcessor.applyGroups(groups, force);
    }

    public void processInternalUpdate(InternalUpdate update) {
        if (update instanceof LoggedIn) {
            ArrayList<ApiUser> users = new ArrayList<ApiUser>();
            users.add(((LoggedIn) update).getAuth().getUser());
            applyRelated(users, new ArrayList<ApiGroup>(), true);
            runOnUiThread(((LoggedIn) update).getRunnable());
        } else if (update instanceof ContactsLoaded) {
            ContactsLoaded contactsLoaded = (ContactsLoaded) update;
            applyRelated(contactsLoaded.getContacts().getUsers(), new ArrayList<ApiGroup>(), false);
            context().getContactsModule().getContactSyncActor()
                    .send(new ContactsSyncActor.ContactsLoaded(contactsLoaded.getContacts()));
        } else if (update instanceof UsersFounded) {
            final UsersFounded founded = (UsersFounded) update;
            applyRelated(((UsersFounded) update).getUsers(), new ArrayList<ApiGroup>(), false);
            final ArrayList<UserVM> users = new ArrayList<UserVM>();
            for (ApiUser u : founded.getUsers()) {
                users.add(context().getUsersModule().getUsers().get(u.getId()));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    founded.getCommandCallback().onResult(users.toArray(new UserVM[users.size()]));
                }
            });
        } else if (update instanceof GroupCreated) {
            final GroupCreated created = (GroupCreated) update;
            ArrayList<ApiGroup> groups = new ArrayList<ApiGroup>();
            groups.add(created.getGroup());
            applyRelated(created.getUsers(), groups, false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    created.getCallback().onResult(created.getGroup().getId());
                }
            });
        } else if (update instanceof RelatedResponse) {
            RelatedResponse relatedResponse = (RelatedResponse) update;
            applyRelated(relatedResponse.getRelatedUsers(), relatedResponse.getRelatedGroups(), false);
            relatedResponse.getAfterApply().run();
        } else if (update instanceof StickersLoaded) {
            stickersProcessor.onOwnStickerCollectionsChanged(((StickersLoaded) update).getCollections());
        }
    }

    public void applyDifferenceUpdate(List<ApiUser> users, List<ApiGroup> groups, List<Update> updates) {

        applyRelated(users, groups, false);

        CombinedDifference combinedDifference = GetDiffCombiner.buildDiff(updates);

        messagesProcessor.onDifferenceStart();

        for (Peer peer : combinedDifference.getReceived().keySet()) {
            long time = combinedDifference.getReceived().get(peer);
            messagesProcessor.onMessageReceived(buildApiPeer(peer), time);
        }

        for (Peer peer : combinedDifference.getRead().keySet()) {
            long time = combinedDifference.getRead().get(peer);
            messagesProcessor.onMessageRead(buildApiPeer(peer), time);
        }

        for (Peer peer : combinedDifference.getReadByMe().keySet()) {
            CombinedDifference.ReadByMeValue time = combinedDifference.getReadByMe().get(peer);
            messagesProcessor.onMessageReadByMe(buildApiPeer(peer), time.getDate(), time.getCounter());
        }

        for (Peer peer : combinedDifference.getMessages().keySet()) {
            messagesProcessor.onMessages(buildApiPeer(peer), combinedDifference.getMessages().get(peer));
        }

        for (Update u : combinedDifference.getOtherUpdates()) {
            processUpdate(u);
        }

        messagesProcessor.onDifferenceEnd();

        applyRelated(users, groups, true);
    }

    public void processWeakUpdate(Update update, long date) {
        if (callsProcessor.process(update)) {
            return;
        }
        if (eventBusProcessor.process(update)) {
            return;
        }
        if (update instanceof UpdateUserOnline) {
            UpdateUserOnline userOnline = (UpdateUserOnline) update;
            presenceProcessor.onUserOnline(userOnline.getUid(), date);
        } else if (update instanceof UpdateUserOffline) {
            UpdateUserOffline offline = (UpdateUserOffline) update;
            presenceProcessor.onUserOffline(offline.getUid(), date);
        } else if (update instanceof UpdateUserLastSeen) {
            UpdateUserLastSeen lastSeen = (UpdateUserLastSeen) update;
            presenceProcessor.onUserLastSeen(lastSeen.getUid(), (int) lastSeen.getDate(), date);
        } else if (update instanceof UpdateGroupOnline) {
            UpdateGroupOnline groupOnline = (UpdateGroupOnline) update;
            presenceProcessor.onGroupOnline(groupOnline.getGroupId(), groupOnline.getCount(), date);
        } else if (update instanceof UpdateTyping) {
            UpdateTyping typing = (UpdateTyping) update;
            typingProcessor.onTyping(typing.getPeer(), typing.getUid(), typing.getTypingType());
        } else if (update instanceof UpdateTypingStop) {
            UpdateTypingStop typing = (UpdateTypingStop) update;
            typingProcessor.onTypingStop(typing.getPeer(), typing.getUid(), typing.getTypingType());
        }
    }

    public void processUpdate(Update update) {
        if (usersProcessor.process(update)) {
            return;
        }
        if (encryptedProcessor.process(update)) {
            return;
        }
        if (callsProcessor.process(update)) {
            return;
        }
        if (eventBusProcessor.process(update)) {
            return;
        }
        if (contactsProcessor.process(update)) {
            return;
        }
        if (update instanceof UpdateMessage) {
            UpdateMessage message = (UpdateMessage) update;
            messagesProcessor.onMessage(message.getPeer(), message.getSenderUid(), message.getDate(), message.getRid(),
                    message.getMessage());
            typingProcessor.onMessage(message.getPeer(), message.getSenderUid());
        } else if (update instanceof UpdateMessageRead) {
            UpdateMessageRead messageRead = (UpdateMessageRead) update;
            messagesProcessor.onMessageRead(messageRead.getPeer(), messageRead.getStartDate());
        } else if (update instanceof UpdateMessageReadByMe) {
            UpdateMessageReadByMe messageReadByMe = (UpdateMessageReadByMe) update;
            if (messageReadByMe.getUnreadCounter() != null) {
                messagesProcessor.onMessageReadByMe(messageReadByMe.getPeer(), messageReadByMe.getStartDate(), messageReadByMe.getUnreadCounter());
            } else {
                messagesProcessor.onMessageReadByMe(messageReadByMe.getPeer(), messageReadByMe.getStartDate(), 0);
            }
        } else if (update instanceof UpdateMessageReceived) {
            UpdateMessageReceived received = (UpdateMessageReceived) update;
            messagesProcessor.onMessageReceived(received.getPeer(), received.getStartDate());
        } else if (update instanceof UpdateMessageDelete) {
            UpdateMessageDelete messageDelete = (UpdateMessageDelete) update;
            messagesProcessor.onMessageDelete(messageDelete.getPeer(), messageDelete.getRids());
        } else if (update instanceof UpdateMessageSent) {
            UpdateMessageSent messageSent = (UpdateMessageSent) update;
            messagesProcessor.onMessageSent(messageSent.getPeer(), messageSent.getRid(), messageSent.getDate());
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
        } else if (update instanceof UpdateGroupTitleChanged) {
            UpdateGroupTitleChanged titleChanged = (UpdateGroupTitleChanged) update;
            groupsProcessor.onTitleChanged(titleChanged.getGroupId(), titleChanged.getRid(),
                    titleChanged.getUid(), titleChanged.getTitle(), titleChanged.getDate(),
                    false);
        } else if (update instanceof UpdateGroupTopicChanged) {
            UpdateGroupTopicChanged topicChanged = (UpdateGroupTopicChanged) update;
            groupsProcessor.onTopicChanged(topicChanged.getGroupId(), topicChanged.getTopic());
        } else if (update instanceof UpdateGroupAboutChanged) {
            UpdateGroupAboutChanged aboutChanged = (UpdateGroupAboutChanged) update;
            groupsProcessor.onAboutChanged(aboutChanged.getGroupId(), aboutChanged.getAbout());
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
        } else if (update instanceof UpdateGroupUserInvited) {
            UpdateGroupUserInvited userInvited = (UpdateGroupUserInvited) update;
            groupsProcessor.onUserAdded(userInvited.getGroupId(),
                    userInvited.getRid(), userInvited.getUid(), userInvited.getInviterUid(), userInvited.getDate(),
                    false);
        } else if (update instanceof UpdateGroupMembersUpdate) {
            groupsProcessor.onMembersUpdated(((UpdateGroupMembersUpdate) update).getGroupId(),
                    ((UpdateGroupMembersUpdate) update).getMembers());
        } else if (update instanceof UpdateParameterChanged) {
            settingsProcessor.onSettingsChanged(
                    ((UpdateParameterChanged) update).getKey(),
                    ((UpdateParameterChanged) update).getValue());
        } else if (update instanceof UpdateChatGroupsChanged) {
            UpdateChatGroupsChanged chatGroupsChanged = (UpdateChatGroupsChanged) update;
            messagesProcessor.onChatGroupsChanged(chatGroupsChanged.getDialogs());
        } else if (update instanceof UpdateReactionsUpdate) {
            messagesProcessor.onReactionsChanged(((UpdateReactionsUpdate) update).getPeer(),
                    ((UpdateReactionsUpdate) update).getRid(), ((UpdateReactionsUpdate) update).getReactions());
        } else if (update instanceof UpdateOwnStickersChanged) {
            stickersProcessor.onOwnStickerCollectionsChanged(((UpdateOwnStickersChanged) update).getCollections());
        } else if (update instanceof UpdateStickerCollectionsChanged) {
            stickersProcessor.onStickerCollectionsChanged(((UpdateStickerCollectionsChanged) update).getCollections());
        }
    }


    public boolean isCausesInvalidation(Update update) {
        HashSet<Integer> users = new HashSet<>();
        HashSet<Integer> groups = new HashSet<>();

        if (update instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) update;
            users.add(updateMessage.getSenderUid());
            if (updateMessage.getPeer().getType() == ApiPeerType.GROUP) {
                groups.add(updateMessage.getPeer().getId());
            }
            if (updateMessage.getPeer().getType() == ApiPeerType.PRIVATE) {
                users.add(updateMessage.getPeer().getId());
            }
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered contactRegistered = (UpdateContactRegistered) update;
            users.add(contactRegistered.getUid());
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            users.add(groupInvite.getInviteUid());
            groups.add(groupInvite.getGroupId());
        } else if (update instanceof UpdateGroupUserInvited) {
            UpdateGroupUserInvited invited = (UpdateGroupUserInvited) update;
            users.add(invited.getInviterUid());
            users.add(invited.getUid());
            groups.add(invited.getGroupId());
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
        } else if (update instanceof UpdateChatGroupsChanged) {
            UpdateChatGroupsChanged changed = (UpdateChatGroupsChanged) update;
            for (ApiDialogGroup group : changed.getDialogs()) {
                for (ApiDialogShort dialog : group.getDialogs()) {
                    if (dialog.getPeer().getType() == ApiPeerType.PRIVATE) {
                        users.add(dialog.getPeer().getId());
                    } else if (dialog.getPeer().getType() == ApiPeerType.GROUP) {
                        groups.add(dialog.getPeer().getId());
                    }
                }
            }
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
