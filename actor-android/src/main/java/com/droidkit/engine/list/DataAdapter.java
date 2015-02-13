package com.droidkit.engine.list;

import com.droidkit.engine._internal.sqlite.BinarySerializator;

public interface DataAdapter<V> extends BinarySerializator<V> {

    public long getId(V value);

    public long getSortKey(V value);

    public byte[] serialize(V entity);

    public V deserialize(byte[] item);
}
