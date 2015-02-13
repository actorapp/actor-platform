package com.droidkit.engine.persistence;

import com.droidkit.bser.Bser;
import com.droidkit.bser.BserObject;
import com.droidkit.engine.persistence.storage.PersistenceStorage;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class BserMap<V extends BserObject> extends PersistenceMap<V> {

    private Class<V> vClass;

    public BserMap(PersistenceStorage storage, Class<V> vClass) {
        super(storage);
        this.vClass = vClass;
        init();
    }

    public BserMap(PersistenceStorage storage, Map<Long, V> backedMap, Class<V> vClass) {
        super(storage, backedMap);
        this.vClass = vClass;
        init();
    }

    @Override
    protected byte[] serialize(V value) {
        return value.toByteArray();
    }

    @Override
    protected V deserialize(byte[] value) {
        try {
            return Bser.parse(vClass, value);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
