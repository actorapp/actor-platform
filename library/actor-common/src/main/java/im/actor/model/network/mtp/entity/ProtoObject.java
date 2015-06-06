/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;
import im.actor.model.log.Log;

public abstract class ProtoObject {

    protected ProtoObject(DataInput stream) throws IOException {
        readObject(stream);
    }

    protected ProtoObject() {

    }

    public abstract void writeObject(DataOutput bs) throws IOException;

    public abstract ProtoObject readObject(DataInput bs) throws IOException;

    public byte[] toByteArray() {
        DataOutput outputStream = new DataOutput();
        try {
            writeObject(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("ProtoObject", "Error: " + e.getMessage());
        }
        return outputStream.toByteArray();
    }
}