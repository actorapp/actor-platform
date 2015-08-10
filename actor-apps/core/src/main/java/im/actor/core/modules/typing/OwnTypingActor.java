/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.typing;

import im.actor.runtime.annotations.Verified;
import im.actor.core.api.OutPeer;
import im.actor.core.api.TypingType;
import im.actor.core.api.rpc.RequestTyping;
import im.actor.runtime.actors.ActorTime;
import im.actor.core.entity.Peer;
import im.actor.core.modules.Modules;
import im.actor.core.modules.utils.ModuleActor;

@Verified
public class OwnTypingActor extends ModuleActor {

    private static final long TYPING_DELAY = 3000L;

    private long lastTypingTime = 0;

    @Verified
    public OwnTypingActor(Modules messenger) {
        super(messenger);
    }

    @Verified
    private void onTyping(Peer peer) {
        if (ActorTime.currentTime() - lastTypingTime < TYPING_DELAY) {
            return;
        }
        lastTypingTime = ActorTime.currentTime();

        OutPeer outPeer = buidOutPeer(peer);
        if (outPeer == null) {
            return;
        }

        request(new RequestTyping(outPeer, TypingType.TEXT));
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof Typing) {
            onTyping(((Typing) message).getPeer());
        } else {
            drop(message);
        }
    }

    public static class Typing {
        private Peer peer;

        public Typing(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }
}
