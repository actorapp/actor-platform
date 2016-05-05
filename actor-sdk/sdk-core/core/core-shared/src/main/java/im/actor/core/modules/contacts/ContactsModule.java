/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.contacts;

import java.util.ArrayList;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestAddContact;
import im.actor.core.api.rpc.RequestRemoveContact;
import im.actor.core.api.rpc.RequestSearchContacts;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.api.updates.UpdateContactsRemoved;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.Modules;
import im.actor.core.viewmodel.Command;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.ListEngine;
import im.actor.core.entity.Contact;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.storage.SyncKeyValue;

import static im.actor.runtime.actors.ActorSystem.system;

public class ContactsModule extends AbsModule {

    private ListEngine<Contact> contacts;
    private ActorRef bookImportActor;
    private ActorRef contactSyncActor;
    private SyncKeyValue bookImportState;

    public ContactsModule(final Modules modules) {
        super(modules);

        contacts = Storage.createList(STORAGE_CONTACTS, Contact.CREATOR);
        bookImportState = new SyncKeyValue(Storage.createKeyValue(STORAGE_BOOK_IMPORT));
    }

    public void run() {
        bookImportActor = system().actorOf(Props.create(() -> new BookImportActor(context())).changeDispatcher("heavy"), "actor/book_import");
        contactSyncActor = system().actorOf(Props.create(() -> new ContactsSyncActor(context())).changeDispatcher("heavy"), "actor/contacts_sync");
    }

    public SyncKeyValue getBookImportState() {
        return bookImportState;
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

    public void markContact(int uid) {
        preferences().putBool("contact_" + uid, true);
    }

    public void markNonContact(int uid) {
        preferences().putBool("contact_" + uid, false);
    }

    public boolean isUserContact(int uid) {
        return preferences().getBool("contact_" + uid, false);
    }

    public Promise<UserVM[]> findUsers(final String query) {
        return api(new RequestSearchContacts(query, ApiSupportConfiguration.OPTIMIZATIONS))
                .chain(responseSearchContacts -> updates().loadRequiredPeers(responseSearchContacts.getUserPeers(), new ArrayList<>()))
                .map(responseSearchContacts1 -> {
                    ArrayList<UserVM> users = new ArrayList<>();
                    for (ApiUserOutPeer u : responseSearchContacts1.getUserPeers()) {
                        users.add(context().getUsersModule().getUsers().get(u.getUid()));
                    }
                    return users.toArray(new UserVM[users.size()]);
                });
    }

    public Command<Boolean> addContact(final int uid) {
        return callback -> {
            User user = users().getValue(uid);
            if (user == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }

            request(new RequestAddContact(uid, user.getAccessHash()), new RpcCallback<ResponseSeq>() {
                @Override
                public void onResult(ResponseSeq response) {
                    ArrayList<Integer> uids = new ArrayList<>();
                    uids.add(uid);
                    SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                            UpdateContactsAdded.HEADER, new UpdateContactsAdded(uids).toByteArray());
                    updates().onUpdateReceived(update);
                    runOnUiThread(() -> callback.onResult(true));
                }

                @Override
                public void onError(RpcException e) {
                    runOnUiThread(() -> callback.onError(new RpcInternalException()));
                }
            });
        };
    }

    public Command<Boolean> removeContact(final int uid) {
        return callback -> {
            User user = users().getValue(uid);
            if (user == null) {
                runOnUiThread(() -> callback.onError(new RpcInternalException()));
                return;
            }

            request(new RequestRemoveContact(uid, user.getAccessHash()), new RpcCallback<ResponseSeq>() {
                @Override
                public void onResult(ResponseSeq response) {
                    ArrayList<Integer> uids = new ArrayList<>();
                    uids.add(uid);
                    SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                            UpdateContactsRemoved.HEADER, new UpdateContactsRemoved(uids).toByteArray());
                    updates().onUpdateReceived(update);
                    runOnUiThread(() -> callback.onResult(true));
                }

                @Override
                public void onError(RpcException e) {
                    runOnUiThread(() -> callback.onError(new RpcInternalException()));
                }
            });
        };
    }

    public void resetModule() {
        // TODO: Implement
    }
}
