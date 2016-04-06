package im.actor.sdk.controllers.compose;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class ComposeActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.compose_title);

        showFragment(new ComposeFragment(), false, false);
    }
}
