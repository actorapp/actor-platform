package im.actor.model.modules.contacts;

import java.io.IOException;
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
import im.actor.model.util.DataInput;
import im.actor.model.util.DataOutput;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class ContactsSyncActor extends ModuleActor {

    private static final String TAG = "ContactsActor";

    private ArrayList<Integer> contacts = new ArrayList<Integer>();

    private boolean isInProgress = false;
    private boolean isInvalidated = false;

    public ContactsSyncActor(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        super.preStart();
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
        self().send(new PerformSync());
    }

    public void performSync() {
        if (isInProgress) {
            isInvalidated = true;
            return;
        }
        isInProgress = true;
        isInvalidated = false;

        Integer[] uids = contacts.toArray(new Integer[0]);
        Arrays.sort(uids);
        String hash = "";
        for (long u : uids) {
            if (hash.length() != 0) {
                hash += ",";
            }
            hash += u;
        }
        String hashValue = CryptoUtils.hex(CryptoUtils.SHA256(hash.getBytes()));

        Log.d(TAG, "Performing sync with uids: " + hash);
        Log.d(TAG, "Performing sync with hash: " + hashValue);

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
        isInProgress = false;
        if (result.isNotChanged()) {
            Log.d(TAG, "Sync: Not changed");
            if (isInvalidated) {
                performSync();
            } else {
                // ProfileSyncState.onContactsLoaded(contactUsers.size() == 0);
            }
            return;
        }

        contacts.clear();
        for (im.actor.model.api.User u : result.getUsers()) {
            contacts.add(u.getId());
        }
        saveList();

        updateEngineList();
    }

    public void onContactsAdded(int[] uids) {
        for (int uid : uids) {
            contacts.add(uid);
        }
        saveList();

        updateEngineList();
    }

    public void onContactsRemoved(int[] uids) {
        for (int uid : uids) {
            contacts.remove(uid);
        }
        saveList();

        updateEngineList();
    }

    public void onUserChanged(User user) {
        if (!contacts.contains(user.getUid())) {
            return;
        }

        updateEngineList();
    }

    private void updateEngineList() {
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
    }

    private void saveList() {
        DataOutput dataOutput = new DataOutput();
        dataOutput.writeInt(contacts.size());
        for (int l : contacts) {
            dataOutput.writeInt(l);
        }
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