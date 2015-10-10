package im.actor.runtime.storage.memory;

import java.util.HashMap;
import java.util.List;

import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;

public class MemoryKeyValueStorage implements KeyValueStorage {

    private HashMap<Long, byte[]> records = new HashMap<Long, byte[]>();

    @Override
    public void addOrUpdateItem(long key, byte[] data) {
        records.put(key, data);
    }

    @Override
    public void addOrUpdateItems(List<KeyValueRecord> values) {
        for (KeyValueRecord r : values) {
            records.put(r.getId(), r.getData());
        }
    }

    @Override
    public void removeItem(long key) {
        records.remove(key);
    }

    @Override
    public void removeItems(long[] keys) {
        for (long k : keys) {
            records.remove(k);
        }
    }

    @Override
    public void clear() {
        records.clear();
    }

    @Override
    public byte[] getValue(long key) {
        return records.get(key);
    }
}
