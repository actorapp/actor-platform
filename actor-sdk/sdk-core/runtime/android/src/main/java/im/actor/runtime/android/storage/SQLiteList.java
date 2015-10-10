/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.storage.ListEngineRecord;
import im.actor.runtime.storage.ListStorageDisplayEx;

public class SQLiteList implements ListStorageDisplayEx {

    private SQLiteDatabase database;
    private String tableName;
    private boolean isTableChecked = false;

    public SQLiteList(SQLiteDatabase database, String tableName) {
        this.database = database;
        this.tableName = tableName;
    }

    private void checkTable() {
        if (isTableChecked) {
            return;
        }
        isTableChecked = true;

        if (!SQLiteHelpers.isTableExists(database, tableName)) {
            database.execSQL("CREATE TABLE IF NOT EXISTS \"" + tableName + "\" (" + //
                    "\"ID\" INTEGER NOT NULL," + // 0: id
                    "\"SORT_KEY\" INTEGER NOT NULL," + // 1: sortKey
                    "\"QUERY\" TEXT," + // 2: query
                    "\"BYTES\" BLOB NOT NULL," + // 3: bytes
                    "PRIMARY KEY(\"ID\"));");

            // Filter index
            database.execSQL("CREATE INDEX IF NOT EXISTS IDX_ID_QUERY_SORT ON \"" + tableName + "\" (\"QUERY\", \"SORT_KEY\");");

            // Standard index
            database.execSQL("CREATE INDEX IF NOT EXISTS IDX_ID_SORT ON \"" + tableName + "\" (\"SORT_KEY\");");
        }
    }

    @Override
    public void updateOrAdd(ListEngineRecord valueContainer) {
        checkTable();

        Object[] args = new Object[]{valueContainer.getKey(), valueContainer.getQuery() != null ? valueContainer.getQuery().toLowerCase() : null, valueContainer.getOrder(), valueContainer.getData()};
        database.execSQL("REPLACE INTO \"" + tableName + "\" (\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?)", args);
    }

