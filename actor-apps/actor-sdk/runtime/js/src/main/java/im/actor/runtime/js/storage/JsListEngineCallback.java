/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.storage;

import java.util.List;

public interface JsListEngineCallback<T> {
    void onItemAddedOrUpdated(T item);

    void onItemsAddedOrUpdated(List<T> items);

    void onItemRemoved(long id);

    void onItemsRemoved(long[] ids);

    void onItemsReplaced(List<T> items);

    void onClear();
}