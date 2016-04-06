package im.actor.sdk.controllers.auth;

import im.actor.sdk.intents.ActorIntentFragmentActivity;

public class BaseAuthActivity extends ActorIntentFragmentActivity {

    public BaseAuthActivity() {
        super();
    }

    public BaseAuthFragment getAuthFragment() {
        return null;
    }

}
