package im.actor.core.modules.users.router;

import java.util.ArrayList;

import im.actor.core.api.ApiAvatar;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.contacts.ContactsSyncActor;
import im.actor.core.modules.users.router.entity.RouterAboutChanged;
import im.actor.core.modules.users.router.entity.RouterAvatarChanged;
import im.actor.core.modules.users.router.entity.RouterLocalNameChanged;
import im.actor.core.modules.users.router.entity.RouterNameChanged;
import im.actor.core.modules.users.router.entity.RouterNicknameChanged;
import im.actor.core.modules.users.router.entity.RouterUserRegistered;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.promise.Promise;

import static im.actor.core.util.JavaUtil.equalsE;

public class UserRouter extends ModuleActor {

    public UserRouter(ModuleContext context) {
        super(context);
    }

    @Verified
    private Promise<Void> onUserNameChanged(int uid, String name) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if name not changed
            if (u.getServerName().equals(name)) {
                return Promise.success(null);
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

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onUserNickChanged(int uid, String nick) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if name not changed
            if (equalsE(u.getNick(), nick)) {
                return Promise.success(null);
            }

            // Changing user name
            u = u.editNick(nick);

            // Updating user in collection
            users().addOrUpdateItem(u);
        }

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onUserAboutChanged(int uid, String about) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if name not changed
            if (equalsE(u.getAbout(), about)) {
                return Promise.success(null);
            }

            // Changing about information
            u = u.editAbout(about);

            // Updating user in collection
            users().addOrUpdateItem(u);
        }

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onUserLocalNameChanged(int uid, String name) {
        User u = users().getValue(uid);
        if (u != null) {

            // Ignore if local name not changed
            if (equalsE(u.getLocalName(), name)) {
                return Promise.success(null);
            }

            // Changing user local name
            u = u.editLocalName(name);

            // Updating user in collection
            users().addOrUpdateItem(u);

            // Notify about user change
            onUserDescChanged(u);
        }

        return Promise.success(null);
    }

    @Verified
    private Promise<Void> onUserAvatarChanged(int uid, ApiAvatar avatar) {
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

        return Promise.success(null);
    }

    @Verified
    public Promise<Void> onUserRegistered(long rid, int uid, long date) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message(rid, date, date, uid, MessageState.UNKNOWN, ServiceUserRegistered.create()));
        context().getMessagesModule().getRouter().onNewMessages(Peer.user(uid), messages);
        return Promise.success(null);
    }

    @Verified
    private void onUserDescChanged(User u) {
        context().getMessagesModule().getRouter().onUserChanged(u);
        context().getContactsModule().getContactSyncActor()
                .send(new ContactsSyncActor.UserChanged(u));
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof RouterNameChanged) {
            RouterNameChanged nameChanged = (RouterNameChanged) message;
            return onUserNameChanged(nameChanged.getUid(), nameChanged.getName());
        } else if (message instanceof RouterLocalNameChanged) {
            RouterLocalNameChanged localNameChanged = (RouterLocalNameChanged) message;
            return onUserLocalNameChanged(localNameChanged.getUid(), localNameChanged.getLocalName());
        } else if (message instanceof RouterAvatarChanged) {
            RouterAvatarChanged avatarChanged = (RouterAvatarChanged) message;
            return onUserAvatarChanged(avatarChanged.getUid(), avatarChanged.getAvatar());
        } else if (message instanceof RouterNicknameChanged) {
            RouterNicknameChanged nicknameChanged = (RouterNicknameChanged) message;
            return onUserNickChanged(nicknameChanged.getUid(), nicknameChanged.getNickname());
        } else if (message instanceof RouterAboutChanged) {
            RouterAboutChanged aboutChanged = (RouterAboutChanged) message;
            return onUserAboutChanged(aboutChanged.getUid(), aboutChanged.getAbout());
        } else if (message instanceof RouterUserRegistered) {
            RouterUserRegistered userRegistered = (RouterUserRegistered) message;
            return onUserRegistered(userRegistered.getRid(), userRegistered.getUid(), userRegistered.getDate());
        } else {
            return super.onAsk(message);
        }
    }
}