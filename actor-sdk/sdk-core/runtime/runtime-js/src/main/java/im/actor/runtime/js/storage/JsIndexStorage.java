package im.actor.runtime.js.storage;

import com.google.gwt.storage.client.Storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.storage.IndexStorage;

import static im.actor.runtime.crypto.Base64Utils.fromBase64;
import static im.actor.runtime.crypto.Base64Utils.toBase64;

public class JsIndexStorage implements IndexStorage {

    private Storage storage;
    private String prefix;
    private Set<Item> items = new HashSet<Item>();

    public JsIndexStorage(String prefix, Storage storage) {
        this.storage = storage;
        this.prefix = prefix;

        try {
            String index = storage.getItem("index_" + prefix + "_index");
            if (index != null) {
                byte[] data = fromBase64(index);
                DataInput dataInput = new DataInput(data, 0, data.length);
                int count = dataInput.readInt();
                for (int i = 0; i < count; i++) {
                    long id = dataInput.readLong();
                    long value = dataInput.readLong();
                    items.add(new Item(id, value));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void put(long key, long value) {
        for (Item itm : items) {
            if (itm.getKey() == key) {
                items.remove(itm);
            }
        }
        items.add(new Item(key, value));
        save();
    }

    @Override
    public Long get(long key) {
        for (Item itm : items) {
            if (itm.getKey() == key) {
                return itm.getValue();
            }
        }
        return null;
    }

    @Override
    public List<Long> findBeforeValue(long value) {
        ArrayList<Long> keys = new ArrayList<Long>();
        for (Item itm : items) {
            if (itm.getValue() <= value) {
                keys.add(itm.getKey());
            }
        }
        return keys;
    }

    @Override
    public List<Long> removeBeforeValue(long value) {
        List<Long> res = findBeforeValue(value);
        remove(res);
        return res;
    }

    @Override
    public void remove(long key) {
        for (Item itm : items) {
            if (itm.getKey() == key) {
                items.remove(itm);
                save();
                break;
            }
        }
    }

    @Override
    public void remove(List<Long> keys) {
        ArrayList<Item> toRemove = new ArrayList<Item>();
        for (Item itm : items) {
            for (Long k : keys) {
                if (k.equals(itm.getKey())) {
                    toRemove.add(itm);
                    break;
                }
            }
        }
        if (toRemove.size() > 0) {
            items.removeAll(toRemove);
            save();
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public void clear() {
        items.clear();
        save();
    }

    private void save() {
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(items.size());
        for (Item l : items) {
            dataOutput.writeLong(l.getKey());
            dataOutput.writeLong(l.getValue());
        }
        storage.setItem("index_" + prefix + "_index", toBase64(dataOutput.toByteArray()));
    }

    private class Item {
        private long key;
        private long value;

        public Item(long key, long value) {
            this.key = key;
            this.value = value;
        }

        public long getKey() {
            return key;
        }

        public long getValue() {
            return value;
        }
    }
}
