package im.actor.sdk.discover;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.group.AddMemberFragment;

public class DiscoverActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Discover");


        if (savedInstanceState == null) {
            showFragment(new StoreFragment(), false, false);
        }
    }
}
