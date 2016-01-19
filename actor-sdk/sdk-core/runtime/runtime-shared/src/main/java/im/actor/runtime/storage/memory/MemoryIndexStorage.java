package im.actor.runtime.storage.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.runtime.storage.IndexStorage;

public class MemoryIndexStorage implements IndexStorage {

    private HashMap<Long, Long> index = new HashMap<Long, Long>();

    @Override
    public void put(long key, long value) {
        index.put(key, value);
    }

    @Override
    public Long get(long key) {
        return index.get(key);
    }

    @Override
    public List<Long> findBeforeValue(long value) {
        ArrayList<Long> ids = new ArrayList<Long>();
        for (long k : index.keySet()) {
            if (index.get(k) < value) {
                ids.add(k);
            }
        }
        return ids;
    }

    @Override
    public List<Long> removeBeforeValue(long value) {
        ArrayList<Long> ids = new ArrayList<Long>();
        for (long k : index.keySet()) {
            if (index.get(k) >= value) {
                ids.add(k);
            }
        }
        return ids;
    }

    @Override
    public void remove(long key) {
        index.remove(key);
    }

    @Override
    public void remove(List<Long> keys) {
        for (long l : keys) {
            remove(l);
        }
    }

    @Override
    public int getCount() {
        return index.size();
    }

    @Override
    public void clear() {
        index.clear();
    }
}
