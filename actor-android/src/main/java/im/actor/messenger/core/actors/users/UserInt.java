package im.actor.messenger.core.actors.users;

import com.droidkit.actors.concurrency.Future;

import im.actor.api.scheme.Avatar;
import im.actor.api.scheme.User;
import im.actor.api.scheme.UserKey;

import java.util.List;

/**
 * Created by ex3ndr on 17.09.14.
 */
public interface UserInt {
    public Future<Boolean> onUpdateUsers(List<User> users);

    public void onLocalNameChanged(int uid, String name);

    public void onServerNameChanged(int uid, String name);

    public void onAvatarChanged(int uid, Avatar avatar);

    public void onDeviceRemoved(int uid, long keyHash);

    public void onDeviceAdded(int uid, long keyHash, byte[] key);

    public void onWrongKeys(List<UserKey> added, List<UserKey> invalid, List<UserKey> removed);

    public void onUserRegistered(int uid);
}
