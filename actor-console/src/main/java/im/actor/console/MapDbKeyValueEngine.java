package im.actor.console;

import im.actor.model.storage.KeyValueEngine;
import im.actor.model.storage.KeyValueItem;
import org.mapdb.DB;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class MapDbKeyValueEngine<V extends KeyValueItem> implements KeyValueEngine<V> {

    private DB db;
    private Serializer<V> serializer;

    public MapDbKeyValueEngine(DB db, Serializer<V> serializer) {
        this.db = db;
        this.serializer = serializer;
    }

    @Override
    public void addOrUpdateItem(V item) {
        String key = "itm_" + item.getEngineId();

        if (db.exists(key)) {
            db.getAtomicString(key).set(Base64.getEncoder().encodeToString(serializer.serialize(item)));
        } else {
            db.createAtomicString(key, Base64.getEncoder().encodeToString(serializer.serialize(item)));
        }
        db.commit();
    }

    @Override
    public void addOrUpdateItems(List<V> values) {
        for (V v : values) {
            addOrUpdateItem(v);
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
        // ???
    }

    @Override
    public List<V> getAll() {
        return new ArrayList<V>();
    }

    @Override
    public V getValue(long id) {
        String key = "itm_" + id;

        if (db.exists(key)) {
            byte[] data = Base64.getDecoder().decode(db.getAtomicString(key).get());
            return serializer.deserialize(data);
        }
        return null;
    }

    public interface Serializer<V> {
        public byte[] serialize(V v);

        public V deserialize(byte[] v);
    }
}
