/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestEditAbout;
import im.actor.core.api.rpc.RequestEditName;
import im.actor.core.api.rpc.RequestEditNickName;
import im.actor.core.api.rpc.RequestEditUserLocalName;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.entity.User;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Storage;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.storage.KeyValueEngine;

public class UsersModule extends AbsModule {

    private KeyValueEngine<User> users;
    private MVVMCollection<User, UserVM> collection;

    public UsersModule(ModuleContext context) {
        super(context);

        this.collection = Storage.createKeyValue(STORAGE_USERS, UserVM.CREATOR(context()), User.CREATOR);
        this.users = collection.getEngine();
    }

    public KeyValueEngine<User> getUsersStorage() {
        return users;
    }

    public MVVMCollection<User, UserVM> getUsers() {
        return collection;
    }

    public Command<Boolean> editMyName(final String newName) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                request(new RequestEditName(newName), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        updates().onSeqUpdateReceived(
                                response.getSeq(),
                                response.getState(),
                                new UpdateUserNameChanged(
                                        myUid(),
                                        newName));
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
                User user = getUsersStorage().getValue(uid);
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

    public Command<Boolean> editNick(final String nick) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                request(new RequestEditNickName(nick), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        updates().onSeqUpdateReceived(response.getSeq(), response.getState(),
                                new UpdateUserNickChanged(myUid(), nick));
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

    public Command<Boolean> editAbout(final String about) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                request(new RequestEditAbout(about), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        updates().onSeqUpdateReceived(response.getSeq(), response.getState(),
                                new UpdateUserAboutChanged(myUid(), about));
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

    public void resetModule() {
        users.clear();
    }
}