/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

public class KeyValueRecord {
    private long id;
    private byte[] data;

    public KeyValueRecord(long id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}