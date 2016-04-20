package im.actor.runtime.clc;

import java.sql.Connection;
import java.util.List;

import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mohammad on 11/18/15.
 */
public class ClcKeyValueStorage implements KeyValueStorage {

    private static final Logger logger = LoggerFactory.getLogger(ClcKeyValueStorage.class);
    private SQLiteStatementWrapper insertStatement;
    private SQLiteStatementWrapper deleteStatement;

    private DBWrapper db;
    private String name;
    private boolean isSqliteChecked = false;
    private String context;

    /**
     * Create table in keyvalue storage if does not exist
     *
     * @param db
     * @param name    table name
     * @param context client unique identifier
     */
    public ClcKeyValueStorage(Connection db, String name, String context) {
        this.db = new DBWrapper(db);
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
                    "\"ID\" INTEGER NOT NULL," + // 0: id
                    "\"BYTES\" BLOB NOT NULL," + // 1: bytes
                    "\"CONTEXT\" TEXT NOT NULL," + // 2: context
                    "PRIMARY KEY(\"ID\", \"CONTEXT\"));");
        }
    }

    private void checkInsertStatement() {
        if (insertStatement == null) {
            insertStatement = db.compileStatement("INSERT OR REPLACE INTO \"" + name + "\" " +
                    "(\"ID\",\"BYTES\",\"CONTEXT\") VALUES (?,?,?)");
        }
    }

    private void checkDeleteStatement() {
        if (deleteStatement == null) {
            deleteStatement = db.compileStatement("DELETE FROM \"" + name + "\" WHERE \"ID\"=? AND \"CONTEXT\"=? ");
        }
    }


    @Override
    public void addOrUpdateItem(long id, byte[] data) {
        checkSqlite();
        checkInsertStatement();

        insertStatement.bindLong(1, id);
        insertStatement.bindBlob(2, data);
        insertStatement.bindString(3, this.context);
        insertStatement.executeInsert();

    }

    @Override
    public void addOrUpdateItems(List<KeyValueRecord> values) {
        checkSqlite();
        checkInsertStatement();

        for (KeyValueRecord r : values) {
            insertStatement.bindLong(1, r.getId());
            insertStatement.bindBlob(2, r.getData());
            insertStatement.bindString(3, this.context);
            insertStatement.executeInsert();
        }

    }

    @Override
    public void removeItem(long id) {
        checkSqlite();
        checkDeleteStatement();

        deleteStatement.bindLong(1, id);
        deleteStatement.bindString(2, this.context);
        deleteStatement.execute();
    }

    @Override
    public void removeItems(long[] ids) {
        checkSqlite();
        checkDeleteStatement();

        for (long id : ids) {
            deleteStatement.bindLong(1, id);
            deleteStatement.bindString(2, this.context);
            deleteStatement.execute();
        }

    }

    @Override
    public byte[] loadItem(long key) {
        checkSqlite();

        Cursor cursor = db.query("\"" + name + "\"", new String[]{"\"BYTES\""}, "\"ID\" = ? AND \"CONTEXT\"='?' ", new String[]{"" + key, this.context}, null, null, null);

        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToNext())
                return cursor.getBlob("BYTES");
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public List<KeyValueRecord> loadItems(long[] keys) {
        return null;
    }

    @Override
    public List<KeyValueRecord> loadAllItems() {
        return null;
    }

    @Override
    public void clear() {
        checkSqlite();

        db.execSQL("DELETE FROM \"" + name + "\"" + " WHERE \"CONTEXT\"='" + this.context + "'");
    }

    public void clearAll() {
        checkSqlite();

        db.execSQL("DELETE FROM \"" + name + "\"");
    }

    public String getContext() {
        return this.context;
    }
}
