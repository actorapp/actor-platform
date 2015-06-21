/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.ArrayList;

import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestAddContact;
import im.actor.model.api.rpc.RequestRemoveContact;
import im.actor.model.api.rpc.RequestSearchContacts;
import im.actor.model.api.rpc.ResponseSearchContacts;
import im.actor.model.api.rpc.ResponseSeq;
import im.actor.model.api.updates.UpdateContactsAdded;
import im.actor.model.api.updates.UpdateContactsRemoved;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.entity.Contact;
import im.actor.model.modules.contacts.BookImportActor;
import im.actor.model.modules.contacts.ContactsSyncActor;
import im.actor.model.modules.updates.internal.UsersFounded;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.RpcInternalException;
import im.actor.model.viewmodel.UserVM;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Contacts extends BaseModule {

    private ListEngine<Contact> contacts;
    private ActorRef bookImportActor;
    private ActorRef contactSyncActor;

    public Contacts(final Modules modules) {
        super(modules);

        contacts = modules.getConfiguration().getStorageProvider()
                .createContactsList(modules.getConfiguration().getStorageProvider().createList(STORAGE_CONTACTS));
    }

    public void run() {
        bookImportActor = system().actorOf(Props.create(BookImportActor.class, new ActorCreator<BookImportActor>() {
            @Override
            public BookImportActor create() {
                return new BookImportActor(modules());
            }
        }), "actor/book_import");
        contactSyncActor = system().actorOf(Props.create(ContactsSyncActor.class, new ActorCreator<ContactsSyncActor>() {
            @Override
            public ContactsSyncActor create() {
                return new ContactsSyncActor(modules());
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

    public void markContact(int uid) {
        preferences().putBool("contact_" + uid, true);
    }

    public void markNonContact(int uid) {
        preferences().putBool("contact_" + uid, false);
    }

    public boolean isUserContact(int uid) {
        return preferences().getBool("contact_" + uid, false);
    }

    public Command<UserVM[]> findUsers(final String query) {
        return new Command<UserVM[]>() {
            @Override
            public void start(final CommandCallback<UserVM[]> callback) {
                request(new RequestSearchContacts(query), new RpcCallback<ResponseSearchContacts>() {
                    @Override
                    public void onResult(ResponseSearchContacts response) {
                        if (response.getUsers().size() == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onResult(new UserVM[0]);
                                }
                            });
                            return;
                        }

                        updates().onUpdateReceived(new UsersFounded(response.getUsers(), callback));
                    }

                    @Override
                    public void onError(RpcException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(new UserVM[0]);
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Boolean> addContact(final int uid) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                im.actor.model.entity.User user = users().getValue(uid);
                if (user == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }

                request(new RequestAddContact(uid, user.getAccessHash()), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        ArrayList<Integer> uids = new ArrayList<Integer>();
                        uids.add(uid);
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateContactsAdded.HEADER, new UpdateContactsAdded(uids).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Boolean> removeContact(final int uid) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                im.actor.model.entity.User user = users().getValue(uid);
                if (user == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }

                request(new RequestRemoveContact(uid, user.getAccessHash()), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        ArrayList<Integer> uids = new ArrayList<Integer>();
                        uids.add(uid);
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateContactsRemoved.HEADER, new UpdateContactsRemoved(uids).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public void resetModule() {
        // TODO: Implement
    }
}
