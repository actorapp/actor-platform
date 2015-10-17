package com.droidkit.pickers.file.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 14/09/2014.
 */
public class HistoryDatabase extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "droidkit_history";
    private static final String SELECTED_HISTORY_TABLE = "selected_history";
    private static final String[] SELECTED_HISTORY_COLUMNS = {
            "path",
            "last_selected"
    };

    private static final String SELECTED_HISTORY_DATABASE_CREATE =
            "create table " +
                    SELECTED_HISTORY_TABLE +
                    " ( " +
                    "path TEXT PRIMARY KEY," +
                    "last_selected INTEGER NOT NULL" +
                    " ) ";

    public HistoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        database.execSQL(SELECTED_HISTORY_DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int currentVersion, int newVersion) {

    }

    public static ArrayList<String> getHistory(Context context) {
        ArrayList<String> history = new ArrayList<String>();
        SQLiteDatabase database = new HistoryDatabase(context).getWritableDatabase();

        Cursor cursor = database.query(SELECTED_HISTORY_TABLE, SELECTED_HISTORY_COLUMNS, null, null, null, null, "last_selected DESC");
        int pathColumnIndex = cursor.getColumnIndex("path");
        ArrayList<String> removeIndexes = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            int count = 0;
            do {
                if (count < 20) {
                    count++;
                    history.add(cursor.getString(pathColumnIndex));
                } else {
                    removeIndexes.add(cursor.getString(pathColumnIndex));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        String[] removeIndexesArray = new String[removeIndexes.size()];
        database.delete(SELECTED_HISTORY_TABLE, "path = ?", removeIndexes.toArray(removeIndexesArray));
        database.close();
        return history;
    }

    public static void save(Context context, ArrayList<String> pathes) {
        SQLiteDatabase database = new HistoryDatabase(context).getWritableDatabase();
        for (String path : pathes) {
            ContentValues values = new ContentValues();
            values.put("path", path);
            values.put("last_selected", TimeUtils.unixtime());
            database.delete(SELECTED_HISTORY_TABLE, "path = \"" + path + "\"", null);

            database.insert(SELECTED_HISTORY_TABLE, null, values);
        }
        database.close();
    }


}