/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.contacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.entity.PhoneBookIds;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.contacts.entity.BookImportStorage;
import im.actor.core.api.ApiEmailToImport;
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
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.bser.Bser;

public class BookImportActor extends ModuleActor {

    private static final String TAG = "ContactsImport";

    // j2objc workaround
    private static final Void DUMB = null;

    private final boolean ENABLE_LOG;

    private static final int MAX_IMPORT_SIZE = 50;

    // Reading Phone Book
    private boolean phoneBookReadingIsInProgress = false;

    // Import Queue
    private boolean isUploadingContacts = false;
    private ArrayList<ImportQueueItem> importQueue = new ArrayList<>();

    // Currently importing phones and emails
    private HashSet<Long> importingPhones = new HashSet<>();
    private HashSet<String> importingEmails = new HashSet<>();

    // Imported phones and emails
    private BookImportStorage storage = new BookImportStorage();

    public BookImportActor(ModuleContext context) {
        super(context);
        ENABLE_LOG = context.getConfiguration().isEnableContactsLogging();
    }

    @Override
    public void preStart() {
        super.preStart();
        byte[] data = context().getContactsModule().getBookImportState().get(0);
        if (data != null) {
            try {
                storage = new BookImportStorage(data);
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
        }

        self().send(new PerformSync());
    }

    private void performSync() {
        // Ignoring syncing if not enabled
        if (!config().isEnablePhoneBookImport()) {
            return;
        }

        if (ENABLE_LOG) {
            Log.d(TAG, "Checking sync...");
        }
        if (phoneBookReadingIsInProgress) {
            if (ENABLE_LOG) {
                Log.d(TAG, "Sync already in progress");
            }
            return;
        }
        phoneBookReadingIsInProgress = true;
        if (ENABLE_LOG) {
            Log.d(TAG, "Starting book loading...");
        }

        context().getConfiguration().getPhoneBookProvider()
                .loadPhoneBook(contacts -> self().send(new PhoneBookLoaded(contacts)));
    }

    private void onPhoneBookLoaded(List<PhoneBookContact> phoneBook) {
        phoneBookReadingIsInProgress = false;
        if (ENABLE_LOG) {
            Log.d(TAG, "Book load completed");
        }

        int newPhones = 0;
        int newEmails = 0;

        for (PhoneBookContact record : phoneBook) {
            for (PhoneBookPhone phone : record.getPhones()) {
                if (storage.isImported(phone.getNumber())) {
                    continue;
                }
                if (importingPhones.contains(phone.getNumber())) {
                    continue;
                }
                importingPhones.add(phone.getNumber());
                importQueue.add(new ImportPhoneQueueItem(phone.getNumber(), record.getName()));
                newPhones++;
            }

            for (PhoneBookEmail email : record.getEmails()) {
                if (storage.isImported(email.getEmail().toLowerCase())) {
                    continue;
                }
                if (importingEmails.contains(email.getEmail().toLowerCase())) {
                    continue;
                }
                importingEmails.add(email.getEmail().toLowerCase());
                importQueue.add(new ImportEmailQueueItem(email.getEmail().toLowerCase(), record.getName()));
                newEmails++;
            }

        }

        if (ENABLE_LOG) {
            if (newPhones == 0 && newEmails == 0) {
                Log.d(TAG, "No new contacts found");
            } else {
                Log.d(TAG, "Founded new " + (newPhones + newEmails) + " contact records");
            }
        }

        performImportIfRequired();
    }

    private void performImportIfRequired() {

        //
        // Checking state
        //

        if (ENABLE_LOG) {
            Log.d(TAG, "performImportIfRequired called");
        }
        if (isUploadingContacts) {
            if (ENABLE_LOG) {
                Log.d(TAG, "performImportIfRequired:exiting:already importing");
            }
            return;
        }

        if (importQueue.size() == 0) {
            if (ENABLE_LOG) {
                Log.d(TAG, "performImportIfRequired:exiting:nothing to import");
            }
            // Marking as everything is imported
            context().getAppStateModule().onBookImported();
            return;
        }

        //
        // Performing import
        //

        isUploadingContacts = true;
        final ArrayList<ApiPhoneToImport> phoneToImports = new ArrayList<>();
        final ArrayList<ApiEmailToImport> emailToImports = new ArrayList<>();
        for (int i = 0; i < MAX_IMPORT_SIZE && importQueue.size() > 0; i++) {
            ImportQueueItem importQueueItem = importQueue.remove(0);
            if (importQueueItem instanceof ImportEmailQueueItem) {
                emailToImports.add(new ApiEmailToImport(((ImportEmailQueueItem) importQueueItem).getEmail(),
                        ((ImportEmailQueueItem) importQueueItem).getName()));
            } else if (importQueueItem instanceof ImportPhoneQueueItem) {
                phoneToImports.add(new ApiPhoneToImport(((ImportPhoneQueueItem) importQueueItem).getPhoneNumber(),
                        ((ImportPhoneQueueItem) importQueueItem).getName()));
            } else {
                throw new RuntimeException();
            }
        }
        request(new RequestImportContacts(phoneToImports, emailToImports, ApiSupportConfiguration.OPTIMIZATIONS), new RpcCallback<ResponseImportContacts>() {
            @Override
            public void onResult(ResponseImportContacts response) {

                //
                // Saving imported state
                //

                for (ApiPhoneToImport phoneToImport : phoneToImports) {
                    storage.markAsImported(phoneToImport.getPhoneNumber());
                    importingPhones.remove(phoneToImport.getPhoneNumber());
                }
                for (ApiEmailToImport emailToImport : emailToImports) {
                    storage.markAsImported(emailToImport.getEmail());
                    importingEmails.remove(emailToImport.getEmail());
                }
                context().getContactsModule().getBookImportState().put(0, storage.toByteArray());

                //
                // Generating update
                //
                if (response.getUsers().size() != 0 || response.getUserPeers().size() != 0) {

                    if (ENABLE_LOG) {
                        Log.d(TAG, "Import success with " +
                                (response.getUsers().size() + response.getUserPeers().size()) + " new contacts");
                    }

                    if (response.getUserPeers().size() != 0) {

                        // Optimized version
                        ArrayList<Integer> uids = new ArrayList<>();
                        for (ApiUserOutPeer u : response.getUserPeers()) {
                            uids.add(u.getUid());
                        }
                        updates().loadRequiredPeers(response.getUserPeers(), new ArrayList<>())
                                .flatMap(v -> updates().applyUpdate(
                                        response.getSeq(),
                                        response.getState(),
                                        new UpdateContactsAdded(uids))
                                );
                    } else {

                        // Old version
                        ArrayList<Integer> uids = new ArrayList<>();
                        for (ApiUser u : response.getUsers()) {
                            uids.add(u.getId());
                        }
                        updates().onUpdateReceived(new FatSeqUpdate(
                                response.getSeq(), response.getState(),
                                UpdateContactsAdded.HEADER,
                                new UpdateContactsAdded(uids).toByteArray(),
                                response.getUsers(),
                                new ArrayList<>()));
                    }
                } else {
                    if (ENABLE_LOG) {
                        Log.d(TAG, "Import success, but no new contacts found");
                    }
                }

                //
                // Launching next iteration
                //
                isUploadingContacts = false;
                performImportIfRequired();
            }

            @Override
            public void onError(RpcException e) {

                // TODO: Better error handling
                if (ENABLE_LOG) {
                    Log.d(TAG, "Import failure");
                }
                e.printStackTrace();

                //
                // Launching next iteration
                //
                isUploadingContacts = false;
                performImportIfRequired();
            }
        });
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PerformSync) {
            performSync();
        } else if (message instanceof PhoneBookLoaded) {
            onPhoneBookLoaded(((PhoneBookLoaded) message).getPhoneBook());
        } else {
            super.onReceive(message);
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

    private static abstract class ImportQueueItem {

    }

    private static class ImportPhoneQueueItem extends ImportQueueItem {

        private long phoneNumber;
        private String name;

        public ImportPhoneQueueItem(long phoneNumber, String name) {
            this.phoneNumber = phoneNumber;
            this.name = name;
        }

        public long getPhoneNumber() {
            return phoneNumber;
        }

        public String getName() {
            return name;
        }
    }

    private static class ImportEmailQueueItem extends ImportQueueItem {
        private String email;
        private String name;

        public ImportEmailQueueItem(String email, String name) {
            this.email = email;
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }
}
