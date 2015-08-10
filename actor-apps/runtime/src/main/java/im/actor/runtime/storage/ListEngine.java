/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

import im.actor.runtime.bser.BserObject;

public interface ListEngine<T extends BserObject & ListEngineItem> {

    // Write

    @ObjectiveCName("addOrUpdateItem:")
    void addOrUpdateItem(T item);

    @ObjectiveCName("addOrUpdateItems:")
    void addOrUpdateItems(List<T> items);

    @ObjectiveCName("replaceItems:")
    void replaceItems(List<T> items);

    @ObjectiveCName("removeItemWithKey:")
    void removeItem(long key);

    @ObjectiveCName("removeItemsWithKeys:")
    void removeItems(long[] keys);

    @ObjectiveCName("clear")
    void clear();

    // Read

    @ObjectiveCName("getValueWithKey:")
    T getValue(long key);

    @ObjectiveCName("getHeadValue")
    T getHeadValue();

    @ObjectiveCName("isEmpty")
    boolean isEmpty();

    @ObjectiveCName("getCount")
    int getCount();
}