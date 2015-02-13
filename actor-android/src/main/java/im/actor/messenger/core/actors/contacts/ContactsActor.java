package im.actor.messenger.core.actors.contacts;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedFuture;
import com.droidkit.engine.persistence.PersistenceLongSet;
import com.droidkit.engine.search.SearchEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.api.scheme.User;
import im.actor.api.scheme.base.SeqUpdate;
import im.actor.api.scheme.rpc.ResponseGetContacts;
import im.actor.api.scheme.rpc.ResponseSearchContacts;
import im.actor.api.scheme.rpc.ResponseSeq;
import im.actor.api.scheme.updates.UpdateContactsAdded;
import im.actor.api.scheme.updates.UpdateContactsRemoved;
import im.actor.crypto.Crypto;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.ProfileSyncState;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.SimpleStorage;
import im.actor.messenger.storage.scheme.Contact;
import im.actor.messenger.storage.scheme.GlobalSearch;
import im.actor.messenger.util.Logger;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.core.actors.users.UserActor.userActor;
import static im.actor.messenger.storage.KeyValueEngines.users;
import static im.actor.messenger.storage.SearchEngines.userSearchEngine;

/**
 * Created by ex3ndr on 31.10.14.
 */
public class ContactsActor extends TypedActor<ContactsInt> implements ContactsInt {

    private static final TypedActorHolder<ContactsInt> HOLDER = new TypedActorHolder<ContactsInt>(
            ContactsInt.class, ContactsActor.class, "updates", "contacts_list");

    public static ContactsInt contactsList() {
        return HOLDER.get();
    }

    private static final String TAG = "ContactsActor";

    private PersistenceLongSet contactUsers;
    private SearchEngine<GlobalSearch> searchEngine;

    private boolean isInProgress = false;
    private boolean isInvalidated = false;

    public ContactsActor() {
        super(ContactsInt.class);
    }

    @Override
    public void preStart() {
        contactUsers = SimpleStorage.getContactsMap();
        searchEngine = userSearchEngine();
        performSync();
    }

    @Override
    public void onUserNameChanged(int uid) {
        if (!contactUsers.contains((long) uid)) {
            return;
        }
        updateContactList();
    }

    @Override
    public void onUserAvatarChanged(int uid) {
        if (!contactUsers.contains((long) uid)) {
            return;
        }
        updateContactList();
    }

    @Override
    public void onContactsAdded(int[] uids) {
        for (int u : uids) {
            UserModel userModel = users().get(u);
            if (userModel != null) {
                contactUsers.add((long) u);
                userModel.getContactModel().change(true);
            } else {
                throw new RuntimeException("Unable to find user #" + u);
            }
        }
        updateContactList();
        ProfileSyncState.onContactsLoaded(contactUsers.size() == 0);
        performSync();
    }

    @Override
    public void onContactsRemoved(int[] uids) {
        for (int u : uids) {
            contactUsers.remove((long) u);
            UserModel userModel = users().get(u);
            if (userModel != null) {
                userModel.getContactModel().change(false);
            }
        }
        updateContactList();
        // ProfileSyncState.onContactsLoaded(contactUsers.size() == 0);
        performSync();
    }

