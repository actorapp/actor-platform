package im.actor.messenger.storage.adapters;

import com.droidkit.bser.Bser;
import com.droidkit.bser.BserObject;
import com.droidkit.engine.list.DataAdapter;
import com.droidkit.engine.list.ListItemIdentity;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class ListBserAdapter<T extends BserObject & ListItemIdentity> implements DataAdapter<T> {

    private Class<T> clazz;

    public ListBserAdapter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public long getId(T value) {
        return value.getListId();
    }

    @Override
    public long getSortKey(T value) {
        return value.getListSortKey();
    }

    @Override
    public byte[] serialize(T entity) {
        return entity.toByteArray();
    }

    @Override
    public T deserialize(byte[] item) {
        try {
            return Bser.parse(clazz, item);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
