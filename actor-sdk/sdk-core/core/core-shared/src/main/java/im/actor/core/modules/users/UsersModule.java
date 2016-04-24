/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.users;

import im.actor.core.api.rpc.RequestEditAbout;
import im.actor.core.api.rpc.RequestEditName;
import im.actor.core.api.rpc.RequestEditNickName;
import im.actor.core.api.rpc.RequestEditUserLocalName;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.KeyValueEngine;

public class UsersModule extends AbsModule {

    private KeyValueEngine<User> users;
    private MVVMCollection<User, UserVM> collection;

    public UsersModule(final ModuleContext context) {
        super(context);

        this.collection = Storage.createKeyValue(STORAGE_USERS, UserVM.CREATOR(context()), User.CREATOR);
        this.users = collection.getEngine();
    }

    // Model

    public KeyValueEngine<User> getUsersStorage() {
        return users;
    }

    public MVVMCollection<User, UserVM> getUsers() {
        return collection;
    }

    public Promise<Long> getUserAccessHash(int uid) {
        return getUsersStorage().getValueAsync(uid).map(User::getAccessHash);
    }

    // Actions

    public Promise<Void> editName(final int uid, final String name) {
        return getUserAccessHash(uid)
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

    public void resetModule() {
        users.clear();
    }
}