/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import java.io.IOException;

import im.actor.core.api.ApiMessage;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public abstract class AbsContentContainer {
    private static final int TYPE_LOCAL = 0;
    private static final int TYPE_REMOTE = 1;

    public static AbsContentContainer loadContainer(byte[] data) throws IOException {
        BserValues values = new BserValues(BserParser.deserialize(new DataInput(data)));
        int type = values.getInt(1);
        byte[] content = values.getBytes(2);
        if (type == TYPE_LOCAL) {
            return new ContentLocalContainer(AbsLocalContent.loadContainer(content));
        } else if (type == TYPE_REMOTE) {
            return new ContentRemoteContainer(ApiMessage.fromBytes(content));
        } else {
            throw new IOException("Unknown type");
        }
    }

    public byte[] buildContainer() throws IOException {
        DataOutput res = new DataOutput();
        BserWriter writer = new BserWriter(res);
        if (this instanceof ContentLocalContainer) {
            writer.writeInt(1, TYPE_LOCAL);
            writer.writeBytes(2, ((ContentLocalContainer) this).getContent().buildContainer());
        } else if (this instanceof ContentRemoteContainer) {
            writer.writeInt(1, TYPE_REMOTE);
            writer.writeBytes(2, ((ContentRemoteContainer) this).getMessage().buildContainer());
        } else {
            throw new IOException("Unknown type");
        }

        return res.toByteArray();
    }
}
