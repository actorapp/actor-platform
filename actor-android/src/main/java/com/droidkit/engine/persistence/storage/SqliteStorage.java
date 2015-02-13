package com.droidkit.engine.persistence.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 26.09.14.
 */
public class SqliteStorage implements PersistenceStorage {
    private SQLiteDatabase database;
    private String tableName;

    public SqliteStorage(SQLiteDatabase database, String tableName) {
        this.database = database;
        this.tableName = tableName;
        createTable();
    }

    private void createTable() {
        database.execSQL("CREATE TABLE IF NOT EXISTS \"" + tableName + "\" (" +
                "\"ID\" PRIMARY KEY, " +
                "\"BYTES\" BLOB NOT NULL);");
    }

    @Override
    public void put(RawValue value) {
        database.execSQL("REPLACE INTO \"" + tableName + "\" (\"ID\",\"BYTES\") VALUES (?,?)", new Object[]{
                value.getKey(), value.getData()});
    }

    @Override
    public void put(RawValue[] values) {
        database.beginTransaction();
        try {
            for (RawValue value : values) {
                database.execSQL("REPLACE INTO \"" + tableName + "\" (\"ID\",\"BYTES\") VALUES (?,?)", new Object[]{
                        value.getKey(), value.getData()});
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void remove(long key) {
        database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"ID\"=?", new Object[]{key});
    }

    @Override
    public RawValue[] readAll() {
        Cursor cursor = database.query("\"" + tableName + "\"", new String[]{"\"ID\"", "\"BYTES\""}, null, null, null, null, null);
        ArrayList<RawValue> res = new ArrayList<RawValue>();
        if (cursor != null) {
            try {
                int idColumn = cursor.getColumnIndex("ID");
                int bytesColumn = cursor.getColumnIndex("BYTES");
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    byte[] bytes = cursor.getBlob(bytesColumn);
                    res.add(new RawValue(id, bytes));
                }
            } finally {
                cursor.close();
            }
        }
        return res.toArray(new RawValue[res.size()]);
    }

    @Override
    public void clear() {
        database.execSQL("DELETE FROM \"" + tableName + "\";");
    }
}