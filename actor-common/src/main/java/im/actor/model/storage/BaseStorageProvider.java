package im.actor.model.storage;

import im.actor.model.StorageProvider;
import im.actor.model.droidkit.engine.AsyncListEngine;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 15.03.15.
 */
public abstract class BaseStorageProvider implements StorageProvider {

    @Override
    public ListEngine<Contact> createContactsList(ListStorage storage) {
        return new AsyncListEngine<Contact>(storage, Contact.CREATOR);
    }

    @Override
    public ListEngine<Dialog> createDialogsList(ListStorage storage) {
        return new AsyncListEngine<Dialog>(storage, Dialog.CREATOR);
    }

    @Override
    public ListEngine<Message> createMessagesList(Peer peer, ListStorage storage) {
        return new AsyncListEngine<Message>(storage, Message.CREATOR);
    }
}
