package im.actor.model.droidkit.engine;

import java.util.List;

import im.actor.model.droidkit.bser.BserObject;

/**
 * Created by ex3ndr on 14.03.15.
 */
public interface ListEngine<T extends BserObject & ListEngineItem> {

    // Write

    public void addOrUpdateItem(T item);

    public void addOrUpdateItems(List<T> items);

    public void replaceItems(List<T> items);

    public void removeItem(long key);

    public void removeItems(long[] keys);

    public void clear();

    // Read

    public T getValue(long key);

    public T getHeadValue();

    public boolean isEmpty();

    public int getCount();
}