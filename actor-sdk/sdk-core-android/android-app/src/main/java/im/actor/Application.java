package im.actor;

import java.util.ArrayList;

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
        //ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ArrayList<String> endpoints = new ArrayList<String>();
        endpoints.add("tls://front1-tcp.llectro.com");
        endpoints.add("tls://front1-tcp.llectro.com");
        ActorSDK.sharedActor().setEndpoints(endpoints);
        ActorSDK.sharedActor().createActor(this);
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {
        @Override
        public AuthState getAuthStartState() {
            return AuthState.AUTH_EMAIL;
        }
    }
}
