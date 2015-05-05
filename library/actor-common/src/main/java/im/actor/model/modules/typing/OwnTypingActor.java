/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.typing;

import im.actor.model.annotation.Verified;
import im.actor.model.api.OutPeer;
import im.actor.model.api.TypingType;
import im.actor.model.api.rpc.RequestTyping;
import im.actor.model.droidkit.actors.ActorTime;
import im.actor.model.entity.Peer;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

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
