package im.actor.runtime.storage;

import java.util.List;

public class IndexEngine implements IndexStorage {

    private IndexStorage storage;

    public IndexEngine(IndexStorage storage) {
        this.storage = storage;
    }

    @Override
    public void put(long key, long value) {
        storage.put(key, value);
    }

    @Override
    public Long get(long key) {
        return storage.get(key);
    }

    @Override
    public List<Long> findBeforeValue(long value) {
        return storage.findBeforeValue(value);
    }

    @Override
    public List<Long> removeBeforeValue(long value) {
        return storage.removeBeforeValue(value);
    }

    @Override
    public void remove(long key) {
        storage.remove(key);
    }

    @Override
    public void remove(List<Long> keys) {
        storage.remove(keys);
    }

    @Override
    public int getCount() {
        return storage.getCount();
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
