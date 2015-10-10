/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.ContentType;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.DialogBuilder;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.messages.entity.DialogHistory;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.storage.ListEngine;

import static im.actor.core.util.JavaUtil.equalsE;

public class DialogsActor extends ModuleActor {

    private ListEngine<Dialog> dialogs;
    private Boolean isEmpty;
    private Boolean emptyNotified;

    public DialogsActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        this.dialogs = context().getMessagesModule().getDialogsEngine();
        notifyState(true);
    }

    @Verified
    private void onMessage(Peer peer, Message message, boolean forceWrite, int counter) {
        long start = im.actor.runtime.Runtime.getCurrentTime();
        PeerDesc peerDesc = buildPeerDesc(peer);
        if (peerDesc == null) {
            Log.d("DialogsActor", "unknown peer desk");
            return;
        }

        if (message == null) {
            // Ignore empty message if not forcing write
            if (!forceWrite) {
                Log.d("DialogsActor", "not force");
                return;
            }

            // Else perform chat clear
            onChatClear(peer);
        } else {
            Dialog dialog = dialogs.getValue(peer.getUnuqueId());

            ContentDescription contentDescription = ContentDescription.fromContent(message.getContent());

            DialogBuilder builder = new DialogBuilder()
                    .setRid(message.getRid())
                    .setTime(message.getDate())
                    .setMessageType(contentDescription.getContentType())
                    .setText(contentDescription.getText())
                    .setRelatedUid(contentDescription.getRelatedUser())
                    .setStatus(message.getMessageState())
                    .setSenderId(message.getSenderId())
                    .setUnreadCount(counter);

            boolean forceUpdate = false;

            if (dialog != null) {
                // Ignore old messages if no force
                if (!forceWrite && dialog.getSortDate() > message.getSortDate()) {
                    Log.d("DialogsActor", "too old");
                    return;
                }

                builder.setPeer(dialog.getPeer())
                        .setDialogTitle(dialog.getDialogTitle())
                        .setDialogAvatar(dialog.getDialogAvatar())
                        .setSortKey(dialog.getSortDate());

                // Do not push up dialogs for silent messages
                if (!contentDescription.isSilent()) {
                    builder.setSortKey(message.getSortDate());
                }

            } else {
                // Do not create dialogs for silent messages
                if (contentDescription.isSilent()) {
                    Log.d("DialogsActor", "is silent in");
                    return;
                }

                builder.setPeer(peer)
                        .setDialogTitle(peerDesc.getTitle())
                        .setDialogAvatar(peerDesc.getAvatar())
                        .setSortKey(message.getSortDate());

                forceUpdate = true;
            }

            addOrUpdateItem(builder.createDialog());
            notifyState(forceUpdate);
        }

        Log.d("DialogsActor", "onMessage in " + (Runtime.getCurrentTime() - start) + " ms");
    }

    @Verified
    private void onUserChanged(User user) {
        Dialog dialog = dialogs.getValue(user.peer().getUnuqueId());
        if (dialog != null) {
            // Ignore if nothing changed
            if (dialog.getDialogTitle().equals(user.getName())
                    && equalsE(dialog.getDialogAvatar(), user.getAvatar())) {
                return;
            }

            // Update dialog peer info
            Dialog updated = dialog.editPeerInfo(user.getName(), user.getAvatar());
            addOrUpdateItem(updated);
            updateSearch(updated);
        }
    }

    @Verified
    private void onGroupChanged(Group group) {
        Dialog dialog = dialogs.getValue(group.peer().getUnuqueId());
        if (dialog != null) {
            // Ignore if nothing changed
            if (dialog.getDialogTitle().equals(group.getTitle())
                    && equalsE(dialog.getDialogAvatar(), group.getAvatar())) {
                return;
            }

            // Update dialog peer info
            Dialog updated = dialog.editPeerInfo(group.getTitle(), group.getAvatar());
            addOrUpdateItem(updated);
            updateSearch(updated);
        }
    }

    @Verified
    private void onChatDeleted(Peer peer) {
        // Removing dialog
        dialogs.removeItem(peer.getUnuqueId());

        notifyState(true);
    }

    @Verified
    private void onChatClear(Peer peer) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());

        // If we have dialog for this peer
        if (dialog != null) {

            // Update dialog
            addOrUpdateItem(new DialogBuilder(dialog)
                    .setMessageType(ContentType.NONE)
                    .setText("")
                    .setTime(0)
                    .setUnreadCount(0)
                    .setRid(0)
                    .setSenderId(0)
                    .setStatus(MessageState.UNKNOWN)
                    .createDialog());
        }
    }

    @Verified
    private void onMessageStatusChanged(Peer peer, long rid, MessageState state) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());

        // If message is on top
        if (dialog != null && dialog.getRid() == rid) {

            // Update dialog
            addOrUpdateItem(new DialogBuilder(dialog)
                    .setStatus(state)
                    .createDialog());
        }
    }

    @Verified
    private void onMessageContentChanged(Peer peer, long rid, AbsContent content) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());

        // If message is on top
        if (dialog != null && dialog.getRid() == rid) {

            // Update dialog
            ContentDescription description = ContentDescription.fromContent(content);
            addOrUpdateItem(new DialogBuilder(dialog)
                    .setText(description.getText())
                    .setRelatedUid(description.getRelatedUser())
                    .setMessageType(description.getContentType())
                    .createDialog());
        }
    }

    @Verified
    private void onCounterChanged(Peer peer, int count) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());

        // If we have dialog for this peer
        if (dialog != null) {

            // Counter not actually changed
            if (dialog.getUnreadCount() == count) {
                return;
            }

            // Update dialog
            addOrUpdateItem(new DialogBuilder(dialog)
                    .setUnreadCount(count)
                    .createDialog());
        }
    }

    @Verified
    private void onHistoryLoaded(List<DialogHistory> history) {
        ArrayList<Dialog> updated = new ArrayList<Dialog>();
        for (DialogHistory dialogHistory : history) {
            // Ignore already available dialogs
            if (dialogs.getValue(dialogHistory.getPeer().getUnuqueId()) != null) {
                continue;
            }

            PeerDesc peerDesc = buildPeerDesc(dialogHistory.getPeer());
            if (peerDesc == null) {
                continue;
            }

            ContentDescription description = ContentDescription.fromContent(dialogHistory.getContent());

            updated.add(new Dialog(dialogHistory.getPeer(),
                    dialogHistory.getSortDate(), peerDesc.getTitle(), peerDesc.getAvatar(),
                    dialogHistory.getUnreadCount(),
                    dialogHistory.getRid(), description.getContentType(), description.getText(), dialogHistory.getStatus(),
                    dialogHistory.getSenderId(), dialogHistory.getDate(), description.getRelatedUser()));
        }
        addOrUpdateItems(updated);
        updateSearch(updated);
        context().getAppStateModule().onDialogsLoaded();
        notifyState(true);
    }

    // Utils

    private void addOrUpdateItems(List<Dialog> updated) {
        dialogs.addOrUpdateItems(updated);
    }

    private void addOrUpdateItem(Dialog dialog) {
        dialogs.addOrUpdateItem(dialog);
    }

    private void updateSearch(Dialog dialog) {
        ArrayList<Dialog> d = new ArrayList<Dialog>();
        d.add(dialog);
        context().getSearchModule().onDialogsChanged(d);
    }

    private void updateSearch(List<Dialog> updated) {
        context().getSearchModule().onDialogsChanged(updated);
    }

    private void notifyState(boolean force) {
        if (isEmpty == null || force) {
            isEmpty = this.dialogs.isEmpty();
        }

        if (!isEmpty.equals(emptyNotified)) {
            emptyNotified = isEmpty;
            context().getAppStateModule().onDialogsUpdate(isEmpty);
        }
    }

    @Verified
    private PeerDesc buildPeerDesc(Peer peer) {
        switch (peer.getPeerType()) {
            case PRIVATE:
                User u = getUser(peer.getPeerId());
                return new PeerDesc(u.getName(), u.getAvatar());
            case GROUP:
                Group g = getGroup(peer.getPeerId());
                return new PeerDesc(g.getTitle(), g.getAvatar());
            default:
                return null;
        }
    }

    private class PeerDesc {

        private String title;
        private Avatar avatar;

        private PeerDesc(String title, Avatar avatar) {
            this.title = title;
            this.avatar = avatar;
        }

        public String getTitle() {
            return title;
        }

        public Avatar getAvatar() {
            return avatar;
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof InMessage) {
            InMessage inMessage = (InMessage) message;
            onMessage(inMessage.getPeer(), inMessage.getMessage(), false, inMessage.getCounter());
        } else if (message instanceof UserChanged) {
            UserChanged userChanged = (UserChanged) message;
            onUserChanged(userChanged.getUser());
        } else if (message instanceof ChatClear) {
            onChatClear(((ChatClear) message).getPeer());
        } else if (message instanceof ChatDelete) {
            onChatDeleted(((ChatDelete) message).getPeer());
        } else if (message instanceof MessageStateChanged) {
            MessageStateChanged messageStateChanged = (MessageStateChanged) message;
            onMessageStatusChanged(messageStateChanged.getPeer(), messageStateChanged.getRid(),
                    messageStateChanged.getState());
        } else if (message instanceof MessageDeleted) {
            MessageDeleted deleted = (MessageDeleted) message;
            onMessage(deleted.getPeer(), deleted.getTopMessage(), true, -1);
        } else if (message instanceof HistoryLoaded) {
            HistoryLoaded historyLoaded = (HistoryLoaded) message;
            onHistoryLoaded(historyLoaded.getHistory());
        } else if (message instanceof GroupChanged) {
            GroupChanged groupChanged = (GroupChanged) message;
            onGroupChanged(groupChanged.getGroup());
        } else if (message instanceof MessageContentChanged) {
            MessageContentChanged contentChanged = (MessageContentChanged) message;
            onMessageContentChanged(contentChanged.getPeer(), contentChanged.getRid(),
                    contentChanged.getContent());
        } else if (message instanceof CounterChanged) {
            CounterChanged counterChanged = (CounterChanged) message;
            onCounterChanged(counterChanged.getPeer(), counterChanged.getCounter());
        } else {
            drop(message);
        }
    }

    public static class InMessage {
        private Peer peer;
        private Message message;
        private int counter;

        public InMessage(Peer peer, Message message, int counter) {
            this.peer = peer;
            this.message = message;
            this.counter = counter;
        }

        public Peer getPeer() {
            return peer;
        }

        public Message getMessage() {
            return message;
        }

        public int getCounter() {
            return counter;
        }
    }

    public static class CounterChanged {
        private Peer peer;
        private int counter;

        public CounterChanged(Peer peer, int counter) {
            this.peer = peer;
            this.counter = counter;
        }

        public Peer getPeer() {
            return peer;
        }

        public int getCounter() {
            return counter;
        }
    }

    public static class UserChanged {
        private User user;

        public UserChanged(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    public static class GroupChanged {
        private Group group;

        public GroupChanged(Group group) {
            this.group = group;
        }

        public Group getGroup() {
            return group;
        }
    }

    public static class ChatClear {
        private Peer peer;

        public ChatClear(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class ChatDelete {
        private Peer peer;

        public ChatDelete(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class MessageStateChanged {
        private Peer peer;
        private long rid;
        private MessageState state;

        public MessageStateChanged(Peer peer, long rid, MessageState state) {
            this.peer = peer;
            this.rid = rid;
            this.state = state;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }

        public MessageState getState() {
            return state;
        }
    }

    public static class MessageContentChanged {
        private Peer peer;
        private long rid;
        private AbsContent content;

        public MessageContentChanged(Peer peer, long rid, AbsContent content) {
            this.peer = peer;
            this.rid = rid;
            this.content = content;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }

        public AbsContent getContent() {
            return content;
        }
    }

    public static class MessageDeleted {
        private Peer peer;
        private Message topMessage;

        public MessageDeleted(Peer peer, Message topMessage) {
            this.peer = peer;
            this.topMessage = topMessage;
        }

        public Peer getPeer() {
            return peer;
        }

        public Message getTopMessage() {
            return topMessage;
        }
    }

    public static class HistoryLoaded {
        private List<DialogHistory> history;

        public HistoryLoaded(List<DialogHistory> history) {
            this.history = history;
        }

        public List<DialogHistory> getHistory() {
            return history;
        }
    }
}
