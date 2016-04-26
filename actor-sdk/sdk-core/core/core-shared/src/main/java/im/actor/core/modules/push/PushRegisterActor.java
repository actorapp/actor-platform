/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.push;

import java.util.ArrayList;

import im.actor.core.api.rpc.RequestRegisterActorPush;
import im.actor.core.api.rpc.RequestRegisterApplePush;
import im.actor.core.api.rpc.RequestRegisterApplePushKit;
import im.actor.core.api.rpc.RequestRegisterGooglePush;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;

public class PushRegisterActor extends ModuleActor {

    // j2objc workaround
    private static final ResponseVoid DUMB = null;

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
        if (preferences().getBool("push.apple_pushkit", false)) {
            if (!preferences().getBool("push.apple_pushkit.registered", false)) {
                int apnsId = preferences().getInt("push.apple_pushkit.id", 0);
                String token = preferences().getString("push.apple_pushkit.token");
                registerApplePushKit(apnsId, token);
            }
        }
        if (preferences().getBool("push.actor", false)) {
            if (!preferences().getBool("push.actor.registered", false)) {
                String endpoint = preferences().getString("push.actor.endpoint");
                registerActorPush(endpoint);
            }
        }
    }

    private void registerGooglePush(long projectId, String token) {

        if (preferences().getBool("push.google", false)
                && preferences().getBool("push.google.registered", false)
                && token.equals(preferences().getString("push.google.token"))
                && projectId == preferences().getLong("push.apple.id", 0)) {
            return;
        }

        preferences().putBool("push.google", true);
        preferences().putBool("push.google.registered", false);
        preferences().putLong("push.google.id", projectId);
        preferences().putString("push.google.token", token);

        api(new RequestRegisterGooglePush(projectId, token))
                .then(r -> preferences().putBool("push.google.registered", true));
    }

    private void registerApplePush(int apnsId, String token) {

        if (preferences().getBool("push.apple", false)
                && preferences().getBool("push.apple.registered", false)
                && token.equals(preferences().getString("push.apple.token"))
                && apnsId == preferences().getInt("push.apple.id", 0)) {
            return;
        }

        preferences().putBool("push.apple", true);
        preferences().putBool("push.apple.registered", false);
        preferences().putInt("push.apple.id", apnsId);
        preferences().putString("push.apple.token", token);

        api(new RequestRegisterApplePush(apnsId, token))
                .then(r -> preferences().putBool("push.apple.registered", true));
    }

    private void registerApplePushKit(int apnsId, String token) {

        if (preferences().getBool("push.apple_puskkit", false)
                && preferences().getBool("push.apple_puskkit.registered", false)
                && token.equals(preferences().getString("push.apple_puskkit.token"))
                && apnsId == preferences().getInt("push.apple_puskkit.id", 0)) {
            return;
        }

        preferences().putBool("push.apple_puskkit", true);
        preferences().putBool("push.apple_puskkit.registered", false);
        preferences().putInt("push.apple_puskkit.id", apnsId);
        preferences().putString("push.apple_puskkit.token", token);

        api(new RequestRegisterApplePushKit(apnsId, token))
                .then(r -> preferences().putBool("push.apple_puskkit.registered", true));
    }

    private void registerActorPush(String endpoint) {

        if (preferences().getBool("push.actor", false)
                && preferences().getBool("push.actor.registered", false)
                && endpoint.equals(preferences().getString("push.actor.endpoint"))) {
            return;
        }

        preferences().putBool("push.actor", true);
        preferences().putBool("push.actor.registered", false);
        preferences().putString("push.actor.endpoint", endpoint);

        api(new RequestRegisterActorPush(endpoint, new ArrayList<>()))
                .then(r -> preferences().putBool("push.actor.registered", true));
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
        } else if (message instanceof RegisterActorPush) {
            RegisterActorPush actorPush = (RegisterActorPush) message;
            registerActorPush(actorPush.getEndpoint());
        } else if (message instanceof RegisterAppleVoipPush) {
            RegisterAppleVoipPush appleVoipPush = (RegisterAppleVoipPush) message;
            registerApplePushKit(appleVoipPush.getApnsKey(), appleVoipPush.getToken());
        } else {
            super.onReceive(message);
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

    public static class RegisterAppleVoipPush {
        private int apnsKey;
        private String token;

        public RegisterAppleVoipPush(int apnsKey, String token) {
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

    public static class RegisterActorPush {
        private String endpoint;

        public RegisterActorPush(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
