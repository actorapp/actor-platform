package im.actor.model.modules.updates;

import im.actor.model.annotation.Verified;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.contacts.ContactsSyncActor;

/**
 * Created by ex3ndr on 25.02.15.
 */
@Verified
public class ContactsProcessor extends BaseModule {

    private ActorRef contactsSyncActor;

    @Verified
    public ContactsProcessor(Modules modules) {
        super(modules);
        contactsSyncActor = modules().getContactsModule().getContactSyncActor();
    }

    @Verified
    public void onContactsAdded(int[] uid) {
        contactsSyncActor.send(new ContactsSyncActor.ContactsAdded(uid));
    }

    @Verified
    public void onContactsRemoved(int[] uid) {
        contactsSyncActor.send(new ContactsSyncActor.ContactsRemoved(uid));
    }
}
