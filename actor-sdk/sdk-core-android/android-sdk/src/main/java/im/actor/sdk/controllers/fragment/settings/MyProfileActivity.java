package im.actor.sdk.controllers.fragment.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class MyProfileActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle(null);

        if (savedInstanceState == null) {
            showFragment(new MyProfileFragment(), false, false);
        }
    }
}
