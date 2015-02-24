package im.actor.messenger.storage.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.List;

import im.actor.model.storage.KeyValueRecord;
import im.actor.model.storage.KeyValueStorage;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class SQLiteKeyValue implements KeyValueStorage {

    private SQLiteStatement insertStatement;
    private SQLiteStatement deleteStatement;

    private SQLiteDatabase db;
    private String name;

    public SQLiteKeyValue(SQLiteDatabase db, String name) {
        this.db = db;
        this.name = name;

        if (!SQLiteHelpers.isTableExists(db, name)) {
            db.execSQL("CREATE TABLE IF NOT EXISTS \"" + name + "\" (" + //
                    "\"ID\" INTEGER NOT NULL," + // 0: id
                    "\"BYTES\" BLOB NOT NULL," + // 1: bytes
                    "PRIMARY KEY(\"ID\"));");
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
