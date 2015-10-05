/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.storage;

import com.google.gwt.storage.client.Storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.storage.ListEngineRecord;
import im.actor.runtime.storage.ListStorage;

import static im.actor.runtime.crypto.Base64Utils.fromBase64;
import static im.actor.runtime.crypto.Base64Utils.toBase64;

public class JsListStorage implements ListStorage {

    private final Storage storage;
    private final String prefix;
    private final ArrayList<Index> index = new ArrayList<Index>();
    private final Comparator<Index> comparator = new Comparator<Index>() {
        @Override
        public int compare(Index o1, Index o2) {
            return -compare(o1.getSortKey(), o2.getSortKey());
        }

        int compare(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    public JsListStorage(String prefix, Storage storage) {
        this.storage = storage;
        this.prefix = prefix;

        String indexData = storage.getItem("list_" + prefix + "_index");
        if (indexData != null) {
            try {
                byte[] data = fromBase64(indexData);
                DataInput dataInput = new DataInput(data, 0, data.length);
                int count = dataInput.readInt();
                for (int i = 0; i < count; i++) {
                    long id = dataInput.readLong();
                    long order = dataInput.readLong();
                    index.add(new Index(id, order));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updateIndex();
    }

    @Override
    public void updateOrAdd(ListEngineRecord record) {
        // Update Index
        for (Index i : index) {
            if (i.getId() == record.getKey()) {
                index.remove(i);
                break;
            }
        }
        index.add(new Index(record.getKey(), record.getOrder()));
        updateIndex();

        // Save record
        storage.setItem(getId(record.getKey()), toBase64(record.getData()));
    }

    @Override
    public void updateOrAdd(List<ListEngineRecord> items) {
        // Update Index
        for (ListEngineRecord record : items) {
            for (Index i : index) {
                if (i.getId() == record.getKey()) {
                    index.remove(i);
                    break;
                }
            }
            index.add(new Index(record.getKey(), record.getOrder()));
        }
        updateIndex();

        // Save records
        for (ListEngineRecord record : items) {
            storage.setItem(getId(record.getKey()), toBase64(record.getData()));
        }
    }

    @Override
    public void delete(long key) {
        for (Index i : index) {
            if (i.getId() == key) {
                index.remove(i);
                storage.removeItem(getId(key));
                updateIndex();
                break;
            }
        }
    }

    @Override
    public void delete(long[] keys) {
        for (long key : keys) {
            for (Index i : index) {
                if (i.getId() == key) {
                    index.remove(i);
                    storage.removeItem(getId(key));
                    updateIndex();
                    break;
                }
            }
        }
    }

    @Override
    public void clear() {
        for (Index i : index) {
            storage.removeItem(getId(i.getId()));
        }
        index.clear();
        updateIndex();
    }

    @Override
    public ListEngineRecord loadItem(long key) {
        Index indexValue = null;
        for (Index i : index) {
            if (i.getId() == key) {
                indexValue = i;
                break;
            }
        }

        if (indexValue == null) {
            return null;
        }

        String item = storage.getItem(getId(key));
        if (item != null) {
            byte[] res = fromBase64(item);
            return new ListEngineRecord(key, indexValue.getSortKey(), null, res);
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public int getCount() {
        return index.size();
    }

    public Long getHeadId() {
        if (index.size() > 0) {
            return index.get(0).getId();
        } else {
            return null;
        }
    }

    public long[] getOrderedIds() {
        long[] res = new long[index.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = index.get(i).getId();
        }
        return res;
    }

    private String getId(long id) {
        return "list_" + prefix + "_" + id;
    }

    private void updateIndex() {
        Collections.sort(index, comparator);
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(index.size());

        for (Index i : index) {
            dataOutput.writeLong(i.getId());
            dataOutput.writeLong(i.getSortKey());
        }

        storage.setItem("list_" + prefix + "_index", toBase64(dataOutput.toByteArray()));
    }

    private class Index {
        private long id;
        private long sortKey;

        private Index(long id, long sortKey) {
            this.id = id;
            this.sortKey = sortKey;
        }

        public long getId() {
            return id;
        }

        public long getSortKey() {
            return sortKey;
        }
    }
}
