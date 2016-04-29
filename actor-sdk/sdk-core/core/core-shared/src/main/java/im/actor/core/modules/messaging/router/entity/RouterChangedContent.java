package im.actor.core.modules.messaging.router.entity;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterChangedContent implements AskMessage<Void>, RouterMessageOnlyActive {

    private Peer peer;
    private long rid;
    private AbsContent content;

    public RouterChangedContent(Peer peer, long rid, AbsContent content) {
        this.peer = peer;
        this.rid = rid;
        this.content = content;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }

    public AbsContent getContent() {
        return content;
    }
}
