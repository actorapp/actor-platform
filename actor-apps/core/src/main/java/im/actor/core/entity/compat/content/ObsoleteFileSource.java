/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.DataInput;

public abstract class ObsoleteFileSource extends BserObject {
    public static ObsoleteFileSource fromBytes(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
        int type = reader.getInt(1);
        switch (type) {
            case 1:
                return new ObsoleteLocalFileSource(reader);
            case 2:
                return new ObsoleteRemoteFileSource(reader);
            default:
                throw new IOException("Invalid source type");
        }
    }
}
