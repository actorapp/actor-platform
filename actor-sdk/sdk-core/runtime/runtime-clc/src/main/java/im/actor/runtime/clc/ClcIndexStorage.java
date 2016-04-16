package im.actor.runtime.clc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.storage.IndexStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by mohammad on 11/18/15.
 */
public class ClcIndexStorage implements IndexStorage {
    private static final Logger logger = LoggerFactory.getLogger(ClcIndexStorage.class);

    private final String name;
    private final DBWrapper db;
    private boolean isSqliteChecked = false;
    private SQLiteStatementWrapper insertStatement;
    private SQLiteStatementWrapper deleteStatement;
    private String context;


    /**
     * Create table in index storage if does not exist
     *
     * @param i_db
     * @param name    table name
     * @param context client unique identifier
     */
    public ClcIndexStorage(Connection i_db, String name, String context) {
        this.db = new DBWrapper(i_db);
        this.name = name;
        this.context = context;
        if (context == null) {
            logger.warn("context is not set");
            this.context = "";
        }

    }

    private void checkSqlite() {
        if (!isSqliteChecked) {
            isSqliteChecked = true;
            db.execSQL("CREATE TABLE IF NOT EXISTS \"" + name + "\" (" + //
                    "\"ID\" LONG NOT NULL," + // 0: key
                    "\"VALUE\" LONG NOT NULL," + // 1: value
                    "\"CONTEXT\" TEXT NOT NULL," + // 2: context
                    "PRIMARY KEY(\"ID\", \"CONTEXT\"));");

        }
    }

    private void checkInsertStatement() {
        if (insertStatement == null) {
            insertStatement = db.compileStatement("INSERT OR REPLACE INTO \"" + name + "\" " +
                    "(\"ID\",\"VALUE\",\"CONTEXT\") VALUES (?,?,?)");
        }
    }

    private void checkDeleteStatement() {
        if (deleteStatement == null) {
            deleteStatement = db.compileStatement("DELETE FROM \"" + name + "\" WHERE \"ID\"=? AND \"CONTEXT\"=? ");
        }
    }

    @Override
    public void put(long key, long value) {
        checkSqlite();
        checkInsertStatement();

        insertStatement.bindLong(1, key);
        insertStatement.bindLong(2, value);
        insertStatement.bindString(3, this.context);
        insertStatement.executeInsert();
    }

    /**
     * Return the first record with this key and context
     * @param key
     * @return
     */
    @Override
    public Long get(long key) {
        checkSqlite();
        Cursor cursor = db.query("\"" + name + "\"", new String[]{"\"VALUE\""}, "\"ID\" = ? AND \"CONTEXT\"='?' ", new String[]{"" + key, this.context}, null, null, null);

        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToNext()) {
                return cursor.getLong(1);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public List<Long> findBeforeValue(long value) {
        checkSqlite();
        List<Long> list = new ArrayList<Long>();

        Cursor cursor = db.query("\"" + name + "\"", new String[]{"\"ID\""}, "\"VALUE\" <= ? AND \"CONTEXT\"='?'", new String[]{"" + value, this.context}, null, null, null);

        if (cursor == null) {
            return list;
        }
        try {
            if (cursor.moveToNext()) {
                do {
                    list.add(cursor.getLong(1));
                } while (cursor.moveToNext());
                return list;
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public List<Long> removeBeforeValue(long value) {
        List<Long> res = findBeforeValue(value);
        remove(res);
        return res;
    }

    @Override
    public void remove(long key) {
        checkSqlite();
        checkDeleteStatement();

        deleteStatement.bindLong(1, key);
        deleteStatement.bindString(2, this.context);
        deleteStatement.execute();
    }

    @Override
    public void remove(List<Long> keys) {
        checkSqlite();
        checkDeleteStatement();
        deleteStatement.bindString(2, this.context);
        for (long key : keys) {
            deleteStatement.bindLong(1, key);
            deleteStatement.execute();
        }


    }

    @Override
    public int getCount() {
        checkSqlite();

        Cursor mCount = null;
        try {
            mCount = db.rawQuery("SELECT COUNT(*) FROM \"" + name + "\"" + " WHERE \"CONTEXT\"='?'", new String[]{this.context});
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

    //Just use in unit test
    public int countAll(){
        checkSqlite();

        Cursor mCount = null;
        try {
            mCount = db.rawQuery("SELECT COUNT(*) FROM \"" + name + "\"", null);
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

    //Just use in unit test
    public void clearAll(){
        checkSqlite();
        db.execSQL("DELETE FROM \"" + name + "\"");
    }


    @Override
    public void clear() {
        checkSqlite();
        db.execSQL("DELETE FROM \"" + name + "\"" + " WHERE \"CONTEXT\"='" + this.context + "'");
    }

    public String getContext() {
        return this.context;
    }
}
