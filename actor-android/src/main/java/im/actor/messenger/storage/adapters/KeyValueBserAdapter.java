package im.actor.messenger.storage.adapters;

import com.droidkit.bser.Bser;
import com.droidkit.bser.BserObject;
import com.droidkit.engine.keyvalue.DataAdapter;
import com.droidkit.engine.keyvalue.KeyValueIdentity;

import java.io.IOException;

/**
 * Created by ex3ndr on 22.10.14.
 */
public class KeyValueBserAdapter<T extends BserObject & KeyValueIdentity> implements DataAdapter<T> {

    private Class<T> tClass;

    public KeyValueBserAdapter(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public long getId(T value) {
        return value.getKeyValueId();
    }

    @Override
    public byte[] serialize(T entity) {
        return entity.toByteArray();
    }

    @Override
    public T deserialize(byte[] item) {
        try {
            return Bser.parse(tClass, item);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
