package im.actor.sdk.controllers.calls;

import android.os.Bundle;
import android.view.Menu;
import android.view.WindowManager;

import im.actor.core.entity.PeerType;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class CallActivity extends BaseFragmentActivity {

    private long callId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Call");

        if (savedInstanceState == null) {
            boolean incoming = getIntent().getBooleanExtra("incoming", false);
            if (incoming) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            }
            callId = getIntent().getLongExtra("callId", -1);
            showFragment(new CallFragment(callId, incoming), false, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating menu
        getMenuInflater().inflate(R.menu.call_menu, menu);
        if(messenger().getCall(callId).getPeer().getPeerType() != PeerType.GROUP){
            menu.findItem(R.id.members).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }
}
