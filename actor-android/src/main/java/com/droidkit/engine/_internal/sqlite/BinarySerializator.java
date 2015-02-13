package com.droidkit.engine._internal.sqlite;

public interface BinarySerializator<V> {

    byte[] serialize(V entity);

    V deserialize(byte[] item);
}
