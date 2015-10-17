/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiFastThumb;
import im.actor.core.entity.content.internal.LocalFastThumb;

public class FastThumb {

    private int w;
    private int h;
    private byte[] image;

    public FastThumb(LocalFastThumb localFastThumb) {
        w = localFastThumb.getW();
        h = localFastThumb.getH();
        image = localFastThumb.getImage();
    }

    public FastThumb(ApiFastThumb fastThumb) {
        w = fastThumb.getW();
        h = fastThumb.getH();
        image = fastThumb.getThumb();
    }

    public FastThumb(int w, int h, byte[] image) {
        this.w = w;
        this.h = h;
        this.image = image;
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
}
