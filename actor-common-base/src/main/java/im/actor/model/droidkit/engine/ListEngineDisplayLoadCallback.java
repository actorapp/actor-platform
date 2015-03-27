package im.actor.model.droidkit.engine;

import java.util.List;

/**
 * Created by ex3ndr on 14.03.15.
 */
public interface ListEngineDisplayLoadCallback<T> {
    public void onLoaded(List<T> items, long topSortKey, long bottomSortKey);
}