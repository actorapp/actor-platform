/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class FastThumb extends BserObject {

    public static FastThumb fromBytes(byte[] data) throws IOException {
        return Bser.parse(new FastThumb(), data);
    }

    private int w;
    private int h;
    private byte[] image;

    public FastThumb(int w, int h, byte[] image) {
        this.w = w;
        this.h = h;
        this.image = image;
    }

    private FastThumb() {

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
