package im.actor.model.mem;

import im.actor.model.StorageProvider;
import im.actor.model.droidkit.engine.IndexStorage;
import im.actor.model.droidkit.engine.KeyValueStorage;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.droidkit.engine.PreferencesStorage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.SearchEntity;
import im.actor.model.mem.storage.MemoryIndexStorage;
import im.actor.model.mem.storage.MemoryKeyValueStorage;
import im.actor.model.mem.storage.MemoryListEngine;
import im.actor.model.mem.storage.MemoryListStorage;
import im.actor.model.mem.storage.MemoryPreferencesStorage;

public class MemoryStorageProvider implements StorageProvider {

    @Override
    public PreferencesStorage createPreferencesStorage() {
        return new MemoryPreferencesStorage();
    }

    @Override
    public IndexStorage createIndex(String name) {
        return new MemoryIndexStorage();
    }

    @Override
    public KeyValueStorage createKeyValue(String name) {
        return new MemoryKeyValueStorage();
    }

    @Override
    public ListStorage createList(String name) {
        return new MemoryListStorage();
    }

    @Override
    public ListEngine<SearchEntity> createSearchList(ListStorage storage) {
        return new MemoryListEngine<SearchEntity>((MemoryListStorage) storage, SearchEntity.CREATOR);
    }

    @Override
    public ListEngine<Contact> createContactsList(ListStorage storage) {
        return new MemoryListEngine<Contact>((MemoryListStorage) storage, Contact.CREATOR);
    }

    @Override
    public ListEngine<Dialog> createDialogsList(ListStorage storage) {
        return new MemoryListEngine<Dialog>((MemoryListStorage) storage, Dialog.CREATOR);
    }

    @Override
    public ListEngine<Message> createMessagesList(Peer peer, ListStorage storage) {
        return new MemoryListEngine<Message>((MemoryListStorage) storage, Message.CREATOR);
    }

    @Override
    public void resetStorage() {
        // TODO: Implement
    }
}
