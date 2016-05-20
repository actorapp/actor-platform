package im.actor.runtime.clc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.Log;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.runtime.storage.ListEngineRecord;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.ListStorageDisplayEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mohammad on 11/18/15.
 */
public class ClcListStorage implements ListStorageDisplayEx {
    private static final Logger logger = LoggerFactory.getLogger(ClcListStorage.class);

    private DBWrapper database;
    private String tableName;
    private boolean isTableChecked = false;
    private String context;

    public ClcListStorage(Connection database, String tableName, String context) {
        this.database = new DBWrapper(database);
        this.tableName = tableName;
        this.context = context;
        if(context == null){
            logger.warn("context is not set");
            this.context = "";
        }
    }

    private void checkTable() {
        if (isTableChecked) {
            return;
        }
        isTableChecked = true;

        database.execSQL("CREATE TABLE IF NOT EXISTS \"" + tableName + "\" (" + //
                "\"ID\" INTEGER NOT NULL," + // 0: id
                "\"SORT_KEY\" INTEGER NOT NULL," + // 1: sortKey
                "\"QUERY\" TEXT," + // 2: query
                "\"BYTES\" BLOB NOT NULL," + // 3: bytes
                "\"CONTEXT\" TEXT NOT NULL," + // 4: context
                "PRIMARY KEY(\"ID\", \"CONTEXT\"));");

        // Filter index
        database.execSQL("CREATE INDEX IF NOT EXISTS IDX_ID_QUERY_SORT ON \"" + tableName + "\" (\"QUERY\", \"SORT_KEY\");");

        // Standard index
        database.execSQL("CREATE INDEX IF NOT EXISTS IDX_ID_SORT ON \"" + tableName + "\" (\"SORT_KEY\");");
    }

    @Override
    public void updateOrAdd(ListEngineRecord valueContainer) {
        checkTable();

        Object[] args = new Object[]{valueContainer.getKey(), valueContainer.getQuery() != null ? valueContainer.getQuery().toLowerCase() : null, valueContainer.getOrder(), valueContainer.getData(), this.context};
        database.execSQL("REPLACE INTO \"" + tableName + "\" (\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\",\"CONTEXT\") VALUES (?,?,?,?,?)", args);
    }

    @Override
    public void updateOrAdd(List<ListEngineRecord> items) {
        checkTable();


        for (ListEngineRecord record : items) {
            Object[] args = new Object[]{record.getKey(), record.getQuery() != null ? record.getQuery().toLowerCase() : null, record.getOrder(), record.getData(), this.context};
            database.execSQL("REPLACE INTO \"" + tableName + "\" (\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\",\"CONTEXT\") VALUES (?,?,?,?,?)", args);
        }

    }

    @Override
    public void delete(long key) {
        checkTable();

        Object[] args = new Object[]{key, this.context};
        database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"ID\"=? AND \"CONTEXT\"=?", args);
    }

    @Override
    public void delete(long[] keys) {
        checkTable();

        for (long key : keys) {
            Object[] args = new Object[]{key, this.context};
            database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"ID\"=? AND \"CONTEXT\"=?", args);
        }

    }

    @Override
    public void clear() {
        checkTable();

        database.execSQL("DELETE FROM \"" + tableName + "\" WHERE \"CONTEXT\"=?",new Object[]{this.context});
    }

    @Override
    public ListEngineRecord loadItem(long key) {
        checkTable();

        Cursor cursor = database.query("\"" + tableName + "\"", new String[]{"\"ID\"", "\"SORT_KEY\"",
                        "\"QUERY\"", "\"BYTES\""}, "\"ID\"=? AND \"CONTEXT\"='?'",
                new String[]{String.valueOf(key), this.context}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToNext()) {
                return new ListEngineRecord(key, cursor.getLong("SORT_KEY"),
                        cursor.getString("QUERY"),
                        cursor.getBlob("BYTES"));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public List<ListEngineRecord> loadAllItems() {
        return null;
    }

    public ListEngineRecord loadItemBySortKey(long key) {
        checkTable();

        Cursor cursor = database.query("\"" + tableName + "\"", new String[]{"\"ID\"", "\"SORT_KEY\"",
                        "\"QUERY\"", "\"BYTES\""}, "\"SORT_KEY\"=? AND \"CONTEXT\"='?'",
                new String[]{String.valueOf(key), this.context}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToNext()) {
                return new ListEngineRecord(key, cursor.getLong("SORT_KEY"),
                        cursor.getString("QUERY"),
                        cursor.getBlob("BYTES"));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        Cursor cursor = database.rawQuery("SELECT EXISTS (SELECT * FROM \"" + tableName + "\" WHERE \"CONTEXT\"='?');", new String[]{this.context});
        if (cursor != null) {
            try {
                return cursor.getInt(1) == 0;
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
            mCount = database.rawQuery("SELECT COUNT(*) FROM \"" + tableName + "\" WHERE \"CONTEXT\"='?'", new String[]{this.context});
            if (mCount.moveToNext()) {
                return mCount.getInt(1);
            }
        } finally {
            if (mCount != null) {
                mCount.close();
            }
        }
        return 0;
    }

    private ArrayList<ListEngineRecord> loadSlice(Cursor cursor) {
        ArrayList<ListEngineRecord> res = new ArrayList<ListEngineRecord>();
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    do {
                        res.add(new ListEngineRecord(cursor.getLong("ID"), cursor.getLong("SORT_KEY"),
                                cursor.getString("QUERY"),
                                cursor.getBlob("BYTES")));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        return res;
    }

    //Just for unit test
    public int countAll(){
        checkTable();

        Cursor mCount = null;
        try {
            mCount = database.rawQuery("SELECT COUNT(*) FROM \"" + tableName + "\"", null);
            if (mCount.moveToNext()) {
                return mCount.getInt(1);
            }
        } finally {
            if (mCount != null) {
                mCount.close();
            }
        }
        return 0;
    }

    //Just for unit test
    public void clearAll(){
        checkTable();
        database.execSQL("DELETE FROM \"" + tableName + "\"");
    }

    public String getContext(){
        return this.context;
    }
}
