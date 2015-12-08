/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.push;

import im.actor.core.api.rpc.RequestRegisterApplePush;
import im.actor.core.api.rpc.RequestRegisterGooglePush;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;

public class PushRegisterActor extends ModuleActor {

    public PushRegisterActor(ModuleContext modules) {
        super(modules);
    }

    @Override
    public void preStart() {
        if (preferences().getBool("push.google", false)) {
            if (!preferences().getBool("push.google.registered", false)) {
                long projectId = preferences().getLong("push.google.id", 0);
                String token = preferences().getString("push.google.token");
                registerGooglePush(projectId, token);
            }
        }
        if (preferences().getBool("push.apple", false)) {
            if (!preferences().getBool("push.apple.registered", false)) {
                int apnsId = preferences().getInt("push.apple.id", 0);
                String token = preferences().getString("push.apple.token");
                registerApplePush(apnsId, token);
            }
        }
    }

    private void registerGooglePush(long projectId, String token) {
        preferences().putBool("push.google", true);
        preferences().putBool("push.google.registered", false);
        preferences().putLong("push.google.id", projectId);
        preferences().putString("push.google.token", token);

        request(new RequestRegisterGooglePush(projectId, token), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                preferences().putBool("push.google.registered", true);
            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    private void registerApplePush(int apnsId, String token) {
        preferences().putBool("push.apple", true);
        preferences().putBool("push.apple.registered", false);
        preferences().putInt("push.apple.id", apnsId);
        preferences().putString("push.apple.token", token);

        request(new RequestRegisterApplePush(apnsId, token), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                preferences().putBool("push.apple.registered", true);
            }

            @Override
            public void onError(RpcException e) {

            }
        });
    }

    private void resendPush() {
        if (preferences().getBool("push.google", false)) {
            preferences().putBool("push.google.registered", false);

            long projectId = preferences().getLong("push.google.id", 0);
            String token = preferences().getString("push.google.token");
            registerGooglePush(projectId, token);
        }
        if (preferences().getBool("push.apple", false)) {
            preferences().putBool("push.apple.registered", false);

            int apnsId = preferences().getInt("push.apple.id", 0);
            String token = preferences().getString("push.apple.token");
            registerApplePush(apnsId, token);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof RegisterGooglePush) {
            RegisterGooglePush registerGooglePush = (RegisterGooglePush) message;
            registerGooglePush(registerGooglePush.getProjectId(),
                    registerGooglePush.getToken());
        } else if (message instanceof RegisterApplePush) {
            RegisterApplePush applePush = (RegisterApplePush) message;
            registerApplePush(applePush.getApnsKey(), applePush.getToken());
        } else if (message instanceof ResendPush) {
            resendPush();
        } else {
            drop(message);
        }
    }

    public static class RegisterGooglePush {
        private long projectId;
        private String token;

        public RegisterGooglePush(long projectId, String token) {
            this.projectId = projectId;
            this.token = token;
        }

        public long getProjectId() {
            return projectId;
        }

        public String getToken() {
            return token;
        }
    }

    public static class RegisterApplePush {
        private int apnsKey;
        private String token;

        public RegisterApplePush(int apnsKey, String token) {
            this.apnsKey = apnsKey;
            this.token = token;
        }

        public int getApnsKey() {
            return apnsKey;
        }

        public String getToken() {
            return token;
        }
    }

    public static class ResendPush {

    }
}
