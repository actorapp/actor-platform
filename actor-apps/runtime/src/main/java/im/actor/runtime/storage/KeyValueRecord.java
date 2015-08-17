/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

public class KeyValueRecord {
    private long id;
    @NotNull
    private byte[] data;

    @ObjectiveCName("initWithKey:withData:")
    public KeyValueRecord(long id, @NotNull byte[] data) {
        this.id = id;
        this.data = data;
    }

    @ObjectiveCName("getId")
    public long getId() {
        return id;
    }

    @NotNull
    @ObjectiveCName("getData")
    public byte[] getData() {
        return data;
    }
}