package im.actor.model.modules.updates;

import im.actor.model.Messenger;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.modules.presence.PresenceActor;

/**
 * Created by ex3ndr on 15.02.15.
 */
public class PresenceProcessor {
    public Messenger messenger;
    private ActorRef presenceActor;

    public PresenceProcessor(Messenger messenger) {
        this.messenger = messenger;
        this.presenceActor = PresenceActor.get(messenger);
    }

    public void onUserOnline(int uid) {
        presenceActor.sendOnce(new PresenceActor.UserOnline(uid));
    }

    public void onUserOffline(int uid) {
        presenceActor.sendOnce(new PresenceActor.UserOffline(uid));
    }

    public void onUserLastSeen(int uid, long date) {
        presenceActor.sendOnce(new PresenceActor.UserLastSeen(uid, date));
    }

    public void onGroupOnline(int gid, int count) {
        presenceActor.sendOnce(new PresenceActor.GroupOnline(gid, count));
    }
}
