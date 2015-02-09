package im.actor.model.storage;

import im.actor.model.entity.User;
import im.actor.model.mvvm.KeyValueEngine;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface EnginesFactory {
    public KeyValueEngine<User> createUsersEngine();
}
