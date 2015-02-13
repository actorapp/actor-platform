package im.actor.messenger.core.actors.contacts;

import com.droidkit.actors.concurrency.Future;

import im.actor.messenger.model.UserModel;

/**
 * Created by ex3ndr on 31.10.14.
 */
public interface ContactsInt {

    public void onUserNameChanged(int uid);

    public void onUserAvatarChanged(int uid);

    public void onContactsAdded(int[] uids);

    public void onContactsRemoved(int[] uids);

    public Future<UserModel[]> findUsers(String query);

    public Future<Boolean> addContact(int uid);

    public Future<Boolean> removeContact(int uid);
}
