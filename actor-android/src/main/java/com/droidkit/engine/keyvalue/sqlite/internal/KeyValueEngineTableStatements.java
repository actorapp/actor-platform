package com.droidkit.engine.keyvalue.sqlite.internal;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.droidkit.engine._internal.sqlite.SqlStatements;

public class KeyValueEngineTableStatements implements SqlStatements {
    private final SQLiteDatabase db;
    private final String tablename;

    private SQLiteStatement insertStatement;
    private SQLiteStatement insertOrReplaceStatement;
    private SQLiteStatement updateStatement;
    private SQLiteStatement deleteStatement;

    private String getByIdStatement;
    private String allStatement;

    public KeyValueEngineTableStatements(SQLiteDatabase db, String tablename) {
        this.db = db;
        this.tablename = tablename;
    }

    public SQLiteStatement getInsertStatement() {
        if (insertStatement == null) {
            String sql = String.format("INSERT INTO '%s' (ID,BYTES) VALUES (?,?)", tablename);
            insertStatement = db.compileStatement(sql);
        }
        return insertStatement;
    }

    public SQLiteStatement getInsertOrReplaceStatement() {
        if (insertOrReplaceStatement == null) {
            String sql = String.format("INSERT OR REPLACE INTO '%s' (ID,BYTES) VALUES (?,?)", tablename);
            insertOrReplaceStatement = db.compileStatement(sql);
        }
        return insertOrReplaceStatement;
    }

    public SQLiteStatement getDeleteStatement() {
        if (deleteStatement == null) {
            String sql = String.format("DELETE FROM '%s' WHERE ID=?", tablename);
            deleteStatement = db.compileStatement(sql);
        }
        return deleteStatement;
    }

    public String getGetByIdStatement() {
        if(getByIdStatement == null) {
            getByIdStatement = String.format("SELECT ID,BYTES FROM '%s' WHERE ID=?", tablename);
        }
        return getByIdStatement;
    }

    public String getAllStatement() {
        if(allStatement == null) {
            allStatement = String.format("SELECT ID,BYTES FROM '%s'", tablename);
        }
        return allStatement;
    }
}

