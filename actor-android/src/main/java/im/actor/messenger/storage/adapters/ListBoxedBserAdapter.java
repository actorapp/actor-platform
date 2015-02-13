package im.actor.messenger.storage.adapters;

import com.droidkit.bser.Bser;
import com.droidkit.bser.BserObject;
import com.droidkit.engine.list.DataAdapter;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ex3ndr on 15.09.14.
 */
public abstract class ListBoxedBserAdapter<B, R extends BserObject> implements DataAdapter<B> {

    private HashMap<Long, B> cache = new HashMap<Long, B>();

    private Class<R> tClass;

    public ListBoxedBserAdapter(Class<R> tClass) {
        this.tClass = tClass;
    }

    @Override
    public abstract long getId(B value);

    protected abstract long getRawId(R raw);

    protected byte[] serializeRaw(R obj) {
        return obj.toByteArray();
    }

    protected R deserializeRaw(byte[] bytes) {
        try {
            return Bser.parse(tClass, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract R convertToRaw(B raw);

    protected abstract B convertToObj(R raw);

    protected abstract void updateObject(B obj, R value);


    @Override
    public synchronized byte[] serialize(B entity) {
        long key = getId(entity);
        if (!cache.containsKey(key)) {
            cache.put(key, entity);
        }
        return serializeRaw(convertToRaw(entity));
    }

    @Override
    public synchronized B deserialize(byte[] item) {
        R r = deserializeRaw(item);
        if (r != null) {
            long key = getRawId(r);
            if (cache.containsKey(key)) {
                B res = cache.get(key);
                updateObject(res, r);
                return res;
            } else {
                B res = convertToObj(r);
                cache.put(key, res);
                return res;
            }
        } else {
            return null;
        }
    }
}
