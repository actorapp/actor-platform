package im.actor.sdk.controllers.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.intents.ActorIntent;

public class MyProfileActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle(null);

        if (savedInstanceState == null) {

            BaseActorSettingsFragment fragment;
            if (ActorSDK.sharedActor().getDelegate().getSettingsIntent() != null) {
                ActorIntent settingsIntent = ActorSDK.sharedActor().getDelegate().getSettingsIntent();
                if (settingsIntent instanceof BaseActorSettingsActivity) {
                    fragment = ((BaseActorSettingsActivity) settingsIntent).getSettingsFragment();
                } else {
                    fragment = new ActorSettingsFragment();
                }
            } else {
                fragment = new ActorSettingsFragment();
            }

            showFragment(fragment, false, false);
        }
    }
}
