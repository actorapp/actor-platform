/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.typing;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiTypingType;
import im.actor.core.api.rpc.RequestStopTyping;
import im.actor.core.api.rpc.RequestTyping;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.ActorTime;
import im.actor.runtime.actors.Props;
import im.actor.runtime.annotations.Verified;

@Verified
public class OwnTypingActor extends ModuleActor {

    public static ActorRef get(final ModuleContext context) {
        return ActorSystem.system().actorOf(Props.create(OwnTypingActor.class, new ActorCreator<OwnTypingActor>() {
            @Override
            public OwnTypingActor create() {
                return new OwnTypingActor(context);
            }
        }), "actor/typing/own");
    }

    private static final long TYPING_DELAY = 3000L;
    private static final long TYPING_CANCEL_DELAY = 4000L;

    private long lastTypingTime = 0;

    private long prevRid = 0;

    @Verified
    public OwnTypingActor(ModuleContext messenger) {
        super(messenger);
    }

    @Verified
    private void onTyping(Peer peer) {
        if (ActorTime.currentTime() - lastTypingTime < TYPING_DELAY) {
            return;
        }
        lastTypingTime = ActorTime.currentTime();

        ApiOutPeer outPeer = buidOutPeer(peer);
        if (outPeer == null) {
            return;
        }

        cancelPrevRequest();
        prevRid = request(new RequestTyping(outPeer, ApiTypingType.TEXT));
        self().sendOnce(new AbortTyping(peer), TYPING_CANCEL_DELAY);
    }

    private void onMessageSent(Peer peer) {
        cancelPrevRequest();
        lastTypingTime = 0;
        self().sendOnce(new AbortTyping(peer), 24 * 60 * 60 * 1000L);
    }

    private void onAbortTyping(Peer peer) {
        ApiOutPeer outPeer = buidOutPeer(peer);
        if (outPeer == null) {
            return;
        }
        cancelPrevRequest();
        prevRid = request(new RequestStopTyping(outPeer, ApiTypingType.TEXT));
    }

    private void cancelPrevRequest() {
        if (prevRid != 0) {
            cancelRequest(prevRid);
            prevRid = 0;
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof Typing) {
            onTyping(((Typing) message).getPeer());
        } else if (message instanceof MessageSent) {
            onMessageSent(((MessageSent) message).getPeer());
        } else if (message instanceof AbortTyping) {
            onAbortTyping(((AbortTyping) message).getPeer());
        } else {
            drop(message);
        }
    }

    private static class AbortTyping {
        private Peer peer;

        public AbortTyping(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
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

    public static class MessageSent {

        private Peer peer;

        public MessageSent(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }
}
