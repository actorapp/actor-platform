package im.actor.sdk.controllers.share;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class ShareActivity extends BaseFragmentActivity {

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

        //
        // Fragment
        //
        if (savedInstanceState == null) {
            ShareFragment shareFragment = new ShareFragment();
            Bundle args;
            if (getIntent().getExtras() != null) {
                args = getIntent().getExtras();
            } else {
                args = new Bundle();
            }
            args.putString(ShareFragment.ARG_INTENT_TYPE, getIntent().getType());
            args.putString(ShareFragment.ARG_INTENT_ACTION, getIntent().getAction());
            shareFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root, shareFragment)
                    .commit();
        }
    }
}
