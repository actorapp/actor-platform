package im.actor.sdk.controllers.group;

import android.os.Bundle;

import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class GroupInfoEditActivity extends BaseFragmentActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            showFragment(new GroupInfoEditFragment(), false);
        }
    }
}
