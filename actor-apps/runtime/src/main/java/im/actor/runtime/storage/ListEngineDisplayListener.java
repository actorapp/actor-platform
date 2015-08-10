/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

public interface ListEngineDisplayListener<T> {

    @ObjectiveCName("onItemRemovedWithKey:")
    void onItemRemoved(long key);

    @ObjectiveCName("onItemsRemovedWithKeys:")
    void onItemsRemoved(long[] keys);

    @ObjectiveCName("addOrUpdate:")
    void addOrUpdate(T item);

    @ObjectiveCName("addOrUpdateWithList:")
    void addOrUpdate(List<T> items);

    @ObjectiveCName("onItemsReplacedWithList:")
    void onItemsReplaced(List<T> items);

    @ObjectiveCName("onListClear")
    void onListClear();
}
