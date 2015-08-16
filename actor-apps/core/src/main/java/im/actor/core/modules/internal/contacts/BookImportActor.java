/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.contacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.PhoneBookProvider;
import im.actor.core.api.ApiEmailToImport;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiPhoneToImport;
import im.actor.core.api.ApiUser;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.rpc.RequestImportContacts;
import im.actor.core.api.rpc.ResponseImportContacts;
import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.entity.PhoneBookContact;
import im.actor.core.entity.PhoneBookEmail;
import im.actor.core.entity.PhoneBookPhone;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Log;

public class BookImportActor extends ModuleActor {

    private static final String TAG = "ContactsImport";

    private final boolean ENABLE_LOG;

    private static final int MAX_IMPORT_SIZE = 50;

    private HashSet<Long> importingPhones = new HashSet<Long>();
    private HashSet<String> importingEmails = new HashSet<String>();

    private boolean isSyncInProgress = false;

    public BookImportActor(ModuleContext context) {
        super(context);
        ENABLE_LOG = context.getConfiguration().isEnableContactsLogging();
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

        context().getConfiguration().getPhoneBookProvider()
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

        ArrayList<ApiPhoneToImport> phoneToImports = new ArrayList<ApiPhoneToImport>();
        ArrayList<ApiEmailToImport> emailToImports = new ArrayList<ApiEmailToImport>();
        for (PhoneBookContact record : phoneBook) {
            for (PhoneBookPhone phone : record.getPhones()) {
                if (isImported(phone.getNumber())) {
                    continue;
                }
                if (importingPhones.contains(phone.getNumber())) {
                    continue;
                }
                importingPhones.add(phone.getNumber());
                phoneToImports.add(new ApiPhoneToImport(phone.getNumber(), record.getName()));
            }

            for (PhoneBookEmail email : record.getEmails()) {
                if (isImported(email.getEmail().toLowerCase())) {
                    continue;
                }
                if (importingEmails.contains(email.getEmail().toLowerCase())) {
                    continue;
                }
                importingEmails.add(email.getEmail().toLowerCase());
                emailToImports.add(new ApiEmailToImport(email.getEmail().toLowerCase(), record.getName()));
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

        ArrayList<ApiPhoneToImport> phoneToImportsPart = new ArrayList<ApiPhoneToImport>();
        ArrayList<ApiEmailToImport> emailToImportsPart = new ArrayList<ApiEmailToImport>();
        int count = 0;
        for (ApiPhoneToImport phoneToImport : phoneToImports) {
            phoneToImportsPart.add(phoneToImport);
            count++;
            if (count >= MAX_IMPORT_SIZE) {
                performImport(phoneToImportsPart, emailToImportsPart);
                phoneToImportsPart.clear();
                emailToImportsPart.clear();
                count = 0;
            }
        }

        for (ApiEmailToImport emailToImport : emailToImports) {
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

    private void performImport(ArrayList<ApiPhoneToImport> phoneToImportsPart,
                               ArrayList<ApiEmailToImport> emailToImportsPart) {

        if (ENABLE_LOG) {
            Log.d(TAG, "Performing import part with " + phoneToImportsPart.size() +
                    " phones and " + emailToImportsPart.size() + " emails");
        }

        final ApiPhoneToImport[] phones = phoneToImportsPart.toArray(new ApiPhoneToImport[phoneToImportsPart.size()]);
        final ApiEmailToImport[] emailToImports = emailToImportsPart.toArray(new ApiEmailToImport[emailToImportsPart.size()]);

        request(new RequestImportContacts((java.util.List<ApiPhoneToImport>) phoneToImportsPart.clone(),
                (java.util.List<ApiEmailToImport>) emailToImportsPart.clone()), new RpcCallback<ResponseImportContacts>() {
            @Override
            public void onResult(ResponseImportContacts response) {
                for (ApiPhoneToImport phoneToImport : phones) {
                    markImported(phoneToImport.getPhoneNumber());
                    importingPhones.remove(phoneToImport.getPhoneNumber());
                }
                for (ApiEmailToImport emailToImport : emailToImports) {
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
                for (ApiUser u : response.getUsers()) {
                    uids.add(u.getId());
                }
                updates().onUpdateReceived(new FatSeqUpdate(
                        response.getSeq(), response.getState(),
                        UpdateContactsAdded.HEADER,
                        new UpdateContactsAdded(uids).toByteArray(),
                        response.getUsers(),
                        new ArrayList<ApiGroup>()));
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
        context().getAppStateModule().onBookImported();
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
