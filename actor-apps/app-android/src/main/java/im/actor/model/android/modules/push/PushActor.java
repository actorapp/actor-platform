package im.actor.model.android.modules.push;

import android.content.Context;

//import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import im.actor.android.AndroidMixpanelAnalytics;
import im.actor.model.droidkit.actors.Actor;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;

/**
 * Created by ex3ndr on 02.04.15.
 */
// TODO: Add processing of token die
public class PushActor extends Actor {

    private static final String TAG = "PushActor";
    private static final long PROJECT_ID = 209133700967L;

    private final Modules messenger;
    private final Context context;
    private boolean isRegistered;

    public PushActor(Context context, Modules messenger) {
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
           /* new Thread() {
                @Override
                public void run() {
                    while (true) {
                        GoogleCloudMessaging cloudMessaging = GoogleCloudMessaging.getInstance(context);
                        try {
                            Log.d(TAG, "Requesting push token iteration...");
                            String regId = cloudMessaging.register("" + PROJECT_ID);
                            if (regId != null) {
                                Log.d(TAG, "Token loaded");
                                self().send(new PushRegistered(regId));
                                if (AndroidMixpanelAnalytics.getRegisteredApi() != null) {
                                    AndroidMixpanelAnalytics.getRegisteredApi()
                                            .getPeople().setPushRegistrationId(regId);
                                }
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
            }.start();*/
        } else {
            Log.d(TAG, "Already registered token");
        }
    }

    private void onPushRegistered(String token) {
        isRegistered = true;
        messenger.getPreferences().putBool("push_registered", true);
        messenger.getPushes().registerGooglePush(PROJECT_ID, token);
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
