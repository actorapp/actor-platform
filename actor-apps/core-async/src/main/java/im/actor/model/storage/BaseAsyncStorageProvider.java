/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.storage;

import im.actor.model.StorageProvider;
import im.actor.model.droidkit.engine.AsyncListEngine;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.droidkit.engine.ListStorageDisplayEx;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.SearchEntity;

public abstract class BaseAsyncStorageProvider implements StorageProvider {

    @Override
    public ListEngine<Contact> createContactsList(ListStorage storage) {
        if (!(storage instanceof ListStorageDisplayEx)) {
            throw new RuntimeException("Storage MUST implement ListStorageDisplayEx");
        }
        return new AsyncListEngine<Contact>((ListStorageDisplayEx) storage, Contact.CREATOR);
    }

    @Override
    public ListEngine<Dialog> createDialogsList(ListStorage storage) {
        if (!(storage instanceof ListStorageDisplayEx)) {
            throw new RuntimeException("Storage MUST implement ListStorageDisplayEx");
        }
        return new AsyncListEngine<Dialog>((ListStorageDisplayEx) storage, Dialog.CREATOR);
    }

    @Override
    public ListEngine<Message> createMessagesList(Peer peer, ListStorage storage) {
        if (!(storage instanceof ListStorageDisplayEx)) {
            throw new RuntimeException("Storage MUST implement ListStorageDisplayEx");
        }
        return new AsyncListEngine<Message>((ListStorageDisplayEx) storage, Message.CREATOR);
    }

    @Override
    public ListEngine<SearchEntity> createSearchList(ListStorage storage) {
        if (!(storage instanceof ListStorageDisplayEx)) {
            throw new RuntimeException("Storage MUST implement ListStorageDisplayEx");
        }
        return new AsyncListEngine<SearchEntity>((ListStorageDisplayEx) storage, SearchEntity.CREATOR);
    }
}
