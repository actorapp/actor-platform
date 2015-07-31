package im.actor.messenger.app.fragment.group;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.BaseFragmentActivity;

/**
 * Created by korka on 25.05.15.
 */
public class JoinPublicGroupActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.join_public_group_title);


        if (savedInstanceState == null) {
            showFragment(new JoinPublicGroupFragment(), false, false);
        }
    }

}
