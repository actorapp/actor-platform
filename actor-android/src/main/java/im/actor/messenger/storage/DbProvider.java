package im.actor.messenger.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.droidkit.engine._internal.sqlite.DbHelper;

public class DbProvider {

    public static final String DB_NAME = "SECRET_DB";

    private static volatile SQLiteDatabase db;

    public synchronized static SQLiteDatabase getDatabase(final Context context) {
        if (db == null || !db.isOpen()) {
            DbHelper helper = new DbHelper(context.getApplicationContext(), DB_NAME, null);
            db = helper.getWritableDatabase();
        }
        return db;
    }

}
