package im.actor.console;

import org.mapdb.DB;

import java.util.Base64;
import java.util.List;

import im.actor.model.droidkit.engine.KeyValueRecord;
import im.actor.model.droidkit.engine.KeyValueStorage;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class MapDbKeyValueStorage implements KeyValueStorage {

    private DB db;

    public MapDbKeyValueStorage(DB db) {
        this.db = db;
    }

    @Override
    public void addOrUpdateItem(long id, byte[] data) {
        String key = "itm_" + id;

        if (db.exists(key)) {
            db.getAtomicString(key).set(Base64.getEncoder().encodeToString(data));
        } else {
            db.createAtomicString(key, Base64.getEncoder().encodeToString(data));
        }
        db.commit();
    }

    @Override
    public void addOrUpdateItems(List<KeyValueRecord> values) {
        for (KeyValueRecord record : values) {
            addOrUpdateItem(record.getId(), record.getData());
        }
    }

    @Override
    public void removeItem(long id) {
        db.delete("itm_" + id);
    }

    @Override
    public void removeItems(long[] ids) {
        for (long l : ids) {
            removeItem(l);
        }
    }

    @Override
    public void clear() {
        // ??
    }

    @Override
    public byte[] getValue(long id) {
        String key = "itm_" + id;
        if (db.exists(key)) {
            return Base64.getDecoder().decode(db.getAtomicString(key).get());
        }
        return null;
    }

}
