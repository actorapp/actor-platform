package im.actor.sdk.controllers.group;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.settings.BaseGroupInfoActivity;

public class GroupInfoActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int chatId = getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle(null);

        if (savedInstanceState == null) {
            GroupInfoFragment fragment;
            BaseGroupInfoActivity profileIntent = ActorSDK.sharedActor().getDelegate().getGroupInfoIntent(chatId);
            if (profileIntent != null) {
                fragment = profileIntent.getGroupInfoFragment(chatId);
            } else {
                fragment = GroupInfoFragment.create(chatId);
            }

            showFragment(fragment, false, false);
        }
    }
}
