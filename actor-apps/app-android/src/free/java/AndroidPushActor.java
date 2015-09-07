import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import im.actor.android.AndroidMixpanelAnalytics;
import im.actor.model.droidkit.actors.Actor;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;

public class AndroidPushActor extends Actor {


    public PushActor(Context context, Modules messenger) {
        this.messenger = messenger;
        this.context = context;
    }

    @Override
    public void preStart() {
        super.preStart();
    }

    private void onPushRegistered(String token) {
    }

    @Override
    public void onReceive(Object message) {
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
