package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.auth.SignPhoneFragment;

public class AuthActivity extends BaseBarFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            showFragment(new SignPhoneFragment(), false, false);
        }
    }

    public void setBarTitle(int resId) {
        getSupportActionBar().setTitle(resId);
    }

    public void setBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}



