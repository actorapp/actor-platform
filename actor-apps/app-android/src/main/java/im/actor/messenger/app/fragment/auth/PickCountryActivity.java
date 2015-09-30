package im.actor.messenger.app.fragment.auth;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.BaseFragmentActivity;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by ex3ndr on 12.01.15.
 */
public class PickCountryActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.auth_phone_country_title);

        if (savedInstanceState == null) {
            showFragment(new PickCountryFragment(), false, false);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }
}
