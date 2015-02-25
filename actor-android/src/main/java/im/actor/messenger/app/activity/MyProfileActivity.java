package im.actor.messenger.app.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.settings.MyProfileFragment;

/**
 * Created by ex3ndr on 27.12.14.
 */
public class MyProfileActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (savedInstanceState == null) {
            showFragment(new MyProfileFragment(), false, false);
        }
    }
}
