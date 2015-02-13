package im.actor.model.droidkit.bser;

import im.actor.model.droidkit.bser.util.SparseArray;
import im.actor.model.util.DataInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static im.actor.model.droidkit.bser.WireTypes.*;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class BserParser {
    public static SparseArray<Object> deserialize(DataInput is) throws IOException {
        SparseArray<Object> hashMap = new SparseArray<Object>();
        int currentTag;
        while ((currentTag = is.readByteSilent()) > 0) {
            int id = currentTag >> 3;
            int type = currentTag & 0x7;

            if (type == TYPE_VARINT) {
                put(id, is.readVarInt(), hashMap);
            } else if (type == TYPE_LENGTH_DELIMITED) {
                int size = (int) is.readVarInt();
                put(id, is.readBytes(size), hashMap);
            } else if (type == TYPE_64BIT) {
                put(id, is.readLong(), hashMap);
            } else if (type == TYPE_32BIT) {
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
}