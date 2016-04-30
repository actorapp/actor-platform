/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public interface KeyValueStorage {

    /**
     * Adding new or replacing old record
     *
     * @param key  record key
     * @param data record raw data
     */
    @ObjectiveCName("addOrUpdateItemWithKey:withData:")
    void addOrUpdateItem(long key, byte[] data);

    /**
     * Adding new or replacing old records
     *
     * @param values records
     */
    @ObjectiveCName("addOrUpdateItems:")
    void addOrUpdateItems(List<KeyValueRecord> values);

    /**
     * Removing record by key
     *
     * @param key record key
     */
    @ObjectiveCName("removeItemWithKey:")
    void removeItem(long key);

    /**
     * Removing records by keys
     *
     * @param keys record keys
     */
    @ObjectiveCName("removeItemsWithKeys:")
    void removeItems(long[] keys);

    /**
     * Loading item by key
     *
     * @param key record key
     * @return result or null if not found
     */
    @ObjectiveCName("loadItemWithKey:")
    byte[] loadItem(long key);

    /**
     * Loading items by keys
     *
     * @param keys record keys
     * @return all loaded items
     */
    @ObjectiveCName("loadItems:")
    List<KeyValueRecord> loadItems(long[] keys);

    /**
     * Loading all items from storage
     *
     * @return all items
     */
    @ObjectiveCName("loadAllItems")
    List<KeyValueRecord> loadAllItems();

    /**
     * Clearing storage
     */
    @ObjectiveCName("clear")
    void clear();
}
