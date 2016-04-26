package im.actor.core.modules.messaging.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiDialogShort;
import im.actor.core.api.rpc.RequestLoadGroupedDialogs;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.ConversationState;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.User;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actions.CursorReaderActor;
import im.actor.core.modules.messaging.actions.CursorReceiverActor;
import im.actor.core.modules.messaging.dialogs.DialogsActor;
import im.actor.core.modules.messaging.history.entity.DialogHistory;
import im.actor.core.modules.messaging.router.entity.ActiveDialogGroup;
import im.actor.core.modules.messaging.router.entity.ActiveDialogStorage;
import im.actor.core.modules.messaging.router.entity.RouterActiveDialogsChanged;
import im.actor.core.modules.messaging.router.entity.RouterAppHidden;
import im.actor.core.modules.messaging.router.entity.RouterAppVisible;
import im.actor.core.modules.messaging.router.entity.RouterApplyChatHistory;
import im.actor.core.modules.messaging.router.entity.RouterApplyDialogsHistory;
import im.actor.core.modules.messaging.router.entity.RouterChangedContent;
import im.actor.core.modules.messaging.router.entity.RouterChangedReactions;
import im.actor.core.modules.messaging.router.entity.RouterChatClear;
import im.actor.core.modules.messaging.router.entity.RouterChatDelete;
import im.actor.core.modules.messaging.router.entity.RouterConversationHidden;
import im.actor.core.modules.messaging.router.entity.RouterConversationVisible;
import im.actor.core.modules.messaging.router.entity.RouterDeletedMessages;
import im.actor.core.modules.messaging.router.entity.RouterDifferenceEnd;
import im.actor.core.modules.messaging.router.entity.RouterDifferenceStart;
import im.actor.core.modules.messaging.router.entity.RouterMessageOnlyActive;
import im.actor.core.modules.messaging.router.entity.RouterMessageRead;
import im.actor.core.modules.messaging.router.entity.RouterMessageReadByMe;
import im.actor.core.modules.messaging.router.entity.RouterMessageReceived;
import im.actor.core.modules.messaging.router.entity.RouterNewMessages;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingError;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingMessage;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingSent;
import im.actor.core.modules.messaging.router.entity.RouterPeersChanged;
import im.actor.core.util.JavaUtil;
import im.actor.core.viewmodel.DialogGroup;
import im.actor.core.viewmodel.DialogSmall;
import im.actor.core.viewmodel.generics.ArrayListDialogSmall;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.ListEngine;

import static im.actor.core.entity.EntityConverter.convert;
import static im.actor.core.util.AssertUtils.assertTrue;

public class RouterActor extends ModuleActor {

    private static final String TAG = "RouterActor";

    // Visibility
    private final HashSet<Peer> visiblePeers = new HashSet<>();
    private boolean isAppVisible = false;

    // Storage
    private KeyValueEngine<ConversationState> conversationStates;

    // Active Dialogs
    private ActiveDialogStorage activeDialogStorage;


    public RouterActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();

        conversationStates = context().getMessagesModule().getConversationStates().getEngine();

