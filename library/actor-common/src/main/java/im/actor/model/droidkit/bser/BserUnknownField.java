/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.bser;

class BserUnknownField {
    private int id;
    private int type;
    private byte[] binary;

    public BserUnknownField(int id, int type, byte[] binary) {
        this.id = id;
        this.type = type;
        this.binary = binary;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public byte[] getBinary() {
        return binary;
    }
}
