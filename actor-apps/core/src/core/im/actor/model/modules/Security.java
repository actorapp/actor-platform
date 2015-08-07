/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.List;

import im.actor.model.api.AuthSession;
import im.actor.model.api.rpc.RequestGetAuthSessions;
import im.actor.model.api.rpc.RequestTerminateAllSessions;
import im.actor.model.api.rpc.RequestTerminateSession;
import im.actor.model.api.rpc.ResponseGetAuthSessions;
import im.actor.model.api.rpc.ResponseVoid;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class Security extends BaseModule {
    public Security(Modules modules) {
        super(modules);
    }

    public Command<List<AuthSession>> loadSessions() {
        return new Command<List<AuthSession>>() {
            @Override
            public void start(final CommandCallback<List<AuthSession>> callback) {
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
