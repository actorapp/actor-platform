/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.storage;

public interface JsListEngineCallback<T> {
    void onItemAddedOrUpdated(T item);

    void onItemRemoved(long id);

    void onClear();
}