/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.bser.BserObject;

public interface ListEngineDisplayExt<T extends BserObject & ListEngineItem> extends ListEngine<T> {

    // Listeners

    @ObjectiveCName("subscribeWithListener:")
    void subscribe(ListEngineDisplayListener<T> listener);

    @ObjectiveCName("unsubscribeWithListener:")
    void unsubscribe(ListEngineDisplayListener<T> listener);

    // Load top

    @ObjectiveCName("loadForwardWithLimit:withCallback:")
    void loadForward(int limit, ListEngineDisplayLoadCallback<T> callback);

    @ObjectiveCName("loadForwardAfterSortKey:withLimit:withCallback:")
    void loadForward(long afterSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    @ObjectiveCName("loadForwardWithQuery:withLimit:withCallback:")
    void loadForward(String query, int limit, ListEngineDisplayLoadCallback<T> callback);

    @ObjectiveCName("loadForwardWithQuery:afterSortKey:withLimit:withCallback:")
    void loadForward(String query, long afterSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    // Load bottom

    @ObjectiveCName("loadBackwardWithLimit:withCallback:")
    void loadBackward(int limit, ListEngineDisplayLoadCallback<T> callback);

    @ObjectiveCName("loadBackwardBeforeSortKey:withLimit:withCallback:")
    void loadBackward(long beforeSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    @ObjectiveCName("loadBackwardWithQuery:withLimit:withCallback:")
    void loadBackward(String query, int limit, ListEngineDisplayLoadCallback<T> callback);

    @ObjectiveCName("loadBackwardWithQuery:beforeSortKey:withLimit:withCallback:")
    void loadBackward(String query, long beforeSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);

    // Load center

    @ObjectiveCName("loadCenterWithSortKey:withLimit:withCallback:")
    void loadCenter(long centerSortKey, int limit, ListEngineDisplayLoadCallback<T> callback);
}
