package im.actor.core.modules.users.router;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiBotCommand;
import im.actor.core.api.ApiContactRecord;
import im.actor.core.api.ApiMapValue;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestLoadFullUsers;
import im.actor.core.api.rpc.ResponseLoadFullUsers;
import im.actor.core.api.updates.UpdateContactRegistered;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserAvatarChanged;
import im.actor.core.api.updates.UpdateUserBlocked;
import im.actor.core.api.updates.UpdateUserBotCommandsChanged;
import im.actor.core.api.updates.UpdateUserContactsChanged;
import im.actor.core.api.updates.UpdateUserExtChanged;
import im.actor.core.api.updates.UpdateUserFullExtChanged;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.api.updates.UpdateUserPreferredLanguagesChanged;
import im.actor.core.api.updates.UpdateUserTimeZoneChanged;
import im.actor.core.api.updates.UpdateUserUnblocked;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.contacts.ContactsSyncActor;
import im.actor.core.modules.users.router.entity.RouterApplyUsers;
import im.actor.core.modules.users.router.entity.RouterFetchMissingUsers;
import im.actor.core.modules.users.router.entity.RouterLoadFullUser;
import im.actor.core.modules.users.router.entity.RouterUserUpdate;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Tuple2;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromisesArray;

import static im.actor.core.util.JavaUtil.equalsE;

public class UserRouter extends ModuleActor {

    // j2objc workaround
    private static final Void DUMB = null;

    private HashSet<Integer> requestedFullUsers = new HashSet<>();
    private boolean isFreezed = false;

    public UserRouter(ModuleContext context) {
        super(context);
    }


    //
    // Small User
    //

