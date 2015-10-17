package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.BaseFragmentActivity;

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
