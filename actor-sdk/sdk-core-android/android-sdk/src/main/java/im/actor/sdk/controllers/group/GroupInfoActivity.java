package im.actor.sdk.controllers.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class GroupInfoActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            int groupId = getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0);
            Fragment profileIntent = ActorSDK.sharedActor().getDelegate().fragmentForGroupInfo(groupId);
            if (profileIntent == null) {
                profileIntent = GroupInfoFragment.create(groupId);
            }
            showFragment(profileIntent, false);
        }
    }
}
