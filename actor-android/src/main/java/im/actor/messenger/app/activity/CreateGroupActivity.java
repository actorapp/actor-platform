package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.compose.GroupNameFragment;

/**
 * Created by ex3ndr on 04.10.14.
 */
public class CreateGroupActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.create_group_title);
        showFragment(new GroupNameFragment(), false, false);
    }
}