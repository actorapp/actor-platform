package im.actor.sdk.core;

import android.content.Context;

import im.actor.core.AndroidMessenger;
import im.actor.sdk.ActorSDK;

public abstract class ActorPushManager {
    public abstract String tryRegisterPush(Context context);

    public void registerOnActorServer(AndroidMessenger messenger, String token) {
        messenger.getPreferences().putBool("push_registered", true);
        messenger.registerGooglePush(ActorSDK.sharedActor().getPushId(), token);
    }


}



