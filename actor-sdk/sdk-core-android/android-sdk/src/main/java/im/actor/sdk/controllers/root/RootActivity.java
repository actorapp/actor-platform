package im.actor.sdk.controllers.root;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

/**
 * Root Activity of Application
 */
public class RootActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        //
        // Configure Toolbar
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        setSupportActionBar(toolbar);
        if (ActorSDK.sharedActor().style.getToolBarColor() != 0) {
            toolbar.setBackgroundDrawable(new ColorDrawable(ActorSDK.sharedActor().style.getToolBarColor()));
        }

        if (savedInstanceState == null) {
            Fragment fragment = ActorSDK.sharedActor().getDelegate().fragmentForRoot();
            if (fragment == null) {
                fragment = new RootFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root, fragment)
                    .commit();
        }
    }
}
