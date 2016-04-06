package im.actor.sdk.controllers.compose;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

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