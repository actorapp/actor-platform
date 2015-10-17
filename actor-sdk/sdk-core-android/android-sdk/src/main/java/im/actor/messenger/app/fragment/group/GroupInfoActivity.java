package im.actor.messenger.app.fragment.group;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.BaseFragmentActivity;

/**
 * Created by ex3ndr on 07.10.14.
 */
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
