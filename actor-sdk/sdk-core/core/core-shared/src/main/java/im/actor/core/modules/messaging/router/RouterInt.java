package im.actor.core.modules.messaging.router;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.User;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.events.PeerChatClosed;
import im.actor.core.events.PeerChatOpened;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.history.entity.DialogHistory;
import im.actor.core.modules.messaging.router.entity.RouterActiveDialogsChanged;
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
import im.actor.core.modules.messaging.router.entity.RouterMessageRead;
import im.actor.core.modules.messaging.router.entity.RouterMessageReadByMe;
import im.actor.core.modules.messaging.router.entity.RouterMessageReceived;
import im.actor.core.modules.messaging.router.entity.RouterNewMessages;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingError;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingMessage;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingSent;
import im.actor.core.modules.messaging.router.entity.RouterPeersChanged;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

import static im.actor.runtime.actors.ActorSystem.system;

public class RouterInt extends ActorInterface implements BusSubscriber {

    private final ModuleContext context;

    public RouterInt(final ModuleContext context) {
        this.context = context;
        setDest(system().actorOf("actor/router", new ActorCreator() {
            @Override
            public Actor create() {
                return new RouterActor(context);
            }
        }));

        context.getEvents().subscribe(this, PeerChatOpened.EVENT);
        context.getEvents().subscribe(this, PeerChatClosed.EVENT);
        context.getEvents().subscribe(this, AppVisibleChanged.EVENT);
    }

    public void onNewMessage(Peer peer, Message message) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        onNewMessages(peer, messages);
    }

    public void onNewMessages(Peer peer, List<Message> messages) {
        send(new RouterNewMessages(peer, messages));
    }

    public void onOutgoingMessage(Peer peer, Message message) {
        send(new RouterOutgoingMessage(peer, message));
    }

    public void onOutgoingSent(Peer peer, long rid, long date) {
        send(new RouterOutgoingSent(peer, rid, date));
    }

    public void onOutgoingError(Peer peer, long rid) {
        send(new RouterOutgoingError(peer, rid));
    }

    public void onContentChanged(Peer peer, long rid, AbsContent content) {
        send(new RouterChangedContent(peer, rid, content));
    }

    public void onReactionsChanged(Peer peer, long rid, List<Reaction> reactions) {
        send(new RouterChangedReactions(peer, rid, reactions));
    }

    public void onMessagesDeleted(Peer peer, List<Long> rids) {
        send(new RouterDeletedMessages(peer, rids));
    }

    public void onMessageRead(Peer peer, long date) {
        send(new RouterMessageRead(peer, date));
    }

    public void onMessageReadByMe(Peer peer, long date, int counter) {
        send(new RouterMessageReadByMe(peer, date, counter));
    }

    public void onMessageReceived(Peer peer, long date) {
        send(new RouterMessageReceived(peer, date));
    }

    public void onDialogsHistoryLoaded(List<DialogHistory> histories, Runnable runnable) {
        send(new RouterApplyDialogsHistory(histories, runnable));
    }

    public void onChatHistoryLoaded(Peer peer, List<Message> history, Long maxReceivedDate, Long maxReadDate, boolean isEnded) {
        send(new RouterApplyChatHistory(peer, history, maxReceivedDate, maxReadDate, isEnded));
    }

    public void onChatClear(Peer peer) {
        send(new RouterChatClear(peer));
    }

    public void onChatDelete(Peer peer) {
        send(new RouterChatDelete(peer));
    }

    public void onActiveDialogsChanged(List<ApiDialogGroup> dialogs, boolean showInvite, boolean hasArchived) {
        send(new RouterActiveDialogsChanged(dialogs, showInvite, hasArchived));
    }

    public void onUserChanged(User user) {
        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        onUsersChanged(users);
    }

    public void onGroupChanged(Group group) {
        ArrayList<Group> groups = new ArrayList<>();
        groups.add(group);
        onGroupsChanged(groups);
    }

    public void onUsersChanged(List<User> users) {
        onPeersChanged(users, new ArrayList<Group>());
    }

    public void onGroupsChanged(List<Group> groups) {
        onPeersChanged(new ArrayList<User>(), groups);
    }

    public void onPeersChanged(List<User> users, List<Group> groups) {
        send(new RouterPeersChanged(users, groups));
    }

    public void onDifferenceStart() {
        send(new RouterDifferenceStart());
    }

    public void onDifferenceEnd() {
        send(new RouterDifferenceEnd());
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof PeerChatOpened) {
            PeerChatOpened peerChatOpened = (PeerChatOpened) event;
            send(new RouterConversationVisible(peerChatOpened.getPeer()));
        } else if (event instanceof PeerChatClosed) {
            PeerChatClosed peerChatClosed = (PeerChatClosed) event;
            send(new RouterConversationHidden(peerChatClosed.getPeer()));
        } else if (event instanceof AppVisibleChanged) {
            if (((AppVisibleChanged) event).isVisible()) {
                send(new RouterAppVisible());
            } else {
                send(new RouterAppVisible());
            }
        }
    }
}
