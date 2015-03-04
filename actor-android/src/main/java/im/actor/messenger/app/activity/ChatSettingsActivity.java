package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.settings.ChatSettingsFragment;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class ChatSettingsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.settings_chat_title);

        showFragment(new ChatSettingsFragment(), false, false);
    }
}
