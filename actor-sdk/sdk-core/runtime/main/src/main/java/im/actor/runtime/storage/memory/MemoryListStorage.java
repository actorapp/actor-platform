package im.actor.runtime.storage.memory;

import java.util.HashMap;
import java.util.List;

import im.actor.runtime.storage.ListEngineRecord;
import im.actor.runtime.storage.ListStorage;

/**
 * Created by ex3ndr on 28.07.15.
 */
public class MemoryListStorage implements ListStorage {

    private HashMap<Long, Record> records = new HashMap<Long, Record>();

    @Override
    public void updateOrAdd(ListEngineRecord valueContainer) {
        records.put(valueContainer.getKey(), new Record(valueContainer.getKey(),
                valueContainer.getOrder(), valueContainer.getData(), valueContainer.getQuery()));
    }

    @Override
    public void updateOrAdd(List<ListEngineRecord> items) {
        for (ListEngineRecord r : items) {
            updateOrAdd(r);
        }
    }

    @Override
    public void delete(long key) {
        records.remove(key);
    }

    @Override
    public void delete(long[] keys) {
        for (long l : keys) {
            delete(l);
        }
    }

    @Override
    public void clear() {
        records.clear();
    }

    @Override
    public ListEngineRecord loadItem(long key) {
        Record record = records.get(key);
        if (record != null) {
            return new ListEngineRecord(record.getId(), record.getOrder(), record.getQuery(),
                    record.getData());
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    public Long getTopKey() {
        if (records.size() == 0) {
            return null;
        }
        long res = 0;
        long value = Long.MIN_VALUE;
        for (Long key : records.keySet()) {
            Record record = records.get(key);
            if (record.getOrder() >= value) {
                value = record.getOrder();
                res = record.getId();
            }
        }
        return res;
    }

    private class Record {

        private long id;
        private long order;
        private byte[] data;
        private String query;

        public Record(long id, long order, byte[] data, String query) {
            this.id = id;
            this.order = order;
            this.data = data;
            this.query = query;
        }

        public long getId() {
            return id;
        }

        public long getOrder() {
            return order;
        }

        public byte[] getData() {
            return data;
        }

        public String getQuery() {
            return query;
        }
    }
}
