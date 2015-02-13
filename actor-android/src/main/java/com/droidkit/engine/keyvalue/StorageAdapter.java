package com.droidkit.engine.keyvalue;

import java.util.ArrayList;

public interface StorageAdapter<V> {

    void insertSingle(V item);

    void insertOrReplaceSingle(V item);

    void deleteSingle(long id);

    void insertBatch(ArrayList<V> items);

    void insertOrReplaceBatch(ArrayList<V> items);

    void deleteBatch(long[] ids);

    void deleteAll();

    ArrayList<V> loadAll();

    V getById(long id);

    //todo ad getByIds

}
