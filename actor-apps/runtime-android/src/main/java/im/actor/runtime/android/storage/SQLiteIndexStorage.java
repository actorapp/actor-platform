package im.actor.runtime.android.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.storage.IndexStorage;

public class SQLiteIndexStorage implements IndexStorage {

    private SQLiteDatabase db;
    private String name;
    private boolean isSqliteChecked = false;

    private SQLiteStatement insertStatement;
    private SQLiteStatement deleteStatement;


    public SQLiteIndexStorage(SQLiteDatabase db, String name) {
        this.db = db;
        this.name = name;
    }

    private void checkSqlite() {
        if (!isSqliteChecked) {
            isSqliteChecked = true;
            if (!SQLiteHelpers.isTableExists(db, name)) {
                db.execSQL("CREATE TABLE IF NOT EXISTS \"" + name + "\" (" + //
                        "\"ID\" LONG NOT NULL," + // 0: key
                        "\"VALUE\" LONG NOT NULL," + // 1: value
                        "PRIMARY KEY(\"ID\"));");
            }
        }
    }

    private void checkInsertStatement() {
        if (insertStatement == null) {
            insertStatement = db.compileStatement("INSERT OR REPLACE INTO \"" + name + "\" " +
                    "(\"ID\",\"VALUE\") VALUES (?,?)");
        }
    }

    private void checkDeleteStatement() {
        if (deleteStatement == null) {
            deleteStatement = db.compileStatement("DELETE FROM \"" + name + "\" WHERE \"ID\"=?");
        }
    }

    @Override
    public void put(long key, long value) {
        checkSqlite();
        checkInsertStatement();

        db.beginTransaction();
        try {
            insertStatement.bindLong(1, key);
            insertStatement.bindLong(2, value);
            insertStatement.executeInsert();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public Long get(long key) {
        checkSqlite();
        Cursor cursor = db.query("\"" + name + "\"", new String[]{"\"VALUE\""}, "\"ID\" = ?", new String[]{"" + key}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
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
        Cursor cursor = db.query("\"" + name + "\"", new String[]{"\"ID\""}, "\"VALUE\" <= ?", new String[]{"" + value}, null, null, null);
        if (cursor == null) {
            return list;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getLong(0));
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

        db.beginTransaction();
        try {
            deleteStatement.bindLong(1, key);
            deleteStatement.execute();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void remove(List<Long> keys) {
        checkSqlite();
        checkDeleteStatement();

        db.beginTransaction();
        try {
            for (long key : keys) {
                deleteStatement.bindLong(1, key);
                deleteStatement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int getCount() {
        checkSqlite();

        Cursor mCount = null;
        try {
            mCount = db.rawQuery("SELECT COUNT(*) FROM \"" + name + "\"", null);
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
    public void clear() {
        checkSqlite();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM \"" + name + "\"");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
