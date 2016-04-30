/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.typing;

import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiTypingType;
import im.actor.core.api.updates.UpdateTyping;
import im.actor.core.api.updates.UpdateTypingStop;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.WeakProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.annotations.Verified;

@Verified
public class TypingProcessor extends AbsModule implements WeakProcessor {

    private ActorRef typingActor;

    @Verified
    public TypingProcessor(ModuleContext modules) {
        super(modules);
        this.typingActor = TypingActor.get(modules);
    }

    @Verified
    public void onTyping(ApiPeer peer, int uid, ApiTypingType type) {
        if (peer.getType() == ApiPeerType.PRIVATE) {
            typingActor.send(new TypingActor.PrivateTyping(uid, type));
        } else if (peer.getType() == ApiPeerType.GROUP) {
            typingActor.send(new TypingActor.GroupTyping(peer.getId(), uid, type));
        }
    }

    @Verified
    public void onTypingStop(ApiPeer peer, int uid, ApiTypingType typingType) {
        // Other types are unsupported
        // TODO: Move to Actor
        if (typingType != ApiTypingType.TEXT) {
            return;
        }
        if (peer.getType() == ApiPeerType.PRIVATE) {
            typingActor.send(new TypingActor.StopTyping(uid));
        } else if (peer.getType() == ApiPeerType.GROUP) {
            typingActor.send(new TypingActor.StopGroupTyping(peer.getId(), uid));
        }
    }

    @Verified
    public void onMessage(ApiPeer peer, int uid) {
        if (peer.getType() == ApiPeerType.PRIVATE) {
            typingActor.send(new TypingActor.StopTyping(uid));
        } else if (peer.getType() == ApiPeerType.GROUP) {
            typingActor.send(new TypingActor.StopGroupTyping(peer.getId(), uid));
        }
    }

    @Override
    public boolean process(Update update, long date) {
        if (update instanceof UpdateTyping) {
            UpdateTyping typing = (UpdateTyping) update;
            onTyping(typing.getPeer(), typing.getUid(), typing.getTypingType());
            return true;
        } else if (update instanceof UpdateTypingStop) {
            UpdateTypingStop typing = (UpdateTypingStop) update;
            onTypingStop(typing.getPeer(), typing.getUid(), typing.getTypingType());
            return true;
        }
        return false;
    }
}
