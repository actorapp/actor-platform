package im.actor.model;

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
    public PreferencesStorage createPreferencesStorage();

    /**
     * Creating key value storage. Called only once for each storage.
     * Preferred lazy initialization in implementation of KeyValueStorage.
     *
     * @param name name of storage
     * @return the KeyValueStorage
     */
    public KeyValueStorage createKeyValue(String name);

    /**
     * Creating list storage. Called only once for each storage.
     * Preferred lazy initialization in implementation of ListStorage.
     *
     * @param name name of list storage
     * @return the ListStorage
     */
    public ListStorage createList(String name);


    /**
     * Creating search list. Search list is global object index list that can be searched.
     * Called only once.
     *
     * @param storage list storage
     * @return the ListEngine
     */
    public ListEngine<SearchEntity> createSearchList(ListStorage storage);

    /**
     * Creating of ordered contacts list engine.
     * Called only once.
     *
     * @param storage list storage
     * @return the ListEngine
     */
    public ListEngine<Contact> createContactsList(ListStorage storage);

    /**
     * Creating of recent dialogs list engine.
     * Called only once.
     *
     * @param storage list storage
     * @return the ListEngine
     */
    public ListEngine<Dialog> createDialogsList(ListStorage storage);

    /**
     * Creating of conversation's list engine
     * Called only once for each peer.
     *
     * @param peer    peer of conversation
     * @param storage list storage
     * @return the ListEngine
     */
    public ListEngine<Message> createMessagesList(Peer peer, ListStorage storage);
}