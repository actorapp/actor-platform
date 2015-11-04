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
            return -compare(o1.sortKey, o2.sortKey);
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
                // Just in case...
                Collections.sort(index, comparator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // updateIndex();
    }

    @Override
    public void updateOrAdd(ListEngineRecord record) {
        // Update Index
        addToIndex(record.getKey(), record.getOrder());
//        for (Index i : index) {
//            if (i.id == record.getKey()) {
//                index.remove(i);
//                break;
//            }
//        }
//        index.add(new Index(record.getKey(), record.getOrder()));
        saveIndex();

        // Save record
        storage.setItem(getId(record.getKey()), toBase64(record.getData()));
    }

    @Override
    public void updateOrAdd(List<ListEngineRecord> items) {
        // Update Index
        for (ListEngineRecord record : items) {
//            for (Index i : index) {
//                if (i.id == record.getKey()) {
//                    index.remove(i);
//                    break;
//                }
//            }
//            index.add(new Index(record.getKey(), record.getOrder()));
            addToIndex(record.getKey(), record.getOrder());
        }
        saveIndex();

        // Save records
        for (ListEngineRecord record : items) {
            storage.setItem(getId(record.getKey()), toBase64(record.getData()));
        }
    }

    @Override
    public void delete(long key) {
        for (Index i : index) {
            if (i.id == key) {
                index.remove(i);
                storage.removeItem(getId(key));
                saveIndex();
                break;
            }
        }
    }

    @Override
    public void delete(long[] keys) {
        for (long key : keys) {
            for (Index i : index) {
                if (i.id == key) {
                    index.remove(i);
                    storage.removeItem(getId(key));
                    saveIndex();
                    break;
                }
            }
        }
    }

    @Override
    public void clear() {
        for (Index i : index) {
            storage.removeItem(getId(i.id));
        }
        index.clear();
        saveIndex();
    }

    @Override
    public ListEngineRecord loadItem(long key) {
        Index indexValue = null;
        for (Index i : index) {
            if (i.id == key) {
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
            return new ListEngineRecord(key, indexValue.sortKey, null, res);
        }
        return null;
    }

    @Override
    public List<ListEngineRecord> loadAllItems() {
        // TODO: Implement
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
            return index.get(0).id;
        } else {
            return null;
        }
    }

    public long[] getOrderedIds() {
        long[] res = new long[index.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = index.get(i).id;
        }
        return res;
    }

    private String getId(long id) {
        return "list_" + prefix + "_" + id;
    }

    private void addToIndex(long id, long sortKey) {
        for (int i = 0; i < index.size(); i++) {
            Index ind = index.get(i);
            if (ind.id == id) {
                index.remove(i);
                break;
            }
        }

        boolean found = false;
        for (int i = 0; i < index.size(); i++) {
            Index ind = index.get(i);
            if (ind.sortKey < sortKey) {
                found = true;
                index.add(i, new Index(id, sortKey));
            }
        }
        if (!found) {
            index.add(new Index(id, sortKey));
        }
//        for (Index i : index) {
//            if (i.id == id) {
//                //index.remove()
//            }
//        }
        //        for (Index i : index) {
//            if (i.id == record.getKey()) {
//                index.remove(i);
//                break;
//            }
//        }
//        index.add(new Index(record.getKey(), record.getOrder()));
    }

    private void updateIndex() {
        Collections.sort(index, comparator);
        saveIndex();
    }

    private void saveIndex() {
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(index.size());

        for (Index i : index) {
            dataOutput.writeLong(i.id);
            dataOutput.writeLong(i.sortKey);
        }

        storage.setItem("list_" + prefix + "_index", toBase64(dataOutput.toByteArray()));
    }

    private class Index {
        public final long id;
        public final long sortKey;

        private Index(long id, long sortKey) {
            this.id = id;
            this.sortKey = sortKey;
        }
    }
}