        //
        // Loading Active Dialogs
        //
        activeDialogStorage = new ActiveDialogStorage();
        byte[] data = context().getStorageModule().getBlobStorage().loadItem(AbsModule.BLOB_DIALOGS_ACTIVE);
        if (data != null) {
            try {
                activeDialogStorage = new ActiveDialogStorage(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!activeDialogStorage.isLoaded()) {
            api(new RequestLoadGroupedDialogs())
                    .chain(r -> updates().applyRelatedData(r.getUsers(), r.getGroups()))
                    .then(r -> {
                        boolean showArchived = false;
                        boolean showInvite = false;
                        if (r.showArchived() != null) {
                            showArchived = r.showArchived();
                        }
                        if (r.showInvite() != null) {
                            showInvite = r.showInvite();
                        }
                        onActiveDialogsChanged(r.getDialogs(), showArchived, showInvite);
                    });
        } else {
            notifyActiveDialogsVM();
        }
    }


    //
    // Active Dialogs
    //

    private void onActiveDialogsChanged(List<ApiDialogGroup> dialogs, boolean showArchived, boolean showInvite) {

        //
        // Updating Counters
        //
        ArrayList<ConversationState> convStates = new ArrayList<>();
        for (ApiDialogGroup g : dialogs) {
            for (ApiDialogShort s : g.getDialogs()) {
                Peer peer = convert(s.getPeer());
                ConversationState state = conversationStates.getValue(peer.getUnuqueId());
                boolean isChanged = false;
                if (state.getUnreadCount() != s.getCounter() && !isConversationVisible(peer)) {
                    state = state.changeCounter(s.getCounter());
                    isChanged = true;
                }
                if (state.getInMaxMessageDate() < s.getDate()) {
                    state = state.changeInMaxDate(s.getDate());
                    isChanged = true;
                }
                if (isChanged) {
                    convStates.add(state);
                }
            }
        }
        conversationStates.addOrUpdateItems(convStates);

        //
        // Updating storage
        //
        activeDialogStorage.setHaveArchived(showArchived);
        activeDialogStorage.setShowInvite(showInvite);
        activeDialogStorage.setLoaded(true);
        activeDialogStorage.getGroups().clear();
        for (ApiDialogGroup g : dialogs) {
            ArrayList<Peer> peers = new ArrayList<>();
            for (ApiDialogShort s : g.getDialogs()) {
                Peer peer = convert(s.getPeer());
                peers.add(peer);
            }
            activeDialogStorage.getGroups().add(new ActiveDialogGroup(g.getKey(), g.getTitle(), peers));
        }
        context().getStorageModule().getBlobStorage()
                .addOrUpdateItem(AbsModule.BLOB_DIALOGS_ACTIVE, activeDialogStorage.toByteArray());

        //
        // Notify VM
        //
        notifyActiveDialogsVM();

        //
        // Unstash all messages after initial loading
        //
        unstashAll();
    }


    //
    // Incoming Messages
    //

    private void onNewMessages(Peer peer, List<Message> messages) {

        assertTrue(messages.size() != 0);

        boolean isConversationVisible = isConversationVisible(peer);

        //
        // Collecting Information
        //
        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        Message topMessage = null;
        int unreadCount = 0;
        long maxInDate = 0;
        for (Message m : messages) {
            if (topMessage == null || topMessage.getSortDate() < m.getSortDate()) {
                topMessage = m;
            }
            if (m.getSenderId() != myUid()) {
                if (m.getSortDate() > state.getInReadDate()) {
                    unreadCount++;
                }
                maxInDate = Math.max(maxInDate, m.getSortDate());
            }
        }


        //
        // Writing to Conversation
        //
        conversation(peer).addOrUpdateItems(messages);


        //
        // Updating Counter
        //
        boolean isRead = false;
        if (unreadCount != 0) {
            if (isConversationVisible) {
                // Auto Reading message
                if (maxInDate > 0) {
                    state = state
                            .changeInReadDate(maxInDate)
                            .changeInMaxDate(maxInDate)
                            .changeCounter(0);
                    context().getMessagesModule().getPlainReadActor()
                            .send(new CursorReaderActor.MarkRead(peer, maxInDate));
                    context().getNotificationsModule().onOwnRead(peer, maxInDate);
                    isRead = true;
                    conversationStates.addOrUpdateItem(state);
                }
            } else {
                // Updating counter
                state = state.changeCounter(state.getUnreadCount() + unreadCount);
                if (maxInDate > 0) {
                    state = state
                            .changeInMaxDate(maxInDate);
                }
                conversationStates.addOrUpdateItem(state);

                notifyActiveDialogsVM();
            }
        }


        //
        // Marking As Received
        //
        if (maxInDate > 0 && !isRead) {
            context().getMessagesModule().getPlainReceiverActor()
                    .send(new CursorReceiverActor.MarkReceived(peer, maxInDate));
        }


        //
        // Updating Dialog List
        //
        dialogsActor(new DialogsActor.InMessage(peer, topMessage, state.getUnreadCount()));


        //
        // Playing notifications
        //
        if (!isConversationVisible) {
            for (Message m : messages) {
                if (m.getSenderId() != myUid()) {
                    boolean hasCurrentMention = false;
                    if (m.getContent() instanceof TextContent) {
                        if (((TextContent) m.getContent()).getMentions().contains(myUid())) {
                            hasCurrentMention = true;
                        }
                    }
                    context().getNotificationsModule().onInMessage(
                            peer,
                            m.getSenderId(),
                            m.getSortDate(),
                            ContentDescription.fromContent(m.getContent()),
                            hasCurrentMention);
                }
            }
        }
    }


    //
    // Outgoing Messages
    //

    private void onOutgoingMessage(Peer peer, Message message) {
        conversation(peer).addOrUpdateItem(message);
    }

    private void onOutgoingSent(Peer peer, long rid, long date) {
        Message msg = conversation(peer).getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating message
            Message updatedMsg = msg
                    .changeAllDate(date)
                    .changeState(MessageState.SENT);
            conversation(peer).addOrUpdateItem(updatedMsg);

            // Notify dialogs
            dialogsActor(new DialogsActor.InMessage(peer, updatedMsg, -1));
        }
    }

