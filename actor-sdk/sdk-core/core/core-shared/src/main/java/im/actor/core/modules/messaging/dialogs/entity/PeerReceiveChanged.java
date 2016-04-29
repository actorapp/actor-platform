package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.Peer;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class PeerReceiveChanged implements AskMessage<Void> {

    private Peer peer;
    private long date;

    public PeerReceiveChanged(Peer peer, long date) {
        this.peer = peer;
        this.date = date;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getDate() {
        return date;
    }
}
