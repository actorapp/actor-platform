package im.actor.model.storage.temp;

import java.util.HashMap;
import java.util.List;

import im.actor.model.storage.KeyValueRecord;
import im.actor.model.storage.KeyValueStorage;

/**
 * Created by ex3ndr on 23.02.15.
 */
public class TempKeyValueStorage implements KeyValueStorage {

    private HashMap<Long, byte[]> items = new HashMap<Long, byte[]>();

    @Override
    public synchronized void addOrUpdateItem(long id, byte[] data) {
        items.put(id, data);
    }

    @Override
    public synchronized void addOrUpdateItems(List<KeyValueRecord> values) {
        for (KeyValueRecord r : values) {
            items.put(r.getId(), r.getData());
        }
    }

    @Override
    public synchronized void removeItem(long id) {
        items.remove(id);
    }

    @Override
    public synchronized void removeItems(long[] ids) {
        for (long l : ids) {
            items.remove(l);
        }
    }

    @Override
    public synchronized void clear() {
        items.clear();
    }

    @Override
    public synchronized byte[] getValue(long id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            return null;
        }
    }
}
