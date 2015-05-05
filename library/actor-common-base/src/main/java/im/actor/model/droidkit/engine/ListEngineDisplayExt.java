/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import im.actor.model.droidkit.bser.BserObject;

public interface ListEngineDisplayExt<T extends BserObject & ListEngineItem> extends ListEngine<T> {

    // Listeners

    void subscribe(ListEngineDisplayListener<T> listener);

    void unsubscribe(ListEngineDisplayListener<T> listener);

    // Load top

    void loadForward(int limit, ListEngineDisplayLoadCallback<T> callback);

    void loadForward(long afterSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    void loadForward(String query, int limit, ListEngineDisplayLoadCallback<T> callback);

    void loadForward(String query, long afterSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    // Load bottom

    void loadBackward(int limit, ListEngineDisplayLoadCallback<T> callback);

    void loadBackward(long beforeSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    void loadBackward(String query, int limit, ListEngineDisplayLoadCallback<T> callback);

    void loadBackward(String query, long beforeSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    // Load center

    void loadCenter(long centerSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);
}