    @Override
    public Future<UserModel[]> findUsers(String query) {
        final TypedFuture<UserModel[]> res = future();
        ask(requests().searchContacts(query), new FutureCallback<ResponseSearchContacts>() {
            @Override
            public void onResult(final ResponseSearchContacts result) {
                if (result.getUsers().size() == 0) {
                    res.doComplete(new UserModel[0]);
                    return;
                }
                ask(userActor().onUpdateUsers(result.getUsers()), new FutureCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result2) {
                        UserModel[] users = new UserModel[result.getUsers().size()];
                        for (int i = 0; i < users.length; i++) {
                            users[i] = users().get(result.getUsers().get(i).getId());
                        }
                        res.doComplete(users);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        res.doComplete(new UserModel[0]);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                res.doComplete(new UserModel[0]);
            }
        });
        return res;
    }

    @Override
    public Future<Boolean> addContact(final int uid) {
        UserModel userModel = users().get(uid);
        final TypedFuture<Boolean> res = new TypedFuture<Boolean>();
        ask(requests().addContact(uid, userModel.getAccessHash()), new FutureCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq result) {
                res.doComplete(true);
                List<Integer> uids = new ArrayList<Integer>();
                uids.add(uid);
                system().actorOf(SequenceActor.sequence()).send(
                        new SeqUpdate(result.getSeq(), result.getState(),
                                UpdateContactsAdded.HEADER,
                                new UpdateContactsAdded(uids).toByteArray()));

            }

            @Override
            public void onError(Throwable throwable) {
                res.doError(throwable);
                onContactsAdded(new int[]{uid});
            }
        });
        return res;
    }

    @Override
    public Future<Boolean> removeContact(final int uid) {
        UserModel userModel = users().get(uid);
        final TypedFuture<Boolean> res = new TypedFuture<Boolean>();
        ask(requests().removeContact(uid, userModel.getAccessHash()), new FutureCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq result) {
                res.doComplete(true);
                List<Integer> uids = new ArrayList<Integer>();
                uids.add(uid);
                system().actorOf(SequenceActor.sequence()).send(
                        new SeqUpdate(result.getSeq(), result.getState(),
                                UpdateContactsRemoved.HEADER,
                                new UpdateContactsRemoved(uids).toByteArray()));
            }

            @Override
            public void onError(Throwable throwable) {
                res.doError(throwable);
                onContactsRemoved(new int[]{uid});
            }
        });
        return res;
    }

    private void updateContactList() {
        ArrayList<UserModel> userList = new ArrayList<UserModel>();
        for (long u : contactUsers) {
            userList.add(users().get((int) u));
        }
        Collections.sort(userList, new Comparator<UserModel>() {
            @Override
            public int compare(UserModel lhs, UserModel rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        List<Contact> registeredContacts = new ArrayList<Contact>();
        int index = -1;
        for (UserModel userModel : userList) {
            Contact contact = new Contact(userModel.getId(),
                    (long) index--,
                    userModel.getName(),
                    userModel.getAvatar().getValue());
            registeredContacts.add(contact);
            searchEngine.indexLow(DialogUids.getDialogUid(DialogType.TYPE_USER, userModel.getId()), 0,
                    userModel.getName(), new GlobalSearch(DialogType.TYPE_USER, userModel.getId(),
                            userModel.getName(), userModel.getAvatar().getValue()));
        }

        ListEngines.getContactsEngine().replaceItems(registeredContacts);
    }

    private void performSync() {
        if (isInProgress) {
            isInvalidated = true;
            return;
        }
        isInProgress = true;
        isInvalidated = false;
        Long[] uids = contactUsers.toArray(new Long[0]);
        Arrays.sort(uids);
        String hash = "";
        for (long u : uids) {
            if (hash.length() != 0) {
                hash += ",";
            }
            hash += u;
        }
        String hashValue = Crypto.hex(Crypto.SHA256(hash.getBytes()));

        Logger.d(TAG, "Performing sync with uids: " + hash);
        Logger.d(TAG, "Performing sync with hash: " + hashValue);

        ask(requests().getContacts(hashValue), new FutureCallback<ResponseGetContacts>() {
            @Override
            public void onResult(final ResponseGetContacts result) {
                isInProgress = false;
                if (result.isNotChanged()) {
                    Logger.d(TAG, "Sync: Not changed");
                    if (isInvalidated) {
                        performSync();
                    } else {
                        ProfileSyncState.onContactsLoaded(contactUsers.size() == 0);
                    }
                    return;
                }

                Logger.d(TAG, "Sync: Users count: " + result.getUsers().size());

                final boolean isInvalidatedFinal = isInvalidated;

                ask(userActor().onUpdateUsers(result.getUsers()), new FutureCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result2) {
                        outer:
                        for (long user : contactUsers.toArray(new Long[0])) {
                            for (User u : result.getUsers()) {
                                // contactUsers.add((long) u.getId());
                                if (user == u.getId()) {
                                    continue outer;
                                }
                            }
                            UserModel userModel = users().get((int) user);
                            userModel.getContactModel().change(false);
                            contactUsers.remove(user);
                        }


                        outer:
                        for (User u : result.getUsers()) {
                            for (long user : contactUsers.toArray(new Long[0])) {
                                if (user == u.getId()) {
                                    continue outer;
                                }
                            }
                            UserModel userModel = users().get(u.getId());
                            userModel.getContactModel().change(true);
                            contactUsers.add((long) u.getId());
                        }

                        updateContactList();

                        if (!isInvalidatedFinal) {
                            ProfileSyncState.onContactsLoaded(contactUsers.size() == 0);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

                if (isInvalidated) {
                    performSync();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                isInProgress = false;
                throwable.printStackTrace();
            }
        });
    }
}
