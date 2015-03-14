package com.droidkit.engine.list.view;

import com.droidkit.engine.list.FilterableDataAdapter;
import com.droidkit.engine.list.DataAdapter;
import com.droidkit.engine.list.ListEngine;
import com.droidkit.engine.list.storage.SQLiteStorageAdapter;
import im.actor.model.android.sql.SQLiteProvider;

/**
 * Created by ex3ndr on 15.09.14.
 */
public class ListHolder<T> {
    private DataAdapter<T> dataAdapter;
    private volatile ListEngine<T> engine;
    private String tableName;
    private final Object lock = new Object();
    private EngineUiList<T> uiListEngine;

    public ListHolder(DataAdapter<T> dataAdapter, String tableName) {
        this.dataAdapter = dataAdapter;
        this.tableName = tableName;
    }

    public ListEngine<T> getEngine() {
        if (engine == null) {
            synchronized (lock) {
                if (engine == null) {
                    SQLiteStorageAdapter storageAdapter = new SQLiteStorageAdapter(
                            SQLiteProvider.db(),
                            tableName, dataAdapter instanceof FilterableDataAdapter);
                    engine = new ListEngine<T>(storageAdapter, dataAdapter);
                    uiListEngine = new EngineUiList<T>(engine);
                }
            }
        }
        return engine;
    }

    public EngineUiList<T> getUiListEngine() {
        getEngine();
        return uiListEngine;
    }
}
