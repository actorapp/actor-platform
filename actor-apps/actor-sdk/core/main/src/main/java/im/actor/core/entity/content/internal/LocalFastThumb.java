/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.content.FastThumb;

public class LocalFastThumb extends BserObject {

    private int w;
    private int h;
    private byte[] image;

    public LocalFastThumb(FastThumb fastThumb) {
        w = fastThumb.getW();
        h = fastThumb.getH();
        image = fastThumb.getImage();
    }

    public LocalFastThumb(int w, int h, byte[] image) {
        this.w = w;
        this.h = h;
        this.image = image;
    }

    public LocalFastThumb(byte[] data) throws IOException {
        load(data);
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public byte[] getImage() {
        return image;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        w = values.getInt(1);
        h = values.getInt(2);
        image = values.getBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, w);
        writer.writeInt(2, h);
        writer.writeBytes(3, image);
    }
}
