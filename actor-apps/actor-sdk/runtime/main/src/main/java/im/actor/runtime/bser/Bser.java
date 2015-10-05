/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import java.io.IOException;

public final class Bser {

    public static <T extends BserObject> T parse(BserCreator<T> creator, byte[] data) throws IOException {
        return parse(creator.createInstance(), new DataInput(data, 0, data.length));
    }

    public static <T extends BserObject> T parse(T res, byte[] data) throws IOException {
        return parse(res, new DataInput(data, 0, data.length));
    }

    public static <T extends BserObject> T parse(T res, DataInput inputStream) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(inputStream));
        res.parse(reader);
        return res;
    }

    private Bser() {

    }
}
