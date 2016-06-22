package im.actor.sdk.controllers.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import im.actor.core.entity.Contact;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.base.ControllerActivity;
import im.actor.sdk.controllers.root.MainBaseController;
import im.actor.sdk.controllers.root.MainPhoneController;
import im.actor.core.entity.Dialog;
import im.actor.sdk.controllers.root.RootFragment;

/**
 * Root Activity of Application. Do not move unless home screen buttons will stop working.
 */
public class ActorMainActivity extends BaseFragmentActivity {

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
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root, new RootFragment())
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.root);
        if (fragment instanceof RootFragment) {
            ((RootFragment) fragment).onHandleIntent(intent);
        }
    }
}
