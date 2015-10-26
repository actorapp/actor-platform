package im.actor;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import im.actor.runtime.Log;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.core.ActorPushManager;

public class PushManager extends ActorPushManager {
    @Override
    public String tryRegisterPush(Context context) {
        GoogleCloudMessaging cloudMessaging = GoogleCloudMessaging.getInstance(context);
        Log.d("Actor GCM", "Requesting push token iteration...");
        try {
            return cloudMessaging.register("" + ActorSDK.sharedActor().getPushId());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
