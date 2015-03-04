package im.actor.model.modules.updates;

import im.actor.model.annotation.Verified;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.presence.PresenceActor;

/**
 * Created by ex3ndr on 15.02.15.
 */
@Verified
public class PresenceProcessor extends BaseModule {
    private ActorRef presenceActor;

    @Verified
    public PresenceProcessor(Modules modules) {
        super(modules);
        this.presenceActor = PresenceActor.get(modules);
    }

    @Verified
    public void onUserOnline(int uid) {
        presenceActor.sendOnce(new PresenceActor.UserOnline(uid));
    }

    @Verified
    public void onUserOffline(int uid) {
        presenceActor.sendOnce(new PresenceActor.UserOffline(uid));
    }

    @Verified
    public void onUserLastSeen(int uid, long date) {
        presenceActor.sendOnce(new PresenceActor.UserLastSeen(uid, date));
    }

    @Verified
    public void onGroupOnline(int gid, int count) {
        presenceActor.sendOnce(new PresenceActor.GroupOnline(gid, count));
    }
}
