package im.actor.model.droidkit.engine;

import java.util.List;

/**
 * Created by ex3ndr on 19.02.15.
 */
public interface KeyValueStorage {
    public void addOrUpdateItem(long id, byte[] data);

    public void addOrUpdateItems(List<KeyValueRecord> values);

    public void removeItem(long id);

    public void removeItems(long[] ids);

    public void clear();

    public byte[] getValue(long id);
}
