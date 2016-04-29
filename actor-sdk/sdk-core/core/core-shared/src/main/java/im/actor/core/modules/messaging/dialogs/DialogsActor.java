/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.dialogs;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.ContentDescription;
import im.actor.core.entity.ContentType;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.DialogBuilder;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.dialogs.entity.ChatClear;
import im.actor.core.modules.messaging.dialogs.entity.ChatDelete;
import im.actor.core.modules.messaging.dialogs.entity.CounterChanged;
import im.actor.core.modules.messaging.dialogs.entity.GroupChanged;
import im.actor.core.modules.messaging.dialogs.entity.HistoryLoaded;
import im.actor.core.modules.messaging.dialogs.entity.InMessage;
import im.actor.core.modules.messaging.dialogs.entity.MessageContentChanged;
import im.actor.core.modules.messaging.dialogs.entity.MessageDeleted;
import im.actor.core.modules.messaging.dialogs.entity.PeerReadChanged;
import im.actor.core.modules.messaging.dialogs.entity.PeerReceiveChanged;
import im.actor.core.modules.messaging.dialogs.entity.UserChanged;
import im.actor.core.modules.messaging.history.entity.DialogHistory;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.promise.Promise;
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
    private Promise<Void> onMessage(Peer peer, Message message, boolean forceWrite, int counter) {
        long start = im.actor.runtime.Runtime.getCurrentTime();
        PeerDesc peerDesc = buildPeerDesc(peer);
        if (peerDesc == null) {
            Log.d("DialogsActor", "unknown peer desc");
            return Promise.success(null);
        }

        if (message == null) {
            // Ignore empty message if not forcing write
            if (!forceWrite) {
                Log.d("DialogsActor", "not force");
                return Promise.success(null);
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
                    .setSenderId(message.getSenderId());

            if (counter >= 0) {
                builder.setUnreadCount(counter);
            }

            boolean forceUpdate = false;

            if (dialog != null) {
                // Ignore old messages if no force
                if (!forceWrite && dialog.getSortDate() > message.getSortDate()) {
                    Log.d("DialogsActor", "too old");
                    return Promise.success(null);
                }

                builder.setPeer(dialog.getPeer())
                        .setDialogTitle(dialog.getDialogTitle())
                        .setDialogAvatar(dialog.getDialogAvatar())
                        .setSortKey(dialog.getSortDate())
                        .updateKnownReceiveDate(dialog.getKnownReceiveDate())
                        .updateKnownReadDate(dialog.getKnownReadDate());

                // Do not push up dialogs for silent messages
                if (!contentDescription.isSilent()) {
                    builder.setSortKey(message.getSortDate());
                }

            } else {
                // Do not create dialogs for silent messages
                if (contentDescription.isSilent()) {
                    Log.d("DialogsActor", "is silent in");
                    return Promise.success(null);
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

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onUserChanged(User user) {
        Dialog dialog = dialogs.getValue(user.peer().getUnuqueId());
        if (dialog != null) {
            // Ignore if nothing changed
            if (dialog.getDialogTitle().equals(user.getName())
                    && equalsE(dialog.getDialogAvatar(), user.getAvatar())) {
                return Promise.success(null);
            }

            // Update dialog peer info
            Dialog updated = dialog.editPeerInfo(user.getName(), user.getAvatar());
            addOrUpdateItem(updated);
            updateSearch(updated);
        }

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onGroupChanged(Group group) {
        Dialog dialog = dialogs.getValue(group.peer().getUnuqueId());
        if (dialog != null) {
            // Ignore if nothing changed
            if (dialog.getDialogTitle().equals(group.getTitle())
                    && equalsE(dialog.getDialogAvatar(), group.getAvatar())) {
                return Promise.success(null);
            }

            // Update dialog peer info
            Dialog updated = dialog.editPeerInfo(group.getTitle(), group.getAvatar());
            addOrUpdateItem(updated);
            updateSearch(updated);
        }
        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onChatDeleted(Peer peer) {

        dialogs.removeItem(peer.getUnuqueId());

        notifyState(true);

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onChatClear(Peer peer) {
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
                    .createDialog());
        }

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onPeerRead(Peer peer, long date) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());
        if (dialog != null) {
            addOrUpdateItem(new DialogBuilder(dialog)
                    .updateKnownReadDate(date)
                    .updateKnownReceiveDate(date)
                    .createDialog());
        }
        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onPeerReceive(Peer peer, long date) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());
        if (dialog != null) {
            addOrUpdateItem(new DialogBuilder(dialog)
                    .updateKnownReceiveDate(date)
                    .createDialog());
        }
        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onMessageContentChanged(Peer peer, long rid, AbsContent content) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());

        // If message is on top
        if (dialog != null && dialog.getRid() == rid) {

            ContentDescription description = ContentDescription.fromContent(content);

            addOrUpdateItem(new DialogBuilder(dialog)
                    .setText(description.getText())
                    .setRelatedUid(description.getRelatedUser())
                    .setMessageType(description.getContentType())
                    .createDialog());
        }

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onCounterChanged(Peer peer, int count) {
        Dialog dialog = dialogs.getValue(peer.getUnuqueId());

        // If we have dialog for this peer and counter changed
        if (dialog != null && dialog.getUnreadCount() != count) {
            addOrUpdateItem(new DialogBuilder(dialog)
                    .setUnreadCount(count)
                    .createDialog());
        }

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onHistoryLoaded(List<DialogHistory> history) {
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

            DialogBuilder builder = new DialogBuilder()
                    .setPeer(dialogHistory.getPeer())
                    .setDialogTitle(peerDesc.getTitle())
                    .setDialogAvatar(peerDesc.getAvatar())
                    .setSortKey(dialogHistory.getSortDate())

                    .setRid(dialogHistory.getRid())
                    .setTime(dialogHistory.getDate())
                    .setMessageType(description.getContentType())
                    .setText(description.getText())
                    .setSenderId(dialogHistory.getSenderId())
                    .setRelatedUid(description.getRelatedUser())

                    .setUnreadCount(dialogHistory.getUnreadCount());

            if (dialogHistory.isRead()) {
                builder.updateKnownReadDate(dialogHistory.getDate());
                builder.updateKnownReceiveDate(dialogHistory.getDate());
            } else if (dialogHistory.isReceived()) {
                builder.updateKnownReceiveDate(dialogHistory.getDate());
            }

            updated.add(builder.createDialog());
        }
        addOrUpdateItems(updated);
        updateSearch(updated);
        context().getAppStateModule().onDialogsLoaded();
        notifyState(true);
        return Promise.success(null);
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
        } else if (message instanceof PeerReadChanged) {
            PeerReadChanged peerReadChanged = (PeerReadChanged) message;
            onPeerRead(peerReadChanged.getPeer(), peerReadChanged.getDate());
        } else if (message instanceof PeerReceiveChanged) {
            PeerReceiveChanged peerReceiveChanged = (PeerReceiveChanged) message;
            onPeerReceive(peerReceiveChanged.getPeer(), peerReceiveChanged.getDate());
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
            super.onReceive(message);
        }
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof InMessage) {
            InMessage inMessage = (InMessage) message;
            return onMessage(inMessage.getPeer(), inMessage.getMessage(), false, inMessage.getCounter());
        } else if (message instanceof UserChanged) {
            UserChanged userChanged = (UserChanged) message;
            return onUserChanged(userChanged.getUser());
        } else if (message instanceof ChatClear) {
            return onChatClear(((ChatClear) message).getPeer());
        } else if (message instanceof ChatDelete) {
            return onChatDeleted(((ChatDelete) message).getPeer());
        } else if (message instanceof PeerReadChanged) {
            PeerReadChanged peerReadChanged = (PeerReadChanged) message;
            return onPeerRead(peerReadChanged.getPeer(), peerReadChanged.getDate());
        } else if (message instanceof PeerReceiveChanged) {
            PeerReceiveChanged peerReceiveChanged = (PeerReceiveChanged) message;
            return onPeerReceive(peerReceiveChanged.getPeer(), peerReceiveChanged.getDate());
        } else if (message instanceof MessageDeleted) {
            MessageDeleted deleted = (MessageDeleted) message;
            return onMessage(deleted.getPeer(), deleted.getTopMessage(), true, -1);
        } else if (message instanceof HistoryLoaded) {
            HistoryLoaded historyLoaded = (HistoryLoaded) message;
            return onHistoryLoaded(historyLoaded.getHistory());
        } else if (message instanceof GroupChanged) {
            GroupChanged groupChanged = (GroupChanged) message;
            return onGroupChanged(groupChanged.getGroup());
        } else if (message instanceof MessageContentChanged) {
            MessageContentChanged contentChanged = (MessageContentChanged) message;
            return onMessageContentChanged(contentChanged.getPeer(), contentChanged.getRid(),
                    contentChanged.getContent());
        } else if (message instanceof CounterChanged) {
            CounterChanged counterChanged = (CounterChanged) message;
            return onCounterChanged(counterChanged.getPeer(), counterChanged.getCounter());
        } else {
            return super.onAsk(message);
        }
    }
}
