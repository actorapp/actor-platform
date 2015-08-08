/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import im.actor.runtime.annotations.Verified;
import im.actor.core.api.Peer;
import im.actor.core.api.PeerType;
import im.actor.core.api.TypingType;
import im.actor.runtime.actors.ActorRef;
import im.actor.core.modules.BaseModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.typing.TypingActor;

@Verified
public class TypingProcessor extends BaseModule {
    private ActorRef typingActor;

    @Verified
    public TypingProcessor(Modules modules) {
        super(modules);
        this.typingActor = TypingActor.get(modules);
    }

    @Verified
    public void onTyping(Peer peer, int uid, TypingType type) {
        if (peer.getType() == PeerType.PRIVATE) {
            typingActor.sendOnce(new TypingActor.PrivateTyping(uid, type));
        } else if (peer.getType() == PeerType.GROUP) {
            typingActor.sendOnce(new TypingActor.GroupTyping(peer.getId(), uid, type));
        }
    }

    @Verified
    public void onMessage(Peer peer, int uid) {
        if (peer.getType() == PeerType.PRIVATE) {
            typingActor.sendOnce(new TypingActor.StopTyping(uid));
        } else if (peer.getType() == PeerType.GROUP) {
            typingActor.sendOnce(new TypingActor.StopGroupTyping(peer.getId(), uid));
        }
    }
}
