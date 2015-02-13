package im.actor.model.entity;

import im.actor.model.mvvm.KeyValueItem;

/**
 * Created by ex3ndr on 11.02.15.
 */
public class PendingMessage implements KeyValueItem {
    private long rid;
    private long date;

    public PendingMessage(long rid, long date) {
        this.rid = rid;
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public long getRid() {
        return rid;
    }

    @Override
    public long getEngineId() {
        return rid;
    }
}
