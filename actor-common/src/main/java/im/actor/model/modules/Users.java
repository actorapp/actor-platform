package im.actor.model.modules;

import im.actor.model.Messenger;
import im.actor.model.entity.User;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.storage.EnginesFactory;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Users {
    private Messenger messenger;
    private KeyValueEngine<User> users;

    public Users(Messenger messenger) {
        this.messenger = messenger;
        this.users = messenger.getConfiguration().getEnginesFactory().createUsersEngine();
    }

    public KeyValueEngine<User> getUsers() {
        return users;
    }
}