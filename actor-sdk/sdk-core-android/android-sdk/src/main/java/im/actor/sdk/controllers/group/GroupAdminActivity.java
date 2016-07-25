package im.actor.sdk.controllers.group;

import android.os.Bundle;

import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class GroupAdminActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            showFragment(GroupAdminFragment.create(
                    getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0)), false);
        }
    }
}
