package im.actor.core.modules.api.entity;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PersistentStorage extends BserObject {

    public static PersistentStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new PersistentStorage(), data);
    }

    private ArrayList<StoredRequest> requests = new ArrayList<StoredRequest>();
    private ArrayList<CursorStoredRequest> cursorRequests = new ArrayList<CursorStoredRequest>();

    public PersistentStorage() {

    }

    public ArrayList<StoredRequest> getRequests() {
        return requests;
    }

    public ArrayList<CursorStoredRequest> getCursorRequests() {
        return cursorRequests;
    }

    public CursorStoredRequest findCursorRequest(String name) {
        for (CursorStoredRequest r : cursorRequests) {
            if (name.equals(r.getName())) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void parse(BserValues values) throws IOException {

        requests.clear();
        for (byte[] r : values.getRepeatedBytes(1)) {
            try {
                requests.add(StoredRequest.fromBytes(r));
            } catch (IOException e) {
                // Ignoring unknown requests
                e.printStackTrace();
            }
        }

        cursorRequests.clear();
        for (byte[] r : values.getRepeatedBytes(2)) {
            try {
                cursorRequests.add(CursorStoredRequest.fromBytes(r));
            } catch (IOException e) {
                // Ignoring unknown requests
                e.printStackTrace();
            }
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {

        for (StoredRequest req : requests) {
            writer.writeObject(1, req);
        }

        for (CursorStoredRequest req : cursorRequests) {
            writer.writeObject(2, req);
        }
    }
}