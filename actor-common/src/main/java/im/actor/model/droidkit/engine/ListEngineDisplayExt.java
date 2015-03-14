package im.actor.model.droidkit.engine;

import im.actor.model.droidkit.bser.BserObject;

/**
 * Created by ex3ndr on 14.03.15.
 */
public interface ListEngineDisplayExt<T extends BserObject & ListEngineItem> extends ListEngine<T> {

    // Listeners

    public void subscribe(ListEngineDisplayListener<T> listener);

    public void unsubscribe(ListEngineDisplayListener<T> listener);

    // Load top

    public void loadForward(int limit, ListEngineCallback<T> callback);

    public void loadForward(long afterSortKey, int limit, ListEngineCallback<T> callback);

    public void loadForward(String query, int limit, ListEngineCallback<T> callback);

    public void loadForward(String query, long afterSortKey, int limit, ListEngineCallback<T> callback);

    // Load bottom

    public void loadBackward(int limit, ListEngineCallback<T> callback);

    public void loadBackward(long beforeSortKey, int limit, ListEngineCallback<T> callback);

    public void loadBackward(String query, int limit, ListEngineCallback<T> callback);

    public void loadBackward(String query, long beforeSortKey, int limit, ListEngineCallback<T> callback);

    // Load center

    public void loadCenter(long centerSortKey, int limit, ListEngineCallback<T> callback);
}
