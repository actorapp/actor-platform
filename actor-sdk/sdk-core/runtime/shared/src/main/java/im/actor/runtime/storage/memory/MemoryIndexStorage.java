package im.actor.runtime.storage.memory;

import java.util.List;

import im.actor.runtime.storage.IndexStorage;

public class MemoryIndexStorage implements IndexStorage {

    @Override
    public void put(long key, long value) {

    }

    @Override
    public Long get(long key) {
        return null;
    }

    @Override
    public List<Long> findBeforeValue(long value) {
        return null;
    }

    @Override
    public List<Long> removeBeforeValue(long value) {
        return null;
    }

    @Override
    public void remove(long key) {

    }

    @Override
    public void remove(List<Long> keys) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
