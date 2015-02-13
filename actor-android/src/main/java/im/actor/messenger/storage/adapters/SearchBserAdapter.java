package im.actor.messenger.storage.adapters;

import com.droidkit.bser.Bser;
import com.droidkit.bser.BserObject;
import com.droidkit.engine.search.DataAdapter;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class SearchBserAdapter<T extends BserObject> implements DataAdapter<T> {
    private Class<T> clazz;

    public SearchBserAdapter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(byte[] data) {
        try {
            return Bser.parse(clazz, data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] serialize(T data) {
        return data.toByteArray();
    }
}
