package im.actor.sdk.controllers.compose;

import android.os.Bundle;

import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class ComposeActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            showFragment(new ComposeFragment(), false);
        }
    }
}