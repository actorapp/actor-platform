package im.actor.model.modules.updates;

import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.contacts.ContactsSyncActor;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class ContactsProcessor extends BaseModule {
    public ContactsProcessor(Modules modules) {
        super(modules);
    }

    public void onContactsAdded(int[] uid) {
        modules().getContactsModule().getContactSyncActor()
                .send(new ContactsSyncActor.ContactsAdded(uid));
    }

    public void onContactsRemoved(int[] uid) {
        modules().getContactsModule().getContactSyncActor()
                .send(new ContactsSyncActor.ContactsRemoved(uid));
    }
}
