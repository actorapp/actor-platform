package im.actor.gwt.app.storage;

import com.google.gwt.storage.client.Storage;

import java.util.List;

import im.actor.gwt.app.base64.Base64Utils;
import im.actor.model.storage.KeyValueRecord;
import im.actor.model.storage.KeyValueStorage;

import static im.actor.gwt.app.base64.Base64Utils.toBase64;

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

    private String getId(long id) {
        return prefix + "_" + id;
    }

    @Override
    public void addOrUpdateItem(long id, byte[] data) {
        storage.setItem(getId(id), toBase64(data));
    }

    @Override
    public void addOrUpdateItems(List<KeyValueRecord> values) {
        for (KeyValueRecord r : values) {
            addOrUpdateItem(r.getId(), r.getData());
        }
    }

    @Override
    public void removeItem(long id) {
        storage.removeItem(getId(id));
    }

    @Override
    public void removeItems(long[] ids) {
        for (long l : ids) {
            storage.removeItem(getId(l));
        }
    }

    @Override
    public void clear() {
        // TODO: Implement
    }

    @Override
    public byte[] getValue(long id) {
        String res = storage.getItem(getId(id));
        if (res == null) {
            return null;
        } else {
            return Base64Utils.fromBase64(res);
        }
    }
}
