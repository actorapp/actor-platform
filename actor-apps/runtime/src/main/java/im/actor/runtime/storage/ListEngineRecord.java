/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListEngineRecord {

    private long key;
    private long order;
    @Nullable
    private String query;
    @NotNull
    private byte[] data;

    @ObjectiveCName("initWithKey:withOrder:withQuery:withData:")
    public ListEngineRecord(long key, long order, @Nullable String query, @NotNull byte[] data) {
        this.key = key;
        this.order = order;
        this.query = query;
        this.data = data;
    }

    @ObjectiveCName("getKey")
    public long getKey() {
        return key;
    }

    @ObjectiveCName("getOrder")
    public long getOrder() {
        return order;
    }

    @ObjectiveCName("getQuery")
    @Nullable
    public String getQuery() {
        return query;
    }

    @ObjectiveCName("getData")
    @NotNull
    public byte[] getData() {
        return data;
    }
}
