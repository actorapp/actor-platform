/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import im.actor.runtime.annotations.Verified;
import im.actor.runtime.actors.ActorRef;
import im.actor.core.modules.BaseModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.contacts.ContactsSyncActor;

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
