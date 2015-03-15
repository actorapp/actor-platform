package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.contacts.PickUserFragment;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class PickUserActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.pick_user_title);

        showFragment(new PickUserFragment(), false, false);
    }
}
