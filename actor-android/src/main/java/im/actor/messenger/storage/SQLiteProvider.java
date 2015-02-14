package im.actor.messenger.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import im.actor.messenger.core.AppContext;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class SQLiteProvider {
    private static final String DB = "ACTOR";
    private static SQLiteDatabase sqLiteDatabase;

    public static synchronized SQLiteDatabase db() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            NoOpOpenHelper helper = new NoOpOpenHelper(AppContext.getContext(), DB);
            sqLiteDatabase = helper.getWritableDatabase();
        }
        return sqLiteDatabase;
    }

    private static class NoOpOpenHelper extends SQLiteOpenHelper {

        public NoOpOpenHelper(Context context, String name) {
            super(context, name, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
