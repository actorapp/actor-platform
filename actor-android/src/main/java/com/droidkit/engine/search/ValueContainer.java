package com.droidkit.engine.search;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class ValueContainer {
    private long id;
    private long order;
    private String query;
    private byte[] data;

    public ValueContainer(long id, long order, String query, byte[] data) {
        this.id = id;
        this.order = order;
        this.query = query;
        this.data = data;
    }

    public long getId() {
        return id;
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
