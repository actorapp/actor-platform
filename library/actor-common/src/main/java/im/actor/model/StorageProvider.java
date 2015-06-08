/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.model.droidkit.engine.KeyValueStorage;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.entity.SearchEntity;

/**
 * Provider for data storage.
 * Provider is separated to two parts: Storage and Engines.
 * Storage are simple interfaces for storing untyped data in some persistent offline storage.
 * Engines are interfaces for working with storage that contains various implementations for different
 * platforms and provide clever caching and multithread support. Engines are also has methods for getting
 * data for UI.
 */
public interface StorageProvider {

    /**
     * Creating main preferences storage. Called only once.
     *
     * @return the PreferencesStorage
     */
    @ObjectiveCName("createPreferencesStorage")
    PreferencesStorage createPreferencesStorage();

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
     * Creating search list. Search list is global object index list that can be searched.
     * Called only once.
     *
     * @param storage list storage
     * @return the ListEngine
     */
    @ObjectiveCName("createSearchListWithStorage:")
    ListEngine<SearchEntity> createSearchList(ListStorage storage);

    /**
     * Creating of ordered contacts list engine.
     * Called only once.
     *
     * @param storage list storage
     * @return the ListEngine
     */
    @ObjectiveCName("createContactsListWithStorage:")
    ListEngine<Contact> createContactsList(ListStorage storage);

    /**
     * Creating of recent dialogs list engine.
     * Called only once.
     *
     * @param storage list storage
     * @return the ListEngine
     */
    @ObjectiveCName("createDialogsListWithStorage:")
    ListEngine<Dialog> createDialogsList(ListStorage storage);

    /**
     * Creating of conversation's list engine
     * Called only once for each peer.
     *
     * @param peer    peer of conversation
     * @param storage list storage
     * @return the ListEngine
     */
    @ObjectiveCName("createMessagesListWithPeer:withStorage:")
    ListEngine<Message> createMessagesList(Peer peer, ListStorage storage);

    /**
     * Reset storage
     */
    @ObjectiveCName("resetStorage")
    void resetStorage();
}