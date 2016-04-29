package im.actor.core.modules.messaging.dialogs;

import java.util.List;

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
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class DialogsInt extends ActorInterface {

    public DialogsInt(ModuleContext context) {
        setDest(system().actorOf("actor/dialogs", () -> new DialogsActor(context)));
    }

    public Promise<Void> onChatClear(Peer peer) {
        return ask(new ChatClear(peer));
    }

    public Promise<Void> onChatDelete(Peer peer) {
        return ask(new ChatDelete(peer));
    }

    public Promise<Void> onCounterChanged(Peer peer, int counter) {
        return ask(new CounterChanged(peer, counter));
    }

    public Promise<Void> onGroupChanged(Group group) {
        return ask(new GroupChanged(group));
    }

    public Promise<Void> onUserChanged(User user) {
        return ask(new UserChanged(user));
    }

    public Promise<Void> onHistoryLoaded(List<DialogHistory> history) {
        return ask(new HistoryLoaded(history));
    }

    public Promise<Void> onMessage(Peer peer, Message message, int counter) {
        return ask(new InMessage(peer, message, counter));
    }

    public Promise<Void> onMessageContentChanged(Peer peer, long rid, AbsContent content) {
        return ask(new MessageContentChanged(peer, rid, content));
    }

    public Promise<Void> onMessageDeleted(Peer peer, Message topMessage) {
        return ask(new MessageDeleted(peer, topMessage));
    }

    public Promise<Void> onPeerReadChanged(Peer peer, long date) {
        return ask(new PeerReadChanged(peer, date));
    }

    public Promise<Void> onPeerReceiveChanged(Peer peer, long date) {
        return ask(new PeerReceiveChanged(peer, date));
    }
}