    @Verified
    private Promise<Void> onUserNameChanged(int uid, String name) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && !u.getServerName().equals(name)) {

                        // Changing user name
                        u = u.editName(name);

                        // Updating user in collection
                        users().addOrUpdateItem(u);

                        // Notify if user doesn't have local name
                        if (u.getLocalName() == null) {
                            return onUserDescChanged(u);
                        }
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }


    @Verified
    private Promise<Void> onUserLocalNameChanged(int uid, String name) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if local name not changed
                    if (u != null && !equalsE(u.getLocalName(), name)) {

                        // Changing user local name
                        u = u.editLocalName(name);

                        // Updating user in collection
                        users().addOrUpdateItem(u);

                        // Notify about user change
                        return onUserDescChanged(u);
                    }

                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserAvatarChanged(int uid, ApiAvatar avatar) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if local name not changed
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
                        return onUserDescChanged(u);
                    }

                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserNickChanged(int uid, String nick) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if username not changed
                    if (u != null && !equalsE(u.getNick(), nick)) {

                        // Changing user name
                        u = u.editNick(nick);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserExtChanged(int uid, ApiMapValue ext) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if username not changed
                    if (u != null) {

                        // Changing user name
                        u = u.editExt(ext);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }


    //
    // Full User
    //

    @Verified
    private Promise<Void> onUserAboutChanged(int uid, String about) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && u.isHaveExtension() && !equalsE(u.getAbout(), about)) {

                        // Changing about information
                        u = u.editAbout(about);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserPreferredLanguagesChanged(int uid, List<String> languages) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && u.isHaveExtension()) {

                        // Changing about information
                        u = u.editPreferredLanguages(languages);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserTimeZoneChanged(int uid, String timeZone) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && u.isHaveExtension()) {

                        // Changing about information
                        u = u.editTimeZone(timeZone);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserContactsChanged(int uid, List<ApiContactRecord> contacts) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && u.isHaveExtension()) {

                        // Changing about information
                        u = u.editContacts(contacts);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserCommandsChanged(int uid, List<ApiBotCommand> commands) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && u.isHaveExtension()) {

                        // Changing about information
                        u = u.editBotCommands(commands);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserBlockedChanged(int uid, boolean isBlocked) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && u.isHaveExtension()) {

                        // Changing about information
                        u = u.editBlocked(isBlocked);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<Void> onUserFullExtChanged(int uid, ApiMapValue ext) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && u.isHaveExtension()) {

                        // Changing about information
                        u = u.editFullExt(ext);

                        // Updating user in collection
                        users().addOrUpdateItem(u);
                    }
                    return Promise.success((Void) null);
                })
                .after((r, e) -> unfreeze());
    }

    //
    // Misc
    //

    @Verified
    public Promise<Void> onUserRegistered(long rid, int uid, long date) {
        context().getMessagesModule().getRouter().onNewMessage(Peer.user(uid), new Message(rid, date, date, uid, MessageState.UNKNOWN, ServiceUserRegistered.create()));
        return Promise.success((Void) null);
    }

    @Verified
    private Promise<Void> onUserDescChanged(User u) {
        context().getContactsModule().getContactSyncActor().send(new ContactsSyncActor.UserChanged(u));
        return context().getMessagesModule().getRouter().onUserChanged(u);
    }


    //
    // Users changed
    //

    @Verified
    private void onLoadFullUser(int uid) {
        if (requestedFullUsers.contains(uid)) {
            return;
        }
        requestedFullUsers.add(uid);

        freeze();
        users().getValueAsync(uid)
                .flatMap((Function<User, Promise<Tuple2<ResponseLoadFullUsers, User>>>) u -> {
                    if (!u.isHaveExtension()) {
                        ArrayList<ApiUserOutPeer> users = new ArrayList<>();
                        users.add(new ApiUserOutPeer(u.getUid(), u.getAccessHash()));
                        return api(new RequestLoadFullUsers(users))
                                .map(responseLoadFullUsers ->
                                        new Tuple2<>(responseLoadFullUsers, u));
                    } else {
                        return Promise.failure(new RuntimeException("Already loaded"));
                    }
                })
                .then(r -> {

                    // Changing user extension
                    User upd = r.getT2().updateExt(r.getT1().getFullUsers().get(0));

                    // Updating user in collection
                    users().addOrUpdateItem(upd);
                })
                .after((r, e) -> unfreeze());
    }

    @Verified
    private Promise<List<ApiUserOutPeer>> fetchMissingUsers(List<ApiUserOutPeer> users) {
        freeze();
        return PromisesArray.of(users)
                .map((Function<ApiUserOutPeer, Promise<ApiUserOutPeer>>) u -> users().containsAsync(u.getUid())
                        .map(v -> v ? null : u))
                .filterNull()
                .zip()
                .after((r, e) -> unfreeze());
    }


    @Verified
    private Promise<Void> applyUsers(List<ApiUser> users) {
        freeze();
        return PromisesArray.of(users)
                .map((Function<ApiUser, Promise<Tuple2<ApiUser, Boolean>>>) u -> users().containsAsync(u.getId())
                        .map(v -> new Tuple2<>(u, v)))
                .filter(t -> !t.getT2())
                .zip()
                .then(x -> {
                    List<User> res = new ArrayList<>();
                    for (Tuple2<ApiUser, Boolean> u : x) {
                        res.add(new User(u.getT1(), null));
                    }
                    if (res.size() > 0) {
                        users().addOrUpdateItems(res);
                    }
                })
                .map(x -> (Void) null)
                .after((r, e) -> unfreeze());
    }


    //
    // Tools
    //

    private void freeze() {
        isFreezed = true;
    }

    private void unfreeze() {
        isFreezed = false;
        unstashAll();
    }


    //
    // Messages
    //

    private Promise<Void> onUpdate(Update update) {
        if (update instanceof UpdateUserNameChanged) {
            UpdateUserNameChanged updateUserNameChanged = (UpdateUserNameChanged) update;
            return onUserNameChanged(
                    updateUserNameChanged.getUid(),
                    updateUserNameChanged.getName());
        } else if (update instanceof UpdateUserLocalNameChanged) {
            UpdateUserLocalNameChanged localNameChanged = (UpdateUserLocalNameChanged) update;
            return onUserLocalNameChanged(
                    localNameChanged.getUid(),
                    localNameChanged.getLocalName());
        } else if (update instanceof UpdateUserNickChanged) {
            UpdateUserNickChanged nickChanged = (UpdateUserNickChanged) update;
            return onUserNickChanged(
                    nickChanged.getUid(),
                    nickChanged.getNickname());
        } else if (update instanceof UpdateUserAvatarChanged) {
            UpdateUserAvatarChanged userAvatarChanged = (UpdateUserAvatarChanged) update;
            return onUserAvatarChanged(
                    userAvatarChanged.getUid(),
                    userAvatarChanged.getAvatar());
        } else if (update instanceof UpdateUserAboutChanged) {
            UpdateUserAboutChanged aboutChanged = (UpdateUserAboutChanged) update;
            return onUserAboutChanged(
                    aboutChanged.getUid(),
                    aboutChanged.getAbout());
        } else if (update instanceof UpdateUserPreferredLanguagesChanged) {
            UpdateUserPreferredLanguagesChanged languagesChanged = (UpdateUserPreferredLanguagesChanged) update;
            return onUserPreferredLanguagesChanged(
                    languagesChanged.getUid(),
                    languagesChanged.getPreferredLanguages());
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered contactRegistered = (UpdateContactRegistered) update;
            if (!contactRegistered.isSilent()) {
                return onUserRegistered(
                        contactRegistered.getRid(),
                        contactRegistered.getUid(),
                        contactRegistered.getDate());
            }
        } else if (update instanceof UpdateUserTimeZoneChanged) {
            UpdateUserTimeZoneChanged timeZoneChanged = (UpdateUserTimeZoneChanged) update;
            return onUserTimeZoneChanged(
                    timeZoneChanged.getUid(),
                    timeZoneChanged.getTimeZone());
        } else if (update instanceof UpdateUserContactsChanged) {
            UpdateUserContactsChanged contactsChanged = (UpdateUserContactsChanged) update;
            return onUserContactsChanged(
                    contactsChanged.getUid(),
                    contactsChanged.getContactRecords());
        } else if (update instanceof UpdateUserExtChanged) {
            UpdateUserExtChanged extChanged = (UpdateUserExtChanged) update;
            return onUserExtChanged(
                    extChanged.getUid(),
                    extChanged.getExt());
        } else if (update instanceof UpdateUserFullExtChanged) {
            UpdateUserFullExtChanged extChanged = (UpdateUserFullExtChanged) update;
            return onUserFullExtChanged(
                    extChanged.getUid(),
                    extChanged.getExt());
        } else if (update instanceof UpdateUserBotCommandsChanged) {
            UpdateUserBotCommandsChanged commandsChanged = (UpdateUserBotCommandsChanged) update;
            return onUserCommandsChanged(
                    commandsChanged.getUid(),
                    commandsChanged.getCommands());
        } else if (update instanceof UpdateUserBlocked) {
            UpdateUserBlocked updateUserBlocked = (UpdateUserBlocked) update;
            return onUserBlockedChanged(
                    updateUserBlocked.getUid(),
                    true);
        } else if (update instanceof UpdateUserUnblocked) {
            UpdateUserUnblocked unblocked = (UpdateUserUnblocked) update;
            return onUserBlockedChanged(
                    unblocked.getUid(),
                    false);
        }
        return Promise.success(null);
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof RouterUserUpdate) {
            if (isFreezed) {
                stash();
                return null;
            }

            RouterUserUpdate userUpdate = (RouterUserUpdate) message;
            return onUpdate(userUpdate.getUpdate());
        } else if (message instanceof RouterFetchMissingUsers) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterFetchMissingUsers fetchMissingUsers = (RouterFetchMissingUsers) message;
            return fetchMissingUsers(fetchMissingUsers.getSourcePeers());
        } else if (message instanceof RouterApplyUsers) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterApplyUsers applyUsers = (RouterApplyUsers) message;
            return applyUsers(applyUsers.getUsers());
        } else {
            return super.onAsk(message);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof RouterLoadFullUser) {
            if (isFreezed) {
                stash();
                return;
            }
            RouterLoadFullUser loadFullUser = (RouterLoadFullUser) message;
            onLoadFullUser(loadFullUser.getUid());
        } else {
            super.onReceive(message);
        }
    }
}