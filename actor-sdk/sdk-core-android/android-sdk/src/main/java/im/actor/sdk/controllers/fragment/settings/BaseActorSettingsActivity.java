package im.actor.sdk.controllers.fragment.settings;

import android.app.Fragment;
import android.content.Intent;

import im.actor.sdk.intents.ActorIntentFragmentActivity;

public abstract class BaseActorSettingsActivity extends ActorIntentFragmentActivity {
    public BaseActorSettingsActivity(Intent intent) {
        super(intent);
    }

    public BaseActorSettingsActivity(Intent intent, Fragment fragment) {
        super(intent, fragment);
    }

    public IActorSettingsFragment getSettingsFragment() {
        return null;
    }

    ;
}
