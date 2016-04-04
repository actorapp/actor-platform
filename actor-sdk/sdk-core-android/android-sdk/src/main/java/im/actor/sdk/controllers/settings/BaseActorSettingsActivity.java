package im.actor.sdk.controllers.settings;

import android.content.Intent;

import im.actor.sdk.intents.ActorIntentFragmentActivity;

public abstract class BaseActorSettingsActivity extends ActorIntentFragmentActivity {
    public BaseActorSettingsActivity(Intent intent) {
        super(intent);
    }

    public BaseActorSettingsActivity(Intent intent, BaseActorSettingsFragment fragment) {
        super(intent, fragment);
    }

    public BaseActorSettingsActivity() {
        super();
    }

    public BaseActorSettingsFragment getSettingsFragment() {
        return null;
    }
}
