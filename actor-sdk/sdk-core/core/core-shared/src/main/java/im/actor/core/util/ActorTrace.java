/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.util;

import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorTime;
import im.actor.runtime.actors.TraceInterface;
import im.actor.runtime.actors.dispatch.Envelope;

/**
 * Actor System Trace Interface implementation for logging problems
 */
public class ActorTrace implements TraceInterface {

    private static final String TAG = "ACTOR_SYSTEM";

    private static final int PROCESS_THRESHOLD = 100;

    @Override
    public void onEnvelopeDelivered(Envelope envelope) {

    }

    @Override
    public void onEnvelopeProcessed(Envelope envelope, long duration) {
        if (duration > PROCESS_THRESHOLD) {
            Log.w(TAG, "Too long " + envelope.getScope().getPath() + " {" + envelope.getMessage() + "} in " + duration + " ms");
        }
    }

    @Override
    public void onDrop(ActorRef sender, Object message, Actor actor) {
        Log.w(TAG, "Drop: " + message);
    }

    @Override
    public void onDeadLetter(ActorRef receiver, Object message) {
        Log.w(TAG, "Dead Letter: " + message);
    }

    @Override
    public void onActorDie(ActorRef ref, Envelope envelope, Exception e) {
        Log.w(TAG, "Die(" + ref.getPath() + ") by " + envelope.getMessage() + " with " + e.getMessage());
        Log.e(TAG, e);
    }

    @Override
    public void onMessageSent(ActorRef ref, Object message) {

    }
}
