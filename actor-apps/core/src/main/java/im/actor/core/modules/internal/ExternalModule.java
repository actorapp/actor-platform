/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import org.jetbrains.annotations.NotNull;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;

public class ExternalModule extends AbsModule {

    public ExternalModule(ModuleContext context) {
        super(context);
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
