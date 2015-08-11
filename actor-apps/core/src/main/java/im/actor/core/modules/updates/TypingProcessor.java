/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import im.actor.core.api.Peer;
import im.actor.core.api.PeerType;
import im.actor.core.api.TypingType;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.typing.TypingActor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.annotations.Verified;

@Verified
public class TypingProcessor extends AbsModule {
    private ActorRef typingActor;

    @Verified
    public TypingProcessor(ModuleContext modules) {
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
