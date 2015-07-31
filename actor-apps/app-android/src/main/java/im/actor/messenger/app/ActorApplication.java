package im.actor.messenger.app;

import android.app.Application;

import im.actor.messenger.app.core.Core;

public class ActorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Core.init(this);
    }
}