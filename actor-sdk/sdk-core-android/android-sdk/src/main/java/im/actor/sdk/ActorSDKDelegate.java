package im.actor.sdk;

import im.actor.sdk.intents.ActorIntent;

public interface ActorSDKDelegate {

    /**
     * Intent for first authentication Activity. For default activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getAuthStartIntent();

    /**
     * Intent for activity that is launched after user log in. For start activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getStartAfterLoginIntent();

    /**
     * Intent for application start activity for logged in user. For default activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getStartIntent();
}
