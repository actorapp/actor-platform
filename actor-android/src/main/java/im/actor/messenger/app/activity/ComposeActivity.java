package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.compose.ComposeFragment;

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
