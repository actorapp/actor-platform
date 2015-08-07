/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.actors;

import im.actor.model.droidkit.actors.mailbox.Envelope;

public interface TraceInterface {
    void onEnvelopeDelivered(Envelope envelope);

    void onEnvelopeProcessed(Envelope envelope, long duration);

    void onDrop(ActorRef sender, Object message, Actor actor);

    void onDeadLetter(ActorRef receiver, Object message);

    void onActorDie(ActorRef ref, Envelope envelope, Exception e);
}
