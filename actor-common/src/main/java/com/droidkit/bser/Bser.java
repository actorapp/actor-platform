package com.droidkit.bser;

import im.actor.model.util.DataInput;

import java.io.IOException;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class Bser {
    public static <T extends BserObject> T parse(T res, DataInput inputStream) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(inputStream));
        res.parse(reader);
        return res;
    }

    public static <T extends BserObject> T parse(T res, byte[] data) throws IOException {
        return parse(res, new DataInput(data, 0, data.length));
    }
}
