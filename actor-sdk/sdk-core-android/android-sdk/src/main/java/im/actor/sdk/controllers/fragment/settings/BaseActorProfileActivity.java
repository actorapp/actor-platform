package im.actor.sdk.controllers.fragment.settings;

import android.content.Intent;

import im.actor.sdk.controllers.fragment.profile.ProfileFragment;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public abstract class BaseActorProfileActivity extends ActorIntentFragmentActivity {
    public BaseActorProfileActivity(Intent intent) {
        super(intent);
    }

    public BaseActorProfileActivity(Intent intent, BaseActorSettingsFragment fragment) {
        super(intent, fragment);
    }

    public BaseActorProfileActivity() {
        super();
    }

    public ProfileFragment getProfileFragment(int uid) {
        return ProfileFragment.create(uid);
    }
}
