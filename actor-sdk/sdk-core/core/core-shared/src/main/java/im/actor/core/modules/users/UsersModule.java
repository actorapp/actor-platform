/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.users;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestBlockUser;
import im.actor.core.api.rpc.RequestEditAbout;
import im.actor.core.api.rpc.RequestEditName;
import im.actor.core.api.rpc.RequestEditNickName;
import im.actor.core.api.rpc.RequestEditUserLocalName;
import im.actor.core.api.rpc.RequestLoadBlockedUsers;
import im.actor.core.api.rpc.RequestUnblockUser;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserBlocked;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.api.updates.UpdateUserUnblocked;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.events.PeerChatOpened;
import im.actor.core.events.PeerInfoOpened;
import im.actor.core.events.UserVisible;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.users.router.UserRouter;
import im.actor.core.modules.users.router.UserRouterInt;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.storage.KeyValueEngine;

import static im.actor.runtime.actors.ActorSystem.system;

public class UsersModule extends AbsModule implements BusSubscriber {

    private UserRouterInt userRouter;
    private KeyValueEngine<User> users;
    private MVVMCollection<User, UserVM> collection;

    public UsersModule(ModuleContext context) {
        super(context);

        this.collection = Storage.createKeyValue(STORAGE_USERS, UserVM.CREATOR(context()), User.CREATOR);
        this.users = collection.getEngine();

        this.userRouter = new UserRouterInt(context);
    }

    public void run() {
        context().getEvents().subscribe(this, PeerChatOpened.EVENT);
        context().getEvents().subscribe(this, UserVisible.EVENT);
        context().getEvents().subscribe(this, PeerInfoOpened.EVENT);
    }

    // Model

    public KeyValueEngine<User> getUsersStorage() {
        return users;
    }

    public MVVMCollection<User, UserVM> getUsers() {
        return collection;
    }

    public UserRouterInt getUserRouter() {
        return userRouter;
    }

    // Actions

    public Promise<Void> editName(final int uid, final String name) {
        return getUsersStorage()
                .getValueAsync(uid)
                .map(User::getAccessHash)
                .flatMap(aLong -> api(new RequestEditUserLocalName(uid, aLong, name)))
                .flatMap(responseSeq -> updates().applyUpdate(
                        responseSeq.getSeq(),
                        responseSeq.getState(),
                        new UpdateUserLocalNameChanged(uid, name))
                );
    }

    public Promise<Void> editMyName(final String newName) {
        return api(new RequestEditName(newName))
                .flatMap(responseSeq -> updates().applyUpdate(
                        responseSeq.getSeq(),
                        responseSeq.getState(),
                        new UpdateUserNameChanged(myUid(), newName))
                );
    }

    public Promise<Void> editNick(final String nick) {
        return api(new RequestEditNickName(nick))
                .flatMap(responseSeq -> updates().applyUpdate(
                        responseSeq.getSeq(),
                        responseSeq.getState(),
                        new UpdateUserNickChanged(myUid(), nick))
                );
    }

    public Promise<Void> editAbout(final String about) {
        return api(new RequestEditAbout(about))
                .flatMap(responseSeq -> updates().applyUpdate(
                        responseSeq.getSeq(),
                        responseSeq.getState(),
                        new UpdateUserAboutChanged(myUid(), about))
                );
    }

    public Promise<List<User>> loadBlockedUsers() {
        return api(new RequestLoadBlockedUsers())
                .chain(response -> updates().loadRequiredPeers(response.getUserPeers(), new ArrayList<>()))
                .flatMap(responseLoadBlockedUsers ->
                        PromisesArray.of(responseLoadBlockedUsers.getUserPeers())
                                .map(apiUserOutPeer -> users().getValueAsync(apiUserOutPeer.getUid()))
                                .zip());
    }

    public Promise<Void> blockUser(final int uid) {
        return buildOutPeer(Peer.user(uid))
                .flatMap(apiOutPeer ->
                        api(new RequestBlockUser(new ApiUserOutPeer(apiOutPeer.getId(), apiOutPeer.getAccessHash()))))
                .flatMap(responseSeq ->
                        updates().applyUpdate(
                                responseSeq.getSeq(),
                                responseSeq.getState(),
                                new UpdateUserBlocked(uid)));
    }

    public Promise<Void> unblockUser(final int uid) {
        return buildOutPeer(Peer.user(uid))
                .flatMap(apiOutPeer ->
                        api(new RequestUnblockUser(new ApiUserOutPeer(apiOutPeer.getId(), apiOutPeer.getAccessHash()))))
                .flatMap(responseSeq ->
                        updates().applyUpdate(
                                responseSeq.getSeq(),
                                responseSeq.getState(),
                                new UpdateUserUnblocked(uid)));
    }

    public void resetModule() {
        users.clear();
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof PeerChatOpened) {
            Peer peer = ((PeerChatOpened) event).getPeer();
            if (peer.getPeerType() == PeerType.PRIVATE) {
                getUserRouter().onFullUserNeeded(peer.getPeerId());
            }
        } else if (event instanceof UserVisible) {
            getUserRouter().onFullUserNeeded(((UserVisible) event).getUid());
        } else if (event instanceof PeerInfoOpened) {
            Peer peer = ((PeerInfoOpened) event).getPeer();
            if (peer.getPeerType() == PeerType.PRIVATE) {
                getUserRouter().onFullUserNeeded(peer.getPeerId());
            }
        }
    }
}