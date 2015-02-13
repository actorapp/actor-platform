package im.actor.messenger.core.actors.api;

import com.droidkit.actors.ActorRef;
import com.droidkit.actors.typed.TypedActor;

import java.util.List;

import im.actor.api.ActorApiCallback;
import im.actor.api.parser.Update;
import im.actor.api.scheme.Group;
import im.actor.api.scheme.User;
import im.actor.messenger.core.actors.presence.UsersPresence;
import im.actor.messenger.core.actors.updates.UpdateBroker;
import im.actor.messenger.util.Logger;

/**
 * Created by ex3ndr on 04.09.14.
 */
public class ApiStateBroker extends TypedActor<ActorApiCallback> implements ActorApiCallback {

    private static final String TAG = "ApiStateBroker";

    private ActorRef seqActor;
    private ActorRef seqBroker;
    private ActorRef usersPresence;

    public ApiStateBroker() {
        super(ActorApiCallback.class);
    }

    @Override
    public void preStart() {
        super.preStart();
        seqActor = system().actorOf(SequenceActor.sequence());
        seqBroker = system().actorOf(UpdateBroker.sequenceBroker());
        usersPresence = UsersPresence.presence();
    }

    @Override
    public void onAuthIdInvalidated() {
        Logger.w(TAG, "Received AuthIdInvalidated");
        // TODO: Implement
    }

    @Override
    public void onNewSessionCreated() {
        Logger.w(TAG, "Received NewSessionCreated");
        seqActor.send(new NewSessionCreated());
        usersPresence.send(new NewSessionCreated());
    }

    @Override
    public void onSeqFatUpdate(int seq, byte[] state, Update update, List<User> users, List<Group> groups) {
        seqActor.send(new SequenceActor.SeqFatUpdate(seq, state, update, users, groups));
    }

    @Override
    public void onSeqUpdate(int seq, byte[] state, Update update) {
        seqActor.send(new SequenceActor.SeqUpdate(seq, state, update));
    }

    @Override
    public void onSeqTooLong() {
        seqActor.send(new SequenceActor.Invalidate());
    }

    @Override
    public void onWeakUpdate(long date, Update update) {
        seqBroker.send(update);
    }
}
