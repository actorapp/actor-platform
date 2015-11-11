package im.actor.sdk.intents;

import android.app.Fragment;
import android.content.Intent;

public class ActorIntentFragmentActivity extends ActorIntentActivity {
    Fragment fragment;

    public ActorIntentFragmentActivity(Intent intent) {
        super(intent);
    }

    public ActorIntentFragmentActivity(Intent intent, Fragment fragment) {
        super(intent);
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return fragment;
    }
}
