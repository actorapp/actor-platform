/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.modules.updates.SequenceActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Updates extends BaseModule {

    private ActorRef updateActor;

    public Updates(Modules messenger) {
        super(messenger);
    }

    public void run() {
        this.updateActor = system().actorOf(Props.create(SequenceActor.class, new ActorCreator<SequenceActor>() {
            @Override
            public SequenceActor create() {
                return new SequenceActor(modules());
            }
        }), "actor/updates");
    }

    public void onNewSessionCreated() {
        updateActor.send(new SequenceActor.Invalidate());
    }

    public void onPushReceived(int seq) {
        updateActor.send(new SequenceActor.PushSeq(seq));
    }

    public void onUpdateReceived(Object update) {
        updateActor.send(update);
    }

    public void resetModule() {
        // TODO: Implement
    }
}