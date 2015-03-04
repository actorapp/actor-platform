package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.settings.NotificationSettingsFragment;

public class NotificationsActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.not_title);

        if (savedInstanceState == null) {
            showFragment(new NotificationSettingsFragment(), false, false);
        }
    }
}
