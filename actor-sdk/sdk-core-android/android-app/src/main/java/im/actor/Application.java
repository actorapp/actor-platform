package im.actor;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.BaseActorSDKDelegate;

/**
 * Created by badgr on 16.10.2015.
 */
public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);
        ActorSDK.sharedActor().createActor(this);
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {

    }
}
