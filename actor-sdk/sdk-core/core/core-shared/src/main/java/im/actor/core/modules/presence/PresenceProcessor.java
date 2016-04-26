/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.presence;

import im.actor.core.api.updates.UpdateGroupOnline;
import im.actor.core.api.updates.UpdateUserLastSeen;
import im.actor.core.api.updates.UpdateUserOffline;
import im.actor.core.api.updates.UpdateUserOnline;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.WeakProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.annotations.Verified;

@Verified
public class PresenceProcessor extends AbsModule implements WeakProcessor {

    private ActorRef presenceActor;

    @Verified
    public PresenceProcessor(ModuleContext modules) {
        super(modules);
        this.presenceActor = PresenceActor.create(modules);
    }

    @Verified
    public void onUserOnline(int uid, long updateDate) {
        presenceActor.send(new PresenceActor.UserOnline(uid, updateDate));
    }

    @Verified
    public void onUserOffline(int uid, long updateDate) {
        presenceActor.send(new PresenceActor.UserOffline(uid, updateDate));
    }

    @Verified
    public void onUserLastSeen(int uid, int date, long updateDate) {
        presenceActor.send(new PresenceActor.UserLastSeen(uid, date, updateDate));
    }

    @Verified
    public void onGroupOnline(int gid, int count, long updateDate) {
        presenceActor.send(new PresenceActor.GroupOnline(gid, count, updateDate));
    }

    @Override
    public boolean process(Update update, long date) {
        if (update instanceof UpdateUserOnline) {
            UpdateUserOnline userOnline = (UpdateUserOnline) update;
            onUserOnline(userOnline.getUid(), date);
            return true;
        } else if (update instanceof UpdateUserOffline) {
            UpdateUserOffline offline = (UpdateUserOffline) update;
            onUserOffline(offline.getUid(), date);
            return true;
        } else if (update instanceof UpdateUserLastSeen) {
            UpdateUserLastSeen lastSeen = (UpdateUserLastSeen) update;
            onUserLastSeen(lastSeen.getUid(), (int) lastSeen.getDate(), date);
            return true;
        } else if (update instanceof UpdateGroupOnline) {
            UpdateGroupOnline groupOnline = (UpdateGroupOnline) update;
            onGroupOnline(groupOnline.getGroupId(), groupOnline.getCount(), date);
            return true;
        }
        return false;
    }
}
