package im.actor.sdk.controllers.root;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.HTTP;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.tools.InviteHandler;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentActivity;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

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

        InviteHandler.handleIntent(this, getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        InviteHandler.handleIntent(this, intent);
    }

}
