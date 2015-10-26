package im.actor.sdk.core;

import android.content.Context;
import im.actor.core.AndroidMessenger;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;

/**
 * GCM push implementation for Actor push registration.
 * This actor is used for load GCM token and register it on server.
 */
public class AndroidPushActor extends Actor {

    private static final String TAG = "im.actor.sdk.core.AndroidPushActor";

    private final AndroidMessenger messenger;
    private final Context context;
    private boolean isRegistered;
    private ActorPushManager pushManager;

    public AndroidPushActor(Context context, AndroidMessenger messenger, ActorPushManager pushManager) {
        this.messenger = messenger;
        this.context = context;
        this.pushManager = pushManager;
    }

    @Override
    public void preStart() {
        super.preStart();

        isRegistered = messenger.getPreferences().getBool("push_registered", false);

        if (!isRegistered) {
            Log.d(TAG, "Requesting push token...");

            // TODO: Add backoff
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String regId = pushManager.tryRegisterPush(context);
                            if (regId != null) {
                                Log.d(TAG, "Token loaded");
                                self().send(new PushRegistered(regId));
                                return;
                            } else {
                                Log.d(TAG, "Unable to load Token");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "Waiting for next attempt");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                            return;
                        }
                    }
                }
            }.start();
        } else {
            Log.d(TAG, "Already registered token");
        }
    }

    private void onPushRegistered(String token) {
        isRegistered = true;
        pushManager.registerOnActorServer(messenger, token);

    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PushRegistered) {
            PushRegistered pushRegistered = (PushRegistered) message;
            onPushRegistered(pushRegistered.getToken());
        } else {
            drop(message);
        }
    }

    public static class PushRegistered {
        private String token;

        public PushRegistered(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
