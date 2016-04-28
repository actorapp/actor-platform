package im.actor.core.modules.users.router;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiAvatar;
import im.actor.core.api.ApiUser;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestLoadFullUsers;
import im.actor.core.api.rpc.ResponseLoadFullUsers;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.User;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.contacts.ContactsSyncActor;
import im.actor.core.modules.users.router.entity.RouterAboutChanged;
import im.actor.core.modules.users.router.entity.RouterApplyUsers;
import im.actor.core.modules.users.router.entity.RouterAvatarChanged;
import im.actor.core.modules.users.router.entity.RouterFetchMissingUsers;
import im.actor.core.modules.users.router.entity.RouterLoadFullUser;
import im.actor.core.modules.users.router.entity.RouterLocalNameChanged;
import im.actor.core.modules.users.router.entity.RouterNameChanged;
import im.actor.core.modules.users.router.entity.RouterNicknameChanged;
import im.actor.core.modules.users.router.entity.RouterUserRegistered;
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
    private Promise<Void> onUserAboutChanged(int uid, String about) {
        freeze();
        return users().getValueAsync(uid)
                .fallback(u -> null)
                .flatMap(u -> {
                    // Ignore if name not changed
                    if (u != null && !equalsE(u.getAbout(), about)) {

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
    public Promise<Void> onUserRegistered(long rid, int uid, long date) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message(rid, date, date, uid, MessageState.UNKNOWN, ServiceUserRegistered.create()));
        context().getMessagesModule().getRouter().onNewMessages(Peer.user(uid), messages);
        return Promise.success((Void) null);
    }

    @Verified
    private Promise<Void> onUserDescChanged(User u) {
        context().getMessagesModule().getRouter().onUserChanged(u);
        context().getContactsModule().getContactSyncActor()
                .send(new ContactsSyncActor.UserChanged(u));
        return Promise.success((Void) null);
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
                .map(u -> users().containsAsync(u.getUid())
                        .map(v -> v ? null : u))
                .filterNull()
                .zip()
                .after((r, e) -> unfreeze());
    }


    @Verified
    private Promise<Void> applyUsers(List<ApiUser> users) {
        freeze();
        return PromisesArray.of(users)
                .map(u -> users().containsAsync(u.getId())
                        .map(v -> new Tuple2<ApiUser, Boolean>(u, v)))
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

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof RouterNameChanged) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterNameChanged nameChanged = (RouterNameChanged) message;
            return onUserNameChanged(nameChanged.getUid(), nameChanged.getName());
        } else if (message instanceof RouterLocalNameChanged) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterLocalNameChanged localNameChanged = (RouterLocalNameChanged) message;
            return onUserLocalNameChanged(localNameChanged.getUid(), localNameChanged.getLocalName());
        } else if (message instanceof RouterAvatarChanged) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterAvatarChanged avatarChanged = (RouterAvatarChanged) message;
            return onUserAvatarChanged(avatarChanged.getUid(), avatarChanged.getAvatar());
        } else if (message instanceof RouterNicknameChanged) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterNicknameChanged nicknameChanged = (RouterNicknameChanged) message;
            return onUserNickChanged(nicknameChanged.getUid(), nicknameChanged.getNickname());
        } else if (message instanceof RouterAboutChanged) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterAboutChanged aboutChanged = (RouterAboutChanged) message;
            return onUserAboutChanged(aboutChanged.getUid(), aboutChanged.getAbout());
        } else if (message instanceof RouterUserRegistered) {
            if (isFreezed) {
                stash();
                return null;
            }
            RouterUserRegistered userRegistered = (RouterUserRegistered) message;
            return onUserRegistered(userRegistered.getRid(), userRegistered.getUid(), userRegistered.getDate());
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