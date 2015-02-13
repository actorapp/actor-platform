package com.droidkit.engine.persistence;

import com.droidkit.engine.persistence.storage.PersistenceStorage;

import java.io.*;

/**
 * Created by ex3ndr on 26.09.14.
 */
public class SerializableMap<V extends Serializable> extends PersistenceMap<V> {
    public SerializableMap(PersistenceStorage storage) {
        super(storage);
        init();
    }

    @Override
    protected byte[] serialize(V value) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(value);
            objectOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    protected V deserialize(byte[] value) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(value);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (V) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
