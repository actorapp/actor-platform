package im.actor.model.modules;

import im.actor.model.Messenger;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.Peer;
import im.actor.model.modules.typing.OwnTypingActor;
import im.actor.model.modules.typing.TypingActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 15.02.15.
 */
public class Typing {
    private ActorRef ownTypingActor;
    private ActorRef typingActor;

    public Typing(final Messenger messenger) {
        this.ownTypingActor = system().actorOf(Props.create(OwnTypingActor.class, new ActorCreator<OwnTypingActor>() {
            @Override
            public OwnTypingActor create() {
                return new OwnTypingActor(messenger);
            }
        }), "actor/typing/own");
        this.typingActor = TypingActor.get(messenger);
    }

    public void onTyping(Peer peer) {
        ownTypingActor.send(new OwnTypingActor.Typing(peer));
    }
}
