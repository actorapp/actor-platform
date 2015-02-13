package im.actor.messenger.core.actors.contacts;

import android.database.ContentObserver;
import android.provider.ContactsContract;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.engine.persistence.PersistenceLongSet;
import com.droidkit.engine.persistence.PersistenceSet;
import com.droidkit.engine.persistence.SerializableMap;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import org.apache.http.conn.scheme.Scheme;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.api.scheme.EmailToImport;
import im.actor.api.scheme.Group;
import im.actor.api.scheme.PhoneToImport;
import im.actor.api.scheme.User;
import im.actor.api.scheme.rpc.ResponseImportContacts;
import im.actor.api.scheme.updates.UpdateContactsAdded;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.Core;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.contacts.book.PhoneBookEmail;
import im.actor.messenger.core.actors.contacts.book.PhoneBookLoader;
import im.actor.messenger.core.actors.contacts.book.PhoneBookPhone;
import im.actor.messenger.core.actors.contacts.book.PhoneBookRecord;
import im.actor.messenger.model.ProfileSyncState;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.util.Logger;
import im.actor.messenger.util.country.CountryUtil;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 31.10.14.
 */
public class BookImportActor extends Actor {
    public static ActorSelection contactsImport() {
        return new ActorSelection(Props.create(BookImportActor.class).changeDispatcher("contacts"), "contacts/import");
    }

    private static final String TAG = "ContactsImportActor";
    private static final int MAX_IMPORT_SIZE = 50;

    private String isoCountry;

    private PersistenceLongSet importedPhones;
    private PersistenceSet<String> importedEmails;

    private HashSet<Long> importingPhones = new HashSet<Long>();
    private HashSet<String> importingEmails = new HashSet<String>();

    private ContentObserver contentObserver = new ContentObserver(null) {
        @Override
        public void onChange(final boolean selfChange) {
            self().sendOnce(new PerformSync());
        }
    };

    @Override
    public void preStart() {
        isoCountry = CountryUtil.getDeviceCountry(AppContext.getContext());

        importedPhones = new PersistenceLongSet(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()),
                "imported_phones"));
        importedEmails = new PersistenceSet<String>(new SerializableMap<String>(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()),
                "imported_emails")));
    }

    @Override
    public void onReceive(Object message) {
        if (!Core.isLoggedIn()) {
            return;
        }
        if (message instanceof StartSync) {
            AppContext.getContext()
                    .getContentResolver()
                    .registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contentObserver);
            self().send(new PerformSync());
        } else if (message instanceof PerformSync) {
            checkBook();
        }
    }

    private void checkBook() {
        ArrayList<PhoneBookRecord> phoneContacts = PhoneBookLoader.loadPhoneBook(AppContext.getContext(), isoCountry);
        ArrayList<PhoneToImport> phoneToImports = new ArrayList<PhoneToImport>();
        ArrayList<EmailToImport> emailToImports = new ArrayList<EmailToImport>();
        for (PhoneBookRecord record : phoneContacts) {
            for (PhoneBookPhone phone : record.getPhones()) {
                if (importedPhones.contains(phone.getNumber())) {
                    continue;
                }
                if (importingPhones.contains(phone.getNumber())) {
                    continue;
                }
                importingPhones.add(phone.getNumber());
                phoneToImports.add(new PhoneToImport(phone.getNumber(), record.getName()));
            }

            for (PhoneBookEmail email : record.getEmails()) {
                if (importedEmails.contains(email.getEmail())) {
                    continue;
                }
                if (importingEmails.contains(email.getEmail())) {
                    continue;
                }
                importingEmails.add(email.getEmail());
                emailToImports.add(new EmailToImport(email.getEmail(), record.getName()));
            }
        }

        Logger.d(TAG, "Importing contacts: " + (phoneToImports.size() + emailToImports.size()));

        ArrayList<PhoneToImport> phoneToImportsPart = new ArrayList<PhoneToImport>();
        ArrayList<EmailToImport> emailToImportsPart = new ArrayList<EmailToImport>();
        int count = 0;
        for (PhoneToImport phoneToImport : phoneToImports) {
            phoneToImportsPart.add(phoneToImport);
            count++;
            if (count >= MAX_IMPORT_SIZE) {
                performImport(phoneToImportsPart, emailToImportsPart);
                phoneToImportsPart.clear();
                emailToImportsPart.clear();
                count = 0;
            }
        }

        for (EmailToImport emailToImport : emailToImports) {
            emailToImportsPart.add(emailToImport);
            count++;
            if (count >= MAX_IMPORT_SIZE) {
                performImport(phoneToImportsPart, emailToImportsPart);
                phoneToImportsPart.clear();
                emailToImportsPart.clear();
                count = 0;
            }
        }

        if (count > 0) {
            performImport(phoneToImportsPart, emailToImportsPart);
        }

        checkImport();
    }

    private void performImport(ArrayList<PhoneToImport> phoneToImportsPart,
                               ArrayList<EmailToImport> emailToImportsPart) {
        final PhoneToImport[] phones = phoneToImportsPart.toArray(new PhoneToImport[0]);
        final EmailToImport[] emailToImports = emailToImportsPart.toArray(new EmailToImport[0]);

        ask(requests().importContacts((java.util.List<PhoneToImport>) phoneToImportsPart.clone(),
                (java.util.List<EmailToImport>) emailToImportsPart.clone()), new FutureCallback<ResponseImportContacts>() {
            @Override
            public void onResult(ResponseImportContacts result) {

                for (PhoneToImport phoneToImport : phones) {
                    importedPhones.add(phoneToImport.getPhoneNumber());
                    importingPhones.remove(phoneToImport.getPhoneNumber());
                }
                for (EmailToImport emailToImport : emailToImports) {
                    importedEmails.add(emailToImport.getEmail());
                    importingEmails.remove(emailToImport.getEmail());
                }

                checkImport();

                if (result.getUsers().size() == 0) {
                    Logger.d(TAG, "Import success: empty");
                    return;
                }

                ProfileSyncState.onContactsNotEmpty();

                Logger.d(TAG, "Import success: " + result.getUsers().size());
                ArrayList<Integer> uids = new ArrayList<Integer>();
                for (User u : result.getUsers()) {
                    uids.add(u.getId());
                }
                SequenceActor.SeqFatUpdate seqFatUpdate = new SequenceActor.SeqFatUpdate(
                        result.getSeq(), result.getState(),
                        new UpdateContactsAdded(uids), result.getUsers(),
                        new ArrayList<Group>());
                system().actorOf(SequenceActor.sequence()).send(seqFatUpdate);
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.d(TAG, "Import failure");
                throwable.printStackTrace();
            }
        });
    }

    private void checkImport() {
        if (importingEmails.size() == 0 && importingPhones.size() == 0) {
            ProfileSyncState.onImportEnded();
        }
    }

    public static class StartSync {

    }

    public static class PerformSync {

    }
}