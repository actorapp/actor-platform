/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import org.jetbrains.annotations.NotNull;

import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;

public class External extends BaseModule {

    public External(Modules modules) {
        super(modules);
    }

    @NotNull
    public <T extends Response> Command<T> externalMethod(@NotNull final Request<T> request) {
        return new Command<T>() {
            @Override
            public void start(final CommandCallback<T> callback) {
                request(request, new RpcCallback<T>() {
                    @Override
                    public void onResult(T response) {
                        callback.onResult(response);
                    }

                    @Override
                    public void onError(RpcException e) {
                        callback.onError(e);
                    }
                });
            }
        };
    }
}
