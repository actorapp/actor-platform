package im.actor.sdk.controllers.fragment.settings;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

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
