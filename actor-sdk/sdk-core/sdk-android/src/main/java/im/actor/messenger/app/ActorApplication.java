package im.actor.messenger.app;

import android.app.Application;

import im.actor.messenger.app.core.ActorSDK;

public class ActorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActorSDK.sharedActor().createActor(this);
        ActorSDK.sharedActor().startMessagingApp();
    }
}