    private void onOutgoingError(Peer peer, long rid) {
        Message msg = conversation(peer).getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating message
            Message updatedMsg = msg
                    .changeState(MessageState.ERROR);
            conversation(peer).addOrUpdateItem(updatedMsg);
        }
    }


    //
    // History Messages
    //

    private void onDialogHistoryLoaded(List<DialogHistory> dialogs) {
        for (DialogHistory d : dialogs) {
            ConversationState state = conversationStates.getValue(d.getPeer().getUnuqueId());
            if (d.getUnreadCount() > 0) {
                state = state
                        .changeCounter(d.getUnreadCount())
                        .changeInMaxDate(d.getDate());
            }
            if (d.isRead()) {
                state = state
                        .changeOutReadDate(d.getDate())
                        .changeOutReceiveDate(d.getDate());
            } else if (d.isReceived()) {
                state = state
                        .changeOutReceiveDate(d.getDate());
            }
            conversationStates.addOrUpdateItem(state);
        }

        dialogsActor(new DialogsActor.HistoryLoaded(dialogs));
    }

    private void onChatHistoryLoaded(Peer peer, List<Message> messages, Long maxReadDate,
                                     Long maxReceiveDate, boolean isEnded) {

        long maxMessageDate = 0;

        // Processing all new messages
        ArrayList<Message> updated = new ArrayList<>();
        for (Message historyMessage : messages) {
            // Ignore already present messages
            if (conversation(peer).getValue(historyMessage.getEngineId()) != null) {
                continue;
            }

            updated.add(historyMessage);

            if (historyMessage.getSenderId() != myUid()) {
                maxMessageDate = Math.max(maxMessageDate, historyMessage.getSortDate());
            }
        }

        // Writing messages
        conversation(peer).addOrUpdateItems(updated);

        // Updating conversation state
        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        boolean isChanged = false;
        if (state.getInMaxMessageDate() < maxMessageDate) {
            state = state.changeInMaxDate(maxMessageDate);
            isChanged = true;
        }
        if (maxReadDate != null && maxReadDate != 0 && state.getOutReadDate() < maxMessageDate) {
            state = state.changeOutReadDate(maxReadDate);
            isChanged = true;
        }
        if (maxReceiveDate != null && maxReceiveDate != 0 && state.getOutReceiveDate() < maxReceiveDate) {
            state = state.changeOutReceiveDate(maxReceiveDate);
            isChanged = true;
        }
        if (state.isLoaded() != isEnded) {
            state = state.changeIsLoaded(isEnded);
            isChanged = true;
        }
        if (isChanged) {
            conversationStates.addOrUpdateItem(state);
        }
    }


    //
    // Message Updating
    //

    private void onContentUpdate(Peer peer, long rid, AbsContent content) {
        Message message = conversation(peer).getValue(rid);

        // Ignore if we already doesn't have this message
        if (message == null) {
            return;
        }

        conversation(peer).addOrUpdateItem(message.changeContent(content));
    }

    private void onReactionsUpdate(Peer peer, long rid, List<Reaction> reactions) {
        Message message = conversation(peer).getValue(rid);

        // Ignore if we already doesn't have this message
        if (message == null) {
            return;
        }

        conversation(peer).addOrUpdateItem(message.changeReactions(reactions));
    }


    //
    // Message Deletions
    //

    private void onMessageDeleted(Peer peer, List<Long> rids) {

        // Delete Messages
        conversation(peer).removeItems(JavaUtil.unbox(rids));

        Message head = conversation(peer).getHeadValue();
        dialogsActor(new DialogsActor.MessageDeleted(peer, head));
    }

    private void onChatClear(Peer peer) {

        conversation(peer).clear();

        dialogsActor(new DialogsActor.ChatClear(peer));
    }

    private void onChatDelete(Peer peer) {

        conversation(peer).clear();

        dialogsActor(new DialogsActor.ChatDelete(peer));
    }


    //
    // Read States
    //

    private void onMessageRead(Peer peer, long date) {
        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        boolean isChanged = false;
        if (date > state.getOutReadDate()) {
            state = state.changeOutReadDate(date);
            dialogsActor(new DialogsActor.PeerReadChanged(peer, date));
            isChanged = true;
        }
        if (date > state.getOutReceiveDate()) {
            state = state.changeOutReceiveDate(date);
            isChanged = true;
        }
        if (isChanged) {
            conversationStates.addOrUpdateItem(state);
        }
    }

    private void onMessageReceived(Peer peer, long date) {
        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        if (date > state.getOutReceiveDate()) {
            dialogsActor(new DialogsActor.PeerReceiveChanged(peer, date));
            state = state.changeOutReceiveDate(date);
            conversationStates.addOrUpdateItem(state);
        }
    }

    private void onMessageReadByMe(Peer peer, long date, int counter) {
        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        state = state
                .changeCounter(counter)
                .changeInReadDate(date);
        conversationStates.addOrUpdateItem(state);

        dialogsActor(new DialogsActor.CounterChanged(peer, counter));

        notifyActiveDialogsVM();

        context().getNotificationsModule().onOwnRead(peer, date);
    }


    //
    // Peer Changed
    //

    private void onPeersChanged(List<User> users, List<Group> groups) {
        boolean isActiveNeedUpdate = false;
        for (User u : users) {
            if (!isActiveNeedUpdate) {
                for (ActiveDialogGroup g : activeDialogStorage.getGroups()) {
                    if (g.getPeers().contains(u.peer())) {
                        isActiveNeedUpdate = true;
                        break;
                    }
                }
            }
            dialogsActor(new DialogsActor.UserChanged(u));
        }
        for (Group group : groups) {
            if (!isActiveNeedUpdate) {
                for (ActiveDialogGroup g : activeDialogStorage.getGroups()) {
                    if (g.getPeers().contains(group.peer())) {
                        isActiveNeedUpdate = true;
                        break;
                    }
                }
            }
            dialogsActor(new DialogsActor.GroupChanged(group));
        }

        if (isActiveNeedUpdate) {
            notifyActiveDialogsVM();
        }
    }


    //
    // Auto Messages Read
    //

    private void onConversationVisible(Peer peer) {
        visiblePeers.add(peer);

        markAsReadIfNeeded(peer);
    }

    private void onConversationHidden(Peer peer) {
        visiblePeers.remove(peer);
    }

    private void onAppVisible() {
        isAppVisible = true;

        for (Peer p : visiblePeers) {
            markAsReadIfNeeded(p);
        }
    }

    private void onAppHidden() {
        isAppVisible = false;
    }

    private boolean isConversationVisible(Peer peer) {
        return visiblePeers.contains(peer) && isAppVisible;
    }

    private void markAsReadIfNeeded(Peer peer) {
        if (isConversationVisible(peer)) {
            ConversationState state = conversationStates.getValue(peer.getUnuqueId());
            if (state.getUnreadCount() != 0 || state.getInReadDate() < state.getInMaxMessageDate()) {
                state = state
                        .changeCounter(0)
                        .changeInReadDate(state.getInMaxMessageDate());
                conversationStates.addOrUpdateItem(state);

                context().getMessagesModule().getPlainReadActor()
                        .send(new CursorReaderActor.MarkRead(peer, state.getInMaxMessageDate()));

                dialogsActor(new DialogsActor.CounterChanged(peer, 0));

                notifyActiveDialogsVM();
            }
        }
    }


    //
    // Difference Handling
    //

    public void onDifferenceStart() {
        context().getNotificationsModule().pauseNotifications();
    }

    public void onDifferenceEnd() {
        context().getNotificationsModule().resumeNotifications();
    }


    //
    // Tools
    //

    private void dialogsActor(Object message) {
        context().getMessagesModule().getDialogsActor().send(message);
    }

    private ListEngine<Message> conversation(Peer peer) {
        return context().getMessagesModule().getConversationEngine(peer);
    }

    private void notifyActiveDialogsVM() {
        int counter = 0;
        ArrayList<DialogGroup> groups = new ArrayList<>();
        for (ActiveDialogGroup i : activeDialogStorage.getGroups()) {
            ArrayListDialogSmall dialogSmalls = new ArrayListDialogSmall();
            for (Peer p : i.getPeers()) {
                String title;
                Avatar avatar;
                if (p.getPeerType() == PeerType.GROUP) {
                    Group group = getGroup(p.getPeerId());
                    title = group.getTitle();
                    avatar = group.getAvatar();
                } else if (p.getPeerType() == PeerType.PRIVATE) {
                    User user = getUser(p.getPeerId());
                    title = user.getName();
                    avatar = user.getAvatar();
                } else {
                    continue;
                }

                int unreadCount = conversationStates.getValue(p.getUnuqueId()).getUnreadCount();
                counter += unreadCount;
                dialogSmalls.add(new DialogSmall(p, title, avatar, unreadCount));
            }
            groups.add(new DialogGroup(i.getTitle(), i.getKey(), dialogSmalls));
        }
        context().getMessagesModule().getDialogGroupsVM().getGroupsValueModel().change(groups);
        context().getAppStateModule().getGlobalStateVM().onGlobalCounterChanged(counter);
    }

    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (!activeDialogStorage.isLoaded() && message instanceof RouterMessageOnlyActive) {
            stash();
            return;
        }
        if (message instanceof RouterConversationVisible) {
            RouterConversationVisible conversationVisible = (RouterConversationVisible) message;
            onConversationVisible(conversationVisible.getPeer());
        } else if (message instanceof RouterConversationHidden) {
            RouterConversationHidden conversationHidden = (RouterConversationHidden) message;
            onConversationHidden(conversationHidden.getPeer());
        } else if (message instanceof RouterAppVisible) {
            onAppVisible();
        } else if (message instanceof RouterAppHidden) {
            onAppHidden();
        } else if (message instanceof RouterNewMessages) {
            RouterNewMessages routerNewMessages = (RouterNewMessages) message;
            onNewMessages(routerNewMessages.getPeer(), routerNewMessages.getMessages());
        } else if (message instanceof RouterOutgoingMessage) {
            RouterOutgoingMessage routerOutgoingMessage = (RouterOutgoingMessage) message;
            onOutgoingMessage(routerOutgoingMessage.getPeer(), routerOutgoingMessage.getMessage());
        } else if (message instanceof RouterOutgoingSent) {
            RouterOutgoingSent routerOutgoingSent = (RouterOutgoingSent) message;
            onOutgoingSent(routerOutgoingSent.getPeer(), routerOutgoingSent.getRid(), routerOutgoingSent.getDate());
        } else if (message instanceof RouterOutgoingError) {
            RouterOutgoingError outgoingError = (RouterOutgoingError) message;
            onOutgoingError(outgoingError.getPeer(), outgoingError.getRid());
        } else if (message instanceof RouterChangedContent) {
            RouterChangedContent routerChangedContent = (RouterChangedContent) message;
            onContentUpdate(routerChangedContent.getPeer(), routerChangedContent.getRid(), routerChangedContent.getContent());
        } else if (message instanceof RouterChangedReactions) {
            RouterChangedReactions routerChangedReactions = (RouterChangedReactions) message;
            onReactionsUpdate(routerChangedReactions.getPeer(), routerChangedReactions.getRid(), routerChangedReactions.getReactions());
        } else if (message instanceof RouterDeletedMessages) {
            RouterDeletedMessages routerDeletedMessages = (RouterDeletedMessages) message;
            onMessageDeleted(routerDeletedMessages.getPeer(), routerDeletedMessages.getRids());
        } else if (message instanceof RouterMessageRead) {
            RouterMessageRead messageRead = (RouterMessageRead) message;
            onMessageRead(messageRead.getPeer(), messageRead.getDate());
        } else if (message instanceof RouterMessageReadByMe) {
            RouterMessageReadByMe readByMe = (RouterMessageReadByMe) message;
            onMessageReadByMe(readByMe.getPeer(), readByMe.getDate(), readByMe.getCounter());
        } else if (message instanceof RouterMessageReceived) {
            RouterMessageReceived messageReceived = (RouterMessageReceived) message;
            onMessageReceived(messageReceived.getPeer(), messageReceived.getDate());
        } else if (message instanceof RouterApplyDialogsHistory) {
            RouterApplyDialogsHistory dialogsHistory = (RouterApplyDialogsHistory) message;
            onDialogHistoryLoaded(dialogsHistory.getDialogs());
            dialogsHistory.getExecuteAfter().run();
        } else if (message instanceof RouterApplyChatHistory) {
            RouterApplyChatHistory chatHistory = (RouterApplyChatHistory) message;
            onChatHistoryLoaded(chatHistory.getPeer(),
                    chatHistory.getMessages(), chatHistory.getMaxReadDate(),
                    chatHistory.getMaxReceiveDate(), chatHistory.isEnded());
        } else if (message instanceof RouterChatClear) {
            RouterChatClear routerChatClear = (RouterChatClear) message;
            onChatClear(routerChatClear.getPeer());
        } else if (message instanceof RouterChatDelete) {
            RouterChatDelete chatDelete = (RouterChatDelete) message;
            onChatDelete(chatDelete.getPeer());
        } else if (message instanceof RouterPeersChanged) {
            RouterPeersChanged peersChanged = (RouterPeersChanged) message;
            onPeersChanged(peersChanged.getUsers(), peersChanged.getGroups());
        } else if (message instanceof RouterActiveDialogsChanged) {
            RouterActiveDialogsChanged dialogsChanged = (RouterActiveDialogsChanged) message;
            onActiveDialogsChanged(dialogsChanged.getGroups(), dialogsChanged.isHasArchived(),
                    dialogsChanged.isShowInvite());
        } else if (message instanceof RouterDifferenceStart) {
            onDifferenceStart();
        } else if (message instanceof RouterDifferenceEnd) {
            onDifferenceEnd();
        } else {
            super.onReceive(message);
        }
    }
}
