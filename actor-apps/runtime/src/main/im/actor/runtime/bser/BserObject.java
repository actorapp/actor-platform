/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import java.io.IOException;

import im.actor.runtime.collections.SparseArray;

public abstract class BserObject {

    private SparseArray<Object> unmappedObjects;

    public BserObject() {

    }

    protected void load(byte[] data) throws IOException {
        BserValues values = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
        parse(values);
    }

    public SparseArray<Object> getUnmappedObjects() {
        return unmappedObjects;
    }

    public void setUnmappedObjects(SparseArray<Object> unmappedObjects) {
        this.unmappedObjects = unmappedObjects;
    }

    public byte[] toByteArray() {
        DataOutput outputStream = new DataOutput();
        BserWriter writer = new BserWriter(outputStream);
        try {
            serialize(writer);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IO exception");
        }
        return outputStream.toByteArray();
    }

    public abstract void parse(BserValues values) throws IOException;

    public abstract void serialize(BserWriter writer) throws IOException;
}
