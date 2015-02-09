package im.actor.model.modules.updates;

import im.actor.model.Messenger;
import im.actor.model.api.Avatar;
import im.actor.model.api.UserState;
import im.actor.model.entity.EntityConverter;
import im.actor.model.entity.User;
import im.actor.model.mvvm.KeyValueEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class UsersProcessor {
    private Messenger messenger;
    private KeyValueEngine<User> users;

    public UsersProcessor(Messenger messenger) {
        this.messenger = messenger;
        this.users = messenger.getUsers();
    }

    public void applyUsers(Collection<im.actor.model.api.User> updated, boolean forced) {
        ArrayList<User> batch = new ArrayList<User>();
        for (im.actor.model.api.User u : updated) {
            if (users.getValue(u.getId()) == null) {
                batch.add(EntityConverter.convert(u));
            } else if (forced) {
                batch.add(EntityConverter.convert(u));
            }
        }
        if (batch.size() > 0) {
            users.addOrUpdateItems(batch);
        }
    }

    public void onUserNameChanged(int uid, String name) {
        User u = users.getValue(uid);
        if (u != null) {
            users.addOrUpdateItem(u.editName(name));
        }
    }

    public void onUserLocalNameChanged(int uid, String name) {
        User u = users.getValue(uid);
        if (u != null) {
            users.addOrUpdateItem(u.editLocalName(name));
        }
    }

    public void onUserAvatarChanged(int uid, Avatar avatar) {
        User u = users.getValue(uid);
        if (u != null) {
            // TODO: Implement
        }
    }

    public void onUserStateChanged(int uid, UserState state) {
        User u = users.getValue(uid);
        if (u != null) {
            // TODO: Implement
        }
    }

    public boolean hasUsers(Collection<Integer> uids) {
        for (Integer uid : uids) {
            if (users.getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }
}
