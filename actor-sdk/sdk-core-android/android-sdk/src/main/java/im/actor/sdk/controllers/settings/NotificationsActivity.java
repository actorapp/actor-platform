package im.actor.sdk.controllers.settings;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class NotificationsActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.not_title);

        if (savedInstanceState == null) {
            showFragment(new NotificationsFragment(), false, false);
        }
    }
}
