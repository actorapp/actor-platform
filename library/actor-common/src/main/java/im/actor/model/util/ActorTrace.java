package im.actor.model.util;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.TraceInterface;
import im.actor.model.droidkit.actors.mailbox.Envelope;
import im.actor.model.log.Log;

/**
 * Created by ex3ndr on 09.04.15.
 */
public class ActorTrace implements TraceInterface {

    private static final String TAG = "ACTOR_SYSTEM";
    private static final int PROCESS_THRESHOLD = 300;

    @Override
    public void onEnvelopeDelivered(Envelope envelope) {

    }

    @Override
    public void onEnvelopeProcessed(Envelope envelope, long duration) {
        if (duration > PROCESS_THRESHOLD) {
            Log.w(TAG, "Too long " + envelope.getScope().getPath() + " {" + envelope.getMessage() + "}");
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
    public void onActorDie(ActorRef ref, Exception e) {
        Log.w(TAG, "Die: " + e);
        e.printStackTrace();
    }
}
