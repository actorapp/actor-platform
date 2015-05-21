/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.bser;

import java.io.IOException;

import im.actor.model.droidkit.bser.util.SparseArray;

public abstract class BserObject {

    private SparseArray<Object> unmappedObjects;

    public BserObject() {

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
