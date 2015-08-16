/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

public interface KeyValueEngine<V extends KeyValueItem> {
    @ObjectiveCName("addOrUpdateItem:")
    void addOrUpdateItem(V item);

    @ObjectiveCName("addOrUpdateItems:")
    void addOrUpdateItems(List<V> values);

    @ObjectiveCName("removeItemWithKey:")
    void removeItem(long key);

    @ObjectiveCName("removeItemsWithKeys:")
    void removeItems(long[] keys);

    @ObjectiveCName("clear")
    void clear();

    @ObjectiveCName("getValueWithKey:")
    V getValue(long key);
}