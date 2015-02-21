package im.actor.gwt.app.storage;

import com.google.gwt.storage.client.Storage;

import java.util.List;

import im.actor.model.storage.KeyValueRecord;
import im.actor.model.storage.KeyValueStorage;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsKeyValue implements KeyValueStorage {

    private Storage storage;
    private String prefix;

    public JsKeyValue(Storage storage, String prefix) {
        this.storage = storage;
        this.prefix = prefix;
    }

    @Override
    public void addOrUpdateItem(long id, byte[] data) {

    }

    @Override
    public void addOrUpdateItems(List<KeyValueRecord> values) {

    }

    @Override
    public void removeItem(long id) {

    }

    @Override
    public void removeItems(long[] ids) {

    }

    @Override
    public void clear() {

    }

    @Override
    public byte[] getValue(long id) {
        return new byte[0];
    }
}
