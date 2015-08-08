/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.content.internal.LocalFastThumb;

public class ObsoleteFastThumb extends BserObject {

    private int w;
    private int h;
    private byte[] image;

    public ObsoleteFastThumb(byte[] data) throws IOException {
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

    public im.actor.core.api.FastThumb toApiFastThumb() {
        return new im.actor.core.api.FastThumb(w, h, image);
    }

    public LocalFastThumb toFastThumb() {
        return new LocalFastThumb(w, h, image);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        w = values.getInt(1);
        h = values.getInt(2);
        image = values.getBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
