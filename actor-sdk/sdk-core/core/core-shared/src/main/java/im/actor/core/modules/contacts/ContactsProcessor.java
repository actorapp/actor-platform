/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.contacts;

import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.api.updates.UpdateContactsRemoved;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class ContactsProcessor implements SequenceProcessor {

    private ModuleContext context;

    public ContactsProcessor(ModuleContext context) {
        this.context = context;
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateContactsAdded) {
            UpdateContactsAdded contactsAdded = (UpdateContactsAdded) update;
            int[] uids = new int[contactsAdded.getUids().size()];
            for (int i = 0; i < uids.length; i++) {
                uids[i] = contactsAdded.getUids().get(i);
            }
            context.getContactsModule().getContactSyncActor()
                    .send(new ContactsSyncActor.ContactsAdded(uids));
            return Promise.success(null);
        } else if (update instanceof UpdateContactsRemoved) {
            UpdateContactsRemoved contactsRemoved = (UpdateContactsRemoved) update;
            int[] uids = new int[contactsRemoved.getUids().size()];
            for (int i = 0; i < uids.length; i++) {
                uids[i] = contactsRemoved.getUids().get(i);
            }
            context.getContactsModule().getContactSyncActor()
                    .send(new ContactsSyncActor.ContactsRemoved(uids));
            return Promise.success(null);
        }
        return null;
    }
}
