package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.fragment.settings.SecuritySettingsFragment;

/**
 * Created by ex3ndr on 09.10.14.
 */
public class SecuritySettingsActivity extends BaseBarFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setTitle(R.string.security_title);

        showFragment(new SecuritySettingsFragment(), false, false);
    }
}
