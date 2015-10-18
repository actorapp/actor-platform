package im.actor;

import im.actor.sdk.ActorSDK;

/**
 * Created by badgr on 16.10.2015.
 */
public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ActorSDK.sharedActor().createActor(this);
    }
}
