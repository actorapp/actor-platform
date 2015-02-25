package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.Contact;
import im.actor.model.modules.contacts.BookImportActor;
import im.actor.model.modules.contacts.ContactsSyncActor;
import im.actor.model.storage.ListEngine;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class Contacts extends BaseModule {

    private ListEngine<Contact> contacts;
    private ActorRef bookImportActor;
    private ActorRef contactSyncActor;

    public Contacts(final Modules modules) {
        super(modules);
        contacts = modules.getConfiguration().getStorage().createContactsEngine();
        bookImportActor = system().actorOf(Props.create(BookImportActor.class, new ActorCreator<BookImportActor>() {
            @Override
            public BookImportActor create() {
                return new BookImportActor(modules);
            }
        }), "actor/book_import");
        contactSyncActor = system().actorOf(Props.create(ContactsSyncActor.class, new ActorCreator<ContactsSyncActor>() {
            @Override
            public ContactsSyncActor create() {
                return new ContactsSyncActor(modules);
            }
        }), "actor/contacts_sync");
    }

    public ListEngine<Contact> getContacts() {
        return contacts;
    }

    public void onPhoneBookChanged() {
        bookImportActor.send(new BookImportActor.PerformSync());
    }

    public ActorRef getContactSyncActor() {
        return contactSyncActor;
    }
}
