/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.contacts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.model.api.rpc.RequestGetContacts;
import im.actor.model.api.rpc.ResponseGetContacts;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.entity.Contact;
import im.actor.model.entity.User;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

public class ContactsSyncActor extends ModuleActor {

    private static final String TAG = "ContactsServerSync";

    private final boolean ENABLE_LOG;

    private ArrayList<Integer> contacts = new ArrayList<Integer>();

    private boolean isInProgress = false;
    private boolean isInvalidated = false;

    public ContactsSyncActor(Modules messenger) {
        super(messenger);
        ENABLE_LOG = messenger.getConfiguration().isEnableContactsLogging();
    }

    @Override
    public void preStart() {
        super.preStart();
        if (ENABLE_LOG) {
            Log.d(TAG, "Loading contacts ids from storage...");
        }
        byte[] data = preferences().getBytes("contact_list");
        if (data != null) {
            try {
                DataInput dataInput = new DataInput(data, 0, data.length);
                int count = dataInput.readInt();
                for (int i = 0; i < count; i++) {
                    contacts.add(dataInput.readInt());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        notifyState();
        self().send(new PerformSync());
    }

    public void performSync() {
        if (ENABLE_LOG) {
            Log.d(TAG, "Checking sync");
        }

        if (isInProgress) {
            if (ENABLE_LOG) {
                Log.d(TAG, "Sync in progress, invalidating current sync");
            }
            isInvalidated = true;
            return;
        }
        isInProgress = true;
        isInvalidated = false;

        if (ENABLE_LOG) {
            Log.d(TAG, "Starting sync");
        }

        Integer[] uids = contacts.toArray(new Integer[contacts.size()]);
        Arrays.sort(uids);
        String hash = "";
        for (long u : uids) {
            if (hash.length() != 0) {
                hash += ",";
            }
            hash += u;
        }
        byte[] hashData;
        try {
            hashData = hash.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        String hashValue = CryptoUtils.hex(CryptoUtils.SHA256(hashData));

        Log.d(TAG, "Performing sync with uids: " + hash);
        Log.d(TAG, "Performing sync with hash: " + hashValue + ", hashData:" + hashData.length);

        request(new RequestGetContacts(hashValue), new RpcCallback<ResponseGetContacts>() {
            @Override
            public void onResult(ResponseGetContacts response) {
                updates().onUpdateReceived(
                        new im.actor.model.modules.updates.internal.ContactsLoaded(response));
            }

            @Override
            public void onError(RpcException e) {
                isInProgress = false;
                e.printStackTrace();
            }
        });
    }

    public void onContactsLoaded(ResponseGetContacts result) {
        if (ENABLE_LOG) {
            Log.d(TAG, "Sync result received");
        }

        isInProgress = false;

        modules().getAppStateModule().onContactsLoaded();

        if (result.isNotChanged()) {
            Log.d(TAG, "Sync: Not changed");
            if (isInvalidated) {
                performSync();
            } else {
                // ProfileSyncState.onContactsLoaded(contactUsers.size() == 0);
            }
            return;
        }

        if (ENABLE_LOG) {
            Log.d(TAG, "Sync received " + result.getUsers().size() + " contacts");
        }

        outer:
        for (Integer uid : contacts.toArray(new Integer[contacts.size()])) {
            for (im.actor.model.api.User u : result.getUsers()) {
                if (u.getId() == uid) {
                    continue outer;
                }
            }
            if (ENABLE_LOG) {
                Log.d(TAG, "Removing: #" + uid);
            }
            contacts.remove((Integer) uid);
            if (getUser(uid) != null) {
                getUserVM(uid).isContact().change(false);
            }
            modules().getContactsModule().markNonContact(uid);
        }
        for (im.actor.model.api.User u : result.getUsers()) {
            if (contacts.contains(u.getId())) {
                continue;
            }
            if (ENABLE_LOG) {
                Log.d(TAG, "Adding: #" + u.getId());
            }
            contacts.add(u.getId());
            if (getUser(u.getId()) != null) {
                getUserVM(u.getId()).isContact().change(true);
            }
            modules().getContactsModule().markContact(u.getId());
        }
        saveList();

        updateEngineList();

        if (isInvalidated) {
            self().send(new PerformSync());
        }
    }

    public void onContactsAdded(int[] uids) {
        if (ENABLE_LOG) {
            Log.d(TAG, "OnContactsAdded received");
        }

        for (int uid : uids) {
            if (ENABLE_LOG) {
                Log.d(TAG, "Adding: #" + uid);
            }
            contacts.add(uid);
            modules().getContactsModule().markContact(uid);
            getUserVM(uid).isContact().change(true);
        }
        saveList();

        updateEngineList();

        self().send(new PerformSync());
    }

    public void onContactsRemoved(int[] uids) {
        if (ENABLE_LOG) {
            Log.d(TAG, "OnContactsRemoved received");
        }

        for (int uid : uids) {
            Log.d(TAG, "Removing: #" + uid);
            contacts.remove((Integer) uid);
            modules().getContactsModule().markNonContact(uid);
            getUserVM(uid).isContact().change(false);
        }
        saveList();

        updateEngineList();

        self().send(new PerformSync());
    }

    public void onUserChanged(User user) {
        if (ENABLE_LOG) {
            Log.d(TAG, "OnUserChanged #" + user.getUid() + " received");
        }

        if (!contacts.contains(user.getUid())) {
            return;
        }

        updateEngineList();
    }

    private void updateEngineList() {
        if (ENABLE_LOG) {
            Log.d(TAG, "Saving contact EngineList");
        }
        ArrayList<User> userList = new ArrayList<User>();
        for (int u : contacts) {
            userList.add(getUser(u));
        }
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        List<Contact> registeredContacts = new ArrayList<Contact>();
        int index = -1;
        for (User userModel : userList) {
            Contact contact = new Contact(userModel.getUid(),
                    (long) index--,
                    userModel.getAvatar(),
                    userModel.getName());
            registeredContacts.add(contact);
        }
        modules().getContactsModule().getContacts().replaceItems(registeredContacts);
        Integer[] sorted = new Integer[contacts.size()];
        int sindex = 0;
        for (User userModel : userList) {
            sorted[sindex++] = userModel.getUid();
        }
        modules().getSearch().onContactsChanged(sorted);

        notifyState();
    }

    private void saveList() {
        if (ENABLE_LOG) {
            Log.d(TAG, "Saving contacts ids to storage");
        }
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(contacts.size());
        for (int l : contacts) {
            dataOutput.writeInt(l);
        }
        preferences().putBytes("contact_list", dataOutput.toByteArray());
    }

    private void notifyState() {
        modules().getAppStateModule().onContactsUpdate(modules().getContactsModule().getContacts().isEmpty());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ContactsLoaded) {
            onContactsLoaded(((ContactsLoaded) message).getResult());
        } else if (message instanceof ContactsAdded) {
            onContactsAdded(((ContactsAdded) message).getUids());
        } else if (message instanceof ContactsRemoved) {
            onContactsRemoved(((ContactsRemoved) message).getUids());
        } else if (message instanceof UserChanged) {
            onUserChanged(((UserChanged) message).getUser());
        } else if (message instanceof PerformSync) {
            performSync();
        } else {
            drop(message);
        }
    }

    private static class PerformSync {

    }

    public static class ContactsLoaded {
        private ResponseGetContacts result;

        public ContactsLoaded(ResponseGetContacts result) {
            this.result = result;
        }

        public ResponseGetContacts getResult() {
            return result;
        }
    }

    public static class ContactsAdded {
        private int[] uids;

        public ContactsAdded(int[] uids) {
            this.uids = uids;
        }

        public int[] getUids() {
            return uids;
        }
    }

    public static class ContactsRemoved {
        private int[] uids;

        public ContactsRemoved(int[] uids) {
            this.uids = uids;
        }

        public int[] getUids() {
            return uids;
        }
    }

    public static class UserChanged {
        private User user;

        public UserChanged(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }
}