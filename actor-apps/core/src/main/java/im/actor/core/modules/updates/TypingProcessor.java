/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiTypingType;
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
    public void onTyping(ApiPeer peer, int uid, ApiTypingType type) {
        if (peer.getType() == ApiPeerType.PRIVATE) {
            typingActor.sendOnce(new TypingActor.PrivateTyping(uid, type));
        } else if (peer.getType() == ApiPeerType.GROUP) {
            typingActor.sendOnce(new TypingActor.GroupTyping(peer.getId(), uid, type));
        }
    }

    @Verified
    public void onMessage(ApiPeer peer, int uid) {
        if (peer.getType() == ApiPeerType.PRIVATE) {
            typingActor.sendOnce(new TypingActor.StopTyping(uid));
        } else if (peer.getType() == ApiPeerType.GROUP) {
            typingActor.sendOnce(new TypingActor.StopGroupTyping(peer.getId(), uid));
        }
    }
}
