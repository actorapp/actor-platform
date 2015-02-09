package im.actor.console;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import im.actor.model.storage.PreferencesStorage;
import org.mapdb.DB;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class MapDbPreferences implements PreferencesStorage {
    private DB db;

    public MapDbPreferences(DB db) {
        this.db = db;
    }

    @Override
    public void putLong(String key, long v) {
        if (db.exists(key)) {
            db.getAtomicLong(key).set(v);
        } else {
            db.createAtomicLong(key, v);
        }
        db.commit();
    }

    @Override
    public long getLong(String key, long def) {
        if (db.exists(key)) {
            return db.getAtomicLong(key).get();
        } else {
            return def;
        }
    }

    @Override
    public void putInt(String key, int v) {
        if (db.exists(key)) {
            db.getAtomicInteger(key).set(v);
        } else {
            db.createAtomicInteger(key, v);
        }
        db.commit();
    }

    @Override
    public int getInt(String key, int def) {
        if (db.exists(key)) {
            return db.getAtomicInteger(key).get();
        } else {
            return def;
        }
    }

    @Override
    public void putBool(String key, boolean v) {
        if (db.exists(key)) {
            db.getAtomicBoolean(key).set(v);
        } else {
            db.createAtomicBoolean(key, v);
        }
        db.commit();
    }

    @Override
    public boolean getBool(String key, boolean def) {
        if (db.exists(key)) {
            return db.getAtomicBoolean(key).get();
        } else {
            return def;
        }
    }

    @Override
    public void putBytes(String key, byte[] v) {
        if (db.exists(key)) {
            db.getAtomicString(key).set(java.util.Base64.getEncoder().encodeToString(v));
        } else {
            db.createAtomicString(key, java.util.Base64.getEncoder().encodeToString(v));
        }
        db.commit();
    }

    @Override
    public byte[] getBytes(String key) {
        if (db.exists(key)) {
            return java.util.Base64.getDecoder().decode(db.getAtomicString(key).get());
        } else {
            return null;
        }
    }

    @Override
    public void putString(String key, String v) {
        if (db.exists(key)) {
            db.getAtomicString(key).set(v);
        } else {
            db.createAtomicString(key, v);
        }
        db.commit();
    }

    @Override
    public String getString(String key) {
        if (db.exists(key)) {
            return db.getAtomicString(key).get();
        } else {
            return null;
        }
    }
}
