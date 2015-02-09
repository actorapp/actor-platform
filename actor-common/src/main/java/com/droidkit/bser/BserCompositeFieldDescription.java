package com.droidkit.bser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ex3ndr on 18.10.14.
 */
public abstract class BserCompositeFieldDescription<T extends BserComposite> {
    private HashMap<Integer, Class<? extends T>> types = new HashMap<Integer, Class<? extends T>>();

    protected BserCompositeFieldDescription() {
        init();
    }

    protected abstract void init();

    public void registerClass(int fieldId, Class<? extends T> type) {
        types.put(fieldId, type);
    }

    public T readObject(BserValues values) throws IOException {
        for (Map.Entry<Integer, Class<? extends T>> t : types.entrySet()) {
            T res = values.optObj(t.getKey(), t.getValue());
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public void writeObject(T obj, BserWriter writer) throws IOException {
        for (Map.Entry<Integer, Class<? extends T>> t : types.entrySet()) {
            if (t.getValue().equals(obj.getClass())) {
                writer.writeObject(t.getKey(), obj);
                return;
            }
        }
        throw new IllegalArgumentException("Unable to find field id for object");
    }
}
