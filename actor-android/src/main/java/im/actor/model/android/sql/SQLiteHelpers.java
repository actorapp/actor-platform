package im.actor.model.android.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class SQLiteHelpers {
    public static boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }
}
