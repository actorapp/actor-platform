package im.actor.sdk.controllers.fragment.settings;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class SecuritySettingsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.security_title);

        if (savedInstanceState == null) {
            showFragment(new SecuritySettingsFragment(), false, false);
        }
    }
}
