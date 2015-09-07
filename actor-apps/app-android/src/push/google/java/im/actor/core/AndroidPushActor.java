package im.actor.core;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import im.actor.core.modules.Modules;
import im.actor.messenger.app.core.Core;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;

/**
 * GCM push implementation for Actor push registration.
 * This actor is used for load GCM token and register it on server.
 */
public class AndroidPushActor extends Actor {

    private static final String TAG = "im.actor.core.AndroidPushActor";

    private final AndroidMessenger messenger;
    private final Context context;
    private boolean isRegistered;

    public AndroidPushActor(Context context, AndroidMessenger messenger) {
        this.messenger = messenger;
        this.context = context;
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
                        GoogleCloudMessaging cloudMessaging = GoogleCloudMessaging.getInstance(context);
                        try {
                            Log.d(TAG, "Requesting push token iteration...");
                            String regId = cloudMessaging.register("" + Core.PUSH_ID);
                            if (regId != null) {
                                Log.d(TAG, "Token loaded");
                                self().send(new PushRegistered(regId));
                                return;
                            } else {
                                Log.d(TAG, "Unable to load Token");
                            }
                        } catch (IOException e) {
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
        messenger.getPreferences().putBool("push_registered", true);
        messenger.registerGooglePush(Core.PUSH_ID, token);
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
