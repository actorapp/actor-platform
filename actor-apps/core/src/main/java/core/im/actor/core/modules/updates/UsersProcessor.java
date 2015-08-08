/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.runtime.annotations.Verified;
import im.actor.core.api.Avatar;
import im.actor.core.entity.User;
import im.actor.core.modules.BaseModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.contacts.ContactsSyncActor;
import im.actor.core.modules.messages.DialogsActor;

import static im.actor.core.util.JavaUtil.equalsE;

@Verified
public class UsersProcessor extends BaseModule {

    @Verified
    public UsersProcessor(Modules messenger) {
        super(messenger);
    }

    @Verified
    public void applyUsers(Collection<im.actor.core.api.User> updated, boolean forced) {
        ArrayList<User> batch = new ArrayList<User>();
        for (im.actor.core.api.User u : updated) {

            User saved = users().getValue(u.getId());
            if (saved == null) {
                batch.add(new User(u));
            } else if (forced) {
                User upd = new User(u);
                batch.add(upd);

                // Sending changes to dialogs
                if (!upd.getName().equals(saved.getName()) ||
                        !equalsE(upd.getAvatar(), saved.getAvatar())) {
                    onUserDescChanged(upd);
                }
            }
        }
        if (batch.size() > 0) {
            users().addOrUpdateItems(batch);
        }
    }

    @Verified
    public void onUserNameChanged(int uid, String name) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if name not changed
            if (u.getServerName().equals(name)) {
                return;
            }

            // Changing user name
            u = u.editName(name);

            // Updating user in collection
            users().addOrUpdateItem(u);

            // Notify if user doesn't have local name
            if (u.getLocalName() == null) {
                onUserDescChanged(u);
            }
        }
    }

    @Verified
    public void onUserNickChanged(int uid, String nick) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if name not changed
            if (equalsE(u.getNick(), nick)) {
                return;
            }

            // Changing user name
            u = u.editNick(nick);

            // Updating user in collection
            users().addOrUpdateItem(u);
        }
    }

    @Verified
    public void onUserAboutChanged(int uid, String about) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if name not changed
            if (equalsE(u.getAbout(), about)) {
                return;
            }

            // Changing about information
            u = u.editAbout(about);

            // Updating user in collection
            users().addOrUpdateItem(u);
        }
    }

    @Verified
    public void onUserLocalNameChanged(int uid, String name) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if local name not changed
            if (equalsE(u.getLocalName(), name)) {
                return;
            }

            // Changing user local name
            u = u.editLocalName(name);

            // Updating user in collection
            users().addOrUpdateItem(u);

            // Notify about user change
            onUserDescChanged(u);
        }
    }

    @Verified
    public void onUserAvatarChanged(int uid, Avatar avatar) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if avatar not changed
            // Disabled because of future-compatibility it is unable to check equality
            // if (equalsE(u.getAvatar(), new im.actor.model.entity.Avatar(avatar))) {
            //    return;
            // }

            // Changing user avatar
            u = u.editAvatar(avatar);

            // Updating user in collection
            users().addOrUpdateItem(u);

            // Notify about user change
            onUserDescChanged(u);
        }
    }

    @Verified
    public boolean hasUsers(Collection<Integer> uids) {
        for (Integer uid : uids) {
            if (users().getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }

    @Verified
    private void onUserDescChanged(User u) {
        modules().getMessagesModule().getDialogsActor().send(
                new DialogsActor.UserChanged(u));
        modules().getContactsModule().getContactSyncActor()
                .send(new ContactsSyncActor.UserChanged(u));
    }
}
