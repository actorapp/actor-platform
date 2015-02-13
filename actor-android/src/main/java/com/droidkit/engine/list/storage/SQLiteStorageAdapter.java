package com.droidkit.engine.list.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteStorageAdapter implements StorageAdapter {

    private SQLiteDatabase database;
    private long tableId;
    private String tableName;
    private boolean enableSearch;

    public SQLiteStorageAdapter(SQLiteDatabase database, String listEngineName) {
        this(database, listEngineName, false);
    }

    public SQLiteStorageAdapter(SQLiteDatabase database, String listEngineName, boolean enableSearch) {
        this(database, listEngineName, 0, enableSearch);
    }

    public SQLiteStorageAdapter(SQLiteDatabase database, String listEngineName, long listEngineId, boolean enableSearch) {
        this.database = database;
        this.tableId = listEngineId;
        this.tableName = listEngineName;
        this.enableSearch = enableSearch;
        createTable();
    }

    private void createTable() {
        database.execSQL("CREATE TABLE IF NOT EXISTS \"" + tableName + "\" (" + //
                "\"LIST_ID\" INTEGER NOT NULL," + // 0: listId
                "\"ID\" INTEGER NOT NULL," + // 1: id
                "\"SORT_KEY\" INTEGER NOT NULL," + // 2: sortKey
                (enableSearch ? "\"QUERY\" TEXT NOT NULL," : "") +
                "\"BYTES\" BLOB NOT NULL," + // 4: bytes
                "PRIMARY KEY(\"LIST_ID\", \"ID\"));");

        database.execSQL("PRAGMA synchronous = NORMAL;");

        if (enableSearch) {
            database.execSQL("CREATE INDEX IF NOT EXISTS IDX_ID_QUERY_SORT ON \"" + tableName + "\" (\"LIST_ID\",\"QUERY\", \"SORT_KEY\");");
        }
        database.execSQL("CREATE INDEX IF NOT EXISTS IDX_ID_SORT ON \"" + tableName + "\" (\"LIST_ID\", \"SORT_KEY\");");
    }

    @Override
    public void updateOrAdd(ValueContainer valueContainer) {
        if (enableSearch) {
            Object[] args = new Object[]{tableId, valueContainer.getId(), valueContainer.getQuery(), valueContainer.getOrder(), valueContainer.getData()};
            database.execSQL("REPLACE INTO \"" + tableName + "\" (\"LIST_ID\",\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?,?)", args);
        } else {
            Object[] args = new Object[]{tableId, valueContainer.getId(), valueContainer.getOrder(), valueContainer.getData()};
            database.execSQL("REPLACE INTO \"" + tableName + "\" (\"LIST_ID\",\"ID\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?)", args);
        }
    }

    @Override
    public void updateOrAdd(List<ValueContainer> items) {
        database.beginTransaction();
        try {
            for (ValueContainer valueContainer : items) {
                if (enableSearch) {
                    Object[] args = new Object[]{tableId, valueContainer.getId(), valueContainer.getQuery(), valueContainer.getOrder(), valueContainer.getData()};
                    database.execSQL("REPLACE INTO \"" + tableName + "\" (\"LIST_ID\",\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?,?)", args);
                } else {
                    Object[] args = new Object[]{tableId, valueContainer.getId(), valueContainer.getOrder(), valueContainer.getData()};
                    database.execSQL("REPLACE INTO \"" + tableName + "\" (\"LIST_ID\",\"ID\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?)", args);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void delete(long id) {
        Object[] args = new Object[]{tableId, id};
        database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"LIST_ID\"=? AND \"ID\"=?", args);
    }

    @Override
    public void clear() {
        Object[] args = new Object[]{tableId};
        database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"LIST_ID\"=?", args);
    }

    @Override
    public void delete(long[] ids) {
        database.beginTransaction();
        try {
            for (long id : ids) {
                Object[] args = new Object[]{tableId, id};
                database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"LIST_ID\"=? AND \"ID\"=?", args);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private SliceResult<ValueContainer> loadSlice(Cursor cursor) {
        int idColumn = cursor.getColumnIndex("ID");
        int sortColumn = cursor.getColumnIndex("SORT_KEY");
        int bytesColumn = cursor.getColumnIndex("BYTES");
        // int queryColumn = enableSearch ? cursor.getColumnIndex("QUERY") : -1;
        ArrayList<ValueContainer> res = new ArrayList<ValueContainer>();
        Object nMinKey = null;
        Object nMaxKey = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        res.add(new ValueContainer(cursor.getLong(idColumn), cursor.getLong(sortColumn), null,
                                cursor.getBlob(bytesColumn)));
                    } while (cursor.moveToNext());
                    if (res.size() > 0) {
                        ValueContainer first = res.get(0);
                        ValueContainer last = res.get(res.size() - 1);
                        nMinKey = new SqlKey(first.getOrder());
                        nMaxKey = new SqlKey(last.getOrder());
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return new SliceResult<ValueContainer>(res, nMaxKey, nMinKey);
    }

    @Override
    public SliceResult<ValueContainer> loadTail(Object sortKey, int limit) {
        Cursor cursor;
        if (sortKey == null) {
            cursor = database.query("\"" + tableName + "\"", new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"BYTES\""}, "\"LIST_ID\"=?", new String[]{
                    String.valueOf(tableId)}, null, null, "SORT_KEY DESC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\",\"SORT_KEY\",\"BYTES\""},
                    "\"LIST_ID\"=? AND \"SORT_KEY\" < ?",
                    new String[]{
                            String.valueOf(tableId),
                            String.valueOf(((SqlKey) sortKey).getSortingKey())
                    }, null, null, "\"SORT_KEY\" DESC", String.valueOf(limit));
        }

        return loadSlice(cursor);
    }


    @Override
    public SliceResult<ValueContainer> loadHead(Object bottomSortingKey, int limit) {
        Cursor cursor;
        if (bottomSortingKey == null) {
            cursor = database.query("\"" + tableName + "\"", new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"BYTES\""}, "\"LIST_ID\"=?", new String[]{
                    String.valueOf(tableId)}, null, null, "SORT_KEY ASC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\",\"SORT_KEY\",\"BYTES\""},
                    "\"LIST_ID\"=? AND \"SORT_KEY\" > ?",
                    new String[]{
                            String.valueOf(tableId),
                            String.valueOf(((SqlKey) bottomSortingKey).getSortingKey())
                    }, null, null, "\"SORT_KEY\" ASC", String.valueOf(limit));
        }

        return loadSlice(cursor);
    }

    @Override
    public SliceResult<ValueContainer> loadHead(String query, Object bottomSortingKey, int limit) {
        if (!enableSearch) {
            throw new RuntimeException("Search not enabled");
        }

        Cursor cursor;
        if (bottomSortingKey == null) {
            cursor = database.query("\"" + tableName + "\"", new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "\"LIST_ID\"=? AND (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?)", new String[]{
                            String.valueOf(tableId),
                            query + "%",
                            "% " + query + "%"
                    }, null, null, "SORT_KEY ASC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\",\"SORT_KEY\",\"BYTES\""},
                    "\"LIST_ID\"=? AND (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" > ?",
                    new String[]{
                            String.valueOf(tableId),
                            query + "%",
                            "% " + query + "%",
                            String.valueOf(((SqlKey) bottomSortingKey).getSortingKey())
                    }, null, null, "\"SORT_KEY\" ASC", String.valueOf(limit));
        }

        return loadSlice(cursor);
    }

    @Override
    public SliceResult<ValueContainer> loadTail(String query, Object topSortingKey, int limit) {
        if (!enableSearch) {
            throw new RuntimeException("Search not enabled");
        }
        Cursor cursor;
        if (topSortingKey == null) {
            cursor = database.query("\"" + tableName + "\"", new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"QUERY\"", "\"BYTES\""},
                    "\"LIST_ID\"=? AND (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?)", new String[]{
                            String.valueOf(tableId),
                            query + "%",
                            "% " + query + "%"
                    }, null, null, "SORT_KEY DESC", String.valueOf(limit));
        } else {
            cursor = database.query("\"" + tableName + "\"",
                    new String[]{"\"ID\",\"SORT_KEY\",\"BYTES\""},
                    "\"LIST_ID\"=? AND (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" < ?",
                    new String[]{
                            String.valueOf(tableId),
                            query + "%",
                            "% " + query + "%",
                            String.valueOf(((SqlKey) topSortingKey).getSortingKey())
                    }, null, null, "\"SORT_KEY\" DESC", String.valueOf(limit));
        }

        return loadSlice(cursor);
    }

    @Override
    public SliceResult<ValueContainer> loadBefore(long sortingKey, int limit) {
        Cursor cursor = database.query("\"" + tableName + "\"",
                new String[]{"\"ID\",\"SORT_KEY\",\"BYTES\""},
                "\"LIST_ID\"=? AND \"SORT_KEY\" <= ?",
                new String[]{String.valueOf(tableId), String.valueOf(sortingKey)},
                null, null, "\"SORT_KEY\" DESC", String.valueOf(limit));
        return loadSlice(cursor);
    }

    @Override
    public SliceResult<ValueContainer> loadAfter(long sortingKey, int limit) {
        Cursor cursor = database.query("\"" + tableName + "\"",
                new String[]{"\"ID\",\"SORT_KEY\",\"BYTES\""},
                "\"LIST_ID\"=? AND \"SORT_KEY\" > ?",
                new String[]{String.valueOf(tableId), String.valueOf(sortingKey)},
                null, null, "\"SORT_KEY\" ASC", String.valueOf(limit));
        return loadSlice(cursor);
    }

    @Override
    public int getCount() {
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

    @Override
    public ValueContainer loadItem(long id) {
        Cursor cursor = database.query("\"" + tableName + "\"", new String[]{"\"LIST_ID\"", "\"ID\"", "\"SORT_KEY\"", "\"BYTES\""}, "\"LIST_ID\"=? AND \"ID\"=?",
                new String[]{String.valueOf(tableId), String.valueOf(id)}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                return new ValueContainer(id, cursor.getLong(cursor.getColumnIndex("SORT_KEY")), null,
                        cursor.getBlob(cursor.getColumnIndex("BYTES")));
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
