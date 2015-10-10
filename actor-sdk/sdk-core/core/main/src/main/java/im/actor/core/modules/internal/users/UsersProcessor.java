package im.actor.core.modules.internal.users;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiUser;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserAvatarChanged;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.Processor;
import im.actor.core.modules.internal.contacts.ContactsSyncActor;
import im.actor.core.modules.internal.messages.DialogsActor;
import im.actor.runtime.annotations.Verified;

import static im.actor.core.util.JavaUtil.equalsE;

public class UsersProcessor extends AbsModule implements Processor {

    public UsersProcessor(ModuleContext context) {
        super(context);
    }

    @Verified
    public void applyUsers(Collection<ApiUser> updated, boolean forced) {
        ArrayList<User> batch = new ArrayList<User>();
        for (ApiUser u : updated) {

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
    public boolean hasUsers(Collection<Integer> uids) {
        for (Integer uid : uids) {
            if (users().getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }

    @Verified
    private void onUserNameChanged(int uid, String name) {
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
    private void onUserNickChanged(int uid, String nick) {
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
    private void onUserAboutChanged(int uid, String about) {
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
    private void onUserLocalNameChanged(int uid, String name) {
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
    private void onUserAvatarChanged(int uid, ApiAvatar avatar) {
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

    @Override
    public boolean process(Object update) {
        if (update instanceof UpdateUserNameChanged) {
            UpdateUserNameChanged userNameChanged = (UpdateUserNameChanged) update;
            onUserNameChanged(userNameChanged.getUid(), userNameChanged.getName());
            return true;
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            onUserLocalNameChanged(localNameChanged.getUid(), localNameChanged.getLocalName());
            return true;
        } else if (update instanceof UpdateUserNickChanged) {
            UpdateUserNickChanged nickChanged = (UpdateUserNickChanged) update;
            onUserNickChanged(nickChanged.getUid(), nickChanged.getNickname());
            return true;
        } else if (update instanceof UpdateUserAboutChanged) {
            UpdateUserAboutChanged userAboutChanged = (UpdateUserAboutChanged) update;
            onUserAboutChanged(userAboutChanged.getUid(), userAboutChanged.getAbout());
            return true;
        } else if (update instanceof UpdateUserAvatarChanged) {
            UpdateUserAvatarChanged avatarChanged = (UpdateUserAvatarChanged) update;
            onUserAvatarChanged(avatarChanged.getUid(), avatarChanged.getAvatar());
            return true;
        }
        return false;
    }

    @Verified
    private void onUserDescChanged(User u) {
        context().getMessagesModule().getDialogsActor().send(
                new DialogsActor.UserChanged(u));
        context().getContactsModule().getContactSyncActor()
                .send(new ContactsSyncActor.UserChanged(u));
    }
}
