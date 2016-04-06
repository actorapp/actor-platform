package im.actor.sdk.controllers.group;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class InviteLinkActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.invite_link_title);

        int chatId = getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0);

        if (savedInstanceState == null) {
            showFragment(InviteLinkFragment.create(chatId), false, false);
        }
    }

}
