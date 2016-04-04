package im.actor.sdk.controllers.settings;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.Intents;

public class EditNameActivity extends BaseFragmentActivity {

    public static final int TYPE_ME = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_GROUP = 2;
    public static final int TYPE_GROUP_THEME = 3;
    public static final int TYPE_NICK = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int type = getIntent().getIntExtra(Intents.EXTRA_EDIT_TYPE, 0);
        int id = getIntent().getIntExtra(Intents.EXTRA_EDIT_ID, 0);

        if (type == TYPE_ME) {
            getSupportActionBar().setTitle(R.string.edit_name_title_you);
        } else if (type == TYPE_USER) {
            getSupportActionBar().setTitle(R.string.edit_name_title_contact);
        } else if (type == TYPE_GROUP) {
            getSupportActionBar().setTitle(R.string.edit_name_title_group);
        } else if (type == TYPE_GROUP_THEME) {
            getSupportActionBar().setTitle(R.string.edit_name_title_group_theme);
        } else if (type == TYPE_NICK) {
            getSupportActionBar().setTitle(R.string.edit_name_title_nick);
        }

        if (savedInstanceState == null) {
            showFragment(EditNameFragment.editName(type, id), false, false);
        }
    }
}
