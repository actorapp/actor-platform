package im.actor.model.modules.updates;

import im.actor.model.Messenger;
import im.actor.model.api.Avatar;
import im.actor.model.modules.entity.EntityConverter;
import im.actor.model.entity.User;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.mvvm.KeyValueEngine;

import java.util.ArrayList;
import java.util.Collection;

import static im.actor.model.util.JavaUtil.equalsE;

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
            User saved = users.getValue(u.getId());
            if (saved == null) {
                batch.add(EntityConverter.convert(u));
            } else if (forced) {
                User upd = EntityConverter.convert(u);
                batch.add(upd);

                // Sending changes to dialogs
                if (!upd.getName().equals(saved.getName()) ||
                        !equalsE(upd.getAvatar(), saved.getAvatar())) {
                    messenger.getMessagesModule().getDialogsActor()
                            .send(new DialogsActor.UserChanged(upd));
                }
            }
        }
        if (batch.size() > 0) {
            users.addOrUpdateItems(batch);
        }
    }

    public void onUserNameChanged(int uid, String name) {
        User u = users.getValue(uid);
        if (u != null) {
            if (u.getServerName().equals(name)) {
                return;
            }
            u = u.editName(name);
            users.addOrUpdateItem(u);
            if (u.getLocalName() == null) {
                messenger.getMessagesModule().getDialogsActor()
                        .send(new DialogsActor.UserChanged(u));
            }
        }
    }

    public void onUserLocalNameChanged(int uid, String name) {
        User u = users.getValue(uid);
        if (u != null) {
            if (u.getLocalName() == null && name == null) {
                return;
            }
            if (u.getLocalName() != null && u.getLocalName().equals(name)) {
                return;
            }
            u = u.editLocalName(name);
            users.addOrUpdateItem(u);

            messenger.getMessagesModule().getDialogsActor().send(
                    new DialogsActor.UserChanged(u));
        }
    }

    public void onUserAvatarChanged(int uid, Avatar _avatar) {
        im.actor.model.entity.Avatar avatar = EntityConverter.convert(_avatar);
        User u = users.getValue(uid);
        if (u != null) {
            if (u.getAvatar() == null && avatar == null) {
                return;
            }
            if (u.getAvatar() != null && u.getAvatar().equals(avatar)) {
                return;
            }

            u = u.editAvatar(avatar);
            users.addOrUpdateItem(u);

            messenger.getMessagesModule().getDialogsActor().send(
                    new DialogsActor.UserChanged(u));
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
