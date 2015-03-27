package im.actor.model.modules.updates.internal;

import im.actor.model.api.rpc.ResponseGetContacts;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class ContactsLoaded extends InternalUpdate {
    private ResponseGetContacts contacts;

    public ContactsLoaded(ResponseGetContacts contacts) {
        this.contacts = contacts;
    }

    public ResponseGetContacts getContacts() {
        return contacts;
    }
}
