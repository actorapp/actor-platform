package com.droidkit.engine._internal.sqlite;

import android.database.sqlite.SQLiteStatement;

public interface SqlStatements {
    SQLiteStatement getInsertStatement();

    SQLiteStatement getInsertOrReplaceStatement();

    SQLiteStatement getDeleteStatement();

    String getGetByIdStatement();

    String getAllStatement();
}
