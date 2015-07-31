package im.actor.model.mem.storage;

import java.util.List;

import im.actor.model.droidkit.engine.IndexStorage;

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
