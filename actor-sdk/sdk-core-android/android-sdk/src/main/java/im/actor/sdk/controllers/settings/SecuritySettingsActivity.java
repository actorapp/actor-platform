package im.actor.sdk.controllers.settings;

import android.os.Bundle;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public class SecuritySettingsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.security_title);

        SecuritySettingsFragment fragment;
        ActorIntentFragmentActivity securitySettingsIntent = ActorSDK.sharedActor().getDelegate().getSecuritySettingsIntent();
        if (securitySettingsIntent != null) {
            fragment = (SecuritySettingsFragment) securitySettingsIntent.getFragment();
        } else {
            fragment = new SecuritySettingsFragment();
        }

        if (savedInstanceState == null) {
            showFragment(fragment, false, false);
        }
    }
}
