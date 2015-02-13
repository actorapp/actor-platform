package com.droidkit.engine.keyvalue;

import com.droidkit.engine._internal.sqlite.BinarySerializator;

public interface DataAdapter<V> extends BinarySerializator<V> {

    long getId(V value);

    byte[] serialize(V entity);

    V deserialize(byte[] item);
}
