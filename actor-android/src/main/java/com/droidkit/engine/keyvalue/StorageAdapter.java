package com.droidkit.engine.keyvalue;

import java.util.ArrayList;
import java.util.List;

public interface StorageAdapter<V> {

    void insertSingle(V item);

    void insertOrReplaceSingle(V item);

    void deleteSingle(long id);

    void insertBatch(List<V> items);

    void insertOrReplaceBatch(List<V> items);

    void deleteBatch(long[] ids);

    void deleteAll();

    ArrayList<V> loadAll();

    V getById(long id);

    //todo ad getByIds

}
