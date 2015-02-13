package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.Messenger;
import im.actor.model.modules.updates.SequenceActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Updates {

    private Messenger messenger;
    private ActorRef updateActor;

    public Updates(Messenger messenger) {
        this.messenger = messenger;
        run();
    }

    public void run() {
        this.updateActor = system().actorOf(Props.create(SequenceActor.class, new ActorCreator<SequenceActor>() {
            @Override
            public SequenceActor create() {
                return new SequenceActor(Updates.this);
            }
        }), "actor/updates");
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public void onSessionCreated() {
        updateActor.send(new SequenceActor.Invalidate());
    }

    public void onPushReceived(int seq) {
        updateActor.send(new SequenceActor.PushSeq(seq));
    }

    public void onUpdateReceived(Object update) {
        updateActor.send(update);
    }
}