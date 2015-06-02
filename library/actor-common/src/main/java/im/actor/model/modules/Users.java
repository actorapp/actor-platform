/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.io.IOException;

import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestEditName;
import im.actor.model.api.rpc.RequestEditUserLocalName;
import im.actor.model.api.rpc.ResponseSeq;
import im.actor.model.api.updates.UpdateUserLocalNameChanged;
import im.actor.model.api.updates.UpdateUserNameChanged;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.User;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.RpcInternalException;
import im.actor.model.droidkit.engine.KeyValueEngine;
import im.actor.model.viewmodel.UserVM;

public class Users extends BaseModule {
    private KeyValueEngine<User> users;
    private MVVMCollection<User, UserVM> collection;

    public Users(Modules messenger) {
        super(messenger);
        this.collection = new MVVMCollection<User, UserVM>(messenger.getConfiguration().getStorageProvider().createKeyValue(STORAGE_USERS)) {
            @Override
            protected UserVM createNew(User raw) {
                return new UserVM(raw, modules());
            }

            @Override
            protected byte[] serialize(User raw) {
                return raw.toByteArray();
            }

            @Override
            protected User deserialize(byte[] raw) {
                try {
                    return new User(raw);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        this.users = collection.getEngine();
    }

    public MVVMCollection<User, UserVM> getUsersCollection() {
        return collection;
    }

    public KeyValueEngine<User> getUsers() {
        return users;
    }

    public Command<Boolean> editMyName(final String newName) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                request(new RequestEditName(newName), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateUserNameChanged.HEADER, new UpdateUserNameChanged(myUid(),
                                newName).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Boolean> editName(final int uid, final String name) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                User user = getUsers().getValue(uid);
                if (user == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestEditUserLocalName(
                        user.getUid(), user.getAccessHash(), name), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        SeqUpdate update = new SeqUpdate(response.getSeq(), response.getState(),
                                UpdateUserLocalNameChanged.HEADER, new UpdateUserLocalNameChanged(uid,
                                name).toByteArray());
                        updates().onUpdateReceived(update);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                    }
                });
            }
        };
    }

    public void resetModule() {
        users.clear();
    }
}