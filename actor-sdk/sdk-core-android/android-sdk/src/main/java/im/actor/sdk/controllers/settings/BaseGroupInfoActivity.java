package im.actor.sdk.controllers.settings;

import android.content.Intent;

import im.actor.sdk.controllers.group.GroupInfoFragment;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public abstract class BaseGroupInfoActivity extends ActorIntentFragmentActivity {
    public BaseGroupInfoActivity(Intent intent) {
        super(intent);
    }

    public BaseGroupInfoActivity(Intent intent, BaseActorSettingsFragment fragment) {
        super(intent, fragment);
    }

    public BaseGroupInfoActivity() {
        super();
    }

    public GroupInfoFragment getGroupInfoFragment(int chatId) {
        return GroupInfoFragment.create(chatId);
    }
}
