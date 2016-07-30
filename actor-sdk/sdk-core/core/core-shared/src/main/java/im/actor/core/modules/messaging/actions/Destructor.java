package im.actor.core.modules.messaging.actions;

import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actions.entity.MessageDesc;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class Destructor extends ActorInterface {

    public Destructor(ModuleContext context) {
        super(system().actorOf("router/destructor", () -> new DestructorActor(context)));
    }

    public Promise<Void> onMessages(Peer peer, List<MessageDesc> messages) {
        return ask(new DestructorActor.NewMessages(peer, messages));
    }

    public Promise<Void> onMessageRead(Peer peer, long readDate) {
        return ask(new DestructorActor.MessageRead(peer, readDate));
    }

    public Promise<Void> onMessageReadByMe(Peer peer, long readDate) {
        return ask(new DestructorActor.MessageReadByMe(peer, readDate));
    }
}
