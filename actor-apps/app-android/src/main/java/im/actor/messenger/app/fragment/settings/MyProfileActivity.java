package im.actor.messenger.app.fragment.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import im.actor.messenger.app.activity.BaseFragmentActivity;

/**
 * Created by ex3ndr on 27.12.14.
 */
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
