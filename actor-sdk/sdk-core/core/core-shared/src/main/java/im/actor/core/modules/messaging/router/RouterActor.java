package im.actor.core.modules.messaging.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiDialogShort;
import im.actor.core.api.ApiMessageReaction;
import im.actor.core.api.rpc.RequestLoadGroupedDialogs;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateMessageContentChanged;
import im.actor.core.api.updates.UpdateMessageDelete;
import im.actor.core.api.updates.UpdateMessageRead;
import im.actor.core.api.updates.UpdateMessageReadByMe;
import im.actor.core.api.updates.UpdateMessageReceived;
import im.actor.core.api.updates.UpdateMessageSent;
import im.actor.core.api.updates.UpdateReactionsUpdate;
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
import im.actor.core.modules.messaging.actions.SenderActor;
import im.actor.core.modules.messaging.dialogs.DialogsInt;
import im.actor.core.modules.messaging.dialogs.entity.ChatClear;
import im.actor.core.modules.messaging.dialogs.entity.ChatDelete;
import im.actor.core.modules.messaging.dialogs.entity.CounterChanged;
import im.actor.core.modules.messaging.dialogs.entity.GroupChanged;
import im.actor.core.modules.messaging.dialogs.entity.InMessage;
import im.actor.core.modules.messaging.dialogs.entity.MessageDeleted;
import im.actor.core.modules.messaging.dialogs.entity.PeerReadChanged;
import im.actor.core.modules.messaging.dialogs.entity.PeerReceiveChanged;
import im.actor.core.modules.messaging.dialogs.entity.UserChanged;
import im.actor.core.modules.messaging.dialogs.entity.HistoryLoaded;
import im.actor.core.modules.messaging.history.entity.DialogHistory;
import im.actor.core.modules.messaging.router.entity.ActiveDialogGroup;
import im.actor.core.modules.messaging.router.entity.ActiveDialogStorage;
import im.actor.core.modules.messaging.router.entity.RouterAppHidden;
import im.actor.core.modules.messaging.router.entity.RouterAppVisible;
import im.actor.core.modules.messaging.router.entity.RouterApplyChatHistory;
import im.actor.core.modules.messaging.router.entity.RouterApplyDialogsHistory;
import im.actor.core.modules.messaging.router.entity.RouterChangedContent;
import im.actor.core.modules.messaging.router.entity.RouterConversationHidden;
import im.actor.core.modules.messaging.router.entity.RouterConversationVisible;
import im.actor.core.modules.messaging.router.entity.RouterDeletedMessages;
import im.actor.core.modules.messaging.router.entity.RouterDifferenceEnd;
import im.actor.core.modules.messaging.router.entity.RouterDifferenceStart;
import im.actor.core.modules.messaging.router.entity.RouterMessageOnlyActive;
import im.actor.core.modules.messaging.router.entity.RouterMessageUpdate;
import im.actor.core.modules.messaging.router.entity.RouterNewMessages;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingError;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingMessage;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingSent;
import im.actor.core.modules.messaging.router.entity.RouterPeersChanged;
import im.actor.core.network.parser.Update;
import im.actor.core.util.JavaUtil;
import im.actor.core.viewmodel.DialogGroup;
import im.actor.core.viewmodel.DialogSmall;
import im.actor.core.viewmodel.generics.ArrayListDialogSmall;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.ListEngine;

import static im.actor.core.entity.EntityConverter.convert;
import static im.actor.core.util.AssertUtils.assertTrue;

public class RouterActor extends ModuleActor {

    private static final String TAG = "RouterActor";

    // j2objc workaround
    private static final Void DUMB = null;

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

    private Promise<Void> onNewMessages(Peer peer, List<Message> messages) {

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
        Promise<Void> res = getDialogsRouter().onMessage(peer, topMessage, state.getUnreadCount());


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

        return res;
    }


    //
    // Outgoing Messages
    //

    private Promise<Void> onOutgoingMessage(Peer peer, Message message) {
        conversation(peer).addOrUpdateItem(message);
        return Promise.success(null);
    }

