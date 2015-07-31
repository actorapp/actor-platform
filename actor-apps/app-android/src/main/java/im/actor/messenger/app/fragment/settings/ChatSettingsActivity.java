package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.BaseFragmentActivity;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class ChatSettingsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.settings_chat_title);

        if (savedInstanceState == null) {
            showFragment(new ChatSettingsFragment(), false, false);
        }
    }
}
