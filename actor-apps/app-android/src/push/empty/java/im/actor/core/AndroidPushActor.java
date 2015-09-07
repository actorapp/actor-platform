package im.actor.core;

import android.content.Context;

import im.actor.core.modules.Modules;
import im.actor.runtime.actors.Actor;

/**
 * this actor is used for load push token and register it on server
 *
 * we are not using pushes for free version of Actor, that's why it's empty
 */
public class AndroidPushActor extends Actor {


    public AndroidPushActor(Context context, AndroidMessenger messenger) {
    }


}
