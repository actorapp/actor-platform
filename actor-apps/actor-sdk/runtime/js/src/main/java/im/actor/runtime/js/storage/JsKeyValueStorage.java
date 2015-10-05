/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.storage;

import com.google.gwt.storage.client.Storage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;

import static im.actor.runtime.crypto.Base64Utils.fromBase64;
import static im.actor.runtime.crypto.Base64Utils.toBase64;

public class JsKeyValueStorage implements KeyValueStorage {

    private Storage storage;
    private String prefix;
    private Set<Long> items = new HashSet<Long>();

    public JsKeyValueStorage(String prefix, Storage storage) {
        this.storage = storage;
        this.prefix = prefix;

        try {
            String index = storage.getItem("kv_" + prefix + "_index");
            if (index != null) {
                byte[] data = fromBase64(index);
                DataInput dataInput = new DataInput(data, 0, data.length);
                int count = dataInput.readInt();
                for (int i = 0; i < count; i++) {
                    items.add(dataInput.readLong());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addOrUpdateItem(long id, byte[] data) {
        storage.setItem(getId(id), toBase64(data));

        if (!items.contains(id)) {
            items.add(id);
            saveIndex();
        }
    }

    @Override
    public void addOrUpdateItems(List<KeyValueRecord> values) {
        boolean isAdded = false;
        for (KeyValueRecord record : values) {
            long id = record.getId();
            storage.setItem(getId(id), toBase64(record.getData()));

            if (!items.contains(id)) {
                items.add(id);
                isAdded = true;
            }
        }
        if (isAdded) {
            saveIndex();
        }
    }

    @Override
    public void removeItem(long id) {
        if (!items.contains(id)) {
            return;
        }
        storage.removeItem(getId(id));
        items.remove(id);
        saveIndex();
    }

    @Override
    public void removeItems(long[] ids) {
        boolean isRemoved = false;
        for (long id : ids) {
            if (items.contains(id)) {
                storage.removeItem(getId(id));
                items.remove(id);
                isRemoved = true;
            }
        }
        if (isRemoved) {
            saveIndex();
        }
    }

    @Override
    public void clear() {
        for (long id : items) {
            storage.removeItem(getId(id));
        }
        storage.removeItem("kv_" + prefix + "_index");
        items.clear();
    }

    @Override
    public byte[] getValue(long id) {
        String res = storage.getItem(getId(id));
        if (res == null) {
            return null;
        } else {
            return fromBase64(res);
        }
    }

    private void saveIndex() {
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(items.size());
        for (long l : items) {
            dataOutput.writeLong(l);
        }
        storage.setItem("kv_" + prefix + "_index", toBase64(dataOutput.toByteArray()));
    }

    private String getId(long id) {
        return "kv_" + prefix + "_" + id;
    }
}
