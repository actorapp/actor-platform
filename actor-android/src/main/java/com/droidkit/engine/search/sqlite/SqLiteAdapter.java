package com.droidkit.engine.search.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.droidkit.engine.search.SearchAdapter;
import com.droidkit.engine.search.ValueContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class SqLiteAdapter implements SearchAdapter {

    private final SQLiteDatabase database;
    private final String tableName;

    public SqLiteAdapter(SQLiteDatabase database, String tableName) {
        this.database = database;
        this.tableName = tableName;
        buildTable();
    }

    private void buildTable() {
        database.execSQL("CREATE TABLE IF NOT EXISTS \"" + tableName + "\" (" + //
                        "\"ID\" INTEGER NOT NULL," + // 0: id
                        "\"ORDER\" INTEGER NOT NULL," + // 1: order
                        "\"QUERY\" TEXT NOT NULL," + // 1: order
                        "\"BYTES\" BLOB NOT NULL," + // 1: bytes
                        "PRIMARY KEY(\"ID\"));"
        );
    }

    @Override
    public void clear() {
        database.delete(tableName, null, null);
    }

    @Override
    public void index(ValueContainer valueContainer) {
        Object[] args = new Object[]{valueContainer.getId(), valueContainer.getOrder(), valueContainer.getQuery(), valueContainer.getData()};
        database.execSQL("REPLACE INTO \"" + tableName + "\" (\"ID\",\"ORDER\",\"QUERY\",\"BYTES\") VALUES (?,?,?,?)", args);
    }

    @Override
    public void indexLow(ValueContainer valueContainer) {
        Cursor cursor = database.query(tableName, new String[]{"\"ID\"", "\"ORDER\""}, "\"ID\" = ?", new String[]{"" + valueContainer.getId()}, null, null, null);
        long order;
        if (cursor.moveToFirst()) {
            order = cursor.getLong(cursor.getColumnIndex("ORDER"));
            if (order < valueContainer.getOrder()) {
                order = valueContainer.getOrder();
            }
        } else {
            order = valueContainer.getOrder();
        }
        index(new ValueContainer(valueContainer.getId(), order, valueContainer.getQuery(), valueContainer.getData()));
    }

    @Override
    public void remove(long key) {
        database.delete(tableName, "\"ID\" = ?", new String[]{"" + key});
    }

    @Override
    public List<ValueContainer> query(String query) {
        Cursor cursor = database.query(tableName, new String[]{"\"ID\"", "\"ORDER\"", "\"QUERY\"", "\"BYTES\""},
                "\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?", new String[]{query + "%", "% " + query + "%"}, null, null, "\"ORDER\" DESC");
        List<ValueContainer> res = new ArrayList<ValueContainer>();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        res.add(new ValueContainer(cursor.getLong(0), cursor.getLong(1), cursor.getString(2), cursor.getBlob(3)));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return res;
    }
}