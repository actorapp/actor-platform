/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content.internal;

import java.io.IOException;

import im.actor.model.api.Message;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

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
            return new ContentRemoteContainer(Message.fromBytes(content));
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
