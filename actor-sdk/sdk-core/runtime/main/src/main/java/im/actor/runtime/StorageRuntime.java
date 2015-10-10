/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListStorage;
import im.actor.runtime.storage.PreferencesStorage;

/**
 * Provider for data storage.
 * Provider is separated to two parts: Storage and Engines.
 * Storage are simple interfaces for storing untyped data in some persistent offline storage.
 * Engines are interfaces for working with storage that contains various implementations for different
 * platforms and provide clever caching and multithread support. Engines are also has methods for getting
 * data for UI.
 */
public interface StorageRuntime {

    /**
     * Creating main preferences storage. Called only once.
     *
     * @return the PreferencesStorage
     */
    @ObjectiveCName("createPreferencesStorage")
    PreferencesStorage createPreferencesStorage();

    /**
     * Creating index storage. Called only once for each index.
     *
     * @param name name of index engine
     * @return the IndexStorage
     */
    @ObjectiveCName("createIndexWithName:")
    IndexStorage createIndex(String name);

    /**
     * Creating key value storage. Called only once for each storage.
     * Preferred lazy initialization in implementation of KeyValueStorage.
     *
     * @param name name of storage
     * @return the KeyValueStorage
     */
    @ObjectiveCName("createKeyValueWithName:")
    KeyValueStorage createKeyValue(String name);

    /**
     * Creating list storage. Called only once for each storage.
     * Preferred lazy initialization in implementation of ListStorage.
     *
     * @param name name of list storage
     * @return the ListStorage
     */
    @ObjectiveCName("createListWithName:")
    ListStorage createList(String name);

    /**
     * Reset storage
     */
    @ObjectiveCName("resetStorage")
    void resetStorage();
}