/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.contacts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestGetContacts;
import im.actor.core.api.rpc.ResponseGetContacts;
import im.actor.core.entity.Contact;
import im.actor.core.entity.User;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;

public class ContactsSyncActor extends ModuleActor {

    private static final String TAG = "ContactsServerSync";

    // j2objc workaround
    private static final Void DUMB = null;

    private final boolean ENABLE_LOG;

    private ArrayList<Integer> contacts = new ArrayList<>();

    private boolean isInProgress = false;
    private boolean isInvalidated = false;

    public ContactsSyncActor(ModuleContext context) {
        super(context);
        ENABLE_LOG = context.getConfiguration().isEnableContactsLogging();
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
                    int uid = dataInput.readInt();
                    if (!contacts.contains(uid)) {
                        contacts.add(uid);
                    }
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
        String hashValue = Crypto.hex(Crypto.SHA256(hashData));

        if (ENABLE_LOG) {
            Log.d(TAG, "Performing sync with hash: " + hashValue);
            Log.d(TAG, "Performing sync with uids: " + hash);
        }

        request(new RequestGetContacts(hashValue, ApiSupportConfiguration.OPTIMIZATIONS), new RpcCallback<ResponseGetContacts>() {
            @Override
            public void onResult(ResponseGetContacts response) {

                if (ENABLE_LOG) {
                    Log.d(TAG, "Sync received " + (response.getUsers().size() + response.getUserPeers().size()) + " contacts");
                }

                if (response.getUserPeers().size() > 0) {
                    updates().loadRequiredPeers(response.getUserPeers(), new ArrayList<>())
                            .then(v -> onContactsLoaded(response));
                } else {
                    updates().applyRelatedData(response.getUsers())
                            .then(v -> onContactsLoaded(response));
                }
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

        context().getAppStateModule().onContactsLoaded();

        if (result.isNotChanged()) {
            Log.d(TAG, "Sync: Not changed");
            if (isInvalidated) {
                performSync();
            } else {
                // ProfileSyncState.onContactsLoaded(contactUsers.size() == 0);
            }
            return;
        }

        // Reading all uids
        HashSet<Integer> uids = new HashSet<>();
        for (ApiUser u : result.getUsers()) {
            uids.add(u.getId());
        }
        for (ApiUserOutPeer u : result.getUserPeers()) {
            uids.add(u.getUid());
        }

        if (ENABLE_LOG) {
            Log.d(TAG, "Sync received " + uids.size() + " contacts");
        }

        outer:
        for (Integer uid : contacts.toArray(new Integer[contacts.size()])) {
            for (Integer u : uids) {
                if (u.equals(uid)) {
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
            context().getContactsModule().markNonContact(uid);
        }
        for (Integer u : uids) {
            if (contacts.contains(u)) {
                continue;
            }
            if (ENABLE_LOG) {
                Log.d(TAG, "Adding: #" + u);
            }
            contacts.add(u);
            if (getUser(u) != null) {
                getUserVM(u).isContact().change(true);
            }
            context().getContactsModule().markContact(u);
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
            if (contacts.contains(uid)) {
                continue;
            }
            if (ENABLE_LOG) {
                Log.d(TAG, "Adding: #" + uid);
            }
            contacts.add(uid);
            context().getContactsModule().markContact(uid);
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
            context().getContactsModule().markNonContact(uid);
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
        ArrayList<User> userList = new ArrayList<>();
        for (int u : contacts) {
            userList.add(getUser(u));
        }
        Collections.sort(userList, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

        List<Contact> registeredContacts = new ArrayList<>();
        int index = -1;
        for (User userModel : userList) {
            Contact contact = new Contact(userModel.getUid(),
                    (long) index--,
                    userModel.getAvatar(),
                    userModel.getName());
            registeredContacts.add(contact);
        }
        context().getContactsModule().getContacts().replaceItems(registeredContacts);
        Integer[] sorted = new Integer[contacts.size()];
        int sindex = 0;
        for (User userModel : userList) {
            sorted[sindex++] = userModel.getUid();
        }
        context().getSearchModule().onContactsChanged(sorted);

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
        context().getAppStateModule().onContactsUpdate(context().getContactsModule().getContacts().isEmpty());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ContactsAdded) {
            onContactsAdded(((ContactsAdded) message).getUids());
        } else if (message instanceof ContactsRemoved) {
            onContactsRemoved(((ContactsRemoved) message).getUids());
        } else if (message instanceof UserChanged) {
            onUserChanged(((UserChanged) message).getUser());
        } else if (message instanceof PerformSync) {
            performSync();
        } else {
            super.onReceive(message);
        }
    }

    private static class PerformSync {

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