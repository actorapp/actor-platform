package im.actor.sdk.controllers.fragment.group;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class GroupInfoActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int chatId = getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle(null);

        if (savedInstanceState == null) {
            showFragment(GroupInfoFragment.create(chatId), false, false);
        }
    }
}
