/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.contacts.ContactsSyncActor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.annotations.Verified;

@Verified
public class ContactsProcessor extends AbsModule {

    private ActorRef contactsSyncActor;

    @Verified
    public ContactsProcessor(ModuleContext context) {
        super(context);
        contactsSyncActor = context().getContactsModule().getContactSyncActor();
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
