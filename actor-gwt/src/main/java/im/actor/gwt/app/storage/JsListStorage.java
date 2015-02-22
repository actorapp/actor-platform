package im.actor.gwt.app.storage;

import com.google.gwt.storage.client.Storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import im.actor.gwt.app.base64.Base64Utils;
import im.actor.model.util.DataInput;
import im.actor.model.util.DataOutput;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsListStorage {

    private Storage storage;
    private String prefix;

    private ArrayList<Index> indexes = new ArrayList<Index>();

    public JsListStorage(String prefix, Storage storage) {
        this.storage = storage;
        this.prefix = prefix;
        String countRef = storage.getItem(prefix + "_count");
        if (countRef != null) {
            int count = Integer.parseInt(countRef);
            byte[] data = Base64Utils.fromBase64(storage.getItem(prefix + "_index"));
            DataInput dataInput = new DataInput(data, 0, data.length);
            for (int i = 0; i < count; i++) {
                try {
                    indexes.add(new Index(dataInput.readLong(), dataInput.readLong()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void updateIndex() {
        Collections.sort(indexes, new Comparator<Index>() {
            @Override
            public int compare(Index o1, Index o2) {
                return -compare(o1.getSortKey(), o2.getSortKey());
            }

            int compare(long x, long y) {
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        });
        storage.setItem(prefix + "_count", "" + indexes.size());
        DataOutput dataOutput = new DataOutput();
        for (Index i : indexes) {
            dataOutput.writeLong(i.getId());
            dataOutput.writeLong(i.getSortKey());
        }
        storage.setItem(prefix + "_index", Base64Utils.toBase64(dataOutput.toByteArray()));
    }

    public void addOrUpdateItem(long id, long sortKey, byte[] data) {
        boolean isUpdatedIndex = false;
        for (Index i : indexes) {
            if (i.getId() == id) {
                indexes.remove(i);
                isUpdatedIndex = true;
                break;
            }
        }

        indexes.add(new Index(id, sortKey));

        storage.setItem(prefix + "_i_" + id, Base64Utils.toBase64(data));

        if (isUpdatedIndex) {
            updateIndex();
        }
    }

    public void remove(long id) {
        boolean isRemoved = false;
        for (Index i : indexes) {
            if (i.getId() == id) {
                indexes.remove(i);
                isRemoved = true;
                break;
            }
        }
        if (isRemoved) {
            updateIndex();
        }
    }

    public byte[] getItem(long id) {
        String item = storage.getItem(prefix + "_i_" + id);
        if (item != null) {
            return Base64Utils.fromBase64(item);
        } else {
            return null;
        }
    }

    public int getCount() {
        return indexes.size();
    }

    public long[] getOrderedIds() {
        long[] res = new long[indexes.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = indexes.get(i).getId();
        }
        return res;
    }

    public void clear() {
        indexes.clear();
        updateIndex();
        // TODO: Remove actual data
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