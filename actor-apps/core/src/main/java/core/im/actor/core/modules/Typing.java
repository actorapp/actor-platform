/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import java.util.HashMap;

import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.core.entity.Peer;
import im.actor.core.modules.typing.OwnTypingActor;
import im.actor.core.modules.typing.TypingActor;
import im.actor.core.viewmodel.GroupTypingVM;
import im.actor.core.viewmodel.UserTypingVM;

import static im.actor.runtime.actors.ActorSystem.system;

public class Typing extends BaseModule {
    private ActorRef ownTypingActor;
    private ActorRef typingActor;
    private HashMap<Integer, UserTypingVM> uids = new HashMap<Integer, UserTypingVM>();
    private HashMap<Integer, GroupTypingVM> groups = new HashMap<Integer, GroupTypingVM>();

    public Typing(final Modules messenger) {
        super(messenger);
        this.ownTypingActor = system().actorOf(Props.create(OwnTypingActor.class, new ActorCreator<OwnTypingActor>() {
            @Override
            public OwnTypingActor create() {
                return new OwnTypingActor(messenger);
            }
        }), "actor/typing/own");
        this.typingActor = TypingActor.get(messenger);
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
