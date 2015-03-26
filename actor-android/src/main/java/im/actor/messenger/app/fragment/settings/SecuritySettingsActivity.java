package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.settings.SecuritySettingsFragment;

/**
 * Created by ex3ndr on 09.10.14.
 */
public class SecuritySettingsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.security_title);

        showFragment(new SecuritySettingsFragment(), false, false);
    }
}
