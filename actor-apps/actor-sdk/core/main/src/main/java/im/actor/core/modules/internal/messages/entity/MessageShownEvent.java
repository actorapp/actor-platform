package im.actor.core.modules.internal.messages.entity;

import im.actor.core.entity.Peer;

public class MessageShownEvent {

    private Peer peer;
    private long sortDate;

    public MessageShownEvent(Peer peer, long sortDate) {
        this.peer = peer;
        this.sortDate = sortDate;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getSortDate() {
        return sortDate;
    }
}
