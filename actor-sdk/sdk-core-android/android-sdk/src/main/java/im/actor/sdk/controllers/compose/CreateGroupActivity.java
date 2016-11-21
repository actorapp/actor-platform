package im.actor.sdk.controllers.compose;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class CreateGroupActivity extends BaseFragmentActivity {

    public static String EXTRA_IS_CHANNEL = "is_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            showFragment(new GroupNameFragment(getIntent().getBooleanExtra(EXTRA_IS_CHANNEL, false)),
                    false);
        }
    }
}