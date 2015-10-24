package im.actor;

import java.util.ArrayList;

import im.actor.auth.SignEmailFragment;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.fragment.auth.BaseAuthFragment;

/**
 * Created by badgr on 16.10.2015.
 */
public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().createActor(this);
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {
        @Override
        public BaseAuthFragment getSignFragment() {
            return new SignEmailFragment();
        }
    }
}
