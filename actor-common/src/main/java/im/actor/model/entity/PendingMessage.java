package im.actor.model.entity;

import im.actor.model.entity.content.AbsContent;
import im.actor.model.mvvm.KeyValueItem;

/**
 * Created by ex3ndr on 11.02.15.
 */
public class PendingMessage implements KeyValueItem {
    private Peer peer;
    private long rid;
    private AbsContent content;

    public PendingMessage(Peer peer, long rid, AbsContent content) {
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

    @Override
    public long getEngineId() {
        return rid;
    }
}
