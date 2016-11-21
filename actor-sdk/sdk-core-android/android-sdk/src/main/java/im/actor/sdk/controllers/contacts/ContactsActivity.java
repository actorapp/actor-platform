package im.actor.sdk.controllers.contacts;

import android.os.Bundle;
import android.support.annotation.Nullable;

import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class ContactsActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            showFragment(new ContactsFragment(), false);
        }
    }
}
