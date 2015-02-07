package com.droidkit.actors.debug;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.mailbox.Envelope;

/**
 * Created by ex3ndr on 04.10.14.
 */
public interface TraceInterface {
    public void onEnvelopeDelivered(Envelope envelope);

    public void onEnvelopeProcessed(Envelope envelope, long duration);

    public void onDrop(ActorRef sender, Object message, Actor actor);

    public void onDeadLetter(ActorRef receiver, Object message);

    public void onActorDie(ActorRef ref, Exception e);
}
