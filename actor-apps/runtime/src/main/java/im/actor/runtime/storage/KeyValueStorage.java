/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

public interface KeyValueStorage {
    @ObjectiveCName("addOrUpdateItemWithKey:withData:")
    void addOrUpdateItem(long key, byte[] data);

    @ObjectiveCName("addOrUpdateItems:")
    void addOrUpdateItems(List<KeyValueRecord> values);

    @ObjectiveCName("removeItemWithKey:")
    void removeItem(long key);

    @ObjectiveCName("removeItemsWithKeys:")
    void removeItems(long[] keys);

    @ObjectiveCName("clear")
    void clear();

    @ObjectiveCName("getValueWithKey:")
    byte[] getValue(long key);
}
