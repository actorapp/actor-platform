/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

public class ListEngineRecord {

    private long key;
    private long order;
    private String query;
    private byte[] data;

    public ListEngineRecord(long key, long order, String query, byte[] data) {
        this.key = key;
        this.order = order;
        this.query = query;
        this.data = data;
    }

    public long getKey() {
        return key;
    }

    public long getOrder() {
        return order;
    }

    public String getQuery() {
        return query;
    }

    public byte[] getData() {
        return data;
    }
}
