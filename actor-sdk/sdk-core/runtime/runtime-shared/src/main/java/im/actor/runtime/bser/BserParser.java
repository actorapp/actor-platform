/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.collections.SparseArray;

public final class BserParser {
    public static SparseArray<Object> deserialize(DataInput is) throws IOException {
        SparseArray<Object> hashMap = new SparseArray<Object>();
        while (!is.isEOF()) {
            long currentTag = is.readVarInt();

            int id = (int) (currentTag >> 3);
            int type = (int) (currentTag & 0x7);

            if (type == WireTypes.TYPE_VARINT) {
                put(id, is.readVarInt(), hashMap);
            } else if (type == WireTypes.TYPE_LENGTH_DELIMITED) {
                int size = (int) is.readVarInt();
                put(id, is.readBytes(size), hashMap);
            } else if (type == WireTypes.TYPE_64BIT) {
                put(id, is.readLong(), hashMap);
            } else if (type == WireTypes.TYPE_32BIT) {
                put(id, is.readUInt(), hashMap);
            } else {
                throw new IOException("Unknown Wire Type #" + type);
            }
        }
        return hashMap;
    }

    private static void put(int id, Object res, SparseArray<Object> hashMap) {
        if (hashMap.get(id) != null) {
            if (hashMap.get(id) instanceof List) {
                ((List) hashMap.get(id)).add(res);
            } else {
                ArrayList<Object> list = new ArrayList<Object>();
                list.add(hashMap.get(id));
                list.add(res);
                hashMap.put(id, list);
            }
        } else {
            hashMap.put(id, res);
        }
    }

    private BserParser() {

    }
}