    private Promise<Void> onOutgoingSent(Peer peer, long rid, long date) {
        Message msg = conversation(peer).getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating message
            Message updatedMsg = msg
                    .changeAllDate(date)
                    .changeState(MessageState.SENT);
            conversation(peer).addOrUpdateItem(updatedMsg);

            // Notify dialogs
            return getDialogsRouter().onMessage(peer, updatedMsg, -1);
        } else {
            return Promise.success(null);
        }
    }

    private Promise<Void> onOutgoingError(Peer peer, long rid) {
        Message msg = conversation(peer).getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating message
            Message updatedMsg = msg
                    .changeState(MessageState.ERROR);
            conversation(peer).addOrUpdateItem(updatedMsg);
        }
        return Promise.success(null);
    }


    //
    // History Messages
    //

    private Promise<Void> onDialogHistoryLoaded(List<DialogHistory> dialogs) {
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

        return getDialogsRouter().onHistoryLoaded(dialogs);
    }

    private Promise<Void> onChatHistoryLoaded(Peer peer, List<Message> messages, Long maxReadDate,
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

        return Promise.success(null);
    }


    //
    // Message Updating
    //

    private Promise<Void> onContentUpdate(Peer peer, long rid, AbsContent content) {

        Message message = conversation(peer).getValue(rid);

        if (message != null) {
            conversation(peer).addOrUpdateItem(message.changeContent(content));
            return getDialogsRouter().onMessageContentChanged(peer, rid, content);
        } else {
            return Promise.success(null);
        }
    }

    private Promise<Void> onReactionsUpdate(Peer peer, long rid, List<Reaction> reactions) {
        Message message = conversation(peer).getValue(rid);

        // Ignore if we already doesn't have this message
        if (message != null) {
            conversation(peer).addOrUpdateItem(message.changeReactions(reactions));
        }

        return Promise.success(null);
    }


    //
    // Message Deletions
    //

    private Promise<Void> onMessageDeleted(Peer peer, List<Long> rids) {

        // Delete Messages
        conversation(peer).removeItems(JavaUtil.unbox(rids));

        Message head = conversation(peer).getHeadValue();

        return getDialogsRouter().onMessageDeleted(peer, head.getMessageState() == MessageState.PENDING ? null : head);
    }

    private Promise<Void> onChatClear(Peer peer) {

        conversation(peer).clear();

        return getDialogsRouter().onChatClear(peer);
    }

    private Promise<Void> onChatDelete(Peer peer) {

        conversation(peer).clear();

        return getDialogsRouter().onChatDelete(peer);
    }


    //
    // Read States
    //

    private Promise<Void> onMessageRead(Peer peer, long date) {
        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        boolean isChanged = false;
        Promise<Void> res;
        if (date > state.getOutReadDate()) {
            state = state.changeOutReadDate(date);
            res = getDialogsRouter().onPeerReadChanged(peer, date);
            isChanged = true;
        } else {
            res = Promise.success(null);
        }
        if (date > state.getOutReceiveDate()) {
            state = state.changeOutReceiveDate(date);
            isChanged = true;
        }
        if (isChanged) {
            conversationStates.addOrUpdateItem(state);
        }

        return res;
    }

    private Promise<Void> onMessageReceived(Peer peer, long date) {
        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        if (date > state.getOutReceiveDate()) {
            state = state.changeOutReceiveDate(date);
            conversationStates.addOrUpdateItem(state);

            return getDialogsRouter().onPeerReceiveChanged(peer, date);
        } else {
            return Promise.success(null);
        }
    }

    private Promise<Void> onMessageReadByMe(Peer peer, long date, int counter) {

        ConversationState state = conversationStates.getValue(peer.getUnuqueId());
        state = state
                .changeCounter(counter)
                .changeInReadDate(date);
        conversationStates.addOrUpdateItem(state);

        Promise<Void> res = getDialogsRouter().onCounterChanged(peer, counter);

        notifyActiveDialogsVM();

        context().getNotificationsModule().onOwnRead(peer, date);

        return res;
    }


    //
    // Peer Changed
    //

    private Promise<Void> onPeersChanged(List<User> users, List<Group> groups) {

        Promise<Void> res = Promise.success(null);

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
            res = res.chain(v -> getDialogsRouter().onUserChanged(u));
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

            res = res.chain(v -> getDialogsRouter().onGroupChanged(group));
        }

        if (isActiveNeedUpdate) {
            notifyActiveDialogsVM();
        }

        return res;
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

                notifyActiveDialogsVM();

                getDialogsRouter().onCounterChanged(peer, 0);
            }
        }
    }


    //
    // Difference Handling
    //

    public Promise<Void> onDifferenceStart() {
        context().getNotificationsModule().pauseNotifications();
        return Promise.success(null);
    }

    public Promise<Void> onDifferenceEnd() {
        context().getNotificationsModule().resumeNotifications();
        return Promise.success(null);
    }


    //
    // Tools
    //

    private DialogsInt getDialogsRouter() {
        return context().getMessagesModule().getDialogsInt();
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

    public boolean isValidPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return users().getValue(peer.getPeerId()) != null;
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return groups().getValue(peer.getPeerId()) != null;
        }
        return false;
    }


    //
    // Messages
    //

    public Promise<Void> onUpdate(Update update) {
        if (update instanceof UpdateMessage) {
            UpdateMessage msg = (UpdateMessage) update;

            Peer peer = convert(msg.getPeer());

            AbsContent msgContent = AbsContent.fromMessage(msg.getMessage());

            Message message = new Message(
                    msg.getRid(),
                    msg.getDate(),
                    msg.getDate(),
                    msg.getSenderUid(),
                    myUid() == msg.getSenderUid() ? MessageState.SENT : MessageState.UNKNOWN,
                    msgContent);

            ArrayList<Message> messages = new ArrayList<>();
            messages.add(message);
            return onNewMessages(peer, messages);
        } else if (update instanceof UpdateMessageSent) {
            UpdateMessageSent messageSent = (UpdateMessageSent) update;
            Peer peer = convert(messageSent.getPeer());
            if (isValidPeer(peer)) {
                // Notify Sender
                context().getMessagesModule()
                        .getSendMessageActor()
                        .send(new SenderActor.MessageSent(peer, messageSent.getRid()));
                onOutgoingSent(
                        peer,
                        messageSent.getRid(),
                        messageSent.getDate());
            }
            return Promise.success(null);
        } else if (update instanceof UpdateMessageRead) {
            UpdateMessageRead read = (UpdateMessageRead) update;
            Peer peer = convert(read.getPeer());
            if (isValidPeer(peer)) {
                onMessageRead(peer, read.getStartDate());
            }
            return Promise.success(null);
        } else if (update instanceof UpdateMessageReadByMe) {
            UpdateMessageReadByMe readByMe = (UpdateMessageReadByMe) update;
            Peer peer = convert(readByMe.getPeer());
            if (isValidPeer(peer)) {
                int counter = 0;
                if (readByMe.getUnreadCounter() != null) {
                    counter = readByMe.getUnreadCounter();
                }
                onMessageReadByMe(peer, readByMe.getStartDate(), counter);
            }
            return Promise.success(null);
        } else if (update instanceof UpdateMessageReceived) {
            UpdateMessageReceived received = (UpdateMessageReceived) update;
            Peer peer = convert(received.getPeer());
            if (isValidPeer(peer)) {
                onMessageReceived(peer, received.getStartDate());
            }
            return Promise.success(null);
        } else if (update instanceof UpdateChatDelete) {
            UpdateChatDelete delete = (UpdateChatDelete) update;
            Peer peer = convert(delete.getPeer());
            if (isValidPeer(peer)) {
                onChatDelete(peer);
            }
            return Promise.success(null);
        } else if (update instanceof UpdateChatClear) {
            UpdateChatClear clear = (UpdateChatClear) update;
            Peer peer = convert(clear.getPeer());
            if (isValidPeer(peer)) {
                onChatClear(peer);
            }
            return Promise.success(null);
        } else if (update instanceof UpdateChatGroupsChanged) {
            UpdateChatGroupsChanged chatGroupsChanged = (UpdateChatGroupsChanged) update;
            onActiveDialogsChanged(chatGroupsChanged.getDialogs(), true, true);
            return Promise.success(null);
        } else if (update instanceof UpdateMessageDelete) {
            UpdateMessageDelete delete = (UpdateMessageDelete) update;
            Peer peer = convert(delete.getPeer());
            if (isValidPeer(peer)) {
                onMessageDeleted(peer, delete.getRids());
            }
            return Promise.success(null);
        } else if (update instanceof UpdateMessageContentChanged) {
            UpdateMessageContentChanged contentChanged = (UpdateMessageContentChanged) update;
            Peer peer = convert(contentChanged.getPeer());
            if (isValidPeer(peer)) {
                AbsContent content = AbsContent.fromMessage(contentChanged.getMessage());
                onContentUpdate(peer, contentChanged.getRid(), content);
            }
            return Promise.success(null);
        } else if (update instanceof UpdateReactionsUpdate) {
            UpdateReactionsUpdate reactionsUpdate = (UpdateReactionsUpdate) update;
            Peer peer = convert(reactionsUpdate.getPeer());
            if (isValidPeer(peer)) {
                ArrayList<Reaction> reactions = new ArrayList<>();
                for (ApiMessageReaction r : reactionsUpdate.getReactions()) {
                    reactions.add(new Reaction(r.getCode(), r.getUsers()));
                }
                onReactionsUpdate(peer, reactionsUpdate.getRid(), reactions);
            }
            return Promise.success(null);
        }

        return Promise.success(null);
    }

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
        } else {
            super.onReceive(message);
        }
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (!activeDialogStorage.isLoaded() && message instanceof RouterMessageOnlyActive) {
            stash();
            return null;
        }
        if (message instanceof RouterMessageUpdate) {
            return onUpdate(((RouterMessageUpdate) message).getUpdate());
        } else if (message instanceof RouterDifferenceStart) {
            return onDifferenceStart();
        } else if (message instanceof RouterDifferenceEnd) {
            return onDifferenceEnd();
        } else if (message instanceof RouterPeersChanged) {
            RouterPeersChanged peersChanged = (RouterPeersChanged) message;
            return onPeersChanged(peersChanged.getUsers(), peersChanged.getGroups());
        } else if (message instanceof RouterApplyChatHistory) {
            RouterApplyChatHistory chatHistory = (RouterApplyChatHistory) message;
            return onChatHistoryLoaded(
                    chatHistory.getPeer(),
                    chatHistory.getMessages(),
                    chatHistory.getMaxReadDate(),
                    chatHistory.getMaxReceiveDate(),
                    chatHistory.isEnded());
        } else if (message instanceof RouterApplyDialogsHistory) {
            RouterApplyDialogsHistory dialogsHistory = (RouterApplyDialogsHistory) message;
            return onDialogHistoryLoaded(dialogsHistory.getDialogs());
        } else if (message instanceof RouterNewMessages) {
            RouterNewMessages routerNewMessages = (RouterNewMessages) message;
            return onNewMessages(
                    routerNewMessages.getPeer(),
                    routerNewMessages.getMessages());
        } else if (message instanceof RouterOutgoingMessage) {
            RouterOutgoingMessage routerOutgoingMessage = (RouterOutgoingMessage) message;
            return onOutgoingMessage(
                    routerOutgoingMessage.getPeer(),
                    routerOutgoingMessage.getMessage());
        } else if (message instanceof RouterOutgoingSent) {
            RouterOutgoingSent routerOutgoingSent = (RouterOutgoingSent) message;
            return onOutgoingSent(
                    routerOutgoingSent.getPeer(),
                    routerOutgoingSent.getRid(),
                    routerOutgoingSent.getDate());
        } else if (message instanceof RouterOutgoingError) {
            RouterOutgoingError outgoingError = (RouterOutgoingError) message;
            return onOutgoingError(
                    outgoingError.getPeer(),
                    outgoingError.getRid());
        } else if (message instanceof RouterChangedContent) {
            RouterChangedContent routerChangedContent = (RouterChangedContent) message;
            return onContentUpdate(
                    routerChangedContent.getPeer(),
                    routerChangedContent.getRid(),
                    routerChangedContent.getContent());
        } else if (message instanceof RouterDeletedMessages) {
            RouterDeletedMessages routerDeletedMessages = (RouterDeletedMessages) message;
            return onMessageDeleted(
                    routerDeletedMessages.getPeer(),
                    routerDeletedMessages.getRids());
        } else {
            return super.onAsk(message);
        }
    }
}
