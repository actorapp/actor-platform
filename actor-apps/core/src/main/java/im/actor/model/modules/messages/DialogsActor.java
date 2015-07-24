/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.annotation.Verified;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.ContentType;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.DialogBuilder;
import im.actor.model.entity.Group;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.User;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.DialogHistory;
import im.actor.model.modules.utils.ModuleActor;

import static im.actor.model.util.JavaUtil.equalsE;

public class DialogsActor extends ModuleActor {

    private ListEngine<Dialog> dialogs;

    public DialogsActor(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        super.preStart();
        this.dialogs = modules().getMessagesModule().getDialogsEngine();
        notifyState();
    }

    @Verified
    private void onMessage(Peer peer, Message message, boolean forceWrite, int counter) {
        PeerDesc peerDesc = buildPeerDesc(peer);
        if (peerDesc == null) {
            return;
        }

        if (message == null) {
            // Ignore empty message if not forcing write
            if (!forceWrite) {
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

            if (dialog != null) {
                // Ignore old messages if no force
                if (!forceWrite && dialog.getSortDate() > message.getSortDate()) {
                    return;
                }

                builder.setPeer(dialog.getPeer())
                        .setDialogTitle(dialog.getDialogTitle())
                        .setDialogAvatar(dialog.getDialogAvatar())
                        .setUnreadCount(dialog.getUnreadCount())
                        .setSortKey(dialog.getSortDate());

                // Do not push up dialogs for silent messages
                if (!contentDescription.isSilent()) {
                    builder.setSortKey(message.getSortDate());
                }
            } else {
                // Do not create dialogs for silent messages
                if (contentDescription.isSilent()) {
                    return;
                }

                builder.setPeer(peer)
                        .setDialogTitle(peerDesc.getTitle())
                        .setDialogAvatar(peerDesc.getAvatar())
                        .setUnreadCount(0)
                        .setSortKey(message.getSortDate());
            }

            addOrUpdateItem(builder.createDialog());
            notifyState();
        }
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
            addOrUpdateItem(dialog.editPeerInfo(user.getName(), user.getAvatar()));
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
            addOrUpdateItem(dialog.editPeerInfo(group.getTitle(), group.getAvatar()));
        }
    }

    @Verified
    private void onChatDeleted(Peer peer) {
        // Removing dialog
        dialogs.removeItem(peer.getUnuqueId());

        notifyState();
    }

    @Verified
    private void onChatClear(Peer peer) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());

        // If we have dialog for this peer
        if (dialog != null) {

            // Update dialog
            addOrUpdateItem(new DialogBuilder(dialog)
                    .setMessageType(ContentType.EMPTY)
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

//    @Verified
//    private void onMessageSent(Peer peer, long rid, long date) {
//        Dialog dialog = dialogs.getValue(peer.getUnuqueId());
//
//        // If message is on top
//        if (dialog != null && dialog.getRid() == rid) {
//
//            // Update dialog
//            addOrUpdateItem(new DialogBuilder(dialog)
//                    .setStatus(MessageState.SENT)
//                    .setTime(date)
//                    .createDialog());
//        }
//    }

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

            // Update dialog
            addOrUpdateItem(new DialogBuilder(dialog)
                    .setUnreadCount(count)
                    .createDialog());
        }
    }

    @Verified
    private void onHistoryLoaded(List<DialogHistory> history) {
        Log.d("AppStateVM", "onHistoryLoaded");
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
        modules().getAppStateModule().onDialogsLoaded();
        notifyState();
    }

    // Utils

    private void addOrUpdateItems(List<Dialog> updated) {
        dialogs.addOrUpdateItems(updated);
        modules().getSearch().onDialogsChanged(updated);
    }

    private void addOrUpdateItem(Dialog dialog) {
        dialogs.addOrUpdateItem(dialog);
        ArrayList<Dialog> d = new ArrayList<Dialog>();
        d.add(dialog);
        modules().getSearch().onDialogsChanged(d);
    }

    private void notifyState() {
        boolean isEmpty = this.dialogs.isEmpty();
        Log.d("NOTIFY_DIALOGS", "isEmpty: " + isEmpty);
        modules().getAppStateModule().onDialogsUpdate(isEmpty);
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
