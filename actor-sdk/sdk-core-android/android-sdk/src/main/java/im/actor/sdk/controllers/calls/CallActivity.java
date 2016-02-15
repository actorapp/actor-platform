package im.actor.sdk.controllers.calls;

import android.os.Bundle;
import android.view.WindowManager;

import im.actor.sdk.controllers.activity.BaseFragmentActivity;

/**
 * Created by ex3ndr on 30.09.14.
 */
public class CallActivity extends BaseFragmentActivity {
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
            showFragment(new CallFragment(getIntent().getLongExtra("callId", -1), incoming), false, false);
        }
    }

}
