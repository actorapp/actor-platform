package im.actor;

import im.actor.core.AuthState;
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
        ActorSDK.sharedActor().createActor(this);
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {
        @Override
        public AuthState getAuthStartState() {
            return AuthState.AUTH_EMAIL;
        }
    }
}
