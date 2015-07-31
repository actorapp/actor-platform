package im.actor.messenger.app.fragment.group;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.BaseFragmentActivity;

/**
 * Created by ex3ndr on 08.10.14.
 */
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
