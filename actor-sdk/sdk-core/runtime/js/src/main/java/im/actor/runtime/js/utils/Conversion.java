/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.utils;

import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

/**
 * Created by ex3ndr on 09.05.15.
 */
public final class Conversion {

    public static byte[] convertBytes(ArrayBuffer buffer) {
        Uint8Array array = TypedArrays.createUint8Array(buffer);
        byte[] res = new byte[array.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) (array.get(i));
        }
        return res;
    }

    public static ArrayBuffer convertBytes(byte[] data) {
        Uint8Array push = TypedArrays.createUint8Array(data.length);
        for (int i = 0; i < data.length; i++) {
            push.set(i, data[i]);
        }
        return push.buffer();
    }

    private Conversion() {

    }
}
