package im.actor.sdk;

import android.app.Application;

public class ActorSDKApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        onConfigureActorSDK();

        ActorSDK.sharedActor().createActor(this);
    }

    /**
     * Override this method for implementing Actor SDK Implementation
     */
    public void onConfigureActorSDK() {

    }
}