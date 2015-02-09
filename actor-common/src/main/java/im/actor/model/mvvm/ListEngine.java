package im.actor.model.mvvm;

import java.util.List;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface ListEngine<V> {
    public void addOrUpdateItem(V item);

    public void addOrUpdateItems(List<V> values);

    public void replaceItems(List<V> values);

    public void removeItem(long id);

    public void removeItems(long[] ids);

    public void clear();

    public V getValue(long id);

    public V getHeadValue();

    public void getCount();
}
