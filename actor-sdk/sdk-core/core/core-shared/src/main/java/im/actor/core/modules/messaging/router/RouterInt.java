package im.actor.core.modules.messaging.router;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.events.PeerChatClosed;
import im.actor.core.events.PeerChatOpened;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.history.entity.DialogHistory;
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
import im.actor.core.modules.messaging.router.entity.RouterMessageUpdate;
import im.actor.core.modules.messaging.router.entity.RouterNewMessages;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingError;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingMessage;
import im.actor.core.modules.messaging.router.entity.RouterPeersChanged;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class RouterInt extends ActorInterface implements BusSubscriber {

    private final ModuleContext context;

    public RouterInt(final ModuleContext context) {
        this.context = context;
        setDest(system().actorOf("actor/router", () -> new RouterActor(context)));

        context.getEvents().subscribe(this, PeerChatOpened.EVENT);
        context.getEvents().subscribe(this, PeerChatClosed.EVENT);
        context.getEvents().subscribe(this, AppVisibleChanged.EVENT);
    }


    //
    // Updates
    //

    public Promise<Void> onDifferenceStart() {
        return ask(new RouterDifferenceStart());
    }

    public Promise<Void> onUpdate(Update update) {
        return ask(new RouterMessageUpdate(update));
    }

    public Promise<Void> onDifferenceEnd() {
        return ask(new RouterDifferenceEnd());
    }


    //
    // New Messages
    //

    public Promise<Void> onNewMessage(Peer peer, Message message) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        return onNewMessages(peer, messages);
    }

    public Promise<Void> onNewMessages(Peer peer, List<Message> messages) {
        return ask(new RouterNewMessages(peer, messages));
    }


    //
    // Outgoing Messages
    //

    public Promise<Void> onOutgoingMessage(Peer peer, Message message) {
        return ask(new RouterOutgoingMessage(peer, message));
    }

    public Promise<Void> onOutgoingError(Peer peer, long rid) {
        return ask(new RouterOutgoingError(peer, rid));
    }

    public Promise<Void> onContentChanged(Peer peer, long rid, AbsContent content) {
        return ask(new RouterChangedContent(peer, rid, content));
    }


    //
    // Message Deletions
    //

    public Promise<Void> onMessagesDeleted(Peer peer, List<Long> rids) {
        return ask(new RouterDeletedMessages(peer, rids));
    }


    //
    // History
    //

    public Promise<Void> onDialogsHistoryLoaded(List<DialogHistory> histories) {
        return ask(new RouterApplyDialogsHistory(histories));
    }

    public Promise<Void> onChatHistoryLoaded(Peer peer, List<Message> history, Long maxReceivedDate, Long maxReadDate, boolean isEnded) {
        return ask(new RouterApplyChatHistory(peer, history, maxReceivedDate, maxReadDate, isEnded));
    }


    //
    // Peer Changed
    //

    public Promise<Void> onUserChanged(User user) {
        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        return onUsersChanged(users);
    }

    public Promise<Void> onGroupChanged(Group group) {
        ArrayList<Group> groups = new ArrayList<>();
        groups.add(group);
        return onGroupsChanged(groups);
    }

    public Promise<Void> onUsersChanged(List<User> users) {
        return onPeersChanged(users, new ArrayList<>());
    }

    public Promise<Void> onGroupsChanged(List<Group> groups) {
        return onPeersChanged(new ArrayList<>(), groups);
    }

    public Promise<Void> onPeersChanged(List<User> users, List<Group> groups) {
        return ask(new RouterPeersChanged(users, groups));
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
                send(new RouterAppHidden());
            }
        }
    }
}
