/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.util.HashMap;

import im.actor.core.entity.Peer;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.typing.OwnTypingActor;
import im.actor.core.modules.internal.typing.TypingActor;
import im.actor.core.viewmodel.GroupTypingVM;
import im.actor.core.viewmodel.UserTypingVM;
import im.actor.runtime.actors.ActorRef;

public class TypingModule extends AbsModule {

    private ActorRef ownTypingActor;
    private ActorRef typingActor;
    private HashMap<Integer, UserTypingVM> uids = new HashMap<Integer, UserTypingVM>();
    private HashMap<Integer, GroupTypingVM> groups = new HashMap<Integer, GroupTypingVM>();

    public TypingModule(final ModuleContext context) {
        super(context);

        this.ownTypingActor = OwnTypingActor.get(context);
        this.typingActor = TypingActor.get(context);
    }

    public GroupTypingVM getGroupTyping(int gid) {
        synchronized (groups) {
            if (!groups.containsKey(gid)) {
                groups.put(gid, new GroupTypingVM(gid));
            }
            return groups.get(gid);
        }
    }

    public UserTypingVM getTyping(int uid) {
        synchronized (uids) {
            if (!uids.containsKey(uid)) {
                uids.put(uid, new UserTypingVM(uid));
            }
            return uids.get(uid);
        }
    }

    public void onTyping(Peer peer) {
        ownTypingActor.send(new OwnTypingActor.Typing(peer));
    }

    public void resetModule() {
        // TODO: Implement
    }
}
