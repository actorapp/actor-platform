package im.actor.model.droidkit.engine;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class KeyValueRecord {
    private long id;
    private byte[] data;

    public KeyValueRecord(long id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}