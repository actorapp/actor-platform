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

    @Verified
    public ContactsProcessor(ModuleContext context) {
        super(context);
    }

    @Verified
    public void onContactsAdded(int[] uid) {
        context().getContactsModule().getContactSyncActor().send(new ContactsSyncActor.ContactsAdded(uid));
    }

    @Verified
    public void onContactsRemoved(int[] uid) {
        context().getContactsModule().getContactSyncActor().send(new ContactsSyncActor.ContactsRemoved(uid));
    }
}
