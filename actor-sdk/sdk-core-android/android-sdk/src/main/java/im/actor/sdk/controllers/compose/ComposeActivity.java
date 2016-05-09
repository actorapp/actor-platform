package im.actor.sdk.controllers.compose;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class ComposeActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.compose_title);
        }

        if (savedInstanceState == null) {
            showFragment(new ComposeFragment(), false, false);
        }
    }
}