package im.actor.sdk.controllers.group;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class AddMemberActivity extends BaseFragmentActivity {

    private int gid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.group_add_title);

        gid = getIntent().getIntExtra("GROUP_ID", 0);

        if (savedInstanceState == null) {
            showFragment(AddMemberFragment.create(gid), false, false);
        }
    }
}
