package im.actor.sdk.intents;

import android.content.Intent;

public class ActorIntentFragmentActivity extends ActorIntentActivity {
    android.support.v4.app.Fragment fragment;

    public ActorIntentFragmentActivity(Intent intent) {
        super(intent);
    }

    public ActorIntentFragmentActivity(Intent intent, android.support.v4.app.Fragment fragment) {
        super(intent);
        this.fragment = fragment;
    }

    public ActorIntentFragmentActivity() {
        super(null);
    }

    public android.support.v4.app.Fragment getFragment() {
        return fragment;
    }
}
