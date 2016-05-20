/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.external;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import im.actor.core.api.ApiMapValue;
import im.actor.core.api.ApiMapValueItem;
import im.actor.core.api.ApiRawValue;
import im.actor.core.api.rpc.RequestCompleteWebaction;
import im.actor.core.api.rpc.RequestInitWebaction;
import im.actor.core.api.rpc.RequestRawRequest;
import im.actor.core.api.rpc.ResponseCompleteWebaction;
import im.actor.core.api.rpc.ResponseInitWebaction;
import im.actor.core.api.rpc.ResponseRawRequest;
import im.actor.core.entity.WebActionDescriptor;
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
        return callback -> request(request, new RpcCallback<T>() {
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

    public Command<WebActionDescriptor> startWebAction(final String webAction) {
        return callback -> request(new RequestInitWebaction(webAction, new ApiMapValue(new ArrayList<>())),
                new RpcCallback<ResponseInitWebaction>() {
                    @Override
                    public void onResult(ResponseInitWebaction response) {
                        callback.onResult(
                                new WebActionDescriptor(
                                        response.getUri(),
                                        response.getRegexp(),
                                        response.getActionHash()));
                    }

                    @Override
                    public void onError(RpcException e) {
                        callback.onError(e);
                    }
                });
    }

    public Command<Boolean> completeWebAction(final String actionHash, final String url) {
        return callback -> request(new RequestCompleteWebaction(actionHash, url), new RpcCallback<ResponseCompleteWebaction>() {
            @Override
            public void onResult(ResponseCompleteWebaction response) {
                callback.onResult(true);
            }

            @Override
            public void onError(RpcException e) {
                callback.onError(e);
            }
        });
    }

    public Command<ResponseRawRequest> rawRequestCommand(final String service, final String method, final ApiRawValue params) {
        return callback -> request(new RequestRawRequest(service, method, params), new RpcCallback<ResponseRawRequest>() {
            @Override
            public void onResult(ResponseRawRequest response) {
                callback.onResult(response);
            }

            @Override
            public void onError(RpcException e) {
                callback.onError(e);
            }
        });
    }

    public void rawPersistentRequest(String service, String method, ApiRawValue params) {
        context().getApiModule().performPersistRequest(new RequestRawRequest(service, method, params));
    }

    public void rawRequest(String service, String method, ApiRawValue params) {
        request(new RequestRawRequest(service, method, params));
    }
}