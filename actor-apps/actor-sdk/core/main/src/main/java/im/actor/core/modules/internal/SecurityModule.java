/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.util.List;

import im.actor.core.api.ApiAuthSession;
import im.actor.core.api.rpc.RequestGetAuthSessions;
import im.actor.core.api.rpc.RequestTerminateAllSessions;
import im.actor.core.api.rpc.RequestTerminateSession;
import im.actor.core.api.rpc.ResponseGetAuthSessions;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;

public class SecurityModule extends AbsModule {

    public SecurityModule(ModuleContext context) {
        super(context);
    }

    public Command<List<ApiAuthSession>> loadSessions() {
        return new Command<List<ApiAuthSession>>() {
            @Override
            public void start(final CommandCallback<List<ApiAuthSession>> callback) {
                request(new RequestGetAuthSessions(), new RpcCallback<ResponseGetAuthSessions>() {
                    @Override
                    public void onResult(final ResponseGetAuthSessions response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(response.getUserAuths());
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

    public Command<Boolean> terminateAllSessions() {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                request(new RequestTerminateAllSessions(), new RpcCallback<ResponseVoid>() {
                    @Override
                    public void onResult(ResponseVoid response) {
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

    public Command<Boolean> terminateSession(final int id) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                request(new RequestTerminateSession(id), new RpcCallback<ResponseVoid>() {
                    @Override
                    public void onResult(ResponseVoid response) {
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
}