    @Override
    public void updateOrAdd(List<ListEngineRecord> items) {
        checkTable();

        database.beginTransaction();

        try {
            for (ListEngineRecord record : items) {
                Object[] args = new Object[]{record.getKey(), record.getQuery() != null ? record.getQuery().toLowerCase() : null, record.getOrder(), record.getData()};
                database.execSQL("REPLACE INTO \"" + tableName + "\" (\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?)", args);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void delete(long key) {
        checkTable();

        Object[] args = new Object[]{key};
        database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"ID\"=?", args);
    }

    @Override
    public void delete(long[] keys) {
        checkTable();

        database.beginTransaction();
        try {
            for (long key : keys) {
                Object[] args = new Object[]{key};
                database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"ID\"=?", args);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void clear() {
        checkTable();

        database.execSQL("DELETE FROM \"" + tableName + "\"");
    }

    @Override
    public ListEngineRecord loadItem(long key) {
        checkTable();

        Cursor cursor = database.query("\"" + tableName + "\"", new String[]{"\"ID\"", "\"SORT_KEY\"",
                        "\"QUERY\"", "\"BYTES\""}, "\"ID\"=?",
                new String[]{String.valueOf(key)}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                return new ListEngineRecord(key, cursor.getLong(cursor.getColumnIndex("SORT_KEY")),
                        cursor.getString(cursor.getColumnIndex("QUERY")),
                        cursor.getBlob(cursor.getColumnIndex("BYTES")));
            }
        } finally {
            cursor.close();
        }
        return null;
    }


    public ListEngineRecord loadItemBySortKey(long key) {
        checkTable();

        Cursor cursor = database.query("\"" + tableName + "\"", new String[]{"\"ID\"", "\"SORT_KEY\"",
                        "\"QUERY\"", "\"BYTES\""}, "\"SORT_KEY\"=?",
                new String[]{String.valueOf(key)}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                return new ListEngineRecord(key, cursor.getLong(cursor.getColumnIndex("SORT_KEY")),
                        cursor.getString(cursor.getColumnIndex("QUERY")),
                        cursor.getBlob(cursor.getColumnIndex("BYTES")));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        Cursor cursor = database.rawQuery("EXISTS (SELECT * FROM \"" + tableName + "\");", null);
        if (cursor != null) {
            try {
                return cursor.getInt(0) > 0;
            } finally {
                cursor.close();
            }
        }
        return false;
    }

    @Override
    public List<ListEngineRecord> loadForward(Long sortingKey, int limit) {
        checkTable();

        Cursor cursor;
        if (sortingKey == null) {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    null, null, null, null, "\"SORT_KEY\" DESC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "\"SORT_KEY\" < ?",
                    new String[]{
                            String.valueOf(sortingKey)
                    }, null, null, "\"SORT_KEY\" DESC", String.valueOf(limit));
        }

        return loadSlice(cursor);
    }

    @Override
    public List<ListEngineRecord> loadBackward(Long sortingKey, int limit) {
        checkTable();

        Cursor cursor;
        if (sortingKey == null) {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    null, null, null, null, "\"SORT_KEY\" ASC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "\"SORT_KEY\" > ?",
                    new String[]{
                            String.valueOf(sortingKey)
                    }, null, null, "\"SORT_KEY\" ASC", String.valueOf(limit));
        }

        return loadSlice(cursor);
    }

    @Override
    public List<ListEngineRecord> loadForward(String query, Long sortingKey, int limit) {
        checkTable();
        Cursor cursor;
        if (sortingKey == null) {
            cursor = database.query("\"" + tableName + "\"", new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?", new String[]{
                            query + "%",
                            "% " + query + "%"
                    }, null, null, "SORT_KEY DESC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "(\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" < ?",
                    new String[]{
                            query + "%",
                            "% " + query + "%",
                            String.valueOf(sortingKey)
                    }, null, null, "\"SORT_KEY\" DESC", String.valueOf(limit));
        }
        return loadSlice(cursor);
    }

    @Override
    public List<ListEngineRecord> loadCenter(Long centerSortKey, int limit) {
        checkTable();

        Cursor cursor;
        if (centerSortKey == null) {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    null, null, null, null, "\"SORT_KEY\" DESC", String.valueOf(limit));
            return loadSlice(cursor);
        } else {

            ListEngineRecord centerItem = loadItemBySortKey(centerSortKey);

            ArrayList<ListEngineRecord> ret = new ArrayList<ListEngineRecord>();
            ret.addAll(loadBackward(centerSortKey, limit));
            if (centerItem != null) ret.add(centerItem);
            ret.addAll(loadForward(centerSortKey, limit));
            return ret;
        }


    }

    @Override
    public List<ListEngineRecord> loadBackward(String query, Long sortingKey, int limit) {
        checkTable();
        Cursor cursor;
        if (sortingKey == null) {
            cursor = database.query("\"" + tableName + "\"", new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?", new String[]{
                            query + "%",
                            "% " + query + "%"
                    }, null, null, "SORT_KEY ASC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "(\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" > ?",
                    new String[]{
                            query + "%",
                            "% " + query + "%",
                            String.valueOf(sortingKey)
                    }, null, null, "\"SORT_KEY\" ASC", String.valueOf(limit));
        }
        return loadSlice(cursor);
    }

    @Override
    public int getCount() {
        checkTable();

        Cursor mCount = null;
        try {
            mCount = database.rawQuery("SELECT COUNT(*) FROM \"" + tableName + "\"", null);
            if (mCount.moveToFirst()) {
                return mCount.getInt(0);
            }
        } finally {
            if (mCount != null) {
                mCount.close();
            }
        }
        return 0;
    }

    private ArrayList<ListEngineRecord> loadSlice(Cursor cursor) {
        // int queryColumn = enableSearch ? cursor.getColumnIndex("QUERY") : -1;
        ArrayList<ListEngineRecord> res = new ArrayList<ListEngineRecord>();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndex("ID");
            int sortColumn = cursor.getColumnIndex("SORT_KEY");
            int bytesColumn = cursor.getColumnIndex("BYTES");
            int queryColumn = cursor.getColumnIndex("QUERY");
            try {
                if (cursor.moveToFirst()) {
                    do {
                        res.add(new ListEngineRecord(cursor.getLong(idColumn), cursor.getLong(sortColumn),
                                cursor.getString(queryColumn),
                                cursor.getBlob(bytesColumn)));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        return res;
    }
}