package im.actor.sdk;

import im.actor.core.AuthState;
import im.actor.sdk.intents.ActorIntent;

public class BaseActorSDKDelegate implements ActorSDKDelegate {
    
    @Override
    public ActorIntent getAuthStartIntent() {
        return null;
    }

    @Override
    public ActorIntent getStartAfterLoginIntent() {
        return null;
    }

    @Override
    public ActorIntent getStartIntent() {
        return null;
    }

    @Override
    public AuthState getAuthStartState() {
        return AuthState.AUTH_PHONE;
    }
}
