/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public abstract class AbsLocalContent extends BserObject {

    private static final int CONTENT_DOC = 0;
    private static final int CONTENT_PHOTO = 1;
    private static final int CONTENT_VIDEO = 2;
    private static final int CONTENT_VOICE = 3;
    private static final int CONTENT_ANIMATION = 4;

    public static AbsLocalContent loadContainer(byte[] data) throws IOException {
        BserValues values = new BserValues(BserParser.deserialize(new DataInput(data)));
        int type = values.getInt(1);
        byte[] content = values.getBytes(2);
        if (type == CONTENT_DOC) {
            return new LocalDocument(content);
        } else if (type == CONTENT_PHOTO) {
            return new LocalPhoto(content);
        } else if (type == CONTENT_VIDEO) {
            return new LocalVideo(content);
        } else if (type == CONTENT_VOICE) {
            return new LocalVoice(content);
        } else if (type == CONTENT_ANIMATION) {
            return new LocalAnimation(content);
        } else {
            throw new IOException("Unknown type");
        }
    }

    public byte[] buildContainer() throws IOException {
        DataOutput res = new DataOutput();
        BserWriter writer = new BserWriter(res);
        if (this instanceof LocalPhoto) {
            writer.writeInt(1, CONTENT_PHOTO);
        } else if (this instanceof LocalVideo) {
            writer.writeInt(1, CONTENT_VIDEO);
        } else if (this instanceof LocalDocument) {
            writer.writeInt(1, CONTENT_DOC);
        } else if (this instanceof LocalVoice) {
            writer.writeInt(1, CONTENT_VOICE);
        } else if (this instanceof LocalAnimation) {
            writer.writeInt(1, CONTENT_ANIMATION);
        } else {
            throw new IOException("Unknown type");
        }
        writer.writeBytes(2, toByteArray());
        return res.toByteArray();
    }
}
