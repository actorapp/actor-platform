package im.actor.core.modules.api.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class CursorStoredRequest extends BserObject {

    public static CursorStoredRequest fromBytes(byte[] data) throws IOException {
        return Bser.parse(new CursorStoredRequest(), data);
    }

    private String name;
    private long currentKey;
    private StoredRequest request;

    public CursorStoredRequest(String name, long currentKey, StoredRequest request) {
        this.name = name;
        this.currentKey = currentKey;
        this.request = request;
    }

    private CursorStoredRequest() {

    }

    public String getName() {
        return name;
    }

    public long getCurrentKey() {
        return currentKey;
    }

    public StoredRequest getRequest() {
        return request;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        name = values.getString(1);
        currentKey = values.getLong(2);

        byte[] rawBytes = values.getBytes(3);
        if (rawBytes != null) {
            request = StoredRequest.fromBytes(rawBytes);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, name);
        writer.writeLong(2, currentKey);

        if (request != null) {
            writer.writeObject(3, request);
        }
    }
}
