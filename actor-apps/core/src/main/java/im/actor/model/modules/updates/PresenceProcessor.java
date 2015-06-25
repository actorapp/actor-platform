/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.updates;

import im.actor.model.annotation.Verified;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.presence.PresenceActor;

@Verified
public class PresenceProcessor extends BaseModule {
    private ActorRef presenceActor;

    @Verified
    public PresenceProcessor(Modules modules) {
        super(modules);
        this.presenceActor = PresenceActor.get(modules);
    }

    @Verified
    public void onUserOnline(int uid, long updateDate) {
        presenceActor.sendOnce(new PresenceActor.UserOnline(uid, updateDate));
    }

    @Verified
    public void onUserOffline(int uid, long updateDate) {
        presenceActor.sendOnce(new PresenceActor.UserOffline(uid, updateDate));
    }

    @Verified
    public void onUserLastSeen(int uid, long date, long updateDate) {
        presenceActor.sendOnce(new PresenceActor.UserLastSeen(uid, date, updateDate));
    }

    @Verified
    public void onGroupOnline(int gid, int count, long updateDate) {
        presenceActor.sendOnce(new PresenceActor.GroupOnline(gid, count, updateDate));
    }
}
