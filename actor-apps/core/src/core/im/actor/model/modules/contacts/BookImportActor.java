/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.contacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.model.PhoneBookProvider;
import im.actor.model.api.EmailToImport;
import im.actor.model.api.Group;
import im.actor.model.api.PhoneToImport;
import im.actor.model.api.base.FatSeqUpdate;
import im.actor.model.api.rpc.RequestImportContacts;
import im.actor.model.api.rpc.ResponseImportContacts;
import im.actor.model.api.updates.UpdateContactsAdded;
import im.actor.model.entity.PhoneBookContact;
import im.actor.model.entity.PhoneBookEmail;
import im.actor.model.entity.PhoneBookPhone;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class BookImportActor extends ModuleActor {

    private static final String TAG = "ContactsImport";

    private final boolean ENABLE_LOG;

    private static final int MAX_IMPORT_SIZE = 50;

    private HashSet<Long> importingPhones = new HashSet<Long>();
    private HashSet<String> importingEmails = new HashSet<String>();

    private boolean isSyncInProgress = false;

    public BookImportActor(Modules messenger) {
        super(messenger);
        ENABLE_LOG = messenger.getConfiguration().isEnableContactsLogging();
    }

    @Override
    public void preStart() {
        super.preStart();
        self().send(new PerformSync());
    }

    private void performSync() {
        if (ENABLE_LOG) {
            Log.d(TAG, "Checking sync...");
        }
        if (isSyncInProgress) {
            if (ENABLE_LOG) {
                Log.d(TAG, "Sync already in progress");
            }
            return;
        }
        isSyncInProgress = true;
        if (ENABLE_LOG) {
            Log.d(TAG, "Starting book loading...");
        }

        modules().getConfiguration().getPhoneBookProvider()
                .loadPhoneBook(new PhoneBookProvider.Callback() {
                    @Override
                    public void onLoaded(List<PhoneBookContact> contacts) {
                        self().send(new PhoneBookLoaded(contacts));
                    }
                });
    }

    private void onPhoneBookLoaded(List<PhoneBookContact> phoneBook) {
        isSyncInProgress = false;
        if (ENABLE_LOG) {
            Log.d(TAG, "Book load completed");
        }

        ArrayList<PhoneToImport> phoneToImports = new ArrayList<PhoneToImport>();
        ArrayList<EmailToImport> emailToImports = new ArrayList<EmailToImport>();
        for (PhoneBookContact record : phoneBook) {
            for (PhoneBookPhone phone : record.getPhones()) {
                if (isImported(phone.getNumber())) {
                    continue;
                }
                if (importingPhones.contains(phone.getNumber())) {
                    continue;
                }
                importingPhones.add(phone.getNumber());
                phoneToImports.add(new PhoneToImport(phone.getNumber(), record.getName()));
            }

            for (PhoneBookEmail email : record.getEmails()) {
                if (isImported(email.getEmail().toLowerCase())) {
                    continue;
                }
                if (importingEmails.contains(email.getEmail().toLowerCase())) {
                    continue;
                }
                importingEmails.add(email.getEmail().toLowerCase());
                emailToImports.add(new EmailToImport(email.getEmail().toLowerCase(), record.getName()));
            }
        }

        if (phoneToImports.size() == 0 && emailToImports.size() == 0) {
            if (ENABLE_LOG) {
                Log.d(TAG, "No new contacts found");
            }
            markImported();
            return;
        } else {
            if (ENABLE_LOG) {
                Log.d(TAG, "Founded new " + (phoneToImports.size() + emailToImports.size()) + " contact records");
            }
        }

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
    }

    private void performImport(ArrayList<PhoneToImport> phoneToImportsPart,
                               ArrayList<EmailToImport> emailToImportsPart) {

        if (ENABLE_LOG) {
            Log.d(TAG, "Performing import part with " + phoneToImportsPart.size() +
                    " phones and " + emailToImportsPart.size() + " emails");
        }

        final PhoneToImport[] phones = phoneToImportsPart.toArray(new PhoneToImport[phoneToImportsPart.size()]);
        final EmailToImport[] emailToImports = emailToImportsPart.toArray(new EmailToImport[emailToImportsPart.size()]);

        request(new RequestImportContacts((java.util.List<PhoneToImport>) phoneToImportsPart.clone(),
                (java.util.List<EmailToImport>) emailToImportsPart.clone()), new RpcCallback<ResponseImportContacts>() {
            @Override
            public void onResult(ResponseImportContacts response) {
                for (PhoneToImport phoneToImport : phones) {
                    markImported(phoneToImport.getPhoneNumber());
                    importingPhones.remove(phoneToImport.getPhoneNumber());
                }
                for (EmailToImport emailToImport : emailToImports) {
                    markImported(emailToImport.getEmail());
                    importingEmails.remove(emailToImport.getEmail());
                }

                if (importingEmails.size() == 0 && importingPhones.size() == 0) {
                    markImported();
                }

                if (response.getUsers().size() == 0) {
                    if (ENABLE_LOG) {
                        Log.d(TAG, "Import success, but no new contacts found");
                    }
                    return;
                }

                if (ENABLE_LOG) {
                    Log.d(TAG, "Import success with " + response.getUsers().size() + " new contacts");
                }

                ArrayList<Integer> uids = new ArrayList<Integer>();
                for (im.actor.model.api.User u : response.getUsers()) {
                    uids.add(u.getId());
                }
                updates().onUpdateReceived(new FatSeqUpdate(
                        response.getSeq(), response.getState(),
                        UpdateContactsAdded.HEADER,
                        new UpdateContactsAdded(uids).toByteArray(),
                        response.getUsers(),
                        new ArrayList<Group>()));
            }

            @Override
            public void onError(RpcException e) {
                // TODO: Better error handling
                if (ENABLE_LOG) {
                    Log.d(TAG, "Import failure");
                }
                e.printStackTrace();
            }
        });
    }

    private boolean isImported(long phone) {
        return preferences().getBool("book_phone_" + phone, false);
    }

    private boolean isImported(String email) {
        return preferences().getBool("book_email_" + email.toLowerCase(), false);
    }

    private void markImported(long phone) {
        preferences().putBool("book_phone_" + phone, true);
    }

    private void markImported(String email) {
        preferences().putBool("book_email_" + email.toLowerCase(), true);
    }

    private void markImported() {
        modules().getAppStateModule().onBookImported();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PerformSync) {
            performSync();
        } else if (message instanceof PhoneBookLoaded) {
            onPhoneBookLoaded(((PhoneBookLoaded) message).getPhoneBook());
        } else {
            drop(message);
        }
    }

    public static class PerformSync {

    }

    private static class PhoneBookLoaded {
        private List<PhoneBookContact> phoneBook;

        private PhoneBookLoaded(List<PhoneBookContact> phoneBook) {
            this.phoneBook = phoneBook;
        }

        public List<PhoneBookContact> getPhoneBook() {
            return phoneBook;
        }
    }
}
