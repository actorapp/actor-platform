package im.actor.model.storage;

import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface EnginesFactory {
    public KeyValueEngine<User> createUsersEngine();

    public ListEngine<Dialog> createDialogsEngine();
}
