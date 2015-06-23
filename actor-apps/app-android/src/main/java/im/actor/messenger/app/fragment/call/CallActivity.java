/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.fragment.call;

import android.os.Bundle;

import im.actor.messenger.app.base.BaseFragmentActivity;

public class CallActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            showFragment(new CallFragment(), false, false);
        }
    }
}
