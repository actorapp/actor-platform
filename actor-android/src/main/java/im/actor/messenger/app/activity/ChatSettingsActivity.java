package im.actor.messenger.app.activity;

import android.os.Bundle;
import android.view.MenuItem;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.settings.ChatSettingsFragment;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class ChatSettingsActivity extends BaseBarFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setTitle(R.string.settings_chat_title);

        showFragment(new ChatSettingsFragment(), false, false);
    }
}
