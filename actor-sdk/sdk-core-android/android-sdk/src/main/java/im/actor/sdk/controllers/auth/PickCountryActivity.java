package im.actor.sdk.controllers.auth;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class PickCountryActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.auth_phone_country_title);

        if (savedInstanceState == null) {
            showFragment(new PickCountryFragment(), false, false);
        }
    }
}
