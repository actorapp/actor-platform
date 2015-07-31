package im.actor.messenger.app.fragment.help;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.BaseFragmentActivity;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class HelpActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.help_title);

        showFragment(new HelpFragment(), false, false);
    }
}
