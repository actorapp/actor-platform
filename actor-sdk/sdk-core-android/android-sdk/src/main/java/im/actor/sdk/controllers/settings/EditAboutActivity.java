package im.actor.sdk.controllers.settings;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class EditAboutActivity extends BaseFragmentActivity {

    public static final int TYPE_ME = 0;
    public static final int TYPE_GROUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int type = getIntent().getIntExtra(Intents.EXTRA_EDIT_TYPE, 0);
        int id = getIntent().getIntExtra(Intents.EXTRA_EDIT_ID, 0);

        if (type == TYPE_ME) {
            getSupportActionBar().setTitle(R.string.about_user_me);
        } else if (type == TYPE_GROUP) {
            getSupportActionBar().setTitle(R.string.about_group);
        }

        if (savedInstanceState == null) {
            showFragment(EditAboutFragment.editAbout(type, id), false, false);
        }
    }
}
