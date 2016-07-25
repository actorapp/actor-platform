package im.actor.core.modules.messaging.history;

import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class ConversationHistory extends ActorInterface {

    public ConversationHistory(Peer peer, ModuleContext context) {
        setDest(system().actorOf("history/" + peer, () -> {
            return new ConversationHistoryActor(peer, context);
        }));
    }

    public void loadMore() {
        send(new ConversationHistoryActor.LoadMore());
    }

    public Promise<Void> reset() {
        return ask(new ConversationHistoryActor.Reset());
    }
}
