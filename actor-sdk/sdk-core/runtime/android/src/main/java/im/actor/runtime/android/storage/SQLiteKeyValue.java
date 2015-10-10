/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.List;

import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;

public class SQLiteKeyValue implements KeyValueStorage {

    private SQLiteStatement insertStatement;
    private SQLiteStatement deleteStatement;

    private SQLiteDatabase db;
    private String name;
    private boolean isSqliteChecked = false;

    public SQLiteKeyValue(SQLiteDatabase db, String name) {
        this.db = db;
        this.name = name;
    }

    private void checkSqlite() {
        if (!isSqliteChecked) {
            isSqliteChecked = true;
            if (!SQLiteHelpers.isTableExists(db, name)) {
                db.execSQL("CREATE TABLE IF NOT EXISTS \"" + name + "\" (" + //
                        "\"ID\" INTEGER NOT NULL," + // 0: id
                        "\"BYTES\" BLOB NOT NULL," + // 1: bytes
                        "PRIMARY KEY(\"ID\"));");
            }
        }
    }

    private void checkInsertStatement() {
        if (insertStatement == null) {
            insertStatement = db.compileStatement("INSERT OR REPLACE INTO \"" + name + "\" " +
                    "(\"ID\",\"BYTES\") VALUES (?,?)");
        }
    }

    private void checkDeleteStatement() {
        if (deleteStatement == null) {
            deleteStatement = db.compileStatement("DELETE FROM \"" + name + "\" WHERE \"ID\"=?");
        }
    }

    @Override
    public void addOrUpdateItem(long id, byte[] data) {
        checkSqlite();
        checkInsertStatement();

        db.beginTransaction();
        try {
            insertStatement.bindLong(1, id);
            insertStatement.bindBlob(2, data);
            insertStatement.executeInsert();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void addOrUpdateItems(List<KeyValueRecord> values) {
        checkSqlite();
        checkInsertStatement();

        db.beginTransaction();
        try {
            for (KeyValueRecord r : values) {
                insertStatement.bindLong(1, r.getId());
                insertStatement.bindBlob(2, r.getData());
                insertStatement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void removeItem(long id) {
        checkSqlite();
        checkDeleteStatement();

        db.beginTransaction();
        try {
            deleteStatement.bindLong(1, id);
            deleteStatement.execute();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void removeItems(long[] ids) {
        checkSqlite();
        checkDeleteStatement();

        db.beginTransaction();
        try {
            for (long id : ids) {
                deleteStatement.bindLong(1, id);
                deleteStatement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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

    @Override
    public byte[] getValue(long id) {
        checkSqlite();
        Cursor cursor = db.query("\"" + name + "\"", new String[]{"\"BYTES\""}, "\"ID\" = ?", new String[]{"" + id}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                return cursor.getBlob(0);
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
